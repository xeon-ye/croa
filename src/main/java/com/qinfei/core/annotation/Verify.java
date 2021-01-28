package com.qinfei.core.annotation;


import java.lang.annotation.*;

/**
 * 统一认证注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Verify {

    /**
     * 权限编码
     * @return
     */
    String code() default "";//备注

    /**
     * 动作名称
     * @return
     */
    String action() default "";//1 添加 2 修改 3 删除

    /**
     * 所属模块
     * @return
     */
    String module() default "";//模块

}