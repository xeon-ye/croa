package com.qinfei.qferp.controller.biz.statistics;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.service.biz.statistics.IBusinessManagerStatisticsService;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 业务统计
 */
@Slf4j
@Controller
@RequestMapping("/businessManagerStatistics")
class BusinessManagerStatisticsController {
    @Autowired
    IBusinessManagerStatisticsService businessManagerStatisticsService;

    /**
     * 应收金额
     * @param map
     * @return
     */
    @RequestMapping("/topOptionSetValue")
    @Log(opType = OperateType.UPDATE, module = "业务统计/设置稿件数量，应收金额", note = "业务统计/设置稿件数量，应收金额")
    @ResponseBody
    public List<Map> topOptionSetValue(@RequestParam Map map){
        //添加数据权限的控制代码
//        DataSecurityUtil.addSecurity(map);
        return businessManagerStatisticsService.topOptionSetValue(map);
    }

    /**
     * 订单排名
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/orderSort")
    @Log(opType = OperateType.QUERY, module = "业务统计/订单排名", note = "业务统计/订单排名")
    @ResponseBody
    public PageInfo<Map> orderSort(@RequestParam Map map, Pageable pageable){
        //添加数据权限的控制代码
//        DataSecurityUtil.addSecurity(map);
        List<Map> lists = businessManagerStatisticsService.orderSort(map,pageable.getPageNumber(),pageable.getPageSize());
        return new PageInfo<Map>(lists);
    }

    /**
     * 稿件排名
     * @param map
     * @param pageable
     * @return
     */
    @Log(opType = OperateType.QUERY,note = "查询利润排名", module = "业务管理/利润排名")
    @RequestMapping("/profitSort")
    @ResponseBody
    public PageInfo<Map> profitSort(@RequestParam Map map, Pageable pageable){
        return businessManagerStatisticsService.profitSort(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @Log(opType = OperateType.QUERY,note = "查询业务员未到款排名", module = "业务管理/业务员未到款排名")
    @RequestMapping("/businessNoIncomeSort")
    @ResponseBody
    public PageInfo<Map> businessNoIncomeSort(@RequestParam Map map, Pageable pageable){
        return businessManagerStatisticsService.businessNoIncomeSort(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @Log(opType = OperateType.QUERY,note = "查询客户应收金额排名", module = "业务管理/客户应收金额排名")
    @RequestMapping("/custSaleAmountSort")
    @ResponseBody
    public PageInfo<Map> custSaleAmountSort(@RequestParam Map map, Pageable pageable){
        return businessManagerStatisticsService.custSaleAmountSort(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @Log(opType = OperateType.QUERY,note = "查询客户未到款金额排名", module = "业务管理/客户未到款金额排名")
    @RequestMapping("/custNoIncomeSort")
    @ResponseBody
    public PageInfo<Map> custNoIncomeSort(@RequestParam Map map, Pageable pageable){
        return businessManagerStatisticsService.custNoIncomeSort(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @RequestMapping("/articleSort")
    @Log(opType = OperateType.QUERY, module = "业务统计/稿件排名", note = "业务统计/稿件排名")
    @ResponseBody
    public PageInfo<Map> articleSort(@RequestParam Map map,Pageable pageable){
        //添加数据权限的控制代码
//        DataSecurityUtil.addSecurity(map);
        List<Map> lists = businessManagerStatisticsService.articleSort(map,pageable.getPageNumber(),pageable.getPageSize());
        return new PageInfo<Map>(lists);
    }

    /**
     * 查询各个部门的
     * @param list
     * @param dateSelect
     * @return
     */
    @RequestMapping("/everyDeptBusiness")
    @Log(opType = OperateType.QUERY, module = "业务统计/统计各个部门的业务", note = "业务统计/统计各个部门的业务")
    @ResponseBody
    public ResponseData everyDeptBusiness(@RequestParam(required = false,value = "list") String list, @RequestParam(required = false,value = "dateSelect") String dateSelect){
        if(StringUtils.isEmpty(list) || list.split(",").length < 1){
            return ResponseData.ok().putDataValue("list",new ArrayList<>());
        }
        String[] strs = list.split(",");
        List<Integer> depts = new ArrayList<>();
        try{
            for(int i = 0; i<strs.length; i++){
                if(!StringUtils.isEmpty(strs[i])){
                    depts.add(Integer.parseInt(strs[i]));
                }
            }
        }catch (Exception e){
            return ResponseData.customerError(1001,"部门ID转换异常");
        }
        List<Map> result = businessManagerStatisticsService.everyDeptBusiness(depts,dateSelect);
        return ResponseData.ok().putDataValue("list",result);
    }

    /**
     * 统计业务前5名
     * @param map
     * @return
     */
    @RequestMapping("/businessTop")
    @Log(opType = OperateType.QUERY, module = "业务统计/统计业务前5名", note = "业务统计/统计业务前5名")
    @ResponseBody
    public List<Map> businessTop(@RequestParam Map map){
        return businessManagerStatisticsService.businessTop(map);
    }
}
