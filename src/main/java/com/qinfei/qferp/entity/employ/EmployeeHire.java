package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 员工录用记录实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployeeHire extends EmployeeCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 3184474676926243133L;

	/**
	 * 主键ID；
	 */
	private Integer hireId;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 申请人性别，0为女，1为男；
	 */
	private Integer empGender;

	/**
	 * 出生日期；
	 */
	private Date empBirth;

	/**
	 * 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 */
	private Integer empMarriage;

	/**
	 * 学历；
	 */
	private Integer empEducation;

	/**
	 * 其他学历；
	 */
	private String empEducationOther;

	/**
	 * 毕业院校；
	 */
	private String empCollege;

	/**
	 * 专业；
	 */
	private String empMajor;

	/**
	 * 家庭住址；
	 */
	private String empLocalAddress;

	/**
	 * 拟到岗日期；
	 */
	private Date empExpectDate;

	/**
	 * 试用期开始；
	 */
	private Date trialBegin;

	/**
	 * 试用期结束；
	 */
	private Date trialEnd;

	/**
	 * 状态，0为审核中，1为审核通过，2为审核拒绝，-1为删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：hireId 主键ID；
	 */
	public Integer getHireId() {
		return hireId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param hireId：主键ID；
	 */
	public void setHireId(Integer hireId) {
		this.hireId = hireId;
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
	 * 申请人性别，0为女，1为男；
	 * 
	 * @return ：empGender 申请人性别，0为女，1为男；
	 */
	public Integer getEmpGender() {
		return empGender;
	}

	/**
	 * 申请人性别，0为女，1为男；
	 * 
	 * @param empGender：申请人性别，0为女，1为男；
	 */
	public void setEmpGender(Integer empGender) {
		this.empGender = empGender;
	}

	/**
	 * 出生日期；
	 *
	 * @return ：empBirth 出生日期；
	 */
	public Date getEmpBirth() {
		return empBirth;
	}

	/**
	 * 出生日期；
	 *
	 * @param empBirth：出生日期；
	 */
	public void setEmpBirth(Date empBirth) {
		this.empBirth = empBirth;
	}

	/**
	 * 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 *
	 * @return ：empMarriage 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 */
	public Integer getEmpMarriage() {
		return empMarriage;
	}

	/**
	 * 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 *
	 * @param empMarriage：婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 */
	public void setEmpMarriage(Integer empMarriage) {
		this.empMarriage = empMarriage;
	}

	/**
	 * 学历；
	 * 
	 * @return ：empEducation 学历；
	 */
	public Integer getEmpEducation() {
		return empEducation;
	}

	/**
	 * 学历；
	 * 
	 * @param empEducation：学历；
	 */
	public void setEmpEducation(Integer empEducation) {
		this.empEducation = empEducation;
	}

	/**
	 * 其他学历；
	 * 
	 * @return ：empEducationOther 其他学历；
	 */
	public String getEmpEducationOther() {
		return empEducationOther;
	}

	/**
	 * 其他学历；
	 * 
	 * @param empEducationOther：其他学历；
	 */
	public void setEmpEducationOther(String empEducationOther) {
		this.empEducationOther = empEducationOther == null ? null : empEducationOther.trim();
	}

	/**
	 * 毕业院校；
	 * 
	 * @return ：empCollege 毕业院校；
	 */
	public String getEmpCollege() {
		return empCollege;
	}

	/**
	 * 毕业院校；
	 * 
	 * @param empCollege：毕业院校；
	 */
	public void setEmpCollege(String empCollege) {
		this.empCollege = empCollege == null ? null : empCollege.trim();
	}

	/**
	 * 专业；
	 * 
	 * @return ：empMajor 专业；
	 */
	public String getEmpMajor() {
		return empMajor;
	}

	/**
	 * 专业；
	 * 
	 * @param empMajor：专业；
	 */
	public void setEmpMajor(String empMajor) {
		this.empMajor = empMajor == null ? null : empMajor.trim();
	}

	/**
	 * 家庭住址；
	 * 
	 * @return ：empLocalAddress 家庭住址；
	 */
	public String getEmpLocalAddress() {
		return empLocalAddress;
	}

	/**
	 * 家庭住址；
	 * 
	 * @param empLocalAddress：家庭住址；
	 */
	public void setEmpLocalAddress(String empLocalAddress) {
		this.empLocalAddress = empLocalAddress == null ? null : empLocalAddress.trim();
	}

	/**
	 * 拟到岗日期；
	 * 
	 * @return ：empExpectDate 拟到岗日期；
	 */
	public Date getEmpExpectDate() {
		return empExpectDate;
	}

	/**
	 * 拟到岗日期；
	 * 
	 * @param empExpectDate：拟到岗日期；
	 */
	public void setEmpExpectDate(Date empExpectDate) {
		this.empExpectDate = empExpectDate;
	}

	/**
	 * 试用期开始；
	 * 
	 * @return ：trialBegin 试用期开始；
	 */
	public Date getTrialBegin() {
		return trialBegin;
	}

	/**
	 * 试用期开始；
	 * 
	 * @param trialBegin：试用期开始；
	 */
	public void setTrialBegin(Date trialBegin) {
		this.trialBegin = trialBegin;
	}

	/**
	 * 试用期结束；
	 * 
	 * @return ：trialEnd 试用期结束；
	 */
	public Date getTrialEnd() {
		return trialEnd;
	}

	/**
	 * 试用期结束；
	 * 
	 * @param trialEnd：试用期结束；
	 */
	public void setTrialEnd(Date trialEnd) {
		this.trialEnd = trialEnd;
	}

	@Override
	public String toString() {
		return "EmployeeHire{" +
				"hireId=" + hireId +
				", entryId=" + entryId +
				", empGender=" + empGender +
				", empBirth=" + empBirth +
				", empMarriage=" + empMarriage +
				", empEducation=" + empEducation +
				", empEducationOther='" + empEducationOther + '\'' +
				", empCollege='" + empCollege + '\'' +
				", empMajor='" + empMajor + '\'' +
				", empLocalAddress='" + empLocalAddress + '\'' +
				", empExpectDate=" + empExpectDate +
				", trialBegin=" + trialBegin +
				", trialEnd=" + trialEnd +
				'}';
	}
}