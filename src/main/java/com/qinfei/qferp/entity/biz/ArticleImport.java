package com.qinfei.qferp.entity.biz;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.media.MediaType;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

@Table(name = "t_biz_article_import")
@Alias("articleImport")
public class ArticleImport implements Serializable {
    @Id
    private Integer id;

    private Integer mediaTypeId ;

    private String mediaTypeName ;

    private Integer mediaId;

    private String mediaName;

    private Integer supplierId;

    private String supplierName;

    private String supplierContactor ;

    private Integer mediaUserId;

    private String mediaUserName;

    @JSONField(format = "yyyy-MM-dd")
    private Date issuedDate;

    private String title;

    private String link;

    private Integer num ;

    private String priceColumn ;

    private String priceType ;

    private Double payAmount ;

    private Double saleAmount ;

    private Integer issueStates ;

    private Integer state ;

    private Integer creator ;
    @JSONField(format = "yyyy-MM-dd")
    private Date createTime ;

    private Integer updateUserId ;
    @JSONField(format = "yyyy-MM-dd")
    private Date updateTime ;

    private Integer userId ;

    private String userName ;

    private String remark ;

    private String brand ;

    private String innerOuter;

    private String channel;

    private String enectrictityBusinesses;


    public String getEnectrictityBusinesses() {
        return enectrictityBusinesses;
    }

    public void setEnectrictityBusinesses(String enectrictityBusinesses) {
        this.enectrictityBusinesses = enectrictityBusinesses;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getInnerOuter() {
        return innerOuter;
    }

    public void setInnerOuter(String innerOuter) {
        this.innerOuter = innerOuter;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMediaId() {
        return mediaId;
    }

    public void setMediaId(Integer mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getMediaUserId() {
        return mediaUserId;
    }

    public void setMediaUserId(Integer mediaUserId) {
        this.mediaUserId = mediaUserId;
    }

    public String getMediaUserName() {
        return mediaUserName;
    }

    public void setMediaUserName(String mediaUserName) {
        this.mediaUserName = mediaUserName;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getPriceColumn() {
        return priceColumn;
    }

    public void setPriceColumn(String priceColumn) {
        this.priceColumn = priceColumn;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public Double getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Double payAmount) {
        this.payAmount = payAmount;
    }

    public Double getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(Double saleAmount) {
        this.saleAmount = saleAmount;
    }

    public Integer getIssueStates() {
        return issueStates;
    }

    public void setIssueStates(Integer issueStates) {
        this.issueStates = issueStates;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Integer updateUserId) {
        this.updateUserId = updateUserId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Integer mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public String getMediaTypeName() {
        return mediaTypeName;
    }

    public void setMediaTypeName(String mediaTypeName) {
        this.mediaTypeName = mediaTypeName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSupplierContactor() {
        return supplierContactor;
    }

    public void setSupplierContactor(String supplierContactor) {
        this.supplierContactor = supplierContactor;
    }

    @Override
    public String toString() {
        return "ArticleImport{" +
                "id=" + id +
                ", mediaTypeId=" + mediaTypeId +
                ", mediaTypeName='" + mediaTypeName + '\'' +
                ", mediaId=" + mediaId +
                ", mediaName='" + mediaName + '\'' +
                ", supplierId=" + supplierId +
                ", supplierName='" + supplierName + '\'' +
                ", supplierContactor='" + supplierContactor + '\'' +
                ", mediaUserId=" + mediaUserId +
                ", mediaUserName='" + mediaUserName + '\'' +
                ", issuedDate=" + issuedDate +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", num=" + num +
                ", priceColumn='" + priceColumn + '\'' +
                ", priceType='" + priceType + '\'' +
                ", payAmount=" + payAmount +
                ", saleAmount=" + saleAmount +
                ", issueStates=" + issueStates +
                ", state=" + state +
                ", creator=" + creator +
                ", createTime=" + createTime +
                ", updateUserId=" + updateUserId +
                ", updateTime=" + updateTime +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", remark='" + remark + '\'' +
                ", brand='" + brand + '\'' +
                ", innerOuter='" + innerOuter + '\'' +
                ", channel='" + channel + '\'' +
                ", enectrictityBusinesses='" + enectrictityBusinesses + '\'' +
                '}';
    }
}