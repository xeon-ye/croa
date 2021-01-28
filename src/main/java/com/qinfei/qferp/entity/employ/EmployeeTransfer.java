package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 员工调岗实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployeeTransfer extends EmployeeCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -6805178957528915562L;

	/**
	 * 主键ID；
	 */
	private Integer tranId;

	/**
	 * 员工状态，0为试用期，1为转正；
	 */
	private Integer empState;

	/**
	 * 入职日期；
	 */
	private Date empDate;

	/**
	 * 调岗前工资；
	 */
	private Float beforeSalary;

	/**
	 * 调岗前职位工资；
	 */
	private Float beforePost;

	/**
	 * 调岗前绩效工资；
	 */
	private Float beforePerformance;

	/**
	 * 调岗前其他工资；
	 */
	private Float beforeOther;

	/**
	 * 调岗后工资；
	 */
	private Float afterSalary;

	/**
	 * 调岗后职位工资；
	 */
	private Float afterPost;

	/**
	 * 调岗后绩效工资；
	 */
	private Float afterPerformance;

	/**
	 * 调岗后其他工资；
	 */
	private Float afterOther;

	/**
	 * 调岗后的部门ID；
	 */
	private Integer afterDept;

	/**
	 * 调岗后的部门名称；
	 */
	private String afterDeptName;

	/**
	 * 调岗后的职位；
	 */
	private Integer afterProfession;

	/**
	 * 调岗后的职位名称；
	 */
	private String afterProfessionName;

	/**
	 * 调岗后绑定的角色id
	 */
	private Integer roleId;

	/**
	 * 调岗后绑定的角色类型
	 */
	private String roleType;

	/**
	 * 调岗后绑定的角色名称
	 */
	private String roleName;

	/**
	 * 执行日期；
	 */
	private Date transDate;

	/**
	 * 申请原因；
	 */
	private String transReason;

	/**
	 * 状态，0为审核中，1为审核通过，2为审核拒绝，3为调岗完成，4为准备交接，5为交接处理中，-1为删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：tranId 主键ID；
	 */
	public Integer getTranId() {
		return tranId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param tranId：主键ID；
	 */
	public void setTranId(Integer tranId) {
		this.tranId = tranId;
	}

	/**
	 * 员工状态，0为试用期，1为转正；
	 *
	 * @return ：empState 员工状态，0为试用期，1为转正；
	 */
	public Integer getEmpState() {
		return empState;
	}

	/**
	 * 员工状态，0为试用期，1为转正；
	 *
	 * @param empState：员工状态，0为试用期，1为转正；
	 */
	public void setEmpState(Integer empState) {
		this.empState = empState;
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

	/**
	 * 调岗前工资；
	 * 
	 * @return ：beforeSalary 调岗前工资；
	 */
	public Float getBeforeSalary() {
		return beforeSalary;
	}

	/**
	 * 调岗前工资；
	 * 
	 * @param beforeSalary：调岗前工资；
	 */
	public void setBeforeSalary(Float beforeSalary) {
		this.beforeSalary = beforeSalary;
	}

	/**
	 * 调岗前职位工资；
	 * 
	 * @return ：beforePost 调岗前职位工资；
	 */
	public Float getBeforePost() {
		return beforePost;
	}

	/**
	 * 调岗前职位工资；
	 * 
	 * @param beforePost：调岗前职位工资；
	 */
	public void setBeforePost(Float beforePost) {
		this.beforePost = beforePost;
	}

	/**
	 * 调岗前绩效工资；
	 * 
	 * @return ：beforePerformance 调岗前绩效工资；
	 */
	public Float getBeforePerformance() {
		return beforePerformance;
	}

	/**
	 * 调岗前绩效工资；
	 * 
	 * @param beforePerformance：调岗前绩效工资；
	 */
	public void setBeforePerformance(Float beforePerformance) {
		this.beforePerformance = beforePerformance;
	}

	/**
	 * 调岗前其他工资；
	 * 
	 * @return ：beforeOther 调岗前其他工资；
	 */
	public Float getBeforeOther() {
		return beforeOther;
	}

	/**
	 * 调岗前其他工资；
	 * 
	 * @param beforeOther：调岗前其他工资；
	 */
	public void setBeforeOther(Float beforeOther) {
		this.beforeOther = beforeOther;
	}

	/**
	 * 调岗后工资；
	 * 
	 * @return ：afterSalary 调岗后工资；
	 */
	public Float getAfterSalary() {
		return afterSalary;
	}

	/**
	 * 调岗后工资；
	 * 
	 * @param afterSalary：调岗后工资；
	 */
	public void setAfterSalary(Float afterSalary) {
		this.afterSalary = afterSalary;
	}

	/**
	 * 调岗后职位工资；
	 * 
	 * @return ：afterPost 调岗后职位工资；
	 */
	public Float getAfterPost() {
		return afterPost;
	}

	/**
	 * 调岗后职位工资；
	 * 
	 * @param afterPost：调岗后职位工资；
	 */
	public void setAfterPost(Float afterPost) {
		this.afterPost = afterPost;
	}

	/**
	 * 调岗后绩效工资；
	 * 
	 * @return ：afterPerformance 调岗后绩效工资；
	 */
	public Float getAfterPerformance() {
		return afterPerformance;
	}

	/**
	 * 调岗后绩效工资；
	 * 
	 * @param afterPerformance：调岗后绩效工资；
	 */
	public void setAfterPerformance(Float afterPerformance) {
		this.afterPerformance = afterPerformance;
	}

	/**
	 * 调岗后其他工资；
	 * 
	 * @return ：afterOther 调岗后其他工资；
	 */
	public Float getAfterOther() {
		return afterOther;
	}

	/**
	 * 调岗后其他工资；
	 * 
	 * @param afterOther：调岗后其他工资；
	 */
	public void setAfterOther(Float afterOther) {
		this.afterOther = afterOther;
	}

	/**
	 * 调岗后的部门ID；
	 * 
	 * @return ：afterDept 调岗后的部门ID；
	 */
	public Integer getAfterDept() {
		return afterDept;
	}

	/**
	 * 调岗后的部门ID；
	 * 
	 * @param afterDept：调岗后的部门ID；
	 */
	public void setAfterDept(Integer afterDept) {
		this.afterDept = afterDept;
	}

	/**
	 * 调岗后的部门名称；
	 * 
	 * @return ：afterDeptName 调岗后的部门名称；
	 */
	public String getAfterDeptName() {
		return afterDeptName;
	}

	/**
	 * 调岗后的部门名称；
	 * 
	 * @param afterDeptName：调岗后的部门名称；
	 */
	public void setAfterDeptName(String afterDeptName) {
		this.afterDeptName = afterDeptName == null ? null : afterDeptName.trim();
	}

	/**
	 * 调岗后的职位；
	 * 
	 * @return ：afterProfession 调岗后的职位；
	 */
	public Integer getAfterProfession() {
		return afterProfession;
	}

	/**
	 * 调岗后的职位；
	 * 
	 * @param afterProfession：调岗后的职位；
	 */
	public void setAfterProfession(Integer afterProfession) {
		this.afterProfession = afterProfession;
	}

	/**
	 * 调岗后的职位名称；
	 * 
	 * @return ：afterProfessionName 调岗后的职位名称；
	 */
	public String getAfterProfessionName() {
		return afterProfessionName;
	}

	/**
	 * 调岗后的职位名称；
	 * 
	 * @param afterProfessionName：调岗后的职位名称；
	 */
	public void setAfterProfessionName(String afterProfessionName) {
		this.afterProfessionName = afterProfessionName == null ? null : afterProfessionName.trim();
	}

	/**
	 * 执行日期；
	 * 
	 * @return ：transDate 执行日期；
	 */
	public Date getTransDate() {
		return transDate;
	}

	/**
	 * 执行日期；
	 * 
	 * @param transDate：执行日期；
	 */
	public void setTransDate(Date transDate) {
		this.transDate = transDate;
	}

	/**
	 * 申请原因；
	 * 
	 * @return ：transReason 申请原因；
	 */
	public String getTransReason() {
		return transReason;
	}

	/**
	 * 申请原因；
	 * 
	 * @param transReason：申请原因；
	 */
	public void setTransReason(String transReason) {
		this.transReason = transReason == null ? null : transReason.trim();
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public String toString() {
		return "EmployeeTransfer{" +
				"tranId=" + tranId +
				", empState=" + empState +
				", empDate=" + empDate +
				", beforeSalary=" + beforeSalary +
				", beforePost=" + beforePost +
				", beforePerformance=" + beforePerformance +
				", beforeOther=" + beforeOther +
				", afterSalary=" + afterSalary +
				", afterPost=" + afterPost +
				", afterPerformance=" + afterPerformance +
				", afterOther=" + afterOther +
				", afterDept=" + afterDept +
				", afterDeptName='" + afterDeptName + '\'' +
				", afterProfession=" + afterProfession +
				", afterProfessionName='" + afterProfessionName + '\'' +
				", transDate=" + transDate +
				", transReason='" + transReason + '\'' +
				'}';
	}
}