package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 入职申请信息实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployEntry extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -2340232345520355997L;

	/**
	 * 主键ID；
	 */
	private Integer entryId;

	private String entryCompanyCode; //申请公司代码

	private Integer entryFirstDept; //一级部门：0-业务部门、1-媒介部门、2-其他部门

	private Integer entryState; //录用状态：0-试用、1-实习

	/**
	 * 申请的部门；
	 */
	private Integer entryDept;

	/**
	 * 申请的职位；
	 */
	private Integer entryProfession;

	/**
	 * 期望薪资待遇；
	 */
	private String entryExpectSalary;

	/**
	 * 申请人姓名；
	 */
	private String entryName;

	/**
	 * 申请人照片；
	 */
	private String entryImage;

	/**
	 * 身份证附件；
	 */
	private String entryCodeFile;

	/**
	 * 户口类型，0为城镇户口，1为农村户口；
	 */
	private Integer entryResidence;

	/**
	 * 户口附件；
	 */
	private String entryResidenceFile;

	/**
	 * 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 */
	private Integer entryMarriage;

	/**
	 * 现住址；
	 */
	private String entryLocalAddress;

	/**
	 * 邮箱；
	 */
	private String entryMail;

	/**
	 * 联系电话；
	 */
	private String entryPhone;

	/**
	 * 紧急联系人；
	 */
	private String entryUrgent;

	/**
	 * 紧急联系人电话；
	 */
	private String entryUrgentPhone;

	/**
	 * 与紧急联系人关系；
	 */
	private String entryUrgentRelation;

	/**
	 * 是否有驾照，0为是，1为否；
	 */
	private Integer entryHasLicence;

	/**
	 * 驾照；
	 */
	private String entryLicence;

	/**
	 * 驾龄；
	 */
	private Integer entryDriveAge;

	/**
	 * 兴趣爱好特长；
	 */
	private String entryInterest;

	/**
	 * 是否曾经有病史，0为否，1为是；
	 */
	private Integer entryHasSick;

	/**
	 * 病史描述；
	 */
	private String entrySick;

	/**
	 * 体检报告文件；
	 */
	private String entryReport;

	/**
	 * 求职途径:0-BOSS/BOSS直聘，1-社交媒体，2-离职再入职，3-人才市场，4-校园招聘，5-猎头推荐，6-内部推荐 ，7-其他，8-前程/前程无忧，9-智联/智联招聘、10-分子公司调岗
	 */
	private Integer entryChannel;

	/**
	 * 求职途径名称；
	 */
	private String entryChannelName;

	/**
	 * 是否有亲友在公司，0为是，1为否；
	 */
	private Integer entryHasRelative;

	/**
	 * 入职申请表的扫描件；
	 */
	private String entryFile;

	/**
	 * 查询码；
	 */
	private String entryValidate;

	/**
	 * 资料完整，0为否，1为是；
	 */
	private Integer entryComplete;

	/**
	 * 拟到岗日期；
	 */
	private Date entryExpectDate;

	/**
	 * 员工ID；
	 */
	private Integer empId;

	/**
	 * 流程的任务ID；
	 */
	private String taskId;

	/**
	 * 待办事项ID；
	 */
	private Integer itemId;

	/**
	 * 流程审核状态；
	 */
	private Integer processState;

	/**
	 * 状态，0为待审核，1为审核中，2为同意录用，3为不予考虑（存档备用），4为已入职，5为已离职，-1为已删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：entryId 主键ID；
	 */
	public Integer getEntryId() {
		return entryId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param entryId：主键ID；
	 */
	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	/**
	 * 申请的部门；
	 * 
	 * @return ：entryDept 申请的部门；
	 */
	public Integer getEntryDept() {
		return entryDept;
	}

	/**
	 * 申请的部门；
	 * 
	 * @param entryDept：申请的部门；
	 */
	public void setEntryDept(Integer entryDept) {
		this.entryDept = entryDept;
	}

	/**
	 * 申请的职位；
	 * 
	 * @return ：entryProfession 申请的职位；
	 */
	public Integer getEntryProfession() {
		return entryProfession;
	}

	/**
	 * 申请的职位；
	 * 
	 * @param entryProfession：申请的职位；
	 */
	public void setEntryProfession(Integer entryProfession) {
		this.entryProfession = entryProfession;
	}

	/**
	 * 期望薪资待遇；
	 * 
	 * @return ：entryExpectSalary 期望薪资待遇；
	 */
	public String getEntryExpectSalary() {
		return entryExpectSalary;
	}

	/**
	 * 期望薪资待遇；
	 * 
	 * @param entryExpectSalary：期望薪资待遇；
	 */
	public void setEntryExpectSalary(String entryExpectSalary) {
		this.entryExpectSalary = entryExpectSalary == null ? null : entryExpectSalary.trim();
	}

	/**
	 * 申请人姓名；
	 * 
	 * @return ：entryName 申请人姓名；
	 */
	public String getEntryName() {
		return entryName;
	}

	/**
	 * 申请人姓名；
	 * 
	 * @param entryName：申请人姓名；
	 */
	public void setEntryName(String entryName) {
		this.entryName = entryName == null ? null : entryName.trim();
	}

	/**
	 * 申请人照片；
	 * 
	 * @return ：entryImage 申请人照片；
	 */
	public String getEntryImage() {
		return entryImage;
	}

	/**
	 * 申请人照片；
	 * 
	 * @param entryImage：申请人照片；
	 */
	public void setEntryImage(String entryImage) {
		this.entryImage = entryImage == null ? null : entryImage.trim();
	}

	/**
	 * 身份证附件；
	 * 
	 * @return ：entryCodeFile 身份证附件；
	 */
	public String getEntryCodeFile() {
		return entryCodeFile;
	}

	/**
	 * 身份证附件；
	 * 
	 * @param entryCodeFile：身份证附件；
	 */
	public void setEntryCodeFile(String entryCodeFile) {
		this.entryCodeFile = entryCodeFile == null ? null : entryCodeFile.trim();
	}

	/**
	 * 户口类型，0为城镇户口，1为农村户口；
	 * 
	 * @return ：entryResidence 户口类型，0为城镇户口，1为农村户口；
	 */
	public Integer getEntryResidence() {
		return entryResidence;
	}

	/**
	 * 户口类型，0为城镇户口，1为农村户口；
	 * 
	 * @param entryResidence：户口类型，0为城镇户口，1为农村户口；
	 */
	public void setEntryResidence(Integer entryResidence) {
		this.entryResidence = entryResidence;
	}

	/**
	 * 户口附件；
	 * 
	 * @return ：entryResidenceFile 户口附件；
	 */
	public String getEntryResidenceFile() {
		return entryResidenceFile;
	}

	/**
	 * 户口附件；
	 * 
	 * @param entryResidenceFile：户口附件；
	 */
	public void setEntryResidenceFile(String entryResidenceFile) {
		this.entryResidenceFile = entryResidenceFile == null ? null : entryResidenceFile.trim();
	}

	/**
	 * 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 * 
	 * @return ：entryMarriage 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 */
	public Integer getEntryMarriage() {
		return entryMarriage;
	}

	/**
	 * 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 * 
	 * @param entryMarriage：婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 */
	public void setEntryMarriage(Integer entryMarriage) {
		this.entryMarriage = entryMarriage;
	}

	/**
	 * 现住址；
	 * 
	 * @return ：entryLocalAddress 现住址；
	 */
	public String getEntryLocalAddress() {
		return entryLocalAddress;
	}

	/**
	 * 现住址；
	 * 
	 * @param entryLocalAddress：现住址；
	 */
	public void setEntryLocalAddress(String entryLocalAddress) {
		this.entryLocalAddress = entryLocalAddress == null ? null : entryLocalAddress.trim();
	}

	/**
	 * 邮箱；
	 * 
	 * @return ：entryMail 邮箱；
	 */
	public String getEntryMail() {
		return entryMail;
	}

	/**
	 * 邮箱；
	 * 
	 * @param entryMail：邮箱；
	 */
	public void setEntryMail(String entryMail) {
		this.entryMail = entryMail == null ? null : entryMail.trim();
	}

	/**
	 * 联系电话；
	 * 
	 * @return ：entryPhone 联系电话；
	 */
	public String getEntryPhone() {
		return entryPhone;
	}

	/**
	 * 联系电话；
	 * 
	 * @param entryPhone：联系电话；
	 */
	public void setEntryPhone(String entryPhone) {
		this.entryPhone = entryPhone == null ? null : entryPhone.trim();
	}

	/**
	 * 紧急联系人；
	 * 
	 * @return ：entryUrgent 紧急联系人；
	 */
	public String getEntryUrgent() {
		return entryUrgent;
	}

	/**
	 * 紧急联系人；
	 * 
	 * @param entryUrgent：紧急联系人；
	 */
	public void setEntryUrgent(String entryUrgent) {
		this.entryUrgent = entryUrgent == null ? null : entryUrgent.trim();
	}

	/**
	 * 紧急联系人电话；
	 * 
	 * @return ：entryUrgentPhone 紧急联系人电话；
	 */
	public String getEntryUrgentPhone() {
		return entryUrgentPhone;
	}

	/**
	 * 紧急联系人电话；
	 * 
	 * @param entryUrgentPhone：紧急联系人电话；
	 */
	public void setEntryUrgentPhone(String entryUrgentPhone) {
		this.entryUrgentPhone = entryUrgentPhone == null ? null : entryUrgentPhone.trim();
	}

	/**
	 * 与紧急联系人关系；
	 * 
	 * @return ：entryUrgentRelation 与紧急联系人关系；
	 */
	public String getEntryUrgentRelation() {
		return entryUrgentRelation;
	}

	/**
	 * 与紧急联系人关系；
	 * 
	 * @param entryUrgentRelation：与紧急联系人关系；
	 */
	public void setEntryUrgentRelation(String entryUrgentRelation) {
		this.entryUrgentRelation = entryUrgentRelation == null ? null : entryUrgentRelation.trim();
	}

	/**
	 * 是否有驾照，0为是，1为否；
	 * 
	 * @return ：entryHasLicence 是否有驾照，0为是，1为否；
	 */
	public Integer getEntryHasLicence() {
		return entryHasLicence;
	}

	/**
	 * 是否有驾照，0为是，1为否；
	 * 
	 * @param entryHasLicence：是否有驾照，0为是，1为否；
	 */
	public void setEntryHasLicence(Integer entryHasLicence) {
		this.entryHasLicence = entryHasLicence;
	}

	/**
	 * 驾照；
	 * 
	 * @return ：entryLicence 驾照；
	 */
	public String getEntryLicence() {
		return entryLicence;
	}

	/**
	 * 驾照；
	 * 
	 * @param entryLicence：驾照；
	 */
	public void setEntryLicence(String entryLicence) {
		this.entryLicence = entryLicence == null ? null : entryLicence.trim();
	}

	/**
	 * 驾龄；
	 * 
	 * @return ：entryDriveAge 驾龄；
	 */
	public Integer getEntryDriveAge() {
		return entryDriveAge;
	}

	/**
	 * 驾龄；
	 * 
	 * @param entryDriveAge：驾龄；
	 */
	public void setEntryDriveAge(Integer entryDriveAge) {
		this.entryDriveAge = entryDriveAge;
	}

	/**
	 * 兴趣爱好特长；
	 * 
	 * @return ：entryInterest 兴趣爱好特长；
	 */
	public String getEntryInterest() {
		return entryInterest;
	}

	/**
	 * 兴趣爱好特长；
	 * 
	 * @param entryInterest：兴趣爱好特长；
	 */
	public void setEntryInterest(String entryInterest) {
		this.entryInterest = entryInterest == null ? null : entryInterest.trim();
	}

	/**
	 * 是否曾经有病史，0为否，1为是；
	 * 
	 * @return ：entryHasSick 是否曾经有病史，0为否，1为是；
	 */
	public Integer getEntryHasSick() {
		return entryHasSick;
	}

	/**
	 * 是否曾经有病史，0为否，1为是；
	 * 
	 * @param entryHasSick：是否曾经有病史，0为否，1为是；
	 */
	public void setEntryHasSick(Integer entryHasSick) {
		this.entryHasSick = entryHasSick;
	}

	/**
	 * 病史描述；
	 * 
	 * @return ：entrySick 病史描述；
	 */
	public String getEntrySick() {
		return entrySick;
	}

	/**
	 * 病史描述；
	 * 
	 * @param entrySick：病史描述；
	 */
	public void setEntrySick(String entrySick) {
		this.entrySick = entrySick == null ? null : entrySick.trim();
	}

	/**
	 * 体检报告文件；
	 * 
	 * @return ：entryReport 体检报告文件；
	 */
	public String getEntryReport() {
		return entryReport;
	}

	/**
	 * 体检报告文件；
	 * 
	 * @param entryReport：体检报告文件；
	 */
	public void setEntryReport(String entryReport) {
		this.entryReport = entryReport == null ? null : entryReport.trim();
	}

	/**
	 * 求职途径:0-BOSS/BOSS直聘，1-社交媒体，2-离职再入职，3-人才市场，4-校园招聘，5-猎头推荐，6-内部推荐 ，7-其他，8-前程/前程无忧，9-智联/智联招聘、10-分子公司调岗
	*/
	public Integer getEntryChannel() {
		return entryChannel;
	}

	/**
	 * 求职途径:0-BOSS/BOSS直聘，1-社交媒体，2-离职再入职，3-人才市场，4-校园招聘，5-猎头推荐，6-内部推荐 ，7-其他，8-前程/前程无忧，9-智联/智联招聘、10-分子公司调岗
	*/
	public void setEntryChannel(Integer entryChannel) {
		this.entryChannel = entryChannel;
	}

	/**
	 * 求职途径名称；
	 * 
	 * @return ：entryChannelName 求职途径名称；
	 */
	public String getEntryChannelName() {
		return entryChannelName;
	}

	/**
	 * 求职途径名称；
	 * 
	 * @param entryChannelName：求职途径名称；
	 */
	public void setEntryChannelName(String entryChannelName) {
		this.entryChannelName = entryChannelName == null ? null : entryChannelName.trim();
	}

	/**
	 * 是否有亲友在公司，0为是，1为否；
	 * 
	 * @return ：entryHasRelative 是否有亲友在公司，0为是，1为否；
	 */
	public Integer getEntryHasRelative() {
		return entryHasRelative;
	}

	/**
	 * 是否有亲友在公司，0为是，1为否；
	 * 
	 * @param entryHasRelative：是否有亲友在公司，0为是，1为否；
	 */
	public void setEntryHasRelative(Integer entryHasRelative) {
		this.entryHasRelative = entryHasRelative;
	}

	/**
	 * 入职申请表的扫描件；
	 * 
	 * @return ：entryFile 入职申请表的扫描件；
	 */
	public String getEntryFile() {
		return entryFile;
	}

	/**
	 * 入职申请表的扫描件；
	 * 
	 * @param entryFile：入职申请表的扫描件；
	 */
	public void setEntryFile(String entryFile) {
		this.entryFile = entryFile == null ? null : entryFile.trim();
	}

	/**
	 * 查询码；
	 * 
	 * @return ：entryValidate 查询码；
	 */
	public String getEntryValidate() {
		return entryValidate;
	}

	/**
	 * 查询码；
	 * 
	 * @param entryValidate：查询码；
	 */
	public void setEntryValidate(String entryValidate) {
		this.entryValidate = entryValidate == null ? null : entryValidate.trim();
	}

	/**
	 * 资料完整，0为否，1为是；
	 * 
	 * @return ：entryComplete 资料完整，0为否，1为是；
	 */
	public Integer getEntryComplete() {
		return entryComplete;
	}

	/**
	 * 资料完整，0为否，1为是；
	 * 
	 * @param entryComplete：资料完整，0为否，1为是；
	 */
	public void setEntryComplete(Integer entryComplete) {
		this.entryComplete = entryComplete;
	}

	/**
	 * 拟到岗日期；
	 * 
	 * @return ：entryExpectDate 拟到岗日期；
	 */
	public Date getEntryExpectDate() {
		return entryExpectDate;
	}

	/**
	 * 拟到岗日期；
	 * 
	 * @param entryExpectDate：拟到岗日期；
	 */
	public void setEntryExpectDate(Date entryExpectDate) {
		this.entryExpectDate = entryExpectDate;
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
	 * 流程的任务ID；
	 *
	 * @return ：taskId 流程的任务ID；
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * 流程的任务ID；
	 *
	 * @param taskId：流程的任务ID；
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * 待办事项ID；
	 *
	 * @return ：itemId 待办事项ID；
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * 待办事项ID；
	 *
	 * @param itemId：待办事项ID；
	 */
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	/**
	 * 流程审核状态；
	 *
	 * @return ：processState 流程审核状态；
	 */
	public Integer getProcessState() {
		return processState;
	}

	/**
	 * 流程审核状态；
	 *
	 * @param processState：流程审核状态；
	 */
	public void setProcessState(Integer processState) {
		this.processState = processState;
	}

	public String getEntryCompanyCode() {
		return entryCompanyCode;
	}

	public void setEntryCompanyCode(String entryCompanyCode) {
		this.entryCompanyCode = entryCompanyCode;
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
		return "EmployEntry{" +
				"entryId=" + entryId +
				", entryCompanyCode='" + entryCompanyCode + '\'' +
				", entryFirstDept=" + entryFirstDept +
				", entryState=" + entryState +
				", entryDept=" + entryDept +
				", entryProfession=" + entryProfession +
				", entryExpectSalary='" + entryExpectSalary + '\'' +
				", entryName='" + entryName + '\'' +
				", entryImage='" + entryImage + '\'' +
				", entryCodeFile='" + entryCodeFile + '\'' +
				", entryResidence=" + entryResidence +
				", entryResidenceFile='" + entryResidenceFile + '\'' +
				", entryMarriage=" + entryMarriage +
				", entryLocalAddress='" + entryLocalAddress + '\'' +
				", entryMail='" + entryMail + '\'' +
				", entryPhone='" + entryPhone + '\'' +
				", entryUrgent='" + entryUrgent + '\'' +
				", entryUrgentPhone='" + entryUrgentPhone + '\'' +
				", entryUrgentRelation='" + entryUrgentRelation + '\'' +
				", entryHasLicence=" + entryHasLicence +
				", entryLicence='" + entryLicence + '\'' +
				", entryDriveAge=" + entryDriveAge +
				", entryInterest='" + entryInterest + '\'' +
				", entryHasSick=" + entryHasSick +
				", entrySick='" + entrySick + '\'' +
				", entryReport='" + entryReport + '\'' +
				", entryChannel=" + entryChannel +
				", entryChannelName='" + entryChannelName + '\'' +
				", entryHasRelative=" + entryHasRelative +
				", entryFile='" + entryFile + '\'' +
				", entryValidate='" + entryValidate + '\'' +
				", entryComplete=" + entryComplete +
				", entryExpectDate=" + entryExpectDate +
				", empId=" + empId +
				", taskId='" + taskId + '\'' +
				", itemId=" + itemId +
				", processState=" + processState +
				'}';
	}
}