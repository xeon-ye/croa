package com.qinfei.qferp.service.inventory;

import java.util.List;
import java.util.Map;

/**
 * 进销存首页接口
 * @author tsf
 */
public interface IInventoryHomeService {

    /**
     * 物品采购订单统计
     * @param map
     * @return
     */
    Map purchaseOrderStatistics(Map map);

    /**
     * 商品库存分析数据
     * @param map
     * @return
     */
    Map stockAnalysis(Map map);

    /**
     * 出入库统计(入库金额，入库单数量，出库金额，出库单数量)
     * @param map
     * @return
     */
    Map outBoundStatistics(Map map);
}
