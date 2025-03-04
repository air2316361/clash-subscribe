package com.ds.tech.subscribe.entity;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
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
    private List<String> rules;

    public void groupPadding() {
        if (CollectionUtils.isEmpty(proxies)) {
            return;
        }
        List<String> proxyNames = proxies.stream().map(proxy -> proxy.get("name").toString()).toList();
        proxyGroups.forEach(proxyGroup -> {
            List<String> orgProxies = proxyGroup.getProxies();
            boolean flag = false;
            for (Iterator<String> iterator = orgProxies.iterator(); iterator.hasNext(); ) {
                String propertyName = iterator.next();
                if (propertyName.startsWith("dongtaiwang.com")) {
                    iterator.remove();
                    flag = true;
                }
            }
            if (flag) {
                orgProxies.addAll(proxyNames);
            }
        });
    }
}
