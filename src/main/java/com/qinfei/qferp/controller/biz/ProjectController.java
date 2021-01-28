package com.qinfei.qferp.controller.biz;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.biz.Project;
import com.qinfei.qferp.service.biz.IProjectService;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 项目管理
 */
@Slf4j
@Controller
@RequestMapping("project")
class ProjectController {
    @Autowired
    IProjectService projectService;

    @ResponseBody
    @RequestMapping("/listPg")
    public PageInfo<Map> listPg(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        return projectService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
    }

    @ResponseBody
    @RequestMapping("/initNodeConfig")
    public ResponseData initNodeConfig(Map map) {
        try {
            ResponseData data = ResponseData.ok();
            List<Map> list = projectService.initNodeConfig(map);
            data.putDataValue("list", list);
            return data;
        } catch (QinFeiException e) {
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/add")
    @ResponseBody
    @Log(opType = OperateType.ADD, module = "项目管理", note = "编辑项目")
//    @Verify(code = "/project/edit", module = "项目管理/编辑项目")
    public ResponseData add(Project project, @RequestParam Map map) {
        try{
            project.setId(null);
            Project entity = projectService.add(project,map);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", entity) ;
            return data ;
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "项目管理", note = "编辑项目")
//    @Verify(code = "/project/edit", module = "项目管理/编辑项目")
    public ResponseData edit(Project project, @RequestParam Map map) {
        try{
            Project entity = projectService.edit(project,map);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", entity) ;
            return data ;
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }



    @RequestMapping("/view")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "项目管理", note = "编辑项目")
//    @Verify(code = "/project/edit", module = "项目管理/编辑项目")
    public ResponseData view(@RequestParam("id") Integer id) {
        try{
            Map map = projectService.view(id);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("data",map);
            data.putDataValue("message","操作成功");
//            data.putDataValue("entity", entity) ;
            return data ;
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping("/enableProject")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "项目管理|启用项目", note = "项目管理|启用项目")
    public ResponseData enableProject(@RequestParam("id") Integer id) {
        try{
            Project entity = projectService.getById(id) ;
            //审核通过才能出款
            if(entity.getState() == IConst.STATE_FINISH && entity.getDisabled() == IConst.STATE_FINISH){
                projectService.enableOrDisable(entity,IConst.STATE_SAVE);
                ResponseData data = ResponseData.ok() ;
                data.putDataValue("message","操作成功");
                return data ;
            }else{
                return ResponseData.customerError(1002,"当前状态不支持启用操作，请刷新后重试") ;
            }
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }
    @RequestMapping("/disableProject")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "项目管理|停用项目", note = "项目管理|停用项目")
    public ResponseData disableProject(@RequestParam("id") Integer id) {
        try{
            Project entity = projectService.getById(id) ;
            //审核通过才能出款
            if(entity.getState() == IConst.STATE_FINISH && entity.getDisabled() == IConst.STATE_SAVE){
                projectService.enableOrDisable(entity,IConst.STATE_FINISH);
                ResponseData data = ResponseData.ok() ;
                data.putDataValue("message","操作成功");
                return data ;
            }else{
                return ResponseData.customerError(1002,"当前状态不支持停用操作，请刷新后重试") ;
            }
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping("/del")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "项目管理|删除项目", note = "项目管理|删除项目")
    public ResponseData del(@RequestParam("id") Integer id) {
        try{
            projectService.del(id);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            return data ;
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @ResponseBody
    @RequestMapping("/queryArticlesByProjectId")
    public PageInfo<Map> queryArticlesByProjectId(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        return projectService.queryArticlesByProjectId(pageable.getPageNumber(), pageable.getPageSize(), map);
    }

    @ResponseBody
    @RequestMapping("/querySumByProjectId")
    public ResponseData querySumByProjectId(@RequestParam Map map) {
        try{
            Map result = projectService.querySumByProjectId(map);
            Double commSum = 0D;
            Double saleSum = 0D;
            Double incomeSum = 0D;
            Integer count = 0 ;
            if(!ObjectUtils.isEmpty(result)){
                if(result.containsKey("commSum")){
                    commSum = MapUtils.getDoubleValue(result,"commSum");
                }
                if(result.containsKey("saleSum")){
                    saleSum = MapUtils.getDoubleValue(result,"saleSum");
                }
                if(result.containsKey("incomeSum")){
                    incomeSum = MapUtils.getDoubleValue(result,"incomeSum");
                }
                if(result.containsKey("count")){
                    count = MapUtils.getInteger(result,"count");
                }
            }
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("commSum", commSum);
            data.putDataValue("saleSum", saleSum);
            data.putDataValue("incomeSum", incomeSum);
            data.putDataValue("notIncomeSum", new BigDecimal(saleSum).subtract(new BigDecimal(incomeSum)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            data.putDataValue("count", count);
            data.putDataValue("message","操作成功");
            return data ;
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping("/confirm")
    @ResponseBody
    public ResponseData confirm(@RequestParam("itemId") Integer itemId) {
        try{
            projectService.confirm(itemId);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            return data ;
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }
}