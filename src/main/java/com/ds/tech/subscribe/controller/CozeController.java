package com.ds.tech.subscribe.controller;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@RestController
public class CozeController {
    private static final ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

    @Value("${DOMAIN:gai.cloudns.org}")
    private String domain;
    @Value("${PARAMS:%7B%22chatClientId%22%3A%22AzDK04pouSYGqyummL3fc%22%2C%22chatConfig%22%3A%7B%22bot_id%22%3A%227337864834458927122%22%2C%22user%22%3A%22T9P-YFCkdoj0LesDeV5ck%22%2C%22conversation_id%22%3A%22kQyX6XhI6Y-RleKULPEwn%22%7D%2C%22componentProps%22%3A%7B%22title%22%3A%22%E8%93%9D%E5%AE%9D%E5%98%9F%E5%98%9F%22%2C%22lang%22%3A%22zh-CN%22%2C%22layout%22%3A%22mobile%22%7D%7D}")
    private String params;
    @Resource
    private ResourceLoader resourceLoader;

    private String indexPage;
    private String proxyJsCode;
    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        indexPage = readFile("index.html").replace("DOMAIN", domain).replace("PARAMS", params);
        proxyJsCode = "<script>" + readFile("proxy.js").replaceAll("DOMAIN", domain) + "</script>";
        restTemplate = new RestTemplate();
    }

    @GetMapping
    public String index() {
        return indexPage;
    }

    @GetMapping("/open-platform/sdk/chatapp")
    public String iframe(@RequestParam String params, HttpServletResponse response) {
        ResponseEntity<String> entity = restTemplate.getForEntity("https://api.coze.com/open-platform/sdk/chatapp?params={0}", String.class, params);
        entity.getHeaders().forEach((key, value) -> value.forEach(it -> response.addHeader(key, it)));
        String body = entity.getBody();
        if (body == null) {
            return "";
        }
        int index = body.lastIndexOf("</html>");
        if (index < 0) {
            return body;
        }
        String prefix = body.substring(0, index);
        String suffix = body.substring(index);
        return prefix + proxyJsCode + suffix;
    }

    @RequestMapping("/open_api/v1/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = "https://api.coze.com" + request.getRequestURI();
        String query = request.getQueryString();
        URI targetURI = new URI(StringUtils.hasLength(query) ? url + "?" + query : url);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        ClientHttpRequest delegate = requestFactory.createRequest(targetURI, method);
        Enumeration<String> headerNames = request.getHeaderNames();
        HttpHeaders targetHeaders = delegate.getHeaders();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> v = request.getHeaders(headerName);
            while (v.hasMoreElements()) {
                targetHeaders.add(headerName, v.nextElement());
            }
        }
        StreamUtils.copy(request.getInputStream(), delegate.getBody());
        try (ClientHttpResponse clientHttpResponse = delegate.execute()) {
            response.setStatus(clientHttpResponse.getStatusCode().value());
            clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> response.addHeader(key, it)));
            InputStream inputStream = clientHttpResponse.getBody();
            ServletOutputStream outputStream = response.getOutputStream();
            StreamUtils.copy(inputStream, outputStream);
        }
    }

    private String readFile(String fileName) {
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:pages/" + fileName);
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}
