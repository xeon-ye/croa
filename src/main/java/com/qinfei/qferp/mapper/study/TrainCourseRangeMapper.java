package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.TrainCourseRange;
import com.qinfei.qferp.entity.study.TrainCourseSign;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @CalssName: TrainCourseRangeMapper
 * @Description: 课程范围表
 * @Author: Xuxiong
 * @Date: 2020/4/7 0007 17:56
 * @Version: 1.0
 */
public interface TrainCourseRangeMapper extends BaseMapper<TrainCourseRange, Integer> {
    //新增
    int save(TrainCourseRange trainCourseRange);

    //批量新增
    int saveBatch(List<TrainCourseRange> trainCourseRangeList);

    //更新
    int updateById(TrainCourseRange trainCourseRange);

    //批量更新状态
    int updateStateByCourseId(@Param("state") byte state, @Param("updateId") int updateId, @Param("courseId") int courseId);

}
