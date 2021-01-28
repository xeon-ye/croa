package com.qinfei.qferp.service.biz;

import com.github.pagehelper.PageInfo;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IArticleHistoryService {

    PageInfo<Map<String,Object>> queryArticleChange(int pageNum, int pageSize, Map map);

    Map queryArticleChangeSum(Map map);

    Map queryArticleSaleAmountSum(Map map);

    PageInfo<Map<String,Object>> queryArticleChangeDetail(int pageNum, int pageSize, Map map);

    Map queryArticleChangeDetailSum(Map map);

    List<Map<String,Object>> exportArticleChange(Map map, OutputStream outputStream);

    List<Map<String,Object>> exportArticleChangeDetail(Map map, OutputStream outputStream);

    PageInfo<Map<String, Object>> queryArticleChangeSingle(int pageNum, int pageSize, Map map);

    Map queryArticleChangeSingleSum(Map map);
}
