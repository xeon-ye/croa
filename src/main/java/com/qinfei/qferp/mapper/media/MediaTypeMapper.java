package com.qinfei.qferp.mapper.media;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media.MediaType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MediaTypeMapper extends BaseMapper<MediaType, Integer> {

//    @Select("select * from t_media_type where state=0 and id=#{id}")
//    MediaType getById(@Param("id") Integer id);

//    @Select("select * from t_media_type where state=0 and id IN (${ids})")
//    List<MediaType> listByIds(@Param("ids") String ids);

//    @Select("select * from t_media_type where state=0 and parent_id=#{parentId}")
//    List<MediaType> getByParentId(@Param("parentId") Integer parentId);

//    @Select("select * from t_media_type where state=0 and parent_id=#{parentId} and id IN (${ids})")
//    List<MediaType> listByIdsAndParentId(@Param("parentId") Integer parentId, @Param("ids") String ids);


//    @Select("select a.* from t_media_type a,t_media b where a.state=0 and a.id=b.m_type and b.id=#{mediaId}")
//    MediaType getByMediaId(@Param("mediaId") Integer mediaId);

    /**
     * 根据用户Id查询媒体类型
     *
     * @param userId 媒介ID
     * @return
     */
//    @Select("SELECT a.* FROM t_media_type a,t_user_media_type b WHERE a.`id`=b.`media_type_id` AND a.`parent_id`=0 AND b.`user_id`=#{userId}")
//    List<MediaType> listByUserId(@Param("userId") Integer userId);

}