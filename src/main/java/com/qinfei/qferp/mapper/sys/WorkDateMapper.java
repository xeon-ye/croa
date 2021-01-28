package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.WorkDate;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName WorkDateMapper
 * @Description 工作日表
 * @Author xuxiong
 * @Date 2019/10/15 0015 11:09
 * @Version 1.0
 */
public interface WorkDateMapper extends BaseMapper<WorkDate, Integer> {
    //新增日期
    int save(WorkDate workDate);

    //批量新增日期
    int batchSave(List<WorkDate> workDateList);

    //更新日期
    int updateById(WorkDate workDate);

    int batchUpdate(@Param("workDateList") List<WorkDate> workDateList);

    //根据ID修改日期状态
    int updateStateById(@Param("id") Integer id, @Param("state") Integer state);

    //根据参数修改日期状态
    int updateStateByParam(Map<String, Object> param);

    //根据ID获取信息
    WorkDate getWorkDateById(@Param("id") Integer id);

    //根据工作日获取信息
    WorkDate getWorkDateByDate(@Param("workDate") Date workDate, @Param("companyCode") String companyCode);

    List<WorkDate> listAllByParam(WorkDate workDate);

    //根据参数获取日期列表
    List<WorkDate> listByParam(Map<String, Object> param);

    //根据参数获取日期列表
    List<Map<String, Object>> listMapByParam(Map<String, Object> param);

}
