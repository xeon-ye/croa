package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 员工离职记录实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployeeLeave extends EmployeeCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 8995302239960564711L;

	/**
	 * 主键ID；
	 */
	private Integer leaveId;

	/**
	 * 部门主管；
	 */
	private Integer deptLeader;

	/**
	 * 部门主管名称；
	 */
	private String deptLeaderName;

	/**
	 * 员工状态，0为试用期，1为转正；
	 */
	private Integer empState;

	/**
	 * 入职日期；
	 */
	private Date empDate;

	/**
	 * 性质，0为公司，1为员工提出；
	 */
	private Integer leaveType;

	/**
	 * 性质内容，0为终止试用，1为辞退，2为合同期满终止续签，3为试用期内辞退，4为辞职，5为合同期满；
	 */
	private Integer leaveTypeContent;

	/**
	 * 公司原因，多选，存储选项的值，使用逗号隔开，0为薪资福利差，1为缺少培训提升机会，2为工作环境不佳，3为与同事关系不融洽，4为不满公司的政策和制度，5为与上司产生矛盾，6为缺少晋升机会，7为工作压力大，8为其他；
	 */
	private String leaveCompany;

	/**
	 * 其他公司原因；
	 */
	private String leaveCompanyOther;

	/**
	 * 个人原因，多选，存储选项的值，使用逗号隔开，0为找到更好的职位，1为身体健康原因，2为自行创业，3为转换行业，4为家庭原因，5为不续签劳动合同，6为继续升学（研修深造），7为其他；
	 */
	private String leavePerson;

	/**
	 * 其他个人原因；
	 */
	private String leavePersonOther;

	/**
	 * 其他原因，多选，存储选项的值，使用逗号隔开，0为自动离职，1为工作不胜任职位要求，2为解雇，3为其他；
	 */
	private String otherReason;

	/**
	 * 其他原因备注；
	 */
	private String otherReasonRemark;

	/**
	 * 离职日期；
	 */
	private Date leaveDate;

	/**
	 * 状态，0为审核中，1为审核通过，2为审核拒绝，3为离职完成，4为准备交接，5为交接处理中，-1为删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：leaveId 主键ID；
	 */
	public Integer getLeaveId() {
		return leaveId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param leaveId：主键ID；
	 */
	public void setLeaveId(Integer leaveId) {
		this.leaveId = leaveId;
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
	 * 性质，0为公司，1为员工提出；
	 * 
	 * @return ：leaveType 性质，0为公司，1为员工提出；
	 */
	public Integer getLeaveType() {
		return leaveType;
	}

	/**
	 * 性质，0为公司，1为员工提出；
	 * 
	 * @param leaveType：性质，0为公司，1为员工提出；
	 */
	public void setLeaveType(Integer leaveType) {
		this.leaveType = leaveType;
	}

	/**
	 * 性质内容，0为终止试用，1为辞退，2为合同期满终止续签，3为试用期内辞退，4为辞职，5为合同期满；
	 * 
	 * @return ：leaveTypeContent 性质内容，0为终止试用，1为辞退，2为合同期满终止续签，3为试用期内辞退，4为辞职，5为合同期满；
	 */
	public Integer getLeaveTypeContent() {
		return leaveTypeContent;
	}

	/**
	 * 性质内容，0为终止试用，1为辞退，2为合同期满终止续签，3为试用期内辞退，4为辞职，5为合同期满；
	 * 
	 * @param leaveTypeContent：性质内容，0为终止试用，1为辞退，2为合同期满终止续签，3为试用期内辞退，4为辞职，5为合同期满；
	 */
	public void setLeaveTypeContent(Integer leaveTypeContent) {
		this.leaveTypeContent = leaveTypeContent;
	}

	/**
	 * 公司原因，多选，存储选项的值，使用逗号隔开，0为薪资福利差，1为缺少培训提升机会，2为工作环境不佳，3为与同事关系不融洽，4为不满公司的政策和制度，5为与上司产生矛盾，6为缺少晋升机会，7为工作压力大，8为其他；
	 * 
	 * @return ：leaveCompany
	 *         公司原因，多选，存储选项的值，使用逗号隔开，0为薪资福利差，1为缺少培训提升机会，2为工作环境不佳，3为与同事关系不融洽，4为不满公司的政策和制度，5为与上司产生矛盾，6为缺少晋升机会，7为工作压力大，8为其他；
	 */
	public String getLeaveCompany() {
		return leaveCompany;
	}

	/**
	 * 公司原因，多选，存储选项的值，使用逗号隔开，0为薪资福利差，1为缺少培训提升机会，2为工作环境不佳，3为与同事关系不融洽，4为不满公司的政策和制度，5为与上司产生矛盾，6为缺少晋升机会，7为工作压力大，8为其他；
	 * 
	 * @param leaveCompany：公司原因，多选，存储选项的值，使用逗号隔开，0为薪资福利差，1为缺少培训提升机会，2为工作环境不佳，3为与同事关系不融洽，4为不满公司的政策和制度，5为与上司产生矛盾，6为缺少晋升机会，7为工作压力大，8为其他；
	 */
	public void setLeaveCompany(String leaveCompany) {
		this.leaveCompany = leaveCompany == null ? null : leaveCompany.trim();
	}

	/**
	 * 其他公司原因；
	 * 
	 * @return ：leaveCompanyOther 其他公司原因；
	 */
	public String getLeaveCompanyOther() {
		return leaveCompanyOther;
	}

	/**
	 * 其他公司原因；
	 * 
	 * @param leaveCompanyOther：其他公司原因；
	 */
	public void setLeaveCompanyOther(String leaveCompanyOther) {
		this.leaveCompanyOther = leaveCompanyOther == null ? null : leaveCompanyOther.trim();
	}

	/**
	 * 个人原因，多选，存储选项的值，使用逗号隔开，0为找到更好的职位，1为身体健康原因，2为自行创业，3为转换行业，4为家庭原因，5为不续签劳动合同，6为继续升学（研修深造），7为其他；
	 * 
	 * @return ：leavePerson
	 *         个人原因，多选，存储选项的值，使用逗号隔开，0为找到更好的职位，1为身体健康原因，2为自行创业，3为转换行业，4为家庭原因，5为不续签劳动合同，6为继续升学（研修深造），7为其他；
	 */
	public String getLeavePerson() {
		return leavePerson;
	}

	/**
	 * 个人原因，多选，存储选项的值，使用逗号隔开，0为找到更好的职位，1为身体健康原因，2为自行创业，3为转换行业，4为家庭原因，5为不续签劳动合同，6为继续升学（研修深造），7为其他；
	 * 
	 * @param leavePerson：个人原因，多选，存储选项的值，使用逗号隔开，0为找到更好的职位，1为身体健康原因，2为自行创业，3为转换行业，4为家庭原因，5为不续签劳动合同，6为继续升学（研修深造），7为其他；
	 */
	public void setLeavePerson(String leavePerson) {
		this.leavePerson = leavePerson == null ? null : leavePerson.trim();
	}

	/**
	 * 其他个人原因；
	 * 
	 * @return ：leavePersonOther 其他个人原因；
	 */
	public String getLeavePersonOther() {
		return leavePersonOther;
	}

	/**
	 * 其他个人原因；
	 * 
	 * @param leavePersonOther：其他个人原因；
	 */
	public void setLeavePersonOther(String leavePersonOther) {
		this.leavePersonOther = leavePersonOther == null ? null : leavePersonOther.trim();
	}

	/**
	 * 其他原因，多选，存储选项的值，使用逗号隔开，0为自动离职，1为工作不胜任职位要求，2为解雇，3为其他；
	 * 
	 * @return ：otherReason 其他原因，多选，存储选项的值，使用逗号隔开，0为自动离职，1为工作不胜任职位要求，2为解雇，3为其他；
	 */
	public String getOtherReason() {
		return otherReason;
	}

	/**
	 * 其他原因，多选，存储选项的值，使用逗号隔开，0为自动离职，1为工作不胜任职位要求，2为解雇，3为其他；
	 * 
	 * @param otherReason：其他原因，多选，存储选项的值，使用逗号隔开，0为自动离职，1为工作不胜任职位要求，2为解雇，3为其他；
	 */
	public void setOtherReason(String otherReason) {
		this.otherReason = otherReason == null ? null : otherReason.trim();
	}

	/**
	 * 其他原因备注；
	 * 
	 * @return ：otherReasonRemark 其他原因备注；
	 */
	public String getOtherReasonRemark() {
		return otherReasonRemark;
	}

	/**
	 * 其他原因备注；
	 * 
	 * @param otherReasonRemark：其他原因备注；
	 */
	public void setOtherReasonRemark(String otherReasonRemark) {
		this.otherReasonRemark = otherReasonRemark == null ? null : otherReasonRemark.trim();
	}

	/**
	 * 离职日期；
	 * 
	 * @return ：leaveDate 离职日期；
	 */
	public Date getLeaveDate() {
		return leaveDate;
	}

	/**
	 * 离职日期；
	 * 
	 * @param leaveDate：离职日期；
	 */
	public void setLeaveDate(Date leaveDate) {
		this.leaveDate = leaveDate;
	}

	@Override
	public String toString() {
		return "EmployeeLeave{" +
				"leaveId=" + leaveId +
				", deptLeader=" + deptLeader +
				", deptLeaderName='" + deptLeaderName + '\'' +
				", empState=" + empState +
				", empDate=" + empDate +
				", leaveType=" + leaveType +
				", leaveTypeContent=" + leaveTypeContent +
				", leaveCompany='" + leaveCompany + '\'' +
				", leaveCompanyOther='" + leaveCompanyOther + '\'' +
				", leavePerson='" + leavePerson + '\'' +
				", leavePersonOther='" + leavePersonOther + '\'' +
				", otherReason='" + otherReason + '\'' +
				", otherReasonRemark='" + otherReasonRemark + '\'' +
				", leaveDate=" + leaveDate +
				'}';
	}
}