package com.qinfei.qferp.service.media1;

import com.qinfei.qferp.entity.media1.MediaBrand;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 媒体品牌服务接口
 *
 * @author tsf
 * @since 2020-10-21 09:30:21
 */
public interface IMediaBrandService {

    /**
     * 新增数据
     * @param mediaBrand 实例对象
     */
    void save(MediaBrand mediaBrand);

    /**
     * 修改数据
     * @param mediaBrand 实例对象
     */
    void update(MediaBrand mediaBrand);

    /**
     * 媒体品牌列表
     * @param map
     * @return
     */
    PageInfo<Map> listPg(Map map, Pageable pageable);

    /**
     * 点击媒体品牌链接查看详情
     * @param map
     * @return
     */
    PageInfo<Map> listPgForView(Map map, Pageable pageable);

    /**
     * 根据id查询媒体品牌
     * @param id
     * @return
     */
    MediaBrand getById(@Param("id") String id);
}