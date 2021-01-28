package com.qinfei.qferp.mapper.crm;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.crm.CrmSearchCache;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企查查客户缓存表(TCrmSearchCache)表数据库访问层
 *
 * @author xuxiong
 * @since 2020-06-29 14:23:37
 */
public interface CrmSearchCacheMapper extends BaseMapper<CrmSearchCache, Integer> {
    //新增数据
    int save(CrmSearchCache crmSearchCache);

    //批量新增数据
    int batchSave(List<CrmSearchCache> list);

    //更新
    int edit(CrmSearchCache crmSearchCache);

    //根据参数查询公司列表
    List<CrmSearchCache> listCrmSearchByParam(@Param("keyword") String keyword);

    //根据公司名称获取公司
    List<String> listCrmSearchByCompanyName(@Param("keywords") List<String> keywords);
}