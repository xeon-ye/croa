package com.qinfei.qferp.service.impl.sys;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.mapper.DictMapper;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.Post;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.fee.AccountMapper;
import com.qinfei.qferp.mapper.media1.Media1Mapper;
import com.qinfei.qferp.mapper.media1.MediaAuditMapper;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.PostMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.sys.IDeptService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class DeptService implements IDeptService {
    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private DictMapper dictMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private Media1Mapper media1Mapper;
    @Autowired
    private MediaAuditMapper mediaAuditMapper;
    @Autowired
    private IUserService userService;

    //    @Cacheable(value = "depts", key = "'listAll'")
    public List<Dept> listAll() {
        return deptMapper.listAll();
    }

    private List<Dept> listDeptAll() {
        return deptMapper.listDeptAll();
    }
    @Override
    public JSONArray listDeptAllMJ() {
        JSONArray deptlist;
        List<Dept> menulist = deptMapper.listDeptAllMJ();
        DeptJsonUtilForTreeView deptJson = new DeptJsonUtilForTreeView();
        deptlist = deptJson.jsonList(menulist);
        return deptlist;
    }

    @Override
//    @Cacheable(value = "depts", key = "'tree'")
    public JSONArray listForTreeView() {
        JSONArray deptlist;
        List<Dept> menulist = listAll();
        DeptJsonUtilForTreeView deptJson = new DeptJsonUtilForTreeView();
        deptlist = deptJson.jsonList(menulist);
        return deptlist;
    }

    @Override
//    @Cacheable(value = "depts", key = "'tree'")
    public JSONArray onlyDeptMent() {
        JSONArray deptlist;
        List<Dept> menulist = listDeptAll();
        DeptJsonUtilForTreeView deptJson = new DeptJsonUtilForTreeView();
        deptlist = deptJson.jsonList(menulist);
        return deptlist;
    }

    /**
     * 获取指定部门下的部门数据；
     *
     * @param deptId：部门ID；
     * @return ：下属的部门数据；
     */
    @Override
    public JSONArray listForTreeView(int deptId) {
        JSONArray deptList;
        List<Dept> dataList = deptMapper.listByParentIdNew(deptId);
        DeptJsonUtilForTreeView deptJson = new DeptJsonUtilForTreeView();
        deptList = deptJson.jsonList(dataList, deptId);
        return deptList;
    }

    @Override
//    @Cacheable(value = "depts", key = "'echarts'")
    public JSONArray list() {
        List<Dept> menulist = listAll();
        DeptJsonUtil deptJson = new DeptJsonUtil();
        return deptJson.jsonList(menulist);
    }

    @Override
    public JSONArray listByCompany() {
        List<Dept> menulist;
        User user = AppUtil.getUser();
        if (user == null)
            throw new QinFeiException(1002, "会话失效，请重新登录！");
        if(AppUtil.isRoleType(IConst.ROLE_TYPE_JT) || user.getCompanyCode().equals(IConst.COMPANY_CODE_XH)){
            //如果是集团或者祥和角色则获取所有公司
            menulist = listAll();
        }else{
            menulist = deptMapper.listAllByCompanyCode(user.getCompanyCode());
        }
        if(menulist!=null && !menulist.isEmpty()){
            DeptJsonUtil deptJson = new DeptJsonUtil();
            JSONArray deptlist = deptJson.jsonDeptList(menulist);
            return deptlist;
        }else{
            throw new QinFeiException(1002, "该公司没有获取到部门信息，请确认后重试！");
        }
    }

    @Override
    @Caching(evict = {@CacheEvict(value = "dictList", allEntries = true),@CacheEvict(value = "EntryAllDeptNameAndCode", allEntries = true),
            @CacheEvict(value = "EntryAllDept", allEntries = true),@CacheEvict(value = "users",key = "'leaderDeptId='+#dept.id"),
            @CacheEvict(value = "EntryDept", allEntries = true),@CacheEvict(value = "DeptTree", allEntries = true)})
    public Dept edit(Dept dept) {
        if(dept.getCode().equals("GL")){
            //修改公司代码是xx的所有部门的公司名称
            deptMapper.editDeptName(dept.getCompanyCode(),dept.getName());
            //修改公司名称时，修改在该部门的人员的部门名称
            userMapper.editUserDeptName(dept.getName(),dept.getId());
            //修改sys_dict,财务账户显示
            Dict dict = dictMapper.getByTypeCodeAndCode("COMPANY_CODE",dept.getCompanyCode());
            if(dict!=null){
                //GL即公司名称
                dict.setName(dept.getName());
                dictMapper.update(dict);
            }
            //公司账户名称需要处理（老数据）
            accountMapper.editAccountCompanyName(dept.getName(),dept.getCompanyCode());
            //修改公司名称处理媒体表公司名称字段
            media1Mapper.editMedia1CompanyName(dept.getCompanyCode(),dept.getName());
            //修改公司名称时处理媒体审核表公司名称字段
            mediaAuditMapper.editMediaAuditCompanyName(dept.getCompanyCode(),dept.getName());
        }else{
            //修改部门名称时，修改在该部门的人员的部门名称
            userMapper.editUserDeptName(dept.getName(),dept.getId());
        }
        deptMapper.update(dept);
        return dept;
    }

    @Override
    @Caching(evict = {@CacheEvict(value = "User_Childs", allEntries = true), @CacheEvict(value = "EntryAllDeptNameAndCode", allEntries = true),
            @CacheEvict(value = "EntryAllDept", allEntries = true),@CacheEvict(value = "EntryDept", allEntries = true),@CacheEvict(value = "DeptTree", allEntries = true)})
    public void delById(Integer id) {
        try {
            User user = AppUtil.getUser();
            if (user == null) {
                throw new QinFeiException(1002, "请先登录！");
            }
            //判断当前部门和子级部门是否存在人员
            String deptIds = userService.getChilds(id);
            if (!StringUtils.isEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            if (StringUtils.isEmpty(deptIds)) {
                throw new QinFeiException(1002, "部门信息有误，请联系技术人员！");
            }
            List<Integer> deptIdList = new ArrayList<>();
            for (String deptId : deptIds.split(",")) {
                deptIdList.add(Integer.parseInt(deptId));
            }
            List<Integer> userList = userMapper.listUserByDeptIds(deptIdList);
            if (CollectionUtils.isNotEmpty(userList)) {
                throw new QinFeiException(1002, "部门或子部门存在未删除的人员，请删除人员后操作！");
            }
            recursiveRemove(id);
            Dept dept = getById(id);
            dept.setState(IConst.STATE_DELETE);
            dept.setUpdateUserId(user.getId());
            //处理部门职位绑定关系
            postMapper.deletePost(id);
            deptMapper.update(dept);
            if ("GL".equals(dept.getCode())) {
                dictMapper.delCompanyDict("COMPANY_CODE", dept.getCompanyCode());
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002, "删除部门异常，请联系管理员！");
        }
    }

    /**
     * 递归删除,有子部门先删除子部门
     */
    private void recursiveRemove(Integer id) {
        List<Dept> list = deptMapper.queryDeptByParentId(id);
        if (list != null || list.size() > 0) {
            for (Dept temp : list) {
                recursiveRemove(temp.getId());
                temp.setState(IConst.STATE_DELETE);
                //处理部门职位绑定关系
                postMapper.deletePost(temp.getId());
                deptMapper.update(temp);
            }
        }
    }

    @Override
    @Caching(evict = {@CacheEvict(value = "User_Childs", allEntries = true), @CacheEvict(value = "dictList", allEntries = true),
            @CacheEvict(value = "EntryAllDeptNameAndCode", allEntries = true),@CacheEvict(value = "EntryAllDept", allEntries = true),
            @CacheEvict(value = "EntryDept", allEntries = true),@CacheEvict(value = "DeptTree", allEntries = true)})
    public Dept addChildDept(Map map) {
        try {
            String name = (String) map.get("name1");
            String code  = (String) map.get("code1");
            Dept childDept = new Dept();
            childDept.setLevel(Integer.parseInt((String) map.get("level1")) + 1);
            childDept.setParentId(Integer.parseInt((String) map.get("parentId1")));
            childDept.setName(name);
            childDept.setCode(code);
            childDept.setType((String) map.get("type1"));
            if(!ObjectUtils.isEmpty(map.get("mgrId1"))){
                childDept.setMgrId(Integer.parseInt((String) map.get("mgrId1")));
            }
            childDept.setMgrName((String) map.get("mgrName1"));
            //对部门所属公司名称进行特殊处理
            String companyCode = (String) map.get("companyCode1");
            String companyCodeName = (String) map.get("companyName");
            List<Dept> deptList = deptMapper.getCompanyByCode(companyCode);
            Dept dept = null;
            if(CollectionUtils.isNotEmpty(deptList)){
                dept = deptList.get(0);
            }
            //分公司以填写的公司名称为准
            if(dept!=null && !"GL".equals(code)){
                //添加子部门以数据库查到的公司为准
                childDept.setCompanyCodeName(dept.getCompanyCodeName());
            }else{
                //新增分公司以填写的公司名称为准
                childDept.setName(companyCodeName);
                childDept.setCompanyCodeName(companyCodeName);
            }
            //code==GL添加分公司，并添加到sys_dict财务账户需要用到
            Integer postId=0;
            if("GL".equals(code)){
                Dict dict = new Dict();
                dict.setCode(companyCode);
                dict.setTypeCode("COMPANY_CODE");
                dict.setTypeName("公司名称");
                dict.setName(companyCodeName);
                dict.setParentId(0);
                dict.setState(0);
                Dict dict2 = dictMapper.getByTypeCodeAndCode("COMPANY_CODE",companyCode);
                //dict字典表查询数据为空添加
                if(ObjectUtils.isEmpty(dict2)){
                    dictMapper.insert(dict);
                }
                //自动生成职位，如果存在不添加
                Post post = new Post();
                post.setName("行政部长");
                post.setCode("XZBZ");
                post.setRemark("部门新增公司，默认绑定一个初始职位。");
                post.setState(0);
                post.setCreator(AppUtil.getUser().getId());
                post.setCreateTime(new Date());
                post.setCompanyCode(companyCode);
                Post post2 = postMapper.getPostInfo(post.getName(),companyCode,null);
                if(ObjectUtils.isEmpty(post2)){
                    postMapper.insert(post);
                    postId = post.getId();
                }else{
                    postId = post2.getId();
                }
            }
            childDept.setCompanyCode(companyCode);
            if(!ObjectUtils.isEmpty(map.get("mgrLeaderId1"))){
                childDept.setMgrLeaderId(Integer.parseInt((String) map.get("mgrLeaderId1")));
            }
            childDept.setMgrLeaderName((String) map.get("mgrLeaderName1"));
            childDept.setCreator((Integer) map.get("userId"));
            childDept.setState(IConst.STATE_SAVE);
            childDept.setCreateTime(new Date());
            deptMapper.insert(childDept);
            if(postId!=0){
                postMapper.saveDeptPost(childDept.getId(),postId);
            }
            return childDept;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
//    @Cacheable(value = "dept", key = "'id='+#id")
    public Dept getById(Integer id) {
        return deptMapper.getById(id);
    }

    @Override
//    @Cacheable(value = "dept", key = "'name='+#name")
    public List<Dept> queryDeptByName(String name) {
        return deptMapper.queryDeptByName(name);
    }

    @Override
//    @Cacheable(value = "depts", key = "'companyCode='+#companyCode+'&code='+#code")
    public List<Dept> queryDeptByCompanyCodeAndCode(String companyCode, String code) {
        return deptMapper.queryDeptByCompanyCodeAndCode(companyCode, code);
    }

    @Override
    public List<Dept> queryDeptByCompanyCodeAndCodeAndDeptName(String companyCode, String code, String deptName) {
        if (StringUtils.isEmpty(deptName))
            return deptMapper.queryDeptByCompanyCodeAndCode(companyCode, code);
        return deptMapper.queryDeptByCompanyCodeAndCodeAndDeptName(companyCode, code, "%" + deptName + "%");
    }

    @Override
    public List<Dept> listAllCompany() {
        return deptMapper.listAllCompany();
    }

    @Override
    public List<Dept> listJTAllCompany(String companyCode) {
        return deptMapper.listJTAllCompany(companyCode);
    }

    /**
     * 根据类型查询部门
     *
     * @param type
     * @return
     */
//    @Cacheable(value = "depts", key = "'type='+#type+'&companyCode='+#companyCode")
    @Override
    public List<Dept> listByTypeAndCompanyCode(String type, String companyCode) {
        Map map = new HashMap();
        map.put("type", type);
        map.put("companyCode", companyCode);
        return deptMapper.listPara(map);
    }

    @Override
//    @Cacheable(value = "depts", key = "'parentId='+#id")
    public List<Dept> listByParentId(Integer id) {
        return deptMapper.listByParentId(id);
    }

    /**
     * 根据部门第一层级查询所有分公司名称
     *
     * @param level
     * @return
     */
    @Override
    public List<Dept> listByLevelId(Integer level) {
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        Map map = new HashMap();
        map.put("level", level);
//        if (!companyCode.equals("XH")) {
//            map.put("companyCode", companyCode);
//        }
        return deptMapper.listByLevel(map);
    }

    @Override
    public List<Dept> allCompany(Integer level) {
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        Map map = new HashMap();
        map.put("level", level);
        if (!companyCode.equals("XH")) {
            map.put("companyCode", companyCode);
        }
        return deptMapper.allCompany(map);
    }

    @Override
//    @Cacheable(value = "depts", key = "'ids=ids&id='+#id")
    public String idsByParentId(Integer id) {
        return deptMapper.idsByParentId(id);
    }

    @Override
//    @Cacheable(value = "depts", key = "'treeFull'")
    public User getFullDeptTree(Map map) {
        User user = AppUtil.getUser();
        Integer deptId = MapUtils.getInteger(map, "currentDeptId");//页面选择的部门
        deptId = deptId == null ? user.getDeptId() : deptId;
        List<Dept> depts = this.listByParentId(deptId);
        Dept root = depts.get(0);
        user.setDept(root);
        for (int i = 0, len = depts.size(); i < len; i++) {
            Dept dept = depts.get(i);
            for (int j = i + 1; j < len; j++) {
                Dept dept1 = depts.get(j);
                if (dept.getId().equals(dept1.getParentId())) {
                    dept.getDepts().add(dept1);
                } else if (root.getParentId().equals(dept1.getId())) {
                    dept1.getDepts().add(dept);
                }
            }
        }
        return user;
    }

    @Override
    public JSONArray listForTreeViewByCompanyCode() {
        JSONArray deptlist;
        List<Dept> menulist = deptMapper.listAllByCompanyCode(AppUtil.getUser().getCompanyCode());
        DeptJsonUtilForTreeView deptJson = new DeptJsonUtilForTreeView();
        deptlist = deptJson.jsonListX(menulist);
        return deptlist;
    }

    /**
     * 根据登录人的公司code获取公司的所有部门
     *
     * @return
     */
    @Override
    public List<Dept> getDeptByCompany() {
        User user = AppUtil.getUser();
        return deptMapper.getDeptByCompany(user.getCompanyCode());
    }

    @Override
    public JSONArray listAllMJYWByDeptId(Integer deptId) {
        JSONArray deptList;
        List<Dept> dataList = deptMapper.listAllMJYWByDeptId(deptId);
        DeptCodeJsonUtilForTreeView deptJson = new DeptCodeJsonUtilForTreeView();
        deptList = deptJson.jsonList(dataList, deptId);
        return deptList;
    }

    @Override
    public JSONArray listAllDeptByIdAndCode(Integer deptId, String deptCode) {
        JSONArray deptList;
        List<Dept> dataList = deptMapper.listAllDeptByIdAndCode(deptId, deptCode);
        DeptCodeJsonUtilForTreeView deptJson = new DeptCodeJsonUtilForTreeView();
        deptList = deptJson.jsonList(dataList, deptId);
        return deptList;
    }

    @Override
    public JSONArray listAllDeptByIdAndCodeAndLevel(Integer deptId, String deptCode,String level) {
        JSONArray deptList;
        List<Dept> dataList = deptMapper.listAllDeptByIdAndCodeAndLevel(deptId, deptCode,level);
        DeptCodeJsonUtilForTreeView deptJson = new DeptCodeJsonUtilForTreeView();
        deptList = deptJson.jsonList(dataList, deptId);
        return deptList;
    }


    @Override
    public Dept getCompanyByCode(String companyCode) {
        List<Dept> deptList = deptMapper.getCompanyByCode(companyCode);
        Dept dept = null;
        if(CollectionUtils.isNotEmpty(deptList)){
            dept = deptList.get(0);
        }
        return dept;
    }

    @Override
    public Dept getRootDept() {
        return deptMapper.getRootDept();
    }

    @Override
    public List<Dept> checkDeptCompanyCode(String companyCode){
        return deptMapper.checkDeptCompanyCode(companyCode);
    }

    @Override
    public Map<String, Map<String, Integer>> getDeptUserNum(Map<String, Object> param) {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        try{
            //由于没写递归算法，此处查询部门必须按照层级倒序排序，不然计算会出问题，存在只统计直属子级部门，所以需要从低层级往层级计算
            List<Map<String, Object>> deptUserNumList = deptMapper.listDeptUserNumByParam(param);
            if(CollectionUtils.isNotEmpty(deptUserNumList)){
                //设置默认总人数
                result.put("total", new HashMap<>());
                result.get("total").put("userNum", 0);
                //查询List转换成Map
                for(Map<String, Object> deptUserNumMap : deptUserNumList){
                    String deptId = String.valueOf(deptUserNumMap.get("id"));
                    Integer level = Integer.parseInt(String.valueOf(deptUserNumMap.get("level")));
                    Integer userNum = Integer.parseInt(String.valueOf(deptUserNumMap.get("userNum")));
                    result.put(deptId, new HashMap<>());
                    result.get(deptId).put("level", level); //当前部门层级
                    result.get(deptId).put("userNum", userNum); //当前部门直接人员，不含子级部门
                    result.get("total").put("userNum", result.get("total").get("userNum") + userNum); //计算总人数
                }
                for(Map<String, Object> deptUserNumMap : deptUserNumList){
                    String deptId = String.valueOf(deptUserNumMap.get("id"));
                    String parentId = String.valueOf(deptUserNumMap.get("parentId"));
                    //如果直属父级部门被查询出来了，则把子部门人数计算进去
                    if(result.containsKey(parentId)){
                        result.get(parentId).put("userNum", result.get(parentId).get("userNum") + result.get(deptId).get("userNum"));
                    }
                }
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "获取部门人员数量异常！");
        }
    }

    @Override
    public List<Dept> listByCode(String code){
        return deptMapper.listByCode(code);
    }
}
