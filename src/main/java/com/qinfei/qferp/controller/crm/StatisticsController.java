package com.qinfei.qferp.controller.crm;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.crm.IStatisticsService;
import com.qinfei.qferp.service.impl.crm.StatisticsService;
import com.qinfei.qferp.utils.AppUtil;

import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Controller
@RequestMapping("/statistics")
class StatisticsController {
    @Autowired
    IStatisticsService statisticsService;

    /**
     * 客户统计列表
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/statisticsResult")
    @Log(opType = OperateType.QUERY, note = "客户统计列表", module = "客户统计列表")
    public List<Map<String,Object>> statisticsResult(@RequestParam Map map){
//        DataSecurityUtil.addSecurity(map);
        return statisticsService.statisticsResult(map);
    }

    /**
     * 客户统计总计、趋势图、板块占比
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/custStatisticsResult")
    @Log(opType = OperateType.QUERY, note = "客户统计总计、趋势、板块占比", module = "统计信息/客户统计")
    public ResponseData custStatisticsResult(@RequestParam Map map){
        return ResponseData.ok().putDataValue("result",statisticsService.custStatisticsResult(map));
    }

    /**
     * 客户统计板块占比
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listMediaTypeStatisticsByParam")
    @Log(opType = OperateType.QUERY, note = "客户统计板块占比", module = "统计信息/客户统计")
    public ResponseData listMediaTypeStatisticsByParam(@RequestParam Map map){
        return ResponseData.ok().putDataValue("list",statisticsService.listMediaTypeStatisticsByParam(map));
    }

    /**
     * 统计客户排名-旧代码
     * @param map
     * @param pageable
     * @return
     */
    @ResponseBody
    @RequestMapping("/statisticsRanking")
    @Log(opType = OperateType.QUERY, note = "统计客户排名", module = "统计信息/客户统计")
    public PageInfo<Map<String,Object>> statisticsRanking(@RequestParam Map map, @PageableDefault() Pageable pageable){
//        DataSecurityUtil.addSecurity(map);
        return statisticsService.statisticsRanking(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    /**
     * 统计客户排名
     * @param map
     * @param pageable
     * @return
     */
    @ResponseBody
    @RequestMapping("/listCustStatisticsRankingByParam")
    @Log(opType = OperateType.QUERY, note = "统计客户排名", module = "统计信息/客户统计")
    public PageInfo<Map> listCustStatisticsRankingByParam(@RequestParam Map map, @PageableDefault() Pageable pageable){
        return statisticsService.listCustStatisticsRankingByParam(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    /**
     * 统计新成交客户排名
     * @param map
     * @param pageable
     * @return
     */
    @ResponseBody
    @RequestMapping("/listNewCustStatisticsRankingByParam")
    @Log(opType = OperateType.QUERY, note = "统计新成交客户排名", module = "统计信息/客户统计")
    public PageInfo<Map> listNewCustStatisticsRankingByParam(@RequestParam Map map, @PageableDefault() Pageable pageable){
        return statisticsService.listNewCustStatisticsRankingByParam(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    /**
     * 导出全部
     */
    @RequestMapping("/statisticsRankingAll")
    @Verify(code = "/statistics/statisticsRankingAll", module = "统计信息/导出全部")
    @Log(opType = OperateType.QUERY, module = "统计信息/导出全部", note = "导出全部")
    public void statisticsRankingAll(HttpServletResponse response, @RequestParam Map map) {
//        DataSecurityUtil.addSecurity(map);
        try {
            String fileName = "客户排名列表";
            if(map.get("newCustFlag") != null){
                fileName = "新成交客户列表";
            }
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName+".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            statisticsService.statisticsRankingAll(map, outputStream);
        } catch (Exception e) {
            log.error("导出客户失败", e);
        }
    }


}
