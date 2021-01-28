package com.qinfei.qferp.service.impl.inventory;

import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventory.GoodsType;
import com.qinfei.qferp.entity.inventoryStock.InventoryCheckDetails;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.GoodsTypeMapper;
import com.qinfei.qferp.mapper.inventory.InventoryCheckDetailsMapper;
import com.qinfei.qferp.service.inventory.IInventoryCheckDetailsService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * InventoryCheckDetails实现类
 *
 * @author tsf
 * @since 2020-06-02 16:18:51
 */
@Service
public class InventoryCheckDetailsService implements IInventoryCheckDetailsService {
    @Autowired
    private InventoryCheckDetailsMapper checkDetailsMapper;
    @Autowired
    private GoodsTypeMapper goodsTypeMapper;
    @Autowired
    private Config config;

    private static List<String> stockCheckTitle=Arrays.asList("*产品名称*","库存数量","*盘点数量*","盘盈数量","盘亏数量","库存最大值","库存最小值");
    static List<String> notices= new ArrayList<>();
    static{
        notices.add("有*号的列为必填列。");
        notices.add("产品名称必须是系统中存在的");
        notices.add("库存数量由系统计算不能修改");
    }

    @Override
    public Integer getPageCount(Map map) {
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        return checkDetailsMapper.getPageCount(map);
    }

    @Override
    public PageInfo<Map> listPg(Map map, Pageable pageable) {
        String companyCode=AppUtil.getUser().getCompanyCode();
        map.put("companyCode",companyCode);
        //获取不同库存的商品id集合
        List<Integer> list=checkDetailsMapper.getStockIds(map);
        map.put("list",list);
        List<Map> maps= new ArrayList<>();
        if(!CollectionUtils.isEmpty(list)){
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            maps=getStockData(map);
        }
        return new PageInfo<>(maps);
    }

    @Override
    public List<Map> exportForeWarning(Map map, OutputStream outputStream) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            String companyCode = user.getCompanyCode();
            map.put("companyCode",companyCode);
            List<Integer> list=checkDetailsMapper.getStockIds(map);
            map.put("list",list);
            List<Map> maps= new ArrayList<>();
            if(!CollectionUtils.isEmpty(list)){
                maps=getStockData(map);
            }
            List<Map> typeList = goodsTypeMapper.loadGoodsTypeInfo(companyCode);
            String[] heads = {"产品编号","产品分类","产品名称","规格","单位","库存数量","库存最大值","库存最小值"};
            String[] fields= {"code","typeId","goodsName","specs","unit","amount","stockMaxAmount","stockMinAmount"};
            ExcelUtil.exportExcel("库存盘点信息", heads, fields, maps, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if(value!=null){
                    if("typeId".equals(field)){
                        String typeId = value.toString();
                        for(int i=0;i<typeList.size();i++){
                            Map obj =  typeList.get(i);
                            String id = obj.get("id").toString();
                            String name = obj.get("name").toString();
                            if(typeId.equals(id)){
                                cell.setCellValue(name);
                                break;
                            }
                        }
                    }else if("amount".equals(field)){
                        Integer amount=Integer.valueOf(value.toString());
                        cell.setCellValue(amount);
                    }
                    else if("stockMaxAmount".equals(field)){
                        Integer stockMaxAmount=Integer.valueOf(value.toString());
                        cell.setCellValue(stockMaxAmount);
                    }
                    else if("stockMinAmount".equals(field)){
                        Integer stockMinAmount=Integer.valueOf(value.toString());
                        cell.setCellValue(stockMinAmount);
                    }else {
                        cell.setCellValue(value.toString());
                    }
                }else{
                    if("specs".equals(field)){
                        cell.setCellValue("");
                    }else {
                        cell.setCellValue(0);
                    }
                }
            });
            return maps;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，物品库存盘点导出出错啦，请联系技术人员");
        }finally {
            try {
                if(outputStream!=null){
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载导入模板
     * @param outputStream
     */
    @Override
    public void exportTemplate(OutputStream outputStream) {
        List<Map<String,Object>> sheetInfo =new ArrayList<>();
        Map<String,Object> stockCheckSheet = new HashMap<>();
        List<String> stockTitles = new ArrayList<>();
        stockTitles.addAll(stockCheckTitle);
        stockCheckSheet.put("templateName","库存盘点导入");
        stockCheckSheet.put("rowTitles",stockTitles);
        stockCheckSheet.put("notices",notices);
        sheetInfo.add(stockCheckSheet);
        DataImportUtil.createMoreSheetFile(sheetInfo,outputStream);
    }

    @Override
    public String importInventoryCheckData(String fileName) {
       File file=new File(fileName);
       String result=null;
       if(!file.exists()){
           file.mkdirs();
       }
       if(file.exists()){
           Map<String,Object> checkResult = handleStockCheckData(file,0);
           List<Map<String, Object>> sheetInfo = new ArrayList<>();
           //打印出错的信息，如果产品库存无误正常导入
           if (checkResult != null && checkResult.size() > 0) {
               sheetInfo.add(checkResult);
           }
           if (sheetInfo.size() > 0) {
               result = DataImportUtil.createMoreSheetFile("产品库存导入失败内容", sheetInfo, config.getUploadDir(), config.getWebDir());
           }
           if(result!=null){
               throw new QinFeiException(1003,result);//如果有内容错误，则抛出异常，事务失效，防止一次导入多个sheet表时，前面sheet数据正常而导致数据入库
           }
       }else{
           throw new QinFeiException(1002,"导入文件不存在");
       }
           return result;
    }

    //处理库存盘点数据
    private Map<String,Object> handleStockCheckData(File file,Integer type){
        List<Object[]> execlContent = null;
        Map<String,Object> handResult = null;
        //产品库存
        execlContent = EasyExcelUtil.getExcelContent(file,type+1,3,2);
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(execlContent)){
            handResult = dealInventoryCheckData(AppUtil.getUser(),execlContent);
        }
        return handResult;
    }

    private Map<String,Object> dealInventoryCheckData(User user,List<Object[]> excelContent){
        List<String> rowTitles = new ArrayList<>();//导入模板总列数
        rowTitles.addAll(stockCheckTitle);
        int totalColumnNum = rowTitles.size();//总列数
        int rowNum = excelContent.size();//导入库存盘点行数
        List<Object[]> validErrorData = new ArrayList<>(); //用于保存校验未通过的数据；
        Map<String,Object> errorMap = new HashMap<>();
        List<InventoryCheckDetails> list = new ArrayList<>();
        Object [] row = null;
        Boolean flag = true;
        for (int i =0; i<rowNum;i++){
            row = excelContent.get(i);
            if (row.length<=1){
                continue;
            }
            if (row.length!=totalColumnNum){
                row = Arrays.copyOf(row,row.length+1);
                row[row.length-1]="模板格式不对应,请核实模板是否一致";
                validErrorData.add(row);
                flag= false;
                break;
            }
            boolean isValidSuccess= true;
            InventoryCheckDetails currentCheckData=new InventoryCheckDetails();
            String companyCode = user.getCompanyCode();
            for (int j = 0;j<row.length ;j++){
                String columnValue = String.valueOf(row[j]);
                boolean requiredFlag = validField(rowTitles,j);
                if (columnValue == null || "".equals(columnValue) && !requiredFlag){
                    columnValue="";
                }
                String errorInfo = null;
                if (!StringUtils.isEmpty(columnValue)){
                    if (j==0){
                        //产品名称
                        Map map = new HashMap();
                        map.put("name", columnValue);
                        map.put("companyCode", companyCode);
                        List<GoodsType> goodsList = goodsTypeMapper.getSameNameList(map);
                        if (CollectionUtils.isEmpty(goodsList)) {
                            currentCheckData.setGoodsId(goodsList.get(0).getId());
                        } else {
                            errorInfo="格式不对";
                        }
                    }else if (j==1){
                        //库存数量
                        try {
                            Integer stockAmount=Integer.valueOf(columnValue);
                            currentCheckData.setStockAmount(stockAmount);
                            if(stockAmount<0){
                                errorInfo="必须大于0";
                            }
                        } catch (NumberFormatException e) {
                            errorInfo="格式不对";
                        }
                    }else if(j==2){
                        //盘点数量
                        try {
                            Integer checkAmount=Integer.valueOf(columnValue);
                            currentCheckData.setCheckAmount(checkAmount);
                            if(checkAmount<0){
                                errorInfo="必须大于0";
                            }
                        } catch (NumberFormatException e) {
                            errorInfo="格式不对";
                        }
                    }else if(j==3){
                        //盘盈数量
                        try {
                            Integer profitAmount=Integer.valueOf(columnValue);
                            currentCheckData.setProfitAmount(profitAmount);
                            if(profitAmount<0){
                                errorInfo="必须大于0";
                            }
                        } catch (NumberFormatException e) {
                            errorInfo="格式不对";
                        }
                    }else if(j==4){
                        //盘亏数量
                        try {
                            Integer lossAmount=Integer.valueOf(columnValue);
                            currentCheckData.setLossAmount(lossAmount);
                            if(lossAmount<0){
                                errorInfo="必须大于0";
                            }
                        } catch (NumberFormatException e) {
                            errorInfo="格式不对";
                        }
                    }else if (j==5){
                        //库存最大值
                        try {
                            Integer stockMaxAmount=Integer.valueOf(columnValue);
                            if(stockMaxAmount<0){
                                errorInfo="必须大于0";
                            }
                        } catch (NumberFormatException e) {
                            errorInfo="格式不对";
                        }

                    }else if (j==6){
                        //库存最小值
                        try {
                            Integer stockMinAmount=Integer.valueOf(columnValue);
//                            currentCheckData.setStockMinAmount(stockMinAmount);
                            if(stockMinAmount<0){
                                errorInfo="必须大于0";
                            }
                        } catch (NumberFormatException e) {
                            errorInfo="格式不对";
                        }
                    }
                }else {
                    if (requiredFlag) {
                        errorInfo="不能为空";
                    }
                }
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(errorInfo)){
                    row = Arrays.copyOf(row, row.length + 1);
                    if (rowTitles.get(j).matches("^\\*.+\\*$")) { //如果是必输字段，则去除开头和结尾的*
                        row[row.length - 1] = rowTitles.get(j).substring(1, rowTitles.get(j).length() - 1) + errorInfo;
                    } else {
                        row[row.length - 1] = rowTitles.get(j) + errorInfo;
                    }
                    validErrorData.add(row); //缓存校验未通过数据
                    isValidSuccess = false;
                    break; //直接进入下一个采购数据判断
                }

            }
            if (!isValidSuccess) {//数据校验不成功继续下一个产品库存
                continue;
            }
            list.add(currentCheckData);
        }
        //判断是否存在有效数据
        //如果有错误，则数据不入库，提示错误信息
        int errorSize = validErrorData.size();
        if (errorSize >0){
            //导入失败原因
            rowTitles.add("失败原因");
            errorMap.put("templateName","库存盘点导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorData);
        }
        int successSize = list.size();
        if (successSize >0){
            //添加产品盘点
            List<InventoryCheckDetails> checkRecordList=new ArrayList<>();
            for(InventoryCheckDetails obj :list){
                InventoryCheckDetails checkRecord=new InventoryCheckDetails();
                checkRecord.setGoodsId(obj.getGoodsId());
                checkRecord.setStockAmount(obj.getStockAmount());
                checkRecord.setCheckAmount(obj.getCheckAmount());
                checkRecord.setProfitAmount(obj.getProfitAmount());
                checkRecord.setLossAmount(obj.getLossAmount());
                checkRecord.setCreateId(user.getId());
                checkRecord.setCreateName(user.getName());
                checkRecord.setCreateTime(new Date());
                checkRecord.setUpdateUserId(user.getId());
                checkRecord.setUpdateTime(new Date());
                checkRecordList.add(checkRecord);
            }
            checkDetailsMapper.addCheckDetailsBatch(checkRecordList);
        }
        if(flag==false){
            rowTitles.add("失败原因");
            errorMap.put("templateName","库存盘点导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorData);
        }
        return errorMap;
    }

    /**
     * 判断指定列值是否必输（根据**判断）
     * @param rowTitles 所有列名称
     * @param columnIndex 列索引
     */
    private boolean validField(List<String> rowTitles, int columnIndex){
        if(rowTitles.get(columnIndex).matches("^\\*.+\\*$")){
            return true;
        }
        return false;
    }

    //获取库存信息
    private List<Map> getStockData(Map map) {
        return checkDetailsMapper.getStockData(map);
    }

    /**
     * 新增数据
     * @param checkDetails 实例对象
     * @return 实例对象
     */
    @Transactional
    @Override
    public void saveCheckDetails(InventoryCheckDetails checkDetails) {
        try{
            User user = AppUtil.getUser();
            checkDetailsMapper.addCheckDetailsBatch(null);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "新增数据异常！");
        }   
    }

    /**
     * 修改数据
     * @param checkDetails 实例对象
     * @return 实例对象
     */
    @Transactional
    @Override
    public void editCheckDetails(InventoryCheckDetails checkDetails) {
       try{
            User user = AppUtil.getUser();
           checkDetailsMapper.editInventoryCheckDetails(checkDetails);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "编辑数据异常！");
        }  
    }
}