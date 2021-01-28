package com.qinfei.core.annotation;

import java.lang.annotation.*;

/**
 * 是否关联对象注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Relate {

    boolean flag() default true;//备注

    Class<?> name();//备注

    String fkName();//关联的外键名称
}
