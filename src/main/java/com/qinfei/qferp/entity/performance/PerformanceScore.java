package com.qinfei.qferp.entity.performance;

import com.qinfei.qferp.entity.employ.EmployCommon;

/**
 * 考核个人评分实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0001 9:49；
 */
public class PerformanceScore extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -638641805354378638L;

	/**
	 * 主键ID；
	 */
	private Integer scoreId;

	/**
	 * 关联的考核计划ID；
	 */
	private Integer proId;

	/**
	 * 考核计划的备注；
	 */
	private String proDesc;

	/**
	 * 关联的考核方案ID；
	 */
	private Integer schId;

	/**
	 * 考核方案的备注；
	 */
	private String schDesc;

	/**
	 * 被考核对象的ID；
	 */
	private Integer userId;

	/**
	 * 被考核对象的名称；
	 */
	private String userName;

	/**
	 * 被考核对象的性别，0为女，1为男；
	 */
	private Integer userGender;

	/**
	 * 被考核对象的部门ID；
	 */
	private Integer deptId;

	/**
	 * 被考核对象的部门名称；
	 */
	private String deptName;

	/**
	 * 被考核对象的职位ID；
	 */
	private Integer postId;

	/**
	 * 被考核对象的职位名称；
	 */
	private String postName;

	/**
	 * 个人评分；
	 */
	private Float scoreSelf;

	/**
	 * 上级评分；
	 */
	private Float scoreLeader;

	/**
	 * 小组评分；
	 */
	private Float scoreGroup;

	/**
	 * 总分；
	 */
	private Float scoreTotal;

	/**
	 * 评分等级；
	 */
	private String scoreLevel;

	/**
	 * 是否合格，0为是，1为否；
	 */
	private Integer beSuffice;

	/**
	 * 流程的任务ID；
	 */
	private String taskId;

	/**
	 * 待办事项ID；
	 */
	private Integer itemId;

	/**
	 * 流程审批节点；
	 */
	private Integer processState;

	/**
	 * 权限访问码；
	 */
	private String validCode;
	//方案类型
	private Integer schemeType;

	/**
	 * 状态，-1为删除，0为评分中，1为评分完成；
	 */

	/**
	 * 主键ID；
	 *
	 * @return ：score_id 主键ID；
	 */
	public Integer getScoreId() {
		return scoreId;
	}

	/**
	 * 主键ID；
	 *
	 * @param scoreId：主键ID；
	 */
	public void setScoreId(Integer scoreId) {
		this.scoreId = scoreId;
	}

	/**
	 * 关联的考核计划ID；
	 *
	 * @return ：pro_id 关联的考核计划ID；
	 */
	public Integer getProId() {
		return proId;
	}

	/**
	 * 关联的考核计划ID；
	 *
	 * @param proId：关联的考核计划ID；
	 */
	public void setProId(Integer proId) {
		this.proId = proId;
	}

	/**
	 * 考核计划的备注；
	 *
	 * @return ：pro_desc 考核计划的备注；
	 */
	public String getProDesc() {
		return proDesc;
	}

	/**
	 * 考核计划的备注；
	 *
	 * @param proDesc：考核计划的备注；
	 */
	public void setProDesc(String proDesc) {
		this.proDesc = proDesc == null ? null : proDesc.trim();
	}

	/**
	 * 关联的考核方案ID；
	 *
	 * @return ：sch_id 关联的考核方案ID；
	 */
	public Integer getSchId() {
		return schId;
	}

	/**
	 * 关联的考核方案ID；
	 *
	 * @param schId：关联的考核方案ID；
	 */
	public void setSchId(Integer schId) {
		this.schId = schId;
	}

	/**
	 * 考核方案的备注；
	 *
	 * @return ：sch_desc 考核方案的备注；
	 */
	public String getSchDesc() {
		return schDesc;
	}

	/**
	 * 考核方案的备注；
	 *
	 * @param schDesc：考核方案的备注；
	 */
	public void setSchDesc(String schDesc) {
		this.schDesc = schDesc == null ? null : schDesc.trim();
	}

	/**
	 * 被考核对象的ID；
	 *
	 * @return ：user_id 被考核对象的ID；
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * 被考核对象的ID；
	 *
	 * @param userId：被考核对象的ID；
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * 被考核对象的名称；
	 *
	 * @return ：user_name 被考核对象的名称；
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 被考核对象的名称；
	 *
	 * @param userName：被考核对象的名称；
	 */
	public void setUserName(String userName) {
		this.userName = userName == null ? null : userName.trim();
	}

	/**
	 * 被考核对象的性别，0为女，1为男；
	 *
	 * @return ：user_gender 被考核对象的性别，0为女，1为男；
	 */
	public Integer getUserGender() {
		return userGender;
	}

	/**
	 * 被考核对象的性别，0为女，1为男；
	 *
	 * @param userGender：被考核对象的性别，0为女，1为男；
	 */
	public void setUserGender(Integer userGender) {
		this.userGender = userGender;
	}

	/**
	 * 被考核对象的部门ID；
	 *
	 * @return ：dept_id 被考核对象的部门ID；
	 */
	public Integer getDeptId() {
		return deptId;
	}

	/**
	 * 被考核对象的部门ID；
	 *
	 * @param deptId：被考核对象的部门ID；
	 */
	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

	/**
	 * 被考核对象的部门名称；
	 *
	 * @return ：dept_name 被考核对象的部门名称；
	 */
	public String getDeptName() {
		return deptName;
	}

	/**
	 * 被考核对象的部门名称；
	 *
	 * @param deptName：被考核对象的部门名称；
	 */
	public void setDeptName(String deptName) {
		this.deptName = deptName == null ? null : deptName.trim();
	}

	/**
	 * 被考核对象的职位ID；
	 *
	 * @return ：post_id 被考核对象的职位ID；
	 */
	public Integer getPostId() {
		return postId;
	}

	/**
	 * 被考核对象的职位ID；
	 *
	 * @param postId：被考核对象的职位ID；
	 */
	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	/**
	 * 被考核对象的职位名称；
	 *
	 * @return ：post_name 被考核对象的职位名称；
	 */
	public String getPostName() {
		return postName;
	}

	/**
	 * 被考核对象的职位名称；
	 *
	 * @param postName：被考核对象的职位名称；
	 */
	public void setPostName(String postName) {
		this.postName = postName == null ? null : postName.trim();
	}

	/**
	 * 个人评分；
	 *
	 * @return ：score_self 个人评分；
	 */
	public Float getScoreSelf() {
		return scoreSelf;
	}

	/**
	 * 个人评分；
	 *
	 * @param scoreSelf：个人评分；
	 */
	public void setScoreSelf(Float scoreSelf) {
		this.scoreSelf = scoreSelf;
	}

	/**
	 * 上级评分；
	 *
	 * @return ：score_leader 上级评分；
	 */
	public Float getScoreLeader() {
		return scoreLeader;
	}

	/**
	 * 上级评分；
	 *
	 * @param scoreLeader：上级评分；
	 */
	public void setScoreLeader(Float scoreLeader) {
		this.scoreLeader = scoreLeader;
	}

	/**
	 * 小组评分；
	 *
	 * @return ：score_group 小组评分；
	 */
	public Float getScoreGroup() {
		return scoreGroup;
	}

	/**
	 * 小组评分；
	 *
	 * @param scoreGroup：小组评分；
	 */
	public void setScoreGroup(Float scoreGroup) {
		this.scoreGroup = scoreGroup;
	}

	/**
	 * 总分；
	 *
	 * @return ：score_total 总分；
	 */
	public Float getScoreTotal() {
		return scoreTotal;
	}

	/**
	 * 总分；
	 *
	 * @param scoreTotal：总分；
	 */
	public void setScoreTotal(Float scoreTotal) {
		this.scoreTotal = scoreTotal;
	}

	/**
	 * 评分等级；
	 *
	 * @return ：score_level 评分等级；
	 */
	public String getScoreLevel() {
		return scoreLevel;
	}

	/**
	 * 评分等级；
	 *
	 * @param scoreLevel：评分等级；
	 */
	public void setScoreLevel(String scoreLevel) {
		this.scoreLevel = scoreLevel == null ? null : scoreLevel.trim();
	}

	/**
	 * 是否合格，0为是，1为否；
	 *
	 * @return ：be_suffice 是否合格，0为是，1为否；
	 */
	public Integer getBeSuffice() {
		return beSuffice;
	}

	/**
	 * 是否合格，0为是，1为否；
	 *
	 * @param beSuffice：是否合格，0为是，1为否；
	 */
	public void setBeSuffice(Integer beSuffice) {
		this.beSuffice = beSuffice;
	}

	/**
	 * 流程的任务ID；
	 *
	 * @return ：taskId 流程的任务ID；
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * 流程的任务ID；
	 *
	 * @param taskId：流程的任务ID；
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * 待办事项ID；
	 *
	 * @return ：item_id 待办事项ID；
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * 待办事项ID；
	 *
	 * @param itemId：待办事项ID；
	 */
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	/**
	 * 流程审批节点；
	 *
	 * @return ：process_state 流程审批节点；
	 */
	public Integer getProcessState() {
		return processState;
	}

	/**
	 * 流程审批节点；
	 *
	 * @param processState：流程审批节点；
	 */
	public void setProcessState(Integer processState) {
		this.processState = processState;
	}

	/**
	 * 权限访问码；
	 *
	 * @return ：valid_code 权限访问码；
	 */
	public String getValidCode() {
		return validCode;
	}

	/**
	 * 权限访问码；
	 *
	 * @param validCode：权限访问码；
	 */
	public void setValidCode(String validCode) {
		this.validCode = validCode == null ? null : validCode.trim();
	}

	public Integer getSchemeType() {
		return schemeType;
	}

	public void setSchemeType(Integer schemeType) {
		this.schemeType = schemeType;
	}

	@Override
	public String toString() {
		return "PerformanceScore{" +
				"scoreId=" + scoreId +
				", proId=" + proId +
				", proDesc='" + proDesc + '\'' +
				", schId=" + schId +
				", schDesc='" + schDesc + '\'' +
				", userId=" + userId +
				", userName='" + userName + '\'' +
				", userGender=" + userGender +
				", deptId=" + deptId +
				", deptName='" + deptName + '\'' +
				", postId=" + postId +
				", postName='" + postName + '\'' +
				", scoreSelf=" + scoreSelf +
				", scoreLeader=" + scoreLeader +
				", scoreGroup=" + scoreGroup +
				", scoreTotal=" + scoreTotal +
				", scoreLevel='" + scoreLevel + '\'' +
				", beSuffice=" + beSuffice +
				", taskId='" + taskId + '\'' +
				", itemId=" + itemId +
				", processState=" + processState +
				", validCode='" + validCode + '\'' +
				'}';
	}
}