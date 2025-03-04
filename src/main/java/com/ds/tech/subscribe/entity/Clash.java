package com.ds.tech.subscribe.entity;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class Clash {
    private String secret;
    private int mixedPort;
    private boolean allowLan;
    private String logLevel;
    private Map<String, Object> dns;
    private List<Map<String, Object>> proxies;
    private List<ProxyGroup> proxyGroups;
    private Object ruleProviders;
    private List<String> rules;

    public void groupPadding() {
        ProxyGroup proxySelect = this.proxyGroups.getFirst();
        ProxyGroup autoSelect = this.proxyGroups.get(1);
        if (CollectionUtils.isEmpty(proxies)) {
            proxySelect.setProxies(List.of("♻️ 自动选择", "DIRECT"));
            autoSelect.setProxies(Collections.emptyList());
            return;
        }
        List<String> proxyNames = proxies.stream().map(proxy -> proxy.get("name").toString()).toList();
        autoSelect.setProxies(proxyNames);
        List<String> selectNames = new ArrayList<>(proxies.size() + 2);
        selectNames.add("♻️ 自动选择");
        selectNames.add("DIRECT");
        selectNames.addAll(proxyNames);
        proxySelect.setProxies(selectNames);
    }
}
