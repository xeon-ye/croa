package com.qinfei.qferp.entity.performance;

import com.qinfei.qferp.entity.employ.EmployCommon;

/**
 * 考核细则实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0001 9:49；
 */
public class PerformancePlate extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -3882158397619835719L;

	/**
	 * 主键ID；
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
	 * 考核内容；
	 */
	private String plateContent;

	/**
	 * 排序编号；
	 */
	private Integer plateOrder;

	/**
	 * 部门ID；
	 */
	private Integer deptId;

	/**
	 * 主键ID；
	 *
	 * @return ：plate_id 主键ID；
	 */
	public Integer getPlateId() {
		return plateId;
	}

	/**
	 * 主键ID；
	 *
	 * @param plateId：主键ID；
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

	@Override
	public String toString() {
		return "PerformancePlate{" +
				"plateId=" + plateId +
				", plateLevel=" + plateLevel +
				", plateProportion=" + plateProportion +
				", plateParent=" + plateParent +
				", plateContent='" + plateContent + '\'' +
				", plateOrder=" + plateOrder +
				", deptId=" + deptId +
				'}';
	}
}