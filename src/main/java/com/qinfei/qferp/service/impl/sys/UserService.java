package com.qinfei.qferp.service.impl.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.exception.ResultEnum;
import com.qinfei.core.utils.IpUtils;
import com.qinfei.core.utils.MD5Utils;
import com.qinfei.qferp.entity.propose.ProposeTips;
import com.qinfei.qferp.entity.sys.*;
import com.qinfei.qferp.mapper.sys.*;
import com.qinfei.qferp.service.crm.IStatisticsService;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.service.propose.IProposeTipsService;
import com.qinfei.qferp.service.sys.IDeptService;
import com.qinfei.qferp.service.sys.IRoleService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DeptParseUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class UserService extends BaseService implements IUserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    PostMapper postMapper;
    @Autowired
    DeptMapper deptMapper;
    @Autowired
    IRoleService roleService;
    @Autowired
    IDeptService deptService;
    @Autowired
    IStatisticsService statisticsService;
    @Autowired
    UserService userService;
    @Autowired
    SysUpdatePasswordMapper sysUpdatePasswordMapper;
    @Autowired
    SysConfigMapper sysConfigMapper;
    @Autowired
    IProposeTipsService proposeTipsService;


    @Override
    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true)})
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public int add(User user) {
        user.setPassword(MD5Utils.encode(user.getPassword()));
        user.setCreator(AppUtil.getUser().getId());
        user.setCreateTime(new Date());
        user.setId(null);
        user.setState(IConst.STATE_FINISH);
        Integer roleId= userMapper.selectMRJS();
        Integer  bb = userMapper.insert(user);
        userMapper.addBatch(user.getId(), roleId ,AppUtil.getUser().getId());
        return bb;
    }

    @Override
    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true)})
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public int addSelective(User user) {
        return userMapper.insert(user);
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'id='+#id")
    @Transactional(readOnly = true)
    public User getById(Integer id) {
        return userMapper.getById(id);
    }

    @Override
    public User getById2(Integer id) {
        return userMapper.getById(id);
    }

    @Override
    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true), @CacheEvict(value = "role_isRole", allEntries = true), @CacheEvict(value = "roles", key = "'userId='+#user.id")})
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public int update(User user) {
        return userMapper.update(user);
    }

    @Override
    public PageInfo<User> listPg(int pageNum, int pageSize) {
        try {
            PageHelper.startPage(pageNum, pageSize);
            List<User> list = userMapper.listAll();
            return new PageInfo<>(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public PageInfo<User> listPg(int pageNum, int pageSize, Map map) {
        try {

            if (AppUtil.getUser() != null) {
                String companyCode = AppUtil.getUser().getCompanyCode();
                if (!companyCode.equals("XH") && !companyCode.equals("JT")) {
                    map.put("companyCodeQc", companyCode);
                }
            }
            if (!ObjectUtils.isEmpty(map.get("deptId1"))) {//当且仅指定了部门时
                Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId1")));//获取请求的部门ID
                String deptIds = userService.getChilds(deptId);
                if (deptIds.contains("$,")) {
                    deptIds = deptIds.substring(2);
                }
                map.put("deptIds", deptIds);
            }
            PageHelper.startPage(pageNum, pageSize);
            List<User> list = userMapper.listPg(map);
            Map<Integer, List<Role>> userRoles = roleService.listUserRoles();
            Integer userId;
            for (User user : list) {
                userId = user.getId();
                user.setRoles(userRoles.get(userId));
                user.setMJ(roleService.isRole(userId, IConst.ROLE_TYPE_MJ));
                user.setYW(roleService.isRole(userId, IConst.ROLE_TYPE_YW));
            }
            return new PageInfo<>(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'LIST=LIST'")
    public PageInfo<User> list() {
        PageHelper.startPage(1, 2);
        List<User> list = userMapper.all(User.class);
        return new PageInfo<>(list);
    }

    @Override
    // @Cacheable(value = CACHE_LIST_KEY, key = "'userList=userList'")
    public List<User> list(User user) {
        return userMapper.list(user);
    }

    @Override
    // @Cacheable(value = CACHE_LIST_KEY, key = "'userList=userList'")
    public List<User> listUser(User user) {
        User emp = AppUtil.getUser();
        return userMapper.listUser(emp.getCompanyCode());
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'userName=' + #user.userName+'&pwd='+#user.password")
    public User login(User user) {
        final User userData = userMapper.getByUserName(user.getUserName());
        String password = user.getPassword();
        if (userData == null) {
            // throw new RuntimeException("用户名输入错误!");
            throw new QinFeiException(ResultEnum.ACCT_EOOR);
        }
        if (password.equals(userData.getPassword())) {
            userData.setRoles(roleService.queryRoleByUserId(userData.getId()));
                        String depts = deptService.idsByParentId(userData.getDeptId());
            userData.setDeptIdSet(new HashSet<>(Arrays.asList(depts.split(","))));
            userData.setCurrentDeptQx(currentDept(userData));
            userData.setCurrentCompanyQx(currentCompanyQx(userData));
            HttpServletRequest request = AppUtil.getRequest();
            userData.setSessionId(AppUtil.getSession().getId());
            // 图片显示优化；
            String image = userData.getImage();
            userData.setImage(StringUtils.isEmpty(image) ? "/img/mrtx_2.png" : image.replace("\\images\\", "/images/"));
            return userData;
        } else {
            // userData = new User();
            // userData.setMessage("密码输入错误!");
            // return userData;
            throw new QinFeiException(ResultEnum.PWD_ERROR);
        }
    }

    @Override
    public User loginOther(String userName, String password) {
        User user = userMapper.getByUserName(userName);
        if (StringUtils.equalsIgnoreCase(user.getPassword(), password)) {
            List<Role> roles = roleService.queryRoleByUserId(user.getId());
            if(roles!=null && roles.size()>0){
                roles.forEach(role -> {
                    role.setUpdateTime(null);
                    role.setCreateTime(null);
                });
            }
            user.setRoles(roles);
            user.setPassword(null);
            user.setUpdateTime(null);
            user.setCreateTime(null);
            user.setLoginTime(null);
            return user;
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.getByUserName(username);
    }

    /**
     * 判断当前用户是否具有当前部门的权限
     *
     * @return
     */
    private boolean currentDept(User user) {
        boolean b = false;
        int level = user.getDept().getLevel();
        List<Role> roles = user.getRoles();
        if (roles != null) {
            for (Role r : roles) {
                if (IConst.ROLE_CODE_ZJ.equals(r.getCode()) || IConst.ROLE_CODE_ZJL.equals(r.getCode()) || IConst.ROLE_CODE_FZ.equals(r.getCode()) || IConst.ROLE_CODE_ZC.equals(r.getCode()) || IConst.ROLE_CODE_FZC.equals(r.getCode()) || (IConst.ROLE_TYPE_JT.equals(r.getType()) && IConst.ROLE_CODE_KJ.equals(r.getCode()))) {
                    return true;
                }
                if (IConst.ROLE_CODE_BZ.equals(r.getCode()) && IConst.ROLE_TYPE_CW.equals(r.getType())) {
                    return true;
                }
                if (IConst.ROLE_CODE_BZ.equals(r.getCode())) {
                    if (level >= 2) {
                        return true;
                    }
                }
                if (IConst.ROLE_CODE_ZZ.equals(r.getCode())) {
                    if (level >= 3) {
                        return true;
                    }
                }
            }
        }
        return b;
    }

//    /**
//     * 判断用户是否有当前公司的权限
//     *
//     * @param user
//     * @return
//     */
//    private boolean currentCompanyQx(User user) {
//        List<Role> roles = user.getRoles();
//        if (roles != null) {
//            for (Role r : roles) {
//                if ((IConst.ROLE_CODE_ZJ.equals(r.getCode()) && !IConst.ROLE_TYPE_YW.equals(r.getType()) && !IConst.ROLE_TYPE_MJ.equals(r.getType()))
//                        || IConst.ROLE_CODE_ZJL.equals(r.getCode()) || IConst.ROLE_CODE_FZ.equals(r.getCode())
//                        || IConst.ROLE_CODE_ZC.equals(r.getCode())  || IConst.ROLE_CODE_FZC.equals(r.getCode()) || (IConst.ROLE_TYPE_JT.equals(r.getType()) && IConst.ROLE_CODE_KJ.equals(r.getCode()))) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private Set getDeptStr(Integer deptId, Set set) {
        // 加入它本身
        set.add(deptId);
        // 加入子集
        List<Dept> list = deptMapper.queryDeptByParentId(deptId);
        for (Dept dept : list) {
            getDeptStr(dept.getId(), set);
        }
        // sb.substring(sb.length()-1,sb.length()) ;
        return set;
    }

    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true)})
    public User save(User user) {
        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(value = "role_isRole", allEntries = true),
            @CacheEvict(value = "roles", key = "'userId='+#userId"),
            @CacheEvict(value = "user", key = "'userId='+#userId"),
            @CacheEvict(value = "resources", key = "'userId='+#userId"),
            @CacheEvict(value = "resources", key = "'userIdNew='+#userId"),
            @CacheEvict(value = "groups", key = "'userId='+#userId"),
            @CacheEvict(value = "menu", key = "'userId='+#userId"),
            @CacheEvict(value ="SysConfig",  allEntries = true)
    })
    public void submitUserRole(Integer userId, String checkId,boolean XMZJFlag) {
        // 删除旧的
        userMapper.delUserRoleByUserId(userId);
        // 防止为空；
        if (!StringUtils.isEmpty(checkId)) {
            // 插入新的
            List<Map<String, Object>> list = new ArrayList<>();
            String[] pArray = checkId.split(",");
            int len = pArray.length;
            for (int i = 0; i < len; i++) {
                Map<String, Object> map = new HashMap<>();
                int roleId = Integer.parseInt(pArray[i]);
                map.put("userId", userId);
                map.put("roleId", roleId);
                list.add(map);
            }
            // 批量插入
            if (list != null && list.size() > 0) {
                int size = list.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    List<Map<String, Object>> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(list.get(j));
                    }
                    userMapper.saveBatch(insertData, AppUtil.getUser().getId());
                }
            }
        }

        SysConfig sysConfig = sysConfigMapper.getOneConfigByKey("projectDirector", null);
            if (sysConfig !=null){
                String users = sysConfig.getConfigValue();
                //获取项目总监用户
                if (XMZJFlag) {
                    if(!users.contains(userId.toString())){
                        //该用户为项目总监
                        String newUser = userId + "," + users;
                        sysConfigMapper.updateConfigValue(sysConfig.getId(), AppUtil.getUser().getId(), newUser);
                    }
                }else {
                    if (users.contains(userId.toString())){
                        String[] s =  users.split(",");
                        String ns ="";
                        for (String t : s){
                            if (StringUtils.isNotEmpty(t) && userId !=Integer.parseInt(t)){
                                ns += org.apache.commons.lang3.StringUtils.join(t,",");
                            }
                        }
                        sysConfigMapper.updateConfigValue(sysConfig.getId(),AppUtil.getUser().getId(),ns);
                    }
                }
            }



    }

    /**
     * 清空用户的角色信息；
     *
     * @param userId：用户ID；
     */
    @Override
    public void delUserRoleByUserId(int userId) {
        userMapper.delUserRoleByUserId(userId);
    }

    /**
     * 用户绑定角色（调岗）
     * @param userId
     * @param roleId
     * @param creator
     */
    @Override
    @Caching(evict = {@CacheEvict(value = "role_isRole", allEntries = true),
            @CacheEvict(value = "roles", key = "'userId='+#userId"),
            @CacheEvict(value = "user", key = "'userId='+#userId"),
            @CacheEvict(value = "resources", key = "'userId='+#userId"),
            @CacheEvict(value = "resources", key = "'userIdNew='+#userId"),
            @CacheEvict(value = "groups", key = "'userId='+#userId"),
            @CacheEvict(value = "menu", key = "'userId='+#userId")
    })
    public void addBatch(Integer userId, Integer roleId, Integer creator) {
        userMapper.addBatch(userId,roleId,creator);
    }

    /**
     * 查看其他员工角色id
     * @return
     */
    @Override
    public Integer selectMRJS() {
        return userMapper.selectMRJS();
    }

    /**
     * 批量清空用户的角色信息；
     *
     * @param params：参数集合；
     */
    @Override
    public void delUserRoleByUserIds(Map<String, Object> params) {
        userMapper.delUserRoleByUserIds(params);
    }

    @Override
    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true)})
    public void delById(Integer id) {
        User user = userMapper.get(User.class, id);
        user.setState(IConst.STATE_DELETE);
        user.setUpdateTime(new Date());
        userMapper.update(user);
    }

    /**
     * 批量删除用户；
     *
     * @param params：参数集合；
     */
    @Override
    public void delByIds(Map<String, Object> params) {
        userMapper.delByIds(params);
    }

    @Cacheable(value = CACHE_LIST_KEY, key = "'listAll=listAll'")
    @Override
    public List<User> listAll() {
        return userMapper.listAll();
    }

    @Override
    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true)})
    @Transactional
    public void updatePassword(Integer id, String password) {
        svaeUpdatePwdRecord(id, password);
        userMapper.updatePassword(id, password);
    }

    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'userName='+#userName")
    public List<User> queryUserByUserName(String userName) {
        return userMapper.queryUserByUserName(userName);
    }

    @Override
    public PageInfo<Map> queryUserInfo(Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map> list = userMapper.queryUserInfo();
        for (Map m : list) {
            if (m.get("roleName") != null) {
                m.put("roleName", new String((byte[]) m.get("roleName"), StandardCharsets.UTF_8));
            }
        }
        return new PageInfo<Map>(list);
    }

    /**
     * 通过部门ID查询所有用户
     *
     * @param deptId
     * @return
     */
    @Cacheable(value = CACHE_LIST_KEY, key = "'deptId='+#deptId")
    @Override
    public List<User> queryUserByDeptId(Integer deptId) {
        Dept dept = new Dept();
        dept.setId(deptId);
        List<Dept> allDepts = deptService.listAll();
        DeptParseUtil deptParseUtil = new DeptParseUtil(allDepts);
        deptParseUtil.parse(dept);
        // 得到所有子部门
        List<Dept> depts = dept.getChildDepts();
        depts.add(dept);
        return userMapper.queryUserByDepts(depts);
    }

    /**
     * 通过部门ID查询所有用户
     *
     * @param deptId
     * @return
     */
    @Override
    public List<User> queryDeptUsers(Integer deptId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("roleType", IConst.ROLE_TYPE_YW);
//        DataSecurityUtil.addSecurity(map);
        return deptUsers(deptId, IConst.ROLE_TYPE_YW);
    }

    /**
     * 根据角色类型查询用户列表
     *
     * @param type 角色类型
     * @return
     */
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type='+#type")
    public List<User> listByType(String type) {
        return userMapper.listByType(type);
    }

    /**
     * 根据角色类型查询用户列表
     *
     * @param type 角色类型
     * @return
     */
    @Override
    // @Cacheable(value = CACHE_LIST_KEY, key = "'type='+#type")
    public List<User> listPart(String type) {
        return userMapper.listPart(type);
    }

    @Override
    public List<User> secretary(){
        User user = AppUtil.getUser();
        Map map =new HashMap();
        map.put("companyCode",user.getCompanyCode());
        return userMapper.secretary(map);
    }

    @Override
    // @Cacheable(value = CACHE_LIST_KEY, key = "'type='+#type")
    public List<User> listPartAll(String type, String companyCode) {
        return userMapper.listPartAll(type, companyCode);
    }

    @Override
    // @Cacheable(value = CACHE_LIST_KEY, key = "'type='+#type")
    public List<User> listPartAll2(String type, String companyCode) {
        return userMapper.listPartAll2(type, companyCode);
    }

    @Override
    public List<User> listPartAll3(String type, String companyCode) {
        return userMapper.listPartAll3(type, companyCode);
    }

    /**
     * 根据角色类型查询用户列表
     *
     * @param type 角色类型
     * @return
     */
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type='+#type+',companyCode='+#companyCode")
    public List<User> listByTypeAndCompanyCode(String type, String companyCode, Integer handoverState) {
        return userMapper.listByTypeAndCompanyCode(type, companyCode, handoverState);
    }

    /**
     * 根据角色类型查询用户列表
     *
     * @param id 角色类型
     * @return
     */
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=MJ&id='+#id")
    public List<User> listByMediaTypeUserId(Integer id) {
        return userMapper.listByMediaTypeUserId(id);
    }

    /**
     * 查询当前媒介同公司同板块的媒介
     */
    @Override
    public List<User> listPastMedia(Integer deptId) {
        User user = AppUtil.getUser();
        String deptIds = "";
        if(deptId != null){
            deptIds = getChilds(deptId);
            if (!StringUtils.isEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
        }
        return userMapper.listPastMedia(user.getId(), deptIds);
    }

    /**
     * 查找本公司行政人事专员
     */
    @Override
    public List<User> administrativePersonnel(String companyCode) {
        return userMapper.administrativePersonnel(companyCode);
    }

    /**
     * 根据角色类型查询用户列表
     *
     * @param type 角色类型
     * @return
     */
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type='+#type+',code='+#code+',companyCode='+#companyCode")
    public List<User> listByTypeAndCode(String type, String code, String companyCode) {
        return userMapper.listByTypeAndCode(type, code, companyCode);
    }

    @Override
    public List<User> listByTypeAndCodeJT(String type, String code) {
        return userMapper.listByTypeAndCodeJT(type, code);
    }

    @Override
    // @Cacheable(value = CACHE_LIST_KEY, key = "'type=mgr'")
    public List<Map> listMgr(Map map) {
        return userMapper.listMgr(map);
    }

    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(value = "MediaType", key = "'userId='+#params.get(0).get(\"userId\")"),
            @CacheEvict(value = "MediaPlate", key = "'userId='+#params.get(0).get(\"userId\")")})
    public boolean batchSave(List<Map> params) {
        Integer userId = (Integer) params.get(0).get("userId");
        userMapper.delUserMediaTypeByUserId(userId);
        userMapper.addUserMediaType(params);
        return true;
    }

    // 获取财务总监信息
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=CFO&companyCode='+#companyCode")
    public User getCFOInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_ZJ, companyCode);
        return list.get(0);
    }

    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=CFO&companyCode='+#companyCode")
    public List<User> queryCFOInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_ZJ, companyCode);
    }

    // 获取出纳信息
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=teller&companyCode='+#companyCode")
    public User getTellerInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_CN, companyCode);
        return list.get(0);
    }

    // 获取出纳信息
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=teller&companyCode='+#companyCode")
    public List<User> queryTellerInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_CN, companyCode);
    }

    // 获取会计信息
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=account&companyCode='+#companyCode")
    public User getAccountingInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_KJ, companyCode);
        return list.get(0);
    }

    // 获取会计信息
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=account&companyCode='+#companyCode")
    public List<User> queryAccountingInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_KJ, companyCode);
    }

    // 获取财务部长信息
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=CWBZ&companyCode='+#companyCode")
    public User getCWBZInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_BZ, companyCode);
        return list.get(0);
    }

    // 获取财务部长信息
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=CWBZ&companyCode='+#companyCode")
    public List<User> queryCWBZInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_BZ, companyCode);
    }

    // 获取财务助理信息
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=CWZL&companyCode='+#companyCode")
    public User getCWZLInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_ZL, companyCode);
        return list.get(0);
    }

    // 获取财务助理信息
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=CWZL&companyCode='+#companyCode")
    public List<User> queryCWZLInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_ZL, companyCode);
    }
    // 获取财务总监信息
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=CWZJ&companyCode='+#companyCode")
    public List<User> queryCWZJInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return listByTypeAndCode(IConst.ROLE_TYPE_CW, IConst.ROLE_CODE_ZJ, companyCode);
    }

    // 获取总经理CEO信息
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=CEO&companyCode='+#companyCode")
    public User getCEOInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_ZJB, IConst.ROLE_CODE_ZJL, companyCode);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    // 获取总经理CEO信息
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=CEO&companyCode='+#companyCode")
    public List<User> queryCEOInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return listByTypeAndCode(IConst.ROLE_TYPE_ZJB, IConst.ROLE_CODE_ZJL, companyCode);
    }

    /**
     * @return 副总以上返回true，否则返回false
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'ceoFlagId='+#userId")
    public Boolean getCEOFlag(Integer userId) {
        List<String> codes = new ArrayList<>();
        codes.add(IConst.ROLE_CODE_ZJL);
        codes.add(IConst.ROLE_CODE_FZ);
        List<User> list = userMapper.getUserRoleInfo(userId, "", codes);
        return list != null && list.size() > 0;
    }

    // 获取人事部长信息
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=RSBZ&companyCode='+#companyCode")
    public User getRSBZInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_RS, IConst.ROLE_CODE_BZ, companyCode);
        return list.get(0);
    }

    // 获取人事部门人员列表
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=RS&companyCode='+#companyCode")
    public List<User> queryRSList(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return userMapper.listByTypeAndCompanyCode(IConst.ROLE_TYPE_RS, companyCode, 0);
    }

    // 获取行政部长信息
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=XZBZ&companyCode='+#companyCode")
    public User getXZBZInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_XZ, IConst.ROLE_CODE_BZ, companyCode);
        return list.get(0);
    }

    // 获取行政部门人员列表
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'type=XZ&companyCode='+#companyCode")
    public List<User> queryXZList(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        return userMapper.listByTypeAndCompanyCode(IConst.ROLE_TYPE_XZ, companyCode, 0);
    }

    // 获取行政总监信息
    @Override
    public User getXZZJInfo(String companyCode) {
        if (StringUtils.isEmpty(companyCode)) {
            companyCode = IConst.COMPANY_CODE_XH;
        }
        List<User> list = listByTypeAndCode(IConst.ROLE_TYPE_XZ, IConst.ROLE_CODE_ZJ, companyCode);
        return list.get(0);
    }

    //根据部门获取部门总监
    @Override
    public User getZJInfo(String companyCode, String userId) {
        User result = null;
        try{
            if (StringUtils.isEmpty(companyCode)) {
                companyCode = IConst.COMPANY_CODE_XH;
            }
            List<Role> roleList = roleService.queryRoleByUserId(Integer.valueOf(userId));
            if(CollectionUtils.isEmpty(roleList)){
                throw new QinFeiException(1002, "发起人没有任何角色，请先绑定角色再操作！");
            }
            List<String> roleTypeList = new ArrayList<>();//缓存发起人角色类型
            roleList.forEach(role -> {
                if(!roleTypeList.contains(role.getType())){
                    roleTypeList.add(role.getType());
                }
            });
            List<Dept> deptList = null;
            User user = userMapper.get(User.class, Integer.parseInt(userId));
            Dept userDept = deptMapper.getById(user.getDeptId());
            //如果有业务角色 并且 是业务部门，需要获取部门对应总监，所以优先判断业务角色
            if(roleTypeList.contains(IConst.ROLE_TYPE_YW) && IConst.ROLE_TYPE_YW.equals(userDept.getCode())){
                //获取业务区部门
                deptList = deptMapper.queryByCompanyCodeAndCodeAndLevel(companyCode, IConst.ROLE_TYPE_YW, 2);
            }
            //如果有部门列表说明是业务
            User xzUser = null;//行政总监
            if(!CollectionUtils.isEmpty(deptList)){
                for(Dept dept : deptList){
                    //如果流程发起用户为业务区负责人，则审核节点为分管领导
                    if(user.getId().equals(dept.getMgrId())){
                        result = userMapper.get(User.class, dept.getMgrLeaderId());
                        break;
                    }else {
                        //判断发起用户管理的部门是否属于区部门
                        String childDept = deptMapper.idsByParentId(dept.getId());
                        if(Arrays.asList(childDept.split(",")).contains(String.valueOf(user.getDeptId()))){
                            result = userMapper.get(User.class, dept.getMgrId());
                            break;
                        }
                    }
                }
            }

            //如果业务区部门没有筛选到人员
            if(result == null){
                List<Map<String, Object>> userList = userMapper.listUserByParam(null, IConst.ROLE_CODE_ZJ, companyCode, 0);
                if(!CollectionUtils.isEmpty(userList)){
                    for(Map<String, Object> map : userList){
                        String roleType =  String.valueOf(map.get("roleType"));
                        //行政总监
                        if(IConst.ROLE_TYPE_XZ.equals(map.get("roleType"))){
                            xzUser = new User();
                            xzUser.setId(Integer.parseInt(String.valueOf(map.get("id"))));
                            xzUser.setDeptId(Integer.parseInt(String.valueOf(map.get("deptId"))));
                            xzUser.setUserName(String.valueOf(map.get("userName")));
                            xzUser.setName(String.valueOf(map.get("name")));
                        }
                        //如果匹配到自己角色的总监，则直接审核
                        if(roleTypeList.contains(roleType)){
                            result = new User();
                            result.setId(Integer.parseInt(String.valueOf(map.get("id"))));
                            result.setDeptId(Integer.parseInt(String.valueOf(map.get("deptId"))));
                            result.setUserName(String.valueOf(map.get("userName")));
                            result.setName(String.valueOf(map.get("name")));
                            break;
                        }
                    }

                }
            }

            //如果以上没有找到总监，则审核人为行政总监
            if(result == null){
                result = xzUser;
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "很抱歉，获取审核人异常，请联系技术人员！");
        }
        return result;
    }

    /**
     * @param userId
     * @return 返回String，新媒体=XMT,网络=WL
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'type=MJ&userId='+#userId")
    public String getMJType(Integer userId) {
        List<Dept> list = userMapper.getMJType(userId);
        if (list != null && list.size() > 0){
            return list.get(0).getType();
        } else {
            return null;
        }
    }

    @Override
    public List<User> getUserByDeptId(Integer deptId) {
        List<User> list = userMapper.getUserByDeptId(deptId);
        if (list != null && list.size() > 0) {
            return list;
        } else {
            return null;
        }
    }


    @Override
    public List<User> getUserByManger() {
        List<User> list = userMapper.getUserByManger();
        if (list != null && list.size() > 0) {
            return list;
        } else {
            return null;
        }
    }


    /**
     * 根据角色查用户信息
     *
     * @return
     */
    @Override
    public PageInfo<User> queryByRoleId(int pageNum, int pageSize, Map map) {
        try {
            PageHelper.startPage(pageNum, pageSize);
            List<User> list = userMapper.queryByRoleId(map);
            return new PageInfo<>(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据媒体ID查询媒介人员列表
     *
     * @param mediaId
     * @return
     */
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'mediaId='+#mediaId")
    public List<User> listMJByMediaId(Integer mediaId) {
        return userMapper.listMJByMediaId(mediaId);
    }

    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'mediaTypeId='+#mediaTypeId")
    public List<User> listMJByMediaTypeId(Integer mediaTypeId) {
        return userMapper.listMJByMediaTypeId(mediaTypeId);
    }

    @Override
    public List<User> listPastMJByMediaTypeId(Integer mediaTypeId, String companyCode) {
        return userMapper.listPastMJByMediaTypeId(mediaTypeId, companyCode);
    }

    @Override
    public List<User> listPastMJByMediaTypeId2(Integer mediaTypeId, String companyCode) {
        return userMapper.listPastMJByMediaTypeId2(mediaTypeId, companyCode);
    }

    @Override
    public List<User> listMJByPlateId(Integer plateId) {
        User user = AppUtil.getUser();
        if (user == null) {
            return new ArrayList<>();
        }
        return userMapper.listMJByPlateId(plateId, user.getDept().getCompanyCode());
    }

    @Override
    public List<User> listMJByPlateId2(Integer plateId) {
        User user = AppUtil.getUser();
        if (user == null) {
            return new ArrayList<>();
        }
        return userMapper.listMJByPlateId2(plateId, user.getDept().getCompanyCode());
    }
    /**
     * 获取所有用户信息；
     *
     * @return ：存储用户信息的集合；
     */
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'listAllUserMap'")
    public Map<Integer, User> listAllUserMap() {
        List<User> users = listAll();
        Map<Integer, User> datas = new HashMap<>();
        for (User user : users) {
            datas.put(user.getId(), user);
        }
        return datas;
    }

    /**
     * 查询所有的用户信息集合，key为用户名称，用于导入数据获取ID；
     *
     * @param mediaType：媒体板块类型；
     * @return ：用户信息集合；
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'mediaType='+#mediaType")
    public Map<String, Integer> listAllUserNameMap(int mediaType) {
        List<User> users = listMJByMediaTypeId(mediaType);
        Map<String, Integer> datas = new HashMap<>();
        for (User user : users) {
            datas.put(user.getName(), user.getId());
        }
        return datas;
    }

    /**
     * 联系人信息获取；
     *
     * @param queryContent：查询内容；
     * @param pageNum：查询页码；
     * @return ：拼接完毕的分页内容；
     */
    @Override
    public String listUserInfo(String queryContent, int pageNum) {
        PageHelper.startPage(pageNum, 30);
        // 查询条件；
        Map<String, Object> map = new HashMap<>();
        map.put("queryContent", queryContent);
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        List<User> users = userMapper.listUserInformation(map);
        PageInfo<User> userInfo = new PageInfo<>(users);

        // 开始拼接返回内容；
        StringBuilder content = new StringBuilder();
        content.append("<div class=\"content\">");
        int size = users.size();
        if (size > 0 && pageNum <= userInfo.getPages()) {
            User user;
            String userField;
            String dataContent;
            for (int i = 0; i < size; i++) {
                user = users.get(i);

                content.append("<div class=\"excerpt\">");
                content.append("<div class=\"col-sm-4\">");
                content.append("<div class=\"contact-box\" style=\"padding: 15px;\">");
                content.append("<a href=\"#\" onclick=\"return false;\">");

                // --------------------------------头像模块；------------------------------------
                content.append("<div class=\"col-sm-3\" style=\"margin-left: -20px;\">");
                content.append("<div class=\"text-center\" id=\"box");
                content.append(i);
                content.append("\">");
                // 获取照片；
                userField = user.getImage();
                dataContent = StringUtils.isEmpty(userField) ? "/img/mrtx_1.png" : userField.replace("\\images\\", "/images/");
                content.append("<img alt=\"图片丢失\" onerror=\"this.src='/img/mrtx_1.png'\" class=\"img-circle m-t-xs img-responsive\" src=\"");
                content.append(dataContent);
                content.append("\" style=\"margin-top: 0px;\"/>");

                content.append("</div>");
                content.append("</div>");
                // --------------------------------头像模块；------------------------------------

                // --------------------------------信息模块；------------------------------------
                content.append("<div class=\"col-sm-9\" style=\"padding: 0 0 0 5px;\">");
                // 姓名；
                userField = user.getName();
                content.append("<h3><strong>");
                content.append(userField == null ? "" : userField);
                content.append("</strong></h3>");

                content.append("<address>");
                // 邮箱；
                userField = user.getEmail();
                content.append("邮箱：");
                content.append(userField == null ? "" : userField);
                content.append("<br/>");

                // 微信；
                userField = user.getWechat();
                content.append("微信：");
                content.append(userField == null ? "" : userField);
                content.append("<br/>");

                // 电话；
                userField = user.getPhone();
                content.append("电话：");
                content.append(userField == null ? "" : userField);
                content.append("</address>");

                content.append("</div>");
                // --------------------------------信息模块；------------------------------------

                // 底部填充；
                content.append("<div class=\"clearfix\"></div>");

                content.append("</a>");
                content.append("</div>");
                content.append("</div>");
                content.append("</div>");
            }

        } else {
            content.append("<div class='pagination'><h3><strong>查询无结果。</strong></h3></div>");
        }
        // 导航；
        content.append("<nav class=\"pagination\" style=\"display:none;\"><ul><li class=\"next-page\"><a href=\"/user/listUserInfo\"></a></li></ul></nav>");
        content.append("</div>");
        return content.toString();
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'userId='+#userId+'&roleType='+#roleType")
    public Boolean queryRoleByUserIdAndRoleType(Integer userId, String roleType) {
        Boolean flag = false;
        List<Role> list = userMapper.queryRoleByUserIdAndRoleType(userId, roleType);
        if (list != null && list.size() > 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * 只查当前部门，不包含子部门
     *
     * @param deptId
     * @return
     */
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'deptIdOnly='+#deptId")
    public List<User> queryUserByDeptIdONLY(Integer deptId) {
        return userMapper.queryUserByDeptIdONLY(deptId);
    }

    /**
     * 查询指定部门的负责人信息；
     *
     * @param deptId：部门ID；
     * @return ：部门的负责人信息；
     */
    @Override
    @Cacheable(value = CACHE_LIST_KEY, key = "'leaderDeptId='+#deptId")
    public List<User> listLeaderByDeptId(int deptId) {
        // 获取登录人的公司代号；
        String code = null;
        // 获取登录人；
        User user = AppUtil.getUser();
        Dept dept = user.getDept();
        if (dept != null) {
            code = dept.getCompanyCode();
        }
        return userMapper.listLeaderByDeptId(deptId, code);
    }

    /**
     * 查询指定审核状态的负责人信息；
     *
     * @param state：状态，定义参com.qinfei.qferp.entity.crm.Const；
     * @return ：指定审核状态的负责人信息；
     */
    @Override
    public List<User> listLeaderByState(int state) {
        return listLeaderByState(state, AppUtil.getUser().getId());
    }

/*    @Override
//    @Caching(evict = {@CacheEvict(value = "user", key = "'id='+#userId")})
    public void updateUserCust(Map map) {
        userMapper.updateUserCust(map);
    }*/

    /**
     * 查询不建议客户的所有理由
     *
     * @param dockingPeople
     * @return
     */
/*    @Override
    public List<Map> queryUserCust(int dockingPeople) {
        return custService.queryUserCust(dockingPeople);
    }

    *//**
     * 查询原来的保护状态
     *
     * @param dockingPeople
     * @return
     *//*
    @Override
    public int queryCustState(Integer dockingPeople) {
        return custService.queryCustState(dockingPeople);
    }*/

    /**
     * 添加客户保护记录
     *
     * @return
     */
    /*@Override
    @Transactional
    @Caching(evict = {@CacheEvict(value = "user", key = "'id='+#userId")})
    public ResponseData addUserCust(Integer saveState, Integer dockingPeople, String reason, Integer userId) {
        ResponseData data = ResponseData.ok();
        User user = getById2(userId);
        // 原来的保护状态
        int state = custService.queryCustState(dockingPeople);
        // 保护客户数量
        int saveCust = user.getSaveCustNum();
        // 已保护客户数量
        int protectedCust = custService.queryProectedNum(userId);
//        int protectedCust = user.getProtectedCustNum();
        // 可保护数量
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        // 保存原来的保护数量
        map.put("saveCust", saveCust);
        // 由未保护状态改变
        if (state == 0) {
            // 设置不建议合作
            if (saveState == -1) {
                custService.updateNoUserCust(saveState, dockingPeople, reason);
                custService.addNoUserCust(user.getId(), user.getName(), -1, dockingPeople, reason);
                // 需跳出编辑客户信息，编辑不建议合作的理由
                data.putDataValue("message", "客户不建议合作");
                // 关闭不建议合作理由的模态框的标志
                data.putDataValue("closeModal", 1);
                map.put("protectedCustNum", protectedCust);
                updateUserCust(map);
                // 设置保护
            } else if (saveState == 1) {
                // 已保护数量小于保护数量才允许修改
                if (protectedCust < saveCust) {
                    protectedCust += 1;
                    custService.updateYesUserCust(saveState, dockingPeople);
                    custService.addYesUserCust(user.getId(), user.getName(), 1, dockingPeople);
                    data.putDataValue("message", "客户已保护");
                    map.put("protectedCustNum", protectedCust);
                    updateUserCust(map);
                } else {
                    data.putDataValue("message", "保护客户已满");
                }
            }
            // 由保护状态改变
        } else if (state == 1) {
            // 设置不建议合作
            if (saveState == -1) {
                // 防止可用数量大于10
                if (protectedCust >= 1) {
                    protectedCust -= 1;
                    // 需跳出编辑客户信息，编辑不建议合作的理由
                    custService.updateNoUserCust(saveState, dockingPeople, reason);
                    custService.addNoUserCust(user.getId(), user.getName(), -1, dockingPeople, reason);
                    data.putDataValue("message", "该客户不建议合作");
                    data.putDataValue("closeModal", 1);
                    map.put("protectedCustNum", protectedCust);
                    updateUserCust(map);
                } else {
                    data.putDataValue("message", "保护客户已满");
                }
                // 取消保护
            } else if (saveState == 0) {
                if (protectedCust >= 1) {
                    protectedCust -= 1;
                    custService.updateYesUserCust(saveState, dockingPeople);
                    custService.addYesUserCust(user.getId(), user.getName(), 0, dockingPeople);
                    data.putDataValue("message", "客户修改为未保护状态");
                    map.put("protectedCustNum", protectedCust);
                    updateUserCust(map);
                } else {
                    data.putDataValue("message", "保护客户已满");
                }
            }
            // 由不建议合作状态改变
        } else if (state == -1) {
            // 取消不建议
            if (saveState == 0) {
                custService.updateYesUserCust(saveState, dockingPeople);
                custService.addYesUserCust(user.getId(), user.getName(), 0, dockingPeople);
                map.put("protectedCustNum", protectedCust);
                updateUserCust(map);
                data.putDataValue("message", "客户修改为未保护状态");
                // 设置保护
            } else if (saveState == 1) {
                // 已保护数量小于保护数量才允许修改
                if (protectedCust < saveCust) {
                    protectedCust += 1;
                    custService.updateYesUserCust(saveState, dockingPeople);
                    custService.addYesUserCust(user.getId(), user.getName(), 1, dockingPeople);
                    data.putDataValue("message", "客户已保护");
                    map.put("protectedCustNum", protectedCust);
                    updateUserCust(map);
                } else {
                    data.putDataValue("message", "保护客户已满");
                }
            }
        }
        return data;
    }*/

    @Override
    public Boolean checkUserName(Integer id, String userName) {
        Boolean flag = false;
        if (id != null) {
            // 编辑页面判断用户名是否重复，先排除自己
            User user = this.getById(id);
            if (userName.equals(user.getUserName())) {
                flag = true;
            } else {
                List<User> list = this.queryUserByUserName(userName);
                if (list.size() == 0) {
                    flag = true;
                }
            }
        } else {
            // 新增页面判断用户名是否重复
            List<User> list = this.queryUserByUserName(userName);
            if (list.size() == 0) {
                flag = true;
            }
        }
        return flag;
    }

    @Override
//    @Cacheable(value = CACHE_LIST_KEY, key = "'companyCode='+#companyCode")
    public List<User> queryByCompanyCode(String companyCode) {
        return userMapper.queryByCompanyCode(companyCode);
    }
    /**
     * 根据条件查询用户
     * @param map
     * @return
     */
    @Override
    public List<User> queryUserByCondition(Map map) {
        return userMapper.queryUserByCondition(map);
    }

    /**
     * 查询指定审核状态的负责人信息；
     *
     * @param state：状态，定义参com.qinfei.qferp.entity.crm.Const；
     * @param userId：当前登录人ID；
     * @return ：指定审核状态的负责人信息；
     */
    @Cacheable(value = CACHE_LIST_KEY, key = "'listLeaderByState='+#state + '&userId=' + userId ")
    public List<User> listLeaderByState(int state, int userId) {
        List<User> users = null;
        // 获取登录人；
        User user = AppUtil.getUser();
        // 获取登录人的公司代号；
        String code = null;
        Dept dept = user.getDept();
        if (dept != null) {
            code = dept.getCompanyCode();
        }
        switch (state) {
            // 直属领导；
            case IConst.STATE_BZ:
               if(dept.getLevel() <= 3 && !user.getId().equals(dept.getMgrId())){
                   users = userMapper.listLeaderByDeptId(user.getDeptId(), code);
               }else{
                   //如果是组部长的话就部门分管领导审核
                   users = userMapper.listMgrLeaderByDeptId(user.getDeptId(), code);
               }

                break;
            // 人事；
            case IConst.STATE_RS:
                users = queryRSList(code);
                break;
            // 行政；
            case IConst.STATE_XZ:
                users = queryXZList(code);
                break;
            // 会计；
            case IConst.STATE_KJ:
                users = queryAccountingInfo(code);
                break;
            // 财务总监；
            case IConst.STATE_CFO:
                users = queryCFOInfo(code);
                break;
            // 总经理；
            case IConst.STATE_CEO:
                users = queryCEOInfo(code);
                break;
            // 不存在；
            default:
                break;
        }
        return users;
    }

    @Override
    public PageInfo<Map> queryNoProposeUser(Map map, Pageable pageable) {
        map.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        Object object=map.get("timeRange");
        String timeRange="";
        if(!ObjectUtils.isEmpty(object)){
            timeRange=object.toString();
        }
        Integer timeAdjust=0;
        if(StringUtils.isNotEmpty(timeRange)){
           if(timeRange.equals(1)){
               timeAdjust=0;
           }else if(timeRange.equals(2)){
               timeAdjust=-1;
           }else {
               timeAdjust=-2;
           }
        }else{
            timeAdjust=0;
        }
        map.put("timeAdjust",timeAdjust);
        List<Map> proposeList = userMapper.queryDeptByCompany(map);
        return new PageInfo<>(proposeList);
    }

    /**
     * 根据用户姓名和联系电话查询信息；
     *
     * @param name：姓名；
     * @param phone：联系电话；
     * @return ：查询结果；
     */
    @Override
    public List<User> selectUserId(String name, String phone) {
        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        return userMapper.list(user);
    }

    /**
     * 判断用户是否为部门的最高领导；
     *
     * @param userId：用户ID；
     * @param deptId：部门ID；
     * @return ：是否为该部门最高领导，true为是，false为否；
     */
    @Override
    public boolean isDeptLeader(int userId, int deptId) {
        return userMapper.isDeptLeader(userId, deptId) > 0;
    }

    @Override
    public List<User> listExclude(Integer postId, int[] excludeIds) {
        return userMapper.listExclude(postId, excludeIds);
    }

    /**
     * 根据查询条件获取相关的用户信息；
     *
     * @param params：查询条件集合；
     * @return ：查询结果集合；
     */
    @Override
    public List<User> listByParams(Map<String, Object> params) {
        return userMapper.listByParams(params);
    }

    @Override
    public String getChilds(Integer deptId) {
        return userMapper.getChilds(deptId);
    }

    @Override
    public List<User> listByUserIds(String userIds) {
        if (!StringUtils.isEmpty(userIds) && userIds.indexOf("$,") > -1) {
            userIds = userIds.substring(2);
        }
        return userMapper.listByUserIds(userIds);
    }

    @Override
    public List<User> listByUserIdsAndRoleType(String userIds, String roleType) {
        if (!StringUtils.isEmpty(userIds) && userIds.indexOf("$,") > -1) {
            userIds = userIds.substring(2);
        }
        return userMapper.listByUserIdsAndRoleType(userIds, roleType);
    }

    @Override
    public List<User> listByDeptId(Integer deptId) {
        String userIds = getChilds(deptId);
        return listByUserIds(userIds);
    }

    private List<User> listByDeptIdAndRoType(Integer deptId, String roleType) {
        String userIds = getChilds(deptId);
        return listByUserIdsAndRoleType(userIds, roleType);
    }

    @Override
    public List<User> listByCompanyCodeAndRoleType(String companyCode, String roleType) {
        return userMapper.listByCompanyCodeAndRoleType(companyCode, roleType);
    }


    @Override
    public List<User> deptUsers(Integer deptId, String roleType) {
        User user = AppUtil.getUser();
        if (AppUtil.isRoleType(IConst.ROLE_TYPE_CW)) {
            String companyCode = user.getCompanyCode();
            return listByCompanyCodeAndRoleType(companyCode, roleType);
        } else {
            return listByDeptIdAndRoType(deptId == null ? user.getDeptId() : deptId, roleType);
        }
    }

    /**
     * 获取指定角色下的部门所有人员的ID；
     *
     * @param roleType：角色标识；
     * @return ：部门下人员的ID集合；
     */
    @Override
    public List<Integer> listUserIdsByRoleType(String roleType) {
        List<User> users = listByType(roleType);
        if (users.isEmpty()) {
            return null;
        } else {
            List<Integer> ids = new ArrayList<>();
            for (User data : users) {
                ids.add(data.getId());
            }
            return ids;
        }
    }

    /**
     * 查询指定公司下的所有职位（分页）
     *
     * @param map
     * @param pageable
     * @return
     */
    @Override
    public PageInfo<Map> getCompanyPost(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map> list = postMapper.getCompanyPost(map);
        return new PageInfo(list);
    }

    /**
     * 获取指定公司的职位
     *
     * @param map
     * @return
     */
    @Override
    public List<Map> queryCompanyPost(Map map) {
        return postMapper.getCompanyPost(map);
    }

    /**
     * 根据id获取职位信息
     *
     * @param id
     * @return
     */
    @Override
    public Post getPostById(Integer id) {
        return postMapper.get(Post.class, id);
    }

    /**
     * 新增编辑核实职位是否重复
     *
     * @param name
     * @param companyCode
     * @return
     */
    @Override
    public Post getPostInfo(String name, String companyCode, Integer id) {
        return postMapper.getPostInfo(name, companyCode, id);
    }

    /**
     * 判断是否可以删除职位
     *
     * @param id
     * @return
     */
    @Override
    public Post queryByDeletePost(Integer id) {
        return postMapper.queryByDeletePost(AppUtil.getUser().getCompanyCode(), id);
    }


    /**
     * 增加部门职位
     *
     * @param post
     */
    @Override
    public void saveDeptPost(Post post) {
        User user = AppUtil.getUser();
        post.setCreator(user.getId());
        post.setState(0);
        postMapper.insert(post);
    }

    /**
     * 修改部门职位
     *
     * @param post
     */
    @Override
    public void updateDeptPost(Post post) {
        postMapper.update(post);
    }

    /**
     * 删除职位
     *
     * @param id
     */
    @Override
    public void delDeptPost(Integer id) {
        postMapper.delDeptPost(id);
    }

    /**
     * 删除部门职位关系
     *
     * @param deptId
     */
    @Override
    public void deletePost(Integer deptId) {
        postMapper.deletePost(deptId);
    }

    /**
     * 插入部门职位关系
     *
     * @param map
     */
    @Override
    public void insertDeptPost(List<Map> map) {
        postMapper.insertDeptPost(map);
    }

    /**
     * 绑定职位时显示
     *
     * @param companyCode
     * @param deptId
     * @return
     */
    @Override
    public List<Map> queryDeptPost(String companyCode, Integer deptId) {
        return postMapper.queryDeptPost(companyCode, deptId);
    }

    @Override
    public List<User> listUserByDeptAndRole(Map map) {
        User user = AppUtil.getUser();
        Integer deptId = null;
        if (!ObjectUtils.isEmpty(map.get("deptId"))) {
            deptId = Integer.parseInt(String.valueOf(map.get("deptId")));
        }
        deptId = deptId == null ? user.getDeptId() : deptId;
        String deptIds = getChilds(deptId);
        if (!StringUtils.isEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
            deptIds = deptIds.substring(2);
        }
        return userMapper.listUserByDeptAndRole(deptIds, user.getCompanyCode(), String.valueOf(map.get("roleType")));
    }

    @Override
    public List<User> listUserByDeptAndRole2(Map map) {
        User user = AppUtil.getUser();
        Integer deptId = null;
        if (!ObjectUtils.isEmpty(map.get("deptId"))) {
            deptId = Integer.parseInt(String.valueOf(map.get("deptId")));
        }
        deptId = deptId == null ? user.getDeptId() : deptId;
        String deptIds = getChilds(deptId);
        if (!StringUtils.isEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
            deptIds = deptIds.substring(2);
        }
        return userMapper.listUserByDeptAndRole2(deptIds, user.getCompanyCode(), String.valueOf(map.get("roleType")));
    }

    @Override
    public List<User> listUserByDeptAndRoleJT(Map map) {
        User user = AppUtil.getUser();
        Integer deptId = null;
        if (!ObjectUtils.isEmpty(map.get("deptId"))) {
            deptId = Integer.parseInt(String.valueOf(map.get("deptId")));
        }
        deptId = deptId == null ? user.getDeptId() : deptId;
        String deptIds = getChilds(deptId);
        if (!StringUtils.isEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
            deptIds = deptIds.substring(2);
        }
        return userMapper.listUserByDeptAndRole2(deptIds, IConst.COMPANY_CODE_JT, String.valueOf(map.get("roleType")));
    }

    @Override
    public List<User> listUserByDeptAndRole3(Map<String, Object> param) {
        String deptIds = null;
        if(!ObjectUtils.isEmpty(param.get("deptId"))){
            deptIds = getChilds(Integer.parseInt(String.valueOf(param.get("deptId"))));
            if (!StringUtils.isEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
        }

        return userMapper.listPartAll4(String.valueOf(param.get("roleType")),deptIds);
    }

    @Override
    public List<Dict> listByTypeAndCode() {
        String type = "ROLE_TYPE";
        return userMapper.roleType(type);
    }

    @Override
    public List<Role> characterName(String nameQc) {
        return userMapper.characterName(nameQc);
    }

    @Override
    public List<Role> getRoleByType(String nameQc) {
        return roleService.queryRoleByRoleType(nameQc);
    }

    @Override
    public List<User> listBusinessPart(String name) {
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        return userMapper.listBusinessPart("%" + name + "%", companyCode);
    }

    @Override
    public void logout(User user) {
        log.debug("注销" + log.isDebugEnabled());
    }

    @Override
    public List<User> listDeptUser() {
        Map map = new HashMap();
        User user = AppUtil.getUser();
        Boolean flag = false;
        List<User> list = new ArrayList<>();

        if (user.getRoles() != null && user.getRoles().size() > 0) {
            for (Role role : user.getRoles()) {
                if (IConst.ROLE_TYPE_YW.equals(role.getType()) || IConst.ROLE_TYPE_MJ.equals(role.getType())) {
                    flag = true;
                }
            }
        }
        if (flag) {
            map.put("deptId", user.getDeptId());
            this.addSecurity(map);
            list = userMapper.listDeptUser(map);
        } else {
            list = userMapper.queryByCompanyCode(user.getCompanyCode());


        }

        return list;
    }

    @Override
    @Transactional
    public Boolean validateLogin(User user) {

        List<SysUpdatePassword> sysLis = sysUpdatePasswordMapper.querySysUpdatePasswordByCreator(user.getId());
        SysUpdatePassword sys = null;
        if (!ObjectUtils.isEmpty(sysLis)) {
            sys = sysLis.get(0);
            long str = ((new Date().getTime() - sys.getUpdateTime().getTime()) / 1000);
            long str1 = 90 * 24 * 60 * 60;
            return str <= str1;
        } else {
            String pwd = user.getPassword();
            sys = new SysUpdatePassword();
            sys.setCreator(user.getId());
            sys.setOldPassword(pwd);
            sys.setNewPassword(pwd);
            sys.setCreateTime(new Date());
            sys.setUpdateTime(new Date());
            sysUpdatePasswordMapper.insert(sys);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean svaeUpdatePwdRecord(Integer userId, String pwd) {
        List<SysUpdatePassword> sysLis = sysUpdatePasswordMapper.querySysUpdatePasswordByCreator(userId);
        SysUpdatePassword sys = null;
        if (!ObjectUtils.isEmpty(sysLis)) {
            sys = sysLis.get(0);
            sys.setOldPassword(sys.getNewPassword());
            sys.setNewPassword(pwd);
            sys.setUpdateTime(new Date());
            sysUpdatePasswordMapper.update(sys);
        } else {
            sys = new SysUpdatePassword();
            sys.setCreator(userId);
            sys.setOldPassword(pwd);
            sys.setNewPassword(pwd);
            sys.setCreateTime(new Date());
            sys.setUpdateTime(new Date());
            sysUpdatePasswordMapper.insert(sys);
        }
        return true;
    }

    @Override
    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true)})
    public void onHandover(Integer id) {
        User user = userMapper.get(User.class, id);
        user.setHandoverState(IConst.handOverStateOn);
        user.setUpdateTime(new Date());
        user.setUpdateUserId(AppUtil.getUser().getId());
        userMapper.update(user);
    }

    @Override
    public void back(Integer id){
        User user = userMapper.get(User.class, id);
        user.setState(1);
        user.setUpdateTime(new Date());
        user.setUpdateUserId(AppUtil.getUser().getId());
        userMapper.update(user);
    }
    @Override
    @Caching(evict = {@CacheEvict(value = CACHE_KEY, allEntries = true), @CacheEvict(value = CACHE_LIST_KEY, allEntries = true)})
    public void offHandover(Integer id) {
        User user = userMapper.get(User.class, id);
        user.setHandoverState(IConst.handOverStateOff);
        user.setUpdateTime(new Date());
        user.setUpdateUserId(AppUtil.getUser().getId());
        userMapper.update(user);
    }

//    @Override
//    public Boolean checkDuplicateUserName(Map map) {
//        List<User> list = userMapper.checkDuplicateUserName(map);
//        return list.size()>0?false:true;
//    }
    @Override
    public ResponseData checkDuplicateUserName(Integer id,String name){
        try{
            ResponseData data = ResponseData.ok();
            Map userState = userMapper.checkDuplicateUserName(id,name);
            data.putDataValue("userState",userState);
            return  data;
        }catch (QinFeiException e) {
           throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "很抱歉，增加用户失败，请联系技术人员！");
        }
    }

//    @Override
//    public Boolean checkDuplicateName(Map map) {
//        List<User> list = userMapper.checkDuplicateName(map);
//        return list.size()>0?false:true;
//    }
    @Override
    public ResponseData checkDuplicateName(Integer id, String name){
        try{
            ResponseData data = ResponseData.ok();
            Map userState = userMapper.checkDuplicateName(id,name);
            data.putDataValue("userState",userState);
            return  data;
        }catch(QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "很抱歉，增加用户失败，请联系技术人员！");
        }

    }

    @Override
    public List<Map> listUserByTypeAndCompanyCode(Map map){
        return userMapper.listUserByTypeAndCompanyCode(map);
    }

    @Override
    public List<Map> listUserByCompanyCode(Map map){
        return userMapper.listUserByCompanyCode(map);
    }

    @Override
    public List<Integer> querySuggestHintData(String companyCode) {
        Map map=new HashMap();
        map.put("companyCode",companyCode);
        Integer timeAdjust=0;
        List<ProposeTips> list = proposeTipsService.queryTipsByType(companyCode);
        if(!CollectionUtils.isEmpty(list)){
            Integer state=list.get(0).getState();
            if(state==1){
                timeAdjust=0;//一个月
            }else if(state==2){
                timeAdjust=-1;//两个月
            }else {
                timeAdjust=-2;//三个月
            }
        }else {
            timeAdjust=0;//默认本月
        }
        map.put("timeAdjust",timeAdjust);
        return userMapper.querySuggestHintData(map);
    }

    @Override
    public void delSuggestUserCache() {

    }

    @Override
    public List<User> queryUserByRoleType(Map map) {
        List<User> list = userMapper.queryUserByRoleType(map);
        if(!CollectionUtils.isEmpty(list)){
            return list;
        }
        return null;
    }

    @Override
    public Map getUserByUserId(Integer id) {
        Map user = userMapper.getUserByUserId(id);
        List<Role> roles = roleService.queryRoleByUserId((int) user.get("userId"));
        user.put("roleList",roles);
        return user;
    }

    @Override
    public PageInfo<List<Map>> intranetlistPg(int pageNum, int pageSize,String keyword) {
        PageHelper.startPage(pageNum,pageSize);
        List<Map> list = userMapper.intranetlistPg(keyword);
        return new PageInfo(list);
    }

    @Override
    public PageInfo<List<Map>> intranetUserListPg(int pageNum, int pageSize,String keyword) {
        PageHelper.startPage(pageNum,pageSize);
        List<Map> list = userMapper.intranetUserListPg(keyword);
        for (Map map:list){
            List<Role> roles = roleService.queryRoleByUserId((int) map.get("id"));
            map.put("roleList",roles);
        }
        return new PageInfo(list);
    }

    @Override
    public User intranetGetUserByUsername(String username) {
        User user = userMapper.getByUserName(username);
        List<Role> roles = roleService.queryRoleByUserId(user.getId());
        if (roles != null && roles.size() > 0) {
            roles.forEach(role -> {
                role.setUpdateTime(null);
                role.setCreateTime(null);
            });
        }
        user.setRoles(roles);
        user.setUpdateTime(null);
        user.setCreateTime(null);
        user.setLoginTime(null);
        return user;
    }
}
