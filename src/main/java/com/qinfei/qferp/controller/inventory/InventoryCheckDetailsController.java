package com.qinfei.qferp.controller.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.inventoryStock.InventoryCheckDetails;
import com.qinfei.qferp.service.inventory.IInventoryCheckDetailsService;
import com.qinfei.qferp.service.inventory.IWarehouseService;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.FileUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

/**
 * 库存盘点明细表控制层
 *
 * @author tsf
 * @since 2020-06-02 16:18:51
 */
@Slf4j
@Controller
@RequestMapping("/checkDetails")
public class InventoryCheckDetailsController {
    @Autowired
    private IInventoryCheckDetailsService checkDetailsService;
    @Autowired
    private Config config;
    @Autowired
    private IWarehouseService warehouseService;

    /**
     * 库存预警分页数量
     * @param map
     * @return
     */
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        Integer total=checkDetailsService.getPageCount(map);
        data.putDataValue("total",total);
        return data;
    }

    /**
     * 库存预警分页数据
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, Pageable pageable){
        return checkDetailsService.listPg(map,pageable);
    }

    /**
     * 库存预警导出
     * @param map
     * @param response
     */
    @RequestMapping("/exportForeWarning")
    @ResponseBody
    public void exportForeWarning(@RequestParam Map map, HttpServletResponse response){
        try {
            String wareId=map.get("foreWareIdQc").toString();
            String wareName=null;
            if(StringUtils.isEmpty(wareId)){
                wareName="总仓库";
            }else {
                String name = warehouseService.getWareNameById(Integer.valueOf(wareId));
                wareName=name;
            }
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;fileName="+ URLEncoder.encode(wareName+"库存预警"+ UUIDUtil.get8UUID()+".xls","UTF-8"));
            OutputStream outputStream=response.getOutputStream();
            checkDetailsService.exportForeWarning(map,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 库存盘点导入
     * @param filePath
     */
    @RequestMapping("/importInventoryCheckData")
    @ResponseBody
    public ResponseData importInventoryCheckData(@RequestParam("filePath")String filePath){
        if(!ObjectUtils.isEmpty(filePath) && filePath.contains("statics")){
            filePath = filePath.replace("statics", "");
        }
        String fileName = config.getUploadDir() + File.separator + filePath;
        try {
            checkDetailsService.importInventoryCheckData(fileName);
            FileUtil.delFiles(Arrays.asList(fileName));
            return ResponseData.ok();
        } catch (QinFeiException e) {
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            if(e.getCode() == 1003){  //1003：message值为文件下载路径
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "导入失败，请点击红色提示内容下载文件检查内容是否符合要求。");
                data.putDataValue("file", e.getMessage());
                return data;
            }else{
                return ResponseData.customerError(e.getCode(),e.getMessage());
            }
        } catch (Exception e){
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002,"很抱歉，批量导入出错啦！");
        }
    }


    /**
     * 获取导入模板
     * @param response
     */
    @RequestMapping("/exportTemplate")
    @ResponseBody
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response){
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = "库存盘点导入模板";
            String index = UUIDUtil.get8UUID();
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + index + ".xls");
            OutputStream outputStream = response.getOutputStream();
            checkDetailsService.exportTemplate(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }
    }

    @RequestMapping("/editCheckDetails")
    @ResponseBody
    public ResponseData editCheckDetails(InventoryCheckDetails checkDetails){
        checkDetailsService.editCheckDetails(checkDetails);
        return null;
    }
}