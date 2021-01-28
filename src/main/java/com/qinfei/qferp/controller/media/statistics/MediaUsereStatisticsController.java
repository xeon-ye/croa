package com.qinfei.qferp.controller.media.statistics;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.service.mediauser.statistics.IMediaUserStatisticsService;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mediaUsereStatistics")
class MediaUsereStatisticsController {
    @Autowired
    IMediaUserStatisticsService mediaUsereStatisticsService;

    @RequestMapping("/listSupplierByMediaUser")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "根据媒介或媒介部门查询供应商列表", module = "统计信息/媒介统计")
    public ResponseData listSupplierByMediaUser(@RequestParam Map map) {
        return ResponseData.ok().putDataValue("list",mediaUsereStatisticsService.listSupplierByMediaUser(map));
    }

    @RequestMapping("/mediaUserResult")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "媒体统计合计", module = "统计信息/媒介统计")
    public ResponseData mediaUserResult(@RequestParam Map map) {
//        DataSecurityUtil.addSecurity(map);
        return ResponseData.ok().putDataValue("result",mediaUsereStatisticsService.mediaUserResult(map));
    }

    @RequestMapping("/zwMediaUserResult")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "媒体统计合计", module = "统计信息/媒介统计")
    public ResponseData zwMediaUserResult(@RequestParam Map map) {
//        DataSecurityUtil.addSecurity(map);
        return ResponseData.ok().putDataValue("result",mediaUsereStatisticsService.zwMediaUserResult(map));
    }

    @RequestMapping("/listMediaUserMediaStatisticsByParam")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "媒体列表", module = "统计信息/媒介统计")
    public PageInfo<Map> listMediaUserMediaStatisticsByParam(@PageableDefault() Pageable pageable, @RequestParam Map map) {
        return mediaUsereStatisticsService.listMediaUserMediaStatisticsByParam(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @GetMapping("mediaStatisticsRankingExport")
    @Log(opType = OperateType.QUERY, note = "媒体列表导出", module = "统计信息/媒介统计")
    public void mediaStatisticsRankingExport(HttpServletResponse response, @RequestParam Map map) throws Exception {
        mediaUsereStatisticsService.mediaStatisticsRankingExport(response,map);
    }

    @RequestMapping("/supplierResult")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "供应商统计合计·", module = "媒体管理/统计")
    public List<Map> supplierResult(@RequestParam Map map) {
//        DataSecurityUtil.addSecurity(map);
        return mediaUsereStatisticsService.supplierResult(map);
    }

}
