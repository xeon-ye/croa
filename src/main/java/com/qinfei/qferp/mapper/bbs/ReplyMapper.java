package com.qinfei.qferp.mapper.bbs;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.bbs.Reply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 论坛帖子数据库接口
 * @author tsf
 */
public interface ReplyMapper extends BaseMapper<Reply,Integer> {
    /**
     * 查询帖子回复记录
     * @param commentId
     * @return
     */
    @Select({"<script>",
            "select * from bbs_reply where comm_id=#{commentId} " ,
            "<when test='userId!=null and userId!=\"\"'>",
            " and reply_user_id=#{userId} " ,
            "</when>" ,
            " order by create_time desc",
            "</script>"})
    List<Reply> queryReply(@Param("commentId") Integer commentId,@Param("userId") Integer userId);

    /**
     * 根据id查询回复人姓名
     * @param replyId
     * @return
     */
    @Select("select reply_user_name from bbs_reply where id = #{replyId}")
    String queryReplyById(@Param("replyId") Integer replyId);
}
