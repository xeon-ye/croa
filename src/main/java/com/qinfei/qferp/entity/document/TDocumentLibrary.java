package com.qinfei.qferp.entity.document;


import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 制度管理表
 * dsg
 */
@Table(name = "t_document_library")
@Data
public class TDocumentLibrary implements Serializable {

  //制度管理表id
  private Integer id;

  //制度权限范围id
  private Integer permissionId;

  //制度编号
  private String libraryCode;

  //制度名称
  private String libraryName;

  //制度类型id
  private Integer typeId;

  @Transient
  private String releaseName;

  //制度生效时间
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date effectiveStartTime;

  //制度失效时间
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date effectiveEndTime;

  //设置范围权限：0、未设定 1、部门，2、角色、3、黑名单用户
  private Integer libraryEnrollFlag;

  //设置职龄权限类型：0、年 1、月
  private Integer workAgeFlag;

  //职龄范围最大值
  private Integer workAgeMax;

  //职龄范围最小值
  private Integer workAgeMin;

  //制度内容
  private String content;

  //发布时间
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date releaseTime;

  //发布部门
  private Integer releaseDept;

  //发布用户
  private Integer releaseUser;

  //缩略图
  private String thumbnailPictureLink;

  //附件名称
  private String attachment;

  //附件链接
  private String attachmentLink;

  //创建用户id
  private Integer createId;

  private String companyCode;

  private String enrollFlag;

  private String randId;

  //制度权限编号
  @Transient
  private String permissionCode;

  @Transient
  private Integer btnState;

  //创建时间
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date createTime;

  //更新用户
  private Date updateId;

  //更新时间
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;
  //制度状态
  private Integer state;

  //版次
  private String version;

  //等级：A重要，B中等，C一般
  private String level;

  @Transient
  private String flag;
  @Transient
  private String documentPermissionDetailsList;
}
