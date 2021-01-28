package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeOnBusiness;

public interface AdministrativeOnBusinessMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeOnBusiness record);

    int insertSelective(AdministrativeOnBusiness record);

    AdministrativeOnBusiness selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeOnBusiness record);

    int updateByPrimaryKey(AdministrativeOnBusiness record);

    //伪删除
    int deleteById(Integer id);

    //通过流程id伪删除
    int deleteByAdmId(Integer id);

    //通过行政流程id获取请假数据
    AdministrativeOnBusiness selectByAdministrativeId(Integer id);

}