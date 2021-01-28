package com.qinfei.qferp.mapper.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.meeting.Meeting;
import com.qinfei.qferp.entity.meeting.MeetingRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingMapper
 * @Description 会议表
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:03
 * @Version 1.0
 */
public interface MeetingMapper extends BaseMapper<Meeting, Integer> {

    int addMeeting(Meeting meeting);

    Meeting editMeeting(Integer id);

    int delMeeting(Meeting meeting);

    int updateMeeting(Meeting meeting);

    int deleteMeetingUser(Integer meetingId);

    List<Meeting> meetingListPg(Map<String, Object> map);

    List<Meeting> meetingAllList(Map<String,Object> map);

    List<Meeting> meetingorganization(Integer userId);

    int meetingFlag(Map<String,Object> map);

    List<Map<String, Object>> listMeetingByRoomId(Map<String, Object> map);

    int getMeetingTotal(Map<String, Object> map);

    Meeting getMeetingById(@Param("id") Integer id);

    int updateStateById(Meeting meeting);

    String getUser(Integer userId);

}
