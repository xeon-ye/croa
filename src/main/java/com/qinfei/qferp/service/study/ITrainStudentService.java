package com.qinfei.qferp.service.study;

import com.qinfei.qferp.entity.study.TrainCourse;
import com.qinfei.qferp.entity.study.TrainCourseSign;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: ITrainStudentService
 * @Description: 培训学员接口
 * @Author: Xuxiong
 * @Date: 2020/4/17 0017 16:01
 * @Version: 1.0
 */
public interface ITrainStudentService {
    String CACHE_KEY = "trainStudent";

    //学员课程列表
    List<TrainCourseSign> listSignUpCourseByParam(Map<String, Object> param);

    //学员总数
    int getStudentTotal(Map<String, Object> param);

    //学员统计
    PageInfo<Map<String, Object>> listStudentByParam(Map<String, Object> param, Pageable pageable);

    //更新学员积分
    void updateStudentIntergal(TrainCourse trainCourse, TrainCourseSign trainCourseSign);

    //学员列表导出
    void trainStudentExport(OutputStream outputStream, Map<String, Object> param);
}
