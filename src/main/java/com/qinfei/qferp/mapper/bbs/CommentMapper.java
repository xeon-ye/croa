package com.qinfei.qferp.mapper.bbs;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.bbs.Comment;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 评论数据库接口
 * @author tsf
 */
public interface CommentMapper extends BaseMapper<Comment,Integer> {
    /**
     * 根据id查询评论
     * @param id
     * @return
     */
    @Select("select id,topic_id,content,user_id,user_name,create_time,parent_id,picture from bbs_comment where id=#{id}")
    Comment getById(Integer id);

    /**
     * 查询帖子的一级评论
     * @param topicId
     * @return
     */
    @Select({"<script>",
            "select c.id,c.topic_id,c.content,c.user_id,c.user_name,c.create_time,c.parent_id,c.picture,count(r.id) replyFlag " +
                    " from bbs_comment c left join bbs_reply r on c.id = r.comm_id where c.topic_id=#{topicId} ",
                    "<when test='userId!=null and userId!=\"\"'>" ,
                    " and c.user_id=#{userId}" ,
                    "</when>" ,
                    " group by c.id " ,
                    " order by c.create_time asc" ,
                    "</script>"})
    List<Comment> queryComment(@Param("topicId")Integer topicId,@Param("userId")Integer userId);

    /**
     * 查询帖子的所有评论
     * @param topicId
     * @return
     */
    @Select({"<script>",
            "select count(id) from bbs_comment where topic_id=#{topicId} " ,
                    "<when test='userId!=null and userId!=\"\"'>",
                    " and user_id=#{userId}" ,
                    "</when>" ,
                    "</script>"})
    int queryCommentCount(@Param("topicId")Integer topicId,@Param("userId")Integer userId);

    /**
     * 查询一级评论下有多少子评论
     * @param topicId
     * @return
     */
    @Select("select parent_id commentId,count(1) num from bbs_comment where parent_id!=0 GROUP BY commentId")
    List<Map> queryChildCommentNum(Integer topicId);

    /**
     * 查询帖子的子级评论
     * @param topicId
     * @param parentId
     * @return
     */
    @Select("select id,topic_id,content,user_id,user_name,create_time,parent_id,picture from bbs_comment where topic_id=#{topicId} and parent_id=#{parentId}")
    List<Comment> queryCommentByParentId(@Param("topicId") Integer topicId,@Param("parentId") Integer parentId);
}
