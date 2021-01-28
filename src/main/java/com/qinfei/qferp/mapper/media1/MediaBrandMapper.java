package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaBrand;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 媒体品牌数据库访问层
 *
 * @author tsf
 * @since 2020-10-21 09:30:21
 */
public interface MediaBrandMapper extends BaseMapper<MediaBrand, Integer> {

    /**
     * 新增数据
     * @param mediaBrand 实例对象
     * @return 影响行数
     */
    void saveMediaBrand(MediaBrand mediaBrand);

    /**
     * 修改数据
     *
     * @param mediaBrand 实例对象
     * @return 影响行数
     */
    void updateMediaBrand(MediaBrand mediaBrand);

    /**
     * 媒体品牌列表
     * @param map
     * @return
     */
    List<Map> listPg(Map map);

    /**
     * 点击媒体品牌链接查看详情
     * @param map
     * @return
     */
    List<Map> listPgForView(Map map);

    /**
     * 根据id查询媒体品牌
     * @param id
     * @return
     */
    MediaBrand getById(@Param("id") String id);
}