package com.qinfei.qferp.flow.listener1.refund;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.fee.Refund;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.fee.IRefundService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName BursarTaskHandler
 * @Description 会计确认出款
 * @Author xuxiong
 * @Date 2019/9/29 0029 14:30
 * @Version 1.0
 */
public class BursarTaskHandler implements ICommonTaskHandler, TaskListener {
    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
        // 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        String company = delegateTask.getVariable("company", String.class);  // 获取公司代码变量；
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
                // 会计审核；
                case IConst.STATE_KJ:
                    datas = getApproveUserId(delegateTask, IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_KJ, company, true);
                    nextUser = datas[0];
                    nextUserName = datas[1];
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
            state = IConst.STATE_KJ;
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
        map.put("messageTypeName","退款");//消息子类类型
        map.put("type",4);//退款
        if(state == IConst.STATE_REJECT){
            //消息分类
            map.put("parentType",2);//提醒
            commonRejectHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //驳回处理方法
        }else {
            // 发送信息需要的信息，通知流程发起人；
            //消息标题
            String title = (String)map.get("messageTitle");
            //消息子类类型名称
            String messageTypeName = (String)map.get("messageTypeName");
            //消息发起人
            String createName = (String)map.get("messageUserName");
            String processName = title!=null?(String)map.get(IProcess.PROCESS_NAME)+"["+title+"]":(String)map.get(IProcess.PROCESS_NAME);
            String processName2 = (createName!=null && title!=null)?(String)map.get(IProcess.PROCESS_NAME)+":"+createName+"发起的["+title+"]":(String)map.get(IProcess.PROCESS_NAME);
            map.put("parentType",1);//待办
            map.put("newPic", map.get("pictureAddress"));
            map.put("newContent", String.format("[%s]您提交的%s已审核通过,下一审核节点：%s,审核人：%s。", messageTypeName, processName,delegateTask.getName(),delegateTask.getOwner()));

            // ===============================通知出纳===============================
            map.put("pic", (String)map.get("pictureAddress"));
            map.put("content", String.format("[%s]您有新的%s待审核。",messageTypeName,processName2));
            // 增加待办事项需要的信息；
            map.put("itemName", String.format("%s - 出账确认等待处理", delegateTask.getVariable("dataName")));
            map.put("itemContent", String.format("[%s]您有新的%s需要处理", messageTypeName,processName));
            // 增加待办事项需要的信息；
            map.put("workType", map.get(IProcess.PROCESS_NAME));
            // 获取出纳处理的链接页面；
            String accUrl = delegateTask.getVariable(IProcess.PROCESS_ACCIYBR_URL, String.class);
            if (StringUtils.isEmpty(accUrl)) {
                accUrl = (String) map.get("dataUrl");
            }
            // 处理中的待办事项跳转到流程审核页面；
            map.put("transactionAddress", accUrl);
            // 处理完成的待办事项跳转到关联数据的列表页面；
            map.put("finishAddress", map.get("dataUrl"));
            // 代办事项的紧急程度；
            map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
            // 获取出纳用户信息；
            IUserService userC = SpringUtils.getBean("userService");
            // 开票出账找财务部长确认
            String companyCodeCW = delegateTask.getVariable("companyCode", String.class);
            // 获取公司代码变量；
            String company1 = delegateTask.getVariable("company", String.class);
            String tempCompanyCode = StringUtils.isEmpty(company1) ? companyCodeCW : company1;
            tempCompanyCode = StringUtils.isEmpty(tempCompanyCode) ? IConst.COMPANY_CODE_XH : tempCompanyCode;
            List<User> list = userC.listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_KJ, tempCompanyCode);
            if(CollectionUtils.isEmpty(list)){
                throw new QinFeiException(1001, "会计不存在！");
            }
            User chun = list.get(0);
            delegateTask.setVariable("acceptDept", chun.getDeptId());
            delegateTask.setVariable("acceptWorker", chun.getId());
        }
        Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
        // =================================================通知推送模块结束=================================================
        // 流程当前的任务ID；
        String taskId = delegateTask.getId();
        //更新数据
        Refund refund = new Refund();
        refund.setId(Integer.parseInt((String) map.get("dataId")));
        refund.setState(state);
        refund.setUpdateUserId(AppUtil.getUser().getId());
        // 更新流程当前的任务ID；
        refund.setTaskId(taskId);
        // 更新待办事项的ID；
        refund.setItemId(itemId);
        IRefundService refundService = SpringUtils.getBean("refundService");
        refundService.update(refund);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}
