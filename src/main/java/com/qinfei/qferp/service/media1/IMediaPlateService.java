package com.qinfei.qferp.service.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.media1.MediaPlate;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IMediaPlateService
 * @Description 媒体板块接口
 * @Author xuxiong
 * @Date 2019/6/26 0026 10:09
 * @Version 1.0
 */
public interface IMediaPlateService {
    String CACHE_KEY = "MediaPlate";
    String CACHE_KEY_LIST = "MediaPlateList";

    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    void save(MediaPlate mediaPlate);

    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    void update(MediaPlate mediaPlate);

    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    void updateState(int id, int state);

    PageInfo<MediaPlate> listPlate(Map<String, Object> param, Pageable pageable);

    ResponseData mediaAllPlateList();

    @Cacheable(value = CACHE_KEY, key = "'plateTypeId='+#plateTypeId", unless = "#result==null||#result.size()==0")
    List<MediaPlate> listByPlateTypeId(Integer plateTypeId);

//    @Cacheable(value = CACHE_KEY, key = "'userId='+#userId", unless = "#result==null||#result.size()==0")
    List<MediaPlate> listMediaPlateByUserId(Integer userId);

    @Cacheable(value = CACHE_KEY, key = "'mediaId'+#mediaId")
    MediaPlate getByMediaId(Integer mediaId);

    @Cacheable(value = CACHE_KEY_LIST, key = "'ALL'")
    List<MediaPlate>  queryMediaPlate();
}
