package com.qinfei.qferp.service.inventory;

import com.qinfei.qferp.entity.inventory.Purchase;
import com.qinfei.qferp.entity.inventory.PurchaseDetails;
import com.qinfei.qferp.entity.workbench.Items;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 物品采购接口
 * @author tsf
 */
public interface IPurchaseService {

    /**
     * 根据id查询物品采购信息(包含物品采购明细信息)
     * @param id
     * @return
     */
    Purchase getById(Integer id);

    /**
     * 根据id查询物品采购信息
     * @param id
     * @return
     */
    Purchase getById2(Integer id);

    /**
     * 获取采购列表总数
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 查询物品采购列表
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Map> getPurchasePage(Map map, Pageable pageable);

    /**
     * 新增物品采购信息
     * @param purchase
     */
    Purchase savePurchase(Purchase purchase, List<Integer> type, List<Integer> goodsId, List<String> specs, List<String> unit,
                      List<Integer> amount, List<Double> price, List<Double> totalMoney,List<Integer> supplierId,List<Integer> payMethod);

    /**
     * 编辑物品采购信息
     * @param purchase
     */
    Purchase editPurchase(Purchase purchase, List<Integer> type, List<Integer> goodsId, List<String> specs, List<String> unit,
                      List<Integer> amount, List<Double> price, List<Double> totalMoney,List<Integer> supplierId,List<Integer> payMethod);

    /**
     * 删除产品时判断是否在采购明细中被使用
     * @param id
     * @return
     */
    Integer getPurchaseDetailsByGoodsId(Integer id);

    /**
     * 根据采购编号查询物品明细信息
     * @param id
     * @return
     */
    List<PurchaseDetails> getPurchaseDetailsById(Integer id);

    /**
     * 物品入库时绑定仓库id
     * @param map
     */
    void editPurchaseDetailsByParam(Map map);

    /**
     * 根据采购id置空采购明细的仓库id
     * @param id
     */
    void editWarehouseByPurchaseId(Integer id);

    /**
     * 根据id删除物品采购信息
     * @param id
     */
    void delPurchase(Integer id);

    /**
     * 根据id修改订单状态
     * @param state
     * @param rejectReason
     * @param id
     */
    void editPurchaseState(Integer state,String rejectReason,Integer id);

    /**
     * 根据供应商id查询采购记录(判断供应商是否可以删除)
     * @param supplierId
     * @return
     */
    Integer getPurchaseBySupplierId(Integer supplierId);

    /**
     * 导出物品采购信息
     * @param map
     * @param outputStream
     * @return
     */
    List<Map> exportPurchaseDetail(Map map, OutputStream outputStream);

    /**
     * 物品采购流程更新状态
     * @param purchase
     */
    void processPurchase(Purchase purchase);

    /**
     * 自动生成采购编号
     * @return
     */
    String getPurchaseCode();

    /**
     * 新增采购入库显示的采购订单
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Map> orderList(Map<String,Object>map,Pageable pageable);
    /**
     * 编辑采购入库显示的采购订单
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Map> orderList2(Map<String,Object>map,Pageable pageable);

    /**
     * 物品采购导出模板
     * @param outputStream
     */
    void getDataImportTemplate(OutputStream outputStream);

    /**
     * 批量导入物品采购信息
     * @param fileName
     */
    String importPurchaseData(String fileName);

    /**
     * 添加待办
     * @param userId
     * @param purchaseId
     */
    Items addItem(Integer userId, Integer purchaseId,String title);

    /**
     * 财务抄送确认
     * @param itemId
     * @return
     */
    void confirm(Integer itemId);

    /**
     * 采购关联报销
     * @param ids
     * @return
     */
    List<Map> relatedReimbursement(List<String> ids);

    /**
     * 确认选中采购单中是否关联报销
     * @param list
     * @return
     */
    List<Map> checkRelatedReimbursement(List<String> list);
}
