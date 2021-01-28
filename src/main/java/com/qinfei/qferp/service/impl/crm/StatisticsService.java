package com.qinfei.qferp.service.impl.crm;

import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.crm.StatisticsMapper;
import com.qinfei.qferp.service.crm.IStatisticsService;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.SysConfigUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户统计
 */
@Service
public class StatisticsService extends BaseService implements IStatisticsService{
    public final static String cacheKey = "statistics";
    @Autowired
    StatisticsMapper statisticsMapper;


//    @Cacheable(value=cacheKey)
    public List<Map<String,Object>> statisticsResult(Map map){
        this.addSecurityByFore(map);
        return statisticsMapper.statisticsResult(map);
    }

    @Override
    public Map<String, Object> custStatisticsResult(Map map) {
        this.addSecurityByFore(map);
        Map custStatistics = statisticsMapper.getCustStatisticsByParam(map);
        List<Map> trend = statisticsMapper.listCustTrendStatisticsByParam(map);
        List<Map> mediaTypeRate = statisticsMapper.listCustMediaTypeStatisticsByParam(map);
        Map<String,Object> result = new HashMap<>();
        result.put("custStatistics",custStatistics);
        result.put("trend",trend);
        result.put("mediaTypeRate",mediaTypeRate);
        return result;
    }

    @Override
    public List<Map> listMediaTypeStatisticsByParam(Map<String, Object> map) {
        this.addSecurityByFore(map);
        return statisticsMapper.listCustMediaTypeStatisticsByParam(map);
    }

    public PageInfo<Map<String,Object>> statisticsRanking(Map map,Integer pageNum,Integer pageSize){
        this.addSecurityByFore(map);
        PageHelper.startPage(pageNum,pageSize);
        return new PageInfo<Map<String,Object>>(statisticsMapper.statisticsRanking(map));
    }

    @Override
    public PageInfo<Map> listCustStatisticsRankingByParam(Map map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);
        PageHelper.startPage(pageNum,pageSize);
        return new PageInfo<>(statisticsMapper.listCustStatisticsRankingByParam(map));
    }

    @Override
    public PageInfo<Map> listNewCustStatisticsRankingByParam(Map map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);
        PageHelper.startPage(pageNum,pageSize);
        map.put("newCustFlag",true);//统计新成交客户
        return new PageInfo<>(statisticsMapper.listCustStatisticsRankingByParam(map));
    }

    @Override
    public void statisticsRankingAll(Map map, OutputStream outputStream){
        this.addSecurityByFore(map);
//        List<Map<String,Object>> list = statisticsMapper.statisticsRanking(map);
        List<Map> list =  statisticsMapper.listCustStatisticsRankingByParam(map);
        String fileName = "客户排名列表";
        if(map.get("newCustFlag") != null){
            fileName = "新成交客户列表";
        }
        List<String> PD = SysConfigUtils.getConfigValue("projectDirector",List.class);
        Integer userId = AppUtil.getUser().getId();
        String[] heads = {"客户公司名称","对接人","成交金额","未到款额","逾期款金额 ","利润"};
        String[] fields={ "companyName","custName","saleAmount","noIncomeAmount","dqysIncomeAmount","profit"};
        ExcelUtil.exportExcel( fileName,heads, fields, list, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if(value!=null){
                if("saleAmount".equals(field) || "noIncomeAmount".equals(field) || "dqysIncomeAmount".equals(field) || "profit".equals(field)){
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else if("custName".equals(field)){
                    boolean b = false;
                    boolean a = false;
                    if (CollectionUtils.isNotEmpty(PD) && PD.contains(userId.toString())){
                        b = true;
                    }
                    if (b){
                        if( b && !StringUtils.isEmpty(list.get(rowIndex).get("userId")) &&  !userId.equals(list.get(rowIndex).get("userId") ) ){
                            a = true;
                        }
                        cell.setCellValue(a ? "***":value.toString());
                    }else {
                        cell.setCellValue(value.toString());
                    }
                }
                else{
                    cell.setCellValue(value.toString());
                }
            }
        });
//        return list;
     }
//
//    @Cacheable(value=cacheKey)
//    public List<Map> deptUsers(Map map){
//        return statisticsMapper.deptUsers(map);
//    }
}
