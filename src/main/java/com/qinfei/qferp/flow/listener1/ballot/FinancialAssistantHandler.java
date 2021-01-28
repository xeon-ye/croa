package com.qinfei.qferp.flow.listener1.ballot;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.fee.Invoice;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.fee.IInvoiceService;
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
 * @CalssName FinancialAssistantHandler
 * @Description 财务助理开票
 * @Author xuxiong
 * @Date 2019/9/27 0027 17:27
 * @Version 1.0
 */
public class FinancialAssistantHandler implements TaskListener, ICommonTaskHandler {
    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
        // 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        String company = delegateTask.getVariable("company", String.class); // 获取公司代码变量；


//        if (state==11 && nextUserId==null){
//            throw new QinFeiException(1002, "开票流程请到详情页审批！");
//        }
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
                // 财务助理审核；
                case IConst.STATE_CWKP:
                    IInvoiceService invoiceService = SpringUtils.getBean("invoiceService");
                    String taxType= String.valueOf(delegateTask.getVariable("taxType"));
                    if (StringUtils.isEmpty(taxType)){
                        throw new QinFeiException(1002, "抬头未设置，请联系财务！");
                    }
                    List<User> assistantUser= invoiceService.getTaxType(taxType);
                    if (assistantUser.size()==0){
                        throw new QinFeiException(1002, "未配置助理，请联系财务！");
                    }
                    nextUser = assistantUser.get(0).getId().toString();
                    nextUserName = assistantUser.get(0).getName();
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
            state = IConst.STATE_CWKP;
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
        map.put("messageTypeName","开票");//消息子类类型
        map.put("type",1);//开票
        if(state == IConst.STATE_REJECT){
            //消息分类
            map.put("parentType",3);//通知
            commonRejectHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //驳回处理方法
        }else {
            // 发送信息需要的信息，通知流程发起人；
            //消息子类类型名称
            String messageTypeName = (String)map.get("messageTypeName");
            //消息标题
            String title = (String)map.get("messageTitle");
            //消息发起人
            String createName = (String)map.get("messageUserName");
            //消息分类
            map.put("parentType",1);//待办
            map.put("newPic", map.get("pictureAddress"));
            map.put("newContent", String.format("[%s]您提交的%s已审核通过,下一审核节点：%s,审核人：%s。",messageTypeName, map.get(IProcess.PROCESS_NAME)+"["+title+"]",delegateTask.getName(),delegateTask.getOwner()));

            // ===============================通知助理===============================
            // 增加待办事项需要的信息；
            map.put("pic", (String)map.get("pictureAddress"));
            map.put("content", String.format("[%s]您有新的%s待审核。",messageTypeName,(String)map.get(IProcess.PROCESS_NAME)+":"+createName+"发起的["+title+"]"));
            map.put("itemName", String.format("%s - 开票等待处理", delegateTask.getVariable("dataName")));
            map.put("itemContent", String.format("[%s]您有新的%s开票需要处理", messageTypeName,map.get(IProcess.PROCESS_NAME)+"["+title+"]"));
            // 增加待办事项需要的信息；
            map.put("workType", map.get(IProcess.PROCESS_NAME));
            // 获取出纳处理的链接页面；
            String assUrl = delegateTask.getVariable(IProcess.PROCESS_ASSISTANT_URL, String.class);
            if (StringUtils.isEmpty(assUrl)) {
                assUrl = (String) map.get("dataUrl");
            }
            // 处理中的待办事项跳转到流程审核页面；
            map.put("transactionAddress", assUrl);
            // 处理完成的待办事项跳转到关联数据的列表页面；
            map.put("finishAddress", map.get("dataUrl"));
            // 代办事项的紧急程度；
            map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
            // 获取出纳用户信息；
            IUserService userZ = SpringUtils.getBean("userService");

            // 开票出账找财务部长确认
            String companyCodeC1 = delegateTask.getVariable("companyCode", String.class);
            // 获取公司代码变量；
            String company2 = delegateTask.getVariable("company", String.class);
            String tempCompanyCode = StringUtils.isEmpty(company2) ? companyCodeC1 : company2;
            tempCompanyCode = StringUtils.isEmpty(tempCompanyCode) ? IConst.COMPANY_CODE_XH : tempCompanyCode;
            if(delegateTask.getAssignee()==null){
                List<User> list = userZ.listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_ZL, tempCompanyCode);
                if(CollectionUtils.isEmpty(list)){
                    throw new QinFeiException(1001, "财务助理不存在！");
                }
                User zhul = list.get(0);
                delegateTask.setVariable("acceptDept", zhul.getDeptId());
                delegateTask.setVariable("acceptWorker", zhul.getId());
            }else {
                UserMapper userMapper = SpringUtils.getBean("userMapper");
                User user = userMapper.getById(Integer.valueOf(delegateTask.getAssignee()));
                delegateTask.setVariable("acceptDept",user.getDeptId());
                delegateTask.setVariable("acceptWorker", user.getId());
            }

        }
        Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
        // =================================================通知推送模块结束=================================================
        // 流程当前的任务ID；
        String taskId = delegateTask.getId();
        //更新开票记录表数据
        Invoice invoice = new Invoice();
        invoice.setId(Integer.parseInt((String)map.get("dataId")));
        invoice.setState(state);
        // 更新流程当前的任务ID；
        invoice.setTaskId(taskId);
        // 更新待办事项的ID；
        invoice.setItemId(itemId);
        IInvoiceService invoiceService = SpringUtils.getBean("invoiceService");
        invoiceService.update(invoice);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}
