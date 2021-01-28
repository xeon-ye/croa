package com.qinfei.qferp.entity.administrative;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.qferp.entity.employ.EmployCommon;
import com.qinfei.qferp.entity.employ.EmployEntry;
import com.qinfei.qferp.entity.employ.EmployEntryComment;

import java.util.Date;

public class AdministrativeOnbusinessReport extends EmployCommon {
    private Integer id;

    private Integer administrativeId;//行政流程id

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date reportDate;//报告日期

    private Integer empId;//报告人id

    private String empName;//报告人姓名

    private Integer empDept;//部门id

    private String empDeptName;//部门名称

    private String empDuty;//报告人职位

    private String reportContent;//报告内容

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdministrativeId() {
        return administrativeId;
    }

    public void setAdministrativeId(Integer administrativeId) {
        this.administrativeId = administrativeId;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
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

    public String getEmpDuty() {
        return empDuty;
    }

    public void setEmpDuty(String empDuty) {
        this.empDuty = empDuty == null ? null : empDuty.trim();
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent == null ? null : reportContent.trim();
    }

    public Integer getEmpDept() {
        return empDept;
    }

    public void setEmpDept(Integer empDept) {
        this.empDept = empDept;
    }

    public String getEmpDeptName() {
        return empDeptName;
    }

    public void setEmpDeptName(String empDeptName) {
        this.empDeptName = empDeptName;
    }

    @Override
    public String toString() {
        return "AdministrativeOnbusinessReport{" +
                "id=" + id +
                ", administrativeId=" + administrativeId +
                ", reportDate=" + reportDate +
                ", empId=" + empId +
                ", empName='" + empName + '\'' +
                ", empDept=" + empDept +
                ", empDeptName='" + empDeptName + '\'' +
                ", empDuty='" + empDuty + '\'' +
                ", reportContent='" + reportContent + '\'' +
                '}';
    }
}