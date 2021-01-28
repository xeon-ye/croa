package com.qinfei.qferp.service.meeting;

import com.qinfei.qferp.entity.meeting.MeetingRoom;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @CalssName IMeetingRoomService
 * @Description 会议室表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:31
 * @Version 1.0
 */
public interface IMeetingRoomService {
    String CACHE_KEY = "meetingRoom";

    //新增会议室
    void save(MeetingRoom meetingRoom);

    //修改会议室
    void update(MeetingRoom meetingRoom);

    //删除会议室
    void del(Integer id);

    MeetingRoom getMeetingRoomById(Integer id);

    //获取会议室列表
    PageInfo<MeetingRoom> listMeetingRoom(Map<String, Object> param, Pageable pageable);

    //获取会议室预约
    PageInfo<MeetingRoom> listApplyMeetingRoom(Map<String, Object> param, Pageable pageable);

    //获取会议室预约的时间段
    List<Map<String, String>> getMeetingTimeSlot(Integer id);

    //根据参数获取会议室总数
    int getCountByParam(Map<String, Object> param);

    //导出会议列表
    void exportMeetingList(HttpServletResponse response, Map<String, Object> map);

    //导出会议室列表
    void exportMeetingRoomList(HttpServletResponse response, Map<String, Object> map);

    //会议室列表
    List<MeetingRoom> listMeetingRoom();

}
