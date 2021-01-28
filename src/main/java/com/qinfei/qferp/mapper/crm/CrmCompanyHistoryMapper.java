package com.qinfei.qferp.mapper.crm;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.crm.CrmCompanyHistory;

import java.util.List;
import java.util.Map;

/**
 * 表：crm客户公司表(TCrmCompanyHistory)表数据库访问层
 *
 * @author jca
 * @since 2020-07-29 17:54:55
 */
public interface CrmCompanyHistoryMapper extends BaseMapper<CrmCompanyHistory, Integer> {

    /**
     * 查询列表
     */
    List<Map> listPg(Map map);
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CrmCompanyHistory getById(Integer id);


}