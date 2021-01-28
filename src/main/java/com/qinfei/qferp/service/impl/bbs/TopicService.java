package com.qinfei.qferp.service.impl.bbs;

import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.bbs.*;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.bbs.ForumMapper;
import com.qinfei.qferp.mapper.bbs.RecordMapper;
import com.qinfei.qferp.mapper.bbs.ReplyMapper;
import com.qinfei.qferp.mapper.bbs.TopicMapper;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.bbs.ITopicService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import net.sf.jsqlparser.statement.select.Top;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 帖子管理接口实现类
 * @author tsf
 */
@Service
public class TopicService implements ITopicService {
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ReplyMapper replyMapper;
    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private ForumMapper forumMapper;
    @Autowired
    private IMessageService messageService;

    /**
     * 根据id查询帖子信息
     * @param topicId
     * @return
     */
    @Override
    public Map getById(Integer topicId) {
        return topicMapper.getById(topicId);
    }

    /**
     * 根据id查询帖子信息
     * @param topicId
     * @return
     */
    @Override
    public Topic queryById(Integer topicId) {
        return topicMapper.queryById(topicId);
    }

    /**
     * 根据板块id查询所有的帖子信息
     * @param forumId
     * @return
     */
    @Override
    public List<Topic> getByForumId(Integer forumId) {
        return topicMapper.getByForumId(forumId);
    }

    /**
     * 查询所有的帖子信息（论坛列表页）
     * @param companyCode
     * @param forumId
     * @Param pageable
     * @Param titleQc
     * @Param userId
     * @Param searchState(1,所有;2置顶;3,精华;)
     * @return
     */
    @Override
    public PageInfo<Topic> queryTopic(String companyCode,Integer forumId,Integer moderator,String titleQc,Integer searchState,Integer queryFlag,Pageable pageable) {
        List<Topic> topicList = new ArrayList<>();
        List<Integer> tids;
        Map<Integer,Topic> map2;
        try {
            Map map = new HashMap();
            Integer userId = AppUtil.getUser().getId();
            if(searchState!=null){
                if(searchState==1){
//                查询所有
                }else if(searchState==2){
//                 查询置顶
                    map.put("topState",2);
                }else if(searchState==3){
//                 查询精华
                    map.put("highState",2);
                }
            }
            //放开查看的权限
            if(moderator==null && "".equals(moderator)){
                Forum forum = forumMapper.queryById(forumId);
                moderator = forum.getModerator();
            }
            map.put("flag", userId.equals(moderator)?1:0);
            //选中的公司代码
            map.put("companyCode",companyCode);
            //当前人公司代码
            map.put("userCompanyCode",AppUtil.getUser().getCompanyCode());
            map.put("forumId",forumId);
            map.put("titleQc",titleQc);
            map.put("userId",userId);
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            //可见帖子集合
            if(queryFlag==0){
                topicList = topicMapper.queryTopic(map);
                if(topicList.size()==0){
                    return new PageInfo<>();
                }
                Integer hot = 0;
                map2 = new HashMap<>();
                tids = new ArrayList<>();
                for(Topic topic :topicList){
                    map2.put(topic.getId(),topic);
                    tids.add(topic.getId());
                }
                if (tids != null && tids.size() > 0) {
                    List<Map> list = topicMapper.queryCommReplyNum(forumId, tids);
                    for (Map objectMap : list) {
                        Integer topicId = Integer.parseInt(objectMap.get("id").toString());
                        Integer replyNum = Integer.parseInt(objectMap.get("replyNum").toString());
                        Integer commNum = Integer.parseInt(objectMap.get("commNum").toString());
                        Integer viewNum = map2.get(topicId).getViewNum();
                        Integer likeNum = map2.get(topicId).getLikeNum();
                        Integer dislikeNum = map2.get(topicId).getDislikeNum();
                        Date createTime = map2.get(topicId).getCreateTime();
                        Integer day = Math.round((new Date().getTime() - createTime.getTime())/ 1000 / 60 / 60 / 24);
//                  热度值计算 = 1*viewNum + 5*commNum + 5*replyNum+3*likeNum-8*dislikeNum-1*day;
//                  热度值>=75显示热度标志
                        hot = viewNum + commNum * 5 + replyNum * 5 + likeNum * 3 - 8 * dislikeNum - day;
                        map2.get(topicId).setHot(hot);
                        map2.get(topicId).setCommNum(commNum);
                    }
                }
            }else {
                topicList = topicMapper.querySearchTopic(map);
                if(topicList.size()==0){
                    return new PageInfo<>();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PageInfo<>(topicList);
    }

    @Override
    public int getTopicCount(String companyCode, Integer forumId,Integer moderator) {
        Map map = new HashMap();
        Integer userId = AppUtil.getUser().getId();
        //放开查看的权限
        map.put("flag", userId.equals(moderator)?1:0);
        //选中的公司代码
        map.put("companyCode",companyCode);
        //当前人公司代码
        map.put("userCompanyCode",AppUtil.getUser().getCompanyCode());
        map.put("forumId",forumId);
        map.put("userId",userId);
        return topicMapper.getTopicCount(map);
    }

    /**
     * 工作台论坛列表展示
     * @return
     */
    @Override
    public List<Map> queryTopicByWorkbench() {
        List<Map> list = new ArrayList<>();
        try {
            Map map = new HashMap();
            Integer userId = AppUtil.getUser().getId();
            String companyCode = AppUtil.getUser().getCompanyCode();
            map.put("companyCode",companyCode);
            map.put("userId",userId);
            list =  topicMapper.queryTopicByWorkbench(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询帖子信息(论坛管理中心：我发表的帖子，我点赞的帖子)
     * @param map
     * @param pageable
     * @return
     */
    @Override
    public PageInfo<Topic> queryTopicManage(Map map,Pageable pageable) {
        List<Topic> list=null;
        try {
            User user = AppUtil.getUser();
            Integer userId =  user.getId();
            String postState = (String)map.get("postState");
            String banzuFlag = (String)map.get("banzuFlag");
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            if(postState==null){
                //我发表的帖子
                String moderator = (String)map.get("moderator");
                //版主标志0
                if(!StringUtils.isEmpty(moderator) && "0".equals(banzuFlag)){
                    //版主
                    map.put("moderator",moderator);
                }else{
                    map.put("userId",userId);
                    map.put("moderator","");
                }
                list = topicMapper.queryTopicManage(map);
            }else{
                //我赞过的帖子
                //添加记录表的点赞人
                map.put("recordUserId",userId);
                //无版主
                map.put("moderator","");
                //不区分板块
                map.put("forumId","");
                list = topicMapper.queryTopicManage(map);
                if(list.size()==0){
                    return new PageInfo<>();
                }
                Map<Integer,Topic> topicMap = new HashMap();
                List<Integer> ids = new ArrayList<>();
                for (Topic topic:list) {
                    topicMap.put(topic.getId(),topic);
                    ids.add(topic.getId());
                }
                map.put("ids",ids);
                if(ids!=null && ids.size()>0){
                    List<Map> postStatistics = topicMapper.queryPostStatistics(map);
                    for(Map map2:postStatistics){
                        Integer id = Integer.parseInt(map2.get("id").toString());
                        Integer viewNum = Integer.parseInt(map2.get("viewNum").toString());
                        Integer likeNum = Integer.parseInt(map2.get("likeNum").toString());
                        Integer dislikeNum = Integer.parseInt(map2.get("dislikeNum").toString());
                        topicMap.get(id).setViewNum(viewNum);
                        topicMap.get(id).setLikeNum(likeNum);
                        topicMap.get(id).setDislikeNum(dislikeNum);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PageInfo<>(list);
    }
    //返回点赞发表的总数
    @Override
    public int queryPublishNum(Map map) {
        User user = AppUtil.getUser();
        Integer userId =  user.getId();
        String postState = (String)map.get("postState");
        String banzuFlag = (String)map.get("banzuFlag");
        int result;
        if(postState==null){
            //我发表的帖子
            String moderator = (String)map.get("moderator");
            if(!StringUtils.isEmpty(moderator) && "0".equals(banzuFlag)){
                //版主
                map.put("moderator",moderator);
            }else{
                map.put("userId",userId);
                map.put("moderator","");
            }
            result=topicMapper.queryPublishNum(map);
        }else{
            //我赞过的帖子
            //添加记录表的点赞人
            map.put("recordUserId",userId);
            map.put("forumId","");
            result=topicMapper.queryPostNum(map);
        }
        return result;
    }

    //返回回复帖子的总数
    @Override
    public int queryReplyNum(Map map) {
        map.put("replyUserId",AppUtil.getUser().getId());
        List<Integer> list = topicMapper.queryReplyNum(map);
        return list.size();
    }

    /**
     * 查询帖子信息(论坛管理中心：我回复的帖子)
     * @param map
     * @param pageable
     * @return
     */
    @Override
    public PageInfo<Topic> queryReplyTopic(Map map, Pageable pageable) {
        List<Topic> list = null;
        try {
            User user = AppUtil.getUser();
            Integer userId = user.getId();
            map.put("replyUserId",userId);
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            list = topicMapper.queryReplyTopic(map);
            if(list.size()==0){
                return new PageInfo<>();
            }
            List<Integer> ids = new ArrayList<>();
            Map<Integer, Topic> topicMap = new HashMap<>();
            for (Topic topic : list){
                topicMap.put(topic.getId(),topic);
                ids.add(topic.getId());
            }
            if(ids!=null && ids.size()>0){
                List<Map> objs = topicMapper.getReplyNumbersByTids(ids);
                for(Map obj :objs){
                    Integer id = Integer.parseInt(obj.get("id").toString());
                    String viewNum = obj.get("viewNum").toString();
                    String likeNum = obj.get("likeNum").toString();
                    String dislikeNum = obj.get("dislikeNum").toString();
                    topicMap.get(id).setViewNum(Integer.parseInt(viewNum));
                    topicMap.get(id).setLikeNum(Integer.parseInt(likeNum));
                    topicMap.get(id).setDislikeNum(Integer.parseInt(dislikeNum));
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return new PageInfo<>(list);
    }

    /**
     * 热门推荐的帖子
     * @param companyCode
     * @param forumId
     * @return
     */
    @Override
    public Map queryHotTopic(String companyCode,Integer forumId,Integer moderator) {
        Map map = new HashMap();
        Integer userId =  AppUtil.getUser().getId();
        map.put("flag", userId.equals(moderator)?1:0);
        map.put("companyCode",companyCode);
        map.put("userCompanyCode",AppUtil.getUser().getCompanyCode());
        map.put("forumId",forumId);
        map.put("userId",userId);
        List<Map> mapList = topicMapper.queryHotTopic(map);
        //热门推荐的帖子
        map.put("list",mapList);
        //查询当日稿件发布情况(板块)
        List<Integer> list = topicMapper.queryTopicByToday(companyCode,forumId);
        //防止新增板块没有帖子数报数组下标异常
        if(list.size()>1){
            map.put("first",list.get(0));
            map.put("second",list.get(1));
            //存在今天0发帖，总0帖子，今天2发帖，总2帖子
        }else if(list.size()==1 && list.get(0)>0){
            map.put("first",list.get(0));
            map.put("second",list.get(0));
        }else{
            map.put("first",0);
            map.put("second",0);
        }
        //查询当日稿件发布情况(我的)
        List<Integer> myTopic = topicMapper.queryTodayByUserId(companyCode,forumId,userId);
        if(myTopic.size()>1){
            map.put("one",myTopic.get(0));
            map.put("two",myTopic.get(1));
            //存在今天0发帖，总0帖子，今天2发帖，总2帖子
        }else if(myTopic.size()==1 && myTopic.get(0)>0){
            map.put("one",myTopic.get(0));
            map.put("two",myTopic.get(0));
        }else{
            map.put("one",0);
            map.put("two",0);
        }
        return map;
    }

    /**
     * 添加帖子信息
     * @param topic
     */
    @Transactional
    @Override
    public void addTopic(Topic topic) {
        try {
            User user = AppUtil.getUser();
            //添加默认有效
            topic.setState(0);
            //添加默认为普通帖
            topic.setLevel(0);
            //添加默认排序为9，置顶为0
            topic.setSort(9);
            topic.setHighState(0);
            topic.setTopState(0);
            //防止主键冲突
            topic.setId(null);
            topic.setPicture(user.getImage());
            topicMapper.insert(topic);
            //帖子可见设置
            String users = topic.getUsers();
            //部分可见评论人id集合
            String [] comm = null;
            //部分可见可见人id集合
            String [] ids = null;
            //全部可见评论人员集合
            String [] commIds = null;
           if(users!=null){
               ids = users.split(",");
           }
            if(topic.getCommentUsers()!=null){
                comm = topic.getCommentUsers().split(",");
            }
            saveTopicRelation(ids,comm,topic.getCommUserIds(),topic.getId(),topic.getViewSetting());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改帖子信息
     * @param topic
     */
    @Transactional
    @Override
    public void updateTopic(Topic topic) {
        try {
            User user = AppUtil.getUser();
            topic.setPicture(user.getImage());
            //修改操作，为空的数据不变
            topicMapper.update(topic);
            String userId = user.getId().toString();
            Integer viewFlag = topic.getViewSetting();
            //部分可见
            if(viewFlag==0 && !userId.equals(topic.getUsers())) {
                //删除可见人员绑定
                topicMapper.delTopicRelation(topic.getId());
                String [] comm = null;
                String [] ids = null;
                //部分可见可见人员集合
                if(topic.getUsers()!=null){
                    ids = topic.getUsers().split(",");
                }
                //部分可见评论人员集合
                if(topic.getCommentUsers()!=null){
                    comm = topic.getCommentUsers().split(",");
                }
                saveTopicRelation(ids,comm,null,topic.getId(),viewFlag);
            }else if(viewFlag==0 && userId.equals(topic.getUsers())){
                //部分可见（1、点击部分可见不选人，2、部分可见不做修改。解决办法：不处理关系表）
//                topicMapper.delTopicRelation(topic.getId());
//                String users = topic.getUsers();
//                String [] ids = null;
//                if(users!=null){
//                    ids = users.split(",");
//                }
//                saveTopicRelation(ids,null,topic.getId(),viewFlag);
                //全部可见
            }else if(viewFlag==1){
                //全部可见评论人员集合
                String commIds = topic.getCommUserIds();
                if(commIds!=null){
                    topicMapper.delTopicRelation(topic.getId());
                    saveTopicRelation(null,null,commIds,topic.getId(),viewFlag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除帖子信息
     * @param topicId
     */
    @Override
    public void delTopic(Integer topicId,String reason) {
        User user = AppUtil.getUser();
        //如果当前人不是发帖人，那就是被版主删除
        Topic topic = topicMapper.queryById(topicId);
        String userName = user.getName();
        String tips = "";
        if(userName.equals(topic.getUserName())){
            tips = String.format("[论坛]您的帖子[%s]已成功删除,删除原因：%s",topic.getTitle(),reason);
        }else{
            tips = String.format("[论坛]您的帖子[%s]已被版主删除,删除原因：%s",topic.getTitle(),reason);
        }
        topicMapper.delTopic(reason,new Date(),topicId,user.getId(),userName);
        sendMessage(topic,tips);
    }

    /**
     * 置顶，加精，取消置顶，取消加精消息（发给发帖人）
     * @param topic
     * @param tips
     */
    private void sendMessage(Topic topic,String tips) {
        User user = AppUtil.getUser();
        User obj = userMapper.getById(topic.getUserId());
        String subject = "论坛通知消息：";
        // 推送WebSocket消息(右侧弹框消息)；
        WSMessage message = new WSMessage();
        message.setReceiveUserId(obj.getId() + "");
        message.setReceiveName(obj.getName());
        message.setSendName(user.getName());
        message.setSendUserId(user.getId() + "");
        message.setSendUserImage(user.getImage());
        message.setContent(tips);
        message.setSubject(subject);
        message.setUrl(null);
        WebSocketServer.sendMessage(message);

        //发送消息的内容
        Message mes = new Message();
        String userImage = user.getImage();
        // 获取消息显示的图片；
        String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
        mes.setPic(pictureAddress);
        //消息分类
        mes.setParentType(3);//通知
        mes.setType(13);//论坛
        mes.setContent(tips);
        mes.setInitiatorWorker(user.getId());
        mes.setInitiatorDept(user.getDeptId());
        mes.setAcceptWorker(topic.getUserId());
        mes.setAcceptDept(obj.getDeptId());
        messageService.addMessage(mes);
    }

    /**
     * 申请置顶，申请加精添加消息（发给版主）
     * @param topic
     * @param tips
     */
    private void addMessage(Topic topic,String tips) {
        User user = AppUtil.getUser();
        Forum forum = forumMapper.findById(topic.getForumId());
        User obj = userMapper.getById(forum.getModerator());
        String subject = "论坛申请消息：";
        // 推送WebSocket消息(右侧弹框消息)；
        WSMessage message = new WSMessage();
        message.setReceiveUserId(obj.getId() + "");
        message.setReceiveName(obj.getName());
        message.setSendName(user.getName());
        message.setSendUserId(user.getId() + "");
        message.setSendUserImage(user.getImage());
        message.setContent(tips);
        message.setSubject(subject);
        message.setUrl(null);
        WebSocketServer.sendMessage(message);

        //发送消息的内容
        Message mes = new Message();
        String userImage = user.getImage();
        // 获取消息显示的图片；
        String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
        mes.setPic(pictureAddress);
        //消息分类
        mes.setParentType(3);//通知
        mes.setType(13);//论坛
        mes.setContent(tips);
        mes.setInitiatorWorker(user.getId());
        mes.setInitiatorDept(user.getDeptId());
        mes.setAcceptWorker(obj.getId());
        mes.setAcceptDept(obj.getDeptId());
        messageService.addMessage(mes);
    }

    /**
     * 版主加精
     * @param topicId
     */
    @Override
    public void update(Integer topicId) {
        topicMapper.addLevel(topicId);
        Topic topic = topicMapper.queryById(topicId);
        String tips = String.format("[论坛]您的帖子[%s]已成功加精",topic.getTitle());
        sendMessage(topic,tips);
    }

    /**
     * 版主置顶
     * @param topicId
     */
    @Override
    public void update2(Integer topicId) {
        try {
            topicMapper.goTop(topicId);
            Topic topic = topicMapper.queryById(topicId);
            String tips = String.format("[论坛]您的帖子[%s]已成功置顶",topic.getTitle());
            sendMessage(topic,tips);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加帖子点赞记录
     * @param record
     */
    @Override
    public void addTopicRecord(Record record) {
        try {
            recordMapper.insert(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询帖子点赞记录(防止重复点赞)
     * @param topicId
     * @param userId
     * @return
     */
    @Override
    public Record queryRecord(Integer topicId, Integer userId) {
        return recordMapper.queryRecord(topicId, userId);
    }

    /**
     * 根据点赞记录表统计点赞数
     * @param topicId
     * @return
     */
    @Override
    public Map queryByRecord(Integer topicId) {
        return recordMapper.queryByRecord(topicId);
    }

    /**
     * 修改点赞记录
     * @param state,topicId
     * @param topicId
     */
    @Override
    public void updateTopicRecord(Integer state,Integer topicId) {
        try {
            recordMapper.updateTopicRecord(state,topicId,AppUtil.getUser().getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加回复
     * @param reply
     */
    @Override
    public void addReply(Reply reply) {
        try {
            replyMapper.insert(reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据id查询回复人姓名
     * @param replyId
     * @return
     */
    @Override
    public String queryReplyById(Integer replyId) {
        return replyMapper.queryReplyById(replyId);
    }

    /**
     * 根据评论id查询回复信息
     * @param commentId
     * @return
     */
    @Override
    public List<Reply> queryReply(Integer commentId,Integer userId) {
        return replyMapper.queryReply(commentId,userId);
    }

    /**
     * 根据id查询回复信息
     * @param commentId
     * @param pageNum
     * @return
     */
    @Override
    public PageInfo<Reply> queryReplyforPage(Integer commentId,Integer userId, int pageNum) {
        PageHelper.startPage(pageNum,5);
        List<Reply> list = replyMapper.queryReply(commentId,userId);
        return new PageInfo<>(list);
    }

    /**
     * 查询当日板块发帖数(板块)
     * @param companyCode
     * @param forumId
     * @return
     */
    @Override
    public List<Integer> queryTopicByToday(String companyCode, Integer forumId) {
        return topicMapper.queryTopicByToday(companyCode, forumId);
    }

    /**
     * 查询当日板块发帖数(我的)
     * @param companyCode
     * @param forumId
     * @return
     */
    @Override
    public List<Integer> queryTodayByUserId(String companyCode, Integer forumId) {
        Integer userId = AppUtil.getUser().getId();
        return topicMapper.queryTodayByUserId(companyCode,forumId,userId);
    }

    /**
     * 申请加精
     * @param topicId
     * @param highReason
     */
    @Override
    public void updateLevel(Integer topicId, String highReason,Integer state) {
        try {
            topicMapper.updateLevel(topicId,highReason,state);
            Topic topic = topicMapper.queryById(topicId);
            String tips = "";
            if(state==1){
               //发给版主
                tips = String.format("[论坛]%s的帖子[%s]加精申请已提交,申请加精原因：%s",topic.getUserName(),topic.getTitle(),highReason);
                addMessage(topic,tips);
            }else{
                //通知发帖人
                tips = String.format("[论坛]您的帖子[%s],经过审核，申请加精没有成功，再接再厉哦！拒绝理由：%s",topic.getTitle(),highReason);
                sendMessage(topic,tips);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 申请置顶
     * @param topicId
     * @param topReason
     */
    @Override
    public void updateTop(Integer topicId, String topReason,Integer state) {
        try {
            topicMapper.updateTop(topicId,topReason,state);
            Topic topic = topicMapper.queryById(topicId);
            String tips = "";
            if(state==1){
                //发给版主
                tips = String.format("[论坛]%s的帖子[%s]置顶申请已提交，申请置顶原因：%s",topic.getUserName(),topic.getTitle(),topReason);
                addMessage(topic,tips);
            }else{
                //通知发帖人
                tips = String.format("[论坛]您的帖子[%s],经过审核，申请置顶没有成功，再接再厉哦！拒绝理由：%s",topic.getTitle(),topReason);
                sendMessage(topic,tips);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消加精
     * @param topicId
     */
    @Override
    public void notAddLevel(Integer topicId) {
        try {
            User user= AppUtil.getUser();
            String name = user.getName();
            Integer id = user.getId();
            topicMapper.notAddLevel(topicId,new Date(),id,name);
            Topic topic = topicMapper.queryById(topicId);
            String tips = String.format("[论坛]您提交的[%s]的帖子已被取消加精",topic.getTitle());
            sendMessage(topic,tips);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消置顶
     * @param topicId
     */
    @Override
    public void notGoTop(Integer topicId) {
        try {
            User user= AppUtil.getUser();
            String name = user.getName();
            Integer id = user.getId();
            topicMapper.notGoTop(topicId,new Date(),id,name);
            Topic topic = topicMapper.queryById(topicId);
            String tips = String.format("[论坛]您提交的[%s]的帖子已被取消置顶",topic.getTitle());
            sendMessage(topic,tips);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //查询部门下的所有人
    private List<User> queryDeptUser(Integer id){
        List<User> list = userMapper.queryUserByDeptIdONLY(id);
        return list;
    }

    /**
     * 查看部门及人员的数据
     * @param flag
     * @return
     */
    @Override
    @Cacheable(value = "DeptTree", key = "'flag='+#flag+',companycode='+#companyCode")
    public List<DeptTree> queryDeptTree(Integer flag,String companyCode) {
        List<DeptTree> list = new ArrayList<>();
        try {
            //查询所有部门集合
            List<Dept> deptList = deptMapper.queryDeptByCompany(companyCode);
            if(deptList!=null && deptList.size()>0) {
                for (Dept dept : deptList) {
                    //创建部门人员数据结构
                    DeptTree dt = new DeptTree();
                    Integer deptId = dept.getId();
                    Integer level = dept.getLevel();
                    Integer parentId = dept.getParentId();
                    String deptName = dept.getName();
                    if (flag == 1) {
                        //如果是集团那么设置其为父级节点
                        if (level == 0) {
                            dt.setPid(0);
                        } else {
                            dt.setPid(parentId);
                        }
                    } else {
                        //如果是分公司的话则设置其为父级节点
                        if (level == 1) {
                            dt.setPid(0);
                        } else {
                            dt.setPid(parentId);
                        }
                    }
                    dt.setId(deptId);
                    dt.setName(deptName);
                    //查询部门下的人
                    List<User> userList = queryDeptUser(deptId);
                    List<DeptTree> deptTrees = new ArrayList<>();
                    if(userList.size()>0 && userList!=null){
                        for (User user:userList) {
                            //保存部门下的人员
                            DeptTree deptTree = new DeptTree();
                            deptTree.setId(user.getId());
                            deptTree.setName(user.getName());
                            deptTree.setPid(user.getDeptId());
                            //设置人员的图标
                            deptTree.setIcon("/img/user.png");
                            deptTrees.add(deptTree);
                        }
                    }
                    dt.setChildren(deptTrees);
                    list.add(dt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    /**
     * 根据帖子id查询所有可见人id
     * @param topicId
     * @return
     */
    @Override
    public List<Map> queryTopicRelation(Integer topicId) {
         return topicMapper.queryTopicRelation(topicId);
    }

    /**
     * 编辑帖子回显主题图状态
     * @return
     */
    @Override
    public List<Topic> queryTopicImage(Integer topicId){
        return topicMapper.queryTopicImage(AppUtil.getUser().getCompanyCode(),topicId);
    }

    /**
     * 论坛首页查询可见的主题图片
     * @return
     */
    @Override
    public PageInfo<Topic> queryFirstImage(String companyCode) {
        PageHelper.startPage(1,6);
        List<Topic> list = topicMapper.queryFirstImage(companyCode);
        if(list==null || list.size()==0){
            return new PageInfo<>();
        }
        return new PageInfo(list);
    }

    /**
     * 保存帖子可见权限设置
     * @param topicId 帖子id
     * @param comm 可评论用户id
     * @param users 可见人员id
     * @param flag 可见标志0部分可见，1全部可见
     */
    private void saveTopicRelation(String [] users,String [] comm,String commIds,Integer topicId,Integer flag) {
        try {
            List<Map> list = new ArrayList<>();
            String companyCode = AppUtil.getUser().getCompanyCode();
            Map map;
            String viewFlag="";
            if(flag==0){//部分可见
                if(comm!=null && comm.length>0){
                    //可见人员，评论人员
                    if (users != null && users.length > 0) {
                        if(users.length==1){
                            viewFlag = companyCode;
                        }
                        for (int i=0; i < users.length; i++) {
                            String userId = users[i];
                            map = new HashMap();
                            map.put("topicId",topicId);
                            map.put("userId", userId);
                            map.put("viewFlag",viewFlag);
                            //部分可见此字段没用
                            map.put("commUserIds",null);
                            for (String commId:comm) {
                                if (userId.equals(commId)){
                                    map.put("flag", 1);//可评论
                                    break;
                                } else {
                                    map.put("flag", 0);//不评论
                                }
                            }
                            list.add(map);
                        }
                    }
                }else{
                    if(users!=null && users.length>0){
                        //只有可见人员（1、一人可见2、多人可见）
                        for (int i = 0;i<users.length;i++){
                            map=new HashMap();
                            if(users.length==1){
                                map.put("viewFlag",companyCode);
                            }else{
                                map.put("viewFlag","");
                            }
                            map.put("topicId",topicId);
                            map.put("flag",0);
                            map.put("userId",users[i]);
                            //部分可见此字段没用
                            map.put("commUserIds",null);
                            list.add(map);
                        }
                    }
                }
            }else if (flag==1){//全部可见
                map = new HashMap();
                map.put("viewFlag",companyCode);
                map.put("topicId",topicId);
                map.put("flag",1);
                map.put("userId",AppUtil.getUser().getId());
                map.put("commUserIds",commIds);
                list.add(map);
            }
            topicMapper.saveTopicRelation(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new QinFeiException(1002,"很抱歉,添加帖子关系表时出错啦，请联系技术人员！");
        }
    }

    /**
     * 查询可见评论权限
     * @param topicId 主题id
     * @param id 发帖人id
     * @return
     */
    @Override
    public Map queryRelationFlag(Integer topicId,Integer id) {
        Map map = new HashMap();
        try {
            Integer userId = AppUtil.getUser().getId();
            //flag=1可评论，flag=0不可评论
            Integer flag = topicMapper.queryRelationFlag(topicId,userId);
            //全部可见评论人员集合(id:发帖人id)
            List<String> list = topicMapper.queryCommIds(topicId,id);
            String commIds = "";
            for(int i=0;i<list.size();i++){
                if(StringUtils.isNotEmpty(list.get(i))){
                    commIds=list.get(i);
                }
            }
            if("".equals(commIds) || commIds==null){
                //flag=null无评论权限，否则有评论权限
                flag = flag==null?0:flag;
            }else {
                //commIds包含当前用户则有评论权限，否则无评论权限
                flag = commIds.indexOf(userId.toString())==-1?0:1;
            }
            map.put("flag",flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 首页推荐帖子
     * @param pageNum
     * @return
     */
    @Override
    public String queryFirstInfo(int pageNum) {
        Map map = new HashMap();
        User user = AppUtil.getUser();
        map.put("companyCode",user.getCompanyCode());
        map.put("userId",user.getId());
        PageHelper.startPage(pageNum, 20);
        List<Topic> list = topicMapper.queryFirstInfo(map);
        PageInfo<Topic> topicPageInfo = new PageInfo<>(list);

        // 开始拼接返回内容；
        StringBuilder content = new StringBuilder();
        content.append("<li class=\"content\" style=\"overflow:auto;height:800px\">");
        int size = list.size();
        if (size > 0 && pageNum <= topicPageInfo.getPages()) {
            String Field;
            String dataContent;
            for (int i = 0; i < size; i++) {
                Topic topic = list.get(i);
                content.append("<div class=\"excerpt\">");
                content.append("<div>");
                content.append("<div style=\"padding: 10px;border-bottom: #ebebeb 1px solid;\">");
                // --------------------------------头像模块；------------------------------------
                content.append("<div style=\"display:flex\">");
                if(pageNum==1){
                    if(i==0){
                        content.append("<div style=\"display:inline-block;background: url(/img/bbs/jin.png) 34px 0px no-repeat;padding: 6px 16px 0 0;\" id=\"box");
                    }else if(i==1){
                        content.append("<div style=\"display:inline-block;background: url(/img/bbs/yin.png) 34px 0px no-repeat;padding: 6px 16px 0 0;\" id=\"box");
                    }else if(i==2 ){
                        content.append("<div style=\"display:inline-block;background: url(/img/bbs/tong.png) 34px 0px no-repeat;padding: 6px 16px 0 0;\" id=\"box");
                    }else{
                        content.append("<div style=\"display:inline-block;padding: 6px 16px 0 0;\" id=\"box");
                    }
                }else{
                    content.append("<div style=\"display:inline-block;padding: 6px 16px 0 0;\" id=\"box");
                }
                content.append(i);
                content.append("\">");
                // 获取照片；
                Field = topic.getPicture();
                dataContent = StringUtils.isEmpty(Field) ? "/img/mrtx_1.png" : Field.replace("\\images\\", "/images/");
                content.append("<img alt=\"图片丢失\" onerror=\"this.src='/img/mrtx_1.png'\" class=\"img-circle\" style='width:50px;height:50px;' src=\"");
                content.append(dataContent);
                content.append("\"/>");
                content.append("</div>");
                content.append("<div style='display:inline-block;width:75%;'>");
                Field = topic.getForumName();
                content.append("<div style=\"padding:8px 0;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;\">");
                content.append("<span style=\"color:#346fa9;margin-right:15px;\">【&nbsp;");
                content.append(Field == null ? "" : Field);
                content.append("&nbsp;】</span>");
                content.append("<a style=\"color:#222222;margin:10px 0;\" href='/bbs/showTopic?topicId=");
                content.append(topic.getId());
                content.append("&companyCode=");
                content.append(topic.getCompanyCode());
                content.append("'>");
                Field = topic.getTitle();
                content.append(Field == null ? "" : Field);
                content.append("</a>");
                content.append("</div>");
                //---------------------用户名----------------------
                content.append("<div style=\"display:inline-block;width:80%;\">");
                content.append("<span style=\"margin-right:10px\">");
                String userName = topic.getUserName();
                content.append(userName == null ? "" : userName);
                content.append("</span>");
                content.append("<span style=\"color:#999999\">");
                Date date = topic.getCreateTime();
                String sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date);
                content.append(date == null ? "" : sdf);
                content.append("</span>");
                content.append("</div>");
                content.append("<div style='text-align: right;display:inline-block;width:20%;' >");
                content.append("<span><img style='width:11px;height:14px;' src='/img/bbs/yuedu.png' title='阅读数'/>&nbsp;&nbsp;");
                Field = topic.getViewNum().toString();
                content.append(Field == null ? "" : Field);
                content.append("</span>");
                content.append("</div>");
                content.append("</div>");
                content.append("</div>");
                // --------------------------------头像模块；------------------------------------

                // --------------------------------信息模块；------------------------------------
//                content.append("<div class=\"col-sm-12\">");
                // 标题；
//              // 内容
//                content.append("<div id='showTopicContent' class='contentStyle col-sm-12'>");
//                Field = topic.getContent();
//                content.append(Field == null ? "" : Field);
//                content.append("</div>");
//                content.append("</div>");
                // --------------------------------信息模块；------------------------------------
                // 底部填充；
                content.append("<div class=\"clearfix\"></div>");
                content.append("</a>");
                content.append("</div>");
                content.append("</div>");
                content.append("</div>");
            }
        } else {
            content.append("<div class='pagination'><h3><strong>查询无结果。</strong></h3></div>");
        }
        // 导航；
        content.append("<nav class=\"pagination\" style=\"display:none;\"><ul><li class=\"next-page\"><a href=\"/topic/queryFirstInfo\"></a></li></ul></nav>");
        content.append("</li>");
        return content.toString();
    }
}
