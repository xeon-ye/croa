package com.qinfei.qferp.service.crm;

import com.qinfei.qferp.entity.crm.CrmCompanyUser;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * 表：对接人信息(TCrmCompanyUser)表服务接口
 *
 * @author jca
 * @since 2020-07-09 09:55:34
 */
public interface ICrmCompanyUserService {

    /**
     * 查询列表
     */
    PageInfo list(Map map, int pageNum, int pageSize);

    PageInfo listPublic(Map map, int pageNum, int pageSize);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CrmCompanyUser getById(Integer id);

    Boolean getYwFlag(Integer companyUserId, Integer userId);

    Map getBasicById(Integer id);

    int updateCompanyUser(Map map);

    void saveCompanyUserHistory(CrmCompanyUser companyUser, String oldCompanyName, Integer userId);

    void savePublic(Integer id);

    void bind(Integer id);

    int delCompanyUser(Map map);

    void saveTransfer(Map param);

    Map queryUserBasicInfoByCompanyId(Integer companyUserId, Integer loginUserId);

    List<CrmCompanyUser> queryByCompanyIdAndNormalize(Integer companyId, Integer normalize);

    PageInfo queryUserByCompanyId(Map map, int pageNum, int pageSize);

    PageInfo querySalesmanByCompanyUserId(Integer companyUserId, int pageNum, int pageSize);

    PageInfo queryHistoryCompanyUserId(Integer companyUserId, int pageNum, int pageSize);

    void doWithPublic();

    void doWithEval();

    PageInfo listCustForYW(Map map, int pageNum, int pageSize);

    PageInfo listCustForFee(Map map, int pageNum, int pageSize);

    Map getCustByCompanyUserIdAndUserId(Integer companyUserId, Integer userId);

    List<Map> listCompanyUser(Map map);

    PageInfo listInquire(Map map, int pageNumber, int pageSize);
}
