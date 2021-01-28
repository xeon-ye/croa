package com.qinfei.qferp.service.bbs;

import com.qinfei.qferp.entity.bbs.Comment;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

/**
 * 评论的接口
 * @author tsf
 */
public interface ICommentService {
    /**
     * 根据id查询评论信息
     * @param id
     * @return
     */
    Comment getById(Integer id);

    /**
     * 查询指定帖子的评论
     * @param topicId
     * @return
     */
    List<Comment> queryComment(Integer topicId,Integer userId);

    /**
     * 查询指定帖子的评论(分页)
     * @param topicId
     * @return
     */
    PageInfo<Comment> queryComment(Integer topicId,Integer userId,Pageable pageable);

    int queryCommentCount(Integer topicId,Integer userId);

    /**
     * 以流的方式输出评论信息
     * @param topicId
     * @return
     */
    String queryCommentStr(Integer topicId,Integer userId,Integer pageNum);

    /**
     * 查询帖子的子级评论
     * @param topicId
     * @param parentId
     * @return
     */
    List<Comment> queryCommentByParentId(Integer topicId,Integer parentId);

    /**
     * 查询一级评论下有多少子评论
     * @param topicId
     * @return
     */
    List<Map> queryChildCommentNum(Integer topicId);

    /**
     * 添加帖子信息
     * @param comment
     */
    void addComment(Comment comment);

    /**
     * 修改帖子信息
     * @param comment
     */
    void updateComment(Comment comment);
}
