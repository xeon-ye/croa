package com.qinfei.qferp.controller.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media1.Media1;
import com.qinfei.qferp.service.media1.IMedia1Service;
import com.qinfei.qferp.utils.DataImportUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaType1Controller
 * @Description 媒体类型接口1
 * @Author xuxiong
 * @Date 2019/6/26 0026 17:14
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/media1")
@Api(description = "媒体接口1")
public class Media1Controller {
    @Autowired
    private IMedia1Service media1Service;

    @PostMapping("listMediaSupplierByParam")
    @ApiOperation(value = "新增稿件", notes = "新增稿件媒体供应商选择列表")
    @Verify(code = "/mediauser/mediauser_list1", module = "媒介查询/新增稿件媒体供应商选择列表", action = "4")
    @ResponseBody
    public PageInfo<Map<String, Object>> listMediaSupplierByParam(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable){
        return media1Service.listMediaSupplierByParam(map,pageable);
    }

    @PostMapping("listMediaByParam")
    @ApiOperation(value = "新增稿件媒体选择列表", notes = "新增稿件媒体选择列表")
    @Verify(code = "/mediauser/mediauser_list1", module = "媒介查询/新增稿件媒体选择列表", action = "4")
    @ResponseBody
    public PageInfo<Map<String, Object>> listMediaByParam(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable){
        return media1Service.listMediaByParam(map,pageable);
    }

    @PostMapping("getMediaSupplierInfoByMediaId")
    @ApiOperation(value = "媒体供应商价格", notes = "根据媒体ID获取媒体供应商价格信息")
    @Verify(code = "/media1/order", module = "媒体下单/根据媒体ID获取媒体供应商价格信息", action = "4")
    @ResponseBody
    public ResponseData getMediaSupplierInfoByMediaId(@RequestParam("mediaId") Integer mediaId, @RequestParam("cell") String cell){
        try{
            return ResponseData.ok().putDataValue("media",media1Service.getMediaSupplierInfoByMediaId(mediaId, cell));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "获取媒体供应商价格错误！");
        }
    }

    @GetMapping("getMediaSupplierInfoByMediaId/{mediaId}")
    @ApiOperation(value = "媒体供应商信息", notes = "根据媒体ID获取媒体供应商信息")
    @Verify(code = "/media1/order", module = "媒体下单/根据媒体ID获取媒体供应商信息", action = "4")
    @ResponseBody
    public ResponseData getMediaSupplierInfoByMediaId(@PathVariable("mediaId") Integer mediaId){
        try{
            return ResponseData.ok().putDataValue("media",media1Service.getMediaSupplierInfoByMediaId(mediaId));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "获取媒体供应商信息错误!");
        }
    }

    @GetMapping("listMedia")
    @ApiOperation(value = "媒体下单", notes = "分页查询媒体管理媒体列表")
    @Verify(code = "/media1/order", module = "媒体下单/分页查询媒体管理媒体列表", action = "4")
    @ResponseBody
    public PageInfo<Media1> listMedia(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable){
        return media1Service.listMedia(map, pageable);
    }

    /**
     * 获取指定板块的数据导入模板；
     * @param request：请求对象；
     * @param response：响应对象；
     */
    @GetMapping("batchExport")
    @ApiOperation(value = "媒体导出", notes = "根据条件导出所有媒体")
    @ResponseBody
    public void batchExport(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = map.get("plateName") + "导出媒体信息";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            media1Service.batchExport(outputStream,map);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }

    @PostMapping("listMediaFT")
    @ApiOperation(value = "媒体列表", notes = "媒体复投统计")
    @ResponseBody
    public List<Map<String, Object>> listMediaFT(@RequestParam Map<String, Object> map) {
        return media1Service.listMediaFT(map);
    }
/*
    @PostMapping("listMediaFTByPage")
    @ApiOperation(value = "媒体列表", notes = "媒体复投统计列表")
    @ResponseBody
    public PageInfo<Map<String, Object>> listMediaFTByPage(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable) {
        return media1Service.listMediaFTByPage(map, pageable);
    }*/

    /**
     * 获取指定板块的数据导入模板；
     * @param request：请求对象；
     * @param response：响应对象；
     */
    /*@GetMapping("batchFTExport")
    @ApiOperation(value = "媒体列表", notes = "媒体复投统计列表导出")
    @ResponseBody
    public void batchFTExport(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = String.format("%s-媒体复投详情", map.get("mediaName"));
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            media1Service.batchFTExport(outputStream,map);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }*/


    /**
     * 媒体表数据迁移
     * @return
     */
/*    @GetMapping("transfer")
    @ResponseBody
    public ResponseData transfer(){
        try{
            return ResponseData.ok().putDataValue("result",media1Service.transfer());
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002,"数据迁移错误！");
        }
    }*/
}
