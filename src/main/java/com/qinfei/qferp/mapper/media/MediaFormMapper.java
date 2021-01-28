package com.qinfei.qferp.mapper.media;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media.MediaForm;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MediaFormMapper extends BaseMapper<MediaForm, Integer> {

    @Select("select * from t_media_form where disabled=0 and  media_type_id=#{mediaTypeId} ORDER BY sort_no ")
    List<MediaForm> listByMediaTypeId(@Param("mediaTypeId") Integer mediaTypeId);

    @Select("select * from t_media_form where disabled=0 and  code like 'f%' ORDER BY sort_no ")
    List<MediaForm> queryAllPriceColumns();

    @Select("select * from t_media_form where disabled=0 and  code like 'f%' and  media_type_id=#{mediaTypeId} ORDER BY sort_no ")
    List<MediaForm> queryPriceColumnsByTypeId(@Param("mediaTypeId") Integer mediaTypeId);
}