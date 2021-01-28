package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeLeaveType;

import java.util.List;

public interface AdministrativeLeaveTypeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeLeaveType record);

    int insertSelective(AdministrativeLeaveType record);

    AdministrativeLeaveType selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeLeaveType record);

    int updateByPrimaryKey(AdministrativeLeaveType record);
    //获取所有请假类型
    List<AdministrativeLeaveType> getAllTpe();
}