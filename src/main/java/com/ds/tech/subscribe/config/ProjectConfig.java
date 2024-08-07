package com.ds.tech.subscribe.config;

import com.ds.tech.subscribe.entity.Clash;
import com.ds.tech.subscribe.entity.ProxyGroup;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableScheduling
public class ProjectConfig {
    @Bean
    public ObjectMapper objectMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        return new ObjectMapper(yamlFactory)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                if (connection instanceof HttpsURLConnection httpsConnection) {
                    httpsConnection.setHostnameVerifier((s, sslSession) -> true);
                    try {
                        SSLContext context = SSLContext.getInstance("TLS");
                        context.init(null, new TrustManager[]{new X509TrustManager() {

                            @Override
                            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }}, new SecureRandom());
                        httpsConnection.setSSLSocketFactory(context.getSocketFactory());
                    } catch (NoSuchAlgorithmException | KeyManagementException e) {
                        super.prepareConnection(connection, httpMethod);
                        return;
                    }
                }
                super.prepareConnection(connection, httpMethod);
            }
        });
    }

    @Bean("clashTemplate")
    public Clash clashTemplate(ObjectMapper objectMapper) throws JsonProcessingException {
        Clash clash = new Clash();
        clash.setAllowLan(true);
        clash.setSecret("xiaobaihe");
        clash.setPort(7897);
        clash.setSocksPort(7891);
        clash.setMixedPort(7890);
        clash.setCfwBypass(objectMapper.readValue("""
                - localhost
                - 127.*
                - 10.*
                - 172.16.*
                - 172.17.*
                - 172.18.*
                - 172.19.*
                - 172.20.*
                - 172.21.*
                - 172.22.*
                - 172.23.*
                - 172.24.*
                - 172.25.*
                - 172.26.*
                - 172.27.*
                - 172.28.*
                - 172.29.*
                - 172.30.*
                - 172.31.*
                - 192.168.*
                - <local>""", listType));
        clash.setDns(objectMapper.readValue("""
                enable: true
                ipv6: false
                nameserver:
                  - 1.1.1.1
                  - 8.8.8.8
                fallback:
                  - 114.114.114.114
                  - 223.5.5.5
                fallback-filter:
                geoip: true
                geoip-code: CN
                ipcidr:
                  - 240.0.0.0/4
                nameserver-policy:
                "geosite:cn": [114.114.114.114, 223.5.5.5]
                default-nameserver:
                  - 1.1.1.1
                  - 8.8.8.8
                enhanced-mode: fake-ip
                fake-ip-range: 198.18.0.1/16""", mapType));
        clash.setExternalController("127.0.0.1:9090");
        clash.setLogLevel("info");
        clash.setMode("Rule");
        List<ProxyGroup> proxyGroups = new ArrayList<>(2);
        ProxyGroup nodeSelect = new ProxyGroup();
        nodeSelect.setName("\uD83D\uDE80 节点选择");
        nodeSelect.setType("select");
        proxyGroups.add(nodeSelect);
        ProxyGroup autoSelect = new ProxyGroup();
        autoSelect.setName("♻️ 自动选择");
        autoSelect.setType("url-test");
        autoSelect.setUrl("https://www.gstatic.com/generate_204");
        autoSelect.setInterval(300);
        autoSelect.setTolerance(50);
        proxyGroups.add(autoSelect);
        clash.setProxyGroups(proxyGroups);
        clash.setRuleProviders(objectMapper.readValue("""
                reject:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/reject.txt"
                  path: ./ruleset/reject.yaml
                  interval: 86400
                icloud:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/icloud.txt"
                  path: ./ruleset/icloud.yaml
                  interval: 86400
                apple:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/apple.txt"
                  path: ./ruleset/apple.yaml
                  interval: 86400
                google:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/google.txt"
                  path: ./ruleset/google.yaml
                  interval: 86400
                proxy:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/proxy.txt"
                  path: ./ruleset/proxy.yaml
                  interval: 86400
                direct:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/direct.txt"
                  path: ./ruleset/direct.yaml
                  interval: 86400
                private:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/private.txt"
                  path: ./ruleset/private.yaml
                  interval: 86400
                gfw:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/gfw.txt"
                  path: ./ruleset/gfw.yaml
                  interval: 86400
                tld-not-cn:
                  type: http
                  behavior: domain
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/tld-not-cn.txt"
                  path: ./ruleset/tld-not-cn.yaml
                  interval: 86400
                telegramcidr:
                  type: http
                  behavior: ipcidr
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/telegramcidr.txt"
                  path: ./ruleset/telegramcidr.yaml
                  interval: 86400
                cncidr:
                  type: http
                  behavior: ipcidr
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/cncidr.txt"
                  path: ./ruleset/cncidr.yaml
                  interval: 86400
                lancidr:
                  type: http
                  behavior: ipcidr
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/lancidr.txt"
                  path: ./ruleset/lancidr.yaml
                  interval: 86400
                applications:
                  type: http
                  behavior: classical
                  url: "https://cdn.jsdelivr.net/gh/Loyalsoldier/clash-rules@release/applications.txt"
                  path: ./ruleset/applications.yaml
                  interval: 86400""", mapType));
        clash.setRules(objectMapper.readValue("""
                - RULE-SET,applications,DIRECT
                - DOMAIN,clash.razord.top,DIRECT
                - DOMAIN,yacd.haishan.me,DIRECT
                - RULE-SET,private,DIRECT
                - RULE-SET,reject,REJECT
                - RULE-SET,icloud,DIRECT
                - RULE-SET,apple,DIRECT
                - RULE-SET,google,\uD83D\uDE80 节点选择
                - RULE-SET,proxy,\uD83D\uDE80 节点选择
                - RULE-SET,direct,DIRECT
                - RULE-SET,lancidr,DIRECT
                - RULE-SET,cncidr,DIRECT
                - RULE-SET,telegramcidr,\uD83D\uDE80 节点选择
                - GEOIP,LAN,DIRECT
                - GEOIP,CN,DIRECT
                - MATCH,\uD83D\uDE80 节点选择""", listType));
        return clash;
    }

    public static final TypeReference<List<String>> listType = new TypeReference<>() {
    };

    public static final TypeReference<Map<String, Object>> mapType = new TypeReference<>() {
    };
}
