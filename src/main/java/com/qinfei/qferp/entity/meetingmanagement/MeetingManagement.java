package com.qinfei.qferp.entity.meetingmanagement;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;

import java.util.Date;

/**
 * 会议室管理
 *
 * @author  dengshenggeng
 */
@Table(name="meeting_management")
public class MeetingManagement {
    @Id
    private Integer id;

    private String conferenceRoomName;

    private String photos;

    private String place;

    private Integer accommodateNumber;

    private Integer state;

    private String describe;

    private Date createName;

    private Integer createId;

    private Date createTime;

    private Date updateName;

    private Integer updateId;

    private Date updateTime;

    private String  meetingRoomId;

    private String meetingRoomUser;

    private Integer reviewerId;

    private Integer reviewerDeptId;

    private String reviewerName;

    private String taskId;

    private  Integer itemId;

    public Integer getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Integer getReviewerDeptId() {
        return reviewerDeptId;
    }

    public void setReviewerDeptId(Integer reviewerDeptId) {
        this.reviewerDeptId = reviewerDeptId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }



    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(String approval) {
        this.approval = approval;
    }

    private String approval;


    public String getReservation() {
        return reservation;
    }

    public void setReservation(String reservation) {
        this.reservation = reservation;
    }

    private String reservation;

    public String getPhotosLink() {
        return photosLink;
    }


    public void setPhotosLink(String photosLink) {
        this.photosLink = photosLink;
    }

    private String photosLink;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConferenceRoomName() {
        return conferenceRoomName;
    }

    public void setConferenceRoomName(String conferenceRoomName) {
        this.conferenceRoomName = conferenceRoomName == null ? null : conferenceRoomName.trim();
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos == null ? null : photos.trim();
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place == null ? null : place.trim();
    }

    public Integer getAccommodateNumber() {
        return accommodateNumber;
    }

    public void setAccommodateNumber(Integer accommodateNumber) {
        this.accommodateNumber = accommodateNumber;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe == null ? null : describe.trim();
    }

    public Date getCreateName() {
        return createName;
    }

    public void setCreateName(Date createName) {
        this.createName = createName;
    }

    public Integer getCreateId() {
        return createId;
    }

    public void setCreateId(Integer createId) {
        this.createId = createId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateName() {
        return updateName;
    }

    public void setUpdateName(Date updateName) {
        this.updateName = updateName;
    }

    public Integer getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getMeetingRoomId() {
        return meetingRoomId;
    }

    public void setMeetingRoomId(String meetingRoomId) {
        this.meetingRoomId = meetingRoomId;
    }

    public String getMeetingRoomUser() {
        return meetingRoomUser;
    }

    public void setMeetingRoomUser(String meetingRoomUser) {
        this.meetingRoomUser = meetingRoomUser == null ? null : meetingRoomUser.trim();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "MeetingManagement{" +
                "id=" + id +
                ", conferenceRoomName='" + conferenceRoomName + '\'' +
                ", photos='" + photos + '\'' +
                ", place='" + place + '\'' +
                ", accommodateNumber=" + accommodateNumber +
                ", state=" + state +
                ", describe='" + describe + '\'' +
                ", createName=" + createName +
                ", createId=" + createId +
                ", createTime=" + createTime +
                ", updateName=" + updateName +
                ", updateId=" + updateId +
                ", updateTime=" + updateTime +
                ", meetingRoomId='" + meetingRoomId + '\'' +
                ", meetingRoomUser='" + meetingRoomUser + '\'' +
                ", reviewerId=" + reviewerId +
                ", reviewerDeptId=" + reviewerDeptId +
                ", reviewerName='" + reviewerName + '\'' +
                ", taskId='" + taskId + '\'' +
                ", itemId=" + itemId +
                ", approval='" + approval + '\'' +
                ", reservation='" + reservation + '\'' +
                ", photosLink='" + photosLink + '\'' +
                '}';
    }
}