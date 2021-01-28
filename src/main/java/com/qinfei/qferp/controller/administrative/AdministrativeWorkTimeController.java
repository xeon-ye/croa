package com.qinfei.qferp.controller.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.administrative.AdministrativeLeave;
import com.qinfei.qferp.entity.administrative.AdministrativeOverTimeWork;
import com.qinfei.qferp.service.administrative.IAdministrativeOverTimeWorkService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/workTime")
class AdministrativeWorkTimeController {

    @Autowired
    private IAdministrativeOverTimeWorkService TimeWork;
    @Autowired
    private Config config;

    /**
     * 添加加班申请
     * @param overTime
     * @return
     */
    @RequestMapping("saveWorkTime")
    @ApiOperation(value = "提交加班申请", notes = "保存加班信息")
//    @Log(opType = OperateType.ADD, module = "加班管理", note = "保存加班信息")
    @ResponseBody
    public ResponseData saveWorkTime(AdministrativeOverTimeWork overTime, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] files) {
        ResponseData data = null;
        data = TimeWork.addTimework(overTime,files,pics);
        return data;
    }

    /**
     * 加班申请审核
     * @param timeWork
     * @param pics
     * @param files
     * @return
     */
    @RequestMapping("editWorkTime")
    @ApiOperation(value = "开启加班申请审核", notes = "开启加班审核")
//    @Log(opType = OperateType.UPDATE, module = "加班管理", note = "开启加班审核")
    @ResponseBody
    public ResponseData editWorkTime(AdministrativeOverTimeWork timeWork, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] files) {
        return TimeWork.edit(timeWork,files,pics);
    }

    /**
     * 加班（保存后）申请审核
     * @param timeWork
     * @param pics
     * @param multipartFiles
     * @return
     */
    @RequestMapping("editUpdateTimeWork")
    @ApiOperation(value = "开启加班（保存后）申请审核", notes = "开启加班（保存后）审核")
//    @Log(opType = OperateType.UPDATE, module = "加班（保存后）管理", note = "开启加班（保存后）审核")
    @ResponseBody
    public ResponseData editUpdateTimeWork(AdministrativeOverTimeWork timeWork, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles){
        return TimeWork.editUpdateTimeWork(timeWork,multipartFiles,pics);
    }


    /**
     * 删除加班信息
     * @param leaveId
     * @return
     */
    @RequestMapping("deleteTimework")
    @ApiOperation(value = "删除加班申请", notes = "删除加班信息")
//    @Log(opType = OperateType.DELETE, module = "加班管理", note = "删除加班信息")
    @ResponseBody
    public ResponseData deleteTimework(int leaveId){
        ResponseData data = ResponseData.ok();
        //根据加班id删除加班信息
        TimeWork.deleteTimework(leaveId);
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", leaveId) ;
        return data;
    }

    /**
     * 通过流程id获取加班的详细信息
     * @param id
     * @return
     */
    @RequestMapping("getTimeworkByAdmId")
    @ApiOperation(value = "加班查询", notes = "通过流程id查询加班信息")
//    @Log(opType = OperateType.QUERY, module = "加班管理", note = "通过流程id查询加班信息")
    @ResponseBody
    public ResponseData getTimeworkByAdmId(Integer id){
        return TimeWork.getTimeworkByAdministrativeId(id);
    }

    /**
     * 修改加班信息
     * @param leave
     * @param pics
     * @param files
     * @return
     */
    @RequestMapping("updateTimeworkByAdmId")
    @ApiOperation(value = "加班管理", notes = "修改加班信息")
//    @Log(opType = OperateType.UPDATE, module = "加班管理", note = "修改加班信息")
    @ResponseBody
    public ResponseData updateTimeworkByAdmId(AdministrativeOverTimeWork leave, @RequestParam(value = "pic", required = false) MultipartFile[] pics, @RequestParam(value = "file", required = false) MultipartFile[] files){
        return TimeWork.updateTimeworkByAdmId(leave,files,pics);
    }

    //删除加班信息
    @RequestMapping("deleteTimeworkByAdmId")
    @ApiOperation(value = "加班管理", notes = "删除加班信息")
//    @Log(opType = OperateType.UPDATE, module = "加班管理", note = "删除加班信息")
    @ResponseBody
    public ResponseData deleteTimeworkByAdmId(Integer id){
        return TimeWork.deleteTimeworkByAdmId(id);
    }
}
