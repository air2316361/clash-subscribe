package com.ds.tech.subscribe.config;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ds.tech.subscribe.entity.Clash;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Function;

@AllArgsConstructor
public enum Client {
    clashmeta(resp -> {
        try {
            return ObjectMapperHolder.getObjectMapper().readValue(resp, Clash.class).getProxies();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }),
    Xray(resp -> {
        List<Map<String, Object>> result = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(resp);
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
                    result.add(clashProxy);
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
                    result.add(clashProxy);
                }
            }
        }
        return result;
    }),
    hysteria(resp -> {
        JSONObject proxy = JSONObject.parseObject(resp);
        String[] server = proxy.getString("server").split(":");
        Map<String, Object> clashProxy = new HashMap<>();
        clashProxy.put("type", "hysteria");
        clashProxy.put("server", server[0]);
        String[] portSplit = server[1].split(",");
        clashProxy.put("port", portSplit[0]);
        clashProxy.put("ports", portSplit[1]);
        clashProxy.put("auth-str", proxy.get("auth_str"));
        clashProxy.put("sni", proxy.get("server_name"));
        clashProxy.put("skip-cert-verify", proxy.get("disable_mtu_discovery"));
        clashProxy.put("protocol", proxy.get("protocol"));
        clashProxy.put("up", proxy.get("up_mbps"));
        clashProxy.put("down", proxy.get("down_mbps"));
        clashProxy.put("alpn", Collections.singletonList(proxy.get("alpn")));
        clashProxy.put("recv-window-conn", proxy.get("recv_window_conn"));
        clashProxy.put("recv-window", proxy.get("recv_window"));
        return Collections.singletonList(clashProxy);
    }),
    hysteria2(resp -> {
        JSONObject proxy = JSONObject.parseObject(resp);
        String[] server = proxy.getString("server").split(":");
        Map<String, Object> clashProxy = new HashMap<>();
        clashProxy.put("type", "hysteria2");
        clashProxy.put("server", server[0]);
        String[] portSplit = server[1].split(",");
        clashProxy.put("port", portSplit[0]);
        clashProxy.put("ports", portSplit[1]);
        clashProxy.put("password", proxy.get("auth"));
        JSONObject tls = proxy.getJSONObject("tls");
        clashProxy.put("sni", tls.get("sni"));
        clashProxy.put("skip-cert-verify", tls.get("insecure"));
        return Collections.singletonList(clashProxy);
    }),
    singbox(resp -> {
        List<Map<String, Object>> proxies = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(resp);
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
            proxies.add(clashProxy);
        }
        return proxies;
    });
//    mieru(resp -> {
//        List<Map<String, Object>> proxies = new ArrayList<>();
//        JSONObject jsonObject = JSONObject.parseObject(resp);
//        JSONArray profiles = jsonObject.getJSONArray("profiles");
//        for (int i = 0; i < profiles.size(); ++i) {
//            JSONObject profile = profiles.getJSONObject(i);
//            JSONObject user = profile.getJSONObject("user");
//            JSONArray servers = profile.getJSONArray("servers");
//            for (int j = 0; j < servers.size(); ++j) {
//                JSONObject server = servers.getJSONObject(j);
//                Map<String, Object> clashProxy = new HashMap<>();
//                clashProxy.put("type", "mieru");
//                clashProxy.put("server", server.get("ipAddress"));
//                clashProxy.put("port", server.getJSONArray("portBindings").getJSONObject(0).get("port"));
//                clashProxy.put("username", user.get("name"));
//                clashProxy.put("password", user.get("password"));
//                proxies.add(clashProxy);
//            }
//        }
//        return proxies;
//    }),
//    juicity(resp -> {
//        List<Map<String, Object>> proxies = new ArrayList<>();
//        JSONObject jsonObject = JSONObject.parseObject(resp);
//        Map<String, Object> clashProxy = new HashMap<>();
//        String[] server = jsonObject.getString("server").split(":");
//        clashProxy.put("type", "juicity");
//        clashProxy.put("server", server[0]);
//        clashProxy.put("port", server[1]);
//        clashProxy.put("uuid", jsonObject.get("uuid"));
//        clashProxy.put("password", jsonObject.get("password"));
//        clashProxy.put("sni", jsonObject.get("sni"));
//        clashProxy.put("skip-cert-verify", jsonObject.get("allow_insecure"));
//        clashProxy.put("congestion-control", jsonObject.get("congestion_control"));
//        proxies.add(clashProxy);
//        return proxies;
//    }),
//    naiveproxy(resp -> {
//        JSONObject jsonObject = JSONObject.parseObject(resp);
//        Map<String, Object> clashProxy = new HashMap<>();
//        String proxy = jsonObject.getString("proxy");
//        String[] split = proxy.split("@");
//        String[] user = split[0].split(":");
//        String[] server = split[1].split(":");
//        clashProxy.put("type", "naiveproxy");
//        clashProxy.put("server", server[0]);
//        clashProxy.put("port", server[1]);
//        clashProxy.put("username", user[1].substring(2));
//        clashProxy.put("password", user[2]);
//        return Collections.singletonList(clashProxy);
//    });

    public final Function<String, List<Map<String, Object>>> function;
}
