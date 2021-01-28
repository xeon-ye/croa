package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaExtend;
import com.qinfei.qferp.entity.media1.MediaExtendAudit;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaExtendMapper
 * @Description 媒体扩展表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:37
 * @Version 1.0
 */
public interface MediaExtendMapper extends BaseMapper<MediaExtend,Integer> {
    /**
     * 批量插入媒体扩展数据
     * @param mediaExtends 媒体扩展集合
     */
    int saveBatch(List<MediaExtend> mediaExtends);

    /**
     * 批量插入媒体扩展表（含ID，数据迁移使用）
     */
    int saveBatchForId(List<MediaExtend> mediaExtends);

    /**
     * 批量更新媒体扩展数据
     * @param map 参数
     */
    int updateBatch(Map map);

    int updateMediaPriceAsZero(@Param("mediaIds") List<Integer> mediaIds);

    int updateBatchMoreMedia(@Param("mediaExtends") List<MediaExtend> list);

    /**
     * 根据媒体ID删除扩展表
     * @param mediaId
     * @return
     */
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

    int copyMediaNotPriceExtendByMediaId(@Param("mediaId") Integer mediaId);

    List<String> listMediaPriceTypeByMediaIds(@Param("mediaIds") List<Integer> mediaIds);
}

