package com.qinfei.qferp.service.impl.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.meeting.MeetingRecord;
import com.qinfei.qferp.entity.meeting.MeetingRecordViewUser;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.meeting.MeetingRecordMapper;
import com.qinfei.qferp.mapper.meeting.MeetingRecordViewUserMapper;
import com.qinfei.qferp.service.meeting.IMeetingRecordViewUserService;
import com.qinfei.qferp.utils.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @CalssName MeetingRecordServiceImpl
 * @Description 会议记录可见人员表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:36
 * @Version 1.0
 */
@Service
@Slf4j
public class MeetingRecordViewUserService implements IMeetingRecordViewUserService {
    @Autowired
    private MeetingRecordViewUserMapper meetingRecordViewUserMapper;
    @Autowired
    private MeetingRecordMapper meetingRecordMapper;

    @Override
    @Transactional
    public ResponseData addMeetingRecord(MeetingRecord meetingRecord){
        try{
            User user = AppUtil.getUser();
            if (user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            meetingRecord.setUpdateDate(new Date());
            meetingRecord.setCompanyCode(user.getCompanyCode());
            meetingRecord.setCreateId(user.getId());
            meetingRecord.setCreateDate(new Date());
            meetingRecord.setUpdateId(user.getId());
            meetingRecordMapper.addMeetingRecord(meetingRecord);
            if(CollectionUtils.isNotEmpty(meetingRecord.getInputVisibleUserId())){ //判断可见人员是否包含自己，不包含则默认设置
                if(!meetingRecord.getInputVisibleUserId().contains(user.getId())){
                    meetingRecord.getInputVisibleUserId().add(user.getId());
                }
            }else{
                meetingRecord.setInputVisibleUserId(Arrays.asList(user.getId()));
            }
            List<MeetingRecordViewUser> meetingRecordViewUserList = new ArrayList<>();
            for (Integer userId : meetingRecord.getInputVisibleUserId()){
                meetingRecordViewUserList.add(bulidMeetingUser(meetingRecord.getId(),userId, user));
            }
            meetingRecordViewUserMapper.saveBatch(meetingRecordViewUserList);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            return data ;
        }catch (QinFeiException e){
            e.printStackTrace();
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "很抱歉，添加会议记录出错啦，请联系技术人员！");
        }
    }

    //构建会议人员实例
    private MeetingRecordViewUser bulidMeetingUser(Integer meetingRecordId, Integer userId, User user){
        MeetingRecordViewUser meetingRecordViewUser = new MeetingRecordViewUser();
        meetingRecordViewUser.setMeetRecordId(meetingRecordId);
        meetingRecordViewUser.setUserId(userId);
        meetingRecordViewUser.setCreateId(user.getId());
        meetingRecordViewUser.setUpdateId(user.getId());
        return meetingRecordViewUser;
    }

    public List<User> getUserId(Integer id){
        return meetingRecordViewUserMapper.getUserId(id);
    }


    
}
