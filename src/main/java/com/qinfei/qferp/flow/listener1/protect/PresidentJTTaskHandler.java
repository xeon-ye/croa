package com.qinfei.qferp.flow.listener1.protect;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.biz.Project;
import com.qinfei.qferp.entity.crm.CrmCompanyProtect;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.biz.IProjectService;
import com.qinfei.qferp.service.crm.ICrmCompanyService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 总经办监听
 */
public class PresidentJTTaskHandler implements ICommonTaskHandler, TaskListener {
    @Override
    public void setApproveUser(DelegateTask delegateTask,int state){
        // 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        String company = delegateTask.getVariable("company", String.class);
        if (StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)){
            String[] datas;
            switch (state){
                //审核驳回
                case IConst.PROTECT_STATE_FAIL:
                    delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    nextUser = delegateTask.getVariable("userId", String.class);
                    nextUserName = delegateTask.getVariable("userName", String.class);
                    break;
                case IConst.PROTECT_STATE_CEOJT:
                    IUserService userService = SpringUtils.getBean("userService");
                    List<User> userList = userService.listByTypeAndCode(IConst.ROLE_TYPE_JT, IConst.ROLE_CODE_ZC, IConst.COMPANY_CODE_JT);
                    if(CollectionUtils.isEmpty(userList)){
                        throw new QinFeiException(1001, "没有下一个审批人，请联系管理员！");
                    }
                    User user = userList.get(0); //默认取第一个
                    // 获取用户信息；
                    if (user != null) {
                        delegateTask.setVariable("acceptDept", user.getDeptId());
                        delegateTask.setVariable("acceptWorker", user.getId());
                        nextUser = user.getId().toString();
                        nextUserName = user.getName();
                    }
                    break;
                default:
                    break;
            }
        }else{
            delegateTask.setVariable("acceptDept", nextUserDept);
            delegateTask.setVariable("acceptWorker", nextUserId);

            // 使用完毕后清空；
            delegateTask.removeVariable("nextUser");
            delegateTask.removeVariable("nextUserName");
            delegateTask.removeVariable("nextUserDept");

        }
        delegateTask.setAssignee(nextUser);
        delegateTask.setOwner(nextUserName);
        delegateTask.setVariable("approveUser", nextUser);
        delegateTask.setVariable("approveUserName", nextUserName);
    }

    @Override
    public void updateProcessData(DelegateTask delegateTask) {
        Map<String, Object> map = getTaskParam(delegateTask); //获取基础数据
        Integer state = (Integer) map.get("state");
        map.put("messageTypeName","客户保护流程");//消息子类类型
        map.put("type", IProcess.PROCESS_PROTECT);
        if(state == IConst.PROTECT_STATE_FAIL){
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
        ICrmCompanyService crmCompanyService=SpringUtils.getBean("crmCompanyService");
        Integer protectId = Integer.parseInt((String)map.get("dataId"));
        CrmCompanyProtect protect = crmCompanyService.getCompanyIdByProtectId(protectId);
        protect.setState(state);
        protect.setUpdateUserId(AppUtil.getUser().getId());
        protect.setTaskId(taskId);
        protect.setItemId(itemId);
        crmCompanyService.updateProtect(protect);

        if(state == IConst.PROTECT_STATE_FAIL){
            //更新公司表信息
            Map param = new HashMap();
            param.put("alterFlag", IConst.COMMON_FLAG_FALSE);//不用保存公司表历史记录了
            param.put("id", protect.getCompanyId());
            param.put("auditFlag", IConst.COMMON_FLAG_FALSE);//把审核状态还原
            crmCompanyService.updateCompany(param);
        }
    }

    @Override
    public void handleApproveData(DelegateTask delegateTask) {
        boolean agree = getOpinion(delegateTask);
        int state;
        if (agree) {
            state = IConst.PROTECT_STATE_CEOJT;
        } else {
            state = IConst.PROTECT_STATE_FAIL;
        }

        // 更新到数据库中；
        delegateTask.setVariable("state", state);
        // 设置审核人；
        setApproveUser(delegateTask, state);
    }




    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}
