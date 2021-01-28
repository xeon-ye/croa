package com.qinfei.qferp.controller.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.administrative.AdministrativeLeaveType;
import com.qinfei.qferp.service.impl.administrative.AdministrativeLeaveTypeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/administrativeLeaveType")
class AdministrativeLeaveTypeController {
    @Autowired
    private AdministrativeLeaveTypeService leaveType;

    /**
     * 获取所有的请假类型
     * @return
     */
    @RequestMapping("getLeaveType")
//    @Log(opType = OperateType.QUERY, module = "行政管理/获取所有的请假类型", note = "行政管理/获取所有的请假类型")
    @ResponseBody
    public ResponseData getLeaveType(){
        ResponseData data = ResponseData.ok();
        data.putDataValue("leaveType",leaveType.getType());
        return data;
    }
}
