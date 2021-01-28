package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName MediaExtendAudit
 * @Description  媒体扩展审核表
 * @Author xuxiong
 * @Date 2019/6/25 0025 18:25
 * @Version 1.0
 */
@Table(name = "t_media_extend_audit")
public class MediaExtendAudit implements Serializable {
    private Integer id; //主键
    private Integer mediaId; //对应媒体id
    private String cell; //列名称
    private String cellName; //列描述
    private String cellValue; //列对应的值
    private String cellValueText; //列对应的中文值，主要用于扩展字段中单选框、复选框、下拉列表中文描述
    private String dbType; //列数据类型
    private String type; //控件类型
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间'
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer isDelete; //逻辑删除 0 false 正常 1 true 删除
    private Integer versions; //版本号

    //以下是非数据库字段
    @Transient
    private boolean editAddStatus; //编辑媒体时新增的字段

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

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getCellValue() {
        return cellValue;
    }

    public void setCellValue(String cellValue) {
        this.cellValue = cellValue;
    }

    public String getCellValueText() {
        return cellValueText;
    }

    public void setCellValueText(String cellValueText) {
        this.cellValueText = cellValueText;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getVersions() {
        return versions;
    }

    public void setVersions(Integer versions) {
        this.versions = versions;
    }

    public boolean isEditAddStatus() {
        return editAddStatus;
    }

    public void setEditAddStatus(boolean editAddStatus) {
        this.editAddStatus = editAddStatus;
    }

    @Override
    public String toString() {
        return "MediaExtendAudit{" +
                "id=" + id +
                ", mediaId=" + mediaId +
                ", cell='" + cell + '\'' +
                ", cellName='" + cellName + '\'' +
                ", cellValue='" + cellValue + '\'' +
                ", cellValueText='" + cellValueText + '\'' +
                ", dbType='" + dbType + '\'' +
                ", type='" + type + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", isDelete=" + isDelete +
                ", versions=" + versions +
                ", editAddStatus=" + editAddStatus +
                '}';
    }
}
