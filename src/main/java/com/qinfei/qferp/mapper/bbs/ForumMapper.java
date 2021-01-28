package com.qinfei.qferp.mapper.bbs;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.bbs.Forum;
import org.apache.ibatis.annotations.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 论坛板块数据库接口
 * @author tsf
 */
public interface ForumMapper extends BaseMapper<Forum,Integer> {

    /**
     * 根据id查询论坛信息
     * @param id
     * @return
     */
    @Select("select a.id,a.`name`,a.moderator,a.user_name userName,DATE_FORMAT(a.create_time,\"%Y-%m-%d %H:%i:%s\") createTime,a.company_code companyCode,a.remark,a.affix_name affixName,a.affix_link affixLink from bbs_forum a where a.state>-2 and a.id = #{id}")
    Forum queryById(Integer id);

    /**
     * 根据公司代码查询公司名称
     * @param companyCode
     * @return
     */
    @Select("select DISTINCT(company_code_name) from sys_dept WHERE state>-2 and level=1 and company_code=#{companyCode}")
    String queryCompanyCode(String companyCode);

    /**
     * 根据id查询论坛信息
     * @param id
     * @return
     */
    @Select("select * from bbs_forum where state>-2 and id = #{id}")
    Forum findById(Integer id);

    /**
     * 论坛板块去重
     * @param companyCode
     * @param name
     * @return
     */
    @Select("select * from bbs_forum where state>-2 and company_code=#{companyCode} and name=#{name}")
    List<Forum> checkForum(@Param("companyCode") String companyCode,@Param("name") String name);

    /**
     * 论坛管理查询分公司下的论坛板块
     * @param companyCode
     * @return
     */
    @Select("select a.id,a.name name,a.moderator,b.name moderatorName,a.company_code,a.affix_link affixLink,a.affix_name affixName from bbs_forum a left JOIN sys_user b on a.moderator=b.id where a.state>-2 and a.company_code=#{companyCode}")
    List<Map> findForum(@Param("companyCode") String companyCode);

    /**
     * 查询所有的板块
     * @param map
     * @return
     */
    @Select({"<script>"+
            "select DISTINCT(a.id),a.name,b.name chargeMan,a.user_id,a.user_name,a.moderator,a.create_time,a.update_time,a.company_code,a.state,a.remark" +
            " from bbs_forum a " +
            " left join sys_user b on a.moderator=b.id "+
            " where a.state>-2 and b.state>-2 and a.company_code=#{companyCode}"+
            "<when test='nameQc!=null and nameQc!=\"\"'>" ,
                " and a.name like '%${nameQc}%'",
            "</when>",
            "<when test='startTimeQc!=null and startTimeQc!=\"\"'>" ,
                 " and a.create_time &gt;= #{startTimeQc}",
            "</when>",
            "<when test='endTimeQc!=null and endTimeQc!=\"\"'>" ,
                " and a.create_time &lt;= #{endTimeQc}",
            "</when>",
            "</script>"})
    List<Map> queryForum(Map map);

    /**
     * 删除论坛信息
     * @param updateTime
     * @param id
     */
    @Update("update bbs_forum set state=-9 ,update_time=#{updateTime} where id = #{id}")
    void  delForum(@Param("updateTime") Date updateTime,@Param("id") Integer id);

    @Update({"<script>",
            " update bbs_forum "+
            "<trim prefix=\"set\" suffixOverrides=\",\">" +
            "<if test='name!=null'>",
            " name=#{name}," ,
            "</if>" ,
            "<if test='moderator!=null and moderator!=\"\"'>",
            " moderator=#{moderator}," ,
            "</if>" ,
            "<if test='remark!=\"\"'>",
            " remark=#{remark}," ,
            "</if>" ,
            "<if test='affixLink!=\"\" and affixLink!=null'>",
            " affix_link=#{affixLink}," ,
            "</if>" ,
            "<if test='affixName!=\"\" and affixName!=null'>",
            " affix_name=#{affixName}," ,
            "</if>" ,
            "<if test='updateTime!=null'>",
            " update_time=#{updateTime}," ,
            "</if>" ,
            "</trim>",
            " where id=#{id}" ,
            "</script>"})
    void updateForum(Forum t);

    @Select("select a.id forumId,a.`name` forumName,a.company_code companyCode,b.`name` moderator FROM bbs_forum a LEFT JOIN sys_user b on a.moderator=b.id where a.state>-2 where a.id=#{forumId}")
    Map getForumInfo(@Param("forumId")int forumId);

    /**
     * 论坛首页获取板块信息
     * @param companyCode
     * @return
     */
    @Select({"<script>"+
            "select a.id,a.name name,a.moderator,b.name moderatorName,a.company_code,a.affix_link affixLink,a.affix_name affixName,count(t.id) topicNum " +
            "from bbs_forum a " +
            "left JOIN sys_user b on a.moderator=b.id " +
            "left join bbs_topic t on a.id = t.forum_id and t.state>-2 " +
            "where a.state>-2 and a.company_code=#{companyCode} " +
            "group by a.id "+
            "</script>"})
    List<Map> getForumData(@Param("companyCode") String companyCode);
    //获取当前人是否有版主权限
    @Select({"<script>"+
            "select a.id,a.name name,a.moderator,b.name moderatorName,a.company_code " +
            "from bbs_forum a " +
            "LEFT JOIN sys_user b on a.moderator=b.id " +
            "where a.state>-2 and a.moderator=#{moderator} and a.company_code=#{companyCode} and a.id=#{forumId} " +
            "group by a.id"+
            "</script>"})
    List<Forum> getForumByBanzu(Map map);
}
