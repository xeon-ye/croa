package com.qinfei.qferp.service.impl.inventory;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.inventoryStock.InventoryCheck;
import com.qinfei.qferp.entity.inventoryStock.InventoryCheckDetails;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.InventoryCheckDetailsMapper;
import com.qinfei.qferp.mapper.inventory.InventoryCheckMapper;
import com.qinfei.qferp.mapper.inventory.WarehouseMapper;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.inventory.IInventoryCheckService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * (TSavesCheck)表服务实现类
 *
 * @author makejava
 * @since 2020-06-05 11:09:37
 */
@Service
public class InventoryCheckService implements IInventoryCheckService {
    @Autowired
    private InventoryCheckMapper inventoryCheckMapper;
    @Autowired
    private InventoryCheckDetailsMapper detailsMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public Integer getPageCount(Map map) {
        try {
            map.put("companyCode", AppUtil.getUser().getCompanyCode());
            return inventoryCheckMapper.getPageCount(map);
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取库存盘点分页数量出错啦，请联系技术人员");
        }
    }

    @Override
    public PageInfo<Map> listPg(Map map, Pageable pageable) {
        try {
            map.put("companyCode", AppUtil.getUser().getCompanyCode());
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            List<Map> list = inventoryCheckMapper.listPg(map);
            PageInfo<Map> pageInfo = new PageInfo<>(list);
            return pageInfo;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，获取库存盘点分页数据出错啦，请联系技术人员");
        }
    }

    @Override
    public String getStockCheckCode() {
        return IConst.STOCK_CHECK_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.STOCK_CHECK_CODE),5);
    }

    /**
     * 新增库存盘点
     * @param inventoryCheck 实例对象
     * @param goodsId
     * @param stockAmount
     * @param checkAmount
     * @param profitAmount
     * @param lossAmount
     * @param remark
     * @return
     */
    @Transactional
    @Override
    public InventoryCheck saveInventoryCheck(InventoryCheck inventoryCheck, List<Integer> goodsId, List<Integer> stockAmount, List<Integer> checkAmount, List<Integer> profitAmount, List<Integer> lossAmount, List<String> remark) {
        try {
            User user = AppUtil.getUser();
            if (user == null) {
                throw new QinFeiException(1002, "请先登录");
            }
            inventoryCheck.setState(0);
            inventoryCheck.setCreateId(user.getId());
            inventoryCheck.setCreateName(user.getName());
            inventoryCheck.setUpdateTime(new Date());
            inventoryCheck.setUpdateUserId(user.getId());
            inventoryCheck.setUpdateTime(new Date());
            inventoryCheck.setCompanyCode(user.getCompanyCode());
            //添加库存盘点
            inventoryCheckMapper.saveInventoryCheck(inventoryCheck);
            if (CollectionUtils.isNotEmpty(goodsId)) {
                List<InventoryCheckDetails> list = new ArrayList<>();
                for (int i = 0; i < goodsId.size(); i++) {
                    InventoryCheckDetails details = new InventoryCheckDetails();
                    details.setCheckId(inventoryCheck.getId());
                    details.setState(0);
                    details.setGoodsId(goodsId.get(i));
                    details.setStockAmount(stockAmount.get(i));
                    details.setCheckAmount(checkAmount.get(i));
                    details.setProfitAmount(profitAmount.get(i));
                    details.setLossAmount(lossAmount.get(i));
                    details.setCreateId(user.getId());
                    details.setCreateName(user.getName());
                    details.setCreateTime(inventoryCheck.getCreateTime());
                    details.setUpdateUserId(user.getId());
                    details.setUpdateTime(new Date());
                    if (StringUtils.isEmpty(remark.get(i))) {
                        details.setRemark("");
                    } else {
                        details.setRemark(remark.get(i));
                    }
                    list.add(details);
                }
                //批量添加盘点明细信息
                detailsMapper.addCheckDetailsBatch(list);
            }
            return inventoryCheck;
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002, "抱歉，新增库存盘点出错啦，请联系技术人员");
        }
    }

    /**
     * 修改数据
     * @param inventoryCheck 实例对象
     * @return 实例对象
     */
    @Transactional
    @Override
    public void updateInventoryCheck(InventoryCheck inventoryCheck) {
        inventoryCheckMapper.updateInventoryCheck(inventoryCheck);
    }

    @Transactional
    @Override
    public void delInventoryCheck(Integer id) {
        //step1删除库存盘点明细
        detailsMapper.deleteCheckDetails(IConst.STATE_DELETE,id);
        //step2删除库存盘点
        inventoryCheckMapper.delInventoryCheck(IConst.STATE_DELETE,id);
    }

    @Override
    public InventoryCheck editAjax(Integer id) {
        try {
            if(id==null){
                throw new QinFeiException(1002,"找不到该库存盘点");
            }
            InventoryCheck inventoryCheck = inventoryCheckMapper.editAjax(id);
            List<InventoryCheckDetails> details=detailsMapper.queryByCheckId(id);
            inventoryCheck.setDetails(details);
            return inventoryCheck;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，库存盘点查看出错啦，请联系技术人员");
        }
    }

    /**
     * 下载导入模板
     * @param outputStream
     */
    @Override
    public void exportStockCheck(Map map,OutputStream outputStream){
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            String companyCode = user.getCompanyCode();
            map.put("companyCode",companyCode);
            List<Map> wareList = warehouseMapper.getWarehouseList(companyCode);
            List<Map> list=inventoryCheckMapper.listPg(map);
            String[] heads = {"盘点编号","标题","仓库","创建人","创建时间","备注"};
            String[] fields= {"code","title","wareId","createName","createTime","remark"};
            ExcelUtil.exportExcel("库存盘点信息", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if(value!=null){
                    if("wareId".equals(field)){
                        Integer warehouseId = Integer.valueOf(value.toString());
                        for(int i=0;i<wareList.size();i++){
                            Map obj =  wareList.get(i);
                            Integer id = Integer.valueOf(obj.get("id").toString());
                            String name = obj.get("name").toString();
                            if(warehouseId.equals(id)){
                                cell.setCellValue(name);
                                break;
                            }
                        }
                    }else {
                        cell.setCellValue(value.toString());
                    }
                }else{
                    if("wareId".equals(field)){
                        cell.setCellValue("总仓库");
                    }
                }
            });
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
}