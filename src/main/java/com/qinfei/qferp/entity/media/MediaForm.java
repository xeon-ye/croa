package com.qinfei.qferp.entity.media;

import io.swagger.models.auth.In;

import com.qinfei.core.annotation.Table;
import java.io.Serializable;

@Table(name = "t_media_form")
public class MediaForm implements Serializable {
    private Integer id;

    private Integer mediaTypeId;

    private String colName;

    private String code;

    private String name;

    private String type;

    private Integer sortNo;

    private Integer disabled;

    private String desc;

    private String rule;

    private Integer dataType;

    private String html;

    private String json;

    private String sql;
    /**
     * 是否必须字段
     *
     * @return
     */
    private Byte required;
    private Byte size;
    private Byte maxlength;
    private Byte minlength;
    private Byte min;
    private Byte max;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Integer mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName == null ? null : colName.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getDisabled() {
        return disabled;
    }

    public void setDisabled(Integer disabled) {
        this.disabled = disabled;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule == null ? null : rule.trim();
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html == null ? null : html.trim();
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json == null ? null : json.trim();
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql == null ? null : sql.trim();
    }

    public void setRequired(Byte required) {
        this.required = required;
    }

    public Byte getRequired() {
        return required;
    }

    public Byte getSize() {
        return size;
    }

    public void setSize(Byte size) {
        this.size = size;
    }

    public Byte getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(Byte maxlength) {
        this.maxlength = maxlength;
    }

    public Byte getMinlength() {
        return minlength;
    }

    public void setMinlength(Byte minlength) {
        this.minlength = minlength;
    }

    public Byte getMin() {
        return min;
    }

    public void setMin(Byte min) {
        this.min = min;
    }

    public Byte getMax() {
        return max;
    }

    public void setMax(Byte max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "MediaForm{" +
                "id=" + id +
                ", mediaTypeId=" + mediaTypeId +
                ", colName='" + colName + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", sortNo=" + sortNo +
                ", disabled=" + disabled +
                ", desc='" + desc + '\'' +
                ", rule='" + rule + '\'' +
                ", dataType=" + dataType +
                ", html='" + html + '\'' +
                ", json='" + json + '\'' +
                ", sql='" + sql + '\'' +
                ", required=" + required +
                ", size=" + size +
                ", maxlength=" + maxlength +
                ", minlength=" + minlength +
                ", min=" + min +
                ", max=" + max +
                '}';
    }
}