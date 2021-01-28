package com.qinfei.qferp.entity.workbench;

import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息读取记录
 */
@Table(name = "t_index_message_read")
public class MessageRead implements Serializable{
    private Integer id;
    private Integer userId;
    private Integer messageId;
    private Date readTime;

    public MessageRead(Integer userId, Integer messageId, Date readTime) {
        this.userId = userId;
        this.messageId = messageId;
        this.readTime = readTime;
    }

    public MessageRead() {
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

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageRead that = (MessageRead) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (messageId != null ? !messageId.equals(that.messageId) : that.messageId != null) return false;
        return readTime != null ? readTime.equals(that.readTime) : that.readTime == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
        result = 31 * result + (readTime != null ? readTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageRead{" +
                "userId=" + userId +
                ", messageId=" + messageId +
                ", readTime=" + readTime +
                '}';
    }
}
