package com.qinfei.qferp.controller.biz.statistics;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.service.biz.statistics.IBusinessStatisticsService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/businessStatistics")
class BusinessStatisticsController {
    @Autowired
    IBusinessStatisticsService businessStatisticsService;

    /**
     * 业务统计结果-旧的
     * @param map
     * @return
     */
/*    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "业务统计/业务统计结果", note = "业务统计/业务统计结果")
    @RequestMapping("/statisticsResult")
    public List<Map<String,Object>> statisticsResult(@RequestParam Map map){
//        DataSecurityUtil.addSecurity(map);
        return businessStatisticsService.statisticsResult(map);
    }*/

    /**
     * 业务统计结果
     * @param map
     * @return
     */
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "业务统计/业务统计结果", note = "业务统计/业务统计结果")
    @RequestMapping("/businessStatisticsResult")
    public ResponseData businessStatisticsResult(@RequestParam Map map){
        return ResponseData.ok().putDataValue("result",businessStatisticsService.businessStatisticsResult(map));
    }

    /**
     * 业务统计结果
     * @param map
     * @return
     */
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "业务统计/业务统计结果", note = "业务统计/业务统计结果")
    @RequestMapping("/zwBusinessStatisticsResult")
    public ResponseData zwBusinessStatisticsResult(@RequestParam Map map){
        return ResponseData.ok().putDataValue("result",businessStatisticsService.zwBusinessStatisticsResult(map));
    }

    /**
     * 获取最近三个月未成交的客户
     * @param pageable
     * @param map
     * @return
     */
    @RequestMapping("/listCustForNotTrans")
    @ResponseBody
    public PageInfo<Map> listCustForNotTrans(@PageableDefault() Pageable pageable, @RequestParam Map map) {
        return businessStatisticsService.listCustForNotTrans(pageable.getPageNumber(), pageable.getPageSize(), map);
    }

    /**
     * 最近三个月未成交客户
     * @param map
     * @param pageable
     * @return
     */
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "业务统计/最近三个月未成交客户", note = "业务统计/最近三个月未成交客户")
    @RequestMapping("/statisticsRanking")
    public PageInfo<Map<String,Object>> statisticsRanking(@RequestParam Map map, @PageableDefault() Pageable pageable){
//        DataSecurityUtil.addSecurity(map);
        return businessStatisticsService.statisticsRanking(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    /**
     * 查询各个部门的
     */
    @RequestMapping("/everyDeptBusiness")
    @Log(opType = OperateType.QUERY, module = "业务统计/统计各个部门的业务", note = "业务统计/统计各个部门的业务")
    @ResponseBody
    public ResponseData everyDeptBusiness(@RequestParam Map<String, Object> map){
        if(StringUtils.isEmpty(map.get("list")) || String.valueOf(map.get("list")).split(",").length < 1){
            return ResponseData.ok().putDataValue("list",new ArrayList<>());
        }
        String[] strs = String.valueOf(map.get("list")).split(",");
        List<Integer> depts = new ArrayList<>();
        try{
            for(int i = 0; i<strs.length; i++){
                if(!StringUtils.isEmpty(strs[i])){
                    depts.add(Integer.parseInt(strs[i]));
                }
            }
            map.remove("list");
        }catch (Exception e){
            return ResponseData.customerError(1001,"部门ID转换异常");
        }
        List<Map<String, Object>> result = businessStatisticsService.everyDeptBusiness(depts, map);
        return ResponseData.ok().putDataValue("list",result);
    }

    /**
     * 查询各个部门的
     */
    @RequestMapping("/businessTop")
    @Log(opType = OperateType.QUERY, module = "业务统计/统计业务前5名", note = "业务统计/统计业务前5名")
    @ResponseBody
    public List<Map<String, Object>> businessTop(@RequestParam Map<String, Object> map){
       return businessStatisticsService.businessTop(map);
    }

    /**
     * 查询未到款
     */
    @RequestMapping("/queryNotIncome")
    @Log(opType = OperateType.QUERY, module = "业务统计/未到款统计", note = "业务统计/未到款统计")
    @ResponseBody
    public PageInfo<Map<String, Object>> queryNotIncome(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map){
        return businessStatisticsService.queryNotIncome(pageable.getPageNumber(), pageable.getPageSize(),map);
    }

    @RequestMapping("/exportNotIncome")
    @Log(opType = OperateType.QUERY, module = "业务统计/未到款导出", note = "业务统计/未到款导出")
    @ResponseBody
    public void exportNotIncome(HttpServletResponse response, @RequestParam Map map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("未到款导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            businessStatisticsService.exportNotIncome(map, outputStream);
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

    @RequestMapping("/querySumNotIncome")
    @Log(opType = OperateType.QUERY, module = "业务统计/未到款统计合计", note = "业务统计/未到款统计合计")
    @ResponseBody
    public Map querySumNotIncome(@RequestParam Map map){
        return businessStatisticsService.querySumNotIncome(map);
    }

    /**
     * 查询未到款
     */
    @RequestMapping("/queryNotIncomeYear")
    @Log(opType = OperateType.QUERY, module = "业务统计/未到款统计详情", note = "业务统计/未到款统计详情")
    @ResponseBody
    public PageInfo<Map<String, Object>> queryNotIncomeYear(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map){
        return businessStatisticsService.queryNotIncomeYear(pageable.getPageNumber(), pageable.getPageSize(),map);
    }

    @RequestMapping("/exportNotIncomeDetail")
    @Log(opType = OperateType.QUERY, module = "业务统计/未到款导出", note = "业务统计/未到款导出")
    @ResponseBody
    public void exportNotIncomeDetail(HttpServletResponse response, @RequestParam Map map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("未到款详情导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            businessStatisticsService.exportNotIncomeDetail(map, outputStream);
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

    /**
     * 查询未到款
     */
    @RequestMapping("/querySaleStat")
    @Log(opType = OperateType.QUERY, module = "业务统计/业绩统计", note = "业务统计/业绩统计")
    @ResponseBody
    public PageInfo<Map<String, Object>> querySaleStat(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map){
        return businessStatisticsService.querySaleStat(pageable.getPageNumber(), pageable.getPageSize(),map);
    }

    /**
     * 查询未到款
     */
    @RequestMapping("/querySaleStatSum")
    @Log(opType = OperateType.QUERY, module = "业务统计/业绩统计", note = "业务统计/业绩统计")
    @ResponseBody
    public Map querySaleStatSum(@RequestParam Map map){
        return businessStatisticsService.querySaleStatSum(map);
    }

    @RequestMapping("/exportSaleStat")
    @Log(opType = OperateType.QUERY, module = "业务统计/业绩导出", note = "业务统计/业绩导出")
    @ResponseBody
    public void exportSaleStat(HttpServletResponse response, @RequestParam Map map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("业绩统计" + DateUtils.getNowTime() + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            businessStatisticsService.exportSaleStat(map, outputStream);
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

}
