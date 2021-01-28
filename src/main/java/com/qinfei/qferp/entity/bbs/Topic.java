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
 * 板块实体类
 * @autor tsf
 */
@Table(name = "bbs_topic")
@Getter
@Setter
public class Topic implements Serializable {
    /**
     * 帖子id
     */
    @Id
    private Integer id;
    /**
     * 论坛板块id
     */
    private Integer forumId;
    /**
     * 帖子标题
     */
    private String title;
    /**
     * 发帖内容
     */
    private String content;
    /**
     * 发帖人
     */
    private Integer userId;
    /**
     * 发帖人姓名
     */
    private String userName;
    /**
     * 修改人id
     */
    private Integer updateUserId;
    /**
     * 修改人姓名
     */
    private String  updateUserName;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 附件名称
     */
    private String affixName;
    /**
     * 附件链接
     */
    private String affixLink;
    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 置顶标志
     */
    private Integer sort;
    /**
     * 是否加精
     */
    private Integer level;
    /**
     * 公司代码
     */
    private String companyCode;
    /**
     * 发帖人头像
     */
    private String picture;
    /**
     * 删帖原因
     */
    private String delReason;
    /**
     * 置顶原因
     */
    private String topReason;
    /**
     * 加精原因
     */
    private String highReason;
    /**
     * 加精标志
     */
    private Integer highState;
    /**
     * 置顶标志
     */
    private Integer topState;
    /**
     * 首推图片标志
     */
    private Integer imageFlag;
    /**
     * 图片路径
     */
    private String imageUrl;
    /**
     * 图片名称
     */
    private String imageName;
    /**
     * 图片标志
     */
    private Integer imageSign;
    //板块名称
    @Transient
    private String forumName;
    @Transient
    private Integer viewNum;
    @Transient
    private Integer likeNum;
    @Transient
    private Integer dislikeNum;
    //热度
    @Transient
    private Integer hot;
    @Transient
    private Integer commNum;
    //部分可见人员集合
    @Transient
    private String users;
    //部分评论用户集合
    @Transient
    private String commentUsers;
    @Transient
    private Integer viewSetting;
    //全部可见评论用户集合
    @Transient
    private String commUserIds;

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", forumId=" + forumId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", updateUserId=" + updateUserId +
                ", updateUserName='" + updateUserName + '\'' +
                ", createTime=" + createTime +
                ", affixName='" + affixName + '\'' +
                ", affixLink='" + affixLink + '\'' +
                ", updateTime=" + updateTime +
                ", state=" + state +
                ", sort=" + sort +
                ", level=" + level +
                ", companyCode='" + companyCode + '\'' +
                ", picture='" + picture + '\'' +
                ", delReason='" + delReason + '\'' +
                ", topReason='" + topReason + '\'' +
                ", highReason='" + highReason + '\'' +
                ", highState=" + highState +
                ", topState=" + topState +
                ", imageFlag=" + imageFlag +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imageSign=" + imageSign +
                '}';
    }
}
