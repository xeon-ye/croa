package com.qinfei.qferp.mapper.meeting;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingRoomApplyMapper
 * @Description 会议室预约表
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:02
 * @Version 1.0
 */
public interface MeetingRoomApplyMapper extends BaseMapper<MeetingRoomApply, Integer> {
    //新增会议室预约
    int save(MeetingRoomApply meetingRoomApply);

    //根据ID更新预约信息
    int updateById(MeetingRoomApply meetingRoomApply);

    //根据ID更新状态
    int updateStateById(@Param("id") Integer id, @Param("state") Integer state);

    //根据ID获取预约信息
    MeetingRoomApply getMeetingRoomApplyById(@Param("id") Integer id);

    //根据时间获取存在预约的预约列表
    List<MeetingRoomApply> listMeetingApplyByTimeRange(@Param("startTime")Date startTime, @Param("endTime") Date endTime,
                                                       @Param("companyCode")String companyCode, @Param("meetRoomId") Integer meetRoomId,
                                                       @Param("id") Integer id);
    //根据参数获取预约列表
    List<MeetingRoomApply> listMeetingApplyByParam(Map<String, Object> param);

    //获取我的预约列表
    List<MeetingRoomApply> listMyMeetingApplyByParam(Map<String, Object> param);

    //根据预约ID获取预约信息
    MeetingRoomApply getMyMeetingApplyById(@Param("id") Integer id);

    //获取我的预约数量
    int getMyApplyCountByParam(Map<String, Object> param);

    //获取预约中列表
    List<MeetingRoomApply> listApplyMeetingRoomByParam(Map<String, Object> param);

    //根据roomId获取预约集合
    List<MeetingRoomApply> listMeetingApplyByRoomId(Map<String, Object> param);

    //管理员获取会议室预约记录总数
    int getMeetingRoomHasApplyCount(Map<String, Object> param);

    //管理员获取会议室预约记录列表
    List<Map<String, Object>> listMeetingRoomHasApplyByParam(Map<String, Object> param);
}
