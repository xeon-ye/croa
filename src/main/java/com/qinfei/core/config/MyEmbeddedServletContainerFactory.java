//package com.qinfei.core.config;
//
//import org.apache.catalina.connector.Connector;
//import org.apache.coyote.http11.Http11NioProtocol;
//import org.springframework.boot.web.servlet.ServletContextInitializer;
//
//public class MyEmbeddedServletContainerFactory extends TomcatEmbeddedServletContainerFactory {
//    public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers)
//    {
//        //设置端口
//        this.setPort(8081);
//        return super.getEmbeddedServletContainer(initializers);
//    }
//
//    protected void customizeConnector(Connector connector)
//    {
//        super.customizeConnector(connector);
//        Http11NioProtocol protocol = (Http11NioProtocol)connector.getProtocolHandler();
//        //设置最大连接数
//        protocol.setMaxConnections(2000);
//        //设置最大线程数
//        protocol.setMaxThreads(2000);
//        protocol.setConnectionTimeout(30000);
//    }
//}