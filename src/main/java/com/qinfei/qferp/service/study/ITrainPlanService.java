package com.qinfei.qferp.service.study;

import com.qinfei.qferp.entity.study.Paper;
import com.qinfei.qferp.entity.study.TrainPlan;
import com.qinfei.qferp.entity.study.TrainSetting;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: IQuestionService
 * @Description: 培训计划表接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:39
 * @Version: 1.0
 */
public interface ITrainPlanService {
    String CACHE_KEY = "trainPlan";

    //新增
    void save(TrainPlan trainPlan);

    //修改状态
    void updateStateById(byte state, int id);

    //修改
    void update(TrainPlan trainPlan);

    //获取培训计划总数
    int getTrainPlanTotal(Map<String, Object> param);

    //分页获取
    PageInfo<TrainPlan> listTrainPlan(Map<String, Object> param, Pageable pageable);
}
