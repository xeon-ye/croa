package com.qinfei.qferp.service.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.meeting.MeetingTask;
import com.github.pagehelper.PageInfo;

import org.springframework.data.domain.Pageable;
import java.util.Map;

/**
 * @CalssName IMeetingTaskService
 * @Description 会议任务表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:32
 * @Version 1.0
 */
public interface IMeetingTaskService {
    String CACHE_KEY = "meetingTask";

    ResponseData addMeetingTask(MeetingTask meetingTask);

    PageInfo<MeetingTask> meetingTaskListPg(Map map, Pageable pageable);

    PageInfo<MeetingTask> meMeet(Map map, Pageable pageable);

    PageInfo<MeetingTask> duMeet(Map map, Pageable pageable);

    PageInfo<MeetingTask> meetingTask(Integer id,Pageable pageable);

    int getMeetingTaskTotal(Integer meetId);
    ResponseData getMeetingTask(Integer id);

    ResponseData editBaoMeetTask(MeetingTask meetingTask);


}
