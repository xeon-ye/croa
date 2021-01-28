package com.qinfei.qferp.service.fee;

import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.fee.Income;
import com.qinfei.qferp.entity.fee.IncomeUser;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IIncomeService {


    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    Income getById(Integer id) ;

    Income add(Income entity);

    Income edit(Income entity);

    Income update(Income entity);

    void delById(Income entity);

    /**
     * 业务员领款
     * @param amount 金额
     * @return
     */
    Income receive(Income income,Double amount);

    /**
     * 分配领款，财务帮别人领款或部长帮员工领款
     * @param userId
     * @param amount
     * @return
     */
    Income dispatch(Income income,Integer userId,Double amount);

    /**
     * 领款退回
     * @param id
     * @param user
     * @param receiveAmount
     * @param list
     */
    void withdraw(Integer id,User user,Double receiveAmount,List<IncomeUser> list);

    PageInfo<Map> queryArticleForAssign(int pageNum, int pageSize, Map map);

    /**
     * 业务员分款给稿件
     * @param map
     * @param user
     */
    void assignArticle(Map map,User user);

    /**
     * 根据进账id查询出该进账的已分款详情
     * @param incomeId 进账id
     * @return
     */
    PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Integer incomeId);

    /**
     * 查询出当前用户的已领款列表
     * @param pageNum
     * @param pageSize
     * @param map
     * @return
     */
    PageInfo<Map> listPgForAssign(int pageNum, int pageSize, Map map);

    /**
     * 分款管理（财务）页面的列表数据，根据进账关联多张表查询
     * @param map
     * @return
     */
    PageInfo<Map> listPgForAssignCW(int pageNum, int pageSize, Map map);

    IncomeUser getIncomeUser(Integer incomeId, Integer userId);

    /**
     * 根据稿件id查询出当前稿件的进账列表
     * 一个稿件可能有多个进账
     * @param article
     * @return
     */
    List<Map> listPgByArticleId(Article article);

    List<Income> queryIncomeByAccountId(Integer accountId);

    /**
     * 根据进账id查询出当前进账的领款人列表
     * @param incomeId
     * @return
     */
    List<IncomeUser> queryIncomeUserByIncomeId(Integer incomeId);

    List<IncomeUser> queryIncomeUserByIncomeIdAndUserId(Integer incomeId, Integer userId);

    Map querySumAmount(Integer incomeId, Integer userId);

    String backAssign(Integer incomeId,Integer userId);

    String backIncome(Integer incomeId,Integer userId);

    String backAssignArticle(Integer incomeId,Integer articleId);

    List<Map> exportIncomeDetail(Map map, OutputStream outputStream);

    @Transactional
    void exportTemplate(Map map, OutputStream outputStream);

    @Transactional
    String importIncome(File file);

    List<Map> exportIncome(Map map, OutputStream outputStream);

    Map reimburseSum(Map map);

}
