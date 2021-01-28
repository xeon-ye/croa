package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.sys.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface RoleMapper extends BaseMapper<Role, Integer> {



    @Select("select * from sys_role where state>-2 order by name")
    List<Role> listAll();

    @Select("select * from sys_role where state>-2 and type <> 'JT' order by id desc")
    List<Role> listFilialeAll();

    @Select({"<script>",
            " SELECT a.id id,c.name type,d.name code,a.name name,a.create_time as create_time, " +
                    "a.update_time as update_time, a.remark remark,b.id userId,b.user_name userName,b.name uname " +
                    " FROM sys_role a " +
                    " left join sys_dict c on a.type=c.code and c.type_code='ROLE_TYPE' " +
                    " left join sys_dict d on a.code=d.code and d.type_code='ROLE_CODE' " +
                    " left join sys_user b on a.creator=b.id " +
                    " where a.state>-2 " +
                " <when test='type!=null and type!=\"\"'>",
                    " AND c.code like '%${type}%'",
                " </when>",
                " <when test='name!=null and name!=\"\"'>",
                    " AND a.name like '%${name}%'",
                " </when>",
                " order by a.id desc",
            "</script>"})
    @Results({@Result(column = "userId", property = "user.id"), @Result(column = "userName", property = "user.userName"), @Result(column = "uname", property = "user.name")})
    List<Role> listPg(Map map);

    @Delete("delete from sys_user_role where role_id=#{roleId}")
    void delUserRoleByRoleId(@Param("roleId") Integer roleId);

    @Select("SELECT DISTINCT(u.id) from sys_user u LEFT JOIN sys_user_role ur on ur.user_id = u.id where u.state>-2 and ur.role_id = #{id}")
    List<Integer> getUserByRoleId(@Param("id") Integer id);

    @Select("select * from sys_role where state>-2 and id=#{id} ")
    Role getById(@Param("id") Integer id);

    @Select("SELECT sr.* FROM sys_role sr,sys_user su,sys_user_role sur " +
            " where sr.id=sur.role_id and su.id=sur.user_id " +
            " and sr.state>-2 and su.state>-2 and su.id=#{id} ")
    List<Role> queryRoleByUserId(@Param("id") Integer id);

    @Select("SELECT role_id as id, user_id as company_id FROM sys_user_role")
    List<Role> querySelRole();

    @Select("SELECT * FROM sys_role where name=#{name} and state>-2 ")
    List<Role> queryRoleByName(@Param("name") String name);

//    @Insert({"<script>",
//            " insert into sys_role_resource (" +
//                    "role_id," +
//                    "resource_id" +
//                    ") values " +
//                    " <foreach collection='list' item='item' separator=',' >" +
//                    "(#{item.roleId}," +
//                    "#{item.resourceId})" +
//                    "</foreach>",
//            "</script>"})
//    void saveBatch(List<Map<String,Object>> list);

    @Delete("delete from sys_role_group where role_id=#{roleId}")
    void delRoleGroupByRoleId(@Param("roleId") Integer roleId);

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
    void saveBatch(List<Map<String, Object>> list);

    @Select("SELECT a.* FROM sys_role a, sys_role_group b,sys_group c where " +
            " a.id=b.role_id and b.group_id=c.id " +
            " and a.state>-2 and c.state>-2" +
            " and c.id=#{id} ")
    List<Role> queryRoleByGroupId(@Param("id") Integer id);

    @Select("select r.id id,r.`name` name from sys_role r where r.state>-2 and r.type=#{nameQc} ")
    List<Role> queryRoleByRoleType(String nameQc);

    @Select("<script>select id,name,code,remark,type,create_time from sys_role where state = 1 " +
            "<when test=\"keyword!=null and keyword !='null' and keyword !=''\"> AND name like  CONCAT(CONCAT('%',#{keyword}),'%') </when></script>")
    List<Role> nwList(@Param("keyword") String keyword);
}
