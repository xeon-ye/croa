package com.qinfei.qferp.service.media;

import com.qinfei.qferp.entity.media.FileEntitys;

public interface IFileEntityService {
    FileEntitys getByArticleId(Integer artId, Integer type);
}
