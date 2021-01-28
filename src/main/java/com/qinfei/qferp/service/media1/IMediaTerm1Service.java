package com.qinfei.qferp.service.media1;

import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.media1.MediaTerm1;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IMediaTerm1Service
 * @Description 媒体查询条件表
 * @Author xuxiong
 * @Date 2019/6/26 0026 10:02
 * @Version 1.0
 */
public interface IMediaTerm1Service {
    String CACHE_KEY = "MediaTerm1";

    @CacheEvict(value = CACHE_KEY, allEntries = true)
    void deleteBatch(List<Integer> ids);

    @CacheEvict(value = CACHE_KEY, key = "'mediaPlateId='+#mediaTerm1.mediaPlateId")
    void save(MediaTerm1 mediaTerm1);

    @CacheEvict(value = CACHE_KEY, key = "'mediaPlateId='+#mediaTerm1.mediaPlateId")
    void update(MediaTerm1 mediaTerm1);

    @Cacheable(value = CACHE_KEY, key = "'mediaPlateId='+#mediaPlateId")
    List<MediaTerm1> findAllByMediaPlateId(Integer mediaPlateId);

    PageInfo<Map> list(Map<String, Object> map, Integer pageNum, Integer pageSize);
}
