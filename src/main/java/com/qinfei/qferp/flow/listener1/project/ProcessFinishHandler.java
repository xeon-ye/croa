package com.qinfei.qferp.flow.listener1.project;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.biz.Project;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.biz.IProjectService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 流程审核完成
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
        map.put("messageTypeName","项目管理审批");//消息子类类型
        map.put("type",2);//借款
        if(state == IConst.STATE_REJECT){
            //消息分类
            map.put("parentType",2);//提醒
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
        //更新数据
        User user = AppUtil.getUser();
        IProjectService projectService= SpringUtils.getBean("projectService");
        Project project = projectService.getById(Integer.parseInt((String)map.get("dataId")));
        project.setState(state);
        project.setUpdateUserId(user.getId());
        project.setTaskId(taskId);
        project.setItemId(itemId);

        projectService.update(project);

        UserService userService = SpringUtils.getBean("userService");
        List<User> cwbzList = userService.queryCWBZInfo(user.getCompanyCode()) ;
        List<User> accountList = userService.queryAccountingInfo(user.getCompanyCode()) ;
        accountList.addAll(cwbzList);
        List<Items> list = new ArrayList<>();
        if(accountList!=null && accountList.size()>0){
            ItemsService itemsService = SpringUtils.getBean("itemsService");
            for(User receiver:accountList){
                Items items = new Items();
                items.setItemName(project.getName() + "-项目管理确认抄送");
                items.setItemContent("您有新的项目管理确认抄送需要处理");
                items.setWorkType("项目管理确认抄送");
                items.setInitiatorWorker(user.getId());
                items.setInitiatorDept(user.getDeptId());
                items.setStartTime(new Date());
                Calendar ca = Calendar.getInstance();
                ca.add(Calendar.DATE, 3);// 增加的天数3，
                items.setEndTime(ca.getTime());
                items.setFinishAddress("/biz/project_list?flag=0&id=" + project.getId());
                items.setAcceptWorker(receiver.getId());
                items.setAcceptDept(receiver.getDeptId());
                items.setTransactionState(Const.ITEM_W);
                itemsService.addItems(items);
                list.add(items);
            }

            for(Items items:list){
                items.setTransactionAddress("/biz/project_list?flag=3&id=" + project.getId()+"&itemId="+items.getId());
            }
            itemsService.updateItemsTransactionAddress(list);
        }
    }
}
