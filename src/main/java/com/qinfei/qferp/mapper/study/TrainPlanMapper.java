package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.TrainPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainTeacherMapper
 * @Description: 培训计划表
 * @Author: Xuxiong
 * @Date: 2020/4/7 0007 17:56
 * @Version: 1.0
 */
public interface TrainPlanMapper extends BaseMapper<TrainPlan, Integer> {
    //新增
    int save(TrainPlan trainPlan);

    //修改状态
    int updateStateById(@Param("state") byte state, @Param("updateId") int updateId, @Param("id") int id);

    //更新
    int updateById(TrainPlan trainPlan);

    //分页查询培训计划
    List<TrainPlan> listTrainPlanByParam(Map<String, Object> param);

    //查询培训计划总数
    int getTrainPlanTotal(Map<String, Object> param);
}
