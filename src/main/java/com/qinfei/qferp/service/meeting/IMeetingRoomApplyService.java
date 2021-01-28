package com.qinfei.qferp.service.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IMeetingRoomApplyService
 * @Description 会议室预约表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:22
 * @Version 1.0
 */
public interface IMeetingRoomApplyService {
    String CACHE_KEY = "meetingRoomApply";

    //新增会议预约
    ResponseData save(MeetingRoomApply meetingRoomApply);

    //修改会议室预约
    void update(MeetingRoomApply meetingRoomApply);

    //取消会议室预定
    void del(Integer id);

    //修改流程审核
    void updateProcess(MeetingRoomApply meetingRoomApply);

    //添加会议页面
    ResponseData addMeetingPage(Integer id);

    //根据参数获取预约列表
    PageInfo<MeetingRoomApply> listMeetingApplyByParam(Map<String, Object> param, Pageable pageable);

    //我的预约列表
    PageInfo<MeetingRoomApply> listMyMeetingApply(Map<String, Object> param, Pageable pageable);

    //我的预约数量
    int getMyMeetingApplyTotal(Map<String, Object> param);

    //通知会议人员开会
    ResponseData noticeMeetingUser(Integer meetApplyId);

    //管理员获取会议室预约记录列表
    PageInfo<Map<String, Object>> listMeetingRoomHasApply(Map<String, Object> param, Pageable pageable);

    //管理员获取会议室预约记录数量
    int getMeetingRoomHasApplyTotal(Map<String, Object> param);
}
