package com.qinfei.qferp.entity.performance;

import com.qinfei.qferp.entity.employ.EmployCommon;

/**
 * 考核方案关联的考核细则实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0001 9:49；
 */
public class PerformanceHistory extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -2111866140960509407L;

	/**
	 * 主键ID；
	 */
	private Integer historyId;

	/**
	 * 考核细则ID；
	 */
	private Integer plateId;

	/**
	 * 项目层级，0为板块，1为项目，2为细则；
	 */
	private Integer plateLevel;

	/**
	 * 权重、分值；
	 */
	private Float plateProportion;

	/**
	 * 上级ID；
	 */
	private Integer plateParent;

	/**
	 * 考核指标
	 */
	private String plateTarget;

	/**
	 * 指标要求
	 */
	private String plateDemand;

	/**
	 * 排序编号；
	 */
	private Integer plateOrder;

	/**
	 * 考核内容；
	 */
	private String plateContent;

	/**
	 * 关联的方案ID；
	 */
	private Integer schId;

	/**
	 * 部门ID；
	 */
	private Integer deptId;

	/**
	 * 主键ID；
	 *
	 * @return ：history_id 主键ID；
	 */
	public Integer getHistoryId() {
		return historyId;
	}

	/**
	 * 主键ID；
	 *
	 * @param historyId：主键ID；
	 */
	public void setHistoryId(Integer historyId) {
		this.historyId = historyId;
	}

	/**
	 * 考核细则ID；
	 *
	 * @return ：plate_id 考核细则ID；
	 */
	public Integer getPlateId() {
		return plateId;
	}

	/**
	 * 考核细则ID；
	 *
	 * @param plateId：考核细则ID；
	 */
	public void setPlateId(Integer plateId) {
		this.plateId = plateId;
	}

	/**
	 * 项目层级，0为板块，1为项目，2为细则；
	 *
	 * @return ：plate_level 项目层级，0为板块，1为项目，2为细则；
	 */
	public Integer getPlateLevel() {
		return plateLevel;
	}

	/**
	 * 项目层级，0为板块，1为项目，2为细则；
	 *
	 * @param plateLevel：项目层级，0为板块，1为项目，2为细则；
	 */
	public void setPlateLevel(Integer plateLevel) {
		this.plateLevel = plateLevel;
	}

	/**
	 * 权重、分值；
	 *
	 * @return ：plate_proportion 权重、分值；
	 */
	public Float getPlateProportion() {
		return plateProportion;
	}

	/**
	 * 权重、分值；
	 *
	 * @param plateProportion：权重、分值；
	 */
	public void setPlateProportion(Float plateProportion) {
		this.plateProportion = plateProportion;
	}

	/**
	 * 上级ID；
	 *
	 * @return ：plate_parent 上级ID；
	 */
	public Integer getPlateParent() {
		return plateParent;
	}

	/**
	 * 上级ID；
	 *
	 * @param plateParent：上级ID；
	 */
	public void setPlateParent(Integer plateParent) {
		this.plateParent = plateParent;
	}

	/**
	 * 考核内容；
	 *
	 * @return ：plate_content 考核内容；
	 */
	public String getPlateContent() {
		return plateContent;
	}

	/**
	 * 考核内容；
	 *
	 * @param plateContent：考核内容；
	 */
	public void setPlateContent(String plateContent) {
		this.plateContent = plateContent == null ? null : plateContent.trim();
	}

	/**
	 * 排序编号；
	 *
	 * @return ：plateOrder 排序编号；
	 */
	public Integer getPlateOrder() {
		return plateOrder;
	}

	/**
	 * 排序编号；
	 *
	 * @param plateOrder：排序编号；
	 */
	public void setPlateOrder(Integer plateOrder) {
		this.plateOrder = plateOrder;
	}

	/**
	 * 关联的方案ID；
	 *
	 * @return ：sch_id 关联的方案ID；
	 */
	public Integer getSchId() {
		return schId;
	}

	/**
	 * 关联的方案ID；
	 *
	 * @param schId：关联的方案ID；
	 */
	public void setSchId(Integer schId) {
		this.schId = schId;
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

	public String getPlateTarget() {
		return plateTarget;
	}

	public void setPlateTarget(String plateTarget) {
		this.plateTarget = plateTarget;
	}

	public String getPlateDemand() {
		return plateDemand;
	}

	public void setPlateDemand(String plateDemand) {
		this.plateDemand = plateDemand;
	}

	@Override
	public String toString() {
		return "PerformanceHistory{" +
				"historyId=" + historyId +
				", plateId=" + plateId +
				", plateLevel=" + plateLevel +
				", plateProportion=" + plateProportion +
				", plateParent=" + plateParent +
				", plateOrder=" + plateOrder +
				", plateContent='" + plateContent + '\'' +
				", schId=" + schId +
				", deptId=" + deptId +
				'}';
	}
}