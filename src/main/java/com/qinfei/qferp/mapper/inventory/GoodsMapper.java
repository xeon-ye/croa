package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import com.qinfei.qferp.service.impl.inventory.excelModal.GoodsInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GoodsMapper extends BaseMapper<Goods,Integer> {

    //添加产品
    int saveGoods(Goods goods);

    //批量生成库存
    void addGoodsBatch(List<Goods> goodsList);

    //产品导入
    void insertGoodsFormExcel(List<GoodsInfo> list);

    //修改产品
    int updateGoods(Goods goods);

    //产品报修，报废，归还，库存信息显示
    Map getById(@Param("id")Integer id);

    //根据产品id查询(缓存)
    Goods getGoodsById(@Param("id")Integer id);

    //查询是否存在的相同产品名称
    List<Goods> checkSameName(@Param("id")Integer id,@Param("name")String name,@Param("companyCode")String companyCode);

    //修改库存状态
    void editGoodsState(Map map);

    //根据产品分类id查询产品信息
    List<Goods> getGoodsByParentId(Map map);

    //根据库存id查询库存记录
    List<Map> getInventoryById(Integer id);

    //根据产品id查询所有的库存
    List<Integer> getGoodsIdByProductId(Map map);

    //批量修改库存信息
    void updateGoodsBatch(Map map);

    //获取用户使用数据数量
    Integer getUserApplyCount(Map map);

    //获取用户使用数据
    List<Map> getUserApplyData(Map map);

    //查询产品库存分页数量
    Integer getPageCount(Map map);

    //产品库存分页数据
    List<Map> listPg(Map map);

    //库存盘点选择产品查询库存产品的数量
    Integer getTotalAmount(Map map);

    //选择产品(库存盘点)显示在盘点明细的数据
    List<Map> getGoodsList(Map map);

    //选择产品(库存盘点)查询库存产品数据
    List<Map> queryGoodsData(Map map);

    //选择产品显示的所有库存id
    List<Integer> getGoodIds(Map map);

    //根据产品id产品库存数量
    Integer getStockAmountById(Map map);
}
