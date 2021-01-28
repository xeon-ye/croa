package com.qinfei.qferp.controller.bbs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.bbs.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.bbs.ICommentService;
import com.qinfei.qferp.service.bbs.ITopicService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 帖子管理的控制器
 * @author tsf
 */
@Slf4j
@Controller
@RequestMapping("/topic")
class TopicController {
    @Autowired
    private ITopicService topicService;
    @Autowired
    private ICommentService commentService;
    @Autowired
    Config config;

    /**
     * 根据id查询帖子信息
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/findById")
    public ResponseData findById(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        Map map = topicService.getById(id);
        data.putDataValue("entity",map);
        return data;
    }

    /**
     * 根据id查询帖子信息
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/getTopicById")
    public ResponseData getTopicById(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        Topic topic = topicService.queryById(id);
        data.putDataValue("entity",topic);
        return data;
    }

    /**
     * 去除重复点赞
     * @param topicId
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkInfo")
    public ResponseData checkInfo(@RequestParam("topicId")Integer topicId){
        ResponseData data = ResponseData.ok();
        Record entity = topicService.queryRecord(topicId, AppUtil.getUser().getId());
        if(entity!=null){
            //已经点过赞了
            if(entity.getState()==1){
//              data.putDataValue("message","已经支持过该帖子");
                data.putDataValue("flag",1);
            }else if(entity.getState()==2){
//              data.putDataValue("message","不支持该帖子");
                data.putDataValue("flag",2);
            }else{
//              data.putDataValue("message","可以支持该帖子和反对该帖子");
                data.putDataValue("flag",0);
            }
        }
        return data;
    }

    /**
     * 根据点赞记录表统计点赞数
     * @param topicId
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryCount")
    public ResponseData queryCount(@RequestParam("topicId")Integer topicId){
        ResponseData data = ResponseData.ok();
        Map map = topicService.queryByRecord(topicId);
        data.putDataValue("message","操作成功");
        data.putDataValue("entity",map);
        if(map!=null){
            data.putDataValue("likeNum",map.get("likeNum"));
            data.putDataValue("dislikeNum",map.get("dislikeNum"));
            data.putDataValue("viewNum",map.get("viewNum"));
        }
        return data;
    }

    /**
     * 根据板块id查询帖子信息
     * @param forumId
     * @return
     */
    @ResponseBody
    @RequestMapping("/findByForumId")
    public ResponseData getByForumId(@RequestParam("forumId")Integer forumId){
        ResponseData data = ResponseData.ok();
        List<Topic> topicList = topicService.getByForumId(forumId);
        data.putDataValue("list",topicList);
        data.putDataValue("number",topicList.size());
        return data;
    }

    /**
     * 查询所有帖子信息(论坛列表页)
     * @param companyCode
     * @param forumId
     * @param titleQc
     * @param searchState 1,所有;2置顶;3,精华;
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPg")
    public PageInfo<Topic> findTopic(@RequestParam("companyCode") String companyCode, @RequestParam("forumId") Integer forumId,@RequestParam("moderator") Integer moderator, @RequestParam(value = "titleQc", required = false) String titleQc, @RequestParam(value = "searchState", required = false) Integer searchState,@RequestParam("queryFlag") Integer queryFlag,Pageable pageable) {
        return topicService.queryTopic(companyCode, forumId,moderator, titleQc, searchState,queryFlag,pageable);
    }

    /**
     * 查询所有的帖子总数
     * @param companyCode
     * @param forumId
     * @return
     */
    @ResponseBody
    @RequestMapping("/getTopicCount")
    public ResponseData getTopicCount(@RequestParam("companyCode") String companyCode,@RequestParam("forumId") Integer forumId,@RequestParam("moderator") Integer moderator){
        ResponseData data = ResponseData.ok();
        return data.putDataValue("total",topicService.getTopicCount(companyCode,forumId,moderator));
    }

    /**
     * 查询我发布的所有帖子数
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/getPublishCount")
    public ResponseData getPublishCount(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        return data.putDataValue("total",topicService.queryPublishNum(map));
    }

    /**
     * 查询所有的帖子总数
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/getReplyCount")
    public ResponseData getReplyCount(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        return data.putDataValue("total",topicService.queryReplyNum(map));
    }

    /**
     * 查询所有帖子信息(论坛管理中心：我发表的帖子，我点赞的帖子)
     * @param map
     * @param pageable
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryTopicManage")
    public PageInfo<Topic> queryTopicManage(@RequestParam Map map, Pageable pageable){
        return topicService.queryTopicManage(map,pageable);
    }

    /**
     * 查询所有帖子信息(论坛管理中心：我发表的帖子，我点赞的帖子)
     * @param map
     * @param pageable
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryReplyTopic")
    public PageInfo<Topic> queryReplyTopic(@RequestParam Map map, Pageable pageable){
        return topicService.queryReplyTopic(map,pageable);
    }

    /**
     * 论坛列表展示在工作台
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryTopicByWorkbench")
    public List<Map> queryTopicByWorkbench(){
        return topicService.queryTopicByWorkbench();
    }

    /**
     * 增加帖子信息
     * @param entity
     * @return
     */
    @ResponseBody
    @PostMapping("/add")
//    @Log(opType = OperateType.ADD, module = "论坛管理|帖子添加", note = "论坛帖子添加")
    public ResponseData saveTopic(@RequestBody Topic entity){
        try{
            ResponseData data = ResponseData.ok();
            topicService.addTopic(entity);
            data.putDataValue("message","操作成功");
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 修改帖子信息
     * @param topic
     * @return
     */
    @ResponseBody
    @PostMapping("/edit")
//    @Log(opType = OperateType.UPDATE, module = "论坛管理|帖子修改", note = "论坛帖子修改")
    public ResponseData updateTopic(@RequestBody Topic topic){
        try{
            ResponseData data = ResponseData.ok();
            data.putDataValue("message","操作成功");
            topicService.updateTopic(topic);
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 删除帖子信息
     * @param topicId
     * @return
     */
    @ResponseBody
    @RequestMapping("/del")
//    @Log(opType = OperateType.DELETE, module = "论坛管理|帖子删除", note = "论坛帖子删除")
    public ResponseData delTopic(Integer topicId,String reason){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","操作成功");
        topicService.delTopic(topicId,reason);
        return data;
    }

    /**
     * 帖子加精
     * @param topicId
     * @return
     */
    @ResponseBody
    @RequestMapping("/addLevel")
//    @Log(opType = OperateType.UPDATE, module = "论坛管理|帖子加精", note = "帖子加精")
    public ResponseData addLevel(Integer topicId){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","加精成功");
        topicService.update(topicId);
        return data;
    }

    /**
     * 帖子置顶
     * @param topicId
     * @return
     */
    @ResponseBody
    @RequestMapping("/goTop")
//    @Log(opType = OperateType.UPDATE, module = "论坛管理|帖子置顶", note = "帖子置顶")
    public ResponseData goTop(Integer topicId){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","置顶成功");
        topicService.update2(topicId);
        return data;
    }

    /**
     * 添加帖子点赞记录
     * @param topicId
     */
    @ResponseBody
    @RequestMapping("/addTopicRecord")
    public ResponseData addTopicRecord(@Param("topicId")Integer topicId){
        ResponseData data = ResponseData.ok();
        User user = AppUtil.getUser();
        Record record = new Record();
        record.setTopicId(topicId);
        record.setCreateTime(new Date());
        record.setUserId(user.getId());
        record.setUserName(user.getName());
        record.setState(0);
        Record entity = topicService.queryRecord(topicId,user.getId());
        if(entity!=null){
//            System.out.println("已经存在了");
        }else{
            topicService.addTopicRecord(record);
        }
        return data;
    }

    /**
     * 修改帖子点赞记录
     * @param state
     * @param topicId
     */
    @ResponseBody
    @RequestMapping("/updateTopicRecord")
    public ResponseData updateTopicRecord(@Param("state") Integer state, @Param("topicId") Integer topicId){
        ResponseData data = ResponseData.ok();
        topicService.updateTopicRecord(state, topicId);
        Map map = topicService.queryByRecord(topicId);
        data.putDataValue("entity",map);
        if(map!=null){
            data.putDataValue("likeNum",map.get("likeNum"));
            data.putDataValue("dislikeNum",map.get("dislikeNum"));
            data.putDataValue("viewNum",map.get("viewNum"));
        }
        return data;
    }

    /**
     * 查询帖子点赞记录
     * @param topicId
     * @param userId
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryRecord")
    public ResponseData queryRecord(@Param("topicId") Integer topicId, @Param("userId") Integer userId){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","操作成功");
        Record record = topicService.queryRecord(topicId, userId);
        data.putDataValue("entity",record);
        return data;
    }

    /**
     * 添加帖子评论
     * @param content
     * * @param topicId
     * @return
     */
    @ResponseBody
    @RequestMapping("/addTopicComment")
    public ResponseData addTopicComment(@Param("content")String content,@Param("topicId")Integer topicId){
        ResponseData data = ResponseData.ok();
        try {
            Comment comment = new Comment();
            User user = AppUtil.getUser();
            comment.setId(null);
            comment.setTopicId(topicId);
            comment.setUserId(user.getId());
            comment.setUserName(user.getName());
            comment.setPicture(user.getImage());
            comment.setCreateTime(new Date());
            comment.setParentId(0);
            comment.setContent(content);
            commentService.addComment(comment);
            data.putDataValue("message","发表成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 根据帖子id查询评论信息
     * @param topicId
     * @return
     */
    @RequestMapping("/queryCommentByTopicId")
    @ResponseBody
    public PageInfo<Comment> queryCommentByTopicId(@Param("topicId") Integer topicId,@RequestParam(value = "userId",required = false) Integer userId,Pageable pageable){
        return commentService.queryComment(topicId,userId,pageable);
    }

    /**
     * 根据帖子id查询所有评论数量
     * @param topicId
     * @return
     */
    @RequestMapping("/queryCommentCount")
    @ResponseBody
    public ResponseData queryCommentCount(@Param("topicId") Integer topicId,@RequestParam(value = "userId",required = false) Integer userId){
        ResponseData data = ResponseData.ok();
        return data.putDataValue("total",commentService.queryCommentCount(topicId,userId));
    }

//    /**
//     * 根据帖子id查询评论信息(以流的方式展示)
//     * @param topicId
//     * @return
//     */
//    @RequestMapping("/queryCommentContent")
//    @ResponseBody
//    public void queryCommentContent(@Param("topicId") Integer topicId,@Param("pageNum") int pageNum, HttpServletResponse response){
//        try{
//            response.getWriter().write(commentService.queryCommentStr(topicId,pageNum));
//        }catch (Exception e){
//            log.error("获取信息异常", e);
//        }
//
//    }

    /**
     * 根据帖子id查询评论信息
     * @param topicId
     * @param parentId
     * @return
     */
    @RequestMapping("/queryCommentByParentId")
    @ResponseBody
    public ResponseData queryCommentByParentId(@Param("topicId") Integer topicId,@Param("parentId") Integer parentId){
        ResponseData data = ResponseData.ok();
        List<Comment> list = commentService.queryCommentByParentId(topicId,parentId);
        if(list.size()==0){
            data.putDataValue("message","没有数据");
        }else{
            data.putDataValue("message","操作成功");
        }
        data.putDataValue("list",list);
        data.putDataValue("size",list.size());
        return data;
    }

    /**
     * 添加回复
     * @param id
     * @param content
     * @param replyId
     * @return
     */
    @RequestMapping("/saveReply")
    @ResponseBody
    public ResponseData saveReply(@Param("id") Integer id,@Param("content") String content,@RequestParam(value = "replyId", required = false) Integer replyId){
        ResponseData data = ResponseData.ok();
        Reply entity = new Reply();
        User user = AppUtil.getUser();
        entity.setCommId(id);
        Comment comment = commentService.getById(id);
        //设置回复的主题id
        entity.setTopicId(comment.getTopicId());
        entity.setContent(content);
        entity.setReplyUserId(user.getId());
        entity.setReplyUserName(user.getName());
        entity.setPicture(user.getImage());
        entity.setCreateTime(new Date());
        if(replyId!=null){
            String userName = topicService.queryReplyById(replyId);
            if(!user.getName().equals(userName)){
                entity.setUserName(userName);
            }
        }else{
            String userName = comment.getUserName();
            if(!user.getName().equals(userName)){
                entity.setUserName(userName);
            }
        }
        topicService.addReply(entity);
        data.putDataValue("message","操作成功");
        return data;
    }

    /**
     * 根据评论id查询评论信息
     * @param commentId
     * @return
     */
    @RequestMapping("/queryCommentById")
    @ResponseBody
    public ResponseData queryCommentById(@Param("commentId") Integer commentId){
        ResponseData data = ResponseData.ok();
        Comment comment = commentService.getById(commentId);
        data.putDataValue("entity",comment);
        data.putDataValue("message","操作成功");
        return data;
    }

    /**
     * 根据评论id查询回复信息
     * @param commentId
     * @param pageNum
     * @return
     */
    @RequestMapping("/queryReply")
    @ResponseBody
    public PageInfo<Reply> queryReply(@Param("commentId") Integer commentId,@RequestParam(value = "userId",required = false) Integer userId,@Param("pageNum") Integer pageNum){
        return topicService.queryReplyforPage(commentId,userId,pageNum);
    }

    /**
     * 图片上传测试
     * @return
     */
    @RequestMapping("/uploadSummerPic")
    @ResponseBody
    public ResponseData uploadSummerPic(HttpServletRequest request,HttpServletResponse response,MultipartFile file){
        ResponseData data = ResponseData.ok();
        try {
            if (file != null && StringUtils.isNotBlank(file.getOriginalFilename())) {
                String dateDir = DateUtils.format(new Date(), "yyyy/MM/dd/");
                String childPath = dateDir + "topic/images/";
                File photoDir = new File(config.getUploadDir(), childPath);
                if (!photoDir.exists()) {
                    photoDir.mkdirs();
                }
                String filename = UUIDUtil.get32UUID() + file.getOriginalFilename();
                File fullPath = new File(photoDir, filename);
                file.transferTo(fullPath);
                //返回全路径
                String reqUrl = request.getRequestURL().toString();
                //返回除去host（域名或者ip）部分的路径
                String reqUri = request.getRequestURI();
                reqUrl = reqUrl.replace(reqUri,"");
                String url = reqUrl+config.getWebDir()+childPath+filename;
                data.putDataValue("url",url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 查询当天的发帖数
     * @param forumId
     * @return
     */
    @RequestMapping("/queryTopicByToday")
    @ResponseBody
    public Map queryTopicByToday(@Param("companyCode") String companyCode,@Param("forumId") Integer forumId,@Param("moderator") Integer moderator){
        return topicService.queryHotTopic(companyCode,forumId,moderator);
    }

    /**
     * 申请加精
     * @param topicId
     * @param highReason
     */
    @RequestMapping("/updateLevel")
    @ResponseBody
    public ResponseData updateLevel(@Param("topicId")Integer topicId, @Param("highReason")String highReason,@Param("state")Integer state) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","操作成功");
        topicService.updateLevel(topicId,highReason,state);
        return  data;
    }

    /**
     * 申请置顶
     * @param topicId
     * @param topReason
     */
    @RequestMapping("/updateTop")
    @ResponseBody
    public ResponseData updateTop(@Param("topicId")Integer topicId, @Param("topReason")String topReason,@Param("state")Integer state) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","操作成功");
        topicService.updateTop(topicId,topReason,state);
        return data;
    }

    /**
     * 取消加精
     * @param topicId
     */
    @RequestMapping("/notAddLevel")
    @ResponseBody
    public ResponseData notAddLevel(Integer topicId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","取消加精操作成功");
        topicService.notAddLevel(topicId);
        return data;
    }

    /**
     * 取消置顶
     * @param topicId
     */
    @RequestMapping("/notGoTop")
    @ResponseBody
    public ResponseData notGoTop(Integer topicId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","取消置顶操作成功");
        topicService.notGoTop(topicId);
        return data;
    }

//    /**
//     * 评论可见设置
//     * @param flag
//     */
//    @RequestMapping("/queryDepts")
//    @ResponseBody
//    public JSONArray queryDepts(Integer flag){
//        JSONArray array = new JSONArray() ;
//        List<Map> deptList = topicService.queryDepts(flag);
//        for (Map map : deptList) {
//            JSONObject obj = new JSONObject() ;
//            Integer pId=0;
//            Integer id=0;
//            String name=null;
//            if(map.get("pId")!=null){
//                 pId = (Integer)map.get("pId");
//            }
//            if(map.get("id")!=null){
//                 id = (Integer)map.get("id");
//            }
//            if(map.get("name")!=null){
//                name = (String)map.get("name");
//            }
//            if(pId==0){
//                obj.put("id", id);
//                obj.put("name", name);
//                obj.put("pId", pId);
//                obj.put("open",true);
//                array.add(obj) ;
//            }else{
//                obj.put("id", id);
//                obj.put("name", name);
//                obj.put("pId", pId);
//                array.add(obj) ;
//            }
//        }
//        return array;
//    }

    /**
     * 评论可见设置
     * @param flag
     * @return
     */
    @RequestMapping("/queryDeptTree")
    @ResponseBody
    public JSONArray queryDeptTree(Integer flag,String companyCode){
        JSONArray array = new JSONArray() ;
//        String companyCode = AppUtil.getUser().getCompanyCode();
        List<DeptTree> list = topicService.queryDeptTree(flag,companyCode);
        if(list!=null && list.size()>0){
            for (DeptTree deptTree : list) {
                JSONObject obj = new JSONObject() ;
                Integer pId=deptTree.getPid();
                Integer id=deptTree.getId();
                String name=deptTree.getName();
                List<DeptTree> deptTrees = deptTree.getChildren();
                if(pId==0){
                    obj.put("id", id);
                    obj.put("name", name);
                    obj.put("pId", pId);
                    obj.put("open",true);
                    obj.put("children",deptTrees);
                    array.add(obj) ;
                }else{
                    obj.put("id", id);
                    obj.put("name", name);
                    obj.put("pId", pId);
                    obj.put("children",deptTrees);
                    array.add(obj) ;
                }
            }
        }
        return array;
    }

    /**
     * 编辑帖子根据id查询可见状态及是否有主题图
     * @param topicId
     */
    @RequestMapping("/queryTopicRelation")
    @ResponseBody
    public ResponseData queryTopicRelation(Integer topicId) {
        ResponseData data = ResponseData.ok();
        List<Map> list = topicService.queryTopicRelation(topicId);
        if(list!=null && list.size()==1){
            //flag=1全部可见，0部分可见
//            Object viewFlag = list.get(0).get("viewFlag");
            Object viewSetting = list.get(0).get("commUserIds");
            Integer flag=ObjectUtils.isEmpty(viewSetting)?0:1;
            data.putDataValue("flag",flag);
        }else{
            data.putDataValue("flag",0);
        }
        data.putDataValue("list",list);
        data.putDataValue("number",list.size());
        List<Topic> imageObj = topicService.queryTopicImage(topicId);
        if(imageObj!=null && imageObj.size()>0){
            //存在主题图
            data.putDataValue("topic",imageObj.get(0));
            data.putDataValue("imageState","yes");
        }else {
            data.putDataValue("imageState","no");
        }
        return data;
    }

    /**
     * 获取论坛首页轮播图
     * @return
     */
    @RequestMapping("/getFirstImage")
    @ResponseBody
    public ResponseData getFirstImage(@RequestParam("companyCode") String companyCode) {
        ResponseData data = ResponseData.ok();
        PageInfo<Topic> imageObj = topicService.queryFirstImage(companyCode);
        if(imageObj!=null){
            //存在主题图
            data.putDataValue("topic",imageObj);
        }
        return data;
    }

    /**
     * 根据帖子id查询评论权限
     * @param topicId
     * @return
     */
    @RequestMapping("/queryRelationFlag")
    @ResponseBody
    public Map queryRelationFlag(@RequestParam("topicId")Integer topicId,@RequestParam("userId")Integer userId){
        return topicService.queryRelationFlag(topicId,userId);
    }

    /**
     * 分页查询联系人信息；
     * @param pageNum：页码；
     * @return ：拼接完毕的分页内容
     */
    @RequestMapping("/queryFirstInfo")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "分页查询联系人信息")
    public void queryFirstInfo(@Param("pageNum") Integer pageNum, HttpServletResponse response) {
        try {
            response.getWriter().write(topicService.queryFirstInfo(pageNum));
        } catch (IOException e) {
            log.error("获取信息异常", e);
        }
    }
}
