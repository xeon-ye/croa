package com.qinfei.qferp.mapper;

import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.media.MediaInfo;

import java.math.BigDecimal;
import java.util.Map;

public interface MediaUserMapper {
    /**
     * 通过稿件查询单价
     * @param article
     * @return
     */
    BigDecimal getMediaInfoPrice(Article article);

    /**
     * 通过稿件获取
     * @param article
     * @return
     */
    BigDecimal getArticleOutgoAmount(Article article);

    /**
     * 修改mediaInfoState
     * @param mediaInfo
     * @return
     */
    int updateMediaInfoState(MediaInfo mediaInfo);

    /**
     * 修改media表的状态和价格
     * @param map
     * @return
     */
    int updateMediaStatePrice(Map map);
}
