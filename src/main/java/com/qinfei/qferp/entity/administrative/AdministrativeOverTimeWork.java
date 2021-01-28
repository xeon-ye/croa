package com.qinfei.qferp.entity.administrative;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.employ.EmployCommon;

import java.util.Date;

public class AdministrativeOverTimeWork extends EmployCommon {
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

    //加班时间
    private Double workTime;

    /**
     * 图片；
     */
    private String picture;

    /**
     * 附件；
     */
    private String attachment;

    //图片保存地址
    private String pictureLink;

    //附件保存地址
    private String attachmentLink;

    //行政流程id
    private Integer administrativeId;

    //任务流程id
    @Transient
    private String taskId;




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

    public Double getWorkTime() {
        return workTime;
    }

    public void setWorkTime(Double workTime) {
        this.workTime = workTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getAdministrativeId() {
        return administrativeId;
    }

    public void setAdministrativeId(Integer administrativeId) {
        this.administrativeId = administrativeId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "AdministrativeOverTimeWork{" +
                "id=" + id +
                ", empId=" + empId +
                ", empName='" + empName + '\'' +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", reason='" + reason + '\'' +
                ", workTime=" + workTime +
                ", picture='" + picture + '\'' +
                ", attachment='" + attachment + '\'' +
                ", pictureLink='" + pictureLink + '\'' +
                ", attachmentLink='" + attachmentLink + '\'' +
                ", administrativeId=" + administrativeId +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}