package com.qinfei.qferp.service.sys;

import com.qinfei.qferp.entity.sys.WorkDate;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName IWorkDateService
 * @Description 工作日表服务接口
 * @Author xuxiong
 * @Date 2019/10/15 0015 11:35
 * @Version 1.0
 */
public interface IWorkDateService {
    String CACHE_KEY = "workdate";

    //新增日期
    void save(WorkDate workDate);

    //获取指定日期
    WorkDate getWorkDateByDate(String workDate);

    //获取指定时间范围的工作日、休息日、法定节假日
    Map<String, List<String>> listDateByRange(String startDate, String endDate);

    //初始化时间范围
    void initBatchSave(WorkDate workDate);

    //范围编辑日期
    void batchEdit(WorkDate workDate);

    //更新日期
    void updateById(WorkDate workDate);

    //根据ID修改日期状态
    void updateStateById(Integer id, Integer state);

    //根据参数获取日期列表
    PageInfo<WorkDate> listByParam(Map<String, Object> param, Pageable pageable);

    //根据年月返回日历
    List<Map<String, String>> getCalendar(int year, int month);

    //导出日期列表
    void exportWorkDate(Map<String, Object> map, OutputStream outputStream);

    //获取当前日期，之前第几个工作日日期
    String getLastNumWorkDate(int num, Date submitDate);
}
