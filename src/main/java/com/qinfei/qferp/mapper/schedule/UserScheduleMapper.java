package com.qinfei.qferp.mapper.schedule;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserPlan;
import com.qinfei.qferp.entity.schedule.UserSchedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName UserPlanMapper
 * @Description 用户日程表
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:53
 * @Version 1.0
 */
public interface UserScheduleMapper extends BaseMapper<UserSchedule, Integer> {
    /**
     * 新增日程
     */
    int save(UserSchedule userSchedule);

    /**
     * 批量新增日程
     */
    int batchSave(List<UserSchedule> userScheduleList);

    /**
     * 查询所有能提醒的日程
     */
    List<UserSchedule> listScheduleByParam(Map<String, Object> map);

    /**
     * 查询指定用户日程日历数据
     */
    List<Map<String,Object>> listScheduleCalendarByParam(Map<String, Object> param);

    /**
     * 查询指定时间的日程列表
     */
    List<UserSchedule> listScheduleByDate(@Param("date") String date, @Param("userId") Integer userId);

    /**
     * 更新日程状态
     */
    int updateSchedule(UserSchedule userSchedule);

    /**
     * 查询日程
     */
    List<Integer> selectSchedule(Integer id);

}
