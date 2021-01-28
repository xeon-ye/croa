package com.qinfei.qferp.mapper.media;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media.MediaTerm;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MediaTermMapper extends BaseMapper<MediaTerm, Integer> {

    @Select("select * from t_media_term where state=0 and type_id=#{typeId}")
    List<MediaTerm> listByTypeId(@Param("typeId") Integer typeId);
}