package com.qinfei.qferp.service.impl.media1;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media.MediaTerm;
import com.qinfei.qferp.entity.media1.MediaTerm1;
import com.qinfei.qferp.mapper.media1.MediaTerm1Mapper;
import com.qinfei.qferp.service.media1.IMediaTerm1Service;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.PageOrder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaTerm1ServiceImpl
 * @Description 媒体查询条件表接口
 * @Author xuxiong
 * @Date 2019/7/6 0006 15:34
 * @Version 1.0
 */
@Service
public class MediaTerm1ServiceImpl implements IMediaTerm1Service {
    @Autowired
    private MediaTerm1Mapper mediaTerm1Mapper;

    @Override
    public void deleteBatch(List<Integer> ids) {
        mediaTerm1Mapper.deleteBatch(ids);
    }

    @Override
    public void save(MediaTerm1 mediaTerm1) {
        if(mediaTerm1Mapper.getMediaTermCount(mediaTerm1) > 0){
            throw new QinFeiException(1002, "当前媒体板块已存在相同的列名");
        }
        mediaTerm1.setCreatorId(AppUtil.getUser().getId());
        mediaTerm1Mapper.insert(mediaTerm1);
    }

    @Override
    public void update(MediaTerm1 mediaTerm1) {
        if(mediaTerm1Mapper.getMediaTermCount(mediaTerm1) > 0){
            throw new QinFeiException(1002, "当前媒体板块已存在相同的列名");
        }
        mediaTerm1Mapper.updateById(mediaTerm1);
    }

    @Override
    public List<MediaTerm1> findAllByMediaPlateId(Integer mediaPlateId) {
        List<MediaTerm1> result = mediaTerm1Mapper.listMediaTermByPlateId(mediaPlateId);
        if(CollectionUtils.isNotEmpty(result)){
            for(MediaTerm1 mediaTerm1 : result){
                if(mediaTerm1.getDataType() != null && "sql".equals(mediaTerm1.getDataType()) && StringUtils.isNotEmpty(mediaTerm1.getDbSql())){
                    mediaTerm1.setDatas(mediaTerm1Mapper.dictSQL(mediaTerm1.getDbSql()));
                }
            }
        }
        return result;
    }

    @Override
    public PageInfo<Map> list(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> mapList = mediaTerm1Mapper.listByMediaPlateId(map);
        return new PageInfo<>(mapList);
    }
}
