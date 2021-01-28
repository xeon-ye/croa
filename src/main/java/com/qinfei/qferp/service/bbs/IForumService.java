package com.qinfei.qferp.service.bbs;

import com.qinfei.qferp.entity.bbs.Forum;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 论坛板块接口
 * @author tsf
 */
public interface IForumService {
    /**
     * 根据id查询论坛信息
     * @param id
     * @return
     */
    Forum queryById(Integer id);

    /**
     * 论坛板块去重
     * @param companyCode
     * @param name
     * @return
     */
    Boolean checkForum(String companyCode,String name,Integer id);

    /**
     * 根据公司代码查询公司名称
     * @param companyCode
     * @return
     */
    String queryCompanyCode(String companyCode);

    /**
     * 查询所有的板块
     * @param map
     * @return
     */
    PageInfo<Map> queryForum(Map map, Pageable pageable);

    /**
     * 论坛管理查询分公司下的论坛板块
     * @Param companyCode
     * @return
     */
    List<Map> queryForum(String companyCode);

    List<Map> getForumData(String companyCode);

    /**
     * 添加版块信息
     * @param forum
     */
    void addForum(Forum forum);

    /**
     * 修改版块信息
     * @param forum
     */
    void updateForum(Forum forum);

    /**
     * 删除论坛信息
     * @param id
     */
    void  delForum(Integer id);

    /**
     * 获取板块信息（版主，id,公司代码，板块名称）
     * @param forumId
     * @return
     */
    Map getForumInfo(Integer forumId);

    Forum getById(Integer id);

    /**
     * 获取当前人是否有板块权限
     * @return
     */
    List<Forum> getForumByBanzu(String companyCode,Integer forumId);
}
