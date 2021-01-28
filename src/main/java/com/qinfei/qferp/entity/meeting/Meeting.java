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
 * @CalssName Meeting
 * @Description 会议表
 * @Author xuxiong
 * @Date 2019/10/18 0018 9:17
 * @Version 1.0
 */
@Table(name = "t_meeting")
@Data
public class Meeting implements Serializable {
    @Id
    private Integer id; //主键
    private Integer meetType; //会议类型
    private String title; //会议主题
    private String content; //会议内容
    private Integer meetRoomId; //会议室ID，当不在公司开会时不需要设置
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date startTime; //开始时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime; //结束结束
    private Integer relateMeetId; //关联会议ID，只能关联当前组织者组织的会议
    private String address; //会议地址，当不在公司开会时需要设置
    private Integer meetSummaryId; //会议纪要人员
    private String otherOrganizer; //其他会议组织者，主要用于非本公司人员
    private String otherHost; //其他会议主持人，主要用于非本公司人员
    private String otherPark; //其他参会人员，主要用于非本公司人员
    private String attachmentName; //附件名称
    private String attachmentLink; //附件地址
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updateId; //更新人
    private Integer state; //状态：0-正常、-9-删除
    private String companyCode; //公司代码:XH-祥和，HY-华越，BD-波动，DY-第一事业部，JT-集团
    @Transient
    private List<Integer> inputUserId;//公司内部主持人
    @Transient
    private List<Integer> inputUserId1;//公司内部组织人
    @Transient
    private List<Integer> inputUserId2;//公司内部参与人
    @Transient
    private Integer otherParkSum;//
    @Transient
    private String userName;
    @Transient
    private Integer auditFlag;
    @Transient
    private Integer auditUserId;
    @Transient
    private String auditUserName;
    @Transient
    private Integer otherSum;
    @Transient
    private Integer acceptSum;

    @Transient
    private MeetingRoom meetingRoom;
    @Transient
    private Integer meetRoomApplyId;
    @Transient
    private String taskId;
    @Transient
    private String meetSummaryName;
    @Transient
    private String relateMeetName;
    @Transient
    private String meetRoomName;
    @Transient
    private String meetRoomAddress;


}
