package com.ds.tech.subscribe.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ds.tech.subscribe.config.ProxyConfig;
import com.ds.tech.subscribe.entity.Clash;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RestController
public class IndexController {
    private static final int THREAD_NUM = 24;
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(THREAD_NUM * 3));

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource(name = "clashTemplate")
    private Clash clashTemplate;
    @Value("${PROXY_CONFIG:{}}")
    private String proxyConfig;
    @Value("${WARP_DOMAIN:warp.grf.cloudns.org}")
    private String warpDomain;
    @Value("${WARP_SECRET:xiaobaihe}")
    private String warpSecret;
    private JSONObject proxyJson;

    @PostConstruct
    private void init() {
        proxyJson = JSON.parseObject(proxyConfig.trim());
        System.out.println(proxyJson);
    }

    @GetMapping("/subscribe")
    public Clash subscribe() throws InterruptedException {
        clashTemplate.reset();
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
        proxyJson.forEach((key, value) -> {
            JSONArray jsonArray = (JSONArray) value;
            switch (key) {
                case "clashmeta":
                    jsonArray.forEach(urls -> clashmeta(urls, countDownLatch));
                    break;
                case "xray":
                    jsonArray.forEach(urls -> xray(urls, countDownLatch));
                    break;
                case "hysteria":
                    jsonArray.forEach(urls -> hysteria(urls, countDownLatch));
                    break;
                case "singbox":
                    jsonArray.forEach(urls -> singbox(urls, countDownLatch));
                    break;
                case "hysteria2":
                    jsonArray.forEach(urls -> hysteria2(urls, countDownLatch));
                    break;
            }
        });
        countDownLatch.await();
        List<Map<String, Object>> proxies = clashTemplate.getProxies();
        proxies.sort((o1, o2) -> {
            int result = o1.get("server").toString().compareTo(o2.get("server").toString());
            return result == 0 ? Integer.parseInt(o1.get("port").toString()) - Integer.parseInt(o2.get("port").toString()) : result;
        });
        int index = 0;
        for (Map<String, Object> proxy : proxies) {
            ++index;
            String proxyName = "IP_" + index;
            proxy.put("name", proxyName);
        }
        clashTemplate.groupPadding();
        return clashTemplate;
    }

    @GetMapping("/warp")
    public Clash warp(HttpServletResponse response) {
        String httpHeader = "subscription-userinfo";
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("https://" + warpDomain + "/api/clash?best=true&randomName=false&proxyFormat=only_proxies&ipv6=false&key=" + warpSecret, String.class);
        Clash clash;
        try {
            clash = objectMapper.readValue(responseEntity.getBody(), Clash.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<String> userInfo = responseEntity.getHeaders().get(httpHeader);
        if (userInfo != null) {
            userInfo.forEach(s -> response.addHeader(httpHeader, s));
        }
        clashTemplate.reset();
        clashTemplate.setProxies(clash.getProxies());
        List<String> proxyNames = clash.getProxies().stream().map(proxy -> proxy.get("name").toString()).toList();
        clashTemplate.getProxyGroups().forEach(proxyGroup -> proxyGroup.getProxies().addAll(proxyNames));
        return clashTemplate;
    }

    @GetMapping
    public String index() {
        return "Hello World!";
    }

    private void request(Object o, CountDownLatch countDownLatch, Consumer<String> consumer) {
        if (!(o instanceof JSONArray urls)) {
            return;
        }
        if (CollectionUtils.isEmpty(urls)) {
            return;
        }
        threadPoolExecutor.execute(() -> {
            try {
                String s = restTemplate.getForObject(urls.getString(0), String.class);
                if (s != null) {
                    consumer.accept(s);
                    return;
                }
                if (urls.size() < 2) {
                    return;
                }
                s = restTemplate.getForObject(urls.getString(1), String.class);
                if (s != null) {
                    consumer.accept(s);
                }
            } catch (Exception ignored) {
            } finally {
                countDownLatch.countDown();
            }
        });
    }

    private void clashmeta(Object urls, CountDownLatch countDownLatch) {
        request(urls, countDownLatch, s -> {
            try {
                Clash clash = objectMapper.readValue(s, Clash.class);
                clashTemplate.getProxies().addAll(clash.getProxies());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void xray(Object urls, CountDownLatch countDownLatch) {
        request(urls, countDownLatch, s -> {
            JSONObject jsonObject = JSONObject.parseObject(s);
            JSONArray proxies = jsonObject.getJSONArray("outbounds");
            for (int i = 0; i < proxies.size(); ++i) {
                JSONObject proxy = proxies.getJSONObject(i);
                String protocol = proxy.getString("protocol");
                if ("vless".equals(protocol)) {
                    JSONArray vnexts = proxy.getJSONObject("settings").getJSONArray("vnext");
                    JSONObject streamSettings = proxy.getJSONObject("streamSettings");
                    JSONObject tlsSettings = streamSettings.getJSONObject("tlsSettings");
                    for (int j = 0; j < vnexts.size(); ++j) {
                        JSONObject vnext = vnexts.getJSONObject(j);
                        Map<String, Object> clashProxy = new HashMap<>();
                        clashProxy.put("type", "vless");
                        clashProxy.put("server", vnext.get("address"));
                        clashProxy.put("port", vnext.get("port"));
                        clashProxy.put("uuid", vnext.getJSONArray("users").getJSONObject(0).get("id"));
                        clashProxy.put("udp", true);
                        clashProxy.put("tls", true);
                        clashProxy.put("network", streamSettings.get("network"));
                        clashProxy.put("servername", tlsSettings.get("serverName"));
                        clashProxy.put("skip-cert-verify", tlsSettings.get("allowInsecure"));
                        clashProxy.put("fingerprint", tlsSettings.get("fingerprint"));
                        clashProxy.put("ws-opts", streamSettings.get("wsSettings"));
                        clashTemplate.getProxies().add(clashProxy);
                    }
                } else if ("shadowsocks".equals(protocol)) {
                    JSONArray servers = proxy.getJSONObject("settings").getJSONArray("servers");
                    for (int j = 0; j < servers.size(); ++j) {
                        JSONObject server = servers.getJSONObject(j);
                        Map<String, Object> clashProxy = new HashMap<>();
                        clashProxy.put("type", "ss");
                        clashProxy.put("server", server.get("address"));
                        clashProxy.put("port", server.get("port"));
                        clashProxy.put("cipher", server.get("method"));
                        clashProxy.put("password", server.get("password"));
                        clashTemplate.getProxies().add(clashProxy);
                    }
                }
            }
        });
    }

    private void hysteria(Object urls, CountDownLatch countDownLatch) {
        request(urls, countDownLatch, s -> {
            JSONObject proxy = JSONObject.parseObject(s);
            String[] server = proxy.getString("server").split(":");
            Map<String, Object> clashProxy = new HashMap<>();
            clashProxy.put("type", "hysteria");
            clashProxy.put("server", server[0]);
            clashProxy.put("port", Integer.parseInt(server[1]));
            clashProxy.put("auth-str", proxy.get("auth_str"));
            clashProxy.put("sni", proxy.get("server_name"));
            clashProxy.put("skip-cert-verify", proxy.get("disable_mtu_discovery"));
            clashProxy.put("protocol", proxy.get("protocol"));
            clashProxy.put("up", proxy.get("up_mbps"));
            clashProxy.put("down", proxy.get("down_mbps"));
            clashProxy.put("alpn", Collections.singletonList(proxy.get("alpn")));
            clashProxy.put("recv-window-conn", proxy.get("recv_window_conn"));
            clashProxy.put("recv-window", proxy.get("recv_window"));
            clashTemplate.getProxies().add(clashProxy);
        });
    }

    private void singbox(Object urls, CountDownLatch countDownLatch) {
        request(urls, countDownLatch, s -> {
            JSONObject jsonObject = JSONObject.parseObject(s);
            JSONArray outbounds = jsonObject.getJSONArray("outbounds");
            for (int i = 0; i < outbounds.size(); ++i) {
                JSONObject proxy = outbounds.getJSONObject(i);
                Map<String, Object> clashProxy = new HashMap<>();
                clashProxy.put("type", proxy.get("type"));
                clashProxy.put("server", proxy.get("server"));
                clashProxy.put("port", proxy.get("server_port"));
                clashProxy.put("auth-str", proxy.get("auth_str"));
                JSONObject tls = proxy.getJSONObject("tls");
                clashProxy.put("sni", tls.get("server_name"));
                clashProxy.put("alpn", tls.get("alpn"));
                clashProxy.put("up", proxy.get("up_mbps"));
                clashProxy.put("down", proxy.get("down_mbps"));
                clashProxy.put("uuid", proxy.get("uuid"));
                clashProxy.put("password", proxy.get("password"));
                clashProxy.put("congestion-control", proxy.get("congestion_control"));
                clashProxy.put("udp-relay-mode", proxy.get("udp_relay_mode"));
                clashProxy.put("zero-rtt-handshake", proxy.get("zero_rtt_handshake"));
                clashTemplate.getProxies().add(clashProxy);
            }
        });
    }

    private void hysteria2(Object urls, CountDownLatch countDownLatch) {
        request(urls, countDownLatch, s -> {
            JSONObject proxy = JSONObject.parseObject(s);
            String[] server = proxy.getString("server").split(":");
            Map<String, Object> clashProxy = new HashMap<>();
            clashProxy.put("type", "hysteria2");
            clashProxy.put("server", server[0]);
            clashProxy.put("port", Integer.parseInt(server[1]));
            clashProxy.put("password", proxy.get("auth"));
            JSONObject tls = proxy.getJSONObject("tls");
            clashProxy.put("sni", tls.get("sni"));
            clashProxy.put("skip-cert-verify", tls.get("insecure"));
            clashTemplate.getProxies().add(clashProxy);
        });
    }
}
