package com.qinfei.qferp.service.schedule;

import com.qinfei.qferp.entity.schedule.UserSchedule;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IUserScheduleService
 * @Description 用户日程服务接口
 * @Author xuxiong
 * @Date 2019/9/2 0002 18:30
 * @Version 1.0
 */
public interface IUserScheduleService {
    String CACHE_KEY = "userSchedule";

    List<UserSchedule> save(UserSchedule userSchedule, int userId);

    @CachePut(value = CACHE_KEY, key = "'todayAll'")
    List<UserSchedule> todayCachePut(List<UserSchedule> userScheduleList);

    @CachePut(value = CACHE_KEY, key = "'todayAll'")
    List<UserSchedule> todayCachePut();

    List<Map<String, String>> getCalendar(int year, int month, int userId);

    List<UserSchedule> listScheduleByDate(String date, int userId);

    @Cacheable(value = CACHE_KEY,  key = "'todayAll'")
    List<UserSchedule> listAllScheduleByDate(String date);

    @CacheEvict(value = CACHE_KEY,  key = "'todayAll'")
    void deleteCache();

    void sendMessage(List<UserSchedule> userScheduleList);
}
