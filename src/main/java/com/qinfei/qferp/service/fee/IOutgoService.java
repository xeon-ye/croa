package com.qinfei.qferp.service.fee;

import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workup.WorkupRequestParam;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IOutgoService {


    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    PageInfo<Map> resetListPg(int pageNum, int pageSize, Map map);

    Outgo getById(Integer id) ;

    Outgo add(Outgo entity);

    Outgo edit(Outgo entity);

    void workupOutgo(Outgo outgo, WorkupRequestParam workupRequestParam);

    Outgo update(Outgo entity);

    /**
     * 1、修改稿件请款信息
     * 2、删除稿件和请款关系表
     * 3、处理借款表
     * 4、处理借款关系表
     * 5、处理还款表
     * 6、删除请款表
     * @param entity
     */
    void delById(Outgo entity);

    /**
     * 1、保存请款信息
     * 2、保存请款和稿件关系
     * 3、稿件中请款信息更改
     * @param map
     * @param user
     * @return
     */
    Outgo saveStepOne(Map map, User user);

    PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Integer id);

    PageInfo<Map> listPgForSelectArticle(int pageNum, int pageSize, Map map);

    Map listPgForSelectArticleSum(Map map);

    List<Map> queryBorrowById(Integer outgoId);

    Map querySumAmount(Integer id);

    /**
     * 1、处理请款和借款表关系
     * 2、处理还款表
     * 3、处理借款表
     * 4、处理请款表
     * @param map
     * @return
     */
    Double saveOutgoBorrow(Map map);
    /**
     * 1、处理借款表
     * 2、删除借款关系表
     * 3、删除还款表
     * 4、还原请款表本次还款金额
     * @param id
     */
    void cleanOutgoBorrow(Integer id);

    /**
     * 1、处理稿件请款状态
     * 2、处理还款状态
     * 3、处理请款表状态和待办
     * @param entity
     */
    Outgo confirm(Outgo entity,Map map);

    @Transactional
    Outgo changeAccount(Outgo entity, Map map);

    @Transactional
    void CWReject(Outgo entity);

    /**
     * 对应的时confirm方法的还原
     * 还原稿件请款状态
     * 还原还款表
     * 处理请款状态和待办
     * @param entity
     * @return
     */
    @Transactional
    Boolean CWReturn(Outgo entity);

    @Transactional
    void checkBtoB(Outgo entity,String desc);

    List<Map> exportOutgo(Map map, OutputStream outputStream);

    Integer queryOutgoId(Integer articleId);

    Map aggregateAmount(Map map);

    Map calculationOfTotal(Map map);

    List<Map> mediaGroupLeader(Integer mediaTypeId);

    int selectMediaType(Integer mediaTypeId);

    int backfill(Outgo outgo);

    List<String> HTLScompanyCode();

    String downloadData(Map<String, Object> param);

    String batchDownloadData(Map<String, Object> param);

    List<Map> listOutgoData(Map<String, Object> param);

}
