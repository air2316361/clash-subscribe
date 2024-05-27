package com.ds.tech.subscribe.controller;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class CozeController {

    @Value("${PARAMS:%7B%22chatClientId%22%3A%22AzDK04pouSYGqyummL3fc%22%2C%22chatConfig%22%3A%7B%22bot_id%22%3A%227337864834458927122%22%2C%22user%22%3A%22T9P-YFCkdoj0LesDeV5ck%22%2C%22conversation_id%22%3A%22kQyX6XhI6Y-RleKULPEwn%22%7D%2C%22componentProps%22%3A%7B%22title%22%3A%22ChatGPT%E8%81%8A%E5%A4%A9%22%2C%22lang%22%3A%22zh-CN%22%2C%22layout%22%3A%22mobile%22%7D%7D}")
    private String params;
    @Resource
    private ResourceLoader resourceLoader;

    private String indexPage;

    @PostConstruct
    private void init() {
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:pages/index.html");
        try {
            indexPage = resource.getContentAsString(StandardCharsets.UTF_8).replace("PARAMS", params);
        } catch (IOException e) {
            indexPage = "Hello World!";
        }
    }

    @GetMapping
    public String index() {
        return indexPage;
    }
}
