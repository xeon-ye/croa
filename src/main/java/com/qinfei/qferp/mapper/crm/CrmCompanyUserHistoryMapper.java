package com.qinfei.qferp.mapper.crm;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.crm.CrmCompanyUserHistory;

import java.util.List;
import java.util.Map;

/**
 * 表：对接人信息(TCrmCompanyUserHistory)表数据库访问层
 *
 * @author jca
 * @since 2020-07-29 17:56:01
 */
public interface CrmCompanyUserHistoryMapper extends BaseMapper<CrmCompanyUserHistory, Integer> {

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
    CrmCompanyUserHistory getById(Integer id);


}