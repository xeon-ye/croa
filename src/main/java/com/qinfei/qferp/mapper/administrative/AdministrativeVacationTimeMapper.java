package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeVacationTime;

public interface AdministrativeVacationTimeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeVacationTime record);

    int insertSelective(AdministrativeVacationTime record);

    AdministrativeVacationTime selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeVacationTime record);

    int updateByPrimaryKey(AdministrativeVacationTime record);

    //通过员工id获取剩余调休时间
    AdministrativeVacationTime selectByEmpId(Integer id);

}