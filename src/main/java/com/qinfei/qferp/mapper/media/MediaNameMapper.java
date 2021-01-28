package com.qinfei.qferp.mapper.media;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media.MediaName;
import com.qinfei.qferp.entity.media.MediaTerm;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MediaNameMapper extends BaseMapper<MediaName, Integer> {

    @Select("select * from t_media_name state=0 and  where type_id=#{typeId}")
    List<MediaTerm> listByTypeId(@Param("typeId") Integer typeId);

    @Select("select * from t_media_name where state=0 and id=#{id}")
    MediaTerm getById(@Param("id") Integer id);
}