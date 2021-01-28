package com.qinfei.qferp.entity.plan;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName UserGroupRelate
 * @Description 用户群组关系表
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:48
 * @Version 1.0
 */
@Table(name = "t_user_group_relate")
public class UserGroupRelate implements Serializable {
    private Integer id; //主键
    private Integer groupId; //群组ID
    private Integer userId; //用户ID
    private Integer state; //状态：0-正常、-9-删除
    private Integer createId; //创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer updateId; //更新者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getCreateId() {
        return createId;
    }

    public void setCreateId(Integer createId) {
        this.createId = createId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "UserGroupRelate{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", userId=" + userId +
                ", state=" + state +
                ", createId=" + createId +
                ", createDate=" + createDate +
                ", updateId=" + updateId +
                ", updateDate=" + updateDate +
                '}';
    }
}
