package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeLeave;

import java.util.List;

public interface AdministrativeLeaveMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeLeave record);

    int insertSelective(AdministrativeLeave record);

    AdministrativeLeave selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeLeave record);

    int updateByPrimaryKey(AdministrativeLeave record);

    //伪删除
    int deleteById(Integer id);

    //通过流程id伪删除
    int deleteByAdmId(Integer id);

    //获取员工所有请假信息
    List<AdministrativeLeave> listByEmpId(Integer empId);

    //通过行政流程id获取请假数据
    AdministrativeLeave selectByAdministrativeId(Integer id);


}