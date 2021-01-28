package com.qinfei.qferp.service.impl.fee;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleHistory;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.fee.Commission;
import com.qinfei.qferp.entity.fee.CommissionProcess;
import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.excelListener.ArticleExcelCommissionRegister;
import com.qinfei.qferp.excelListener.ArticleExcelCommissionUnRegister;
import com.qinfei.qferp.mapper.biz.ArticleHistoryMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapper;
import com.qinfei.qferp.mapper.fee.CommissionMapper;
import com.qinfei.qferp.mapper.fee.CommissionProcessMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.biz.IArticleService;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.service.fee.ICommissionService;
import com.qinfei.qferp.service.media1.IMediaPlateService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class CommissionService implements ICommissionService {
    @Autowired
    private CommissionMapper commissionMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IUserService userService;
    @Autowired
    private IItemsService itemsService;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private CommissionProcessMapper commissionProcessMapper;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IMediaPlateService mediaPlateService;
    @Autowired
    private ArticleHistoryMapper articleHistoryMapper;

    @Override
    public PageInfo<Map> listPg(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = commissionMapper.listPg(map);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map> listFeeCommissionByPage(int pageNum, int pageSize, Map map) {
        handleMap(map);// 处理请求参数-权限
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = commissionMapper.listFeeCommissionByPage(map);
        return new PageInfo<>(list);
    }

    /**
     * 处理请求参数-权限
     */
    private void handleMap(Map<String, Object> map) {
        User user = AppUtil.getUser();
        if (!IConst.ROLE_TYPE_JT.equals(user.getCompanyCode())) {
            map.put("companyCode", user.getCompanyCode());//当用户为业务部时，仅能查询当前公司下，该领导人部门下面的信息
        }
    }

    @Override
    public Commission getById(Integer id) {
        return commissionMapper.getById(id);
    }

    @Override
    public Commission add(Commission entity) {
        commissionMapper.insert(entity);
        return entity;
    }

    @Override
    public Commission edit(Commission entity) {
        commissionMapper.update(entity);
        return entity;
    }

    @Override
    public Commission update(Commission entity) {
        commissionMapper.update(entity);
        return entity;
    }

    @Override
    public void del(Integer id) {
        commissionMapper.del(id);
    }

    @Override
    public List<Commission> checkCommissionInfo(Integer userId) {
        return commissionMapper.queryCommissionByUserAndState(userId, getLastMonthInfo().get(0), getLastMonthInfo().get(1));
    }

    @Override
    public Commission initCommissionInfo(Integer userId) {
        Commission entity = null;
        List<Commission> list = commissionMapper.queryCommissionByUser(userId, getLastMonthInfo().get(0), getLastMonthInfo().get(1));
        if (list != null && list.size() > 0) {
            entity = list.get(0);
        } else {
            User user = userMapper.getById(userId);
            entity = new Commission();
            entity.setUserId(userId);
            entity.setName(user.getName());
            entity.setDeptId(user.getDeptId());
            entity.setDeptName(user.getDeptName());
            entity.setYear(getLastMonthInfo().get(0));
            entity.setMonth(getLastMonthInfo().get(1));
            entity.setCompanyCode(AppUtil.getUser().getDept().getCompanyCode());
            commissionMapper.insert(entity);
        }
        return entity;
    }

    private List<Integer> getLastMonthInfo() {
        List<Integer> list = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);    //得到前一个月
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH) + 1;
        list.add(year);
        list.add(month);
        return list;
    }

    /**
     * 计算提成，目前的规则是：
     *
     * @param ids
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public Commission batchRegister(String ids, Integer userId) {
        User user = AppUtil.getUser();
        Map map = new HashMap();
        Integer year = getLastMonthInfo().get(0);
        Integer month = getLastMonthInfo().get(1);
        //        以下变量是金额增量
        Double sale = 0.0;
        Double income = 0.0;
        Double outgo = 0.0;
        Double taxes = 0.0;
        Double refund = 0.0;
        Double otherPay = 0.0;
        Double profit = 0.0;
        Double comm = 0.0;
        Commission entity = commissionMapper.queryCommissionByUser(userId, year, month).get(0);
        if (entity == null) {
            throw new QinFeiException(1002, "没有获取到该用户" + year + "年" + month + "月的提成信息！");
        }
        List<Article> list = new ArrayList<>();
        List<ArticleHistory> historyList = new ArrayList<>();
        List<CommissionProcess> processList = new ArrayList();
        List<MediaPlate> plateList = mediaPlateService.queryMediaPlate();
        if (plateList == null || plateList.size() == 0) {
            throw new QinFeiException(1002, "获取媒体板块失败！");
        }
        User yw = userService.getById(userId);
        String companyCode = yw.getCompanyCode();
        if (ids.indexOf(",") > -1) {
            String[] idss = ids.split(",");
            for (String s : idss) {
                Article article = articleMapper.get(Article.class, Integer.parseInt(s));
                Order order = orderService.get(article.getOrderId());
                //稿件中的业务员id必须和订单表中用户id一致,如果不一致就不处理了，避免异常情况导致的登记提成错误
                if (userId.equals(order.getUserId())) {
                    ArticleHistory history = new ArticleHistory();
                    BeanUtils.copyProperties(article, history);
                    history.setCompanyCode(companyCode);
                    history.setUserId(order.getUserId());
                    history.setDeptId(order.getDepatId());
                    history.setId(null);
                    history.setArtId(article.getId());
                    history.setEditDesc(IConst.article_change_comm_add);
                    history.setCreator(user.getId());
                    history.setCreateTime(new Date());
                    historyList.add(history);

                    Double percent = 0D;
                    for (MediaPlate plate : plateList) {
                        if (article.getMediaTypeId().equals(plate.getId())) {
                            percent = plate.getPercent();
                        }
                    }

                    Double amount = new BigDecimal(article.getIncomeAmount()).
                            subtract(new BigDecimal(article.getOutgoAmount())).
                            subtract(new BigDecimal(article.getTaxes())).
                            subtract(new BigDecimal(article.getRefundAmount())).
                            subtract(new BigDecimal(article.getOtherPay())).
                            setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                    Double tempComm = new BigDecimal(amount).
                            multiply(new BigDecimal(percent.toString()).
                                    multiply(new BigDecimal(0.01))).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    article.setProfit(amount);
                    article.setCommission(tempComm);
                    list.add(article);

                    sale = new BigDecimal(sale).
                            add(new BigDecimal(article.getSaleAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    income = new BigDecimal(income).
                            add(new BigDecimal(article.getIncomeAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    outgo = new BigDecimal(outgo).
                            add(new BigDecimal(article.getOutgoAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    taxes = new BigDecimal(taxes).
                            add(new BigDecimal(article.getTaxes())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    refund = new BigDecimal(refund).
                            add(new BigDecimal(article.getRefundAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    otherPay = new BigDecimal(otherPay).
                            add(new BigDecimal(article.getOtherPay())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    profit = new BigDecimal(profit).
                            add(new BigDecimal(amount)).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    comm = new BigDecimal(comm).
                            add(new BigDecimal(tempComm)).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                    //                存过程表
                    CommissionProcess process = new CommissionProcess();
                    process.setUserId(userId);
                    process.setArticleId(article.getId());
                    process.setYear(entity.getYear());
                    process.setMonth(entity.getMonth());
                    process.setIncome(article.getIncomeAmount());
                    process.setOutgo(article.getOutgoAmount());
                    process.setTaxes(article.getTaxes());
                    process.setRefund(article.getRefundAmount());
                    process.setOtherPay(article.getOtherPay());
                    process.setProfit(article.getProfit());
                    process.setPercent(percent);
                    process.setComm(article.getCommission());
                    process.setCreateUserId(AppUtil.getUser().getId());
                    process.setCreateTime(new Date());
                    processList.add(process);
                }
            }
        } else {
            Integer id = Integer.parseInt(ids);
            Article article = articleMapper.get(Article.class, id);
            Order order = orderService.get(article.getOrderId());
            //稿件中的业务员id必须和订单表中用户id一致,如果不一致就不处理了，避免异常情况导致的登记提成错误
            if (userId.equals(order.getUserId())) {
                ArticleHistory history = new ArticleHistory();
                BeanUtils.copyProperties(article, history);
                history.setCompanyCode(companyCode);
                history.setUserId(order.getUserId());
                history.setDeptId(order.getDepatId());
                history.setId(null);
                history.setArtId(article.getId());
                history.setEditDesc(IConst.article_change_comm_add);
                history.setCreator(user.getId());
                history.setCreateTime(new Date());
                historyList.add(history);

                Double percent = 0D;
                for (MediaPlate plate : plateList) {
                    if (article.getMediaTypeId().equals(plate.getId())) {
                        percent = plate.getPercent();
                    }
                }
                Double amount = new BigDecimal(article.getIncomeAmount()).
                        subtract(new BigDecimal(article.getOutgoAmount())).
                        subtract(new BigDecimal(article.getTaxes())).
                        subtract(new BigDecimal(article.getRefundAmount())).
                        subtract(new BigDecimal(article.getOtherPay())).
                        setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                //先算提成，再算利润，避免小数位丢失的问题
                Double tempComm = new BigDecimal(amount).
                        multiply(new BigDecimal(percent.toString()).
                                multiply(new BigDecimal(0.01))).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                article.setProfit(amount);
                article.setCommission(tempComm);
                list.add(article);

                sale = new BigDecimal(sale).
                        add(new BigDecimal(article.getSaleAmount())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                income = new BigDecimal(income).
                        add(new BigDecimal(article.getIncomeAmount())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                outgo = new BigDecimal(outgo).
                        add(new BigDecimal(article.getOutgoAmount())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                taxes = new BigDecimal(taxes).
                        add(new BigDecimal(article.getTaxes())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                refund = new BigDecimal(refund).
                        add(new BigDecimal(article.getRefundAmount())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                otherPay = new BigDecimal(otherPay).
                        add(new BigDecimal(article.getOtherPay())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                profit = new BigDecimal(profit).
                        add(new BigDecimal(amount)).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                comm = new BigDecimal(comm).
                        add(new BigDecimal(tempComm)).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                //                存过程表
                CommissionProcess process = new CommissionProcess();
                process.setUserId(userId);
                process.setArticleId(article.getId());
                process.setYear(entity.getYear());
                process.setMonth(entity.getMonth());
                process.setIncome(article.getIncomeAmount());
                process.setOutgo(article.getOutgoAmount());
                process.setTaxes(article.getTaxes());
                process.setRefund(article.getRefundAmount());
                process.setOtherPay(article.getOtherPay());
                process.setProfit(article.getProfit());
                process.setPercent(percent);
                process.setComm(article.getCommission());
                process.setCreateUserId(AppUtil.getUser().getId());
                process.setCreateTime(new Date());
                processList.add(process);
            }
        }
        if (list == null || list.size() == 0) {
            throw new QinFeiException(1002, "没有选中稿件或选中的稿件不是当前业务员的稿件，请核实后重试！");
        }

        try {
            //批量插入关系表
            if (historyList != null && historyList.size() > 0) {
                int length = historyList.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = length % subLength == 0 ? length / subLength : length / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    List<ArticleHistory> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < length; j++) {
                        insertData.add(historyList.get(j));
                    }
                    articleHistoryMapper.saveBatch(insertData);
                }
            }
        } catch (Exception e) {
            throw new QinFeiException(1002, "记录稿件更改前信息失败！");
        }

        try {
            //批量修改稿件表
            map.put("year", year);
            map.put("month", month);
            map.put("userId", userId);
            map.put("commissionState", IConst.FEE_STATE_PROCESS);
            map.put("list", list);
            commissionMapper.updateBatch(map);
        } catch (Exception e) {
            throw new QinFeiException(1002, "更新稿件提成状态失败！");
        }
        try {
            //处理提成过程表
            if (processList != null && processList.size() > 0) {
                int size = processList.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    List<CommissionProcess> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(processList.get(j));
                    }
                    commissionProcessMapper.insertBatch(insertData);
                }
            }
        } catch (Exception e) {
            throw new QinFeiException(1002, "插入提成过程表数据失败！");
        }
        try {
            //处理提成表
            dealCommInfo(sale, income, outgo, taxes, refund, otherPay, profit, comm, entity);
        } catch (Exception e) {
            throw new QinFeiException(1002, "处理提成表数据失败！");
        }
        return entity;
    }

    @Override
    @Transactional
    public Commission batchRegisterOff(String ids, Integer userId) {
        User user = AppUtil.getUser();
        List<Integer> list = new ArrayList();
        List<ArticleHistory> historyList = new ArrayList<>();
        //        以下变量是金额增量
        Double sale = 0.0;
        Double income = 0.0;
        Double outgo = 0.0;
        Double taxes = 0.0;
        Double refund = 0.0;
        Double otherPay = 0.0;
        Double profit = 0.0;
        Double comm = 0.0;
        Integer year = getLastMonthInfo().get(0);
        Integer month = getLastMonthInfo().get(1);
        Commission entity = commissionMapper.queryCommissionByUser(userId, year, month).get(0);
        if (entity == null) {
            throw new QinFeiException(1002, "没有获取到该用户" + year + "年" + month + "月的提成信息！");
        }
        User yw = userService.getById(userId);
        String companyCode = yw.getCompanyCode();
        if (ids.indexOf(",") > -1) {
            String[] idss = ids.split(",");
            for (String s : idss) {
                Article article = articleMapper.get(Article.class, Integer.parseInt(s));
                Order order = orderService.get(article.getOrderId());
                //稿件中的业务员id必须和订单表中用户id一致,如果不一致就不处理了，避免异常情况导致的登记提成错误
                if (userId.equals(order.getUserId())) {
                    ArticleHistory history = new ArticleHistory();
                    BeanUtils.copyProperties(article, history);
                    history.setCompanyCode(companyCode);
                    history.setUserId(order.getUserId());
                    history.setDeptId(order.getDepatId());
                    history.setId(null);
                    history.setArtId(article.getId());
                    history.setEditDesc(IConst.article_change_comm_del);
                    history.setCreator(user.getId());
                    history.setCreateTime(new Date());
                    historyList.add(history);

                    sale = new BigDecimal(sale).
                            subtract(new BigDecimal(article.getSaleAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    income = new BigDecimal(income).
                            subtract(new BigDecimal(article.getIncomeAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    outgo = new BigDecimal(outgo).
                            subtract(new BigDecimal(article.getOutgoAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    taxes = new BigDecimal(taxes).
                            subtract(new BigDecimal(article.getTaxes())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    refund = new BigDecimal(refund).
                            subtract(new BigDecimal(article.getRefundAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    otherPay = new BigDecimal(otherPay).
                            subtract(new BigDecimal(article.getOtherPay())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    profit = new BigDecimal(profit).
                            subtract(new BigDecimal(article.getProfit())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    comm = new BigDecimal(comm).
                            subtract(new BigDecimal(article.getCommission())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    list.add(article.getId());
                }
            }
        } else {
            Integer id = Integer.parseInt(ids);
            Article article = articleMapper.get(Article.class, id);
            Order order = orderService.get(article.getOrderId());
            //稿件中的业务员id必须和订单表中用户id一致,如果不一致就不处理了，避免异常情况导致的登记提成错误
            if (userId.equals(order.getUserId())) {
                ArticleHistory history = new ArticleHistory();
                BeanUtils.copyProperties(article, history);
                history.setCompanyCode(companyCode);
                history.setUserId(order.getUserId());
                history.setDeptId(order.getDepatId());
                history.setId(null);
                history.setArtId(article.getId());
                history.setEditDesc(IConst.article_change_comm_del);
                history.setCreator(user.getId());
                history.setCreateTime(new Date());
                historyList.add(history);

                sale = new BigDecimal(sale).
                        subtract(new BigDecimal(article.getSaleAmount())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                income = new BigDecimal(income).
                        subtract(new BigDecimal(article.getIncomeAmount())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                outgo = new BigDecimal(outgo).
                        subtract(new BigDecimal(article.getOutgoAmount())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                taxes = new BigDecimal(taxes).
                        subtract(new BigDecimal(article.getTaxes())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                refund = new BigDecimal(refund).
                        subtract(new BigDecimal(article.getRefundAmount())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                otherPay = new BigDecimal(otherPay).
                        subtract(new BigDecimal(article.getOtherPay())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                profit = new BigDecimal(profit).
                        subtract(new BigDecimal(article.getProfit())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                comm = new BigDecimal(comm).
                        subtract(new BigDecimal(article.getCommission())).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                list.add(article.getId());
            }
        }
        if (list == null || list.size() == 0) {
            throw new QinFeiException(1002, "没有选中稿件或选中的稿件不是当前业务员的稿件，请核实后重试！");
        }
        try {
            //批量插入关系表
            if (historyList != null && historyList.size() > 0) {
                int length = historyList.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = length % subLength == 0 ? length / subLength : length / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    List<ArticleHistory> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < length; j++) {
                        insertData.add(historyList.get(j));
                    }
                    articleHistoryMapper.saveBatch(insertData);
                }
            }
        } catch (Exception e) {
            throw new QinFeiException(1002, "记录稿件更改前信息失败！");
        }
        try {
            //批量修改稿件表
            commissionMapper.initArticleCommBatch(list);
        } catch (Exception e) {
            throw new QinFeiException(1002, "还原稿件提成状态失败！");
        }
        try {
            //处理提成过程表
            Map map = new HashMap();
            map.put("list", list);
            map.put("userId", AppUtil.getUser().getId());
            commissionProcessMapper.delByArticleIdBatch(map);
        } catch (Exception e) {
            throw new QinFeiException(1002, "处理提成过程表失败！");
        }
        try {
            //处理提成表
            dealCommInfo(sale, income, outgo, taxes, refund, otherPay, profit, comm, entity);
        } catch (Exception e) {
            throw new QinFeiException(1002, "处理提成表数据失败！");
        }
        return entity;
    }

    private void dealCommInfo(Double sale, Double income, Double outgo, Double taxes, Double refund, Double otherPay, Double profit, Double comm, Commission entity) {
        entity.setSale(new BigDecimal(entity.getSale()).
                add(new BigDecimal(sale)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setIncome(new BigDecimal(entity.getIncome()).
                add(new BigDecimal(income)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setOutgo(new BigDecimal(entity.getOutgo()).
                add(new BigDecimal(outgo)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setTaxExpense(new BigDecimal(entity.getTaxExpense()).
                add(new BigDecimal(taxes)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setRefund(new BigDecimal(entity.getRefund()).
                add(new BigDecimal(refund)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setOtherExpense(new BigDecimal(entity.getOtherExpense()).
                add(new BigDecimal(otherPay)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setProfit(new BigDecimal(entity.getProfit()).
                add(new BigDecimal(profit)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setComm(new BigDecimal(entity.getComm()).
                add(new BigDecimal(comm)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        commissionMapper.update(entity);
    }

    /*    @Override
        public List<Map> exportUnRegister(Map map, OutputStream outputStream) {
            List<Map> list = commissionMapper.queryArticleByCommStates(map);
            String[] heads = {"媒体板块","业务员","媒介","客户名称","发布日期","媒体",
                    "标题","内外部","频道","电商商家","应收（报价）",
                    "回款金额","回款状态","成本（请款）","请款状态","税金","开票状态","退款","退款状态","其他支出","其他支出状态", "利润","提成",
                    "进账编号","回款明细","账户名称","进账人","进账时间","进账金额","请款编号","请款总金额"
                    };
            String[] fields = {"media_type_name","user_name","media_user_name","company_name","issued_date","media_name",
                    "title","inner_outer","channel","electricity_businesses","sale_amount",
                     "income_amount","income_states","outgo_amount","outgo_states","taxes","invoice_states",
                    "refund_amount","refund_states","other_pay","other_pay_states", "profit", "commission",
                    "income_code","amountDetail","income_account","income_man","income_date","income_total_amount","outgo_code","outgo_total_amount"
                    };
            ExcelUtil.exportExcel("未登记提成稿件列表", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("issued_date".equals(field)||"incomeDate".equals(field)) {
                        cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                    } else if ("income_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未进账");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已进账");
                        } else{
                            cell.setCellValue("进行中");
                        }
                    } else if ("outgo_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未出账");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已出账");
                        } else{
                            cell.setCellValue("进行中");
                        }
                    } else if ("invoice_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未开票");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已开票");
                        } else{
                            cell.setCellValue("进行中");
                        }
                    } else if ("refund_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未退款");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已退款");
                        } else{
                            cell.setCellValue("进行中");
                        }
                    } else if ("other_pay_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未支出");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已支出");
                        } else{
                            cell.setCellValue("支出中");
                        }
                    } else if("income_amount".equals(field)||"outgo_amount".equals(field)||"taxes".equals(field)
                            ||"refund_amount".equals(field)||"other_pay".equals(field)||"profit".equals(field)
                            ||"commission".equals(field) || "sale_amount".equals(field)||"tradeAmount".equals(field)
                            ||"outgo_total_amount".equals(field)){
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }else{
                        cell.setCellValue(value.toString());
                    }
                }
            });
            return list;
        }*/
    @Override
    public List<ArticleExcelCommissionUnRegister> exportUnRegisterNew(Map map) {
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        if (roles != null && roles.size() > 0) {
            map.put("roleType", roles.get(0).getType());
            map.put("roleCode", roles.get(0).getCode());
            map.put("user", user);
            map.put("commissionStates", 0);
            map.put("companyCode", AppUtil.getUser().getCompanyCode());
        }
        List<Map> list = commissionMapper.queryArticleByCommStates(map);
        return doWithEasyExcelUnRegister(list);
    }

    private List<ArticleExcelCommissionUnRegister> doWithEasyExcelUnRegister(List<Map> list) {
        List<ArticleExcelCommissionUnRegister> excelList = new ArrayList<>();
        for (Map temp : list) {
            ArticleExcelCommissionUnRegister articleExcel = new ArticleExcelCommissionUnRegister();
            articleExcel.setMediaTypeName(MapUtils.getString(temp, "media_type_name"));
            articleExcel.setUserName(MapUtils.getString(temp, "user_name"));
            articleExcel.setMediaUserName(MapUtils.getString(temp, "media_user_name"));
            articleExcel.setCompanyName(MapUtils.getString(temp, "company_name"));
            articleExcel.setIssuedDate(ObjectUtils.isEmpty(temp.get("issued_date")) ? null : MapUtils.getString(temp, "issued_date").substring(0, 10));
            articleExcel.setMediaName(MapUtils.getString(temp, "media_name"));
            articleExcel.setTitle(MapUtils.getString(temp, "title"));
            articleExcel.setInnerOuter(MapUtils.getString(temp, "inner_outer"));
            articleExcel.setChannel(MapUtils.getString(temp, "channel"));
            articleExcel.setElectricityBusinesses(MapUtils.getString(temp, "electricity_businesses"));
            articleExcel.setSaleAmount(ObjectUtils.isEmpty(temp.get("sale_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("sale_amount"))));
            articleExcel.setIncomeAmount(ObjectUtils.isEmpty(temp.get("income_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("income_amount"))));
            String incomeStates = String.valueOf(temp.get("income_states"));
            if ("1".equals(String.valueOf(incomeStates))) {
                articleExcel.setIncomeStates("已回款");
            } else if ("2".equals(String.valueOf(incomeStates))) {
                articleExcel.setIncomeStates("回款中");
            } else {
                articleExcel.setIncomeStates("未回款");
            }
            articleExcel.setOutgoAmount(ObjectUtils.isEmpty(temp.get("outgo_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgo_amount"))));
            String outgoStates = String.valueOf(temp.get("outgo_states"));
            if ("1".equals(String.valueOf(outgoStates))) {
                articleExcel.setOutgoStates("已请款");
            } else if ("2".equals(String.valueOf(outgoStates))) {
                articleExcel.setOutgoStates("请款中");
            } else {
                articleExcel.setOutgoStates("未请款");
            }
            articleExcel.setTaxes(ObjectUtils.isEmpty(temp.get("taxes")) ? 0 : Double.parseDouble(String.valueOf(temp.get("taxes"))));
            String invoiceStates = String.valueOf(temp.get("invoice_states"));
            if ("1".equals(String.valueOf(invoiceStates))) {
                articleExcel.setInvoiceStates("已开票");
            } else if ("2".equals(String.valueOf(invoiceStates))) {
                articleExcel.setInvoiceStates("开票中");
            } else {
                articleExcel.setInvoiceStates("未开票");
            }
            articleExcel.setRefundAmount(ObjectUtils.isEmpty(temp.get("refund_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("refund_amount"))));
            String refundStates = String.valueOf(temp.get("refund_states"));
            if ("1".equals(String.valueOf(refundStates))) {
                articleExcel.setRefundStates("已退款");
            } else if ("2".equals(String.valueOf(refundStates))) {
                articleExcel.setRefundStates("退款中");
            } else {
                articleExcel.setRefundStates("未退款");
            }
            articleExcel.setOtherPay(ObjectUtils.isEmpty(temp.get("other_pay")) ? 0 : Double.parseDouble(String.valueOf(temp.get("other_pay"))));
            String otherPayStates = String.valueOf(temp.get("other_pay_states"));
            if ("1".equals(String.valueOf(otherPayStates))) {
                articleExcel.setOtherPayStates("已支出");
            } else if ("2".equals(String.valueOf(otherPayStates))) {
                articleExcel.setOtherPayStates("支出中");
            } else {
                articleExcel.setOtherPayStates("未支出");
            }
            articleExcel.setProfit(ObjectUtils.isEmpty(temp.get("profit")) ? 0 : Double.parseDouble(String.valueOf(temp.get("profit"))));
            articleExcel.setCommission(ObjectUtils.isEmpty(temp.get("commission")) ? 0 : Double.parseDouble(String.valueOf(temp.get("commission"))));
            articleExcel.setIncomeCode(MapUtils.getString(temp, "income_code"));
            articleExcel.setAmountDetail(MapUtils.getString(temp, "amountDetail"));
            articleExcel.setIncomeAccount(MapUtils.getString(temp, "income_account"));
            articleExcel.setIncomeMan(MapUtils.getString(temp, "income_man"));
            articleExcel.setIncomeDate(ObjectUtils.isEmpty(temp.get("income_date")) ? null : MapUtils.getString(temp, "income_date").substring(0, 10));
            articleExcel.setIncomeTotalAmount(ObjectUtils.isEmpty(temp.get("income_total_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("income_total_amount"))));
            articleExcel.setOutgoCode(MapUtils.getString(temp, "outgo_code"));
            articleExcel.setOutgoTotalAmount(ObjectUtils.isEmpty(temp.get("outgo_total_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgo_total_amount"))));
            excelList.add(articleExcel);
        }
        return excelList;
    }

    /*    @Override
        public List<Map> exportRegister(Map map, OutputStream outputStream) {
            handleQueryData(map);
            List<Map> list = commissionMapper.queryArticleByUserAndYearAndMonth(map);
            String[] heads = {"年","月","媒体板块","业务员","媒介","客户名称","发布日期","媒体",
                    "内外部","频道","电商商家","标题","应收（报价）",
                    "回款金额","回款状态","成本（请款）","请款状态", "税金","开票状态","退款", "退款状态","其他支出","其他支出状态", "利润","提成",
                    "进账编号","回款明细","账户名称","进账人","进账时间","进账总金额","请款编号","请款总金额",
                    };
            String[] fields = {"year", "month","media_type_name","user_name","media_user_name","company_name","issued_date","media_name",
                    "inner_outer","channel","electricity_businesses","title","sale_amount",
                     "income_amount","income_states","outgo_amount","outgo_states","taxes","invoice_states",
                    "refund_amount","refund_states","other_pay","other_pay_states", "profit", "commission",
                    "income_code","amountDetail","income_account","income_man","income_date","income_total_amount","outgo_code","outgo_total_amount"
                    };
            ExcelUtil.exportExcel("已登记稿件列表", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("issued_date".equals(field)||"income_date".equals(field)) {
                        cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                    } else if ("income_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未进账");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已进账");
                        } else{
                            cell.setCellValue("进行中");
                        }
                    } else if ("outgo_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未出账");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已出账");
                        } else{
                            cell.setCellValue("进行中");
                        }
                    } else if ("invoice_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未开票");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已开票");
                        } else{
                            cell.setCellValue("进行中");
                        }
                    } else if ("refund_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未退款");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已退款");
                        } else{
                            cell.setCellValue("进行中");
                        }
                    } else if ("other_pay_states".equals(field)) {
                        if ((int) value == 0) {
                            cell.setCellValue("未支出");
                        } else if ((int) value == 1) {
                            cell.setCellValue("已支出");
                        } else{
                            cell.setCellValue("支出中");
                        }
                    } else if("income_amount".equals(field)||"outgo_amount".equals(field)||"taxes".equals(field)
                            ||"refund_amount".equals(field)||"other_pay".equals(field)||"profit".equals(field)
                            ||"commission".equals(field) || "sale_amount".equals(field)||"tradeAmount".equals(field)
                            ||"outgo_total_amount".equals(field)){
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            });
            return list;
        }*/
    @Override
    public List<ArticleExcelCommissionRegister> exportRegisterNew(Map map) {
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        if (roles != null && roles.size() > 0) {
            map.put("roleType", roles.get(0).getType());
            map.put("roleCode", roles.get(0).getCode());
            map.put("user", user);
            map.put("commissionStates", IConst.FEE_STATE_PROCESS);
            map.put("companyCode", AppUtil.getUser().getCompanyCode());
        }
        handleQueryData(map);
        // 清除线程缓存，确保不会分页查10条
        PageHelper.clearPage();
        List<Map> list = commissionMapper.queryArticleByUserAndYearAndMonth(map);
        return doWithEasyExcelRegister(list);
    }

    private List<ArticleExcelCommissionRegister> doWithEasyExcelRegister(List<Map> list) {
        List<ArticleExcelCommissionRegister> excelList = new ArrayList<>();
        for (Map temp : list) {
            ArticleExcelCommissionRegister articleExcel = new ArticleExcelCommissionRegister();
            articleExcel.setYear(ObjectUtils.isEmpty(temp.get("year")) ? 0 : Integer.parseInt(String.valueOf(temp.get("year"))));
            articleExcel.setMonth(ObjectUtils.isEmpty(temp.get("month")) ? 0 : Integer.parseInt(String.valueOf(temp.get("month"))));
            articleExcel.setMediaTypeName(MapUtils.getString(temp, "media_type_name"));
            articleExcel.setUserName(MapUtils.getString(temp, "user_name"));
            articleExcel.setMediaUserName(MapUtils.getString(temp, "media_user_name"));
            articleExcel.setCompanyName(MapUtils.getString(temp, "company_name"));
            articleExcel.setIssuedDate(ObjectUtils.isEmpty(temp.get("issued_date")) ? null : MapUtils.getString(temp, "issued_date").substring(0, 10));
            articleExcel.setMediaName(MapUtils.getString(temp, "media_name"));
            articleExcel.setTitle(MapUtils.getString(temp, "title"));
            articleExcel.setInnerOuter(MapUtils.getString(temp, "inner_outer"));
            articleExcel.setChannel(MapUtils.getString(temp, "channel"));
            articleExcel.setElectricityBusinesses(MapUtils.getString(temp, "electricity_businesses"));
            articleExcel.setSaleAmount(ObjectUtils.isEmpty(temp.get("sale_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("sale_amount"))));
            articleExcel.setIncomeAmount(ObjectUtils.isEmpty(temp.get("income_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("income_amount"))));
            String incomeStates = String.valueOf(temp.get("income_states"));
            if ("1".equals(String.valueOf(incomeStates))) {
                articleExcel.setIncomeStates("已回款");
            } else if ("2".equals(String.valueOf(incomeStates))) {
                articleExcel.setIncomeStates("回款中");
            } else {
                articleExcel.setIncomeStates("未回款");
            }
            articleExcel.setOutgoAmount(ObjectUtils.isEmpty(temp.get("outgo_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgo_amount"))));
            String outgoStates = String.valueOf(temp.get("outgo_states"));
            if ("1".equals(String.valueOf(outgoStates))) {
                articleExcel.setOutgoStates("已请款");
            } else if ("2".equals(String.valueOf(outgoStates))) {
                articleExcel.setOutgoStates("请款中");
            } else {
                articleExcel.setOutgoStates("未请款");
            }
            articleExcel.setTaxes(ObjectUtils.isEmpty(temp.get("taxes")) ? 0 : Double.parseDouble(String.valueOf(temp.get("taxes"))));
            String invoiceStates = String.valueOf(temp.get("invoice_states"));

            if ("1".equals(String.valueOf(invoiceStates))) {
                articleExcel.setInvoiceStates("已开票");
            } else if ("2".equals(String.valueOf(invoiceStates))) {
                articleExcel.setInvoiceStates("开票中");
            } else {
                articleExcel.setInvoiceStates("未开票");
            }
            articleExcel.setRefundAmount(ObjectUtils.isEmpty(temp.get("refund_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("refund_amount"))));
            String refundStates = String.valueOf(temp.get("refund_states"));
            if ("1".equals(String.valueOf(refundStates))) {
                articleExcel.setRefundStates("已退款");
            } else if ("2".equals(String.valueOf(refundStates))) {
                articleExcel.setRefundStates("退款中");
            } else {
                articleExcel.setRefundStates("未退款");
            }
            articleExcel.setOtherPay(ObjectUtils.isEmpty(temp.get("other_pay")) ? 0 : Double.parseDouble(String.valueOf(temp.get("other_pay"))));
            String otherPayStates = String.valueOf(temp.get("other_pay_states"));
            if ("1".equals(String.valueOf(otherPayStates))) {
                articleExcel.setOtherPayStates("已支出");
            } else if ("2".equals(String.valueOf(otherPayStates))) {
                articleExcel.setOtherPayStates("支出中");
            } else {
                articleExcel.setOtherPayStates("未支出");
            }
            articleExcel.setProfit(ObjectUtils.isEmpty(temp.get("profit")) ? 0 : Double.parseDouble(String.valueOf(temp.get("profit"))));
            articleExcel.setCommission(ObjectUtils.isEmpty(temp.get("commission")) ? 0 : Double.parseDouble(String.valueOf(temp.get("commission"))));
            articleExcel.setIncomeCode(MapUtils.getString(temp, "income_code"));
            articleExcel.setAmountDetail(MapUtils.getString(temp, "amountDetail"));
            articleExcel.setIncomeAccount(MapUtils.getString(temp, "income_account"));
            articleExcel.setIncomeMan(MapUtils.getString(temp, "income_man"));
            articleExcel.setIncomeDate(ObjectUtils.isEmpty(temp.get("income_date")) ? null : MapUtils.getString(temp, "income_date").substring(0, 10));
            articleExcel.setIncomeTotalAmount(ObjectUtils.isEmpty(temp.get("income_total_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("income_total_amount"))));
            articleExcel.setOutgoCode(MapUtils.getString(temp, "outgo_code"));
            articleExcel.setOutgoTotalAmount(ObjectUtils.isEmpty(temp.get("outgo_total_amount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgo_total_amount"))));
            excelList.add(articleExcel);
        }
        return excelList;
    }
    /*    *//**
     * 导出个人详情
     * @param map
     * @param outputStream
     * @return
     *//*
    @Override
    public List<Map> exportDetail(Map map, OutputStream outputStream) {
        List<Map> list = commissionMapper.queryArticleByUserAndYearAndMonth(map);
        String[] heads = {"年","月", "业务员","供应商","媒体","标题","链接","应收（报价）",
                "回款金额", "回款状态","成本（请款）","请款状态", "税额","开票状态",  "退款", "退款状态","其他支出","其他支出状态", "利润","提成"};
        String[] fields = {"year", "month", "user_name","supplier_name","media_name", "title","link","sale_amount",
                "income_amount","income_states", "outgo_amount","outgo_states", "taxes","invoice_states",
                "refund_amount","refund_states","other_pay","other_pay_states", "profit", "commission"};
        ExcelUtil.exportExcel("提成信息", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("updateTime".equals(field)) {
                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                } else if ("income_states".equals(field)) {
                    if ((int) value == 0) {
                        cell.setCellValue("未进账");
                    } else if ((int) value == 1) {
                        cell.setCellValue("已进账");
                    } else{
                        cell.setCellValue("进行中");
                    }
                } else if ("outgo_states".equals(field)) {
                    if ((int) value == 0) {
                        cell.setCellValue("未出账");
                    } else if ((int) value == 1) {
                        cell.setCellValue("已出账");
                    } else{
                        cell.setCellValue("进行中");
                    }
                } else if ("invoice_states".equals(field)) {
                    if ((int) value == 0) {
                        cell.setCellValue("未开票");
                    } else if ((int) value == 1) {
                        cell.setCellValue("已开票");
                    } else{
                        cell.setCellValue("进行中");
                    }
                } else if ("refund_states".equals(field)) {
                    if ((int) value == 0) {
                        cell.setCellValue("未退款");
                    } else if ((int) value == 1) {
                        cell.setCellValue("已退款");
                    } else{
                        cell.setCellValue("进行中");
                    }
                } else if ("other_pay_states".equals(field)) {
                    if ((int) value == 0) {
                        cell.setCellValue("未支出");
                    } else if ((int) value == 1) {
                        cell.setCellValue("已支出");
                    } else{
                        cell.setCellValue("支出中");
                    }
                } else if("income_amount".equals(field)||"outgo_amount".equals(field)||"taxes".equals(field)
                        ||"refund_amount".equals(field)||"other_pay".equals(field)||"profit".equals(field)
                        ||"commission".equals(field)){
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }*/

    /**
     * 导出全部
     *
     * @param map
     * @param outputStream
     * @return
     */
    @Override
    public List<Map> exportAll(Map map, OutputStream outputStream) {
        handleMap(map);// 处理请求参数-权限
        List<Map> list = commissionMapper.listFeeCommissionByPage(map);
        String[] heads = {"年", "月", "业务员", "部门名称", "应收（报价）", "回款", "成本（请款）", "税额", "退款", "其他支出", "利润", "提成", "状态", "更新日期"};
        String[] fields = {"year", "month", "name", "dept_name", "sale", "income", "outgo", "tax_expense", "refund",
                "other_expense", "profit", "comm", "state", "update_time"};
        ExcelUtil.exportExcel("提成信息", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("state".equals(field)) {
                    if ((int) value == 0) {
                        cell.setCellValue("已保存");
                    } else if ((int) value == 1) {
                        cell.setCellValue("确认完成");
                    } else if ((int) value == 10) {
                        cell.setCellValue("待确认");
                    }
                } else if ("sale".equals(field) || "income".equals(field) || "outgo".equals(field) || "tax_expense".equals(field)
                        || "refund".equals(field) || "other_expense".equals(field) || "profit".equals(field)
                        || "comm".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else if ("updateTime".equals(field)) {
                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    @Transactional
    public Commission confirm(Commission entity, User user) {
        //增加待办
        Items items = addItem(entity, user);
        entity.setItemId(items.getId());
        commissionMapper.update(entity);
        return null;
    }

    @Override
    @Transactional
    public Commission pass(Commission entity, User user) {
        // 待办变已办
        finishItem(entity);
        // 发消息
        String content = user.getName() + ":" + entity.getYear() + "年" + entity.getMonth() + "月的提成由业务员核实通过！";
        addMessage(user, content);

        entity.setState(IConst.STATE_PASS);
        commissionMapper.update(entity);
        return entity;
    }

    @Override
    @Transactional
    public Commission reject(Commission entity, User user) {
        // 待办变已办
        finishItem(entity);
        // 发消息
        String content = user.getName() + ":" + entity.getYear() + "年" + entity.getMonth() + "月的提成由业务员核实有误，请核实后重新发送确认！";
        addMessage(user, content);

        entity.setState(IConst.STATE_REJECT);
        commissionMapper.update(entity);
        return entity;
    }

    @Override
    @Transactional
    public Commission release(Commission entity, User user) {
        // 把稿件状态改成已提成
        List<Article> list = commissionMapper.queryArticleListByUserAndYearAndMonth(entity.getUserId(), entity.getYear(), entity.getMonth());
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("state", IConst.FEE_STATE_FINISH);
        commissionMapper.changeCommissionState(map);

        // 发消息
        Message message = new Message();
        String userImage = user.getImage();
        // 获取消息显示的图片；
        String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
        message.setPic(pictureAddress);
        // message.setType(0) ;
        message.setContent(user.getName() + ":" + entity.getYear() + "年" + entity.getMonth() + "月的提成已由财务核实完成！");
        message.setInitiatorWorker(user.getId());
        message.setInitiatorDept(user.getDeptId());
        message.setAcceptWorker(entity.getUserId());
        message.setAcceptDept(entity.getDeptId());
        messageService.addMessage(message);

        entity.setState(IConst.STATE_FINISH);
        entity.setReleaseId(user.getId());
        entity.setReleaseTime(new Date());
        commissionMapper.update(entity);
        return null;
    }


    private Items addItem(Commission entity, User user) {
        // 增加待办
        Items items = new Items();
        items.setItemName(entity.getName() + ":" + entity.getYear() + "年" + entity.getMonth() + "月-提成确认等待处理");
        items.setItemContent("您有新的提成申请需要处理");
        items.setWorkType("提成确认");
        items.setInitiatorWorker(user.getId());
        items.setInitiatorDept(user.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/fee/queryCommission");
        items.setFinishAddress("/fee/queryCommission");
        User cw = userService.getAccountingInfo(user.getCompanyCode());
        items.setAcceptWorker(cw.getId());
        items.setAcceptDept(cw.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items;
    }

    //待办变已办
    private void finishItem(Commission entity) {
        if (entity.getItemId() != null) {
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }

    private void addMessage(User user, String content) {
        Message message = new Message();
        String userImage = user.getImage();
        // 获取消息显示的图片；
        String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
        message.setPic(pictureAddress);
        // message.setType(0) ;
        message.setContent(content);
        message.setInitiatorWorker(user.getId());
        message.setInitiatorDept(user.getDeptId());
        User account = userService.getAccountingInfo(user.getCompanyCode());
        message.setAcceptWorker(account.getId());
        message.setAcceptDept(account.getDeptId());
        messageService.addMessage(message);
    }

    /**
     * 默认上一月,若为一月年份需要是上一年（默认查询2019-12数据,不是2020-1）
     *
     * @param map
     */
    private void handleQueryData(Map map) {
        Object year = map.get("commissionYear");
        Object month = map.get("commissionMonth");
        Object startTime = map.get("commissionStartTime");
        Object endTime = map.get("commissionEndTime");
        Object year2 = map.get("year");
        Object month2 = map.get("month");
        Calendar cal = Calendar.getInstance();
        Integer systemYear = cal.get(Calendar.YEAR);
        Integer systemMonth = cal.get(Calendar.MONTH) + 1;
        cal.add(Calendar.MONTH, -1);
        Integer preMonth = cal.get(Calendar.MONTH) + 1;
        if (startTime != null && !"".equals(startTime)) {
            String begin = startTime.toString().replaceAll("/", "-");
            map.put("commissionStartTime", begin);
        }
        if (endTime != null && !"".equals(endTime)) {
            String end = endTime.toString().replaceAll("/", "-");
            map.put("commissionEndTime", end);
        }
        //提成管理页面查询
        if (year2 == null && month2 == null) {
            //管理上月提成页面
            if ((month == null || "".equals(month)) && (startTime == null || "".equals(startTime)) && (endTime == null || "".equals(endTime))) {
                //未填值,默认上一月,若为一月年份需要是上一年（2020-1，默认查询2019-12数据）
                if (year == null || "".equals(year)) {
                    systemYear = systemMonth == 1 ? systemYear - 1 : systemYear;
                    map.put("commissionYear", systemYear);
                    map.put("commissionMonth", preMonth);
                }
            } else if (month != null && !"".equals(month) && (startTime == null || "".equals(startTime)) && (endTime == null || "".equals(endTime))) {
                //若只填写提成月，年份默认当年
                if (year == null || "".equals(year)) {
                    map.put("commissionYear", systemYear);
                }
            }
        }
    }

    /* *//**\
     * 提交1、请款申请，2、借款和其他支出申请
     * 这2个条件下把稿件提成状态还原（提成中2-->已保存0），金额置0，该稿件提成汇总表的金额退回去
     * @param user 业务员对象
     *//*
    @Override
    @Transactional
    public void backCommInfo(Integer flag, Article article, User user) {
        if(article.getCommissionStates()==2){
            //提成汇总表减去该稿件提成
            List<Commission> list = commissionMapper.queryCommissionByUser(user.getId(),article.getYear(),article.getMonth()) ;
            if(list!=null && list.size()>0){
                Commission oldComm = list.get(0) ;
                oldComm.setProfit(oldComm.getProfit()-article.getProfit());
                oldComm.setComm(oldComm.getComm()-article.getCommission());
                commissionMapper.update(oldComm);
            }
            article.setProfit(0.0);
            article.setCommission(0.0);
            article.setCommissionStates(0);
            article.setYear(null) ;
            article.setMonth(null) ;
            articleMapper.update(article) ;
        }
        return ;
    }
    *//**
     * 1、分款且稿件的实收大于等于应收income>=sale；2、财务确认开票；3、财务确认退款；、4、财务确认其他支出；5、财务批准请款。
     * 这5个条件下执行该方法，计算稿件提成和提成汇总
     * 修改稿件提成状态commission_states为2提成中，财务发放提成后更改为1
     * @param flag 计算提成标志位，以上5种状态都会导致提成变化，通过标志位更改
     * @param user 业务员对象
     *//*
    @Override
    @Transactional
    public void updateCommInfo(Integer flag,Article article,User user) {
        //该稿件未计算过提成，则计算出提成信息，并把金额添加到提成汇总表中
        if(article.getCommissionStates()!=1){
            Commission comm = new Commission() ;
            List<Commission> list = commissionMapper.queryCommissionByUser(user.getId(), CodeUtil.getYear(),CodeUtil.getMonth()) ;
            if(list!=null && list.size()>0){
                Commission oldComm = list.get(0) ;
                BeanUtils.copyProperties(oldComm,comm);
                oldComm.setState(IConst.STATE_DELETE);
                commissionMapper.update(oldComm);
                comm.setId(null);
            }else{
                comm.setUserId(user.getId());
                comm.setName(user.getName());
                comm.setDeptId(user.getDeptId());
                comm.setDeptName(user.getDeptName());
                comm.setYear(CodeUtil.getYear()) ;
                comm.setMonth(CodeUtil.getMonth()) ;
                if(comm.getIncome()==null){
                    comm.setIncome(0D);
                }
                if(comm.getOutgo()==null){
                    comm.setOutgo(0D);
                }
                if(comm.getTaxExpense()==null){
                    comm.setTaxExpense(0D);
                }
                if(comm.getRefund()==null){
                    comm.setRefund(0D);
                }
                if(comm.getOtherExpense()==null){
                    comm.setOtherExpense(0D);
                }
                if(comm.getProfit()==null){
                    comm.setProfit(0D);
                }
                if(comm.getComm()==null){
                    comm.setComm(0D);
                }
            }
            Double saleAmount = article.getSaleAmount() ;
            Double incomeAmount = article.getIncomeAmount() ;
            Double outgoAmount = article.getOutgoAmount() ;
            Double taxes = article.getTaxes() ;
            Double otherPay = article.getOtherPay() ;
            Double refundAmount = article.getRefundAmount() ;
            */

    /**
     *
     *//*
//            Double taxes = article.getTaxes() ;
            Double profit = article.getProfit() ;
//            Double commAmount = article.getCommission() ;

            switch (flag){
                case IConst.INCOME_FLAG :
                    if(article.getIncomeStates()==1){
                        profit = profit + saleAmount ;
                        comm.setIncome(comm.getIncome()+incomeAmount);
                        comm.setProfit(comm.getProfit()+incomeAmount);
                        comm.setComm(Math.floor(comm.getProfit() * 20)*0.01);
                    }
                    break ;
                case IConst.OUTGO_FLAG :
                    if(article.getOutgoStates()==1){
                        profit = profit - outgoAmount ;
                        comm.setOutgo(comm.getOutgo()+outgoAmount);
                        comm.setProfit(comm.getProfit()-outgoAmount);
                        comm.setComm(Math.floor(comm.getProfit() * 20)*0.01);
                    }
                    break ;
                case IConst.INVOICE_FLAG :
                    if(article.getInvoiceStates()==1){
                        profit = profit - taxes ;
                        comm.setTaxExpense(comm.getTaxExpense()+taxes) ;
                        comm.setProfit(comm.getProfit()-taxes);
                        comm.setComm(Math.floor(comm.getProfit() * 20)*0.01);
                    }
                    break ;
                case IConst.REFUND_FLAG :
                    if(article.getRefundStates()==1){
                        profit = profit - refundAmount - otherPay;
                        comm.setRefund(comm.getRefund()+refundAmount);
                        comm.setOtherExpense(comm.getOtherExpense()+otherPay);
                        comm.setProfit(comm.getProfit()-refundAmount-otherPay);
                        comm.setComm(Math.floor(comm.getProfit() * 20)*0.01);
                    }
                    break ;
                default:
                    break ;
            }

            //提成 = （进账 - 成本价 - 退款 - 其他支出 - 税额）* 0.2
            Double articleCommAmount = Math.floor(profit * 20)*0.01 ;
            article.setYear(CodeUtil.getYear());
            article.setMonth(CodeUtil.getMonth());
            article.setProfit(profit);
            article.setCommission(articleCommAmount);
            article.setCommissionStates(2);
            articleMapper.update(article) ;

            comm.setCommPercent(0.2);
            commissionMapper.insert(comm) ;
        }
    }*/
    @Override
    public PageInfo<Map> queryArticleByCommStates(Pageable pageable, Map map) {
        User user = AppUtil.getUser();
        map.put("companyCode", user.getCompanyCode());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map> list = commissionMapper.queryArticleByCommStates(map);
        return new PageInfo(list);
    }

    /**
     * 已登记稿件查询列表（默认查询上一月已登记稿件）
     *
     * @param pageable
     * @param map
     * @return
     */
    @Override
    public PageInfo<Map> queryArticleByYearAndMonth(Pageable pageable, Map map) {
        User user = AppUtil.getUser();
        map.put("companyCode", user.getCompanyCode());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        handleQueryData(map);
        List<Map> list = commissionMapper.queryArticleByUserAndYearAndMonth(map);
        return new PageInfo(list);
    }
}
