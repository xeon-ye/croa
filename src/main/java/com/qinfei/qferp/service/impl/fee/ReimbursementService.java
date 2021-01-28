package com.qinfei.qferp.service.impl.fee;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.fee.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.fee.BorrowRepayMapper;
import com.qinfei.qferp.mapper.fee.ReimbursementBorrowMapper;
import com.qinfei.qferp.mapper.fee.ReimbursementMapper;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import com.qinfei.qferp.mapper.inventory.PurchaseMapper;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.fee.IBorrowService;
import com.qinfei.qferp.service.fee.IReimbursementService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class ReimbursementService implements IReimbursementService {

    @Autowired
    private ReimbursementMapper reimbursementMapper ;
    @Autowired
    private ItemsService itemsService ;
    @Autowired
    private IProcessService processService ;
    @Autowired
    private IUserService userService ;
    @Autowired
    private IBorrowService borrowService;
    @Autowired
    private BorrowRepayMapper borrowRepayMapper ;
    @Autowired
    private IAccountService accountService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private ReimbursementBorrowMapper reimbursementBorrowMapper ;
    @Autowired
    private Config config;
    @Autowired
    private FlowableMapper flowableMapper;
    @Autowired
    private PurchaseMapper purchaseMapper;

    //报销状态
    private final static Map<Integer, String> reimburseStateMap = new HashMap<Integer, String>(){
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
            put(9,"会计确认出款");
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

    /**
     * 获取到数据集合
     * @param pageNum
     * @param pageSize
     * @param map
     * @return
     */
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
        List<Map> list = reimbursementMapper.listPg(map);
//        Integer userId = user.getId();
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

    /**
     * 1、处理报销主表：往主表添加数据，发起流程
     * 2、往子表添加数据
     *
     * @param entity
     * @return
     */
    @Override
    @Transactional
    public Reimbursement add(Reimbursement entity,
                             List<String> costType,
                             List<String> purpose,
                             List<Double> money,
                             List<Integer> numberOfDocument,
                             List<Double> currentTotalPrice) {
//        step1:处理报销表信息
        entity.setCode(IConst.REIMBURSEMENT_CODE+ DateUtils.format(new Date(),"yyyyMMdd") + CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.REIMBURSEMENT_CODE),5));
        entity.setType(IConst.REIMBURSEMENT_CODE);
        User user = AppUtil.getUser();
        entity.setApplyId(user.getId());
        entity.setApplyName(user.getName());
        entity.setApplyTime(new Date());
        entity.setDeptId(user.getDeptId());
        entity.setDeptName(user.getDeptName());
        entity.setCompanyCode(user.getCompanyCode());

        reimbursementMapper.insert(entity);
        if(entity.getState() > 0){
            //step2、发起流程
            try{
                processService.addReimbursementProcess(entity,3);
            }catch (Exception e){
                throw new QinFeiException(1002,"发起审批流出错！") ;
            }
        }

//        step3:处理报销子表信息
        insertChildTable(entity,costType,purpose, money, numberOfDocument,currentTotalPrice,user) ;
        if(StringUtils.isNotEmpty(entity.getPurchaseIds())){
            //step4:采购关联报销
            Map map =new HashMap();
            String purchaseIds=entity.getPurchaseIds();
            List<String> list=Arrays.asList(purchaseIds.split(","));
            map.put("id",entity.getId());
            map.put("list",list);
            purchaseMapper.editPurchaseReimbursementId(map);
        }
        return entity;
    }

    /**
     * 1、处理报销主表：往主表添加数据，发起流程
     * 2、往子表添加数据
     * @param entity
     * @return
     */
    @Override
    @Transactional
    public Reimbursement edit(Reimbursement entity,
                              List<String> costType,
                              List<String> purpose,
                              List<Double> money,
                              List<Integer> numberOfDocument,
                              List<Double> currentTotalPrice) {
//        step1：处理主表信息
        //如果是提交，要发起流程和完成待办
        User user = AppUtil.getUser();
        entity.setUpdateUserId(user.getId());
        reimbursementMapper.update(entity);
        if(entity.getState()==IConst.STATE_BZ){
            //发起流程
            processService.addReimbursementProcess(entity, 3);
            //处理待办
            finishItem(entity) ;
        }

//        step2:处理报销子表信息
        reimbursementMapper.delReimbursement_dByRemId(entity.getId());
        insertChildTable(entity,costType,purpose, money, numberOfDocument,currentTotalPrice,user) ;
        return entity;
    }

    private void insertChildTable(Reimbursement entity,
                                  List<String> costType,
                                  List<String> purpose,
                                  List<Double> money,
                                  List<Integer> numberOfDocument,
                                  List<Double> currentTotalPrice,
                                  User user){
        if(costType!=null && costType.size()>0){
            int size = costType.size() ;
            List<Reimbursement_d> list = new ArrayList<>() ;
            for(int i=0;i<size;i++){
                Reimbursement_d reimbursement_d = new Reimbursement_d();
                reimbursement_d.setRemId(entity.getId());
                reimbursement_d.setCostType(costType.get(i));
                reimbursement_d.setPurpose(purpose.get(i));
                reimbursement_d.setMoney(money.get(i));
                reimbursement_d.setNumberOfDocument(numberOfDocument.get(i));
                reimbursement_d.setCurrentTotalPrice(currentTotalPrice.get(i));
                reimbursement_d.setUpdateUserId(user.getId());
                list.add(reimbursement_d) ;
            }
            //进行从表数据的录入
            reimbursementMapper.saveBatch(list);
        }
    }

    /**
     * 通过id查询主表与从表
     * @param id
     * @return
     */
    @Override
    public Reimbursement getById(Integer id) {
        //先查询主表数据，将其保存在实体类中
        Reimbursement reimbursement = reimbursementMapper.getById(id);
        if(reimbursement==null){
            throw new QinFeiException(1002,"该报销流程已删除！");
        }
        //查询从表数据，将其用集合保存
        List<Reimbursement_d> reimbursement_ds = reimbursementMapper.getReimbursement_dsById(id);

        //将查询到的从表数据插入到entity类中，方便更新方法拿到从表数据
        //使实体类属性完善
        reimbursement.setReimbursementDs(reimbursement_ds);
        return reimbursement;
    }

    /**
     * state=2时财务撤回，此时，稿件状态和借款状态还未变更
     * 1、完成待办
     * 2、增加新的待办
     * 3、请款订单状态修改为-1
     *
     * @param entity
     */
    @Override
    @Transactional
    public void CWReject(Reimbursement entity) {
        User user = AppUtil.getUser();
        try {
            String taskId = entity.getTaskId();
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckC", true);
            entity.setState(IConst.STATE_REJECT);
            entity.setUpdateUserId(user.getId());
            //1、待办变已办
            finishItem(entity);

            //2、增加待办
            //流程监听里会发驳回待办所以不需要额外增加待办的处理。
           // Items item = addApplyItem(entity, user);
          //entity.setItemId(item.getId());
        } catch (Exception e) {
            throw new QinFeiException(1002, "审批流异常，报销id=" + entity.getId());
        }
        //3、请款订单状态修改
        try
        {
            reimbursementMapper.update(entity);
        }catch(Exception e) {
            throw new QinFeiException(1002, "更新报销表信息失败！报销id=" + entity.getId());
        }
    }


    public Boolean CWReturn(Reimbursement entity){
        User user= AppUtil.getUser();
        try{
            reimbursementMapper.returnRefundInfo(entity.getId(),user.getId());
        }catch (Exception e){
            throw new QinFeiException(1002,"还原报销表信息失败！报销id="+entity.getId());

        }
        return true ;
    }

    @Override
    public String downloadData(Map<String, Object> param) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(param.get("auditTable") != null){
                List<Map> auditTable = JSON.parseArray(String.valueOf(param.get("auditTable")), Map.class);
                param.put("auditTable", auditTable);
            }
            if(param.get("detailTable") != null){
                List<Map> detailTable = JSON.parseArray(String.valueOf(param.get("detailTable")), Map.class);
                param.put("detailTable", detailTable);
            }
            return DataImportUtil.createReimburseFile("报销信息", config.getUploadDir(), config.getWebDir(), Arrays.asList(param));
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "下载报销信息异常！");
        }
    }

    @Override
    public String batchDownloadData(Map<String, Object> param) {
        try{
            List<Map> list = listReimburseData(param);
            return DataImportUtil.createReimburseFile("报销信息", config.getUploadDir(), config.getWebDir(), list);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "批量下载报销信息异常！");
        }
    }

    @Override
    public List<Map> listReimburseData(Map<String, Object> param) {
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
            List<Map> list = reimbursementMapper.listPg(param);
            //获取报销详情 和 审核列表
            if(!CollectionUtils.isEmpty(list)){
                List<Map<String, Object>> paramList = new ArrayList<>();
                List<Integer> ids = new ArrayList<>();
                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                list.forEach(o -> {
                    //设置查询审核详情条件
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("dataId", o.get("id"));
                    tmp.put("process", 12);
                    paramList.add(tmp);
                    ids.add(Integer.parseInt(String.valueOf(o.get("id"))));
                    //设置中文值
                    o.put("modalTitle", String.format("【报销】%s-%s-%s", String.valueOf(o.get("code")),String.valueOf(o.get("applyName")),String.valueOf(o.get("applyTime"))));
                    String state = reimburseStateMap.get(Integer.parseInt(String.valueOf(o.get("state"))));
                    o.put("state", state);

                    //设置实际出款金额
                    o.put("payAmount", decimalFormat.format(MapUtils.getDoubleValue(o, "totalMoney") - MapUtils.getDoubleValue(o, "unpaidLoan")));
                });
                //报销详情
                List<Reimbursement_d> reimbursementDList = reimbursementMapper.listReimbursementDetailByIds(ids);
                if(!CollectionUtils.isEmpty(reimbursementDList)){
                    for(Map reimburse : list){
                        String dataId = String.valueOf(reimburse.get("id"));
                        List<Reimbursement_d> tmpList = new ArrayList<>();
                        for(Reimbursement_d reimbursementD : reimbursementDList){
                            if(dataId.equals(String.valueOf(reimbursementD.getRemId()))){
                                tmpList.add(reimbursementD);
                            }
                        }
                        reimburse.put("detailTable", tmpList);
                    }
                }

                //抵充详情
                List<Map<String, Object>> borrowList = reimbursementBorrowMapper.listBorrowByIds(ids);
                if(!CollectionUtils.isEmpty(borrowList)){
                    for(Map reimburse : list){
                        String dataId = String.valueOf(reimburse.get("id"));
                        List<Map<String, Object>> tmpList = new ArrayList<>();
                        for(Map<String, Object> borrow : borrowList){
                            if(dataId.equals(String.valueOf(borrow.get("rem_id")))){
                                if(borrow.get("type") != null){
                                    borrow.put("type", borrowTypeMap.get(Integer.parseInt(String.valueOf(borrow.get("type")))));
                                }
                                tmpList.add(borrow);
                            }
                        }
                        reimburse.put("borrowTable", tmpList);
                    }
                }

                //审核列表
                List<Map<String, Object>> taskList =  flowableMapper.listHistoryTask(paramList);
                if(!CollectionUtils.isEmpty(taskList)){
                    for(Map reimburse : list){
                        String dataId = String.valueOf(reimburse.get("id"));
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
                        reimburse.put("auditTable", tmpList);
                    }
                }
            }
            return list;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "批量打印报销信息异常！");
        }
    }

    //增加待办
    private Items addApplyItem(Reimbursement entity, User user) {
        Items items = new Items();
        items.setItemName(entity.getTitle() + "-报销撤回等待处理");
        items.setItemContent("您有新的报销撤回需要处理");
        items.setWorkType("报销撤回");
        User mediaUser = userService.getById(entity.getApplyId());
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
//        items.setTransactionAddress("/fee/expenseReimbursement?flag=-2&id=" + entity.getId());
        items.setFinishAddress("/fee/expenseReimbursement?flag=1&id=" + entity.getId());
        items.setAcceptWorker(mediaUser.getId());
        items.setAcceptDept(mediaUser.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items;
    }


    private void sendMessage(Reimbursement entity,String tips,String url) {
        User user = AppUtil.getUser();
        String subject = "[费用报销]";
        // 推送WebSocket消息(右侧弹框消息)；
        WSMessage message = new WSMessage();
        message.setReceiveUserId(entity.getApplyId() + "");
        message.setReceiveName(entity.getApplyName());
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
        mes.setType(5);//费用报销
        mes.setContent(tips);
        mes.setInitiatorWorker(user.getId());
        mes.setInitiatorDept(user.getDeptId());
        mes.setAcceptWorker(entity.getApplyId());
        mes.setAcceptDept(entity.getDeptId());
        mes.setUrl(url);
        mes.setUrlName("费用报销");
        messageService.addMessage(mes);
    }

    /**
     * 通过id删除报销（更改状态值，进行备份，并不是真正删除）
     * @param entity
     */
    @Override
    @Transactional
    public void delById(Reimbursement entity) {
        User user = AppUtil.getUser() ;
        //处理借款表
        if (entity.getUnpaidLoan()>0) {
            List<Borrow> borrows = reimbursementBorrowMapper.queryBorrowByRemId(entity.getId());
            for(int i=0;i<borrows.size();i++){
                Borrow borrow = borrows.get(i);
                Integer borrowId = borrow.getId();
                borrowService.backBorrowInfo(borrow,entity.getUnpaidLoan());
                //删除还款表
                borrowRepayMapper.deleteByRepayIdAndTypeAndState(entity.getId(), IConst.REPAY_TYPE_REIM,IConst.REPAY_STATE_SAVE);
                reimbursementBorrowMapper.deleteByRemId(entity.getId());
            }
        }
        try {
            //删除借款关系表
            reimbursementBorrowMapper.deleteByRemId(entity.getId());
        } catch (Exception e) {
            throw new QinFeiException(1002, "删除借款关系表失败，报销id=" + entity.getId());
        }
        //处理待办
        finishItem(entity) ;
        //先删从表
        reimbursementMapper.delReimbursement_dByRemId(entity.getId()) ;
        //再删主表
        entity.setState(IConst.STATE_DELETE);
        entity.setUpdateUserId(user.getId());
        entity.setReimbursementDs(null);
        reimbursementMapper.update(entity);
        //进销存功能上线后取消注释
//        purchaseMapper.editReimbursementId(entity.getId());
    }

    @Override
    @Transactional
    public List<Map> saveBorrowInfo(Map map){
        Integer id = MapUtils.getInteger(map,"id");
        String borrowIds = MapUtils.getString(map,"borrowIds");
        Reimbursement entity = reimbursementMapper.getById(id) ;
        Double resultAmount = 0D ;
        if (borrowIds.indexOf(",") > -1) {
            String[] ids = borrowIds.split(",") ;
            for(int i=0;i<ids.length;i++){
                Integer borrowId ;
                try{
                    borrowId = Integer.parseInt(ids[i]);
                }catch (Exception e){
                    throw new QinFeiException(1002,"数组转换异常，借款编号应为数字，实际获取为："+ids[i]);
                }
                ReimbursementBorrow rb = new ReimbursementBorrow() ;
                rb.setRemId(entity.getId());
                rb.setBorrowId(borrowId);
                String fundAmount = (String)map.get("fund_"+ids[i]) ;
                Double amount ;
                try{
                    amount= StringUtils.isNotEmpty(fundAmount)?Double.parseDouble(fundAmount):0 ;
                }catch (Exception e){
                    throw new QinFeiException(1002,"获取的金额不正确，应为数字，实际为："+fundAmount) ;
                }
                resultAmount += amount ;
                if(amount>0){
//              1、处理借款表
                    Borrow borrow = borrowService.getById(borrowId) ;
                    if(amount>borrow.getRemainAmount()){
                        throw new QinFeiException(1002,"冲抵金额不能超过未还金额！");
                    }
                    borrowService.dealBorrowInfo(borrow,amount) ;
//              2、处理还款表
                    insertBorrowRepay(entity,borrowId,amount) ;
//              3、把借款信息存入关系表
                    rb.setAmount(amount);
                    reimbursementBorrowMapper.insert(rb) ;
                }else{
                    throw new QinFeiException(1002,"冲抵金额必须大于0");
                }
            }
        }else{
            Integer borrowId ;
            try {
                borrowId = Integer.parseInt(borrowIds);
            }catch (Exception e){
                throw new QinFeiException(1002,"数组转换异常，借款编号应为数字，实际获取为："+borrowIds);
            }
            ReimbursementBorrow rb = new ReimbursementBorrow() ;
            rb.setRemId(entity.getId());
            rb.setBorrowId(borrowId);
            String fundAmount = MapUtils.getString(map,"fund_"+borrowIds) ;
            Double amount ;
            try{
                amount= StringUtils.isNotEmpty(fundAmount)?Double.parseDouble(fundAmount):0 ;
            }catch (Exception e){
                throw new QinFeiException(1002,"获取的金额不正确，应为数字，实际为："+fundAmount) ;
            }
            resultAmount += amount ;
            if(amount>0){
//        1、处理借款表
                Borrow borrow = borrowService.getById(borrowId) ;
                if(amount>borrow.getRemainAmount()){
                    throw new QinFeiException(1002,"冲抵金额不能超过未还金额！");
                }
                borrowService.dealBorrowInfo(borrow,amount) ;
//        2、处理还款表
                insertBorrowRepay(entity,borrowId,amount) ;
//        3、把借款信息存入关系表
                rb.setAmount(amount);
                reimbursementBorrowMapper.insert(rb) ;
            }else{
                throw new QinFeiException(1002,"冲抵金额必须大于0");
            }
        }

        if(resultAmount>entity.getTotalMoney()){
            throw new QinFeiException(1002,"抵消金额不能大于实报销金额！");
        }
        entity.setUnpaidLoan(resultAmount);
        entity.setUpdateUserId(AppUtil.getUser().getId());
        reimbursementMapper.update(entity);
        List<Map> list = reimbursementBorrowMapper.queryBorrowMapByRemId(id);
        return list;
    }

    @Override
    public List<Map> queryBorrowMapByRemId(Integer remId){
        return reimbursementBorrowMapper.queryBorrowMapByRemId(remId);
    }

    /**
     * 向还款表插入数据
     */
    private void insertBorrowRepay(Reimbursement entity,Integer borrowId,Double fundAmount){
        User user = AppUtil.getUser() ;
        BorrowRepay repay = new BorrowRepay() ;
        repay.setBorrowId(borrowId);
        repay.setRepayId(entity.getId());
        repay.setRepayCode(entity.getCode());
        repay.setType(IConst.REPAY_TYPE_REIM);
        repay.setAmount(fundAmount);
        repay.setState(IConst.REPAY_STATE_SAVE);
        repay.setCreateTime(new Date());
        repay.setCreateUserId(user.getId());
        repay.setCreateName(user.getName());
        borrowRepayMapper.insert(repay) ;
    }

    @Override
    @Transactional
    public void cleanBorrowInfo(Integer id) {
        Reimbursement entity = this.getById(id) ;
        if(entity.getUnpaidLoan()>0){
            //1、还原还款
            borrowRepayMapper.deleteByRepayIdAndType(entity.getId(),IConst.REPAY_TYPE_REIM);
            List<Borrow> borrows = reimbursementBorrowMapper.queryBorrowByRemId(entity.getId());
            for(Borrow borrow:borrows){
                //2、还原借款
                borrowService.backBorrowInfo(borrow,entity.getUnpaidLoan()) ;
            }
            //3、删除报销和借款关系表
            reimbursementBorrowMapper.deleteByRemId(id);
            //3、还原报销关系字段
            reimbursementMapper.initBorrowColumns(id);

        }
    }

    /**
     * 处理待办
     * @param entity
     */
    private void finishItem(Reimbursement entity){
        if(entity.getItemId()!=null){
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }

    /**
     * 更新报销申请的审批状态
     * @param dataId,state,loginUserId,taskId,itemId
     */
    @Transactional
    @Override
    public void processReimbursement(String dataId,Integer state,Integer loginUserId,String taskId,Integer itemId,Integer acceptWorker) {
        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setId(Integer.parseInt(dataId));
        reimbursement.setState(state);
        //获取审批人的id
        //获取报销流程记录
        Reimbursement ment = reimbursementMapper.getById(Integer.valueOf(dataId));
        StringBuffer userId = new StringBuffer(ment.getApproverId()==null?"":ment.getApproverId());
        userId.append(","+acceptWorker);
        reimbursement.setApproverId(userId.toString());
        reimbursement.setUpdateUserId(loginUserId);
        // 更新流程当前的任务ID；
        reimbursement.setTaskId(taskId);
        // 更新待办事项的ID；
        reimbursement.setItemId(itemId);
        // 根据流程状态来更新报销的状态
        reimbursementMapper.update(reimbursement);
    }

    @Override
    @Transactional
    public void confirm(Reimbursement entity, Map map) {
        User user = AppUtil.getUser() ;
        Double payAmount ;
        try{
            payAmount = Double.parseDouble(map.get("payAmount").toString()) ;
        }catch (Exception e){
            throw new QinFeiException(1002,"获取的付款金额不正确：获取到的出账金额="+map.get("payAmount").toString()) ;
        }
        Integer outAccountId = 0 ;
        try{
            outAccountId = Integer.parseInt((String)map.get("outAccountId")) ;
        }catch (Exception e){
            throw new QinFeiException(1002,"获取的出款账户不正确：获取到的出账账户id="+map.get("outAccountId").toString()) ;
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

        Account account = accountService.getById(outAccountId) ;
        entity.setOutAccountId(account.getId());
        entity.setOutAccountName(account.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date time1 = null;
        try {
            time1  = sdf.parse("2020-7-6");
        }catch (Exception e){
            e.printStackTrace();
        }
        if(IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType()) && entity.getApplyTime().before(time1)){
            entity.setState(IConst.STATE_KJ);//财务复核
//            Items item = addItem(entity,AppUtil.getUser()) ;
//            if(item!=null){
//                entity.setItemId(item.getId());
//            }

        }else{
            finishItem(entity);
            entity.setState(IConst.STATE_FINISH);//1通过
            sendMessage(entity,String.format("[%s]您提交的费用报销[%s]审核已完成。","费用报销",entity.getTitle()),"/fee/expenseReimbursement?flag=-1&id="+entity.getId());
        }

        entity.setPayAmount(payAmount);
        entity.setPayUserId(user.getId());
        entity.setUpdateUserId(user.getId());
        //将reimbursementDs置为空
        entity.setReimbursementDs(null);

        try{
            if (entity.getUnpaidLoan()>0) {
                List<Map> borrows = reimbursementBorrowMapper.queryBorrowMapByRemId(entity.getId());
                for(int i=0;i<borrows.size();i++){
                    Map borrow = borrows.get(i);
                    Integer borrowId = MapUtils.getInteger(borrow,"id");
                    //处理还款关系表
                    BorrowRepay borrowRepay = borrowRepayMapper.getByRepayIdAndBorrowIdAndTypeAndState(entity.getId(),borrowId,IConst.REPAY_TYPE_REIM,IConst.REPAY_STATE_SAVE) ;
                    borrowRepay.setState(IConst.REPAY_STATE_FINISH);
                    borrowRepay.setUpdateTime(new Date());
                    borrowRepay.setUpdateUserId(AppUtil.getUser().getId());
                    borrowRepayMapper.update(borrowRepay);
                }
            }
        }catch (Exception e){
            throw new QinFeiException(1002,"处理冲抵借款信息失败，报销id="+entity.getId()) ;
        }

        //创建事务回滚点，就算后面的业务有异常，前面的业务也要执行
//        Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();

        try{
            String taskId = entity.getTaskId();
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckC", IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType()));
            String[] taskIds = new String[]{entity.getTaskId()};
            String desc = map.get("desc").toString();
            processService.approveProcess(taskIds, desc, true);
        }catch (Exception e){
//            e.printStackTrace();
            throw new QinFeiException(1002,"审批流异常,报销id="+entity.getId()) ;
            //回滚事务
//            TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
        }

        try{
            reimbursementMapper.updateaccount(entity);
        }catch (Exception e){
            throw new QinFeiException(1002,"更新出款账户失败！报销id="+entity.getId());
        }
    }

    @Override
    @Transactional
    public void checkBtoB(Reimbursement entity) {
        //将reimbursementDs置为空
        //entity.setReimbursementDs(null);
        entity.setState(IConst.STATE_FINISH);//1通过
        //entity.setReimbursementDs(null);
        entity.setUpdateUserId(AppUtil.getUser().getId());
        entity.setPayTime(new Date());
        String[] taskIds = new String[]{entity.getTaskId()};
        try{
            String result = processService.approveProcess(taskIds, null, true);
            if(!"操作成功。".equals(result)){
                throw new QinFeiException(1002, result);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e) {
            throw new QinFeiException(1002, "审批流异常，报销id=" + entity.getId());
        }
        try{
            reimbursementMapper.updatePayTime(entity);
        }catch (Exception e) {
            throw new QinFeiException(1002, "更新时间异常，报销id=" + entity.getId());
        }
//        reimbursementMapper.update(entity);
//
//        //处理待办
//        finishItem(entity) ;
    }

    @Override
    public Reimbursement update(Reimbursement entity){
        reimbursementMapper.update(entity);
        return entity;
    }

    /**
     * 报销导出
     * @param map
     * @param outputStream
     * @return
     */
    @Override
    public List<Map> export(Map map, OutputStream outputStream) {
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = reimbursementMapper.listPg(map);
        String [] titles = {"报销编号","报销标题","报销人","报销部门","报销时间","收款单位","支付开户行","摘要","实报销金额","报销状态","出账账户名称","实际出款金额","出款时间"};
        String [] obj = {"code","title","applyName","deptName","applyTime","accountName","accountBankName","remark","totalMoney","state","outAccountName","payAmount","payTime"};
        ExcelUtil.exportExcel("报销导出",titles,obj,list,outputStream,"yyyy-MM-dd",(sheet, rowIndex, cellIndex, row, cell, field, value)->{
            if(value!=null){
                if("applyTime".equals(field) || "payTime".equals(field)){
                    cell.setCellValue(value.toString());
                }else if("state".equals(field)){
                    if((int) value==-1){
                        cell.setCellValue("审核驳回");
                    }else if((int) value ==0){
                        cell.setCellValue("已保存");
                    }else if((int) value  ==1){
                        cell.setCellValue("已完成");
                    }else if((int) value ==2){
                        cell.setCellValue("审核通过");
                    }else if((int) value ==3){
                        cell.setCellValue("组长审核");
                    }else if((int) value ==4){
                        cell.setCellValue("部长审核");
                    }else if((int) value ==5){
                        cell.setCellValue("总监审核");
                    }else if((int) value ==6){
                        cell.setCellValue("财务总监审核");
                    }else if((int) value ==7){
                        cell.setCellValue("副总经理审核");
                    }else if((int) value ==8){
                        cell.setCellValue("总经理审核");
                    }else if((int) value ==9){
                        cell.setCellValue("会计确认出款");
                    }else if((int) value ==10){
                        cell.setCellValue("业务员确认");
                    }else if((int) value ==12){
                        cell.setCellValue("财务部长审核");
                    }else if((int) value ==16){
                        cell.setCellValue("出纳出款");
                    }
                }else if("totalMoney".equals(field) || "payAmount".equals(field)){
                        cell.setCellValue(Double.parseDouble(value.toString()));
                }else{
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    public ResponseData getByAdmId(Integer admId) {
        ResponseData data = ResponseData.ok();
        Reimbursement reimbursement =reimbursementMapper.getByAdmId(admId);
        data.putDataValue("entity",reimbursement);
        return data;
    }

    //出款后给财务负责人增加待办
    private Items addItem(Reimbursement entity, User user){
        Items items = new Items();
        items.setItemName(entity.getTitle()+"-报销出账确认等待处理");
        items.setItemContent("您有新的报销出账确认需要处理");
        items.setWorkType("出账确认申请");
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/expenseReimbursement?flag=3&id="+entity.getId());
        items.setFinishAddress("/fee/expenseReimbursement?flag=-1&id="+entity.getId());
        User cw = userService.getAccountingInfo(user.getCompanyCode()) ;
        items.setAcceptWorker(cw.getId()) ;
        items.setAcceptDept(cw.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }


    @Override
    public Map reimburseSum(Map map){
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        Map<String,Object> resultMap = reimbursementMapper.reimburseSum(map);
        return  initResult(resultMap);
    }

    private Map initResult(Map resultMap) {
        if (resultMap == null || resultMap.size() == 0) {
            resultMap = new HashMap<>();
            resultMap.put("totalMoney", 0);
            resultMap.put("payAmount", 0);

        }
        return resultMap;
    }

    @Override
    public Reimbursement changeAccount (Reimbursement entity,Map<Object,String> map){
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
        reimbursementMapper.update(entity);
        return entity;
    }
}
