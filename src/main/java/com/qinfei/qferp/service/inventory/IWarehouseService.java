package com.qinfei.qferp.service.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.inventoryStock.Warehouse;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IWarehouseService {

    ResponseData addWarehouse(Warehouse warehouse);

    PageInfo<Warehouse> warehouseList(Map<String,Object> map, Pageable pageable);

    /**
     * 判断仓库名称是否相同
     * @param map
     * @return
     */
    List<Warehouse> getSameNameList(Map map);

    /**
     * 根据id查询仓库信息
     * @param id
     * @return
     */
    Warehouse editAjax(Integer id);

    /**
     * 根据id查询仓库名称
     * @param id
     * @return
     */
    String getWareNameById(Integer id);

    /**
     * 查询仓库是否被使用
     * @param id
     * @return
     */
    Integer getCountByWareId(Integer id);

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
    PageInfo<Map> listPg(Map map,Pageable pageable);

    /**
     * 编辑仓库信息
     * @param warehouse
     */
    void editWarehouse(Warehouse warehouse);

    /**
     * 删除仓库信息
     * @param id
     * @return
     */
    ResponseData delWarehouse(Integer id);
}
