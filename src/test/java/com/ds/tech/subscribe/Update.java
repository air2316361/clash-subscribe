package com.ds.tech.subscribe;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ds.tech.subscribe.config.Client;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Update {
    public static void main(String[] args) {
        String basePath = "D:/Tools/freeair";
        JSONObject jsonObject = new JSONObject(7);
        for (File file : new File(basePath).listFiles()) {
            if (!file.getName().endsWith(".7z")) {
                continue;
            }
            try (SevenZFile sevenZFile = SevenZFile.builder().setFile(file).get()) {
                SevenZArchiveEntry entry;
                while ((entry = sevenZFile.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (!name.contains("/ip_Update/ip_") || !name.endsWith(".bat")) {
                        continue;
                    }
                    int start = name.indexOf('/') + 1;
                    int end = name.indexOf('/', start);
                    String client = name.substring(start, end);
                    if ("clash.meta".equals(client) || "Quick".equals(client)) {
                        client = "clashmeta";
                    }
                    try {
                        Client.valueOf(client);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    JSONArray urls = new JSONArray(2);
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sevenZFile.getInputStream(entry)))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            int index = line.indexOf("http");
                            if (index < 0) {
                                continue;
                            }
                            String url = line.substring(index);
                            urls.add(url);
                        }
                    }
                    JSONArray urlJson = jsonObject.getJSONArray(client);
                    if (urlJson == null) {
                        urlJson = new JSONArray(16);
                        jsonObject.put(client, urlJson);
                    }
                    urlJson.add(urls);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(jsonObject);
    }
}
