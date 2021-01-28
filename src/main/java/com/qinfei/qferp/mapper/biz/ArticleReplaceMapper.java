package com.qinfei.qferp.mapper.biz;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.ArticleReplace;

import java.util.List;

/**
 * 稿件媒体供应商替换记录表(TBizArticleReplace)表数据库访问层
 *
 * @author xuxiong
 * @since 2020-08-07 09:41:55
 */
public interface ArticleReplaceMapper extends BaseMapper<ArticleReplace, Integer> {

    //批量新增
    int saveBatch(List<ArticleReplace> articleReplaceList);

}