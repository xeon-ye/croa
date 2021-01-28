package com.qinfei.qferp.controller.statistics;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.service.statistics.IStatisticsOverviewService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 *统计概况
 */
@Controller
@RequestMapping("/statisticsOverview")
class StatisticsOverviewController {
    @Autowired
    private IStatisticsOverviewService statisticsOverviewService;

    @PostMapping("getStatisticsById")
    @Log(opType = OperateType.QUERY, note = "不同维度的统计总数", module = "统计概况")
    @ResponseBody
    public ResponseData getStatisticsById(@RequestParam Map map){
        return ResponseData.ok().putDataValue("result",statisticsOverviewService.getStatisticsById(map));
    }

    @PostMapping("listMediaTypeStatisticsById")
    @Log(opType = OperateType.QUERY, note = "不同维度板块占比", module = "统计概况")
    @ResponseBody
    public ResponseData listMediaTypeStatisticsById(@RequestParam Map map){
        return ResponseData.ok().putDataValue("result",statisticsOverviewService.listMediaTypeStatisticsById(map));
    }

    @PostMapping("listTrendStatisticsById")
    @Log(opType = OperateType.QUERY, note = "不同维度趋势图，按时间排序", module = "统计概况")
    @ResponseBody
    public ResponseData listTrendStatisticsById(@RequestParam Map map){
        return ResponseData.ok().putDataValue("list",statisticsOverviewService.listTrendStatisticsById(map));
    }


    @PostMapping("listMediaStatisticsByCustId")
    @Log(opType = OperateType.QUERY, note = "分页查询客户涉及媒体列表", module = "统计概况/客户维度")
    @ResponseBody
    public PageInfo<Map> listMediaStatisticsByCustId(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return statisticsOverviewService.listMediaStatisticsByCustId(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listCustStatisticsBybusinessId")
    @Log(opType = OperateType.QUERY, note = "分页查询业务员涉及的客户列表", module = "统计概况/业务员维度")
    @ResponseBody
    public PageInfo<Map> listCustStatisticsBybusinessId(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return statisticsOverviewService.listCustStatisticsBybusinessId(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listMediaStatisticsByMediaUserId")
    @Log(opType = OperateType.QUERY, note = "分页查询媒介涉及媒体列表", module = "统计概况/媒介维度")
    @ResponseBody
    public PageInfo<Map> listMediaStatisticsByMediaUserId(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return statisticsOverviewService.listMediaStatisticsByMediaUserId(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listMediaStatisticsByMediaTypeId")
    @Log(opType = OperateType.QUERY, note = "分页查询板块涉及媒体列表", module = "统计概况/板块维度")
    @ResponseBody
    public PageInfo<Map> listMediaStatisticsByMediaTypeId(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return statisticsOverviewService.listMediaStatisticsByMediaTypeId(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listCustStatisticsByMediaId")
    @Log(opType = OperateType.QUERY, note = "分页查询媒体涉及的客户列表", module = "统计概况/媒体维度")
    @ResponseBody
    public PageInfo<Map> listCustStatisticsByMediaId(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return statisticsOverviewService.listCustStatisticsByMediaId(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listMediaStatisticsBySupplierId")
    @Log(opType = OperateType.QUERY, note = "分页查询供应商涉及媒体列表", module = "统计概况/板块维度")
    @ResponseBody
    public PageInfo<Map> listMediaStatisticsBySupplierId(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return statisticsOverviewService.listMediaStatisticsBySupplierId(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @GetMapping("statisticsOverviewRankingExport")
    @Log(opType = OperateType.QUERY, note = "列表导出", module = "统计概况/导出列表")
    public void statisticsOverviewRankingExport(HttpServletResponse response, @RequestParam Map map) throws Exception {
        statisticsOverviewService.statisticsOverviewRankingExport(response,map);
    }


    @GetMapping("statisticsOverviewPermission")
    @Verify(code = "/statisticsOverview/statisticsOverviewPermission", module = "统计概况权限控制", action = "4")
    @ResponseBody
    public ResponseData statisticsOverviewPermission() {
        return ResponseData.ok();
    }
}
