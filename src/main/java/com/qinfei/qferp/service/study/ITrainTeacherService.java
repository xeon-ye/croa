package com.qinfei.qferp.service.study;

import com.qinfei.qferp.entity.study.TrainTeacher;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: ITrainTeacherService
 * @Description: 培训讲师接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:39
 * @Version: 1.0
 */
public interface ITrainTeacherService {
    String CACHE_KEY = "trainTeacher";

    //新增
    @CacheEvict(value = CACHE_KEY, key = "'userId='+#trainTeacher.userId")
    void save(TrainTeacher trainTeacher);

    //更新
    @CacheEvict(value = CACHE_KEY, key = "'userId='+#trainTeacher.userId")
    void update(TrainTeacher trainTeacher);

    //删除
    @CacheEvict(value = CACHE_KEY, key = "'userId='+#userId")
    void del(int id, int userId);

   //根据用户ID获取讲师信息，结果为空不缓存
    @Cacheable(value = CACHE_KEY, key = "'userId='+#userId", unless = "#result==null")
    TrainTeacher getTrainTeacherByUserId(int userId);

    //根据条件获取讲师总数
    int getTrainTeacherTotal(Map<String, Object> param);

    //分页查询讲师
    PageInfo<TrainTeacher> listTrainTeacher(Map<String, Object> param, Pageable pageable);

    //查询讲师/非讲师的用户
    List<Map<String, Object>> listUserNotTeacher(boolean existsFlag);

    //查询所有用户
    List<Map<String, Object>> listUser();

    //讲师统计导出
    void trainTeacherStatisticsExport(OutputStream outputStream, Map<String, Object> param);
}
