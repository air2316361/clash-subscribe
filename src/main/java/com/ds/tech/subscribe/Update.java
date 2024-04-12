package com.ds.tech.subscribe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Update {
    private static final String version = "2024.3.21";
    private static final String chromeVersion = "123";
    private static final String basePath = "D:/Tools";

    public static void main(String[] args) {
        System.out.println("PORT=8080");
        read("clashmeta");
        read("hysteria");
        read("singbox");
        read("hysteria2");
        read("quick");
    }

    private static void read(String name) {
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
        List<String> urls = new ArrayList<>(2);
        int num = 0;
        for (File file : Objects.requireNonNull(new File(clientPath + subPath).listFiles())) {
            ++num;
            urls.clear();
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
            String upperCase = name.toUpperCase();
            System.out.println(upperCase + num + "=" + urls.get(0));
            System.out.println(upperCase + num + "s=" + urls.get(1));
        }
    }
}
