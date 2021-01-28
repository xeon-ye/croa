package com.qinfei.qferp.controller.inventory;
import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.inventory.Purchase;
import com.qinfei.qferp.entity.inventory.PurchaseDetails;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.service.inventory.IPurchaseService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.FileUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 物品采购控制器
 * @author tsf
 */
@Slf4j
@Controller
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private IPurchaseService purchaseService;
    @Autowired
    private Config config;

    //获取分页信息
    @RequestMapping("/getPageInfo")
    @ResponseBody
    public PageInfo<Map> getPageInfo(@RequestParam Map map,Pageable pageable){
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        if (roles == null || roles.size() == 0) {
            ResponseData.customerError(1002,"未查询到角色信息");
        } else {
            map.put("roleType", roles.get(0).getType());
            map.put("roleCode", roles.get(0).getCode());
            map.put("user", user);
        }
        return purchaseService.getPurchasePage(map,pageable);
    }

    //获取分页数据个数
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map){
        try {
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                ResponseData.customerError(1002,"未查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
                Integer number = purchaseService.getPageCount(map);
                data.putDataValue("total",number);
            }
            return data;
        } catch (Exception e) {
            return ResponseData.customerError(1002,"很抱歉,物品采购获取分页数量出错啦,请联系技术人员!");
        }
    }

    //根据id查询物品采购记录
    @RequestMapping("/editAjax")
    @ResponseBody
    public ResponseData editAjax(@RequestParam("id") Integer id){
        try {
            ResponseData data = ResponseData.ok();
            Purchase purchase = purchaseService.getById(id);
            data.putDataValue("entity",purchase);
            return data;
        } catch (Exception e) {
            return ResponseData.customerError(1002,"很抱歉，编辑物品采购出错啦,请联系技术人员!");
        }
    }
    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }

    /**
     * 新增物品采购信息
     * @param purchase 物品采购信息
     * @param type 产品分类id
     * @param goodsId 产品id
     * @param specs 规格
     * @param unit 单位
     * @param amount 数量
     * @param price 单价
     * @param totalMoney 总金额
     * @param payMethod 支付方式
     * @param supplierId 供应商id
     * @param multipartFiles 附件
     * @return
     */
    @RequestMapping(value="/savePurchase",produces="text/html;charset=utf-8")
    @ResponseBody
    public ResponseData savePurchase(Purchase purchase, @RequestParam(value="type", required = false) List<Integer> type,
                                     @RequestParam(value="goodsId", required = false) List<Integer> goodsId,
                                     @RequestParam(value="specs", required = false) List<String> specs,
                                     @RequestParam(value="unit", required = false) List<String> unit,
                                     @RequestParam(value="amount", required = false) List<Integer> amount,
                                     @RequestParam(value="price", required = false) List<Double> price,
                                     @RequestParam(value="totalMoney", required = false) List<Double> totalMoney,
                                     @RequestParam(value="supplierId", required = false) List<Integer> supplierId,
                                     @RequestParam(value="payMethod", required = false) List<Integer> payMethod,
                                     @RequestParam(value = "file", required = false) MultipartFile [] multipartFiles){
        try {
            ResponseData data = ResponseData.ok();
            List<String> picNames = new ArrayList<>();
            List<String> picPaths = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                if (multipartFile.getSize() > 0) {
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/fee/borrow/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                }
            }
            purchase.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
            purchase.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));            Purchase entity = purchaseService.savePurchase(purchase,type,goodsId,specs,unit,amount,price,totalMoney,supplierId,payMethod);
            data.putDataValue("entity",entity);
            data.putDataValue("message","操作成功");
            return data;
        } catch (IOException e) {
            return ResponseData.customerError(1002,"很抱歉，新增物品采购时文件流出错啦，请联系技术人员！");
        }catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，新增物品采购出错啦，请联系技术人员！");
        }
    }

    /**
     * 编辑物品采购信息
     * @param purchase 物品采购信息
     * @param type 产品分类id
     * @param goodsId 产品id
     * @param specs 规格
     * @param unit 单位
     * @param amount 数量
     * @param price 单价
     * @param totalMoney 总金额
     * @param supplierId 供应商id
     * @param payMethod 支付方式
     * @param multipartFiles 附件
     * @return
     */
    @RequestMapping(value="/editPurchase",produces="text/html;charset=utf-8")
    @ResponseBody
    public ResponseData editPurchase(Purchase purchase,@RequestParam(value="type", required = false) List<Integer> type,
                                     @RequestParam(value="goodsId", required = false) List<Integer> goodsId,
                                     @RequestParam(value="specs", required = false) List<String> specs,
                                     @RequestParam(value="unit", required = false) List<String> unit,
                                     @RequestParam(value="amount", required = false) List<Integer> amount,
                                     @RequestParam(value="price", required = false) List<Double> price,
                                     @RequestParam(value="totalMoney", required = false) List<Double> totalMoney,
                                     @RequestParam(value="supplierId", required = false) List<Integer> supplierId,
                                     @RequestParam(value="payMethod", required = false) List<Integer> payMethod,
                                     @RequestParam(value = "file", required = false) MultipartFile [] multipartFiles){
        try {
            //通过选中的记录的id，查询数据库，并判定该记录的状态是否可被删除
            ResponseData data = ResponseData.ok();
            Purchase old = purchaseService.getById(purchase.getId());
            if(old.getState()==IConst.STATE_SAVE || old.getState()==IConst.STATE_REJECT){
                //判断前台是否传过来文件，若为有，给予保存信息
                dealAffix(purchase, old, multipartFiles);
                Purchase entity = purchaseService.editPurchase(purchase,type,goodsId,specs,unit,amount,price,totalMoney,supplierId,payMethod);
                data.putDataValue("entity",entity);
                data.putDataValue("message","操作成功");
                return data;
            }else{
                return ResponseData.customerError(1002,"当前状态不支持修改");
            }
        } catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，修改物品采购出错啦，请联系技术人员！");
        }
    }

    private Purchase dealAffix(Purchase entity, Purchase old, MultipartFile[] multipartFiles) {
        try {
            List<String> picNames = new ArrayList<>();
            List<String> picPaths = new ArrayList<>();
            //附件处理逻辑：1、如果取得的multipartFiles.length>1,那么一定是上传了多个新附件，直接使用二进制存储
            //2、如果multipartFiles.length=1，那么可能没有上传附件，也可能上传了一个附件
            //3、如果上传了一个附件，multipartFile.getSize()=1,二进制存储
            //4、如果没有上传新附件，直接把old中的附件拿过来
            if (multipartFiles.length > 1) {//表示上传了新附件
                for (MultipartFile multipartFile : multipartFiles) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath =getStringData()+ "/fee/borrow/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        picNames.add(multipartFile.getOriginalFilename());
                        picPaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            } else {
                MultipartFile multipartFile = multipartFiles[0];
                if (multipartFile.getSize() > 0) {//表示上传了新附件
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/fee/borrow/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                    entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                    entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
                } else {//表示附件没有变化
                    entity.setAffixName(old.getAffixName());
                    entity.setAffixLink(old.getAffixLink());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }


    //删除产品时判断是否在采购明细中被使用
    @RequestMapping("/getPurchaseDetailsByGoodsId")
    @ResponseBody
    public ResponseData getPurchaseDetailsByGoodsId(@RequestParam("id") Integer id){
        try {
            ResponseData data = ResponseData.ok();
            Integer result = purchaseService.getPurchaseDetailsByGoodsId(id);
            data.putDataValue("result",result);
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，此产品已被使用，不能删除");
        }
    }

    //删除物品采购信息
    @RequestMapping("/delPurchase")
    @ResponseBody
    public ResponseData delPurchase(@RequestParam("id") Integer id){

        try {
            purchaseService.delPurchase(id);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"很抱歉，删除物品采购出错啦，请联系技术人员！");
        }
    }

    //生成物品采购编号
    @RequestMapping("/getPurchaseCode")
    @ResponseBody
    public ResponseData getPurchaseCode(){
        try {
            String code = purchaseService.getPurchaseCode();
            ResponseData data = ResponseData.ok();
            data.putDataValue("code",code);
            return data;
        }catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，自动生成物品采购编号出错啦，请联系技术人员！");
        }
    }

    /**
     * 根据供应商id查询采购记录
     * @param supplierId
     * @return
     */
    @RequestMapping("/getPurchaseBySupplierId")
    @ResponseBody
    public ResponseData getPurchaseBySupplierId(@RequestParam("supplierId")Integer supplierId){
        Integer number = null;
        try {
            number = purchaseService.getPurchaseBySupplierId(supplierId);
            ResponseData data = ResponseData.ok();
            data.putDataValue("number",number);
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            return ResponseData.customerError(1002,"很抱歉，根据供应商id查询采购信息出错啦，请联系技术人员！");
        }
    }

    /**
     * 导出供应商信息
     */
    @RequestMapping("/exportPurchaseDetail")
//    @Log(opType = OperateType.QUERY, module = "物品采购管理/导出物品采购", note = "导出物品采购信息")
    public void exportPurchaseDetail(HttpServletResponse response, @RequestParam Map map){
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("companyCode",user.getCompanyCode()) ;
            }
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("物品采购信息.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            purchaseService.exportPurchaseDetail(map,outputStream);
        } catch (Exception e) {
            log.error("导出物品采购信息失败", e);
        }
    }

    /**
     * 查询未入库并且审核通过的采购订单
     *
     */
    @PostMapping("/orderList")
    @ResponseBody
    public PageInfo<Map> orderList(@RequestParam Map<String, Object> map,Pageable pageable){
       return purchaseService.orderList(map,pageable);
    }

    /**
     * 查询未入库并且审核通过的采购订单和编辑本身的采购订单
     *
     */
    @PostMapping("/orderList2")
    @ResponseBody
    public PageInfo<Map> orderList2(@RequestParam Map<String, Object> map,Pageable pageable){
        return purchaseService.orderList2(map,pageable);
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
            String sheetName = "物品采购批量导入模板";
            String index = UUIDUtil.get8UUID();
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + index + ".xls");
            OutputStream outputStream = response.getOutputStream();
            purchaseService.getDataImportTemplate(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，物品采购批量导入模板导出时出错啦，请联系技术人员");
        }
    }

    @RequestMapping("/importPurchaseData")
    @ResponseBody
    public ResponseData importPurchaseData(@RequestParam("filePath")String filePath){
        if(!ObjectUtils.isEmpty(filePath) && filePath.contains("statics")){
            filePath = filePath.replace("statics", "");
        }
        String fileName = config.getUploadDir() + File.separator + filePath;
        try {
            purchaseService.importPurchaseData(fileName);
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
     * 添加待办
     * @param userId
     * @param purchaseId
     * @param title
     * @return
     */
    @RequestMapping("/addItem")
    @ResponseBody
    public Items addItem(@RequestParam("userId") Integer userId, @RequestParam("purchaseId") Integer purchaseId,@RequestParam("title")String title){
        return purchaseService.addItem(userId,purchaseId,title);
    }

    /**
     * 财务确认抄送
     * @param itemId
     */
    @RequestMapping("/purchaseConfirm")
    @ResponseBody
    public ResponseData purchaseConfirm(@RequestParam("itemId") Integer itemId){
        purchaseService.confirm(itemId);
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","操作成功");
        return data ;
    }

    /**
     * 采购关联报销
     * @param ids
     */
    @RequestMapping("/relatedReimbursement")
    @ResponseBody
    public ResponseData relatedReimbursement(@RequestParam("ids")String ids){
        ResponseData data = ResponseData.ok();
        if(!StringUtils.isEmpty(ids)){
            List<String> arrayList = Arrays.asList(ids.split(","));
            List<Map> list=purchaseService.relatedReimbursement(arrayList);
            data.putDataValue("list",list);
        }else{
            return ResponseData.customerError(1002,"找不到相关采购记录");
        }
        return data ;
    }

    /**
     * 判断选中的采购是否关联报销
     * @param ids
     */
    @RequestMapping("/checkRelatedReimbursement")
    @ResponseBody
    public ResponseData checkRelatedReimbursement(@RequestParam("ids[]")List<String> ids){
        ResponseData data = ResponseData.ok();
        if(!StringUtils.isEmpty(ids)){
            List<Map> list=purchaseService.checkRelatedReimbursement(ids);
            data.putDataValue("list",list);
        }else{
            return ResponseData.customerError(1002,"找不到相关采购记录");
        }
        return data ;
    }
}
