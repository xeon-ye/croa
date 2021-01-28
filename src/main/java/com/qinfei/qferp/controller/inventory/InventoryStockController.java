
package com.qinfei.qferp.controller.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.inventoryStock.Outbound;
import com.qinfei.qferp.service.inventory.IInventoryStockService;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.FileUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/inventoryStock")
public class InventoryStockController {
    @Autowired
    private IInventoryStockService inventoryStockService;
    @Autowired
    private Config config;

    /**
     * 获取入库列表分页数据
     * @param map
     * @return
     */
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        Integer total = inventoryStockService.getPageCount(map);
        data.putDataValue("total",total);
        return data;
    }

    /**
     * 获取入库列表分页数据
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, @PageableDefault()Pageable pageable){
        return inventoryStockService.listPg(map,pageable);
    }

    /**
     * 获取出库分页数据
     * @param map
     * @return
     */
    @RequestMapping("/getOutStockPageCount")
    @ResponseBody
    public ResponseData getOutStockPageCount(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        Integer total = inventoryStockService.getOutStockPageCount(map);
        data.putDataValue("total",total);
        return data;
    }

    /**
     * 获取出库分页数据
     * @param map
     * @return
     */
    @RequestMapping("/getOutStockListPg")
    @ResponseBody
    public PageInfo<Map> getOutStockListPg(@RequestParam Map map,@PageableDefault()Pageable pageable){
        return inventoryStockService.getOutStockListPg(map,pageable);
    }

    /**
     * 新增入库
     * @param outbound
     * @param purchaseDetails
     * @return
     */
    @RequestMapping("/addOutBound")
    @ResponseBody
    public ResponseData addOutBound(Outbound outbound,@RequestParam(value = "purchaseDetails[]",required = false)List<String> purchaseDetails){
        ResponseData data = ResponseData.ok();
        try {
            Outbound obj = inventoryStockService.addOutBound(outbound,purchaseDetails);
            data.putDataValue("entity",obj);
            data.putDataValue("message","操作成功");
        } catch (Exception e) {
            ResponseData.customerError(1002,"抱歉，新增物品入库出错啦，请联系技术人员");
        }
        return data;
    }

    /**
     * 编辑入库
     * @param outbound
     * @param purchaseDetails
     * @return
     */
    @RequestMapping("/editOutBound")
    @ResponseBody
    public ResponseData editOutBound(Outbound outbound,@RequestParam(value = "purchaseDetails[]",required = false)List<String> purchaseDetails){
        ResponseData data = ResponseData.ok();
        try {
            Outbound obj = inventoryStockService.editOutBound(outbound,purchaseDetails);
            data.putDataValue("entity",obj);
            data.putDataValue("message","操作成功");
        } catch (QinFeiException e) {
            ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            ResponseData.customerError(1002,"抱歉，编辑物品入库出错啦，请联系技术人员");
        }
        return data;
    }

    /**
     * 根据id删除出入库记录
     * @param id 出入库记录id
     * @param purchaseId 物品采购id
     * @return
     */
    @RequestMapping("/delOutbound")
    @ResponseBody
    public ResponseData delOutbound(@RequestParam(value = "id")Integer id,@RequestParam(value = "purchaseId")Integer purchaseId){
        ResponseData data = ResponseData.ok();
        try {
            inventoryStockService.delOutboundById(id,purchaseId);
            data.putDataValue("message","操作成功");
        } catch (Exception e) {
            ResponseData.customerError(1002,"抱歉，删除物品入库出错啦，请联系技术人员");
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 新增出库
     * @param outbound
     * @param applyDetails
     * @return
     */
    @RequestMapping("/addOutStock")
    @ResponseBody
    public ResponseData addOutStock(Outbound outbound,@RequestParam(value = "applyDetails[]")List<String> applyDetails){
        ResponseData data = ResponseData.ok();
        try {
            inventoryStockService.addOutStock(outbound,applyDetails);
            data.putDataValue("message","操作成功");
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
            ResponseData.customerError(1002,"抱歉，新增出库出错啦，请联系技术人员");
        }
        return data;
    }

    /**
     * 编辑出库
     * @param outbound
     * @param applyDetails
     * @return
     */
    @RequestMapping("/editOutStock")
    @ResponseBody
    public ResponseData editOutStock(Outbound outbound,@RequestParam(value = "applyDetails[]")List<String> applyDetails){
        ResponseData data = ResponseData.ok();
        try {
            inventoryStockService.editOutStock(outbound,applyDetails);
            data.putDataValue("message","操作成功");
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
            ResponseData.customerError(1002,"抱歉，编辑出库出错啦，请联系技术人员");
        }
        return data;
    }

    /**
     * 自动生成入库编号
     * @return
     */
    @RequestMapping("/getOutboundCode")
    @ResponseBody
    public ResponseData getOutboundCode(){
        ResponseData data = ResponseData.ok();
        String code = inventoryStockService.getOutboundCode();
        data.putDataValue("code",code);
        return data;
    }

    /**
     * 自动生成出库编号
     * @return
     */
    @RequestMapping("/getOutStockCode")
    @ResponseBody
    public ResponseData getOutStockCode(){
        ResponseData data = ResponseData.ok();
        String code = inventoryStockService.getOutStockCode();
        data.putDataValue("code",code);
        return data;
    }

    /**
     * 自动生成库存编号
     * @return
     */
    @RequestMapping("/getInventoryCode")
    @ResponseBody
    public ResponseData getInventoryCode(){
        ResponseData data = ResponseData.ok();
        String code = inventoryStockService.getInventoryCode();
        data.putDataValue("code",code);
        return data;
    }

    /**
     * 入库编辑查看
     * @param id
     * @return
     */
    @RequestMapping("/viewOutbound")
    @ResponseBody
    public ResponseData viewOutbound(Integer id){
        return inventoryStockService.viewOutbound(id);
    }

    /**
     * 出库编辑查看
     * @param id
     * @return
     */
    @RequestMapping("/editAjax")
    @ResponseBody
    public ResponseData editAjax(Integer id){
        return inventoryStockService.editAjax(id);
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
            String sheetName = "物品库存批量导入模板";
            String index = UUIDUtil.get8UUID();
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + index + ".xls");
            OutputStream outputStream = response.getOutputStream();
            inventoryStockService.exportTemplate(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，物品库存批量导入模板导出时出错啦，请联系技术人员");
        }
    }

    @RequestMapping("importInventoryData")
    @ResponseBody
    public ResponseData importInventoryData(@RequestParam("filePath")String filePath){
        if(!ObjectUtils.isEmpty(filePath) && filePath.contains("statics")){
            filePath = filePath.replace("statics", "");
        }
        String fileName = config.getUploadDir() + File.separator + filePath;
        try {
            inventoryStockService.importInventoryData(fileName);
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

    @RequestMapping("/exportInventoryDetail")
    @ResponseBody
//    @Log(opType = OperateType.QUERY,module = "库存管理/库存导出", note = "导出产品库存信息")
    public void exportInventoryDetail(HttpServletResponse response, @RequestParam Map map){
        try{
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("产品库存信息"+UUIDUtil.get8UUID()+".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            inventoryStockService.exportInventoryDetail(map,outputStream);
        }catch (Exception e){
            log.error("导出库存信息失败",e);
        }
    }

    /**
     * 物品入库选择采购订单时，显示物品采购明细信息
     * @param map
     * @return
     */
    @PostMapping("/warehousingDetail")
    @ResponseBody
    public ResponseData warehousingDetail(@RequestParam Map<String,Object> map){
        Integer id= Integer.parseInt(map.get("id").toString());
        return inventoryStockService.warehousingDetail(id);
    }

    /**
     * 物品出库选择领用订单时，显示物品领用明细信息
     * @param id
     * @return
     */
    @PostMapping("/applyDetailsData")
    @ResponseBody
    public ResponseData applyDetailsData(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        try {
            List<Map<String,Object>> list = inventoryStockService.applyDetailsData(id);
            data.putDataValue("list",list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，物品出库选择领用订单出错啦，请联系技术人员");
        }
    }

    /**
     * 采购单失效
     * @param outbound
     * @return
     */
    @PostMapping("/lossEffect")
    @ResponseBody
    public ResponseData lossEffect(Outbound outbound){
        ResponseData data = ResponseData.ok();
        try {
            inventoryStockService.lossEffect(outbound);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，采购单失效出错啦，请联系技术人员");
        }
    }
}
