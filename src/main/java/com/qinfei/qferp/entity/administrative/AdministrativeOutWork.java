package com.qinfei.qferp.entity.administrative;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.employ.EmployCommon;

import java.util.Date;

public class AdministrativeOutWork extends EmployCommon {
    private Integer id;

    private Integer empId;

    private String empName;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private String title;

    private String reason;

    private String picture;

    private String pictureLink;

    private String attachment;

    private String attachmentLink;

    private Integer administrativeId;

    private Double time;

    private Double days;
    //任务流程id
    @Transient
    private String taskId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
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

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getDays() {
        return days;
    }

    public void setDays(Double days) {
        this.days = days;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "AdministrativeOutWork{" +
                "id=" + id +
                ", empId=" + empId +
                ", empName='" + empName + '\'' +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", reason='" + reason + '\'' +
                ", picture='" + picture + '\'' +
                ", pictureLink='" + pictureLink + '\'' +
                ", attachment='" + attachment + '\'' +
                ", attachmentLink='" + attachmentLink + '\'' +
                ", administrativeId=" + administrativeId +
                ", time=" + time +
                ", days=" + days +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}