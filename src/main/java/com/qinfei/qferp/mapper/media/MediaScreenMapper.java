package com.qinfei.qferp.mapper.media;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media.MediaScreen;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MediaScreenMapper extends BaseMapper<MediaScreen, Integer> {

    @Select("select * from t_media_screen where state=0 and media_type_id=#{mediaTypeId}")
    List<MediaScreen> listByMediaTypeId(@Param("mediaTypeId") Integer mediaTypeId);
}