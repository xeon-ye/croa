package com.qinfei.qferp.flow.listener1.meetingRoom;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.meeting.Meeting;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import com.qinfei.qferp.entity.meeting.MeetingUser;
import com.qinfei.qferp.entity.schedule.UserSchedule;
import com.qinfei.qferp.entity.schedule.UserScheduleRelate;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.meeting.MeetingMapper;
import com.qinfei.qferp.mapper.meeting.MeetingUserMapper;
import com.qinfei.qferp.mapper.schedule.UserScheduleMapper;
import com.qinfei.qferp.mapper.schedule.UserScheduleRelateMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.service.meeting.IMeetingRoomApplyService;
import com.qinfei.qferp.service.meeting.IMeetingService;
import com.qinfei.qferp.service.schedule.IUserScheduleService;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName ProcessFinishHandler
 * @Description 审核完成
 * @Author xuxiong
 * @Date 2019/9/27 0027 9:05
 * @Version 1.0
 */
public class ProcessFinishHandler implements TaskListener, ICommonTaskHandler {
    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }

    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
        // 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        // 如果审核人信息不完整，从数据库获取角色默认的用户信息；
        if (StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)) {
            if(state == IConst.STATE_REJECT){
                delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                nextUser = delegateTask.getVariable("userId", String.class);
                nextUserName = delegateTask.getVariable("userName", String.class);
            }
        } else {
            delegateTask.setVariable("acceptDept", nextUserDept);
            delegateTask.setVariable("acceptWorker", nextUserId);

            // 使用完毕后清空；
            delegateTask.removeVariable("nextUser");
            delegateTask.removeVariable("nextUserName");
            delegateTask.removeVariable("nextUserDept");

        }

        // 设置审核人；
        delegateTask.setAssignee(nextUser);
        delegateTask.setOwner(nextUserName);
        // 更新审核人到数据库中；
        delegateTask.setVariable("approveUser", nextUser);
        delegateTask.setVariable("approveUserName", nextUserName);
    }

    @Override
    public void handleApproveData(DelegateTask delegateTask) {
        boolean agree = getOpinion(delegateTask);
        int state;
        if (agree) {
            state = IConst.STATE_FINISH;
        } else {
            state = IConst.STATE_REJECT;
        }

        // 更新到数据库中；
        delegateTask.setVariable("state", state);
        // 设置审核人；
        setApproveUser(delegateTask, state);
    }

    @Override
    public void updateProcessData(DelegateTask delegateTask) {
        Map<String, Object> map = getTaskParam(delegateTask); //获取基础数据
        Integer state = (Integer) map.get("state");
        map.put("messageTypeName","会议");//消息子类类型
        map.put("type",23);//会议
        if(state == IConst.STATE_REJECT){
            //消息分类
            map.put("parentType",3);//通知
            commonRejectHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //驳回处理方法
        }else{
            //消息分类
            map.put("parentType",2);//提醒
            commonFinishHandle((String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //审核完成处理方法
        }
        Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
        // =================================================通知推送模块结束=================================================
        // 流程当前的任务ID；
        String taskId = delegateTask.getId();
        //更新会议室预约表
        IMeetingRoomApplyService meetingRoomApplyService = SpringUtils.getBean(IMeetingRoomApplyService.class);
        MeetingMapper meetingMapper  = SpringUtils.getBean(MeetingMapper.class);
        MeetingRoomApply meetingRoomApply = new MeetingRoomApply();
        meetingRoomApply.setId(delegateTask.getVariable("MeetingRoomApplyId", Integer.class));
        meetingRoomApply.setState(state);
        meetingRoomApplyService.updateProcess(meetingRoomApply);
        //更新会议表
        Meeting meeting = new Meeting();
        meeting.setId(Integer.parseInt((String)map.get("dataId")));
        meeting.setState(state);
        meetingMapper.update(meeting);
        //日程操作
        if(state == IConst.STATE_FINISH){
            MeetingUserMapper meetingUserMapper = SpringUtils.getBean(MeetingUserMapper.class);
            Meeting tempMeeting = meetingMapper.getMeetingById(Integer.parseInt((String)map.get("dataId")));
            List<MeetingUser> meetingUserList = meetingUserMapper.listMeetUserByMeetId(Integer.parseInt((String)map.get("dataId")));
            //创建日程
            if(CollectionUtils.isNotEmpty(meetingUserList)){
                addSchedule(tempMeeting, meetingUserList);
            }
        }
    }

    //添加日程
    private void addSchedule(Meeting meeting, List<MeetingUser> meetingUserList){
        IUserScheduleService userScheduleService = SpringUtils.getBean(IUserScheduleService.class);
        UserScheduleMapper userScheduleMapper = SpringUtils.getBean(UserScheduleMapper.class);
        UserScheduleRelateMapper userScheduleRelateMapper = SpringUtils.getBean(UserScheduleRelateMapper.class);
        List<UserSchedule> userScheduleList = new ArrayList<>();

        for(MeetingUser meetingUser : meetingUserList){
            UserSchedule userSchedule = new UserSchedule();
            userSchedule.setName(meeting.getTitle());
            userSchedule.setIsAllDay(1);
            userSchedule.setStartDate(meeting.getStartTime());
            userSchedule.setEndDate(meeting.getEndTime());
            userSchedule.setRepeatFlag(0);
            userSchedule.setRemindFlag(1);
            userSchedule.setScheduleType(1);
            userSchedule.setOtherReplateId(meeting.getId());
            userSchedule.setJumpTitle("会议管理");
            userSchedule.setJumpUrl("meeting/meetingManage?type=2&id=" +meeting.getId());
            userSchedule.setCreateId(meetingUser.getUserId());
            userSchedule.setUpdateId(meetingUser.getUserId());
            userScheduleList.add(userSchedule);
        }
        int row = userScheduleMapper.batchSave(userScheduleList);
        if(row > 0){
            List<UserScheduleRelate> userScheduleRelateList = new ArrayList<>();
            for(UserSchedule userSchedule : userScheduleList){
                UserScheduleRelate userScheduleRelate = new UserScheduleRelate();
                userScheduleRelate.setScheduleId(userSchedule.getId());
                userScheduleRelate.setRepeatDate(new Date(meeting.getStartTime().getTime() - 30*60*1000));
                userScheduleRelate.setRemindDate(new Date(meeting.getStartTime().getTime() - 30*60*1000));//提前30分钟提醒
                userScheduleRelate.setCreateId(userSchedule.getCreateId());
                userScheduleRelate.setUpdateId(userSchedule.getUpdateId());
                userScheduleRelateList.add(userScheduleRelate);
            }
            userScheduleRelateMapper.batchSave(userScheduleRelateList);
        }
        userScheduleService.todayCachePut(); //刷新日程缓存
    }
}
