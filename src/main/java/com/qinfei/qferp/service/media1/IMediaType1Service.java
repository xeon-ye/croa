package com.qinfei.qferp.service.media1;

import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.entity.media1.MediaType1;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IMediaType1Service
 * @Description 媒体类型
 * @Author xuxiong
 * @Date 2019/6/26 0026 10:11
 * @Version 1.0
 */
public interface IMediaType1Service {
    String CACHE_KEY = "MediaType1";

    @Cacheable(value = CACHE_KEY, key = "'plateId='+#plateId", unless = "#result==null||#result.size()==0")
    List<MediaType1> listByPlateId(Integer plateId);

    @Cacheable(value = CACHE_KEY, key = "'listMediaTypeByPlateId='+#plateId", unless = "#result==null||#result.size()==0")
    Map<String, Integer> listMediaTypeByPlateId(Integer plateId);
}
