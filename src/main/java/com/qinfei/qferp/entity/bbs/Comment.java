package com.qinfei.qferp.entity.bbs;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 论坛评论表
 * @author tsf
 */
@Table(name="bbs_comment")
@Getter
@Setter
public class Comment implements Serializable {
    /**
     * 评论id
     */
    @Id
    private Integer id;
    /**
     * 帖子id
     */
    private Integer topicId;
    /**
     * 评论正文
     */
    private String content;
    /**
     * 评论人id
     */
    private Integer userId;
    /**
     * 评论人
     */
    private String userName;
    /**
     * 评论人头像
     */
    private String picture;
    /**
     * 评论时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 上一级评论id
     */
    private Integer parentId;
    /**
     * 评论下是否有回复
     */
    @Transient
    private Integer replyFlag;

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", topicId=" + topicId +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", picture='" + picture + '\'' +
                ", createTime=" + createTime +
                ", parentId=" + parentId +
                '}';
    }
}
