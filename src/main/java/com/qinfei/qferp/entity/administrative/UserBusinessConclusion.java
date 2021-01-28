package com.qinfei.qferp.entity.administrative;


import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Table(name = "t_business_conclusion")
@Setter
@Getter
public class UserBusinessConclusion implements Serializable {
    private Integer id;

    private Integer planId;

    private Integer administrativeId;

    private String conclusion;

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
    private String pic;
    //图片链接
    private String picLink;
    //附件
    private String attach;
    //附件练级
    private  String attachLink;

    private Integer ItemConclusionId;

    @Transient
    private Integer taskIds;

    @Override
    public String toString() {
        return "UserBusinessConclusion{" +
                "id=" + id +
                ", planId=" + planId +
                ", administrativeId=" + administrativeId +
                ", conclusion='" + conclusion + '\'' +
                ", state=" + state +
                ", updateId=" + updateId +
                ", updateUser='" + updateUser + '\'' +
                ", updateTime=" + updateTime +
                ", createId=" + createId +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", pic='" + pic + '\'' +
                ", picLink='" + picLink + '\'' +
                ", attach='" + attach + '\'' +
                ", attachLink='" + attachLink + '\'' +
                ", ItemConclusionId='" + ItemConclusionId + '\'' +
                ", taskIds=" + taskIds +
                '}';
    }
}
