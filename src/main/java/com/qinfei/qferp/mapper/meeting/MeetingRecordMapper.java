package com.qinfei.qferp.mapper.meeting;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.meeting.MeetingRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingRecordMapper
 * @Description 会议记录表
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:05
 * @Version 1.0
 */
public interface MeetingRecordMapper extends BaseMapper<MeetingRecord, Integer> {

    void addMeetingRecord(MeetingRecord meetingRecord);

    MeetingRecord getById(Integer meetId);

    void delMeetingRecord(Map map);

    List<MeetingRecord> meetingRecordListPg(Map<String, Object> map);

    int getMeetingRecordTotal(Map<String, Object> map);

}
