package com.qinfei.qferp.entity.sys;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;

import java.util.Date;

/**
 * 职位实体类
 * @author tsf
 */
@Table(name="sys_post")
public class Post {
    /**
     * 职位id
     */
    @Id
    private Integer id;
    /**
     * 职位编码
     */
    private String code;
    /**
     * 职位名称
     */
    private String name;
    /**
     * 职位介绍
     */
    private String remark;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 创建人
     */
    private Integer creator;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 公司代码
     */
    private String companyCode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", remark='" + remark + '\'' +
                ", state=" + state +
                ", creator=" + creator +
                ", createTime=" + createTime +
                ", companyCode='" + companyCode + '\'' +
                '}';
    }
}
