package com.qinfei.qferp.entity.administrative;

import com.qinfei.qferp.entity.employ.EmployCommon;

import java.util.Date;

public class AdministrativeSuggest extends EmployCommon {
    /**
     * 主键ID；
     */
    private Integer id;

    /**
     * 录入人ID；
     */
    private Integer empId;

    /**
     * 录入人姓名；
     */
    private String empName;

    /**
     * 录入月份；
     */
    private Integer createMonth;

    /**
     * 录入日期；
     */
    private Integer createDay;

    /**
     * 描述；
     */
    private String description;

    /**
     * 解决方案；
     */
    private String solution;

    /**
     * 处理人id；
     */
    private Integer conductorId;

    /**
     * 处理人姓名；
     */
    private String conductorName;

    private Integer solveId;

    private String solveName;


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
     * 录入人ID；
     * @return ：emp_id 录入人ID；
     */
    public Integer getEmpId() {
        return empId;
    }

    /**
     * 录入人ID；
     * @param empId：录入人ID；
     */
    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    /**
     * 录入人姓名；
     * @return ：emp_name 录入人姓名；
     */
    public String getEmpName() {
        return empName;
    }

    /**
     * 录入人姓名；
     * @param empName：录入人姓名；
     */
    public void setEmpName(String empName) {
        this.empName = empName == null ? null : empName.trim();
    }

    /**
     * 录入月份；
     * @return ：create_month 录入月份；
     */
    public Integer getCreateMonth() {
        return createMonth;
    }

    /**
     * 录入月份；
     * @param createMonth：录入月份；
     */
    public void setCreateMonth(Integer createMonth) {
        this.createMonth = createMonth;
    }

    /**
     * 录入日期；
     * @return ：create_day 录入日期；
     */
    public Integer getCreateDay() {
        return createDay;
    }

    /**
     * 录入日期；
     * @param createDay：录入日期；
     */
    public void setCreateDay(Integer createDay) {
        this.createDay = createDay;
    }

    /**
     * 描述；
     * @return ：description 描述；
     */
    public String getDescription() {
        return description;
    }

    /**
     * 描述；
     * @param description：描述；
     */
    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    /**
     * 解决方案；
     * @return ：solution 解决方案；
     */
    public String getSolution() {
        return solution;
    }

    /**
     * 解决方案；
     * @param solution：解决方案；
     */
    public void setSolution(String solution) {
        this.solution = solution == null ? null : solution.trim();
    }

    /**
     * 处理人id；
     * @return ：conductor_id 处理人id；
     */
    public Integer getConductorId() {
        return conductorId;
    }

    /**
     * 处理人id；
     * @param conductorId：处理人id；
     */
    public void setConductorId(Integer conductorId) {
        this.conductorId = conductorId;
    }

    /**
     * 处理人姓名；
     * @return ：conductor_name 处理人姓名；
     */
    public String getConductorName() {
        return conductorName;
    }

    /**
     * 处理人姓名；
     * @param conductorName：处理人姓名；
     */
    public void setConductorName(String conductorName) {
        this.conductorName = conductorName == null ? null : conductorName.trim();
    }

    @Override
    public String toString() {
        return "AdministrativeSuggest{" +
                "id=" + id +
                ", empId=" + empId +
                ", empName='" + empName + '\'' +
                ", createMonth=" + createMonth +
                ", createDay=" + createDay +
                ", description='" + description + '\'' +
                ", solution='" + solution + '\'' +
                ", conductorId=" + conductorId +
                ", conductorName='" + conductorName + '\'' +
                '}';
    }

    public Integer getSolveId() {
        return solveId;
    }

    public void setSolveId(Integer solveId) {
        this.solveId = solveId;
    }

    public String getSolveName() {
        return solveName;
    }

    public void setSolveName(String solveName) {
        this.solveName = solveName;
    }
}