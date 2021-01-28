package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.fee.IncomeArticle;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IncomeArticleMapper extends BaseMapper<IncomeArticle,Integer> {


    @Select("select * from fee_income_article where state>-2 and id = #{id}")
    IncomeArticle getById(Integer id);

    @Select("select b.* from fee_income_article b where b.state>-2 and b.income_id = #{incomeId} and b.article_id = #{articleId}")
    IncomeArticle getIncomeArticle(@Param("incomeId") Integer incomeId, @Param("articleId") Integer articleId);

    @Delete("delete from fee_income_article where income_id = #{incomeId} and income_user_id = #{incomeUserId}")
    void deleteIncomeArticleByIncomeIdAndUserId(@Param("incomeId") Integer incomeId, @Param("incomeUserId") Integer incomeUserId) ;

    @Select("select * from fee_income_article where income_id=#{incomeId}")
    List<IncomeArticle> queryByIncomeId(@Param("incomeId") Integer incomeId) ;

    @Select("select * from fee_income_article where income_id=#{incomeId} and income_user_id = #{incomeUserId} order by id desc")
    List<IncomeArticle> queryByIncomeIdAndUserId(@Param("incomeId") Integer incomeId, @Param("incomeUserId") Integer incomeUserId) ;

    @Select("select * from fee_income_article where income_id=#{incomeId} and article_id = #{articleId} order by id desc")
    List<IncomeArticle> queryByIncomeIdAndArticleId(@Param("incomeId") Integer incomeId, @Param("articleId") Integer articleId) ;

    @Delete("delete from fee_income_article where income_id = #{incomeId} and article_id = #{articleId}")
    void deleteIncomeArticleByIncomeIdAndArticleId(@Param("incomeId") Integer incomeId, @Param("articleId") Integer articleId) ;
}
