package com.qinfei.qferp.controller.administrative;


import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.administrative.AdministrativeOnbusinessReport;
import com.qinfei.qferp.service.impl.administrative.AdministrativeOnbusinessReportService;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/onbusinessReport")
class AdministrativeOnbusinessReportController {
    @Autowired
    private AdministrativeOnbusinessReportService reportService;
    /**
     * 新增出差总结报告
     * @param report
     * @return
     */
    @RequestMapping("addReport")
    @ApiOperation(value = "出差报告新增", notes = "出差报告新增\n" +"return data ;")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "出差报告新增")
    @ResponseBody
    public ResponseData addReport(AdministrativeOnbusinessReport report){
        return reportService.addReport(report);
    }

    /**
     * 出差总结报告
     * @param report
     * @return
     */
    @RequestMapping("updateReport")
    @ApiOperation(value = "更新出差总结报告",notes = "更新出差总结报告\n" +"return data ;")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "更新出差总结报告")
    @ResponseBody
    public ResponseData updateReport(AdministrativeOnbusinessReport report){
        return reportService.updateReport(report);
    }

    /**
     * 查看报告
     * @param admId
     * @return
     */
    @RequestMapping("getReport")
    @ApiOperation(value = "获取出差报告",notes = "获取出差报告\n" +"return data ;")
//    @Log(opType = OperateType.QUERY, module = "行政管理", note = "获取出差报告")
    @ResponseBody
    public ResponseData getReport(@RequestParam("id") Integer admId){
        return reportService.getReport(admId);
    }

}
