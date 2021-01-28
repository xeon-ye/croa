package com.qinfei.qferp.flow.listener1.handover;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.employ.EmployeeConnect;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.service.employ.IEmployeeConnectService;
import com.qinfei.qferp.service.employ.IEmployeeService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IEmployConnect;
import com.qinfei.qferp.utils.IProcess;
import com.qinfei.qferp.utils.PrimaryKeyUtil;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @CalssName PersonnelTaskHandler
 * @Description 人事部审核
 * @Author xuxiong
 * @Date 2019/9/29 0029 15:10
 * @Version 1.0
 */
public class PersonnelTaskHandler implements ICommonTaskHandler, TaskListener {
    @Override
    public void setApproveUser(DelegateTask delegateTask, int state) {
// 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        String company = delegateTask.getVariable("companyCode", String.class); // 获取公司代码变量；
        // 如果审核人信息不完整，从数据库获取角色默认的用户信息；
        if (StringUtils.isEmpty(nextUser) || StringUtils.isEmpty(nextUserName) || StringUtils.isEmpty(nextUserDept)) {
            String[] datas;
            switch (state){
                // 审核被驳回；
                case IConst.STATE_REJECT:
                    delegateTask.setVariable("processState", IProcess.PROCESS_REJECT);
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("initiatorDept", Integer.class));
                    delegateTask.setVariable("acceptWorker", delegateTask.getVariable("initiatorWorker", Integer.class));
                    nextUser = delegateTask.getVariable("userId", String.class);
                    nextUserName = delegateTask.getVariable("userName", String.class);
                    break;
                case IConst.STATE_RS:
                    IUserService userService = SpringUtils.getBean("userService");
                    List<User> list=userService.listByTypeAndCompanyCode(IConst.ROLE_TYPE_RS,company, 0);
                    User user= list.get(0);
                    // datas = getApproveUserId(delegateTask, IConst.ROLE_TYPE_RS, IConst.ROLE_CODE_ZZ, company, false);
                    delegateTask.setVariable("acceptDept", user.getDeptId());
                    delegateTask.setVariable("acceptWorker", user.getId());
                    nextUser =user.getId().toString();
                    nextUserName = user.getName();
                    break;
                // 不存在；
                default:
                    break;
            }
        }else {
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
            state = IConst.STATE_RS;
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
        map.put("messageTypeName","员工交接");//消息子类类型
        map.put("type",19);//员工交接
        if(state == IConst.STATE_REJECT){
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
        Integer process = delegateTask.getVariable("process", Integer.class);
        process = process == null ? -1 : process;
        int empId = Integer.parseInt(delegateTask.getVariable("empId", String.class));
        if(process == IProcess.PROCESS_HANDOVER_LEAVE){
            IEmployeeService employeeService = SpringUtils.getBean("employeeService");
            employeeService.processConnect(empId, delegateTask.getVariable("code", String.class), state, taskId, itemId, IEmployConnect.CONNECT_LEAVE);
            String uniqueKey = PrimaryKeyUtil.getStringUniqueKey();// 更新查询码；
            delegateTask.setVariable("code", uniqueKey);
            delegateTask.setVariable("dynamicUrl", "/leaveConnectApprove?code=" + uniqueKey);  // 更新链接；
        }else if(process == IProcess.PROCESS_HANDOVER_TRANSFER){
            IEmployeeService employeeService = SpringUtils.getBean("employeeService");
            employeeService.processConnect(empId, delegateTask.getVariable("code", String.class), state, taskId, itemId, IEmployConnect.CONNECT_TRANSFER);
            String uniqueKey = PrimaryKeyUtil.getStringUniqueKey(); // 更新查询码；
            delegateTask.setVariable("code", uniqueKey);
            delegateTask.setVariable("dynamicUrl", "/transferConnectApprove?code=" + uniqueKey); // 更新链接；
        }
        IEmployeeConnectService connectService =SpringUtils.getBean("employeeConnectService");
        EmployeeConnect employeeConnect = new EmployeeConnect();
        employeeConnect.setConId(Integer.parseInt(delegateTask.getVariable("conId").toString()));
        IUserService userService = SpringUtils.getBean("userService");
        List<User> userList= userService.queryUserByDeptIdONLY(Integer.parseInt(delegateTask.getVariable("initiatorDept").toString()));//交接人所在部门id
        // 排除申请人；
        Integer userId = Integer.parseInt(delegateTask.getVariable("initiatorDept").toString());
        if (userId != null) {
            Iterator<User> iterator = userList.iterator();
            User tempUser;
            while (iterator.hasNext()) {
                tempUser = iterator.next();
                if (tempUser.getId().equals(userId)) {
                    iterator.remove();
                }
            }
        }
        employeeConnect.setConEmpId(userList.get(0).getId());
        employeeConnect.setConEmpName(userList.get(0).getName());
        connectService.saveOrUpdate(employeeConnect);

    }

    @Override
    public void notify(DelegateTask delegateTask) {
        //封装数据
        handleApproveData(delegateTask);
        //更新数据
        updateProcessData(delegateTask);
    }
}
