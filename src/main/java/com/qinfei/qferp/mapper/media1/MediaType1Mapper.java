package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaType1;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaType1Mapper
 * @Description 媒体类型
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:10
 * @Version 1.0
 */
public interface MediaType1Mapper extends BaseMapper<MediaType1, Integer> {
    /**
     * 根据板块ID获取媒体类型列表
     * @param plateId 媒体板块ID
     */
    List<MediaType1> listByPlateId(@Param("plateId") Integer plateId);
}
