package com.qinfei.qferp.service.impl.statistics;

import com.qinfei.qferp.entity.sys.SysConfig;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.biz.ArticleMapperXML;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.service.statistics.IStatisticsOverviewService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatisticsOverviewServiceImpl extends BaseService implements IStatisticsOverviewService {
    @Autowired
    private ArticleMapperXML articleMapperXML;

    @Override
    public Map<String, Object> getStatisticsById(Map<String, Object> map) {
        this.addSecurityByFore(map);//权限添加
        return articleMapperXML.getStatisticsById(map);
    }

    @Override
    public List<Map> listMediaTypeStatisticsById(Map<String, Object> map) {
        this.addSecurityByFore(map);//权限添加
        return articleMapperXML.listMediaTypeStatisticsById(map);
    }

    @Override
    public List<Map> listTrendStatisticsById(Map<String, Object> map) {
        this.addSecurityByFore(map);//权限添加
        return articleMapperXML.listTrendStatisticsById(map);
    }

    @Override
    public PageInfo<Map> listMediaStatisticsByCustId(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);//权限添加
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(articleMapperXML.listMediaStatisticsById(map));
    }

    @Override
    public PageInfo<Map> listCustStatisticsBybusinessId(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);//权限添加
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(articleMapperXML.listCustStatisticsBybusinessId(map));
    }

    @Override
    public PageInfo<Map> listMediaStatisticsByMediaUserId(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);//权限添加
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(articleMapperXML.listMediaStatisticsById(map));
    }

    @Override
    public PageInfo<Map> listMediaStatisticsByMediaTypeId(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);//权限添加
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(articleMapperXML.listMediaStatisticsById(map));
    }

    @Override
    public PageInfo<Map> listCustStatisticsByMediaId(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);//权限添加
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(articleMapperXML.listCustStatisticsByMediaId(map));
    }

    @Override
    public PageInfo<Map> listMediaStatisticsBySupplierId(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);//权限添加
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(articleMapperXML.listMediaStatisticsById(map));
    }

    @Override
    public void statisticsOverviewRankingExport(HttpServletResponse response, Map map) {
        try {
            if(map == null || map.get("exportFileType") == null){
                throw new RuntimeException("导出文件类型不存在");
            }
            String fileName = String.valueOf(map.get("exportFileName"));
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName+".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            exportHandleByType(fileName,String.valueOf(map.get("exportFileType")),outputStream,map);
        }catch (Exception e){
            throw new RuntimeException("导出文件失败");
        }
    }

    /**
     * 统计概况-根据不同维度导出不同列表
     * @param exportFileName 导出文件名
     * @param exportType 维度
     */
    private void exportHandleByType(String exportFileName,String exportType, OutputStream outputStream, Map map){
        this.addSecurityByFore(map);
        User user = AppUtil.getUser();
        if("mediaType".equals(exportType) || "cust".equals(exportType) || "mediaUser".equals(exportType) || "supplier".equals(exportType)){
            List<Map> list = articleMapperXML.listMediaStatisticsById(map);
            List<String> uList = SysConfigUtils.getConfigValue("projectDirector",List.class);
            String[] heads = {"媒体名称","板块名称", "稿件数量", "报价金额", "成本金额", "未到款金额","利润","业务员","客户","供应商"};
            String[] fields={"mediaName","mediaTypeName","articleNum","saleAmount","payAmount","noIncomeAmount","profit","businessUser","cust","supplierUser"};
            ExcelUtil.exportExcel(exportFileName,heads, fields, list, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if("articleNum".equals(field) || "saleAmount".equals(field) || "payAmount".equals(field) || "noIncomeAmount".equals(field) || "profit".equals(field)){
                    if(value != null){
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }else{
                        cell.setCellValue(0);
                    }
                } else if("businessUser".equals(field) || "cust".equals(field) || "supplierUser".equals(field)){
                   if(value != null){
                       if("cust".equals(field)){
                           if(IConst.ROLE_TYPE_MJ.equals(user.getDept().getCode())){
                               cell.setCellValue("****");
                           } else{
                               if(CollectionUtils.isNotEmpty(uList) && uList.contains(user.getId().toString())){
                                   cell.setCellValue("****");
                               }else {
                                   cell.setCellValue(handleCellValue(value, field));
                               }
                           }
                       }else if("supplierUser".equals(field)){
                           if(IConst.ROLE_TYPE_YW.equals(user.getDept().getCode())){
                               cell.setCellValue("****");
                           }else{
                               cell.setCellValue(handleCellValue(value, field));
                           }
                       }
                       else{
                           cell.setCellValue(handleCellValue(value, field));
                       }
                   }else {
                       cell.setCellValue("");
                   }
                } else{
                    if(value != null){
                        cell.setCellValue(value.toString());
                    }else{
                        cell.setCellValue("");
                    }
                }
            });
        }
        if("media".equals(exportType)){
            List<Map> list = articleMapperXML.listCustStatisticsByMediaId(map);
            String[] heads = {"客户名称","客户公司名称", "稿件数量", "报价金额", "成本金额", "未到款金额","利润","板块名称","业务员","媒介","供应商"};
            String[] fields={"custName","custCompanyName","articleNum","saleAmount","payAmount","noIncomeAmount","profit","mediaType","businessUser","mediaUser","supplierUser"};
            ExcelUtil.exportExcel(exportFileName,heads, fields, list, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if("articleNum".equals(field) || "saleAmount".equals(field) || "payAmount".equals(field) || "noIncomeAmount".equals(field) || "profit".equals(field)){
                    if(value != null){
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }else{
                        cell.setCellValue(0);
                    }
                }else if("mediaType".equals(field) || "businessUser".equals(field) || "mediaUser".equals(field) || "supplierUser".equals(field)){
                    if(value != null){
                        if("supplierUser".equals(field)){
                            if(IConst.ROLE_TYPE_YW.equals(user.getDept().getCode())){
                                cell.setCellValue("****");
                            }else{
                                cell.setCellValue(handleCellValue(value, field));
                            }
                        }else{
                            cell.setCellValue(handleCellValue(value, field));
                        }
                    }else {
                        cell.setCellValue("");
                    }
                }else if("custName".equals(field) || "custCompanyName".equals(field)){
                   if(value != null){
                       if(IConst.ROLE_TYPE_MJ.equals(user.getDept().getCode())){
                           cell.setCellValue(value.toString().substring(0,1)+"****");
                       }else{
                           cell.setCellValue(value.toString());
                       }
                   }else{
                       cell.setCellValue("");
                   }
                } else{
                    if(value != null){
                        cell.setCellValue(value.toString());
                    }else{
                        cell.setCellValue("");
                    }
                }
            });
        }
        if("business".equals(exportType)){
            List<String> PD = SysConfigUtils.getConfigValue("projectDirector",List.class);
            Integer userId = AppUtil.getUser().getId();
            List<Map> list = articleMapperXML.listCustStatisticsBybusinessId(map);
            String[] heads = {"客户名称","客户公司名称", "稿件数量", "报价金额", "成本金额", "未到款金额","利润","板块名称","媒体名称","媒介","供应商"};
            String[] fields={"custName","custCompanyName","articleNum","saleAmount","payAmount","noIncomeAmount","profit","mediaType","media","mediaUser","supplierUser"};
            ExcelUtil.exportExcel(exportFileName,heads, fields, list, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if("articleNum".equals(field) || "saleAmount".equals(field) || "payAmount".equals(field) || "noIncomeAmount".equals(field) || "profit".equals(field)){
                    if(value != null){
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }else{
                        cell.setCellValue(0);
                    }
                }else if("mediaType".equals(field) || "media".equals(field) || "mediaUser".equals(field) || "supplierUser".equals(field)){
                    if(value != null){
                        if("supplierUser".equals(field)){
                            if(IConst.ROLE_TYPE_YW.equals(user.getDept().getCode())){
                                cell.setCellValue("****");
                            }else{
                                cell.setCellValue(handleCellValue(value, field));
                            }
                        }else{
                            cell.setCellValue(handleCellValue(value, field));
                        }
                    }else {
                        cell.setCellValue("");
                    }
                } else if("custCompanyName".equals(field)){
                    if(value != null){
                        if(IConst.ROLE_TYPE_MJ.equals(user.getDept().getCode())){
                            cell.setCellValue(value.toString().substring(0,1)+"****");
                        }else{
                            cell.setCellValue(value.toString());
                        }
                    }else{
                        cell.setCellValue("");
                    }
                }else if("custName".equals(field)){
                    boolean b = false;
                    boolean a = false;
                    if (CollectionUtils.isNotEmpty(PD) && PD.contains(userId.toString())){
                        b = true;
                    }
                    if (b){
                        if( b && StringUtils.isNotEmpty(map.get("businessUserId").toString()) && !userId.toString().equals( map.get("businessUserId").toString())){
                            a = true;
                        }
                    }
                    if(value != null){
                        if(IConst.ROLE_TYPE_MJ.equals(user.getDept().getCode())){
                            cell.setCellValue( a ? "****" : value.toString().substring(0,1)+"****");
                        }else{
                            cell.setCellValue(a ? "****" : value.toString());
                        }
                    }else{
                        cell.setCellValue("");
                    }
                }
                else{
                   if(value != null){
                       cell.setCellValue(value.toString());
                   }else{
                       cell.setCellValue("");
                   }
                }
            });
        }
    }

    /**
     * 处理单元格内容
     * @param value
     * @return
     */
    private String handleCellValue(Object value, String field){
        User user = AppUtil.getUser();
        StringBuilder stringBuilder = new StringBuilder();
        if(value != null){
            String [] strs = value.toString().split(",");
            for (String str:strs) {
                int index  = str.indexOf("->");
                if(index != -1){
                    String [] strArr = str.split("->");
                    String name = strArr != null && strArr.length > 1 ? strArr[1] : "";
                    String companyCode = null;
                    if(strArr.length == 3){
                        companyCode = strArr[2];
                    }
                    if("supplierUser".equals(field) && StringUtils.isNotEmpty(companyCode) && !companyCode.equals(user.getDept().getCompanyCode())){
                        if(StringUtils.isEmpty(stringBuilder.toString())){
                            stringBuilder.append("****"+"，");
                        }
                    }else {
                        stringBuilder.append(name+"，");
                    }
                }else{
                    stringBuilder.append(str+"，");
                }
            }
            if(stringBuilder.toString().lastIndexOf("，") == (stringBuilder.toString().length()-1)){
                stringBuilder.deleteCharAt(stringBuilder.toString().length() -1);
            }
        }
        return stringBuilder.toString();
    }
}
