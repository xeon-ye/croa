package com.qinfei.qferp.mapper.inventory;

import java.util.List;
import java.util.Map;

/**
 * 进销存首页数据库接口
 */
public interface InventoryHomeMapper {

    /**
     * 物品采购订单统计
     * @param map
     * @return
     */
    List<Map> purchaseOrderStatistics(Map map);

    /**
     * 查询采购订单的个数及金额
     * @param map
     * @return
     */
    Map  purchaseOrderResult(Map map);

    /**
     * 商品库存分析饼图
     * @param map
     * @return
     */
    List<Map> stockAnalysisPie(Map map);

    /**
     * 商品库存分析饼图
     * @param map
     * @return
     */
    List<Map> stockAnalysis(Map map);

    /**
     * 商品库存分析结果（库存金额，库存数量）
     * @param map
     * @return
     */
    Map stockAnalysisResult(Map map);

    /**
     * 入库统计
     * @param map
     * @return
     */
    List<Map> putStockStatistics(Map map);

    /**
     * 出库统计
     * @param map
     * @return
     */
    List<Map> outStockStatistics(Map map);

    /**
     * 出入库统计结果
     * @param map
     * @return
     */
    List<Map> outBoundResult(Map map);
}
