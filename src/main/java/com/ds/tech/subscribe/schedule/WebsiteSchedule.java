package com.ds.tech.subscribe.schedule;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WebsiteSchedule {
    @Resource
    private RestTemplate restTemplate;
    @Value("${DOMAIN:gai.cloudns.org}")
    private String domain;
    @Value("${WARP_DOMAIN:grf.cloudns.org}")
    private String warpDomain;

    @Scheduled(cron = "0 * * * * *")
    public void schedule() {
        restTemplate.getForObject("https://" + domain + "/test", String.class);

    }

    @Scheduled(cron = "30 * * * * *")
    public void schedule1() {
        restTemplate.getForObject("https://" + warpDomain, String.class);
    }
}
