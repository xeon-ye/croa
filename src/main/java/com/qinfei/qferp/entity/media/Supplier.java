package com.qinfei.qferp.entity.media;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.utils.ObjectFieldCompare;
import lombok.Getter;
import lombok.Setter;

/**
 * 供应商表
 */
@Getter
@Setter
@Table(name = "t_media_supplier")
public class Supplier implements Serializable {
	@Id
	private Integer id;

	private Integer mediaTypeId;

	@ObjectFieldCompare.CompareField(fieldName = "供应商性质")
	private Byte supplierNature;//供应商性质：0-企业供应商、1-个体供应商

	// 媒体类型；
	@Transient
	private String mediaTypeName;

	@ObjectFieldCompare.CompareField(fieldName = "供应商公司名称")
	private String name;

	@ObjectFieldCompare.CompareField(fieldName = "是否标准公司")
	private Byte standarCompanyFlag;//是否标准公司：0-非标准、1-标准，默认0

	private String desc;

	@ObjectFieldCompare.CompareField(fieldName = "供应商联系人")
	private String contactor;

	@ObjectFieldCompare.CompareField(fieldName = "手机号")
	private String phone;

	@ObjectFieldCompare.CompareField(fieldName = "是否规范联系人")
	private Byte standarPhoneFlag;//是否规范联系人：0-不规范、1-规范

	@ObjectFieldCompare.CompareField(fieldName = "微信号")
	private String qqwechat;

	@ObjectFieldCompare.CompareField(fieldName = "QQ号")
	private String qq;//QQ号

	@ObjectFieldCompare.CompareField(fieldName = "备注")
	private String contactorDesc;

	@ObjectFieldCompare.CompareField(fieldName = "状态")
	private Integer state;

	@ObjectFieldCompare.CompareField(fieldName = "责任人")
	private Integer creator;

	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	private Integer updateUserId;

	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	/**
	 * 公司代码
	 */
	private String companyCode;

	private Integer isCopy; //是否拷贝媒体 0-自建、1-拷贝

	private String copyRemarks; //拷贝媒体备注

	@Transient
	private User user; //登记者
	@Transient
	private String creatorName;
	@Transient
	private String companyName;
	@Transient
	private String plateIds;

	@Transient
	private boolean flag;

	@Transient
	private Integer deptId;

	@Override
	public String toString() {
		return "Supplier{" +
				"id=" + id +
				", mediaTypeId=" + mediaTypeId +
				", mediaTypeName='" + mediaTypeName + '\'' +
				", name='" + name + '\'' +
				", desc='" + desc + '\'' +
				", contactor='" + contactor + '\'' +
				", phone='" + phone + '\'' +
				", qqwechat='" + qqwechat + '\'' +
				", contactorDesc='" + contactorDesc + '\'' +
				", state=" + state +
				", creator=" + creator +
				", createTime=" + createTime +
				", updateUserId=" + updateUserId +
				", updateTime=" + updateTime +
				", companyCode='" + companyCode + '\'' +
				", isCopy=" + isCopy +
				", copyRemarks='" + copyRemarks + '\'' +
				", user=" + user +
				", creatorName='" + creatorName + '\'' +
				", companyName='" + companyName + '\'' +
				'}';
	}
}