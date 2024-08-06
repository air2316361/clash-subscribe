package com.ds.tech.subscribe.config;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface Converter {
    void convert(String resp, List<Map<String, Object>> proxies);
}
