package com.qinfei.qferp.entity.employ;

/**
 * 员工薪资实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployeeSalary extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -1494104066133511413L;

	/**
	 * 主键ID；
	 */
	private Integer salId;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 员工ID；
	 */
	private Integer empId;

	/**
	 * 员工工号；
	 */
	private String empNum;

	/**
	 * 试用期工资；
	 */
	private Float trialSalary;

	/**
	 * 职位工资；
	 */
	private Float trialPost;

	/**
	 * 绩效工资；
	 */
	private Float trialPerformance;

	/**
	 * 其他工资；
	 */
	private Float trialOther;

	/**
	 * 转正工资；
	 */
	private Float formalSalary;

	/**
	 * 转正职位工资；
	 */
	private Float formalPost;

	/**
	 * 转正绩效工资；
	 */
	private Float formalPerformance;

	/**
	 * 转正其他工资；
	 */
	private Float formalOther;

	/**
	 * 状态，0为正常，5为已离职，-1为已删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：salId 主键ID；
	 */
	public Integer getSalId() {
		return salId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param salId：主键ID；
	 */
	public void setSalId(Integer salId) {
		this.salId = salId;
	}

	/**
	 * 入职申请ID；
	 *
	 * @return ：entryId 入职申请ID；
	 */
	public Integer getEntryId() {
		return entryId;
	}

	/**
	 * 入职申请ID；
	 *
	 * @param entryId：入职申请ID；
	 */
	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

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
	 * 试用期工资；
	 * 
	 * @return ：trialSalary 试用期工资；
	 */
	public Float getTrialSalary() {
		return trialSalary;
	}

	/**
	 * 试用期工资；
	 * 
	 * @param trialSalary：试用期工资；
	 */
	public void setTrialSalary(Float trialSalary) {
		this.trialSalary = trialSalary;
	}

	/**
	 * 职位工资；
	 * 
	 * @return ：trialPost 职位工资；
	 */
	public Float getTrialPost() {
		return trialPost;
	}

	/**
	 * 职位工资；
	 * 
	 * @param trialPost：职位工资；
	 */
	public void setTrialPost(Float trialPost) {
		this.trialPost = trialPost;
	}

	/**
	 * 绩效工资；
	 * 
	 * @return ：trialPerformance 绩效工资；
	 */
	public Float getTrialPerformance() {
		return trialPerformance;
	}

	/**
	 * 绩效工资；
	 * 
	 * @param trialPerformance：绩效工资；
	 */
	public void setTrialPerformance(Float trialPerformance) {
		this.trialPerformance = trialPerformance;
	}

	/**
	 * 其他工资；
	 * 
	 * @return ：trialOther 其他工资；
	 */
	public Float getTrialOther() {
		return trialOther;
	}

	/**
	 * 其他工资；
	 * 
	 * @param trialOther：其他工资；
	 */
	public void setTrialOther(Float trialOther) {
		this.trialOther = trialOther;
	}

	/**
	 * 转正工资；
	 * 
	 * @return ：formalSalary 转正工资；
	 */
	public Float getFormalSalary() {
		return formalSalary;
	}

	/**
	 * 转正工资；
	 * 
	 * @param formalSalary：转正工资；
	 */
	public void setFormalSalary(Float formalSalary) {
		this.formalSalary = formalSalary;
	}

	/**
	 * 转正职位工资；
	 * 
	 * @return ：formalPost 转正职位工资；
	 */
	public Float getFormalPost() {
		return formalPost;
	}

	/**
	 * 转正职位工资；
	 * 
	 * @param formalPost：转正职位工资；
	 */
	public void setFormalPost(Float formalPost) {
		this.formalPost = formalPost;
	}

	/**
	 * 转正绩效工资；
	 * 
	 * @return ：formalPerformance 转正绩效工资；
	 */
	public Float getFormalPerformance() {
		return formalPerformance;
	}

	/**
	 * 转正绩效工资；
	 * 
	 * @param formalPerformance：转正绩效工资；
	 */
	public void setFormalPerformance(Float formalPerformance) {
		this.formalPerformance = formalPerformance;
	}

	/**
	 * 转正其他工资；
	 * 
	 * @return ：formalOther 转正其他工资；
	 */
	public Float getFormalOther() {
		return formalOther;
	}

	/**
	 * 转正其他工资；
	 * 
	 * @param formalOther：转正其他工资；
	 */
	public void setFormalOther(Float formalOther) {
		this.formalOther = formalOther;
	}

	@Override
	public String toString() {
		return "EmployeeSalary{" +
				"salId=" + salId +
				", entryId=" + entryId +
				", empId=" + empId +
				", empNum='" + empNum + '\'' +
				", trialSalary=" + trialSalary +
				", trialPost=" + trialPost +
				", trialPerformance=" + trialPerformance +
				", trialOther=" + trialOther +
				", formalSalary=" + formalSalary +
				", formalPost=" + formalPost +
				", formalPerformance=" + formalPerformance +
				", formalOther=" + formalOther +
				'}';
	}
}