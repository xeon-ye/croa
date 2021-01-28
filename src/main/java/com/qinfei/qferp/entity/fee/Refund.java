package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name="fee_refund")
public class Refund implements Serializable {
    @Id
    private Integer id ;
    private String code ;
    private String title ;
    private Integer type ;
    private Integer custCompanyId ;
    private String custCompanyName ;
    private Integer custId ;
    private String custName ;
    private Integer accountId ;
    private String accountName ;
    private String accountBankNo ;
    private String accountBankName ;
    private Integer applyId ;
    private String applyName ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime ;
    private Double applyAmount ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date expertPayTime ;
    private Integer outAccountId ;
    private String outAccountName ;
    private Double payAmount ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date payTime ;
    private Integer payUserId ;
    private String remark ;
    private String affix ;
    private Integer state ;
    private Integer creator ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;
    private Integer updateUserId ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime ;
    private Integer deptId;
    private String deptName ;
    private String taskId ;
    private Integer itemId ;
    private String companyCode ;
    private String affixName;
    private String affixLink;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCustCompanyId() {
        return custCompanyId;
    }

    public void setCustCompanyId(Integer custCompanyId) {
        this.custCompanyId = custCompanyId;
    }

    public String getCustCompanyName() {
        return custCompanyName;
    }

    public void setCustCompanyName(String custCompanyName) {
        this.custCompanyName = custCompanyName;
    }

    public Integer getCustId() {
        return custId;
    }

    public void setCustId(Integer custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountBankNo() {
        return accountBankNo;
    }

    public void setAccountBankNo(String accountBankNo) {
        this.accountBankNo = accountBankNo;
    }

    public String getAccountBankName() {
        return accountBankName;
    }

    public void setAccountBankName(String accountBankName) {
        this.accountBankName = accountBankName;
    }

    public Integer getApplyId() {
        return applyId;
    }

    public void setApplyId(Integer applyId) {
        this.applyId = applyId;
    }

    public String getApplyName() {
        return applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Double getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(Double applyAmount) {
        this.applyAmount = applyAmount;
    }

    public Date getExpertPayTime() {
        return expertPayTime;
    }

    public void setExpertPayTime(Date expertPayTime) {
        this.expertPayTime = expertPayTime;
    }

    public Integer getOutAccountId() {
        return outAccountId;
    }

    public void setOutAccountId(Integer outAccountId) {
        this.outAccountId = outAccountId;
    }

    public String getOutAccountName() {
        return outAccountName;
    }

    public void setOutAccountName(String outAccountName) {
        this.outAccountName = outAccountName;
    }

    public Double getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Double payAmount) {
        this.payAmount = payAmount;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public Integer getPayUserId() {
        return payUserId;
    }

    public void setPayUserId(Integer payUserId) {
        this.payUserId = payUserId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAffix() {
        return affix;
    }

    public void setAffix(String affix) {
        this.affix = affix;
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

    public String getAffixName() {
        return affixName;
    }

    public void setAffixName(String affixName) {
        this.affixName = affixName;
    }

    public String getAffixLink() {
        return affixLink;
    }

    public void setAffixLink(String affixLink) {
        this.affixLink = affixLink;
    }

    @Override
    public String toString() {
        return "Refund{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", custCompanyId=" + custCompanyId +
                ", custCompanyName='" + custCompanyName + '\'' +
                ", custId=" + custId +
                ", custName='" + custName + '\'' +
                ", accountId=" + accountId +
                ", accountName='" + accountName + '\'' +
                ", accountBankNo='" + accountBankNo + '\'' +
                ", accountBankName='" + accountBankName + '\'' +
                ", applyId=" + applyId +
                ", applyName='" + applyName + '\'' +
                ", applyTime=" + applyTime +
                ", applyAmount=" + applyAmount +
                ", expertPayTime=" + expertPayTime +
                ", outAccountId=" + outAccountId +
                ", outAccountName='" + outAccountName + '\'' +
                ", payAmount=" + payAmount +
                ", payTime=" + payTime +
                ", payUserId=" + payUserId +
                ", remark='" + remark + '\'' +
                ", affix='" + affix + '\'' +
                ", state=" + state +
                ", creator=" + creator +
                ", createTime=" + createTime +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", taskId='" + taskId + '\'' +
                ", itemId=" + itemId +
                ", companyCode='" + companyCode + '\'' +
                ", affixName='" + affixName + '\'' +
                ", affixLink='" + affixLink + '\'' +
                '}';
    }
}
