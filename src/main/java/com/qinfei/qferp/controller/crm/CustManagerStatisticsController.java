package com.qinfei.qferp.controller.crm;

import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.service.crm.ICustManagerStatisticsService;
import com.qinfei.qferp.service.crm.IStatisticsService;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/custManagerStatistics")
class CustManagerStatisticsController {
    @Autowired
    ICustManagerStatisticsService custManagerStatisticsService;

    /**
     * 按条件查询
     * @param map
     * @return
     */
    @RequestMapping("/topStatistics")
    @Log(opType = OperateType.QUERY, note = "客户统计/按条件查询", module = "客户统计/按条件查询")
    @ResponseBody
    public Map topStatistics(@RequestParam Map map){
//        DataSecurityUtil.addSecurity(map);
        return custManagerStatisticsService.topStatistics(map);
    }

    /**
     * 查询各个部门的男女人数
     * @param list
     * @return
     */
    @RequestMapping("/everyDeptUserCount")
    @Log(opType = OperateType.QUERY, note = "客户统计/查询各个部门的男女人数", module = "客户统计/查询各个部门的男女人数")
    @ResponseBody
    public List<Map> everyDeptUserCount(@RequestParam(required = false,value = "list") String list){
        try {
            if (list == null || list.split(",").length < 1) {
                return new ArrayList();
            }
        }catch(Exception e){
            log.error("查询部门男女人数异常",e);
        }
        List<String> depts = Arrays.asList(list.split(","));
        return custManagerStatisticsService.everyDeptUserCount(depts);
    }

    /**
     * 统计各种类型的客户
     * @param map
     * @return
     */
    @RequestMapping("/custPie")
    @ResponseBody
    public Map custPie(@RequestParam Map map){
//        DataSecurityUtil.addSecurity(map);
        return custManagerStatisticsService.custPie(map);
    }

    /**
     * 获取客户列表
     * @param pageable
     * @param map
     * @return
     */
    @RequestMapping("/listCustByParam")
    @ResponseBody
    public PageInfo<Map<String, Object>> listCustByParam(@PageableDefault() Pageable pageable, @RequestParam Map map) {
        return custManagerStatisticsService.listCustByParam(pageable.getPageNumber(), pageable.getPageSize(), map);
    }
}
