package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaSupplierChange;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 媒体供应商异动表(TMediaSupplierChange)表数据库访问层
 *
 * @author xuxiong
 * @since 2020-05-13 11:19:09
 */
public interface MediaSupplierChangeMapper extends BaseMapper<MediaSupplierChange, Integer> {

    /**
     * 新增数据
     * @param mediaSupplierChange 实例对象
     * @return 影响行数
     */
    int save(MediaSupplierChange mediaSupplierChange);

    int saveBatch(List<MediaSupplierChange> mediaSupplierChangeList);

    //根据媒体关系ID获取异动列表
    List<MediaSupplierChange> listMediaSupplierChangeByParam(@Param("mediaSupplierRelateIds") List<Integer> mediaSupplierRelateIds);

    //根据媒体关系ID获取异动列表
    List<MediaSupplierChange> listMediaSupplierChangeByMediaIds(@Param("mediaIds") List<Integer> mediaIds);

    //获取指定异动信息
    MediaSupplierChange getMediaSupplierChangeById(@Param("id") int id);
}