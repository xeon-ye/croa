package com.qinfei.qferp.entity.meeting;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.core.utils.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName MeetingRoomApply
 * @Description 会议室预约表
 * @Author xuxiong
 * @Date 2019/10/18 0018 9:09
 * @Version 1.0
 */
@Table(name = "t_meeting_room_apply")
@Data
public class MeetingRoomApply implements Serializable {
    @Id
    private Integer id; //主键ID
    private Integer meetRoomId; //会议室ID
    private Integer meetId; //会议ID，会议室预约会先有占位功能，占位后需创建会议，创建完成后需要将会议ID更新
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date startTime; //开始时间，占位时，使用选择的开始时间，当创建会议室后，以会议开始时间为准
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime; //结束结束，占位时，使用选择的结束时间，当创建会议室后，以会议结束时间为准
    private Integer auditFlag; //是否需要审核: 0-不需要、1-需要
    private Integer itemId; //待办事项Id，当需要审核的会议室预约时，使用该字段
    private String taskId; //任务id，当需要审核的会议室预约时，使用该字段
    private Integer approverUserId; //审核人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updateId; //更新人
    private String remarks; //会议室预约描述
    private Integer state; //状态：0-正常/审核中、1-审核完成、-9-删除，如果不需要审核0-正常，否则0-审核中
    private String companyCode; //公司代码:XH-祥和，HY-华越，BD-波动，DY-第一事业部，JT-集团

    @Transient
    private MeetingRoom meetingRoom;
    @Transient
    private Meeting meeting;
    @Transient
    private String userName; //预约人名称
    @Transient
    private String deptName; //部门名称
}
