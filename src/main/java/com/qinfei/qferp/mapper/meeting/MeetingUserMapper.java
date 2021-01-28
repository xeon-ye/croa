package com.qinfei.qferp.mapper.meeting;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.meeting.Meeting;
import com.qinfei.qferp.entity.meeting.MeetingUser;
import com.qinfei.qferp.entity.sys.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingUserMapper
 * @Description 会议/会议任务相关人员表
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:04
 * @Version 1.0
 */
public interface MeetingUserMapper extends BaseMapper<MeetingUser, Integer> {

    int addMeetingUser(Map map);

    int saveBatch(List<MeetingUser> meetingUserList);

    int userIdSum(Integer id);

    int acceptSum(Integer id);

    List<MeetingUser> getUserId(Integer id);

    List<Integer> getInputUserId(Integer meetId);

    List<User> meetUsers(Map<String, Object> param);

    List<MeetingUser> listUserByParam(Map<String, Object> param);

    List<MeetingUser> listMeetUserByMeetId(@Param("meetId") Integer meetId);

    List<MeetingUser> getMeetintTaskUser(@Param("taskId") Integer taskId);

}
