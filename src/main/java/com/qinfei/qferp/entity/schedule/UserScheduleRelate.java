package com.qinfei.qferp.entity.schedule;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName UserSchedule
 * @Description 用户日程表关系表
 * @Author xuxiong
 * @Date 2019/9/2 0002 17:30
 * @Version 1.0
 */
@Table(name = "t_user_schedule_relate")
@Getter
@Setter
@ToString
public class UserScheduleRelate implements Serializable {
    private Integer id;  //主键ID
    private Integer scheduleId; //日程Id
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date remindDate;  //日程具体提醒日期
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date repeatDate;  //日程具体重复日期
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;  //创建时间
    private Integer createId;  //计划创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;  //更新时间
    private Integer updateId;  //更新人
    private Integer state; //状态：0-正常、-9-删除
}
