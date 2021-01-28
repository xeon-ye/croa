package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeLeave;
import com.qinfei.qferp.entity.administrative.AdministrativeOverTimeWork;

import java.util.List;

public interface AdministrativeOvertimeworkMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdministrativeOverTimeWork record);

    int insertSelective(AdministrativeOverTimeWork record);

    AdministrativeOverTimeWork selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdministrativeOverTimeWork record);

    int updateByPrimaryKey(AdministrativeOverTimeWork record);

    //伪删除
    int deleteById(Integer id);

    //通过流程id伪删除
    int deleteByAdmId(Integer id);

    //通过行政流程id获取请假数据
    AdministrativeOverTimeWork selectByAdministrativeId(Integer admId);

}