package com.qinfei.qferp.entity.administrative;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.qferp.entity.employ.EmployCommon;

import java.util.Date;

public class Administrative extends EmployCommon {
    private Integer id;

    private Integer empId;//员工id

    private String empName;//员工姓名

    private Integer administrativeType;//行政类型

    private String administrativeName;//行政标题名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;//开始时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date endtime;//结束时间

    private Double administrativeTime;//时长

    private Integer approveState;//审核状态

    private Integer approveResult;//审核结果
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;//办结时间

    private Integer itemId;//行政流程id

    private String taskId;//任务流程id

    private String title;//标题

    private Integer leaveDay;//请假天数

    private Integer deptId;//部门id

    private String deptName;//部门名称

    private String approverUserId;//审批人id

    private String companyCode;//公司代号

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName == null ? null : empName.trim();
    }

    public Integer getAdministrativeType() {
        return administrativeType;
    }

    public void setAdministrativeType(Integer administrativeType) {
        this.administrativeType = administrativeType;
    }

    public String getAdministrativeName() {
        return administrativeName;
    }

    public void setAdministrativeName(String administrativeName) {
        this.administrativeName = administrativeName == null ? null : administrativeName.trim();
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public Integer getApproveState() {
        return approveState;
    }

    public void setApproveState(Integer approveState) {
        this.approveState = approveState;
    }

    public Integer getApproveResult() {
        return approveResult;
    }

    public void setApproveResult(Integer approveResult) {
        this.approveResult = approveResult;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public Double getAdministrativeTime() {
        return administrativeTime;
    }

    public void setAdministrativeTime(Double administrativeTime) {
        this.administrativeTime = administrativeTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getApproverUserId() {
        return approverUserId;
    }

    public void setApproverUserId(String approverUserId) {
        this.approverUserId = approverUserId;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }


    public Integer getLeaveDay() {
        return leaveDay;
    }

    public void setLeaveDay(Integer leaveDay) {
        this.leaveDay = leaveDay;
    }

    @Override
    public String toString() {
        return "Administrative{" +
                "id=" + id +
                ", empId=" + empId +
                ", empName='" + empName + '\'' +
                ", administrativeType=" + administrativeType +
                ", administrativeName='" + administrativeName + '\'' +
                ", beginTime=" + beginTime +
                ", endtime=" + endtime +
                ", administrativeTime=" + administrativeTime +
                ", approveState=" + approveState +
                ", approveResult=" + approveResult +
                ", finishTime=" + finishTime +
                ", itemId=" + itemId +
                ", taskId='" + taskId + '\'' +
                ", title='" + title + '\'' +
                ", leaveDay=" + leaveDay +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", approverUserId='" + approverUserId + '\'' +
                ", companyCode='" + companyCode + '\'' +
                '}';
    }
}