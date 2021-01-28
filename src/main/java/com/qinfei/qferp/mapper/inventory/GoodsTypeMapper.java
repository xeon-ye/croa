package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventory.GoodsType;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GoodsTypeMapper extends BaseMapper<GoodsType,Integer> {

    //添加产品分类
    int saveGoodsType(GoodsType goodsType);

    //修改产品分类
    int updateGoodsType(GoodsType goodsType);

    //根据产品id查询
    GoodsType getById(@Param("id") Integer id);

    //根据id查询产品信息及库存数量（物品领用）
    Map getStockDataById(Map map);

    //判断是否存在相同的产品分类名称
    List<GoodsType> getSameNameList(Map map);

    //判断产品分类下是否含有产品
    List<GoodsType> checkGoodsTypeData(Map map);

    //删除产品分类信息
    void del(@Param("id") Integer id,@Param("state") Integer state);

    //修改库存最大值，最小值
    void editStockAmount(Map map);

    //产品分类的分页数量
    Integer getPageCount(Map map);

    //产品分类数据
    List<GoodsType> getGoodsTypeInfo(Map map);

    //产品的分页数量
    Integer getGoodsPageCount(Map map);

    //产品的分页数据
    List<GoodsType> getGoodsInfo(Map map);

    //加载产品分类数据
    List<Map> loadGoodsTypeInfo(@Param("companyCode")String companyCode);

    //根据父级id加载产品分类信息
    List<Map> loadGoodsTypeByParentId(@Param("parentId")Integer parentId,@Param("companyCode")String companyCode);

    /**
     * 查询产品库存id集合
     * @param map
     * @return
     */
    List<Integer> getStockIds(Map map);

    /**
     * 库存最大值预警
     * @param map
     * @return
     */
    List<Map> getStockMaxWarnData(Map map);

    /**
     * 库存最大值预警
     * @param map
     * @return
     */
    List<Map> getStockMinWarnData(Map map);

}
