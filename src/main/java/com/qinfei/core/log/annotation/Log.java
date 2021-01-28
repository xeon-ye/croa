package com.qinfei.core.log.annotation;

import com.qinfei.core.log.OperateType;

import java.lang.annotation.*;

/**
 * 统一日志记录注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Log {

    /**
     * 备注
     * @return
     */
    String note() default "";//备注

    /**
     * 操作类型
     * @return
     */
    OperateType opType() default OperateType.ADD;//1 添加 2 修改 3 删除

    /**
     * 所属模块
     * @return
     */
    String module() default "";//模块
}