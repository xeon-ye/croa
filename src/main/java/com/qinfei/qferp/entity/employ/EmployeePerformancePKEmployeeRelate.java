package com.qinfei.qferp.entity.employ;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;

import java.io.Serializable;

/**
 * Created by yanhonghao on 2019/4/23 17:36.
 */
@Table(name = "e_employee_performance_pk_employee_relate")
public class EmployeePerformancePKEmployeeRelate implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id primary key
     */
    @Id
    private Integer id;

    /**
     * pk模板id
     */
    private Integer eEmployeePerformancePkId;

    /**
     * 左侧员工id
     */
    private Integer leftEmployeeId;

    /**
     * 右侧员工id
     */
    private Integer rightEmployeeId;

    private String leftEmployeeName;
    private String rightEmployeeName;

    public String getLeftEmployeeName() {
        return leftEmployeeName;
    }

    public void setLeftEmployeeName(String leftEmployeeName) {
        this.leftEmployeeName = leftEmployeeName;
    }

    public String getRightEmployeeName() {
        return rightEmployeeName;
    }

    public void setRightEmployeeName(String rightEmployeeName) {
        this.rightEmployeeName = rightEmployeeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEEmployeePerformancePkId() {
        return eEmployeePerformancePkId;
    }

    public void setEEmployeePerformancePkId(Integer eEmployeePerformancePkId) {
        this.eEmployeePerformancePkId = eEmployeePerformancePkId;
    }

    public Integer getLeftEmployeeId() {
        return leftEmployeeId;
    }

    public void setLeftEmployeeId(Integer leftEmployeeId) {
        this.leftEmployeeId = leftEmployeeId;
    }

    public Integer getRightEmployeeId() {
        return rightEmployeeId;
    }

    public void setRightEmployeeId(Integer rightEmployeeId) {
        this.rightEmployeeId = rightEmployeeId;
    }

    @Override
    public String toString() {
        return "EmployeePerformancePKEmployeeRelate{" +
                "id=" + id +
                ", eEmployeePerformancePkId=" + eEmployeePerformancePkId +
                ", leftEmployeeId=" + leftEmployeeId +
                ", rightEmployeeId=" + rightEmployeeId +
                ", leftEmployeeName='" + leftEmployeeName + '\'' +
                ", rightEmployeeName='" + rightEmployeeName + '\'' +
                '}';
    }
}
