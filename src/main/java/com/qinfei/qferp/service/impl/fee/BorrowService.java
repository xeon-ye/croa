package com.qinfei.qferp.service.impl.fee;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.config.Config;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.fee.Borrow;
import com.qinfei.qferp.entity.fee.BorrowRepay;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.fee.BorrowMapper;
import com.qinfei.qferp.mapper.fee.BorrowRepayMapper;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.fee.IBorrowService;
import com.qinfei.qferp.service.fee.IOutgoService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class BorrowService implements IBorrowService {
    @Autowired
    private BorrowMapper borrowMapper ;
    @Autowired
    private ItemsService itemsService ;
    @Autowired
    private IProcessService processService ;
    @Autowired
    private IUserService userService ;
    @Autowired
    private BorrowRepayMapper borrowRepayMapper ;
    @Autowired
    private IOutgoService outgoService ;
    @Autowired
    private IAccountService accountService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private Config config;
    @Autowired
    private FlowableMapper flowableMapper;

    //借款状态
    private final static Map<Integer, String> borrowStateMap = new HashMap<Integer, String>(){
        {
            put(-1,"审核驳回");
            put(0,"已保存");
            put(1,"已完成");
            put(2,"审核通过");
            put(3,"组长审核");
            put(4,"部长审核");
            put(5,"总监审核");
            put(6,"财务总监审核");
            put(7,"副总经理审核");
            put(8,"总经理审核");
            put(9,"会计确认");
            put(10,"业务员确认");
            put(12,"财务部长审核");
            put(16,"出纳出款");
            put(30,"集团财务部门负责人");
            put(31,"集团财务分管领导");
            put(32,"集团总裁");
        }
    };
    //借款类型
    private final static Map<Integer, String> borrowTypeMap = new HashMap<Integer, String>(){
        {
            put(0,"备用金");
            put(1,"其他");
        }
    };


    @Override
    public PageInfo<Map> listPg(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = borrowMapper.listPg(map);
//        Integer userId = AppUtil.getUser().getId();
//        for (Map tmp : list) {
//            String taskId = String.valueOf(tmp.get("taskId"));
//            FindFinanceTaskOwnerCommand findTaskOwner = new FindFinanceTaskOwnerCommand(taskId);
//            managementService.executeCommand(findTaskOwner);
//            if (StringUtils.equalsIgnoreCase(findTaskOwner.getUserId(), String.valueOf(userId)))
//                tmp.put("isOwner", true);
//            else
//                tmp.put("isOwner", false);
//        }
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map> listPgForOutgo(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        Integer outgoId = Integer.parseInt(map.get("outgoId").toString());
        Outgo outgo = outgoService.getById(outgoId);
        map.put("companyCode",outgo.getCompanyCode()) ;
        List<Map> list = borrowMapper.listPgForOutgo(map);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map> listPgForReimbursement(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = borrowMapper.listPgForReimbursement(map);
        return new PageInfo<>(list);
    }

    @Override
    public Borrow getById(Integer id) {
        return borrowMapper.getById(id);
    }

    @Override
    @Transactional
    public Borrow add(Borrow entity) {
        //生成KP2018110001的编号，前六位是年月，后四位累加
        User user = AppUtil.getUser();
        entity.setId(null);
        entity.setApplyId(user.getId());
        entity.setApplyName(user.getName());
        entity.setDeptId(user.getDeptId());
        entity.setDeptName(user.getDeptName()) ;
        entity.setCreator(user.getId());
        entity.setApplyTime(new Date());
        entity.setCreateTime(new Date());
        entity.setCompanyCode(user.getCompanyCode());
        entity.setCode(IConst.BORROW_CODE+ CodeUtil.getMonthStr()+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.BORROW_CODE),4)) ;
        borrowMapper.insert(entity);
        if(entity.getState()>0){//提交
            // 紧急程度字段暂不启用
            // taskId为空：首次提交审核；不为空：驳回后提交审核
            processService.addBorrowProcess(entity, 3);
        }
        return entity;
    }

    @Override
    public Borrow edit(Borrow entity) {
        User user = AppUtil.getUser();
        entity.setUpdateUserId(user.getId());
        borrowMapper.update(entity);
        //处理待办
        finishItem(entity) ;
        //借款暂存
        if(entity.getState() > 0){//借款提交审核
            // 紧急程度字段暂不启用
            // taskId为空：首次提交审核；不为空：驳回后提交审核
            processService.addBorrowProcess(entity, 3);
        }
        return entity;
    }
    @Override
    public Borrow update(Borrow entity) {
        borrowMapper.update(entity);
        return entity;
    }
    @Override
    @Transactional
    public void delById(Borrow entity) {
        User user = AppUtil.getUser() ;
        entity.setState(IConst.STATE_DELETE);
        entity.setUpdateUserId(user.getId());
        borrowMapper.update(entity);
        //处理待办
        finishItem(entity) ;
    }

    /**
     * 处理待办
     * @param entity
     */
    private void finishItem(Borrow entity){
        if(entity.getItemId()!=null){
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }

    @Override
    @Transactional
    public Borrow confirm(Map map,Borrow entity) {
        User user = AppUtil.getUser() ;
        Double payAmount ;
        try{
            payAmount = Double.parseDouble(map.get("payAmount").toString()) ;
        }catch (Exception e){
            throw new QinFeiException(1002,"获取的付款金额不正确：获取到的出账金额="+map.get("payAmount").toString()) ;
        }
        Integer outAccountId = 0 ;
        try{
            outAccountId = Integer.parseInt((String)map.get("outAccountIds")) ;
        }catch (Exception e){
            throw new QinFeiException(1002,"获取的出款账户不正确：获取到的出账账户id="+map.get("outAccountIds").toString()) ;
        }
        Object payTime = map.get("payTime") ;
        if(ObjectUtils.isEmpty(payTime)){
            entity.setPayTime(new Date());
        }else{
            try{
                entity.setPayTime(DateUtils.parse(payTime.toString(),DateUtils.DATE_FULL));
            }catch (Exception e){
                throw new QinFeiException(1002,"获取日期格式不正确：获取到的日期为："+payTime.toString());
            }
        }

        Account account = accountService.getById(outAccountId);
        entity.setOutAccountId(outAccountId);
        entity.setOutAccountName(account.getName());
        if(IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType())){
            entity.setState(IConst.STATE_KJ);//财务会计复核
        } else {
            finishItem(entity);
            entity.setState(IConst.STATE_FINISH);//1通过
            //普通账户直接结束通知发起人
            sendMessage(entity,String.format("[%s]您提交的借款申请[%s]审核已完成。","借款",entity.getTitle()),"/fee/queryBorrow?flag=1&id="+entity.getId());
        }
        entity.setPayAmount(payAmount);
        entity.setRemainAmount(payAmount);
        entity.setUpdateUserId(user.getId());
        entity.setPayUserId(user.getId());
        try{
            borrowMapper.updateaccount(entity);
        }catch (Exception e){
            throw new QinFeiException(1002,"更新出款账户失败！借款id="+entity.getId());
        }

        //创建事务回滚点，就算后面的业务有异常，前面的业务也要执行
//        Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
        try{
            String taskId = entity.getTaskId() ;
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckC", IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType()));
            String[] taskIds = new String[]{taskId};
            String desc = map.get("desc").toString();
            processService.approveProcess(taskIds, desc, true);
        }catch (Exception e){
            throw new QinFeiException(1002,"审批流异常！借款id="+entity.getId());
            //回滚事务
//            TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
        }

        return entity;
    }
    //增加待办
    public Items addItemCheckBtoB(Borrow entity,User user){
        Items items = new Items();
        items.setItemName(entity.getCode()+entity.getTitle()+"-借款出账确认等待处理");
        items.setItemContent("您有新的借款出账确认需要处理");
        items.setWorkType("出账确认申请");
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryBorrow?flag=5&id="+entity.getId());
        items.setFinishAddress("/fee/queryBorrow?flag=1&id="+entity.getId());
        User cw = userService.getCWBZInfo(user.getCompanyCode()) ;
        items.setAcceptWorker(cw.getId()) ;
        items.setAcceptDept(cw.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }
    @Override
    @Transactional
    public Borrow checkBtoB(Borrow entity, String desc) {
        User user = AppUtil.getUser() ;
        entity.setState(IConst.STATE_FINISH);//1通过
        entity.setUpdateUserId(user.getId());
        try{
            String[] taskIds = new String[]{entity.getTaskId()};
            processService.approveProcess(taskIds, desc, true);
        }catch (Exception e){
            throw new QinFeiException(1002,"审批流异常,借款id="+entity.getId());
        }

        //处理待办
        //finishItem(entity) ;
        //borrowMapper.update(entity);
        return entity;

    }

    @Override
    @Transactional
    public Borrow repay(Borrow entity) {
        User user = AppUtil.getUser() ;
        entity.setRepayTime(new Date());
        entity.setUpdateUserId(user.getId());

        Items items = addItem(entity,user) ;
        entity.setItemId(items.getId());
        try{
            dealBorrowInfo(entity,entity.getAmount()) ;
        }catch(Exception e){
            throw new QinFeiException(1002,"处理借款表数据失败！") ;
        }

        try{
            //再插入新的还款关系表
            insertBorrowRepay(entity.getId(),entity.getRepayRemark()) ;
        }catch (Exception e){
            throw new QinFeiException(1002,"处理还款表数据失败！") ;
        }
        return entity;
    }
    /**
     * 向借款还款关系表插入数据
     */
    private void insertBorrowRepay(Integer borrowId,String remark){
        User user = AppUtil.getUser() ;
        Borrow entity = borrowMapper.getById(borrowId) ;
        BorrowRepay repay = new BorrowRepay() ;
        repay.setBorrowId(borrowId);
        repay.setRepayId(entity.getId());
        repay.setRepayCode(entity.getCode());
        repay.setType(IConst.REPAY_TYPE_BORROW);
        repay.setAmount(entity.getAmount());
        repay.setState(IConst.REPAY_STATE_SAVE);
        repay.setCreateTime(new Date());
        repay.setCreateUserId(user.getId());
        repay.setCreateName(user.getName());
        repay.setRemark(remark);
        borrowRepayMapper.insert(repay) ;
    }
    //增加待办
    private Items addItem(Borrow entity, User user){
        Items items = new Items();
        items.setItemName(entity.getTitle()+"-还款确认等待处理");
        items.setItemContent("您有新的还款申请需要处理");
        items.setWorkType("还款申请");
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryBorrow?repayId="+entity.getId());
        items.setFinishAddress("/fee/queryBorrow?repayId="+entity.getId());
        User cw = userService.getTellerInfo(user.getCompanyCode()) ;
        items.setAcceptWorker(cw.getId()) ;
        items.setAcceptDept(cw.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }

    @Override
    @Transactional
    public Borrow repayConfirm(Borrow entity) {
        User user = AppUtil.getUser() ;
        entity.setRepaying(IConst.REPAYING_FALSE);
        entity.setUpdateUserId(user.getId());
        borrowMapper.update(entity);
        finishItem(entity);

        BorrowRepay repay = borrowRepayMapper.getByRepayIdAndBorrowIdAndTypeAndState(entity.getId(),entity.getId(),IConst.REPAY_TYPE_BORROW,IConst.REPAY_STATE_SAVE) ;
        if(repay != null){
            repay.setState(IConst.REPAY_STATE_FINISH);
            repay.setUpdateUserId(user.getId());
            repay.setUpdateTime(new Date());
            borrowRepayMapper.update(repay);
        }else{
            throw new QinFeiException(1002,"未找到借款对应的还款记录！");
        }
        return entity;
    }
    @Override
    @Transactional
    public Borrow repayReject(Borrow entity) {
        entity.setRepaying(IConst.REPAYING_FALSE);
        entity.setUpdateUserId(AppUtil.getUser().getId());
        backBorrowInfo(entity,entity.getAmount()) ;
        //待办变已办
        finishItem(entity);
        //删除旧的还款关系表
        borrowRepayMapper.deleteByRepayIdAndTypeAndState(entity.getId(),IConst.REPAY_TYPE_BORROW,IConst.REPAY_STATE_SAVE);
        return entity;
    }

    /**
     * state=2时财务撤回，此时，借款状态还未变更
     * 1、完成待办
     * 2、增加新的待办
     * 3、请款订单状态修改为-1
     * @param entity
     */
    @Override
    @Transactional
    public void CWReject(Borrow entity){
        String taskId= entity.getTaskId();
        try{
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckC", false);
            //(gateCheckC为true时不用发消息，false特殊发消息，不然会多发流程消息)
            sendMessage(entity,String.format("[%s]您提交的借款申请[%s]在%s节点被驳回","借款",entity.getTitle(),"出纳出款"),"/fee/queryBorrow?flag=0&id="+entity.getId());
        }catch (Exception e){
            throw new QinFeiException(1002,"审批流异常，借款id="+entity.getId()) ;
        }

        entity.setState(IConst.STATE_REJECT);
        entity.setUpdateUserId(AppUtil.getUser().getId());
        //1、待办变已办
        finishItem(entity) ;
        //2、增加待办
       User user = AppUtil.getUser() ;
       Items item = addApplyItem(entity,user) ;
        entity.setItemId(item.getId());
        //3、借款订单状态修改
        borrowMapper.update(entity);
    }

    /**
     * state=1或state=12时财务撤回，需要变更以下内容
     * 3、完成待办
     * 4、增加新的待办
     * 5、请款订单状态修改为-1，出款账户和时间还原
     * @param entity
     */
    @Override
    @Transactional
    public Boolean CWReturn(Borrow entity){
        String taskId= entity.getTaskId();
        try{
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckC", false);
        }catch (Exception e){
            throw new QinFeiException(1002,"审批流异常，借款id="+entity.getId()) ;
        }
        entity.setState(IConst.STATE_REJECT);
        //1、待办变已办
        finishItem(entity) ;
//        //2、增加待办
        User user = AppUtil.getUser() ;
//        Items item = addApplyItem(entity,user) ;
//        entity.setItemId(item.getId());
        //3、更新借款订单状态
        borrowMapper.returnBorrowInfo(entity.getId(),user.getId());
        return true ;
    }

    //增加待办
    private Items addApplyItem(Borrow entity, User user){
        Items items = new Items();
        items.setItemName(entity.getTitle()+"-借款撤回等待处理");
        items.setItemContent("您有新的借款撤回需要处理");
        items.setWorkType("借款撤回");
        User mediaUser = userService.getById(entity.getApplyId()) ;
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryBorrow?flag=0&id="+entity.getId());
        items.setFinishAddress("/fee/queryBorrow?flag=1&id="+entity.getId());
        items.setAcceptWorker(mediaUser.getId()) ;
        items.setAcceptDept(mediaUser.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }

    @Override
    public List<BorrowRepay> queryRepayByBorrowId(Integer borrowId) {
        return borrowRepayMapper.queryByBorrowId(borrowId);
    }

    //借款表还款
    @Override
    public void dealBorrowInfo(Borrow borrow, Double amount){
        borrow.setRemainAmount(new BigDecimal(borrow.getRemainAmount()).
                subtract(new BigDecimal(amount)).
                setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        borrow.setRepayAmount(new BigDecimal(borrow.getRepayAmount()).
                add(new BigDecimal(amount)).
                setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        if(Math.abs(borrow.getRemainAmount())<0.01){
            //还清了，置标志位为1
            borrow.setRepayFlag(IConst.REPAY_STATE_FINISH) ;
        }
        borrowMapper.update(borrow) ;
    }

    //借款表还款信息撤回
    @Override
    public void backBorrowInfo(Borrow borrow, Double amount){
        borrow.setRemainAmount(new BigDecimal(borrow.getRemainAmount()).
                add(new BigDecimal(amount)).
                setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        borrow.setRepayAmount(new BigDecimal(borrow.getRepayAmount()).
                subtract(new BigDecimal(amount)).
                setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        if(Math.abs(borrow.getRemainAmount())>0){
            //还原标志位为0
            borrow.setRepayFlag(IConst.REPAY_STATE_SAVE) ;
        }
        borrowMapper.update(borrow) ;
    }

    @Override
    public List<Map> exportBorrow (Map map, OutputStream outputStream){
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = borrowMapper.listPg(map);
        String[] heads = {"借款编号","标题","申请人","所属部门","申请金额","申请日期",
                "实际出款账户", "实际出款金额","实际出款日期","已还金额","未还金额"};
        String[] fields = {"code","title","applyName","deptName","applyAmout",
                "applyTime","outAccountName", "payAmount","payTime","repayAmount",
                "remainAmount"};
        ExcelUtil.exportExcel("借款列表", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("applyAmount".equals(field)||"payAmount".equals(field)||"repayAmount".equals(field)||"remainAmount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else if ("applyTime".equals(field)||"payTime".equals(field)) {
                    cell.setCellValue(value.toString());
//                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                }
                else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }
    @Override
    public Map remburseSum(Map map){
        User user = AppUtil.getUser();
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        map.put("user",user);
        Map<String,Object> resultMap = borrowMapper.reimburseSum(map);
        return  initResult(resultMap);
    }

    @Override
    public String downloadBorrowData(Map<String, Object> param) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(param.get("auditTable") != null){
                List<Map> auditTable = JSON.parseArray(String.valueOf(param.get("auditTable")), Map.class);
                param.put("auditTable", auditTable);
            }
            return DataImportUtil.createBorrowFile("借款信息", config.getUploadDir(), config.getWebDir(), Arrays.asList(param));
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "下载借款信息异常！");
        }
    }

    @Override
    public String batchDownloadData(Map<String, Object> param) {
        try{
            List<Map> list = listBorrowData(param);
            return DataImportUtil.createBorrowFile("借款信息", config.getUploadDir(), config.getWebDir(), list);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "批量下载借款信息异常！");
        }
    }

    @Override
    public List<Map> listBorrowData(Map<String, Object> param) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(param.get("deptId") != null){//当且仅指定了部门时
                Integer deptId = Integer.parseInt(String.valueOf(param.get("deptId")));//获取请求的部门ID
                String deptIds = userService.getChilds(deptId);
                if (deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                }
                param.put("deptIds", deptIds);
            }
            List<Map> list = borrowMapper.listPg(param);
            //获取审核列表
            if(!CollectionUtils.isEmpty(list)){
                List<Map<String, Object>> paramList = new ArrayList<>();
                list.forEach(o -> {
                    //设置查询审核详情条件
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("dataId", o.get("id"));
                    tmp.put("process", 2);
                    paramList.add(tmp);
                    //设置中文值
                    o.put("modalTitle", String.format("【借款】%s-%s-%s", String.valueOf(o.get("code")),String.valueOf(o.get("applyName")),String.valueOf(o.get("applyTime"))));
                    String type = borrowTypeMap.get(Integer.parseInt(String.valueOf(o.get("type"))));
                    String state = borrowStateMap.get(Integer.parseInt(String.valueOf(o.get("state"))));
                    o.put("typeName", type);
                    o.put("state", state);
                });
                List<Map<String, Object>> taskList =  flowableMapper.listHistoryTask(paramList);
                if(!CollectionUtils.isEmpty(taskList)){
                    for(Map borrow : list){
                        String dataId = String.valueOf(borrow.get("id"));
                        List<Map<String, Object>> tmpList = new ArrayList<>();
                        for(Map<String, Object> task : taskList){
                            if("0".equals(String.valueOf(task.get("state")))){
                                task.put("descName", "正在审核");
                            }else {
                                String desc = String.valueOf(task.get("desc"));
                                String [] descArr = desc.split("</");
                                desc = (descArr != null && descArr.length > 0) ? descArr[0].substring(descArr[0].indexOf(">")+1) : "";
                                task.put("descName", desc);
                            }
                            if(dataId.equals(String.valueOf(task.get("dataId")))){
                                tmpList.add(task);
                            }
                        }
                        borrow.put("auditTable", tmpList);
                    }
                }
            }
            return list;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "批量打印借款信息异常！");
        }
    }

    private Map initResult(Map resultMap) {
        if (resultMap == null || resultMap.size() == 0) {
            resultMap = new HashMap<>();
            resultMap.put("repayAmount", 0);
            resultMap.put("payAmount", 0);
            resultMap.put("applyAmount", 0);
            resultMap.put("remainAmount", 0);


        }
        return resultMap;
    }

    //借款发消息
    private void sendMessage(Borrow entity,String tips,String url) {
        User user = AppUtil.getUser();
        User obj = userMapper.getById(entity.getApplyId());
        String subject = "[借款]";
        // 推送WebSocket消息(右侧弹框消息)；
        WSMessage message = new WSMessage();
        message.setReceiveUserId(obj.getId() + "");
        message.setReceiveName(obj.getName());
        message.setSendName(user.getName());
        message.setSendUserId(user.getId() + "");
        message.setSendUserImage(user.getImage());
        message.setContent(tips);
        message.setSubject(subject);
        message.setUrl(null);
        WebSocketServer.sendMessage(message);

        //发送消息的内容
        Message mes = new Message();
        String userImage = user.getImage();
        // 获取消息显示的图片；
        String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
        mes.setPic(pictureAddress);
        //消息分类
        mes.setParentType(2);//提醒
        mes.setType(3);//请款
        mes.setContent(tips);
        mes.setInitiatorWorker(user.getId());
        mes.setInitiatorDept(user.getDeptId());
        mes.setAcceptWorker(entity.getApplyId());
        mes.setAcceptDept(obj.getDeptId());
        mes.setUrl(url);
        mes.setUrlName("借款管理");
        messageService.addMessage(mes);
    }
    @Override
    @Transactional
    public Borrow changeAccount(Borrow entity, Map map){
        User user = AppUtil.getUser();
        Integer outAccountId = 0;
        try {
            outAccountId = Integer.parseInt((String) map.get("outAccountIds"));
        } catch (Exception e) {
            throw new QinFeiException(1002, "获取的出款账户不正确：获取到的出账账户id=" + map.get("outAccountIds").toString());
        }
        Object payTime = map.get("payTime");
        if (ObjectUtils.isEmpty(payTime)) {
            entity.setPayTime(new Date());
        } else {
            try {
                entity.setPayTime(DateUtils.parse(payTime.toString(), DateUtils.DATE_FULL));
            } catch (Exception e) {
                throw new QinFeiException(1002, "获取日期格式不正确：获取到的日期为：" + payTime.toString());
            }
        }
        Account account = accountService.getById(outAccountId);
        entity.setOutAccountId(outAccountId);
        entity.setOutAccountName(account.getName());
        entity.setUpdateUserId(user.getId());
        borrowMapper.update(entity);
        return entity;
    }

}