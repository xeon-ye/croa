package com.qinfei.qferp.entity.schedule;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @CalssName UserSchedule
 * @Description 用户日程表
 * @Author xuxiong
 * @Date 2019/9/2 0002 17:30
 * @Version 1.0
 */
@Table(name = "t_user_schedule")
@Getter
@Setter
@ToString
public class UserSchedule implements Serializable {
    private Integer id;  //主键ID
    private String name; //日程标题
    private Integer isAllDay; //是否全天 0-是、1-否
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;  //开始时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;  //结束时间
    private Integer repeatFlag; //重复：0-无、1-每天、2-每周、3-每两周、4-每月、5-每年
    private Integer remindFlag; //提醒：当is_all_day = 0时，0-无、1-日程当天、2-1天前、3-2天前、4-1周前；否则：0-无、1-日程开始时、2-5分钟前、3-15分钟前、4-30分钟前、5-1小时前、6-2小时前
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;  //创建时间
    private Integer createId;  //日程创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;  //更新时间
    private Integer updateId;  //更新人
    private Integer state; //状态：0-正常、-9-删除
    private Integer scheduleType; //日程类型：0-新建日程、1-会议日程
    private Integer otherReplateId; //其他表关联ID，根据需求自己设置，当schedule_type != 0时，可能需要进行表关联, 例如：会议日程可能存放会议ID
    private String jumpTitle; //跳转链接
    private String jumpUrl; //跳转链接

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Transient
    private Date remindDate;  //日程通知时间，用于获取当前通知时间
    @Transient
    private String userName;  //用户名称
    @Transient
    private String userImage; //用户头像
    @Transient
    private Integer deptId; //用户部门ID
    @Transient
    private String deptName; //用户部门名称

}
