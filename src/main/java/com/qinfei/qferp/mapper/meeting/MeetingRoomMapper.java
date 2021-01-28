package com.qinfei.qferp.mapper.meeting;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.meeting.MeetingRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingRoomMapper
 * @Description 会议室表
 * @Author xuxiong
 * @Date 2019/10/18 0018 10:07
 * @Version 1.0
 */
public interface MeetingRoomMapper extends BaseMapper<MeetingRoom, Integer> {
    //新增会议室
    int save(MeetingRoom meetingRoom);

    //根据ID更新
    int updateById(MeetingRoom meetingRoom);

    //根据ID更新状态
    int updateStateById(@Param("id") Integer id, @Param("state") Integer state);

    //根据ID更新预约状态
    int updateEnabledById(@Param("id") Integer id, @Param("enabled") Integer enabled);

    //根据会议室名称查看是否存在该名称
    MeetingRoom getMeetingRoomByName(@Param("id") Integer id, @Param("name") String name, @Param("companyCode") String companyCode);

    //根据ID获取会议室信息
    MeetingRoom getMeetingRoomById(@Param("id") Integer id);

    //根据参数获取会议室列表
    List<MeetingRoom> listByParam(Map<String, Object> param);

    //根据参数获取会议室列表（用于导出）
    List<Map<String, Object>> listMapByParam(Map<String, Object> param);

    //根据参数获取会议室总数
    int getCountByParam(Map<String, Object> param);

    //获取指定公司的会议室列表
    List<MeetingRoom> listMeetingRoomByCompanyCode(@Param("companyCode") String companyCode);
}
