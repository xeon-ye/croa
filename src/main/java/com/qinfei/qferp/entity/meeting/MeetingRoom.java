package com.qinfei.qferp.entity.meeting;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingRoom
 * @Description 会议室表
 * @Author xuxiong
 * @Date 2019/10/18 0018 8:43
 * @Version 1.0
 */
@Table(name = "t_meeting_room")
@Data
public class MeetingRoom implements Serializable {
    @Id
    private Integer id; //主键ID
    private String name; //会议室名称，同一个公司名称唯一
    private String picPath; //会议室图片
    private String address; //会议室地址
    private Integer peopleNum; //可容纳人数
    private String otherDevice; //会议室其他设备（非物品管理中的）
    private Integer enabled; //是否可预定: 0-可预定、1-不可预定
    private Integer auditFlag; //是否需要审核: 0-不需要、1-需要
    private Integer auditUserId; //会议室需要审核时的审核人ID
    private String openTimeStart; //每日开放时间范围，格式：HH:mm，开放时间如果选择的不是整除最小预约时间，前台进行处理
    private String openTimeEnd; //每日开放时间范围，格式：HH:mm，开放时间如果选择的不是整除最小预约时间，前台进行处理
    private Integer meetUnit; //最小预约时间，单位分钟，默认30分钟，支持：15分钟、30分钟、60分钟（1小时）
    private Integer onceTime; //单次预约最长时间，单位分钟，默认无
    private Integer applyRange; //可预订范围，用于设置最大可提前多少天预约，单位天，默认无
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updateId; //更新人
    private String remarks; //会议室描述
    private String applyNotice; //会议室预定须知
    private Integer state; //状态：0-正常、-9-删除
    private String companyCode; //公司代码:XH-祥和，HY-华越，BD-波动，DY-第一事业部，JT-集团

    @Transient
    private List<Map<String, String>> timeSlotList;
    @Transient
    private List<MeetingRoomApply> meetingRoomApplyList;
    @Transient
    private String approverUserName;
}
