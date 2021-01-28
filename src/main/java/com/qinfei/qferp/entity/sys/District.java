package com.qinfei.qferp.entity.sys;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 地区实体类
 */
@Table(name = "sys_district")
public class District implements Serializable {
    @Id
    private Integer id;

    private String name;

    private Integer areaCode;

    private Integer postCode;

    private Integer parentId;

    private String shortName;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Short sort;

    private Date createTime;

    private Byte state;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode;
    }

    public Integer getPostCode() {
        return postCode;
    }

    public void setPostCode(Integer postCode) {
        this.postCode = postCode;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName == null ? null : shortName.trim();
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public Short getSort() {
        return sort;
    }

    public void setSort(Short sort) {
        this.sort = sort;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "District{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", areaCode=" + areaCode +
                ", postCode=" + postCode +
                ", parentId=" + parentId +
                ", shortName='" + shortName + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", sort=" + sort +
                ", createTime=" + createTime +
                ", state=" + state +
                '}';
    }
}