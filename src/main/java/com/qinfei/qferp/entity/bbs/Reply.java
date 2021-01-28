package com.qinfei.qferp.entity.bbs;
import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论回复实体类
 * @author tsf
 */
@Table(name="bbs_reply")
@Getter
@Setter
public class Reply implements Serializable {
    /**
     * 回复id
     */
    @Id
    private Integer id;
    /**
     * 评论id
     */
    private Integer commId;
    /**
     * 回复内容
     */
    private String content;
    /**
     * 主题id
     */
    private Integer topicId;
    /**
     * 评论人id
     */
    private Integer userId;
    /**
     * 评论人姓名
     */
    private String userName;
    /**
     * 回复人id
     */
    private Integer replyUserId;
    /**
     * 回复人姓名
     */
    private String replyUserName;
    /**
     * 回复人头像
     */
    private String picture;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Override
    public String toString() {
        return "Reply{" +
                "id=" + id +
                ", commId=" + commId +
                ", content='" + content + '\'' +
                ", topicId=" + topicId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", replyUserId=" + replyUserId +
                ", replyUserName='" + replyUserName + '\'' +
                ", picture='" + picture + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
