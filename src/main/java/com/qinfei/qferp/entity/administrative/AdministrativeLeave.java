package com.qinfei.qferp.entity.administrative;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.employ.EmployCommon;

import java.util.Date;

public class AdministrativeLeave extends EmployCommon {
    /**
     * 主键ID；
     */
    private Integer id;

    /**
     * 员工ID；
     */
    private Integer empId;

    /**
     * 员工姓名；
     */
    private String empName;

    /**
     * 开始时间；
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    /**
     * 结束时间；
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 标题；
     */
    private String title;

    /**
     * 原因；
     */
    private String reason;

    /**
     * 图片；
     */
    private String picture;

    //图片的存储路径
    private String pictureLink;

    /**
     * 附件；
     */
    private String attachment;

    //附件的存储路径
    private String attachmentLink;

    /**
     * 请假类型；
     */
    private Integer leaveType;

    /**
     * 行政流程id；
     */
    private Integer administrativeId;

    @Transient
    private Integer leaveDay;

    //请假时长
    private Double leaveTime;

    @Transient
    private String taskId;

    //请假时长
    private Double leaveDays;

    private Double vacationTime;

    /**
     * 主键ID；
     * @return ：id 主键ID；
     */
    public Integer getId() {
        return id;
    }

    /**
     * 主键ID；
     * @param id：主键ID；
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 员工ID；
     * @return ：emp_id 员工ID；
     */
    public Integer getEmpId() {
        return empId;
    }

    /**
     * 员工ID；
     * @param empId：员工ID；
     */
    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    /**
     * 员工姓名；
     * @return ：emp_name 员工姓名；
     */
    public String getEmpName() {
        return empName;
    }

    /**
     * 员工姓名；
     * @param empName：员工姓名；
     */
    public void setEmpName(String empName) {
        this.empName = empName == null ? null : empName.trim();
    }

    /**
     * 开始时间；
     * @return ：begin_time 开始时间；
     */
    public Date getBeginTime() {
        return beginTime;
    }

    /**
     * 开始时间；
     * @param beginTime：开始时间；
     */
    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    /**
     * 结束时间；
     * @return ：end_time 结束时间；
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * 结束时间；
     * @param endTime：结束时间；
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * 标题；
     * @return ：title 标题；
     */
    public String getTitle() {
        return title;
    }

    /**
     * 标题；
     * @param title：标题；
     */
    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    /**
     * 原因；
     * @return ：reason 原因；
     */
    public String getReason() {
        return reason;
    }

    /**
     * 原因；
     * @param reason：原因；
     */
    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    /**
     * 图片；
     * @return ：picture 图片；
     */
    public String getPicture() {
        return picture;
    }

    /**
     * 图片；
     * @param picture：图片；
     */
    public void setPicture(String picture) {
        this.picture = picture == null ? null : picture.trim();
    }

    /**
     * 附件；
     * @return ：attachment 附件；
     */
    public String getAttachment() {
        return attachment;
    }

    /**
     * 附件；
     * @param attachment：附件；
     */
    public void setAttachment(String attachment) {
        this.attachment = attachment == null ? null : attachment.trim();
    }


    public Integer getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(Integer leaveType) {
        this.leaveType = leaveType;
    }


    public Integer getLeaveDay() {
        return leaveDay;
    }

    public void setLeaveDay(Integer leaveDay) {
        this.leaveDay = leaveDay;
    }

    public Integer getAdministrativeId() {
        return administrativeId;
    }

    public void setAdministrativeId(Integer administrativeId) {
        this.administrativeId = administrativeId;
    }

    public Double getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(Double leaveTime) {
        this.leaveTime = leaveTime;
    }

    public String getPictureLink() {
        return pictureLink;
    }

    public void setPictureLink(String pictureLink) {
        this.pictureLink = pictureLink;
    }

    public String getAttachmentLink() {
        return attachmentLink;
    }

    public void setAttachmentLink(String attachmentLink) {
        this.attachmentLink = attachmentLink;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Double getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(Double leaveDays) {
        this.leaveDays = leaveDays;
    }

    public Double getVacationTime() {
        return vacationTime;
    }

    public void setVacationTime(Double vacationTime) {
        this.vacationTime = vacationTime;
    }

    @Override
    public String toString() {
        return "AdministrativeLeave{" +
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
                ", leaveType=" + leaveType +
                ", administrativeId=" + administrativeId +
                ", leaveDay=" + leaveDay +
                ", leaveTime=" + leaveTime +
                ", taskId='" + taskId + '\'' +
                ", leaveDays=" + leaveDays +
                ", vacationTime=" + vacationTime +
                '}';
    }
}