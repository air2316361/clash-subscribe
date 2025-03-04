package com.ds.tech.subscribe.entity;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Clash {
    private boolean allowLan;
    private String secret;
    private int port;
    private int socksPort;
    private int mixedPort;
    private List<String> cfwBypass;
    private Map<String, Object> dns;
    private String externalController;
    private String logLevel;
    private String mode;
    private List<Map<String, Object>> proxies;
    private List<ProxyGroup> proxyGroups;
    private Map<String, Object> ruleProviders;
    private List<String> rules;

    public void reset() {
        this.proxies = new ArrayList<>(30);
        List<String> proxies = new ArrayList<>(32);
        proxies.add("♻️ 自动选择");
        proxies.add("DIRECT");
        this.proxyGroups.getFirst().setProxies(proxies);
        this.proxyGroups.getLast().setProxies(new ArrayList<>(30));
    }

    public void groupPadding() {
        if (CollectionUtils.isEmpty(proxies)) {
            return;
        }
        List<String> proxyNames = proxies.stream().map(proxy -> proxy.get("name").toString()).toList();
        proxyGroups.forEach(proxyGroup -> proxyGroup.getProxies().addAll(proxyNames));
    }
}
