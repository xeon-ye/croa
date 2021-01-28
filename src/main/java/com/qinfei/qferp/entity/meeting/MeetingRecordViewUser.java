package com.qinfei.qferp.entity.meeting;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName MeetingRecordViewUser
 * @Description 会议记录可见人员表
 * @Author xuxiong
 * @Date 2019/10/18 0018 9:50
 * @Version 1.0
 */
@Table(name = "t_meeting_record_view_user")
@Data
public class MeetingRecordViewUser implements Serializable {
    @Id
    private Integer id; //主键
    private Integer meetRecordId; //会议ID
    private Integer userId; //可见人员ID
    private Integer createId; //创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer updateId; //更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer state; //状态：0-正常、-9-删除
}
