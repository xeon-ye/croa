package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.TrainCourse;
import com.qinfei.qferp.entity.study.TrainCourseRange;
import com.qinfei.qferp.entity.sys.Dept;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainCourseMapper
 * @Description: 培训课程表
 * @Author: Xuxiong
 * @Date: 2020/4/7 0007 17:56
 * @Version: 1.0
 */
public interface TrainCourseMapper extends BaseMapper<TrainCourse, Integer> {
    //新增
    int save(TrainCourse trainCourse);

    //更新
    int updateById(TrainCourse trainCourse);

    //批量更新
    int batchUpdate(@Param("updateId") int updateId,  @Param("trainCourseList") List<TrainCourse> trainCourseList);

    //修改状态
    int updateStateById(@Param("state") byte state, @Param("updateId") int updateId, @Param("id") int id);

    //审核
    int auditById(@Param("state") byte state, @Param("rejectReason") String rejectReason, @Param("updateId") int updateId, @Param("id") int id);

    //批量审核
    int batchAuditById(@Param("state") byte state, @Param("rejectReason") String rejectReason, @Param("updateId") int updateId, @Param("ids") List<Integer> ids);

    //根据讲师ID获取课程列表
    List<TrainCourse> listTrainCourseByTeacher(@Param("createId") int createId);

    //根据ID获取课程
    TrainCourse getTrainCourseById(@Param("id") int id);

    //根据ID列表获取课程集合
    List<TrainCourse> listTrainCourseByIds(@Param("ids") List<Integer> ids);

    //根据ID获取课程详情
    TrainCourse getTrainCourseDeailById(@Param("id") int id);

    //根据ID获取课程详情，编辑使用
    Map<String, Object> getCourseDetailById(@Param("id") int id);

    //获取课程总数
    int getCourseTotal(Map<String, Object> param);

    //根据参数查询课程列表
    List<TrainCourse> listCourseByParam(Map<String, Object> param);

    //根据课程和学员查询指定答题卡
    TrainCourse getCourseByAnswerdCardId(@Param("id") int id);

    //课程范围
    List<Map<String, Object>> listDeptByParam(@Param("name") String name, @Param("companyCode") String companyCode);

    List<Map<String, Object>> listUserByParam(@Param("name") String name, @Param("companyCode") String companyCode);

    List<Map<String, Object>> listRoleByParam(@Param("name") String name);
}
