package com.qinfei.qferp.entity.media;

import com.qinfei.core.annotation.Column;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Table(name = "t_media_term")
public class MediaTerm implements Serializable {
    @Id
    @Column(name = "id")
    private Integer id;

    private Integer typeId;

    private String termName;

    private String termSql;

    private Boolean state;

    private Integer type;
    /**
     * 数据类型
     * 0 sql 1 JSON 2HTML
     */
    private Integer dataType;

    private String html;
    private String json;//
    private String name;
    private String field;
    private String sql;
    @Transient
    private List<Map<String, Object>> datas;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName == null ? null : termName.trim();
    }

    public String getTermSql() {
        return termSql;
    }

    public void setTermSql(String termSql) {
        this.termSql = termSql == null ? null : termSql.trim();
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<Map<String, Object>> getDatas() {
        return datas;
    }

    public void setDatas(List<Map<String, Object>> datas) {
        this.datas = datas;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaTerm mediaTerm = (MediaTerm) o;
        return Objects.equals(id, mediaTerm.id) &&
                Objects.equals(typeId, mediaTerm.typeId) &&
                Objects.equals(termName, mediaTerm.termName) &&
                Objects.equals(termSql, mediaTerm.termSql) &&
                Objects.equals(state, mediaTerm.state) &&
                Objects.equals(type, mediaTerm.type) &&
                Objects.equals(dataType, mediaTerm.dataType) &&
                Objects.equals(html, mediaTerm.html) &&
                Objects.equals(json, mediaTerm.json) &&
                Objects.equals(name, mediaTerm.name) &&
                Objects.equals(field, mediaTerm.field) &&
                Objects.equals(sql, mediaTerm.sql) &&
                Objects.equals(datas, mediaTerm.datas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, typeId, termName, termSql, state, type, dataType, html, json, name, field, sql, datas);
    }

    @Override
    public String toString() {
        return "MediaTerm{" +
                "id=" + id +
                ", typeId=" + typeId +
                ", termName='" + termName + '\'' +
                ", termSql='" + termSql + '\'' +
                ", state=" + state +
                ", type=" + type +
                ", dataType=" + dataType +
                ", html='" + html + '\'' +
                ", json='" + json + '\'' +
                ", name='" + name + '\'' +
                ", field='" + field + '\'' +
                ", sql='" + sql + '\'' +
                '}';
    }
}