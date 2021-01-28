package com.qinfei.qferp.entity.administrative;

import com.qinfei.qferp.entity.employ.EmployCommon;


public class AdministrativeLeaveType extends EmployCommon {
    /**
     * 主键ID；
     */
    private Integer id;

    /**
     * 员工ID；
     */
    private Integer typeId;

    /**
     * 员工姓名；
     */
    private String typeName;



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
     * @return ：type_id 员工ID；
     */
    public Integer getTypeId() {
        return typeId;
    }

    /**
     * 员工ID；
     * @param typeId：员工ID；
     */
    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    /**
     * 员工姓名；
     * @return ：type_name 员工姓名；
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * 员工姓名；
     * @param typeName：员工姓名；
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName == null ? null : typeName.trim();
    }

    @Override
    public String toString() {
        return "AdministrativeLeaveType{" +
                "id=" + id +
                ", typeId=" + typeId +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}