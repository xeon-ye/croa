package com.qinfei.qferp.entity.workup;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @CalssName WorkupRequestParam
 * @Description 唤醒流程请求参数
 * @Author xuxiong
 * @Date 2019/11/15 0015 16:58
 * @Version 1.0
 */
@Data
@ToString
public class WorkupRequestParam implements Serializable {
    private Integer workupId; //唤醒记录的ID
    private String workupName; //当前唤醒记录的title
    private Integer processType; //唤醒流程类型
    private String processName; //唤醒流程名称
    private String taskDefKey; //唤醒到指定的节点名称
    private String workupTaskId; //唤醒记录完成的taskId
    private Boolean gatewayFlag; //是否有网关
}
