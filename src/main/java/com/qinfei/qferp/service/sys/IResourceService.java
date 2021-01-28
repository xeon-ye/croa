package com.qinfei.qferp.service.sys;

import com.qinfei.qferp.entity.sys.Resource;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IResourceService {
    String CHACHE_KEY = "resource";
    String CHACHE_KEY_LIST = "resources";

    PageInfo<Resource> search(Pageable pageable, Resource resource);

    //@Cacheable(value = CHACHE_KEY_LIST)
    List<Resource> listAll();

    //@Cacheable(value = CHACHE_KEY_LIST)
    List<Resource> listChild();

    PageInfo<Resource> listPg(int pageNum, int pageSize, Map map);

    Resource add(Resource Resource, Integer userId);

    Resource edit(Resource Resource, Integer userId);

    void delById(Integer id);

    Resource getById(Integer id);

    /**
     * 根据名称查找资源
     * @param name 资源名称
     * @return 资源列表
     */
    List<Resource> queryResourceByName(String name);


    /**
     * 加载菜单
     * @param userId 用户id
     * @return 有层级关系的菜单list
     */
    List<Resource> queryMenuByUserId(Integer userId);

    /**
     * 根据用户查找拥有的权限
     * @param userId 用户id
     * @return 用户具有的资源列表
     */
    List<Resource> queryResourceByUserId(Integer userId);

    //    @Cacheable(value = "resources", key = "'userId='+#userId")
    List<Resource> queryResourceByUserIdNew(Integer userId);

    @Cacheable(value = CHACHE_KEY_LIST, key = "'userIdNew=admin'")
    List<Resource> queryResourceByAdmin();

    /**
     * 根据父级资源id查找资源
     * @param parentId 父级资源id
     * @return 资源列表
     */
    List<Resource> queryResourceByParentId(Integer parentId);

    /**
     * 根据角色id查找资源
     * @param roleId
     * @return
     */
    List<Map> queryResourceByRoleId(Integer roleId);

    List<Resource> queryResourcesByGroupId(Integer groupId);

    List<Map<String, Object>> queryResourceByGroupId(Integer groupId);

    List<Integer> queryParentIdsForMenu(Set<Integer> set);

    @Transactional
    void submitResourceGroup(Integer resourceId, String checkId);

    List<Resource> listAllMenu();
}
