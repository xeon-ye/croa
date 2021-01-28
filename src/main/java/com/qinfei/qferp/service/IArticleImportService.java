package com.qinfei.qferp.service;

import com.qinfei.qferp.entity.biz.ArticleImport;
import com.qinfei.qferp.entity.biz.Order;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IArticleImportService {
    PageInfo<Map> listPgMJ(int pageNum, int pageSize, Map map);
    PageInfo<Map> listPgYW(int pageNum, int pageSize, Map map);

    ArticleImport getById(Integer id);

    ArticleImport add(ArticleImport entity);

    ArticleImport edit(ArticleImport entity);

    Boolean delById(Integer id);

    @Transactional
    Map batchDel(String ids);

    @Transactional
    String batchOrder(File file);

    void complete(Order order, Map map);

    int updateAmountAndBrand(Map map);

    List<Map> exportArticleYW(Map map, OutputStream outputStream);

    Map checkCustInfo(String ids);

    PageInfo<Map> queryArticleByIds(int pageNum, int pageSize, String ids);

    Map getArticleImportSum(Map map);

    Map querySumArticleByIds(String ids);

    List<Map> exportArticleMJ(Map map, OutputStream outputStream);

    @Transactional
    void batchSaleAmount(File file);
}
