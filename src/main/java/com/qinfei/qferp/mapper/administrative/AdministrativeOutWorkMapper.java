package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeOutWork;

public interface AdministrativeOutWorkMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeOutWork record);

    int insertSelective(AdministrativeOutWork record);

    AdministrativeOutWork selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeOutWork record);

    int updateByPrimaryKey(AdministrativeOutWork record);
    //伪删除
    int deleteById(Integer id);
    //通过流程id伪删除
    int deleteByAdmId(Integer id);
    //通过行政流程id获取外出数据
    AdministrativeOutWork selectByAdministrativeId(Integer id);
}