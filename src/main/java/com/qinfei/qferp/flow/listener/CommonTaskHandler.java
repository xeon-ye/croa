package com.qinfei.qferp.flow.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.fee.*;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.service.employ.IEmployEntryService;
import com.qinfei.qferp.service.employ.IEmployeeService;
import com.qinfei.qferp.service.fee.*;
import com.qinfei.qferp.service.performance.IPerformanceScoreService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.*;

import lombok.extern.slf4j.Slf4j;

/**
 * 流程审核的公共业务类；
 *
 * @Author ：Yuan；
 * @Date ：2018/12/6 0006 15:16；
 */
@Slf4j
public abstract class CommonTaskHandler implements TaskListener {
    /**
     * 部门直属领导审核中需要处理的业务内容：1、更新关联数据的状态；2、更新下一个审核人信息到任务对象中；
     *
     * @param delegateTask：任务对象；
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        // 封装数据；
        handleApproveData(delegateTask);
        // 更新数据的状态；
        updateProcessData(delegateTask);
    }

    /**
     * 根据流程类型获取对应流程需要更新的审核状态，子类覆盖；
     *
     * @param delegateTask：任务对象，用于更新流程审核人；
     */
    public abstract void handleApproveData(DelegateTask delegateTask);

    /**
     * 获取审核意见；
     *
     * @param delegateTask：任务对象，用于更新流程审核人；
     * @return ：是否同意审核；
     */
    protected boolean getOpinion(DelegateTask delegateTask) {
        Boolean agree = delegateTask.getVariable("agree", Boolean.class);
        // 默认为通过，比如流程的开始和网关的跳转；
        if (agree == null) {
            agree = true;
        }
        return agree;
    }

    /**
     * 确定审核人并更新消息接收人的用户ID和部门ID；
     *
     * @param delegateTask：任务对象；
     * @param state：下个审核节点；
     */
    protected void setApproveUser(DelegateTask delegateTask, int state) {
        // 判断前端是否有审核人信息传递过来；
        Integer nextUserId = delegateTask.getVariable("nextUser", Integer.class);
        String nextUser = nextUserId == null ? null : nextUserId.toString();
        String nextUserName = delegateTask.getVariable("nextUserName", String.class);
        Integer nextUserDept = delegateTask.getVariable("nextUserDept", Integer.class);
        // 获取公司代码变量；
        String company = delegateTask.getVariable("company", String.class);
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
                // 直属领导审核；
//			case IConst.STATE_BZ:
//				User user = AppUtil.getUser();
//				Integer deptId = user.getDeptId();
//				Dept dept = user.getDept();
//				if (dept != null) {
//					delegateTask.setVariable("acceptDept", deptId);
//					Integer userId = dept.getMgrLeaderId();
//					delegateTask.setVariable("acceptWorker", userId);
//					nextUser = userId == null ? null : userId.toString();
//					nextUserName = dept.getMgrLeaderName();
//				}
//				break;
                // 直属领导审核；
                case IConst.STATE_BZ:
                    UserMapper userMapper = SpringUtils.getBean("userMapper");
                    User user = userMapper.getById(Integer.valueOf(delegateTask.getVariable("userId").toString()));
                    Integer deptId = user.getDeptId();
                    DeptMapper deptMapper = SpringUtils.getBean("deptMapper");
                    Dept dept = deptMapper.getById(deptId);
                    if (dept != null) {
                        delegateTask.setVariable("acceptDept", deptId);
                        Integer userId = dept.getMgrLeaderId();
                        delegateTask.setVariable("acceptWorker", userId);
                        nextUser = userId == null ? null : userId.toString();
                        nextUserName = dept.getMgrLeaderName();
                    }
                    break;
                // 组长审核
                case IConst.STATE_ZZ:
                    User emp = AppUtil.getUser();
                    Integer empId = emp.getDeptId();
                    Dept dep = emp.getDept();
                    if (dep != null) {
                        delegateTask.setVariable("acceptDept", empId);
                        Integer userId = dep.getMgrId();
                        delegateTask.setVariable("acceptWorker", userId);
                        nextUser = userId == null ? null : userId.toString();
                        nextUserName = dep.getMgrName();
                    }
                    break;
                // 人事审核；
                case IConst.STATE_RS:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                // 行政审核；
                case IConst.STATE_XZ:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                // 行政总监审核；
                case IConst.STATE_XZZJ:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                //总监审核
                case IConst.STATE_ZJ:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                // 会计审核；
                case IConst.STATE_KJ:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                // 出纳审核；
                case IConst.STATE_CN:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                    //财务部长
                case IConst.STATE_CWFH:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser =datas[0];
                    nextUserName = datas[1];
                    break;
                case IConst.STATE_CWKP:
                    datas = getApproveUserId(delegateTask, state ,company);
                    nextUser =datas[0];
                    nextUserName = datas [1];
                // 财务总监审核；
                case IConst.STATE_CFO:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                // 总经理审核；
                case IConst.STATE_CEO:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                //出差总结报告审核
                case IConst.STATE_REPORT:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                //总经理复审
                case IConst.STATE_ZJLFS:
                    datas = getApproveUserId(delegateTask, state, company);
                    nextUser = datas[0];
                    nextUserName = datas[1];
                    break;
                // 业务员确认；
                case IConst.STATE_YW:
                    delegateTask.setVariable("acceptDept", delegateTask.getVariable("businessDept", Integer.class));
                    Integer userId = delegateTask.getVariable("businessId", Integer.class);
                    delegateTask.setVariable("acceptWorker", userId);
                    nextUser = userId.toString();
                    nextUserName = delegateTask.getVariable("businessName", String.class);
                    break;
                // 审核完成，更新流程的状态；
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

    /**
     * 获取审核的用户ID；
     *
     * @param delegateTask：任务对象；
     * @param state：下个审核节点；
     * @param company：额外处理的公司代码；
     * @return ：审核的用户ID；
     */
    private String[] getApproveUserId(DelegateTask delegateTask, int state, String company) {
        // 获取数据服务接口；
        IUserService userService = SpringUtils.getBean("userService");
        User user = null;
        String nextUser = null;
        String userName = null;
        // 获取需要更新的状态；
        String companyCode = delegateTask.getVariable("companyCode", String.class);

        if (!StringUtils.isEmpty(companyCode)) {
            try{
                switch (state) {
                    // 人事审核；
                    case IConst.STATE_RS:
                        user = userService.getRSBZInfo(companyCode);
                        break;
                    // 行政审核；
                    case IConst.STATE_XZ:
                        user = userService.getXZBZInfo(companyCode);
                        break;
                    // 行政总监审核；
                    case IConst.STATE_XZZJ:
                        user = userService.getXZZJInfo(companyCode);
                        break;
                    //总监审核
                    case IConst.STATE_ZJ:
                        user = userService.getZJInfo(companyCode,delegateTask.getVariable("userId", String.class));
                        break;
                    // 会计审核；
                    case IConst.STATE_KJ:
                        if (StringUtils.isEmpty(company)) {
                            user = userService.getAccountingInfo(companyCode);
                        } else {
                            user = userService.getAccountingInfo(company);
                        }
                        break;
                    case IConst.STATE_CWKP:
                        if (StringUtils.isEmpty(company)) {
                            user = userService.getCWZLInfo(companyCode);
                        } else {
                            user = userService.getCWZLInfo(company);
                        }
                        break;
                    // 出纳审核；
                    case IConst.STATE_CN:
                        if (StringUtils.isEmpty(company)) {
                            user = userService.getTellerInfo(companyCode);
                        } else {
                            user = userService.getTellerInfo(company);
                        }
                        break;
                    // 财务总监审核；
                    case IConst.STATE_CFO:
                        if (StringUtils.isEmpty(company)) {
                            user = userService.getCFOInfo(companyCode);
                        } else {
                            user = userService.getCFOInfo(company);
                        }
                        break;
                    // 出纳审核；
                    case IConst.STATE_PASS:
                        user = userService.getTellerInfo(companyCode);
                        break;
                    //财务部长
                    case IConst.STATE_CWFH:
                        if (StringUtils.isEmpty(company)) {
                            user = userService.getCWBZInfo(companyCode);
                        } else {
                            user = userService.getCWBZInfo(company);
                        }
                        break;
                    // 总经理审核；
                    case IConst.STATE_CEO:
                        user = userService.getCEOInfo(companyCode);
                        break;

                    // 总经理复核；
                    case IConst.STATE_ZJLFS:
                        user = userService.getCEOInfo(companyCode);
                        break;
                    // 出差总结获取出差人信息
                    case IConst.STATE_REPORT:
                        Integer initiatorWorker = delegateTask.getVariable("initiatorWorker", Integer.class);
                        user = userService.getById(initiatorWorker);
                        break;
                    // 不存在；
                    default:
                        break;
                }
            }catch (IndexOutOfBoundsException i){
                i.printStackTrace();
                throw new QinFeiException(1001, "没有下一个审批人，请联系管理员！");
            }catch (Exception e){
                e.printStackTrace();
                throw new QinFeiException(1001, "获取下一个审批人出错，请联系管理员！");
            }
        }
        // 获取用户信息；
        if (user != null) {
            delegateTask.setVariable("acceptDept", user.getDeptId());
            delegateTask.setVariable("acceptWorker", user.getId());
            nextUser = user.getId().toString();
            userName = user.getName();
        }

        return new String[]{nextUser, userName};
    }

    /**
     * 根据流程类型，更新对应的数据状态；
     *
     * @param delegateTask：任务对象；
     */
    private void updateProcessData(DelegateTask delegateTask) {
        // 用于保存创建代办事项和消息数据的集合；
        Map<String, Object> map = new HashMap<>();

        // =================================================通知推送模块开始=================================================
        // 获取审核的流程类型，类型定义参考接口：com.qinfei.qferp.utils.IProcess；
        Integer process = delegateTask.getVariable("process", Integer.class);
        process = process == null ? -1 : process;
        // 更新消息内容，根据状态来确定消息内容；
        String processName = delegateTask.getVariable(IProcess.PROCESS_NAME, String.class);
        // 获取需要审核的数据ID，类型不确定，统一使用字符串获取；
        String dataId = delegateTask.getVariable("dataId", String.class);
        dataId = dataId == null ? "-1" : dataId;
        // WebSocket发送消息需要此字段；
        map.put(IProcess.PROCESS_NAME, processName);

		// 发送信息需要的信息；
		// FIXME: 2018/12/11 0011 默认显示图片，如需替换请修改此处；
		// 获取登录用户；
		User loginUser = AppUtil.getUser();
		String userImage = loginUser.getImage();
		// 获取消息显示的图片；
		String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
		String dataUrl = delegateTask.getVariable(IProcess.PROCESS_FINISH_URL, String.class);

        // 获取数据需要更新的状态；
        Integer state = delegateTask.getVariable("state", Integer.class);
        // 为空则驳回；
        state = state == null ? IConst.STATE_REJECT : state;
        switch (state) {
            // 审核驳回；
            case IConst.STATE_REJECT:
                // 发送信息需要的信息；
                map.put("pic", pictureAddress);
                map.put("content", String.format("您提交的%s在%s中被驳回。", processName, delegateTask.getName()));

                // 增加待办事项需要的信息，跳转的地址需要变更为数据编辑的页面；
                map.put("itemName", String.format("%s - 已被驳回", delegateTask.getVariable("dataName")));
                map.put("itemContent", String.format("您的%s已被驳回，请重新提交", processName));
                // 增加待办事项需要的信息；
                map.put("workType", processName);
                // 获取驳回处理的链接页面；
                String editUrl = delegateTask.getVariable(IProcess.PROCESS_EDIT_URL, String.class);
                if (StringUtils.isEmpty(editUrl)) {
                    editUrl = dataUrl;
                }
                // 更新代办事项的跳转地址，跳转到审核关联数据的列表页面；
                map.put("transactionAddress", editUrl);
                // 处理完成的待办事项跳转到关联数据的列表页面；
                map.put("finishAddress", dataUrl);
                // 代办事项的紧急程度；
                map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
                break;
            // 审核通过；
            case IConst.STATE_FINISH:
                // ===============================发起人===============================
                // 发送信息需要的信息，用于在审核操作后通知流程发起人；
                map.put("newPic", pictureAddress);
                map.put("newContent", String.format("您提交的%s状态已更新。", processName));
                // 处理中的待办事项跳转到流程审核页面；
                map.put("transactionAddress", dataUrl);
                map.put("finishAddress", dataUrl);
                // ===============================发起人结束===============================
                break;
            // 财务出纳；
            case IConst.STATE_PASS:
                // 发送信息需要的信息，通知流程发起人；
                map.put("newPic", pictureAddress);
                map.put("newContent", String.format("您提交的%s已审核通过。", processName));

                // ===============================通知出纳===============================
                // 增加待办事项需要的信息；
                map.put("itemName", String.format("%s - 等待处理", delegateTask.getVariable("dataName")));
                map.put("itemContent", String.format("您有新的%s需要处理", processName));
                // 增加待办事项需要的信息；
                map.put("workType", processName);
                // 获取出纳处理的链接页面；
                String cashierUrl = delegateTask.getVariable(IProcess.PROCESS_PASS_URL, String.class);
                if (StringUtils.isEmpty(cashierUrl)) {
                    cashierUrl = dataUrl;
                }
                // 处理中的待办事项跳转到流程审核页面；
                map.put("transactionAddress", cashierUrl);
                // 处理完成的待办事项跳转到关联数据的列表页面；
                map.put("finishAddress", dataUrl);
                // 代办事项的紧急程度；
                map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
                // 获取出纳用户信息；
                IUserService userService = SpringUtils.getBean("userService");
                User user;
                // 开票找财务部长确认
                String companyCode = delegateTask.getVariable("companyCode", String.class);
                if (process == IProcess.PROCESS_BALLOT) {
                    user = userService.getCWBZInfo(companyCode);
                } else {
                    // 获取公司代码变量；
                    String company = delegateTask.getVariable("company", String.class);
                    if (StringUtils.isEmpty(company)) {
                        user = userService.getTellerInfo(companyCode);
                    } else {
                        user = userService.getTellerInfo(company);
                    }
                }

                delegateTask.setVariable("acceptDept", user.getDeptId());
                delegateTask.setVariable("acceptWorker", user.getId());
                // ===============================通知出纳结束===============================
                break;

            //财务会计复核对公账户出款
            case IConst.STATE_KJ:
                // 发送信息需要的信息，通知流程发起人；
                map.put("newPic", pictureAddress);
                map.put("newContent", String.format("您提交的%s已审核通过。", processName));

                // ===============================通知出纳===============================
                // 增加待办事项需要的信息；
                map.put("itemName", String.format("%s - 出账确认等待处理", delegateTask.getVariable("dataName")));
                map.put("itemContent", String.format("您有新的%s出账确认需要处理", processName));
                // 增加待办事项需要的信息；
                map.put("workType", processName);
                // 获取出纳处理的链接页面；
                String accUrl = delegateTask.getVariable(IProcess.PROCESS_ACCIYBR_URL, String.class);
                if (StringUtils.isEmpty(accUrl)) {
                    accUrl = dataUrl;
                }
                // 处理中的待办事项跳转到流程审核页面；
                map.put("transactionAddress", accUrl);
                // 处理完成的待办事项跳转到关联数据的列表页面；
                map.put("finishAddress", dataUrl);
                // 代办事项的紧急程度；
                map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
                // 获取出纳用户信息；
                IUserService userC = SpringUtils.getBean("userService");
                User chun;
                // 开票出账找财务部长确认
                String companyCodeCW = delegateTask.getVariable("companyCode", String.class);
                // 获取公司代码变量；
                String company1 = delegateTask.getVariable("company", String.class);
                if (StringUtils.isEmpty(company1)) {
                    chun = userC.getAccountingInfo(companyCodeCW);
                } else {
                    chun = userC.getAccountingInfo(company1);
                }

                delegateTask.setVariable("acceptDept", chun.getDeptId());
                delegateTask.setVariable("acceptWorker", chun.getId());
                // ===============================通知财务部长结束===============================
                break;
            //财务财务助理开票
            case IConst.STATE_CWKP:
                // 发送信息需要的信息，通知流程发起人；
                map.put("newPic", pictureAddress);
                map.put("newContent", String.format("您提交的%s已审核通过。", processName));

                // ===============================通知助理===============================
                // 增加待办事项需要的信息；
                map.put("itemName", String.format("%s - 开票等待处理", delegateTask.getVariable("dataName")));
                map.put("itemContent", String.format("您有新的%s开票需要处理", processName));
                // 增加待办事项需要的信息；
                map.put("workType", processName);
                // 获取出纳处理的链接页面；
                String assUrl = delegateTask.getVariable(IProcess.PROCESS_ASSISTANT_URL, String.class);
                if (StringUtils.isEmpty(assUrl)) {
                    assUrl = dataUrl;
                }
                // 处理中的待办事项跳转到流程审核页面；
                map.put("transactionAddress", assUrl);
                // 处理完成的待办事项跳转到关联数据的列表页面；
                map.put("finishAddress", dataUrl);
                // 代办事项的紧急程度；
                map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
                // 获取出纳用户信息；
                IUserService userZ = SpringUtils.getBean("userService");
                User zhul;
                // 开票出账找财务部长确认
                String companyCodeC1 = delegateTask.getVariable("companyCode", String.class);
                // 获取公司代码变量；
                String company2 = delegateTask.getVariable("company", String.class);
                if (StringUtils.isEmpty(company2)) {
                    zhul = userZ.getCWZLInfo(companyCodeC1);
                } else {
                    zhul = userZ.getCWZLInfo(company2);
                }

                delegateTask.setVariable("acceptDept", zhul.getDeptId());
                delegateTask.setVariable("acceptWorker", zhul.getId());
                // ===============================通知财务部长结束===============================
                break;
            //出纳审核
            case IConst.STATE_CN:
                // 发送信息需要的信息，通知流程发起人；
                map.put("newPic", pictureAddress);
                map.put("newContent", String.format("您提交的%s已审核通过。", processName));

                // ===============================通知出纳===============================
                // 增加待办事项需要的信息；
                map.put("itemName", String.format("%s - 等待处理", delegateTask.getVariable("dataName")));
                map.put("itemContent", String.format("您有新的%s需要处理", processName));
                // 增加待办事项需要的信息；
                map.put("workType", processName);
                // 获取出纳处理的链接页面；
                String chunaUrl = delegateTask.getVariable(IProcess.PROCESS_PASS_URL, String.class);
                if (StringUtils.isEmpty(chunaUrl)) {
                    chunaUrl = dataUrl;
                }
                // 处理中的待办事项跳转到流程审核页面；
                map.put("transactionAddress", chunaUrl);
                // 处理完成的待办事项跳转到关联数据的列表页面；
                map.put("finishAddress", dataUrl);
                // 代办事项的紧急程度；
                map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
                // 获取出纳用户信息；
                IUserService userChuna = SpringUtils.getBean("userService");
                User chuna;
                // 开票找财务部长确认
                String companyCode1 = delegateTask.getVariable("companyCode", String.class);
                if (process == IProcess.PROCESS_BALLOT) {
                    chuna = userChuna.getCWBZInfo(companyCode1);
                } else {
                    // 获取公司代码变量；
                    String company = delegateTask.getVariable("company", String.class);
                    if (StringUtils.isEmpty(company)) {
                        chuna = userChuna.getTellerInfo(companyCode1);
                    } else {
                        chuna = userChuna.getTellerInfo(company);
                    }
                }

                delegateTask.setVariable("acceptDept", chuna.getDeptId());
                delegateTask.setVariable("acceptWorker", chuna.getId());
                // ===============================通知出纳结束===============================
                break;

            // 审核正常流转；
            default:
                // ===============================发起人===============================
                // 发送信息需要的信息，用于在审核操作后通知流程发起人；
                map.put("newPic", pictureAddress);
                map.put("newContent", String.format("您提交的%s状态已更新。", processName));

                // 通知审核人；
                map.put("pic", pictureAddress);
                map.put("content", String.format("您有新的%s需要审核。", processName));
                // ===============================发起人结束===============================

                // ===============================审核人===============================
                // 增加待办事项需要的信息；
                map.put("itemName", String.format("%s - 等待审核", delegateTask.getVariable("dataName")));
                map.put("itemContent", String.format("您有新的%s需要审核", processName));
                // 增加待办事项需要的信息；
                map.put("workType", processName);
                // 处理中的待办事项跳转到流程审核页面；
                String processUrl = delegateTask.getVariable(IProcess.PROCESS_APPROVE_URL, String.class);
                if (StringUtils.isEmpty(processUrl)) {
                    processUrl = "/process/queryTask";
                } else {
                    // 如果有动态URL，进行更新；
                    String dynamicUrl = delegateTask.getVariable("dynamicUrl", String.class);
                    if (!StringUtils.isEmpty(dynamicUrl)) {
                        processUrl = dynamicUrl;
                        delegateTask.setVariable(IProcess.PROCESS_APPROVE_URL, dynamicUrl);
                    }
                }
                map.put("transactionAddress", processUrl);
                // 处理完成的待办事项跳转到关联数据的列表页面；
                map.put("finishAddress", dataUrl);
                // 代办事项的紧急程度；
                map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
                // ===============================审核人结束===============================
                break;
        }

        // 获取流程接收人的信息；
        Integer acceptDept = delegateTask.getVariable("acceptDept", Integer.class);
        Integer acceptWorker = delegateTask.getVariable("acceptWorker", Integer.class);
        if (acceptDept != null && acceptWorker != null) {
            map.put("acceptDept", acceptDept);
            map.put("acceptWorker", acceptWorker);
        }

        // 获取流程发起人信息；
        Integer initiatorDept = delegateTask.getVariable("initiatorDept", Integer.class);
        Integer initiatorWorker = delegateTask.getVariable("initiatorWorker", Integer.class);
        map.put("initiatorDept", initiatorDept);
        map.put("initiatorWorker", initiatorWorker);

        // 获取上个待办事项的ID，在添加待办事项的时候更新上个待办事项的状态；
        Integer oldItemId = delegateTask.getVariable("itemId", Integer.class);
        if (oldItemId != null) {
            updateWork(oldItemId);
        }

        // 发送消息；
        sendMessage(map);
        // 增加待办事项；
        Integer itemId = addWork(map);
        // 更新到数据库，用于在流程流转时更新状态；
        if (itemId != null) {
            delegateTask.setVariable("itemId", itemId);
        }

        // 如果有新消息，通知流程发起人；
        Object object = map.get("newContent");
        if (object != null) {
            map.put("pic", map.get("newPic").toString());
            map.put("content", object.toString());

            // 接收人改为流程发起人；
            map.put("acceptDept", initiatorDept);
            map.put("acceptWorker", initiatorWorker);

            sendMessage(map);
        }
        // =================================================通知推送模块结束=================================================

        // TODO: 2019/3/27 0027 流程回调配置模块，流程单节点处理完毕的回调处理；
        // 配置说明：流程的回调会默认更新关联数据的state、taskId、itemId属性，提供相应的业务更新处理逻辑即可；
        // =================================================审核数据更新模块开始==============================================
        // 当前登录的用户ID；
        int loginUserId = loginUser.getId();
        // 流程当前的任务ID；
        String taskId = delegateTask.getId();
        switch (process) {
            // 开票申请；
            case IProcess.PROCESS_BALLOT:
                Invoice invoice = new Invoice();

                invoice.setId(Integer.parseInt(dataId));
                invoice.setState(state);
                // 更新流程当前的任务ID；
                invoice.setTaskId(taskId);
                // 更新待办事项的ID；
                invoice.setItemId(itemId);

                IInvoiceService invoiceService = SpringUtils.getBean("invoiceService");
                invoiceService.update(invoice);
                break;
            // 借款申请；
            case IProcess.PROCESS_BORROW:
                Borrow borrow = new Borrow();

                borrow.setId(Integer.parseInt(dataId));
                borrow.setState(state);
                borrow.setUpdateUserId(loginUserId);
                // 更新流程当前的任务ID；
                borrow.setTaskId(taskId);
                // 更新待办事项的ID；
                borrow.setItemId(itemId);

                IBorrowService borrowService = SpringUtils.getBean("borrowService");
                borrowService.update(borrow);
                break;
            // 媒介请款；
            case IProcess.PROCESS_MEDIAREFUND:
                Outgo outgo = new Outgo();

                outgo.setId(Integer.parseInt(dataId));
                outgo.setState(state);
                outgo.setUpdateUserId(loginUserId);
                // 更新流程当前的任务ID；
                outgo.setTaskId(taskId);
                // 更新待办事项的ID；
                outgo.setItemId(itemId);

                IOutgoService outgoService = SpringUtils.getBean("outgoService");
                outgoService.update(outgo);
                break;
            //自媒体媒介请款
            case IProcess.PROCESS_SELF_MEDIAREFUND:
                Outgo selfOutgo = new Outgo();
                selfOutgo.setId(Integer.parseInt(dataId));
                selfOutgo.setState(state);
                selfOutgo.setUpdateUserId(loginUserId);
                // 更新流程当前的任务ID；
                selfOutgo.setTaskId(taskId);
                // 更新待办事项的ID；
                selfOutgo.setItemId(itemId);

                IOutgoService outgoServiceSelf = SpringUtils.getBean("outgoService");
                outgoServiceSelf.update(selfOutgo);
                break;
            //河图请款
            case IProcess.PROCESS_HTOUTPLEASETYPE:
                Outgo HTOutgo = new Outgo();
                HTOutgo.setId(Integer.parseInt(dataId));
                HTOutgo.setState(state);
                HTOutgo.setUpdateUserId(loginUserId);
                // 更新流程当前的任务ID；
                HTOutgo.setTaskId(taskId);
                // 更新待办事项的ID；
                HTOutgo.setItemId(itemId);

                IOutgoService outgoServiceHTOutgo = SpringUtils.getBean("outgoService");
                outgoServiceHTOutgo.update(HTOutgo);
                break;
            //报纸板块请款
            case IProcess.PROCESS_NEWSPAPEROUTGO:
                Outgo newspaperOutgo = new Outgo();
                newspaperOutgo.setId(Integer.parseInt(dataId));
                newspaperOutgo.setState(state);
                newspaperOutgo.setUpdateUserId(loginUserId);
                // 更新流程当前的任务ID；
                newspaperOutgo.setTaskId(taskId);
                // 更新待办事项的ID；
                newspaperOutgo.setItemId(itemId);

                IOutgoService outgoServicenewspaperOutgo = SpringUtils.getBean("outgoService");
                outgoServicenewspaperOutgo.update(newspaperOutgo);
                break;
            // 退款申请；
            case IProcess.PROCESS_REFUND:
                Refund refund = new Refund();
                refund.setId(Integer.parseInt(dataId));
                refund.setState(state);
                refund.setUpdateUserId(loginUserId);
                // 更新流程当前的任务ID；
                refund.setTaskId(taskId);
                // 更新待办事项的ID；
                refund.setItemId(itemId);

                IRefundService refundService = SpringUtils.getBean("refundService");
                refundService.update(refund);
                break;
            // 财务提成；
            case IProcess.PROCESS_ROYALTY:
                Commission commission = new Commission();
                commission.setId(Integer.parseInt(dataId));
                commission.setState(state);
                // 更新流程当前的任务ID；
                commission.setTaskId(taskId);
                // 更新待办事项的ID；
                commission.setItemId(itemId);

                ICommissionService commissionService = SpringUtils.getBean("commissionService");
                commissionService.update(commission);
                break;
            // 员工录用；
            case IProcess.PROCESS_EMPLOY:
                IEmployEntryService entryService = SpringUtils.getBean("employEntryService");
                entryService.processEntry(Integer.parseInt(dataId), delegateTask.getVariable("code", String.class), state, taskId, itemId, getOpinion(delegateTask), delegateTask.getVariable("desc", String.class));

                // 更新查询码；
                String uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                delegateTask.setVariable("code", uniqueKey);
                // 更新链接；
                delegateTask.setVariable("dynamicUrl", "/employApprove?code=" + uniqueKey);
                break;
            // 员工转正；
            case IProcess.PROCESS_FORMAL:
                IEmployeeService employeeService = SpringUtils.getBean("employeeService");
                employeeService.processFormal(Integer.parseInt(dataId), delegateTask.getVariable("code", String.class), state, taskId, itemId);

                // 更新查询码；
                uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                delegateTask.setVariable("code", uniqueKey);
                // 更新链接；
                delegateTask.setVariable("dynamicUrl", "/formalApprove?code=" + uniqueKey);
                break;
            // 员工离职；
            case IProcess.PROCESS_LEAVE:
                employeeService = SpringUtils.getBean("employeeService");
                employeeService.processLeave(Integer.parseInt(dataId), delegateTask.getVariable("code", String.class), state, taskId, itemId);

                // 更新查询码；
                uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                delegateTask.setVariable("code", uniqueKey);
                // 更新链接；
                delegateTask.setVariable("dynamicUrl", "/leaveApprove?code=" + uniqueKey);
                break;
            // 员工调岗；
            case IProcess.PROCESS_TRANSFER:
                employeeService = SpringUtils.getBean("employeeService");
                employeeService.processTransfer(Integer.parseInt(dataId), delegateTask.getVariable("code", String.class), state, taskId, itemId);

                // 更新查询码；
                uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                delegateTask.setVariable("code", uniqueKey);
                // 更新链接；
                delegateTask.setVariable("dynamicUrl", "/transferApprove?code=" + uniqueKey);
                break;
            // 员工离职交接；
            case IProcess.PROCESS_HANDOVER_LEAVE:
                employeeService = SpringUtils.getBean("employeeService");
                employeeService.processConnect(Integer.parseInt(dataId), delegateTask.getVariable("code", String.class), state, taskId, itemId, IEmployConnect.CONNECT_LEAVE);

                // 更新查询码；
                uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                delegateTask.setVariable("code", uniqueKey);
                // 更新链接；
                delegateTask.setVariable("dynamicUrl", "/leaveConnectApprove?code=" + uniqueKey);
                break;
            // 员工调岗交接；
            case IProcess.PROCESS_HANDOVER_TRANSFER:
                employeeService = SpringUtils.getBean("employeeService");
                employeeService.processConnect(Integer.parseInt(dataId), delegateTask.getVariable("code", String.class), state, taskId, itemId, IEmployConnect.CONNECT_TRANSFER);

                // 更新查询码；
                uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                delegateTask.setVariable("code", uniqueKey);
                // 更新链接；
                delegateTask.setVariable("dynamicUrl", "/transferConnectApprove?code=" + uniqueKey);
                break;
            // 绩效考核；
            case IProcess.PROCESS_PERFORMANCE:
                IPerformanceScoreService performanceScoreService = SpringUtils.getBean("performanceScoreService");
                performanceScoreService.processPerformance(Integer.parseInt(dataId), delegateTask.getVariable("code", String.class), state, taskId, itemId);

                // 更新查询码；
                uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                delegateTask.setVariable("code", uniqueKey);
                // 更新链接；
                delegateTask.setVariable("dynamicUrl", "/performanceApprove?code=" + uniqueKey);
                break;
            // 请假流程（一天内）；
            case IProcess.PROCESS_VOCATION_ONE:
                handleVocation(dataId, state, taskId, itemId, acceptWorker);
                break;
            // 请假流程（2天内）；
            case IProcess.PROCESS_VOCATION_TWO:
                handleVocation(dataId, state, taskId, itemId, acceptWorker);
                break;
            // 请假流程（3天内）；
            case IProcess.PROCESS_VOCATION_THREE:
                handleVocation(dataId, state, taskId, itemId, acceptWorker);
                break;
            // 外出流程
            case IProcess.PROCESS_OUTWORK:
                handleVocation(dataId, state, taskId, itemId, acceptWorker);
                break;
            // 加班流程
            case IProcess.PROCESS_WORKOVERTIME:
                handleVocation(dataId, state, taskId, itemId, acceptWorker);
                break;
            //出差流程
            case IProcess.PROCESS_ONBUSINESS:
                handleVocation(dataId, state, taskId, itemId, acceptWorker);
                break;
            // 退稿流程；
            case IProcess.PROCESS_MANUSCRIPT:
                Drop drop = new Drop();
                drop.setId(Integer.parseInt(dataId));
                drop.setState(state);
                drop.setUpdateUserId(loginUserId);
                // 更新流程当前的任务ID；
                drop.setTaskId(taskId);
                // 更新待办事项的ID；
                drop.setItemId(itemId);

                IDropService dropService = SpringUtils.getBean("dropService");
                dropService.update(drop);
                break;
            // 报销流程
            case IProcess.PROCESS_REIMBURSEMENT:

                IReimbursementService reimbursementService = SpringUtils.getBean("reimbursementService");
                //TODO 添加审批人id
                reimbursementService.processReimbursement(dataId, state, loginUserId, taskId, itemId, acceptWorker);
                break;
            // 流程不存在；
            default:
                break;
        }
        // =================================================审核数据更新模块结束==============================================
    }

    /**
     * 更新请假相关的数据；
     *
     * @param dataId：主键ID；
     * @param state：状态；
     * @param taskId：任务流程ID；
     * @param itemId：待办事项ID；
     */
    private void handleVocation(String dataId, int state, String taskId, Integer itemId, Integer acceptWorker) {
        Administrative leave = new Administrative();
        if (acceptWorker != null) {
            leave.setApproverUserId(acceptWorker.toString());
        }
        leave.setId(Integer.parseInt(dataId));
        leave.setState(state);
        // 更新流程当前的任务ID；
        leave.setTaskId(taskId);
        // 更新待办事项的ID；
        leave.setItemId(itemId);
        IAdministrativeLeaveService leaveService = SpringUtils.getBean("administrativeLeaveService");
        leaveService.processLeava(leave);
    }

    /**
     * 给子类使用的公用方法，用于给流程的处理人发送提醒消息；
     *
     * @param map：保存审核信息的集合；
     */
    private void sendMessage(Map<String, Object> map) {
        // 发送消息；
        Object object = EntityUtil.getNewObject(map, Message.class);
        if (object != null) {
            Message message = (Message) object;
            if (!StringUtils.isEmpty(message.getContent())) {
                // 系统右侧消息推送；
                IMessageService messageService = SpringUtils.getBean("messageService");
                messageService.addMessage(message);

                // WebSocket的消息推送；
                WSMessage wsMessage = new WSMessage();

                // 接收消息的用户信息；
                Integer userId = message.getAcceptWorker();
                wsMessage.setReceiveUserId(userId.toString());
                IUserService userService = SpringUtils.getBean("userService");
                User user = userService.getById(userId);
                // 防止用户被删除出现异常；
                if (user != null) {
                    wsMessage.setReceiveName(user.getName());

                    // 发送消息的用户信息；
                    User loginUser = AppUtil.getUser();
                    wsMessage.setSendName(loginUser.getName());
                    wsMessage.setSendUserId(loginUser.getId().toString());
                    wsMessage.setSendUserImage(loginUser.getImage());

                    // 消息内容；
                    wsMessage.setContent(message.getContent());
                    wsMessage.setSubject(map.get(IProcess.PROCESS_NAME).toString());
                    wsMessage.setUrl(map.get("finishAddress").toString());
                    // 提交信息；
                    WebSocketServer.sendMessage(wsMessage);
                }
            }
        }
    }

    /**
     * 给子类使用的公用方法，用于给流程的处理人增加代办事项；
     *
     * @param map：保存审核信息的集合；
     */
    private Integer addWork(Map<String, Object> map) {
        Integer itemId = null;
        // 创建代办事项；
        Object object = EntityUtil.getNewObject(map, Items.class);
        if (object != null) {
            Items items = (Items) object;
            if (!StringUtils.isEmpty(items.getItemName())) {
                IItemsService itemsService = SpringUtils.getBean("itemsService");
                Date date = new Date();
                items.setStartTime(date);
                // FIXME: 2018/12/8 0008 默认期限为3天，如需修改请在此处编辑；
                items.setEndTime(DateUtils.getAfterDay(date, 3));
                itemsService.addItemsReturnId(items);
                itemId = items.getId();
            }
        }
        return itemId;
    }

    /**
     * 更新待办事项的状态；
     *
     * @param itemId：待办事项ID；
     */
    private void updateWork(int itemId) {
        Items items = new Items();
        items.setId(itemId);
        items.setTransactionState(Const.ITEM_Y);
        IItemsService itemsService = SpringUtils.getBean("itemsService");
        itemsService.finishItems(items);
    }
}