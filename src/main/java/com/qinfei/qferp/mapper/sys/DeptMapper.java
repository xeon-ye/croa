package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.sys.Dept;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface DeptMapper extends BaseMapper<Dept, Integer> {

    @Select("select * from sys_dept where state>-2 order by id ")
    List<Dept> listAll();

    @Select("select * from sys_dept where state >-2 and ((level >=0 and code ='YW')or level<2 ) and find_in_set(id,getChilds(0)) ")
    List<Dept> listDeptAll();

    @Select("select * from sys_dept where state >-2 and ((level >=0 and code ='MJ') or level<2 ) and find_in_set(id,getChilds(0)) ")
    List<Dept> listDeptAllMJ();

    @Select("select * from sys_dept where state>-2 and id=#{id} ")
    Dept getById(@Param("id") Integer id);

    /**
     * 根据部门ID集合获取部门信息
     *
     * @param ids
     * @return
     */
    /*@Select("select * from sys_dept where state>-2 and id in " +
            "<foreach item=\"item\" index=\"index\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\">" +
            "   #{item} " +
            "</foreach>")*/
    @Select({"<script>", "select * from sys_dept where state > -2 and id in " +
            "<foreach item=\"item\" index=\"index\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\">" +
            "   #{item} " +
            "</foreach>", "</script>"})
    List<Dept> listById(@Param("ids") List<Integer> ids);

    /**
     * 获取集团总经办信息
     *
     * @return
     */
    @Select("select * from sys_dept where state > -2 and level = 0")
    Dept getRootDept();

    /**
     * 根据公司编码获取公司信息
     *
     * @param companyCode
     * @return
     */
    @Select("select * from sys_dept where state > -2 and level = 1 and company_code = #{companyCode}")
    List<Dept> getCompanyByCode(@Param("companyCode") String companyCode);

    @Select("select * from sys_dept where state>-2 and parent_id=#{parentId}")
    List<Dept> queryDeptByParentId(@Param("parentId") Integer id);

    @Select("SELECT * FROM sys_dept WHERE FIND_IN_SET(id,getChilds(#{parentId})) order by level asc")
    List<Dept> listByParentId(@Param("parentId") Integer id);

    @Select("SELECT * FROM sys_dept WHERE FIND_IN_SET(id,getChilds(#{parentId})) and state>-2 order by level asc")
    List<Dept> listByParentIdNew(@Param("parentId") Integer id);

    /**
     * 根据部门ID查询下方所有媒介部和业务部
     *
     * @param id
     * @return
     */
    @Select("SELECT * FROM sys_dept WHERE state > -2 and ((level >=2 and (code = 'YW' or code = 'MJ')) or level < 2) and FIND_IN_SET(id,getChilds(#{parentId}))")
    List<Dept> listAllMJYWByDeptId(@Param("parentId") Integer id);

    /**
     * 根据部门ID查询下方指定部门
     *
     * @param id
     * @return
     */
    @Select("SELECT * FROM sys_dept WHERE state > -2 and ((code = #{deptCode}) or (level < 2 and code ='GL')) and FIND_IN_SET(id,getChilds(#{parentId}))")
    List<Dept> listAllDeptByIdAndCode(@Param("parentId") Integer id, @Param("deptCode") String deptCode);

    @Select("SELECT * FROM sys_dept WHERE state > -2 and ((level <= #{level} and code = #{deptCode}) or (level < 2 and code ='GL'))  and FIND_IN_SET(id,getChilds(#{parentId}))")
    List<Dept> listAllDeptByIdAndCodeAndLevel(@Param("parentId") Integer id, @Param("deptCode") String deptCode, @Param("level") String level);

    @Select({"<script>select * from sys_dept where state>-2 and level=#{level} " +
            "<when test='companyCode!=null'>",
            " and company_code=#{companyCode} ",
            "</when></script>"})
    List<Dept> listByLevel(Map map);

    @Select({"<script>select * from sys_dept where state>-2 and (level=#{level} or level=0) " +
            "<when test='companyCode!=null'>",
            " and company_code=#{companyCode} ",
            "</when></script>"})
    List<Dept> allCompany(Map map);

    @Select("select getChilds(#{parentId})")
    String idsByParentId(@Param("parentId") Integer id);

    @Select("SELECT * FROM sys_dept where name=#{name} and state>-2")
    List<Dept> queryDeptByName(@Param("name") String name);

    @Select("SELECT * FROM sys_dept where name=#{name} and state>-2 and company_code=#{companyCode}")
    List<Dept> queryDeptByNameAndCompanyCode(@Param("companyCode") String code, @Param("name") String name);

    @Select({"<script>",
            "select * from sys_dept where state>-2 \n" +
                    "<when test='type!=null and type!=\"\"'>",
            "and type = #{type}",
            "</when>",
            "<when test='companyCode!=null and companyCode!=\"\"'>",
            "and company_code = #{companyCode}",
            "</when>",
            "</script>"
    })
    List<Dept> listPara(Map map);

    @Select("select * from sys_dept where company_code=#{companyCode} and code=#{code} and state>-2 ")
    List<Dept> queryDeptByCompanyCodeAndCode(@Param("companyCode") String companyCode, @Param("code") String code);

    @Select("select * from sys_dept where company_code=#{companyCode} and code=#{code} and name like #{deptName} and state>-2 ")
    List<Dept> queryDeptByCompanyCodeAndCodeAndDeptName(@Param("companyCode") String companyCode,
                                                        @Param("code") String code, @Param("deptName") String deptName);


    /**
     * 根据登录人的公司code获取公司的所有部门
     *
     * @param companyCode
     * @return
     */
    @Select("select * from sys_dept where company_code=#{companyCode} and state>-2 ")
    List<Dept> getDeptByCompany(@Param("companyCode") String companyCode);

    /**
     * 根据登录人的公司code获取公司的所有部门(论坛管理)
     *
     * @param companyCode
     * @return
     */
    @Select({"<script>" +
                 "select * from sys_dept where state>-2 "+
                 "<choose>" +
                    "<when test='companyCode!=\"JT\"'>" +
                    " and company_code=#{companyCode}"+
                    "</when>"+
                    "<otherwise>" +

                    "</otherwise>"+
                "</choose>"+
            "</script>"})
    List<Dept> queryDeptByCompany(@Param("companyCode") String companyCode);

    @Select("select * from sys_dept where state>-2 and company_code = #{v} order by id ")
    List<Dept> listAllByCompanyCode(String companyCode);

    @Select("select * from sys_dept where state>-2 and code = #{code} order by id ")
    List<Dept> listByCode(@Param("code") String code);

    //获取集团下所有公司
    @Select("select sd.id, sd.company_code as code,sd.`name` from sys_dept sd where sd.`code` = 'GL' and sd.`level` = 1 and sd.state <> -9 and sd.company_code <> 'JT'")
    List<Dept> listAllCompany();

    //获取集团所有公司（包含集团）
    List<Dept> listJTAllCompany(@Param("companyCode") String companyCode);

    //获取某一类型的部门字符串，以逗号分隔
    @Select("SELECT GROUP_CONCAT(id) FROM sys_dept where company_code = #{companyCode} AND code = #{code}")
    String getStringByCompanyCodeAndCode(@Param("companyCode") String companyCode, @Param("code") String code);

    //获取某一类型的部门字符串，以逗号分隔
    @Select("SELECT id,code,name,parent_id,level,company_code,mgr_id,mgr_name,mgr_leader_id,mgr_leader_name FROM sys_dept where state>-2 and company_code = #{companyCode} AND code = #{code} AND level = #{level}")
    List<Dept> queryByCompanyCodeAndCodeAndLevel(@Param("companyCode") String companyCode, @Param("code") String code, @Param("level") Integer level);

    //获取某一类型的部门字符串，以逗号分隔
    @Select("SELECT id,code,name,parent_id parentId,level,company_code companyCode FROM sys_dept where state>-2 AND code = #{code} AND level = #{level}")
    List<Map> queryByCodeAndLevel(@Param("code") String code, @Param("level") Integer level);

    //获取某一类型的部门字符串，以逗号分隔
    @Select({"<script>SELECT id,code,name,parent_id parentId,level,company_code companyCode FROM sys_dept " +
            "where state>-2 and company_code = #{companyCode} AND code = #{code} AND level = #{level} AND name like concat('%',#{deptName},'%')" +
            "</script>"})
    List<Map> queryByCompanyCodeAndCodeAndLevelAndName(@Param("companyCode") String companyCode, @Param("code") String code, @Param("level") Integer level, @Param("deptName") String deptName);

    //获取某一类型的部门字符串，以逗号分隔
    @Select({"<script>SELECT id,code,name,parent_id parentId,level,company_code companyCode FROM sys_dept where state>-2 AND code = #{code} AND level = #{level} AND name like concat('%',#{deptName},'%')</script>"})
    List<Map> queryByCodeAndLevelAndName(@Param("code") String code, @Param("level") Integer level, @Param("deptName") String deptName);

    //修改公司名称时修改所有的子部门的公司名称
    @Update("update sys_dept set company_code_name = #{companyCodeName} where company_code = #{companyCode} and state <> -9")
    void editDeptName(@Param("companyCode") String companyCode, @Param("companyCodeName") String companyCodeName);

    //添加公司时
    @Select("SELECT * from sys_dept where `code`='GL' and `level`=1 and state>-2 and company_code = #{companyCode}")
    List<Dept> checkDeptCompanyCode(@Param("companyCode") String companyCode);

    //根据条件查询部门人数，如果是父级部门则会把所有子级部门人员进行统计
    List<Map<String, Object>> listDeptUserNumByParam(Map<String, Object> param);
}