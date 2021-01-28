package com.qinfei.qferp.mapper.biz;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleExtend;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ArticleExtendMapper extends BaseMapper<ArticleExtend, Integer> {

    @Insert({"<script>",
            " insert into t_biz_article_extend (" +
                    "article_id," +
                    "project_id," +
                    "project_code," +
                    "project_name" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.articleId}," +
                    "#{item.projectId}," +
                    "#{item.projectCode}," +
                    "#{item.projectName})" +
                    "</foreach>",
            "</script>"})
    void saveExtendBatch(List<ArticleExtend> list);

    @Delete({"<script>" +
            " delete from t_biz_article_extend " +
            " where article_id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">\n" +
            "       #{item.id}" +
            "   </foreach>" +
            "</script>"})
    Integer deleteExtendBatch(List<Article> list);

    @Delete({"<script>" +
            " delete from t_biz_article_extend " +
            " where article_id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">\n" +
            "       #{item.id}" +
            "   </foreach>" +
            "</script>"})
    Integer deleteExtendBatch2(List<Map<String,Object>> list);
}