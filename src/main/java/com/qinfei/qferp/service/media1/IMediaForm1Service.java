package com.qinfei.qferp.service.media1;

import com.qinfei.qferp.entity.media1.MediaForm1;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IMediaForm1Service
 * @Description 媒体扩展表单接口
 * @Author xuxiong
 * @Date 2019/6/26 0026 10:02
 * @Version 1.0
 */
public interface IMediaForm1Service {
    String CACHE_KEY = "MediaForm1";

    @CacheEvict(value = CACHE_KEY, allEntries = true)
    void deleteBatch(List<Integer> ids);

    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, key = "'mediaPriceType='+#mediaForm.mediaPlateId"),
            @CacheEvict(value = CACHE_KEY, key = "'mediaPlateId='+#mediaForm.mediaPlateId"),
            @CacheEvict(value = CACHE_KEY, key = "'AllByPlateId='+#mediaForm.mediaPlateId"),
            @CacheEvict(value = CACHE_KEY, key = "'allMediaPriceType'")})
    void save(MediaForm1 mediaForm);

    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, key = "'AllByPlateId='+#mediaForm.mediaPlateId"),
            @CacheEvict(value = CACHE_KEY, key = "'mediaPlateId='+#mediaForm.mediaPlateId"),
            @CacheEvict(value = CACHE_KEY, key = "'AllByPlateId='+#mediaForm.mediaPlateId"),
            @CacheEvict(value = CACHE_KEY, key = "'allMediaPriceType'")})
    void update(MediaForm1 mediaForm);

    @Cacheable(value = CACHE_KEY, key = "'AllByPlateId='+#mediaPlateId")
    List<MediaForm1> findAllByMediaPlateId(Integer mediaPlateId);

    PageInfo<Map> list(Map<String,Object> map, Integer pageNum, Integer pageSize);

    @Cacheable(value = CACHE_KEY, key = "'mediaPriceType='+#mediaPlateId")
    List<MediaForm1> listPriceTypeByPlateId(Integer mediaPlateId);

    @Cacheable(value = CACHE_KEY, key = "'allMediaPriceType'")
    List<MediaForm1> listAllPriceType();

    @Cacheable(value = CACHE_KEY, key = "'mediaPlateId='+#mediaPlateId")
    List<MediaForm1> listMediaFormByPlateId(Integer mediaPlateId);

    Map<Integer, Map<String, MediaForm1>> getAllMediaForm();

    Map<Integer, Map<String, String>> getFormRelete();
}
