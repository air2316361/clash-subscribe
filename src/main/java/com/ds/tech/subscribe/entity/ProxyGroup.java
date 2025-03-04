package com.ds.tech.subscribe.entity;

import lombok.Data;

import java.util.List;

@Data
public class ProxyGroup {
    private String name;
    private String type;
    private String url;
    private Integer interval;
    private Integer tolerance;
    private List<String> proxies;
}
