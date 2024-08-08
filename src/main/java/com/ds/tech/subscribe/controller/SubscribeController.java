package com.ds.tech.subscribe.controller;

import com.ds.tech.subscribe.entity.Clash;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscribeController {
    @Resource(name = "clashTemplate")
    private Clash clashTemplate;
    @Value("${DOMAIN:grf.cloudns.org}")
    private String domain;

    @GetMapping
    public String index() {
        return "Welcome to clash subscribe server. The subscribe link was https://" + domain + "/subscribe";
    }

    @GetMapping("/subscribe")
    public Clash subscribe() {
        return clashTemplate;
    }
}
