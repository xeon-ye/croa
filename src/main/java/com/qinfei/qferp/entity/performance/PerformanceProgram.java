package com.qinfei.qferp.entity.performance;

import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.employ.EmployCommon;

/**
 * 考核计划关联考核方案的实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0001 9:49；
 */
public class PerformanceProgram extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 7364170589711780948L;

	/**
	 * 主键ID；
	 */
	private Integer graId;

	/**
	 * 计划ID；
	 */
	private Integer proId;

	/**
	 * 计划名称；
	 */
	private String proName;

	/**
	 * 方案ID；
	 */
	private Integer schId;

	/**
	 * 方案名称；
	 */
	private String schName;

	/**
	 * 考核的用户ID；
	 */
	private String schUserId;

	/**
	 * 考核的用户名称；
	 */
	private String schUserName;

	/**
	 * 计划对应的职位ID；
	 */
	private Integer postId;

	/**
	 * 适用的职位名称；
	 */
	private String postName;
	//评价等级
	@Transient
	private String schLevel;
	//合格等级
	@Transient
	private String schSuffice;
	@Transient
	private Integer schState;
	@Transient
	private Integer schemeType;

	/**
	 * 主键ID；
	 *
	 * @return ：gra_id 主键ID；
	 */
	public Integer getGraId() {
		return graId;
	}

	/**
	 * 主键ID；
	 *
	 * @param graId：主键ID；
	 */
	public void setGraId(Integer graId) {
		this.graId = graId;
	}

	/**
	 * 计划ID；
	 *
	 * @return ：pro_id 计划ID；
	 */
	public Integer getProId() {
		return proId;
	}

	/**
	 * 计划ID；
	 *
	 * @param proId：计划ID；
	 */
	public void setProId(Integer proId) {
		this.proId = proId;
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
	 * 方案ID；
	 *
	 * @return ：sch_id 方案ID；
	 */
	public Integer getSchId() {
		return schId;
	}

	/**
	 * 方案ID；
	 *
	 * @param schId：方案ID；
	 */
	public void setSchId(Integer schId) {
		this.schId = schId;
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
	 * 考核的用户ID；
	 *
	 * @return ：sch_user_id 考核的用户ID；
	 */
	public String getSchUserId() {
		return schUserId;
	}

	/**
	 * 考核的用户ID；
	 *
	 * @param schUserId：考核的用户ID；
	 */
	public void setSchUserId(String schUserId) {
		this.schUserId = schUserId == null ? null : schUserId.trim();
	}

	/**
	 * 考核的用户名称；
	 *
	 * @return ：sch_user_name 考核的用户名称；
	 */
	public String getSchUserName() {
		return schUserName;
	}

	/**
	 * 考核的用户名称；
	 *
	 * @param schUserName：考核的用户名称；
	 */
	public void setSchUserName(String schUserName) {
		this.schUserName = schUserName == null ? null : schUserName.trim();
	}

	/**
	 * 计划对应的职位ID；
	 *
	 * @return ：post_id 计划对应的职位ID；
	 */
	public Integer getPostId() {
		return postId;
	}

	/**
	 * 计划对应的职位ID；
	 *
	 * @param postId：计划对应的职位ID；
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

	public String getSchLevel() {
		return schLevel;
	}

	public void setSchLevel(String schLevel) {
		this.schLevel = schLevel;
	}

	public String getSchSuffice() {
		return schSuffice;
	}

	public void setSchSuffice(String schSuffice) {
		this.schSuffice = schSuffice;
	}

	public Integer getSchState() {
		return schState;
	}

	public void setSchState(Integer schState) {
		this.schState = schState;
	}

	public Integer getSchemeType() {
		return schemeType;
	}

	public void setSchemeType(Integer schemeType) {
		this.schemeType = schemeType;
	}

	@Override
	public String toString() {
		return "PerformanceProgram{" +
				"graId=" + graId +
				", proId=" + proId +
				", proName='" + proName + '\'' +
				", schId=" + schId +
				", schName='" + schName + '\'' +
				", schUserId='" + schUserId + '\'' +
				", schUserName='" + schUserName + '\'' +
				", postId=" + postId +
				", postName='" + postName + '\'' +
				'}';
	}
}