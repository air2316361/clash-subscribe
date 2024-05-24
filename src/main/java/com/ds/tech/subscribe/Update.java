package com.ds.tech.subscribe;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class Update {
    private static final String version = "2024.5.17";
    private static final String chromeVersion = "125";
    private static final String basePath = "D:/Tools";

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject(5);
        read("clashmeta", jsonObject);
        read("xray", jsonObject);
        read("hysteria", jsonObject);
        read("singbox", jsonObject);
        read("hysteria2", jsonObject);
        read("quick", jsonObject);
        System.out.println(jsonObject);
    }

    private static void read(String name, JSONObject jsonObject) {
        String clientPath;
        if ("quick".equals(name)) {
            clientPath = basePath + "/Chrome" + chromeVersion + "_Quick_" + version;
        } else {
            clientPath = basePath + "/Chrome" + chromeVersion + "_AllNew_" + version;
        }
        String subPath;
        if ("clashmeta".equals(name)) {
            subPath = "/clash.meta/ip_Update";
        } else {
            subPath = "/" + name + "/ip_Update";
        }
        for (File file : Objects.requireNonNull(new File(clientPath + subPath).listFiles())) {
            JSONArray urls = new JSONArray(2);
            if (!file.getName().endsWith(".bat")) {
                continue;
            }
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    int index = line.indexOf("http");
                    if (index < 0) {
                        continue;
                    }
                    String url = line.substring(index);
                    urls.add(url);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (name.equals("quick")) {
                name = "clashmeta";
            }
            JSONArray urlJson = jsonObject.getJSONArray(name);
            if (urlJson == null) {
                urlJson = new JSONArray(16);
                jsonObject.put(name, urlJson);
            }
            urlJson.add(urls);
        }
    }
}
