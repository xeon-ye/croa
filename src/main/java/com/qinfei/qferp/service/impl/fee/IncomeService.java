package com.qinfei.qferp.service.impl.fee;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleHistory;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.fee.Income;
import com.qinfei.qferp.entity.fee.IncomeArticle;
import com.qinfei.qferp.entity.fee.IncomeUser;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.sys.WorkDate;
import com.qinfei.qferp.mapper.biz.ArticleHistoryMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapper;
import com.qinfei.qferp.mapper.fee.AccountMapper;
import com.qinfei.qferp.mapper.fee.IncomeArticleMapper;
import com.qinfei.qferp.mapper.fee.IncomeMapper;
import com.qinfei.qferp.mapper.fee.IncomeUserMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.service.fee.IIncomeService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.sys.IWorkDateService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class IncomeService implements IIncomeService {
    @Autowired
    private IncomeMapper incomeMapper;
    @Autowired
    private IncomeUserMapper incomeUserMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private IncomeArticleMapper incomeArticleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleHistoryMapper articleHistoryMapper;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IWorkDateService workDateService;

    @Override
    public PageInfo<Map> listPg(int pageNum, int pageSize, Map map) {
        //如果是业务部查看进账，找到导入当前日期前7个工作日的日期
        if (IConst.ROLE_TYPE_YW.equals(map.get("roleType"))) {
            Date date = new Date();
            Calendar end = Calendar.getInstance();
            end.setTime(date);
            Calendar start = Calendar.getInstance();
            start.setTime(new Date());
            //7个工作日，一般会有双休，往前推9天
            start.add(Calendar.DATE, -9);
            Map result = workDateService.listDateByRange(
                    new SimpleDateFormat("yyyy-MM-dd").format(start.getTime()),
                    new SimpleDateFormat("yyyy-MM-dd").format(end.getTime()));
            if (result.containsKey("workDate")) {
                List<String> list = (List<String>) result.get("workDate");
                int size = list.size();//工作日数量
                String str = null;//7个工作日开始日期
                switch (size) {
                    case 9:
                        str = list.get(2);
                        break;
                    case 8:
                        str = list.get(1);
                        break;
                    case 7:
                        str = list.get(0);
                        break;
                    default:
                        String temp = list.get(0);
                        Calendar clss = Calendar.getInstance();
                        int j = 1;//往前推的天数
                        //先往前推了9天，工作日不够7天，那就继续一天一天往前推，
                        //如果是工作日，就size++，如果不是，就再往前一天
                        do {
                            try {
                                clss.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(temp));
                                clss.add(Calendar.DATE, -j);
                                WorkDate workDate = workDateService.getWorkDateByDate(
                                        new SimpleDateFormat("yyyy-MM-dd").format(clss.getTime()));
                                if (!ObjectUtils.isEmpty(workDate)) {
                                    if (workDate.getDateType() == 0) {
                                        size++;
                                    }
                                } else {
                                    int week = DateUtils.getWeek(clss.getTime());
                                    if (!(week == Calendar.SUNDAY || week == Calendar.SATURDAY)) {
                                        size++;
                                    }
                                }
                                j++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } while (size < 7);
                        str = new SimpleDateFormat("yyyy-MM-dd").format(clss.getTime());
                        break;
                }
                map.put("startDate", str);
            }
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = incomeMapper.listPg(map);
        return new PageInfo<>(list);
    }

    @Override
    public Income getById(Integer id) {
        return incomeMapper.getById(id);
    }

    @Override
    public Income add(Income entity) {
        User user = AppUtil.getUser();
        entity.setCompanyCode(user.getCompanyCode());
        entity.setCreator(user.getId());
        entity.setCreateTime(new Date());
        entity.setId(null);
        entity.setVisiableDay(IConst.VISIABLE_DAY);
        entity.setUnclaimedAmount(entity.getTradeAmount());
        incomeMapper.insert(entity);
        return entity;
    }

    @Override
    public Income edit(Income entity) {
        incomeMapper.update(entity);
        return entity;
    }

    @Override
    public Income update(Income entity) {
        incomeMapper.update(entity);
        return entity;
    }

    @Override
    public void delById(Income entity) {
        User user = AppUtil.getUser();
        entity.setState(IConst.STATE_DELETE);
        entity.setUpdateUserId(user.getId());
        incomeMapper.update(entity);
    }

    @Override
    @Transactional
    public Income receive(Income entity, Double amount) {
        User user = AppUtil.getUser();
        Double oldPreclaimedAmount = entity.getPreclaimedAmount();
        Double oldUnclaimedAmount = entity.getUnclaimedAmount();
        entity.setUnclaimedAmount(new BigDecimal(oldUnclaimedAmount).
                subtract(new BigDecimal(amount)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setPreclaimedAmount(oldPreclaimedAmount + amount);
        //查询领款信息
        IncomeUser incomeUser = incomeUserMapper.getIncomeUser(entity.getId(), user.getId());
        //已领，就update，否则新增
        if (incomeUser != null) {
            incomeUser.setReceiveTime(new Date());
            incomeUser.setReceiveAmount(new BigDecimal(incomeUser.getReceiveAmount()).
                    add(new BigDecimal(amount)).
                    setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            incomeUser.setRemainAmount(new BigDecimal(incomeUser.getRemainAmount()).
                    add(new BigDecimal(amount)).
                    setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            incomeUserMapper.update(incomeUser);
        } else {
            incomeUser = new IncomeUser();
            incomeUser.setIncomeId(entity.getId());
            incomeUser.setUserId(user.getId());
            incomeUser.setName(user.getName());
            incomeUser.setReceiveAmount(amount);
            incomeUser.setRemainAmount(amount);
            incomeUser.setReceiveTime(new Date());
            incomeUser.setCreateTime(new Date());
            incomeUser.setDeptId(user.getDeptId());
            incomeUser.setDeptName(user.getDeptName());
            incomeUserMapper.insert(incomeUser);
        }
        entity.setUpdateUserId(user.getId());
        incomeMapper.update(entity);
        return entity;
    }

    @Override
    public Income dispatch(Income entity, Integer userId, Double amount) {
        Double oldPreclaimedAmount = entity.getPreclaimedAmount();
        Double oldUnclaimedAmount = entity.getUnclaimedAmount();
        entity.setUnclaimedAmount(new BigDecimal(oldUnclaimedAmount).
                subtract(new BigDecimal(amount)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setPreclaimedAmount(oldPreclaimedAmount + amount);
        //查询领款信息
        IncomeUser incomeUser = incomeUserMapper.getIncomeUser(entity.getId(), userId);
        //已领，就update，否则新增
        if (incomeUser != null) {
            incomeUser.setReceiveTime(new Date());
            incomeUser.setReceiveAmount(new BigDecimal(incomeUser.getReceiveAmount()).
                    add(new BigDecimal(amount)).
                    setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            incomeUser.setRemainAmount(new BigDecimal(incomeUser.getRemainAmount()).
                    add(new BigDecimal(amount)).
                    setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            incomeUserMapper.update(incomeUser);
        } else {
            incomeUser = new IncomeUser();
            incomeUser.setIncomeId(entity.getId());
            incomeUser.setUserId(userId);
            User YW = userMapper.getById(userId);
            incomeUser.setName(YW.getName());
            incomeUser.setReceiveAmount(amount);
            incomeUser.setRemainAmount(amount);
            incomeUser.setReceiveTime(new Date());
            incomeUser.setCreateTime(new Date());
            incomeUser.setDeptId(YW.getDeptId());
            incomeUser.setDeptName(YW.getDeptName());
            incomeUserMapper.insert(incomeUser);
        }
        entity.setUpdateUserId(AppUtil.getUser().getId());
        incomeMapper.update(entity);
        return entity;
    }

    @Override
    @Transactional
    public void withdraw(Integer id, User user, Double receiveAmount, List<IncomeUser> list) {
        Income entity = incomeMapper.getById(id);
        //查询领款信息
        entity.setUnclaimedAmount(new BigDecimal(entity.getUnclaimedAmount()).
                add(new BigDecimal(receiveAmount)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setPreclaimedAmount(new BigDecimal(entity.getPreclaimedAmount()).
                subtract(new BigDecimal(receiveAmount)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        entity.setUpdateUserId(user.getId());
        //list.size()=1,不用sql批量更新了。
        for (IncomeUser iu : list) {
            iu.setState(IConst.STATE_DELETE);
            incomeUserMapper.update(iu);
        }
        incomeMapper.update(entity);
    }

    /**
     * 查询分款未完成的稿件，用于分款
     * 条件：未分款或分款未完成 && 已发布 && 已完善客户 && 当前业务员 && 去除2019年1月份老系统的稿件
     *
     * @param map
     * @return 未分款的稿件列表
     */
    @Override
    public PageInfo<Map> queryArticleForAssign(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = incomeMapper.queryArticleForAssign(map);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional
    public void assignArticle(Map map, User user) {
        Integer incomeId;
        try {
            incomeId = Integer.parseInt((String) map.get("incomeId"));
        } catch (Exception e) {
            throw new QinFeiException(1002, "获取进账id失败，请刷新后重试！");
        }

        Income income = incomeMapper.getById(incomeId);
        String articleIds = (String) map.get("ids");
        if (articleIds == null) {
            throw new QinFeiException(1002, "没有获取到分款的稿件信息！");
        }
        IncomeUser incomeUser = incomeUserMapper.getIncomeUser(incomeId, user.getId());
        if (incomeUser == null) {
            throw new QinFeiException(1002, "没有查询到该用户的领款记录！");
        }
        Double total = 0.0;
        List<IncomeArticle> incomeArticles = new ArrayList<>();
        List<Article> articles = new ArrayList<>();
        List<ArticleHistory> historyList = new ArrayList<>();
        String curMonth = DateUtils.getYearAndMonthStr2(new Date());//当前年月
        if (articleIds.indexOf(",") > -1) {
            String[] ids = articleIds.split(",");
            //放入set排重
            Set<Integer> set = new HashSet();
            for (int i = 0; i < ids.length; i++) {
                set.add(Integer.parseInt(ids[i]));
            }

            //处理进账和稿件关系
            Iterator<Integer> iterator = set.iterator();
            while (iterator.hasNext()) {
                Integer next = iterator.next();
                Double incomeAmount = Double.parseDouble((String) map.get("income_" + next));
                total += incomeAmount;

                //处理稿件进账金额和状态
                Article article = articleMapper.get(Article.class, next);
                Order order = orderService.get(article.getOrderId());
                ArticleHistory history = new ArticleHistory();
                BeanUtils.copyProperties(article, history);
                history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                history.setUserId(order.getUserId());
                history.setDeptId(order.getDepatId());
                history.setId(null);
                history.setArtId(article.getId());
                history.setEditDesc(IConst.article_change_income_add);
                history.setCreator(user.getId());
                history.setCreateTime(new Date());

                Double temp = new BigDecimal(article.getIncomeAmount()).add(new BigDecimal(incomeAmount)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (incomeAmount > 0) {
                    article.setIncomeAmount(new BigDecimal(temp).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    article.setIncomeId(income.getId());
                    article.setIncomeCode(income.getCode());
                    article.setIncomeAccount(income.getAccountName());
                    article.setIncomeMan(income.getTradeMan());
                    article.setIncomeTotalAmount(income.getTradeAmount());
                    article.setIncomeDate(income.getTradeTime());
                    article.setAssignDate(new Date());
                    Double profit;
                    if (!(article.getSaleAmount() > (temp + 1))) {//进账大于等于应收-1则回款完成
                        //更新稿件进款信息
                        article.setIncomeStates(IConst.FEE_STATE_FINISH);
                        //利润计算：回款完成，回款-成本-税金-退款金额-其他支出
                        profit = temp - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                    } else {
                        article.setIncomeStates(IConst.FEE_STATE_PROCESS);
                        //利润计算：部分回款，应收-成本-税金-退款金额-其他支出
                        profit = article.getSaleAmount() - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                    }

                    String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
//                  满足两个条件回款就有异动：1、更改月份和稿件发布月份不一致，2、利润有变动
                    Double alterProfit = profit - article.getProfit();
                    if (!(curMonth.equals(issueMonth))
                            && Math.abs(alterProfit) > 0.01) {
                        article.setAlterFlag(IConst.article_alter_flag_true);
                        history.setAlterIncome(incomeAmount);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                        history.setAlterProfit(alterProfit);
                    }

                    article.setProfit(profit);
                    articles.add(article);
                    //处理分款关系表的关系
                    IncomeArticle incomeArticle = new IncomeArticle();
                    incomeArticle.setIncomeId(incomeId);
                    incomeArticle.setArticleId(next);
                    incomeArticle.setAmount(incomeAmount);
                    incomeArticle.setDate(new Date());
                    incomeArticle.setIncomeUserId(incomeUser.getUserId());
                    incomeArticles.add(incomeArticle);
                } else {
                    throw new QinFeiException(1002, "分款金额必须大于0！");
                }
                historyList.add(history);
            }
        } else {
            Integer id = Integer.parseInt(articleIds);
            Double incomeAmount = Double.parseDouble((String) map.get("income_" + id));
            total += incomeAmount;
            Article article = articleMapper.get(Article.class, id);
            Order order = orderService.get(article.getOrderId());
            ArticleHistory history = new ArticleHistory();
            BeanUtils.copyProperties(article, history);
            history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
            history.setUserId(order.getUserId());
            history.setDeptId(order.getDepatId());
            history.setId(null);
            history.setArtId(article.getId());
            history.setEditDesc(IConst.article_change_income_add);
            history.setCreator(user.getId());
            history.setCreateTime(new Date());

            Double temp = new BigDecimal(article.getIncomeAmount()).add(new BigDecimal(incomeAmount)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            //处理稿件进账金额和状态
            if (incomeAmount > 0) {
                article.setIncomeAmount(new BigDecimal(temp).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                article.setIncomeId(income.getId());
                article.setIncomeCode(income.getCode());
                article.setIncomeAccount(income.getAccountName());
                article.setIncomeMan(income.getTradeMan());
                article.setIncomeTotalAmount(income.getTradeAmount());
                article.setIncomeDate(income.getTradeTime());
                article.setAssignDate(new Date());
                Double profit;
                if (!(article.getSaleAmount() > (temp + 1))) {
                    //更新稿件进款信息
                    article.setIncomeStates(IConst.FEE_STATE_FINISH);
                    //利润计算：回款完成，回款-成本-税金-退款金额-其他支出
                    profit = temp - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                } else {
                    article.setIncomeStates(IConst.FEE_STATE_PROCESS);
                    //利润计算：回款完成，回款-成本-税金-退款金额-其他支出
                    profit = article.getSaleAmount() - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                }

                String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
//              满足两个条件回款就有异动：1、更改月份和稿件发布月份不一致，2、利润有变动
                Double alterProfit = profit - article.getProfit();
                if (!(curMonth.equals(issueMonth))
                        && Math.abs(alterProfit) > 0.01) {
                    article.setAlterFlag(IConst.article_alter_flag_true);
                    history.setAlterIncome(incomeAmount);
                    history.setAlterLabel(IConst.article_alter_flag_true);
                    history.setAlterProfit(alterProfit);
                }

                article.setProfit(profit);
                articles.add(article);

                //处理分款关系表的关系
                IncomeArticle incomeArticle = new IncomeArticle();
                incomeArticle.setIncomeId(incomeId);
                incomeArticle.setArticleId(id);
                incomeArticle.setIncomeUserId(incomeUser.getUserId());
                incomeArticle.setAmount(incomeAmount);
                incomeArticles.add(incomeArticle);
            } else {
                throw new QinFeiException(1002, "分款金额必须大于0！");
            }
            historyList.add(history);
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
            //批量更新稿件
            incomeMapper.updateArticleIncomeInfoBatch(articles);
        } catch (Exception e) {
            throw new QinFeiException(1002, "分款时更新稿件利润失败！");
        }

        try {
            //批量插入关系表
            if (incomeArticles != null && incomeArticles.size() > 0) {
                int size = incomeArticles.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    List<IncomeArticle> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(incomeArticles.get(j));
                    }
                    incomeMapper.insertIncomeArticleBatch(insertData);
                }
            }
        } catch (Exception e) {
            throw new QinFeiException(1002, "分款时插入进账和稿件关系表失败！");
        }

        //处理领款表金额
        incomeUser.setAssignAmount(new BigDecimal(incomeUser.getAssignAmount()).
                add(new BigDecimal(total)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        incomeUser.setRemainAmount(new BigDecimal(incomeUser.getRemainAmount()).
                subtract(new BigDecimal(total)).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        if (incomeUser.getRemainAmount() < 0) {
            throw new QinFeiException(1002, "分款金额不足！");
        }
        try {
            incomeUserMapper.update(incomeUser);
        } catch (Exception e) {
            throw new QinFeiException(1002, "分款后处理领款表金额失败！");
        }
    }

    @Override
    public PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Integer incomeId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = incomeMapper.listPgForSelectedArticle(incomeId);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map> listPgForAssign(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = incomeUserMapper.listPgForAssign(map);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map> listPgForAssignCW(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        if (map.get("deptId") != null) {//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
        List<Map> list = incomeUserMapper.listPgForAssignCW(map);
        for (Map data : list) {
            if(data.get("phone") != null && !org.springframework.util.StringUtils.isEmpty(String.valueOf(data.get("phone")))){
                String phone = EncryptUtils.decrypt(String.valueOf(data.get("phone")));
                data.put("phone", phone);
            }
        }
        return new PageInfo<>(list);
    }

    @Override
    public IncomeUser getIncomeUser(Integer incomeId, Integer userId) {
        return incomeUserMapper.getIncomeUser(incomeId, userId);
    }

    @Override
    public List<Map> listPgByArticleId(Article article) {
        return incomeMapper.listPgByArticleId(article);
    }

    @Override
    public List<Income> queryIncomeByAccountId(Integer accountId) {
        return incomeMapper.queryIncomeByAccountId(accountId);
    }

    @Override
    public List<IncomeUser> queryIncomeUserByIncomeId(Integer incomeId) {
        return incomeUserMapper.queryIncomeUserByIncomeId(incomeId);
    }

    @Override
    public List<IncomeUser> queryIncomeUserByIncomeIdAndUserId(Integer incomeId, Integer userId) {
        return incomeUserMapper.queryIncomeUserByIncomeIdAndUserId(incomeId, userId);
    }

    @Override
    public Map querySumAmount(Integer incomeId, Integer userId) {
        return incomeUserMapper.querySumAmount(incomeId, userId);
    }

    @Override
    @Transactional
    public String backAssign(Integer incomeId, Integer userId) {
        List<IncomeArticle> list = incomeArticleMapper.queryByIncomeIdAndUserId(incomeId, userId);
        Boolean flag = handleArticleIncome(list);
        if (flag) {
            incomeArticleMapper.deleteIncomeArticleByIncomeIdAndUserId(incomeId, userId);
            //退回分款，把每个领款人的分款金额退回去
            List<IncomeUser> list2 = incomeUserMapper.queryIncomeUserByIncomeIdAndUserId(incomeId, userId);
            //list2只会有一条数据
            if (list2 != null && list2.size() > 0) {
                for (IncomeUser iu : list2) {
                    iu.setAssignAmount(0D);
                    iu.setRemainAmount(iu.getReceiveAmount());
                    incomeUserMapper.update(iu);
                }
            }
            return null;
        } else {
            return "选中的稿件有提成数据，无法撤回！";
        }
    }

    @Override
    @Transactional
    public String backIncome(Integer incomeId, Integer userId) {
        Income income = incomeMapper.getById(incomeId);
        List<IncomeArticle> list = incomeArticleMapper.queryByIncomeIdAndUserId(incomeId, userId);
        Boolean flag = handleArticleIncome(list);
        if (flag) {
            incomeArticleMapper.deleteIncomeArticleByIncomeIdAndUserId(incomeId, userId);
            //退回领款，把每个领款人的领款信息删除，把领款金额放回去
            List<IncomeUser> list2 = incomeUserMapper.queryIncomeUserByIncomeIdAndUserId(incomeId, userId);
            //list2只会有一条数据
            if (list2 != null && list2.size() > 0) {
                for (IncomeUser iu : list2) {
                    income.setUnclaimedAmount(income.getUnclaimedAmount() + iu.getReceiveAmount());
                    income.setPreclaimedAmount(new BigDecimal(income.getPreclaimedAmount()).
                            subtract(new BigDecimal(iu.getReceiveAmount())).
                            setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    iu.setState(IConst.STATE_DELETE);
                    incomeUserMapper.update(iu);
                }
            }
            incomeMapper.update(income);
            return null;
        } else {
            return "选中的稿件有提成数据，无法撤回！";
        }
    }

    @Override
    @Transactional
    public String backAssignArticle(Integer incomeId, Integer articleId) {
        List<IncomeArticle> list = incomeArticleMapper.queryByIncomeIdAndArticleId(incomeId, articleId);
        if (list == null || list.size() == 0) {
            throw new QinFeiException(1002, "未找到可以撤回的稿件，请刷新后重试！");
        }
        Boolean flag = handleArticleIncome(list);
        if (flag) {
            incomeArticleMapper.deleteIncomeArticleByIncomeIdAndArticleId(incomeId, articleId);
            //退回分款，把每个领款人的分款金额退回去
            for (IncomeArticle incomeArticle : list) {
                List<IncomeUser> list2 = incomeUserMapper.queryIncomeUserByIncomeIdAndUserId(incomeId, incomeArticle.getIncomeUserId());
                //list2只会有一条数据
                if (list2 != null && list2.size() > 0) {
                    for (IncomeUser iu : list2) {
                        iu.setAssignAmount(iu.getAssignAmount() - incomeArticle.getAmount());
                        iu.setRemainAmount(iu.getRemainAmount() + incomeArticle.getAmount());
                        incomeUserMapper.update(iu);
                    }
                }
            }
            return null;
        } else {
            return "选中的稿件有提成数据，无法撤回！";
        }
    }

    /**
     * 处理稿件表进账金额及进账状态
     *
     * @param list
     */
    private Boolean handleArticleIncome(List<IncomeArticle> list) {
        List<ArticleHistory> historyList = new ArrayList<>();
        User user = AppUtil.getUser();
        double dis = 0.00001d;
        String curMonth = DateUtils.getYearAndMonthStr2(new Date());//当前年月
        //如果是领款撤回.list为空，不用走一下业务，
        // 如果list不为空，则是分款撤回，需要把稿件中的回款撤回来
        if (list != null && list.size() > 0) {
            List<Article> articles = new ArrayList<>();
            //先判断是否有已经产生提成数据的稿件，
            // 如果有提成，返回false，不支持更改，如果没有提成，返回true，允许更改
            if (articleMapper.getInvoiceAndCommissionByArticleIds(list) > 0) {
                return false;
            }
            //这里有一个很绕的地方，incomeArticle是按id倒序来的，即后分款的，先撤回，这是为了避免以下情况
            //假设同一笔稿件A同一个进账B分款两次，假设稿件100元，第一次分款别的进账C先分5元，第二次进账B分款5元，第三次进账B分款100元，撤回的时候，我要撤回进账B的分款
            //错误的是：第二次进账的先撤回，这时稿件进账状态为1已回款，第三次进账后撤回，这时稿件进账状态为2部分回款，这两个article是放到list里一起执行的，前后顺序会导致结果不一样
            //正确的是：第三次进账先撤回，这时稿件进账状态为2部分回款，第二次进账的后撤回，这时稿件进账状态为2部分回款，状态一致，多执行一次也没有关系
            for (IncomeArticle incomeArticle : list) {
                Article article = articleMapper.get(Article.class, incomeArticle.getArticleId());
                Order order = orderService.get(article.getOrderId());
                ArticleHistory history = new ArticleHistory();
                BeanUtils.copyProperties(article, history);
                history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                history.setUserId(order.getUserId());
                history.setDeptId(order.getDepatId());
                history.setId(null);
                history.setArtId(article.getId());
                history.setEditDesc(IConst.article_change_income_del);

                Double profit;
                Double amount;
                try {
                    //分款还原要把稿件进账重置，提成和利润也要变
                    amount = incomeArticle.getAmount();//分到每个稿件的金额
                    //处理稿件表数据
                    if (Math.abs(article.getIncomeAmount() - amount) < dis) {
                        article.setIncomeAmount(0D);
                    } else {
                        article.setIncomeAmount(new BigDecimal(article.getIncomeAmount()).
                                subtract(new BigDecimal(amount)).
                                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    }
                    if (article.getIncomeAmount() > 0) {
                        //如果撤回后稿件中的进账小于报价，回款状态要变
                        if (article.getIncomeAmount() < (article.getSaleAmount() - 1)) {
                            if (Math.abs(article.getIncomeAmount()) < dis) {
                                article.setIncomeStates(IConst.FEE_STATE_SAVE);//未回款
                                profit = article.getSaleAmount() - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                            } else {
                                article.setIncomeStates(IConst.FEE_STATE_PROCESS);//部分回款
                                profit = article.getSaleAmount() - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                            }
                        } else {
                            article.setIncomeStates(IConst.FEE_STATE_FINISH);//已回款
                            //利润计算：回款完成，回款-成本-税金-退款金额-其他支出
                            profit = article.getIncomeAmount() - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                        }
                    } else {
                        article.setIncomeStates(IConst.FEE_STATE_SAVE);//未回款
                        //利润计算：回款完成，回款-成本-税金-退款金额-其他支出
                        profit = article.getSaleAmount() - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                    }
                } catch (Exception e) {
                    throw new QinFeiException(1002, "处理分款稿件的回款和利润出错！");
                }
                String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
//              满足两个条件回款就有异动：1、更改月份和稿件发布月份不一致，2、利润有变动
                Double alterProfit = profit - article.getProfit();
                if (!(curMonth.equals(issueMonth))
                        && Math.abs(alterProfit) > 0.01) {
                    article.setAlterFlag(IConst.article_alter_flag_true);
                    history.setAlterIncome(0 - amount);
                    history.setAlterLabel(IConst.article_alter_flag_true);
                    history.setAlterProfit(alterProfit);
                }
                history.setCreator(user.getId());
                history.setCreateTime(new Date());
                historyList.add(history);
                article.setProfit(profit);

                Integer incomeId = incomeArticle.getIncomeId();
                //如果撤回后稿件中还有回款，说明有其他进账分到了这个稿件，查出来，
                if (article.getIncomeStates() > 0) {
                    List<Map> temp = incomeMapper.queryIncomeByArticleIdAndNotIncomeId(article.getId(), incomeId);
                    if (temp != null && temp.size() > 0) {
                        Map map = temp.get(0);
                        if (!ObjectUtils.isEmpty(map)) {
                            article.setIncomeId(map.containsKey("incomeId") ? MapUtils.getInteger(map, "incomeId") : null);
                            article.setIncomeCode(map.containsKey("incomeCode") ? MapUtils.getString(map, "incomeCode") : null);
                            article.setIncomeAccount(map.containsKey("incomeAccount") ? MapUtils.getString(map, "incomeAccount") : null);
                            article.setIncomeMan(map.containsKey("tradeMan") ? MapUtils.getString(map, "tradeMan") : null);
                            article.setIncomeTotalAmount(map.containsKey("tradeAmount") ? MapUtils.getDouble(map, "tradeAmount") : null);
                            article.setIncomeDate(map.containsKey("tradeTime") ? (Date) map.get("tradeTime") : null);
                            article.setAssignDate(map.containsKey("assignDate") ? (Date) map.get("assignDate") : null);
                            articles.add(article);
                        }else{
                            article.setIncomeId(null);
                            article.setIncomeCode(null);
                            article.setIncomeAccount(null);
                            article.setIncomeMan(null);
                            article.setIncomeTotalAmount(null);
                            article.setIncomeDate(null);
                            article.setAssignDate(null);
                            articles.add(article);
                        }
                    } else {
                        article.setIncomeId(null);
                        article.setIncomeCode(null);
                        article.setIncomeAccount(null);
                        article.setIncomeMan(null);
                        article.setIncomeTotalAmount(null);
                        article.setIncomeDate(null);
                        article.setAssignDate(null);
                        articles.add(article);
                    }
                } else {
                    article.setIncomeId(null);
                    article.setIncomeCode(null);
                    article.setIncomeAccount(null);
                    article.setIncomeMan(null);
                    article.setIncomeTotalAmount(null);
                    article.setIncomeDate(null);
                    article.setAssignDate(null);
                    articles.add(article);
                }
            }
            if (articles == null || articles.size() == 0) {
                throw new QinFeiException(1002, "未查询到分款的稿件！");
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
                incomeMapper.updateArticleIncomeInfoBatch(articles);
            } catch (QinFeiException e) {
                throw new QinFeiException(1002, "撤回稿件中的分款金额出错！");
            }
        }
        return true;
    }

    @Override
    public List<Map> exportIncomeDetail(Map map, OutputStream outputStream) {
        List<Map> list = incomeUserMapper.listPgForAssignCW(map);
        String[] heads = {"进账编号", "收款账户", "付款账户", "付款人", "付款日期", "交易金额",
                "未领金额", "预领金额", "等级", "登记日期", "业务员",
                "部门", "已领款金额", "领款时间", "已分款金额", "可分款金额", "客户公司名称",
                "客户联系人", "供应商","供应商联系人","联系人电话", "媒体", "媒介", "标题", "链接", "发布日期", "分款日期", "报价", "已分款金额"};
        String[] fields = {"incomeCode", "accountName", "tradeBank", "tradeMan", "tradeTime", "tradeAmount",
                "unclaimedAmount", "preclaimedAmount", "level", "createTime", "userName",
                "deptName", "receiveAmount", "receiveTime", "assignAmount", "remainAmount", "companyName",
                "custName", "supplierName","supplierContactor","phone", "mediaName", "mediaUserName", "title", "link", "assignDate", "issuedDate", "saleAmount", "amount"};
        ExcelUtil.exportExcel("分款详情", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("tradeTime".equals(field) || "createTime".equals(field)
                        || "assignDate".equals(field) || "receiveTime".equals(field)
                        || "issuedDate".equals(field)) {
                    cell.setCellValue(value.toString());
//                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                } else if ("tradeAmount".equals(field) || "unclaimedAmount".equals(field)
                        || "preclaimedAmount".equals(field) || "receiveAmount".equals(field)
                        || "assignAmount".equals(field) || "remainAmount".equals(field)
                        || "saleAmount".equals(field) || "amount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else if ("phone".equals(field)){
                    if (value != null && !StringUtils.isEmpty(value) ){
                        String phone = EncryptUtils.decrypt(String.valueOf(value));
                        if(phone.length() >= 11){
                            String start = phone.length() > 11 ? "*****" : "****";
                            phone = phone.substring(0, 3) + start + phone.substring(phone.length() - 4, phone.length());
                        }else if(phone.length() >= 3){
                            phone = phone.substring(0, 1) + "***" + phone.substring(phone.length() - 1);
                        }else {
                            phone = "**";
                        }
                        cell.setCellValue(phone);
                    }else {
                        cell.setCellValue("");
                    }
                }else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    @Transactional
    public void exportTemplate(Map map, OutputStream outputStream) {
        // 创建一个excel
        XSSFWorkbook book = new XSSFWorkbook();
        // 创建需要用户填写的数据页
        // 设计表头
        XSSFSheet sheet1 = book.createSheet("sheet1");
        XSSFCellStyle cellStyle = book.createCellStyle();// 创建提示样式

        // XSSFFont ztFont = book.createFont();
        // ztFont.setColor(Font.COLOR_RED);
        // cellStyle.setFont(ztFont);
        XSSFRow row0 = sheet1.createRow(0);
        row0.setHeightInPoints((4 * sheet1.getDefaultRowHeightInPoints()));
        cellStyle.setWrapText(true);
        String info = "提示：1、有*号的列，必填，有*号的列为空时不会导入，导入不成功的会有弹框提示。\n" + "2、进款日期格式为“yyyy/MM/dd”,例如：2018/01/12。\n"
                + "3、级别为A或B或C，填其他内容会导致导入不成功。\n" +
                "4、级别说明：A级：仅财务可见；B级：财务和业务组部长可见，业务员不可见，C级：所有人可见。";
        row0.createCell(0).setCellValue(info);
        row0.getCell(0).setCellStyle(cellStyle);

        CellRangeAddress region = new CellRangeAddress(0, 0, 0, 5);
        sheet1.addMergedRegion(region);

        XSSFCellStyle headerStyle = book.createCellStyle();// 创建标题样式
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFRow row1 = sheet1.createRow(1);
        row1.setHeightInPoints(30);// 单元格高度
        sheet1.setColumnWidth(0, 4000);
        sheet1.setColumnWidth(1, 8000);
        sheet1.setColumnWidth(2, 8000);
        sheet1.setColumnWidth(3, 4000);
        sheet1.setColumnWidth(4, 3000);
        sheet1.setColumnWidth(5, 2000);
//        sheet1.setColumnWidth(6, 2000);
//        sheet1.setColumnWidth(7, 2000);

        row1.createCell(0).setCellValue("*编号");
        row1.createCell(1).setCellValue("*收款账号");
        row1.createCell(2).setCellValue("*进款人名称");
//        row1.createCell(3).setCellValue("*进款人账户");
        row1.createCell(3).setCellValue("*进款日期");
        row1.createCell(4).setCellValue("*进款金额");
        row1.createCell(5).setCellValue("*级别");
//        row1.createCell(7).setCellValue("*可见天数");

        try {
            book.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    @Transactional
    public String importIncome(File file) {
        StringBuffer buf = new StringBuffer();
        User user = AppUtil.getUser();
        Workbook workbook = null;
        List<Income> incomeList = new ArrayList<>();
        try {
            ZipSecureFile.setMinInflateRatio(-1.0d);
            FileInputStream fis = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (file.getName().toLowerCase().endsWith("xls")) {
                workbook = new HSSFWorkbook(fis);
            }
            // 得到一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            // 获得数据的总行数
            int totalRowNum = sheet.getLastRowNum();

            for (int i = 2; i <= totalRowNum; i++) {
                // 获得第i行对象
                Row row = sheet.getRow(i);
                if (row == null || StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(0)))) {
                    continue;
                } else {
                    Boolean flag = true;
                    for (int j = 1; j < 5; j++) {
                        if (row.getCell(j) == null || StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(j)))) {
                            buf.append(i + 1 + "行" + j + 1 + "列为空、");
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        Income income = new Income();
                        if (StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(0)))) {
                            buf.append(i + 1 + "行" + 1 + "列为空、");
                            continue;//
                        } else {
                            income.setCode(DataImportUtil.getValue(row.getCell(0)).trim());
                        }
//                        String bankNo = "" ;
//                        if (StringUtils.isEmpty(getValue(row.getCell(1)))) {
//                            buf.append(i + 1 + "行" + 2 + "列为空、");
//                            continue;//
//                        }else{
//                            bankNo = String.valueOf(row.getCell(1).getStringCellValue()) ;
//                        }
                        if (StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(1)))) {
                            buf.append(i + 1 + "行" + 2 + "列为空、");
                            continue;//
                        } else {
                            List<Account> list = accountMapper.getCompanyAccountByName(DataImportUtil.getValue(row.getCell(1)).trim(), user.getCompanyCode());
                            if (list != null && list.size() > 0) {
                                Account account = list.get(0);
                                income.setAccountId(account.getId());
                                income.setAccountName(account.getName());
                                income.setBankNo(account.getBankNo());
                            } else {
                                buf.append(i + 1 + "行收款账户信息不正确、");
                                continue;
                            }
                        }

                        if (StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(2)))) {
                            buf.append(i + 1 + "行" + 3 + "列为空、");
                            continue;//
                        } else {
                            income.setTradeMan(DataImportUtil.getValue(row.getCell(2)).trim());
                        }
//                        if (StringUtils.isEmpty(getValue(row.getCell(3)))) {
//                            buf.append(i + 1 + "行" + 4 + "列为空、");
//                            continue;//
//                        }else{
//                            String tradeBank = String.valueOf(row.getCell(3).getStringCellValue()) ;
//                            income.setTradeBank(tradeBank);
//                        }

                        // 处理日期格式
                        Cell cell3 = row.getCell(3);
                        if (cell3.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell3)) {
                            DataFormatter dataFormatter = new DataFormatter();
                            Format format = dataFormatter.createFormat(cell3);
                            Date date = cell3.getDateCellValue();
                            //String str = format.format(date);
                            //income.setTradeTime(DateUtils.parse(str, "yyyy/MM/dd hh:mm:ss"));
                            income.setTradeTime(date);

                        } else {
                            buf.append(i + 1 + "行4列、");
                            continue;
                        }
                        Cell cell4 = row.getCell(4);
                        if (cell4.getCellType() == CellType.NUMERIC) {
                            income.setTradeAmount(Double.parseDouble(new DecimalFormat("0.##").format(cell4.getNumericCellValue())));
                        } else {
                            buf.append(i + 1 + "行5列、");
                            continue;//
                        }
                        Cell cell5 = row.getCell(5);
                        String level = DataImportUtil.getValue(cell5).trim();
                        if (IConst.ACCOUNT_LEVEL_A.equals(level) || IConst.ACCOUNT_LEVEL_B.equals(level) || IConst.ACCOUNT_LEVEL_C.equals(level)) {
                            income.setLevel(cell5.getStringCellValue().trim());
                        } else {
                            buf.append(i + 1 + "行6列、");
                            continue;//
                        }

                        income.setVisiableDay(IConst.VISIABLE_DAY);
                        income.setUnclaimedAmount(income.getTradeAmount());
                        income.setPreclaimedAmount(0D);
                        income.setState(IConst.FEE_STATE_SAVE);
                        income.setCreateTime(new Date());
                        income.setCreator(user.getId());
                        income.setCompanyCode(user.getCompanyCode());
                        incomeList.add(income);
//                        incomeMapper.insert(income);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
            buf.append("行信息不正确。\n请核实信息后重试！");
            return buf.toString();
        } else {
            if (incomeList != null && incomeList.size() > 0) {
                int size = incomeList.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    List<Income> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(incomeList.get(j));
                    }
                    incomeMapper.saveBatch(insertData);
                }
            }
            return null;
        }
    }

    @Override
    public List<Map> exportIncome(Map map, OutputStream outputStream) {
        List<Map> list = incomeMapper.listPg(map);
        String[] heads = {"进账编号", "账户名称", "进账日期", "进账人", "进账金额", "未领金额", "预领金额",
                "录入日期", "等级", "领款人姓名"};
        String[] fields = {"code", "account_name", "trade_time", "trade_man", "trade_amount", "unclaimed_amount",
                "preclaimed_amount", "create_time", "level", "receiveInfo"};
        ExcelUtil.exportExcel("进账列表", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("trade_time".equals(field) || "create_time".equals(field)) {
                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                } else if ("trade_amount".equals(field) || "unclaimed_amount".equals(field) || "preclaimed_amount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    public Map reimburseSum(Map map) {
        User user = AppUtil.getUser();
        map.put("user", user);
        map.put("id", user.getId());
        Map<String, Object> resultMap = incomeMapper.reimburseSum(map);
        return initResult(resultMap);
    }

    private Map initResult(Map resultMap) {
        if (resultMap == null || resultMap.size() == 0) {
            resultMap = new HashMap<>();
            resultMap.put("tradeAmount", 0);
            resultMap.put("receiveAmount", 0);
            resultMap.put("assignAmount", 0);
            resultMap.put("remainAmount", 0);

        }
        return resultMap;
    }
}
