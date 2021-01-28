package com.qinfei.qferp.entity.meeting;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @CalssName MeetingTask
 * @Description 会议任务表
 * @Author xuxiong
 * @Date 2019/10/18 0018 9:40
 * @Version 1.0
 */
@Table(name = "t_meeting_task")
@Data
public class MeetingTask implements Serializable {
    @Id
    private Integer id; //主键
    private Integer meetId; //会议ID，不在公司的会议无需设置
    private String title; //会议任务标题
    private String content; //会议任务内容
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date startTime; //开始时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime; //结束结束
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updateId; //更新人
    private String companyCode; //公司代码:XH-祥和，HY-华越，BD-波动，DY-第一事业部，JT-集团
    private Integer state; //状态：0-正常、1-延期、2-已结束、-9-删除
    @Transient
    private List<Integer> inputUserId;//公司内部会议任务负责人
    @Transient
    private List<Integer> inputUserId1;//公司内部会议任务检查人
    @Transient
    private String inputUserName;//公司内部会议任务负责人
    @Transient
    private String inputUserName1;//公司内部会议任务检查人

    @Transient
    private String userId;

    @Transient
    private String userId1;
    @Transient
    private String meetTitle;

}
