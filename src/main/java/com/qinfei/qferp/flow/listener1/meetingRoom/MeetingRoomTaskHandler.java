package com.qinfei.qferp.flow.listener1.meetingRoom;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.meeting.IMeetingRoomApplyService;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @CalssName MeetingRoomTaskHandler
 * @Description 会议审核
 * @Author xuxiong
 * @Date 2019/10/30 0030 16:56
 * @Version 1.0
 */
public class MeetingRoomTaskHandler implements TaskListener, ICommonTaskHandler {
    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
        // 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        if(StringUtils.isEmpty(nextUser)){
            throw new QinFeiException(1001, "会议对应会议室没有设置审核人！");
        }
        delegateTask.setVariable("acceptDept", nextUserDept);
        delegateTask.setVariable("acceptWorker", nextUserId);

        // 使用完毕后清空；
        delegateTask.removeVariable("nextUser");
        delegateTask.removeVariable("nextUserName");
        delegateTask.removeVariable("nextUserDept");

        // 设置审核人；
        delegateTask.setAssignee(nextUser);
        delegateTask.setOwner(nextUserName);
        // 更新审核人到数据库中；
        delegateTask.setVariable("approveUser", nextUser);
        delegateTask.setVariable("approveUserName", nextUserName);
    }

    @Override
    public void handleApproveData(DelegateTask delegateTask) {
        // 更新到数据库中；
        delegateTask.setVariable("state", IConst.STATE_MEETINGROM);
        setApproveUser(delegateTask,IConst.STATE_MEETINGROM);
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
        }else {
            //待办消息分类
            map.put("parentType",1);//待办
            commonDefaultHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //默认其他情况处理方法
        }
        Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
        // =================================================通知推送模块结束=================================================
        // 流程当前的任务ID；
        String taskId = delegateTask.getId();
        //更新会议室预约表
        IMeetingRoomApplyService meetingRoomApplyService = SpringUtils.getBean(IMeetingRoomApplyService.class);
        MeetingRoomApply meetingRoomApply = new MeetingRoomApply();
        meetingRoomApply.setId(delegateTask.getVariable("MeetingRoomApplyId", Integer.class));
        meetingRoomApply.setItemId(itemId);
        if(delegateTask.getVariable("acceptWorker", Integer.class) != null){
            meetingRoomApply.setApproverUserId(delegateTask.getVariable("acceptWorker", Integer.class));
        }
        meetingRoomApply.setTaskId(taskId);
        meetingRoomApplyService.updateProcess(meetingRoomApply);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}
