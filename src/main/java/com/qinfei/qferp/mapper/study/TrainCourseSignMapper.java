package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.TrainCourseSign;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainCourseSignMapper
 * @Description: 课程报名表
 * @Author: Xuxiong
 * @Date: 2020/4/7 0007 17:56
 * @Version: 1.0
 */
public interface TrainCourseSignMapper extends BaseMapper<TrainCourseSign, Integer> {
    //新增
    int save(TrainCourseSign trainCourseSign);

    //更新
    int updateById(TrainCourseSign trainCourseSign);

    //修改状态
    int updateStateById(@Param("state") byte state, @Param("updateId") int updateId, @Param("id") int id);

    //修改状态
    int updateStateByCourseId(@Param("state") byte state, @Param("updateId") int updateId, @Param("courseId") int courseId);

    //获取指定人员的课程报名
    TrainCourseSign getCourseSignByUserId(@Param("courseId") int courseId, @Param("userId") int userId);

    //获取指定学员的报名课程列表
    List<TrainCourseSign> listSignUpCourseByParam(Map<String, Object> param);

    //根据课程ID查询报名列表
    List<TrainCourseSign> listCourseSignByCourseId(@Param("courseId") int courseId);

    //学员总数
    int getStudentTotal(Map<String, Object> param);

    //学员统计
    List<Map<String, Object>> listStudentByParam(Map<String, Object> param);

    List<Map<String, Object>> listStudentByParamForExport(Map<String, Object> param);

    //课程学员列表
    List<Map<String, Object>> listStudentByCourseIdAndDept(Map<String, Object> param);
}
