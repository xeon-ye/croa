package com.qinfei.qferp.flow.listener1.royalty;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.fee.Commission;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.fee.ICommissionService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
        String company = delegateTask.getVariable("company", String.class); // 获取公司代码变量；
        // 如果审核人信息不完整，从数据库获取角色默认的用户信息；
        if (StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)) {
            String[] datas;
            switch (state) {
                // 审核被驳回；
                case IConst.STATE_REJECT:
                    delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    nextUser = delegateTask.getVariable("userId", String.class);
                    nextUserName = delegateTask.getVariable("userName", String.class);
                    break;
                case IConst.STATE_PASS:
                    delegateTask.setVariable("processState", IProcess.PROCESS_FINISHED);
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    break;
                // 不存在；
                default:
                    break;
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
            // state = IConst.STATE_FINISH;
            state = IConst.STATE_PASS;
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

        if(state == IConst.STATE_REJECT){
            commonRejectHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //驳回处理方法
        }else{
            // 发送信息需要的信息，通知流程发起人；
            map.put("newPic", map.get("pictureAddress"));
            map.put("newContent", String.format("您提交的%s已审核通过。", map.get(IProcess.PROCESS_NAME)));

            // ===============================通知出纳===============================
            // 增加待办事项需要的信息；
            map.put("itemName", String.format("%s - 等待处理", delegateTask.getVariable("dataName")));
            map.put("itemContent", String.format("您有新的%s需要处理", map.get(IProcess.PROCESS_NAME)));
            // 增加待办事项需要的信息；
            map.put("workType", map.get(IProcess.PROCESS_NAME));
            // 获取出纳处理的链接页面；
            String cashierUrl = delegateTask.getVariable(IProcess.PROCESS_PASS_URL, String.class);
            if (StringUtils.isEmpty(cashierUrl)) {
                cashierUrl = (String) map.get("dataUrl");
            }
            // 处理中的待办事项跳转到流程审核页面；
            map.put("transactionAddress", cashierUrl);
            // 处理完成的待办事项跳转到关联数据的列表页面；
            map.put("finishAddress", map.get("dataUrl"));
            // 代办事项的紧急程度；
            map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
            // 获取出纳用户信息；
            IUserService userService = SpringUtils.getBean("userService");
            // 开票找财务部长确认
            String companyCode = delegateTask.getVariable("companyCode", String.class);
            // 获取公司代码变量；
            String company = delegateTask.getVariable("company", String.class);
            String tempCompanyCode = StringUtils.isEmpty(company) ? companyCode : company;
            tempCompanyCode = StringUtils.isEmpty(tempCompanyCode) ? IConst.COMPANY_CODE_XH : tempCompanyCode;
            List<User> list = userService.listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_CN, tempCompanyCode);
            if(CollectionUtils.isEmpty(list)){
                throw new QinFeiException(1001, "财务出纳不存在！");
            }
            User user = list.get(0);
            delegateTask.setVariable("acceptDept", user.getDeptId());
            delegateTask.setVariable("acceptWorker", user.getId());
        }

        Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
        // =================================================通知推送模块结束=================================================
        // 流程当前的任务ID；
        String taskId = delegateTask.getId();
        //更新数据
        Commission commission = new Commission();
        commission.setId(Integer.parseInt((String) map.get("dataId")));
        commission.setState(state);
        // 更新流程当前的任务ID；
        commission.setTaskId(taskId);
        // 更新待办事项的ID；
        commission.setItemId(itemId);

        ICommissionService commissionService = SpringUtils.getBean("commissionService");
        commissionService.update(commission);
    }
}
