package com.qinfei.qferp.service.impl.flow;

import com.alibaba.druid.util.StringUtils;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.serivce.impl.DictService;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.accountsMess.AccountsMess;
import com.qinfei.qferp.entity.administrative.*;
import com.qinfei.qferp.entity.biz.Project;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.crm.CrmCompanyProtect;
import com.qinfei.qferp.entity.employ.EmployEntry;
import com.qinfei.qferp.entity.employ.Employee;
import com.qinfei.qferp.entity.fee.*;
import com.qinfei.qferp.entity.inventory.*;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import com.qinfei.qferp.entity.meetingmanagement.MeetingManagement;
import com.qinfei.qferp.entity.performance.PerformanceScore;
import com.qinfei.qferp.entity.standardized.StandardizedCompany;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.flow.command.*;
import com.qinfei.qferp.flow.generator.DefinedProcessDiagramGenerator;
import com.qinfei.qferp.flow.listener.JumpTaskCommand;
import com.qinfei.qferp.mapper.ProcessMapper;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.service.employ.IEmployeeConnectService;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.fee.OutgoService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * 开票流程的流程服务实现类；
 *
 * @Author ：Yuan；
 * @Date ：2018/12/6 0006 19:56；
 */
@Slf4j
@Service
public class ProcessService implements IProcessService {
    // 流程引擎管理服务；
    @Autowired
    private ManagementService managementService;
    // 任务存储库接口；
    @Autowired
    private RepositoryService repositoryService;
    // // 任务的运行服务；
    @Autowired
    private RuntimeService runtimeService;
    // // 任务的业务服务；
    @Autowired
    private TaskService taskService;
    // 审核记录的业务服务；
    @Autowired
    private HistoryService historyService;
    // 用户的业务服务；
    @Autowired
    private IUserService userService;
    // 待办事项的业务服务；
    @Autowired
    private IItemsService itemsService;
    // 员工交接的业务服务；
    @Autowired
    private IEmployeeConnectService connectService;
    // 借款对公账户
    @Autowired
    private IAccountService accountService;
    @Autowired
    private ProcessMapper processMapper;
    @Autowired
    private FlowableMapper flowableMapper;
    @Autowired
    private Config config;
    @Autowired
    private DeptZwMapper deptZwMapper;
    @Autowired
    private DictService dictService;
    @Autowired
    private OutgoService outgoService;

    /**
     * 发起开票审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param invoice：需要审核的开票对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addBallotProcess(Invoice invoice, int urgencyLevel) {
        Map map = new HashMap();
        //消息发起人
        map.put("messageUserName", invoice.getApplyName());
        //消息标题
        map.put("messageTitle", invoice.getName());
        map.put("taxType", invoice.getTaxType());
        map.put("money", invoice.getInvoiceAmount());
        return startProcess(map, IProcess.PROCESS_BALLOT, String.valueOf(invoice.getId()), invoice.getName(), invoice.getTaskId(), urgencyLevel);
    }

    /**
     * 发起借款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param borrow：需要审核的借款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addBorrowProcess(Borrow borrow, int urgencyLevel) {
//        return startProcess(getBorrowMoneyGateData(borrow.getApplyAmount(), borrow.getAccountId(), borrow.getApplyName(), borrow.getTitle()), IProcess.PROCESS_BORROW, String.valueOf(borrow.getId()), borrow.getTitle(), borrow.getTaskId(), urgencyLevel, borrow.getExpertPayTime());
        return startProcess(getBorrowData(borrow.getApplyAmount(),borrow.getType(),borrow.getApplyName(), borrow.getTitle()), IProcess.PROCESS_BORROW, String.valueOf(borrow.getId()), borrow.getTitle(), borrow.getTaskId(), urgencyLevel, borrow.getExpertPayTime());

    }

    /**
     * 发起物品采购流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param purchase：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addPurchaseProcess(Purchase purchase, int urgencyLevel) {
        User user = AppUtil.getUser();
        if (user == null) {
            throw new QinFeiException(1002, "请先登录！");
        }
        Map<String, Object> map = new HashMap();
        //消息发起人
        map.put("messageUserName", user.getName());
        //消息标题
        map.put("messageTitle", purchase.getTitle());
        return startProcess(map, IProcess.PROCESS_PURCHASE, String.valueOf(purchase.getId()), purchase.getTitle(), purchase.getTaskId(), urgencyLevel);
    }

    /**
     * 发起物品领用流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param apply：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addApplyProcess(ReceiveApply apply, int urgencyLevel) {
        User user = AppUtil.getUser();
        if (user == null) {
            throw new QinFeiException(1002, "请先登录！");
        }
        Map<String, Object> map = new HashMap();
        //消息发起人
        map.put("messageUserName", user.getName());
        //消息标题
        map.put("messageTitle", apply.getTitle());
        return startProcess(map, IProcess.PROCESS_APPLY, String.valueOf(apply.getId()), apply.getTitle(), apply.getTaskId(), urgencyLevel);
    }

    /**
     * 发起物品报修流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param repair：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addRepairProcess(ReceiveRepair repair, int urgencyLevel) {
        User user = AppUtil.getUser();
        if (user == null) {
            throw new QinFeiException(1002, "请先登录！");
        }
        Map<String, Object> map = new HashMap();
        //消息发起人
        map.put("messageUserName", user.getName());
        //消息标题
        map.put("messageTitle", repair.getTitle());
        String html="";
        //页面标志1库存报修2领用报修
        if(repair.getHtmlFlag()==1){
            html="/inventory/inventory_stock";
        }else {
            html="/inventory/apply_list";
        }
        map.put("html",html);
        return startProcess(map,IProcess.PROCESS_REPAIR,String.valueOf(repair.getId()),repair.getTitle(),repair.getTaskId(),urgencyLevel);
    }

    /**
     * 发起物品报废流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param scrap：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addScrapProcess(ReceiveScrap scrap, int urgencyLevel) {
        User user = AppUtil.getUser();
        if (user == null) {
            throw new QinFeiException(1002, "请先登录！");
        }
        Map<String, Object> map = new HashMap();
        //消息发起人
        map.put("messageUserName", user.getName());
        //消息标题
        map.put("messageTitle", scrap.getTitle());
        String html="";
        //页面标志1库存报废2领用报废
        if(scrap.getHtmlFlag()==1){
            html="/inventory/inventory_stock";
        }else {
            html="/inventory/apply_list";
        }
        map.put("html",html);
        return startProcess(map,IProcess.PROCESS_SCRAP,String.valueOf(scrap.getId()),scrap.getTitle(),scrap.getTaskId(),urgencyLevel);
    }
    /**
     * 发起物品归还流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param receiveReturn：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addReturnProcess(ReceiveReturn receiveReturn, int urgencyLevel) {
        User user = AppUtil.getUser();
        if (user == null) {
            throw new QinFeiException(1002, "请先登录！");
        }
        Map<String, Object> map = new HashMap();
        //消息发起人
        map.put("messageUserName", user.getName());
        //消息标题
        map.put("messageTitle", receiveReturn.getTitle());
        String html="";
        //页面标志1库存归还2领用归还
        if(receiveReturn.getHtmlFlag()==1){
            html="/inventory/inventory_stock";
        }else {
            html="/inventory/apply_list";
        }
        map.put("html",html);
        return startProcess(map,IProcess.PROCESS_RETURN,String.valueOf(receiveReturn.getId()),receiveReturn.getTitle(),receiveReturn.getTaskId(),urgencyLevel);
    }

    /**
     * 发起媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addMediaRefundProcess(Outgo outgo, int urgencyLevel) {
        Map<String, Object> map = new HashMap<>();
        // 设置第一个网关，判断流程申请人是否新媒介；
        boolean isNewMedia = StringUtils.equals(IConst.DEPT_TYPE_XMT, userService.getMJType(AppUtil.getUser().getId()));
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", isNewMedia);
        // 获取系统定义的新媒介申请的网关金额；
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneyNew"));
        // 获取系统定义的非新媒介申请的网关金额；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
        // 设置第二个网关，判断金额是否符合要求，两种媒介的网关金额不一致，需要分开处理；
        if (isNewMedia) {
            map.put("gateCheckB", applyAmount >= definedMoneyNew);
            map.put("gateCheckC", false);
        } else {
            map.put("gateCheckB", false);
            map.put("gateCheckC", applyAmount >= mediaRefundMoneyOld);
        }
        // 额外处理，用于获取业务员对应公司的财务总监审批人；
        map.put("company", outgo.getCompanyCode());
        //消息发起人
        map.put("messageUserName", outgo.getApplyName());
        //消息标题
        map.put("messageTitle", outgo.getTitle());
        return startProcess(map, IProcess.PROCESS_MEDIAREFUND, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    @Override
    public String workupMediaRefundProcess(Outgo outgo, int urgencyLevel, Map<String, Object> map) {
        // 设置第一个网关，判断流程申请人是否新媒介；
        boolean isNewMedia = StringUtils.equals(IConst.DEPT_TYPE_XMT, userService.getMJType(Integer.parseInt(String.valueOf(map.get("userId")))));
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", isNewMedia);
        // 获取系统定义的新媒介申请的网关金额；
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneyNew"));
        // 获取系统定义的非新媒介申请的网关金额；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
        // 设置第二个网关，判断金额是否符合要求，两种媒介的网关金额不一致，需要分开处理；
        if (isNewMedia) {
            map.put("gateCheckB", applyAmount >= definedMoneyNew);
            map.put("gateCheckC", false);
        } else {
            map.put("gateCheckB", false);
            map.put("gateCheckC", applyAmount >= mediaRefundMoneyOld);
        }
        // 额外处理，用于获取业务员对应公司的财务总监审批人；
        map.put("company", outgo.getCompanyCode());
        return startProcess(map, IProcess.PROCESS_MEDIAREFUND, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());

    }

    /**
     * 发起自媒体媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addSelfMediaRefundProcess(Outgo outgo, int urgencyLevel,Integer mediaType,Boolean configurationProcess) {
        Map<String, Object> map = new HashMap<>();
        // 获取系统定义的新媒介是否需要主管审批的申请的网关金额；
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneySelf"));
        // 获取系统定义的新媒介申请的网关金额；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
        // 设置第一个网关，判断金额是否小于需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", applyAmount <= 2000);
        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
        if (CollectionUtils.isNotEmpty(sc)) {
            if (sc.contains(outgo.getCompanyCode())) {
                map.put("gateCheckB", true);
            } else {
                // 设置第二个网关，判断金额是否大于1000
                map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

            }
        } else {
            // 设置第二个网关，判断金额是否大于1000
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

        }

        if (outgo.getInvoiceFlag() != null) {
            map.put("gateCheckE", outgo.getInvoiceFlag() == 1);
        }
        map.put("messageUserName", outgo.getApplyName());
        //消息标题
        map.put("messageTitle", outgo.getTitle());
        //媒体板块id
        map.put("meidaTypeId", outgo.getMediaTypeId());
        map.put("mediaType", mediaType);
        map.put("configurationProcess",configurationProcess);
        map.put("money", outgo.getApplyAmount());
        if (outgo.getMediaGroupLeader() != null) {
            map.put("nextUser", outgo.getMediaGroupLeader());
            map.put("nextUserName", outgo.getMediaGroupLeaderName());
            map.put("nextUserDept", outgo.getMediaGroupLeaderDept());
        }
        map.put("company", outgo.getCompanyCode());
        return startProcess(map, IProcess.PROCESS_SELF_MEDIAREFUND, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    /**
     * 发起报纸请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String newspaperOutgo(Outgo outgo, int urgencyLevel, Integer mediaType,Boolean configurationProcess) {
        Map<String, Object> map = new HashMap<>();
        // 获取系统定义的是否有媒介组长 、部长审核的金额，报纸是500
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneySelf"));
        // 获取系统定义的新媒介申请的网关金额 5000 ；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyNew"));
        // 设置第一个网关，判断金额是否小于等于500需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", applyAmount <= 2000);
        // 设置第二个网关，判断金额是否大于5000,如果稿件的业务员是htls gateCheckB 直接返回true
        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
        if (CollectionUtils.isNotEmpty(sc)) {
            if (sc.contains(outgo.getCompanyCode())) {
                map.put("gateCheckB", true);
            } else {
                map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
            }
        } else {
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

        }
        if (outgo.getInvoiceFlag() != null) {
            map.put("gateCheckE", outgo.getInvoiceFlag() == 1);
        }
        map.put("messageUserName", outgo.getApplyName());
        //消息标题
        map.put("messageTitle", outgo.getTitle());
        map.put("configurationProcess",configurationProcess);
        map.put("mediaType", mediaType);
        map.put("money", outgo.getApplyAmount());
        if (outgo.getMediaGroupLeader() != null) {
            map.put("nextUser", outgo.getMediaGroupLeader());
            map.put("nextUserName", outgo.getMediaGroupLeaderName());
            map.put("nextUserDept", outgo.getMediaGroupLeaderDept());
        }
        map.put("company", outgo.getCompanyCode());
        return startProcess(map, IProcess.PROCESS_NEWSPAPEROUTGO, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    @Override
    public String workupNewspaperOutgo(Outgo outgo, int urgencyLevel, Map<String, Object> map ,boolean configurationProcess) {
        // 获取系统定义的是否有媒介组长 、部长审核的金额，报纸是500
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneySelf"));
        // 获取系统定义的新媒介申请的网关金额 5000 ；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyNew"));
        // 设置第一个网关，判断金额是否小于等于500需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", applyAmount <= definedMoneyNew);
        map.put("configurationProcess",configurationProcess);
        // 设置第二个网关，判断金额是否大于5000
        // 设置第二个网关，判断金额是否大于5000,如果稿件的业务员是htls gateCheckB 直接返回true
        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
        if (CollectionUtils.isNotEmpty(sc)) {
            if (sc.contains(outgo.getCompanyCode())) {
                map.put("gateCheckB", true);
            } else {
                map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
            }
        } else {
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
        }

        map.put("money", outgo.getApplyAmount());
        return startProcess(map, IProcess.PROCESS_NEWSPAPEROUTGO, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    /**
     * 发起新媒体请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String newMediaOutgo(Outgo outgo, int urgencyLevel, Integer mediaType,Boolean configurationProcess) {
        Map<String, Object> map = new HashMap<>();
        // 获取系统定义的是否有媒介组长 、部长审核的金额，新媒体是1000
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
        // 获取系统定义的新媒介申请的网关金额 5000 ；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyNew"));
        // 设置第一个网关，判断金额是否小于等于500需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", applyAmount <= 2000);

        // 设置第二个网关，判断金额是否大于5000,如果稿件的业务员是htls gateCheckB 直接返回true
        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
        if (CollectionUtils.isNotEmpty(sc)) {
            if (sc.contains(outgo.getCompanyCode())) {
                map.put("gateCheckB", true);
            } else {
                map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

            }
        } else {
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

        }
        if (outgo.getInvoiceFlag() != null) {
            map.put("gateCheckE", outgo.getInvoiceFlag() == 1);
        }
        map.put("messageUserName", outgo.getApplyName());
        //消息标题
        map.put("messageTitle", outgo.getTitle());
        map.put("mediaType", mediaType);
        map.put("money", outgo.getApplyAmount());
        map.put("configurationProcess",configurationProcess);
        if (outgo.getMediaGroupLeader() != null) {
            map.put("nextUser", outgo.getMediaGroupLeader());
            map.put("nextUserName", outgo.getMediaGroupLeaderName());
            map.put("nextUserDept", outgo.getMediaGroupLeaderDept());
        }
        map.put("company", outgo.getCompanyCode());
        return startProcess(map, IProcess.PROCESS_MEDIAREFUND, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    @Override
    public String workNewMediaOutgo(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean configurationProcess) {
        // 获取系统定义的是否有媒介组长 、部长审核的金额，新媒体是1000
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
        // 获取系统定义的新媒介申请的网关金额 5000 ；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyNew"));
        // 设置第一个网关，判断金额是否小于等于500需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", applyAmount <= definedMoneyNew);

        // 设置第二个网关，判断金额是否大于5000,如果稿件的业务员是htls gateCheckB 直接返回true
        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
        if (CollectionUtils.isNotEmpty(sc)) {
            if (sc.contains(outgo.getCompanyCode())) {
                map.put("gateCheckB", true);
            } else {
                // 设置第二个网关，判断金额是否大于5000
                map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

            }
        } else {
            // 设置第二个网关，判断金额是否大于5000
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
        }
        map.put("configurationProcess",configurationProcess);
        map.put("money", outgo.getApplyAmount());
        if (outgo.getMediaGroupLeader() != null) {
            map.put("nextUser", outgo.getMediaGroupLeader());
            map.put("nextUserName", outgo.getMediaGroupLeaderName());
            map.put("nextUserDept", outgo.getMediaGroupLeaderDept());
        }
        return startProcess(map, IProcess.PROCESS_MEDIAREFUND, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    /**
     * 发起网络请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String networkOutgo(Outgo outgo, int urgencyLevel, Integer mediaType ,Boolean configurationProcess) {
        Map<String, Object> map = new HashMap<>();
        // 获取系统定义的是否有媒介组长 、部长审核的金额，网络是500
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneySelf"));
        // 获取系统定义的新媒介申请的网关金额 1000 ；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
        // 设置第一个网关，判断金额是否小于等于500需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", applyAmount <= 2000);
        // 设置第二个网关，判断金额是否大于1000

        // 设置第二个网关，判断金额是否大于5000,如果稿件的业务员是htls gateCheckB 直接返回true
        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
        if (CollectionUtils.isNotEmpty(sc)) {
            if (sc.contains(outgo.getCompanyCode())) {
                map.put("gateCheckB", true);
            } else {
                map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

            }
        } else {
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
        }

        if (outgo.getInvoiceFlag() != null) {
            map.put("gateCheckE", outgo.getInvoiceFlag() == 1);
        }

        map.put("messageUserName", outgo.getApplyName());
        //消息标题
        map.put("messageTitle", outgo.getTitle());
        map.put("configurationProcess",configurationProcess);
        map.put("mediaType", mediaType);
        map.put("money", outgo.getApplyAmount());
        if (outgo.getMediaGroupLeader() != null) {
            map.put("nextUser", outgo.getMediaGroupLeader());
            map.put("nextUserName", outgo.getMediaGroupLeaderName());
            map.put("nextUserDept", outgo.getMediaGroupLeaderDept());
        }
        map.put("company", outgo.getCompanyCode());
        return startProcess(map, IProcess.PROCESS_MEDIAREFUND, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    /**
     * 发起项目管理流程
     *
     * @param project      实体类
     * @param urgencyLevel 审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @param nextUser     下一个审核人id
     * @param nextUserName 下一个审核人名
     * @param nextUserDept 下个审核人部门
     * @return
     */
    @Override
    public String addProjctProcess(Project project, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nextUser", nextUser);
        map.put("nextUserName", nextUserName);
        map.put("nextUserDept", nextUserDept);
        map.put("messageTitle", project.getName());
        map.put("company",project.getCompanyCode());
        //消息发起人
        map.put("messageUserName", project.getApplyName());
        return startProcess(map, IProcess.PROCESS_PROJECT, String.valueOf(project.getId()), project.getName(), project.getTaskId(), urgencyLevel, project.getApplyTime());
    }

    @Override
    public String workupNetworkOutgo(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean configurationProcess) {
        // 获取系统定义的是否有媒介组长 、部长审核的金额，新媒体是1000
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneySelf"));
        // 获取系统定义的新媒介申请的网关金额 5000 ；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
        // 设置第一个网关，判断金额是否小于等于500需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        map.put("gateCheckA", applyAmount <= definedMoneyNew);
        map.put("configurationProcess",configurationProcess);
        // 设置第二个网关，判断金额是否大于5000,如果稿件的业务员是htls gateCheckB 直接返回true
        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
        if (CollectionUtils.isNotEmpty(sc)) {
            if (sc.contains(outgo.getCompanyCode())) {
                map.put("gateCheckB", true);
            } else {
                map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

            }
        } else {
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
        }

        map.put("money", outgo.getApplyAmount());
        return startProcess(map, IProcess.PROCESS_MEDIAREFUND, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }


    /**
     *
     * @param outgo
     * @param urgencyLevel
     * @param mediaType
     *  configurationProcess 是否是特殊配置流程
     * @return
     */
    public String addProcess(Outgo outgo, int urgencyLevel, int mediaType,boolean configurationProcess) {
        Map<String, Object> map = new HashMap<>();
        if (outgo.getMediaGroupLeader() != null) {
            map.put("nextUser", outgo.getMediaGroupLeader());
            map.put("nextUserName", outgo.getMediaGroupLeaderName());
            map.put("nextUserDept", outgo.getMediaGroupLeaderDept());
        }
        // 设置第一个网关，判断金额是否小于需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        // 获取系统定义的新媒介申请的网关金额；
        if (mediaType == 1) {
            double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
            // 设置第二个网关，判断金额是否大于1000
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
        } else {
            // 获取系统定义的新媒介申请的网关金额 5000 ；
            double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyNew"));
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

        }

        if (outgo.getInvoiceFlag() != null) {
            map.put("gateCheckE", outgo.getInvoiceFlag() == 1);
        }
        map.put("configurationProcess",configurationProcess);
        map.put("messageUserName", outgo.getApplyName());
        map.put("money", outgo.getApplyAmount());
        //消息标题
        map.put("messageTitle", outgo.getTitle());
        map.put("company", outgo.getCompanyCode());
        map.put("mediaType", mediaType);
        return startProcess(map, IProcess.PROCESS_HTOUTPLEASETYPE, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());

    }

    @Override
    public String workupCompanyOutgo(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean workupCompanyOutgo) {
        // 设置第一个网关，判断金额是否小于需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        // 获取系统定义的新媒介申请的网关金额；
        if ("1".equals(map.get("mediaType"))) {
            double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
            // 设置第二个网关，判断金额是否大于1000
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
        } else {
            // 获取系统定义的新媒介申请的网关金额 5000 ；
            double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyNew"));
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);
        }
        map.put("money", outgo.getApplyAmount());
        map.put("workupCompanyOutgo",workupCompanyOutgo);
        return startProcess(map, IProcess.PROCESS_HTOUTPLEASETYPE, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    /**
     * 发起烂账审核流程
     *
     */
    @Override
    public String addAccountsMessProcess(AccountsMess accountsMess,int urgencyLevel){
        Map<String,Object> map = new HashMap<>();
        map.put("company",accountsMess.getCompanyCode());
        map.put("messageUserName",accountsMess.getApplyName());
        map.put("messageTitle",accountsMess.getTitle());
        return startProcess(map,IProcess.PROCESS_MESS,String.valueOf(accountsMess.getId()),accountsMess.getTitle(),accountsMess.getTaskId(), urgencyLevel);
    }

    /**
     * 发起河图请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addHtRefundProcess(Outgo outgo, int urgencyLevel) {
        Map<String, Object> map = new HashMap<>();
        if (outgo.getMediaGroupLeader() != null) {
            map.put("nextUser", outgo.getMediaGroupLeader());
            map.put("nextUserName", outgo.getMediaGroupLeaderName());
            map.put("nextUserDept", outgo.getMediaGroupLeaderDept());
        }
        if (outgo.getInvoiceFlag() != null) {
            map.put("gateCheckE", outgo.getInvoiceFlag() == 1);
        }
        map.put("messageUserName", outgo.getApplyName());
        map.put("money", outgo.getApplyAmount());
        //消息标题
        map.put("messageTitle", outgo.getTitle());
        map.put("company", outgo.getCompanyCode());
        return startProcess(map, IProcess.PROCESS_HTOUTPLEASETYPE, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    @Override
    public String workupHtRefundProcess(Outgo outgo, int urgencyLevel, Map<String, Object> map) {
        map.put("money", outgo.getApplyAmount());
        return startProcess(map, IProcess.PROCESS_HTOUTPLEASETYPE, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }

    @Override
    public String workupSelfMediaRefundProcess(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean configurationProcess) {
        // 获取系统定义的新媒介是否需要主管审批的申请的网关金额；
        double definedMoneyNew = Double.parseDouble(SpringUtils.get("mediaRefundMoneySelf"));
        // 获取系统定义的新媒介申请的网关金额；
        double mediaRefundMoneyOld = Double.parseDouble(SpringUtils.get("mediaRefundMoneyOld"));
        // 设置第一个网关，判断金额是否小于需要组长审批的金额；
        double applyAmount = outgo.getApplyAmount();
        map.put("money", outgo.getApplyAmount());
        map.put("gateCheckA", applyAmount <= definedMoneyNew);
        map.put("configurationProcess",configurationProcess);
        // 设置第二个网关，判断金额是否大于1000

        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
        if (CollectionUtils.isNotEmpty(sc)) {
            if (sc.contains(outgo.getCompanyCode())) {
                map.put("gateCheckB", true);
            } else {
                // 设置第二个网关，判断金额是否大于1000
                map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

            }
        } else {
            // 设置第二个网关，判断金额是否大于1000
            map.put("gateCheckB", applyAmount > mediaRefundMoneyOld);

        }

        return startProcess(map, IProcess.PROCESS_SELF_MEDIAREFUND, String.valueOf(outgo.getId()), outgo.getTitle(), outgo.getTaskId(), urgencyLevel, outgo.getExpertPayTime());
    }


    /**
     * 发起退款申请审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param refund：需要审核的退款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addRefundProcess(Refund refund, int urgencyLevel) {
        Map<String, Object> map = new HashMap<>();
        // 获取系统定义的退款金额；
        double definedMoney = Double.parseDouble(SpringUtils.get("refundMoney"));
        // 设置网关，判断金额是否符合要求；
        map.put("gateCheckA", refund.getApplyAmount() >= definedMoney);
        map.put("messageUserName", refund.getApplyName());
        //消息标题
        map.put("messageTitle", refund.getTitle());
        map.put("money", refund.getApplyAmount());
        return startProcess(map, IProcess.PROCESS_REFUND, String.valueOf(refund.getId()), refund.getTitle(), refund.getTaskId(), urgencyLevel, refund.getExpertPayTime());
    }

    /**
     * 发起财务提成流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param commission：需要审核的提成对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addCommissionProcess(Commission commission, int urgencyLevel) {
        return startProcess(new HashMap<>(), IProcess.PROCESS_ROYALTY, String.valueOf(commission.getId()), commission.getName(), commission.getTaskId(), urgencyLevel);
    }

    /**
     * 发起员工录用流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param employEntry：需要审核的入职申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @param nextUser：下个节点的审核人ID；
     * @param nextUserName：下个节点的审核人姓名；
     * @param nextUserDept：下个节点的审核人部门ID；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addEmployProcess(EmployEntry employEntry, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nextUser", nextUser);
        map.put("nextUserName", nextUserName);
        map.put("nextUserDept", nextUserDept);
        //消息发起人
        map.put("messageUserName", employEntry.getEntryName());
        //消息标题
        map.put("messageTitle", null);
        return startProcess(map, IProcess.PROCESS_EMPLOY, String.valueOf(employEntry.getEntryId()), employEntry.getEntryName(), employEntry.getTaskId(), urgencyLevel);
    }

    /**
     * 发起员工转正流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param employee：需要审核的员工对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @param nextUser：下个节点的审核人ID；
     * @param nextUserName：下个节点的审核人姓名；
     * @param nextUserDept：下个节点的审核人部门ID；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addFormalProcess(Employee employee, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nextUser", nextUser);
        map.put("nextUserName", nextUserName);
        map.put("nextUserDept", nextUserDept);
        //消息发起人
        map.put("messageUserName", employee.getEmpName());
        //消息标题
        map.put("messageTitle", null);
        return startProcess(map, IProcess.PROCESS_FORMAL, String.valueOf(employee.getEmpId()), employee.getEmpName(), employee.getTaskId(), urgencyLevel);
    }

    /**
     * 发起员工离职流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param employee：需要审核的员工对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @param nextUser：下个节点的审核人ID；
     * @param nextUserName：下个节点的审核人姓名；
     * @param nextUserDept：下个节点的审核人部门ID；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addLeaveProcess(Employee employee, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nextUser", nextUser);
        map.put("nextUserName", nextUserName);
        map.put("nextUserDept", nextUserDept);
        //消息发起人
        map.put("messageUserName", employee.getEmpName());
        //消息标题
        map.put("messageTitle", null);
        return startProcess(map, IProcess.PROCESS_LEAVE, String.valueOf(employee.getEmpId()), employee.getEmpName(), employee.getTaskId(), urgencyLevel);
    }

    /**
     * 发起员工调岗流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param employee：需要审核的员工对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @param nextUser：下个节点的审核人ID；
     * @param nextUserName：下个节点的审核人姓名；
     * @param nextUserDept：下个节点的审核人部门ID；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addTransferProcess(Employee employee, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept,Integer afterDept, int tranId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nextUser", nextUser);
        map.put("nextUserName", nextUserName);
        map.put("nextUserDept", nextUserDept);
        //消息发起人
        map.put("messageUserName", employee.getEmpName());
        //消息标题
        map.put("messageTitle",null);
        map.put("afterDept",afterDept);
        //添加员工ID
        map.put("empId", String.valueOf(employee.getEmpId()));
        return startProcess(map, IProcess.PROCESS_TRANSFER, String.valueOf(employee.getEmpId())+"_"+tranId, employee.getEmpName(), employee.getTaskId(), urgencyLevel);
    }

    /**
     * 发起员工交接流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param employee：需要审核的员工对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @param nextUser：下个节点的审核人ID；
     * @param nextUserName：下个节点的审核人姓名；
     * @param nextUserDept：下个节点的审核人部门ID；
     * @param type：交接流程的类型，参考com.qinfei.qferp.utils.IEmployConnect；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addHandOverProcess(Employee employee, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept, int type, int conId) {
        // 根据类型获取对应的流程定义；
        Integer processId = connectService.getHandOverProcessId(type);
        if (processId == null) {
            return "交接类型不存在。";
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("nextUser", nextUser);
            map.put("nextUserName", nextUserName);
            map.put("nextUserDept", nextUserDept);
            map.put("conId", conId);
            //消息发起人
            map.put("messageUserName", employee.getEmpName());
            //消息标题
            map.put("messageTitle",null);
            //添加员工ID
            map.put("empId", String.valueOf(employee.getEmpId()));
            return startProcess(map, processId, String.valueOf(employee.getEmpId())+"_"+conId, employee.getEmpName(), employee.getTaskId(), urgencyLevel);
        }
    }

    /**
     * 发起请假流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param leave：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addVocationProcess(Administrative leave, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept) {
        User user = AppUtil.getUser();
        if (user == null) {
            throw new QinFeiException(1002, "请先登录！");
        }
        HashMap<String, Object> map = new HashMap<>();
        // 设置网关，判断职务是否符合要求；
        boolean isDeptLeader = userService.isDeptLeader(leave.getEmpId(), leave.getDeptId());
        map.put("gateCheck", isDeptLeader);
        //如果是部门负责人并且是组以上的部门才不需要经过政委审核
        if (isDeptLeader && user.getDept().getLevel() < 4) {
            map.put("gateCheckA", false);
        } else {
            List<User> userList = deptZwMapper.listUserByParam(null, leave.getDeptId());
            //如果没有政委也不需要经过政委审核
            if (CollectionUtils.isNotEmpty(userList)) {
                //此处赋值，可以减少在ZwTaskHandler类中再做查询部门政委接口调用
                nextUser = nextUser != null ? nextUser : userList.get(0).getId();
                nextUserName = StringUtils.isEmpty(nextUserName) ? userList.get(0).getName() : nextUserName;
                nextUserDept = nextUserDept != null ? nextUserDept : userList.get(0).getDeptId();
                map.put("gateCheckA", true);
                map.put("nextUser", nextUser);
                map.put("nextUserName", nextUserName);
                map.put("nextUserDept", nextUserDept);
            } else {
                map.put("gateCheckA", false);
            }
        }

        // leave.getDeptId()));
        // 根据请假的天数判断启用的流程；
        int leaveDay = leave.getLeaveDay();
        int process;
        if (leaveDay >= 3) {
            process = IProcess.PROCESS_VOCATION_THREE;
        } else if (leaveDay == 2) {
            process = IProcess.PROCESS_VOCATION_TWO;
        } else {
            process = IProcess.PROCESS_VOCATION_ONE;
        }
        //消息发起人
        map.put("messageUserName", leave.getEmpName());
        //消息标题
        map.put("messageTitle", leave.getTitle());
        return startProcess(map, process, String.valueOf(leave.getId()), leave.getTitle(), leave.getTaskId(), urgencyLevel);
    }

    /**
     * 发起退稿流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param drop：需要审核的退稿对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addManuscriptProcess(Drop drop, int urgencyLevel) {
        Map map = new HashMap();
        //消息发起人
        map.put("messageUserName", drop.getApplyName());
        //消息标题
        map.put("messageTitle", drop.getTitle());
        map.put("money", drop.getPayAmount());
        map.put("company",drop.getCompanyCode());
        return startProcess(map, IProcess.PROCESS_MANUSCRIPT, String.valueOf(drop.getId()), drop.getTitle(), drop.getTaskId(), urgencyLevel);
    }

    /**
     * 发起费用报销流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param reimbursement：需要审核的费用报销对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addReimbursementProcess(Reimbursement reimbursement, int urgencyLevel) {
        return startProcess(getReimbursermentData(reimbursement.getTotalMoney(), reimbursement.getOutAccountId(), reimbursement.getApplyName(), reimbursement.getTitle()), IProcess.PROCESS_REIMBURSEMENT, String.valueOf(reimbursement.getId()), reimbursement.getTitle(), reimbursement.getTaskId(), urgencyLevel, null);
    }

    /**
     * 发起外出申请报销流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outWork：需要审核的外出申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addOutWorkProcess(Administrative adm, AdministrativeOutWork outWork, int urgencyLevel) {
        Map<String, Object> map = new HashMap<>();
        // 设置第一个网关，判断流程申请人的职务是否符合要求；
        boolean isCEO = userService.getCEOFlag(AppUtil.getUser().getId());
        map.put("gateCheckA", isCEO);
        Double days = outWork.getDays();
        if (isCEO) {
            map.put("gateCheckB", true);
        } else {
            boolean gateCheckB = days == null || days.doubleValue() > 1;
            // 设置第二个网关，判断时间是否符合要求；
            map.put("gateCheckB", gateCheckB);
        }
        //消息发起人
        map.put("messageUserName", outWork.getEmpName());
        //消息标题
        map.put("messageTitle", outWork.getTitle());
        return startProcess(map, IProcess.PROCESS_OUTWORK, String.valueOf(adm.getId()), adm.getTitle(), adm.getTaskId(), urgencyLevel);
    }

    /**
     * 发起加班申请流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param overTimeWork：需要审核的加班申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addOverTimeWorkProcess(Administrative adm, AdministrativeOverTimeWork overTimeWork, int urgencyLevel) {
        Map<String, Object> map = new HashMap<>();
        // 设置第一个网关，判断流程申请人的职务是否符合要求；
        boolean isCEO = userService.getCEOFlag(AppUtil.getUser().getId());
        map.put("gateCheckA", isCEO);
        Double workTime = overTimeWork.getWorkTime();
        boolean gateCheckB = workTime == null || workTime.doubleValue() > 8;
        // 设置第二个网关，判断时间是否符合要求；
        map.put("gateCheckB", gateCheckB);
//        if (isCEO) {
//            map.put("gateCheckB", true);
//        } else {
//            boolean gateCheckB = workTime == null || workTime.doubleValue() > 8;
//            // 设置第二个网关，判断时间是否符合要求；
//            map.put("gateCheckB", gateCheckB);
//        }
        //消息发起人
        map.put("messageUserName", overTimeWork.getEmpName());
        //消息标题
        map.put("messageTitle", overTimeWork.getTitle());
        return startProcess(map, IProcess.PROCESS_WORKOVERTIME, String.valueOf(adm.getId()), adm.getTitle(), adm.getTaskId(), urgencyLevel);
    }

    /**
     * 发起出差申请流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param adm
     * @param onBusiness：需要审核的出差申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return
     */
    @Override
    public String addOnbusinessWorkProcess(Administrative adm, AdministrativeOnBusiness onBusiness, int urgencyLevel) {
        Map<String, Object> map = new HashMap<>();
        // 设置第一个网关，判断流程申请人的职务是否符合要求；
        map.put("gateCheckA", userService.isDeptLeader(adm.getEmpId(), adm.getDeptId()));
        Integer isExpense = onBusiness.getIsExpense() == null ? 0 : onBusiness.getIsExpense();
        boolean gateCheckB = isExpense == 0 || isExpense.intValue() > 0;
        // 设置第二个网关，判断是否借款；
        map.put("gateCheckB", gateCheckB);
        //消息发起人
        map.put("messageUserName", onBusiness.getEmpName());
        //消息标题
        map.put("messageTitle", onBusiness.getTitle());
        return startProcess(map, IProcess.PROCESS_ONBUSINESS, String.valueOf(adm.getId()), adm.getTitle(), adm.getTaskId(), urgencyLevel);
    }

    /**
     * 发起出差申请流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param adm
     * @param userBusinessPlan：需要审核的出差申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return
     */
    @Override
    public String addOnbusiness(Administrative adm, UserBusinessPlan userBusinessPlan, int urgencyLevel) {
        Map<String, Object> map = new HashMap<>();
        // 设置第一个网关，判断流程申请人的职务是否符合要求；
        map.put("gateCheckA", userService.isDeptLeader(adm.getEmpId(), adm.getDeptId()));
        //消息发起人
        map.put("messageUserName", userBusinessPlan.getApplyName());
        //消息标题
        map.put("messageTitle", userBusinessPlan.getTitle());
        return startProcess(map, IProcess.PROCESS_ONBUSINESS, String.valueOf(adm.getId()), adm.getTitle(), adm.getTaskId(), urgencyLevel);
    }

    /**
     * 发起绩效考核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param performanceScores：需要审核的绩效考核集合对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addPerformanceProcess(List<PerformanceScore> performanceScores, int urgencyLevel) {
        // 先校验数据；
        if (performanceScores == null || performanceScores.size() <= 0) {
            return null;
        } else {
            // 循环获取数据；
            HashMap<String, Object> map;
            for (PerformanceScore performanceScore : performanceScores) {
                map = new HashMap<>();
                map.put("gateCheck", userService.isDeptLeader(performanceScore.getUserId(), performanceScore.getDeptId()));
                map.put("nextUser", performanceScore.getUserId());
                map.put("nextUserName", performanceScore.getUserName());
                map.put("nextUserDept", performanceScore.getDeptId());
                //消息发起人
                map.put("messageUserName", AppUtil.getUser().getName());
                //消息标题
                map.put("messageTitle", performanceScore.getUserName()+"绩效考核");
                startProcess(map, IProcess.PROCESS_PERFORMANCE, String.valueOf(performanceScore.getScoreId()), performanceScore.getUserName(), performanceScore.getTaskId(), urgencyLevel);
            }
            return "发起流程成功！";
        }
    }

    /**
     * 发起会议室考核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param meet：需要审核的绩效考核集合对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    @Override
    public String addMeetRoomProcess(MeetingManagement meet, int urgencyLevel) {
        // 先校验数据；
        if (meet == null)
            return null;
        // 循环获取数据；
        HashMap<String, Object> map;
        map = new HashMap<>();
        map.put("nextUser", meet.getReviewerId());
        map.put("nextUserName", meet.getReviewerName());
        map.put("nextUserDept", meet.getReviewerDeptId());
        //消息发起人
        map.put("messageUserName", meet.getCreateName());
        //消息标题
        map.put("messageTitle", meet.getConferenceRoomName());
        startProcess(map, IProcess.PROCESS_MEETINGROOM, String.valueOf(meet.getId()), meet.getConferenceRoomName(), meet.getTaskId(), urgencyLevel);
        return "操作完成。";
    }

    @Override
    public String addStandardizedCompanyProcess(StandardizedCompany standardizedCompany, int urgencyLevel) {
        Map<String, Object> map = new HashMap<>();
        User user = userService.getById(standardizedCompany.getApplyId());
        //消息发起人
        map.put("messageUserName", AppUtil.getUser().getName());
        //消息标题
        map.put("messageTitle", standardizedCompany.getCompanyName());
        map.put("companyName", standardizedCompany.getCompanyName());
        map.put("deptId", user.getDeptId().toString());
        map.put("companyCode", user.getCompanyCode());
        return startProcess(map, IProcess.PROCESS_STANDARDIZED_COMPANY, String.valueOf(standardizedCompany.getId()), standardizedCompany.getCompanyName(), standardizedCompany.getTaskId(), urgencyLevel);
    }

    /**
     * 批量审核流程数据：1、流程更新；2、数据状态更新；3、审核对象的代办事项和消息中添加数据；
     *
     * @param taskIds：需要审核的任务ID数组；
     * @param desc：审核备注信息；
     * @param agree                ：是否同意，true为是，false为否；
     * @return ：操作结果描述；
     */
    @Override
    @Transactional
    public String approveProcess(String[] taskIds, String desc, boolean agree) {
        return approveProcess(taskIds, desc, agree, null, null, null);
    }

    @Override
    @Transactional
    public String refused(Map<String, Object> map) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("agree", Boolean.parseBoolean(map.get("agree").toString()));
        // 设置审核备注信息；
        StringBuilder approveDesc = new StringBuilder();
        if (Boolean.parseBoolean(map.get("agree").toString())) {
            approveDesc.append("<b style='color:green;'>【同意】</b>");
        } else {
            approveDesc.append("<b style='color:red;'>【拒绝】</b>");
        }
        // 拼接审核备注；
        if (!StringUtils.isEmpty(map.get("desc").toString())) {
            approveDesc.append("：").append(map.get("desc").toString());
            // 审核备注不为空也存储；
            map.put("desc", map.get("desc").toString());
        }
        // 获取当前登录用户；
        String userId = AppUtil.getUser().getId().toString();
        String proList = map.get("listStr").toString();
        try {
            String[] process = proList.split(",");
                for (int i = 0; i < process.length; i++) {
                    String[] d = process[i].split(":");
                    String taskId1 = d[0];
                    String type1 = d[1];
                    //请款流程的流程类型（3，24,22）
                    if (Integer.parseInt(type1) == 3 || Integer.parseInt(type1) == 24 || Integer.parseInt(type1) == 22) {
                            // 设置审核备注信息；
                            String instId = taskService.createTaskQuery().taskId(taskId1).singleResult().getProcessInstanceId();
                                runtimeService.setVariable(instId, "gateCheckD", true);
                            // 获取审核人；
                            String approveUser = taskService.getVariable(taskId1, "approveUser", String.class);
                        // 验证审核权限；
                        if (StringUtils.equals(userId, approveUser)) {
                                // 更新流程的历史信息；
                                managementService.executeCommand(new UpdateHistoryTaskCommand(taskId1, taskService.getVariable(taskId1, "approveUserName", String.class), approveUser, approveDesc.toString()));
                                taskService.complete(taskId1, map1);
                        }else {
                            throw new QinFeiException(1002, "审核人不一致,请刷新页面");
                        }
                    } else {
                            // 设置审核备注信息；
                            String instId = taskService.createTaskQuery().taskId(taskId1).singleResult().getProcessInstanceId();
                            if(Integer.parseInt(type1)==4){
                                runtimeService.setVariable(instId, "gateCheckB", true);
                            }else {
                                runtimeService.setVariable(instId, "gateCheckC", true);
                            }
                            // 获取审核人；
                            String approveUser = taskService.getVariable(taskId1, "approveUser", String.class);

                        if (StringUtils.equals(userId, approveUser)){
                            // 更新流程的历史信息；
                            managementService.executeCommand(new UpdateHistoryTaskCommand(taskId1, taskService.getVariable(taskId1, "approveUserName", String.class), approveUser, approveDesc.toString()));
                            taskService.complete(taskId1, map1);
                        }else {
                            throw new QinFeiException(1002, "审核人不一致,请刷新页面");
                        }
                    }
                }
            return "操作成功";
        } catch (FlowableObjectNotFoundException e) {
            return "该审批已完成，请刷新页面。";
        }
    }

    /**
     *
     * @param protect 客户部傲虎流程
     * @param urgencyLevel
     * @returnpro
     */
    @Override
    public String addProtectProcess(CrmCompanyProtect protect, int urgencyLevel) {
        HashMap<String, Object> map = new HashMap<>();
//        map.put("nextUser", nextUser);
//        map.put("nextUserName", nextUserName);
//        map.put("nextUserDept", nextUserDept);
        map.put("messageTitle", protect.getCompanyName());
        //消息发起人
        map.put("messageUserName", protect.getApplyName());
        return startProcess(map, IProcess.PROCESS_PROTECT, String.valueOf(protect.getId()), protect.getCompanyName(), protect.getTaskId(), urgencyLevel, protect.getApplyTime());
    }


    @Override
    public String approveProcess(String[] taskIds, String desc, boolean agree, boolean nextGatewayValue) {
        return approveProcess(taskIds, desc, agree, null, null, null, nextGatewayValue);
    }

    /**
     * 批量审核流程数据：1、流程更新；2、数据状态更新；3、审核对象的代办事项和消息中添加数据；
     *
     * @param taskIds：需要审核的任务ID数组；
     * @param desc：审核备注信息；
     * @param agree                      ：是否同意，true为是，false为否；
     * @param nextUser：下个节点的审核人ID；
     * @param nextUserName：下个节点的审核人姓名；
     * @param nextUserDept：下个节点的审核人部门ID；
     * @return ：操作结果描述；
     */
    @Override
    public String approveProcess(String[] taskIds, String desc, boolean agree, Integer nextUser, String nextUserName, Integer nextUserDept) {
        Map<String, Object> map = new HashMap<>();
        map.put("agree", agree);
        // 如果同意审核，审核人不为空，则保存数据至流程变量库；
        if (agree && nextUser != null && !StringUtils.isEmpty(nextUserName) && nextUserDept != null) {
            map.put("nextUser", nextUser);
            map.put("nextUserName", nextUserName);
            map.put("nextUserDept", nextUserDept);
        }
        // 获取当前登录用户；
        String userId = AppUtil.getUser().getId().toString();
        String approveUser;
        // 设置审核备注信息；
        StringBuilder approveDesc = new StringBuilder();
        if (agree) {
            approveDesc.append("<b style='color:green;'>【同意】</b>");
        } else {
            approveDesc.append("<b style='color:red;'>【拒绝】</b>");
        }
        // 拼接审核备注；
        if (!StringUtils.isEmpty(desc)) {
            approveDesc.append("：").append(desc);
            // 审核备注不为空也存储；
            map.put("desc", desc);
        }
        try {
            for (String taskId : taskIds) {
                // 获取审核人；
                approveUser = taskService.getVariable(taskId, "approveUser", String.class);
                // 验证审核权限；
                if (StringUtils.equals(userId, approveUser)) {
                    // 更新流程的历史信息；
                    managementService.executeCommand(new UpdateHistoryTaskCommand(taskId, taskService.getVariable(taskId, "approveUserName", String.class), approveUser, approveDesc.toString()));
                    taskService.complete(taskId, map);
                } else {
                    throw new QinFeiException(1002, "审核人不一致,请刷新页面");
                    // 只要有一个审核异常即表示失败，退出审核；
//                    return "该审批已完成。";
                }
            }
            return "操作成功。";
        } catch (FlowableObjectNotFoundException e) {
            return "该审批已完成，请刷新页面。";
        }
    }

    @Override
    public String approveProcess(String[] taskIds, String desc, boolean agree, Integer nextUser, String nextUserName, Integer nextUserDept, Boolean nextGatewayValue) {
        Map<String, Object> map = new HashMap<>();
        map.put("agree", agree);
        // 如果同意审核，审核人不为空，则保存数据至流程变量库；
        if (agree && nextUser != null && !StringUtils.isEmpty(nextUserName) && nextUserDept != null) {
            map.put("nextUser", nextUser);
            map.put("nextUserName", nextUserName);
            map.put("nextUserDept", nextUserDept);
        }
        //如果同意审核，则保存下一个网关值到流程变量库
        if (agree && nextGatewayValue != null) {
            map.put("nextGatewayValue", nextGatewayValue);
        } else {
            map.put("nextGatewayValue", true);
        }
        // 获取当前登录用户；
        String userId = AppUtil.getUser().getId().toString();
        String approveUser;
        // 设置审核备注信息；
        StringBuilder approveDesc = new StringBuilder();
        if (agree) {
            approveDesc.append("<b style='color:green;'>【同意】</b>");
        } else {
            approveDesc.append("<b style='color:red;'>【拒绝】</b>");
        }
        // 拼接审核备注；
        if (!StringUtils.isEmpty(desc)) {
            approveDesc.append("：").append(desc);
            // 审核备注不为空也存储；
            map.put("desc", desc);
        }
        try {
            for (String taskId : taskIds) {
                // 获取审核人；
                approveUser = taskService.getVariable(taskId, "approveUser", String.class);
                // 验证审核权限；
                if (StringUtils.equals(userId, approveUser)) {
                    // 更新流程的历史信息；
                    managementService.executeCommand(new UpdateHistoryTaskCommand(taskId, taskService.getVariable(taskId, "approveUserName", String.class), approveUser, approveDesc.toString()));
                    taskService.complete(taskId, map);
                } else {
                    // 只要有一个审核异常即表示失败，退出审核；
                    throw new QinFeiException(1002, "审核人不一致,请刷新页面！");
//                    return "该审批已完成。";
                }
            }
            return "操作成功。";
        } catch (FlowableObjectNotFoundException e) {
            throw new QinFeiException(10000, "该审批已完成，请刷新页面。");
//            return "该审批已完成，请刷新页面。";
        }
    }


    /**
     * 驳回到任意节点
     * @param
     * @return
     * @throws Exception
     */

	/*public ResponseData backToStep(String taskId, String nextId, String userCode, boolean agree, Integer nextUser, String nextUserName, Integer nextUserDept) throws Exception {
		ResponseData data = ResponseData.ok();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		String processInstanceId = task.getProcessInstanceId();
		ChangeActivityStateBuilder changeActivityStateBuilder;

		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(nextId);


		FlowElement distActivity = processDefinitionUtils.findFlowElementById(task.getProcessDefinitionId(), backVo.getDistFlowElementId());
		FlowElement distActivity = processDefinitionUtils.findFlowElementById(task.getProcessDefinitionId(), backVo.getDistFlowElementId());
		//1. 保存任务信息
		task.setAssignee(userCode);
		taskService.saveTask(task);
		//2. 如果上一个节点是提交者的话要处理一下
		if (FlowConstant.FLOW_SUBMITTER.equals(pde.getName())) {
			//查找发起人 设置到变量中，以便驳回到提起人的时候能留在提交人这个节点
			ExtendProcinst extendProcinst = this.extendProcinstService.findExtendProcinstByProcessInstanceId(processInstanceId);
			String creator = null;
			if (extendProcinst != null) {
				creator = extendProcinst.getCreator();
				if (StringUtils.isBlank(creator)) {
					creator = extendProcinst.getCurrentUserCode();
				}
			} else {
				ExtendHisprocinst extendHisprocinst = extendHisprocinstService.getExtendHisprocinstByProcessInstanceId(processInstanceId);
				creator = extendHisprocinst.getCreator();
				if (StringUtils.isBlank(creator)) {
					creator = extendHisprocinst.getCurrentUserCode();
				}
			}
			if (StringUtils.isNotBlank(creator)) {
				runtimeService.setVariable(processInstanceId, FlowConstant.FLOW_SUBMITTER_VAR, creator);
			}
		}
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
		List<String> currentActivityIds = new ArrayList<>();
		tasks.forEach(t -> currentActivityIds.add(t.getTaskDefinitionKey()));
		//3. 删除节点信息
		if (!(distActivity instanceof EndEvent)) {
			this.deleteHisActivities((Activity) distActivity, processInstanceId);
		}
		//4. 添加审批意见和修改流程状态
		this.addCommentAndUpdateProcessStatus(backVo, processInstanceId);
		//5.执行驳回操作
		runtimeService.createChangeActivityStateBuilder()
				.processInstanceId(processInstanceId)
				.moveActivityIdsToSingleActivityId(currentActivityIds, backVo.getDistFlowElementId())
				.changeState();
		return data;
	}*/


    /**
     * 流程的撤回；
     *
     * @param taskId：流程任务ID；
     * @return ：操作结果描述；
     */
    @Override
    @Transactional
    public String withdrawProcess(String taskId, Integer itemId) {
        // 确保审核任务还存在；
        TaskQuery taskQuery = taskService.createTaskQuery().taskId(taskId);
        if (taskQuery.count() == 1) {
            // 获取流程提交人的信息；
            String initUser = taskService.getVariable(taskId, "userId", String.class);
            // 获取当前登录用户信息；
            User loginUser = AppUtil.getUser();
            String userId = loginUser.getId().toString();
            String loginUserName = loginUser.getName();

            // 判断登录人是否为媒介部负责人；
            boolean flag = AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && AppUtil.isRoleCode(IConst.ROLE_CODE_BZ);
//            // 获取角色；
//            List<Role> roles = loginUser.getRoles();
//            if (roles != null && roles.size() > 0) {
//                for (Role role : roles) {
//                    if (IConst.ROLE_TYPE_MJ.equals(role.getType()) && IConst.ROLE_CODE_BZ.equals(role.getCode())) {
//                        flag = true;
//                    }
//                }
//            }

            try {
                // 验证权限，流程发起人或媒介部部长可撤回流程；
                if (flag || StringUtils.equals(userId, initUser)) {
                    // 更新流程的历史信息；
                    managementService.executeCommand(new UpdateHistoryTaskCommand(taskId, loginUserName, userId, "<b style='color:blue;'>【撤回】</b>"));
                    // 撤回为驳回操作；
                    Map<String, Object> map = new HashMap<>();
                    map.put("agree", false);
                    taskService.complete(taskId, map);
                    if (itemId != null) {
                        finishItem(itemId);
                    }
                    return "操作完成。";
                } else {
                    return "权限不足，当前流程仅能由【" + taskService.getVariable(taskId, "userName", String.class) + "】或媒介部长撤回。";
                }
            } catch (FlowableObjectNotFoundException e) {
                return "该审批已完成，请刷新页面。";
            }
        } else {
            return "流程未找到，可能已审核完成。";
        }
    }

    /**
     * 流程的删除；
     *
     * @param taskId：流程任务ID；
     * @return ：操作结果描述；
     */
    @Override
    public String deleteProcess(String taskId, Integer itemId) {
        // 确保审核任务还存在；
        TaskQuery taskQuery = taskService.createTaskQuery().taskId(taskId);
        if (taskQuery.count() == 1) {
            // 获取当前登录用户信息；
            User loginUser = AppUtil.getUser();
            String userId = "";
            String loginUserName = "";
            if (loginUser == null) {
                userId = loginUser.getId().toString();
                loginUserName = loginUser.getName();
            }
            try {
                // 删除为驳回操作；
                Map<String, Object> map = new HashMap<>();
                map.put("agree", false);
                // 更新流程的历史信息；
                managementService.executeCommand(new UpdateHistoryTaskCommand(taskId, loginUserName, userId, "<b style='color:darkorange;'>【删除】</b>"));
                taskService.complete(taskId, map);
                if (itemId != null) {
                    finishItem(itemId);
                }
                return "操作完成。";
            } catch (FlowableObjectNotFoundException e) {
                return "该审批已完成，请刷新页面。";
            }
        } else {
            return "流程未找到，可能已审核完成。";
        }
    }

    /**
     * 已审核完成的任务
     *
     * @param map
     * @param pageNum
     * @param pageSize
     * @return
     */

    @Override
    public PageInfo<Map> theApproved(Map map, int pageNum, int pageSize) {
        //移除停留时间筛选
        map.remove("day");
        map.remove("hour");
        map.remove("minute");
        map.remove("stopTimeType");
        PageHelper.startPage(pageNum, pageSize);
        User user = AppUtil.getUser();
        map.put("userId", user.getId());
        List<Map> list = processMapper.theApproved(map);
        for (Map map1 : list) {
            String[] ss = String.valueOf(map1.get("collection")).split(",");
            if (ss != null && ss.length > 0) {
                for (int i = 0; i < ss.length; i++) {
                    String[] ss1 = ss[i].split(":");
                    if (ss1 != null && ss1.length > 0) {
                        if ("expectTime".equals(ss1[0]) || "processDate".equals(ss1[0])) {
                            map1.put(ss1[0], DateUtils.unixTimestampToDate(Long.valueOf(ss1[1])));
                        }else{
                            if(ss1.length > 1){
                                map1.put(ss1[0],ss1[1]);
                            }else {
                                map1.put(ss1[0],"");
                            }
                        }
                    }
                }
            }


        }
        return new PageInfo<>(list);

    }

    /**
     * 分页查询登录人的审核任务列表；
     *
     * @param map：查询参数；
     * @param pageNum：当前页码；
     * @param pageSize：每页显示数量；
     * @return ：分页数据集合；
     */
    @Override
    public PageInfo<Map<String, Object>> listTasks(Map<String, String> map, int pageNum, int pageSize) {
        TaskQuery taskQuery = taskService.createTaskQuery();
        TaskQuery query = taskQuery.taskAssignee(AppUtil.getUser().getId().toString());


        // 流程状态；
        query = query.processVariableValueLessThan("processState", IProcess.PROCESS_REJECT);

        //任务状态不等于 请款回填状态
        query.processVariableValueNotEquals("state", IConst.STATE_MEDIUMBACKFILL);

        //停留时间搜索  day hour minute stopTimeType: 0-等于、1-小于、2-大于、3-小于等于、4-大于等于
        if((map.get("day") != null && !"".equals(map.get("day"))) || (map.get("hour") != null && !"".equals(map.get("hour"))) ||
                (map.get("minute") != null && !"".equals(map.get("minute")))){
            int day = map.get("day") != null && !"".equals(map.get("day")) ? Integer.parseInt(map.get("day")) : 0;
            int hour = map.get("hour") != null && !"".equals(map.get("hour")) ? Integer.parseInt(map.get("hour")) : 0;
            int minute = map.get("minute") != null && !"".equals(map.get("minute")) ? Integer.parseInt(map.get("minute")) : 0;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -day);
            calendar.add(Calendar.HOUR_OF_DAY, -hour);
            calendar.add(Calendar.MINUTE, -minute);
            //由于任务创建时间包含秒，我们计算的时间只到分钟，所以下面需要对时间进行+-1处理
            if("1".equals(map.get("stopTimeType"))){
                //停留时间小于  就是  任务创建时间 大于 计算的时间
                calendar.add(Calendar.MINUTE, 1);
                Date calTime = DateUtils.parse(DateUtils.format(calendar.getTime(),"yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm"); //当前时间减去停留时间结果
                query.or().taskCreatedOn(calTime).taskCreatedAfter(calTime).endOr();
            }else if("2".equals(map.get("stopTimeType"))){
                //停留时间大于  就是 任务创建时间 小于 计算的时间
                Date calTime = DateUtils.parse(DateUtils.format(calendar.getTime(),"yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm"); //当前时间减去停留时间结果
                query.taskCreatedBefore(calTime);
            }else if("3".equals(map.get("stopTimeType"))){
                Date calTime = DateUtils.parse(DateUtils.format(calendar.getTime(),"yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm"); //当前时间减去停留时间结果
                query.taskCreatedAfter(calTime);
            }else if("4".equals(map.get("stopTimeType"))){
                calendar.add(Calendar.MINUTE, 1);
                Date calTime = DateUtils.parse(DateUtils.format(calendar.getTime(),"yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm"); //当前时间减去停留时间结果
                query.taskCreatedBefore(calTime);
            }else {
                //由于计算停留不算秒，并且任务创建时间含秒，所以需要排除秒影响，满足公式：calTime <= create_time < calTime + 1分钟
                calendar.add(Calendar.MINUTE, 1);
                Date calTime1 = DateUtils.parse(DateUtils.format(calendar.getTime(),"yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm");
                query.taskCreatedBefore(calTime1); //calTime + 1分钟 > create_time
                calendar.add(Calendar.MINUTE, -1);
                Date calTime2 = DateUtils.parse(DateUtils.format(calendar.getTime(),"yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm");
                query.or().taskCreatedOn(calTime2).taskCreatedAfter(calTime2).endOr(); //calTime <= create_time
            }
        }

        // 流程关联的信息；
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (org.springframework.util.StringUtils.isEmpty(value))
                continue;
            if ("dataName".equals(key) || "userName".equals(key) || "initiatorDeptName".equals(key) || "processName".equals(key)) {
                query = query.processVariableValueLike(key, "%" + value + "%");
            }

            if ("urgencyLevel".equals(key)) {
                // 5小时；
                if ("1".equals(value)) {
                    query.processVariableValueLessThan("expectTime", DateUtils.getAfterHour(new Date(), 5));
                }
                // 5-10小时；
                if ("2".equals(value)) {
                    query.processVariableValueGreaterThanOrEqual("expectTime", DateUtils.getAfterHour(new Date(), 5)).processVariableValueLessThan("expectTime", DateUtils.getAfterHour(new Date(), 10));
                }
                // 10小时以上；
                if ("3".equals(value)) {
                    query.or().processVariableNotExists("expectTime").processVariableValueGreaterThan("expectTime", DateUtils.getAfterHour(new Date(), 10)).endOr();
                }
            }
        }

        String proc= map.get("process");
        if (!StringUtils.isEmpty(proc)){
            String[]  processS =  proc.split(",");
            query.or();
            for (String p : processS){
                if (!StringUtils.isEmpty(p)){
                    query.processVariableValueEquals("process", Integer.parseInt(p));
                }
            }
            query.endOr();
        }

        // 期望日期区间；
        String dateStart = map.get("expectTimeStart");
        if (!StringUtils.isEmpty(dateStart)) {
            query.processVariableValueGreaterThanOrEqual("expectTime", DateUtils.parse(dateStart, "yyyy/MM/dd"));
        }
        String dateEnd = map.get("expectTimeEnd");
        if (!StringUtils.isEmpty(dateEnd)) {
            query.processVariableValueLessThanOrEqual("expectTime", DateUtils.parse(dateEnd, "yyyy/MM/dd"));
        }

        // 提交日期区间；
        dateStart = map.get("dateStart");
        if (!StringUtils.isEmpty(dateStart)) {
            query.processVariableValueGreaterThanOrEqual("processDate", DateUtils.parse(dateStart, "yyyy/MM/dd"));
        }
        dateEnd = map.get("dateEnd");
        if (!StringUtils.isEmpty(dateEnd)) {
            query.processVariableValueLessThanOrEqual("processDate", DateUtils.parse(dateEnd, "yyyy/MM/dd"));
        }
        //输入金额区间
        if (map.get("moneyState") != null) {
            double moneyState = Double.parseDouble(map.get("moneyState"));
            if (!Double.isNaN(moneyState)) {
                query.processVariableValueGreaterThanOrEqual("money", moneyState);

            }
        }
        if (map.get("moneyEnd") != null) {
            double moneyEnd = Double.parseDouble(map.get("moneyEnd"));
            if (!Double.isNaN(moneyEnd)) {
                query.processVariableValueLessThanOrEqual("money", moneyEnd);
            }
        }

        // 计算页面数量；
        int totalNum = (int) query.count();
        int pages = totalNum / pageSize;
        if (totalNum % pageSize != 0) {
            pages += 1;
        }

        // 计算数据开始的位置；
        int startRow = (pageNum - 1) * (pageSize);
        if (startRow >= totalNum) {
            startRow = totalNum;
        }

        // 计算最后一条数据位置；
        int endRow = pageNum * pageSize;
        if (endRow >= totalNum) {
            endRow = totalNum;
        }

        List<Task> tasks = query.orderByTaskCreateTime().desc().listPage(startRow, endRow);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> taskDatas;
        String taskId;
        for (Task task : tasks) {
            taskDatas = new HashMap<>();
            //计算流程停留时间
            Date currentTime = DateUtils.parse(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm");
            Date taskCreateTime = DateUtils.parse(DateUtils.format(task.getCreateTime(), "yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm");
            String taskStopTime = "";
            long minute = (currentTime.getTime() - taskCreateTime.getTime()) / 60 / 1000;//获取分钟
            if(minute <= 60){
                taskStopTime = minute + "分钟";
            }else if(minute <= 24 * 60){
                int hour = (int)(minute / 60);
                int min = (int)(minute % 60);
                taskStopTime = hour + "小时" + min + "分钟";
            }else {
                int day = (int)(minute / 60 / 24);
                int hour = (int)((minute - (day * 24 * 60)) / 60);
                int min = (int)((minute - (day * 24 * 60)) % 60);
                taskStopTime = day + "天" + hour + "小时" + min + "分钟";
            }
            taskDatas.put("taskStopTime", taskStopTime); //根据任务的创建时间，计算任务的停留时间
            taskId = task.getId();
            // 获取设置的变量；
            taskDatas.put("taskId", taskId);
            taskDatas.putAll(taskService.getVariables(taskId));
            taskDatas.putAll(task.getTaskLocalVariables());
            taskDatas.putAll(task.getProcessVariables());
            list.add(taskDatas);
        }
        // 更新到数据集合中；
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Map<String, Object>> mapPageInfo = new PageInfo<Map<String, Object>>(list);
        mapPageInfo.setPageSize(pageSize);
        mapPageInfo.setPages(pages);
        mapPageInfo.setStartRow(startRow);
        mapPageInfo.setEndRow(endRow);
        mapPageInfo.setHasPreviousPage(pageNum > pages);
        mapPageInfo.setHasNextPage(pageNum < pages);
        mapPageInfo.setIsFirstPage(pageNum == 1);
        mapPageInfo.setIsLastPage(pageNum == pages);
        mapPageInfo.setPageNum(pageNum);
        mapPageInfo.setTotal(totalNum);

        return mapPageInfo;
    }

    /**
     * 查询数据的审核记录；
     *
     * @param dataId：数据ID；
     * @param process：流程标志，定义参考com.qinfei.qferp.utils.IProcess；
     * @return ：审核记录集合；
     */
    public List<Map<String, Object>> listTaskHistory(String dataId, int process) {

        // 定义集合用来保存数据；
        List<Map<String, Object>> datas = new ArrayList<>();
        // 获取查询对象；
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
        // 增加查询条件；
        query = query.processVariableValueEquals("process", process).processVariableValueEquals("dataId", dataId);
        // 获得查询结果；
        List<HistoricTaskInstance> list = query.orderByHistoricTaskInstanceEndTime().asc().list();
        Map<String, Object> map;
        String user;
        // 遍历获取需要的数据；
        // 审核备注；
        String desc;
        for (HistoricTaskInstance historicTaskInstance : list) {
            // 没有审核人的数据可能在审核中，或者是已驳回的数据，跳过；
            user = historicTaskInstance.getOwner();
            // 老流程使用的Assignee保存审核人姓名；
            if (StringUtils.isEmpty(user)) {
                user = historicTaskInstance.getAssignee();
            }
            if (!StringUtils.isEmpty(user)) {
                map = new HashMap<>();
                map.put("name", historicTaskInstance.getName());
                map.put("user", user);
                desc = historicTaskInstance.getDeleteReason();
                String tips = desc.replaceAll("<b.*?>","").replaceAll("</b>","");//去除b标签
                StringBuilder descContent = new StringBuilder();
                descContent.append("<span title=\"").append(tips).append("\">").append(desc).append("</span>");
                desc = descContent.toString();
                map.put("desc", desc);
                map.put("time", DateUtils.format(historicTaskInstance.getEndTime(), DateUtils.DATE_FULL));
                datas.add(map);
            }
        }
        // 查询一下当前正在运行的流程；
        TaskQuery taskQuery = getTaskData(process, dataId, IProcess.PROCESS_HANDLING);
        // 防止异常（数据库操作等）；
        // Task task = taskQuery.singleResult();
        List<Task> tasks = taskQuery.list();
        // 如果查询有结果；
        if (tasks != null && !tasks.isEmpty()) {
            Task task = tasks.get(0);
            String taskId = task.getId();
            map = new HashMap<>();
            map.put("name", task.getName());
            map.put("user", taskService.getVariable(taskId, "approveUserName", String.class));
            map.put("desc", "<b style='color:darkorange;'>正在审核</b>");
            map.put("time", DateUtils.format(task.getCreateTime(), DateUtils.DATE_FULL));
            datas.add(map);
        }
        return datas;
    }

    /**
     * 获取流程图；
     *
     * @param response：响应对象；
     * @param taskId：任务ID；
     */
    @Override
    public void getProcessImage(HttpServletResponse response, String taskId) {
        // 设置返回的内容格式；
        response.setContentType("image/png;charset=utf-8");
        // 获取任务对象；
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 如果流程已完成则查找历史记录；
        if (task != null) {
            handleImage(response, task.getProcessInstanceId(), task.getProcessDefinitionId());
        } else {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            if (historicTaskInstance != null) {
                handleImage(response, historicTaskInstance.getProcessInstanceId(), historicTaskInstance.getProcessDefinitionId());
            }
        }
    }

    /**
     * 获取流程图；
     *
     * @param response：响应对象；
     * @param dataId：任务ID；
     * @param process：流程类型，定义参考com.qinfei.qferp.utils.IProcess；
     */
    @Override
    public void getProcessImag(HttpServletResponse response, String dataId, int process) {
        // 设置返回的内容格式；
        response.setContentType("image/png;charset=utf-8");
        // 查询一下当前正在运行的流程；
        TaskQuery taskQuery = getTaskData(process, dataId, IProcess.PROCESS_HANDLING);
        // 防止异常（数据库操作等）；
        List<Task> tasks = taskQuery.orderByTaskCreateTime().desc().list();
        // 如果查询有结果；
        if (tasks != null && !tasks.isEmpty()) {
            Task task = tasks.get(tasks.size() - 1);
            handleImage(response, task.getProcessInstanceId(), task.getProcessDefinitionId());
        } else {
            // 如果流程已完成则查找历史记录；
            HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("process", process).processVariableValueEquals("dataId", dataId);
            List<HistoricTaskInstance> historicTaskInstances = historicTaskInstanceQuery.orderByTaskCreateTime().desc().list();
            if (historicTaskInstances != null && !historicTaskInstances.isEmpty()) {
                HistoricTaskInstance historicTaskInstance = historicTaskInstances.get(0);
                handleImage(response, historicTaskInstance.getProcessInstanceId(), historicTaskInstance.getProcessDefinitionId());
            }
        }
    }

    @Override
    public void rollback(String id, String target, Integer nextUserId) {
        if("start".equals(target)){
            approveProcess(new String[]{id}, String.format("<b style='color:blue;'>撤回至【%s】</b>", "发起人"), false);
        }else {
            Map<String, Object> param = new HashMap<>();
            param.put("nextUser", nextUserId);
            managementService.executeCommand(new RollbackCommand(id, target,param));
            //更新历史节点
            managementService.executeCommand(new UpdateHiTaskCommand(id, AppUtil.getUser().getName(), String.valueOf(AppUtil.getUser().getId()), String.format("<b style='color:blue;'>撤回至【%s】</b>", String.valueOf(param.get("targetName")))));
        }
    }

    @Override
    public List<Map<String, String>> listTaskDefKey(String taskId) {
        List<Map<String, String>> result = new ArrayList<>();
        try{
            SelectProcessNodeCommand command = new SelectProcessNodeCommand(taskId);
            managementService.executeCommand(command);
            result = command.getResult();
        }catch (Exception e){
            log.error(e.getMessage());
        }
       return result;
    }

    @Override
    public PageInfo<Map<String, Object>> listProcessDefinition(Map<String, Object> map, Pageable pageable) {
        User user = AppUtil.getUser();
        if (user == null) {
            return new PageInfo<>();
        }
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String, Object>> result = processMapper.listProcessDefinition(map);
        return new PageInfo<>(result);
    }

    @Override
    @Transactional
    public void deploy(List<String> fileNames, boolean uploadFlag) {
        try {
            if (CollectionUtils.isEmpty(fileNames)) {
                throw new QinFeiException(1002, "请传入部署文件名！");
            }
            Set<String> fileNameSet = new HashSet<>(); //让名称唯一，防止重名多次部署，没意义
            fileNameSet.addAll(fileNames);
            //如果是上传的文件，则读取上传的文件
            if (uploadFlag) {
                for (String fileName : fileNameSet) {
                    fileName = config.getUploadDir() + File.separator + fileName;
                    File file = new File(fileName);
                    repositoryService.createDeployment().addInputStream(file.getName(), new FileInputStream(file)).name(file.getName()).deploy();
                }
            } else {
                ResourceLoader resourceLoader = new DefaultResourceLoader();
                for (String fileName : fileNameSet) {
                    String filePath = "classpath:/processes" + File.separator + fileName;
                    Resource resource = resourceLoader.getResource(filePath);
                    repositoryService.createDeployment().addInputStream(resource.getFilename(), resource.getInputStream()).name(resource.getFilename()).deploy();
                }
            }
        } catch (QinFeiException b) {
            throw b;
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            throw new QinFeiException(1002, "存在流程定义文件不存在！");
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002, "流程部署异常！");
        }
    }

    @Override
    public List<Map<String, String>> getProcessTaskDefKey(String taskId) {
        Map<String, String> processMap = flowableMapper.findProcInstIdByHisTaskId(taskId);
        List<Map<String, String>> actList = flowableMapper.listActByProcInstId(processMap.get("processInstId"));
        return CollectionUtils.isNotEmpty(actList) ? actList : new ArrayList<>();
    }

    @Override
    public void workupProcess(Integer id, String name, Integer processType, String processName, String taskDefKey, String taskId, boolean gatewayFlag, String companyCode) {
        String dataName = "";
        if (!StringUtils.isEmpty(processName)) {
            dataName += processName;
        }
        if (!StringUtils.isEmpty(name)) {

        }
        dataName += "正在唤醒";
        Map<String, Object> map = new HashMap<>();
        map.put("gatewayFlag", gatewayFlag); //是否被唤醒流程中含有网关，有的话需要将原网关状态值设置到流程中
        map.put("processType", processType); //流程类型
        map.put("taskDefKey", taskDefKey);
        map.put("successTaskId", taskId);
        if (!StringUtils.isEmpty(companyCode)) {
            map.put("company", companyCode);
        }
        if (processType == IProcess.PROCESS_MEDIAREFUND || processType == IProcess.PROCESS_SELF_MEDIAREFUND ||
                processType == IProcess.PROCESS_HTOUTPLEASETYPE || processType == IProcess.PROCESS_NEWSPAPEROUTGO) {
            map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryOutgo?flag=3&workupFlag=23&id=" + id);
            map.put(IProcess.PROCESS_PASS_URL, "/fee/queryOutgo?flag=3&workupFlag=23&id=" + id);
            map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryOutgo?flag=1&workupFlag=23&id=" + id);
        } else {
            map.put(IProcess.PROCESS_APPROVE_URL, "process/queryTask");
            map.put(IProcess.PROCESS_FINISH_URL, "process/queryTask");
        }
        //消息发起人(以发起人为准)
        map.put("messageUserName", AppUtil.getUser().getName());
        //消息标题
        map.put("messageTitle", name);
        startProcess(map, IProcess.PROCESS_WORKUP, String.valueOf(id), dataName, "", 3, null);
    }

    @Override
    public void addMeetingRoomProcess(MeetingRoomApply meetingRoomApply) {
        User user = userService.getById(meetingRoomApply.getApproverUserId());
        Map<String, Object> map = new HashMap<>();
        map.put("nextUser", user.getId());
        map.put("nextUserName", user.getName());
        map.put("nextUserDept", user.getDeptId());
        map.put("MeetingRoomApplyId", meetingRoomApply.getId());
        //消息发起人
        map.put("messageUserName", AppUtil.getUser().getName());
        //消息标题
        map.put("messageTitle", meetingRoomApply.getMeeting().getTitle());
        startProcess(map, IProcess.PROCESS_MEETINGROOM, String.valueOf(meetingRoomApply.getMeeting().getId()), meetingRoomApply.getMeeting().getTitle(), "", 3, null);
    }

    @Override
    public Map<String, List<Map<String, Object>>> listFlowTask(List<Map<String, Object>> param) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> list = flowableMapper.listHistoryTask(param);
        if(CollectionUtils.isNotEmpty(list)){
            for(Map<String, Object> task : list){
                String dataId = String.valueOf(task.get("dataId"));
                String process = String.valueOf(task.get("process"));
                if(!result.containsKey(String.format("%s-%s", dataId, process))){
                    result.put(String.format("%s-%s", dataId, process), new ArrayList<>());
                }
                result.get(String.format("%s-%s", dataId, process)).add(task);
            }
        }
        return result;
    }

    /**
     * 启动流程；
     *
     * @param map：预先定义了网关数据的集合；
     * @param process：流程类型，定义参考com.qinfei.qferp.utils.IProcess；
     * @param dataId：需要审核的数据ID；
     * @param dataName：需要审核的数据名称；
     * @param taskId：上个流程结束的任务ID；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：流程启动的ID；
     */
    private String startProcess(Map<String, Object> map, int process, String dataId, String dataName, String taskId, int urgencyLevel) {
        return startProcess(map, process, dataId, dataName, taskId, urgencyLevel, null);
}

    /**
     * 启动流程；
     *
     * @param map：预先定义了网关数据的集合；
     * @param process：流程类型，定义参考com.qinfei.qferp.utils.IProcess；
     * @param dataId：需要审核的数据ID；
     * @param dataName：需要审核的数据名称；
     * @param taskId：上个流程结束的任务ID；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @param expectTime：期望审核结束时间，用于显示倒计时及显示紧急程度；
     * @return ：流程启动的ID；
     */
    private String startProcess(Map<String, Object> map, int process, String dataId, String dataName, String taskId, int urgencyLevel, Date expectTime) {
        // 检查数据是否已提交流程；
        if (checkDataExsit(process, dataId)) {
            return null;
        } else {
            // 流程信息；
            String processId = null;

            // TODO: 2019/3/27 0027 流程启动配置模块，配置流程启动所需的各项参数；
            // =================================================配置模块开始=================================================
            // 配置说明：配置流程请务必遵循以下规则；
            // 1、各个流程之间使用常量值来区分，增加流程请在IProcess中配置，确保各流程定义的值不能相同；
            // 2、以下4个变量为必须：
            // 2.1 processId：流程的ID，此属性可在流程定义的bpmn20.xml配置文件找到；
            // 2.2 IProcess.PROCESS_NAME：流程名称，该名称会存储在待办事项的工作类型字段中，还会作为链接跳转的显示文字；
            // 2.3 IProcess.PROCESS_APPROVE_URL：流程审核人点击待处理的待办事项的链接跳转目标；
            // 2.4 IProcess.PROCESS_FINISH_URL：待办事项处理完毕后会更新至已完成列表，此链接会作为该待办事项的跳转目标；
            // 3、另外两个变量说明：
            // 3.1 IProcess.PROCESS_EDIT_URL：此变量用于申请驳回后，申请人点击待办事项跳转的链接；
            // 3.2 IProcess.PROCESS_PASS_URL：此变量用于财务处理的专用链接，如在审核完成后推送给财务的待办事项需要额外处理，请配置此变量；

            // 获取流程名称和审核页面地址；
            switch (process) {
                // 开票申请；
                case IProcess.PROCESS_BALLOT:
                    processId = "ballot";
                    map.put("money", map.get("money"));
                    map.put("taxType", map.get("taxType"));
                    map.put(IProcess.PROCESS_NAME, "开票申请");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryInvoice?flag=3&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryInvoice?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/queryInvoice?flag=4&id=" + dataId);
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/queryInvoice?flag=0&id=" + dataId);
                    map.put(IProcess.PROCESS_ASSISTANT_URL, "/fee/queryInvoice?flag=5&id=" + dataId);
                    break;
                // 借款申请；
                case IProcess.PROCESS_BORROW:
                    if("JT".equals(map.get("company"))){
                        processId = "JTborrow";
                    }else {
                        processId = "ZGSBorrow";
                    }
                    map.put(IProcess.PROCESS_NAME, "借款申请");
                    map.put("money", map.get("money"));
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryBorrow?flag=3&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryBorrow?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/queryBorrow?flag=4&id=" + dataId);
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/queryBorrow?flag=0&id=" + dataId);
                    map.put(IProcess.PROCESS_ACCIYBR_URL, "/fee/queryBorrow?flag=5&id=" + dataId);
                    break;
                // 媒介请款；1网络 2、新媒体
                case IProcess.PROCESS_MEDIAREFUND:
                    if ((Integer) map.get("mediaType") == 1) {
                        processId = "networkOutgo";
                    } else if ((Integer) map.get("mediaType") == 2) {
                        processId = "newMediaOutgo";
                    }
                    map.put("money", map.get("money"));
                    map.put(IProcess.PROCESS_NAME, "媒介请款");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryOutgo?flag=3&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryOutgo?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/queryOutgo?flag=4&id=" + dataId);
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/queryOutgo?flag=0&id=" + dataId);
                    map.put(IProcess.PROCESS_BACKFILL_URL, "/fee/queryOutgo?flag=7&id=" + dataId);
                    map.put(IProcess.PROCESS_ACCIYBR_URL, "/fee/queryOutgo?flag=5&id=" + dataId);
                    break;
                //自媒体请款
                case IProcess.PROCESS_SELF_MEDIAREFUND:
                    if ((Integer) map.get("meidaTypeId") == 317) {
                        processId = "networkOutgo";
                        map.put(IProcess.PROCESS_NAME, "媒介请款");
                    } else {
                        processId = "mediaSelfRefund";
                        map.put(IProcess.PROCESS_NAME, "媒介请款");
                        map.put("company", map.get("company"));
                    }
                    map.put("money", map.get("money"));
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryOutgo?flag=3&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryOutgo?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/queryOutgo?flag=4&id=" + dataId);
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/queryOutgo?flag=0&id=" + dataId);
                    map.put(IProcess.PROCESS_BACKFILL_URL, "/fee/queryOutgo?flag=7&id=" + dataId);
                    map.put(IProcess.PROCESS_ACCIYBR_URL, "/fee/queryOutgo?flag=5&id=" + dataId);
                    break;
                //河图请款
                case IProcess.PROCESS_HTOUTPLEASETYPE:
//                    Map map1=new HashMap();
//                    map1.put("typeCode","process");
//                    map1.put("type","1");
//                    String sc= outgoService.HTLScompanyCode(map1);
                    List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
                    if (CollectionUtils.isNotEmpty(sc)) {
                        if (sc.contains(map.get("company"))) {
                            processId = "mediaHtoutPleaseType";
                        } else {
                            processId = "outgoProcess2020225";
                        }
                    } else {
                        processId = "outgoProcess2020225";
                    }
                    map.put("mediaType", map.get("mediaType"));
                    map.put("money", map.get("money"));
                    map.put(IProcess.PROCESS_NAME, "媒介请款");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryOutgo?flag=3&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryOutgo?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/queryOutgo?flag=4&id=" + dataId);
                    map.put(IProcess.PROCESS_BACKFILL_URL, "/fee/queryOutgo?flag=7&id=" + dataId);
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/queryOutgo?flag=0&id=" + dataId);
                    map.put(IProcess.PROCESS_ACCIYBR_URL, "/fee/queryOutgo?flag=5&id=" + dataId);
                    break;
                //报纸板块请款
                case IProcess.PROCESS_NEWSPAPEROUTGO:
                    processId = "newspaperOutgo";
                    map.put("money", map.get("money"));
                    map.put(IProcess.PROCESS_NAME, "报纸板块请款");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryOutgo?flag=3&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryOutgo?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/queryOutgo?flag=4&id=" + dataId);
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/queryOutgo?flag=0&id=" + dataId);
                    map.put(IProcess.PROCESS_BACKFILL_URL, "/fee/queryOutgo?flag=7&id=" + dataId);
                    map.put(IProcess.PROCESS_ACCIYBR_URL, "/fee/queryOutgo?flag=5&id=" + dataId);
                    break;
                // 退款申请；
                case IProcess.PROCESS_REFUND:
                    processId = "refund";
                    map.put("money", map.get("money"));
                    map.put(IProcess.PROCESS_NAME, "退款申请");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryRefund?flag=3&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryRefund?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/queryRefund?flag=4&id=" + dataId);
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/queryRefund?flag=0&id=" + dataId);
                    map.put(IProcess.PROCESS_ACCIYBR_URL, "/fee/queryRefund?flag=5&id=" + dataId);
                    break;
                // 财务提成；
                case IProcess.PROCESS_ROYALTY:
                    processId = "royalty";
                    map.put(IProcess.PROCESS_NAME, "财务提成");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryCommission?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryCommission?flag=1&id=" + dataId);
                    break;
                // 员工录用；
                case IProcess.PROCESS_EMPLOY:
                    processId = "employ";
                    String uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                    map.put("code", uniqueKey);
                    map.put(IProcess.PROCESS_NAME, "员工录用");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/employApprove?code=" + uniqueKey);
                    map.put(IProcess.PROCESS_FINISH_URL, "/employ/employeeHire");
                    break;
                // 员工转正；
                case IProcess.PROCESS_FORMAL:
                    processId = "positive";
                    uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                    map.put("code", uniqueKey);
                    map.put(IProcess.PROCESS_NAME, "员工转正");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/formalApprove?code=" + uniqueKey);
                    map.put(IProcess.PROCESS_FINISH_URL, "/employ/employeeFormal");
                    break;
                // 员工离职；
                case IProcess.PROCESS_LEAVE:
                    processId = "leave";
                    uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                    map.put("code", uniqueKey);
                    map.put(IProcess.PROCESS_NAME, "员工离职");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/leaveApprove?code=" + uniqueKey);
                    map.put(IProcess.PROCESS_FINISH_URL, "/employ/employeeLeave");
                    break;
                // 员工调岗；
                case IProcess.PROCESS_TRANSFER:
                    processId = "transfer";
                    uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                    map.put("code", uniqueKey);
                    map.put(IProcess.PROCESS_NAME, "员工调岗");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/transferApprove?code=" + uniqueKey);
                    map.put(IProcess.PROCESS_FINISH_URL, "/employ/employeeTransfer");
                    break;
                // 员工离职交接；
                case IProcess.PROCESS_HANDOVER_LEAVE:
                    processId = "handover";
                    uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                    map.put("code", uniqueKey);
                    map.put(IProcess.PROCESS_NAME, "员工离职交接");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/leaveConnectApprove?code=" + uniqueKey);
                    map.put(IProcess.PROCESS_FINISH_URL, "/employ/employeeConnect");
                    break;
                // 员工调岗交接；
                case IProcess.PROCESS_HANDOVER_TRANSFER:
                    processId = "handover";
                    uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                    map.put("code", uniqueKey);
                    map.put(IProcess.PROCESS_NAME, "员工调岗交接");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/transferConnectApprove?code=" + uniqueKey);
                    map.put(IProcess.PROCESS_FINISH_URL, "/employ/employeeConnect");
                    break;
                // 绩效考核；
                case IProcess.PROCESS_PERFORMANCE:
                    processId = "performance";
                    uniqueKey = PrimaryKeyUtil.getStringUniqueKey();
                    map.put("code", uniqueKey);
                    map.put(IProcess.PROCESS_NAME, "绩效考核");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/performanceApprove?flag=2&code=" + uniqueKey);
                    map.put(IProcess.PROCESS_EDIT_URL, "/performanceApprove?flag=0&code=" + uniqueKey);
                    map.put(IProcess.PROCESS_FINISH_URL, "/performance/performanceScore?flag=1");
                    break;
                // 请假审核(1天内)
                case IProcess.PROCESS_VOCATION_ONE:
                    map.put(IProcess.PROCESS_NAME, "请假审核（一天内）");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/administrative/administrative?type=1&flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/administrative/administrative?type=1&flag=-1&id=" + dataId);
                    processId = "administrative1";
                    break;
                // 请假审核（2天内）
                case IProcess.PROCESS_VOCATION_TWO:
                    map.put(IProcess.PROCESS_NAME, "请假审核（2天内）");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/administrative/administrative?type=1&flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/administrative/administrative?type=1&flag=-1&id=" + dataId);
                    processId = "administrative2";
                    break;
                // 请假审核（3天及以上）
                case IProcess.PROCESS_VOCATION_THREE:
                    map.put(IProcess.PROCESS_NAME, "请假审核（3天及以上）");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/administrative/administrative?type=1&flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/administrative/administrative?type=1&flag=-1&id=" + dataId);
                    processId = "administrative3";
                    break;
                // 外出审核
                case IProcess.PROCESS_OUTWORK:
                    map.put(IProcess.PROCESS_NAME, "外出审核");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/administrative/administrative?type=3&flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/administrative/administrative?type=3&flag=-1&id=" + dataId);
                    processId = "outWork";
                    break;
                // 加班审核
                case IProcess.PROCESS_WORKOVERTIME:
                    map.put(IProcess.PROCESS_NAME, "加班审核");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/administrative/administrative?type=2&flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/administrative/administrative?type=2&flag=-1&id=" + dataId);
                    processId = "workOverTime";
                    break;
                //出差审核
                case IProcess.PROCESS_ONBUSINESS:
                    map.put(IProcess.PROCESS_NAME, "出差审核");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/administrative/administrative?type=4&flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/administrative/administrative?type=4&flag=-1&id=" + dataId);
                    processId = "onBusiness";
                    break;
                // 退稿流程
                case IProcess.PROCESS_MANUSCRIPT:
                    processId = "manuscript";
                    map.put("money", map.get("money"));
                    map.put(IProcess.PROCESS_NAME, "退稿流程");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/queryDrop?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/queryDrop?flag=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/queryDrop?flag=-1&id=" + dataId);
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/queryDrop?editId=" + dataId);
                    break;
                    //烂账申请流程
                case IProcess.PROCESS_MESS:
                    processId ="accountsMessProcess";
                    map.put(IProcess.PROCESS_NAME,"烂账流程");
                    map.put(IProcess.PROCESS_APPROVE_URL,"/accountsMess/accountsMess?flag=2&id="+dataId);
                    map.put(IProcess.PROCESS_FINISH_URL,"/accountsMess/accountsMess?flag=1&id="+dataId);
                    map.put(IProcess.PROCESS_EDIT_URL,"/accountsMess/accountsMess?flag=0&id="+dataId);
                    break;
                // 费用报销流程
                case IProcess.PROCESS_REIMBURSEMENT:
//                    processId = "newreimburse";
                    if("JT".equals(map.get("company"))){
                        processId = "JTreimbursement202006";
                    }else {
                        processId = "reimbursementZGS";
                    }
                    map.put("money", map.get("money"));
                    map.put(IProcess.PROCESS_NAME, "费用报销流程");
                    // 流程审核人点击待处理的待办事项的链接跳转目标
                    map.put(IProcess.PROCESS_APPROVE_URL, "/fee/expenseReimbursement?flag=1&id=" + dataId);
                    // 待办事项处理完毕后会更新至已完成列表，此链接会作为该待办事项的跳转目标；
                    map.put(IProcess.PROCESS_FINISH_URL, "/fee/expenseReimbursement?flag=-1&id=" + dataId);
                    // 此变量用于申请驳回后，申请人点击待办事项跳转的链接；
                    map.put(IProcess.PROCESS_EDIT_URL, "/fee/expenseReimbursement?editId=" + dataId);
                    // 财务出纳出款
                    map.put(IProcess.PROCESS_PASS_URL, "/fee/expenseReimbursement?flag=1&approveId=" + dataId);
                    //财务会计确认出款
                    map.put(IProcess.PROCESS_ACCIYBR_URL, "/fee/expenseReimbursement?flag=3&id=" + dataId);
                    break;
                //会议室审批流程
                case IProcess.PROCESS_MEETINGROOM:
                    map.put(IProcess.PROCESS_NAME, "会议室审批");
                    map.put(IProcess.PROCESS_APPROVE_URL, "/meeting/meetingManage?type=1&id=" + dataId);
                    map.put(IProcess.PROCESS_FINISH_URL, "/meeting/meetingManage");
                    processId = "meetingRoom";
                    break;
                //唤醒流程
                case IProcess.PROCESS_WORKUP:
                    map.put(IProcess.PROCESS_NAME, "唤醒流程");
                    processId = "workup";
                    break;
                case IProcess.PROCESS_PROJECT:
                    processId = "ProjectManageProcess";
                    map.put(IProcess.PROCESS_NAME, "项目管理审批");
                    //流程审核人点击待处理的待办事项的链接跳转目标
                    map.put(IProcess.PROCESS_APPROVE_URL, "/biz/project_list?flag=2&id=" + dataId);
                    // 待办事项处理完毕后会更新至已完成列表，此链接会作为该待办事项的跳转目标；
                    map.put(IProcess.PROCESS_FINISH_URL, "/biz/project_list?flag=0&id=" + dataId);
                    // 此变量用于申请驳回后，申请人点击待办事项跳转的链接；
                    map.put(IProcess.PROCESS_EDIT_URL, "/biz/project_list?flag=1&id=" + dataId);
                    break;
                //物品采购流程
                case IProcess.PROCESS_PURCHASE:
                    map.put(IProcess.PROCESS_NAME, "物品采购");
                    //流程审核人点击待处理的待办事项的链接跳转目标，审核
                    map.put(IProcess.PROCESS_APPROVE_URL, "/inventory/purchase_list?flag=2&id=" + dataId);
                    //待办事项处理完毕后会更新至已完成列表，查看
                    map.put(IProcess.PROCESS_FINISH_URL, "/inventory/purchase_list?flag=1&id=" + dataId);
                    //此变量用于申请驳回后，编辑
                    map.put(IProcess.PROCESS_EDIT_URL, "/inventory/purchase_list?flag=0&id=" + dataId);
                    processId = "ProductPurchase";
                    break;
                //物品领用流程
                case IProcess.PROCESS_APPLY:
                    map.put(IProcess.PROCESS_NAME, "物品领用");
                    //流程审核人点击待处理的待办事项的链接跳转目标，审核
                    map.put(IProcess.PROCESS_APPROVE_URL, "/inventory/apply_list?flag=2&id=" + dataId);
                    //待办事项处理完毕后会更新至已完成列表，查看
                    map.put(IProcess.PROCESS_FINISH_URL, "/inventory/apply_list?flag=1&id=" + dataId);
                    //此变量用于申请驳回后，编辑
                    map.put(IProcess.PROCESS_EDIT_URL, "/inventory/apply_list?flag=0&id=" + dataId);
                    processId = "ProductApplyProcess";
                    break;
                //物品报修流程
                case IProcess.PROCESS_REPAIR:
                    Object object=map.get("html");
                    String html=null;
                    if(!ObjectUtils.isEmpty(object)){
                        //html：1物品库存页面2使用页面
                        html = (String) object;
                    }
                    map.put(IProcess.PROCESS_NAME, "物品报修");
                    //流程审核人点击待处理的待办事项的链接跳转目标，审核
                    map.put(IProcess.PROCESS_APPROVE_URL, html+"?repairFlag=2&id=" + dataId);
                    //待办事项处理完毕后会更新至已完成列表，查看
                    map.put(IProcess.PROCESS_FINISH_URL, html+"?repairFlag=1&id=" + dataId);
                    //此变量用于申请驳回后，编辑
                    map.put(IProcess.PROCESS_EDIT_URL, html+"?repairFlag=0&id=" + dataId);
                    processId = "ProductRepairProcess";
                    break;
                //物品报废流程
                case IProcess.PROCESS_SCRAP:
                    Object object2=map.get("html");
                    String html2=null;
                    if(!ObjectUtils.isEmpty(object2)){
                        //html：1物品库存页面2使用页面
                        html2 = (String) object2;
                    }
                    map.put(IProcess.PROCESS_NAME, "物品报废");
                    //流程审核人点击待处理的待办事项的链接跳转目标，审核
                    map.put(IProcess.PROCESS_APPROVE_URL, html2+"?scrapFlag=2&id=" + dataId);
                    //待办事项处理完毕后会更新至已完成列表，查看
                    map.put(IProcess.PROCESS_FINISH_URL, html2+"?scrapFlag=1&id=" + dataId);
                    //此变量用于申请驳回后，编辑
                    map.put(IProcess.PROCESS_EDIT_URL, html2+"?scrapFlag=0&id=" + dataId);
                    processId = "ProductScrapProcess";
                    break;
                //物品归还流程
                case IProcess.PROCESS_RETURN:
                    Object object3=map.get("html");
                    String html3=null;
                    if(!ObjectUtils.isEmpty(object3)){
                        //html：1物品库存页面2使用页面
                        html3 = (String) object3;
                    }
                    map.put(IProcess.PROCESS_NAME, "物品归还");
                    //流程审核人点击待处理的待办事项的链接跳转目标，审核
                    map.put(IProcess.PROCESS_APPROVE_URL, html3+"?returnFlag=2&id=" + dataId);
                    //待办事项处理完毕后会更新至已完成列表，查看
                    map.put(IProcess.PROCESS_FINISH_URL, html3+"?returnFlag=1&id=" + dataId);
                    //此变量用于申请驳回后，编辑
                    map.put(IProcess.PROCESS_EDIT_URL, html3+"?returnFlag=0&id=" + dataId);
                    processId = "ProductReturnProcess";
                    break;
                case IProcess.PROCESS_PROTECT:
                    map.put(IProcess.PROCESS_NAME, "客户保护审批");
                    //流程审核人点击待处理的待办事项的链接跳转目标
                    map.put(IProcess.PROCESS_APPROVE_URL, "/crm/company_user_list?flag=1&protectId=" + dataId);
                    // 待办事项处理完毕后会更新至已完成列表，此链接会作为该待办事项的跳转目标；
                    map.put(IProcess.PROCESS_FINISH_URL, "/crm/company_user_list?flag=0&protectId=" + dataId);
                    // 此变量用于申请驳回后，申请人点击待办事项跳转的链接；
                    map.put(IProcess.PROCESS_EDIT_URL, "/crm/company_user_list?flag=-1&protectId=" + dataId);
                    processId = "customerProtection";
                    break;
                case IProcess.PROCESS_STANDARDIZED_COMPANY:
                    processId = "StandardizedCompanyProcesses";
                    //流程审核人点击待处理的待办事项的链接跳转目标，审核
                    map.put(IProcess.PROCESS_APPROVE_URL, "/crm/queryStandardizedCompany?flag=2&id=" + dataId);
                    //待办事项处理完毕后会更新至已完成列表，查看
                    map.put(IProcess.PROCESS_FINISH_URL, "/crm/queryStandardizedCompany?flag=1&id=" + dataId);
                    //此变量用于申请驳回后，编辑
                    map.put(IProcess.PROCESS_EDIT_URL, "/crm/queryStandardizedCompany?flag=0&id=" + dataId);
                    map.put(IProcess.PROCESS_NAME, "标准化公司申请");
                    break;
                default:
                    map.put(IProcess.PROCESS_NAME, "流程不存在");
                    break;
            }
            // =================================================配置模块结束=================================================

            // =================================================以下内容为流程启动核心业务处理，如不清楚业务请勿修改=================
            // 流程不存在直接结束；
            if (processId == null) {
                return "流程不存在。";
            } else {
                // 流程标识；
                map.put("process", process);
                // 提交的数据ID；
                map.put("dataId", dataId);
                // 提交的数据名称，用于跳转链接的显示内容；
                map.put("dataName", dataName);
                // 审核的紧急程度；
                // 紧急程度只有3个值，不在范围内的默认为普通；
                if (urgencyLevel < Const.ITEM_J1 || urgencyLevel > Const.ITEM_J3) {
                    urgencyLevel = Const.ITEM_J3;
                }
                map.put("urgencyLevel", urgencyLevel);
                // 期望审核结束时间；
                if (expectTime != null) {
                    map.put("expectTime", expectTime);
                }
                // 流程提交的时间；
                map.put("processDate", new Date());
                // 数据审核状态，0为审核中，1为已完成；
                map.put("processState", IProcess.PROCESS_HANDLING);
                // taskId的长度为36位，防止数据异常；
                if (StringUtils.isEmpty(taskId) || taskId.length() != 36) {
                    // 查询审批驳回的数据；
                    TaskQuery taskQuery = getTaskData(process, dataId, IProcess.PROCESS_REJECT);
                    // 时间递增排序；
                    List<Task> tasks = taskQuery.orderByTaskCreateTime().desc().list();
                    if (tasks != null && !tasks.isEmpty()) {
                        // 获取最新的一条记录；
                        // 因为执行流程跳转命令会清空任务列表，因此需提前获取；
                        // 上个流程结束发起的待办事项要进行更新；
                        Integer itemId = taskService.getVariable(tasks.get(0).getId(), "itemId", Integer.class);
                        if (itemId != null) {
                            map.put("itemId", itemId);
                        }
                        for (Task task : tasks) {
                            taskId = task.getId();
                            // 结束老的流程；
                            managementService.executeCommand(new JumpTaskCommand(taskId, "endEvent"));
                            // 删除驳回前的任务；
                            managementService.executeCommand(new DeleteHistoryTaskCommand(taskId));
                        }
                    }
                } else {
                    // 确保审核任务还存在；
                    TaskQuery taskQuery = taskService.createTaskQuery().taskId(taskId);
                    if (taskQuery.count() == 1) {
                        // 上个流程结束发起的待办事项要进行更新；
                        Integer itemId = taskService.getVariable(taskId, "itemId", Integer.class);
                        if (itemId != null) {
                            map.put("itemId", itemId);
                        }
                        // 结束老的流程；
                        managementService.executeCommand(new JumpTaskCommand(taskId, "endEvent"));
                        // 删除驳回前的任务；
                        managementService.executeCommand(new DeleteHistoryTaskCommand(taskId));
                    } else {
                        // 当前运行流程未找到则去查询历史记录；
                        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
                        if (historicTaskInstance != null) {
                            HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).variableName("itemId").singleResult();
                            if (variableInstance != null) {
                                // 获取原有的待办事项ID，更新到数据集合中，最终将在更新数据提交后保存到数据库，并会更新对应的待办事项状态；
                                map.put("itemId", Integer.parseInt(variableInstance.getValue().toString()));
                            }
                        }
                    }
                }

                // 流程申请人信息；
                if (map.get("workup") != null && "workup".equals(map.get("workup"))) { //如果是唤醒流程启动，则申请人在唤醒流程完成时设置，否则当前用户
                    // 设置流程启动人；
                    Authentication.setAuthenticatedUserId(String.valueOf(map.get("userId")));
                } else {


                    User user = AppUtil.getUser();
                    Integer userId = user.getId();
                    // 设置流程启动人；
                    Authentication.setAuthenticatedUserId(userId.toString());
                    if (process == 14 || process == 16) {
                        User user1 = userService.getById(Integer.parseInt(map.get("nextUser").toString()));
                        // diaogang的用户ID；
                        map.put("userId", user1.getId().toString());
                        // 提交人用户名称；
                        map.put("userName", user1.getName());
                        // 提交人的部门ID；
                        map.put("initiatorDept", user1.getDeptId());
                        // 提交人的部门名称；
                        map.put("initiatorDeptName", user1.getDeptName());
                        // 提交人的用户ID，用于发送信息填充字段；
                        map.put("initiatorWorker", user1.getId());
                        // 获取公司代码；
                        map.put("companyCode", user1.getCompanyCode());

                    } else {
                        // 提交人的用户ID；
                        map.put("userId", userId.toString());
                        // 提交人用户名称；
                        map.put("userName", user.getName());
                        // 提交人的部门ID；
                        map.put("initiatorDept", user.getDeptId());
                        // 提交人的部门名称；
                        map.put("initiatorDeptName", user.getDeptName());
                        // 提交人的用户ID，用于发送信息填充字段；
                        map.put("initiatorWorker", userId);
                        // 获取公司代码；
                        Dept dept = user.getDept();
                        if (dept != null) {
                            map.put("companyCode", dept.getCompanyCode());
                        }
                    }


                }
                map.put("messagePattern","[%s]恭喜你，您提交的%s已成功提交,下一审核节点：%s,审核人：%s。");
                map.put("messageSign","1");
                // 启动流程，返回流程实例ID；
                ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processId, map);
                String processInstanceId = processInstance.getId();
                if (processInstanceId == null) {
                    return "该审批已完成";
                } else {
                    return processInstanceId;
                }
            }
            // =================================================流程启动完毕=================================================
        }
    }

    /**
     * 检查数据是否已有审核中的流程；
     *
     * @param process：流程类型，定义参考com.qinfei.qferp.utils.IProcess；
     * @param dataId：需要审核的数据ID；
     * @return ：true为已存在，false为不存在；
     */
    private boolean checkDataExsit(int process, String dataId) {
        // 审核状态；
        TaskQuery taskQuery = getTaskData(process, dataId, IProcess.PROCESS_HANDLING);
        long count = taskQuery.count();
        return count > 0;
    }

    /**
     * 获取运行中的流程任务数据；
     *
     * @param processState：流程状态；
     * @return ：流程查询对象；
     */
    private TaskQuery getTaskData(int process, String dataId, int processState) {
        TaskQuery taskQuery = taskService.createTaskQuery();
        taskQuery = taskQuery.processVariableValueEquals("process", process);
        taskQuery = taskQuery.processVariableValueEquals("dataId", dataId);
        // 审核状态；
        taskQuery = taskQuery.processVariableValueEquals("processState", processState);
        return taskQuery;
    }

    /**
     * 手动完成待办事项；
     *
     * @param itemId：主键ID；
     */
    private void finishItem(Integer itemId) {
        Items items = new Items();
        items.setId(itemId);
        items.setTransactionState(Const.ITEM_Y);
        itemsService.finishItems(items);
    }

    /**
     * 获取流程图；
     *
     * @param response：响应对象；
     * @param processDefinitionId：流程ID；
     */
    private void handleImage(HttpServletResponse response, String processInstanceId, String processDefinitionId) {
        // 获取高亮的路线图；
        List<Process> processes = repositoryService.getBpmnModel(processDefinitionId).getProcesses();
        //由于此处获取的流程节点顺序可能异常
        List<HistoricActivityInstance> highLightedActivityList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
        // 获取执行中的任务ID集合；
        List<String> activityIds = new ArrayList<>();
        //定义一个缓存的MAP
        Map<String, HistoricActivityInstance> historicActivityInstanceMap = new HashMap<>();
        //定义一个顺序的历史流程节点
        List<HistoricActivityInstance> historicActivityInstanceList = new ArrayList<>();
        // 线路集合；
        List<String[]> flowPaths = new ArrayList<>();
        HistoricActivityInstance historyActivity;
        String[] flowPath;
        //缓存查询出来的流程节点
        for(HistoricActivityInstance historicActivityInstance : highLightedActivityList){
            if(!historicActivityInstanceMap.containsKey(historicActivityInstance.getActivityId())){
                historicActivityInstanceMap.put(historicActivityInstance.getActivityId(), historicActivityInstance);
            }
        }
        //对历史流程节点进行排序
        for(Process process : processes){
            for(FlowElement flowElement : process.getFlowElements()){
                if(historicActivityInstanceMap.containsKey(flowElement.getId())){
                    historicActivityInstanceList.add(historicActivityInstanceMap.get(flowElement.getId()));
                }
            }
        }
        historicActivityInstanceMap.clear();
        historicActivityInstanceMap = null;
        highLightedActivityList.clear();
        highLightedActivityList = null;
        for (int i = 0; i < historicActivityInstanceList.size(); i++) {
            historyActivity = historicActivityInstanceList.get(i);
            activityIds.add(historyActivity.getActivityId());
            // 处理路线；
            if (i > 1) {
                flowPath = new String[2];
                flowPath[0] = activityIds.get(i - 1);
                flowPath[1] = historyActivity.getActivityId();
                flowPaths.add(flowPath);
            }
        }

        // 定义变量存储循环中的对象；
        Collection<FlowElement> flowElements;
        // 获取线路箭头的对象集合；
        List<SequenceFlow> arrows = new ArrayList<>();
        SequenceFlow sequenceFlow;
        FlowElement prevFlowElement;
        // 箭头的名称；
        String arrowName;
        // 获取箭头集合的大小；
        int arrowsSize;
        // 定义高亮的线路存储集合；
        List<String> highlightFlows = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processes)) {
            for (Process process : processes) {
                // 媒介请款的文字显示需要调换；
                if ("mediaRefund".equals(process.getId())) {
                    process.getFlowElement("sid-D89A84F3-9E0C-49E1-BA16-77586081123E").setName("否");
                    process.getFlowElement("sid-DBF2CAC3-F2B3-4BE0-88EF-5F5F20CA1622").setName("是");
                    process.getFlowElement("judgeLittleMoney").setName("是否金额大于5,000");
                    process.getFlowElement("judgeBigMoney").setName("是否金额大于1,000");
                }
                //根据流程id 查询请款中稿件类型，和流程实属公司
                List<Map<String, Object>> sp = processMapper.selectProcess(processInstanceId);
                if ("newMediaOutgo".equals(process.getId()) || "networkOutgo".equals(process.getId()) || "newspaperOutgo".equals(process.getId())) {
                    for (Map<String, Object> co : sp) {
//                        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
//                        if (CollectionUtils.isNotEmpty(sc) && sc.contains(co.get("company"))) {
                        if (!StringUtils.isEmpty(co.get("configurationProcess").toString()) && Integer.parseInt(co.get("configurationProcess").toString()) == 1 && !StringUtils.isEmpty(co.get("company").toString())){
                            String code= dictService.code(co.get("company").toString());
                            process.getFlowElement("sid-F89C02A9-0F7B-4BE0-B6C9-B7069ABCBF17").setName("不是"+code+"业务员");
                            process.getFlowElement("sid-8EF633B5-B6D4-4976-BCE0-DEB84F874578").setName("是"+code+"业务员");

                        }
                    }
                }
                if ("mediaSelfRefund".equals(process.getId())) {
                    for (Map<String, Object> co : sp) {
//                        List<String> sc = SysConfigUtils.getConfigValue("process", List.class);
//                        if (CollectionUtils.isNotEmpty(sc) && sc.contains(co.get("company"))) {
                            if (!StringUtils.isEmpty(co.get("company").toString()) && Integer.parseInt(co.get("configurationProcess").toString()) == 1) {
                                String code = dictService.code(co.get("company").toString());
                                process.getFlowElement("sid-942A0225-C928-4C8E-847F-9F732A6734A6").setName("不是" + code + "业务员");
                                process.getFlowElement("sid-3DCAA3B8-0CF0-487F-9243-0D7E071FF84C").setName("是" + code + "业务员");

                        }
                    }
                }
                if ("outgoProcess2020225".equals(process.getId())) {
                    for (Map<String, Object> co : sp) {
                        if (Integer.parseInt(co.get("mediaType").toString()) == 2) {
                            process.getFlowElement("sid-01F79109-6932-463A-9F62-3D8CB43B7F82").setName("金额小于等于5000");
                            process.getFlowElement("sid-5AF6BC0B-F40F-447B-A5E0-3D270C063245").setName("金额大于5000");
                        }
                    }

                }
                // 借款、借款中总经理审核的金额显示信息修改；
                if ("refund".equals(process.getId())) {
                    process.getFlowElement("judgeMoney").setName("是否金额大于1,000,000");
                }
                flowElements = process.getFlowElements();
                if (CollectionUtils.isNotEmpty(flowElements)) {
                    for (FlowElement flowElement : flowElements) {
                        if (flowElement instanceof SequenceFlow) {
                            sequenceFlow = (SequenceFlow) flowElement;
                            // 获取箭头的名称；
                            arrowName = sequenceFlow.getName();
                            // 判断是否已经修改过了，网关出来的箭头名称长度为1，部分流程的节点可能没有名字，注意；
                            if (!org.springframework.util.StringUtils.isEmpty(arrowName) && arrowName.length() == 1) {
                                prevFlowElement = sequenceFlow.getSourceFlowElement();
                                // 如果前一个节点是网关，获取其名称，更新到箭头的名称中；
                                if (prevFlowElement instanceof ExclusiveGateway) {
                                    sequenceFlow.setName(arrowName + "（" + prevFlowElement.getName() + "）");
                                }
                            }
                            arrows.add(sequenceFlow);
                        }
                    }
                    // 确认集合中有获取到数据；
                    arrowsSize = arrows.size();
                    if (arrowsSize > 0) {
                        // 第一个节点前的线路也是需要高亮的，流程发起时，路线是空的，请注意要高亮第一个节点；
                        if (flowPaths.isEmpty()) {
                            highlightFlows.add(arrows.get(0).getId());
                        } else {
                            for (int i = 0; i < flowPaths.size(); i++) {
                                flowPath = flowPaths.get(i);
                                for (int j = 0; j < arrowsSize; j++) {
                                    sequenceFlow = arrows.get(j);
                                    if (j == 0) {
                                        highlightFlows.add(sequenceFlow.getId());
                                        // 开始和结束的位置与路径一致就是已经走过的线路；
                                    } else {
                                        if (flowPath[0].equals(sequenceFlow.getSourceRef()) && flowPath[1].equals(sequenceFlow.getTargetRef())) {
                                            highlightFlows.add(sequenceFlow.getId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 查询流程定义信息；
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessDiagramGenerator diagramGenerator = new DefinedProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "PNG", activityIds, highlightFlows, "微软雅黑", "微软雅黑", "微软雅黑", null, 1.0, true);
        OutputStream out = null;
        try {
            int length = 0;
            byte[] buf = new byte[1024];
            out = response.getOutputStream();
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
        } catch (IOException e) {
            log.error("获取流程图异常。" + e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.error("流关闭异常。" + e);
            }
        }
    }
    private Map<String,Object> getBorrowData(double money,Integer type, String applyName, String title){
        Map<String,Object> map = new HashMap<>();
        User user = AppUtil.getUser();
        if ("JT".equals(user.getCompanyCode())){
            if(type == 0){
                //备用金借款
                map.put("gateCheckA",true);
                map.put("gateCheckC",money>50000);
            }else {
                map.put("gateCheckA",false);
                map.put("gateCheckB",money>20000);
            }
        }else {
            if(type == 0){
                //备用金借款
                map.put("gateCheckA",true);
                map.put("gateCheckB",money>50000);
            }else {
                map.put("gateCheckA",false);
                map.put("gateCheckC",money>20000);
            }
        }
        map.put("company",user.getCompanyCode());
        //消息分类
        map.put("messageUserName", applyName);
        //消息标题
        map.put("messageTitle", title);
        map.put("money", money);
        return map;
    }

    /**
     * 获取借款流程的网关配置；
     *
     * @param money：流程金额；
     * @return ：配置参数集合；
     */
    private Map<String, Object> getBorrowMoneyGateData(double money, Integer accountId, String applyName, String title) {
        Map<String, Object> map = new HashMap<>();
        // 设置第一个网关，判断流程申请人的职务是否符合要求；
        boolean isCEO = userService.getCEOFlag(AppUtil.getUser().getId());
        // 如果是副总以上的需注意增加审核意见；
        if (isCEO) {
            map.put("agree", true);
        }
        map.put("gateCheckA", isCEO);
        // 获取系统定义的网关金额；
        double definedMoney = Double.parseDouble(SpringUtils.get("managerMoney"));
        // 设置第二个网关，判断金额是否符合要求；
        map.put("gateCheckB", money >= definedMoney);
        //设置第三个网关，判断出款账户是否对公账户
        //map.put("gateCheckC", true);
        if (accountId != null) {
            Account account = accountService.getById(accountId);
            map.put("gateCheckC", IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType()));
        }
        //消息分类
        map.put("messageUserName", applyName);
        //消息标题
        map.put("messageTitle", title);
        map.put("money", money);
        return map;
    }

    /**
     * 获取报销流程的网关配置
     */
    private Map<String,Object> getReimbursermentData(double money, Integer accountId, String applyName, String title){
        Map<String,Object> map  = new HashMap<>();
        map.put("gateCheckA",money > 10000);
        //消息分类
        map.put("messageUserName", applyName);
        //消息标题
        map.put("messageTitle", title);
        map.put("money", money);
        map.put("company",AppUtil.getUser().getCompanyCode());
        return map;
    }
}