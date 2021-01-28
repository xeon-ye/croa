package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 员工转正记录实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployeeFormal extends EmployeeCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -6047757275248944147L;

	/**
	 * 主键ID；
	 */
	private Integer formId;

	/**
	 * 部门主管；
	 */
	private Integer deptLeader;

	/**
	 * 部门主管名称；
	 */
	private String deptLeaderName;

	/**
	 * 入职日期；
	 */
	private Date empDate;

	/**
	 * 状态，0为审核中，1为审核通过，2为审核拒绝，-1为删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：formId 主键ID；
	 */
	public Integer getFormId() {
		return formId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param formId：主键ID；
	 */
	public void setFormId(Integer formId) {
		this.formId = formId;
	}

	/**
	 * 部门主管；
	 * 
	 * @return ：deptLeader 部门主管；
	 */
	public Integer getDeptLeader() {
		return deptLeader;
	}

	/**
	 * 部门主管；
	 * 
	 * @param deptLeader：部门主管；
	 */
	public void setDeptLeader(Integer deptLeader) {
		this.deptLeader = deptLeader;
	}

	/**
	 * 部门主管名称；
	 * 
	 * @return ：deptLeaderName 部门主管名称；
	 */
	public String getDeptLeaderName() {
		return deptLeaderName;
	}

	/**
	 * 部门主管名称；
	 * 
	 * @param deptLeaderName：部门主管名称；
	 */
	public void setDeptLeaderName(String deptLeaderName) {
		this.deptLeaderName = deptLeaderName == null ? null : deptLeaderName.trim();
	}

	/**
	 * 入职日期；
	 * 
	 * @return ：empDate 入职日期；
	 */
	public Date getEmpDate() {
		return empDate;
	}

	/**
	 * 入职日期；
	 * 
	 * @param empDate：入职日期；
	 */
	public void setEmpDate(Date empDate) {
		this.empDate = empDate;
	}

	@Override
	public String toString() {
		return "EmployeeFormal{" +
				"formId=" + formId +
				", deptLeader=" + deptLeader +
				", deptLeaderName='" + deptLeaderName + '\'' +
				", empDate=" + empDate +
				'}';
	}
}