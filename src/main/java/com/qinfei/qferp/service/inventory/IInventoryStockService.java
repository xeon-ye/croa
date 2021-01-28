package com.qinfei.qferp.service.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.inventoryStock.Outbound;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IInventoryStockService {

    /**
     * 获取入库分页数量
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 获取入库列表分页数据
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Map> listPg(Map<String,Object> map, Pageable pageable);

    /**
     * 获取出库分页数量
     * @param map
     * @return
     */
    Integer getOutStockPageCount(Map map);

    /**
     * 获取出库分页数据
     * @param map
     * @return
     */
    PageInfo<Map> getOutStockListPg(Map map,Pageable pageable);

    /**
     * 根据出入库id查询出入库信息
     * @param id
     * @return
     */
    ResponseData viewOutbound(Integer id);

    /**
     * 出库编辑查看
     * @param id
     * @return
     */
    ResponseData editAjax(Integer id);

    /**
     * 新增物品入库
     * 添加出入库记录，修改物品采购明细
     * @param outbound
     * @param purchaseDetails
     */
    Outbound addOutBound(Outbound outbound,List<String>purchaseDetails);

    /**
     * 编辑物品入库
     * @param outbound
     * @param purchaseDetails
     * @return
     */
    Outbound editOutBound(Outbound outbound,List<String>purchaseDetails);

    /**
     * 删除入库记录
     * @param id
     */
    void delOutboundById(Integer id,Integer purchaseId);

    /**
     * 新增出库
     * @param outbound
     * @param applyDetails
     */
    Outbound addOutStock(Outbound outbound,List<String> applyDetails);

    /**
     * 编辑出库
     * @param outbound
     * @param applyDetails
     * @return
     */
    Outbound editOutStock(Outbound outbound,List<String> applyDetails);

    /**
     * 自动生成出入库编号
     * @return
     */
    String getOutboundCode();

    /**
     * 自动生成出库编号
     * @return
     */
    String getOutStockCode();

    /**
     * 自动生成库存编号
     * @return
     */
    String getInventoryCode();

    //产品库存导入模板
    void exportTemplate(OutputStream outputStream);

    //处理产品库存数据
    String importInventoryData(String fileName);

    //产品库存导出
    List<Map> exportInventoryDetail(Map map,OutputStream outputStream);

    /**
     * 物品入库选择采购订单时，显示物品采购明细信息
     * @param id
     * @return
     */
    ResponseData warehousingDetail(Integer id);

    /**
     * 物品出库选择领用订单时，显示物品领用明细信息
     * @param id
     * @return
     */
    List<Map<String,Object>> applyDetailsData(Integer id);

    /**
     * 使采购单失效
     * @param outbound
     */
    void lossEffect(Outbound outbound);
}
