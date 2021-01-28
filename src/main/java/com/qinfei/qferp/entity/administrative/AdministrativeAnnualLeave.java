package com.qinfei.qferp.entity.administrative;

import com.qinfei.qferp.entity.employ.EmployCommon;

import java.util.Date;

public class AdministrativeAnnualLeave extends EmployCommon {
    private Integer id;

    private Integer empId;

    private String empName;

    private Integer dempId;

    private String dempName;

    private Integer typeId;

    private String typeName;

    private Integer time;

    private Integer surplusTime;


    private Integer state;

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

    public Integer getDempId() {
        return dempId;
    }

    public void setDempId(Integer dempId) {
        this.dempId = dempId;
    }

    public String getDempName() {
        return dempName;
    }

    public void setDempName(String dempName) {
        this.dempName = dempName == null ? null : dempName.trim();
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName == null ? null : typeName.trim();
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getSurplusTime() {
        return surplusTime;
    }

    public void setSurplusTime(Integer surplusTime) {
        this.surplusTime = surplusTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "AdministrativeAnnualLeave{" +
                "id=" + id +
                ", empId=" + empId +
                ", empName='" + empName + '\'' +
                ", dempId=" + dempId +
                ", dempName='" + dempName + '\'' +
                ", typeId=" + typeId +
                ", typeName='" + typeName + '\'' +
                ", time=" + time +
                ", surplusTime=" + surplusTime +
                ", state=" + state +
                '}';
    }
}