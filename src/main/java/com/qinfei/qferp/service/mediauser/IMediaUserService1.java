package com.qinfei.qferp.service.mediauser;

import com.qinfei.qferp.entity.biz.Article;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.OutputStream;
import java.util.Map;

public interface IMediaUserService1 {

    //驳回
    void turnDown(@RequestParam Article article);

    //安排
    void arrange(Article article);

    //发布
    @Caching(evict = {
            @CacheEvict(value = "mediaAudit", allEntries = true),
            @CacheEvict(value = "media1", allEntries = true)
    })
    int publish(Map map, Integer updatePrice);

    //稿件移交
    void yj(String artId, Integer mediaUserId,String mediaUserName);

    //判断价格浮动是否需要修改媒体的单价
    boolean priceFloat(Article article);

    //批量导入稿件模板
    void exportTemplate(Map<String, Object> map, OutputStream outputStream);
}
