package com.qinfei.qferp.flow.listener1.standardized;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.standardized.StandardizedCompany;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.standardized.IStandardizedCompanyService;
import com.qinfei.qferp.service.sys.IUserService;
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
 * 客户管理员审核
 */
public class UserAdminTaskHandler implements ICommonTaskHandler, TaskListener {
    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
        // 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        String company = delegateTask.getVariable("company", String.class);
        if (StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)) {
            String[] datas;
            switch (state) {
                //审核驳回
                case IConst.PROTECT_STATE_FAIL:
                    delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    nextUser = delegateTask.getVariable("userId", String.class);
                    nextUserName = delegateTask.getVariable("userName", String.class);
                    break;
                case IConst.STATE_KHGLY:
                    // 客户管理员审核
                    IUserService userService = SpringUtils.getBean("userService");
                    List<User> userList = userService.listByTypeAndCodeJT(IConst.ROLE_TYPE_XT, IConst.ROLE_CODE_ZY);
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
        } else {
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
    public void handleApproveData(DelegateTask delegateTask) {
        boolean agree = getOpinion(delegateTask);
        int state;
        if (agree) {
            state = IConst.STATE_KHGLY;
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
        map.put("messageTypeName", "标准化公司申请审批");//消息子类类型
        map.put("type", IProcess.PROCESS_STANDARDIZED_COMPANY);
        if (state == IConst.STATE_REJECT) {
            //消息分类
            map.put("parentType", 2);//提醒
            commonRejectHandle(delegateTask, (String) map.get("pictureAddress"), (String) map.get(IProcess.PROCESS_NAME), (String) map.get("dataUrl"), map); //驳回处理方法
        } else {
            map.put("parentType", 1);//待办
            commonDefaultHandle(delegateTask, (String) map.get("pictureAddress"), (String) map.get(IProcess.PROCESS_NAME), (String) map.get("dataUrl"), map); //默认其他情况处理方法
        }
        Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
        // =================================================通知推送模块结束=================================================
        // 流程当前的任务ID；
        String taskId = delegateTask.getId();
        //更新数据
        StandardizedCompany standardizedCompany = new StandardizedCompany();
        standardizedCompany.setId(Integer.parseInt((String) map.get("dataId")));
        standardizedCompany.setState(state);
        standardizedCompany.setUpdateUserId(AppUtil.getUser().getId());
        standardizedCompany.setTaskId(taskId);
        standardizedCompany.setItemId(itemId);
        IStandardizedCompanyService standardizedCompanyService = SpringUtils.getBean("standardizedCompanyService");
        standardizedCompanyService.updateStandardizedCompany(standardizedCompany, AppUtil.getUser());
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}
