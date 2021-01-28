package com.qinfei.qferp.flow.listener1.outgo;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.fee.OutgoMapper;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.fee.IOutgoService;
import com.qinfei.qferp.service.impl.fee.OutgoService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KJProcessCompleteHandler implements TaskListener,ICommonTaskHandler {

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
            if (state == IConst.STATE_REJECT) {
                delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                nextUser = delegateTask.getVariable("userId", String.class);
                nextUserName = delegateTask.getVariable("userName", String.class);
                IOutgoService outgoService = SpringUtils.getBean("outgoService");
                Integer dataId= Integer.parseInt(delegateTask.getVariable("dataId").toString());
                Outgo outgo = outgoService.getById(dataId);
                outgoService.CWReturn(outgo);

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
            map.put("messageTypeName", "请款");//消息子类类型
            map.put("type", 3);//请款
            if (state == IConst.STATE_REJECT) {
                //消息分类
                map.put("parentType", 3);//通知
                commonRejectHandle(delegateTask, (String) map.get("pictureAddress"), (String) map.get(IProcess.PROCESS_NAME), (String) map.get("dataUrl"), map); //驳回处理方法
            } else {
                //消息分类
                map.put("parentType", 2);//提醒
                commonFinishHandle((String) map.get("pictureAddress"), (String) map.get(IProcess.PROCESS_NAME), (String) map.get("dataUrl"), map); //审核完成处理方法
            }
            Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
            // 流程当前的任务ID；
            String taskId = delegateTask.getId();
            IOutgoService outgoService = SpringUtils.getBean("outgoService");
            Integer dataId= Integer.parseInt(map.get("dataId").toString());
            Outgo outgo = outgoService.getById(dataId);
            outgo.setUpdateUserId(AppUtil.getUser().getId());
            if(!delegateTask.getVariable("gateCheckE",Boolean.class) &&  state!=IConst.STATE_REJECT){
                outgo.setState(IConst.STATE_FINISH);
                try {
                    OutgoMapper outgoMapper= SpringUtils.getBean("outgoMapper");
                    //处理稿件表的状态
                    List<Integer> ids = outgoMapper.selectArticleId(dataId);
                    if (ids == null || ids.size() == 0) {
                        throw new QinFeiException(1002, "没有获取到请款中的稿件。请款id=" +dataId);
                    }
                    Map<String, Object> temp = new HashMap<>();
                    temp.put("list", ids);
                    temp.put("state", IConst.FEE_STATE_FINISH);
                    outgoMapper.changeOutgoState(temp);
                    //判断流程结束且需绑定账户  且该账户未被增加
                    if(outgo.getAccountBinding() == 1 && outgo.getAccountId() == null){
                        IAccountService accountService = SpringUtils.getBean("accountService");

                        Account account = new Account();
                        account.setType(2);
                        account.setCompanyId(outgo.getSupplierId());
                        account.setCompanyName(outgo.getSupplierName());
                        account.setBankNo(outgo.getAccountBankNo());
                        account.setBankName(outgo.getAccountBankName());
                        account.setOwner(outgo.getAccountName());
                        account.setState(1);

                        account.setCompanyCode(outgo.getCompanyCode());
                        account.setContactor(outgo.getSupplierContactor());
                        account.setCreator(outgo.getCreator());
                        account.setCreateTime(new Date());
                        //如果是 企业供应商则name 为供应商 名称 ， 个提供应商  name为 联系人 名称 0-企业供应商、1-个体供应商
                        Integer supplierType = accountService.supplierType(outgo.getSupplierId());
                        if (supplierType == 0){
                            account.setName(outgo.getSupplierName());
                            account.setAccountType("B2B");
                        }else {
                            account.setName(outgo.getSupplierContactor());
                            account.setAccountType("B2C");
                        }
                        accountService.outgoAccountAdd(account);
                    }
                } catch (Exception e) {
                    throw new QinFeiException(1002, "处理关联的稿件状态出错，请款id=" + dataId);
                }
            }
            // 更新流程当前的任务ID；
            outgo.setTaskId(taskId);
            // 更新待办事项的ID；
            outgo.setItemId(itemId);
            outgoService.update(outgo);

    }
}
