package com.qinfei.qferp.mapper.crm;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.crm.CrmCompany;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 表：crm客户公司表(TCrmCompany)表数据库访问层
 *
 * @author jca
 * @since 2020-07-07 17:27:22
 */
public interface CrmCompanyMapper extends BaseMapper<CrmCompany, Integer> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CrmCompany getById(Integer id);

    CrmCompany getByName(String name);

    CrmCompany getByCompanyUserId(Integer companyUserId);

    CrmCompany getByUserId(Integer companyUserId);

    Map getByIdDetail(Integer id);

    int updateCompany(Map map);

    List<Map> queryCompanyProduct(Integer companyId);

    Map getProductById(Integer id);

    void saveProduct(Map map);

    int updateProduct(Map map);

    List<Map> queryCompanyConsumer(Integer companyId);

    Map getConsumerById(Integer id);

    void saveConsumer(Map map);

    int updateConsumer(Map map);

    Map queryProtectByProtectId(@Param("protectId") Integer protectId);

    List<Map> listProtect(Map map);

    void saveProtect(Map map);

    int updateProtect(Map map);

    Map getProtect(Integer protectId);

    List<Map> trackList(Map map);

    int saveCompanyTrack(Map map);

    List<Map> getRepeadCompanyName();

    List<CrmCompany> getEarliestByName(String name);

    int deleteById(Integer id);
    int updateUserCompanyId(@Param("oldCompanyId") Integer oldCompanyId, @Param("newCompanyId") Integer newCompanyId);
    int updateOrderCompanyId(@Param("oldCompanyId") Integer oldCompanyId, @Param("newCompanyId") Integer newCompanyId);
    int updateInvoiceCompanyId(@Param("oldCompanyId") Integer oldCompanyId, @Param("newCompanyId") Integer newCompanyId);
    int updateRefundCompanyId(@Param("oldCompanyId") Integer oldCompanyId, @Param("newCompanyId") Integer newCompanyId);

    int updateOrderCompanyInfo(@Param("companyUserId") Integer companyUserId, @Param("newCompanyId") Integer newCompanyId, @Param("newCompanyName") String newCompanyName);
    int updateInvoiceCompanyInfo(@Param("companyUserId") Integer companyUserId, @Param("newCompanyId") Integer newCompanyId, @Param("newCompanyName") String newCompanyName);
    int updateRefundCompanyInfo(@Param("companyUserId") Integer companyUserId, @Param("newCompanyId") Integer newCompanyId, @Param("newCompanyName") String newCompanyName);
    int updateMessCompanyInfo(@Param("companyUserId") Integer companyUserId, @Param("newCompanyId") Integer newCompanyId, @Param("newCompanyName") String newCompanyName);

    @Update("UPDATE t_crm_company_user SET protect_level = 1,protect_strong = 1 WHERE normalize = 1 AND company_id = #{value}")
    int updateByNormalize(Integer companyId);

}