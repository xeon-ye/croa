package com.qinfei.qferp.service.impl.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.meeting.MeetingRecord;
import com.qinfei.qferp.entity.meeting.MeetingTask;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.meeting.MeetingRecordMapper;
import com.qinfei.qferp.mapper.meeting.MeetingTaskMapper;
import com.qinfei.qferp.service.impl.flow.ProcessService;
import com.qinfei.qferp.service.meeting.IMeetingRecordService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.*;

/**
 * @CalssName MeetingRecordServiceImpl
 * @Description 会议记录表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:36
 * @Version 1.0
 */
@Service
@Slf4j
public class MeetingRecordService implements IMeetingRecordService {

    @Autowired
    private MeetingRecordMapper meetingRecordMapper;
    @Autowired
    private MeetingTaskMapper meetingTaskMapper;
    @Autowired
    private ProcessService processService;

    @Override
    public MeetingRecord getById(Integer id){
        return  meetingRecordMapper.getById(id);
    }

    @Override
    public  void delMeetingRecord(Integer id){
        User user = AppUtil.getUser();
        Map map = new HashMap();
        map.put("state", IConst.STATE_DELETE);
        map.put("updateId",user.getId());
        meetingRecordMapper.delMeetingRecord(map);
    }

    @Override
    public int getMeetingRecordTotal(Map<String, Object> map) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            if(map.get("recordType") == null){
                map.put("recordType", 0); //默认查询记录
            }
            map.put("userId", user.getId());
            result = meetingRecordMapper.getMeetingRecordTotal(map);
        }
        return result;
    }

    @Override
    public PageInfo<MeetingRecord> meetingRecordListPg(Map<String, Object> map,Pageable pageable){
        List<MeetingRecord> list = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            if(map.get("recordType") == null){
                map.put("recordType", 0); //默认查询记录
            }
            map.put("userId", user.getId());
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            list = meetingRecordMapper.meetingRecordListPg(map);
        }
        return (PageInfo<MeetingRecord>) new PageInfo(list);
    }


    @Override
    public ResponseData meetingDelay(MeetingTask meetingTask){
        User user = AppUtil.getUser();
        meetingTask.setUpdateId(user.getId());
     //   meetingTask.setUpdateDate(new Date());
        meetingTaskMapper.meetingDelay(meetingTask);
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        return data;
    }


}
