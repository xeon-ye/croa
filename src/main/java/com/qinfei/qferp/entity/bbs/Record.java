package com.qinfei.qferp.entity.bbs;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author tsf
 * 点赞记录实体类
 */
@Table(name="bbs_record")
@Getter
@Setter
public class Record {
    /**
     * 帖子id
     */
    private Integer topicId;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 点赞状态
     */
    private Integer state;
    /**
     * 点赞数
     */
    @Transient
    private Integer likeNum;
    /**
     * 不支持数
     */
    @Transient
    private Integer dislikeNum;
    /**
     * 浏览数
     */
    @Transient
    private Integer viewNum;

    @Override
    public String toString() {
        return "Record{" +
                "topicId=" + topicId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", createTime=" + createTime +
                ", state=" + state +
                '}';
    }
}
