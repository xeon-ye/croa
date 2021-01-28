package com.qinfei.qferp.controller.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.administrative.AdministrativeOnBusiness;
import com.qinfei.qferp.service.impl.administrative.AdministrativeOnBusinessService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/onBusiness")
class AdministrativeOnBusinessController {
    
    @Autowired
    private AdministrativeOnBusinessService onBusinessService;

    /**
     * 添加出差申请
     * @param onBusiness
     * @return
     */
    @RequestMapping("saveOnBusiness")
    @ApiOperation(value = "提交出差申请", notes = "保存出差信息")
//    @Log(opType = OperateType.ADD, module = "出差管理", note = "保存出差信息")
    @ResponseBody
    public ResponseData saveOnBusiness(AdministrativeOnBusiness onBusiness,@RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] files) {
        ResponseData data = null;
        data = onBusinessService.addOnBusiness(onBusiness,files,pics);
        return data;
    }

    /**
     * 出差申请审核
     * @param onBusiness
     * @param multipartFiles
     * @return
     */
    @RequestMapping("editOnBusiness")
    @ApiOperation(value = "开启出差申请审核", notes = "开启出差审核")
//    @Log(opType = OperateType.UPDATE, module = "出差管理", note = "开启出差审核")
    @ResponseBody
    public ResponseData onBusiness(AdministrativeOnBusiness onBusiness, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles) {
        return onBusinessService.edit(onBusiness,multipartFiles,pics);
    }

    /**
     * 出差（保存后）申请审核
     * @param onBusiness
     * @param pics
     * @param multipartFiles
     * @return
     */
    @RequestMapping("editUpdateOnBusiness")
    @ApiOperation(value = "开启出差（保存后）申请审核", notes = "开启出差（保存后）审核")
//    @Log(opType = OperateType.UPDATE, module = "出差（保存后）管理", note = "开启出差（保存后）审核")
    @ResponseBody
    public ResponseData editUpdateOnBusiness(AdministrativeOnBusiness onBusiness,
                                             @RequestParam(value = "pic", required = false) MultipartFile[] pics,
                                             @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles,
                                             @RequestParam(value = "report", required = false) MultipartFile[] reports){
        return onBusinessService.editUpdateOnBusiness(onBusiness,multipartFiles,pics,reports);
    }

    /**
     * 删除出差信息
     * @param onBusinessId
     * @return
     */
    @RequestMapping("deleteOnBusiness")
    @ApiOperation(value = "删除出差申请", notes = "删除出差信息")
//    @Log(opType = OperateType.DELETE, module = "出差管理", note = "删除出差信息")
    @ResponseBody
    public ResponseData deleteOnBusiness(int onBusinessId){
        ResponseData data = ResponseData.ok();
        //根据出差id删除出差信息
        onBusinessService.deleteOnBusiness(onBusinessId);
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", onBusinessId) ;
        return data;
    }

    /**
     * 通过流程id获取出差的详细信息
     * @param id
     * @return
     */
    @RequestMapping("getOnBusinessByAdmId")
    @ApiOperation(value = "出差查询", notes = "通过流程id查询出差信息")
//    @Log(opType = OperateType.QUERY, module = "出差管理", note = "通过流程id查询出差信息")
    @ResponseBody
    public ResponseData getOnBusinessByAdmId(Integer id){
        return onBusinessService.getOnBusinessByAdministrativeId(id);
    }

    /**
     * 修改出差信息
     * @param onBusiness
     * @param files
     * @return
     */
    @RequestMapping("updateOnBusinessById")
    @ApiOperation(value = "出差管理", notes = "修改出差信息")
//    @Log(opType = OperateType.UPDATE, module = "出差管理", note = "修改出差信息")
    @ResponseBody
    public ResponseData updateOnBusinessById(AdministrativeOnBusiness onBusiness,
     @RequestParam(value = "pic", required = false) MultipartFile[] pics,
     @RequestParam(value = "file", required = false) MultipartFile[] files,
     @RequestParam(value = "reports", required = false) MultipartFile[] reports){
        return onBusinessService.updateOnBusinessByAdmId(onBusiness,files,pics,reports);
    }

    //删除出差信息
    @RequestMapping("deleteOnBusinessByAdmId")
    @ApiOperation(value = "出差管理", notes = "删除出差信息")
//    @Log(opType = OperateType.UPDATE, module = "出差管理", note = "删除出差信息")
    @ResponseBody
    public ResponseData deleteOnBusinessByAdmId(Integer id){
        return onBusinessService.deleteOnBusinessByAdmId(id);
    }
}
