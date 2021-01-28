package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.qferp.utils.ObjectFieldCompare;

import java.util.Date;
import java.util.Objects;

@Table(name="fee_account")
public class Account {
    @Id
    private Integer id ;
    private Integer type ;
    private Integer companyId ;
    @ObjectFieldCompare.CompareField(fieldName = "供应商公司名称")
    private String companyName ;
    @ObjectFieldCompare.CompareField(fieldName = "账户名称")
    private String name ;
    @ObjectFieldCompare.CompareField(fieldName = "账号")
    private String bankNo ;
    @ObjectFieldCompare.CompareField(fieldName = "开户行")
    private String bankName ;
    @ObjectFieldCompare.CompareField(fieldName = "户主")
    private String owner ;
    @ObjectFieldCompare.CompareField(fieldName = "预留电话")
    private String phone ;
    private Double balance ;
    private Long deptId ;
    private String deptName ;
    private String remark ;
    private Integer state ;
    private Integer creator ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;
    private Integer updateUserId ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime ;
    //对接人
    private Integer dockingId;
    private String taskId ;
    private String accountType ;
    private String companyCode ;
    @ObjectFieldCompare.CompareField(fieldName = "供应商联系人")
    private String contactor ;

    public String getContactor() {
        return contactor;
    }

    public void setContactor(String contactor) {
        this.contactor = contactor;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
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

    public Integer getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Integer updateUserId) {
        this.updateUserId = updateUserId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDockingId() {
        return dockingId;
    }

    public void setDockingId(Integer dockingId) {
        this.dockingId = dockingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
                Objects.equals(type, account.type) &&
                Objects.equals(companyId, account.companyId) &&
                Objects.equals(companyName, account.companyName) &&
                Objects.equals(name, account.name) &&
                Objects.equals(bankNo, account.bankNo) &&
                Objects.equals(bankName, account.bankName) &&
                Objects.equals(owner, account.owner) &&
                Objects.equals(phone, account.phone) &&
                Objects.equals(balance, account.balance) &&
                Objects.equals(deptId, account.deptId) &&
                Objects.equals(deptName, account.deptName) &&
                Objects.equals(remark, account.remark) &&
                Objects.equals(state, account.state) &&
                Objects.equals(creator, account.creator) &&
                Objects.equals(createTime, account.createTime) &&
                Objects.equals(updateUserId, account.updateUserId) &&
                Objects.equals(updateTime, account.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, companyId, companyName, name, bankNo, bankName,  owner, phone, balance, deptId, deptName, remark, state, creator, createTime, updateUserId, updateTime);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", type=" + type +
                ", companyId=" + companyId +
                ", companyName='" + companyName + '\'' +
                ", name='" + name + '\'' +
                ", bankNo='" + bankNo + '\'' +
                ", bankName='" + bankName + '\'' +
                ", owner='" + owner + '\'' +
                ", phone='" + phone + '\'' +
                ", balance=" + balance +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", remark='" + remark + '\'' +
                ", state=" + state +
                ", creator=" + creator +
                ", createTime=" + createTime +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                ", dockingId=" + dockingId +
                ", taskId='" + taskId + '\'' +
                ", accountType='" + accountType + '\'' +
                ", companyCode='" + companyCode + '\'' +
                '}';
    }
}
