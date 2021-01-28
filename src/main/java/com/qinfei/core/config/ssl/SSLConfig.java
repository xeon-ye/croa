//package com.qinfei.core.config.ssl;
//
//import org.apache.catalina.Context;
//import org.apache.catalina.connector.Connector;
//import org.apache.tomcat.util.descriptor.web.SecurityCollection;
//import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SSLConfig {
//    @Value("${http.port}")
//    private Integer http;
//    @Value("${server.port}")
//    private Integer https;
//    @Value("${server.environment}")
//    private String environment;
//
//    @Bean
//    public TomcatServletWebServerFactory servletContainer() {
//        if ("official".equals(environment)) {
//            TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
//                @Override
//                protected void postProcessContext(Context context) {
//                    SecurityConstraint constraint = new SecurityConstraint();
//                    constraint.setUserConstraint("CONFIDENTIAL");
//                    SecurityCollection collection = new SecurityCollection();
//                    collection.addPattern("/*");
//                    constraint.addCollection(collection);
//                    context.addConstraint(constraint);
//                }
//            };
//            tomcat.addAdditionalTomcatConnectors(httpConnector());
//            return tomcat;
//        }
//        return new TomcatServletWebServerFactory();
//    }
//
//    @Bean
//    public Connector httpConnector() {
//        if ("official".equals(environment)) {
//            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
//            connector.setScheme("http");
//            //Connector监听的http的端口号
//            connector.setPort(http);
//            connector.setSecure(false);
//            //监听到http的端口号后转向到的https的端口号
//            connector.setRedirectPort(https);
//            return connector;
//        }
//        return new Connector();
//    }
//}