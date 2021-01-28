package com.qinfei.qferp.flow.listener1.accountsMess;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.accountsMess.AccountsMess;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.accountsMess.IAccountsMessService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 *  业务总监审核
 */
public class SalesDirectorHeadTaskHandler implements TaskListener , ICommonTaskHandler {
    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        String company = delegateTask.getVariable("company", String.class);
        if (StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)) {
            try {
                switch (state) {
                    case IConst.STATE_REJECT:
                        delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                        delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                        delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                        nextUser = delegateTask.getVariable("userId", String.class);
                        nextUserName = delegateTask.getVariable("userName", String.class);
                        break;
                    case IConst.STATE_ZJ:
                        IUserService userService = SpringUtils.getBean("userService");
                        User user = userService.getZJInfo(company, delegateTask.getVariable("userId", String.class));
                        if (user != null) {
                            delegateTask.setVariable("acceptDept", user.getDeptId());
                            delegateTask.setVariable("acceptWorker", user.getId());
                            nextUser = user.getId().toString();
                            nextUserName = user.getName();
                        } else {
                            nextUser = null;
                            nextUserName = null;
                        }
                        break;
                    default:
                        break;
                }
            } catch (QinFeiException e) {
                throw new QinFeiException(e.getCode(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                throw new QinFeiException(1001, "未获取到下一审核人，请联系管理员");
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
            state = IConst.STATE_ZJ;
        } else {
            state = IConst.STATE_REJECT;
        }

        // 更新到数据库中；
        delegateTask.setVariable("state", state);
        // 设置审核人；
        setApproveUser(delegateTask, state);

    }

    @Override
    public void updateProcessData(DelegateTask delegateTask){
        Map<String,Object> map = getTaskParam(delegateTask);
        Integer state = (Integer)map.get("state");
        map.put("messageTypeName","烂账申请");
        map.put("type",29);
        if(state == IConst.STATE_REJECT){
            map.put("parentType",3);
            commonRejectHandle(delegateTask,(String)map.get("pictureAddress"),(String)map.get(IProcess.PROCESS_NAME),(String)map.get("dataUrl"),map);
        }else {
            map.put("parentType",1);
            commonDefaultHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //默认其他情况处理方法

        }

        Integer itemId = commonSendMessage(delegateTask,map);
        String taskId = delegateTask.getId();
        User user = AppUtil.getUser();
        IAccountsMessService accountsMessService = SpringUtils.getBean("accountsMessService");
        AccountsMess accountsMess = new AccountsMess();
        accountsMess.setItemId(itemId);
        accountsMess.setTaskId(taskId);
        accountsMess.setState(state);
        accountsMess.setUpdateUser(user.getId());
        accountsMess.setUpdateTime(new Date());
        accountsMess.setId(Integer.parseInt(map.get("dataId").toString()));
        accountsMessService.updateMess(accountsMess);



    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}