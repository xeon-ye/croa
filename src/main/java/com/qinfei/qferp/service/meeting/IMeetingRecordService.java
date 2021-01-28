package com.qinfei.qferp.service.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.meeting.MeetingRecord;
import com.qinfei.qferp.entity.meeting.MeetingTask;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @CalssName IMeetingRecordService
 * @Description 会议记录表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:21
 * @Version 1.0
 */
public interface IMeetingRecordService {
    String CACHE_KEY = "meetingRecord";

    MeetingRecord getById(Integer id);

    void delMeetingRecord(Integer id);

    //我的记录数量
    int getMeetingRecordTotal(Map<String, Object> map);

    PageInfo<MeetingRecord> meetingRecordListPg(Map<String, Object> map,Pageable pageable);

    ResponseData meetingDelay(MeetingTask meetingTask);


}
