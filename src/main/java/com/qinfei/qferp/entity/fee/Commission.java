package com.qinfei.qferp.entity.fee;

import com.alibaba.fastjson.annotation.JSONField;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Table(name = "fee_commission")
public class Commission implements Serializable {
    @Id
    private Integer id;
    private Integer userId ;
    private String name ;
    private Integer year ;
    private Integer month ;
    private Double sale=0.0 ;
    private Double income=0.0 ;
    private Double outgo=0.0 ;
    private Double taxExpense=0.0 ;
    private Double refund=0.0;
    private Double otherExpense=0.0 ;
    private Double profit=0.0 ;
    private Double profitPercent ;
    private Double commPercent ;
    private Double comm=0.0 ;
    private Integer state ;
    private String remark ;
    private Integer releaseId ;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime ;
    private Integer deptId;
    private String deptName;
    private String taskId ;
    private Integer itemId ;
    private String companyCode ;

    public Double getSale() {
        return sale;
    }

    public void setSale(Double sale) {
        this.sale = sale;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
    public Double getRefund() {
        return refund;
    }

    public void setRefund(Double refund) {
        this.refund = refund;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Double getIncome() {
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
    }

    public Double getOutgo() {
        return outgo;
    }

    public void setOutgo(Double outgo) {
        this.outgo = outgo;
    }

    public Double getTaxExpense() {
        return taxExpense;
    }

    public void setTaxExpense(Double taxExpense) {
        this.taxExpense = taxExpense;
    }

    public Double getOtherExpense() {
        return otherExpense;
    }

    public void setOtherExpense(Double otherExpense) {
        this.otherExpense = otherExpense;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    public Double getProfitPercent() {
        return profitPercent;
    }

    public void setProfitPercent(Double profitPercent) {
        this.profitPercent = profitPercent;
    }

    public Double getCommPercent() {
        return commPercent;
    }

    public void setCommPercent(Double commPercent) {
        this.commPercent = commPercent;
    }

    public Double getComm() {
        return comm;
    }

    public void setComm(Double comm) {
        this.comm = comm;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(Integer releaseId) {
        this.releaseId = releaseId;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    @Override
    public String toString() {
        return "Commission{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", sale=" + sale +
                ", income=" + income +
                ", outgo=" + outgo +
                ", taxExpense=" + taxExpense +
                ", refund=" + refund +
                ", otherExpense=" + otherExpense +
                ", profit=" + profit +
                ", profitPercent=" + profitPercent +
                ", commPercent=" + commPercent +
                ", comm=" + comm +
                ", state=" + state +
                ", remark='" + remark + '\'' +
                ", releaseId=" + releaseId +
                ", releaseTime=" + releaseTime +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", taskId='" + taskId + '\'' +
                ", itemId=" + itemId +
                ", companyCode='" + companyCode + '\'' +
                '}';
    }
}
