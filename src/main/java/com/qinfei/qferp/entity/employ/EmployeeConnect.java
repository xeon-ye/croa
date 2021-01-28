package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 交接清单实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployeeConnect extends EmployeeCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 1414510948604870327L;

	/**
	 * 主键ID；
	 */
	private Integer conId;

	/**
	 * 交接类型，0为离职，1为调岗；
	 */
	private Integer conType;

	/**
	 * 关联的数据ID；
	 */
	private Integer conData;

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
	 * 状态，0为试用，1为转正，2为离职，-1为已删除；
	 */
	private Integer empState;

	/**
	 * 性质，0为公司，1为员工提出；
	 */
	private Integer leaveType;

	/**
	 * 性质内容，0为终止试用，1为辞退，2为合同期满终止续签，3为试用期内辞退，4为辞职，5为合同期满；
	 */
	private Integer leaveTypeContent;

	/**
	 * 时间类型，0为本人书面申请时间，1为公司通知时间；
	 */
	private Integer conDateType;

	/**
	 * 时间；
	 */
	private Date conDate;

	/**
	 * 工作接收人的ID；
	 */
	private Integer conEmpId;

	/**
	 * 工作接收人的名称；
	 */
	private String conEmpName;

	/**
	 * 部门审核内容；
	 */
	private String conDeptApprove;

	/**
	 * 交接清单；
	 */
	private String conList;

	/**
	 * 部门其他说明；
	 */
	private String conDeptRemark;

	/**
	 * 人事部审核内容；
	 */
	private String conPersonal;

	/**
	 * 人事部专员；
	 */
	private Integer conPersonalId;

	/**
	 * 人事部专员名称；
	 */
	private String conPersonalName;

	/**
	 * 人事部审核的其他内容；
	 */
	private String conPersonalRemark;

	/**
	 * 离职时间或执行日期；
	 */
	private Date completeDate;

	/**
	 * 违约金；
	 */
	private Float breakMoney;

	/**
	 * 财务确认人ID；
	 */
	private Integer conFinanceId;

	/**
	 * 财务确认人名称；
	 */
	private String conFinanceName;

	/**
	 * 财务审核；
	 */
	private String conFinance;

	/**
	 * 财务审核的其他内容；
	 */
	private String conFinanceRemark;

	/**
	 * 附件，交接的附件；
	 */
	private String conFile;

	/**
	 * 状态，0为审核中，1为审核通过，2为审核拒绝，3为交接完成，-1为删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：conId 主键ID；
	 */
	public Integer getConId() {
		return conId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param conId：主键ID；
	 */
	public void setConId(Integer conId) {
		this.conId = conId;
	}

	/**
	 * 交接类型，0为离职，1为调岗；
	 * 
	 * @return ：conType 交接类型，0为离职，1为调岗；
	 */
	public Integer getConType() {
		return conType;
	}

	/**
	 * 交接类型，0为离职，1为调岗；
	 * 
	 * @param conType：交接类型，0为离职，1为调岗；
	 */
	public void setConType(Integer conType) {
		this.conType = conType;
	}

	/**
	 * 关联的数据ID；
	 * 
	 * @return ：conData 关联的数据ID；
	 */
	public Integer getConData() {
		return conData;
	}

	/**
	 * 关联的数据ID；
	 * 
	 * @param conData：关联的数据ID；
	 */
	public void setConData(Integer conData) {
		this.conData = conData;
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

	/**
	 * 状态，0为试用，1为转正，2为离职，-1为已删除；
	 * 
	 * @return ：empState 状态，0为试用，1为转正，2为离职，-1为已删除；
	 */
	public Integer getEmpState() {
		return empState;
	}

	/**
	 * 状态，0为试用，1为转正，2为离职，-1为已删除；
	 * 
	 * @param empState：状态，0为试用，1为转正，2为离职，-1为已删除；
	 */
	public void setEmpState(Integer empState) {
		this.empState = empState;
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
	 * 时间类型，0为本人书面申请时间，1为公司通知时间；
	 * 
	 * @return ：conDateType 时间类型，0为本人书面申请时间，1为公司通知时间；
	 */
	public Integer getConDateType() {
		return conDateType;
	}

	/**
	 * 时间类型，0为本人书面申请时间，1为公司通知时间；
	 * 
	 * @param conDateType：时间类型，0为本人书面申请时间，1为公司通知时间；
	 */
	public void setConDateType(Integer conDateType) {
		this.conDateType = conDateType;
	}

	/**
	 * 时间；
	 * 
	 * @return ：conDate 时间；
	 */
	public Date getConDate() {
		return conDate;
	}

	/**
	 * 时间；
	 * 
	 * @param conDate：时间；
	 */
	public void setConDate(Date conDate) {
		this.conDate = conDate;
	}

	/**
	 * 工作接收人的ID；
	 * 
	 * @return ：conEmpId 工作接收人的ID；
	 */
	public Integer getConEmpId() {
		return conEmpId;
	}

	/**
	 * 工作接收人的ID；
	 * 
	 * @param conEmpId：工作接收人的ID；
	 */
	public void setConEmpId(Integer conEmpId) {
		this.conEmpId = conEmpId;
	}

	/**
	 * 工作接收人的名称；
	 * 
	 * @return ：conEmpName 工作接收人的名称；
	 */
	public String getConEmpName() {
		return conEmpName;
	}

	/**
	 * 工作接收人的名称；
	 * 
	 * @param conEmpName：工作接收人的名称；
	 */
	public void setConEmpName(String conEmpName) {
		this.conEmpName = conEmpName == null ? null : conEmpName.trim();
	}

	/**
	 * 部门审核内容；
	 * 
	 * @return ：conDeptApprove 部门审核内容；
	 */
	public String getConDeptApprove() {
		return conDeptApprove;
	}

	/**
	 * 部门审核内容；
	 * 
	 * @param conDeptApprove：部门审核内容；
	 */
	public void setConDeptApprove(String conDeptApprove) {
		this.conDeptApprove = conDeptApprove == null ? null : conDeptApprove.trim();
	}

	/**
	 * 交接清单；
	 * 
	 * @return ：conList 交接清单；
	 */
	public String getConList() {
		return conList;
	}

	/**
	 * 交接清单；
	 * 
	 * @param conList：交接清单；
	 */
	public void setConList(String conList) {
		this.conList = conList == null ? null : conList.trim();
	}

	/**
	 * 部门其他说明；
	 * 
	 * @return ：conDeptRemark 部门其他说明；
	 */
	public String getConDeptRemark() {
		return conDeptRemark;
	}

	/**
	 * 部门其他说明；
	 * 
	 * @param conDeptRemark：部门其他说明；
	 */
	public void setConDeptRemark(String conDeptRemark) {
		this.conDeptRemark = conDeptRemark == null ? null : conDeptRemark.trim();
	}

	/**
	 * 人事部审核内容；
	 * 
	 * @return ：conPersonal 人事部审核内容；
	 */
	public String getConPersonal() {
		return conPersonal;
	}

	/**
	 * 人事部审核内容；
	 * 
	 * @param conPersonal：人事部审核内容；
	 */
	public void setConPersonal(String conPersonal) {
		this.conPersonal = conPersonal == null ? null : conPersonal.trim();
	}

	/**
	 * 人事部专员；
	 * 
	 * @return ：conPersonalId 人事部专员；
	 */
	public Integer getConPersonalId() {
		return conPersonalId;
	}

	/**
	 * 人事部专员；
	 * 
	 * @param conPersonalId：人事部专员；
	 */
	public void setConPersonalId(Integer conPersonalId) {
		this.conPersonalId = conPersonalId;
	}

	/**
	 * 人事部专员名称；
	 *
	 * @return ：conPersonalName 人事部专员名称；
	 */
	public String getConPersonalName() {
		return conPersonalName;
	}

	/**
	 * 人事部专员名称；
	 *
	 * @param conPersonalName：人事部专员名称；
	 */
	public void setConPersonalName(String conPersonalName) {
		this.conPersonalName = conPersonalName;
	}

	/**
	 * 人事部审核的其他内容；
	 *
	 * @return ：conPersonalRemark 人事部审核的其他内容；
	 */
	public String getConPersonalRemark() {
		return conPersonalRemark;
	}

	/**
	 * 人事部审核的其他内容；
	 *
	 * @param conPersonalRemark：人事部审核的其他内容；
	 */
	public void setConPersonalRemark(String conPersonalRemark) {
		this.conPersonalRemark = conPersonalRemark;
	}

	/**
	 * 离职时间或执行日期；
	 * 
	 * @return ：completeDate 离职时间或执行日期；
	 */
	public Date getCompleteDate() {
		return completeDate;
	}

	/**
	 * 离职时间或执行日期；
	 * 
	 * @param completeDate：离职时间或执行日期；
	 */
	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}

	/**
	 * 违约金；
	 * 
	 * @return ：breakMoney 违约金；
	 */
	public Float getBreakMoney() {
		return breakMoney;
	}

	/**
	 * 违约金；
	 * 
	 * @param breakMoney：违约金；
	 */
	public void setBreakMoney(Float breakMoney) {
		this.breakMoney = breakMoney;
	}

	/**
	 * 财务确认人ID；
	 * 
	 * @return ：conFinanceId 财务确认人ID；
	 */
	public Integer getConFinanceId() {
		return conFinanceId;
	}

	/**
	 * 财务确认人ID；
	 * 
	 * @param conFinanceId：财务确认人ID；
	 */
	public void setConFinanceId(Integer conFinanceId) {
		this.conFinanceId = conFinanceId;
	}

	/**
	 * 财务确认人名称；
	 * 
	 * @return ：conFinanceName 财务确认人名称；
	 */
	public String getConFinanceName() {
		return conFinanceName;
	}

	/**
	 * 财务确认人名称；
	 * 
	 * @param conFinanceName：财务确认人名称；
	 */
	public void setConFinanceName(String conFinanceName) {
		this.conFinanceName = conFinanceName == null ? null : conFinanceName.trim();
	}

	/**
	 * 财务审核；
	 * 
	 * @return ：conFinance 财务审核；
	 */
	public String getConFinance() {
		return conFinance;
	}

	/**
	 * 财务审核；
	 * 
	 * @param conFinance：财务审核；
	 */
	public void setConFinance(String conFinance) {
		this.conFinance = conFinance == null ? null : conFinance.trim();
	}

	/**
	 * 财务审核的其他内容；
	 * 
	 * @return ：conFinanceFemark 财务审核的其他内容；
	 */
	public String getConFinanceRemark() {
		return conFinanceRemark;
	}

	/**
	 * 财务审核的其他内容；
	 * 
	 * @param conFinanceRemark：财务审核的其他内容；
	 */
	public void setConFinanceRemark(String conFinanceRemark) {
		this.conFinanceRemark = conFinanceRemark == null ? null : conFinanceRemark.trim();
	}

	/**
	 * 附件，交接的附件；
	 * 
	 * @return ：conFile 附件，交接的附件；
	 */
	public String getConFile() {
		return conFile;
	}

	/**
	 * 附件，交接的附件；
	 * 
	 * @param conFile：附件，交接的附件；
	 */
	public void setConFile(String conFile) {
		this.conFile = conFile == null ? null : conFile.trim();
	}

	@Override
	public String toString() {
		return "EmployeeConnect{" +
				"conId=" + conId +
				", conType=" + conType +
				", conData=" + conData +
				", deptLeader=" + deptLeader +
				", deptLeaderName='" + deptLeaderName + '\'' +
				", empDate=" + empDate +
				", empState=" + empState +
				", leaveType=" + leaveType +
				", leaveTypeContent=" + leaveTypeContent +
				", conDateType=" + conDateType +
				", conDate=" + conDate +
				", conEmpId=" + conEmpId +
				", conEmpName='" + conEmpName + '\'' +
				", conDeptApprove='" + conDeptApprove + '\'' +
				", conList='" + conList + '\'' +
				", conDeptRemark='" + conDeptRemark + '\'' +
				", conPersonal='" + conPersonal + '\'' +
				", conPersonalId=" + conPersonalId +
				", conPersonalName='" + conPersonalName + '\'' +
				", conPersonalRemark='" + conPersonalRemark + '\'' +
				", completeDate=" + completeDate +
				", breakMoney=" + breakMoney +
				", conFinanceId=" + conFinanceId +
				", conFinanceName='" + conFinanceName + '\'' +
				", conFinance='" + conFinance + '\'' +
				", conFinanceRemark='" + conFinanceRemark + '\'' +
				", conFile='" + conFile + '\'' +
				'}';
	}
}