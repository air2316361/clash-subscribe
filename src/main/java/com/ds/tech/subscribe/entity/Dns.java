package com.ds.tech.subscribe.entity;

import lombok.Data;

import java.util.List;

@Data
public class Dns {
    private boolean enabled;
    private List<String> nameserver;
    private FallbackFilter fallbackFilter;
}
