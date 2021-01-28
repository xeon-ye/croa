package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaChange;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 媒体异动表(TMediaChange)表数据库访问层
 *
 * @author xuxiong
 * @since 2020-05-11 15:59:18
 */
public interface MediaChangeMapper extends BaseMapper<MediaChange, Integer> {

    /**
     * 新增数据
     * @param mediaChange 实例对象
     * @return 影响行数
     */
    int save(MediaChange mediaChange);

    /**
     * 批量插入媒体异动表
     * @param changeList 实例对象
     * @return 影响行数
     */
    int saveBatch(List<MediaChange> changeList);

    //根据媒体ID获取异动列表
    List<MediaChange> listMediaChangeByParam(@Param("mediaIds") List<Integer> mediaIds);

    //获取指定异动信息
    MediaChange getMediaChangeById(@Param("id") int id);

}