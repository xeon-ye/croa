package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ResourceMapper extends BaseMapper<Resource, Integer> {

    @Select("select * from sys_resource where state>-2 order by id desc")
    List<Resource> listAll();

    @Select("select * from sys_resource where state>-2 and parent_id > 0 order by id desc")
    List<Resource> listChild();

    @Select({"<script>",
            " SELECT a.*,b.name parentName,c.id userId,c.user_name userName,c.name uname," +
                    "d.id updateUserId, d.user_name updateUserName, d.name updateName FROM\n" +
                    "  sys_resource a LEFT JOIN sys_user c  ON a.creator = c.id \n" +
                    "  LEFT JOIN sys_user d ON a.`update_user_id` = d.id \n" +
                    "  LEFT JOIN sys_resource b ON a.parent_id = b.id AND b.state > - 2  " +
                    " WHERE a.state>-2 " +
                    " <when test='name!=null and name!=\"\"'>",
                " AND a.name like '%${name}%'",
            " </when>",
            " <when test='url!=null and url!=\"\"'>",
                " AND a.url like '%${url}%'",
            " </when>",
            " <when test='parentId != null and parentId.size() > 0'>",
            "   AND a.parent_id in ",
            "   <foreach item=\"item\" index=\"index\" collection=\"parentId\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            " </when>",
            " <when test='state!=null and state!=\"\"'>",
            " AND a.state = #{state}",
            " </when>",
            " <when test='isMenu!=null and isMenu!=\"\"'>",
            " AND a.is_menu = #{isMenu}",
            " </when>",
            " order by ",
            " <when test='sidx != null and sidx != \"\"'>",
            " ${sidx} ${sord}",
            " </when>",
            " <when test='sidx == null or sidx ==  \"\"'>",
            " a.id desc",
            " </when>",
            "</script>"})
    @Results({@Result(column = "userId", property = "user.id"),
            @Result(column = "userName", property = "user.userName"),
            @Result(column = "uname", property = "user.name"),
            @Result(column = "parentName", property = "parent.name"),
            @Result(column = "updateUserId", property = "updateUser.id"),
            @Result(column = "updateUserName", property = "updateUser.userName"),
            @Result(column = "updateName", property = "updateUser.name"),
    })
    List<Resource> listPg(Map map);

    @Select("select * from sys_resource where  state>-2 and  id=#{id}")
    Resource getById(@Param("id") Integer id);

    /**
     * 根据父节点查询子节点的资源
     * @param parentId 父节点id
     * @return 子节点资源列表
     */
    @Select("select * from sys_resource  where state=0  and parent_id=#{parentId} order by sort")
    List<Resource> queryResourceByParentId(@Param("parentId") Integer parentId);

    /**
     * 根据父节点查询子节点的资源
     * @param parentId 父节点id
     * @return 子节点资源列表
     */
    @Select("select * from sys_resource  where state=0 and is_menu=0 and parent_id=#{parentId} order by sort")
    List<Resource> queryMenuByParentId(@Param("parentId") Integer parentId);

    //获取所有菜单
    @Select("select * from sys_resource  where state=0 and is_menu=0 order by sort")
    List<Resource> listAllMenu();

    @Select("select id, parent_id as pId, `name`, is_menu as isMenu  from sys_resource  where state = 0 order by sort")
    List<Map<String, Object>> listAllResource();

    //获取用户所有菜单
    @Select(" SELECT distinct g.id id,g.parent_id parentId,g.name name,g.url url,g.state state," +
            " g.creator creator,g.create_time createTime,g.update_user_id updateUserId,g.update_time updateTime," +
            " g.is_menu isMenu,g.icon icon,g.sort sort  " +
            " FROM sys_user a, sys_user_role b,\n" +
            " sys_role c,sys_role_group d,sys_group e, sys_group_resource f, sys_resource g \n" +
            " WHERE a.id = b.user_id AND b.role_id = c.id AND c.id = d.role_id \n" +
            " AND d.group_id = e.id AND e.id=f.group_id AND f.resource_id=g.id AND a.state > - 2 AND c.state > - 2 " +
            " AND e.state > -2 AND g.state = 0  AND g.is_menu=0 AND a.id = #{userId} order by g.sort ")
    List<Resource> listUserMenu(@Param("userId") Integer userId);

    /**
     * 根据父节点查询子节点的资源
     * @param parentId 父节点id
     * @return 子节点资源列表
     */
    @Select("select * from sys_resource  where state=0 and is_menu=0 and parent_id=#{parentId} order by sort")
    List<Resource> queryMenuByParentIdNew(@Param("parentId") Integer parentId);
    /**
     * 根据名称查找资源
     * @param name
     * @return
     */
    @Select("SELECT * FROM sys_resource where name=#{name} and state=0 ")
    List<Resource> queryResourceByName(@Param("name") String name);

    /**
     * 根据用户id和父菜单查询子菜单
     * @param userId 用户id
     * @param parentId 父节点id，如果查询父节点则parentId=0
     * @return
     */
    @Select(" SELECT distinct e.id id,e.parent_id parentId,e.name name,e.url url,e.state state," +
            " e.creator creator,e.create_time createTime,e.update_user_id updateUserId,e.update_time updateTime," +
            " e.is_menu isMenu,e.icon icon,e.sort sort  " +
            " FROM sys_user a, sys_user_role b,\n" +
            " sys_role c, sys_role_resource d, sys_resource e \n" +
            " WHERE a.id = b.user_id AND b.role_id = c.id AND c.id = d.role_id \n" +
            " AND d.resource_id = e.id AND a.state > - 2 AND c.state > - 2 " +
            " AND e.state = 0  AND e.is_menu=0 AND a.id = #{userId} AND e.parent_id = #{parentId} order by e.sort ")
    List<Resource> queryMenuByUserIdAndParentId(@Param("userId") Integer userId, @Param("parentId") Integer parentId);

    /**
     * 根据用户id和父菜单查询子菜单
     * @param userId 用户id
     * @param parentId 父节点id，如果查询父节点则parentId=0
     * @return
     */
    @Select(" SELECT distinct g.id id,g.parent_id parentId,g.name name,g.url url,g.state state," +
            " g.creator creator,g.create_time createTime,g.update_user_id updateUserId,g.update_time updateTime," +
            " g.is_menu isMenu,g.icon icon,g.sort sort  " +
            " FROM sys_user a, sys_user_role b,\n" +
            " sys_role c,sys_role_group d,sys_group e, sys_group_resource f, sys_resource g \n" +
            " WHERE a.id = b.user_id AND b.role_id = c.id AND c.id = d.role_id \n" +
            " AND d.group_id = e.id AND e.id=f.group_id AND f.resource_id=g.id AND a.state > - 2 AND c.state > - 2 " +
            " AND e.state > -2 AND g.state = 0  AND g.is_menu=0 AND a.id = #{userId} AND g.parent_id = #{parentId} order by g.sort ")
    List<Resource> queryMenuByUserIdAndParentIdNew(@Param("userId") Integer userId, @Param("parentId") Integer parentId);

    /**
     * 根据用户id查询所有的权限
     * @param userId
     * @return
     */
    @Select(" SELECT distinct e.id id,e.parent_id parentId,e.name name,e.url url,e.state state," +
            " e.creator creator,e.create_time createTime,e.update_user_id updateUserId,e.update_time updateTime," +
            " e.is_menu isMenu,e.icon icon,e.sort sort  " +
            " FROM sys_user a, sys_user_role b,\n" +
            " sys_role c, sys_role_resource d, sys_resource e \n" +
            " WHERE a.id = b.user_id AND b.role_id = c.id AND c.id = d.role_id \n" +
            " AND d.resource_id = e.id AND a.state > - 2 AND c.state > - 2 " +
            " AND e.state = 0 AND a.id = #{userId} order by e.sort ")
    List<Resource> queryResourceByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户id查询所有的权限
     * @param userId
     * @return
     */
    @Select(" SELECT distinct g.id id,g.parent_id parentId,g.name name,g.url url,g.state state," +
            " g.creator creator,g.create_time createTime,g.update_user_id updateUserId,g.update_time updateTime," +
            " g.is_menu isMenu,g.icon icon,g.sort sort  " +
            " FROM sys_user a, sys_user_role b,\n" +
            " sys_role c, sys_role_group d, sys_group e, sys_group_resource f, sys_resource g \n" +
            " WHERE a.id = b.user_id AND b.role_id = c.id AND c.id = d.role_id \n" +
            " AND d.group_id = e.id AND e.id=f.group_id AND f.resource_id=g.id AND a.state > - 2 AND c.state > - 2 " +
            " AND e.state = 0 AND g.state=0 AND a.id = #{userId} order by g.sort ")
    List<Resource> queryResourceByUserIdNew(@Param("userId") Integer userId);

    /**
     * 根据用户id查询所有的权限
     * @param userId
     * @return
     */
    @Select(" SELECT distinct g.id id,g.parent_id parentId,g.name name,g.url url,g.state state," +
            " g.creator creator,g.create_time createTime,g.update_user_id updateUserId,g.update_time updateTime," +
            " g.is_menu isMenu,g.icon icon,g.sort sort  " +
            " FROM sys_resource g \n" +
            " WHERE g.state=0 order by g.sort ")
    List<Resource> queryResourceByAdmin();

    @Select("select resource_id resourceId from sys_role_resource where role_id=#{roleId} ")
    List<Integer> queryResourceIdsByRoleId(@Param("roleId") Integer roleId);

    @Select("select resource_id resourceId from sys_group_resource where group_id=#{groupId} ")
    List<Integer> queryResourceIdsByGroupId(@Param("groupId") Integer groupId);

    @Select("select b.* from sys_group_resource a left join sys_resource b on a.resource_id=b.id and b.state>-2 where a.group_id=#{groupId} ")
    List<Resource> queryResourcesByGroupId(@Param("groupId") Integer groupId);

    @Select("select b.id from sys_group_resource a left join sys_resource b on a.resource_id=b.id and b.state>-2 where a.group_id=#{groupId} and b.id=#{resourceId} ")
    List<Resource> queryResourcesByGroupIdAndResourceId(@Param("groupId") Integer groupId, @Param("resourceId") Integer resourceId);

    @Select("<script>" +
            "select parent_id from sys_resource where is_menu=0 and id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>")
    List<Integer> queryParentIdsForMenu(@Param("list") Set<Integer> list);

    @Delete("delete from sys_group_resource where resource_id=#{resourceId}")
    void delGroupResourceByResourceId(@Param("resourceId") Integer resourceId);

    @Insert({"<script>",
            " insert into sys_group_resource (" +
                    "group_id," +
                    "resource_id" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.groupId}," +
                    "#{item.resourceId})" +
                    "</foreach>",
            "</script>"})
    void saveBatch(List<Map<String, Object>> list);
}
