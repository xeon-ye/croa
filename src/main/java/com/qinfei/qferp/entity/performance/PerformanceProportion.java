package com.qinfei.qferp.entity.performance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Transient;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.qferp.entity.employ.EmployCommon;

/**
 * 考核计划实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0001 9:49；
 */
public class PerformanceProportion extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -8102162653677568000L;

	/**
	 * 主键ID；
	 */
	private Integer proId;

	/**
	 * 计划编号；
	 */
	private String proCode;

	/**
	 * 计划类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 */
	private Integer proType;

	/**
	 * 计划名称；
	 */
	private String proName;

	/**
	 * 开始日期；
	 */
	@JSONField(format = "yyyy-MM-dd")
	private Date proBegin;

	/**
	 * 结束日期；
	 */
	@JSONField(format = "yyyy-MM-dd")
	private Date proEnd;

	/**
	 * 自评权重；
	 */
	private Float proportionSelf;

	/**
	 * 上级权重；
	 */
	private Float proportionLeader;

	/**
	 * 考评小组权重；
	 */
	private Float proportionGroup;

	/**
	 * 是否启用，0为启用，1为禁用；
	 */
	private Integer proUsed;

	/**
	 * 是否发通知，0为是，1为否；
	 */
	private Integer proNotice;

	/**
	 * 是否推送消息通知考核对象，0为是，1为否；
	 */
	private Integer proMessage;

	/**
	 * 备注；
	 */
	private String proDesc;

	/**
	 * 部门ID；
	 */
	private Integer deptId;

	/**
	 * 公司代码
	 */
	private String companyCode;

	@Transient
	private List<Integer> postId = new ArrayList<>();
	@Transient
	private List<String> postName = new ArrayList<>();
	@Transient
	private List<Integer> schId = new ArrayList<>();
	@Transient
	private List<String> schName = new ArrayList<>();
	@Transient
	private List<String> schUserId = new ArrayList<>();
	@Transient
	private List<String> schUserName = new ArrayList<>();

	/**
	 * 考核方案列表
	 */
	private List<PerformanceProgram> programList;

	/**
	 * 主键ID；
	 *
	 * @return ：pro_id 主键ID；
	 */
	public Integer getProId() {
		return proId;
	}

	/**
	 * 主键ID；
	 *
	 * @param proId：主键ID；
	 */
	public void setProId(Integer proId) {
		this.proId = proId;
	}

	/**
	 * 计划编号；
	 *
	 * @return ：pro_code 计划编号；
	 */
	public String getProCode() {
		return proCode;
	}

	/**
	 * 计划编号；
	 *
	 * @param proCode：计划编号；
	 */
	public void setProCode(String proCode) {
		this.proCode = proCode == null ? null : proCode.trim();
	}

	/**
	 * 计划类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 *
	 * @return ：pro_type 计划类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 */
	public Integer getProType() {
		return proType;
	}

	/**
	 * 计划类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 *
	 * @param proType：计划类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 */
	public void setProType(Integer proType) {
		this.proType = proType;
	}

	/**
	 * 计划名称；
	 *
	 * @return ：pro_name 计划名称；
	 */
	public String getProName() {
		return proName;
	}

	/**
	 * 计划名称；
	 *
	 * @param proName：计划名称；
	 */
	public void setProName(String proName) {
		this.proName = proName == null ? null : proName.trim();
	}

	/**
	 * 开始日期；
	 *
	 * @return ：pro_begin 开始日期；
	 */
	public Date getProBegin() {
		return proBegin;
	}

	/**
	 * 开始日期；
	 *
	 * @param proBegin：开始日期；
	 */
	public void setProBegin(Date proBegin) {
		this.proBegin = proBegin;
	}

	/**
	 * 结束日期；
	 *
	 * @return ：pro_end 结束日期；
	 */
	public Date getProEnd() {
		return proEnd;
	}

	/**
	 * 结束日期；
	 *
	 * @param proEnd：结束日期；
	 */
	public void setProEnd(Date proEnd) {
		this.proEnd = proEnd;
	}

	/**
	 * 自评权重；
	 *
	 * @return ：proportion_self 自评权重；
	 */
	public Float getProportionSelf() {
		return proportionSelf;
	}

	/**
	 * 自评权重；
	 *
	 * @param proportionSelf：自评权重；
	 */
	public void setProportionSelf(Float proportionSelf) {
		this.proportionSelf = proportionSelf;
	}

	/**
	 * 上级权重；
	 *
	 * @return ：proportion_leader 上级权重；
	 */
	public Float getProportionLeader() {
		return proportionLeader;
	}

	/**
	 * 上级权重；
	 *
	 * @param proportionLeader：上级权重；
	 */
	public void setProportionLeader(Float proportionLeader) {
		this.proportionLeader = proportionLeader;
	}

	/**
	 * 考评小组权重；
	 *
	 * @return ：proportion_group 考评小组权重；
	 */
	public Float getProportionGroup() {
		return proportionGroup;
	}

	/**
	 * 考评小组权重；
	 *
	 * @param proportionGroup：考评小组权重；
	 */
	public void setProportionGroup(Float proportionGroup) {
		this.proportionGroup = proportionGroup;
	}

	/**
	 * 是否启用，0为启用，1为禁用；
	 *
	 * @return ：pro_used 是否启用，0为启用，1为禁用；
	 */
	public Integer getProUsed() {
		return proUsed;
	}

	/**
	 * 是否启用，0为启用，1为禁用；
	 *
	 * @param proUsed：是否启用，0为启用，1为禁用；
	 */
	public void setProUsed(Integer proUsed) {
		this.proUsed = proUsed;
	}

	/**
	 * 是否发通知，0为是，1为否；
	 *
	 * @return ：pro_notice 是否发通知，0为是，1为否；
	 */
	public Integer getProNotice() {
		return proNotice;
	}

	/**
	 * 是否发通知，0为是，1为否；
	 *
	 * @param proNotice：是否发通知，0为是，1为否；
	 */
	public void setProNotice(Integer proNotice) {
		this.proNotice = proNotice;
	}

	/**
	 * 是否推送消息通知考核对象，0为是，1为否；
	 *
	 * @return ：pro_message 是否推送消息通知考核对象，0为是，1为否；
	 */
	public Integer getProMessage() {
		return proMessage;
	}

	/**
	 * 是否推送消息通知考核对象，0为是，1为否；
	 *
	 * @param proMessage：是否推送消息通知考核对象，0为是，1为否；
	 */
	public void setProMessage(Integer proMessage) {
		this.proMessage = proMessage;
	}

	/**
	 * 备注；
	 *
	 * @return ：pro_desc 备注；
	 */
	public String getProDesc() {
		return proDesc;
	}

	/**
	 * 备注；
	 *
	 * @param proDesc：备注；
	 */
	public void setProDesc(String proDesc) {
		this.proDesc = proDesc == null ? null : proDesc.trim();
	}

	/**
	 * 部门ID；
	 *
	 * @return ：dept_id 部门ID；
	 */
	public Integer getDeptId() {
		return deptId;
	}

	/**
	 * 部门ID；
	 *
	 * @param deptId：部门ID；
	 */
	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

	public List<PerformanceProgram> getProgramList() {
		return programList;
	}

	public void setProgramList(List<PerformanceProgram> programList) {
		this.programList = programList;
	}

	public List<Integer> getPostId() {
		return postId;
	}

	public void setPostId(List<Integer> postId) {
		this.postId = postId;
	}

	public void setSchId(List<Integer> schId) {
		this.schId = schId;
	}

	public List<Integer> getSchId() {
		return schId;
	}

	public void setSchName(List<String> schName) {
		this.schName = schName;
	}

	public List<String> getSchName() {
		return schName;
	}

	public void setSchUserId(List<String> schUserId) {
		this.schUserId = schUserId;
	}

	public List<String> getSchUserId() {
		return schUserId;
	}

	public void setSchUserName(List<String> schUserName) {
		this.schUserName = schUserName;
	}

	public List<String> getSchUserName() {
		return schUserName;
	}

	public void setPostName(List<String> postName) {
		this.postName = postName;
	}


	public List<String> getPostName() {
		return postName;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	@Override
	public String toString() {
		return "PerformanceProportion{" +
				"proId=" + proId +
				", proCode='" + proCode + '\'' +
				", proType=" + proType +
				", proName='" + proName + '\'' +
				", proBegin=" + proBegin +
				", proEnd=" + proEnd +
				", proportionSelf=" + proportionSelf +
				", proportionLeader=" + proportionLeader +
				", proportionGroup=" + proportionGroup +
				", proUsed=" + proUsed +
				", proNotice=" + proNotice +
				", proMessage=" + proMessage +
				", proDesc='" + proDesc + '\'' +
				", deptId=" + deptId +
				", companyCode='" + companyCode + '\'' +
				'}';
	}
}