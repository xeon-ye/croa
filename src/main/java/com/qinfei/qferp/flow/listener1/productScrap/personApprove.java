package com.qinfei.qferp.flow.listener1.productScrap;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import com.qinfei.qferp.entity.inventory.ReceiveRepair;
import com.qinfei.qferp.entity.inventory.ReceiveScrap;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.inventory.IGoodsRecordService;
import com.qinfei.qferp.service.inventory.IReceiveRepairService;
import com.qinfei.qferp.service.inventory.IReceiveScrapService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * @CalssName personApprove
 * @Description 个人报修确认审核
 * @Author tsf
 */
public class personApprove implements TaskListener, ICommonTaskHandler {

    @Override
    public void notify(DelegateTask delegateTask) {
        //处理数据
       handleApproveData(delegateTask);
       //修改数据
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
                //个人确认
                case IConst.STATE_GR:
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    nextUser = delegateTask.getVariable("userId", String.class);
                    nextUserName = delegateTask.getVariable("userName", String.class);
                    break;
                // 审核被驳回；
                case IConst.STATE_REJECT:
                    delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    nextUser = delegateTask.getVariable("userId", String.class);
                    nextUserName = delegateTask.getVariable("userName", String.class);
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
            state = IConst.STATE_GR;
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
        Map<String,Object> map = getTaskParam(delegateTask);//获取基础数据
        Integer state = (Integer)map.get("state");
        String processName = (String) map.get(IProcess.PROCESS_NAME);
        map.put("messageTypeName","物品报废");//消息子类类型
        map.put("type",27);//物品报废
        if(state==IConst.STATE_REJECT){
            map.put("parentType",3);//通知
            //驳回处理方法
            commonRejectHandle(delegateTask,(String)map.get("pictureAddress"),processName,(String)map.get("dataUrl"),map);
        }else {
            map.put("parentType",1);//待办
            //审核完成处理方法
            commonDefaultHandle(delegateTask,(String)map.get("pictureAddress"),processName,(String)map.get("dataUrl"),map);
        }
        Integer itemId = commonSendMessage(delegateTask,map);//统一消息处理，返回新增的待办id
        // =================================================通知推送模块结束=================================================
        //流程当前的任务id
        String taskId = delegateTask.getId();

        //更新物品报废数据
        ReceiveScrap scrap = new ReceiveScrap();
        Integer scrapId=Integer.valueOf(map.get("dataId").toString());
        scrap.setId(scrapId);
        scrap.setUpdateTime(new Date());
        scrap.setItemId(itemId);
        scrap.setTaskId(taskId);
        scrap.setState(state);
        IReceiveScrapService scrapService = SpringUtils.getBean("receiveScrapService");
        scrapService.processScrap(scrap);
        IGoodsRecordService goodsRecordService = SpringUtils.getBean("goodsRecordService");
        //type=2物品报废
        GoodsRecord record = goodsRecordService.getGoodsRecordById(2,scrapId);
        if(!ObjectUtils.isEmpty(record)){
            record.setTaskId(taskId);
            record.setState(state);//同步报废表状态
            record.setUpdateTime(new Date());
            record.setUpdateUserId(AppUtil.getUser().getId());
            //修改物品操作记录
            goodsRecordService.editGoodsRecord(record);
        }
    }
}
