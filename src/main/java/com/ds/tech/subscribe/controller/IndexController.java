package com.ds.tech.subscribe.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ds.tech.subscribe.entity.Clash;
import com.ds.tech.subscribe.config.ProxyConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
public class IndexController {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource(name = "clashTemplate")
    private Clash clashTemplate;
    @Resource
    private ProxyConfig proxyConfig;
    @Value("${WARP_DOMAIN:grf.cloudns.org}")
    private String warpDomain;

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(24, 24, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(96));

    @GetMapping
    public Clash index() throws InterruptedException {
        clashTemplate.reset();
        CountDownLatch countDownLatch = new CountDownLatch(24);
        clashmeta(proxyConfig.getClashmeta1(), proxyConfig.getClashmeta1s(), countDownLatch);
        clashmeta(proxyConfig.getClashmeta2(), proxyConfig.getClashmeta2s(), countDownLatch);
        clashmeta(proxyConfig.getClashmeta3(), proxyConfig.getClashmeta3s(), countDownLatch);
        clashmeta(proxyConfig.getClashmeta4(), proxyConfig.getClashmeta4s(), countDownLatch);
        clashmeta(proxyConfig.getClashmeta5(), proxyConfig.getClashmeta5s(), countDownLatch);
        clashmeta(proxyConfig.getClashmeta6(), proxyConfig.getClashmeta6s(), countDownLatch);
        xray(proxyConfig.getXray1(), proxyConfig.getXray1s(), countDownLatch);
        xray(proxyConfig.getXray2(), proxyConfig.getXray2s(), countDownLatch);
        xray(proxyConfig.getXray3(), proxyConfig.getXray3s(), countDownLatch);
        xray(proxyConfig.getXray4(), proxyConfig.getXray4s(), countDownLatch);
        hysteria(proxyConfig.getHysteria1(), proxyConfig.getHysteria1s(), countDownLatch);
        hysteria(proxyConfig.getHysteria2(), proxyConfig.getHysteria2s(), countDownLatch);
        hysteria(proxyConfig.getHysteria3(), proxyConfig.getHysteria3s(), countDownLatch);
        hysteria(proxyConfig.getHysteria4(), proxyConfig.getHysteria4s(), countDownLatch);
        singbox(proxyConfig.getSingbox1(), proxyConfig.getSingbox1s(), countDownLatch);
        singbox(proxyConfig.getSingbox2(), proxyConfig.getSingbox2s(), countDownLatch);
        hysteria2(proxyConfig.getHysteria21(), proxyConfig.getHysteria21s(), countDownLatch);
        hysteria2(proxyConfig.getHysteria22(), proxyConfig.getHysteria22s(), countDownLatch);
        hysteria2(proxyConfig.getHysteria23(), proxyConfig.getHysteria23s(), countDownLatch);
        hysteria2(proxyConfig.getHysteria24(), proxyConfig.getHysteria24s(), countDownLatch);
        clashmeta(proxyConfig.getQuick1(), proxyConfig.getQuick1s(), countDownLatch);
        clashmeta(proxyConfig.getQuick2(), proxyConfig.getQuick2s(), countDownLatch);
        clashmeta(proxyConfig.getQuick3(), proxyConfig.getQuick3s(), countDownLatch);
        clashmeta(proxyConfig.getQuick4(), proxyConfig.getQuick4s(), countDownLatch);
        countDownLatch.await();
        List<Map<String, Object>> proxies = clashTemplate.getProxies();
        List<String> proxyNames = new ArrayList<>(proxies.size());
        proxies.sort((o1, o2) -> {
            int result = o1.get("server").toString().compareTo(o2.get("server").toString());
            return result == 0 ? Integer.parseInt(o1.get("port").toString()) - Integer.parseInt(o2.get("port").toString()) : result;
        });
        int index = 0;
        for (Map<String, Object> proxy : proxies) {
            ++index;
            String proxyName = "IP_" + index;
            proxy.put("name", proxyName);
            proxyNames.add(proxyName);
        }
        clashTemplate.getProxyGroups().forEach(proxyGroup -> proxyGroup.getProxies().addAll(proxyNames));
        return clashTemplate;
    }

    @GetMapping("/warp")
    public Clash warp() {
        String s = restTemplate.getForObject("https://" + warpDomain + "/api/clash?best=true&randomName=false&proxyFormat=only_proxies&ipv6=false&key=xiaobaihe", String.class);
        Clash clash;
        try {
            clash = objectMapper.readValue(s, Clash.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        clashTemplate.reset();
        clashTemplate.setProxies(clash.getProxies());
        List<String> proxyNames = clash.getProxies().stream().map(proxy -> proxy.get("name").toString()).toList();
        clashTemplate.getProxyGroups().forEach(proxyGroup -> proxyGroup.getProxies().addAll(proxyNames));
        return clashTemplate;
    }

    @GetMapping("/test")
    public String test() {
        return "Hello World!";
    }

    private void request(String url, String spareUrl, CountDownLatch countDownLatch, Consumer<String> consumer) {
        threadPoolExecutor.execute(() -> {
            boolean retry = true;
            try {
                String s = restTemplate.getForObject(url, String.class);
                if (s != null) {
                    consumer.accept(s);
                    retry = false;
                }
            } catch (Exception ignored) {
            }
            if (retry) {
                try {
                    String s = restTemplate.getForObject(spareUrl, String.class);
                    if (s != null) {
                        consumer.accept(s);
                    }
                } catch (Exception ignored) {
                }
            }
            countDownLatch.countDown();
        });
    }

    private void clashmeta(String url, String spareUrl, CountDownLatch countDownLatch) {
        request(url, spareUrl, countDownLatch, s -> {
            try {
                Clash clash = objectMapper.readValue(s, Clash.class);
                clashTemplate.getProxies().addAll(clash.getProxies());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void xray(String url, String spareUrl, CountDownLatch countDownLatch) {
        request(url, spareUrl, countDownLatch, s -> {
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

    private void hysteria(String url, String spareUrl, CountDownLatch countDownLatch) {
        request(url, spareUrl, countDownLatch, s -> {
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

    private void singbox(String url, String spareUrl, CountDownLatch countDownLatch) {
        request(url, spareUrl, countDownLatch, s -> {
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

    private void hysteria2(String url, String spareUrl, CountDownLatch countDownLatch) {
        request(url, spareUrl, countDownLatch, s -> {
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
