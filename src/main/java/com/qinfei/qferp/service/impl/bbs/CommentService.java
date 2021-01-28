package com.qinfei.qferp.service.impl.bbs;
import com.qinfei.qferp.entity.bbs.Comment;
import com.qinfei.qferp.mapper.bbs.CommentMapper;
import com.qinfei.qferp.service.bbs.ICommentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * 评论接口的实现类
 * @author tsf
 */
@Service
public class CommentService implements ICommentService {
    @Autowired
    private CommentMapper commentMapper;

    /**
     * 根据id查询评论信息
     * @param id
     * @return
     */
    @Override
    public Comment getById(Integer id) {
        return commentMapper.getById(id);
    }

    /**
     * 查询所有评论信息
     * @param topicId
     * @return
     */
    @Override
    public List<Comment> queryComment(Integer topicId,Integer userId) {
        return commentMapper.queryComment(topicId,userId);
    }

    /**
     * 查询所有评论信息(分页)
     * @param topicId
     * @return
     */
    @Override
    public PageInfo<Comment> queryComment(Integer topicId,Integer userId,Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),20);
        List<Comment> list = commentMapper.queryComment(topicId,userId);
        return new PageInfo<>(list);
    }

    @Override
    public int queryCommentCount(Integer topicId,Integer userId) {
        return commentMapper.queryCommentCount(topicId,userId);
    }

    /**
     * 以流的方式输出评论信息（暂不启用）
     * @param topicId
     * @return
     */
    @Override
    public String queryCommentStr(Integer topicId,Integer userId,Integer pageNum) {
        PageHelper.startPage(pageNum, 10);
        // 查询条件；
        List<Comment> comments = commentMapper.queryComment(topicId,userId);
        PageInfo<Comment> commentInfo = new PageInfo<>(comments);

        // 开始拼接返回内容；
        StringBuilder content = new StringBuilder();
        content.append("<div class=\"content\">");
        int size = comments.size();
        if (size > 0 && pageNum <= commentInfo.getPages()) {
            Comment comment;
            Object userField;
            for (int i = 0; i < size; i++) {
                comment = comments.get(i);
                content.append("<div class='excerpt' style='margin-top: 10px;text-align:center;'>");
                content.append("<div class='contact-box' style='display: inline-block;background-color:#EEEEEE;text-align:left'>");
                // --------------------------------评论人头像模块；------------------------------------
                // 获取照片；
                userField = comment.getPicture();
                content.append("<img alt='image' class='img-circle' style='width: 32px;height: 32px' onerror=\"this.src='/img/mrtx_2.png'\" src=\"");
                content.append(userField);
                content.append("\"/>");
                content.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                // --------------------------------头像模块；------------------------------------
                // --------------------------------信息模块；------------------------------------
                //评论人id；
                userField = comment.getUserId().toString();
                content.append("<input type='hidden' name='userId' value=\"");
                content.append(userField == null ? "" : userField);
                content.append("\"/>");
                //评论人姓名；
                userField = comment.getUserName();
                content.append("<span id='userNameComment'>");
                content.append(userField == null ? "" : userField);
                content.append("</span>");
                content.append("&nbsp;&nbsp;");
                //评论时间；
                userField = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(comment.getCreateTime());
                content.append("<span id='createTime'>");
                content.append(userField == null ? "" : userField);
                content.append("</span><br/>");
                // 评论内容；
                userField = comment.getContent();
                content.append("<div class='col-md-2'></div><span id='content' style='color: #7991E8;'>");
                content.append(userField == null ? "" : userField);
                content.append("</span><br/>");
                Integer commId = comment.getId();
                content.append("<div id=\"showHistory_");
                content.append(commId == null ? "" : commId);
                content.append("\"></div><br/>");
                // 评论操作；
                content.append("<div class='col-md-3'></div>");
                content.append("<a href='javascript:void(0)' onclick=\"addComment(");
                content.append(commId == null ? "" : commId);
                content.append(")\">评论</a>&nbsp;&nbsp;&nbsp;&nbsp;");
                content.append("<a href='javascript:void(0)' id='showCommentChild' value=\"");
                content.append(commId == null ? "" : commId);
                content.append("\" onclick=\"showCommentChild(");
                content.append(topicId);
                content.append(",");
                content.append(commId);
                content.append(")\">查看更多</a><br/>");
                content.append("</div>");
                // --------------------------------信息模块；------------------------------------
                // 底部填充；
                content.append("<div class=\"clearfix\"></div>");
                content.append("</div>");
            }
        } else {
            content.append("<div class='pagination'><h3><strong>查询无结果。</strong></h3></div>");
            content.append("</div>");
            return content.toString();
        }
        // 导航；
        content.append("<nav class=\"pagination\" style=\"display:none\"><ul><li class=\"next-page\"><a href=\"/bbs/showTopic\"></a></li></ul></nav>");
        content.append("</div>");
        return content.toString();
    }

    /**
     * 查询子级评论信息
     * @param topicId
     * @param parentId
     * @return
     */
    @Override
    public List<Comment> queryCommentByParentId(Integer topicId, Integer parentId) {
        return commentMapper.queryCommentByParentId(topicId, parentId);
    }



    /**
     * 查询子级评论数量
     * @param topicId
     * @return
     */
    @Override
    public List<Map> queryChildCommentNum(Integer topicId) {
        return commentMapper.queryChildCommentNum(topicId);
    }

    /**
     * 添加评论信息
     * @param comment
     */
    @Override
    public void addComment(Comment comment) {
        commentMapper.insert(comment);
    }

    /**
     * 修改评论信息
     * @param comment
     */
    @Override
    public void updateComment(Comment comment) {
        commentMapper.update(comment);
    }
}
