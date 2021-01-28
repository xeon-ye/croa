package com.qinfei.qferp.service.impl.bbs;

import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.bbs.Forum;
import com.qinfei.qferp.mapper.bbs.ForumMapper;
import com.qinfei.qferp.mapper.bbs.TopicMapper;
import com.qinfei.qferp.service.bbs.IForumService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛板块接口的实现类
 * @author tsf
 */
@Slf4j
@Service
public class ForumService implements IForumService {
    @Autowired
    private ForumMapper forumMapper;
    @Autowired
    private TopicMapper topicMapper;

    /**
     * 根据id查询论坛信息
     * @param id
     * @return
     */
    @Override
    public Forum queryById(Integer id) {
        Forum forum = forumMapper.queryById(id);
        String companyCode = forum.getCompanyCode();
        String name = forumMapper.queryCompanyCode(companyCode);
        forum.setCompanyCodeName(name);
        return forum;
    }

    /**
     * 论坛板块去重
     * @param companyCode
     * @param name
     * @return
     */
    public Boolean checkForum(String companyCode,String name,Integer id){
        Boolean flag = false;
        if(id!=null){
            // 编辑页面判断板块是否重复，先排除自己
            Forum forum = forumMapper.findById(id);
            if(name.equals(forum.getName())){
                flag = true;
            }else{
                List<Forum> list = forumMapper.checkForum(companyCode, name);
                if(list.size()==0){
                    flag = true;
                }
            }
        }else{
            List<Forum> list = forumMapper.checkForum(companyCode, name);
            if(list.size()==0){
                flag = true;
            }
        }
         return flag;
    }

    @Override
    public String queryCompanyCode(String companyCode) {
        return forumMapper.queryCompanyCode(companyCode);
    }

    /**
     * 查询所有的论坛信息
     * @param map
     * @return
     */
    @Override
    public PageInfo<Map> queryForum(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        List<Map> list = forumMapper.queryForum(map);
        return new PageInfo<>(list);
    }

    /**
     * 论坛管理查询分公司下的论坛板块
     * @param companyCode
     * @return
     */
    public List<Map> queryForum(String companyCode){
        return forumMapper.findForum(companyCode);
    }

    public List<Map> getForumData(String companyCode){
        List<Map> list = forumMapper.getForumData(companyCode);
        if(list.size()>0){
            for(int i = 0;i<list.size();i++){
                Integer forumId = (Integer)list.get(i).get("id");
                if(!StringUtils.isEmpty(forumId)){
                    Integer replyNum= topicMapper.getForumReplyNum(forumId);
                    list.get(i).put("replyNum",replyNum);
                }
            }
        }
        return list;
    }

    /**
     * 添加论坛板块
     * @param forum
     */
    @Override
    @Log(opType = OperateType.ADD, module = "论坛版块管理|添加论坛板块", note = "添加论坛板块")
    public void addForum(Forum forum) {
        forum.setId(null);
        forum.setState(0);
        forumMapper.insert(forum);
    }

    /**
     * 修改论坛板块
     * @param forum
     */
    @Override
    @Log(opType = OperateType.UPDATE, module = "论坛版块管理|修改论坛板块", note = "修改论坛板块")
    public void updateForum(Forum forum) {
        forum.setUpdateTime(new Date());
        forumMapper.updateForum(forum);
    }

    /**
     * 删除论坛信息
     * @param id
     */
    @Override
    @Log(opType = OperateType.DELETE, module = "论坛版块管理|删除论坛板块", note = "删除论坛板块")
    public void delForum(Integer id) {
        forumMapper.delForum(new Date(),id);
    }

    @Override
    public Map getForumInfo(Integer forumId) {
        return forumMapper.getForumInfo(forumId);
    }

    @Override
    public Forum getById(Integer id) {
        return forumMapper.queryById(id);
    }

    @Override
    public List<Forum> getForumByBanzu(String companyCode,Integer forumId) {
        Map map = new HashMap();
        map.put("moderator",AppUtil.getUser().getId());
        map.put("companyCode",companyCode);
        map.put("forumId",forumId);
        return forumMapper.getForumByBanzu(map);
    }


}
