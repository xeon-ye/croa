package com.qinfei.qferp.service.impl.fee;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.config.Config;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.fee.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.entity.workup.WorkupRequestParam;
import com.qinfei.qferp.mapper.biz.ArticleMapperXML;
import com.qinfei.qferp.mapper.fee.BorrowMapper;
import com.qinfei.qferp.mapper.fee.BorrowRepayMapper;
import com.qinfei.qferp.mapper.fee.OutgoBorrowMapper;
import com.qinfei.qferp.mapper.fee.OutgoMapper;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.fee.IOutgoService;
import com.qinfei.qferp.service.impl.flow.ProcessService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

@Service
public class OutgoService implements IOutgoService {
    @Autowired
    private OutgoMapper outgoMapper;
    @Autowired
    private ArticleMapperXML articleMapperXML;
    @Autowired
    private OutgoBorrowMapper outgoBorrowMapper;
    @Autowired
    private ItemsService itemsService;
    @Autowired
    private ProcessService processService;
    @Autowired
    private BorrowMapper borrowMapper;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private UserService userService;
    @Autowired
    private BorrowRepayMapper borrowRepayMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private IAccountService accountService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private Config config;
    @Autowired
    private FlowableMapper flowableMapper;

    //请款状态
    private final static Map<Integer, String> outgoStateMap = new HashMap<Integer, String>(){
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
            put(23,"唤醒中");
            put(26,"媒介回填开票信息");
        }
    };
    //请款是否开票
    private final static Map<Integer, String> invoiceFlagMap = new HashMap<Integer, String>(){
        {
            put(1,"是");
            put(2,"否");
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
        if (!ObjectUtils.isEmpty(map.get("deptId"))) {//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = outgoMapper.listPg(map);
        for (Map data : list) {
            if(data.get("phone") != null && !org.springframework.util.StringUtils.isEmpty(String.valueOf(data.get("phone")))){
                String phone = EncryptUtils.decrypt(String.valueOf(data.get("phone")));
                data.put("phone", phone);
            }
        }
        return new PageInfo<>(list);
    }

    @Override
    public Outgo getById(Integer id) {
        return outgoMapper.getById(id);
    }

    @Override
    public Outgo add(Outgo entity) {
        outgoMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public Outgo edit(Outgo entity) {
        User user = AppUtil.getUser();
//        entity.setState(IConst.STATE_BZ);
        entity.setUpdateUserId(user.getId());
        //待办变已办
        finishItem(entity);

        try {
            //把请款总金额同步到稿件中冗余字段去
            List<Integer> ids = outgoMapper.queryArticleIdsByOutgoId(entity.getId());
            if (ids != null && ids.size() > 0) {
                Map<String, Object> map = new HashMap();
                //批量同步稿件请款状态，请款冗余字段
                map.put("outgoTotalAmount", entity.getApplyAmount());
                map.put("list", ids);
                outgoMapper.updateArticleOutgoTotalAmountBatch(map);
            }
        } catch (QinFeiException e) {
            throw new QinFeiException(1002, "请款总金额同步到稿件失败，请款id=【" + entity.getId() + "】");
        }
        try {
            //审核时需要走审批流
            if (entity.getState() == IConst.STATE_SAVE) {
                //请款暂存
                outgoMapper.update(entity);
            } else {
                //请款暂存
                outgoMapper.update(entity);
                //判断媒介是否新媒体媒介
                //  boolean isNewMedia = com.alibaba.druid.util.StringUtils.equals(IConst.DEPT_TYPE_XMT, userService.getMJType(AppUtil.getUser().getId()));

                if (entity.getMediaGroupLeader() != null){
                    //获取下一个审批人
                    Integer mediaGroupLeader = entity.getMediaGroupLeader();
                    //根据传来的审批人ID 获得姓名 部门
                    User userMedia = userMapper.getById(mediaGroupLeader);
                    entity.setMediaGroupLeaderDept(userMedia.getDeptId());
                    entity.setMediaGroupLeaderName(userMedia.getName());

                }

                //判断板块是否为新媒体 或 网络
                Integer mediaType = outgoMapper.selectMediaType(entity.getMediaTypeId());
                    try {
                        List<String> sc= SysConfigUtils.getConfigValue("process",List.class);
                        if (CollectionUtils.isNotEmpty(sc)){
                        Boolean mediaflag = false;
                        Boolean businessflag = false;
                        Boolean configurationProcess= false;
                            if (sc.contains(user.getCompanyCode())){
                                mediaflag=true;
                            }if (sc.contains(entity.getCompanyCode())){
                                businessflag=true;
                                configurationProcess=true;
                            }

                            if (mediaflag){
                                if (businessflag){
                                    //河图媒介，河图业务员
                                    processService.addHtRefundProcess(entity , 3);
                                }else  {
                                    //河图媒介，非河图业务员
                                    processService.addProcess(entity,3,mediaType,false);
                                }
                            } else {
                                //medaType =1  网络  2 新媒体
                                if (mediaType == 1){
                                    // 紧急程度字段暂不启用
                                    // taskId为空：首次提交审批；不为空：驳回后提交审批
                                    //如果板块是自媒体，则调用自媒体请款审批
                                    List<String> plateCode= SysConfigUtils.getConfigValue("plateCode",List.class);
                                    if (CollectionUtils.isNotEmpty(plateCode) && plateCode.contains(entity.getMediaTypeId().toString())){
//                                    if (entity.getMediaTypeId() == 8 || entity.getMediaTypeId() == 317) {
                                        processService.addSelfMediaRefundProcess(entity, 3,mediaType,configurationProcess);
                                    }else {
                                        processService.networkOutgo(entity,3,mediaType,configurationProcess);
                                    }
                                }else {
                                    //報紙板块请款流程
                                    if (entity.getMediaTypeId() == 3){
                                        processService.newspaperOutgo(entity,3,mediaType,configurationProcess);
                                    }else{
                                        //新媒体请款
                                        processService.newMediaOutgo(entity,3,mediaType,configurationProcess);
                                    }

                                }
                            }
                        }
                            else {
                                //medaType =1  网络  2 新媒体
                                if (mediaType == 1){
                                    // 紧急程度字段暂不启用
                                    // taskId为空：首次提交审批；不为空：驳回后提交审批
                                    //如果板块是自媒体，则调用自媒体请款审批
                                    List<String> plateCode= SysConfigUtils.getConfigValue("plateCode",List.class);
                                    if (CollectionUtils.isNotEmpty(plateCode) && plateCode.contains(entity.getMediaTypeId().toString())){
                                        processService.addSelfMediaRefundProcess(entity, 3,mediaType,false);
                                    }else {
                                        processService.networkOutgo(entity,3,mediaType,false);
                                    }
                                }else {
                                    //報紙板块请款流程
                                    if (entity.getMediaTypeId() == 3){
                                        processService.newspaperOutgo(entity,3,mediaType,false);
                                    }else{
                                        //新媒体请款
                                        processService.newMediaOutgo(entity,3,mediaType,false);
                                    }

                                }
                            }
                    }catch (QinFeiException e) {
                        throw new QinFeiException(1002, "请款审批流启动失败，请款id=【" + entity.getId() + "】");
                    }
            }
        } catch (QinFeiException e) {
            throw new QinFeiException(1002, "请款审批流启动失败，请款id=【" + entity.getId() + "】");
        }
        return entity;
    }
    @Override
    @Transactional
    public void workupOutgo(Outgo entity, WorkupRequestParam workupRequestParam) {
        try {
            //判断当前请款稿件列表是否全部删除了，全部删除了不能唤醒
            List<Integer> artList = outgoMapper.queryArticleIdsByOutgoId(entity.getId());
            if(CollectionUtils.isEmpty(artList)){
                throw new QinFeiException(1002,"很抱歉，不能唤醒，请款稿件列表无数据或者所有稿件被删除！");
            }
            User user = AppUtil.getUser();
            Outgo outgo = new Outgo();
            outgo.setId(entity.getId());
            outgo.setUpdateUserId(user.getId());
            outgo.setEditJson(entity.getEditJson());
            outgoMapper.update(outgo);
            processService.workupProcess(workupRequestParam.getWorkupId(), workupRequestParam.getWorkupName(), workupRequestParam.getProcessType(),
                    workupRequestParam.getProcessName(), workupRequestParam.getTaskDefKey(), workupRequestParam.getWorkupTaskId(),
                    workupRequestParam.getGatewayFlag(), entity.getCompanyCode());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "唤醒请款流程失败！");
        }
    }

    @Override
    public Outgo update(Outgo entity) {
        outgoMapper.update(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delById(Outgo entity) {
        User user = AppUtil.getUser();
        List<Integer> ids = outgoMapper.queryArticleIdsByOutgoId(entity.getId());
        if (ids != null && ids.size() > 0) {
            try {
                List<Article> articles = new ArrayList<>();
                for (Integer item : ids) {
                    Article article = new Article();
                    article.setId(item);
                    article.setOutgoId(null);
                    article.setOutgoCode(null);
                    article.setOutgoStates(IConst.FEE_STATE_SAVE);
                    article.setOutgoTotalAmount(null);
                    articles.add(article);
                }
                //批量同步稿件请款状态，请款冗余字段
                outgoMapper.updateArticleOutgoInfoBatch(articles);
            } catch (Exception e) {
                throw new QinFeiException(1002, "处理请款关联的稿件中请款冗余字段失败，请款id=" + entity.getId());
            }
        }
        try {
            //删除稿件关系表
            outgoMapper.delOutgoArticle(entity.getId());
        } catch (Exception e) {
            throw new QinFeiException(1002, "处理请款和稿件关联表失败，请款id=" + entity.getId());
        }
        try {
            //处理借款表
            List<OutgoBorrow> borrowList = outgoBorrowMapper.queryBorrowById(entity.getId());
            if (borrowList != null && borrowList.size() > 0) {
                for (OutgoBorrow ob : borrowList) {
                    Borrow borrow = borrowMapper.getById(ob.getBorrowId());
                    borrowService.backBorrowInfo(borrow, ob.getAmount());
                }
            }
        } catch (Exception e) {
            throw new QinFeiException(1002, "处理请款关联借款表失败，请款id=" + entity.getId());
        }

        try {
            //删除借款关系表
            outgoBorrowMapper.deleteByOutgoId(entity.getId());
        } catch (Exception e) {
            throw new QinFeiException(1002, "删除借款关系表失败，请款id=" + entity.getId());
        }

        try {
            //删除还款表
            borrowRepayMapper.deleteByRepayIdAndType(entity.getId(), IConst.REPAY_TYPE_OUTGO);
        } catch (Exception e) {
            throw new QinFeiException(1002, "删除还款表失败，请款id=" + entity.getId());
        }

        try {
            //删除请款
            entity.setState(IConst.STATE_DELETE);
            entity.setUpdateUserId(user.getId());
            outgoMapper.update(entity);
        } catch (Exception e) {
            throw new QinFeiException(1002, "删除请款失败，请款id=" + entity.getId());
        }
        finishItem(entity);
    }

    public List<String> HTLScompanyCode(){
        return SysConfigUtils.getConfigValue("process",List.class);
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
            if(param.get("borrowTable") != null){
                List<Map> borrowTable = JSON.parseArray(String.valueOf(param.get("borrowTable")), Map.class);
                param.put("borrowTable", borrowTable);
            }
            return DataImportUtil.createOutgoFile("请款信息", config.getUploadDir(), config.getWebDir(), Arrays.asList(param));
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "下载请款信息异常！");
        }
    }

    @Override
    public String batchDownloadData(Map<String, Object> param) {
        try{
            List<Map> list = listOutgoData(param);
            return DataImportUtil.createOutgoFile("请款信息", config.getUploadDir(), config.getWebDir(), list);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "批量下载请款信息异常！");
        }
    }

    @Override
    public List<Map> listOutgoData(Map<String, Object> param) {
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
            List<Map> list = outgoMapper.listPg(param);
            //获取审核列表
            if(!org.springframework.util.CollectionUtils.isEmpty(list)){
                List<Map<String, Object>> paramList = new ArrayList<>();
                List<Integer> ids = new ArrayList<>();
                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                list.forEach(o -> {
                    //设置查询审核详情条件
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("dataId", o.get("id"));
                    tmp.put("process", o.get("processType"));
                    paramList.add(tmp);
                    ids.add(Integer.parseInt(String.valueOf(o.get("id"))));
                    //设置中文值
                    o.put("modalTitle", String.format("【请款】%s-%s-%s", String.valueOf(o.get("code")),String.valueOf(o.get("applyName")),String.valueOf(o.get("applyTime"))));
                    String state = outgoStateMap.get(Integer.parseInt(String.valueOf(o.get("state"))));
                    String invoiceFlagName = invoiceFlagMap.get(Integer.parseInt(String.valueOf(o.get("invoiceFlag"))));
                    o.put("invoiceFlagName", invoiceFlagName);
                    o.put("state", state);
                    //实际抹零
                    BigDecimal outgoEraseAmount = o.get("outgoEraseAmount") != null ? new BigDecimal(String.valueOf(o.get("outgoEraseAmount"))) : new BigDecimal(0);
                    BigDecimal costEraseAmount = o.get("costEraseAmount") != null ? new BigDecimal(String.valueOf(o.get("costEraseAmount"))) : new BigDecimal(0);
                    BigDecimal actualCombined = outgoEraseAmount.add(costEraseAmount).setScale(2);
                    o.put("actualCombined", actualCombined);
                    //税点
                    Float outgoTax = o.get("outgoTax") != null ? Float.parseFloat(String.valueOf(o.get("outgoTax"))) * 100 : 0;
                    o.put("outgoTax", outgoTax);

                    //设置实际出款金额
                    o.put("payAmount", decimalFormat.format(MapUtils.getDoubleValue(o, "applyAmount") - MapUtils.getDoubleValue(o, "fundAmount")));
                });

                //备用金详情
                List<Map<String, Object>> borrowList = outgoMapper.listBorrowByIds(ids);
                if(CollectionUtils.isNotEmpty(borrowList)){
                    for(Map outgo : list){
                        String dataId = String.valueOf(outgo.get("id"));
                        List<Map<String, Object>> tmpList = new ArrayList<>();
                        for(Map<String, Object> borrow : borrowList){
                            if(dataId.equals(String.valueOf(borrow.get("outgo_id")))){
                                if(borrow.get("type") != null){
                                    borrow.put("type", borrowTypeMap.get(Integer.parseInt(String.valueOf(borrow.get("type")))));
                                }
                                tmpList.add(borrow);
                            }
                        }
                        outgo.put("borrowTable", tmpList);
                    }
                }

                //应付合计
                List<Map<String, Object>> outgoSumList = outgoMapper.listSumAmountByIds(ids);
                if(CollectionUtils.isNotEmpty(outgoSumList)){
                    for(Map outgo : list){
                        String dataId = String.valueOf(outgo.get("id"));
                        for(Map<String, Object> outgoSumMap : outgoSumList){
                            if(dataId.equals(String.valueOf(outgoSumMap.get("id")))){
                                outgo.put("outgoSum", outgoSumMap.get("outgoSum"));
                            }
                        }
                    }
                }

                List<Map<String, Object>> taskList =  flowableMapper.listHistoryTask(paramList);
                if(!org.springframework.util.CollectionUtils.isEmpty(taskList)){
                    for(Map outgo : list){
                        String dataId = String.valueOf(outgo.get("id"));
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
                        outgo.put("auditTable", tmpList);
                    }
                }
            }
            return list;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "批量打印请款信息异常！");
        }
    }

    private void finishItem(Outgo entity) {
        if (entity.getItemId() != null) {
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }

    /**
     * 请款选定供应商和稿件后先存数据
     *
     * @param map
     * @return
     */
    @Override
    @Transactional
    public synchronized Outgo saveStepOne(Map map, User user) {
        Integer supplierId = Integer.parseInt((String) map.get("supplierIdSec"));
        Integer mediaTypeId = Integer.parseInt((String) map.get("media_type_id"));
        String supplierName = (String) map.get("supplierNameSec");
        String supplierContactor = (String) map.get("supplierContactorSec");
        String timeScale = (String) map.get("timeScale");
        String articleIds = "";
        if (!ObjectUtils.isEmpty(map.get("checkState"))) {
            //如果选择了全部稿件，则先按条件查询出所有稿件
            List<Map> list = outgoMapper.listPgForSelectArticle(map);
            if (list.size() > 0) {
                for (Map map1 : list) {
                    articleIds = articleIds + map1.get("id") + ",";
                }
            } else {
                throw new QinFeiException(1002, "当前条件下没有满足请款条件的稿件");
            }
        } else {
            articleIds = (String) map.get("articleIdsSec");
        }
        String companyCode = (String) map.get("companyCode");
        //稿件id放入set排重
        Set<Integer> set = new HashSet();
        try {
            if (articleIds.indexOf(",") > -1) {
                String[] ids = articleIds.split(",");
                for (int i = 0; i < ids.length; i++) {
                    set.add(Integer.parseInt(ids[i]));
                }
            } else {
                Integer id = Integer.parseInt(articleIds);
                set.add(id);
            }
        } catch (QinFeiException e) {
            throw new QinFeiException(1002, "选中的稿件【" + articleIds + "】数据不正确，请刷新后重新选择");
        }
        if (set == null || set.size() == 0) {
            throw new QinFeiException(1002, "选中的稿件【" + articleIds + "】数据不正确，请刷新后重新选择");
        }

        //先判断稿件状态，有已请款的稿件就不允许再发起请款流程了
        Integer count = articleMapperXML.listByIdsAndOutgoStates(set);
        if (count > 0) {
            throw new QinFeiException(1002, "选中的稿件中有已经请款的稿件，请刷新后重新选择！");
        }
        //新媒体选中的请款稿件大于2000，如果有稿件未完善客户，则不允许请款
        if (IConst.DEPT_TYPE_XMT.equals(user.getDept().getType())) {
            //未完善客户的稿件数量
            Integer unCompleteCount = articleMapperXML.countUnCompleteCustByIds(set);
            if (unCompleteCount > 0) {
                if (articleMapperXML.sumOutgoAmountByIds(set) > 2000) {
                    throw new QinFeiException(1002, "新媒体板块：选中的稿件有未完善客户的稿件，且请款总金额大于2000,不支持请款！请重新选择稿件！");
                }
            }
        }

        Outgo entity = new Outgo();
        entity.setSupplierId(supplierId);
        entity.setSupplierName(supplierName);
        entity.setSupplierContactor(supplierContactor);
        entity.setMediaTypeId(mediaTypeId);
        entity.setApplyId(user.getId());
        entity.setApplyName(user.getName());
        entity.setApplyTime(new Date());
        entity.setDeptId(user.getDeptId());
        entity.setDeptName(user.getDeptName());
        entity.setCreator(user.getId());
        entity.setCreateTime(new Date());
        entity.setTimeScale(timeScale);
        //自媒体请款的时候，请款的公司code为媒介的公司code
//        if (mediaTypeId == 8) {
//            entity.setCompanyCode(user.getCompanyCode());
//        } else {
        List<String> plateCode= SysConfigUtils.getConfigValue("plateCode",List.class);
        if (CollectionUtils.isNotEmpty(plateCode) && plateCode.contains(mediaTypeId.toString())){
            entity.setCompanyCode(user.getCompanyCode());
        }else {
            entity.setCompanyCode(companyCode);

        }
//        }
        try {
            //生成KP2018110001的编号，前六位是年月，后四位累加
            entity.setCode(IConst.OUTGO_CODE + CodeUtil.getMonthStr() + CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.OUTGO_CODE), 4));
        } catch (Exception e) {
            throw new QinFeiException(1002, "生成请款编号出错，请刷新后重试！");
        }

        try {
            outgoMapper.insert(entity);
        } catch (Exception e) {
            throw new QinFeiException(1002, "插入请款表失败，请刷新后重试！");
        }

        try {
            //同步稿件请款状态，请款冗余字段
            Map<String, Object> articleMap = new HashMap();
            articleMap.put("outgoId", entity.getId());
            articleMap.put("outgoCode", entity.getCode());
            articleMap.put("outgoStates", IConst.FEE_STATE_PROCESS);
            articleMap.put("outgoTotalAmount", entity.getApplyAmount());
            articleMap.put("articleIds", set);
            outgoMapper.updateArticleOutgoInfoBatchNew(articleMap);
        } catch (Exception e) {
            throw new QinFeiException(1002, "维护稿件请款字段失败，请款id=" + entity.getId());
        }

        try {
            //插入稿件关系表
            insertOutgoArticle(set, entity);
        } catch (Exception e) {
            throw new QinFeiException(1002, "维护请款和稿件关系表事变，请款id=" + entity.getId());
        }

        return entity;
    }

    //1、往关系表插入数据
    private void insertOutgoArticle(Set<Integer> set, Outgo entity) {
        try {
            List<OutgoArticle> outgoArticles = new ArrayList<>();
            Iterator<Integer> iterator = set.iterator();
            //插入关系表数据
            while (iterator.hasNext()) {
                Integer articleId = iterator.next();
                OutgoArticle outgoArticle = new OutgoArticle();
                outgoArticle.setOutgoId(entity.getId());
                outgoArticle.setArticleId(articleId);
                outgoArticles.add(outgoArticle);
            }

            //批量插入关系表
            if (outgoArticles != null && outgoArticles.size() > 0) {
                int size = outgoArticles.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    List<OutgoArticle> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(outgoArticles.get(j));
                    }
                    outgoMapper.insertOutgoArticleBatch(insertData);
                }
            }
        } catch (QinFeiException e) {
            throw new QinFeiException(1002, "插入请款和稿件关系表数据失败，请款id=【" + entity.getId() + "】，请刷新后重试！");
        }

    }

    //选中的稿件
    @Override
    public PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Integer id) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = outgoMapper.listPgForSelectedArticle(id);
        return new PageInfo<>(list);
    }

    /**
     * 查询出待请款稿件
     * 1、已完善客户未请款
     * 2、未完善客户未请款，新媒体请款金额<500,网络请款金额<5000
     *
     * @param map
     * @return
     */
    @Override
    public PageInfo<Map> listPgForSelectArticle(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = outgoMapper.listPgForSelectArticle(map);
        return new PageInfo<>(list);
    }

    @Override
    public Map listPgForSelectArticleSum(Map map) {
        return outgoMapper.listPgForSelectArticleSum(map);
    }

    @Override
    public List<Map> queryBorrowById(Integer outgoId) {
        return outgoMapper.queryBorrowById(outgoId);
    }

    /**
     * 算出当前借款订单的申请金额
     *
     * @param id
     * @return
     */
    @Override
    public Map querySumAmount(Integer id) {
        return outgoMapper.querySumAmount(id);
    }

    /**
     * 根据稿件编号查询请款id
     *
     * @param articleId
     * @return
     */
    @Override
    public Integer queryOutgoId(Integer articleId) {
        Integer outId = outgoMapper.queryOutgoId(articleId);
        return outId != null ? outId : -2;
    }

    @Override
    @Transactional
    public Double saveOutgoBorrow(Map map) {
        Double amount = 0.0;
        Integer outgoId = Integer.parseInt((String) map.get("outgoId"));
        outgoBorrowMapper.deleteByOutgoId(outgoId);
        String borrowIds = (String) map.get("borrowIds");
        if (borrowIds.indexOf(",") > -1) {
            throw new QinFeiException(1002, "不支持选择多条备用金！");
//        1、处理请款和借款表关系
//                String[] ids = borrowIds.split(",") ;
//                for(int i=0;i<ids.length;i++){
//                    OutgoBorrow ob = new OutgoBorrow() ;
//                    ob.setOutgoId(outgoId);
//                    Integer borrowId = Integer.parseInt(ids[i]) ;
//                    ob.setBorrowId(borrowId);
//                    String fundAmount = (String)map.get("fund_"+ids[i]) ;
//                    Double temp = StringUtils.isNotEmpty(fundAmount)?Double.parseDouble(fundAmount):0 ;
//                    if(temp>0){
//                        ob.setAmount(temp);
//                        amount = new BigDecimal(amount.toString()).
//                                add(new BigDecimal(temp)).
//                                setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue() ;
//                        outgoBorrowMapper.insert(ob) ;
////                2、处理还款表
//                        insertBorrowRepay(outgoId, borrowId,temp) ;
//                        //3、处理借款表
//                        Borrow borrow = borrowService.getById(borrowId) ;
//                        borrowService.dealBorrowInfo(borrow,amount) ;
//                    }else{
//                        throw new QinFeiException(1002,"备用金金额必须大于0");
//                    }
//                }
        } else {
//        1、处理请款和借款表关系
            OutgoBorrow ob = new OutgoBorrow();
            ob.setOutgoId(outgoId);
            Integer borrowId = Integer.parseInt(borrowIds);
            ob.setBorrowId(borrowId);
            String fundAmount = (String) map.get("fund_" + borrowIds);
            if (!map.containsKey("applyAmountBorrow")) {
                throw new QinFeiException(1002, "没有获取到实际请款金额信息!");
            }
            Double applyAmount = Double.parseDouble(map.get("applyAmountBorrow").toString());
            Double temp = StringUtils.isNotEmpty(fundAmount) ? Double.parseDouble(fundAmount) : 0;
            if (temp > 0 && !(applyAmount.equals(temp))) {
                throw new QinFeiException(1002, "备用金大于0时，备用金金额必须和实际请款金额一致");
            }
            if (temp > 0) {
                ob.setAmount(temp);
                amount = new BigDecimal(amount.toString()).
                        add(new BigDecimal(temp)).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                outgoBorrowMapper.insert(ob);
//            2、处理还款表
                insertBorrowRepay(outgoId, borrowId, temp);
//            3、处理借款表
                Borrow borrow = borrowService.getById(borrowId);
                borrowService.dealBorrowInfo(borrow, amount);
            } else {
                throw new QinFeiException(1002, "备用金金额不能为0");
            }
        }

//        4、把备用金金额存到请款表中
        Outgo entity = outgoMapper.getById(outgoId);
        entity.setFundAmount(amount);
        entity.setUpdateUserId(AppUtil.getUser().getId());
        outgoMapper.update(entity);
        return amount;
    }

    /**
     * 向还款表插入数据
     */
    private void insertBorrowRepay(Integer outgoId, Integer borrowId, Double fundAmount) {
        User user = AppUtil.getUser();
        Outgo entity = outgoMapper.getById(outgoId);
        BorrowRepay repay = new BorrowRepay();
        repay.setBorrowId(borrowId);
        repay.setRepayId(entity.getId());
        repay.setRepayCode(entity.getCode());
        repay.setType(IConst.REPAY_TYPE_OUTGO);
        repay.setAmount(fundAmount);
        repay.setState(IConst.REPAY_STATE_SAVE);
        repay.setCreateTime(new Date());
        repay.setCreateUserId(user.getId());
        repay.setCreateName(user.getName());
        borrowRepayMapper.insert(repay);
    }

    @Override
    @Transactional
    public void cleanOutgoBorrow(Integer id) {
        //1、处理借款表
        List<OutgoBorrow> borrowList = outgoBorrowMapper.queryBorrowById(id);
        if (borrowList != null && borrowList.size() > 0) {
            for (OutgoBorrow ob : borrowList) {
                Borrow borrow = borrowMapper.getById(ob.getBorrowId());
                borrowService.backBorrowInfo(borrow, ob.getAmount());
            }
        }
        //2、删除借款关系表
        outgoBorrowMapper.deleteByOutgoId(id);
        //3、删除还款表
        borrowRepayMapper.deleteByRepayIdAndType(id, IConst.REPAY_TYPE_OUTGO);
        //4、还原请款表本次还款金额
        outgoMapper.initFundAmount(id);
    }

    @Override
    @Transactional
    public Outgo confirm(Outgo entity, Map map) {
        User user = AppUtil.getUser();
        Double payAmount;
        try {
            payAmount = Double.parseDouble(map.get("payAmount").toString());
        } catch (Exception e) {
            throw new QinFeiException(1002, "获取的付款金额不正确：获取到的出账金额=" + map.get("payAmount").toString());
        }
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
        if (IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType())) {
            entity.setState(IConst.STATE_KJ);//财务复核
        } else {
            //如果是直接完成，需要手动处理待办，
            finishItem(entity);
            entity.setState(IConst.STATE_FINISH);//1通过
            sendMessage(entity,String.format("[%s]您提交的媒介请款[%s]审核已完成。","请款",entity.getTitle()),"/fee/queryOutgo?flag=1&id="+entity.getId());
        }
        entity.setOutAccountName(account.getName());
        entity.setPayAmount(payAmount);
        entity.setUpdateUserId(user.getId());
        entity.setPayUserId(user.getId());

        //不是对公账户 并且不需要回填  就要更新稿件状态
        if( !IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType()) &&  entity.getInvoiceFlag() == 2){
            try {
                //处理稿件表的状态
                List<Integer> ids = outgoMapper.queryArticleIdsByOutgoIdAndState(entity.getId(), IConst.FEE_STATE_PROCESS);
                if (ids == null || ids.size() == 0) {
                    throw new QinFeiException(1002, "没有获取到请款中的稿件。请款id=" + entity.getId());
                }
                Map<String, Object> temp = new HashMap<>();
                temp.put("list", ids);
                temp.put("state", IConst.FEE_STATE_FINISH);
                outgoMapper.changeOutgoState(temp);
                //判断流程结束且需绑定账户  且该账户未被增加
                if(entity.getAccountBinding() == 1 && entity.getAccountId() == null){
                    Account account1 = new Account();
                    account1.setType(2);
                    account1.setCompanyId(entity.getSupplierId());
                    account1.setCompanyName(entity.getSupplierName());
                    account1.setBankNo(entity.getAccountBankNo());
                    account1.setBankName(entity.getAccountBankName());
                    account1.setOwner(entity.getAccountName());
                    account1.setState(1);

                    account1.setCompanyCode(entity.getCompanyCode());
                    account1.setContactor(entity.getSupplierContactor());
                    account1.setCreator(entity.getCreator());
                    account1.setCreateTime(new Date());
                    //如果是 企业供应商则name 为供应商 名称 ， 个提供应商  name为 联系人 名称 0-企业供应商、1-个体供应商
                    Integer supplierType = accountService.supplierType(entity.getSupplierId());
                    if (supplierType == 0){
                        account1.setName(entity.getSupplierName());
                        account1.setAccountType("B2B");
                    }else {
                        account1.setName(entity.getSupplierContactor());
                        account1.setAccountType("B2C");
                    }
                    accountService.outgoAccountAdd(account1);
                }
            } catch (Exception e) {
                throw new QinFeiException(1002, "处理关联的稿件状态出错，请款id=" + entity.getId());
            }
        }

        try {
            //处理还款表
            dealBorrowRepayInfo(entity.getId());
        } catch (Exception e) {
            throw new QinFeiException(1002, "处理备用金还款出错，请款id=" + entity.getId());
        }
        try {
            outgoMapper.updateaccount(entity);
        } catch (Exception e) {
            throw new QinFeiException(1002, "还原出款账户出错，请款id=" + entity.getId());
        }

        //创建事务回滚点，就算后面的业务有异常，前面的业务也要执行
//            Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
        try {
            String taskId = entity.getTaskId();
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckD", IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType()));
            String[] taskIds = new String[]{taskId};
            String desc = map.get("desc").toString();
            processService.approveProcess(taskIds, desc, true);
        } catch (QinFeiException b){
            throw b;
        } catch (Exception e) {
            e.printStackTrace();
            //回滚事务
            throw new QinFeiException(1002, "审批流异常，请款id=" + entity.getId());
//                TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
        }
        return entity;
    }

    @Override
    @Transactional
    public Outgo changeAccount(Outgo entity, Map map) {
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
        outgoMapper.update(entity);
        return entity;
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
    public void CWReject(Outgo entity) {
        User user = AppUtil.getUser();
        try {
            String taskId = entity.getTaskId();
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckD", true);
            Integer state = entity.getState();
            entity.setState(IConst.STATE_REJECT);
            entity.setUpdateUserId(user.getId());
            entity.setUpdateTime(new Date());
            //1、待办变已办
            finishItem(entity);
        } catch (Exception e) {
            throw new QinFeiException(1002, "审批流异常，请款id=" + entity.getId());
        }
        //3、请款订单状态修改
        try {
            outgoMapper.update(entity);
        } catch (Exception e) {
            throw new QinFeiException(1002, "更新请款表信息失败！请款id=" + entity.getId());
        }
    }

    /**
     * state=1或state=12时财务撤回，需要变更以下内容
     * 1、关联的稿件请款状态修改为请款中2
     * 2、处理还款表
     * 3、完成待办
     * 4、增加新的待办
     * 5、请款订单状态修改为-1
     *
     * @param entity
     */
    @Override
    @Transactional
    public Boolean CWReturn(Outgo entity) {
        User user = AppUtil.getUser();
        List<Integer> ids = outgoMapper.selectArticleId(entity.getId());
        if (ids == null || ids.size() == 0) {
            throw new QinFeiException(1002, "未获取到关联的已请款的稿件，请款id=" + entity.getId());
        }
        try {
            //1、处理稿件表的状态
            Map<String, Object> map = new HashMap<>();
            map.put("list", ids);
            map.put("state", IConst.FEE_STATE_PROCESS);
            outgoMapper.changeOutgoState(map);
        } catch (Exception e) {
            throw new QinFeiException(1002, "还原关联的稿件请款状态失败！请款id=" + entity.getId());
        }
        try {
            //2、处理还款表
            backBorrowRepayInfo(entity.getId());
        } catch (Exception e) {
            throw new QinFeiException(1002, "处理请款关联的备用金还款失败，请款id=" + entity.getId());
        }
        try {
            //3、还原请款订单
            outgoMapper.returnOutgoInfo(entity.getId(), user.getId());
        } catch (Exception e) {
            throw new QinFeiException(1002, "还原请款出款信息失败，请款id=" + entity.getId());
        }

        //创建事务回滚点，就算后面的业务有异常，前面的业务也要执行
//        Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
        try {
            //审批流程还原，这一步就算异常，前面的流程也要执行
            String taskId = entity.getTaskId();
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckD", false);
        } catch (Exception e) {
            throw new QinFeiException(1002,"审批流异常，请款id="+entity.getId());
//            e.printStackTrace();
            //回滚事务
//            TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
        }
        return true;
    }

    private void sendMessage(Outgo outgo,String tips,String url) {
        User user = AppUtil.getUser();
        User obj = userMapper.getById(outgo.getApplyId());
        String subject = "[请款]";
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
        mes.setAcceptWorker(outgo.getApplyId());
        mes.setAcceptDept(obj.getDeptId());
        mes.setUrl(url);
        mes.setUrlName("请款管理");
        messageService.addMessage(mes);
    }


    private void dealBorrowRepayInfo(Integer id) {
        List<Borrow> list = borrowMapper.queryByOutgoId(id);
        for (Borrow borrow : list) {
            //处理还款表
            BorrowRepay borrowRepay = borrowRepayMapper.getByRepayIdAndBorrowIdAndTypeAndState(id, borrow.getId(), IConst.REPAY_TYPE_OUTGO, IConst.REPAY_STATE_SAVE);
            if(borrowRepay != null){
                borrowRepay.setUpdateUserId(AppUtil.getUser().getId());
                borrowRepay.setUpdateTime(new Date());
                borrowRepay.setState(IConst.REPAY_STATE_FINISH);
                borrowRepayMapper.update(borrowRepay);
            }
        }
    }

    private void backBorrowRepayInfo(Integer id) {
        List<Borrow> list = borrowMapper.queryByOutgoId(id);
        for (Borrow borrow : list) {
            //处理还款表
            BorrowRepay borrowRepay = borrowRepayMapper.getByRepayIdAndBorrowIdAndTypeAndState(id, borrow.getId(), IConst.REPAY_TYPE_OUTGO, IConst.REPAY_STATE_FINISH);
            borrowRepay.setUpdateUserId(AppUtil.getUser().getId());
            borrowRepay.setUpdateTime(new Date());
            borrowRepay.setState(IConst.REPAY_STATE_SAVE);
            borrowRepayMapper.update(borrowRepay);
        }
    }

    @Override
    @Transactional
    public void checkBtoB(Outgo entity,String desc) {
        entity.setUpdateUserId(AppUtil.getUser().getId());
        entity.setPayTime(new Date());
        String[] taskIds = new String[]{entity.getTaskId()};
        if (entity.getInvoiceFlag()==2) {
            entity.setState(IConst.STATE_FINISH);//1通过

        }else {
            entity.setState(IConst.STATE_MEDIUMBACKFILL);
        }
        outgoMapper.update(entity);
        try {
            processService.approveProcess(taskIds, desc, true);
        } catch (Exception e) {
            throw new QinFeiException(1002, "审批流异常，请款id=" + entity.getId());
        }
        //当媒介没有选择开票回填时 需要更新状态完成，及消待办

        if (entity.getInvoiceFlag()==2){
            finishItem(entity);
        }

    }

    //确认后给财务负责人增加待办
    private Items addItem(Outgo entity, User user) {
        Items items = new Items();
        items.setItemName(entity.getTitle() + "-请款出账确认等待处理");
        items.setItemContent("您有新的请款出账确认需要处理");
        items.setWorkType("出账确认申请");
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryOutgo?flag=5&id=" + entity.getId());
        items.setFinishAddress("/fee/queryOutgo?flag=1&id=" + entity.getId());
        User cw = userService.getCWBZInfo(user.getCompanyCode());
        items.setAcceptWorker(cw.getId());
        items.setAcceptDept(cw.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items;
    }

    //增加待办
    private Items addApplyItem(Outgo entity, User user) {
        Items items = new Items();
        items.setItemName(entity.getTitle() + "-请款撤回等待处理");
        items.setItemContent("您有新的请款撤回需要处理");
        items.setWorkType("请款撤回");
        User mediaUser = userService.getById(entity.getApplyId());
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryOutgo?flag=0&id=" + entity.getId());
        items.setFinishAddress("/fee/queryOutgo?flag=1&id=" + entity.getId());
        items.setAcceptWorker(mediaUser.getId());
        items.setAcceptDept(mediaUser.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items;
    }

    @Override
    public List<Map> exportOutgo(Map map, OutputStream outputStream) {
        if (!ObjectUtils.isEmpty(map.get("deptId"))) {//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = outgoMapper.listPg(map);
        String[] heads = {"请款编号", "请款人", "所在部门", "请款日期", "请款标题", "供应商名称", "支付户主",
                "支付账户", "支付开户行", "请款金额", "期望付款日期", "出款账户", "出款金额", "出款日期"};
        String[] fields = {"code", "applyName", "deptName", "applyTime", "title", "supplierName",
                "accountName", "accountBankNo", "accountBankName", "applyAmount", "expertPayTime",
                "outAccountName", "payAmount", "payTime"};
        ExcelUtil.exportExcel("进账列表", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("applyAmount".equals(field) || "payAmount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else if ("applyTime".equals(field) || "expertPayTime".equals(field) || "payTime".equals(field)) {
                    cell.setCellValue(value.toString());
//                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    public Map aggregateAmount(Map map) {
        User user = AppUtil.getUser();
        if (!ObjectUtils.isEmpty(map.get("deptId"))) {//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        map.put("user", user);
        Map<String, Object> resultMap = outgoMapper.reimburseSum(map);
        return initResult(resultMap);
    }


    @Override
    public Map calculationOfTotal(Map map) {
        User user = AppUtil.getUser();
        if (!ObjectUtils.isEmpty(map.get("deptId"))) {//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        map.put("user", user);
        Map<String, Object> resultMap = outgoMapper.calculationOfTotal(map);
        return initResult(resultMap);
    }

    private Map initResult(Map resultMap) {
        if (resultMap == null || resultMap.size() == 0) {
            resultMap = new HashMap<>();
            resultMap.put("applyAmount", 0);
            resultMap.put("payAmount", 0);
            resultMap.put("outgoamount", 0);
            resultMap.put("costEraseAmountSum", 0);
            resultMap.put("taxesTotalSum", 0);
            resultMap.put("outgoEraseAmountSum", 0);



        }
        return resultMap;
    }
    @Override
    public List<Map> mediaGroupLeader(Integer mediaTypeId){
        Map map = new HashMap();
        User user = AppUtil.getUser();
        map.put("mediaTypeId",mediaTypeId);
        map.put("companyCode",user.getCompanyCode());
        return outgoMapper.mediaGroupLeader(map);
    }

    @Override
    public int selectMediaType(Integer mediaTypeId){
        if(mediaTypeId ==null){
            throw new QinFeiException(1002, "未选择到稿件！");
        }
        return outgoMapper.mediaTypeId(mediaTypeId);
    }

    @Override
    public int backfill(Outgo outgo){
        outgo.setState(IConst.STATE_FINISH);
        outgo.setUpdateTime(new Date());
        outgo.setBackfillTime(new Date());
        String[] taskIds = new String[]{outgo.getTaskId()};
        try{
            String result = processService.approveProcess(taskIds, null, true);
            if(!"操作成功。".equals(result)){
                throw new QinFeiException(1002, result);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e) {
            throw new QinFeiException(1002, "审批流异常，报销id=" + outgo.getId());
        }
        return  outgoMapper.backfill(outgo);

    }

    @Override
    public PageInfo<Map> resetListPg(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        if (!ObjectUtils.isEmpty(map.get("deptId"))) {//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = outgoMapper.resetListPg(map);
        List<Integer> fil = new ArrayList<>();
        Map<Integer, Map> map1 = new HashMap();
        for(Map m :list){
            Integer outgoId= Integer.parseInt(String.valueOf(m.get("id")));
            map1.put(outgoId,m);
            fil.add(outgoId);
        }
        if (fil.size()>0){
            List<Map> outgosum = outgoMapper.sum(fil);
            if (outgosum.size()>0){
                for (Map outgoAmountSum: outgosum) {
                    Integer id = Integer.parseInt(String.valueOf(outgoAmountSum.get("id")));
                    map1.get(id).put("outgoAmountSum", outgoAmountSum.get("outgoAmountSum"));
                }
            }
        }
        return new PageInfo<>(list);
    }
}
