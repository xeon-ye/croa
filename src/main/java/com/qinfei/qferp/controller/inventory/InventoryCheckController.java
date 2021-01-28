package com.qinfei.qferp.controller.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.inventoryStock.InventoryCheck;
import com.qinfei.qferp.service.inventory.IInventoryCheckService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * (InventoryCheck)表控制层
 *
 * @author tsf
 * @since 2020-06-05 11:09:37
 */
@Controller
@RequestMapping("/inventoryCheck")
public class InventoryCheckController {
    @Autowired
    private IInventoryCheckService inventoryCheckService;
    @Autowired
    private Config config;

    /**
     * 查询库存盘点分页数量
     * @param map
     */
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map){
        try {
            ResponseData data=ResponseData.ok();
            Integer total=inventoryCheckService.getPageCount(map);
            data.putDataValue("total",total);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取库存盘点分页数量出错啦，请联系技术人员");
        }
    }

    /**
     * 查询分页库存盘点数据
     * @param map
     * @param pageable
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, Pageable pageable){
        try {
            return inventoryCheckService.listPg(map,pageable);
        } catch (Exception e) {
            throw new QinFeiException(1002,"获取库存盘点分页数据出错啦");
        }
    }

    /**
     * 获取库存盘点编号
     * @return
     */
    @RequestMapping("/getStockCheckCode")
    @ResponseBody
    public ResponseData getStockCheckCode(){
        try {
            ResponseData data=ResponseData.ok();
            String code=inventoryCheckService.getStockCheckCode();
            data.putDataValue("code",code);
            return data;
        } catch (Exception e) {
            throw new QinFeiException(1002,"获取库存盘点编号出错啦");
        }
    }
    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }
    /**
     * 新增库存盘点数据
     * @param inventoryCheck 实例对象
     */
    @RequestMapping(value="/saveInventoryCheck",produces="text/html;charset=utf-8")
    @ResponseBody
    public ResponseData saveInventoryCheck(InventoryCheck inventoryCheck,@RequestParam(value="goodsId", required = false) List<Integer> goodsId,
                                           @RequestParam(value="stockAmount", required = false) List<Integer> stockAmount,
                                           @RequestParam(value="checkAmount", required = false) List<Integer> checkAmount,
                                           @RequestParam(value="profitAmount", required = false) List<Integer> profitAmount,
                                           @RequestParam(value="lossAmount", required = false) List<Integer> lossAmount,
                                           @RequestParam(value="remark2", required = false) List<String> remark2,
                                           @RequestParam(value = "file", required = false) MultipartFile multipartFile){
        try {
            ResponseData data = ResponseData.ok();
            if (multipartFile!=null && multipartFile.getSize() > 0) {
                String temp = multipartFile.getOriginalFilename();
                String ext = null;
                if (temp.indexOf(".") > -1) {
                    ext = temp.substring(temp.lastIndexOf("."));
                }
                String fileName = UUIDUtil.get32UUID() + ext;
                String childPath =getStringData()+ "/inventoryCheck/checkDetails/";
                File destFile = new File(config.getUploadDir() + childPath + fileName);
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                multipartFile.transferTo(destFile);
                inventoryCheck.setAffixName(multipartFile.getOriginalFilename());
                inventoryCheck.setAffixLink(config.getWebDir()+childPath+fileName);
            }
            data.putDataValue("message","操作成功");
            InventoryCheck entity = inventoryCheckService.saveInventoryCheck(inventoryCheck,goodsId,stockAmount,checkAmount,profitAmount,lossAmount,remark2);
            data.putDataValue("entity",entity);
            return data;
        } catch (IOException e) {
            return ResponseData.customerError(1002,"很抱歉，新增盘点单时文件流出错啦，请联系技术人员！");
        }catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，新增盘点单出错啦，请联系技术人员！");
        }

    }

    /**
     * 修改库存盘点数据
     * @param inventoryCheck 实例对象
     */
    @RequestMapping("/updateInventoryCheck")
    @ResponseBody
    public ResponseData updateInventoryCheck(InventoryCheck inventoryCheck) {
        try {
            ResponseData data=ResponseData.ok();
            data.putDataValue("message","操作成功");
            inventoryCheckService.updateInventoryCheck(inventoryCheck);
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，修改库存盘点出错啦，请联系技术人员");
        }
    }

    /**
     * 删除库存盘点
     * @param id 实例对象
     */
    @RequestMapping("/delInventoryCheck")
    @ResponseBody
    public ResponseData delInventoryCheck(@RequestParam("id")Integer id) {
        try {
            ResponseData data=ResponseData.ok();
            data.putDataValue("message","操作成功");
            inventoryCheckService.delInventoryCheck(id);
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，删除库存盘点出错啦，请联系技术人员");
        }
    }

    /**
     * 查看库存盘点数据
     * @param id
     */
    @RequestMapping("/editAjax")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        try {
            ResponseData data=ResponseData.ok();
            InventoryCheck entity=inventoryCheckService.editAjax(id);
            data.putDataValue("entity",entity);
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            return ResponseData.customerError(1002,"抱歉，库存盘点查看出错啦，请联系技术人员");
        }
    }

    /**
     * 导出库存盘点
     * @param response
     */
    @RequestMapping("/exportStockCheck")
    @ResponseBody
    public void exportStockCheck(@RequestParam Map map, HttpServletResponse response){
        try {
            try {
                response.setContentType("application/binary;charset=UTF-8");
                response.setHeader("Content-Disposition","attachment;fileName="+ URLEncoder.encode("库存盘点"+ UUIDUtil.get8UUID()+".xls","UTF-8"));
                OutputStream outputStream=response.getOutputStream();
                inventoryCheckService.exportStockCheck(map,outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，导出库存盘点数据出错啦，请联系技术人员");
        }
    }
}