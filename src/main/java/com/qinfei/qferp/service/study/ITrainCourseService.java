package com.qinfei.qferp.service.study;

import com.qinfei.qferp.entity.study.*;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: ITrainCourseService
 * @Description: 培训课程表接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:39
 * @Version: 1.0
 */
public interface ITrainCourseService {
    String CACHE_KEY = "trainCourse";

    //新增
    void signUp(TrainCourse trainCourse);

    //修改
    void update(TrainCourse trainCourse);

    //修改状态
    void updateStateById(byte state, int id);

    //审批课程
    void auditState(byte state, String rejectReason, int id);

    //批量审批课程
    void batchAuditState(byte state, String rejectReason, List<Integer> ids);

    //根据讲师ID获取课程列表
    List<TrainCourse> listTrainCourseByTeacher();

    //获取课程总数
    int getCourseTotal(Map<String, Object> param);

    //根据参数查询课程列表
    PageInfo<TrainCourse> listCourseByParam(Map<String, Object> param, Pageable pageable);

    //根据ID获取课程详情
    TrainCourse getTrainCourseDeailById(int id);

    //根据课程ID获取对应学员列表
    List<TrainCourseSign> listCourseSign(int courseId);

    //根据ID获取课程详情
    Map<String, Object> getCourseDetailById(int id);

    //课程范围
    List<Map<String, Object>> listCourseRange(String signStr,String name);

    //课程报名
    void courseSignUp(int courseId);

    //吐槽
    void courseVent(int courseId);

    //点赞
    void courseLike(int courseId);

    //评分
    void courseSetScore(int courseId, float score);

    //评论
    void courseSetComment(TrainCourseComment courseComment);

    //迟到/早退/旷课
    TrainCourseSign courseSignState(TrainCourseSign trainCourseSign);

    //停课
    void stopCourse(int courseId, Date trainStartTime);

    //管理员停课
    void adminStopCourse(int courseId);

    //复课
    void recoverCourse(int courseId, Date trainStartTime);

    //取消课程报名
    void cancelCourseSign(int signId);

    //报名名单
    List<Map<String, Object>> listCourseSignStudent(Integer courseId, Integer viewFlag);

    //报名名单导出
    void listCourseSignStudentExport(OutputStream outputStream, Integer courseId, Integer viewFlag);
}
