package com.qinfei.qferp.service.fee;

import com.qinfei.qferp.entity.fee.Invoice;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IInvoiceService {

    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    Invoice getById(Integer id) ;

    Invoice add(Invoice entity);

    Invoice edit(Invoice entity,Double invoiceAmount);

    Invoice update(Invoice entity);

    void delById(Invoice entity);

    PageInfo<Map> listPgForSelectArticle(int pageNum, int pageSize, Map map);

    PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Map map);

    Map listPgForSelectArticleSum(Map map);

    Invoice saveStepOne(Map map);

    Double getSumSaleAmountById(Integer id);

    @Transactional
    Invoice confirm(Invoice entity);

    Invoice invoice(Invoice entity,String desc);

    Integer queryInvoiceId(Integer articleId);

    @Transactional
    void CWReject(Invoice entity);

    @Transactional
    Boolean CWReturn(Invoice entity);

    Map querySumAmount(Integer invoiceId);

    List<Map> exportInvoice(Map map , OutputStream outputStream);

    List<User> getTaxType(String taxType);
}
