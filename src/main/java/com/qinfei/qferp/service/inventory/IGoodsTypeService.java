package com.qinfei.qferp.service.inventory;

import com.qinfei.qferp.entity.inventory.GoodsType;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IGoodsTypeService {

    GoodsType saveGoodsType(GoodsType goodsType);

    GoodsType updateGoodsType(GoodsType goodsType);

    GoodsType getById(Integer id);

    /**
     * 根据id查询产品信息及库存数量（物品领用）
     * @param map
     * @return
     */
    Map getStockDataById(Map map);

    void del(Integer id);

    /**
     * 判断是否存在相同的产品分类名称
     * @param id
     * @param name
     * @param companyCode
     * @return
     */
    List<GoodsType> getGoodsTypeByCondition(Integer id,String name,String companyCode);

    /**
     * 判断产品分类下是否含有产品
     * @param id
     * @return
     */
    List<GoodsType> checkGoodsTypeData(Integer id);

    //修改库存最大值，最小值
    void editStockAmount(Map map);

    //产品分类的分页数量
    Integer getPageCount(Map map);

    //产品分类数据
    PageInfo<GoodsType> getGoodsTypeInfo(Map map, Pageable pageable);

    //产品的分页数量
    Integer getGoodsPageCount(Map map);

    //产品数据
    PageInfo<GoodsType> getGoodsInfo(Map map,Pageable pageable);

    //加载产品分类信息
    List<Map> loadGoodsTypeInfo(String companyCode);

    //根据父级id加载产品分类信息
    List<Map> loadGoodsTypeByParentId(Integer parentId,String companyCode);

    /**
     * 库存最大值预警
     * @param map
     * @return
     */
    PageInfo<Map> getStockMaxWarnData(Map map);

    /**
     * 库存最小值预警
     * @param map
     * @return
     */
    PageInfo<Map> getStockMinWarnData(Map map);
}
