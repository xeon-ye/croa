package com.qinfei.qferp.service.fee;

import com.qinfei.qferp.entity.fee.Refund;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IRefundService {


    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    Refund getById(Integer id) ;

    Refund add(Refund entity);

    Refund edit(Refund entity);

    Refund update(Refund entity);

    void delById(Refund entity);

    Refund changeAccount(Refund entity,Map<Object,String> map);

    /**
     * 没有提成和退款的稿件
     */
    PageInfo<Map> listPgForSelectArticle(int pageNum, int pageSize, Map map);

    /**
     * 没有提成和其他支出的稿件
     */
    PageInfo<Map> listPgForSelectArticle2(int pageNum, int pageSize, Map map);

    Refund saveStepOne(Map map);

    PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Integer id);

    Map querySumAmountById(Integer refundId);

    Refund confirm(Refund entity,Map map);

    @Transactional
    void CWReject(Refund entity);

    @Transactional
    Boolean CWReturn(Refund entity);

    @Transactional
    void checkBtoB(Refund entity);

    Integer queryRefundId(Integer articleId);

    Integer queryOtherPayId(Integer articleId);

    List<Map> exportRefund(Map map, OutputStream outputStream);

    Map reimburseSum(Map map);

    String downloadData(Map<String, Object> param);

    String batchDownloadData(Map<String, Object> param);

    List<Map> listRefundData(Map<String, Object> param);

    boolean getFlowPrintPermission(HttpServletRequest request);
}
