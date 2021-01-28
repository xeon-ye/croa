package com.qinfei.qferp.service.inventory;

import com.qinfei.qferp.entity.inventory.Goods;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IGoodsService {

    //查询产品库存分页数量
    Integer getPageCount(Map map);

    //产品库存分页数据
    PageInfo<Map> listPg(Map map,Pageable pageable);

    //库存盘点选择产品查询库存产品的数量
    Integer getTotalAmount(Map map);

    //库存盘点选择产品查询库存产品数据
    PageInfo<Map> queryGoodsData(Map map,Pageable pageable);

    //根据产品id产品库存数量
    Integer getStockAmountById(Map map);

    //选择产品(库存盘点)显示在盘点明细的数据
    PageInfo<Map> getGoodsList(Integer wareId,List<Integer> ids,Pageable pageable);

    Goods save(Goods goods);

    Goods update(Goods goods);

    //批量生成库存
    void addGoodsBatch(Goods goods);

    Map getById(Integer id);

    Goods getGoodsById(Integer id);

    void del(Integer id);

    /**
     * 修改库存状态
     * @param state
     * @param id
     */
    void editGoodsState(Integer state,Integer id);


    /**
     * 判断产品名称是否重复
     * @param id
     * @param name
     * @return
     */
    List<Goods> checkSameName(Integer id,String name);

    /**
     * 根据产品分类id查询产品信息
     * @param parentId
     * @return
     */
    List<Goods> getGoodsByParentId(Integer parentId);

    /**
     * 根据库存id查询库存记录
     * @param id
     * @return
     */
    List<Map> getInventoryById(Integer id);

    /**
     * 下载导入模板
     * @param outputStream
     */
    void exportTemplate(OutputStream outputStream);

    /**
     * 批量导入产品
     * @param multipartFile
     */
    String importGoodsData(MultipartFile multipartFile);
}
