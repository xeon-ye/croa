//package com.qinfei.core.config.swagger;
//
//import com.qinfei.core.config.websocket.STOMPConnectEventListener;
//import com.qinfei.core.config.websocket.SocketSessionRegistry;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//
///**
// * socket核心配置容器
// */
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig<EnableWebSocketMessageBroker> extends AbstractWebSocketMessageBrokerConfigurer {
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        config.enableSimpleBroker("/topic");// /users 默认通知
//        config.setApplicationDestinationPrefixes("/app");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//            registry.addEndpoint("/ricky-websocket").withSockJS();
//    }
//
//
//    @Bean
//    public SocketSessionRegistry SocketSessionRegistry(){
//        return new SocketSessionRegistry();
//    }
//    @Bean
//    public STOMPConnectEventListener STOMPConnectEventListener(){
//        return new STOMPConnectEventListener();
//    }
//}