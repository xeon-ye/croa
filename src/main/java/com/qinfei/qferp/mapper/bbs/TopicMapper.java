package com.qinfei.qferp.mapper.bbs;
import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.bbs.Record;
import com.qinfei.qferp.entity.bbs.Reply;
import com.qinfei.qferp.entity.bbs.Topic;
import org.apache.ibatis.annotations.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

    /**
     * 论坛帖子数据库接口
     * @author tsf
     */
     public interface TopicMapper extends BaseMapper<Topic,Integer> {
        /**
         * 根据id查询帖子信息
         * @param topicId
         * @return
         */
        @Select({"<script>",
                "select " +
                        "count(case when r.state in(0,1,2) then 3 else NULL end) viewNum," +
                        "count(case when r.state = 1 then 1 else NULL end) likeNum," +
                        "count(case when r.state = 2 then 2 else NULL end) dislikeNum," +
                        "t.id,t.user_id userId,t.user_name userName,t.title,t.sort,t.level,t.content,f.id forumId,f.name forumName,f.moderator moderator,t.high_reason highReason," +
                        "t.top_state topState,t.high_state highState,t.top_reason topReason,t.affix_name affixName,t.affix_link affixLink,DATE_FORMAT(t.create_time,\"%Y-%m-%d %H:%i:%s\") createTime,t.image_flag,t.image_name,t.image_url " +
                        " FROM bbs_topic t" +
                        " LEFT JOIN bbs_forum f on t.forum_id=f.id" +
                        " LEFT JOIN bbs_record r on t.id = r.topic_id" +
                        " where t.state>-2 and t.id=#{topicId}" +
                        "</script>"})
        Map getById(Integer topicId);

        /**
         * 根据topicId查询帖子详情
         * @param topicId
         * @return
         */
        @Select("select * from bbs_topic where state>-2 and id=#{topicId}")
        Topic queryById(Integer topicId);

        /**
         * 查询当日板块发帖数(板块)
         * @param companyCode
         * @param forumId
         * @return
         */
        @Select("select count(1) from bbs_topic where forum_id = #{forumId} and create_time >= curdate() and company_code=#{companyCode} and state>-2 UNION " +
                "select count(1) from bbs_topic where forum_id = #{forumId} and company_code=#{companyCode} and state>-2")
        List<Integer> queryTopicByToday(@Param("companyCode") String companyCode,@Param("forumId") Integer forumId);

        /**
         * 查询当日板块发帖数(我的)
         * @param companyCode
         * @param forumId
         * @param userId
         * @return
         */
        @Select("select count(1) from bbs_topic where forum_id = #{forumId} and create_time >= curdate() and company_code=#{companyCode} and user_id=#{userId} and state>-2 UNION " +
                "select count(1) from bbs_topic where forum_id = #{forumId} and company_code=#{companyCode} and user_id=#{userId} and state>-2")
        List<Integer> queryTodayByUserId(@Param("companyCode") String companyCode,@Param("forumId") Integer forumId,@Param("userId") Integer userId);


         /**
         * 根据板块id查询所有的帖子信息
         * @param forumId
         * @return
         */
        @Select("select t.* from bbs_topic t where t.state>-2 and t.forum_id=#{forumId}")
        List<Topic> getByForumId(Integer forumId);

        /**
         * 查询所有的帖子信息(论坛列表查询自己可见的帖子)
         * @Param map
         * @return
         */
//        @Select({"<script>" ,
//                    "select " +
//                        "count(case when r.state in(0,1,2) then 3 else NULL end) viewNum,"+
//                        "count(case when r.state = 1 then 1 else NULL end) likeNum,"+
//                        "count(case when r.state = 2 then 2 else NULL end) dislikeNum,"+
//                        "t.id,t.user_id,t.user_name,t.title,t.create_time,t.picture,t.state,t.top_state topState,t.high_state highState," +
//                        "t.sort,t.level,f.name forumName,f.moderator moderator,t.high_reason highReason,t.top_reason topReason "+
//                        " FROM bbs_topic t" +
//                        " LEFT JOIN bbs_forum f on t.forum_id=f.id" +
//                        " LEFT JOIN bbs_record r on t.id = r.topic_id"+
//                        " where t.state>-2 "+
//                        "<when test='userCompanyCode==\"JT\" and companyCode!=\"JT\"'> ",
//                        " and t.company_code = #{companyCode} ",
//                        " </when> ",
//                        "<when test='userCompanyCode==\"JT\" and companyCode==\"JT\"'> ",
//                        " and t.id in (select topicId from bbs_relationship where (userId=#{userId} or viewflag='JT') ",
//                        "</when> ",
//                        "<when test='companyCode!=\"JT\" and companyCode!=null and flag!=1'>",
//                        " and t.id in (select topicId from bbs_relationship where userId=#{userId})",
//                        "</when>",
//                        "<when test='forumId!=null and forumId!=\"\"'>",
//                             " and t.forum_id = #{forumId}",
//                        "</when>",
//                        "<when test='titleQc!=null and titleQc!=\"\"'>",
//                        " and t.title like '%${titleQc}%'",
//                        "</when>",
//                        "<when test='topState!=null and topState!=\"\"'>",
//                        " and t.top_state = #{topState}",
//                        "</when>",
//                        "<when test='highState!=null and highState!=\"\"'>",
//                        " and t.high_state = #{highState}",
//                        "</when>",
//                        " group by t.id ",
//                        " order by t.sort asc,t.create_time desc",
//                "</script>"})
        List<Topic> queryTopic(Map map);

        List<Topic> querySearchTopic(Map map);
        //查询可见帖子总数
        int getTopicCount(Map map);
        //论坛评论，回复数统计
        List<Map> queryCommReplyNum(@Param("forumId") Integer forumId,@Param("list") List<Integer> list);

//        @Select({"<script>" ,
//                "select " +
//                    "count(case when r.state in(0,1,2) then 3 else NULL end) viewNum,"+
//                    "count(case when r.state = 1 then 1 else NULL end) likeNum,"+
//                    "count(case when r.state = 2 then 2 else NULL end) dislikeNum,"+
//                    "t.id,t.user_id,t.user_name,t.title,t.create_time,t.picture,t.state,t.top_state topState,t.high_state highState," +
//                    "t.sort,t.level,f.name forumName,f.moderator moderator,t.high_reason highReason,t.top_reason topReason "+
//                    " FROM bbs_topic t" +
//                    " LEFT JOIN bbs_forum f on t.forum_id=f.id" +
//                    " LEFT JOIN bbs_record r on t.id = r.topic_id"+
//                    " where t.state>-2 "+
//                "<when test='companyCode!=\"JT\" and companyCode!=null and flag!=1'>",
//                " and t.company_code = #{companyCode} and t.id in (select topicId from bbs_relationship where userId=#{userId})",
//                "</when>",
//                "<when test='companyCode!=\"JT\" and companyCode!=null and flag==1'>",
//                " and t.company_code = #{companyCode} ",
//                "</when>",
//                "<when test='companyCode==\"JT\"'>",
//                " and t.id in (select topicId from bbs_relationship where (userId=#{userId} or viewflag='JT'))",
//                "</when>",
//                " group by t.id ",
//                " order by t.sort asc,t.create_time desc",
//                "</script>"})
        List<Map> queryTopicByWorkbench(Map map);

         /**
          * 查询所有的帖子信息(论坛管理中心：我发表的帖子，我点赞的帖子)
          * @param map
          * @return
          */
         @Select({"<script>",
                 "select " +
                 "count(case when r.state in(0,1,2) then 3 else NULL end) viewNum,"+
                 "count(case when r.state =1 then 1 else NULL end) likeNum,"+
                 "count(case when r.state = 2 then 2 else NULL end) dislikeNum,"+
                 "t.id,t.user_id,t.user_name,t.title,t.create_time,t.picture,t.state,t.top_state topState,t.high_state highState," +
                 "t.sort,t.level,t.forum_id forumId,f.name forumName,f.moderator moderator,t.high_reason highReason,t.top_reason topReason "+
                 " FROM bbs_topic t" +
                 " LEFT JOIN bbs_forum f on t.forum_id=f.id" +
                 " LEFT JOIN bbs_record r on t.id = r.topic_id"+
                 " where t.state>-2 and t.company_code = #{companyCode} "+
                 "<when test='forumId!=null and forumId!=\"\"'>",
                 " and t.forum_id = #{forumId}",
                 "</when>",
                 "<when test='userId!=null and userId!=\"\"'>",
                 " and t.user_id = #{userId}",
                 "</when>",
                 "<when test='recordUserId!=null and recordUserId!=\"\"'>",
                 " and r.user_id = #{recordUserId}",
                 "</when>",
                 "<when test='moderator!=null and moderator!=\"\"'>",
                 " and f.moderator = #{moderator}",
                 "</when>",
                 "<when test='titleQc!=null and titleQc!=\"\"'>",
                 " and t.title like '%${titleQc}%'",
                 "</when>",
                 "<when test='postState!=null and postState!=\"\"'>",
                 " and r.state = #{postState}",
                 "</when>",
                 " group by t.id",
                 " order by t.sort asc,t.create_time desc",
                 "</script>"})
         List<Topic> queryTopicManage(Map map);

        int queryPublishNum(Map map);

        int queryPostNum(Map map);
        //管理中心点赞阅读数，点赞数统计错误（不包含其他人）
        List<Map> queryPostStatistics(Map map);

        List<Integer> queryReplyNum(Map map);

        /**
         * 查询所有的帖子信息(论坛管理中心：我回复的帖子)
         * @param map
         * @return
         */
//        @Select({"<script>",
//                "select " +
//                        "t.id,t.user_id,t.user_name,t.title,t.create_time,t.picture,t.state,t.top_state topState,t.high_state highState," +
//                        "t.sort,t.level,f.name forumName,f.moderator moderator,t.high_reason highReason,t.top_reason topReason " +
//                        " FROM bbs_topic t" +
//                        " LEFT JOIN bbs_forum f on t.forum_id=f.id " +
//                        " LEFT JOIN bbs_reply r on t.id = r.topic_id " +
//                        " where t.state>-2 and t.company_code = #{companyCode} " +
//                        "<when test='userId!=null and userId!=\"\"'>",
//                " and r.reply_user_id = #{userId}",
//                "</when>",
//                "<when test='titleQc!=null and titleQc!=\"\"'>",
//                " and t.title like '%${titleQc}%'",
//                "</when>",
//                " group by t.id",
//                " order by t.sort asc,t.create_time desc",
//                "</script>"})
        List<Topic> queryReplyTopic(Map map);

        List<Map> getReplyNumbersByTids(@Param("ids")List<Integer> ids);

         /**
          * 热门推荐查询的帖子（按评论数排序）
          * @param map
          * @return
          */
//         @Select({"<script>" ,
//                 "select t.id,t.user_name,t.title,t.create_time,t.picture,count(CASE when c.id then 1 else NULL END) commNum " +
//                 " FROM bbs_topic t" +
//                 " LEFT JOIN bbs_forum f on t.forum_id=f.id " +
//                 " LEFT JOIN bbs_comment c on t.id = c.topic_id "+
//                 " where t.state>-2 "+
//                 "<if test='companyCode!=\"JT\" and companyCode!=null and flag!=1'>",
//                 " and t.company_code = #{companyCode} and t.id in (select topicId from bbs_relationship where userId=#{userId})",
//                 "</if>",
//                 "<if test='companyCode==\"JT\"'>",
//                 " and t.id in (select topicId from bbs_relationship where (userId=#{userId} or viewflag='JT'))",
//                 "</if>",
//                 " and t.forum_id = #{forumId}",
//                 " group by t.id",
//                 " order by commNum DESC LIMIT 5",
//                 "</script>"})
         List<Map> queryHotTopic(Map map);

        /**
         * 首页帖子推荐（按查看数排序）
         * @param map
         * @return
         */
         List<Topic> queryFirstInfo(Map map);

        /**
         * 删除帖子信息
         * @param updateTime
         * @param topicId
         * @param userId
         * @param userName
         */
        @Update("update bbs_topic set state=-9,del_reason=#{reason},update_time=#{updateTime},update_user_id=#{userId},update_user_name=#{userName} where id=#{topicId}")
        void delTopic(@Param("reason")String reason,@Param("updateTime")Date updateTime,@Param("topicId") Integer topicId,@Param("userId") Integer userId,@Param("userName") String userName);

         /**
          * 申请加精
          * @param topicId
          * @param highReason
          */
         @Update("update bbs_topic set high_state =#{state},high_reason=#{highReason} where id=#{topicId}")
         void updateLevel(@Param("topicId") Integer topicId,@Param("highReason") String highReason,@Param("state")Integer state);

         /**
          * 取消加精
          * @param topicId
          * @param updateTime
          * @param userId
          * @param userName
          */
        @Update("update bbs_topic set high_state =0,level=0,update_time=#{updateTime},update_user_id=#{userId},update_user_name=#{userName} where id=#{topicId}")
        void notAddLevel(@Param("topicId") Integer topicId,@Param("updateTime")Date updateTime,@Param("userId") Integer userId,@Param("userName") String userName);

        /**
         * 加精
         * @param topicId
         */
        @Update("update bbs_topic set level =1,high_state=2 where id=#{topicId}")
        void addLevel(@Param("topicId") Integer topicId);

         /**
          * 申请置顶
          * @param topicId
          * @param topReason
          */
         @Update("update bbs_topic set top_state =#{state},top_reason=#{topReason} where id=#{topicId}")
         void updateTop(@Param("topicId") Integer topicId,@Param("topReason") String topReason,@Param("state") Integer state);

         /**
          * 取消置顶
          * @param topicId
          * @param updateTime
          * @param userId
          * @param userName
          */
        @Update("update bbs_topic set top_state =0,sort=9,update_time=#{updateTime},update_user_id=#{userId},update_user_name=#{userName} where id=#{topicId}")
        void notGoTop(@Param("topicId") Integer topicId,@Param("updateTime")Date updateTime,@Param("userId") Integer userId,@Param("userName") String userName);

        /**
         * 置顶
         * @param topicId
         */
        @Update("update bbs_topic set sort =0,top_state=2 where id=#{topicId}")
        void goTop(@Param("topicId") Integer topicId);

        /**
         * 根据帖子id查询所有可见人id
         * @param topicId
         * @return
         */
        @Select("select rs.userId id,rs.viewflag viewFlag,u.name name,rs.comm_user_ids commUserIds from bbs_relationship rs left join sys_user u on rs.userId = u.id where rs.topicId = #{topicId} and u.handover_state=0")
        List<Map> queryTopicRelation(Integer topicId);

        /**
         * 根据帖子id查询评论权限
         * @param topicId
         * @Param userId
         * @return
         */
        @Select("select DISTINCT rs.commflag from bbs_relationship rs where rs.topicId = #{topicId} and rs.userId=#{userId}")
        Integer queryRelationFlag(@Param("topicId") Integer topicId,@Param("userId")Integer userId);

        /**
         * 根据帖子id查询全部可见的评论人员集合
         * @param topicId
         * @Param userId
         * @return
         */
        @Select("select DISTINCT rs.comm_user_ids from bbs_relationship rs where rs.topicId = #{topicId} and rs.userId=#{userId}")
        List<String> queryCommIds(@Param("topicId") Integer topicId,@Param("userId")Integer userId);

        /**
         * 帖子可见评论设置(评论人员集合comm)
         * @param list
         */
        @Insert({"<script>" +
                "insert into bbs_relationship(topicId,userId,commflag,viewflag,comm_user_ids) values" +
                "<foreach collection='list' item='item' separator=','>" +
                "(#{item.topicId}," +
                "#{item.userId}," +
                "#{item.flag}," +
                "#{item.viewFlag}," +
                "#{item.commUserIds})" +
                "</foreach>" +
                "</script>"})
        void saveTopicRelation(List<Map> list);

        /**
         * 删除可见人员绑定
         * @param topicId
         */
        @Delete("DELETE from bbs_relationship where topicId = #{topicId}")
        void delTopicRelation(Integer topicId);

        //获取论坛首页板块信息的回复数
        Integer getForumReplyNum(Integer topicId);

        /**
         * 修改帖子回显主题图单选框状态
         * @param companyCode
         * @param topicId
         * @return
         */
        List<Topic> queryTopicImage(@Param("companyCode") String companyCode,@Param("topicId") Integer topicId);

        /**
         * 论坛首页主题图推荐
         * @param companyCode
         * @return
         */
        List<Topic> queryFirstImage(@Param("companyCode") String companyCode);
}
