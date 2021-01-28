package com.qinfei.qferp.entity.sys;

import com.alibaba.fastjson.annotation.JSONField;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Relate;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Table(name = "sys_resource")
@Getter
@Setter
public class Resource implements Serializable {
    @Id
    private Integer id;

    private Integer parentId;

    private String name;

    private String url;

    private String icon;

    private Integer state;
    private Integer isMenu;

    private Integer creator;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Integer updateUserId;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private Integer sort;
    @Transient
    private User user;
    @Transient
    private User updateUser;
    //    @Transient
    @Relate(name = Resource.class, fkName = "parent_id")
    private Resource parent;
    @Transient
    private List<Resource> childs = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(id, resource.id) &&
                Objects.equals(parentId, resource.parentId) &&
                Objects.equals(name, resource.name) &&
                Objects.equals(url, resource.url) &&
                Objects.equals(icon, resource.icon) &&
                Objects.equals(state, resource.state) &&
                Objects.equals(isMenu, resource.isMenu) &&
                Objects.equals(creator, resource.creator) &&
                Objects.equals(createTime, resource.createTime) &&
                Objects.equals(updateUserId, resource.updateUserId) &&
                Objects.equals(updateTime, resource.updateTime) &&
                Objects.equals(sort, resource.sort) &&
                Objects.equals(user, resource.user) &&
                Objects.equals(updateUser, resource.updateUser) &&
                Objects.equals(parent, resource.parent) &&
                Objects.equals(childs, resource.childs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, name, url, icon, state, isMenu, creator, createTime, updateUserId, updateTime, sort, user, updateUser, parent, childs);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", icon='" + icon + '\'' +
                ", state=" + state +
                ", isMenu=" + isMenu +
                ", creator=" + creator +
                ", createTime=" + createTime +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                ", sort=" + sort +
                ", user=" + user +
                ", updateUser=" + updateUser +
                ", parent=" + parent +
                '}';
    }
}