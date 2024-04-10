package com.ds.tech.subscribe.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class Clash {
    private String secret;
    private int mixedPort;
    private boolean allowLan;
    private String logLevel;
    private Dns dns;
    private List<Map<String, Object>> proxies;

    public void init() {
        this.secret = "dongtaiwang.com";
        this.mixedPort = 7890;
        this.allowLan = true;
        this.logLevel = "info";
        Dns dns = new Dns();
        dns.setEnabled(true);
        dns.setNameserver(Arrays.asList("119.29.29.29", "223.5.5.5"));
        FallbackFilter fallbackFilter = new FallbackFilter();
        fallbackFilter.setGeoip(false);
        fallbackFilter.setIpcidr(Arrays.asList("240.0.0.0/4", "0.0.0.0/32"));
        dns.setFallbackFilter(fallbackFilter);
        this.dns = dns;
        this.proxies = new ArrayList<>();
    }
}
