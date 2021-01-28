package com.qinfei.qferp.service.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.meeting.MeetingRecord;
import com.qinfei.qferp.entity.meeting.MeetingRecordViewUser;
import com.qinfei.qferp.entity.sys.User;

import java.util.List;

/**
 * @CalssName IMeetingRecordViewUserService
 * @Description 会议记录可见人员表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:21
 * @Version 1.0
 */
public interface IMeetingRecordViewUserService {
    String CACHE_KEY = "meetingRecordViewUser";

    ResponseData addMeetingRecord(MeetingRecord meetingRecord);

    List<User> getUserId(Integer id);
}
