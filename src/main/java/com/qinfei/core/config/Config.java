package com.qinfei.core.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Validated
@ConfigurationProperties(prefix = "const")
@Data
@NoArgsConstructor
public class Config implements Serializable {

    String[] urls;
    String[] exUrls;
    Map<String, String> pageUrls;
    Map<String, String> staticPages;
    String env;
    String appName;
    String uploadDir;
    String webDir;
    List<String> authos = new ArrayList<>();
    Map<String, String> custConfig = new HashMap<>();

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public String[] getExUrls() {
        return exUrls;
    }

    public void setExUrls(String[] exUrls) {
        this.exUrls = exUrls;
    }

    public Map<String, String> getPageUrls() {
        return pageUrls;
    }

    public void setPageUrls(Map<String, String> pageUrls) {
        this.pageUrls = pageUrls;
    }

    public Map<String, String> getStaticPages() {
        return staticPages;
    }

    public void setStaticPages(Map<String, String> staticPages) {
        this.staticPages = staticPages;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getWebDir() {
        return webDir;
    }

    public void setWebDir(String webDir) {
        this.webDir = webDir;
    }

    public List<String> getAuthos() {
        return authos;
    }

    public void setAuthos(List<String> authos) {
        this.authos = authos;
    }
}