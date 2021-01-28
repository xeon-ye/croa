package com.qinfei.qferp.service.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.meeting.Meeting;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;
import com.sun.org.apache.regexp.internal.RE;
import io.swagger.models.auth.In;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IMeetingService
 * @Description 会议室服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:19
 * @Version 1.0
 */
public interface IMeetingService {
    String CACHE_KEY = "meeting";

    ResponseData addMeeting(Meeting meeting,MultipartFile[] files);

    ResponseData editMeeting(Integer id);

    ResponseData delMeeting(Integer id);

    ResponseData updateMeeting(Meeting meeting,MultipartFile[] files);

    PageInfo<Meeting> meetingListPg(Map map,Pageable pageable);

    PageInfo<Meeting> meetingAllList(Map map,Pageable pageable);

    List<Meeting> meetingorganization();

    ResponseData meetingFlag(Integer id,Integer flag);

    List<User> meetUsers(Map<String, Object> map);

    PageInfo<Map<String, Object>> listMeetingByRoomId(Map<String, Object> param, Pageable pageable);

    int getMeetingTotal(Map<String, Object> param);
}
