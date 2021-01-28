package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaSupplierPrice;
import com.qinfei.qferp.entity.media1.MediaSupplierPriceAudit;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @CalssName MediaSupplierPriceMapper
 * @Description 媒体供应商价格扩展表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:37
 * @Version 1.0
 */
public interface MediaSupplierPriceMapper extends BaseMapper<MediaSupplierPriceAudit,Integer> {
    /**
     * 根据媒体供应商关系表ID删除供应商价格
     */
    int deleteByRelateIds(@Param("mediaSupplierRelateIds") Set<Integer> mediaSupplierRelateIds);

    /**
     * 根据媒体供应商关系表ID拷贝供应商价格审核表信息到供应商价格表
     */
    int copySupplierPriceByRelateIds(@Param("mediaSupplierRelateIds") Set<Integer> mediaSupplierRelateIds);

    /**
     * 批量插入媒体供应商价格扩展数据
     * @param mediaSupplierPriceAuditList 媒体扩展集合
     */
    int saveBatch(List<MediaSupplierPrice> mediaSupplierPriceAuditList);

    /**
     * 批量插入媒体供应商价格扩展数据（含ID，数据迁移使用）
     * @param mediaSupplierPriceAuditList 媒体扩展集合
     */
    int saveBatchForId(List<MediaSupplierPrice> mediaSupplierPriceAuditList);
}
