package com.ds.tech.subscribe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperHolder {

    @Getter
    @Setter
    private static ObjectMapper objectMapper;
}
