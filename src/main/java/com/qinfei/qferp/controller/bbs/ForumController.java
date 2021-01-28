package com.qinfei.qferp.controller.bbs;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.bbs.Forum;
import com.qinfei.qferp.service.bbs.IForumService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 论坛板块控制器
 * @author tsf
 */
@Slf4j
@Controller
@RequestMapping("/forum")
class ForumController {
    @Autowired
    private IForumService forumService;

    /**
     * 查询所有的论坛信息
     * @param map
     * @param pageable
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPg")
    public PageInfo<Map> findForum(@RequestParam Map map,@PageableDefault() Pageable pageable){
        return forumService.queryForum(map,pageable);
    }

    /**
     * 根据id查询论坛版块信息
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/getById")
    public ResponseData getById(@RequestParam("id") Integer id){
        ResponseData data = ResponseData.ok();
        data.putDataValue("entity",forumService.queryById(id));
        return data;
    }

    /**
     * 根据id查询论坛版块信息(论坛列表调用)
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryForumById")
    public ResponseData queryForumById(@RequestParam("id") Integer id){
        ResponseData data = ResponseData.ok();
        data.putDataValue("entity",forumService.getById(id));
        return data;
    }

    /**
     * 论坛管理查询分公司下的论坛板块
     * @Param companyCode
     * @return
     */
    @ResponseBody
    @RequestMapping("/findForumByCode")
    public ResponseData findForumByCode(@RequestParam("companyCode") String companyCode) {
        ResponseData data = ResponseData.ok();
        List<Map> list = forumService.queryForum(companyCode);
        data.putDataValue("list", list);
        return data;
    }

    /**
     * 论坛首页板块显示数据
     * @param companyCode
     * @return
     */
    @ResponseBody
    @RequestMapping("/getForumData")
    public ResponseData getForumData(@RequestParam("companyCode") String companyCode) {
        ResponseData data = ResponseData.ok();
        List<Map> list = forumService.getForumData(companyCode);
        data.putDataValue("list", list);
        return data;
    }

    /**
     * 添加论坛板块信息
     * @param forum
     * @return
     */
    @ResponseBody
    @RequestMapping("/add")
//    @Log(opType = OperateType.ADD, module = "论坛板块管理|论坛板块添加", note = "论坛板块添加")
    public ResponseData saveForum(Forum forum){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","保存成功");
        forumService.addForum(forum);
        return data;
    }

    /**
     * 编辑论坛版块信息
     * @param forum
     * @return
     */
    @ResponseBody
    @RequestMapping("/edit")
//    @Log(opType = OperateType.UPDATE, module = "论坛板块管理|论坛板块修改", note = "论坛板块修改")
    public ResponseData updateForum(Forum forum){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","保存成功");
        forumService.updateForum(forum);
        return data;
    }

    /**
     * 删除论坛版块信息
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/del")
//    @Log(opType = OperateType.DELETE, module = "论坛板块管理|论坛板块删除", note = "论坛板块删除")
    public ResponseData updateForum(@RequestParam("id") Integer id){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","删除成功");
        forumService.delForum(id);
        return data;
    }

    /**
     * 论坛版块去重
     * @param companyCode
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping("/hasForum")
    public Integer hasForum(@RequestParam("companyCode") String companyCode,@RequestParam("name") String name,@RequestParam(value="id",required = false) Integer id){
        Boolean result = forumService.checkForum(companyCode,name,id);
        return result?1:0;
    }

    /**
     * 获取指定板块的基本信息
     * @param forumId
     * @return
     */
    @ResponseBody
    @RequestMapping("/getForumInfo")
    public Map getForumInfo(@RequestParam("forumId") Integer forumId){
        return forumService.getForumInfo(forumId);
    }

    /**
     * 获取指定板块的基本信息
     * @return
     */
    @ResponseBody
    @RequestMapping("/getForumByBanzu")
    public List<Forum> getForumByBanzu(@RequestParam("companyCode") String companyCode,@RequestParam("forumId") Integer forumId){
        return forumService.getForumByBanzu(companyCode,forumId);
    }
}
