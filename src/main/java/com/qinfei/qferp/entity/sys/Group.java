package com.qinfei.qferp.entity.sys;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Relate;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Table(name = "sys_group")
@Getter
@Setter
public class Group implements Serializable {
    @Id
    private Integer id;

    private String name;

    private Integer state;

    private Integer parentId ;

    private Integer creator;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Integer updateUserId;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Transient
    private User user;
    @Transient
    private User updateUser;
    //    @Transient
    @Relate(name = Group.class, fkName = "parent_id")
    private Group parent;
    @Transient
    private List<Group> childs = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id) &&
                Objects.equals(name, group.name) &&
                Objects.equals(state, group.state) &&
                Objects.equals(parentId, group.parentId) &&
                Objects.equals(creator, group.creator) &&
                Objects.equals(createTime, group.createTime) &&
                Objects.equals(updateUserId, group.updateUserId) &&
                Objects.equals(updateTime, group.updateTime) &&
                Objects.equals(user, group.user) &&
                Objects.equals(updateUser, group.updateUser) &&
                Objects.equals(parent, group.parent) &&
                Objects.equals(childs, group.childs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, state, parentId, creator, createTime, updateUserId, updateTime, user, updateUser, parent, childs);
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", parentId=" + parentId +
                ", creator=" + creator +
                ", createTime=" + createTime +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                ", user=" + user +
                ", updateUser=" + updateUser +
                ", parent=" + parent +
                ", childs=" + childs +
                '}';
    }
}