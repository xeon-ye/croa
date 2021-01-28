package com.qinfei.qferp.mapper.biz;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.ArticleHistory;

import java.util.List;
import java.util.Map;

public interface ArticleHistoryMapper extends BaseMapper<ArticleHistory, Integer> {
    int saveBatch(List<ArticleHistory> list);

    List<Map<String,Object>> queryArticleChange(Map map);

    Map queryArticleChangeSum(Map map);

    Map queryArticleSaleAmountSum(Map map);

    List<Map<String,Object>> queryArticleChangeDetail(Map map);

    Map queryArticleChangeDetailSum(Map map);

    List<Map<String,Object>> queryArticleChangeSingle(Map map);

    Map queryArticleChangeSingleSum(Map map);
}
