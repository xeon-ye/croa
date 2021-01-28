package com.qinfei.qferp.entity.document;


import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 制度权限表
 * dsg
 *
 */
@Table(name = "t_document_permission_details")
public class TDocumentPermissionDetails implements Serializable {

  private Integer id;

  //权限id
  private Integer permissionId;

  public Integer getPermissionId() {
    return permissionId;
  }

  public void setPermissionId(Integer permissionId) {
    this.permissionId = permissionId;
  }

  //制度id
  private Integer libraryId;

  //设置范围权限
  private Integer libraryEnrollFlag;

  //范围id
  private String rangeId;

  private Integer createId;

  private Date createTime;

  private Integer updateId;

  private Date updateTime;

  private Integer state;

  @Override
  public String toString() {
    return "TDocumentPermissionDetails{" +
            "id=" + id +
            ", permissionId=" + permissionId +
            ", libraryId=" + libraryId +
            ", libraryEnrollFlag=" + libraryEnrollFlag +
            ", rangeId='" + rangeId + '\'' +
            ", createId=" + createId +
            ", createTime=" + createTime +
            ", updateId=" + updateId +
            ", updateTime=" + updateTime +
            ", state=" + state +
            '}';
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public Integer getLibraryId() {
    return libraryId;
  }

  public void setLibraryId(Integer libraryId) {
    this.libraryId = libraryId;
  }


  public Integer getLibraryEnrollFlag() {
    return libraryEnrollFlag;
  }

  public void setLibraryEnrollFlag(Integer libraryEnrollFlag) {
    this.libraryEnrollFlag = libraryEnrollFlag;
  }


  public String getRangeId() {
    return rangeId;
  }

  public void setRangeId(String rangeId) {
    this.rangeId = rangeId;
  }


  public Integer getCreateId() {
    return createId;
  }

  public void setCreateId(Integer createId) {
    this.createId = createId;
  }


  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }


  public Integer getUpdateId() {
    return updateId;
  }

  public void setUpdateId(Integer updateId) {
    this.updateId = updateId;
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
