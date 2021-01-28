package com.qinfei.qferp.service.sys;

import com.qinfei.qferp.entity.sys.Group;
import com.qinfei.qferp.entity.sys.Role;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGroupService {
    String CHACHE_KEY = "group";
    String CHACHE_KEY_LIST = "groups";

    PageInfo<Group> search(Pageable pageable, Group group);

    PageInfo<Group> listPg(int pageNum, int pageSize, Map map);

    List<Group> listAll();

    List<Group> listAllChild();

    Group add(Group Group, Integer userId);

    Group edit(Group Group, Integer userId);

    void delById(Integer id);

    Group getById(Integer id);

    /**
     * 根据名称查找资源组
     * @param name 资源组名称
     * @return 资源组列表
     */
    List<Group> queryGroupByName(String name);

    /**
     * 根据父级资源id查找资源组
     * @param parentId 父级资源组id
     * @return 资源组列表
     */
    List<Group> queryGroupByParentId(Integer parentId);

    /**
     * 根据角色id查找资源组
     * @param roleId
     * @return
     */
    List<Map> queryGroupByRoleId(Integer roleId);

    List<Map> queryGroupByResourceId(Integer resourceId);

    void submitGroupResource(Integer groupId, String checkId);

    @Transactional
    @Caching(evict={@CacheEvict(value = "role_isRole", allEntries = true),
            @CacheEvict(value = "roles", allEntries = true),
            @CacheEvict(value = "role", key="'roleId='+#roleId"),
            @CacheEvict(value = "user", allEntries = true),
            @CacheEvict(value = CHACHE_KEY , allEntries=true),
            @CacheEvict(value = CHACHE_KEY_LIST , allEntries=true),
            @CacheEvict(value = "resource" , allEntries=true),
            @CacheEvict(value = "resources" , allEntries=true),
            @CacheEvict(value = "menu" , allEntries=true)})
    void submitGroupRole(Integer groupId, String checkId);
}
