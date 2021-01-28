package com.qinfei.qferp.service.impl.fee;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.config.Config;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleHistory;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.fee.Refund;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.biz.ArticleHistoryMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapperXML;
import com.qinfei.qferp.mapper.fee.RefundMapper;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.fee.IRefundService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class RefundService implements IRefundService {
    @Autowired
    private RefundMapper refundMapper ;
    @Autowired
    private ArticleMapper articleMapper ;
    @Autowired
    private IOrderService orderService ;
    @Autowired
    private ArticleMapperXML articleMapperXML ;
    @Autowired
    private IItemsService itemsService ;
    @Autowired
    private IProcessService processService;
    @Autowired
    private IUserService userService;
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
    private ArticleHistoryMapper articleHistoryMapper ;
    @Autowired
    private Config config;
    @Autowired
    private FlowableMapper flowableMapper;

    //退款状态
    private final static Map<Integer, String> refundStateMap = new HashMap<Integer, String>(){
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
        }
    };
    //退款类型
    private final static Map<Integer, String> refundTypeMap = new HashMap<Integer, String>(){
        {
            put(0,"直接退款");
            put(1,"稿件退款");
            put(2,"其他支出");
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
        List<Map> list = refundMapper.listPg(map);
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
        PageInfo<Map> pageInfo = new PageInfo<>(list);
        return pageInfo ;
    }

    @Override
    public Refund getById(Integer id) {
        return refundMapper.getById(id);
    }

    /**
     * saveStepOne退款，该方法暂不用
     * @param entity
     * @return
     */
    @Override
    @Transactional
    public Refund add(Refund entity) {
        User opUser = AppUtil.getUser() ;
        entity.setCreateTime(new Date());
        entity.setApplyId(opUser.getId());
        entity.setApplyName(opUser.getName());
        entity.setDeptId(opUser.getDeptId());
        entity.setDeptName(opUser.getDeptName()) ;
        entity.setApplyTime(new Date());
        entity.setCreator(opUser.getId());
        refundMapper.insert(entity);
        return entity;
    }
    @Override
    @Transactional
    public Refund edit(Refund entity) {
        User user = AppUtil.getUser() ;
        entity.setUpdateUserId(user.getId());
        //待办变已办
        finishItem(entity);
        //退款暂存存
        if(entity.getState() == IConst.STATE_SAVE){
            refundMapper.update(entity);
         //退款提交审批
        }else{
            refundMapper.update(entity);
            //urgencyLevel紧急程度，暂不启用
            processService.addRefundProcess(entity, 3);
        }
        return entity;
    }
    @Override
    public Refund update(Refund entity) {
        refundMapper.update(entity);
        return entity;
    }
    @Override
    public void delById(Refund entity) {
        User user = AppUtil.getUser() ;
        List<Integer> list = refundMapper.queryArticleIdsByRefundId(entity.getId()) ;

        if(list!=null && list.size()>0){
            String curMonth = DateUtils.getYearAndMonthStr2(new Date()) ;//当前年月
            //查询关联的稿件是否有已提成的稿件，有的话不允许删除
            Integer count = articleMapperXML.countByByIdsAndCommissionStates(list) ;
            if(count>0){
                throw new QinFeiException(1002,"关联的稿件有登记提成的稿件，无法删除流程，请联系财务取消登记提成后重试！") ;
            }
            for(Integer id:list){
                Article article = articleMapper.get(Article.class,id) ;
                Order order = orderService.get(article.getOrderId()) ;
                String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
                Double amount = article.getIncomeStates()==IConst.FEE_STATE_FINISH ? article.getIncomeAmount(): article.getSaleAmount() ;
                Double profit;
                ArticleHistory history = new ArticleHistory();
                BeanUtils.copyProperties(article, history);
                history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                history.setUserId(order.getUserId());
                history.setDeptId(order.getDepatId());
                history.setId(null);
                history.setArtId(article.getId());
                if(entity.getType() == IConst.REFUND_TYPE_REFUND){
                    profit = new BigDecimal(amount).
                            subtract(new BigDecimal(article.getOutgoAmount())).
                            subtract(new BigDecimal(article.getTaxes())).
                            subtract(new BigDecimal(article.getOtherPay())).
                            setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue() ;
                    history.setEditDesc(IConst.article_change_refund_del);
//                  1、更改日期和稿件发布日期不一致，2、退款有变动
                    Double alterRefund = 0 - article.getRefundAmount();
                    if(!(curMonth.equals(issueMonth))
                            && Math.abs(alterRefund)>0.01){
                        article.setAlterFlag(IConst.article_alter_flag_true);
                        history.setAlterRefund(alterRefund);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                    }
                    if(IConst.article_alter_flag_true.equals(history.getAlterLabel())){
                        Double alterProfit = profit - article.getProfit();
                        history.setAlterProfit(alterProfit);
                    }
                    article.setRefundStates(IConst.FEE_STATE_SAVE);
                    article.setProfit(profit);
                    article.setRefundAmount(0D);
                }else if(entity.getType() == IConst.REFUND_TYPE_OTHER_PAY){
                    profit = new BigDecimal(amount).
                            subtract(new BigDecimal(article.getOutgoAmount())).
                            subtract(new BigDecimal(article.getTaxes())).
                            subtract(new BigDecimal(article.getRefundAmount())).
                            setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue() ;
                    history.setEditDesc(IConst.article_change_other_pay_del);
//                  1、更改日期和稿件发布日期不一致，2、退款有变动
                    Double alterOtherPay = 0 - article.getOtherPay();
                    if(!(curMonth.equals(issueMonth))
                            && Math.abs(alterOtherPay)>0.01){
                        article.setAlterFlag(IConst.article_alter_flag_true);
                        history.setAlterOtherPay(alterOtherPay);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                    }
                    if(IConst.article_alter_flag_true.equals(history.getAlterLabel())){
                        Double alterProfit = profit - article.getProfit();
                        history.setAlterProfit(alterProfit);
                    }
                    article.setOtherPayStates(IConst.FEE_STATE_SAVE);
                    article.setProfit(profit);
                    article.setUpdateUserId(user.getId());
                    article.setOtherPay(0D);
                }
                history.setCreator(user.getId());
                history.setCreateTime(new Date());
                articleHistoryMapper.insert(history);
                articleMapper.update(article);
            }
        }
        //删除关系表
        try{
            refundMapper.delRefundArticle(entity.getId());
        }catch (Exception e){
            throw new QinFeiException(1002,"删除退款和稿件关系表失败") ;
        }
        //删除退款表
        try{
            entity.setState(IConst.STATE_DELETE);
            entity.setUpdateUserId(user.getId());
            refundMapper.update(entity);
        }catch (Exception e){
            throw new QinFeiException(1002,"更新退款失败，流程id="+entity.getId()) ;
        }
        //待办变已办
        try{
            finishItem(entity);
        }catch (Exception e){
            throw new QinFeiException(1002,"处理待办失败！");
        }
    }

    public void finishItem(Refund entity){
        if(entity.getItemId()!=null){
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }

    @Override
    public PageInfo<Map> listPgForSelectArticle(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = refundMapper.listPgForSelectArticle(map);
        PageInfo<Map> pageInfo = new PageInfo<>(list);
        return pageInfo ;
    }

    @Override
    public PageInfo<Map> listPgForSelectArticle2(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = refundMapper.listPgForSelectArticle2(map);
        PageInfo<Map> pageInfo = new PageInfo<>(list);
        return pageInfo ;
    }

    @Override
    @Transactional
    public Refund saveStepOne(Map map) {
        User user = AppUtil.getUser() ;
        Integer custCompanyId = Integer.parseInt((String)map.get("custCompanyIdSec")) ;
        String custCompanyName = (String)map.get("custCompanyNameSec");
        Integer custId = Integer.parseInt((String)map.get("custIdSec")) ;
        String custName = (String)map.get("custNameSec");
        Integer type = Integer.parseInt((String)map.get("typeSec"));
        //更新关系表
        String articleIds = "" ;
        synchronized (this){
            Refund entity = new Refund() ;
            if(type==IConst.REFUND_TYPE_REFUND || type==IConst.REFUND_TYPE_OTHER_PAY){
                articleIds = (String)map.get("articleIdsSec");
                if(!StringUtils.isEmpty(articleIds)){
                    Integer count;
                    if(type==IConst.REFUND_TYPE_REFUND){//退款
                        count =  articleMapperXML.listByIdsAndRefundStates(articleIds) ;
                        if(count>0){
                            throw new QinFeiException(1002,"选中的稿件有退款记录，一条稿件不支持多次退款！");
                        }
                    }else{//其他支出
                        count =  articleMapperXML.listByIdsAndOtherPayStates(articleIds) ;
                        if(count>0){
                            throw new QinFeiException(1002,"选中的稿件有其他支出记录，一条稿件不支持多次支出！");
                        }
                    }
                }else{
                    throw new QinFeiException(1002,"获取关联的稿件id失败。获取的id="+articleIds);
                }
            }

            entity.setType(type);
            entity.setApplyId(user.getId());
            entity.setDeptId(user.getDeptId());
            entity.setDeptName(user.getDeptName());
            entity.setApplyName(user.getName());
            entity.setApplyTime(new Date());
            entity.setCustCompanyId(custCompanyId);
            entity.setCustCompanyName(custCompanyName);
            entity.setCustId(custId);
            entity.setCustName(custName);
            entity.setCreator(user.getId());
            entity.setCreateTime(new Date());
            entity.setCompanyCode(user.getCompanyCode());

            try{
                //生成KP2018110001的编号，前六位是年月，后四位累加
                entity.setCode(IConst.REFUND_CODE+ CodeUtil.getMonthStr()+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.REFUND_CODE),4)) ;
            }catch (QinFeiException e){
                throw new QinFeiException(1002,"生成退款编号失败！")  ;
            }
            try{
                refundMapper.insert(entity) ;
            }catch (QinFeiException e){
                throw new QinFeiException(1002,"插入退款表失败！");
            }

            if(type==IConst.REFUND_TYPE_REFUND || type==IConst.REFUND_TYPE_OTHER_PAY){
                insertRefundArticle(entity,articleIds,map) ;
            }
            return entity ;
        }
    }

    /**
     * type=1,稿件退款，type=2其他支出，存入article表的字段不一致
     */
    @Transactional
    public void insertRefundArticle(Refund entity, String articleIds,Map map) {
        if(articleIds.indexOf(",")>-1){
            String[] ids = articleIds.split(",") ;
            //放入set排重
            Set<Integer> set = new HashSet();
            for(int i=0;i<ids.length;i++){
                set.add(Integer.parseInt(ids[i]));
            }
            Iterator<Integer> iterator =set.iterator() ;
            try{
                //一般稿件退款和其他支出每次只选择一条稿件，所以这一块一般不用。直接走else
                while(iterator.hasNext()){
                    Integer articleId = iterator.next() ;
                    Double refundAmount = Double.parseDouble((String)map.get("refund_"+articleId)) ;
                    if(!(refundAmount>0)){
                        throw new QinFeiException(1002,"退款或其它支出金额必须大于0") ;
                    }
                    Article article = articleMapper.get(Article.class,articleId) ;
                    //维护稿件表
                    updateArticleRefund(entity,article,refundAmount) ;
                    //插入关系表
                    refundMapper.insertRefundArticle(entity.getId(),articleId);
                }
            }catch (Exception e){
                throw new QinFeiException(1002,"维护稿件退款信息失败！");
            }
        }else{
            Integer id ;
            try{
                id = Integer.parseInt(articleIds) ;
            }catch (Exception e){
                throw new QinFeiException(1002,"获取的稿件id应为数字，实际为："+articleIds);
            }
            Double refundAmount ;
            String refund = "" ;
            try{
                refund = (String)map.get("refund_"+id) ;
                refundAmount = Double.parseDouble(refund) ;
            }catch (Exception e){
                if(entity.getType()==IConst.REFUND_TYPE_REFUND){
                    throw new QinFeiException(1002,"获取退款金额出错，获取到的数据为："+refund);
                }else{
                    throw new QinFeiException(1002,"获取其他支出金额出错，获取到的数据为："+refund);
                }
            }
            if(!(refundAmount>0)){
                throw new QinFeiException(1002,"申请金额必须大于0") ;
            }

            Article article = articleMapper.get(Article.class,id) ;
            //维护稿件表
            updateArticleRefund(entity,article,refundAmount) ;

            try{
                //插入关系表
                refundMapper.insertRefundArticle(entity.getId(),id);
            }catch (Exception e){
                throw new QinFeiException(1002,"插入退款和稿件关系表失败！");
            }
        }
    }
    @Transactional
    public void updateArticleRefund(Refund entity,Article article,Double refundAmount){
        ArticleHistory history = new ArticleHistory();
        Order order = orderService.get(article.getOrderId()) ;
        BeanUtils.copyProperties(article, history);
        history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
        history.setUserId(order.getUserId());
        history.setDeptId(order.getDepatId());
        history.setId(null);
        history.setArtId(article.getId());
        String curMonth = DateUtils.getYearAndMonthStr2(new Date()) ;//当前年月
        String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
        Double amount = article.getIncomeStates()==IConst.FEE_STATE_FINISH ? article.getIncomeAmount(): article.getSaleAmount() ;
        Double profit = 0D;
        if(entity.getType() == IConst.REFUND_TYPE_REFUND){
            profit = new BigDecimal(amount).
                    subtract(new BigDecimal(article.getOutgoAmount())).
                    subtract(new BigDecimal(article.getTaxes())).
                    subtract(new BigDecimal(article.getOtherPay())).
                    subtract(new BigDecimal(refundAmount)).
                    setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue() ;
            if(article.getRefundAmount()>0){
                article.setRefundAmount(refundAmount);
            }else{
                article.setRefundAmount(new BigDecimal(article.getRefundAmount()).
                        add(new BigDecimal(refundAmount)).
                        setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            article.setRefundStates(IConst.FEE_STATE_PROCESS);
            history.setEditDesc(IConst.article_change_refund_add);
//          1、更改日期和稿件发布日期不一致，2、退款有变动
            if(!(curMonth.equals(issueMonth))
                    && Math.abs(refundAmount)>0.01){
                article.setAlterFlag(IConst.article_alter_flag_true);
                history.setAlterRefund(refundAmount);
                history.setAlterLabel(IConst.article_alter_flag_true);
            }
        }else if(entity.getType() == IConst.REFUND_TYPE_OTHER_PAY){
            profit = new BigDecimal(amount).
                    subtract(new BigDecimal(article.getOutgoAmount())).
                    subtract(new BigDecimal(article.getTaxes())).
                    subtract(new BigDecimal(article.getRefundAmount())).
                    subtract(new BigDecimal(refundAmount)).
                    setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue() ;
            if(article.getOtherPay()>0){
                article.setOtherPay(refundAmount);
            }else{
                article.setOtherPay(new BigDecimal(article.getOtherPay()).
                        add(new BigDecimal(refundAmount)).
                        setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            article.setOtherPayStates(IConst.FEE_STATE_PROCESS);
            history.setEditDesc(IConst.article_change_other_pay_add);
//          1、更改日期和稿件发布日期不一致，2、其他支出有变动
            if(!(curMonth.equals(issueMonth))
                    && Math.abs(refundAmount)>0.01){
                article.setAlterFlag(IConst.article_alter_flag_true);
                history.setAlterOtherPay(refundAmount);
                history.setAlterLabel(IConst.article_alter_flag_true);
            }
        }
        if(IConst.article_alter_flag_true.equals(history.getAlterLabel())){
            Double alterProfit = profit - article.getProfit();
            history.setAlterProfit(alterProfit);
        }
        article.setProfit(profit);
        history.setCreator(AppUtil.getUser().getId());
        history.setCreateTime(new Date());
        articleHistoryMapper.insert(history);
        articleMapper.update(article);
    }
    @Override
    public PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Integer id) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = refundMapper.listPgForSelectedArticle(id);
        PageInfo<Map> pageInfo = new PageInfo<>(list);
        return pageInfo ;
    }

    @Override
    public Map querySumAmountById(Integer refundId) {
        return refundMapper.querySumAmountById(refundId);
    }

    @Override
    @Transactional
    public Refund confirm(Refund entity,Map map){
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
        if(IConst.ACCOUNT_TYPE_B2B.equals(account.getAccountType())){
            entity.setState(IConst.STATE_CWFH);//财务复核
        }else{
            //如果是直接完成，需要手动处理待办，
            finishItem(entity);
            entity.setState(IConst.STATE_FINISH);//1通过
            //网关判断（普通账户直接结束通知发起人）
            sendMessage(entity,String.format("[%s]您提交的退款申请[%s]审核已完成。","退款",entity.getTitle()),"/fee/queryRefund?flag=1&id="+entity.getId());
        }
        entity.setOutAccountName(account.getName());
        entity.setPayAmount(payAmount);
        entity.setPayUserId(user.getId());
        entity.setUpdateUserId(user.getId());

        //refundStates发起退款置2，审批通过财务置1，默认0
        if(entity.getType()==IConst.REFUND_TYPE_REFUND || entity.getType()==IConst.REFUND_TYPE_OTHER_PAY){
            List<Integer> ids ;
            if(entity.getType()==IConst.REFUND_TYPE_REFUND){
                ids = refundMapper.queryArticleIdsByRefundIdAndRefundState(entity.getId(),IConst.FEE_STATE_PROCESS) ;
            }else{
                ids = refundMapper.queryArticleIdsByRefundIdAndOtherPayState(entity.getId(),IConst.FEE_STATE_PROCESS) ;
            }
            //1、更新稿件退款状态,ids.size()只会等于1，直接for循环里处理
            if(ids!=null && ids.size()>0){
                for(Integer id:ids){
                    Article article = articleMapper.get(Article.class,id) ;
                    if(entity.getType() == IConst.REFUND_TYPE_REFUND){
                        article.setRefundStates(IConst.FEE_STATE_FINISH);
                    }else if(entity.getType() == IConst.REFUND_TYPE_OTHER_PAY){
                        article.setOtherPayStates(IConst.FEE_STATE_FINISH);
                    }
                    articleMapper.update(article);
                }
            }else{
                throw new QinFeiException(1002,"未获取到退款或其他支出关联的稿件。退款id="+entity.getId());
            }
        }
        try{
            //2、更新退款出账账户信息
            refundMapper.updateaccount(entity);
        }catch (Exception e){
            throw new QinFeiException(1002,"更新出款账户失败！退款id="+entity.getId());
        }

//            //创建事务回滚点，就算后面的业务有异常，前面的业务也要执行
//            Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
        try{
            String desc = map.get("desc").toString();
            String taskId= entity.getTaskId();
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckB", "B2B".equals(account.getAccountType()));
            String[] taskIds = new String[]{taskId};
            processService.approveProcess(taskIds, desc, true);
        }catch (Exception e){
            throw new QinFeiException(1002,"审批流异常！退款id="+entity.getId());
            //回滚事务
//                TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
        }
        return entity ;
    }

    /**
     * state=2时财务撤回，此时，稿件状态和借款状态还未变更
     * 1、完成待办
     * 2、增加新的待办
     * 3、请款订单状态修改为-1
     * @param entity
     */
    @Override
    @Transactional
    public void CWReject(Refund entity){
        //1、待办变已办
        finishItem(entity) ;

        //2、增加待办
        User user = AppUtil.getUser() ;
        Items item = addApplyItem(entity,user) ;
        entity.setItemId(item.getId());
        //3、请款订单状态修改
        refundMapper.update(entity);

        //创建事务回滚点，就算后面的业务有异常，前面的业务也要执行
//        Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
        try{
            //审批流程还原，这一步就算异常，前面的流程也要执行
            String taskId= entity.getTaskId();
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckB", false);
            //(gateCheckB为true时不用发消息，false特殊发消息，不然会多发流程消息)
            sendMessage(entity,String.format("[%s]您提交的退款申请[%s]在%s节点被驳回","退款",entity.getTitle(),"出纳出款"),"/fee/queryRefund?flag=0&id="+entity.getId());
        }catch (Exception e){
//            e.printStackTrace();
            throw new QinFeiException(1002,"审批流异常！退款id="+entity.getId());
            //回滚事务
//            TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
        }
    }

    /**
     * state=1或state=12时财务撤回，需要变更以下内容
     * 1、关联的稿件请款状态修改为请款中2
     * 2、完成待办
     * 3、增加新的待办
     * 4、请款订单状态修改为-1
     * @param entity
     */
    @Override
    @Transactional
    public Boolean CWReturn(Refund entity){
        User user = AppUtil.getUser() ;
        //1、处理稿件表的状态
        if(entity.getType()==IConst.REFUND_TYPE_REFUND || entity.getType()==IConst.REFUND_TYPE_OTHER_PAY){
            List<Integer> ids ;
            if(entity.getType()==IConst.REFUND_TYPE_REFUND){
                ids = refundMapper.queryArticleIdsByRefundIdAndRefundState(entity.getId(),IConst.FEE_STATE_FINISH) ;
            }else{
                ids = refundMapper.queryArticleIdsByRefundIdAndOtherPayState(entity.getId(),IConst.FEE_STATE_FINISH) ;
            }

            if(ids!=null && ids.size()>0){
                if(articleMapper.getCommissionByArticleIds(ids)>0){
                    throw new QinFeiException(1002,"退款流程中的稿件已经登记提成，无法撤回，请联系财务取消登记提成后再操作！退款id="+entity.getId());
                }
                try{
                    for(Integer id:ids){
                        Article article = articleMapper.get(Article.class,id) ;
                        if(entity.getType() == IConst.REFUND_TYPE_REFUND){
                            article.setRefundStates(IConst.FEE_STATE_PROCESS);
                        }else if(entity.getType() == IConst.REFUND_TYPE_OTHER_PAY){
                            article.setOtherPayStates(IConst.FEE_STATE_PROCESS);
                        }
                        articleMapper.update(article);
                    }
                }catch (Exception e){
                    throw new QinFeiException(1002,"维护稿件退款状态出错！退款id="+entity.getId());
                }
            }else{
                throw new QinFeiException(1002,"未获取到退款或其他支出关联的稿件。退款id="+entity.getId());
            }
        }

        //4、更新请款订单
        try{
            refundMapper.returnRefundInfo(entity.getId(),user.getId());
        }catch (Exception e){
            throw new QinFeiException(1002,"还原退款表信息失败！退款id="+entity.getId());
        }

        //创建事务回滚点，就算后面的业务有异常，前面的业务也要执行
//        Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
        try{
            //审批流程还原，这一步就算异常，前面的流程也要执行
            String taskId= entity.getTaskId();
            String instId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
            runtimeService.setVariable(instId, "gateCheckB", false);
        }catch (Exception e){
//            e.printStackTrace();
            throw new QinFeiException(1002,"审批流异常，退款id="+entity.getId());
            //回滚事务
//            TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
        }
        return true ;
    }

    @Override
    @Transactional
    public void checkBtoB(Refund entity){
        try{
            String[] taskIds = new String[]{entity.getTaskId()};
            processService.approveProcess(taskIds, "", true);
        }catch (Exception e){
            throw new QinFeiException(1002,"审批流异常，退款id="+entity.getId()) ;
        }
    }

    //增加待办
    public Items addApplyItem(Refund entity,User user){
        Items items = new Items();
        items.setItemName(entity.getTitle()+"-退款撤回等待处理");
        items.setItemContent("您有新的退款撤回需要处理");
        items.setWorkType("退款撤回");
        User mediaUser = userService.getById(entity.getApplyId()) ;
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryRefund?flag=0&id="+entity.getId());
        items.setFinishAddress("/fee/queryRefund?flag=1&id="+entity.getId());
        items.setAcceptWorker(mediaUser.getId()) ;
        items.setAcceptDept(mediaUser.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }

    //增加待办
    public Items addItem(Refund entity,User user){
        Items items = new Items();
        items.setItemName(entity.getTitle()+"-退款出账确认等待处理");
        items.setItemContent("您有新的退款出账确认需要处理");
        items.setWorkType("出账确认申请");
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryRefund?flag=5&id="+entity.getId());
        items.setFinishAddress("/fee/queryRefund?flag=1&id="+entity.getId());
        User cw = userService.getCWBZInfo(user.getCompanyCode()) ;
        items.setAcceptWorker(cw.getId()) ;
        items.setAcceptDept(cw.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }

    /**
     * 根据稿件编号查询退款id
     * @param articleId
     * @return
     */
    @Override
    public Integer queryRefundId(Integer articleId){
        Integer refundId = refundMapper.queryRefundId(articleId);
        return refundId!=null?refundId:-2;
    }

    /**
     * 根据稿件编号查询其他支出id
     * @param articleId
     * @return
     */
    @Override
    public Integer queryOtherPayId(Integer articleId){
        Integer refundId = refundMapper.queryOtherPayId(articleId);
        return refundId!=null?refundId:-2;
    }

    /**
     * 导出退款信息
     * @param map
     * @param outputStream
     * @return
     */
    @Override
    public List<Map> exportRefund(Map map, OutputStream outputStream){
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> refundList = refundMapper.listPg(map);
        String [] heads = new String[]{"请款编号","类型","标题","申请人","申请时间","申请部门","客户公司名称","客户联系人","收款人","收款账户","收款银行","申请金额","实际出款账户","实际出款金额","实际出款日期"};
        String [] fields = new String[]{"code","type","title","applyName","applyTime","deptName","custCompanyName","custName","accountName","accountBankNo","accountBankName","applyAmount","outAccountName","payAmount","payTime"};
        ExcelUtil.exportExcel("退款列表",heads,fields,refundList,outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("applyAmount".equals(field)||"payAmount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else if ("applyTime".equals(field)||"payTime".equals(field)) {
                    cell.setCellValue(value.toString());
                }else if("type".equals(field)){
                    if((int) value ==0){
                        cell.setCellValue("全额退款");
                    }else if((int) value ==1){
                        cell.setCellValue("稿件退款");
                    }else{
                        cell.setCellValue("其他支出");
                    }
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return refundList;
    }

    @Override
    public Map reimburseSum(Map map){
        User user =AppUtil.getUser();
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        map.put("user",user);
        Map<String,Object> resultMap = refundMapper.reimburseSum(map);
        return  initResult(resultMap);
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
            return DataImportUtil.createRefundFile("退款信息", config.getUploadDir(), config.getWebDir(), Arrays.asList(param));
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "下载退款信息异常！");
        }
    }

    @Override
    public String batchDownloadData(Map<String, Object> param) {
        try{
            List<Map> list = listRefundData(param);
            return DataImportUtil.createRefundFile("退款信息", config.getUploadDir(), config.getWebDir(), list);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "批量下载退款信息异常！");
        }
    }

    @Override
    public List<Map> listRefundData(Map<String, Object> param) {
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
            List<Map> list = refundMapper.listPg(param);
            //获取审核列表
            if(!CollectionUtils.isEmpty(list)){
                List<Map<String, Object>> paramList = new ArrayList<>();
                list.forEach(o -> {
                    //设置查询审核详情条件
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("dataId", o.get("id"));
                    tmp.put("process", 4);
                    paramList.add(tmp);
                    //设置中文值
                    o.put("modalTitle", String.format("【退款】%s-%s-%s", String.valueOf(o.get("code")),String.valueOf(o.get("applyName")),String.valueOf(o.get("applyTime"))));
                    String type = refundStateMap.get(Integer.parseInt(String.valueOf(o.get("type"))));
                    String state = refundTypeMap.get(Integer.parseInt(String.valueOf(o.get("state"))));
                    o.put("type", type);
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
            throw new QinFeiException(1002, "批量打印退款信息异常！");
        }
    }

    @Override
    public boolean getFlowPrintPermission(HttpServletRequest request) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            HttpSession session = request.getSession();
            //获取当前用户权限资源，判断是否有培训设置权限，有的话则代表有管理员权限
            List<Resource> resources = (List<Resource>) session.getAttribute(IConst.USER_RESOURCE);
            if(!CollectionUtils.isEmpty(resources)){
                for(Resource resource : resources){
                    if(!StringUtils.isEmpty(resource.getUrl()) && resource.getUrl().contains("fee/flowPrint")){
                        return true;
                    }
                }
            }
            return  false;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "获取打印下载权限异常！");
        }
    }

    public Map initResult(Map resultMap) {
        if (resultMap == null || resultMap.size() == 0) {
            resultMap = new HashMap<>();
            resultMap.put("totalMoney", 0);
            resultMap.put("payAmount", 0);

        }
        return resultMap;
    }
    //发送消息
    private void sendMessage(Refund entity, String tips,String url) {
        User user = AppUtil.getUser();
        User obj = userMapper.getById(entity.getApplyId());
        String subject = "[退款]";
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
        mes.setType(4);//退款
        mes.setUrl(url);
        mes.setUrlName("退款管理");
        mes.setContent(tips);
        mes.setInitiatorWorker(user.getId());
        mes.setInitiatorDept(user.getDeptId());
        mes.setAcceptWorker(entity.getApplyId());
        mes.setAcceptDept(obj.getDeptId());
        messageService.addMessage(mes);
    }

    @Override
    @Transactional
    public Refund changeAccount(Refund entity,Map<Object,String> map){
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
        refundMapper.update(entity);
        return entity;
    }
}
