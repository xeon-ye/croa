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
 * @CalssName MeetingUser
 * @Description 会议/会议任务相关人员表
 * @Author xuxiong
 * @Date 2019/10/18 0018 9:29
 * @Version 1.0
 */
@Table(name = "t_meeting_user")
@Data
public class MeetingUser implements Serializable {
    @Id
    private Integer id; //主键
    private Integer meetId; //会议ID
    private Integer meetTaskId; //会议任务ID，仅对会议任务使用
    private Integer userId; //会议涉及人ID
    private Integer userType; //用户类型：0-会议参与人员、1-会议组织者、2-会议主持人、3-会议任务负责人、4-会议任务检查人
    private Integer acceptFlag; //是否接受会议：0-接受、1-不接受，会议参会人员可修改，该字段对会议任务无效
    private Integer createId; //创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer updateId; //更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer state; //状态：0-正常、-9-删除
    @Transient
    private String name ;//涉及人员姓名
    @Transient
    private String nameId;
    @Transient
    private Integer deptId;//用户部门id
    @Transient
    private Integer meetState;//会议状态

}
