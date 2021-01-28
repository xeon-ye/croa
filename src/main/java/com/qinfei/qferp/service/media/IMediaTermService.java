package com.qinfei.qferp.service.media;

import com.qinfei.qferp.entity.media.MediaTerm;

import java.util.List;

public interface IMediaTermService {

    List<MediaTerm> list(Integer typeId);

    MediaTerm getByTerm(MediaTerm term);

    List<MediaTerm> listByTypeId(Integer typeId);
}
