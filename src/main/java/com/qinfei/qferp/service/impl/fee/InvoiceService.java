package com.qinfei.qferp.service.impl.fee;

import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.serivce.impl.DictService;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleHistory;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.crm.CrmCompany;
import com.qinfei.qferp.entity.crm.CrmCompanyUser;
import com.qinfei.qferp.entity.fee.Invoice;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.biz.ArticleHistoryMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapper;
import com.qinfei.qferp.mapper.fee.InvoiceMapper;
import com.qinfei.qferp.mapper.fee.TaxUserMapper;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.service.fee.IInvoiceService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.crm.CrmCompanyService;
import com.qinfei.qferp.service.impl.crm.CrmCompanyUserService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class InvoiceService implements IInvoiceService {
    @Autowired
    private InvoiceMapper invoiceMapper ;
    @Autowired
    private ArticleMapper articleMapper ;
    @Autowired
    private IItemsService itemsService ;
    @Autowired
    private IProcessService processService ;
    @Autowired
    private UserService userService ;
    @Autowired
    private DictService dictService ;
    @Autowired
    private CrmCompanyUserService crmCompanyUserService ;
    @Autowired
    private CrmCompanyService crmCompanyService ;
    @Autowired
    private TaxUserMapper taxUserMapper;
    @Autowired
    private ArticleHistoryMapper articleHistoryMapper ;
    @Autowired
    private IOrderService orderService ;

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
        List<Map> list = invoiceMapper.listPg(map);
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
    public Invoice getById(Integer id) {
        return invoiceMapper.getById(id);
    }

    @Override
    @Transactional
    public Invoice add(Invoice entity) {
        invoiceMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public Invoice edit(Invoice entity,Double invoiceAmount) {
        int count = invoiceMapper.countByInvoiceIdAndCommissionStates(entity.getId());
        //count已登记提成的稿件数量
        if(count > 0){
            //如果invoiceAmount = null ,表示首次申请开票，这个时候提成了就不允许申请开票了
            if(invoiceAmount == null){
                throw new QinFeiException(1002,"关联的稿件中有已登记提成的稿件，不允许提交开票申请，请联系财务取消登记提成后重试！") ;
            }else{
                //如果invoiceAmount!=null，表示是之前已经提交申请了，但是被驳回了，这个时候只要申请金额和之前一致就可以
                if(!invoiceAmount.equals(entity.getInvoiceAmount())){
                    throw new QinFeiException(1002,"关联的稿件中有已提成的稿件，不允许修改实际开票金额，请联系财务取消登记提成后重试！当前实际开票金额只能为="+invoiceAmount) ;
                }
            }
        }
        try{
            entity.setState(IConst.STATE_BZ);
            invoiceMapper.update(entity);
        }catch (Exception e){
            throw new QinFeiException(1002,"更新开票表数据失败：id="+entity.getId()) ;
        }
        try{
            // 紧急程度字段暂不启用
            // taskId为空：首次提交审批；不为空：驳回后提交审批
            processService.addBallotProcess(entity, 3);
        }catch (Exception e){
            throw new QinFeiException(1002,"提交审批流程出错！") ;
        }
        //计算税金
        updateArticleInfo(entity);
        //处理待办
        finishItem(entity);
        return entity;
    }
    @Override
    @Transactional
    public Invoice update(Invoice entity) {
        invoiceMapper.update(entity);
        return entity;
    }
    //提审的时候计算税金
    private void updateArticleInfo(Invoice entity) {
        User user = AppUtil.getUser() ;
        if(user==null){
            throw new QinFeiException(1002,"获取用户信息失败，请刷新后重试！");
        }
        String curMonth = DateUtils.getYearAndMonthStr2(new Date()) ;//当前年月
        if(entity.getAmount()!=null && entity.getInvoiceAmount()!=null){
            Double percent = 1.0 ;
            if(entity.getRatio()==null){
                throw new QinFeiException(1002,"获取换算比失败！");
            }
            percent = new BigDecimal(entity.getTaxPoint()).
                    divide(new BigDecimal(entity.getRatio()),8,BigDecimal.ROUND_HALF_UP).doubleValue() ;
            List<Article> list = invoiceMapper.queryArticleById(entity.getId()) ;
            if(list==null || list.size()==0){
                throw new QinFeiException(1002,"没有获取到开票流程关联的稿件，开票id="+entity.getId()) ;
            }
            List<ArticleHistory> historyList = new ArrayList<>();
            List<Article> articleList = new ArrayList<>();
            if(entity.getTaxType()==null){
                throw new QinFeiException(1002,"请先选择税率!");
            }
            Dict dict = dictService.getByTypeCodeAndName(IConst.DICT_TYPE_CODE_TAX,entity.getTaxType(),entity.getCompanyCode());
            if(!entity.getInvoiceAmount().equals(entity.getAmount())){
                for(Article article:list){
                    int size = list.size() ;
                    //均值
                    Double tax = new BigDecimal(entity.getInvoiceAmount()).multiply(new BigDecimal(percent)).divide(new BigDecimal(size),2,BigDecimal.ROUND_HALF_UP).doubleValue() ;
                    Double profit;
                    if(article.getIncomeStates()==IConst.FEE_STATE_FINISH){
                        profit =article.getIncomeAmount()-article.getOutgoAmount()-tax-article.getRefundAmount()-article.getOtherPay() ;
                    }else{
                        profit =article.getSaleAmount()-article.getOutgoAmount()-tax-article.getRefundAmount()-article.getOtherPay() ;
                    }
                    Order order = orderService.get(article.getOrderId()) ;
                    ArticleHistory history = new ArticleHistory();
                    BeanUtils.copyProperties(article, history);
                    history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                    history.setUserId(order.getUserId());
                    history.setDeptId(order.getDepatId());
                    history.setId(null);
                    history.setArtId(article.getId());
                    history.setEditDesc(IConst.article_change_invoice_add);
                    history.setCreator(user.getId());
                    history.setCreateTime(new Date());

//                  满足三个条件税金就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置税金，3、税金有变动
                    String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
                    Double alterTax = tax - article.getTaxes();
                    if(!(curMonth.equals(issueMonth))
                            && Math.abs(alterTax)>0.01){
                        article.setAlterFlag(IConst.article_alter_flag_true);
                        history.setAlterTax(alterTax);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                    }
                    if(IConst.article_alter_flag_true.equals(history.getAlterLabel())){
                        Double alterProfit = profit - article.getProfit();
                        history.setAlterProfit(alterProfit);
                    }
                    historyList.add(history);

                    article.setTaxType(dict.getId());
                    article.setTaxes(tax);
                    article.setProfit(profit);
                    article.setUpdateUserId(user.getId());
                    articleList.add(article);
                }
            }else{
                for(Article article:list){
                    //税金=报价*比例
                    Double tax = new BigDecimal(article.getSaleAmount()).multiply(new BigDecimal(percent)).doubleValue() ;
                    ArticleHistory history = new ArticleHistory();
                    Order order = orderService.get(article.getOrderId()) ;
                    BeanUtils.copyProperties(article, history);
                    history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                    history.setUserId(order.getUserId());
                    history.setDeptId(order.getDepatId());
                    history.setId(null);
                    history.setArtId(article.getId());
                    history.setEditDesc(IConst.article_change_invoice_add);
                    history.setCreator(user.getId());
                    history.setCreateTime(new Date());
//                  满足三个条件税金就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置税金，3、税金有变动
                    String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
                    Double alterTax = tax - article.getTaxes();
                    if(!(curMonth.equals(issueMonth))
                            && Math.abs(alterTax)>0.01){
                        article.setAlterFlag(IConst.article_alter_flag_true);
                        history.setAlterTax(alterTax);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                    }

                    Double profit;
                    if(article.getIncomeStates()==IConst.FEE_STATE_FINISH){
                        profit =article.getIncomeAmount()-article.getOutgoAmount()-tax-article.getRefundAmount()-article.getOtherPay() ;
                    }else{
                        profit =article.getSaleAmount()-article.getOutgoAmount()-tax-article.getRefundAmount()-article.getOtherPay() ;
                    }
                    if(IConst.article_alter_flag_true.equals(history.getAlterLabel())){
                        Double alterProfit = profit - article.getProfit();
                        history.setAlterProfit(alterProfit);
                    }
                    historyList.add(history);

                    article.setTaxType(dict.getId());
                    article.setTaxes(tax);
                    article.setProfit(profit);
                    article.setUpdateUserId(user.getId());
                    articleList.add(article);
                }
            }

//            try{
                invoiceMapper.updateArticleInfo(articleList);
//            }catch (Exception e){
//                throw  new QinFeiException(1002,"更新稿件税金及利润出错！");
//            }
            try{
                //批量插入关系表
                if(historyList!=null && historyList.size()>0){
                    int length = historyList.size() ;
                    int subLength = 100 ;
                    // 计算需要插入的次数，100条插入一次；
                    int insertTimes = length % subLength == 0 ? length / subLength : length / subLength + 1;
                    for(int i=0;i<insertTimes;i++){
                        List<ArticleHistory> insertData = new ArrayList<>();
                        // 计算起始位置，且j的最大值应不能超过数据的总数；
                        for (int j = i * subLength; j < (i + 1) * subLength && j < length; j++) {
                            insertData.add(historyList.get(j));
                        }
                        articleHistoryMapper.saveBatch(insertData);
                    }
                }
            }catch (Exception e){
                throw new QinFeiException(1002,"记录稿件更改前信息失败！");
            }
        }

    }
    @Override
    @Transactional
    public void delById(Invoice entity) {
        User user = AppUtil.getUser();
        if(user==null){
            throw new QinFeiException(1002,"未找到登录信息，请刷新后重试！");
        }
        if(invoiceMapper.countByInvoiceIdAndCommissionStates(entity.getId())>0){
            throw new QinFeiException(1002,"该开票流程关联的稿件有已提成的稿件，无法删除，请联系财务先取消登记提成！") ;
        }
        //还原稿件开票状态,找到开票中的稿件
        List<Article> list = invoiceMapper.queryArticleByIdAndState(entity.getId(),IConst.FEE_STATE_PROCESS) ;
        if(list==null||list.size()==0){
            throw new QinFeiException(1002,"未查询到关联的开票中的稿件：开票id="+entity.getId()) ;
        }

        List<ArticleHistory> historyList = new ArrayList<>();
        String curMonth = DateUtils.getYearAndMonthStr2(new Date()) ;//当前年月
        for(Article article:list){
            Order order = orderService.get(article.getOrderId()) ;
            ArticleHistory history = new ArticleHistory();
            BeanUtils.copyProperties(article, history);
            history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
            history.setUserId(order.getUserId());
            history.setDeptId(order.getDepatId());
            history.setId(null);
            history.setArtId(article.getId());
            history.setEditDesc(IConst.article_change_invoice_del);
            history.setCreator(user.getId());
            history.setCreateTime(new Date());

//          满足三个条件税金就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置税金，3、税金有变动
            String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
            Double alterTax = 0 - article.getTaxes();
            if(!(curMonth.equals(issueMonth))
                    && Math.abs(alterTax)>0.01){
                article.setAlterFlag(IConst.article_alter_flag_true);
                history.setAlterTax(alterTax);
                history.setAlterLabel(IConst.article_alter_flag_true);
            }

            Double profit;
            //删除后没有税金了，计算利润时不减税金
            if(article.getIncomeStates()==IConst.FEE_STATE_FINISH){
                profit =article.getIncomeAmount()-article.getOutgoAmount()-article.getRefundAmount()-article.getOtherPay() ;
            }else{
                profit =article.getSaleAmount()-article.getOutgoAmount()-article.getRefundAmount()-article.getOtherPay() ;
            }
            if(IConst.article_alter_flag_true.equals(history.getAlterLabel())){
                Double alterProfit = profit - article.getProfit();
                history.setAlterProfit(alterProfit);
            }
            historyList.add(history);

            article.setTaxType(0);
            article.setTaxes(0D);
            article.setProfit(profit);
            article.setInvoiceStates(IConst.FEE_STATE_SAVE);
            article.setUpdateUserId(user.getId());
        }
        try{
            //批量插入关系表
            if(historyList!=null && historyList.size()>0){
                int length = historyList.size() ;
                int subLength = 100 ;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = length % subLength == 0 ? length / subLength : length / subLength + 1;
                for(int i=0;i<insertTimes;i++){
                    List<ArticleHistory> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < length; j++) {
                        insertData.add(historyList.get(j));
                    }
                    articleHistoryMapper.saveBatch(insertData);
                }
            }
        }catch (Exception e){
            throw new QinFeiException(1002,"记录稿件更改前信息失败！");
        }
        try{
            invoiceMapper.updateArticleInfo(list);
        }catch (Exception e){
            throw new QinFeiException(1002,"还原稿件税金信息失败，开票id="+entity.getId());
        }

        try{
            //删除开票和稿件关系
            invoiceMapper.delInvoiceArticle(entity.getId());
        }catch (Exception e){
            throw new QinFeiException(1002,"删除稿件和开票关系表失败，开票id="+entity.getId()) ;
        }
        try{
            //删除开票信息
            entity.setState(IConst.STATE_DELETE);
            invoiceMapper.update(entity);
        }catch (Exception e){
            throw new QinFeiException(1002,"更新开票信息失败，开票id="+entity.getId());
        }
        //处理待办
        finishItem(entity);
    }
    //待办变已办
    private void finishItem(Invoice entity){
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
        List<Map> list = invoiceMapper.listPgForSelectArticle(map);
        return new PageInfo<>(list);
    }

    @Override
    public Map listPgForSelectArticleSum(Map map) {
        return invoiceMapper.listPgForSelectArticleSum(map);
    }

    /**
     * 选择需要开票的供应商，存入关系表，并更改稿件开票状态为2，开票状态默认0，提交开票申请为2，已开票为1
     * @param map
     * @return
     */
    @Override
    @Transactional
    public Invoice saveStepOne(Map map) {
        String articleIds = "";
        Set<Integer> set = new HashSet();

        if(map.get("checkState")!=null){
            //如果选择了全部稿件，则先按条件查询出所有稿件
            List<Map> list = invoiceMapper.listPgForSelectArticle(map);
            if(list.size()>0){
                for (Map map1:list) {
                    articleIds = articleIds+map1.get("id")+",";
                }
            }else{
                throw new QinFeiException(1002,"当前条件下没有满足开票条件的稿件") ;
            }
        }else{
            articleIds = (String)map.get("articleIdsSec");
        }

        if(articleIds.indexOf(",")>-1){
            String[] ids = articleIds.split(",") ;
            for(int i=0;i<ids.length;i++){
                if(ids[i]!=null){
                    set.add(Integer.parseInt(ids[i]));
                }
            }
        }else{
            Integer id = Integer.parseInt(articleIds) ;
            set.add(id) ;
        }
        if(set == null || set.size() == 0){
            throw new QinFeiException(1002, "获取的稿件id不正确，获取的id字符串="+articleIds) ;
        }

        synchronized (this){
            //先判断稿件状态，有已开票的稿件就不允许再发起开票流程了
            Integer count = invoiceMapper.listByIdsAndInvoiceStates(set) ;
            if(count>0){
                throw new QinFeiException(1002,"选择的稿件列表中有已经申请开票的稿件，请刷新后重试！") ;
            }
            User user = AppUtil.getUser() ;
            Invoice entity  = new Invoice() ;
            try{
                Integer custId = Integer.valueOf((String)map.get("custId"));
                entity.setCustId(custId);
                entity.setCustCompanyId(Integer.valueOf((String)map.get("custCompanyId")));
                entity.setCustName((String)map.get("custName"));
                entity.setCustCompanyName((String)map.get("custCompanyName"));
                CrmCompanyUser companyUser = crmCompanyUserService.getById(custId);
                CrmCompany crmCompany = crmCompanyService.getById(companyUser.getCompanyId());
                entity.setInvoiceType(companyUser.getInvoiceType()==null?IConst.INVOICE_TYPE_SPECIAL:companyUser.getInvoiceType());//默认专票
                entity.setTaxType(companyUser.getTaxType());
                if(IConst.CUST_TYPE_COMPANY.equals(crmCompany.getType())){
                    entity.setTitle(crmCompany.getName());
                }else{
                    entity.setTitle(companyUser.getName());
                }
                entity.setTaxCode(companyUser.getTaxCode());
                entity.setBankNo(companyUser.getBankNo());
                entity.setBankName(companyUser.getBankName());
                entity.setAddress(companyUser.getAddress());
                // 电话号码不加密保存
                String phone = companyUser.getPhone();
                if(StringUtils.isNotBlank(phone)){
                    entity.setPhone(EncryptUtils.decrypt(phone));
                }else{
                    entity.setPhone(phone);
                }
            }catch (Exception e){
                throw new QinFeiException(1002,"获取客户或联系人信息出错！") ;
            }
            entity.setApplyId(user.getId());
            entity.setApplyName(user.getName());
            entity.setDeptId(user.getDeptId());
            entity.setDeptName(user.getDeptName());
            entity.setApplyTime(new Date());

            entity.setCompanyCode(user.getCompanyCode());

            try{
                //生成KP2018110001的编号，前六位是年月，后四位累加
                entity.setCode(IConst.INVOICE_CODE+ CodeUtil.getMonthStr()+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.INVOICE_CODE),4)) ;
            }catch (QinFeiException e){
                throw new QinFeiException(1002,"生成开票编号出错，请刷新后重试！") ;
            }
            try{
                invoiceMapper.insert(entity) ;
            }catch (QinFeiException e){
                throw new QinFeiException(1002,"插入开票表失败，请刷新后重试！")  ;
            }
            //更新关系表和稿件状态
            insertInvoiceArticle(entity.getId(),set) ;

            return entity ;
        }
    }

    private void insertInvoiceArticle(Integer invoiceId, Set<Integer> set) {
        List<Map<String,Integer>> relationList = new ArrayList<>() ;//关系表list
        try{
            Iterator<Integer> iterator =set.iterator() ;
            while(iterator.hasNext()){
                Integer articleId = iterator.next() ;
                Map<String,Integer> relationMap = new HashMap<>() ;//关系表详情map
                relationMap.put("articleId",articleId) ;
                relationMap.put("invoiceId",invoiceId) ;
                relationList.add(relationMap) ;
            }
            if(relationList!=null && relationList.size()>0){
                int size = relationList.size() ;
                int subLength = 100 ;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for(int i=0;i<insertTimes;i++){
                    List<Map<String,Integer>> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(relationList.get(j));
                    }
                    invoiceMapper.insertInvoiceArticleBatch(insertData);
                }
            }
        }catch (Exception e){
            throw new QinFeiException(1002,"维护开票和稿件关系表失败，开票id="+invoiceId) ;
        }

        List<Integer> list ;//稿件id集合
        Map<String,Object> map = new HashMap<>() ;//稿件开票信息map
        try{
            //稿件开票状态变成2
            list = new ArrayList<>(set);
            map.put("list",list) ;
            map.put("state",IConst.FEE_STATE_PROCESS) ;
            invoiceMapper.changeInvoiceState(map);
        }catch (Exception e){
            throw new QinFeiException(1002,"维护稿件开票字段失败，开票id="+invoiceId) ;
        }
    }
    @Override
    public PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = invoiceMapper.listPgForSelectedArticle(map);
        return new PageInfo<>(list);
    }

    @Override
    public Double getSumSaleAmountById(Integer id) {
        return invoiceMapper.getSumSaleAmountById(id);
    }

    /**
     * 根据稿件编号查询发票id
     * @param articleId
     * @return
     */
    @Override
    public Integer queryInvoiceId(Integer articleId){
        Integer InvoiceId = invoiceMapper.queryInvoiceId(articleId);
        return InvoiceId!=null?InvoiceId:-2;
    }

    @Override
    @Transactional
    public Invoice confirm(Invoice entity) {
        User user = AppUtil.getUser() ;
        entity.setState(IConst.STATE_CWKP);

        //处理待办
        finishItem(entity);
        //增加待办
        Items item = addItem(entity,user) ;
        if(item!=null){
            entity.setItemId(item.getId());
        }
        invoiceMapper.update(entity);
        return entity;
    }

    //增加待办
    private Items addItem(Invoice entity, User user){
        User cw = userService.getCWZLInfo(user.getCompanyCode()) ;
        if(cw!=null){
            Items items = new Items();
            items.setItemName(entity.getTitle()+"-开票等待处理");
            items.setItemContent("您有新的开票申请需要处理");
            items.setWorkType("开票申请");
            items.setInitiatorWorker(user.getId());
            items.setInitiatorDept(user.getDeptId());
            items.setStartTime(new Date());
            Calendar ca = Calendar.getInstance();
            ca.add(Calendar.DATE, 3);// 增加的天数3，
            items.setEndTime(ca.getTime());
            items.setTransactionAddress("/fee/queryInvoice?flag=5&id="+entity.getId());
            items.setFinishAddress("/fee/queryInvoice?flag=1&id="+entity.getId());
            items.setAcceptWorker(cw.getId()) ;
            items.setAcceptDept(cw.getDeptId());
            items.setTransactionState(Const.ITEM_W);
            itemsService.addItems(items);
            return items ;
        }else{
            return null ;
        }
    }

    @Override
    @Transactional
    public Invoice invoice(Invoice entity,String desc) {
        try{
            entity.setState(IConst.STATE_FINISH);
            invoiceMapper.update(entity);
            String[] taskIds = new String[]{entity.getTaskId()};
            processService.approveProcess(taskIds, desc, true);
        }catch (Exception e){
            throw new QinFeiException(1002,"更新开票信息失败，开票id="+entity.getId()) ;
        }

        //后处理稿件表的状态
        List<Article> articleList = invoiceMapper.queryArticleByIdAndState(entity.getId(),IConst.FEE_STATE_PROCESS) ;
        if(!CollectionUtils.isEmpty(articleList)){
            Map<String,Object> map = new HashMap<>() ;
            map.put("list",articleList) ;
            map.put("state",IConst.FEE_STATE_FINISH) ;
            invoiceMapper.changeInvoiceState2(map);
        }

//            //处理待办
//            finishItem(entity);
        return entity;
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
    public void CWReject(Invoice entity){
        //待办已由审批流处理，这里无需重复处理
        //1、待办变已办
//        finishItem(entity) ;

        //2、增加待办
//        User user = AppUtil.getUser() ;
//        Items item = addApplyItem(entity,user) ;
//        entity.setItemId(item.getId());
        //3、订单状态修改
        invoiceMapper.update(entity);
    }

    /**
     * 废弃
     * state=1或state=12时财务撤回，需要变更以下内容
     * 1、关联的稿件请款状态修改为开票中2
     * 2、完成待办
     * 3、增加新的待办
     * 4、请款订单状态修改为-1
     * @param entity
     */
    @Override
    @Transactional
    public Boolean CWReturn(Invoice entity){
        //1、处理稿件表的状态
        List<Integer> ids = invoiceMapper.queryArticleIdsByInvoiceIdAndState(entity.getId(),IConst.FEE_STATE_FINISH) ;
//        选中的稿件有提成信息，不能撤回；
        if(ids!=null && ids.size()>0){
            if(articleMapper.getCommissionByArticleIds(ids)>0){
                return false;
            }
            try{
                Map<String,Object> map = new HashMap<>() ;
                map.put("list",ids) ;
                map.put("state",IConst.FEE_STATE_PROCESS) ;
                invoiceMapper.changeInvoiceState(map);
            }catch (Exception e){
                throw new QinFeiException(1002,"处理稿件开票状态字段失败，开票id="+entity.getId());
            }
        }else{
            throw new QinFeiException(1002,"没有查询到关联的已开票稿件，开票id="+entity.getId());
        }

        //2、待办变已办
        finishItem(entity) ;
        //3、增加待办
        User user = AppUtil.getUser() ;
        Items item = addApplyItem(entity,user) ;
        entity.setItemId(item.getId());
        //4、更新订单
        invoiceMapper.update(entity);
        return true ;
    }

    //增加待办
    private Items addApplyItem(Invoice entity, User user){
        Items items = new Items();
        items.setItemName(entity.getTitle()+"-开票撤回等待处理");
        items.setItemContent("您有新的开票撤回需要处理");
        items.setWorkType("开票撤回");
        User mediaUser = userService.getById(entity.getApplyId()) ;
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryInvoice?flag=0&id="+entity.getId());
        items.setFinishAddress("/fee/queryInvoice?flag=1&id="+entity.getId());
        items.setAcceptWorker(mediaUser.getId()) ;
        items.setAcceptDept(mediaUser.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items ;
    }

    @Override
    public Map querySumAmount(Integer invoiceId){
        return invoiceMapper.querySumAmount(invoiceId) ;
    }

    @Override
    public List<Map> exportInvoice(Map map, OutputStream outputStream) {
        if(map.get("deptId") != null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = invoiceMapper.listPg(map);
        String[] heads = {"申请编号", "发票编号", "申请人", "所在部门", "请款日期", "客户公司名称", "发票类型", "开票公司名称", "税号", "抬头",
                "税点", "税额", "开票日期", "实际开票金额", "价税合计"};
        String[] fields = {"code", "no", "applyName", "deptName", "applyTime", "custCompanyName", "custName", "type", "title", "taxCode", "taxType", "taxPoint", "taxAmount",
                "invoiceTime", "invoiceAmount", "amount"};
        ExcelUtil.exportExcel("进账列表", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("taxAmount".equals(field) || "invoiceAmount".equals(field) || "amount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else if ("applyTime".equals(field) || "invoiceTime".equals(field)) {
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
    public List<User> getTaxType(String taxType){
        try{
            User user= AppUtil.getUser();
            Map map = new HashMap();
            map.put("companyCode",user.getCompanyCode());
            map.put("taxType",taxType);
            List<User> list= taxUserMapper.taxAssistant(map);
            return list;
        }catch (Exception e){
            throw new QinFeiException(1002,"处理稿件开票状态字段失败");
        }

    }
}
