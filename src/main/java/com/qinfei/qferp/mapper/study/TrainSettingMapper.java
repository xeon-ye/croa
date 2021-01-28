package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.AnswerCard;
import com.qinfei.qferp.entity.study.TrainSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainSettingMapper
 * @Description: 培训设置表
 * @Author: Xuxiong
 * @Date: 2020/03/13 0020 15:54
 * @Version: 1.0
 */
public interface TrainSettingMapper extends BaseMapper<TrainSetting, Integer> {
    //新增
    int save(TrainSetting trainSetting);

    //批量新增
    int saveBatch(List<TrainSetting> trainSettingList);

    //更新
    int updateById(TrainSetting trainSetting);

    //批量更新信息
    int batchUpdateTrainSetting(@Param("updateId") int updateId, @Param("trainSettingList") List<TrainSetting> trainSettingList);

    //更新状态
    int updateStateById(@Param("state") byte state, @Param("updateId") int updateId, @Param("id") int id);

    //更新状态通过模块标识
    int updateStateByModule(@Param("state") byte state, @Param("updateId") int updateId, @Param("settingModule") String settingModule);

    //根据参数查询
    List<TrainSetting> listTrainSettingByParam(Map<String, Object> param);

    //获取最大排序数据
    TrainSetting getMaxSeqTrainSetting(@Param("settingModule") String settingModule);

}
