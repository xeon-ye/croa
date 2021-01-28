package com.qinfei.qferp.controller.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.administrative.AdministrativeVacationTime;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.administrative.IAdministrativeVacationTimeService;
import com.qinfei.qferp.utils.AppUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/administrativeVacationTime")
class AdministrativeVacationTimeController {
    @Autowired
    private IAdministrativeVacationTimeService vacationTimeService;

    /**
     * 根据员工ID获取剩余调休时间
     *
     * @return
     */
    @RequestMapping("getVacationTime")
    @ApiOperation(value = "查询剩余调休时间", notes = "根据员工ID查询调休时间信息")
//    @Log(opType = OperateType.QUERY, module = "请假管理", note = "根据员工ID查询调休时间信息")
    @ResponseBody
    public ResponseData getVacationTime(){
        //获取员工id
        User emp = AppUtil.getUser();
        ResponseData data = ResponseData.ok();
        data.putDataValue("vacaTime", vacationTimeService.getVacationTime(emp.getId()));
        return data;
    }

    //减少调休时间
    @RequestMapping("decreaseVacationTime")
    @ApiOperation(value = "减少剩余调休时间", notes = "减少请假员工的调休时间")
//    @Log(opType = OperateType.QUERY, module = "请假管理", note = "减少请假员工的调休时间")
    @ResponseBody
    public ResponseData decreaseVacationTime(AdministrativeVacationTime vacationTime){
        ResponseData data = ResponseData.ok();
        //获取员工id
        User emp = AppUtil.getUser();
        vacationTime.setEmpId(emp.getId());
        vacationTime.setState(-1);
        data.putDataValue("vacaTime", vacationTimeService.decreaseVacationTime(vacationTime));
        return data;
    }
    //增加调休时间
    @RequestMapping("addVacationTime")
    @ApiOperation(value = "增加剩余调休时间", notes = "增加请假员工的调休时间")
//    @Log(opType = OperateType.QUERY, module = "请假管理", note = "增加请假员工的调休时间")
    @ResponseBody
    public ResponseData addVacationTime(AdministrativeVacationTime vacationTime){
        ResponseData data = ResponseData.ok();
        //获取员工id
        User emp = AppUtil.getUser();
        vacationTime.setEmpId(emp.getId());
        vacationTime.setState(1);
        data.putDataValue("vacaTime", vacationTimeService.addVacationTime(vacationTime));
        return data;
    }
}
