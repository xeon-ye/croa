package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaExtend;
import com.qinfei.qferp.entity.media1.MediaExtendAudit;
import com.qinfei.qferp.entity.media1.MediaSupplierPriceAudit;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaExtendAuditMapper
 * @Description 媒体扩展表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:37
 * @Version 1.0
 */
public interface MediaExtendAuditMapper extends BaseMapper<MediaExtend,Integer> {
    /**
     * 根据媒体ID获取媒体扩展字段
     */
    List<MediaExtendAudit> listExtendByMediaId(@Param("mediaId") Integer madiaId);

    /**
     * 批量插入媒体扩展数据
     * @param mediaExtendAudits 媒体扩展集合
     */
    int saveBatch(List<MediaExtendAudit> mediaExtendAudits);

    /**
     * 批量插入媒体扩展数据（含ID，数据迁移使用）
     * @param mediaExtendAudits
     * @return
     */
    int saveBatchForId(List<MediaExtendAudit> mediaExtendAudits);

    /**
     * 批量更新媒体扩展数据
     * @param map 参数
     */
    int updateBatch(Map map);

    int updateBatchMoreMedia(@Param("mediaExtends") List<MediaExtendAudit> list);

    /**
     * 根据媒体ID和价格类型更新指定媒体价格
     */
    int updateMediaOnePrice(MediaExtendAudit mediaExtendAudit);

    int updateMediaPriceAsZero(@Param("mediaIds") List<Integer> mediaIds);

    int deleteByMediaId(@Param("mediaId") Integer mediaId);

    /**
     * 根据媒体ID批量删除扩展表
     * @param mediaIds
     * @return
     */
    int deleteBatch(List<Integer> mediaIds);

    /**
     * 批量拷贝媒体审核扩展表到扩展表
     * @param mediaIds
     */
    int copyMediaExtendByMediaIds(List<Integer> mediaIds);

    /**
     * 拷贝媒体审核扩展表到扩展表
     * @param mediaId
     */
    int copyMediaExtendByMediaId(@Param("mediaId") Integer mediaId);

    List<String> listMediaPriceTypeByMediaIds(@Param("mediaIds") List<Integer> mediaIds);
}
