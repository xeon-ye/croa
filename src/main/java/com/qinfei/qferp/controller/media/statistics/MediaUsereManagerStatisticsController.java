package com.qinfei.qferp.controller.media.statistics;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.mediauser.statistics.IMediaUsereManagerStatisticsService;
import com.qinfei.qferp.utils.AppUtil;

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
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/mediaUsereManagerStatistics")
class MediaUsereManagerStatisticsController {
    @Autowired
    IMediaUsereManagerStatisticsService mediaUsereManagerStatisticsService;

    @RequestMapping("/topOptionSetValue")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = " 统计稿件数量，应付金额，已付金额，请款金额", module = "媒体管理/统计")
    public List<Map> topOptionSetValue(@RequestParam Map map) {
//        DataSecurityUtil.addSecurity(map);
        return mediaUsereManagerStatisticsService.topOptionSetValue(map);
    }

    //供应商排名
    @RequestMapping("/supplierSort")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = " 供应商排名统计", module = "媒体管理/统计")
    public PageInfo<Map> supplierSort(@RequestParam Map map, Pageable pageable){
//        DataSecurityUtil.addSecurity(map);
        List<Map> lists = mediaUsereManagerStatisticsService.supplierSort(map,pageable.getPageNumber(),pageable.getPageSize());
        return new PageInfo<Map>(lists);
    }

    //请款排名
    @RequestMapping("/outgoSort")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = " 请款排名", module = "媒体管理/统计")
    public PageInfo<Map> outgoSort(@RequestParam Map map,Pageable pageable){
//        DataSecurityUtil.addSecurity(map);
        List<Map> lists = mediaUsereManagerStatisticsService.outgoSort(map,pageable.getPageNumber(),pageable.getPageSize());
        return new PageInfo<Map>(lists);
    }

    //稿件分布类型
    @RequestMapping("/artTypeFb")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = " 稿件分布类型", module = "媒体管理/统计")
    public List<Map> artTypeFb(@RequestParam Map map) {
//        DataSecurityUtil.addSecurity(map);
        return mediaUsereManagerStatisticsService.artTypeFb(map);
    }

    //供应商列表统计-旧的
    @RequestMapping("/supplierListSort")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = " 供应商列表统计", module = "媒体管理/统计")
    public PageInfo<Map> supplierListSort(@RequestParam Map map,Pageable pageable){
//        DataSecurityUtil.addSecurity(map);
//        User user = AppUtil.getUser();
//        map.put("companyCode",user.getCompanyCode());
        List<Map> lists = mediaUsereManagerStatisticsService.supplierListSort(map,pageable.getPageNumber(),pageable.getPageSize());
        return new PageInfo<Map>(lists);
    }

    @RequestMapping("/supplierStatisticsResult")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "供应商统计合计", module = "统计信息/供应商统计")
    public ResponseData supplierStatisticsResult(@RequestParam Map map) {
        return ResponseData.ok().putDataValue("result",mediaUsereManagerStatisticsService.supplierStatisticsResult(map));
    }

    @RequestMapping("/listSupplierStatisticsByParam")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "供应商统计列表", module = "统计信息/供应商统计")
    public PageInfo<Map> listSupplierStatisticsByParam(@PageableDefault() Pageable pageable, @RequestParam Map map) {
        return mediaUsereManagerStatisticsService.listSupplierStatisticsByParam(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    /**
     * 供应商统计导出
     * @param map
     * @param response
     */
    @RequestMapping("/exportAll")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "供应商统计|导出供应商信息", note = "导出供应商信息")
    public void exportAll(@RequestParam Map map, HttpServletResponse response){
        try{
            User user = AppUtil.getUser();
            List<Role> roleList = user.getRoles();
            if(roleList==null && roleList.size()==0){
                throw new Exception("未找到角色信息");
            }else{
                //部门添加权限
//                DataSecurityUtil.addSecurity(map);
                map.put("roleType",roleList.get(0).getType());
                map.put("roleCode",roleList.get(0).getCode());
                map.put("companyCode",user.getCompanyCode());
                //application/binary;charset=utf-8
                response.setContentType("application/binary;charset=utf-8");
                response.setHeader("Content-Disposition","attachment;fileName="+ URLEncoder.encode("供应商排名列表.xls","utf-8"));
                OutputStream out = response.getOutputStream();
                mediaUsereManagerStatisticsService.exportSupplier(map,out);
            }
        }catch (Exception e){
            log.error("供应商统计信息导出失败",e);
        }finally {

        }

    }
}
