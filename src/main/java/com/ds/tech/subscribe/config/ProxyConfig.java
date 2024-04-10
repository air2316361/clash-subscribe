package com.ds.tech.subscribe.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ProxyConfig {
    @Value("${CLASHMETA1:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/clash.meta2/1/config.yaml}")
    private String clashmeta1;
    @Value("${CLASHMETA1S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/clash.meta2/1/config.yaml}")
    private String clashmeta1s;
    @Value("${CLASHMETA2:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/clash.meta2/2/config.yaml}")
    private String clashmeta2;
    @Value("${CLASHMETA2S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/clash.meta2/2/config.yaml}")
    private String clashmeta2s;
    @Value("${CLASHMETA3:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/clash.meta2/3/config.yaml}")
    private String clashmeta3;
    @Value("${CLASHMETA3S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/clash.meta2/3/config.yaml}")
    private String clashmeta3s;
    @Value("${CLASHMETA4:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/clash.meta2/4/config.yaml}")
    private String clashmeta4;
    @Value("${CLASHMETA4S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/clash.meta2/4/config.yaml}")
    private String clashmeta4s;
    @Value("${CLASHMETA5:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/clash.meta2/5/config.yaml}")
    private String clashmeta5;
    @Value("${CLASHMETA5S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/clash.meta2/5/config.yaml}")
    private String clashmeta5s;
    @Value("${CLASHMETA6:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/clash.meta2/6/config.yaml}")
    private String clashmeta6;
    @Value("${CLASHMETA6S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/clash.meta2/6/config.yaml}")
    private String clashmeta6s;
    @Value("${XRAY1:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/xray/1/config.json}")
    private String xray1;
    @Value("${XRAY1S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/xray/1/config.json}")
    private String xray1s;
    @Value("${XRAY2:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/xray/2/config.json}")
    private String xray2;
    @Value("${XRAY2S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/xray/2/config.json}")
    private String xray2s;
    @Value("${XRAY3:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/xray/3/config.json}")
    private String xray3;
    @Value("${XRAY3S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/xray/3/config.json}")
    private String xray3s;
    @Value("${XRAY4:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/xray/4/config.json}")
    private String xray4;
    @Value("${XRAY4S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/xray/4/config.json}")
    private String xray4s;
    @Value("${HYSTERIA1:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/hysteria/1/config.json}")
    private String hysteria1;
    @Value("${HYSTERIA1S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/hysteria/1/config.json}")
    private String hysteria1s;
    @Value("${HYSTERIA2:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/hysteria/2/config.json}")
    private String hysteria2;
    @Value("${HYSTERIA2S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/hysteria/2/config.json}")
    private String hysteria2s;
    @Value("${HYSTERIA3:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/hysteria/3/config.json}")
    private String hysteria3;
    @Value("${HYSTERIA3S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/hysteria/3/config.json}")
    private String hysteria3s;
    @Value("${HYSTERIA4:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/hysteria/4/config.json}")
    private String hysteria4;
    @Value("${HYSTERIA4S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/hysteria/4/config.json}")
    private String hysteria4s;
    @Value("${SINGBOX1:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/singbox/1/config.json}")
    private String singbox1;
    @Value("${SINGBOX1S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/singbox/config.json}")
    private String singbox1s;
    @Value("${SINGBOX2:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/singbox/2/config.json}")
    private String singbox2;
    @Value("${SINGBOX2S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/singbox/2/config.json}")
    private String singbox2s;
    @Value("${HYSTERIA21:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/hysteria2/1/config.json}")
    private String hysteria21;
    @Value("${HYSTERIA21S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/hysteria2/1/config.json}")
    private String hysteria21s;
    @Value("${HYSTERIA22:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/hysteria2/2/config.json}")
    private String hysteria22;
    @Value("${HYSTERIA22S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/hysteria2/2/config.json}")
    private String hysteria22s;
    @Value("${HYSTERIA23:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/hysteria2/3/config.json}")
    private String hysteria23;
    @Value("${HYSTERIA23S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/hysteria2/3/config.json}")
    private String hysteria23s;
    @Value("${HYSTERIA24:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/hysteria2/4/config.json}")
    private String hysteria24;
    @Value("${HYSTERIA24S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/hysteria2/4/config.json}")
    private String hysteria24s;
    @Value("${QUICK1:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/quick/1/config.yaml}")
    private String quick1;
    @Value("${QUICK1S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/quick/1/config.yaml}")
    private String quick1s;
    @Value("${QUICK2:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/quick/2/config.yaml}")
    private String quick2;
    @Value("${QUICK2S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/quick/2/config.yaml}")
    private String quick2s;
    @Value("${QUICK3:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/quick/3/config.yaml}")
    private String quick3;
    @Value("${QUICK3S:https://gitlab.com/free9999/ipupdate/-/raw/master/backup/img/1/2/ip/quick/3/config.yaml}")
    private String quick3s;
    @Value("${QUICK4:https://www.gitlabip.xyz/Alvin9999/PAC/master/backup/img/1/2/ip/quick/4/config.yaml}")
    private String quick4;
    @Value("${QUICK4S:https://fastly.jsdelivr.net/gh/Alvin9999/PAC@latest/backup/img/1/2/ip/quick/4/config.yaml}")
    private String quick4s;
}
