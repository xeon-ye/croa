package com.qinfei.qferp.mapper.meeting;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.meeting.MeetingTask;
import com.qinfei.qferp.entity.meeting.MeetingUser;
import com.qinfei.qferp.entity.sys.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingTaskMapper
 * @Description 会议任务表
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:07
 * @Version 1.0
 */
public interface MeetingTaskMapper extends BaseMapper<MeetingTask, Integer> {

    int addMeetingTask(MeetingTask meetingTask);

    List<MeetingTask> meetingTaskListPg(Map<String,Object> map);

    List<MeetingTask> meMeet(Map<String,Object> map);

    List<MeetingTask> duMeet(Map<String,Object> map);

    List<MeetingTask> meetingTask(Integer id);

    int getMeetingTaskTotal(Integer meetId);

    List<MeetingUser> meetingUser(@Param("ids") List<Integer> ids);

    int meetingDelay(MeetingTask meetingTask);

    MeetingTask getMeetingTask(Integer id);

}
