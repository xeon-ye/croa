package com.qinfei.qferp.flow.listener1.outwork;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @CalssName LeaderTaskHandler
 * @Description 部门审核
 * @Author xuxiong
 * @Date 2019/9/27 0027 15:40
 * @Version 1.0
 */
public class LeaderTaskHandler implements TaskListener, ICommonTaskHandler {
    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
        // 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        // 如果审核人信息不完整，从数据库获取角色默认的用户信息；
        if (StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)) {
            switch (state){
                // 审核被驳回；
                case IConst.STATE_REJECT:
                    delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    nextUser = delegateTask.getVariable("userId", String.class);
                    nextUserName = delegateTask.getVariable("userName", String.class);
                    break;
                case IConst.STATE_BZ:
                    UserMapper userMapper = SpringUtils.getBean("userMapper");
                    //获取流程申请人，判断申请人是否是部长，如果是则直属领导审核则由分管领导
                    User user = userMapper.getById(Integer.valueOf(delegateTask.getVariable("userId").toString()));
                    Integer deptId = user.getDeptId();
                    DeptMapper deptMapper = SpringUtils.getBean("deptMapper");
                    Dept dept = deptMapper.getById(deptId);
                    if (dept != null) {
                        //如果申请人为部门以上的部门，并且不是部门负责人，则部门负责人，否则分管领导审核
                        if(dept.getLevel() <= 3 && !user.getId().equals(dept.getMgrId())){
                            Integer userId = dept.getMgrId();
                            delegateTask.setVariable("acceptDept", deptId);
                            delegateTask.setVariable("acceptWorker", userId);
                            nextUser = userId == null ? null : userId.toString();
                            nextUserName = dept.getMgrName();
                        }else{
                            Integer userId = dept.getMgrLeaderId(); //获取分管领导ID
                            User mgrLeaderUser = userMapper.getById(userId);
                            delegateTask.setVariable("acceptDept", mgrLeaderUser.getDeptId());
                            delegateTask.setVariable("acceptWorker", userId);
                            nextUser = userId == null ? null : userId.toString();
                            nextUserName = mgrLeaderUser.getName();
                        }
                    }
                    break;
                // 不存在；
                default:
                    break;
            }
        }else {
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
            state = IConst.STATE_BZ;
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
        map.put("messageTypeName","外出");//消息子类类型
        map.put("type",9);//外出
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
        //更新出差记录表数据
        Administrative leave = new Administrative();
        if (delegateTask.getVariable("acceptWorker", Integer.class) != null) {
            leave.setApproverUserId(String.valueOf(delegateTask.getVariable("acceptWorker", Integer.class)));
        }
        leave.setId(Integer.parseInt((String)map.get("dataId")));
        leave.setState(state);
        leave.setTaskId(taskId);// 更新流程当前的任务ID；
        leave.setItemId(itemId); // 更新待办事项的ID；
        IAdministrativeLeaveService leaveService = SpringUtils.getBean("administrativeLeaveService");
        leaveService.processLeava(leave);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}
