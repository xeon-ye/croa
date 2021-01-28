package com.qinfei.qferp.service.study;

import com.qinfei.qferp.entity.study.TrainSetting;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: IQuestionService
 * @Description: 培训设置接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:39
 * @Version: 1.0
 */
public interface ITrainSettingService {
    String CACHE_KEY = "trainSetting";

    //根据模块获取所有数据
    List<TrainSetting> listTrainSetting(Map<String, Object> param);

    //新增题目
    void save(TrainSetting trainSetting);

    //修改状态
    void updateStateById(byte state, int id);

    //修改
    void update(TrainSetting trainSetting);

    //置顶
    void top(TrainSetting trainSetting);

    //上移
    void up(TrainSetting trainSetting);

    //下移
    void down(TrainSetting trainSetting);

    //置底
    void bottom(TrainSetting trainSetting);

    /**
     * 根据参数，计算动态规则公式结果，除数为0的情况则默认为0
     * @param trainSettingList 按照层级排序的公式，最后一个为具体公式
     * @param param 公式中的变量 及 值
     * @return 公式计算结果
     */
    float calFormula(List<TrainSetting> trainSettingList, Map<String, String> param);

    //获取最大序号的配置
    TrainSetting getMaxSeqTrainSetting(String settingModule);

    //获取当前用户培训功能权限
    Map<String, Object> getTrainPermission(HttpServletRequest request);
}
