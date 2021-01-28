package com.qinfei.qferp.service.bbs;

import com.qinfei.qferp.entity.bbs.DeptTree;
import com.qinfei.qferp.entity.bbs.Record;
import com.qinfei.qferp.entity.bbs.Reply;
import com.qinfei.qferp.entity.bbs.Topic;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
/**
 * 帖子管理接口
 * @author tsf
 */
public interface ITopicService {
    /**
     * 根据id查询帖子信息
     * @param topicId
     * @return
     */
    Map getById(Integer topicId);

    /**
     * 根据topicId查询帖子详情
     * @param topicId
     * @return
     */
    Topic queryById(Integer topicId);

    /**
     * 根据板块id查询所有的帖子信息
     * @param forumId
     * @return
     */
    List<Topic> getByForumId(Integer forumId);

    /**
     * 查询所有的帖子信息(论坛列表页)
     * @param companyCode
     * @param forumId
     * @param pageable
     * @return
     */
    PageInfo<Topic> queryTopic(String companyCode,Integer forumId,Integer moderator,String titleQc,Integer searchState,Integer queryFlag,Pageable pageable);

    /**
     * 获取所有的帖子总数
     * @param companyCode
     * @param forumId
     * @return
     */
    int getTopicCount(String companyCode,Integer forumId,Integer moderator);

    List<Map> queryTopicByWorkbench();

    /**
     * 查询所有的帖子信息(论坛管理中心：我发表的帖子，我点赞的帖子)
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Topic> queryTopicManage(Map map, Pageable pageable);

    int queryPublishNum(Map map);

    int queryReplyNum(Map map);

    /**
     * 查询所有的帖子信息(论坛管理中心：我回复的帖子)
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Topic> queryReplyTopic(Map map,Pageable pageable);

    /**
     * 热门推荐的帖子
     * @param forumId
     * @return
     */
    Map queryHotTopic(String companyCode,Integer forumId,Integer moderator);

    /**
     * 添加帖子信息
     * @param topic
     */
    void addTopic(Topic topic);

    /**
     * 修改帖子信息
     * @param topic
     */
    void updateTopic(Topic topic);

    /**
     * 删除帖子信息
     * @param topicId
     * @param reason
     */
    void delTopic(Integer topicId,String reason);

    /**
     * 加精
     * @param topicId
     */
    void update(Integer topicId);

    /**
     * 置顶
     * @param topicId
     */
    void update2(Integer topicId);

    /**
     * 添加帖子点赞记录
     * @param record
     */
    void addTopicRecord(Record record);

    /**
     * 修改帖子点赞记录
     * @param state,topicId
     */
    void updateTopicRecord(Integer state,Integer topicId);

    /**
     * 查询帖子点赞记录
     * @param topicId
     * @param userId
     * @return
     */
    Record queryRecord(Integer topicId,Integer userId);

    /**
     * 根据点赞记录表统计点赞数
     * @param topicId
     * @return
     */
    Map queryByRecord(Integer topicId);

    /**
     * 添加回复
     * @param reply
     */
    void addReply(Reply reply);

    /**
     * 根据id查询回复人姓名
     * @param replyId
     * @return
     */
    String queryReplyById(Integer replyId);

    /**
     * 根据评论id查询所有回复信息
     * @param commentId
     * @return
     */
    List<Reply> queryReply(Integer commentId,Integer userId);

    /**
     * 根据评论id查询所有回复信息(分页)
     * @param commentId
     * @param pageNum
     * @return
     */
    PageInfo<Reply> queryReplyforPage(Integer commentId,Integer userId,int pageNum);

    /**
     * 查询当日板块发帖数(板块)
     * @param companyCode
     * @param forumId
     * @return
     */
    List<Integer> queryTopicByToday(String companyCode,Integer forumId);

    /**
     * 查询当日板块发帖数(我的)
     * @param companyCode
     * @param forumId
     * @return
     */
    List<Integer> queryTodayByUserId(String companyCode,Integer forumId);

    /**
     * 申请加精
     * @param topicId
     * @param highReason
     */
    void updateLevel(Integer topicId,String highReason,Integer state);

    /**
     * 申请置顶
     * @param topicId
     * @param topReason
     */
    void updateTop(Integer topicId,String topReason,Integer state);

    /**
     * 取消加精
     * @param topicId
     */
    void notAddLevel(Integer topicId);

    /**
     * 取消置顶
     * @param topicId
     */
    void notGoTop(Integer topicId);

//    /**
//     * 查询部门下的所有人
//     * @param flag
//     * @return
//     */
//    List<Map> queryDepts(Integer flag);

    /**
     * 查询部门树数据
     * @param flag
     * @return
     */
    List<DeptTree> queryDeptTree(Integer flag,String companyCode);

    /**
     * 根据帖子id查询他查看的所有权限
     * @param topicId
     * @return
     */
     List<Map> queryTopicRelation(Integer topicId);

     List<Topic> queryTopicImage(Integer topicId);

    PageInfo<Topic> queryFirstImage(String companyCode);

    /**
     * 根据帖子id查询评论权限
     * @param topicId
     * @return
     */
    Map queryRelationFlag(Integer topicId,Integer userId);

    /**
     * 首页推荐帖子
     * @param pageNum
     * @return
     */
    String queryFirstInfo(int pageNum);
}
