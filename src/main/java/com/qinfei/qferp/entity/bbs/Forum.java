package com.qinfei.qferp.entity.bbs;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 板块实体类
 * @autor tsf
 */
@Table(name = "bbs_forum")
@Getter
@Setter
public class Forum implements Serializable {
    /**
     * 板块id
     */
    @Id
    private Integer id;
    /**
     * 板块名称
     */
    private String name;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 板块负责人
     */
    private Integer moderator;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 公司代码
     */
    private String companyCode;
    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 状态：-9删除
     */
    private Integer state;
    /**
     * 备注
     */
    private String remark;
    /**
     * 附件名称
     */
    private String affixName;
    /**
     * 附件链接
     */
    private String affixLink;
    @Transient
    private String companyCodeName;

    @Override
    public String toString() {
        return "Forum{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", moderator=" + moderator +
                ", createTime=" + createTime +
                ", companyCode='" + companyCode + '\'' +
                ", updateTime=" + updateTime +
                ", state=" + state +
                ", remark='" + remark + '\'' +
                ", affixName='" + affixName + '\'' +
                ", affixLink='" + affixLink + '\'' +
                '}';
    }
}
