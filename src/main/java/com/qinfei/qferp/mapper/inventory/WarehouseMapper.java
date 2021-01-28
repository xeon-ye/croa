package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventoryStock.Warehouse;

import java.util.List;
import java.util.Map;

public interface WarehouseMapper extends BaseMapper<Warehouse,Integer> {

    int addWareHouse(Warehouse warehouse);

    List<Warehouse> warehouseList(Map<String,Object> map);

    /**
     * 判断仓库名称是否相同
     * @param map
     * @return
     */
    List<Warehouse> getSameNameList(Map map);

    /**
     * 查询仓库id仓库信息
     * @param id
     * @return
     */
    Warehouse editAjax(Integer id);

    /**
     * 查询仓库信息集合
     * @param companyCode
     * @return
     */
    List<Map> getWarehouseList(String companyCode);

    /**
     * 根据仓库id查询仓库名称
     * @param id
     * @return
     */
    String getWareNameById(Integer id);

    /**
     * 获取仓库分页数量
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 获取仓库分页数据
     * @param map
     * @return
     */
    List<Map> listPg(Map map);

    /**
     * 编辑仓库信息
     * @param warehouse
     */
    void editWarehouse(Warehouse warehouse);

    /**
     * 查询仓库是否被使用
     * @param id
     * @return
     */
    Integer getCountByWareId(Integer id);

    /**
     * 删除仓库信息
     * @param warehouse
     * @return
     */
    int updateWarehouse(Warehouse warehouse);
}
