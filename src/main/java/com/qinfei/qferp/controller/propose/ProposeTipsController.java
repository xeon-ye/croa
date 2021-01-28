package com.qinfei.qferp.controller.propose;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.propose.ProposeTips;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.propose.IProposeTipsService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;

/**
 * 建议模块控制层
 * @Autor :tsf
 */
@Slf4j
@Controller
@RequestMapping("/proposeTips")
class ProposeTipsController {
    @Autowired
    private IProposeTipsService proposeTipsService;
    @Autowired
    private IUserService userService;

    /**
     * 查询所有的建议提示信息
     * @param id
     * @return
     */
    @RequestMapping("/getById")
    @ResponseBody
    public ResponseData getById(@RequestParam("id")Integer id){
        ResponseData data=ResponseData.ok();
        ProposeTips entity = proposeTipsService.getById(id);
        data.putDataValue("entity",entity);
        return data;
    }

    /**
     * 查询所有的建议提示信息
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<ProposeTips> listPg(@RequestParam Map map, Pageable pageable){
        return proposeTipsService.queryProposeTips(map,pageable);
    }

    /**
     * 查询所有的建议提示信息
     * @return
     */
    @RequestMapping("/updateProposeCache")
    @ResponseBody
    public void updateProposeCache(){
        proposeTipsService.updateProposeCache(AppUtil.getUser().getId(),AppUtil.getUser().getCompanyCode());
    }

    /**
     * 新增建议提示信息
     * @param proposeTips
     * @return
     */
    @RequestMapping("/saveProposeTips")
    @ResponseBody
    public ResponseData saveProposeTips(ProposeTips proposeTips){
        try {
            ResponseData data = ResponseData.ok();
            proposeTipsService.saveProposeTips(proposeTips);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，新增建议提示出错啦，请联系技术人员！");
        }
    }

    /**
     * 修改建议提示信息
     * @param proposeTips
     * @return
     */
    @RequestMapping("/editProposeTips")
    @ResponseBody
    public ResponseData editProposeTips(ProposeTips proposeTips){
        try {
            ResponseData data = ResponseData.ok();
            proposeTipsService.editProposeTips(proposeTips);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，修改建议提示出错啦，请联系技术人员！");
        }
    }

    /**
     * 启用停用建议提示
     * @param tips
     * @return
     */
    @RequestMapping("/editTipsState")
    @ResponseBody
    public ResponseData editTipsState(ProposeTips tips){
        try {
            ResponseData data = ResponseData.ok();
            proposeTipsService.editTipsData(tips);
            data.putDataValue("message","状态修改成功！");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，启用建议提示出错啦，请联系技术人员！");
        }
    }

    /**
     * 启用停用建议制度跳转
     * @param tips
     * @return
     */
    @RequestMapping("/editDocumentState")
    @ResponseBody
    public ResponseData editDocumentState(ProposeTips tips){
        try {
            ResponseData data = ResponseData.ok();
            proposeTipsService.editDocumentData(tips);
            data.putDataValue("message","状态修改成功！");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，启用建议提示出错啦，请联系技术人员！");
        }
    }

    /**
     * 启用停用建议制度跳转
     * @return
     */
    @RequestMapping("/getDocumentUrl")
    @ResponseBody
    public ResponseData getDocumentUrl() {
        ResponseData data = ResponseData.ok();
        Integer id = proposeTipsService.getDocumentUrl();
        data.putDataValue("id",id);
        return data;
    }

    /**
     * 删除建议提示
     * @param id
     * @return
     */
    @RequestMapping("/delSuggestTips")
    @ResponseBody
    public ResponseData delSuggestTips(@RequestParam("id")Integer id){
        try {
            ResponseData data = ResponseData.ok();
            proposeTipsService.delProposeTips(id);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，删除建议提示出错啦，请联系技术人员！");
        }
    }

    /**
     * 查询需要强制提醒的用户
     * @return
     */
    @RequestMapping("/querySuggestHintData")
    @ResponseBody
    public ResponseData querySuggestHintData(){
        try {
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            if(user==null){
                return ResponseData.customerError(1002,"请先登录");
            }
            List<Integer> list = userService.querySuggestHintData(user.getCompanyCode());
            data.putDataValue("list",list);
            //查询建议提示内容
            data.putDataValue("content",proposeTipsService.getSuggestContent());
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，建议提示出错啦，请联系技术人员！");
        }
    }

    /**
     * 建议管理设置统计时间范围
     * @return
     */
    @RequestMapping("/setProposeTimeSection")
    @ResponseBody
    public ResponseData setProposeTimeSection(@RequestParam("state")Integer state,@RequestParam("startTime")String startTime,@RequestParam("endTime")String endTime){
        try {
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录！");
            }
            proposeTipsService.saveTimeSection(state,startTime,endTime,AppUtil.getUser().getCompanyCode());
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，建议管理设置统计时间范围出错啦，请联系技术人员！");
        }
    }

    @RequestMapping("/queryTipsByType")
    @ResponseBody
    public ResponseData queryTipsByType(){
        try {
            ResponseData data = ResponseData.ok();
            List<ProposeTips> list = proposeTipsService.queryTipsByType(AppUtil.getUser().getCompanyCode());
            if(CollectionUtils.isNotEmpty(list)){
               data.putDataValue("state",list.get(0).getState());
            }else{
                //如果为空，统计时间默认一个月
                data.putDataValue("state",1);
            }
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，建议管理设置统计时间范围出错啦，请联系技术人员！");
        }
    }
}
