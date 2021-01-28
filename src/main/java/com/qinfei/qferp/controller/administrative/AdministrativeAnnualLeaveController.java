package com.qinfei.qferp.controller.administrative;


import com.qinfei.core.ResponseData;
import com.qinfei.qferp.service.impl.administrative.AdministrativeAnnualLeaveService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/annualLeave")
class AdministrativeAnnualLeaveController {
    private final AdministrativeAnnualLeaveService annualLeave;

    @Autowired
    public AdministrativeAnnualLeaveController(AdministrativeAnnualLeaveService annualLeave) {
        this.annualLeave = annualLeave;
    }

    /**
     * 根据类型id获取员工的假期信息
     * @param typeId
     * @return
     */
    @RequestMapping("getAnnualLeaveByTypeId")
    @ApiOperation(value = "获取员工的假期信息", notes = "通过类型id获取员工的假期信息")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "获取员工的假期信息")
    @ResponseBody
    public ResponseData getAnnualLeaveByTypeId(Integer typeId){
        ResponseData data = null;
        data = annualLeave.getAnnualLeaveByTypeId(typeId);
        return data;
    }

    //修改假期的时间
    public ResponseData updateAnnualLeave(){
        return null;
    }

    /**
     * 年假初始化接口
     * @return
     */
    @RequestMapping("initialize")
    @ApiOperation(value = "年假初始化接口", notes = "年假初始化接口")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "年假初始化接口")
    @ResponseBody
    public ResponseData initialize(){
        ResponseData data = ResponseData.ok();
        //根据请假id删除请假信息
        annualLeave.initialize();
        return data;
    }

    //

}
