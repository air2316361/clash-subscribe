package com.ds.tech.subscribe.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ds.tech.subscribe.config.Client;
import com.ds.tech.subscribe.config.ObjectMapperHolder;
import com.ds.tech.subscribe.entity.Clash;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
public class SubscribeController {
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
    private JSONObject proxyJson;
    private int len = 0;

    @PostConstruct
    private void init() {
        proxyJson = JSON.parseObject(proxyConfig.trim());
        proxyJson.forEach((key, value) -> {
            JSONArray jsonArray = (JSONArray) value;
            try {
                Client.valueOf(key);
            } catch (IllegalArgumentException e) {
                return;
            }
            len += jsonArray.size();
        });
        ObjectMapperHolder.setObjectMapper(objectMapper);
    }

    @GetMapping("/subscribe")
    public Clash subscribe() throws InterruptedException {
        clashTemplate.reset();
        CountDownLatch countDownLatch = new CountDownLatch(len);
        proxyJson.forEach((key, value) -> {
            JSONArray jsonArray = (JSONArray) value;
            jsonArray.forEach(urls -> request(key, urls, countDownLatch));
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

    private void request(String key, Object o, CountDownLatch countDownLatch) {
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
        threadPoolExecutor.execute(() -> {
            try {
                String s = restTemplate.getForObject(urls.getString(0), String.class);
                if (s != null) {
                    client.converter.convert(s, clashTemplate.getProxies());
                    return;
                }
                if (urls.size() < 2) {
                    return;
                }
                s = restTemplate.getForObject(urls.getString(1), String.class);
                if (s != null) {
                    client.converter.convert(s, clashTemplate.getProxies());
                }
            } catch (Exception ignored) {
            } finally {
                countDownLatch.countDown();
            }
        });
    }
}
