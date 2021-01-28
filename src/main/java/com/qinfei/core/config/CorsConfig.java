package com.qinfei.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    private CorsConfiguration buildConfig() {
        CorsConfiguration cors = new CorsConfiguration();
//        cors.addAllowedMethod("*");
        // 允许跨域访问的域名
        cors.addAllowedOrigin("*");
        // 请求头
        cors.addAllowedHeader("*");
        // 请求方法
        cors.addAllowedMethod(HttpMethod.DELETE);
        cors.addAllowedMethod(HttpMethod.POST);
        cors.addAllowedMethod(HttpMethod.GET);
        cors.addAllowedMethod(HttpMethod.PUT);
        cors.addAllowedMethod(HttpMethod.OPTIONS);
        // 预检请求的有效期，单位为秒。
        cors.setMaxAge(3600L);
        // 是否支持安全证书
        cors.setAllowCredentials(true);
        cors.addAllowedHeader("Access-Control-Allow-Credentials");
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        //允许的访问方法
//        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
//        //Access-Control-Max-Age 用于 CORS 相关配置的缓存
//        response.setHeader("Access-Control-Max-Age", "3600");
//        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
//        cors.setAllowCredentials(true);
//        registry.addMapping("/**")      //所有请求路径
//                .allowedOrigins("*")//所有请求IP
//                .allowCredentials(true)
//                .allowedMethods("GET", "POST", "DELETE", "PUT")
//                .allowedHeaders("*")
//                .maxAge(3600)
////                .allowedHeaders("Accept", "Content-Type", "Origin", "Authorization", "X-Auth-Token")
////                .exposedHeaders("X-Auth-Token", "Authorization")
        return cors;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }
}