package com.qinfei.qferp.mapper.crm;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.crm.CrmCompanyUserSalesman;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * (TCrmCompanyUserSalesman)表数据库访问层
 *
 * @author jca
 * @since 2020-07-08 10:08:47
 */
public interface CrmCompanyUserSalesmanMapper extends BaseMapper<CrmCompanyUserSalesman, Integer> {

    CrmCompanyUserSalesman getByCompanyUserId(Integer companyUserId);

    int expireSalesman(Map map);

    int expireSalesmanSingle(Map map);

    List<Map> queryByCompanyUserId(Integer companyUserId);

    void saveSalesman(Map map);

    int updateSalesman(List<Map> list);

    void saveBatch(List<Map> list);

}