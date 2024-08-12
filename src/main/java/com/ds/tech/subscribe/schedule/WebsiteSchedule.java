package com.ds.tech.subscribe.schedule;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ds.tech.subscribe.config.Client;
import com.ds.tech.subscribe.config.ObjectMapperHolder;
import com.ds.tech.subscribe.controller.SubscribeController;
import com.ds.tech.subscribe.entity.Clash;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class WebsiteSchedule {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource(name = "clashTemplate")
    private Clash clashTemplate;
    @Value("${PROXY_CONFIG:{}}")
    private String proxyConfig;
    private JSONObject proxyJson;
    @Value("${DOMAIN:grf.cloudns.org}")
    private String domain;

    @PostConstruct
    private void init() {
        proxyJson = JSON.parseObject(proxyConfig.trim());
        ObjectMapperHolder.setObjectMapper(objectMapper);
        clashTemplate.reset();
        refreshSubscribe();
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void schedule() {
        restTemplate.getForObject("https://" + domain, String.class);
        refreshSubscribe();
    }

    private void refreshSubscribe() {
        Set<String> set = new HashSet<>();
        proxyJson.forEach((key, value) -> {
            JSONArray jsonArray = (JSONArray) value;
            jsonArray.forEach(urls -> request(key, urls, set));
        });
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
        String proxyConfig;
        try {
            proxyConfig = objectMapper.writeValueAsString(clashTemplate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        SubscribeController.setProxyConfig(proxyConfig);
        clashTemplate.reset();
    }

    private void request(String key, Object o, Set<String> set) {
        if (!(o instanceof JSONArray urls)) {
            return;
        }
        if (CollectionUtils.isEmpty(urls)) {
            return;
        }
        Client client;
        try {
            client = Client.valueOf(key);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (!convert(urls.getString(0), client, clashTemplate.getProxies(), set) && urls.size() > 1) {
            convert(urls.getString(1), client, clashTemplate.getProxies(), set);
        }
    }

    private boolean convert(String url, Client client, List<Map<String, Object>> proxies, Set<String> set) {
        String resp;
        try {
            resp = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            resp = null;
        }
        if (resp == null) {
            return false;
        }
        List<Map<String, Object>> convertProxies = client.function.apply(resp);
        convertProxies.forEach(proxy -> {
            String uniqueKey = proxy.get("server") + ":" + proxy.get("port");
            if (set.contains(uniqueKey)) {
                return;
            }
            set.add(uniqueKey);
            proxies.add(proxy);
        });
        return true;
    }
}
