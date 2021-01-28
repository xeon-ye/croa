package com.qinfei.qferp.service.media;

import com.qinfei.qferp.entity.media.MediaForm;
import com.qinfei.qferp.service.impl.media.MediaFormService;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface IMediaFormService {
    List<MediaForm> all();

    List<MediaForm> list(MediaForm mediaForm);

    List<MediaForm> listByMediaTypeId(Integer mediaTypeId);

    List<MediaForm> queryAllPriceColumns();

    List<MediaForm> queryPriceColumnsByTypeId(Integer mediaTypeId);
}
