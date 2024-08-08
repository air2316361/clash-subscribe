package com.ds.tech.subscribe.controller;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscribeController {
    @Value("${DOMAIN:grf.cloudns.org}")
    private String domain;
    @Setter
    private static String proxyConfig;

    @GetMapping
    public String index() {
        return "<!doctype html><html lang=\"zh_cn\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\"><meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\"><title>Clash订阅</title></head><body><h2>欢迎使用勾魂小寡妇的Clash订阅服务</h2><h4>订阅链接：</h4><h1 style=\"color: red\">https://" + domain + "/subscribe</h1></body></html>";
    }

    @GetMapping(value = "/subscribe", produces = MediaType.APPLICATION_JSON_VALUE)
    public String subscribe() {
        return proxyConfig;
    }
}
