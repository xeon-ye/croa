package com.qinfei.qferp.service.inventory;

import com.qinfei.qferp.entity.inventoryStock.InventoryCheck;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * (InventoryCheck)表服务接口
 *
 * @author makejava
 * @since 2020-06-05 11:09:37
 */
public interface IInventoryCheckService {

    /**
     * 获取库存盘点分页数量
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 获取库存盘点分页数据
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Map> listPg(Map map, Pageable pageable);

    /**
     * 获取库存盘点编号
     * @return
     */
    String getStockCheckCode();

    /**
     * 新增库存盘点数据
     * @param inventoryCheck 实例对象
     */
    InventoryCheck saveInventoryCheck(InventoryCheck inventoryCheck, List<Integer> goodsId, List<Integer> stockAmount, List<Integer> checkAmount, List<Integer> profitAmount, List<Integer> lossAmount, List<String> remark);

    /**
     * 修改库存盘点数据
     * @param inventoryCheck 实例对象
     */
    void updateInventoryCheck(InventoryCheck inventoryCheck);

    /**
     * 删除库存盘点
     * @param id
     */
    void delInventoryCheck(Integer id);

    /**
     * 查看库存盘点信息
     * @param id
     * @return
     */
    InventoryCheck editAjax(Integer id);

    /**
     * 导出库存盘点
     * @param map
     * @param outputStream
     */
    void exportStockCheck(Map map,OutputStream outputStream);
}