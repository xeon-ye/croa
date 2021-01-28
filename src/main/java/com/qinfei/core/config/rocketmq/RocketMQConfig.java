package com.qinfei.core.config.rocketmq;//package com.qinfei.core.config;

//import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
//import com.alibaba.rocketmq.client.exception.MQClientException;
//import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
//import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class RocketMQConfig extends WebApplicationObjectSupport {

    private List<String> namesrvaddr;

    @Autowired
    private RocketMqProperties properties;

    /**
     * @Author: ZR
     * @param: 初始化消费者
     * @Description:
     * @date: Created in 13:26 2018/5/15
     */
    @PostConstruct
    private void init() {
        namesrvaddr = properties.getNamesrvaddr();
        ApplicationContext context = getApplicationContext();
        Map<String, RocketMQListener> mqListenerMap = context.getBeansOfType(RocketMQListener.class);
        if (mqListenerMap != null && !mqListenerMap.isEmpty()) {
            Collection<RocketMQListener> list = mqListenerMap.values();
            list.stream().filter(rocketMqListener -> null != rocketMqListener
                    && rocketMqListener.getClass().isAnnotationPresent(RocketConsume.class))
                    .collect(Collectors.toList())
                    .forEach(listener -> {
                        RocketConsume rocketConsume = listener.getClass().getAnnotation(RocketConsume.class);
                        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
                        consumer.setNamesrvAddr(namesrvaddr.get(0));
                        try {
                            consumer.subscribe(rocketConsume.topic(), rocketConsume.tags()[0]);
                        } catch (MQClientException e) {
                            e.printStackTrace();
                        }
                        consumer.setConsumerGroup(rocketConsume.tags()[0]);
                        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                        consumer.setMessageListener(listener);
                        consumer.setMessageModel(MessageModel.CLUSTERING);
                        try {
                            consumer.start();
                        } catch (MQClientException e) {
                            e.printStackTrace();
                        }

                    });
        }
    }
}