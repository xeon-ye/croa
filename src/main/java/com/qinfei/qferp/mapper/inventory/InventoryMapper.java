package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventoryStock.InventoryStock;
import com.qinfei.qferp.entity.inventoryStock.Outbound;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 出入库记录表数据库接口
 * @author tsf
 */
public interface InventoryMapper extends BaseMapper<InventoryStock,Integer> {
    /**
     * 获取入库分页数量
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 获取入库分页列表数据
     * @param map
     * @return
     */
    List<Map> listPg(Map map);

    /**
     * 获取出库分页数量
     * @param map
     * @return
     */
    Integer getOutStockPageCount(Map map);

    /**
     * 获取出库分页列表数据
     * @param map
     * @return
     */
    List<Map> getOutStockListPg(Map map);

    /**
     * 编辑物品入库
     * @param outbound
     * @return
     */
    int editOutbound(Outbound outbound);

    /**
     * 添加入库记录
     * @param outbound
     * @return
     */
    Integer saveOutbound(Outbound outbound);

    /**
     * 删除入库记录
     * @param id
     */
    void delOutboundById(Integer id);

    /**
     * 使出入库记录失效
     * @param state
     * @param id
     */
    void editOutboundState(@Param("state")Integer state,@Param("id")Integer id);

    /**
     * 根据出入库id查询出入库信息
     * @param id
     * @return
     */
    Outbound getById(Integer id);

    /**
     * 物品入库选择采购订单时，显示物品采购明细信息
     * @param id
     * @return
     */
    List<Map<String,Object>> warehousingDetail(Integer id);

    /**
     * 物品出库选择采购订单时，显示物品领用明细信息
     * @param map
     * @return
     */
    List<Map<String,Object>> applyDetailsData(Map map);
}
