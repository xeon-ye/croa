package com.qinfei.qferp.service.impl.sys;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.Group;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.mapper.sys.GroupMapper;
import com.qinfei.qferp.service.sys.IGroupService;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
public class GroupService implements IGroupService {
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private RoleService roleService;

    @Override
    public PageInfo<Group> listPg(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Group> list = groupMapper.listPg(map);
        return new PageInfo<>(list);
    }

    @Override
    public List<Group> listAll() {
        return groupMapper.listAll();
    }

    @Override
    public List<Group> listAllChild() {
        return groupMapper.listAllChild();
    }

    @Override
    public PageInfo<Group> search(Pageable pageable, Group group) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        String name = group.getName();
        if (StringUtils.isNotEmpty(name))
            group.setName("%" + name + "%");
        List<Group> list = groupMapper.list(group);
        return new PageInfo<>(list);
    }

    /**
     * 添加权限
     *
     * @param group
     * @return
     */
    @Override
    @Caching(evict={@CacheEvict(value = CHACHE_KEY_LIST , allEntries=true)})
    public Group add(Group group, Integer userId) {
        group.setState(IConst.STATE_SAVE);
        group.setCreateTime(new Date());
        group.setCreator(userId);
        groupMapper.insert(group);
        return group;
    }

    /**
     * 编辑权限
     *
     * @param group
     * @return
     */
    @Override
    @Caching(evict={@CacheEvict(value = CHACHE_KEY , key = "'id='+#group.id"),
            @CacheEvict(value = CHACHE_KEY_LIST , allEntries=true)})
    public Group edit(Group group, Integer userId) {
        group.setUpdateTime(new Date());
        group.setUpdateUserId(userId);
        groupMapper.update(group);
        return group;
    }

    @Override
    @Caching(evict={@CacheEvict(value = CHACHE_KEY , key = "'id='+#id"),
            @CacheEvict(value = CHACHE_KEY_LIST , allEntries=true)})
    public void delById(@RequestParam("id") Integer id) {
        Group group = groupMapper.getById(id);
        if(ObjectUtils.isEmpty(group)){
            throw new QinFeiException(1002,"未找到该资源组，可能已删除，请刷新后重试！");
        }
        if(group.getParentId()==0){
            List<Group> groups = queryGroupByParentId(group.getId());
            if(groups!=null && groups.size()>0){
                throw new QinFeiException(1002,"该一级目录下有子目录，无法删除！");
            }
        }
        List<Role> roles = roleService.queryRoleByGroupId(id) ;
        if(roles!=null && roles.size()>0){
            throw new QinFeiException(1002,"该资源组已关联角色，无法删除！");
        }
        List<Resource> Resources = resourceService.queryResourcesByGroupId(id) ;
        if(Resources!=null && Resources.size()>0){
            throw new QinFeiException(1002,"该资源组已关联资源，无法删除！");
        }
        group.setState(IConst.STATE_DELETE);
        group.setUpdateTime(new Date());
        groupMapper.update(group);
    }

    @Override
    @Cacheable(value = CHACHE_KEY, key = "'id='+#id")
    public Group getById(Integer id) {
        return groupMapper.getById(id);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'name='+#name")
    public List<Group> queryGroupByName(String name) {
        return groupMapper.queryGroupByName(name);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST,key="'parentId='+#parentId")
    public List<Group> queryGroupByParentId(Integer parentId) {
        return groupMapper.queryGroupByParentId(parentId);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST,key="'roleId='+#roleId")
    public List<Map> queryGroupByRoleId(Integer roleId) {
        //查询所有的一级权限
        List<Group> parentList = groupMapper.queryGroupByParentId(0);
        List<Map> list = new ArrayList<>();
        List<Integer> selectedGroupListId = groupMapper.queryGroupIdsByRoleId(roleId);
        for (Group parent : parentList) {
            Map map = new HashMap<>();
            map.put("parentList", parent);
            List<Map> pmsInfoList = new ArrayList<>();
            //根据父权限获取子权限
            List<Group> groupList = groupMapper.queryGroupByParentId(parent.getId());
            if (groupList != null && groupList.size() > 0) {
                for (Group group : groupList) {
                    Map pmsInfoMap = new HashMap<>();
                    pmsInfoMap.put("id", group.getId());
                    pmsInfoMap.put("name", group.getName());
                    //查询指定角色的权限信息
                    if (selectedGroupListId != null) {
                        for (Integer pid : selectedGroupListId) {
                            //Integer比较大小必须用equals
                            if (pid.equals(group.getId())) {
                                pmsInfoMap.put("checkInfo", "true");
                            }
                        }
                    }
                    pmsInfoList.add(pmsInfoMap);
                }
            }
            map.put("groupList", pmsInfoList);
            list.add(map);
        }
        return list;
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST,key="'resourceId='+#resourceId")
    public List<Map> queryGroupByResourceId(Integer resourceId) {
        //查询所有的一级权限
        List<Group> parentList = groupMapper.queryGroupByParentId(0);
        List<Map> list = new ArrayList<>();
        List<Integer> selectedGroupListId = groupMapper.queryGroupIdsByResourceId(resourceId);
        for (Group parent : parentList) {
            Map map = new HashMap<>();
            map.put("parent", parent);
            List<Map> pmsInfoList = new ArrayList<>();
            //根据父权限获取子权限
            List<Group> groupList = groupMapper.queryGroupByParentId(parent.getId());
            if (groupList != null && groupList.size() > 0) {
                for (Group group : groupList) {
                    Map pmsInfoMap = new HashMap<>();
                    pmsInfoMap.put("id", group.getId());
                    pmsInfoMap.put("name", group.getName());
                    //查询指定角色的权限信息
                    if (selectedGroupListId != null) {
                        for (Integer pid : selectedGroupListId) {
                            //Integer比较大小必须用equals
                            if (pid.equals(group.getId())) {
                                pmsInfoMap.put("checkInfo", "true");
                            }
                        }
                    }
                    pmsInfoList.add(pmsInfoMap);
                }
            }
            map.put("groupList", pmsInfoList);
            list.add(map);
        }
        return list;
    }

    @Transactional
    @Override
    @Caching(evict={@CacheEvict(value = "role_isRole", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true),
            @CacheEvict(value = "role", key="'roleId='+#roleId"),
            @CacheEvict(value = "user", allEntries = true),
            @CacheEvict(value = CHACHE_KEY , allEntries=true),
            @CacheEvict(value = CHACHE_KEY_LIST , allEntries=true),
            @CacheEvict(value = "resource" , allEntries=true),
            @CacheEvict(value = "resources" , allEntries=true),
            @CacheEvict(value = "menu" , allEntries=true)})
    public void submitGroupResource(Integer groupId, String checkId){
        List<Map<String,Object>> list = new ArrayList<>() ;
        Set<Integer> set = new HashSet<>() ;
        //sys_role_resource根据roleId删除旧数据，
        groupMapper.delGroupResourceByGroupId(groupId) ;
        if(StringUtils.isNotEmpty(checkId)){
            String[] pArray = checkId.split("\\|");
            int len = pArray.length;
            //sys_role_resource根据roleId和resourceId新增新数据，
            for (int i = 0; i < len; i++) {
                Map<String,Object> map = new HashMap<>() ;
                int resourceId = Integer.parseInt(pArray[i]);
                map.put("groupId",groupId) ;
                map.put("resourceId",resourceId) ;
                list.add(map) ;
                set.add(resourceId) ;
            }

            //页面传过来的checkId没有父菜单的资源id，这里后台处理一下，父菜单的资源id查询出来放入数据库中
            List<Integer> parentList = resourceService.queryParentIdsForMenu(set) ;
            for(Integer pid:parentList){
                Map<String,Object> map = new HashMap<>() ;
                map.put("groupId",groupId) ;
                map.put("resourceId",pid) ;
                list.add(map) ;
            }

            //批量插入
            if(list!=null && list.size()>0){
                int size = list.size() ;
                int subLength = 100 ;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for(int i=0;i<insertTimes;i++){
                    List<Map<String,Object>> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(list.get(j));
                    }
                    groupMapper.saveBatchGroupResource(insertData);
                }
            }
        }
    }

    @Transactional
    @Override
    @Caching(evict={@CacheEvict(value = "role_isRole", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true),
            @CacheEvict(value = "role", key="'roleId='+#roleId"),
            @CacheEvict(value = "user", allEntries = true),
            @CacheEvict(value = CHACHE_KEY , allEntries=true),
            @CacheEvict(value = CHACHE_KEY_LIST , allEntries=true),
            @CacheEvict(value = "resource" , allEntries=true),
            @CacheEvict(value = "resources" , allEntries=true),
            @CacheEvict(value = "menu" , allEntries=true)})
    public void submitGroupRole(Integer groupId, String checkId){
        List<Map<String,Object>> list = new ArrayList<>() ;
        Set<Integer> set = new HashSet<>() ;
        //sys_role_resource根据roleId删除旧数据，
        groupMapper.delRoleGroupByGroupId(groupId) ;
        if(StringUtils.isNotEmpty(checkId)){
            String[] pArray = checkId.split("\\|");
            int len = pArray.length;
            //sys_role_group根据roleId和groupId新增新数据，
            for (int i = 0; i < len; i++) {
                Map<String,Object> map = new HashMap<>() ;
                int roleId = Integer.parseInt(pArray[i]);
                map.put("groupId",groupId) ;
                map.put("roleId",roleId) ;
                list.add(map) ;
                set.add(roleId) ;
            }

            //页面传过来的checkId没有父菜单的资源id，这里后台处理一下，父菜单的资源id查询出来放入数据库中
//            List<Integer> parentList = resourceService.queryParentIdsForMenu(set) ;
//            for(Integer pid:parentList){
//                Map<String,Object> map = new HashMap<>() ;
//                map.put("groupId",groupId) ;
//                map.put("resourceId",pid) ;
//                list.add(map) ;
//            }

            //批量插入
            if(list!=null && list.size()>0){
                int size = list.size() ;
                int subLength = 100 ;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for(int i=0;i<insertTimes;i++){
                    List<Map<String,Object>> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(list.get(j));
                    }
                    groupMapper.saveBatchGroupRole(insertData);
                }
            }
        }
    }
}
