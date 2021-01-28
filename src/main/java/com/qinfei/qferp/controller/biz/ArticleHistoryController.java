package com.qinfei.qferp.controller.biz;

import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.service.biz.IArticleHistoryService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/articleHistory")
public class ArticleHistoryController {
    @Autowired
    private IArticleHistoryService articleHistoryService;

    @RequestMapping("/queryArticleChange")
//    @Log(opType = OperateType.QUERY, module = "异动管理/异动统计", note = "异动管理/异动统计")
    @ResponseBody
    public PageInfo<Map<String,Object>> queryArticleChange(@PageableDefault Pageable pageable, @RequestParam Map map){
        return articleHistoryService.queryArticleChange(pageable.getPageNumber(),pageable.getPageSize(),map);
    }

    @RequestMapping("/queryArticleChangeSum")
    @ResponseBody
    public Map queryArticleChangeSum(@RequestParam Map map){
        return articleHistoryService.queryArticleChangeSum(map);
    }

    @RequestMapping("/queryArticleChangeDetail")
//    @Log(opType = OperateType.QUERY, module = "异动管理/异动详情", note = "异动管理/异动详情")
    @ResponseBody
    public PageInfo<Map<String,Object>> queryArticleChangeDetail(@PageableDefault Pageable pageable, @RequestParam Map map){
        return articleHistoryService.queryArticleChangeDetail(pageable.getPageNumber(),pageable.getPageSize(),map);
    }

    @RequestMapping("/queryArticleChangeDetailSum")
    @ResponseBody
    public Map queryArticleChangeDetailSum(@RequestParam Map map){
        return articleHistoryService.queryArticleChangeDetailSum(map);
    }

    @RequestMapping("/exportArticleChange")
//    @Log(opType = OperateType.QUERY, module = "业绩统计/异动导出", note = "业绩统计/异动导出")
    @ResponseBody
    public void exportArticleChange(HttpServletResponse response, @RequestParam Map map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("异动统计" + DateUtils.getNowTime() + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            articleHistoryService.exportArticleChange(map, outputStream);
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

    @RequestMapping("/exportArticleChangeDetail")
//    @Log(opType = OperateType.QUERY, module = "业绩统计/异动详情导出", note = "业绩统计/异动详情导出")
    @ResponseBody
    public void exportArticleChangeDetail(HttpServletResponse response, @RequestParam Map map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("异动详情" + DateUtils.getNowTime() + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            articleHistoryService.exportArticleChangeDetail(map, outputStream);
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

    @RequestMapping("/queryArticleChangeSingle")
//    @Log(opType = OperateType.QUERY, module = "异动管理/异动详情", note = "异动管理/异动详情")
    @ResponseBody
    public PageInfo<Map<String,Object>> queryArticleChangeSingle(@PageableDefault Pageable pageable, @RequestParam Map map){
        return articleHistoryService.queryArticleChangeSingle(pageable.getPageNumber(),pageable.getPageSize(),map);
    }

    @RequestMapping("/queryArticleChangeSingleSum")
    @ResponseBody
    public Map queryArticleChangeSingleSum(@RequestParam Map map){
        return articleHistoryService.queryArticleChangeSingleSum(map);
    }
}
