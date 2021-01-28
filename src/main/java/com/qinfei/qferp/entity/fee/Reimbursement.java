package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name="fee_reimbursement")
public class Reimbursement implements Serializable {

    @Id
    private Integer id ;
    //流程编号
    private String code ;
    //流程类型
    private String type;
    //流程标题
    private String title ;
    //报销人id
    private Integer applyId;
    //报销人名字
    private String applyName;
    //报销部门id
    private Integer deptId;
    //报销部门名字
    private String deptName;
    //报销时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    //支付金额
    private Double  payAmount;
    //支付时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    private Integer payUserId ;
    //收款单位
    private String accountName;
    //收款开户行
    private String accountBankName;
    //收款账号
    private String accountBankNo;
    //出款账号id
    private Integer outAccountId;
    //出款账号名字
    private String outAccountName;

    //一个费用报销对应着多条报销记录
    @Transient
    private List<Reimbursement_d> reimbursementDs;

    //摘要
    private String remark;
    //是否抵冲借款
    private Integer loanQc;
    //未还借款
    private Double unpaidLoan;
    //应报销金额
    private Double reimbursedMoney;
    //实报销金额
    private Double totalMoney;
    //实报销金额大写
    private String sumUpper;

    //更新人id
    private Integer updateUserId;

    private String taskId;
    private Integer state;
    //待办事项
    private Integer itemId;

    //附件的名字
    private String affixName;
    //附件的路径
    private String affixLink;
    @Transient
    private String [] links;
    @Transient
    private String [] names;
    //借款的id
    private Integer borrowId;
    //公司代码
    private String companyCode;
    //审批人
    private String approverId;
    //关联借款标志位
    private Integer borrowFlag ;
    //出差id
    private Integer administrativeId;
    //采购Id
    @Transient
    private String purchaseIds;

    public Integer getBorrowFlag() {
        return borrowFlag;
    }

    public void setBorrowFlag(Integer borrowFlag) {
        this.borrowFlag = borrowFlag;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountBankName() {
        return accountBankName;
    }

    public void setAccountBankName(String accountBankName) {
        this.accountBankName = accountBankName;
    }

    public String getAccountBankNo() {
        return accountBankNo;
    }

    public void setAccountBankNo(String accountBankNo) {
        this.accountBankNo = accountBankNo;
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

    public List<Reimbursement_d> getReimbursementDs() {
        return reimbursementDs;
    }

    public void setReimbursementDs(List<Reimbursement_d> reimbursementDs) {
        this.reimbursementDs = reimbursementDs;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getLoanQc() {
        return loanQc;
    }

    public void setLoanQc(Integer loanQc) {
        this.loanQc = loanQc;
    }

    public Double getUnpaidLoan() {
        return unpaidLoan;
    }

    public void setUnpaidLoan(Double unpaidLoan) {
        this.unpaidLoan = unpaidLoan;
    }

    public Double getReimbursedMoney() {
        return reimbursedMoney;
    }

    public void setReimbursedMoney(Double reimbursedMoney) {
        this.reimbursedMoney = reimbursedMoney;
    }

    public Double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(Double totalMoney) {
        this.totalMoney = totalMoney;
    }

    public String getSumUpper() {
        return sumUpper;
    }

    public void setSumUpper(String sumUpper) {
        this.sumUpper = sumUpper;
    }

    public Integer getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Integer updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
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

    public Integer getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Integer borrowId) {
        this.borrowId = borrowId;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String[] getLinks() {
        return links;
    }

    public void setLinks(String[] links) {
        this.links = links;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public Integer getPayUserId() {
        return payUserId;
    }

    public void setPayUserId(Integer payUserId) {
        this.payUserId = payUserId;
    }

    public String getPurchaseIds() {
        return purchaseIds;
    }

    public void setPurchaseIds(String purchaseIds) {
        this.purchaseIds = purchaseIds;
    }

    @Override
    public String toString() {
        return "Reimbursement{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", applyId=" + applyId +
                ", applyName='" + applyName + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", applyTime=" + applyTime +
                ", payAmount=" + payAmount +
                ", payTime=" + payTime +
                ", payUserId=" + payUserId +
                ", accountName='" + accountName + '\'' +
                ", accountBankName='" + accountBankName + '\'' +
                ", accountBankNo='" + accountBankNo + '\'' +
                ", outAccountId=" + outAccountId +
                ", outAccountName='" + outAccountName + '\'' +
                ", reimbursementDs=" + reimbursementDs +
                ", remark='" + remark + '\'' +
                ", loanQc=" + loanQc +
                ", unpaidLoan=" + unpaidLoan +
                ", reimbursedMoney=" + reimbursedMoney +
                ", totalMoney=" + totalMoney +
                ", sumUpper='" + sumUpper + '\'' +
                ", updateUserId=" + updateUserId +
                ", taskId='" + taskId + '\'' +
                ", state=" + state +
                ", itemId=" + itemId +
                ", affixName='" + affixName + '\'' +
                ", affixLink='" + affixLink + '\'' +
                ", borrowId=" + borrowId +
                '}';
    }

    public Integer getAdministrativeId() {
        return administrativeId;
    }

    public void setAdministrativeId(Integer administrativeId) {
        this.administrativeId = administrativeId;
    }
}
