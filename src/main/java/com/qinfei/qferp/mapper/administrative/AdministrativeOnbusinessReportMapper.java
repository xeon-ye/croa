package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeOnbusinessReport;

public interface AdministrativeOnbusinessReportMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeOnbusinessReport record);

    int insertSelective(AdministrativeOnbusinessReport record);

    AdministrativeOnbusinessReport selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeOnbusinessReport record);

    int updateByPrimaryKey(AdministrativeOnbusinessReport record);

    //根据行政流程id获取总结报告
    AdministrativeOnbusinessReport getByAdmId(Integer admId);
}