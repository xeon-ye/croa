package com.qinfei.qferp.service.media1;

import com.qinfei.qferp.entity.media1.Media1;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @CalssName IMedia1Service
 * @Description 媒体接口
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:55
 * @Version 1.0
 */
public interface IMedia1Service {
    String CACHE_KEY = "media1";

    /**
     * 根据条件分页查询媒体供应商列表
     */
    PageInfo<Map<String, Object>> listMediaSupplierByParam(Map<String, Object> map, Pageable pageable);

    //获取媒体列表（新增稿件媒体列表使用）
    PageInfo<Map<String, Object>> listMediaByParam(Map<String, Object> map, Pageable pageable);

    /**
     * 根据媒体ID获取供应商信息和价格
     */
    @Cacheable(value = CACHE_KEY, key = "'supplierInfo='+#id+'&cell='+#cell")
    List<Map> getMediaSupplierInfoByMediaId(Integer id, String cell);

    /**
     * 根据媒体ID获取供应商信息和价格
     */
    @Cacheable(value = CACHE_KEY, key = "'supplierInfo1='+#id")
    List<Map<String, Object>> getMediaSupplierInfoByMediaId(Integer id);

    /**
     * 根据页面传递的集合信息查询所有媒体；
     *  -> 媒体下单不区分公司编码
     * @param map：查询条件集合；
     * @param pageable：分页对象；
     * @return ：分页完成的数据集合；
     */
    PageInfo<Media1> listMedia(Map<String, Object> map, Pageable pageable);

    void batchExport(OutputStream outputStream, Map<String, Object> map);

    //媒体复投统计-趋势图
    List<Map<String, Object>> listMediaFT(Map<String, Object> param);

    //媒体复投统计-列表
    PageInfo<Map<String, Object>> listMediaFTByPage(Map<String, Object> param, Pageable pageable);

    //导出复投详情
    void batchFTExport(OutputStream outputStream, Map<String, Object> map);

    //数据迁移
    String transfer();
}
