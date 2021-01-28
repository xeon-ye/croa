package com.qinfei.qferp.service.impl.inventory;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.GoodsMapper;
import com.qinfei.qferp.mapper.inventory.GoodsTypeMapper;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.impl.inventory.excelListener.GoodsExcelListener;
import com.qinfei.qferp.service.impl.inventory.excelModal.GoodsInfo;
import com.qinfei.qferp.service.inventory.IGoodsService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * 产品实现类
 * @author tsf
 */
@Service
public class GoodsService implements IGoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsTypeMapper goodsTypeMapper;

    @Override
    public Integer getPageCount(Map map) {
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        return goodsMapper.getPageCount(map);
    }

    @Override
    public PageInfo<Map> listPg(Map map,Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        List<Map> list=goodsMapper.listPg(map);
        PageInfo<Map> pageInfo=new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public Integer getTotalAmount(Map map) {
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        return goodsMapper.getTotalAmount(map);
    }

    @Override
    public PageInfo<Map> queryGoodsData(Map map,Pageable pageable) {
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        List<Integer> list = goodsMapper.getGoodIds(map);
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("list",list);
        if(CollectionUtils.isNotEmpty(list)){
            List<Map> mapList=goodsMapper.queryGoodsData(map);
            return new PageInfo<>(mapList);
        }else{
            return new PageInfo<>();
        }
    }

    @Override
    public Integer getStockAmountById(Map map) {
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        return goodsMapper.getStockAmountById(map);
    }

    @Override
    public PageInfo<Map> getGoodsList(Integer wareId,List<Integer> ids,Pageable pageable) {
        if(CollectionUtils.isNotEmpty(ids)){
            Map map=new HashMap();
            map.put("wareId",wareId);
            map.put("companyCode", AppUtil.getUser().getCompanyCode());
            map.put("list",ids);
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            List<Map> list =goodsMapper.getGoodsList(map);
            return new PageInfo<>(list);
        }else{
            return new PageInfo<>();
        }
    }

    @Override
    public Goods save(Goods goods) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002, "请先登录！");
            }
            goods.setId(null);
            goods.setCreateId(user.getId());
            goods.setCreateName(user.getName());
            goods.setCreateTime(new Date());
            goods.setNumber(0);
            goods.setCompanyCode(user.getDept().getCompanyCode());
            goodsMapper.saveGoods(goods);
            return goods;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，新增产品分类出错啦");
        }
    }

    @Override
    public Goods update(Goods goods) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002, "请先登录！");
            }
            goods.setUpdateUserId(user.getId());
            goodsMapper.updateGoods(goods);
            return goods;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，编辑产品分类出错啦");
        }
    }

    @Override
    public void addGoodsBatch(Goods goods) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            Integer amount =goods.getNumber();
            List<Goods> goodsList=new ArrayList<>();
            if(amount>0){
                for(int i=0;i<amount;i++){
                    Goods obj = new Goods();
                    obj.setId(null);
                    if(i==0){
                        obj.setCode(goods.getCode());
                    }else {
                        String inventoryCode = IConst.INVENTORY_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.INVENTORY_CODE),5);
                        obj.setCode(inventoryCode);
                    }
                    obj.setState(0);
                    obj.setTypeId(goods.getTypeId());
                    obj.setGoodsId(goods.getGoodsId());
                    obj.setNumber(1);
                    obj.setCreateId(goods.getCreateId());
                    obj.setCreateName(goods.getCreateName());
                    obj.setCreateTime(goods.getCreateTime());
                    obj.setUpdateTime(new Date());
                    obj.setUpdateUserId(user.getId());
                    obj.setCompanyCode(user.getCompanyCode());
                    obj.setWarehouseId(goods.getWarehouseId());
                    goodsList.add(obj);
                }
                goodsMapper.addGoodsBatch(goodsList);
            }else{
                throw new QinFeiException(1002,"库存数量必须大于0");
            }
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map getById(Integer id) {
        return goodsMapper.getById(id);
    }

    @Override
    public Goods getGoodsById(Integer id) {
        return goodsMapper.getGoodsById(id);
    }

    @Override
    public void del(Integer id) {
        User user = AppUtil.getUser();
        if(user==null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(ObjectUtils.isEmpty(goodsMapper.getGoodsById(id))){
            throw new QinFeiException(1002,"此产品信息已被删除，请刷新一下页面");
        }
        Map map=new HashMap();
        map.put("state",IConst.STATE_DELETE);
        map.put("id",id);
        map.put("updateUserId",AppUtil.getUser().getId());
        goodsMapper.editGoodsState(map);
    }

    @Override
    public void editGoodsState(Integer state, Integer id) {
        Map map=new HashMap();
        map.put("state",state);
        map.put("id",id);
        map.put("updateUserId",AppUtil.getUser().getId());
        goodsMapper.editGoodsState(map);
    }


    @Override
    public List<Goods> checkSameName(Integer id, String name) {
        try {
            List<Goods> list = goodsMapper.checkSameName(id,name,AppUtil.getUser().getDept().getCompanyCode());
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉,根据产品名称查询出错啦");
        }
    }

    @Override
    public List<Goods> getGoodsByParentId(Integer parentId) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            Map map =new HashMap();
            map.put("parentId",parentId);
            map.put("companyCode",user.getDept().getCompanyCode());
            List<Goods> list = goodsMapper.getGoodsByParentId(map);
            return list;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，根据产品分类id查询产品出错啦，请联系技术人员");
        }
    }

    @Override
    public List<Map> getInventoryById(Integer id) {
        return goodsMapper.getInventoryById(id);
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
        String info = "提示：1、有*号的列为必填列。\n"+
                "2、产品分类必须是系统中存在的。\n"+
                "3、产品名称不能重复";
        info = info.trim();
        row0.createCell(0).setCellValue(info);
        row0.getCell(0).setCellStyle(cellStyle);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, 6);
        sheet1.addMergedRegion(region);
        //创建标题样式
        XSSFCellStyle headerStyle = book.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFRow row1 = sheet1.createRow(1);
        row1.setHeightInPoints(30);

        sheet1.setColumnWidth(0, 5000);
        sheet1.setColumnWidth(1, 5000);
        sheet1.setColumnWidth(2, 5000);
        sheet1.setColumnWidth(3, 5000);
        sheet1.setColumnWidth(4, 5000);
        sheet1.setColumnWidth(5, 5000);

        row1.createCell(0).setCellValue("*产品分类");
        row1.createCell(1).setCellValue("*产品名称");
        row1.createCell(2).setCellValue("*产品编码");
        row1.createCell(3).setCellValue("*产品单位");
        row1.createCell(4).setCellValue("*产品单价");
        row1.createCell(5).setCellValue("产品规格");

        for (int i = 0; i < 6; i++) {
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
    public String importGoodsData(MultipartFile multipartFile) {
        GoodsExcelListener goodsExcelListener = new GoodsExcelListener(goodsMapper, goodsTypeMapper);
        try {
            EasyExcelFactory.readBySax(new BufferedInputStream(multipartFile.getInputStream()), new Sheet(1, 2, GoodsInfo.class), goodsExcelListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> list = goodsExcelListener.getResultMsg();
        if(CollectionUtils.isEmpty(list)){
            return null ;
        }else{
            String message = goodsExcelListener.getResultMsg().toString();
            int length = message.length() ;
            message = message.substring(1,length-1);
            return message;
        }
    }
}
