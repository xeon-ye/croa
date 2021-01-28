package com.qinfei.qferp.mapper.administrative;

import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.UserBusinessConclusion;
import com.qinfei.qferp.entity.administrative.UserBusinessPlan;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdministrativeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Administrative record);

    int insertSelective(Administrative record);

    Administrative selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Administrative record);

    int updateByPrimaryKey(Administrative record);

    //通过员工id获取所有行政相关记录
    List<Administrative> administrativeList(Map<String, Object> params);
    //伪删除
    int updateStateByPrimaryKey(Integer id);
    //行政部获取所有员工审批中的，和自己所有的数据
    List<Administrative> getList(Map<String, Object> params);

    List<Administrative> getList1(Map<String, Object> params);

    List<Administrative> getList2(Map<String, Object> params);

    List<Administrative> getList3(Map<String, Object> params);

    List<Administrative> getList4(Map<String, Object> params);

    List<Map> exportleaveContent(Map map);

    List<Map> exportworkOvertimeContent(Map map);

    List<Map> exportGoOutContent(Map map);

    List<Map> exportBusinessTripContent(Map map);

    List<UserBusinessPlan> exportBusinessPlan(Map map);

    List<UserBusinessPlan> listByParam(Map map);

    List<UserBusinessPlan> listConclusion(Map<String, Object> map);
}