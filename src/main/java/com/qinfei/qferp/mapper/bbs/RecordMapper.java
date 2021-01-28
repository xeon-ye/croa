package com.qinfei.qferp.mapper.bbs;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.bbs.Record;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

public interface RecordMapper extends BaseMapper<Record,Integer> {
    /**
     *  修改帖子点赞记录
     * @param state
     * @param topicId
     * @param userId
     */
    @Update("update bbs_record set state=#{state} where topic_id=#{topicId} and user_id=#{userId}")
    void updateTopicRecord(@Param("state")Integer state, @Param("topicId") Integer topicId, @Param("userId")Integer userId);

    /**
     * 查询帖子点赞记录(防止重复点赞)
     * @param topicId
     * @param userId
     * @return
     */
    @Select("select * from bbs_record where topic_id=#{topicId} and user_id=#{userId}")
    com.qinfei.qferp.entity.bbs.Record queryRecord(@Param("topicId")Integer topicId, @Param("userId") Integer userId);

    /**
     * 根据点赞记录表统计点赞数
     * @param topicId
     * @return
     */
    @Select("SELECT count(CASE WHEN state = 1 THEN 1 else null END) likeNum,count(CASE WHEN state = 2 THEN 2 else null  END) dislikeNum," +
            "count(CASE WHEN state in(0,1,2) THEN 3 else null END) viewNum " +
            "FROM bbs_record WHERE topic_id =#{topicId}")
    Map queryByRecord(@Param("topicId")Integer topicId);
}
