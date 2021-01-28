package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.TrainTeacher;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainTeacherMapper
 * @Description: 培训讲师表
 * @Author: Xuxiong
 * @Date: 2020/4/7 0007 17:56
 * @Version: 1.0
 */
public interface TrainTeacherMapper extends BaseMapper<TrainTeacher, Integer> {
    //新增
    int save(TrainTeacher trainTeacher);

    //根据用户ID获取讲师信息
    TrainTeacher getTrainTeacherByUserId(@Param("userId") int userId);

    //更新
    int updateById(TrainTeacher trainTeacher);

    //修改状态
    int updateStateById(@Param("state") byte state, @Param("updateId") int updateId, @Param("id") int id);

    //获取讲师总数
    int getTrainTeacherTotal(Map<String, Object> param);

    //分页查询讲师
    List<TrainTeacher> listTrainTeacher(Map<String, Object> param);

    List<Map<String, Object>> listTrainTeacherForExport(Map<String, Object> param);

    //查询所有讲师/非讲师的用户
    List<Map<String, Object>> listUserNotTeacher(@Param("companyCode") String companyCode, @Param("existsFlag") boolean existsFlag);

    //查询所有非讲师的用户
    List<Map<String, Object>> listUser(@Param("companyCode") String companyCode);
}
