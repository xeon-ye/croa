package com.qinfei.qferp.flow.listener1.reimbursement202006;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.fee.IReimbursementService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 集团财务分管领导
 */
public class JTCWMgrdepartmentHeadler  implements ICommonTaskHandler, TaskListener {
    @Override
    public void setApproveUser(DelegateTask delegateTask , int state){
        //获取下个审核人id
        Integer nextUserId = delegateTask.getVariable("nextUser",Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName",String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept",Integer.class);
        String company = delegateTask.getVariable("company",String.class);
        if (StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)){
            String[] datas ;
            switch (state){
                case IConst.STATE_REJECT:
                    delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    nextUser = delegateTask.getVariable("userId", String.class);
                    nextUserName = delegateTask.getVariable("userName", String.class);
                    break;
                case IConst.STATE_JTCWMGR:
                    UserMapper userMapper = SpringUtils.getBean("userMapper");
                    //集团财务部门负责人
                    List<Dept> dept = userMapper.listJTCWhead("JT","CW");
                    if (!CollectionUtils.isEmpty(dept)){
                        delegateTask.setVariable("acceptDept",dept.get(0).getId());
                        delegateTask.setVariable("acceptWorker",dept.get(0).getMgrLeaderId());
                        nextUser = dept.get(0).getMgrLeaderId().toString();
                        nextUserName = dept.get(0).getMgrLeaderName();
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
    public void handleApproveData (DelegateTask delegateTask){
        boolean agree = getOpinion(delegateTask);
        int state;
        if (agree){
            state = IConst.STATE_JTCWMGR;
        }else {
            state = IConst.STATE_REJECT;
        }
        delegateTask.setVariable("state",state);
        setApproveUser(delegateTask,state);
    }

    @Override
    public void updateProcessData (DelegateTask delegateTask){
        Map<String,Object> map = getTaskParam(delegateTask);
        Integer state = (Integer)map.get("state");
        map.put("messageTypeName","费用报销");
        map.put("type",5);
        if (state == IConst.STATE_REJECT){
            map.put("parentType",3);
            commonRejectHandle(delegateTask,(String)map.get("pictureAddress"),(String)map.get(IProcess.PROCESS_NAME),(String)map.get("dataUrl"),map);
        }else {
            map.put("parentType",1);
            commonDefaultHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //默认其他情况处理方法
        }
        Integer itemId = commonSendMessage(delegateTask,map);

        String taskId = delegateTask.getId();
        IReimbursementService reimbursementService = SpringUtils.getBean("reimbursementService");
        Integer acceptWorker = delegateTask.getVariable("acceptWorker", Integer.class);
        reimbursementService.processReimbursement((String) map.get("dataId"), state, AppUtil.getUser().getId(), taskId, itemId, acceptWorker);

    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}
