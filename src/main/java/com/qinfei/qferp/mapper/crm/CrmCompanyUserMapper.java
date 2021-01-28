package com.qinfei.qferp.mapper.crm;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.crm.CrmCompanyUser;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 表：对接人信息(TCrmCompanyUser)表数据库访问层
 *
 * @author jca
 * @since 2020-07-08 10:08:37
 */
public interface CrmCompanyUserMapper extends BaseMapper<CrmCompanyUser, Integer> {

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
    CrmCompanyUser getById(Integer id);
    Integer getYwInfo(@Param("companyUserId")Integer companyUserId, @Param("userId") Integer userId);
    /**
     * 通过id获取客户登记的基本信息
     * @param id
     * @return
     */
    Map getBasicById(Integer id);

    List<CrmCompanyUser> queryByCompanyIdAndNormalize(@Param("companyId") Integer companyId, @Param("normalize") Integer normalize);
    Map<String, Date> getMaxDate(@Param("companyId") Integer companyId, @Param("normalize") Integer normalize);

    List<CrmCompanyUser> queryByCompanyIdAndMobile(@Param("companyId") Integer companyId, @Param("mobile") String mobile);
    List<Map> queryByCompanyIdAndMobileOn(@Param("companyId") Integer companyId, @Param("mobile") String mobile);
    List<Map> queryByCompanyIdAndMobilePublic(@Param("companyId") Integer companyId, @Param("mobile") String mobile);
    List<CrmCompanyUser> queryByCompanyIdAndMobileDel(@Param("companyId") Integer companyId, @Param("mobile") String mobile);

    List<CrmCompanyUser> queryByCompanyNameAndMobileNotId(@Param("companyName") String companyName, @Param("mobile") String mobile, @Param("id") Integer id);
    List<Map> queryByCompanyNameAndMobileNotIdOn(@Param("companyName") String companyName, @Param("mobile") String mobile, @Param("id") Integer id);
    List<Map> queryByCompanyNameAndMobileNotIdPublic(@Param("companyName") String companyName, @Param("mobile") String mobile, @Param("id") Integer id);

    List<CrmCompanyUser> listByCompanyId(Integer companyId);

    List<Map> listByCompanyIdAndState(Integer companyId);

    Integer countByUserIdToday(@Param("loginUserId") Integer loginUserId);

    Integer countByUserIdAndStateAndLevel(@Param("loginUserId") Integer loginUserId, @Param("protectLevel") Integer protectLevel);

    Map getByIdAndState(Integer id);

    int updateCompanyUser(Map map);
    /**
     * 查询列表
     */
    List<Map> listPublic(Map map);

    /**
     * 客户查询
     * @param map 入参
     * @return list
     */
    List<Map> listInquire(Map map);

    int savePublic(Map map);

    int doWithPublic();

    Map queryUserBasicInfoByCompanyId(@Param("companyUserId") Integer companyUserId, @Param("loginUserId") Integer loginUserId);

    List<Map> getProtectNum(@Param("loginUserId") Integer loginUserId);

    List<Map> queryUserByCompanyId(Map map);

    int updateUserByCompanyId(Map map);

    int countArticleByCompanyUserId(Integer companyUserId);

    List<Map> queryByProtectLevel(@Param("protectLevel") Integer protectLevel,
                                  @Param("TRACK_EVAL_DAY") Integer TRACK_EVAL_DAY,
                                  @Param("DEAL_EVAL_DAY") Integer DEAL_EVAL_DAY);

    Map getTrackDate(@Param("companyUserId") Integer companyUserId, @Param("userId") Integer userId);

    Map getDealDate(@Param("companyUserId") Integer companyUserId, @Param("userId") Integer userId);

    void saveEval(Map map);

    Integer getAuditCount(List<Map> list);

    List<Map> listCustForYW(Map map);

    Map getCustByCompanyUserIdAndUserId(@Param("companyUserId") Integer companyUserId, @Param("userId") Integer userId);

    void updateDealTimeByCompanyId(Map map);

    void initDealTimeByCompanyId(Map map);

    void initDealTimeByCompanyUserId(@Param("companyUserId") Integer companyUserId, @Param("updateUserId") Integer updateUserId);

    List<Map> listCompanyUser(Map map);

    int resetEvalTime();

    int resetDealTime();

}