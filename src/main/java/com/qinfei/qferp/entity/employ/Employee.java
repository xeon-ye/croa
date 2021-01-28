package com.qinfei.qferp.entity.employ;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工花名册实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class Employee extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -2382837055993960724L;

	/**
	 * 主键ID；
	 */
	private Integer empId;

	/**
	 * 员工工号；
	 */
	private String empNum;

	/**
	 * 姓名；
	 */
	private String empName;

	/**
	 * 入职部门；
	 */
	private Integer empDept;

	/**
	 * 入职职位；
	 */
	private Integer empProfession;

	/**
	 * 联系电话；
	 */
	private String empPhone;

	/**
	 * 婚姻状态，0为未婚，1为已婚，2为离婚，3为丧偶；
	 */
	private Integer empMarriage;

	/**
	 * 紧急联系人；
	 */
	private String empUrgent;

	/**
	 * 紧急联系人电话；
	 */
	private String empUrgentPhone;

	/**
	 * 工作区域；
	 */
	private String empWorkLocation;

	/**
	 * 年龄；
	 */
	private Integer empAge;

	private Float empWorkYear; //员工司龄

	/**
	 * 居住情况，0为住家，1为公司宿舍，2为租房；
	 */
	private Integer empHouse;

	/**
	 * 家庭情况；
	 */
	private String empFamily;

	/**
	 * 合同签订单位；
	 */
	private String empContractUnit;

	/**
	 * 合同签订日期；
	 */
	private Date empContractDate;

	private Date empTwoContractDate; //第二次合同签订日期

	private Date empThreeContractDate; //第三次合同签订日期

	/**
	 * 社保号码；
	 */
	private String empSecurityCode;

	/**
	 * 社保公司；
	 */
	private String empSecurityCompany;

	/**
	 * 社保协议；
	 */
	private String empSecurityProtocol;

	/**
	 * 是否已签署承诺书，0为是，1为否；
	 */
	private Integer empCompliance;

	/**
	 * 备注；
	 */
	private String empRemark;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 关联的系统账号ID；
	 */
	private Integer userId;

	/**
	 * 关联的系统账号名称；
	 */
	private String userName;

	/**
	 * 流程的任务ID；
	 */
	private String taskId;

	/**
	 * 待办事项ID；
	 */
	private Integer itemId;

	/**
	 * 当前进行的流程标识，详细定义请参考com.qinfei.qferp.utils.IProcess；
	 */
	private Integer processId;

	/**
	 * 流程审核状态；
	 */
	private Integer processState;

	/**
	 * 权限访问码；
	 */
	private String validCode;

	/**
	 * 状态，0为试用，1为转正，2为离职，3为交接中，-1为已删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：empId 主键ID；
	 */
	public Integer getEmpId() {
		return empId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param empId：主键ID；
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
	 * 姓名；
	 * 
	 * @return ：empName 姓名；
	 */
	public String getEmpName() {
		return empName;
	}

	/**
	 * 姓名；
	 * 
	 * @param empName：姓名；
	 */
	public void setEmpName(String empName) {
		this.empName = empName == null ? null : empName.trim();
	}

	/**
	 * 入职部门；
	 * 
	 * @return ：empDept 入职部门；
	 */
	public Integer getEmpDept() {
		return empDept;
	}

	/**
	 * 入职部门；
	 * 
	 * @param empDept：入职部门；
	 */
	public void setEmpDept(Integer empDept) {
		this.empDept = empDept;
	}

	/**
	 * 入职职位；
	 * 
	 * @return ：empProfession 入职职位；
	 */
	public Integer getEmpProfession() {
		return empProfession;
	}

	/**
	 * 入职职位；
	 * 
	 * @param empProfession：入职职位；
	 */
	public void setEmpProfession(Integer empProfession) {
		this.empProfession = empProfession;
	}

	/**
	 * 联系电话；
	 * 
	 * @return ：empPhone 联系电话；
	 */
	public String getEmpPhone() {
		return empPhone;
	}

	/**
	 * 联系电话；
	 * 
	 * @param empPhone：联系电话；
	 */
	public void setEmpPhone(String empPhone) {
		this.empPhone = empPhone == null ? null : empPhone.trim();
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
	 * 紧急联系人；
	 * 
	 * @return ：empUrgent 紧急联系人；
	 */
	public String getEmpUrgent() {
		return empUrgent;
	}

	/**
	 * 紧急联系人；
	 * 
	 * @param empUrgent：紧急联系人；
	 */
	public void setEmpUrgent(String empUrgent) {
		this.empUrgent = empUrgent == null ? null : empUrgent.trim();
	}

	/**
	 * 紧急联系人电话；
	 * 
	 * @return ：empUrgentPhone 紧急联系人电话；
	 */
	public String getEmpUrgentPhone() {
		return empUrgentPhone;
	}

	/**
	 * 紧急联系人电话；
	 * 
	 * @param empUrgentPhone：紧急联系人电话；
	 */
	public void setEmpUrgentPhone(String empUrgentPhone) {
		this.empUrgentPhone = empUrgentPhone == null ? null : empUrgentPhone.trim();
	}

	/**
	 * 工作区域；
	 * 
	 * @return ：empWorkLocation 工作区域；
	 */
	public String getEmpWorkLocation() {
		return empWorkLocation;
	}

	/**
	 * 工作区域；
	 * 
	 * @param empWorkLocation：工作区域；
	 */
	public void setEmpWorkLocation(String empWorkLocation) {
		this.empWorkLocation = empWorkLocation == null ? null : empWorkLocation.trim();
	}

	/**
	 * 年龄；
	 * 
	 * @return ：empAge 年龄；
	 */
	public Integer getEmpAge() {
		return empAge;
	}

	/**
	 * 年龄；
	 * 
	 * @param empAge：年龄；
	 */
	public void setEmpAge(Integer empAge) {
		this.empAge = empAge;
	}

	/**
	 * 居住情况，0为住家，1为公司宿舍，2为租房；
	 * 
	 * @return ：empHouse 居住情况，0为住家，1为公司宿舍，2为租房；
	 */
	public Integer getEmpHouse() {
		return empHouse;
	}

	/**
	 * 居住情况，0为住家，1为公司宿舍，2为租房；
	 *
	 * @param empHouse：居住情况，0为住家，1为公司宿舍，2为租房；
	 */
	public void setEmpHouse(Integer empHouse) {
		this.empHouse = empHouse;
	}

	/**
	 * 家庭情况；
	 * 
	 * @return ：empFamily 家庭情况；
	 */
	public String getEmpFamily() {
		return empFamily;
	}

	/**
	 * 家庭情况；
	 * 
	 * @param empFamily：家庭情况；
	 */
	public void setEmpFamily(String empFamily) {
		this.empFamily = empFamily == null ? null : empFamily.trim();
	}

	/**
	 * 合同签订单位；
	 * 
	 * @return ：empContractUnit 合同签订单位；
	 */
	public String getEmpContractUnit() {
		return empContractUnit;
	}

	/**
	 * 合同签订单位；
	 * 
	 * @param empContractUnit：合同签订单位；
	 */
	public void setEmpContractUnit(String empContractUnit) {
		this.empContractUnit = empContractUnit == null ? null : empContractUnit.trim();
	}

	/**
	 * 合同签订日期；
	 * 
	 * @return ：empContractDate 合同签订日期；
	 */
	public Date getEmpContractDate() {
		return empContractDate;
	}

	/**
	 * 合同签订日期；
	 * 
	 * @param empContractDate：合同签订日期；
	 */
	public void setEmpContractDate(Date empContractDate) {
		this.empContractDate = empContractDate;
	}

	/**
	 * 社保号码；
	 * 
	 * @return ：empSecurityCode 社保号码；
	 */
	public String getEmpSecurityCode() {
		return empSecurityCode;
	}

	/**
	 * 社保号码；
	 * 
	 * @param empSecurityCode：社保号码；
	 */
	public void setEmpSecurityCode(String empSecurityCode) {
		this.empSecurityCode = empSecurityCode == null ? null : empSecurityCode.trim();
	}

	/**
	 * 社保公司；
	 * 
	 * @return ：empSecurityCompany 社保公司；
	 */
	public String getEmpSecurityCompany() {
		return empSecurityCompany;
	}

	/**
	 * 社保公司；
	 * 
	 * @param empSecurityCompany：社保公司；
	 */
	public void setEmpSecurityCompany(String empSecurityCompany) {
		this.empSecurityCompany = empSecurityCompany == null ? null : empSecurityCompany.trim();
	}

	/**
	 * 社保协议；
	 * 
	 * @return ：empSecurityProtocol 社保协议；
	 */
	public String getEmpSecurityProtocol() {
		return empSecurityProtocol;
	}

	/**
	 * 社保协议；
	 * 
	 * @param empSecurityProtocol：社保协议；
	 */
	public void setEmpSecurityProtocol(String empSecurityProtocol) {
		this.empSecurityProtocol = empSecurityProtocol == null ? null : empSecurityProtocol.trim();
	}

	/**
	 * 是否已签署承诺书，0为是，1为否；
	 * 
	 * @return ：empCompliance 是否已签署承诺书，0为是，1为否；
	 */
	public Integer getEmpCompliance() {
		return empCompliance;
	}

	/**
	 * 是否已签署承诺书，0为是，1为否；
	 * 
	 * @param empCompliance：是否已签署承诺书，0为是，1为否；
	 */
	public void setEmpCompliance(Integer empCompliance) {
		this.empCompliance = empCompliance;
	}

	/**
	 * 备注；
	 * 
	 * @return ：empRemark 备注；
	 */
	public String getEmpRemark() {
		return empRemark;
	}

	/**
	 * 备注；
	 * 
	 * @param empRemark：备注；
	 */
	public void setEmpRemark(String empRemark) {
		this.empRemark = empRemark == null ? null : empRemark.trim();
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
	 * 关联的系统账号ID；
	 *
	 * @return ：userId 关联的系统账号ID；
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * 关联的系统账号ID；
	 *
	 * @param userId：关联的系统账号ID；
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * 关联的系统账号名称；
	 *
	 * @return ：userName 关联的系统账号名称；
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 关联的系统账号名称；
	 *
	 * @param userName：关联的系统账号名称；
	 */
	public void setUserName(String userName) {
		this.userName = userName;
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
	 * 当前进行的流程标识，详细定义请参考com.qinfei.qferp.utils.IProcess；
	 *
	 * @return ：processId 当前进行的流程标识，详细定义请参考com.qinfei.qferp.utils.IProcess；
	 */
	public Integer getProcessId() {
		return processId;
	}

	/**
	 * 当前进行的流程标识，详细定义请参考com.qinfei.qferp.utils.IProcess；
	 *
	 * @param processId：当前进行的流程标识，详细定义请参考com.qinfei.qferp.utils.IProcess；
	 */
	public void setProcessId(Integer processId) {
		this.processId = processId;
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

	/**
	 * 权限访问码；
	 *
	 * @return ：validCode 权限访问码；
	 */
	public String getValidCode() {
		return validCode;
	}

	/**
	 * 权限访问码；
	 *
	 * @param validCode：权限访问码；
	 */
	public void setValidCode(String validCode) {
		this.validCode = validCode;
	}

	public Float getEmpWorkYear() {
		return empWorkYear;
	}

	public void setEmpWorkYear(Float empWorkYear) {
		this.empWorkYear = empWorkYear;
	}

	public Date getEmpTwoContractDate() {
		return empTwoContractDate;
	}

	public void setEmpTwoContractDate(Date empTwoContractDate) {
		this.empTwoContractDate = empTwoContractDate;
	}

	public Date getEmpThreeContractDate() {
		return empThreeContractDate;
	}

	public void setEmpThreeContractDate(Date empThreeContractDate) {
		this.empThreeContractDate = empThreeContractDate;
	}

	@Override
	public String toString() {
		return "Employee{" +
				"empId=" + empId +
				", empNum='" + empNum + '\'' +
				", empName='" + empName + '\'' +
				", empDept=" + empDept +
				", empProfession=" + empProfession +
				", empPhone='" + empPhone + '\'' +
				", empMarriage=" + empMarriage +
				", empUrgent='" + empUrgent + '\'' +
				", empUrgentPhone='" + empUrgentPhone + '\'' +
				", empWorkLocation='" + empWorkLocation + '\'' +
				", empAge=" + empAge +
				", empWorkYear=" + empWorkYear +
				", empHouse=" + empHouse +
				", empFamily='" + empFamily + '\'' +
				", empContractUnit='" + empContractUnit + '\'' +
				", empContractDate=" + empContractDate +
				", empTwoContractDate=" + empTwoContractDate +
				", empThreeContractDate=" + empThreeContractDate +
				", empSecurityCode='" + empSecurityCode + '\'' +
				", empSecurityCompany='" + empSecurityCompany + '\'' +
				", empSecurityProtocol='" + empSecurityProtocol + '\'' +
				", empCompliance=" + empCompliance +
				", empRemark='" + empRemark + '\'' +
				", entryId=" + entryId +
				", userId=" + userId +
				", userName='" + userName + '\'' +
				", taskId='" + taskId + '\'' +
				", itemId=" + itemId +
				", processId=" + processId +
				", processState=" + processState +
				", validCode='" + validCode + '\'' +
				'}';
	}
}