package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaExtend;
import com.qinfei.qferp.entity.media1.MediaExtendAudit;
import com.qinfei.qferp.entity.media1.MediaSupplierPriceAudit;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @CalssName MediaSupplierPriceAuditMapper
 * @Description 媒体供应商价格扩展表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:37
 * @Version 1.0
 */
public interface MediaSupplierPriceAuditMapper extends BaseMapper<MediaSupplierPriceAudit,Integer> {

    /**
     * 根据关系ID获取供应商价格
     */
    List<MediaSupplierPriceAudit> listSupplierPriceByRelateId(@Param("relateId") Integer relateId);

    /**
     * 批量插入媒体供应商价格扩展数据
     * @param mediaSupplierPriceAuditList 媒体扩展集合
     */
    int saveBatch(List<MediaSupplierPriceAudit> mediaSupplierPriceAuditList);

    /**
     * 批量插入媒体扩展表（含ID，数据迁移使用）
     * @param mediaSupplierPriceAuditList
     * @return
     */
    int saveBatchForId(List<MediaSupplierPriceAudit> mediaSupplierPriceAuditList);

    /**
     * 批量更新媒体扩展数据
     * @param mediaSupplierPriceAuditList 参数
     */
    int updateBatch(@Param("mediaSupplierPriceList") List<MediaSupplierPriceAudit> mediaSupplierPriceAuditList);

    /**
     * 批量更新媒体扩展数据
     * @param mediaSupplierPriceAuditList 参数
     */
    int updateBatchForMap(@Param("mediaSupplierPriceList") List<Map<String, Object>> mediaSupplierPriceAuditList);


    /**
     * 根据关系ID和价格类型更新指定供应商价格
     */
    int updateOnePrice(MediaSupplierPriceAudit mediaSupplierPriceAudit);

    /**
     * 根据媒体供应商关系表ID删除供应商价格
     * @param mediaSupplierRelateIds
     * @return
     */
    int deleteByRelateId(@Param("mediaSupplierRelateIds") Set<Integer> mediaSupplierRelateIds);

    /**
     * 根据媒体供应商关系表ID拷贝供应商价格审核表信息到供应商价格表
     */
    int copySupplierPriceByRelateIds(@Param("mediaSupplierRelateIds") Set<Integer> mediaSupplierRelateIds);
}
