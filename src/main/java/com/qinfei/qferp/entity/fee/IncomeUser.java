package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Table(name = "fee_income_user")
public class IncomeUser implements Serializable {
    @Id
    private Integer id;
    private Integer incomeId ;
    private Integer userId ;
    private String name ;
    private Integer deptId ;
    private String deptName ;
    private Double receiveAmount ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime ;
    private Double assignAmount ;
    private Double remainAmount ;
    private Integer state ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIncomeId() {
        return incomeId;
    }

    public void setIncomeId(Integer incomeId) {
        this.incomeId = incomeId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Double getReceiveAmount() {
        return receiveAmount;
    }

    public void setReceiveAmount(Double receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Double getAssignAmount() {
        return assignAmount;
    }

    public void setAssignAmount(Double assignAmount) {
        this.assignAmount = assignAmount;
    }

    public Double getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(Double remainAmount) {
        this.remainAmount = remainAmount;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "IncomeUser{" +
                "id=" + id +
                ", incomeId=" + incomeId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", receiveAmount=" + receiveAmount +
                ", receiveTime=" + receiveTime +
                ", assignAmount=" + assignAmount +
                ", remainAmount=" + remainAmount +
                ", state=" + state +
                ", createTime=" + createTime +
                '}';
    }
}
