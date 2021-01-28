package com.qinfei.qferp.mapper.crm;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.crm.CrmCompanyProtect;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * (TCrmCompanyProtect)表数据库访问层
 *
 * @author jca
 * @since 2020-08-14 16:06:24
 */
public interface CrmCompanyProtectMapper extends BaseMapper<CrmCompanyProtect, Integer> {

    @Select("select id as id, " +
            "company_id as companyId, " +
            "company_name as companyName, " +
            "apply_id as applyId, " +
            "apply_name as applyName, " +
            "apply_dept_id as applyDeptId, " +
            "apply_dept_name as applyDeptName, " +
            "protect_level as protectLevel, " +
            "state as state " +
            "from  t_crm_company_protect where id = #{protectId}")
    CrmCompanyProtect getCompanyIdByProtectId(@Param("protectId") Integer protectId);
}