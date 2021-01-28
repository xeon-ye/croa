package com.qinfei.qferp.mapper.accountsMess;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.accountsMess.AccountsMess;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface accountsMessMapper extends BaseMapper<AccountsMess,Integer> {

    List<Map<String,Object>> selectArticleList(Map<String,Object> map);

    List<Map<String,Object>> selectHaveArticleList(Map<String,Object> map);

    List<Map<String,Object>> getCustdockingPeople(Map<String,Object> map);

    int saveAccountsMess(AccountsMess accountsMess);

    int addArticle(List<Map<String,Object>> list);

    int updateArticle(@Param("id") Integer id, @Param("state") Integer state);

    List<AccountsMess> selectMessListTable(Map<String,Object> map);

    AccountsMess selectAccountMess(Integer id);

    int updateMess(AccountsMess accountsMess);

    int updateArt(@Param("id") Integer id, @Param("state") Integer state);

    int updateMessArt(Integer id);

    Integer selectMessId(Integer artId);

}
