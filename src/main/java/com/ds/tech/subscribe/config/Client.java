package com.ds.tech.subscribe.config;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ds.tech.subscribe.entity.Clash;
import com.ds.tech.subscribe.schedule.WebsiteSchedule;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Function;

@AllArgsConstructor
public enum Client {
    clashmeta(resp -> {
        try {
            int index = WebsiteSchedule.indexMap.computeIfAbsent("Clash.Meta", s -> 1);
            Clash clash = ObjectMapperHolder.getObjectMapper().readValue(resp, Clash.class);
            for (Iterator<Map<String, Object>> iterator = clash.getProxies().iterator(); iterator.hasNext(); ) {
                Map<String, Object> proxy = iterator.next();
                String key = proxy.get("server") + "|" + proxy.get("type");
                if (WebsiteSchedule.proxySet.contains(key)) {
                    iterator.remove();
                    continue;
                }
                proxy.put("name", "Clash.Meta_" + index);
                WebsiteSchedule.proxySet.add(key);
                ++index;
            }
            WebsiteSchedule.indexMap.put("Clash.Meta", index);
            return clash.getProxies();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }),
    Xray(resp -> {
        List<Map<String, Object>> result = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(resp);
        JSONArray proxies = jsonObject.getJSONArray("outbounds");
        int index = WebsiteSchedule.indexMap.computeIfAbsent("Xray", s -> 1);
        for (int i = 0; i < proxies.size(); ++i) {
            JSONObject proxy = proxies.getJSONObject(i);
            if (!"proxy".equals(proxy.get("tag"))) {
                continue;
            }
            String protocol = proxy.getString("protocol");
            JSONArray vnexts = proxy.getJSONObject("settings").getJSONArray("vnext");
            JSONObject streamSettings = proxy.getJSONObject("streamSettings");
            JSONObject realitySettings = streamSettings.getJSONObject("realitySettings");
            for (int j = 0; j < vnexts.size(); ++j) {
                JSONObject vnext = vnexts.getJSONObject(j);
                Object address = vnext.get("address");
                String key = address + "|" + protocol;
                if (WebsiteSchedule.proxySet.contains(key)) {
                    continue;
                }
                JSONObject user = vnext.getJSONArray("users").getJSONObject(0);
                Map<String, Object> clashProxy = new HashMap<>();
                clashProxy.put("name", "Xray_" + index);
                clashProxy.put("type", protocol);
                clashProxy.put("server", address);
                clashProxy.put("port", vnext.get("port"));
                clashProxy.put("uuid", user.get("id"));
                clashProxy.put("network", streamSettings.get("network"));
                clashProxy.put("tls", true);
                clashProxy.put("flow", user.get("flow"));
                clashProxy.put("servername", realitySettings.get("serverName"));
                JSONObject realityOpts = new JSONObject(2);
                realityOpts.put("public-key", realitySettings.get("publicKey"));
                realityOpts.put("short-id", realitySettings.get("shortId"));
                clashProxy.put("reality-opts", realityOpts);
                clashProxy.put("client-fingerprint", realitySettings.get("fingerprint"));
                result.add(clashProxy);
                WebsiteSchedule.proxySet.add(key);
                ++index;
            }
        }
        WebsiteSchedule.indexMap.put("Xray", index);
        return result;
    }),
    hysteria(resp -> {
        int index = WebsiteSchedule.indexMap.computeIfAbsent("Hysteria", s -> 1);
        JSONObject proxy = JSONObject.parseObject(resp);
        String[] server = proxy.getString("server").split(":");
        String address = server[0];
        String key = address + "|hysteria";
        if (WebsiteSchedule.proxySet.contains(key)) {
            return Collections.emptyList();
        }
        Map<String, Object> clashProxy = new HashMap<>();
        clashProxy.put("name", "Hysteria_" + index);
        clashProxy.put("type", "hysteria");
        clashProxy.put("server", address);
        String[] portSplit = server[1].split(",");
        clashProxy.put("port", portSplit[0]);
        if (portSplit.length > 1) {
            clashProxy.put("ports", portSplit[1]);
        }
        clashProxy.put("auth-str", proxy.get("auth_str"));
        clashProxy.put("sni", proxy.get("server_name"));
        clashProxy.put("skip-cert-verify", proxy.get("disable_mtu_discovery"));
        clashProxy.put("protocol", proxy.get("protocol"));
        clashProxy.put("alpn", Collections.singletonList(proxy.get("alpn")));
        clashProxy.put("recv-window-conn", proxy.get("recv_window_conn"));
        clashProxy.put("recv-window", proxy.get("recv_window"));
        WebsiteSchedule.indexMap.put("Hysteria", ++index);
        WebsiteSchedule.proxySet.add(key);
        return Collections.singletonList(clashProxy);
    }),
    hysteria2(resp -> {
        int index = WebsiteSchedule.indexMap.computeIfAbsent("Hysteria2", s -> 1);
        JSONObject proxy = JSONObject.parseObject(resp);
        String[] server = proxy.getString("server").split(":");
        String address = server[0];
        String key = address + "|hysteria2";
        if (WebsiteSchedule.proxySet.contains(key)) {
            return Collections.emptyList();
        }
        Map<String, Object> clashProxy = new HashMap<>();
        clashProxy.put("name", "Hysteria2_" + index);
        clashProxy.put("type", "hysteria2");
        clashProxy.put("server", server[0]);
        String[] portSplit = server[1].split(",");
        clashProxy.put("port", portSplit[0]);
        if (portSplit.length > 1) {
            clashProxy.put("ports", portSplit[1]);
        }
        clashProxy.put("password", proxy.get("auth"));
        JSONObject tls = proxy.getJSONObject("tls");
        clashProxy.put("sni", tls.get("sni"));
        clashProxy.put("skip-cert-verify", tls.get("insecure"));
        WebsiteSchedule.indexMap.put("Hysteria2", ++index);
        WebsiteSchedule.proxySet.add(key);
        return Collections.singletonList(clashProxy);
    }),
    singbox(resp -> {
        int index = WebsiteSchedule.indexMap.computeIfAbsent("Sing-Box", s -> 1);
        List<Map<String, Object>> proxies = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(resp);
        JSONArray outbounds = jsonObject.getJSONArray("outbounds");
        for (int i = 0; i < outbounds.size(); ++i) {
            JSONObject proxy = outbounds.getJSONObject(i);
            Object server = proxy.get("server");
            Object type = proxy.get("type");
            String key = server + "|" + type;
            if (WebsiteSchedule.proxySet.contains(key)) {
                continue;
            }
            Map<String, Object> clashProxy = new HashMap<>();
            clashProxy.put("name", "Sing-Box_" + index);
            clashProxy.put("type", type);
            clashProxy.put("server", server);
            clashProxy.put("port", proxy.get("server_port"));
            clashProxy.put("auth-str", proxy.get("auth_str"));
            clashProxy.put("obfs", proxy.get("obfs"));
            JSONObject tls = proxy.getJSONObject("tls");
            clashProxy.put("sni", tls.get("server_name"));
            clashProxy.put("alpn", tls.get("alpn"));
            clashProxy.put("skip-cert-verify", true);
            ++index;
            proxies.add(clashProxy);
            WebsiteSchedule.proxySet.add(key);
        }
        WebsiteSchedule.indexMap.put("Sing-Box", index);
        return proxies;
    }),
    mieru(resp -> {
        int index = WebsiteSchedule.indexMap.computeIfAbsent("Mieru", s -> 1);
        List<Map<String, Object>> proxies = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(resp);
        JSONArray profiles = jsonObject.getJSONArray("profiles");
        for (int i = 0; i < profiles.size(); ++i) {
            JSONObject profile = profiles.getJSONObject(i);
            JSONObject user = profile.getJSONObject("user");
            JSONArray servers = profile.getJSONArray("servers");
            for (int j = 0; j < servers.size(); ++j) {
                JSONObject server = servers.getJSONObject(j);
                Object address = server.get("ipAddress");
                String key = address + "|mieru";
                if (WebsiteSchedule.proxySet.contains(key)) {
                    continue;
                }
                Map<String, Object> clashProxy = new HashMap<>();
                clashProxy.put("name", "Mieru_" + index);
                clashProxy.put("type", "mieru");
                clashProxy.put("server", address);
                clashProxy.put("port", server.getJSONArray("portBindings").getJSONObject(0).get("port"));
                clashProxy.put("transport", "TCP");
                clashProxy.put("username", user.get("name"));
                clashProxy.put("password", user.get("password"));
                ++index;
                WebsiteSchedule.proxySet.add(key);
                proxies.add(clashProxy);
            }
        }
        WebsiteSchedule.indexMap.put("Mieru", index);
        return proxies;
    });
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
