package com.qinfei.qferp.service.impl.sys;

import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.sys.RoleMapper;
import com.qinfei.qferp.service.sys.IResourceService;
import com.qinfei.qferp.service.sys.IRoleService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoleService implements IRoleService {
	@Autowired
	private RoleMapper roleMapper;
	@Autowired
	private IResourceService resourceService;

	@Override
	@Caching(evict={@CacheEvict(value = CHACHE_KEY , key = "'id='+#id"),
			@CacheEvict(value = CHACHE_KEY_LIST , allEntries=true)})
	public void delById(Integer id) {
		Role role = roleMapper.get(Role.class, id);
		role.setState(IConst.STATE_DELETE);
		role.setUpdateUserId(AppUtil.getUser().getId());
		role.setUpdateTime(new Date());
		//维护角色资源表
		roleMapper.delRoleGroupByRoleId(id);
		//维护用户角色表
		roleMapper.delUserRoleByRoleId(id);
		roleMapper.update(role);
	}

	@Override
//	@Cacheable(value = CHACHE_KEY_LIST, key = "'listAll=listAll'")
	public List<Role> listAll() {
		List<Role> roles;
		//判断是否为分公司，分公司不能获取集团下的角色
		User user = AppUtil.getUser();
		if(user!=null && !IConst.COMPANY_CODE_XH.equals(user.getCompanyCode()) && !IConst.COMPANY_CODE_JT.equals(user.getCompanyCode())){
			roles = roleMapper.listFilialeAll();
		}else{
			roles = roleMapper.listAll();
		}
		return roles;
	}

	@Override
	public PageInfo<Role> listPg(int pageNum, int pageSize, Map map) {
		PageHelper.startPage(pageNum, pageSize);
		List<Role> roles = roleMapper.listPg(map);
        return new PageInfo<>(roles);
	}

	@Override
	@Cacheable(value = CHACHE_KEY, key = "'id='+#id")
	public Role getById(Integer id) {
        return roleMapper.getById(id);
	}

	@Override
	@Caching(evict={@CacheEvict(value = CHACHE_KEY_LIST , allEntries=true)})
	public void save(Role role) {
		roleMapper.insert(role);
	}

	@Override
	@Caching(evict={@CacheEvict(value = CHACHE_KEY , key = "'id='+#role.getId()"),
			@CacheEvict(value = CHACHE_KEY_LIST , allEntries=true)})
	public void update(Role role) {
		roleMapper.update(role);
	}

	@Override
	public List<Role> queryRoleByName(String name) {
        return roleMapper.queryRoleByName(name);
	}

	/**
	 * 查询某个用户的所有角色列表
	 *
	 * @param userId
	 *            用户ID
	 * @return
	 */
	@Override
	@Cacheable(value = CHACHE_KEY_LIST, key = "'userId='+#userId")
	public List<Role> queryRoleByUserId(Integer userId) {
		return roleMapper.queryRoleByUserId(userId);
	}

	/**
	 * 判断某个用户是否是某个角色
	 *
	 * @param userId
	 *            用户ID
	 * @param roleType
	 *            角色名称
	 * @return
	 */
	@Override
	@Cacheable(value = "role_isRole", key = "'userId='+#userId+'roleType='+#roleType")
	public boolean isRole(Integer userId, String roleType) {
		List<Role> list = this.queryRoleByUserId(userId);
		if (list != null)
			for (Role role : list) {
				//政委不用显示客户保护数据数量，媒体板块赋权
				if (role.getType().indexOf(roleType) > -1 && role.getCode().indexOf("ZW")==-1)
					return true;
			}
		return false;
	}

	/**
	 * 获取用户的角色信息集合；
	 *
	 * @return ：用户的角色数据信息集合；
	 */
	@Override
	public Map<Integer, List<Role>> listUserRoles() {
		Map<Integer, List<Role>> datas = new HashMap<>();
		// 查询权限表的所有数据；
		List<Role> userRoles = roleMapper.querySelRole();
		// 查询所有角色数据；
		List<Role> roles = roleMapper.listAll();
		// 数据处理；
		Map<Integer, Role> roleMap = new HashMap<>();
		for (Role role : roles) {
			roleMap.put(role.getId(), role);
		}
		// 封装用户的角色；
		List<Role> userRoleData;
		int roleId;
		for (Role role : userRoles) {
			// 获取用户的ID；
			roleId = role.getCompanyId();
			// 获取用户的角色集合；
			userRoleData = datas.get(roleId);
			// 如果没有初始化；
			if (userRoleData == null) {
				// 初始化集合；
				userRoleData = new ArrayList<>();
				datas.put(roleId, userRoleData);
			}
			userRoleData.add(roleMap.get(role.getId()));
		}
		return datas;
	}

	@Transactional
	@Override
	@Caching(evict={@CacheEvict(value = "role_isRole", allEntries = true),
					@CacheEvict(value = CHACHE_KEY_LIST, allEntries = true),
					@CacheEvict(value = CHACHE_KEY, key="'roleId='+#roleId"),
					@CacheEvict(value = "user", allEntries = true),
					@CacheEvict(value = "group" , allEntries=true),
					@CacheEvict(value = "groups" , allEntries=true),
					@CacheEvict(value = "resource" , allEntries=true),
					@CacheEvict(value = "resources" , allEntries=true),
					@CacheEvict(value = "menu" , allEntries=true)})
	public void submitRoleResource(Integer roleId, String checkId){
		List<Map<String,Object>> list = new ArrayList<>() ;
		Set<Integer> set = new HashSet<>() ;
		String[] pArray = checkId.split("\\|");
		int len = pArray.length;
		//sys_role_resource根据roleId删除旧数据，
		roleMapper.delRoleGroupByRoleId(roleId) ;
		//sys_role_resource根据roleId和resourceId新增新数据，
		for (int i = 0; i < len; i++) {
			Map<String,Object> map = new HashMap<>() ;
			int groupId = Integer.parseInt(pArray[i]);
			map.put("roleId",roleId) ;
			map.put("groupId",groupId) ;
			list.add(map) ;
			set.add(groupId) ;
		}

		//页面传过来的checkId没有父菜单的资源id，这里后台处理一下，父菜单的资源id查询出来放入数据库中
//		List<Integer> parentList = resourceService.queryParentIdsForMenu(set) ;
//		for(Integer pid:parentList){
//			Map<String,Object> map = new HashMap<>() ;
//			map.put("roleId",roleId) ;
//			map.put("groupId",pid) ;
//			list.add(map) ;
//		}

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
				roleMapper.saveBatch(insertData);
			}
		}
	}

	@Transactional
	@Override
	@Caching(evict={@CacheEvict(value = "role_isRole", allEntries = true),
			@CacheEvict(value = CHACHE_KEY_LIST, allEntries = true),
			@CacheEvict(value = CHACHE_KEY, key="'roleId='+#roleId"),
			@CacheEvict(value = "user", allEntries = true),
			@CacheEvict(value = "group" , allEntries=true),
			@CacheEvict(value = "groups" , allEntries=true),
			@CacheEvict(value = "resource" , allEntries=true),
			@CacheEvict(value = "resources" , allEntries=true),
			@CacheEvict(value = "menu" , allEntries=true)})
	public void submitRoleGroup(Integer roleId, String checkId){
		List<Map<String,Object>> list = new ArrayList<>() ;
		Set<Integer> set = new HashSet<>() ;
		String[] pArray = checkId.split("\\|");
		int len = pArray.length;
		//sys_role_resource根据roleId删除旧数据，
		roleMapper.delRoleGroupByRoleId(roleId) ;
		//sys_role_resource根据roleId和resourceId新增新数据，
		for (int i = 0; i < len; i++) {
			Map<String,Object> map = new HashMap<>() ;
			int groupId = Integer.parseInt(pArray[i]);
			map.put("roleId",roleId) ;
			map.put("groupId",groupId) ;
			list.add(map) ;
			set.add(groupId) ;
		}

		//页面传过来的checkId没有父菜单的资源id，这里后台处理一下，父菜单的资源id查询出来放入数据库中
		List<Integer> parentList = resourceService.queryParentIdsForMenu(set) ;
		for(Integer pid:parentList){
			Map<String,Object> map = new HashMap<>() ;
			map.put("roleId",roleId) ;
			map.put("groupId",pid) ;
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
				roleMapper.saveBatch(insertData);
			}
		}
	}

	@Override
	public List<Integer> getUserByRoleId(Integer id) {
		return roleMapper.getUserByRoleId(id);
	}

	@Override
	public List<Role> queryRoleByGroupId(Integer groupId) {
		return roleMapper.queryRoleByGroupId(groupId);
	}

	@Override
	public List<Role> queryRoleByRoleType(String nameQc) {
		return roleMapper.queryRoleByRoleType(nameQc);
	}

	@Override
	public PageInfo<Role> nwRoleList(Integer pageNum, Integer pageSize,String keyword) {
		PageHelper.startPage(pageNum, pageSize);
		List<Role> roles = roleMapper.nwList(keyword);
		return new PageInfo<>(roles);
	}
}
