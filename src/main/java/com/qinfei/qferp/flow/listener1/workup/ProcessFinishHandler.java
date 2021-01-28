package com.qinfei.qferp.flow.listener1.workup;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.flow.command.InsertHistoryTaskCommand;
import com.qinfei.qferp.flow.command.IntoAppointNodeCommand;
import com.qinfei.qferp.flow.listener1.ICommonTaskHandler;
import com.qinfei.qferp.mapper.fee.OutgoMapper;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.fee.IOutgoService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import com.qinfei.qferp.utils.SysConfigUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName ProcessFinishHandler
 * @Description 审核完成
 * @Author xuxiong
 * @Date 2019/9/27 0027 9:05
 * @Version 1.0
 */
public class ProcessFinishHandler implements TaskListener, ICommonTaskHandler {
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
        Map<String, Object> map = getTaskParam(delegateTask); //获取基础数据
        Integer state = (Integer) map.get("state");
        map.put("messageTypeName","唤醒流程");//消息子类类型
        map.put("type",22);//唤醒流程
        if(state == IConst.STATE_REJECT){
            //消息分类
            map.put("parentType",2);//提醒
            commonRejectHandle(delegateTask, (String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //驳回处理方法
        }else{
            //消息分类
            map.put("parentType",2);//提醒
            commonFinishHandle((String)map.get("pictureAddress"), (String)map.get(IProcess.PROCESS_NAME), (String)map.get("dataUrl"), map); //审核完成处理方法
        }

        Integer itemId = commonSendMessage(delegateTask, map); //统一消息处理逻辑，返回新增的待办ID
        // =================================================通知推送模块结束=================================================
        //开始启动待唤醒的流程

        ManagementService managementService = SpringUtils.getBean(ManagementService.class);

        Integer id = Integer.valueOf((String) map.get("dataId"));
        String successTaskId = delegateTask.getVariable("successTaskId", String.class); //唤醒记录原任务ID
        String result = "";
        if(state == IConst.STATE_REJECT){
            switch (delegateTask.getVariable("processType", Integer.class)){
                case IProcess.PROCESS_MEDIAREFUND:
                case IProcess.PROCESS_SELF_MEDIAREFUND:
                case IProcess.PROCESS_HTOUTPLEASETYPE:
                case IProcess.PROCESS_NEWSPAPEROUTGO:
                    IOutgoService outgoService = SpringUtils.getBean("outgoService");
                    Outgo outgo = new Outgo();
                    outgo.setId(id);
                    outgo.setState(IProcess.PROCESS_FINISHED);
                    outgoService.update(outgo);
                    //将原流程记录增加一个唤醒失败状态
                    managementService.executeCommand(new InsertHistoryTaskCommand(successTaskId, "唤醒流程",
                            AppUtil.getUser().getName(), AppUtil.getUser().getId()+"", "<b style='color:red;'>【唤醒驳回】</b>"));
                    break;
            }
        }else{
            IProcessService processService = SpringUtils.getBean("processService");
            FlowableMapper flowableMapper = SpringUtils.getBean(FlowableMapper.class);
            UserMapper userMapper = SpringUtils.getBean("userMapper");
            RepositoryService repositoryService = SpringUtils.getBean(RepositoryService.class);

            Integer processType = delegateTask.getVariable("processType", Integer.class); //带唤醒流程类型
            boolean gatewayFlag = delegateTask.getVariable("gatewayFlag", Boolean.class);//是否被唤醒流程中含有网关，有的话需要将原网关状态值设置到流程中
            String taskDefKey = delegateTask.getVariable("taskDefKey", String.class); //唤醒到指定流程节点

            Map<String, String> processMap = flowableMapper.findProcInstIdByHisTaskId(successTaskId); //带唤醒流程上一次完成时的任务ID
            String proId = processMap.get("processInstId");
            String proDefId = processMap.get("processDefId");

            List<Process> processes = repositoryService.getBpmnModel(proDefId).getProcesses();// 获取高亮的路线图；用于排序
            List<Map<String, String>> oldActList = flowableMapper.listActByProcInstId(proId); //获取原流程所有节点，由于时间错乱，还是会有顺序错乱问题
            Map<String, Map<String, String>> oldActMap = new HashMap<>();
            List<Map<String, String>> actList = new ArrayList<>();//处理排序后的节点集合
            //缓存查询出来的流程节点
            for(Map<String, String> oldAct : oldActList){
                if(!oldActMap.containsKey(oldAct.get("taskDefKey"))){
                    oldActMap.put(oldAct.get("taskDefKey"), oldAct);
                }
            }
            //对历史流程节点进行排序
            for(Process process : processes){
                for(FlowElement flowElement : process.getFlowElements()){
                    if(oldActMap.containsKey(flowElement.getId())){
                        actList.add(oldActMap.get(flowElement.getId()));
                    }
                }
            }
            Map<String, Object> gatewayMap = new HashMap<>(); //网关的数据
            if(gatewayFlag){
                List<Map<String, Object>> gatewayList = flowableMapper.listGatewayValList(proId); //获取上一次流程完成时的网关值
                if(!CollectionUtils.isEmpty(gatewayList)){
                    for(Map<String, Object> gateway : gatewayList){
                        boolean value = Long.parseLong(String.valueOf(gateway.get("value"))) == 1 ? true : false;
                        gatewayMap.put((String)gateway.get("name"), value);
                    }
                }
            }
            switch (processType){
                case IProcess.PROCESS_MEDIAREFUND:
                case IProcess.PROCESS_SELF_MEDIAREFUND:
                case IProcess.PROCESS_HTOUTPLEASETYPE:
                case IProcess.PROCESS_NEWSPAPEROUTGO:
                    //启动流程
                    IOutgoService outgoService = SpringUtils.getBean("outgoService");
                    Outgo outgo = outgoService.getById(id);
                    User user = userMapper.getById(outgo.getApplyId()); //获取申请人信息
                    Integer parentType = 2;
                    Integer mediaGroupLeader = null;
                    //更新更改数据覆盖原纪录(当有值并且有更改值)
                    if(!StringUtils.isEmpty(outgo.getEditJson())){
                        Outgo editOutgo = JSON.parseObject(outgo.getEditJson(), Outgo.class);
                        parentType = editOutgo.getParentType();
                        mediaGroupLeader = editOutgo.getMediaGroupLeader(); //获取前台传设置的审核组长
                        //如果有改动值，则进行更新
                        if(!(StringUtils.isEmpty(editOutgo.getTitle()) && StringUtils.isEmpty(editOutgo.getAccountName())
                                && StringUtils.isEmpty(editOutgo.getAccountBankNo()) && StringUtils.isEmpty(editOutgo.getAccountBankName())
                                && editOutgo.getExpertPayTime() == null && editOutgo.getApplyAmount() == null
                                && StringUtils.isEmpty(editOutgo.getRemark()) && editOutgo.getActualCost() == null
                                && editOutgo.getCostEraseAmount() == null && editOutgo.getOutgoEraseAmount() == null
                                && editOutgo.getInvoiceFlag() == null && editOutgo.getOutgoTax() == null
                                && editOutgo.getTaxAmount() == null && StringUtils.isEmpty(editOutgo.getInvoiceRise())
                                && editOutgo.getPayAmount() == null && StringUtils.isEmpty(editOutgo.getInvoiceCode())
                                && editOutgo.getInvoiceTax() == null && editOutgo.getInvoiceType() == null)){
                            outgoService.update(editOutgo);
                            //接下来的流程采用更新后的价格计算网关,
                            if(editOutgo.getApplyAmount() != null){
                                outgo.setApplyAmount(editOutgo.getApplyAmount());
                            }
                            //如果标题发生变更，采用新的标题
                            if(!StringUtils.isEmpty(editOutgo.getTitle())){
                                outgo.setTitle(editOutgo.getTitle());
                            }
                            //如果是否回填标识发生变更，采用新的标识
                            if(editOutgo.getInvoiceFlag() != null){
                                //如果不开票，则更新开票信息为空
                                if(editOutgo.getInvoiceFlag() == 2){
                                    OutgoMapper outgoMapper = SpringUtils.getBean(OutgoMapper.class);
                                    outgoMapper.backfill(editOutgo);
                                }
                                outgo.setInvoiceFlag(editOutgo.getInvoiceFlag());
                            }
                        }
                    }
                    //流程启动参数
                    Map<String, Object> param = new HashMap<>();
                    param.put("workup", "workup"); //唤醒标识，启动流程逻辑中判断是否有该标识决定设置流程发起者
                    // 提交人的用户ID；
                    param.put("userId", String.valueOf(user.getId()));
                    // 提交人用户名称；
                    param.put("userName", user.getName());
                    // 提交人的部门ID；
                    param.put("initiatorDept", user.getDeptId());
                    // 提交人的部门名称；
                    param.put("initiatorDeptName", user.getDeptName());
                    // 提交人的用户ID，用于发送信息填充字段；
                    param.put("initiatorWorker", user.getId());
                    // 获取公司代码；
                    param.put("company",outgo.getCompanyCode());
                    param.put("companyCode", user.getCompanyCode());
                    param.put("messageTitle",outgo.getTitle());
                    param.put("messageUserName",outgo.getApplyName());
                    param.put("meidaTypeId",outgo.getMediaTypeId());
                    param.put("mediaType", parentType);
                    param.put("gateCheckE",outgo.getInvoiceFlag() == 1);
                    if(gatewayMap.get("gateCheckE") != null){
                        gatewayMap.put("gateCheckE",outgo.getInvoiceFlag() == 1);
                    }
                    if(mediaGroupLeader != null && ("groupLeaderApprove".equals(taskDefKey) || StringUtils.isEmpty(taskDefKey))){ //如果是组长审核，使用前台传入的组长
                        User groupLeader = userMapper.getById(mediaGroupLeader);
                        param.put("nextUser", groupLeader.getId());
                        param.put("nextUserName", groupLeader.getName());
                        param.put("nextUserDept", groupLeader.getDeptId());
                    }

                    //读取请款流程配置，当满足要求，则走特殊流程，例如：河图公司流程，河图媒介请款其他公司业务或者河图媒介请款河图业务员等
//                    Map<String, Object> tempParam = new HashMap();
//                    tempParam.put("typeCode","process");
//                    tempParam.put("type","1");
//                    String companyCodeStr = outgoService.HTLScompanyCode(tempParam);
                    List<String> companyCodeStr= SysConfigUtils.getConfigValue("process",List.class);
                    boolean isCompanyProcessFlag = false; //是否有发起特殊流程
                    boolean configurationProcess =false;
                    if(CollectionUtils.isNotEmpty(companyCodeStr)){
                        //河图媒介
                        if(companyCodeStr.contains(user.getCompanyCode())){
                            //河图业务员
                            if(companyCodeStr.contains(outgo.getCompanyCode())){
                                result = processService.workupHtRefundProcess(outgo, 3, param);
                                isCompanyProcessFlag = true;
                                configurationProcess = true;
                            }else {
                                result = processService.workupCompanyOutgo(outgo, 3, param,false);
                                isCompanyProcessFlag = true;
                            }
                        }
                    }
                    if(!isCompanyProcessFlag){
                        if(parentType == 1){
                            List<String> plateCode= SysConfigUtils.getConfigValue("plateCode",List.class);
                            if (plateCode.contains(outgo.getMediaTypeId().toString()) &&CollectionUtils.isNotEmpty(plateCode)){
//                            if(outgo.getMediaTypeId() == 8 || outgo.getMediaTypeId() == 317){
                                result = processService.workupSelfMediaRefundProcess(outgo, 3, param,configurationProcess);
                            }else {
                                result = processService.workupNetworkOutgo(outgo, 3, param,configurationProcess);
                            }
                        }else {
                            if(outgo.getMediaTypeId() == 3){
                                result = processService.workupNewspaperOutgo(outgo, 3, param,configurationProcess);
                            }else {
                                result = processService.workNewMediaOutgo(outgo, 3, param,configurationProcess);
                            }
                        }
                    }
                    if(result != null && !"操作失败".equalsIgnoreCase(result)){
                        OutgoMapper outgoMapper = SpringUtils.getBean(OutgoMapper.class);
                        outgoMapper.updatePayTimeForNull(outgo.getId()); //更新时间出款时间
                        int lastCWKeyIndex = 0;
                        int currentTaskKeyIndex = 0;
                        for(int i = 0; i < actList.size(); i++){
                            if(taskDefKey.equals(actList.get(i).get("taskDefKey"))){
                                currentTaskKeyIndex = i;
                            }
                            if("cashierParagraph".equals(actList.get(i).get("taskDefKey"))){
                                lastCWKeyIndex = i;
                            }
                        }
                        if(currentTaskKeyIndex <= lastCWKeyIndex){ //出纳出款，之前的稿件状态都设置成2-进行中
                            //将请款稿件请款状态设置为2-进行中
                            List<Integer> ids = outgoMapper.queryArticleIdsByOutgoIdAndState(id, IConst.FEE_STATE_FINISH);
                            if(!CollectionUtils.isEmpty(ids)){
                                Map<String, Object> temp = new HashMap<>();
                                temp.put("list", ids);
                                temp.put("state", IConst.FEE_STATE_PROCESS);
                                outgoMapper.changeOutgoState(temp);
                            }
                        }
                        //将原流程记录增加一个唤醒成功状态
                        managementService.executeCommand(new InsertHistoryTaskCommand(successTaskId, "唤醒流程",
                                AppUtil.getUser().getName(), AppUtil.getUser().getId()+"", "<b style='color:green;'>【唤醒成功】</b>"));
                        managementService.executeCommand(new IntoAppointNodeCommand(result, taskDefKey, actList,gatewayMap));
                    }else{
                        //流程未创建成功，则设置唤醒失败
                        Outgo outgo3 = new Outgo();
                        outgo3.setId(id);
                        outgo3.setState(IProcess.PROCESS_FINISHED);
                        outgoService.update(outgo3);
                        //将原流程记录增加一个唤醒失败状态
                        managementService.executeCommand(new InsertHistoryTaskCommand(successTaskId, "唤醒流程",
                                outgo.getApplyName(), outgo.getApplyId()+"", "<b style='color:red;'>【唤醒失败】</b>"));
                    }
                    break;
            }
        }

    }
}
