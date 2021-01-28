package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeAnnualLeave;

public interface AdministrativeAnnualLeaveMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeAnnualLeave record);

    int insertSelective(AdministrativeAnnualLeave record);

    AdministrativeAnnualLeave selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeAnnualLeave record);

    int updateByPrimaryKey(AdministrativeAnnualLeave record);

    //通过类型id获取员工的假期信息
    AdministrativeAnnualLeave getAnnualLeaveByTypeId(Integer typeId);
    //通过类型id和员工id获取假期信息
    AdministrativeAnnualLeave getAnnualLeave(Integer typeId,Integer empId);
}