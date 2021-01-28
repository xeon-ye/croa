package com.qinfei.qferp.controller.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.administrative.AdministrativeOutWork;
import com.qinfei.qferp.service.administrative.IAdministrativeOutWorkService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/outWork")
class AdministrativeOutWorkController {

    @Autowired
    private IAdministrativeOutWorkService outWorkService;
    @Autowired
    private Config config;

    /**
     * 添加外出申请
     * @param outWork
     * @return
     */
    @RequestMapping("saveOutWork")
    @ApiOperation(value = "提交外出申请", notes = "保存外出信息")
//    @Log(opType = OperateType.ADD, module = "外出管理", note = "保存外出信息")
    @ResponseBody
    public ResponseData saveOutWork(AdministrativeOutWork outWork,@RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] files) {
        ResponseData data = null;
        data = outWorkService.addOutWork(outWork,files,pics);
        return data;
    }

    /**
     * 外出申请审核
     * @param outWork
     * @param multipartFiles
     * @return
     */
    @RequestMapping("editOutWork")
    @ApiOperation(value = "开启外出申请审核", notes = "开启外出审核")
//    @Log(opType = OperateType.UPDATE, module = "外出管理", note = "开启外出审核")
    @ResponseBody
    public ResponseData editOutWork(AdministrativeOutWork outWork, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles) {
        return outWorkService.edit(outWork,multipartFiles,pics);
    }

    /**
     * 外出（保存后）申请审核
     * @param outWork
     * @param pics
     * @param multipartFiles
     * @return
     */
    @RequestMapping("editUpdateOutWork")
    @ApiOperation(value = "开启外出（保存后）申请审核", notes = "开启外出（保存后）审核")
//    @Log(opType = OperateType.UPDATE, module = "外出（保存后）管理", note = "开启外出（保存后）审核")
    @ResponseBody
    public ResponseData editUpdateOutWork(AdministrativeOutWork outWork, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles){
        return outWorkService.editUpdateOutWork(outWork,multipartFiles,pics);
    }

    /**
     * 删除外出信息
     * @param outWorkId
     * @return
     */
    @RequestMapping("deleteOutWork")
    @ApiOperation(value = "删除外出申请", notes = "删除外出信息")
//    @Log(opType = OperateType.DELETE, module = "外出管理", note = "删除外出信息")
    @ResponseBody
    public ResponseData deleteOutWork(int outWorkId){
        ResponseData data = ResponseData.ok();
        //根据外出id删除外出信息
        outWorkService.deleteOutWork(outWorkId);
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", outWorkId) ;
        return data;
    }

    /**
     * 通过流程id获取外出的详细信息
     * @param id
     * @return
     */
    @RequestMapping("getOutWorkByAdmId")
    @ApiOperation(value = "外出查询", notes = "通过流程id查询外出信息")
//    @Log(opType = OperateType.QUERY, module = "外出管理", note = "通过流程id查询外出信息")
    @ResponseBody
    public ResponseData getLeaByAdmId(Integer id){
        return outWorkService.getOutWorkByAdministrativeId(id);
    }

    /**
     * 修改外出信息
     * @param outWork
     * @param files
     * @return
     */
    @RequestMapping("updateOutWorkById")
    @ApiOperation(value = "外出管理", notes = "修改外出信息")
//    @Log(opType = OperateType.UPDATE, module = "外出管理", note = "修改外出信息")
    @ResponseBody
    public ResponseData updateOutWorkById(AdministrativeOutWork outWork, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] files){
        return outWorkService.updateOutWorkByAdmId(outWork,files,pics);
    }

    /**
     * 删除外出信息
     * @param id
     * @return
     */
    @RequestMapping("deleteOutWorkByAdmId")
    @ApiOperation(value = "外出管理", notes = "删除外出信息")
//    @Log(opType = OperateType.UPDATE, module = "外出管理", note = "删除外出信息")
    @ResponseBody
    public ResponseData deleteLeaByAdmId(Integer id){
        return outWorkService.deleteOutWorkByAdmId(id);
    }
}
