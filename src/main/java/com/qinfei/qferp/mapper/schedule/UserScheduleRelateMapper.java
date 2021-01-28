package com.qinfei.qferp.mapper.schedule;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.schedule.UserSchedule;
import com.qinfei.qferp.entity.schedule.UserScheduleRelate;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @CalssName UserPlanMapper
 * @Description 用户日程关系表
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:53
 * @Version 1.0
 */
public interface UserScheduleRelateMapper extends BaseMapper<UserScheduleRelate, Integer> {

    //批量增加日程对应提醒时间
    int batchSave(List<UserScheduleRelate> userScheduleRelateList);

    //更改状态

    int updateSchedule(@Param("ids") List<Integer> ids, @Param("updateId") Integer updateId, @Param("updateDate")Date updateDate,@Param("state") Integer state);
}
