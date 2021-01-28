package com.qinfei.qferp.service.sys;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.sys.Role;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

public interface IRoleService {

	String CHACHE_KEY = "role";
	String CHACHE_KEY_LIST = "roles";

	void delById(Integer id);

	List<Role> listAll();

	PageInfo<Role> listPg(int pageNum, int pageSize, Map map);

	Role getById(Integer id);

	void save(Role role);

	void update(Role role);

	List<Role> queryRoleByName(String name);

	/**
	 * 查询某个用户的所有角色列表
	 *
	 * @param userId
	 *            用户ID
	 * @return
	 */
	List<Role> queryRoleByUserId(Integer userId);

	/**
	 * 判断某个用户是否是某个角色
	 *
	 * @param userId
	 *            用户ID
	 * @param roleType
	 *            角色类型
	 * @return
	 */
	boolean isRole(Integer userId, String roleType);

	/**
	 * 获取用户的角色信息集合；
	 *
	 * @return ：用户的角色数据信息集合；
	 */
	Map<Integer, List<Role>> listUserRoles();

	@Transactional
	void submitRoleResource(Integer roleId, String checkId);

	@Transactional
	void submitRoleGroup(Integer roleId, String checkId);

	List<Integer> getUserByRoleId(Integer id);

    List<Role> queryRoleByGroupId(Integer groupId);

	List<Role> queryRoleByRoleType(String nameQc);

	PageInfo<Role> nwRoleList(Integer pageNum, Integer pageSize,String keyword);
}
