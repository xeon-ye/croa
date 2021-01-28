package com.qinfei.qferp.service.impl.media1;

import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.entity.media1.MediaType1;
import com.qinfei.qferp.mapper.media1.MediaType1Mapper;
import com.qinfei.qferp.service.media1.IMediaType1Service;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaType1ServiceImpl
 * @Description 媒体类型
 * @Author xuxiong
 * @Date 2019/6/26 0026 17:06
 * @Version 1.0
 */
@Service
public class MediaType1ServiceImpl  implements IMediaType1Service {
    @Autowired
    private MediaType1Mapper mediaType1Mapper;

    @Override
    public List<MediaType1> listByPlateId(Integer plateId) {
        return mediaType1Mapper.listByPlateId(plateId);
    }

    @Override
    public Map<String, Integer> listMediaTypeByPlateId(Integer plateId) {
        List<MediaType1> mediaType1List = mediaType1Mapper.listByPlateId(plateId);
        Map<String, Integer> datas = new HashMap<>();
        if(CollectionUtils.isNotEmpty(mediaType1List)){
            for (MediaType1 data : mediaType1List) {
                datas.put(data.getName(), data.getId());
            }
        }
        return datas;
    }
}
