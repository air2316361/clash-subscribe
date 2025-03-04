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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.*;

@Component
@Slf4j
public class WebsiteSchedule {

    public static Map<String, Integer> indexMap = new HashMap<>();
    public static Set<String> proxySet = new HashSet<>();

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RestTemplate restTemplate;
    @Value("${PROXY_CONFIG:{}}")
    private String proxyConfig;
    private JSONObject proxyJson;
    @Value("${DOMAIN:grf.cloudns.org}")
    private String domain;
    private Clash clashTemplate;

    @PostConstruct
    private void init() {
        proxyJson = JSON.parseObject(proxyConfig.trim());
        ObjectMapperHolder.setObjectMapper(objectMapper);
        try (InputStream inputStream = WebsiteSchedule.class.getClassLoader().getResourceAsStream("template.yml")) {
            clashTemplate = objectMapper.readValue(new String(inputStream.readAllBytes()), Clash.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        refreshSubscribe();
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void schedule() {
        String s = restTemplate.getForObject("https://" + domain, String.class);
        log.info(s);
        refreshSubscribe();
    }

    private void refreshSubscribe() {
        indexMap.clear();
        proxySet.clear();
        List<Map<String, Object>> proxies = new ArrayList<>();
        proxyJson.forEach((key, value) -> {
            JSONArray jsonArray = (JSONArray) value;
            jsonArray.forEach(urls -> request(key, urls, proxies));
        });
        proxies.sort(Comparator.comparing(o -> o.get("name").toString()));
        clashTemplate.setProxies(proxies);
        clashTemplate.groupPadding();
        String proxyConfig;
        try {
            proxyConfig = objectMapper.writeValueAsString(clashTemplate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        SubscribeController.setProxyConfig(proxyConfig);
    }

    private void request(String key, Object o, List<Map<String, Object>> proxies) {
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
        if (!convert(urls.getString(0), client, proxies) && urls.size() > 1) {
            convert(urls.getString(1), client, proxies);
        }
    }

    private boolean convert(String url, Client client, List<Map<String, Object>> proxies) {
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
        proxies.addAll(convertProxies);
        return true;
    }
}
