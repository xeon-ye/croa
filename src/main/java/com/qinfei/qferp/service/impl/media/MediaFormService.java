package com.qinfei.qferp.service.impl.media;

import com.qinfei.qferp.entity.media.MediaForm;
import com.qinfei.qferp.mapper.media.MediaFormMapper;
import com.qinfei.qferp.service.media.IMediaFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MediaFormService implements IMediaFormService {
    @Autowired
    MediaFormMapper mediaFormMapper;
    private static final String CACHE_KEY = "MediaForm";

    @Override
    @Cacheable(value = CACHE_KEY)
    public List<MediaForm> all() {
        return mediaFormMapper.all(MediaForm.class);
    }

    @Override
    @Cacheable(value = CACHE_KEY)
    public List<MediaForm> list(MediaForm mediaForm) {
        return mediaFormMapper.listByOrder(mediaForm, "sort_no");
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "#mediaTypeId")
    public List<MediaForm> listByMediaTypeId(Integer mediaTypeId) {
        return mediaFormMapper.listByMediaTypeId(mediaTypeId);
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'queryAllPriceColumns'")
    public List<MediaForm> queryAllPriceColumns() {
        return mediaFormMapper.queryAllPriceColumns();
    }

    @Override
    @Cacheable(value = CACHE_KEY,key="'code=f&mediaTypeId'+#mediaTypeId")
    public List<MediaForm> queryPriceColumnsByTypeId(Integer mediaTypeId) {
        return mediaFormMapper.queryPriceColumnsByTypeId(mediaTypeId);
    }

}
