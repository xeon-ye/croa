package com.qinfei.qferp.service.flow;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.accountsMess.AccountsMess;
import com.qinfei.qferp.entity.administrative.*;
import com.qinfei.qferp.entity.biz.Project;
import com.qinfei.qferp.entity.crm.CrmCompanyProtect;
import com.qinfei.qferp.entity.employ.EmployEntry;
import com.qinfei.qferp.entity.employ.Employee;
import com.qinfei.qferp.entity.fee.*;
import com.qinfei.qferp.entity.inventory.*;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import com.qinfei.qferp.entity.meetingmanagement.MeetingManagement;
import com.qinfei.qferp.entity.performance.PerformanceScore;
import com.qinfei.qferp.entity.standardized.StandardizedCompany;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 开票流程的流程服务；
 *
 * @Author ：Yuan；
 * @Date ：2018/12/6 0006 14:01；
 */
public interface IProcessService {
    /**
     * 发起开票审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param invoice：需要审核的开票对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addBallotProcess(Invoice invoice, int urgencyLevel);

    /**
     * 发起借款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param borrow：需要审核的借款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addBorrowProcess(Borrow borrow, int urgencyLevel);

    /**
     * 发起媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addMediaRefundProcess(Outgo outgo, int urgencyLevel);

    /**
     * 发起媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String workupMediaRefundProcess(Outgo outgo, int urgencyLevel, Map<String, Object> map);

    /**
     * 发起自媒体媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addSelfMediaRefundProcess(Outgo outgo, int urgencyLevel,Integer mediaType,Boolean configurationProcess);

    /**
     * 发起报纸媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String newspaperOutgo(Outgo outgo, int urgencyLevel, Integer mediaType,Boolean configurationProcess);

    /**
     * 发起报纸媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String workupNewspaperOutgo(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean configurationProcess);

    /**
     * 发起新媒体媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String newMediaOutgo(Outgo outgo, int urgencyLevel, Integer mediaType,Boolean  configurationProcess);

    /**
     * 发起新媒体媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String workNewMediaOutgo(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean configurationProcess);

    /**
     * 发起网络媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String networkOutgo(Outgo outgo, int urgencyLevel, Integer mediaType,Boolean configurationProcess);

    /**
     * 发起网络媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String workupNetworkOutgo(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean configurationProcess);

    String addProcess(Outgo outgo, int urgencyLevel, int mediaType,boolean configurationProcess);

    String workupCompanyOutgo(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean configurationProcess);

    String addProjctProcess(Project project, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept);

    String addAccountsMessProcess(AccountsMess accountsMess,int urgencyLevel);
    /**
     * 发起河图媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addHtRefundProcess(Outgo outgo, int urgencyLevel);

    /**
     * 发起河图媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的自媒体媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String workupHtRefundProcess(Outgo outgo, int urgencyLevel, Map<String, Object> map);

    /**
     * 发起媒介请款审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outgo：需要审核的媒介请款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String workupSelfMediaRefundProcess(Outgo outgo, int urgencyLevel, Map<String, Object> map,boolean configurationProcess);

    /**
     * /** 发起退款申请审核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param refund：需要审核的退款对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addRefundProcess(Refund refund, int urgencyLevel);

    /**
     * 发起财务提成流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param commission：需要审核的提成对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addCommissionProcess(Commission commission, int urgencyLevel);

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
    String addEmployProcess(EmployEntry employEntry, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept);

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
    String addFormalProcess(Employee employee, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept);

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
    String addLeaveProcess(Employee employee, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept);

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
    String addTransferProcess(Employee employee, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept,Integer afterDept, int tranId);

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
    String addHandOverProcess(Employee employee, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept, int type, int conId);

    /**
     * 发起请假流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param leave：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addVocationProcess(Administrative leave, int urgencyLevel, Integer nextUser, String nextUserName, Integer nextUserDept);

    /**
     * 发起物品采购流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param purchase：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addPurchaseProcess(Purchase purchase, int urgencyLevel);

    /**
     * 发起物品领用流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param apply：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addApplyProcess(ReceiveApply apply, int urgencyLevel);

    /**
     * 发起物品报修流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param repair：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addRepairProcess(ReceiveRepair repair, int urgencyLevel);

    /**
     * 发起物品报废流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param scrap：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addScrapProcess(ReceiveScrap scrap, int urgencyLevel);

    /**
     * 发起物品归还流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param receiveReturn：需要审核的请假对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addReturnProcess(ReceiveReturn receiveReturn, int urgencyLevel);

    /**
     * 发起退稿流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param drop：需要审核的退稿对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addManuscriptProcess(Drop drop, int urgencyLevel);

    /**
     * 发起费用报销流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param reimbursement：需要审核的费用报销对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addReimbursementProcess(Reimbursement reimbursement, int urgencyLevel);

    /**
     * 发起外出申请报销流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param outWork：需要审核的外出申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addOutWorkProcess(Administrative adm, AdministrativeOutWork outWork, int urgencyLevel);

    /**
     * 发起加班申请流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param overTimeWork：需要审核的加班申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addOverTimeWorkProcess(Administrative adm, AdministrativeOverTimeWork overTimeWork, int urgencyLevel);


    /**
     * 发起出差申请流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param onBusiness：需要审核的出差申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addOnbusinessWorkProcess(Administrative adm, AdministrativeOnBusiness onBusiness, int urgencyLevel);

    /**
     * 发起出差申请流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param userBusinessPlan                                         ：需要审核的出差申请对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addOnbusiness(Administrative adm, UserBusinessPlan userBusinessPlan, int urgencyLevel);

    /**
     * 发起绩效考核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param performanceScores：需要审核的绩效考核集合对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addPerformanceProcess(List<PerformanceScore> performanceScores, int urgencyLevel);

    /**
     * 发起会议室考核流程：1、发起流程； 2、更新数据状态； 3、审核对象的代办事项和消息中添加数据；
     *
     * @param meet：需要审核的会议室对象，该对象需包含以下5个属性：主键ID，显示标题（String），taskId（String，保存审核节点的任务ID），此外，该对象应声明state（int，状态），itemId(int，待办事项的ID)属性，流程的回调会更新后三个字段；
     * @param urgencyLevel：审核的紧急程度，参考com.qinfei.qferp.entity.crm.Const；
     * @return ：审核的流程ID，流程提交失败返回空；
     */
    String addMeetRoomProcess(MeetingManagement meet, int urgencyLevel);

    /**
     * 批量审核流程数据：1、流程更新；2、数据状态更新；3、审核对象的代办事项和消息中添加数据；
     *
     * @param taskIds：需要审核的任务ID数组；
     * @param desc：审核备注信息；
     * @param agree                ：是否同意，true为是，false为否；
     * @return ：操作结果描述；
     */
    String approveProcess(String[] taskIds, String desc, boolean agree);

    String refused(Map<String, Object> map);

    String addProtectProcess(CrmCompanyProtect protect, int urgencyLevel);

    /**
     * 批量审核流程数据：1、流程更新；2、数据状态更新；3、审核对象的代办事项和消息中添加数据；
     *
     * @param taskIds：需要审核的任务ID数组；
     * @param desc：审核备注信息；
     * @param agree                ：是否同意，true为是，false为否；
     * @param nextGatewayValue     下一个网关值
     * @return ：操作结果描述；
     */
    String approveProcess(String[] taskIds, String desc, boolean agree, boolean nextGatewayValue);

    /**
     * 批量审核流程数据：1、流程更新；2、数据状态更新；3、审核对象的代办事项和消息中添加数据；
     *
     * @param taskIds：需要审核的任务ID数组；
     * @param desc：审核备注信息；
     * @param agree：是否同意，true为是，false为否；
     * @param nextUser：下个节点的审核人ID；
     * @param nextUserName：下个节点的审核人姓名；
     * @param nextUserDept：下个节点的审核人部门ID；
     * @return ：操作结果描述；
     */
    String approveProcess(String[] taskIds, String desc, boolean agree, Integer nextUser, String nextUserName, Integer nextUserDept);

    /**
     * 批量审核流程数据：1、流程更新；2、数据状态更新；3、审核对象的代办事项和消息中添加数据；
     *
     * @param taskIds：需要审核的任务ID数组；
     * @param desc：审核备注信息；
     * @param agree：是否同意，true为是，false为否；
     * @param nextUser：下个节点的审核人ID；
     * @param nextUserName：下个节点的审核人姓名；
     * @param nextUserDept：下个节点的审核人部门ID；
     * @param nextGatewayValue           下一个网关值
     * @return ：操作结果描述；
     */
    String approveProcess(String[] taskIds, String desc, boolean agree, Integer nextUser, String nextUserName, Integer nextUserDept, Boolean nextGatewayValue);

    /**
     * 流程的撤回；
     *
     * @param taskId：流程任务ID；
     * @return ：操作结果描述；
     */
    String withdrawProcess(String taskId, Integer itemId);

    /**
     * 流程的删除；
     *
     * @param taskId：流程任务ID；
     * @return ：操作结果描述；
     */
    String deleteProcess(String taskId, Integer itemId);

    /**
     * 分页查询登录人的审核任务列表；
     *
     * @param map：查询参数；
     * @param pageNum：当前页码；
     * @param pageSize：每页显示数量；
     * @return ：分页数据集合；
     */
    PageInfo<Map<String, Object>> listTasks(Map<String, String> map, int pageNum, int pageSize);

    /**
     * 获取流程当前节点审批完成任务
     *
     * @param map
     * @param pageNum
     * @param pageSize
     * @return
     */

    PageInfo<Map> theApproved(Map map, int pageNum, int pageSize);

    /**
     * 查询数据的审核记录；
     *
     * @param dataId：数据ID；
     * @param process：流程标志，定义参考com.qinfei.qferp.utils.IProcess；
     * @return ：审核记录集合；
     */
    List<Map<String, Object>> listTaskHistory(String dataId, int process);

    /**
     * 获取流程图；
     *
     * @param response：响应对象；
     * @param taskId：任务ID；
     */
    void getProcessImage(HttpServletResponse response, String taskId);

    /**
     * 获取流程图；
     *
     * @param response：响应对象；
     * @param dataId：任务ID；
     * @param process：流程类型，定义参考com.qinfei.qferp.utils.IProcess；
     */
    void getProcessImag(HttpServletResponse response, String dataId, int process);

    void rollback(String id, String target, Integer nextUserId);

    List<Map<String, String>> listTaskDefKey(String taskId);

    PageInfo<Map<String, Object>> listProcessDefinition(Map<String, Object> map, Pageable pageable);

    void deploy(List<String> fileNames, boolean uploadFlag);

    List<Map<String, String>> getProcessTaskDefKey(String taskId);

    //唤醒流程
    void workupProcess(Integer id, String name, Integer processType, String processName, String taskDefKey, String taskId, boolean gatewayFlag, String companyCode);

    //会议室审核流程
    void addMeetingRoomProcess(MeetingRoomApply meetingRoomApply);

    //批量获取流程审核列表
    Map<String, List<Map<String, Object>>> listFlowTask(List<Map<String, Object>> param);


    //标准化公司申请流程
    String addStandardizedCompanyProcess(StandardizedCompany standardizedCompany, int urgencyLevel);
}