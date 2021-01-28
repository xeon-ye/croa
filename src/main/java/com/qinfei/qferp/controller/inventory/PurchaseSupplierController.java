package com.qinfei.qferp.controller.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.inventory.PurchaseSupplier;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.inventory.IPurchaseSupplierService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DataImportUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/purchaseSupplier")
public class PurchaseSupplierController {
    @Autowired
    private IPurchaseSupplierService purchaseSupplierService;

    /**
     * 根据id查询物品供应商信息
     * @param id
     * @return
     */
    @RequestMapping("/getById")
    @ResponseBody
    public ResponseData getById(@RequestParam("id") Integer id){
        ResponseData data = ResponseData.ok();
        PurchaseSupplier purchaseSupplier = purchaseSupplierService.getById(id);
        data.putDataValue("entity",purchaseSupplier);
        return data;
    }

    /**
     * 获取分页数据的总数
     * @param map
     * @return
     */
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        Integer number = purchaseSupplierService.getPurchaseSupplierCount(map);
        data.putDataValue("total",number);
        return data;
    }

    /**
     * 获取分页数据
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, Pageable pageable){
        return purchaseSupplierService.getPurchaseSupplierInfo(map,pageable);
    }

    /**
     * 新增供应商信息
     * @param purchaseSupplier
     * @return
     */
    @RequestMapping("/add")
    @ResponseBody
    public ResponseData add(PurchaseSupplier purchaseSupplier){
        ResponseData data = ResponseData.ok();
        purchaseSupplierService.savePurchaseSupplier(purchaseSupplier);
        data.putDataValue("message","操作成功");
        return data;
    }

    /**
     * 编辑供应商信息
     * @param purchaseSupplier
     * @return
     */
    @RequestMapping("/edit")
    @ResponseBody
    public ResponseData edit(PurchaseSupplier purchaseSupplier){
        ResponseData data = ResponseData.ok();
        purchaseSupplierService.editPurchaseSupplier(purchaseSupplier);
        data.putDataValue("message","操作成功");
        return data;
    }

    /**
     * 删除供应商信息
     * @param id
     * @return
     */
    @RequestMapping("/delPurchaseSupplier")
    @ResponseBody
    public ResponseData delPurchaseSupplier(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        purchaseSupplierService.delPurchaseSupplier(id);
        data.putDataValue("message","操作成功");
        return data;
    }

    /**
     * 获取采购下拉框所需供应商信息
     * @return
     */
    @RequestMapping("/getSupplierList")
    @ResponseBody
    public ResponseData getSupplierList(){
        ResponseData data = ResponseData.ok();
        List<Map> list = purchaseSupplierService.getPurchaseSupplierList();
        data.putDataValue("list",list);
        data.putDataValue("number",list.size());
        return data;
    }

    /**
     * 获取采购下拉框所需供应商信息
     * @return
     */
    @RequestMapping("/getPurchaseSupplierByName")
    @ResponseBody
    public ResponseData getPurchaseSupplierByName(@RequestParam("name")String name,@RequestParam(value = "id",required = false)Integer id){
        ResponseData data = ResponseData.ok();
        Map map = new HashMap();
        map.put("name",name);
        map.put("id",id);
        map.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
        List<PurchaseSupplier> list = purchaseSupplierService.getPurchaseSupplierByName(map);
        data.putDataValue("number",list.size());
        return data;
    }

    /**
     * 导出供应商信息
     */
    @RequestMapping("/exportPurchaseSupplier")
//    @Log(opType = OperateType.QUERY, module = "物品供应商管理/导出物品供应商", note = "导出物品供应商信息")
    public void exportPurchaseSupplier(HttpServletResponse response,@RequestParam Map map){
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
            }
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("物品供应商信息.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            purchaseSupplierService.exportPurchaseSupplier(map,outputStream);
        } catch (Exception e) {
            log.error("导出物品供应商信息失败", e);
        }
    }

    /**
     * 获取导入模板
     * @param response
     */
    @RequestMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request,HttpServletResponse response){
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = "供应商批量导入模板";
            String index = UUIDUtil.get8UUID();
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + index + ".xls");
            OutputStream outputStream = response.getOutputStream();
            purchaseSupplierService.exportTemplate(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量导入供应商
     * @param multipartFile
     */
    @RequestMapping("/batchSupplierForEasyExcel")
    @ResponseBody
    public ResponseData batchSupplierForEasyExcel(@RequestParam(value = "file") MultipartFile multipartFile){
        try{
            String fileName = multipartFile.getOriginalFilename();
            if(fileName.indexOf(".xls")>-1){
                purchaseSupplierService.importPurchaseSupplierData(multipartFile);
                return ResponseData.ok().putDataValue("message","导入成功");
            }else{
                return ResponseData.customerError(1001,"上传的文件类型不正确！") ;
            }
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,"产品供应商导入失败") ;
        }
    }
}
