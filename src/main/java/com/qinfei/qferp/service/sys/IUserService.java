package com.qinfei.qferp.service.sys;

import java.util.List;
import java.util.Map;

import com.qinfei.core.entity.Dict;
import com.qinfei.qferp.entity.sys.Post;
import com.qinfei.qferp.entity.sys.Role;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;

import javax.jws.soap.SOAPBinding;

public interface IUserService {

    String CACHE_KEY = "user";
    String CACHE_LIST_KEY = "users";
    String PROPOSE_CACHE_LIST = "proposeTipsUsers";

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    int add(User user);

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    int addSelective(User user);

    User getById(Integer id);

    /**
     * 去除缓存的影响
     *
     * @param id
     * @return
     */
    User getById2(Integer id);

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    int update(User user);

    PageInfo<User> listPg(int pageNum, int pageSize, Map map);

    PageInfo<User> listPg(int pageNum, int pageSize);

    PageInfo<User> list();

    List<User> list(User user);

    List<User> listUser(User user);

    User login(User user);

    //修改状态

    void submitUserRole(Integer userId, String checkId,boolean XMZJFlag);

    /**
     * 清空用户的角色信息；
     *
     * @param userId：用户ID；
     */
    void delUserRoleByUserId(int userId);

    void addBatch(Integer userId, Integer roleId, Integer creator);

    /**
     * 查看其他员工角色id
     * @return
     */
    Integer selectMRJS();

    /**
     * 批量清空用户的角色信息；
     *
     * @param params：参数集合；
     */
    void delUserRoleByUserIds(Map<String, Object> params);

    void delById(Integer id);

    /**
     * 批量删除用户；
     *
     * @param params：参数集合；
     */
    void delByIds(Map<String, Object> params);

    List<User> listAll();

    void updatePassword(Integer id, String password);

    List<User> queryUserByUserName(String userName);

    PageInfo<Map> queryUserInfo(Pageable pageable);

    List<User> queryUserByDeptId(Integer deptId);

    List<User> queryDeptUsers(Integer deptId);

    @Cacheable(value = CACHE_LIST_KEY, key = "'type='+type")
    List<User> listByType(String type);

    // @Cacheable(value = CACHE_LIST_KEY, key =
    // "'type='+#type+',companyCode='+#companyCode")
    List<User> listPart(String type);

    List<User> secretary();

    List<User> listPartAll(String type, String companyCode);

    List<User> listPartAll2(String type, String companyCode);

    List<User> listPartAll3(String type, String companyCode);

    @Cacheable(value = CACHE_LIST_KEY, key = "'type='+#type+',companyCode='+#companyCode")
    List<User> listByTypeAndCompanyCode(String type, String companyCode, Integer handoverState);

    @Cacheable(value = CACHE_LIST_KEY, key = "'type=MJ&id='+#id")
    List<User> listByMediaTypeUserId(Integer id);

    @Cacheable(value = CACHE_LIST_KEY, key = "'type='+type+',code='+code+',companyCode='+companyCode")
    List<User> listByTypeAndCode(String type, String code, String companyCode);

    List<User> listPastMedia(Integer deptId);

    List<User> administrativePersonnel(String companyCode);

    @Cacheable(value = CACHE_LIST_KEY, key = "'type='+#type+',code='+#code+',companyCode=ALLJT'")
    List<User> listByTypeAndCodeJT(String type, String code);

    List<Map> listMgr(Map map);

    boolean batchSave(List<Map> params);

    User getCFOInfo(String companyCode);

    List<User> queryCFOInfo(String companyCode);

    // 获取出纳信息
    User getTellerInfo(String companyCode);

    // 获取出纳信息
    List<User> queryTellerInfo(String companyCode);

    // 获取会计信息
    User getAccountingInfo(String companyCode);

    // 获取会计信息
    List<User> queryAccountingInfo(String companyCode);

    // 获取财务部长信息
    User getCWBZInfo(String companyCode);

    // 获取财务部长信息
    List<User> queryCWBZInfo(String companyCode);

    // 获取财务助理信息
    User getCWZLInfo(String companyCode);

    // 获取财务助理信息
    List<User> queryCWZLInfo(String companyCode);

    //或取财务总监的信息
    List<User> queryCWZJInfo(String companyCode);

    User getCEOInfo(String companyCode);

    // 获取总经理CEO信息
    List<User> queryCEOInfo(String companyCode);

    Boolean getCEOFlag(Integer userId);

    // 获取人事部长信息
    User getRSBZInfo(String companyCode);

    // 获取人事列表
    List<User> queryRSList(String companyCode);

    // 获取行政部长信息
    User getXZBZInfo(String companyCode);

    // 获取行政列表
    List<User> queryXZList(String companyCode);

    // 获取行政总监信息
    User getXZZJInfo(String companyCode);

    // 获取总监
    User getZJInfo(String companyCode, String userId);

    // 获取总经理CEO信息
    String getMJType(Integer userId);

    //通过部门id获取部门
    List<User> getUserByDeptId(Integer deptId);

    //获取总监办和总经办的成员
    List<User> getUserByManger();

    PageInfo<User> queryByRoleId(int pageNum, int pageSize, Map map);

    /**
     * 根据媒体ID查询媒介人员列表
     *
     * @param mediaId
     * @return
     */
    List<User> listMJByMediaId(Integer mediaId);

    /**
     * 根据媒体类型ID查询媒介人员列表
     *
     * @param mediaId
     * @return
     */
    List<User> listMJByMediaTypeId(Integer mediaId);

    /**
     * 根据媒体类型ID查询媒介人员列表
     *
     * @param mediaId
     * @return
     */
    List<User> listPastMJByMediaTypeId(Integer mediaId, String companyCode);

    /**
     * 根据媒体类型ID查询媒介人员列表
     *
     * @param mediaId
     * @return
     */
    List<User> listPastMJByMediaTypeId2(Integer mediaId, String companyCode);

    /**
     * 根据媒体类型ID查询媒介人员列表（区分公司）
     */
    List<User> listMJByPlateId(Integer plateId);

    /**
     * 根据媒体类型ID查询媒介人员列表（区分公司）
     */
    List<User> listMJByPlateId2(Integer plateId);
    /**
     * 获取所有用户信息；
     *
     * @return ：存储用户信息的集合；
     */
    Map<Integer, User> listAllUserMap();

    /**
     * 查询所有的用户信息集合，key为用户名称，用于导入数据获取ID；
     *
     * @param mediaType：媒体板块类型；
     * @return ：用户信息集合；
     */
    Map<String, Integer> listAllUserNameMap(int mediaType);

    /**
     * 联系人信息获取；
     *
     * @param queryContent：查询内容；
     * @param pageNum：查询页码；
     * @return ：拼接完毕的分页内容；
     */
    String listUserInfo(String queryContent, int pageNum);

    /**
     * 根据用户id和角色类型查看该用户是否有该角色
     *
     * @param userId
     * @param roleType
     * @return
     */
    Boolean queryRoleByUserIdAndRoleType(Integer userId, String roleType);

    List<User> queryUserByDeptIdONLY(Integer deptId);

    /**
     * 查询指定部门的负责人信息；
     *
     * @param deptId：部门ID；
     * @return ：部门的负责人信息；
     */
    List<User> listLeaderByDeptId(int deptId);

    /**
     * 查询指定审核状态的负责人信息；
     *
     * @param state：状态，定义参com.qinfei.qferp.entity.crm.Const；
     * @return ：指定审核状态的负责人信息；
     */
    List<User> listLeaderByState(int state);

    /**
     * 查询原来的保护状态
     *
     * @param dockingPeople
     * @return
     */
/*    int queryCustState(Integer dockingPeople);

    *//**
     * 查询所有的不建议的理由
     *
     * @param dockingPeople
     * @return
     *//*
    List<Map> queryUserCust(int dockingPeople);*/

    /**
     * 修改客户保护数量
     *
     * @param map
     */
/*    void updateUserCust(Map map);*/

    /**
     * 添加客户保护记录
     *
     *//*
    ResponseData addUserCust(Integer saveState, Integer dockingPeople, String reason, Integer userId);*/

    Boolean checkUserName(Integer id, String userName);

    /**
     * 根据公司查询用户
     *
     * @param companyCode
     * @return
     */
    List<User> queryByCompanyCode(String companyCode);

    /**
     * 根据条件查询用户
     *
     * @param map
     * @return
     */
    List<User> queryUserByCondition(Map map);

    /**
     * 查询未录入建议的用户
     *
     * @return
     */
    PageInfo<Map> queryNoProposeUser(Map map, Pageable pageable);

    /**
     * 根据用户姓名和联系电话查询信息；
     *
     * @param name：姓名；
     * @param phone：联系电话；
     * @return ：查询结果；
     */
    List<User> selectUserId(String name, String phone);

    /**
     * 判断用户是否为部门的最高领导；
     *
     * @param userId：用户ID；
     * @param deptId：部门ID；
     * @return ：是否为该部门最高领导，true为是，false为否；
     */
    boolean isDeptLeader(int userId, int deptId);

    List<User> listExclude(Integer postId, int[] excludeIds);

    /**
     * 根据查询条件获取相关的用户信息；
     *
     * @param params：查询条件集合；
     * @return ：查询结果集合；
     */
    List<User> listByParams(Map<String, Object> params);

    /**
     * 获取某个部门下的所有员工
     *
     * @param deptId
     * @return
     */
    @Cacheable(value = "User_Childs", key = "'deptId='+#deptId")
    String getChilds(Integer deptId);

    @Cacheable(value = CACHE_LIST_KEY, key = "'userIds='+#userIds")
    List<User> listByUserIds(String userIds);

    @Cacheable(value = CACHE_LIST_KEY, key = "'userIds='+#userIds+',roleType='+#roleType")
    List<User> listByUserIdsAndRoleType(String userIds, String roleType);

    /**
     * 获取某个部门下的所有员工
     *
     * @param deptId
     * @return
     */
    @Cacheable(value = CACHE_LIST_KEY, key = "'deptId='+#deptId")
    List<User> listByDeptId(Integer deptId);

    @Cacheable(value = CACHE_LIST_KEY, key = "'companyCode='+#companyCode+',roleType='+#roleType")
    List<User> listByCompanyCodeAndRoleType(String companyCode, String roleType);

    //@Cacheable(value = CACHE_KEY, key = "'deptId='+#deptId+',roleType='+#roleType")
    List<User> deptUsers(Integer deptId, String roleType);

    @Cacheable(value = "userIds", key = "'roleType='+#roleType")
    List<Integer> listUserIdsByRoleType(String roleType);

    /**
     * 查询指定公司的职位（分页）
     *
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Map> getCompanyPost(Map map, Pageable pageable);

    /**
     * 获取指定公司的职位
     *
     * @param map
     * @return
     */
    List<Map> queryCompanyPost(Map map);

    /**
     * 根据id获取职位信息
     *
     * @param id
     * @return
     */
    Post getPostById(Integer id);

    /**
     * 根据条件查询职位信息
     *
     * @param name
     * @param companyCode
     * @return
     */
    Post getPostInfo(String name, String companyCode, Integer id);

    /**
     * 判断是否可以删除职位
     *
     * @param id
     * @return
     */
    Post queryByDeletePost(Integer id);

    /**
     * 增加部门职位
     *
     * @param post
     */
    @CacheEvict(value = "EntryAllPost", allEntries = true)
    void saveDeptPost(Post post);

    /**
     * 修改部门职位
     *
     * @param post
     */
    @CacheEvict(value = "EntryAllPost", allEntries = true)
    void updateDeptPost(Post post);

    /**
     * 删除部门职位
     *
     * @param id
     */
    @CacheEvict(value = "EntryAllPost", allEntries = true)
    void delDeptPost(@Param("id") Integer id);

    /**
     * 插入部门职位关系
     *
     * @param map
     */
    @CacheEvict(value = "EntryAllPost", allEntries = true)
    void insertDeptPost(List<Map> map);

    /**
     * 删除部门职位关系
     *
     * @param deptId
     */
    @CacheEvict(value = "EntryAllPost", allEntries = true)
    void deletePost(Integer deptId);

    /**
     * 绑定职位时显示
     *
     * @param companyCode
     * @param deptId
     * @return
     */
    List<Map> queryDeptPost(String companyCode, Integer deptId);

    /**
     * 根据部门ID和角色类型获取该部门下人员
     *
     * @return
     */
    List<User> listUserByDeptAndRole(Map map);

    /**
     * 根据部门ID和角色类型获取该部门下人员（未移交）
     *
     * @return
     */
    List<User> listUserByDeptAndRole2(Map map);

    List<User> listUserByDeptAndRoleJT(Map map);

    //根据部门ID和角色类型获取该部门下人员（未移交）
    List<User> listUserByDeptAndRole3(Map<String, Object> param);

    List<Dict> listByTypeAndCode();

    List<Role> characterName(String nameQc);

    List<Role> getRoleByType(String nameQc);

    List<User> listBusinessPart(String name);

    List<User> listDeptUser();

    @CacheEvict(value = CACHE_KEY, key = "'userName=' + #user.userName+'&pwd='+#user.password")
    void logout(User user);

    Boolean validateLogin(User user);

    Boolean svaeUpdatePwdRecord(Integer userId, String pwd);

    User loginOther(String userName, String password);

    User getUserByUsername(String username);

    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true)})
    void onHandover(Integer id);

    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true)})
    void offHandover(Integer id);
    //用户新增编辑进行用户名去重
    //Boolean checkDuplicateUserName(Map map);
    ResponseData checkDuplicateUserName(Integer id, String name);
//    //用户姓名去重
//    Boolean checkDuplicateName(Map map);

    ResponseData checkDuplicateName(Integer id, String name);

    void back(Integer id);

    List<Map> listUserByTypeAndCompanyCode(Map map);

    List<Map> listUserByCompanyCode(Map map);

    //查询需要被强制提醒填写建议的人员
    @Cacheable(value = PROPOSE_CACHE_LIST,  key = "'companyCode'+#companyCode")
    List<Integer> querySuggestHintData(String companyCode);

    //建议人员缓存
    @CacheEvict(value = PROPOSE_CACHE_LIST, allEntries = true)
    void delSuggestUserCache();
    //查询部长的上级审核人员
    List<User> queryUserByRoleType(Map map);

    Map getUserByUserId(Integer id);

    PageInfo<List<Map>> intranetlistPg(int pageNum, int pageSize,String keyword);

    PageInfo<List<Map>> intranetUserListPg(int pageNum, int pageSize, String keyword);

    User intranetGetUserByUsername(String username);
}