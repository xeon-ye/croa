package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Table(name = "t_assistant_dict")
@Setter
@Getter
public class TaxUser implements Serializable {
    private Integer id;

    private Integer assistantUserId;//财务助理id assistantUserId

    private String assistantUserName;//财务助理名

    private Integer dictId;//税种id

    private Integer createUserId;//创建人id

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;//创建时间

    private Integer updateUserId;//更新时间

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private Integer state;//状态

    @Transient
    private String name;

    @Override
    public String toString() {
        return "TaxUser{" +
                "id=" + id +
                ", assistantUserId=" + assistantUserId +
                ", assistantUserName='" + assistantUserName + '\'' +
                ", dictId=" + dictId +
                ", createUserId=" + createUserId +
                ", createTime=" + createTime +
                ", updateUserId=" + updateUserId +
                ", update_time=" + updateTime +
                ", state=" + state +
                '}';
    }
}
