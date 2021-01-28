package com.qinfei.qferp.entity.administrative;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.employ.EmployCommon;
import com.qinfei.qferp.entity.employ.EmployEntryComment;

import java.util.Date;

public class AdministrativeOnBusiness extends EmployCommon {
    private Integer id;

    private String title;

    private Integer empDept;

    private String empDeptName;

    private Integer empId;

    private String empName;

    private String empDuty;

    private Integer agentDept;

    private String agentDeptName;

    private Integer agentId;

    private String agentName;

    private String agentDuty;

    private Integer escortDept;

    private String escortDeptName;

    private Integer escortId;

    private String escortName;

    private String escortDuty;

    private String destination;

    private String reason;

    private String picture;

    private String pictureLink;

    private String attachment;

    private String attachmentLink;

    private String report;

    private String reportLink;

    private Integer administrativeId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date startoffTime;//出发时间

    private Double expense;//借款金额

    private Integer isExpense;//是否借款

    private Double days;//出差时长（天）
    @Transient
    private String taskId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public Integer getEmpDept() {
        return empDept;
    }

    public void setEmpDept(Integer empDept) {
        this.empDept = empDept;
    }

    public String getEmpDeptName() {
        return empDeptName;
    }

    public void setEmpDeptName(String empDeptName) {
        this.empDeptName = empDeptName == null ? null : empDeptName.trim();
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName == null ? null : empName.trim();
    }

    public String getEmpDuty() {
        return empDuty;
    }

    public void setEmpDuty(String empDuty) {
        this.empDuty = empDuty == null ? null : empDuty.trim();
    }

    public Integer getAgentDept() {
        return agentDept;
    }

    public void setAgentDept(Integer agentDept) {
        this.agentDept = agentDept;
    }

    public String getAgentDeptName() {
        return agentDeptName;
    }

    public void setAgentDeptName(String agentDeptName) {
        this.agentDeptName = agentDeptName == null ? null : agentDeptName.trim();
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName == null ? null : agentName.trim();
    }

    public String getAgentDuty() {
        return agentDuty;
    }

    public void setAgentDuty(String agentDuty) {
        this.agentDuty = agentDuty == null ? null : agentDuty.trim();
    }

    public Integer getEscortDept() {
        return escortDept;
    }

    public void setEscortDept(Integer escortDept) {
        this.escortDept = escortDept;
    }


    public Integer getEscortId() {
        return escortId;
    }

    public void setEscortId(Integer escortId) {
        this.escortId = escortId;
    }

    public String getEscortName() {
        return escortName;
    }

    public void setEscortName(String escortName) {
        this.escortName = escortName == null ? null : escortName.trim();
    }

    public String getEscortDuty() {
        return escortDuty;
    }

    public void setEscortDuty(String escortDuty) {
        this.escortDuty = escortDuty == null ? null : escortDuty.trim();
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination == null ? null : destination.trim();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture == null ? null : picture.trim();
    }

    public String getPictureLink() {
        return pictureLink;
    }

    public void setPictureLink(String pictureLink) {
        this.pictureLink = pictureLink == null ? null : pictureLink.trim();
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment == null ? null : attachment.trim();
    }

    public String getAttachmentLink() {
        return attachmentLink;
    }

    public void setAttachmentLink(String attachmentLink) {
        this.attachmentLink = attachmentLink == null ? null : attachmentLink.trim();
    }

    public Integer getAdministrativeId() {
        return administrativeId;
    }

    public void setAdministrativeId(Integer administrativeId) {
        this.administrativeId = administrativeId;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartoffTime() {
        return startoffTime;
    }

    public void setStartoffTime(Date startoffTime) {
        this.startoffTime = startoffTime;
    }

    public Double getExpense() {
        return expense;
    }

    public void setExpense(Double expense) {
        this.expense = expense;
    }

    public Integer getIsExpense() {
        return isExpense;
    }

    public void setIsExpense(Integer isExpense) {
        this.isExpense = isExpense;
    }

    public Double getDays() {
        return days;
    }

    public void setDays(Double days) {
        this.days = days;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getReportLink() {
        return reportLink;
    }

    public void setReportLink(String reportLink) {
        this.reportLink = reportLink;
    }

    public String getEscortDeptName() {
        return escortDeptName;
    }

    public void setEscortDeptName(String escortDeptName) {
        this.escortDeptName = escortDeptName;
    }

    @Override
    public String toString() {
        return "AdministrativeOnBusiness{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", empDept=" + empDept +
                ", empDeptName='" + empDeptName + '\'' +
                ", empId=" + empId +
                ", empName='" + empName + '\'' +
                ", empDuty='" + empDuty + '\'' +
                ", agentDept=" + agentDept +
                ", agentDeptName='" + agentDeptName + '\'' +
                ", agentId=" + agentId +
                ", agentName='" + agentName + '\'' +
                ", agentDuty='" + agentDuty + '\'' +
                ", escortDept=" + escortDept +
                ", escortDeptName='" + escortDeptName + '\'' +
                ", escortId=" + escortId +
                ", escortName='" + escortName + '\'' +
                ", escortDuty='" + escortDuty + '\'' +
                ", destination='" + destination + '\'' +
                ", reason='" + reason + '\'' +
                ", picture='" + picture + '\'' +
                ", pictureLink='" + pictureLink + '\'' +
                ", attachment='" + attachment + '\'' +
                ", attachmentLink='" + attachmentLink + '\'' +
                ", report='" + report + '\'' +
                ", reportLink='" + reportLink + '\'' +
                ", administrativeId=" + administrativeId +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", startoffTime=" + startoffTime +
                ", expense=" + expense +
                ", isExpense=" + isExpense +
                ", days=" + days +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}