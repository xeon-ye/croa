package com.qinfei.qferp.service.inventory;

import com.qinfei.qferp.entity.inventory.PurchaseSupplier;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 物品供应商接口
 * @author tsf
 */
public interface IPurchaseSupplierService {
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
    PageInfo<Map> getPurchaseSupplierInfo(Map map, Pageable pageable);

    /**
     * 新增物品供应商信息
     * @param purchaseSupplier
     */
    void savePurchaseSupplier(PurchaseSupplier purchaseSupplier);

    /**
     * 编辑物品供应商信息
     * @param purchaseSupplier
     */
    void editPurchaseSupplier(PurchaseSupplier purchaseSupplier);

    /**
     * 删除物品供应商信息
     * @param id
     */
    void delPurchaseSupplier(Integer id);

    /**
     * 判断供应商名称是否存在
     * @param map
     * @return
     */
    List<PurchaseSupplier> getPurchaseSupplierByName(Map map);

    /**
     * 获取加载供应商信息列表数据
     * @return
     */
    List<Map> getPurchaseSupplierList();

    /**
     * 导出供应商信息
     * @param map
     * @return
     */
    List<Map> exportPurchaseSupplier(Map map, OutputStream outputStream);

    /**
     * 获取供应商导入摸板
     * @param outputStream
     */
    void exportTemplate(OutputStream outputStream);

    /**
     * 批量导入供应商
     * @param multipartFile
     * @return
     */
    void importPurchaseSupplierData(MultipartFile multipartFile);
}
