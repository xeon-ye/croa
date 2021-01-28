package com.qinfei.qferp.entity.media;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Relate;
import com.qinfei.qferp.entity.sys.User;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 审核通过媒体,媒体下单用
 */
@Table(name = "t_media_info")
public class MediaInfo implements Serializable {
    @Id
    private Integer id;

    private String name;

    private String remarks;

    //更新责任人
    private Integer userId;
    //创建者
    private Integer creatorId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;

    private Integer supplierId;

    private String supplierName;
    @Transient
    @Relate(name = Supplier.class, fkName = "supplier_id")
    private Supplier supplier;

    private String picPath;

    private Float commStart;

    private Integer mType;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date d1;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date d2;

    private Integer n1;

    private String c1;

    private String c2;

    private String c3;

    private String c4;

    private String c5;

    private String c6;

    private String c7;

    private String c8;

    private Integer n2;

    private Integer n3;

    private Integer n4;

    private Integer n5;

    private Integer n6;

    private Integer n7;

    private Integer n8;

    private Integer discount;

    private Float f1;

    private Float f2;

    private Float f3;

    private Float f4;

    private Float f5;

    private Float f6;

    private Float f7;

    private Float f8;

    private Float f9;

    private Float f10;

    /**
     * 媒体类型
     */
    @Transient
    private MediaType mediaType;
    /**
     * 责任人
     */
    @Transient
    private User user;
    /**
     * 创建人
     */
    @Transient
    private User creator;
    private Integer state;
    @Transient
    private Map<String, Object> n1Data;
    @Transient
    private Map<String, Object> n2Data;
    @Transient
    private Map<String, Object> n3Data;
    @Transient
    private Map<String, Object> n4Data;
    @Transient
    private Map<String, Object> n5Data;
    @Transient
    private Map<String, Object> n6Data;
    @Transient
    private Map<String, Object> n7Data;
    @Transient
    private Map<String, Object> n8Data;
//    @Transient
//    private Map<String,Object> n2Map;
//    @Transient
//    private Map<String,Object> n2Map;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath == null ? null : picPath.trim();
    }

    public Float getCommStart() {
        return commStart;
    }

    public void setCommStart(Float commStart) {
        this.commStart = commStart;
    }

    public Integer getmType() {
        return mType;
    }

    public void setmType(Integer mType) {
        this.mType = mType;
    }

    public Date getD1() {
        return d1;
    }

    public void setD1(Date d1) {
        this.d1 = d1;
    }

    public Date getD2() {
        return d2;
    }

    public void setD2(Date d2) {
        this.d2 = d2;
    }

    public Integer getN1() {
        return n1;
    }

    public void setN1(Integer n1) {
        this.n1 = n1;
    }

    public String getC1() {
        return c1;
    }

    public void setC1(String c1) {
        this.c1 = c1 == null ? null : c1.trim();
    }

    public String getC2() {
        return c2;
    }

    public void setC2(String c2) {
        this.c2 = c2 == null ? null : c2.trim();
    }

    public String getC3() {
        return c3;
    }

    public void setC3(String c3) {
        this.c3 = c3 == null ? null : c3.trim();
    }

    public String getC4() {
        return c4;
    }

    public void setC4(String c4) {
        this.c4 = c4 == null ? null : c4.trim();
    }

    public String getC5() {
        return c5;
    }

    public void setC5(String c5) {
        this.c5 = c5.trim();
    }

    public String getC6() {
        return c6;
    }

    public void setC6(String c6) {
        this.c6 = c6.trim();
    }

    public String getC7() {
        return c7;
    }

    public void setC7(String c7) {
        this.c7 = c7.trim();
    }

    public String getC8() {
        return c8;
    }

    public void setC8(String c8) {
        this.c8 = c8.trim();
    }

    public Integer getN2() {
        return n2;
    }

    public void setN2(Integer n2) {
        this.n2 = n2;
    }

    public Integer getN3() {
        return n3;
    }

    public void setN3(Integer n3) {
        this.n3 = n3;
    }

    public Integer getN4() {
        return n4;
    }

    public void setN4(Integer n4) {
        this.n4 = n4;
    }

    public Integer getN5() {
        return n5;
    }

    public void setN5(Integer n5) {
        this.n5 = n5;
    }

    public Integer getN6() {
        return n6;
    }

    public void setN6(Integer n6) {
        this.n6 = n6;
    }

    public Integer getN7() {
        return n7;
    }

    public void setN7(Integer n7) {
        this.n7 = n7;
    }

    public Integer getN8() {
        return n8;
    }

    public void setN8(Integer n8) {
        this.n8 = n8;
    }

    public Float getF1() {
        return f1;
    }

    public void setF1(Float f1) {
        this.f1 = f1;
    }

    public Float getF2() {
        return f2;
    }

    public void setF2(Float f2) {
        this.f2 = f2;
    }

    public Float getF3() {
        return f3;
    }

    public void setF3(Float f3) {
        this.f3 = f3;
    }

    public Float getF4() {
        return f4;
    }

    public void setF4(Float f4) {
        this.f4 = f4;
    }

    public Float getF5() {
        return f5;
    }

    public void setF5(Float f5) {
        this.f5 = f5;
    }

    public Float getF6() {
        return f6;
    }

    public void setF6(Float f6) {
        this.f6 = f6;
    }

    public Float getF7() {
        return f7;
    }

    public void setF7(Float f7) {
        this.f7 = f7;
    }

    public Float getF8() {
        return f8;
    }

    public void setF8(Float f8) {
        this.f8 = f8;
    }

    public Float getF9() {
        return f9;
    }

    public void setF9(Float f9) {
        this.f9 = f9;
    }

    public Float getF10() {
        return f10;
    }

    public void setF10(Float f10) {
        this.f10 = f10;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Map<String, Object> getN1Data() {
        return n1Data;
    }

    public void setN1Data(Map<String, Object> n1Data) {
        this.n1Data = n1Data;
    }

    public Map<String, Object> getN2Data() {
        return n2Data;
    }

    public void setN2Data(Map<String, Object> n2Data) {
        this.n2Data = n2Data;
    }

    public Map<String, Object> getN3Data() {
        return n3Data;
    }

    public void setN3Data(Map<String, Object> n3Data) {
        this.n3Data = n3Data;
    }

    public Map<String, Object> getN4Data() {
        return n4Data;
    }

    public void setN4Data(Map<String, Object> n4Data) {
        this.n4Data = n4Data;
    }

    public Map<String, Object> getN5Data() {
        return n5Data;
    }

    public void setN5Data(Map<String, Object> n5Data) {
        this.n5Data = n5Data;
    }

    public Map<String, Object> getN6Data() {
        return n6Data;
    }

    public void setN6Data(Map<String, Object> n6Data) {
        this.n6Data = n6Data;
    }

    public Map<String, Object> getN7Data() {
        return n7Data;
    }

    public void setN7Data(Map<String, Object> n7Data) {
        this.n7Data = n7Data;
    }

    public Map<String, Object> getN8Data() {
        return n8Data;
    }

    public void setN8Data(Map<String, Object> n8Data) {
        this.n8Data = n8Data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaInfo mediaInfo = (MediaInfo) o;
        return Objects.equals(id, mediaInfo.id) &&
                Objects.equals(name, mediaInfo.name) &&
                Objects.equals(remarks, mediaInfo.remarks) &&
                Objects.equals(userId, mediaInfo.userId) &&
                Objects.equals(creatorId, mediaInfo.creatorId) &&
                Objects.equals(updateDate, mediaInfo.updateDate) &&
                Objects.equals(supplierId, mediaInfo.supplierId) &&
                Objects.equals(supplierName, mediaInfo.supplierName) &&
                Objects.equals(supplier, mediaInfo.supplier) &&
                Objects.equals(picPath, mediaInfo.picPath) &&
                Objects.equals(commStart, mediaInfo.commStart) &&
                Objects.equals(mType, mediaInfo.mType) &&
                Objects.equals(d1, mediaInfo.d1) &&
                Objects.equals(d2, mediaInfo.d2) &&
                Objects.equals(n1, mediaInfo.n1) &&
                Objects.equals(c1, mediaInfo.c1) &&
                Objects.equals(c2, mediaInfo.c2) &&
                Objects.equals(c3, mediaInfo.c3) &&
                Objects.equals(c4, mediaInfo.c4) &&
                Objects.equals(c5, mediaInfo.c5) &&
                Objects.equals(c6, mediaInfo.c6) &&
                Objects.equals(c7, mediaInfo.c7) &&
                Objects.equals(c8, mediaInfo.c8) &&
                Objects.equals(n2, mediaInfo.n2) &&
                Objects.equals(n3, mediaInfo.n3) &&
                Objects.equals(n4, mediaInfo.n4) &&
                Objects.equals(n5, mediaInfo.n5) &&
                Objects.equals(n6, mediaInfo.n6) &&
                Objects.equals(n7, mediaInfo.n7) &&
                Objects.equals(n8, mediaInfo.n8) &&
                Objects.equals(discount, mediaInfo.discount) &&
                Objects.equals(f1, mediaInfo.f1) &&
                Objects.equals(f2, mediaInfo.f2) &&
                Objects.equals(f3, mediaInfo.f3) &&
                Objects.equals(f4, mediaInfo.f4) &&
                Objects.equals(f5, mediaInfo.f5) &&
                Objects.equals(f6, mediaInfo.f6) &&
                Objects.equals(f7, mediaInfo.f7) &&
                Objects.equals(f8, mediaInfo.f8) &&
                Objects.equals(f9, mediaInfo.f9) &&
                Objects.equals(f10, mediaInfo.f10) &&
                Objects.equals(mediaType, mediaInfo.mediaType) &&
                Objects.equals(user, mediaInfo.user) &&
                Objects.equals(creator, mediaInfo.creator) &&
                Objects.equals(state, mediaInfo.state) &&
                Objects.equals(n1Data, mediaInfo.n1Data) &&
                Objects.equals(n2Data, mediaInfo.n2Data) &&
                Objects.equals(n3Data, mediaInfo.n3Data) &&
                Objects.equals(n4Data, mediaInfo.n4Data) &&
                Objects.equals(n5Data, mediaInfo.n5Data) &&
                Objects.equals(n6Data, mediaInfo.n6Data) &&
                Objects.equals(n7Data, mediaInfo.n7Data) &&
                Objects.equals(n8Data, mediaInfo.n8Data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, remarks, userId, creatorId, updateDate, supplierId, supplierName, supplier,  picPath, commStart, mType, d1, d2, n1, c1, c2, c3, c4,c5, c6, c7, c8, n2, n3, n4, n5, n6, n7, n8, discount, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, mediaType, user, creator, state, n1Data, n2Data, n3Data, n4Data, n5Data, n6Data, n7Data, n8Data);
    }

    @Override
    public String toString() {
        return "MediaInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", remarks='" + remarks + '\'' +
                ", userId=" + userId +
                ", creatorId=" + creatorId +
                ", updateDate=" + updateDate +
                ", supplierId=" + supplierId +
                ", supplierName='" + supplierName + '\'' +
                ", supplier=" + supplier +
                ", picPath='" + picPath + '\'' +
                ", commStart=" + commStart +
                ", mType=" + mType +
                ", d1=" + d1 +
                ", d2=" + d2 +
                ", n1=" + n1 +
                ", c1='" + c1 + '\'' +
                ", c2='" + c2 + '\'' +
                ", c3='" + c3 + '\'' +
                ", c4='" + c4 + '\'' +
                ", c5='" + c5 + '\'' +
                ", c6='" + c6 + '\'' +
                ", c7='" + c7 + '\'' +
                ", c8='" + c8 + '\'' +
                ", n2=" + n2 +
                ", n3=" + n3 +
                ", n4=" + n4 +
                ", n5=" + n5 +
                ", n6=" + n6 +
                ", n7=" + n7 +
                ", n8=" + n8 +
                ", discount=" + discount +
                ", f1=" + f1 +
                ", f2=" + f2 +
                ", f3=" + f3 +
                ", f4=" + f4 +
                ", f5=" + f5 +
                ", f6=" + f6 +
                ", f7=" + f7 +
                ", f8=" + f8 +
                ", f9=" + f9 +
                ", f10=" + f10 +
                ", state=" + state +
                '}';
    }
}