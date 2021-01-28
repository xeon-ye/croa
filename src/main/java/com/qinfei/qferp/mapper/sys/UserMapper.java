package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.entity.Dict;
import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<User, Integer> {

    @Select("SELECT a.*,b.id deptId,b.name deptName,b.parent_id parent_id1,b.type type1,b.code code1,b.level level1,b.creator depCreator," +
            "b.create_time create_time1,b.update_time update_time1,b.mgr_id mgr_id1,b.mgr_name mgr_name1," +
            "b.mgr_leader_id mgr_leader_id1,b.mgr_leader_name mgr_leader_name1,b.company_code companyCode,b.company_code_name companyCodeName,session_id FROM " +
            "sys_user a LEFT JOIN sys_dept b ON a.`dept_id`=b.`id` where user_name=#{userName} and a.state>-2 ")
    @Results({@Result(column = "deptId", property = "dept.id"),
            @Result(column = "deptName", property = "dept.name"),
            @Result(column = "parent_id1", property = "dept.parentId"),
            @Result(column = "type1", property = "dept.type"),
            @Result(column = "code1", property = "dept.code"),
            @Result(column = "level1", property = "dept.level"),
            @Result(column = "depCreator", property = "dept.creator"),
            @Result(column = "create_time1", property = "dept.createTime"),
            @Result(column = "update_time1", property = "dept.updateTime"),
            @Result(column = "mgr_id1", property = "dept.mgrId"),
            @Result(column = "mgr_name1", property = "dept.mgrName"),
            @Result(column = "mgr_leader_id1", property = "dept.mgrLeaderId"),
            @Result(column = "mgr_leader_name1", property = "dept.mgrLeaderName"),
            @Result(column = "companyCode", property = "dept.companyCode"),
            @Result(column = "companyCodeName", property = "dept.companyCodeName"),
    })
    User getByUserName(@Param("userName") String userName);

    @Select("select * from sys_user where state > -1 and phone = #{phone} and name = #{name}")
    List<User> getIdByPhoneAndName(@Param("phone") String phone, @Param("name") String name);

    @Select("select * from sys_user where state > -1 and phone = #{phone}")
    List<User> getUserByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM sys_user where id=#{id} and state>-2")
//    @Results({
//            @Result(id = true, property = "id", column = "id"),
//            @Result(property = "user", column = "mgr_id",
//                    one = @One(select = "com.qinfei.qferp.mapper.sys.UserMapper.getById")),
//            @Result(property = "roles", column = "id",
//                    many = @Many(select = "com.qinfei.qferp.mapper.sys.RoleMapper.queryRoleByUserId")),
//            @Result(property = "depts", column = "id",
//                    many = @Many(select = "com.qinfei.qferp.mapper.sys.DeptMapper.queryDeptByUserId"))
//    })
    @Results({@Result(column = "deptId", property = "dept_id"),
            @Result(column = "deptName", property = "dept_name"),
            @Result(column = "postId", property = "post_id"),
            @Result(column = "postName", property = "post_name"),
            @Result(column = "companyCode", property = "company_code"),
    })
    User getById(@Param("id") Integer id);

    @Select({"<script>",
            " SELECT *  FROM sys_user a WHERE 1=1 " +
                    " <when test='noQc!=null and noQc!=\"\"'>",
            " AND a.no like '%${noQc}%'",
            " </when>",
            " <when test='userNameQc!=null and userNameQc!=\"\"'>",
            " AND a.user_name like '%${userNameQc}%'",
            " </when>",
            " <when test='deptIdQc!=null and deptIdQc!=\"\"'>",
            " AND a.dept_id = #{deptIdQc} ",
            " </when>",
            " <when test='deptIds!=null and deptIds!=\"\"'>",
            " AND a.dept_id in (${deptIds}) ",
            " </when>",
            " <when test='stateQc!=null and stateQc!=\"\"'>",
            " AND a.state = #{stateQc} ",
            " </when>",
            " <when test='companyCodeQc!=null and companyCodeQc!=\"\"'>",
            " AND a.company_code = #{companyCodeQc}",
            " </when>",
            " <when test='deptNameQc!=null and deptNameQc!=\"\"'>",
            " AND a.dept_name like '%${deptNameQc}%'",
            " </when>",
            " <when test='postNameQc!=null and postNameQc!=\"\"'>",
            " AND a.post_name like '%${postNameQc}%'",
            " </when>",
            " <when test='nameQc!=null and nameQc!=\"\"'>",
            " AND a.name like '%${nameQc}%'",
            " </when>",
            " <when test='phoneQc!=null and phoneQc!=\"\"'>",
            " AND a.phone like '%${phoneQc}%'",
            " </when>",
            " <when test='roleId!=null and roleId!=\"\"'>",
            " AND EXISTS(SELECT 1 FROM sys_user_role b WHERE a.`id`=b.`user_id` AND b.`role_id`=#{roleId})",
            " </when>",
            " order by a.id desc",
            "</script>"})
    List<User> listPg(Map map);

    @Delete("delete from sys_user_role  where  user_id=#{userId}")
    void delUserRoleByUserId(@Param("userId") Integer userId);

    /**
     * 批量清空用户的角色权限；
     *
     * @param params：查询参数；
     */
    @Delete({"<script>", "delete from sys_user_role  where user_id in <foreach item='userId' collection='params.userIds' index='index' open='(' close=')' separator=','>#{userId}</foreach>",
            "</script>"})
    void delUserRoleByUserIds(@Param("params") Map<String, Object> params);

    /**
     * 批量删除用户；
     *
     * @param params：查询参数；
     */
    @Update({"<script>", "update sys_user set state = -9 where id in <foreach item='userId' collection='params.userIds' index='index' open='(' close=')' separator=','>#{userId}</foreach>",
            "</script>"})
    void delByIds(@Param("params") Map<String, Object> params);

    @Insert({"<script>",
            " insert into sys_user_role (" +
                    "user_id," +
                    "role_id," +
                    "creator" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.userId}," +
                    "#{item.roleId}," +
                    "#{opUserId})" +
                    "</foreach>",
            "</script>"})
    void saveBatch(@Param("list") List<Map<String, Object>> list, @Param("opUserId") Integer opUserId);

    @Insert("insert into sys_user_role (user_id , role_id, creator) values (#{userId}, #{roleId},#{creator})")
    void addBatch(@Param("userId") Integer userId, @Param("roleId") Integer roleId, @Param("creator") Integer creator);

    @Select("select id from sys_role where type='QT' and code='YG' ")
    Integer selectMRJS();

    @Select("SELECT * FROM sys_user where state>-2 order by id desc ")
    List<User> listAll();

    @Update("update sys_user set password=#{password} where state>-2 and id=#{id} ")
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    @Select("SELECT * FROM sys_user where user_name=#{userName} ")
    List<User> queryUserByUserName(@Param("userName") String userName);

    @Select("SELECT u.id userId,\n" +
            "group_concat(r.name,'') roleName,\n" +
            "u.image image,\n" +
            "u.user_name userName,\n" +
            "u.name name,\n" +
            "u.sex sex,\n" +
            "u.wechat wechat,\n" +
            "u.phone phone,\n" +
            "u.email email\n" +
            " FROM sys_user u \n" +
            " left join sys_user_role ur\n" +
            " on u.id = ur.user_id\n" +
            " left join sys_role r on \n" +
            " ur.role_id = r.id\n" +
            " and r.state >-2\n" +
            " where u.state >-2\n" +
            " group by u.id")
    List<Map> queryUserInfo();

    @Select({"<script>",
            "select u.* from sys_user u,sys_dept d where u.dept_id=d.id and u.state>-2 and d.state>-2 " +
                    "<foreach collection=\"list\" item=\"item\" open=\" and u.dept_id in(\" close=\")\" separator=\",\">\n" +
                    "#{item.id}\n" +
                    "</foreach>\n" +
                    "</script>"})
    List<User> queryUserByDepts(@Param("list") List<Dept> depts);

    @Select({"<script>",
            " select id,name,dept_name deptName " +
                    " from sys_user " +
                    " where state>-2 and is_mgr=1 and company_code=#{companyCode} " +
                    " <when test='deptName!=null and deptName!=\"\"'>",
            " AND dept_name like '%${deptName}%'",
            " </when>",
            " <when test='name!=null and name!=\"\"'>",
            " AND name like '%${name}%'",
            " </when>",
            "</script>"})
    List<Map> listMgr(Map map);

    /**
     * 根据角色类型查询用户列表
     *
     * @param type
     * @return
     */
    @Select("SELECT DISTINCT(a.id)," +
            " a.no,a.user_name,a.`password`,a.`name`,a.image,a.sex,a.phone,a.qq,a.wechat,a.email,a.is_mgr,a.mgr_id,a.remark,a.login_ip,a.mac," +
            " a.state,a.creator,a.create_time,a.update_user_id,a.update_time,a.login_time,a.fail_num,a.dept_id,a.dept_name,a.post_id,a.post_name," +
            " a.company_code,a.protected_cust_num,a.save_cust_num,a.propose_sign,a.session_id,a.handover_state "+
            " FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 and c.state>-2 AND c.`type`=#{type} order by a.handover_state, a.id ASC")
    List<User> listByType(@Param("type") String type);

    /**
     * 根据角色类型查询用户列表(未交接)
     *
     * @param type
     * @return
     */
    @Select("SELECT DISTINCT(a.id)," +
            " a.no,a.user_name,a.`password`,a.`name`,a.image,a.sex,a.phone,a.qq,a.wechat,a.email,a.is_mgr,a.mgr_id,a.remark,a.login_ip,a.mac," +
            " a.state,a.creator,a.create_time,a.update_user_id,a.update_time,a.login_time,a.fail_num,a.dept_id,a.dept_name,a.post_id,a.post_name," +
            " a.company_code,a.protected_cust_num,a.save_cust_num,a.propose_sign,a.session_id,a.handover_state "+
            " FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 and c.state>-2 AND c.`type`=#{type} and a.handover_state=0")
    List<User> listByType2(@Param("type") String type);

    /**
     * 根据角色类型查询用户列表
     *
     * @param type
     * @return
     */
    @Select("SELECT DISTINCT(a.id)," +
            " a.no,a.user_name,a.`password`,a.`name`,a.image,a.sex,a.phone,a.qq,a.wechat,a.email,a.is_mgr,a.mgr_id,a.remark,a.login_ip,a.mac," +
            " a.state,a.creator,a.create_time,a.update_user_id,a.update_time,a.login_time,a.fail_num,a.dept_id,a.dept_name,a.post_id,a.post_name," +
            " a.company_code,a.protected_cust_num,a.save_cust_num,a.propose_sign,a.session_id,a.handover_state "+
            " FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 and c.state>-2 AND c.`type`=#{type} and a.company_code=#{companyCode}")
    List<User> listByTypeAndCompanyCode2(@Param("type") String type, @Param("companyCode") String companyCode);

    /**
     * 根据角色类型查询用户列表（未交接）
     *
     * @param type
     * @return
     */
    @Select("SELECT DISTINCT(a.id)," +
            " a.no,a.user_name,a.`password`,a.`name`,a.image,a.sex,a.phone,a.qq,a.wechat,a.email,a.is_mgr,a.mgr_id,a.remark,a.login_ip,a.mac," +
            " a.state,a.creator,a.create_time,a.update_user_id,a.update_time,a.login_time,a.fail_num,a.dept_id,a.dept_name,a.post_id,a.post_name," +
            " a.company_code,a.protected_cust_num,a.save_cust_num,a.propose_sign,a.session_id,a.handover_state "+
            " FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 and c.state>-2 AND c.`type`=#{type} and a.company_code=#{companyCode} and a.handover_state=0")
    List<User> listByTypeAndCompanyCode3(@Param("type") String type, @Param("companyCode") String companyCode);
    /**
     * 根据角色类型查询用户列表
     *
     * @param type
     * @return
     */
    @Select({"<script>SELECT DISTINCT a.* FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 " +
            "and c.state>-2 AND c.`type`=#{type} order by a.handover_state, a.id ASC" +
            "  </script>"})
    List<User> listPart(@Param("type") String type);

    /**
     * 查询财务部长
     * @param type
     * @param companyCode
     * @return
     */
    @Select("  SELECT" +
            " su.name," +
            " su.id " +
            " FROM " +
            " sys_user su " +
            " LEFT JOIN sys_user_role sur ON su.id = sur.user_id " +
            " LEFT JOIN sys_role sr ON sur.role_id = sr.id " +
            " AND sr.state >- 2 " +
            "   WHERE " +
            "  su.state >- 2 " +
            " and su.handover_state=0 " +
            "   AND sr.type = 'CW'" +
            "        AND sr.CODE ='BZ'" +
            "        AND su.company_code = #{companyCode}")
    List<User> secretary(Map map);

    @Select({"<script>SELECT a.* FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 " +
            "and c.state>-2 AND c.`type`=#{type} order by a.handover_state, a.id ASC" +
            "  </script>"})
    List<User> listPartAll(@Param("type") String type, @Param("companyCode") String companyCode);

    /**
     * 查询出未交接的
     * @param type
     * @param companyCode
     * @return
     */
    @Select({"<script>SELECT a.* FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 " +
            "and c.state>-2 and a.handover_state=0 AND c.`type`=#{type} order by a.handover_state, a.id ASC" +
            "  </script>"})
    List<User> listPartAll2(@Param("type") String type, @Param("companyCode") String companyCode);

    /**
     * 查询出未交接的
     * @param type
     * @param companyCode
     * @return
     */
    @Select({"<script>SELECT a.* FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 " +
            "and c.state>-2 and a.handover_state=0 AND a.company_code = #{companyCode} AND c.`type`=#{type} order by a.handover_state, a.id ASC" +
            "  </script>"})
    List<User> listPartAll3(@Param("type") String type, @Param("companyCode") String companyCode);

    /**
     * 查询出未交接的
     * @param type
     * @return
     */
    @Select({"<script>SELECT a.* FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 " +
            " <when test='deptIds != null and deptIds != \"\"'>",
            " AND a.dept_id in (${deptIds}) ",
            " </when>",
            "and c.state>-2 and a.handover_state=0 AND c.`type`=#{type} order by a.handover_state, a.id ASC" +
            "  </script>"})
    List<User> listPartAll4(@Param("type") String type, @Param("deptIds") String deptIds);

    /**
     * 根据角色类型和公司code查询用户列表
     *
     * @param type
     * @return
     */
    @Select({"<script>",
            "SELECT DISTINCT su.id ,su.name, su.dept_id , su.handover_state  \n" +
                    "FROM sys_user su \n" +
                    "left join sys_user_role sur on su.id = sur.user_id \n" +
                    "left join sys_role sr on sur.role_id = sr.id and sr.state > -2 \n" +
                    "left join sys_dept sd on su.dept_id = sd.id and sd.state > -2 \n" +
                    "where su.state >- 2 and sr.type = #{type} and sd.company_code = #{companyCode} \n" +
            " <when test='handoverState != null'> \n",
            " AND su.handover_state = #{handoverState} \n",
            " </when> \n",
            " order by su.handover_state, su.id ASC",
            "</script>"})
    List<User> listByTypeAndCompanyCode(@Param("type") String type, @Param("companyCode") String companyCode, @Param("handoverState") Integer handoverState);

    /**
     * 根据媒体板块查询用户列表（媒介）
     *
     * @param userId
     * @return
     */
    @Select("SELECT * FROM sys_user where id in (\n" +
            "SELECT distinct user_id FROM t_user_media_type where media_type_id in \n" +
            "(SELECT media_type_id FROM t_user_media_type where user_id = #{userId})\n" +
            ")")
    List<User> listByMediaTypeUserId(@Param("userId") Integer userId);

    /**
     * 根据媒体板块查询用户列表（媒介）
     *
     * @param userId
     * @return
     */
    @Select({"<script>SELECT * FROM sys_user where state > -2 and id in (\n" +
            "SELECT distinct user_id FROM t_user_media_type where media_type_id in \n" +
            "(SELECT media_type_id FROM t_user_media_type where user_id = #{userId})" +
            " <when test='deptIds != null and deptIds != \"\"'>",
            " AND dept_id in (${deptIds}) ",
            " </when>",
//            "<choose>",
//            "<when test ='companyCode !=\"XH\"'>",
//            "and company_code=#{companyCode,jdbcType=VARCHAR}",
//            "</when>",
//            "<otherwise>",
//            "</otherwise>",
//            "</choose>",
            ")</script>"})
    List<User> listPastMedia(@Param("userId") Integer userId,@Param("deptIds") String deptIds);

    /**
     * 查找本公司行政人事专员
     */
    @Select({"<script>SELECT " +
            "u.id,u.name,u.dept_id " +
            "FROM " +
            "sys_user u " +
            "INNER JOIN sys_dept d ON u.dept_id = d.id " +
            "WHERE " +
            "u.company_code =#{companyCode} " +
            "and d.name LIKE '%行政%' " +
            "OR d.name LIKE '%人事%' </script>"
    })
    List<User> administrativePersonnel(@Param("companyCode") String companyCode);


    /**
     * 根据角色类型和角色编号查询用户列表
     *
     * @param type
     * @return
     */
    @Select(" SELECT su.* FROM sys_user su \n" +
            "left join sys_user_role sur on su.id=sur.user_id \n" +
            "left join sys_role sr on sur.role_id=sr.id and sr.state >-2 \n" +
            "where su.state>-2 and sr.type=#{type} and sr.code=#{code} and su.company_code=#{companyCode} and su.handover_state=0")
    List<User> listByTypeAndCode(@Param("type") String type, @Param("code") String code, @Param("companyCode") String companyCode);

    @Select(" SELECT su.* FROM sys_user su \n" +
            "left join sys_user_role sur on su.id=sur.user_id \n" +
            "left join sys_role sr on sur.role_id=sr.id and sr.state >-2 \n" +
            "where su.state>-2 and sr.type=#{type} and sr.code=#{code} and su.handover_state=0 order by su.id desc")
    List<User> listByTypeAndCodeJT(@Param("type") String type, @Param("code") String code);

    /**
     * 查询是否是副总经理以上
     */
    @Select(" <script>SELECT a.* FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 and c.state>-2 AND a.id=#{userId} " +
            " <when test='type!=null and type!=\"\"'>" +
            " AND c.type=#{type}" +
            " </when>" +
            "<foreach collection=\"codes\" item=\"code\" index=\"index\" open=\"and c.code in(\" close=\")\" separator=\",\">" +
            "#{code}</foreach>" +
            "order by a.id" +
            " </script>")
    List<User> getUserRoleInfo(@Param("userId") Integer userId, @Param("type") String type, @Param("codes") List<String> codes);

    @Select("SELECT b.* FROM sys_user a,sys_dept b WHERE a.dept_id=b.id and a.state>-2 and b.state>-2 AND a.id=#{userId} order by a.id")
    List<Dept> getMJType(@Param("userId") Integer userId);


    @Select("SELECT a.* FROM sys_user a WHERE a.dept_id=#{deptId} and a.state>-2 order by a.id")
    List<User> getUserByDeptId(@Param("deptId") Integer deptId);

    @Select("SELECT a.* FROM sys_user a WHERE a.dept_id in(101,103) and a.state>-2 order by a.id")
    List<User> getUserByManger();

    @Insert("<script>insert into `t_user_media_type` (`user_id`,`media_type_id`,`state`,`depart_id`) " +
            " values <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">" +
            "(#{item.userId},#{item.mediaTypeId},0,#{item.departId}) </foreach></script>")
    @Options(useGeneratedKeys = true)
    int addUserMediaType(List<Map> map);

    @Delete("delete from t_user_media_type where user_id=#{userId}")
    int delUserMediaTypeByUserId(@Param("userId") Integer userId);

    @Select({"<script>",
            " SELECT su.* FROM sys_role sr,sys_user su,sys_user_role sur" +
                    " where sr.id=sur.role_id and su.id=sur.user_id and sr.state>-2 and su.state>-2 " +
                    " and sr.id=#{roleId} " +
                    " <when test='name!=null and name!=\"\"'>",
            " AND su.name like '%${name}%'",
            " </when>",
            " <when test='userName!=null and userName!=\"\"'>",
            " AND su.user_name like '%${userName}%'",
            " </when>",
            " <when test='deptName!=null and deptName!=\"\"'>",
            " AND su.dept_name like '%${deptName}%'",
            " </when>",
            " order by su.id desc",
            "</script>"})
    List<User> queryByRoleId(Map map);

    /**
     * 根据媒体ID查询媒介人员列表
     *
     * @param mediaId
     * @return
     */
    @Select("select a.* from sys_user a,t_user_media_type b,t_media_audit c,sys_user_role ur,sys_role r \n" +
            " where a.id = b.`user_id` and b.`media_type_id` = c.`plate_id` and a.id = ur.`user_id` \n" +
            "  and r.`id` = ur.`role_id` and r.`type` = 'MJ'and c.id = #{mediaId}")
    List<User> listMJByMediaId(@Param("mediaId") Integer mediaId);

    /**
     * 根据媒体类型ID查询媒介人员列表
     *
     * @param mediaTypeId
     * @return
     */
    @Select("select a.* from sys_user a,t_user_media_type b,sys_user_role ur,sys_role r \n" +
            " where a.id = b.`user_id` and b.`media_type_id` = #{mediaTypeId} and a.id = ur.`user_id` \n" +
            "  and r.`id` = ur.`role_id` and r.`type` = 'MJ'")
    List<User> listMJByMediaTypeId(@Param("mediaTypeId") Integer mediaTypeId);

    /**
     * 根据媒体类型ID查询媒介人员列表
     *
     * @param mediaTypeId
     * @return
     */
    @Select({"<script>select DISTINCT(a.id)," +
            "a.no,a.user_name,a.`password`,a.`name`,a.image,a.sex,a.phone,a.qq,a.wechat,a.email,a.is_mgr,a.mgr_id,a.remark,a.login_ip,a.mac," +
            "a.state,a.creator,a.create_time,a.update_user_id,a.update_time,a.login_time,a.fail_num,a.dept_id,a.dept_name,a.post_id,a.post_name," +
            "a.company_code,a.protected_cust_num,a.save_cust_num,a.propose_sign,a.session_id,a.handover_state "+
            " from sys_user a,t_user_media_type b,sys_user_role ur,sys_role r \n" +
            " where a.id = b.`user_id` and b.`media_type_id` = #{mediaTypeId} and a.id = ur.`user_id` \n" +
            " and r.`id` = ur.`role_id` and r.`type` = 'MJ' and a.state>-2" +
            " <choose>",
            "   <when test='companyCode != \"XH\"'>",
            "     AND a.company_code = #{companyCode, jdbcType = VARCHAR}",
            "    </when>",
            "    <otherwise>",
            "      </otherwise>",
            "   </choose>",
            "</script>"})
    List<User> listPastMJByMediaTypeId(@Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode);

    /**
     * 根据媒体类型ID查询媒介人员列表（未交接）
     *
     * @param mediaTypeId
     * @return
     */
    @Select({"<script>select DISTINCT(a.id)," +
            " a.no,a.user_name,a.`password`,a.`name`,a.image,a.sex,a.phone,a.qq,a.wechat,a.email,a.is_mgr,a.mgr_id,a.remark,a.login_ip,a.mac," +
            " a.state,a.creator,a.create_time,a.update_user_id,a.update_time,a.login_time,a.fail_num,a.dept_id,a.dept_name,a.post_id,a.post_name," +
            " a.company_code,a.protected_cust_num,a.save_cust_num,a.propose_sign,a.session_id,a.handover_state "+
            " from sys_user a,t_user_media_type b,sys_user_role ur,sys_role r \n" +
            " where a.id = b.`user_id` and b.`media_type_id` = #{mediaTypeId} and a.id = ur.`user_id` \n" +
            " and r.`id` = ur.`role_id` and r.`type` = 'MJ' and a.state>-2 and a.handover_state=0 " +
            " <choose>",
            "   <when test='companyCode != \"XH\"'>",
            "     AND a.company_code = #{companyCode, jdbcType = VARCHAR}",
            "    </when>",
            "    <otherwise>",
            "      </otherwise>",
            "   </choose>",
            "</script>"})
    List<User> listPastMJByMediaTypeId2(@Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode);
    /**
     * 根据媒体板块查询媒介信息
     */
    List<User> listMJByPlateId(@Param("plateId") Integer plateId, @Param("companyCode") String companyCode);

    /**
     * 根据媒体板块查询媒介信息
     */
    List<User> listMJByPlateId2(@Param("plateId") Integer plateId, @Param("companyCode") String companyCode);
    /**
     * 查询系统用户信息；
     *
     * @param map：查询条件；
     * @return ：查询结果集合；
     */
    List<User> listUserInformation(Map<String, Object> map);

    /**
     * 根据用户id和角色类型查看该用户是否有该角色
     *
     * @param userId
     * @param roleType
     * @return
     */
    @Select({"<script>",
            " SELECT sr.* FROM sys_role sr,sys_user su,sys_user_role sur" +
                    " where sr.id=sur.role_id and su.id=sur.user_id and sr.state>-2 and su.state>-2 " +
                    " and su.id=#{userId} and sr.type=#{roleType} " +
                    " order by su.id desc",
            "</script>"})
    List<Role> queryRoleByUserIdAndRoleType(@Param("userId") Integer userId, @Param("roleType") String roleType);

    /**
     * 查询该部门下的人员，不包含子部门
     *
     * @param deptId
     * @return
     */
    @Select("select * from sys_user where state>-2 and dept_id =#{deptId}")
    List<User> queryUserByDeptIdONLY(@Param("deptId") Integer deptId);

    /**
     * 查询指定部门的负责人信息；
     *
     * @param deptId：部门ID；
     * @return ：部门的负责人信息；
     */
    @Select({"<script>",
            "select mgr_id as id, mgr_name as name, id as dept_id from sys_dept where state > -2 and id = #{deptId} and company_code=#{code}",
            "</script>"})
    List<User> listLeaderByDeptId(@Param("deptId") Integer deptId, @Param("code") String code);


    /**
     * 查询指定部门的分管领导信息
     * @param deptId
     * @param code
     * @return
     */
    @Select({"<script>",
            "select mgr_leader_id as id,mgr_leader_name as name, id as dept_id  from sys_dept  where  state > -2 and id = #{deptId} and company_code=#{code}",
            "</script>"})
    List<User> listMgrLeaderByDeptId(@Param("deptId") Integer deptId, @Param("code") String code);

    /**
     * 修改客户保护数量
     *
     * @param map
     */
    @Update({"<script>",
            " update sys_user " +
            "<trim prefix=\"set\" suffixOverrides=\",\">" +
                "<if test='saveCustNum!=null'>",
                "save_cust_num=#{saveCustNum},",
                "</if>",
                "<if test='protectedCustNum!=null'>",
                "protected_cust_num=#{protectedCustNum},",
                "</if>",
            "</trim>",
            "where id=#{userId}",
            "</script>"})
    void updateUserCust(Map map);



    @Select("select user_id from e_employee where emp_id =#{empId}")
    Integer selectUserId(@Param("empId") Integer empId);

    /**
     * 根据公司查询所有用户
     *
     * @param companyCode
     * @return
     */
    @Select("select user.id,user.name,user.dept_id,user.dept_name from sys_user user left join sys_dept dept on user.dept_id=dept.id where dept.company_code=#{companyCode} and user.state>-2 and user.handover_state=0")
    List<User> queryByCompanyCode(@Param("companyCode") String companyCode);

    //根据条件查询用户
    List<User> queryUserByCondition(Map map);

    /**
     * 查询未录入建议的用户
     *
     * @param map
     * @return
     */
    List<Map> queryDeptByCompany(Map map);

    /**
     * 判断用户是否为部门的最高领导；
     *
     * @param userId：用户ID；
     * @param deptId：部门ID；
     * @return ：查询结果；
     */
    @Select({"<script>",
            "select count(*) from sys_dept where state = 0 and mgr_id=#{userId} and id = #{deptId}",
            "</script>"})
    int isDeptLeader(@Param("userId") Integer userId, @Param("deptId") Integer deptId);

    List<User> listExclude(@Param("postId") Integer postId, @Param("excludeIds") int[] excludeIds);

    /**
     * 根据查询条件获取相关的用户信息；
     *
     * @param params：查询条件集合；
     * @return ：查询结果集合；
     */
    @Select({"<script>",
            "select id, name,phone, dept_id from sys_user where state = 1",
            "<when test='userIds != null and userIds != \"\"'>",
            " and id in(#{userIds})",
            " </when> ",
            "<when test='name != null and name != \"\"'>",
            " and name=#{name}",
            " </when> ",
            "</script>"})
    List<User> listByParams(Map<String, Object> params);

    //获取admin用户
    @Select({"<script>",
            "select * from sys_user where user_name ='admin' ",
            "</script>"})
    User getAdmin();


    /**
     * 获取某个部门下的所有员工ID
     *
     * @param deptId
     * @return
     */
    @Select("SELECT getChilds (#{deptId})")
    String getChilds(@Param("deptId") Integer deptId);

    /**
     * 根据用户ID列表查询用户列表信息
     *
     * @return
     */
    @Select("select * from sys_user where dept_id in (${deptIds}) ")
    List<User> listByUserIds(@Param("deptIds") String deptIds);

    /**
     * 根据用户ID列表查询用户列表信息
     *
     * @return
     */
    @Select("select * from sys_user u,sys_role r,sys_user_role ur where u.state > -2 and dept_id in (${deptIds}) and r.id=ur.role_id and  ur.user_id=u.id and r.`type`=#{roleType}")
    List<User> listByUserIdsAndRoleType(@Param("deptIds") String deptIds, @Param("roleType") String roleType);

    /**
     * 根据用户ID列表查询用户列表信息
     *
     * @return
     */
    @Select({"<script>", "select DISTINCT u.* from sys_user u,sys_role r,sys_user_role ur where u.state > -2 and u.dept_id in (${deptIds}) " +
            "<when test='companyCode != null and companyCode!=\"\" and companyCode != \"JT\"'>",
            "and u.company_code = #{companyCode}",
            "</when>" +
                    "and r.id=ur.role_id and  ur.user_id=u.id and r.`type`=#{roleType}", "</script>"})
    List<User> listUserByDeptAndRole(@Param("deptIds") String deptIds, @Param("companyCode") String companyCode, @Param("roleType") String roleType);

    /**
     * 根据用户ID列表查询用户列表信息(未移交)
     *
     * @return
     */
    @Select({"<script>", "select DISTINCT u.* from sys_user u,sys_role r,sys_user_role ur where u.state > -2 and u.handover_state=0 and u.dept_id in (${deptIds}) " +
            "<when test='companyCode != null and companyCode!=\"\" and companyCode != \"JT\"'>",
            "and u.company_code = #{companyCode}",
            "</when>" +
                    "and r.id=ur.role_id and  ur.user_id=u.id and r.`type`=#{roleType}", "</script>"})
    List<User> listUserByDeptAndRole2(@Param("deptIds") String deptIds, @Param("companyCode") String companyCode, @Param("roleType") String roleType);

    /**
     * 根据公司编码和用户角色类型查询用户列表
     * 查询某个公司下制定角色类型的用户列表
     *
     * @param companyCode
     * @param roleType
     * @return
     */
    @Select("select DISTINCT u.* from sys_user u,sys_role r,sys_user_role ur where u.state > -2 and u.company_code=#{companyCode} and r.id=ur.role_id and ur.user_id=u.id and r.`type`=#{roleType}")
    List<User> listByCompanyCodeAndRoleType(@Param("companyCode") String companyCode, @Param("roleType") String roleType);

    @Select("select id,user_name from sys_user where no = #{v}")
    User findUserByEmpNum(String no);

    /**
     * 获取部门code为管理的所有人员
     *
     * @param companyCode
     * @return
     */
    @Select("select u.* from sys_user u LEFT JOIN sys_dept d on u.dept_id = d.id where d.code = 'GL' and d.company_code = #{companyCode} and u.state>-2 and d.state>-2 and u.id>1")
    List<User> selectByDeptCode(@Param("companyCode") String companyCode);

    @Select("select count(1) from sys_user where state > -2 and name = #{v}")
    long countAllByName(String name);

    @Select("select code,name from sys_dict where type_code=#{type}")
    List<Dict> roleType(@Param("type") String type);

    @Select("select r.`name` name from sys_dict d LEFT JOIN sys_role r on d.code=r.type where d.type_code='ROLE_TYPE' and r.type=#{nameQc} and r.state>-2 ")
    List<Role> characterName(String nameQc);

    /**
     * 根据角色类型查询用户列表
     *
     * @return
     */
    @Select({"<script>SELECT a.* FROM sys_user a,sys_user_role b,sys_role c WHERE a.id=b.`user_id` AND b.`role_id`=c.`id` and a.state>-2 " +
            "and c.state>-2 AND c.`type`='YW' AND c.code in ('YG','ZZ','BZ','ZL')" +
            " <choose>",
            "   <when test='companyCode != \"XH\"'>",
            "       AND a.company_code = #{companyCode, jdbcType = VARCHAR}",
            "     </when>",
            "   <when test='name != null and name != \"\"'>",
            "       AND a.name like #{name, jdbcType = VARCHAR}",
            "     </when>",
            "       <otherwise>",
            "       </otherwise>",
            "       </choose>",
            "  </script>"})
    List<User> listBusinessPart(@Param("name") String name, @Param("companyCode") String companyCode);

    @Select("select image from sys_user where state = 1 and id = #{v}")
    String findAvatarById(int id);

    @Select({"<script>" +
            "select id, name from sys_user where state>-2" +
            "<when test='deptIds!=null and deptIds!=\"\"'>" +
            "and dept_id in (${deptIds})" +
            "</when> " +
            "</script>"
    })
    List<User> listDeptUser(Map map);

    //通过姓名和部门获取用户信息
    @Select({"<script>" +
            "select id, name from sys_user where state>-2" +
            "<when test='deptIds!=null and deptIds!=\"\"'>" +
            "and dept_id in (${deptIds})" +
            "</when> " +
            "<when test='userName!=null and userName!=\"\"'>" +
            "and name = '${userName}'" +
            "</when> " +
            "</script>"
    })
    List<User> listGetByNameAndDept(Map map);
    /**
     * 获取boss的id
     */
    @Select("select id from sys_user where propose_sign=1 and state>-2 and handover_state=0")
    List<Integer> listForBossId(Integer id);

    @Select("select id,name from sys_user where state>-2 and handover_state=0 and company_code=#{companyCode} ")
    List<User> listUser(String companyCode);
    //修改公司名称时，修改在该部门的人员的部门名称
    @Update("update sys_user SET dept_name = #{deptName} where dept_id = #{deptId} and state <> -9")
    void editUserDeptName(@Param("deptName") String deptName, @Param("deptId") Integer deptId);
    //用户名去重
//    @Select({"<script>" +
//            "select * from sys_user " +
//            "where user_name =#{name} " +
//            "<when test=\"id!=null\">" +
//            " and id != #{id}" +
//            "</when>" +
//            "</script>"})
//    List<User> checkDuplicateUserName(Map map);

        @Select({"<script>" +
            "select COUNT(*) sum, COUNT(case when state >-9 then 1 end) state ,COUNT(case when state =-9 then 1 end) delState  from sys_user " +
            "where user_name =#{name} " +
            "<when test=\"id!=null\">" +
            " and id != #{id}" +
            "</when>" +
//            " LIMIT 1" +
            "</script>"})
        Map checkDuplicateUserName(@Param("id")Integer id, @Param("name")String name);

    @Select({"<script>" +
            " select COUNT(*) sum, COUNT(case when state >-9 then 1 end) state ,COUNT(case when state =-9 then 1 end) delState  " +
            " from sys_user where name=#{name} " +
            "<when test=\"id!=null\">" +
            " and id != #{id}" +
            "</when>" +
//            " LIMIT 1" +
            "</script>"})
   Map checkDuplicateName(@Param("id")Integer id, @Param("name")String name);

    //根据部门ID列表获取用户信息
    List<Integer> listUserByDeptIds(@Param("deptIds") List<Integer> deptIds);

    /**
     * 根据角色类型查询用户列表（未交接）
     * @return
     */
    List<Map> listUserByTypeAndCompanyCode(Map map);
    /**
     * 根据公司类型查询用户列表（未交接）
     * @return
     */
    List<Map> listUserByCompanyCode(Map map);

    List<Map<String, Object>> listUserByParam(@Param("type")String type, @Param("code")String code, @Param("companyCode")String companyCode, @Param("handoverState")Integer handoverState);

    List<User> listDeptMgrByCompanyCode(@Param("companyCode") String companyCode);

    List<Dept> listJTCWhead(@Param("companyCode") String companyCode , @Param("code")String code);

    /**
     * 查询需要被强制提醒填写建议的人员
     * @param map
     * @return
     */
    List<Integer> querySuggestHintData(Map map);

    /**
     * 查询部长的上级审核人员
     * @param map
     * @return
     */
    List<User> queryUserByRoleType(Map map);

    /**
     * 手动关联用户修改用户职位
     * @param map
     */
    void updateUserPost(Map map);

    @Select("SELECT\n" +

            "u.user_name userName,\n" +
            "u.`name`,\n" +
            "u.dept_name deptName,\n" +
            "d.company_code_name companyCodeName,\n" +
//            "r.`name` roleName,\n" +
            "u.phone,\n" +
            "u.image,\n" +
            "u.sex,\n" +
//            "r.`name` roleName,\n" +
//            "r.`code` roleCode,\n" +
//            "r.type roleType,\n" +
//            "r.id roleId,\n" +
            "u.id userId\n" +
            "FROM\n" +
            "sys_user u\n" +
//            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id\n" +
//            "LEFT JOIN sys_role r ON r.id = ur.role_id\n" +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id\n" +
            "WHERE\n" +
            "u.id = #{id}\n" +
            "AND u.state >- 2 LIMIT 1\n")
    Map getUserByUserId(Integer id);

    @Select("<script>SELECT u.*,d.company_code_name companyCodeName FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id = d.id  " +
            "where u.state>-2" +
            "<when test=\"keyword!=null and keyword !='null' and keyword !=''\"> AND u.name like  CONCAT('%', #{keyword}, '%') </when></script>")
    List<Map> intranetlistPg(@Param("keyword")String keyword);

    List<Map> intranetUserListPg(@Param("keyword")String keyword);
}
