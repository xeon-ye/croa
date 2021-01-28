package com.qinfei.qferp.service.impl.media;

import com.qinfei.qferp.entity.media.MediaTerm;
import com.qinfei.qferp.mapper.media.MediaTermMapper;
import com.qinfei.qferp.service.media.IMediaTermService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class MediaTermService implements IMediaTermService {

    @Autowired
    MediaTermMapper mediaTermMapper;
    private static final String CACHE_KEY = "mediaTerm";
    private static final String CACHE_KEYS = "mediaTerms";

    @Override
    @Cacheable(value = CACHE_KEYS)
    public List<MediaTerm> list(Integer typeId) {
        List<MediaTerm> list = mediaTermMapper.listByTypeId(typeId);
        for (MediaTerm mediaTerm : list) {
            Integer dataType = mediaTerm.getDataType();
            if (dataType == 0) {
                String sql = mediaTerm.getTermSql();
                if (!StringUtils.isEmpty(sql)) {
                    mediaTerm.setDatas(mediaTermMapper.dictSQL(sql));
                }
            }
        }
        return list;
    }

    @Override
    @Cacheable(value = CACHE_KEY)
    public MediaTerm getByTerm(MediaTerm term) {
        List<MediaTerm> list = mediaTermMapper.list(term);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    @Override
//    @Cacheable(value = "mediaTerms", key = "#root.methodName+'-'+#typeId")
    public List<MediaTerm> listByTypeId(Integer typeId) {
        return mediaTermMapper.listByTypeId(typeId);
    }
}
