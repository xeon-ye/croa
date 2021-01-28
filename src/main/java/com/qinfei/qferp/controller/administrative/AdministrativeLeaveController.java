package com.qinfei.qferp.controller.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.administrative.AdministrativeLeave;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Controller
@RequestMapping("/administrativeLeave")
class AdministrativeLeaveController {

    @Autowired
    private IAdministrativeLeaveService leaveService;


    @PostMapping("validationAddLeave")
    @ApiOperation(value = "判断是否可新增请假", notes = "判断是否可新增请假")
    @ResponseBody
    public ResponseData validationAddLeave(@RequestParam("endDate")Date endDate, @RequestParam("num") Integer num,
                                           @RequestParam(value = "id", required = false)Integer id){
        return leaveService.validationAddLeave(endDate, num, id);
    }

    /**
     * 添加请假申请
     * @param leave
     * @return
     */
    @RequestMapping("saveLeave")
    @ApiOperation(value = "提交请假申请", notes = "保存请假信息")
//    @Log(opType = OperateType.ADD, module = "请假管理", note = "保存请假信息")
    @ResponseBody
    public ResponseData saveLeave(AdministrativeLeave leave,@RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] files) {
        ResponseData data = null;
        data = leaveService.addLeave(leave,files,pics);
        return data;
    }

    /**
     * 请假申请审核
     * @param leave
     * @param multipartFiles
     * @return
     */
    @RequestMapping("editLeave")
    @ApiOperation(value = "开启请假申请审核", notes = "开启请假审核")
//    @Log(opType = OperateType.UPDATE, module = "请假管理", note = "开启请假审核")
    @ResponseBody
    public ResponseData editLeave(AdministrativeLeave leave, @RequestParam(value = "pic", required = false) MultipartFile[] pics,
                                  @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles,
                                  @RequestParam(name = "nextUser", required = false) Integer nextUser, @RequestParam(name = "nextUserName", required = false) String nextUserName,
                                  @RequestParam(name = "nextUserDept", required = false) Integer nextUserDept) {
        return leaveService.edit(leave,multipartFiles,pics, nextUser, nextUserName, nextUserDept);
    }

    /**
     * 请假（保存后）申请审核
     * @param leave
     * @param pics
     * @param multipartFiles
     * @return
     */
    @RequestMapping("editUpdateLeave")
    @ApiOperation(value = "开启请假（保存后）申请审核", notes = "开启请假（保存后）审核")
//    @Log(opType = OperateType.UPDATE, module = "请假（保存后）管理", note = "开启请假（保存后）审核")
    @ResponseBody
    public ResponseData editUpdateLeave(AdministrativeLeave leave, @RequestParam(value = "pic", required = false) MultipartFile[] pics,
                                        @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles,
                                        @RequestParam(name = "nextUser", required = false) Integer nextUser, @RequestParam(name = "nextUserName", required = false) String nextUserName,
                                        @RequestParam(name = "nextUserDept", required = false) Integer nextUserDept){
        return leaveService.editUpdateLeave(leave,multipartFiles,pics, nextUser, nextUserName, nextUserDept);
    }

    /**
     * 删除请假信息
     * @param leaveId
     * @return
     */
    @RequestMapping("deleteLeave")
    @ApiOperation(value = "删除请假申请", notes = "删除请假信息")
//    @Log(opType = OperateType.DELETE, module = "请假管理", note = "删除请假信息")
    @ResponseBody
    public ResponseData deleteLeave(int leaveId){
        ResponseData data = ResponseData.ok();
        //根据请假id删除请假信息
        leaveService.deleteLeave(leaveId);
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", leaveId) ;
        return data;
    }

    /**
     * 通过流程id获取请假的详细信息
     * @param id
     * @return
     */
    @RequestMapping("getLeaByAdmId")
    @ApiOperation(value = "请假查询", notes = "通过流程id查询请假信息")
//    @Log(opType = OperateType.QUERY, module = "请假管理", note = "通过流程id查询请假信息")
    @ResponseBody
    public ResponseData getLeaByAdmId(Integer id){
        return leaveService.getLeaveByAdministrativeId(id);
    }

    /**
     * 修改请假信息
     * @param leave
     * @param files
     * @return
     */
    @RequestMapping("updateLeaById")
    @ApiOperation(value = "请假管理", notes = "修改请假信息")
//    @Log(opType = OperateType.UPDATE, module = "请假管理", note = "修改请假信息")
    @ResponseBody
    public ResponseData updateLeaById(AdministrativeLeave leave, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] files){
        return leaveService.updateLeaveByAdmId(leave,files,pics);
    }

    //删除请假信息
    @RequestMapping("deleteLeaByAdmId")
    @ApiOperation(value = "请假管理", notes = "删除请假信息")
//    @Log(opType = OperateType.UPDATE, module = "请假管理", note = "删除请假信息")
    @ResponseBody
    public ResponseData deleteLeaByAdmId(Integer id){
        return leaveService.deleteLeaveByAdmId(id);
    }
}
