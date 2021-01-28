package com.qinfei.qferp.entity.administrative;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 *deng
 */
@Table(name = "t_business_plan")
@Setter
@Getter
public class UserBusinessPlan implements Serializable {
    private Integer id;
    //出差名称
    private String title;
    //行政主表id
    private Integer administrativeId;
    //申请部门id
    private Integer deptId;
    //申请部门
    private String deptName;
    //申请时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date applicationDate;
    //出差地点
    private String place;
    //出差地点类型
    private Integer placeType;
    //事由
    private Integer reason;
    //交通工具
    private Integer traffic;
    //出差开始时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date travelStateTime;
    //出差结束时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date travelEndTime;
    //出差天数
    private Double numberDay;
    //出差责任人
    private String travelUser;
    //原因目标
    private String target;
    //详细行程
    private String trip;
    //费用预算
    private String costBudget;
    //备注
    private String note;
    //申请人id
    private Integer applyId;
    //申请人名
    private String applyName;
    //审核人
    private Integer reviewerUserId;
    //审核人
    private String reviewerUser;
    //批准人id
    private Integer approverUserId;
    //批准人
    private String approverUser;
    //出差类型 单独 或群体
    private Integer separate;
    //同行人
    private String fieldUser;
    //男生数量
    private Integer boy;
    //女数量
    private Integer female;
    //状态
    private Integer state;
    //更新人id
    private Integer updateId;
    //更新人
    private String updateUser;
    //更新时间
    private Date updateTime;
    //创建者
    private Integer createId;
    //创建人
    private String createUser;
    //创建时间
    private Date createTime;
    //图片
    private String picture;
    //图片链接
    private String pictureLink;
    //附件
    private String attachment;
    //附件练级
    private  String attachmentLink;
    @Transient
    private String taskId;
    @Transient
    private Integer taskState;
    @Transient
    private String conclusion;
    @Transient
    //出差计划抄送人的待办id
    private Integer itemId;
    @Transient
    private Integer itemConclusionId;
    @Transient
    private String pic;
    @Transient
    private String picLink;
    @Transient
    private String attach;
    @Transient
    private String attachLink;

    @Override
    public String toString() {
        return "UserBusinessPlan{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", administrativeId=" + administrativeId +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", applicationDate=" + applicationDate +
                ", place='" + place + '\'' +
                ", placeType=" + placeType +
                ", reason=" + reason +
                ", traffic=" + traffic +
                ", travelStateTime=" + travelStateTime +
                ", travelEndTime=" + travelEndTime +
                ", numberDay=" + numberDay +
                ", travelUser='" + travelUser + '\'' +
                ", target='" + target + '\'' +
                ", trip='" + trip + '\'' +
                ", costBudget='" + costBudget + '\'' +
                ", note='" + note + '\'' +
                ", applyId=" + applyId +
                ", applyName='" + applyName + '\'' +
                ", reviewerUserId=" + reviewerUserId +
                ", reviewerUser='" + reviewerUser + '\'' +
                ", approverUserId=" + approverUserId +
                ", approverUser='" + approverUser + '\'' +
                ", separate=" + separate +
                ", fieldUser='" + fieldUser + '\'' +
                ", boy=" + boy +
                ", female=" + female +
                ", state=" + state +
                ", updateId=" + updateId +
                ", updateUser='" + updateUser + '\'' +
                ", updateTime=" + updateTime +
                ", createId=" + createId +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", picture='" + picture + '\'' +
                ", pictureLink='" + pictureLink + '\'' +
                ", attachment='" + attachment + '\'' +
                ", attachmentLink='" + attachmentLink + '\'' +
                ", taskId='" + taskId + '\'' +
                ", taskState=" + taskState +
                ", conclusion='" + conclusion + '\'' +
                ", itemId=" + itemId +
                ", itemConclusionId=" + itemConclusionId +
//                ", pic='" + pic + '\'' +
//                ", picLink='" + picLink + '\'' +
//                ", attach='" + attach + '\'' +
//                ", attachLink='" + attachLink + '\'' +
                '}';
    }
}
