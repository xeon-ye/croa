package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventory.PurchaseSupplier;

import java.util.List;
import java.util.Map;

/**
 * 物品供应商数据库接口
 * @author tsf
 */
public interface PurchaseSupplierMapper extends BaseMapper<PurchaseSupplier,Integer> {
    /**
     * 根据id查询供应商信息
     * @param id
     * @return
     */
    PurchaseSupplier getById(Integer id);

    /**
     * 获取物品供应商数量
     * @param map
     * @return
     */
    Integer getPurchaseSupplierCount(Map map);

    /**
     * 获取物品供应商分页数据
     * @param map
     * @return
     */
    List<Map> getPurchaseSupplierInfo(Map map);

    /**
     * 删除物品供应商信息
     * @param id
     */
    void delPurchaseSupplier(Integer id);

    /**
     * 获取加载供应商所需数据
     * @param companyCode
     * @return
     */
    List<Map> getPurchaseSupplierList(String companyCode);

    /**
     * 判断供应商名称是否存在
     * @param map
     * @return
     */
    List<PurchaseSupplier> getPurchaseSupplierByName(Map map);

    /**
     * 供应商导入
     * @param list
     */
    void insertPurchaseSupplierFormExcel(List<PurchaseSupplier> list);
}
