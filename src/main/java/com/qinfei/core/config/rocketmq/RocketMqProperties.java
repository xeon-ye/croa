package com.qinfei.core.config.rocketmq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties("rocketmq")
class RocketMqProperties {
    //服务地址集合
    private List<String> namesrvaddr;
}