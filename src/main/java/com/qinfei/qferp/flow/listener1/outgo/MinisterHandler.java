package com.qinfei.qferp.flow.listener1.outgo;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.fee.IOutgoService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 媒介部长审核
 */
public class MinisterHandler implements TaskListener, ICommonTaskHandler{
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
                    User user = userMapper.getById(Integer.valueOf(delegateTask.getVariable("userId").toString()));
                    Integer deptId = user.getDeptId();
                    DeptMapper deptMapper = SpringUtils.getBean("deptMapper");
                    Dept dept = deptMapper.getById(deptId);
                    if (dept != null) {
                        //媒介请款，无论是否部长，都是部长审核，如果申请人为部门以下的部门，则分管领导审核，否则部门负责人
                        if(dept.getLevel() > 3){
                            Integer userId = dept.getMgrLeaderId(); //获取分管领导ID
                            User mgrLeaderUser = userMapper.getById(userId);
                            delegateTask.setVariable("acceptDept", mgrLeaderUser.getDeptId());
                            delegateTask.setVariable("acceptWorker", userId);
                            nextUser = userId == null ? null : userId.toString();
                            nextUserName = mgrLeaderUser.getName();
                        }else{
                            Integer userId = dept.getMgrId();
                            delegateTask.setVariable("acceptDept", deptId);
                            delegateTask.setVariable("acceptWorker", userId);
                            nextUser = userId == null ? null : userId.toString();
                            nextUserName = dept.getMgrName();
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
        map.put("messageTypeName","请款");//消息子类类型
        map.put("type",3);//请款
        if(state == IConst.STATE_REJECT){
            //消息分类
            map.put("parentType",3);//通知
            commonRejectHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //驳回处理方法
        }else {
            //消息分类
            map.put("parentType",1);//待办
            commonDefaultHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //默认其他情况处理方法
        }
        Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
        // =================================================通知推送模块结束=================================================
        // 流程当前的任务ID；
        String taskId = delegateTask.getId();
        //更新数据
        Outgo outgo = new Outgo();
        outgo.setId(Integer.parseInt((String) map.get("dataId")));
        outgo.setState(state);
        outgo.setProcessType(delegateTask.getVariable("process", Integer.class));
        outgo.setUpdateUserId(AppUtil.getUser().getId());
        // 更新流程当前的任务ID；
        outgo.setTaskId(taskId);
        // 更新待办事项的ID；
        outgo.setItemId(itemId);
        IOutgoService outgoService = SpringUtils.getBean("outgoService");
        outgoService.update(outgo);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}

