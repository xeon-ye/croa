package com.qinfei.qferp.entity.administrative;

import com.qinfei.qferp.entity.employ.EmployCommon;

import java.util.Date;

public class AdministrativeVacationTime extends EmployCommon {
    /**
     * 主键ID；
     */
    private Integer id;

    /**
     * 员工ID；
     */
    private Integer empId;

    /**
     * 员工姓名；
     */
    private String empName;

    /**
     * 可休假时间；
     */
    private Double vacationTime;

    /**
     * 增减时间；
     */
    private Double changeTime;

    //请假id
    private Integer leaveId;

    /**
     * 主键ID；
     * @return ：id 主键ID；
     */
    public Integer getId() {
        return id;
    }

    /**
     * 主键ID；
     * @param id：主键ID；
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 员工ID；
     * @return ：emp_id 员工ID；
     */
    public Integer getEmpId() {
        return empId;
    }

    /**
     * 员工ID；
     * @param empId：员工ID；
     */
    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    /**
     * 员工姓名；
     * @return ：emp_name 员工姓名；
     */
    public String getEmpName() {
        return empName;
    }

    /**
     * 员工姓名；
     * @param empName：员工姓名；
     */
    public void setEmpName(String empName) {
        this.empName = empName == null ? null : empName.trim();
    }

    public Integer getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(Integer leaveId) {
        this.leaveId = leaveId;
    }

    public Double getVacationTime() {
        return vacationTime;
    }

    public void setVacationTime(Double vacationTime) {
        this.vacationTime = vacationTime;
    }

    public Double getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(Double changeTime) {
        this.changeTime = changeTime;
    }

    @Override
    public String toString() {
        return "AdministrativeVacationTime{" +
                "id=" + id +
                ", empId=" + empId +
                ", empName='" + empName + '\'' +
                ", vacationTime=" + vacationTime +
                ", changeTime=" + changeTime +
                ", leaveId=" + leaveId +
                '}';
    }
}