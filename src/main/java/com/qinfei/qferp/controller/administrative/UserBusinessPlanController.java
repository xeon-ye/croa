package com.qinfei.qferp.controller.administrative;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.administrative.UserBusinessConclusion;
import com.qinfei.qferp.entity.administrative.UserBusinessPlan;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.service.administrative.IUserBusinessPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/userBusinessPlan")
@Api(description = "新增出差计划")
public class UserBusinessPlanController {
    @Autowired
    private IUserBusinessPlanService userBusinessPlanService;

    @PostMapping("saveBusiness")
    @ApiOperation(value = "新增出差计划",notes = "新增出差计划")
    @ResponseBody
    public ResponseData saveBusiness(@RequestBody UserBusinessPlan userBusinessPlan){
        try{
            userBusinessPlanService.saveBusiness(userBusinessPlan);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, e.getMessage());
        }


    }
    @PostMapping("editOnBusiness")
    @ApiOperation(value = "新增出差提交审核",notes = "新增出差提交审核")
    @ResponseBody
    public ResponseData onBusiness(UserBusinessPlan userBusinessPlan,
                                   @RequestParam(value = "pic1", required = false) MultipartFile[] pics,
                                   @RequestParam(value = "file1", required = false) MultipartFile[] multipartFiles){
        try{
            userBusinessPlanService.onBusiness(userBusinessPlan,multipartFiles,pics);            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, e.getMessage());
        }

    }

    /**
     * 编辑获取出差计划
     * @param id
     * @return
     */
    @PostMapping("getBussiness")
    @ResponseBody
    public ResponseData getBussiness(Integer id){
        return userBusinessPlanService.getBussiness(id);
    }

    /**
     * 新增保存按钮
     * @param userBusinessPlan
     * @param pics
     * @param files
     * @return
     */
    @PostMapping("save")
    @ResponseBody
    public ResponseData save (UserBusinessPlan userBusinessPlan,
                              @RequestParam(value = "pic1", required = false) MultipartFile[] pics,
                              @RequestParam(value = "file1", required = false) MultipartFile[] files){
        return userBusinessPlanService.addBusiness(userBusinessPlan,files,pics);
    }

    /**
     * 编辑保存
     * @param userBusinessPlan
     * @param pics
     * @param files
     * @return
     */

    @PostMapping("updateBusiness")
    @ResponseBody
    public ResponseData updateBusiness(UserBusinessPlan userBusinessPlan,
                                     @RequestParam(value = "pic1", required = false) MultipartFile[] pics,
                                     @RequestParam(value = "file1", required = false) MultipartFile[] files){
           return userBusinessPlanService.updateBusiness(userBusinessPlan,pics,files);
    }

    /**
     * 编辑提交审核
     * @param userBusinessPlan
     * @param pics
     * @param files
     * @return
     */
    @PostMapping("editUpdateOnBusiness")
    @ResponseBody
    public ResponseData editUpdateOnBusiness(UserBusinessPlan userBusinessPlan,
    @RequestParam(value = "pic1", required = false) MultipartFile[] pics,
    @RequestParam(value = "file1", required = false) MultipartFile[] files){
        return userBusinessPlanService.editUpdateOnBusiness(userBusinessPlan,files,pics);

    }

    /**
     * 删除出差计划
     * @param id
     * @return
     */

    @PostMapping("deleteBussiness")
    @ResponseBody
    public ResponseData deleteBussiness(Integer id){
        return userBusinessPlanService.deleteBussiness(id);
    }

    /**
     * 查看出差计划
     * @param id
     * @return
     */
    @PostMapping("viewBussiness")
    @ResponseBody
    public ResponseData viewBussiness(Integer id){
        return userBusinessPlanService.viewBusiness(id);
    }

    /**
     * 新增总结
     * @param map
     * @param pics
     * @param attach
     * @return
     */
    @RequestMapping("addConclusion")
    @ResponseBody
    public ResponseData addConclusion (@RequestParam Map map, @RequestParam(value = "pic", required = false) MultipartFile[] pics,
                                       @RequestParam(value = "attach", required = false) MultipartFile[] attach){
        return userBusinessPlanService.addConclusion(map,pics,attach);

    }

    /**
     * 根据用户查询到出差审批人、批准人
     * @return
     */

    @PostMapping("selectUser")
    @ResponseBody
    public ResponseData selectUser(){
        return userBusinessPlanService.selectUser();
    }

    /**
     * 删除总结
     * @param id
     * @return
     */
    @PostMapping("deleteConclusion")
    @ResponseBody
    public int deleteConclusion(Integer id){
        return userBusinessPlanService.deleteConclusion(id);
    }

    /**
     * 查询附件
     * @param id
     * @return
     */
    @PostMapping("selectFile")
    @ResponseBody
    public UserBusinessConclusion selectFile(Integer id){
        return userBusinessPlanService.selectFile(id);
    }

    /**
     * 增加待办
     * @param userId
     * @param administrativeId
     * @return
     */

    @PostMapping("addItem")
    @ResponseBody
    public Items addItem(Integer userId,Integer administrativeId ){
        return userBusinessPlanService.addItem(userId,administrativeId);
    }

    /**
     * 增加总结待办发送
     * @param userId
     * @param administrativeId
     * @return
     */
    @PostMapping("addConlusionItem")
    @ResponseBody
    public Items addConlusionItem(Integer userId,Integer administrativeId ){
        return userBusinessPlanService.addConlusionItem(userId,administrativeId);
    }

    /**
     * 抄送待办确认
     */

    @RequestMapping("/confirm")
    @ResponseBody

    public ResponseData confirm (Integer itemId){
        ResponseData data = ResponseData.ok();
        userBusinessPlanService.confirm(itemId);
        data.putDataValue("message","确认成功");
        return data;
    }
    @RequestMapping("/confirm1")
    @ResponseBody
    public ResponseData confirm1 (Integer itemConclusionId){
        ResponseData data = ResponseData.ok();
        userBusinessPlanService.confirm1(itemConclusionId);
        data.putDataValue("message","确认成功");
        return data;
    }

    /**
     * 判断有没有鞋出差总结报告
     * @param admId
     * @return
     */
    @RequestMapping("/getConlusion")
    @ResponseBody
    public int getConlusion(Integer admId){
       Integer sum =  userBusinessPlanService.getConclusion(admId);
       return sum;

    }

    /**
     * 判断有没有填写报销
     */
    @RequestMapping("getReimbursement")
    @ResponseBody
    public int getReimbursement(Integer admId){
        return userBusinessPlanService.getReimbursement(admId);
    }

}
