package com.qinfei.qferp.service.impl.sys;

import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.mapper.sys.ResourceMapper;
import com.qinfei.qferp.service.sys.IResourceService;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
public class ResourceService implements IResourceService {
    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    //@Cacheable(value = CHACHE_KEY_LIST)
    public List<Resource> listAll() {
        return resourceMapper.listAll();
    }

    @Override
    //@Cacheable(value = CHACHE_KEY_LIST)
    public List<Resource> listChild() {
        return resourceMapper.listChild();
    }

    @Override
    public PageInfo<Resource> listPg(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        if (map.get("parentId") != null) {
            List<Integer> parentIdList = new ArrayList<>();
            for (String parentId : String.valueOf(map.get("parentId")).split(",")) {
                parentIdList.add(Integer.parseInt(parentId));
            }
            map.put("parentId", parentIdList);
        }
        List<Resource> list = resourceMapper.listPg(map);
        return new PageInfo<>(list);
    }

    @Override
//    @Cacheable(value = "resource", key = "'pageNum='+#pageable.getPageNumber()")
    public PageInfo<Resource> search(Pageable pageable, Resource resource) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        String name = resource.getName();
        if (!StringUtils.isEmpty(name))
            resource.setName("%" + name + "%");
        List<Resource> list = resourceMapper.list(resource);
        return new PageInfo<>(list);
    }

    /**
     * 添加权限
     *
     * @param resource
     * @return
     */
    @Override
    @Caching(evict = {@CacheEvict(value = CHACHE_KEY_LIST, allEntries = true),
            @CacheEvict(value = "menu", allEntries = true)})
    public Resource add(Resource resource, Integer userId) {
//      resource.setState(IConst.STATE_FINISH);
        if (StringUtils.isEmpty(resource.getUrl())) {
            resource.setUrl("");//设置空字符
        }
        resource.setCreateTime(new Date());
        resource.setCreator(userId);
        resourceMapper.insert(resource);
        return resource;
    }

    /**
     * 编辑权限
     *
     * @param resource
     * @return
     */
    @Override
    @Caching(evict = {@CacheEvict(value = CHACHE_KEY, key = "'id='+#userId"),
            @CacheEvict(value = CHACHE_KEY, key = "'id='+#resource.id"),
            @CacheEvict(value = CHACHE_KEY_LIST, allEntries = true),
            @CacheEvict(value = "menu", allEntries = true)})
    public Resource edit(Resource resource, Integer userId) {
        if (StringUtils.isEmpty(resource.getUrl())) {
            resource.setUrl("");//设置空字符
        }
        resource.setUpdateTime(new Date());
        resource.setUpdateUserId(userId);
        resourceMapper.update(resource);
        return resource;
    }

    @Override
    @Caching(evict = {@CacheEvict(value = CHACHE_KEY, key = "'id='+#id"),
            @CacheEvict(value = CHACHE_KEY_LIST, allEntries = true),
            @CacheEvict(value = "menu", allEntries = true)})
    public void delById(@RequestParam("id") Integer id) {
        Resource resource = resourceMapper.getById(id);
        resource.setState(IConst.STATE_DELETE);
        resource.setUpdateTime(new Date());
        resourceMapper.update(resource);
    }

    @Override
    @Cacheable(value = CHACHE_KEY, key = "'id='+#id")
    public Resource getById(Integer id) {
        return resourceMapper.getById(id);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'name='+#name")
    public List<Resource> queryResourceByName(String name) {
        return resourceMapper.queryResourceByName(name);
    }

    @Override
    @Cacheable(value = "menu", key = "'userId='+#userId", sync = true)
    public List<Resource> queryMenuByUserId(Integer userId) {
        List<Resource> tree;
        if (userId == -1) {
            tree = listToTree(resourceMapper.listAllMenu());
        } else {
            List<Resource> userMenu = resourceMapper.listUserMenu(userId);
            tree = listToTree(userMenu, resourceMapper.listAllMenu());
        }
        return tree;
    }

    //将菜单列表装换成菜单树
    private List<Resource> listToTree(List<Resource> allList) {
        List<Resource> result = new ArrayList<>();
        for (Resource resource : allList) {
            if (resource.getParentId() == 0) {
                result.add(resource);
                addChildMenu(resource, allList);
            }
        }
        return result;
    }

    //用户权限渲染菜单树
    private List<Resource> listToTree(List<Resource> userMenu, List<Resource> allList) {
        List<Resource> result = new ArrayList<>();
        List<Integer> userMenuId = new ArrayList<>();
        Map<Integer, Resource> allResourceMap = new HashMap<>();
        for (Resource resource : allList) {
            allResourceMap.put(resource.getId(), resource);
        }
        //缓存用户实际有的资源权限
        for (Resource resource : userMenu) {
            userMenuId.add(resource.getId());
        }
        //根据用户实际资源权限，获取所有父节点，并判断是否有父节点的权限，没有就将父节点的链接设置成空
        for (Resource resource : userMenu) {
            result.add(resource);
            addParentMenu(resource, userMenuId, allResourceMap, result);
        }
        return listToTree(result);//将含有父节点的用户资源封装成菜单树
    }

    //添加父级菜单
    private void addParentMenu(Resource currentResource, List<Integer> userMenuId, Map<Integer, Resource> allResourceMap, List<Resource> result) {
        //如果不是一级菜单，并且父级菜单存在
        if (currentResource.getParentId() != 0 && allResourceMap.get(currentResource.getParentId()) != null) {
            //如果用户实际有的资源权限包含该父级菜单则链接不改变，否则链接设置成空
            if (!userMenuId.contains(allResourceMap.get(currentResource.getParentId()).getId())) {
                allResourceMap.get(currentResource.getParentId()).setUrl("");
                result.add(allResourceMap.get(currentResource.getParentId()));//添加父级菜单
                userMenuId.add(allResourceMap.get(currentResource.getParentId()).getId());//添加父级菜单
            }
            addParentMenu(allResourceMap.get(currentResource.getParentId()), userMenuId, allResourceMap, result);//递归寻找父节点
        }
    }

    //添加子菜单
    private void addChildMenu(Resource parentMenu, List<Resource> allList) {
        List<Resource> childMenu = new ArrayList<>();
        for (Resource resource : allList) {
            if (parentMenu.getId().equals(resource.getParentId())) {
                childMenu.add(resource);
                addChildMenu(resource, allList);
            }
        }
        parentMenu.setChilds(childMenu);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'userId='+#userId")
    public List<Resource> queryResourceByUserId(Integer userId) {
        return resourceMapper.queryResourceByUserId(userId);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'userIdNew='+#userId")
    public List<Resource> queryResourceByUserIdNew(Integer userId) {
        return resourceMapper.queryResourceByUserIdNew(userId);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'userIdNew=admin'")
    public List<Resource> queryResourceByAdmin() {
        return resourceMapper.queryResourceByAdmin();
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'parentId='+#parentId")
    public List<Resource> queryResourceByParentId(Integer parentId) {
        return resourceMapper.queryResourceByParentId(parentId);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'roleId='+#roleId")
    public List<Map> queryResourceByRoleId(Integer roleId) {
        //查询所有的一级权限
        List<Resource> parentList = resourceMapper.queryResourceByParentId(0);
        List<Map> list = new ArrayList<>();
        List<Integer> selectedResourceListId = resourceMapper.queryResourceIdsByRoleId(roleId);
        for (Resource parent : parentList) {
            Map map = new HashMap<>();
            map.put("parentList", parent);
            List<Map> pmsInfoList = new ArrayList<>();
            //根据父权限获取子权限
            List<Resource> resourceList = resourceMapper.queryResourceByParentId(parent.getId());
            if (resourceList != null && resourceList.size() > 0) {
                for (Resource resource : resourceList) {
                    Map pmsInfoMap = new HashMap<>();
                    pmsInfoMap.put("id", resource.getId());
                    pmsInfoMap.put("name", resource.getName());
                    pmsInfoMap.put("link", resource.getUrl());
                    //查询指定角色的权限信息
                    if (selectedResourceListId != null) {
                        for (Integer pid : selectedResourceListId) {
                            //Integer比较大小必须用equals
                            if (pid.equals(resource.getId())) {
                                pmsInfoMap.put("checkInfo", "true");
                            }
                        }
                    }
                    pmsInfoList.add(pmsInfoMap);
                }
            }
            map.put("resourceList", pmsInfoList);
            list.add(map);
        }
        return list;
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'groupId='+#groupId")
    public List<Resource> queryResourcesByGroupId(Integer groupId) {
        return resourceMapper.queryResourcesByGroupId(groupId);
    }

    @Override
    @Cacheable(value = CHACHE_KEY_LIST, key = "'relatedGroupId='+#groupId")
    public List<Map<String, Object>> queryResourceByGroupId(Integer groupId) {
        /*//查询所有的一级权限
        List<Resource> parentList = resourceMapper.queryResourceByParentId(0);
        List<Map> list = new ArrayList<>();
        List<Integer> selectedResourceListId = resourceMapper.queryResourceIdsByGroupId(groupId);
        for (Resource parent : parentList) {
            Map map = new HashMap<>();
            map.put("parentList", parent);
            List<Map> pmsInfoList = new ArrayList<>();
            //根据父权限获取子权限
            List<Resource> resourceList = resourceMapper.queryResourceByParentId(parent.getId());
            if (resourceList != null && resourceList.size() > 0) {
                for (Resource resource : resourceList) {
                    Map pmsInfoMap = new HashMap<>();
                    pmsInfoMap.put("id", resource.getId());
                    pmsInfoMap.put("name", resource.getName());
                    pmsInfoMap.put("link", resource.getUrl());
                    //查询指定角色的权限信息
                    if (selectedResourceListId != null) {
                        for (Integer pid : selectedResourceListId) {
                            //Integer比较大小必须用equals
                            if (pid.equals(resource.getId())) {
                                pmsInfoMap.put("checkInfo", "true");
                            }
                        }
                    }
                    pmsInfoList.add(pmsInfoMap);
                }
            }
            map.put("resourceList", pmsInfoList);
            list.add(map);
        }*/

        //获取所有资源
        List<Map<String, Object>> allResource = resourceMapper.listAllResource();
        List<Integer> listGroupResource = resourceMapper.queryResourceIdsByGroupId(groupId);
        //设置一级菜单打开
        for (Map<String, Object> resource : allResource) {
            Integer id = Integer.parseInt(String.valueOf(resource.get("id")));
            //如果分组包含当前节点，则选中
            if (listGroupResource.contains(id)) {
                resource.put("checked", true);
            }
        }
        return allResource;
    }

    @Override
    public List<Integer> queryParentIdsForMenu(Set<Integer> set) {
        return resourceMapper.queryParentIdsForMenu(set);
    }

    @Transactional
    @Override
    @Caching(evict = {@CacheEvict(value = "role_isRole", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true),
            @CacheEvict(value = "role", key = "'roleId='+#roleId"),
            @CacheEvict(value = "user", allEntries = true),
            @CacheEvict(value = "group", allEntries = true),
            @CacheEvict(value = "groups", allEntries = true),
            @CacheEvict(value = CHACHE_KEY, allEntries = true),
            @CacheEvict(value = CHACHE_KEY_LIST, allEntries = true),
            @CacheEvict(value = "menu", allEntries = true)})
    public void submitResourceGroup(Integer resourceId, String checkId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        //当前只有resource表的子菜单和资源组的id，如果这些资源组没有和资源的父菜单关联，添加了菜单也不会显示出来，
        // 所以要先判断一下这些组有没有这个资源的父菜单关联数据，没有的话要放入set中
        if (!StringUtils.isEmpty(checkId)) {
            String[] pArray = checkId.split("\\|");
            int len = pArray.length;
            Resource resource = this.getById(resourceId);
            for (int i = 0; i < len; i++) {
                Map<String, Object> map = new HashMap<>();
                int groupId = Integer.parseInt(pArray[i]);
                //查一下这些组和这个资源的上级菜单有没有关联
                List<Resource> resources = resourceMapper.queryResourcesByGroupIdAndResourceId(groupId, resource.getParentId());
                //如果没有，要把这个资源的父级和资源组关联
                if (resources == null || resources.size() == 0) {
                    Map<String, Object> temp = new HashMap<>();
                    temp.put("resourceId", resource.getParentId());
                    temp.put("groupId", groupId);
                    list.add(temp);
                    set.add(groupId);
                }
                map.put("resourceId", resourceId);
                map.put("groupId", groupId);
                list.add(map);
                set.add(groupId);
            }
        }

        //sys_role_resource根据roleId删除旧数据，
        resourceMapper.delGroupResourceByResourceId(resourceId);
        //批量插入
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
                resourceMapper.saveBatch(insertData);
            }
        }
    }

    @Cacheable(value = CHACHE_KEY_LIST, key = "'allMenu'")
    @Override
    public List<Resource> listAllMenu() {
        return listToTree(resourceMapper.listAllMenu());
    }
}
