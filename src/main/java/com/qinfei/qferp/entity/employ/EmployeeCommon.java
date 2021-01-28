package com.qinfei.qferp.entity.employ;

/**
 * 员工通用属性实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/03/27 0025 15:07；
 */
public class EmployeeCommon extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 1787493333613733775L;

	/**
	 * 员工ID；
	 */
	private Integer empId;

	/**
	 * 员工工号；
	 */
	private String empNum;

	/**
	 * 申请人姓名；
	 */
	private String empName;

	/**
	 * 一级部门：0-业务部门、1-媒介部门、2-其他部门
	 */
	private Integer entryFirstDept;

	/**
	 * 部门；
	 */
	private Integer empDept;

	/**
	 * 部门名称；
	 */
	private String empDeptName;

	/**
	 * 职位；
	 */
	private Integer empProfession;

	/**
	 * 职位名称；
	 */
	private String empProfessionName;

	private Integer entryState; //员工录用状态

	/**
	 * 员工ID；
	 * 
	 * @return ：empId 员工ID；
	 */
	public Integer getEmpId() {
		return empId;
	}

	/**
	 * 员工ID；
	 * 
	 * @param empId：员工ID；
	 */
	public void setEmpId(Integer empId) {
		this.empId = empId;
	}

	/**
	 * 员工工号；
	 *
	 * @return ：empNum 员工工号；
	 */
	public String getEmpNum() {
		return empNum;
	}

	/**
	 * 员工工号；
	 *
	 * @param empNum：员工工号；
	 */
	public void setEmpNum(String empNum) {
		this.empNum = empNum == null ? null : empNum.trim();
	}

	/**
	 * 申请人姓名；
	 * 
	 * @return ：empName 申请人姓名；
	 */
	public String getEmpName() {
		return empName;
	}

	/**
	 * 申请人姓名；
	 * 
	 * @param empName：申请人姓名；
	 */
	public void setEmpName(String empName) {
		this.empName = empName == null ? null : empName.trim();
	}

	/**
	 * 部门；
	 * 
	 * @return ：empDept 部门；
	 */
	public Integer getEmpDept() {
		return empDept;
	}

	/**
	 * 部门；
	 * 
	 * @param empDept：部门；
	 */
	public void setEmpDept(Integer empDept) {
		this.empDept = empDept;
	}

	/**
	 * 部门名称；
	 * 
	 * @return ：empDeptName 部门名称；
	 */
	public String getEmpDeptName() {
		return empDeptName;
	}

	/**
	 * 部门名称；
	 * 
	 * @param empDeptName：部门名称；
	 */
	public void setEmpDeptName(String empDeptName) {
		this.empDeptName = empDeptName == null ? null : empDeptName.trim();
	}

	/**
	 * 职位；
	 * 
	 * @return ：empProfession 职位；
	 */
	public Integer getEmpProfession() {
		return empProfession;
	}

	/**
	 * 职位；
	 * 
	 * @param empProfession：职位；
	 */
	public void setEmpProfession(Integer empProfession) {
		this.empProfession = empProfession;
	}

	/**
	 * 职位名称；
	 * 
	 * @return ：empProfessionName 职位名称；
	 */
	public String getEmpProfessionName() {
		return empProfessionName;
	}

	/**
	 * 职位名称；
	 * 
	 * @param empProfessionName：职位名称；
	 */
	public void setEmpProfessionName(String empProfessionName) {
		this.empProfessionName = empProfessionName == null ? null : empProfessionName.trim();
	}

	public Integer getEntryFirstDept() {
		return entryFirstDept;
	}

	public void setEntryFirstDept(Integer entryFirstDept) {
		this.entryFirstDept = entryFirstDept;
	}

	public Integer getEntryState() {
		return entryState;
	}

	public void setEntryState(Integer entryState) {
		this.entryState = entryState;
	}

	@Override
	public String toString() {
		return "EmployeeCommon{" +
				"empId=" + empId +
				", empNum='" + empNum + '\'' +
				", empName='" + empName + '\'' +
				", entryFirstDept=" + entryFirstDept +
				", empDept=" + empDept +
				", empDeptName='" + empDeptName + '\'' +
				", empProfession=" + empProfession +
				", empProfessionName='" + empProfessionName + '\'' +
				", entryState=" + entryState +
				'}';
	}
}