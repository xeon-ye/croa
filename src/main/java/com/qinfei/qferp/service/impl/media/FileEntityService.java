package com.qinfei.qferp.service.impl.media;

import com.qinfei.qferp.entity.media.FileEntitys;
import com.qinfei.qferp.mapper.media.FileEntityMapper;
import com.qinfei.qferp.service.media.IFileEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileEntityService implements IFileEntityService {
    @Autowired
    private FileEntityMapper fileEntityMapper;
    @Override
    public FileEntitys getByArticleId(Integer artId, Integer type) {
        return fileEntityMapper.selectByArtId(artId,type);
    }
}
