package com.qinfei.qferp.entity.performance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.data.annotation.Transient;

import com.qinfei.qferp.entity.employ.EmployCommon;

/**
 * 考核方案实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0001 9:49；
 */
public class PerformanceScheme extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 7835336065308100030L;

	/**
	 * 主键ID；
	 */
	private Integer schId;

	/**
	 * 方案编号；
	 */
	private String schCode;

	/**
	 * 方案类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 */
	private Integer schType;
	/**
	 * 方案类型1:kpl方案，2:okr方案
	 */
	private Integer schemeType;

	/**
	 * 方案名称；
	 */
	private String schName;

	/**
	 * 适用的职位ID；
	 */
	private Integer postId;

	/**
	 * 适用的职位名称；
	 */
	private String postName;

	/**
	 * 考核原则；
	 */
	private String schPrinciple;

	/**
	 * 考核目的；
	 */
	private String schPurpose;

	/**
	 * 考核的板块信息；
	 */
	private String schComponent;

	/**
	 * 考核方式；
	 */
	private String schModus;

	/**
	 * 考核总分；
	 */
	private Float schTotal;

	/**
	 * 考核组织；
	 */
	private String schOrganization;

	/**
	 * 考核小组成员的ID；
	 */
	private String groupIds;

	/**
	 * 考核小组成员姓名；
	 */
	private String groupNames;

	/**
	 * 考核规则，存储内容未JSON字符串；
	 */
	private String schRule;

	/**
	 * 考核评分等级，存储内容为JSON字符串；
	 */
	private String schLevel;

	/**
	 * 考核合格的等级；
	 */
	private String schSuffice;

	/**
	 * kpl排除考核的用户ID/okr考核对象；
	 */
	private String schUserId;

	/**
	 * 排除考核的用户名称/考核对象名称；
	 */
	private String schUserName;

	/**
	 * 是否启用，0为启用，1为禁用；
	 */
	private Integer schUsed;

	/**
	 * 备注；
	 */
	private String schDesc;

	/**
	 * 部门ID；
	 */
	private Integer deptId;

	/**
	 * 部门ID；
	 */
	private String companyCode;
	@Transient
	private String proName;

	@Transient
	private List<PerformanceHistory> historyList = new ArrayList<>();
	@Transient
	private List<Integer> plateId = new ArrayList<>();
	@Transient
	private List<Integer> plateParent = new ArrayList<>();
	@Transient
	private List<Integer> plateLevel = new ArrayList<>();
	@Transient
	private List<Float> plateProportion = new ArrayList<>();
	@Transient
	private List<String> plateContent = new ArrayList<>();
	@Transient
	private List<Integer> schUserIdLs = new ArrayList<>();
	@Transient
	private List<String> schUserNameLs = new ArrayList<>();
	@Transient
	private List<Integer> groupIdsLs = new ArrayList<>();
	@Transient
	private List<String> groupNamesLs = new ArrayList<>();
	@Transient
	private List<Integer> plateOrder = new ArrayList<>();
	@Transient
	private List<String> plateTarget = new ArrayList<>();
	@Transient
	private List<String> plateDemand = new ArrayList<>();

	public List<PerformanceHistory> getHistoryList() {
		return historyList;
	}

	public void setHistoryList(List<PerformanceHistory> historyList) {
		this.historyList = historyList;
	}

	public List<Integer> getPlateOrder() {
		return plateOrder;
	}

	public void setPlateOrder(List<Integer> plateOrder) {
		this.plateOrder = plateOrder;
	}

	public List<Integer> getPlateParent() {
		return plateParent;
	}

	public void setPlateParent(List<Integer> plateParent) {
		this.plateParent = plateParent;
	}

	public List<Integer> getPlateLevel() {
		return plateLevel;
	}

	public void setPlateLevel(List<Integer> plateLevel) {
		this.plateLevel = plateLevel;
	}

	public List<Integer> getSchUserIdLs() {
		return schUserIdLs;
	}

	public void setSchUserIdLs(List<Integer> schUserIdLs) {
		this.schUserIdLs = schUserIdLs;
	}

	public List<String> getSchUserNameLs() {
		return schUserNameLs;
	}

	public void setSchUserNameLs(List<String> schUserNameLs) {
		this.schUserNameLs = schUserNameLs;
	}

	public List<Integer> getGroupIdsLs() {
		return groupIdsLs;
	}

	public void setGroupIdsLs(List<Integer> groupIdsLs) {
		this.groupIdsLs = groupIdsLs;
	}

	public List<String> getGroupNamesLs() {
		return groupNamesLs;
	}

	public void setGroupNamesLs(List<String> groupNamesLs) {
		this.groupNamesLs = groupNamesLs;
	}

	public List<Float> getPlateProportion() {
		return plateProportion;
	}

	public void setPlateProportion(List<Float> plateProportion) {
		this.plateProportion = plateProportion;
	}

	public List<String> getPlateContent() {
		return plateContent;
	}

	public void setPlateContent(List<String> plateContent) {
		this.plateContent = plateContent;
	}

	public List<Integer> getPlateId() {
		return plateId;
	}

	public void setPlateId(List<Integer> plateId) {
		this.plateId = plateId;
	}

	public List<String> getPlateTarget() {
		return plateTarget;
	}

	public void setPlateTarget(List<String> plateTarget) {
		this.plateTarget = plateTarget;
	}

	public List<String> getPlateDemand() {
		return plateDemand;
	}

	public void setPlateDemand(List<String> plateDemand) {
		this.plateDemand = plateDemand;
	}

	/**
	 * 主键ID；
	 *
	 * @return ：sch_id 主键ID；
	 */
	public Integer getSchId() {
		return schId;
	}

	/**
	 * 主键ID；
	 *
	 * @param schId：主键ID；
	 */
	public void setSchId(Integer schId) {
		this.schId = schId;
	}

	/**
	 * 方案编号；
	 *
	 * @return ：sch_code 方案编号；
	 */
	public String getSchCode() {
		return schCode;
	}

	/**
	 * 方案编号；
	 *
	 * @param schCode：方案编号；
	 */
	public void setSchCode(String schCode) {
		this.schCode = schCode == null ? null : schCode.trim();
	}

	/**
	 * 方案类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 *
	 * @return ：sch_type 方案类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 */
	public Integer getSchType() {
		return schType;
	}

	/**
	 * 方案类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 *
	 * @param schType：方案类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
	 */
	public void setSchType(Integer schType) {
		this.schType = schType;
	}

	/**
	 * 方案名称；
	 *
	 * @return ：sch_name 方案名称；
	 */
	public String getSchName() {
		return schName;
	}

	/**
	 * 方案名称；
	 *
	 * @param schName：方案名称；
	 */
	public void setSchName(String schName) {
		this.schName = schName == null ? null : schName.trim();
	}

	/**
	 * 适用的职位ID；
	 *
	 * @return ：post_id 适用的职位ID；
	 */
	public Integer getPostId() {
		return postId;
	}

	/**
	 * 适用的职位ID；
	 *
	 * @param postId：适用的职位ID；
	 */
	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	/**
	 * 适用的职位名称；
	 *
	 * @return ：post_name 适用的职位名称；
	 */
	public String getPostName() {
		return postName;
	}

	/**
	 * 适用的职位名称；
	 *
	 * @param postName：适用的职位名称；
	 */
	public void setPostName(String postName) {
		this.postName = postName == null ? null : postName.trim();
	}

	/**
	 * 考核原则；
	 *
	 * @return ：sch_principle 考核原则；
	 */
	public String getSchPrinciple() {
		return schPrinciple;
	}

	/**
	 * 考核原则；
	 *
	 * @param schPrinciple：考核原则；
	 */
	public void setSchPrinciple(String schPrinciple) {
		this.schPrinciple = schPrinciple == null ? null : schPrinciple.trim();
	}

	/**
	 * 考核目的；
	 *
	 * @return ：sch_purpose 考核目的；
	 */
	public String getSchPurpose() {
		return schPurpose;
	}

	/**
	 * 考核目的；
	 *
	 * @param schPurpose：考核目的；
	 */
	public void setSchPurpose(String schPurpose) {
		this.schPurpose = schPurpose == null ? null : schPurpose.trim();
	}

	/**
	 * 考核的板块信息；
	 *
	 * @return ：sch_component 考核的板块信息；
	 */
	public String getSchComponent() {
		return schComponent;
	}

	/**
	 * 考核的板块信息；
	 *
	 * @param schComponent：考核的板块信息；
	 */
	public void setSchComponent(String schComponent) {
		this.schComponent = schComponent == null ? null : schComponent.trim();
	}

	/**
	 * 考核方式；
	 *
	 * @return ：sch_modus 考核方式；
	 */
	public String getSchModus() {
		return schModus;
	}

	/**
	 * 考核方式；
	 *
	 * @param schModus：考核方式；
	 */
	public void setSchModus(String schModus) {
		this.schModus = schModus == null ? null : schModus.trim();
	}

	/**
	 * 考核总分；
	 *
	 * @return ：sch_total 考核总分；
	 */
	public Float getSchTotal() {
		return schTotal;
	}

	/**
	 * 考核总分；
	 *
	 * @param schTotal：考核总分；
	 */
	public void setSchTotal(Float schTotal) {
		this.schTotal = schTotal;
	}

	/**
	 * 考核组织；
	 *
	 * @return ：sch_organization 考核组织；
	 */
	public String getSchOrganization() {
		return schOrganization;
	}

	/**
	 * 考核组织；
	 *
	 * @param schOrganization：考核组织；
	 */
	public void setSchOrganization(String schOrganization) {
		this.schOrganization = schOrganization == null ? null : schOrganization.trim();
	}

	/**
	 * 考核小组成员的ID；
	 *
	 * @return ：group_ids 考核小组成员的ID；
	 */
	public String getGroupIds() {
		return groupIds;
	}

	/**
	 * 考核小组成员的ID；
	 *
	 * @param groupIds：考核小组成员的ID；
	 */
	public void setGroupIds(String groupIds) {
		this.groupIds = groupIds == null ? null : groupIds.trim();
	}

	/**
	 * 考核小组成员姓名；
	 *
	 * @return ：group_names 考核小组成员姓名；
	 */
	public String getGroupNames() {
		return groupNames;
	}

	/**
	 * 考核小组成员姓名；
	 *
	 * @param groupNames：考核小组成员姓名；
	 */
	public void setGroupNames(String groupNames) {
		this.groupNames = groupNames == null ? null : groupNames.trim();
	}

	/**
	 * 考核规则，存储内容未JSON字符串；
	 *
	 * @return ：sch_rule 考核规则，存储内容未JSON字符串；
	 */
	public String getSchRule() {
		return schRule;
	}

	/**
	 * 考核规则，存储内容未JSON字符串；
	 *
	 * @param schRule：考核规则，存储内容未JSON字符串；
	 */
	public void setSchRule(String schRule) {
		this.schRule = schRule == null ? null : schRule.trim();
	}

	/**
	 * 考核评分等级，存储内容为JSON字符串；
	 *
	 * @return ：sch_level 考核评分等级，存储内容为JSON字符串；
	 */
	public String getSchLevel() {
		return schLevel;
	}

	/**
	 * 考核评分等级，存储内容为JSON字符串；
	 *
	 * @param schLevel：考核评分等级，存储内容为JSON字符串；
	 */
	public void setSchLevel(String schLevel) {
		this.schLevel = schLevel == null ? null : schLevel.trim();
	}

	/**
	 * 考核合格的等级；
	 *
	 * @return ：sch_suffice 考核合格的等级；
	 */
	public String getSchSuffice() {
		return schSuffice;
	}

	/**
	 * 考核合格的等级；
	 *
	 * @param schSuffice：考核合格的等级；
	 */
	public void setSchSuffice(String schSuffice) {
		this.schSuffice = schSuffice == null ? null : schSuffice.trim();
	}

	/**
	 * 排除考核的用户ID；
	 *
	 * @return ：sch_user_id 排除考核的用户ID；
	 */
	public String getSchUserId() {
		return schUserId;
	}

	/**
	 * 排除考核的用户ID；
	 *
	 * @param schUserId：排除考核的用户ID；
	 */
	public void setSchUserId(String schUserId) {
		this.schUserId = schUserId == null ? null : schUserId.trim();
	}

	/**
	 * 排除考核的用户名称；
	 *
	 * @return ：sch_user_name 排除考核的用户名称；
	 */
	public String getSchUserName() {
		return schUserName;
	}

	/**
	 * 排除考核的用户名称；
	 *
	 * @param schUserName：排除考核的用户名称；
	 */
	public void setSchUserName(String schUserName) {
		this.schUserName = schUserName == null ? null : schUserName.trim();
	}

	/**
	 * 是否启用，0为启用，1为禁用；
	 *
	 * @return ：sch_used 是否启用，0为启用，1为禁用；
	 */
	public Integer getSchUsed() {
		return schUsed;
	}

	/**
	 * 是否启用，0为启用，1为禁用；
	 *
	 * @param schUsed：是否启用，0为启用，1为禁用；
	 */
	public void setSchUsed(Integer schUsed) {
		this.schUsed = schUsed;
	}

	/**
	 * 备注；
	 *
	 * @return ：sch_desc 备注；
	 */
	public String getSchDesc() {
		return schDesc;
	}

	/**
	 * 备注；
	 *
	 * @param schDesc：备注；
	 */
	public void setSchDesc(String schDesc) {
		this.schDesc = schDesc == null ? null : schDesc.trim();
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

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getProName() {
		return proName;
	}

	public void setProName(String proName) {
		this.proName = proName;
	}

	public Integer getSchemeType() {
		return schemeType;
	}

	public void setSchemeType(Integer schemeType) {
		this.schemeType = schemeType;
	}
	@Override
	public String toString() {
		return "PerformanceScheme{" +
				"schId=" + schId +
				", schCode='" + schCode + '\'' +
				", schType=" + schType +
				", schName='" + schName + '\'' +
				", postId=" + postId +
				", postName='" + postName + '\'' +
				", schPrinciple='" + schPrinciple + '\'' +
				", schPurpose='" + schPurpose + '\'' +
				", schComponent='" + schComponent + '\'' +
				", schModus='" + schModus + '\'' +
				", schTotal=" + schTotal +
				", schOrganization='" + schOrganization + '\'' +
				", groupIds='" + groupIds + '\'' +
				", groupNames='" + groupNames + '\'' +
				", schRule='" + schRule + '\'' +
				", schLevel='" + schLevel + '\'' +
				", schSuffice='" + schSuffice + '\'' +
				", schUserId='" + schUserId + '\'' +
				", schUserName='" + schUserName + '\'' +
				", schUsed=" + schUsed +
				", schDesc='" + schDesc + '\'' +
				", deptId=" + deptId +
				", companyCode='" + companyCode + '\'' +
				'}';
	}
}