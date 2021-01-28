package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @author gzw
 * create by Administrator on 2019/7/25
 */
public interface PostMapper extends BaseMapper<Post, Integer> {
    /**
     * 查询所有公司的职位
     *
     * @param map
     * @return
     */
    @Select({"<script>",
            "select * from sys_post where company_code=#{companyCode} and state>-2" +
                    "<when test='postNameQC!=null and postNameQC!=\"\"'>",
            "and name like '%${postNameQC}%'",
            "</when>",
            "</script>"})
    List<Map> getCompanyPost(Map map);

    /**
     * 根据职位id查询职位名称
     * @param postId
     * @return
     */
    @Select({"<script>",
            "select * from sys_post where state>-2 and id=#{postId}" +
            "</script>"})
    Post queryPostById(Integer postId);


    /**
     * 判断职位是否重复
     * @param name
     * @param companyCode
     * @param id
     * @return
     */
    @Select({"<script>",
            "select * from sys_post where name = #{name} and company_code =#{companyCode} and state>-2" +
            "<when test='id!=null'>",
            "and id!=#{id}",
            "</when>",
            "</script>"})
    Post getPostInfo(@Param("name") String name, @Param("companyCode") String companyCode, @Param("id") Integer id);

    /**
     * 判断是否可以删除职位
     *
     * @param CompanyCode
     * @param id
     * @return
     */
    @Select("select * from sys_post where id not in (select DISTINCT(post_id) from sys_dept_post) and company_code=#{companyCode} and state>-2 and id=#{id}")
    Post queryByDeletePost(@Param("companyCode") String CompanyCode, @Param("id") Integer id);

    /**
     * 删除部门职位
     *
     * @param id
     */
    @Update("update sys_post set state=-9 where id = #{id}")
    void delDeptPost(@Param("id") Integer id);

    /**
     * 批量插入部门职位表
     *
     * @param list
     */
    @Insert({"<script>" +
            "insert into sys_dept_post(dept_id,post_id) values" +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.deptId}," +
            "#{item.postId})" +
            "</foreach>" +
            "</script>"})
    void insertDeptPost(List<Map> list);

    /**
     * 单条插入部门职位表
     * @param deptId
     * @param postId
     */
    @Insert({"<script>"+
            "insert into sys_dept_post(dept_id,post_id) values(#{deptId},#{postId})"+
            "</script>"})
    void saveDeptPost(@Param("deptId") Integer deptId,@Param("postId") Integer postId);

    /**
     * 部门职位关系
     *
     * @param deptId
     */
    @Delete("delete from sys_dept_post where dept_id = #{deptId}")
    void deletePost(Integer deptId);

    /**
     * 绑定职位时显示
     *
     * @param companyCode
     * @param deptId
     * @return
     */
    @Select("SELECT id, name from sys_post where id in(select post_id from sys_dept_post where dept_id =#{deptId} ) and company_code =#{companyCode} and state>-2")
    List<Map> queryDeptPost(@Param("companyCode") String companyCode, @Param("deptId") Integer deptId);

    /**
     * 查找职位id
     *
     * @param companyCode
     * @param postName
     * @return
     */
    @Select("select post_id from sys_dept_post where dept_id = #{deptId} and post_id = (SELECT id from sys_post where company_code = #{companyCode} and name = #{postName} and state>-2)")
    List<Integer> findByCompanyCodeAndPostNameAndDeptId(@Param("companyCode") String companyCode,
                                                        @Param("postName") String postName, @Param("deptId") Integer deptId);
}
