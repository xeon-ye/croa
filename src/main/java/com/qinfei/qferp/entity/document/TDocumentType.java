package com.qinfei.qferp.entity.document;


import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * 制度类型表
 * dsg
 */
@Table(name = "t_document_type")
public class TDocumentType implements Serializable {

  //类型id
  private Integer id;

  //类型名称
  private String typeName;

  //父级id
  private Integer parentId;

  //层级
  private Integer level;
  private Integer createId;
  private Integer updateId;
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date createTime;

  private String companyCode;

  public String getCompanyCode() {
    return companyCode;
  }

  public void setCompanyCode(String companyCode) {
    this.companyCode = companyCode;
  }
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;
  private Integer state;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }


  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }


  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }


  public Integer getCreateId() {
    return createId;
  }

  public void setCreateId(Integer createId) {
    this.createId = createId;
  }


  public Integer getUpdateId() {
    return updateId;
  }

  public void setUpdateId(Integer updateId) {
    this.updateId = updateId;
  }


  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }


  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }


  public Integer getState() {
    return state;
  }

  public void setState(Integer state) {
    this.state = state;
  }

}
