package com.qinfei.core.config.rocketmq;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface RocketConsume {

    /**
     * 订阅主题
     *
     * @return
     */
    String topic();

    /**
     * 订阅标签
     *
     * @return
     */
    String[] tags() default {"*"};
}