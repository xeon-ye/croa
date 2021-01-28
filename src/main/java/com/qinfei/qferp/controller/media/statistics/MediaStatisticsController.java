package com.qinfei.qferp.controller.media.statistics;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.service.biz.IArticleService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/mediaStatistics")
@Api(description = "媒体统计接口")
class MediaStatisticsController {
    @Autowired
    private IArticleService articleService;

    @PostMapping("getMediaTypeStatistics")
    @Log(opType = OperateType.QUERY, note = "获取所有板块统计信息", module = "统计信息/板块统计")
    @ResponseBody
    public ResponseData getMediaTypeStatistics(@RequestParam Map map){
        Map result = articleService.getMediaTypeStatistics(map);
        return ResponseData.ok().putDataValue("result",result);
    }

    @PostMapping("listStatisticsByMediaType")
    @Log(opType = OperateType.QUERY, note = "板块列表排名", module = "统计信息/板块统计")
    @ResponseBody
    public PageInfo<Map> listStatisticsByMediaType(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map){
        return articleService.listStatisticsByMediaType(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listStatisticsByDate")
    @Log(opType = OperateType.QUERY, note = "板块趋势图", module = "统计信息/板块统计")
    @ResponseBody
    public ResponseData listStatisticsByDate(@RequestParam Map map){
        return ResponseData.ok().putDataValue("list",articleService.listStatisticsByDate(map));
    }

    @PostMapping("listStatisticsByMedia")
    @Log(opType = OperateType.QUERY, note = "媒体列表排名", module = "统计信息/媒体统计")
    @ResponseBody
    public PageInfo<Map> listStatisticsByMedia(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return articleService.listStatisticsByMedia(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listStatisticsByBusiness")
    @Log(opType = OperateType.QUERY, note = "业务员列表排名", module = "统计信息/媒体统计、板块统计")
    @ResponseBody
    public PageInfo<Map> listStatisticsByBusiness(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return articleService.listStatisticsByBusiness(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listStatisticsByMediaUser")
    @Log(opType = OperateType.QUERY, note = "媒介列表排名", module = "统计信息/媒体统计、板块统计")
    @ResponseBody
    public PageInfo<Map> listStatisticsByMediaUser(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return articleService.listStatisticsByMediaUser(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listStatisticsBySupplier")
    @Log(opType = OperateType.QUERY, note = "供应商列表排名", module = "统计信息/媒体统计、板块统计")
    @ResponseBody
    public PageInfo<Map> listStatisticsBySupplier(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return articleService.listStatisticsBySupplier(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @PostMapping("listStatisticsByCust")
    @Log(opType = OperateType.QUERY, note = "客户列表排名", module = "统计信息/媒体统计、板块统计")
    @ResponseBody
    public PageInfo<Map> listStatisticsByCust(@PageableDefault() Pageable pageable, @RequestParam Map map){
        return articleService.listStatisticsByCust(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @GetMapping("mediaTypeStatisticsRankingExport")
    @Log(opType = OperateType.QUERY, note = "列表导出", module = "统计信息/板块统计")
    public void mediaTypeStatisticsRankingExport(HttpServletResponse response, @RequestParam Map map) throws Exception {
        articleService.statisticsRankingExport(response,map);
    }

    @GetMapping("mediaStatisticsRankingExport")
    @Log(opType = OperateType.QUERY, note = "列表导出", module = "统计信息/媒体统计")
    public void mediaStatisticsRankingExport(HttpServletResponse response, @RequestParam Map map) throws Exception {
        articleService.mediaStatisticsRankingExport(response,map);
    }

    @PostMapping("getMediaStatistics")
    @Log(opType = OperateType.QUERY, note = "获取所有媒体统计信息", module = "统计信息/媒体统计")
    @ResponseBody
    public ResponseData getMediaStatistics(@RequestParam Map map){
        Map result = articleService.getMediaStatistics(map);
        return ResponseData.ok().putDataValue("result",result);
    }

    @PostMapping("listMediaByType")
    @Log(opType = OperateType.QUERY, note = "根据板块ID获取媒体列表", module = "统计信息/媒体统计")
    @ResponseBody
    public ResponseData listMediaByType(@RequestParam Map map){
        return ResponseData.ok().putDataValue("list",articleService.listMediaByType(map));
    }
}
