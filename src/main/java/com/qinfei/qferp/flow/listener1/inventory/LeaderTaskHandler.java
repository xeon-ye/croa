package com.qinfei.qferp.flow.listener1.inventory;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.inventory.Purchase;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.inventory.IPurchaseService;
import com.qinfei.qferp.service.inventory.IPurchaseSupplierService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * @CalssName LeaderTaskHandler
 * @Description 部门审核
 * @Author tsf
 * @Date 2020/3/14 17:04
 */
public class LeaderTaskHandler implements TaskListener, ICommonTaskHandler {

    @Override
    public void notify(DelegateTask delegateTask) {
       //封装数据
       handleApproveData(delegateTask);
       //更新数据
       updateProcessData(delegateTask);
    }

    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
       //判断前端是否有审核人信息传递过来
       Integer nextUserId = delegateTask.getVariable("nextUser",Integer.class);
       String nextUser = nextUserId==null?null:nextUserId.toString();
       String nextUserName = delegateTask.getVariable("nextUserName",String.class);
       Integer nextUserDept = delegateTask.getVariable("nextUserDept",Integer.class);
       // 如果审核人信息不完整，从数据库获取角色默认的用户信息；
        if(StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)){
            switch (state){
                //审核被驳回
                case IConst.STATE_REJECT:
                    delegateTask.setVariable("ProcessState", IProcess.PROCESS_REJECT);
                    delegateTask.setVariable("acceptDept",delegateTask.getVariable("initiatorDept",Integer.class));
                    delegateTask.setVariable("acceptWorker",delegateTask.getVariable("initiatorWorker",Integer.class));
                    nextUser = delegateTask.getVariable("userId",String.class);
                    nextUserName = delegateTask.getVariable("userName",String.class);
                    break;
                case IConst.STATE_BZ:
                    UserMapper userMapper = SpringUtils.getBean("userMapper");
                    //获取流程申请人，判断申请人是否是部长，如果是则直属领导审核则由分管领导
                    User user = userMapper.getById(Integer.valueOf(delegateTask.getVariable("userId").toString()));
                    Integer deptId = user.getDeptId();
                    DeptMapper deptMapper = SpringUtils.getBean("deptMapper");
                    Dept dept = deptMapper.getById(deptId);
                    if (dept != null) {
                        //如果申请人为部门以上的部门，并且不是部门负责人，则部门负责人，否则分管领导审核
                        if(dept.getLevel() <= 3 && !user.getId().equals(dept.getMgrId())){
                            Integer userId = dept.getMgrId();
                            delegateTask.setVariable("acceptDept", deptId);
                            delegateTask.setVariable("acceptWorker", userId);
                            nextUser = userId == null ? null : userId.toString();
                            nextUserName = dept.getMgrName();
                        }else{
                            Integer userId = dept.getMgrLeaderId(); //获取分管领导ID
                            User mgrLeaderUser = userMapper.getById(userId);
                            delegateTask.setVariable("acceptDept", mgrLeaderUser.getDeptId());
                            delegateTask.setVariable("acceptWorker", userId);
                            nextUser = userId == null ? null : userId.toString();
                            nextUserName = mgrLeaderUser.getName();
                        }
                    }
                    break;
                //不存在
                default:
                    break;
            }
        }else{
            delegateTask.setVariable("acceptDept",nextUserDept);
            delegateTask.setVariable("acceptWorker",nextUserId);

            //试用完毕后清空；
            delegateTask.removeVariable("nextUser");
            delegateTask.removeVariable("nextUserName");
            delegateTask.removeVariable("nextUserDept");
        }
        //设置审核人
        delegateTask.setAssignee(nextUser);
        delegateTask.setOwner(nextUserName);
        // 更新审核人到数据库中；
        delegateTask.setVariable("approveUser",nextUser);
        delegateTask.setVariable("approveUserName",nextUserName);
    }

    @Override
    public void handleApproveData(DelegateTask delegateTask) {
        boolean agree = getOpinion(delegateTask);
        int state;
        if(agree){
            state = IConst.STATE_BZ;
        }else{
            state = IConst.STATE_REJECT;
        }
        //更新到数据库中；
        delegateTask.setVariable("state",state);
        //设置审核人；
        setApproveUser(delegateTask,state);
    }

    @Override
    public void updateProcessData(DelegateTask delegateTask) {
        Map<String,Object> map = getTaskParam(delegateTask);//获取基础数据
        Integer state = (Integer)map.get("state");
        String processName = (String) map.get(IProcess.PROCESS_NAME);
        map.put("messageTypeName","物品采购");//消息子类类型
        map.put("type",23);//物品采购
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
        //更新物品采购数据
        Purchase purchase = new Purchase();
        purchase.setId(Integer.valueOf(map.get("dataId").toString()));
        purchase.setState(state);
        purchase.setUpdateUserId(AppUtil.getUser().getId());
        purchase.setUpdateTime(new Date());
        purchase.setTaskId(taskId);//更新流程任务id
        purchase.setItemId(itemId);//更新待办事项id
        IPurchaseService purchaseService = SpringUtils.getBean("purchaseService");
        purchaseService.processPurchase(purchase);
    }
}
