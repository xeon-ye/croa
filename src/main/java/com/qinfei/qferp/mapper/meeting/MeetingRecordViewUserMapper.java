package com.qinfei.qferp.mapper.meeting;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.meeting.MeetingRecordViewUser;
import com.qinfei.qferp.entity.sys.User;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingRecordViewUserMapper
 * @Description 会议记录可见人员表
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:06
 * @Version 1.0
 */
public interface MeetingRecordViewUserMapper extends BaseMapper<MeetingRecordViewUser, Integer> {

    void addUserId(Map map);

    int saveBatch(List<MeetingRecordViewUser> meetingRecordViewUserList);

    List<User> getUserId(Integer id);
}
