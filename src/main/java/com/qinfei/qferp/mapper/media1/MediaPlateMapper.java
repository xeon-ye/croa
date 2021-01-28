package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaPlate;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaPlateMapper
 * @Description 媒体板块表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:18
 * @Version 1.0
 */
public interface MediaPlateMapper extends BaseMapper<MediaPlate,Integer> {
    //校验板块名称唯一
    int checkMediaPlate(@Param("name") String name, @Param("id") Integer id);

    //新增
    int save(MediaPlate mediaPlate);

    //修改
    int updateById(MediaPlate mediaPlate);

    //修改状态
    int updateState(@Param("isDelete") int isDelete, @Param("id") int id);

    //列表查询
    List<MediaPlate> listPlateByParam(Map<String, Object> param);

    List<MediaPlate> listByPlateTypeId(@Param("plateTypeId") Integer plateTypeId);

    List<MediaPlate> listMediaPlateByUserId(@Param("userId") Integer userId);

    MediaPlate getByMediaId(@Param("mediaId") Integer mediaId);

    List<MediaPlate> queryMediaPlate();

}
