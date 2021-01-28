package com.qinfei.qferp.entity.media1;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaTerm1
 * @Description 媒体条件
 * @Author xuxiong
 * @Date 2019/7/6 0006 15:15
 * @Version 1.0
 */
@Table(name = "t_media_term1")
public class MediaTerm1 implements Serializable {
    private Integer id; //主键
    private String cell; //对应t_media_extend表cell字段值，用于查询条件
    private String cellName; //对应t_media_extend表cell_name字段值，用于筛选条件展示名称
    private String fieldName; //字段名称 对应java类属性
    private String dataType; //数据类型:html、json、sql
    private String dbHtml; //页面显示内容
    private String dbJson; //JSON取值
    private String dbSql; //SQL取值
    private String type; //显示类型 radio checkbox select textarea input image file number price date datetime
    private Integer sortNo; //排序
    private String remark; //描述
    private Integer mediaPlateId; //媒体板块
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; //创建时间
    private Integer creatorId; //创建人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate; //更新时间
    private Integer updatedId; //更新人
    private Integer versions; //版本号
    private Integer isDelete; //逻辑删除 0-正常 1-删除
    private Integer state; //逻辑删除 0-有效、1-无效
    @Transient
    private List<Map<String,Object>> datas; //db_sql字段执行后的数据

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDbHtml() {
        return dbHtml;
    }

    public void setDbHtml(String dbHtml) {
        this.dbHtml = dbHtml;
    }

    public String getDbJson() {
        return dbJson;
    }

    public void setDbJson(String dbJson) {
        this.dbJson = dbJson;
    }

    public String getDbSql() {
        return dbSql;
    }

    public void setDbSql(String dbSql) {
        this.dbSql = dbSql;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getMediaPlateId() {
        return mediaPlateId;
    }

    public void setMediaPlateId(Integer mediaPlateId) {
        this.mediaPlateId = mediaPlateId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Integer getUpdatedId() {
        return updatedId;
    }

    public void setUpdatedId(Integer updatedId) {
        this.updatedId = updatedId;
    }

    public Integer getVersions() {
        return versions;
    }

    public void setVersions(Integer versions) {
        this.versions = versions;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public List<Map<String, Object>> getDatas() {
        return datas;
    }

    public void setDatas(List<Map<String, Object>> datas) {
        this.datas = datas;
    }

    @Override
    public String toString() {
        return "MediaTerm1{" +
                "id=" + id +
                ", cell='" + cell + '\'' +
                ", cellName='" + cellName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", dataType='" + dataType + '\'' +
                ", dbHtml='" + dbHtml + '\'' +
                ", dbJson='" + dbJson + '\'' +
                ", dbSql='" + dbSql + '\'' +
                ", type='" + type + '\'' +
                ", sortNo=" + sortNo +
                ", remark='" + remark + '\'' +
                ", mediaPlateId=" + mediaPlateId +
                ", createDate=" + createDate +
                ", creatorId=" + creatorId +
                ", updateDate=" + updateDate +
                ", updatedId=" + updatedId +
                ", versions=" + versions +
                ", isDelete=" + isDelete +
                ", state=" + state +
                ", datas=" + datas +
                '}';
    }
}
