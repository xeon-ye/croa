package com.qinfei.qferp.service.impl.inventory;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.inventory.PurchaseSupplier;
import com.qinfei.qferp.mapper.inventory.GoodsTypeMapper;
import com.qinfei.qferp.mapper.inventory.PurchaseSupplierMapper;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.impl.inventory.excelListener.PurchaseSupplierExcelListener;
import com.qinfei.qferp.service.inventory.IPurchaseSupplierService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 物品供应商接口实现类
 * @author tsf
 */
@Service
public class PurchaseSupplierService implements IPurchaseSupplierService {
    @Autowired
    private PurchaseSupplierMapper purchaseSupplierMapper;
    @Autowired
    private GoodsTypeMapper goodsTypeMapper;

    @Override
    public PurchaseSupplier getById(Integer id) {
        return purchaseSupplierMapper.getById(id);
    }

    @Override
    public Integer getPurchaseSupplierCount(Map map) {
        Integer count =0;
        try {
            map.put("companyCode", AppUtil.getUser().getCompanyCode());
            count = purchaseSupplierMapper.getPurchaseSupplierCount(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public PageInfo<Map> getPurchaseSupplierInfo(Map map, Pageable pageable) {
        PageInfo<Map> pageInfo=null;
        try {
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            map.put("companyCode", AppUtil.getUser().getCompanyCode());
            List<Map> list = purchaseSupplierMapper.getPurchaseSupplierInfo(map);
            pageInfo = new PageInfo<>(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pageInfo;
    }

    @Override
    public void savePurchaseSupplier(PurchaseSupplier purchaseSupplier) {
        try {
            //供应商编号
            String code = IConst.PURCHASE_SUPPLIER_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.PURCHASE_SUPPLIER_CODE),5);
            purchaseSupplier.setCode(code);
            purchaseSupplier.setState(0);
            purchaseSupplier.setCompanyCode(AppUtil.getUser().getCompanyCode());
            purchaseSupplier.setName(purchaseSupplier.getName().trim());
            purchaseSupplier.setCreateTime(new Date());
            Integer payWay = purchaseSupplier.getPayMethod();
            if(payWay==0){
                //支付方式为微信
                purchaseSupplier.setZhifubao("");
                purchaseSupplier.setBankNo("");
                purchaseSupplier.setBankName("");
                purchaseSupplier.setAccountName("");
                purchaseSupplier.setBankPlace("");
            }else if(payWay==1){
                //支付方式为支付宝
                purchaseSupplier.setWeixin("");
                purchaseSupplier.setBankNo("");
                purchaseSupplier.setBankName("");
                purchaseSupplier.setAccountName("");
                purchaseSupplier.setBankPlace("");
            }else if(payWay==2){
                //支付方式为银行卡
                purchaseSupplier.setWeixin("");
                purchaseSupplier.setZhifubao("");
            }
            purchaseSupplierMapper.insert(purchaseSupplier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void editPurchaseSupplier(PurchaseSupplier purchaseSupplier) {
        try {
            purchaseSupplier.setUpdateTime(new Date());
            purchaseSupplier.setUpdateUserId(AppUtil.getUser().getId());
            purchaseSupplier.setName(purchaseSupplier.getName().trim());
            Integer payWay = purchaseSupplier.getPayMethod();
            if(payWay==0){
                //支付方式为微信
                purchaseSupplier.setZhifubao("");
                purchaseSupplier.setBankNo("");
                purchaseSupplier.setBankName("");
                purchaseSupplier.setAccountName("");
                purchaseSupplier.setBankPlace("");
            }else if(payWay==1){
                //支付方式为支付宝
                purchaseSupplier.setWeixin("");
                purchaseSupplier.setBankNo("");
                purchaseSupplier.setBankName("");
                purchaseSupplier.setAccountName("");
                purchaseSupplier.setBankPlace("");
            }else if(payWay==2){
                //支付方式为银行卡
                purchaseSupplier.setWeixin("");
                purchaseSupplier.setZhifubao("");
            }
            purchaseSupplierMapper.update(purchaseSupplier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delPurchaseSupplier(Integer id) {
        try {
            purchaseSupplierMapper.delPurchaseSupplier(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PurchaseSupplier> getPurchaseSupplierByName(Map map) {
        return purchaseSupplierMapper.getPurchaseSupplierByName(map);
    }

    @Override
    public List<Map> getPurchaseSupplierList() {
        return purchaseSupplierMapper.getPurchaseSupplierList(AppUtil.getUser().getCompanyCode());
    }

    @Override
    public List<Map> exportPurchaseSupplier(Map map, OutputStream outputStream) {
        List<Map> list = purchaseSupplierMapper.getPurchaseSupplierInfo(map);
        String[] heads = {"供应商编号","产品分类", "供应商名称", "供应商资质", "支付方式", "微信","支付宝","银行账号","联系人", "联系方式","创建时间"};
        String[] fields = {"code", "typeName", "name", "level", "payMethod", "weixin","zhifubao","bankNo","contactName", "contactPhone","createTime"};
        ExcelUtil.exportExcel("物品供应商信息", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if("level".equals(field)){
                    if((int) value == 1){
                        cell.setCellValue("普通");
                    }else if((int) value == 2){
                        cell.setCellValue("中等");
                    }else {
                        cell.setCellValue("优质");
                    }
                }else if("payMethod".equals(field)){
                    if((int) value == 1){
                        cell.setCellValue("微信");
                    }else if((int) value == 2){
                        cell.setCellValue("支付宝");
                    }else {
                        cell.setCellValue("银行卡");
                    }
                }else{
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    public void exportTemplate(OutputStream outputStream) {
        //创建一个excel
        XSSFWorkbook book = new XSSFWorkbook();
        //设计表头
        XSSFSheet sheet1 = book.createSheet("sheet1");
        XSSFRow row0 = sheet1.createRow(0);
        row0.setHeightInPoints((3*sheet1.getDefaultRowHeightInPoints()));
        XSSFCellStyle cellStyle = book.createCellStyle();
        cellStyle.setWrapText(true);
        String info = "提示：1、有*号的列为必填列。\n" +
                "2、供应商资质：普通，中等，优质。\n" +
                "3、结算方式：微信，支付宝，银行卡。\n";
        info = info.trim();
        row0.createCell(0).setCellValue(info);
        row0.getCell(0).setCellStyle(cellStyle);

        CellRangeAddress region = new CellRangeAddress(0, 0, 0, 15);
        sheet1.addMergedRegion(region);
        //创建标题样式
        XSSFCellStyle headerStyle = book.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFRow row1 = sheet1.createRow(1);
        row1.setHeightInPoints(30);

        sheet1.setColumnWidth(0, 3000);
        sheet1.setColumnWidth(1, 3000);
        sheet1.setColumnWidth(2, 3000);
        sheet1.setColumnWidth(3, 5000);
        sheet1.setColumnWidth(4, 3000);
        sheet1.setColumnWidth(5, 3000);
        sheet1.setColumnWidth(6, 4000);
        sheet1.setColumnWidth(7, 2000);
        sheet1.setColumnWidth(8, 2000);
        sheet1.setColumnWidth(9, 2000);
        sheet1.setColumnWidth(10, 3000);
        sheet1.setColumnWidth(11, 3000);
        sheet1.setColumnWidth(12, 3000);
        sheet1.setColumnWidth(13, 3000);
        sheet1.setColumnWidth(14, 3000);

        row1.createCell(0).setCellValue("*供应商名称");
        row1.createCell(1).setCellValue("*供应商资质");
        row1.createCell(2).setCellValue("*产品分类");
        row1.createCell(3).setCellValue("*供应商地址");
        row1.createCell(4).setCellValue("备注");
        row1.createCell(5).setCellValue("*联系人姓名");
        row1.createCell(6).setCellValue("*联系方式");
        row1.createCell(7).setCellValue("*性别");
        row1.createCell(8).setCellValue("部门");
        row1.createCell(9).setCellValue("职位");
        row1.createCell(10).setCellValue("邮箱");
        row1.createCell(11).setCellValue("*结算方式");
        row1.createCell(12).setCellValue("微信");
        row1.createCell(13).setCellValue("支付宝");
        row1.createCell(14).setCellValue("银行账号");

        for (int i = 0; i < 15; i++) {
            row1.getCell(i).setCellStyle(headerStyle);
        }
        try {
            book.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    public void importPurchaseSupplierData(MultipartFile multipartFile) {
        PurchaseSupplierExcelListener listener = new PurchaseSupplierExcelListener(purchaseSupplierMapper,goodsTypeMapper);
        try {
            EasyExcelFactory.readBySax(new BufferedInputStream(multipartFile.getInputStream()),
                    new Sheet(1,2,PurchaseSupplier.class),listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
