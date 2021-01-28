package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.Group;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GroupMapper extends BaseMapper<Group, Integer> {

    @Select("select * from sys_group where state>-2 order by id desc")
    List<Group> listAll();

    @Select("select * from sys_group where state>-2 and parent_id>0 order by id desc")
    List<Group> listAllChild();

    @Select({"<script>",
            " SELECT a.*,b.name parentName,c.id userId,c.user_name userName,c.name uname," +
                    "d.id updateUserId, d.user_name updateUserName, d.name updateName FROM\n" +
                    "  sys_group a LEFT JOIN sys_user c  ON a.creator = c.id \n" +
                    "  LEFT JOIN sys_user d ON a.`update_user_id` = d.id \n" +
                    "  LEFT JOIN sys_group b ON a.parent_id = b.id AND b.state > - 2  " +
                    " WHERE a.state>-2 " +
                    " <when test='name!=null and name!=\"\"'>",
                " AND a.name like '%${name}%'",
            " </when>",
            " <when test='parentId!=null and parentId!=\"\"'>",
                " AND a.parent_id = #{parentId}",
            " </when>",
            " <when test='state!=null and state!=\"\"'>",
            " AND a.state = #{state}",
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
    List<Group> listPg(Map map);

    @Select("select * from sys_group where  state>-2 and  id=#{id}")
    Group getById(@Param("id") Integer id);

    /**
     * 根据父节点查询子节点的资源
     * @param parentId 父节点id
     * @return 子节点资源列表
     */
    @Select("select * from sys_group  where state=0  and parent_id=#{parentId} order by sort")
    List<Group> queryGroupByParentId(@Param("parentId") Integer parentId);

    /**
     * 根据父节点查询子节点的资源
     * @param parentId 父节点id
     * @return 子节点资源列表
     */
    @Select("select * from sys_group  where state=0 and is_menu=0 and parent_id=#{parentId} order by sort")
    List<Group> queryMenuByParentId(@Param("parentId") Integer parentId);
    /**
     * 根据名称查找资源
     * @param name
     * @return
     */
    @Select("SELECT * FROM sys_group where name=#{name} and state=0 ")
    List<Group> queryGroupByName(@Param("name") String name);

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
            " sys_role c, sys_role_group d, sys_group e \n" +
            " WHERE a.id = b.user_id AND b.role_id = c.id AND c.id = d.role_id \n" +
            " AND d.group_id = e.id AND a.state > - 2 AND c.state > - 2 " +
            " AND e.state = 0  AND e.is_menu=0 AND a.id = #{userId} AND e.parent_id = #{parentId} order by e.sort ")
    List<Group> queryMenuByUserIdAndParentId(@Param("userId") Integer userId, @Param("parentId") Integer parentId);

    /**
     * 根据用户id查询所有的权限
     * @param userId
     * @return
     */
    @Select(" SELECT distinct e.id id,e.parent_id parentId,e.name name,e.url url,e.state state," +
            " e.creator creator,e.create_time createTime,e.update_user_id updateUserId,e.update_time updateTime," +
            " e.is_menu isMenu,e.icon icon,e.sort sort  " +
            " FROM sys_user a, sys_user_role b,\n" +
            " sys_role c, sys_role_group d, sys_group e \n" +
            " WHERE a.id = b.user_id AND b.role_id = c.id AND c.id = d.role_id \n" +
            " AND d.group_id = e.id AND a.state > - 2 AND c.state > - 2 " +
            " AND e.state = 0 AND a.id = #{userId} order by e.sort ")
    List<Group> queryGroupByUserId(@Param("userId") Integer userId);

    @Select("select group_id groupId from sys_role_group where role_id=#{roleId} ")
    List<Integer> queryGroupIdsByRoleId(@Param("roleId") Integer roleId);

    @Select("select group_id groupId from sys_group_resource where resource_id=#{resourceId} ")
    List<Integer> queryGroupIdsByResourceId(@Param("resourceId") Integer resourceId);

    @Select("<script>" +
            "select parent_id from sys_group where is_menu=0 and id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>")
    List<Integer> queryParentIdsForMenu(@Param("list") Set<Integer> list);

    @Delete("delete from sys_group_resource where group_id=#{groupId}")
    void delGroupResourceByGroupId(@Param("groupId") Integer groupId);

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
    void saveBatchGroupResource(List<Map<String, Object>> list);

    @Delete("delete from sys_role_group where group_id=#{groupId}")
    void delRoleGroupByGroupId(@Param("groupId") Integer groupId);

    @Insert({"<script>",
            " insert into sys_role_group (" +
                    "role_id," +
                    "group_id" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.roleId}," +
                    "#{item.groupId})" +
                    "</foreach>",
            "</script>"})
    void saveBatchGroupRole(List<Map<String, Object>> list);
}
