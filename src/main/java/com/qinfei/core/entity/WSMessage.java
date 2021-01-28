package com.qinfei.core.entity;

import java.io.Serializable;

/**
 * 消息
 */
public class WSMessage implements Serializable {

    private String code;
    private String content;
    private String sendUserId;
    private String sendUserName;
    private String sendName;
    private String sendUserImage;
    private String receiveUserImage;
    private String receiveUserId;
    private String receiveUserName;
    private String receiveName;
    private String subject;
    private String title;
    private String url;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    /**
     * 消息内容
     *
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }

    public String getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(String receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public String getReceiveUserName() {
        return receiveUserName;
    }

    public void setReceiveUserName(String receiveUserName) {
        this.receiveUserName = receiveUserName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSendUserImage() {
        return sendUserImage;
    }

    public void setSendUserImage(String sendUserImage) {
        this.sendUserImage = sendUserImage;
    }

    public String getReceiveUserImage() {
        return receiveUserImage;
    }

    public void setReceiveUserImage(String receiveUserImage) {
        this.receiveUserImage = receiveUserImage;
    }

    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    //    @Override
//    public String toString() {
//        return "WSMessage{" +
//                "code='" + code + '\'' +
//                ", content='" + content + '\'' +
//                ", sendUserId='" + sendUserId + '\'' +
//                ", sendUserName='" + sendUserName + '\'' +
//                ", receiveUserId='" + receiveUserId + '\'' +
//                ", receiveUserName='" + receiveUserName + '\'' +
//                ", subject='" + subject + '\'' +
//                ", title='" + title + '\'' +
//                '}';
//    }
}
