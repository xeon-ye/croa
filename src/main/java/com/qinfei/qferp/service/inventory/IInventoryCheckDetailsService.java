package com.qinfei.qferp.service.inventory;
import com.qinfei.qferp.entity.inventoryStock.InventoryCheckDetails;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * InventoryCheckDetails表服务接口
 *
 * @author tsf
 * @since 2020-06-02 16:18:51
 */
public interface IInventoryCheckDetailsService {

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
     * 库存盘点导出
     * @param map
     * @param outputStream
     * @return
     */
    List<Map> exportForeWarning(Map map, OutputStream outputStream);

    /**
     * 下载导入模板
     * @param outputStream
     */
    void exportTemplate(OutputStream outputStream);

    String importInventoryCheckData(String fileName);

    /**
     * 新增数据
     * @param checkDetails 实例对象
     */
    void saveCheckDetails(InventoryCheckDetails checkDetails);

    /**
     * 修改数据
     * @param checkDetails 实例对象
     */
    void editCheckDetails(InventoryCheckDetails checkDetails);
}