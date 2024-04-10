package com.ds.tech.subscribe.entity;

import lombok.Data;

import java.util.List;

@Data
public class FallbackFilter {
    private boolean geoip;
    private List<String> ipcidr;
}
