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
 * @CalssName MeetingRecord
 * @Description 会议记录表
 * @Author xuxiong
 * @Date 2019/10/18 0018 9:36
 * @Version 1.0
 */
@Table(name = "t_meeting_record")
@Data
public class MeetingRecord implements Serializable {
    @Id
    private Integer id; //主键
    private Integer meetId; //会议室ID
    private Integer recordType; //类型：0-会议记录、1-会议纪要，同一个会议，会议纪要人员可填写纪要和记录两条记录，其他人仅一条记录
    private String content; //会议内容
    private Integer createId; //创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer updateId; //更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private String companyCode; //公司代码:XH-祥和，HY-华越，BD-波动，DY-第一事业部，JT-集团
    private Integer state; //状态：0-正常、-9-删除
    @Transient
    private List<Integer> inputVisibleUserId;//任务可见人员
    @Transient
    private String title;
    @Transient
    private  String name;//任务可见人员
}
