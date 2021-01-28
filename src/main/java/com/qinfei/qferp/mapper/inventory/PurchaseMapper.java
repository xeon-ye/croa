package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventory.Purchase;
import com.qinfei.qferp.entity.inventory.PurchaseDetails;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 物品采购数据库接口
 * @author tsf
 */
public interface PurchaseMapper extends BaseMapper<Purchase,String> {
    /**
     * 根据id查询物品采购信息
     * @param id
     * @return
     */
    Purchase getById(Integer id);

    /**
     * 获取采购列表总数
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 查询物品采购列表
     * @param map
     * @return
     */
    List<Map> getPurchasePage(Map map);

    List<Purchase> getPurchaseByCode(@Param("companyCode")String companyCode,@Param("purchaseCode")String purchaseCode);

    /**
     * 根据id删除物品采购信息
     * @param id
     */
    void delPurchase(Integer id);

    /**
     * 根据id修改订单状态
     * @param state
     * @param id
     */
    void editPurchaseState(@Param("state")Integer state,@Param("rejectReason")String rejectReason,@Param("id")Integer id);

    void editPurchase(Purchase purchase);

    /**
     * 判断供应商下是否有采购记录
     * @param supplierId
     * @return
     */
    Integer getCountBySupplierId(Integer supplierId);

    void addPurchaseBatch(List<Purchase> purchases);

    /**
     * 批量添加物品明细数据
     * @param purchaseDetails
     */
    void addPurchaseDetailsBatch(List<PurchaseDetails> purchaseDetails);

    /**
     * 根据采购编号查询物品明细信息
     * @param id
     * @return
     */
    List<PurchaseDetails> getPurchaseDetailsById(Integer id);

    /**
     * 删除物品明细关联信息
     * @param id
     */
    void delPurchaseDetailsBatch(Integer id);

    void delPurchaseDetailsByIds(Map map);

    /**
     * 编辑物品明细状态
     * @param id
     */
    void editPurchaseDetailsState(Integer id);

    /**
     * 采购入库选择采购订单展示的数据
     * @param map
     * @return
     */
    List<Map> orderList(Map map);

    /**
     * 编辑查看采购入库采购订单展示的数据
     * @param map
     * @return
     */
    List<Map> orderList2(Map map);

    List<Purchase> getPurchaseByCondition(Map map);

    /**
     * 删除产品时判断产品id是否被采购明细使用
     * @param id
     * @param companyCode
     * @return
     */
    Integer getPurchaseDetailsByGoodsId(@Param("id")Integer id,@Param("companyCode")String companyCode);

    /**
     * 物品入库时绑定仓库id
     * @param map
     */
    void editPurchaseDetailsByParam(Map map);

    /**
     * 根据采购id置空采购明细的仓库id
     * @param id
     */
    void editWarehouseByPurchaseId(@Param("id")Integer id);

    /**
     * 采购关联报销
     * @param list
     * @return
     */
    List<Map> relatedReimbursement(List<String> list);

    /**
     * 判断该报销id是否关联采购
     * @param id
     * @return
     */
    List<Purchase> getPurchaseByReimbursementId(Integer id);

    /**
     * 确认选中采购单中是否关联报销
     * @param list
     * @return
     */
    List<Map> checkRelatedReimbursement(List<String> list);

    /**
     * 删除报销时，置空报销
     * @param id
     */
    void editReimbursementId(Integer id);

    /**
     * 采购关联报销id
     * @param map
     */
    void editPurchaseReimbursementId(Map map);
}
