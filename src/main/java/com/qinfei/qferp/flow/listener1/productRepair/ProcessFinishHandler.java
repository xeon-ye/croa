package com.qinfei.qferp.flow.listener1.productRepair;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import com.qinfei.qferp.entity.inventory.ReceiveApply;
import com.qinfei.qferp.entity.inventory.ReceiveRepair;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.inventory.IApplyService;
import com.qinfei.qferp.service.inventory.IGoodsRecordService;
import com.qinfei.qferp.service.inventory.IGoodsService;
import com.qinfei.qferp.service.inventory.IReceiveRepairService;
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
 * @CalssName ProcessFinishHandler
 * @Description 审核完成
 * @Author dsg
 */
public class ProcessFinishHandler implements TaskListener, ICommonTaskHandler {

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
        Map<String,Object> map = getTaskParam(delegateTask);//获取基础数据
        Integer state = (Integer)map.get("state");
        String processName = (String) map.get(IProcess.PROCESS_NAME);
        map.put("messageTypeName","物品报修");//消息子类类型
        map.put("type",26);//物品报修
        if(state==IConst.STATE_REJECT){
            map.put("parentType",2);//提醒
            //驳回处理方法
            commonRejectHandle(delegateTask,(String)map.get("pictureAddress"),processName,(String)map.get("dataUrl"),map);
        }else {
            map.put("parentType",2);//提醒
            //审核完成处理方法
            commonFinishHandle((String)map.get("pictureAddress"),processName,(String)map.get("dataUrl"),map);
        }
        Integer itemId = commonSendMessage(delegateTask,map);//统一消息处理，返回新增的待办id
        // =================================================通知推送模块结束=================================================
        //流程当前的任务id
        String taskId = delegateTask.getId();

        //更新物品报修数据
        ReceiveRepair repair = new ReceiveRepair();
        Integer repairId = Integer.valueOf(map.get("dataId").toString());
        repair.setId(repairId);
        repair.setUpdateTime(new Date());
        repair.setItemId(itemId);
        repair.setTaskId(taskId);
        repair.setState(state);
        IReceiveRepairService repairService = SpringUtils.getBean("receiveRepairService");
        IGoodsRecordService goodsRecordService = SpringUtils.getBean("goodsRecordService");
        IGoodsService goodsService = SpringUtils.getBean("goodsService");
        repairService.processRepair(repair);
        //物品报修
        GoodsRecord record = goodsRecordService.getGoodsRecordById(1,repairId);
        record.setTaskId(taskId);
        record.setState(state);
        record.setUpdateTime(new Date());
        record.setUpdateUserId(AppUtil.getUser().getId());
        goodsRecordService.editGoodsRecord(record);
        Goods goods=goodsService.getGoodsById(record.getInventoryId());
        if(!ObjectUtils.isEmpty(goods)){
            //如果用户id不为空则修改成出库状态
            Integer userId=goods.getUserId();
            if(userId==null){
                //产品库存报修完直接变成库存
                goodsService.editGoodsState(0,record.getInventoryId());
            }
        }
    }
}
