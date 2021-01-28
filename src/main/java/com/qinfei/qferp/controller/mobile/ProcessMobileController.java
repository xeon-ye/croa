package com.qinfei.qferp.controller.mobile;

import com.qinfei.qferp.service.mobile.IProcessMobileService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import com.github.pagehelper.PageInfo;

import java.util.Map;


/**
 * @CalssName: ProcessMobileController
 * @Description: 移动端流程接口
 * @Author: Xuxiong
 * @Date: 2020/6/8 0008 16:36
 * @Version: 1.0
 */
@RestController
@RequestMapping("mobile")
public class ProcessMobileController {
    @Autowired
    private IProcessMobileService processMobileService;

    /**
     * 获取行政流程列(审核列表)
     * @param map(type 行政流程类型)
     * @param pageable
     * @return
     */
    @RequestMapping("/administrativeProcess")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询审核的行政流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> administrative(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativeList(map,pageable);
    }

    /**
     * 获取行政已审核列表（已审核列表）
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/administrativeProcessAlready")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的行政流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> administrativeProcessAlready(@RequestParam Map<String,Object> map,@PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativeProcessAlready(map,pageable);
    }

    /**
     * 获取行政会议审核列表（审核列表）
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/administrativeMeeting")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询审核的行政会议流程")
    @ResponseBody
    public PageInfo<Map<String,Object>>administrativeMeeting(@RequestParam Map<String,Object> map,@PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativeMeeting(map,pageable);
    }

    /**
     * 获取行政会议已审核列表（）
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/administrativeMeetingAlready")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的行政会议流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> administrativeMeetingAlready(@RequestParam Map<String,Object> map,@PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativeMeetingAlready(map,pageable);
    }


    /**
     * 获取行政采购审核列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/administrativeSaveBuy")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询审核的行政采购流程")
    @ResponseBody
    public PageInfo<Map<String,Object>>administrativeSaveBuy(@RequestParam Map<String,Object> map,@PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativeSaveBuy(map,pageable);
    }


    /**
     * 获取行政采购已审核列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/administrativeSaveBuyAlready")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的行政采购流程")
    @ResponseBody
    public PageInfo<Map<String,Object>>  administrativeSaveBuyAlready(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativeSaveBuyAlready(map,pageable);
    }


    /**
     * 获取行政领用审核列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/administrativeApply")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询审核的行政领用流程")
    @ResponseBody
    public  PageInfo<Map<String,Object>> administrativeApply(@RequestParam Map<String,Object> map,@PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativeApply(map,pageable);
    }

    /**
     * 获取行政领用已审核列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/administrativeApplyAlready")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的行政领用流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> administrativeApplyAlready(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativeApplyAlready(map,pageable);
    }

    /**
     * 获取业务退款审核列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/businessRefundList")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询审核的业务退款流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> businessRefundList(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return processMobileService.businessRefundList(map,pageable);
    }

    /**
     * 获取业务退款已审核列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/businessRefundListAlready")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询审核的业务退款流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> businessRefundListAlready(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return processMobileService.businessRefundListAlready(map,pageable);
    }

    /**
     * 获取业务提成审核列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/businessCommissionList")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询审核的业务提成流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> businessCommissionList(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return processMobileService.businessCommissionList(map,pageable);
    }

    /**
     * 获取业务提成已审核列表
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/businessCommissionListAlready")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的业务提成流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> businessCommissionListAlready(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return processMobileService.businessCommissionListAlready(map,pageable);
    }


    @RequestMapping("/administrativePerformance")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询审核的绩效流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> administrativePerformance(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
       return processMobileService.administrativePerformance(map,pageable);
    }

    @RequestMapping("/administrativePerformanceAlready")
    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的绩效流程")
    @ResponseBody
    public PageInfo<Map<String,Object>> administrativePerformanceAlready(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return processMobileService.administrativePerformanceAlready(map,pageable);
    }


    @ApiOperation(value = "移动端流程接口", notes = "分页查询员工录用未审核流程")
    @PostMapping("/listHireNotAudit")
    public PageInfo<Map<String, Object>> listHireNotAudit(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listHireNotAudit(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询员工录用已审核流程")
    @PostMapping("/listHireHasAudit")
    public PageInfo<Map<String, Object>> listHireHasAudit(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listHireHasAudit(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询转正、离职、交接、调岗未审核流程")
    @PostMapping("/listEmployeeNotAuditByParam")
    public PageInfo<Map<String, Object>> listEmployeeNotAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listEmployeeNotAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询转正、离职、交接、调岗已审核流程")
    @PostMapping("/listEmployeeHasAuditByParam")
    public PageInfo<Map<String, Object>> listEmployeeHasAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listEmployeeHasAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询未审核的请款流程")
    @PostMapping("/listOutgoNotAuditByParam")
    public PageInfo<Map<String, Object>> listOutgoNotAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listOutgoNotAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的请款流程")
    @PostMapping("/listOutgoHasAuditByParam")
    public PageInfo<Map<String, Object>> listOutgoHasAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listOutgoHasAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询未审核的请款唤醒流程")
    @PostMapping("/listOutgoWorkUpNotAuditByParam")
    public PageInfo<Map<String, Object>> listOutgoWorkUpNotAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listOutgoWorkUpNotAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的请款唤醒流程")
    @PostMapping("/listOutgoWorkUpHasAuditByParam")
    public PageInfo<Map<String, Object>> listOutgoWorkUpHasAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listOutgoWorkUpHasAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询未审核的删稿流程")
    @PostMapping("/listDropNotAuditByParam")
    public PageInfo<Map<String, Object>> listDropNotAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listDropNotAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询未审核的删稿流程")
    @PostMapping("/listDropHasAuditByParam")
    public PageInfo<Map<String, Object>> listDropHasAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listDropHasAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询未审核的借款流程")
    @PostMapping("/listBorrowNotAuditByParam")
    public PageInfo<Map<String, Object>> listBorrowNotAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listBorrowNotAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的借款流程")
    @PostMapping("/listBorrowHasAuditByParam")
    public PageInfo<Map<String, Object>> listBorrowHasAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listBorrowHasAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询未审核的报销流程")
    @PostMapping("/listReimursementNotAuditByParam")
    public PageInfo<Map<String, Object>> listReimursementNotAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listReimursementNotAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的报销流程")
    @PostMapping("/listReimursementHasAuditByParam")
    public PageInfo<Map<String, Object>> listReimursementHasAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listReimursementHasAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询未审核的开票流程")
    @PostMapping("/listInvoiceNotAuditByParam")
    public PageInfo<Map<String, Object>> listInvoiceNotAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listInvoiceNotAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的开票流程")
    @PostMapping("/listInvoiceHasAuditByParam")
    public PageInfo<Map<String, Object>> listInvoiceHasAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listInvoiceHasAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询未审核的退款流程")
    @PostMapping("/listRefundNotAuditByParam")
    public PageInfo<Map<String, Object>> listRefundNotAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listRefundNotAuditByParam(param, pageable);
    }

    @ApiOperation(value = "移动端流程接口", notes = "分页查询已审核的退款流程")
    @PostMapping("/listRefundHasAuditByParam")
    public PageInfo<Map<String, Object>> listRefundHasAuditByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10, page = 1) Pageable pageable){
        return processMobileService.listRefundHasAuditByParam(param, pageable);
    }
}
