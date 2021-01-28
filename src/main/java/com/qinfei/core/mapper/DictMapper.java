package com.qinfei.core.mapper;

import com.qinfei.core.entity.Dict;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.sys.Dept;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 查询字典数据
 */
public interface DictMapper extends BaseMapper<Dict, Integer> {

    /**
     *
     *
     * @return
     */
    @Select({"<script>" ,
            "SELECT" +
            " dict.*, (" +
            " SELECT" +
            " GROUP_CONCAT(dept. NAME)" +
            " FROM" +
            " relationship r" +
            " LEFT JOIN sys_dept dept ON r.dept_id = dept.id" +
            " where r.dict_id = dict.id" +
                    " and r.state > -2" +
            " ) deptName ," +
              "(SELECT GROUP_CONCAT(su.name) from t_assistant_dict tad left join sys_user su on tad.assistant_user_id = su.id where  tad.dict_id = dict.id and tad.state>-2 ) assistantName" +
            " FROM" +
            " sys_dict dict" +
            " where dict.type_code = 'tax'" +
            " AND dict.state > -2" +
            " <when test='companyCode!=null and companyCode!=\"\" and companyCode!=\"JT\"'>",
            " and dict.company_code = #{companyCode}",
            " </when>",
            " <when test='desc!=null and desc!=\"\"'>",
            " AND dict.desc like '%${desc}%'",
            " </when>",
            " <when test='createUser!=null and createUser!=\"\"'>",
            " AND dict.create_user like '%${createUser}%'",
            " </when>",
            " order by dict.id desc",
            "</script>"
    })
    List<Dict> selectDict(Map map);

    @Select("select * from sys_dict where type_code=#{typeCode} and disabled=0 and state>-2 order by sort_no")
    List<Dict> listByTypeCode(@Param("typeCode") String typeCode);

    @Select("select * from sys_dict where type_code=#{typeCode} and company_code=#{companyCode} and disabled=0 and state>-2 order by sort_no")
    List<Dict> listByTypeCodeAndCompanyCode(@Param("typeCode") String typeCode,@Param("companyCode") String companyCode);

    @Select("select s.*,r.dept_id from sys_dict s  LEFT JOIN relationship  r on s.id=r.dict_id " +
            "where s.type_code=#{typeCode} and s.disabled=0 and r.dept_id=#{userDeptId} and s.state>-2 and r.state>-2 order by s.sort_no")
    List<Dict> listDict(Map map);


    @Select("select * from sys_dict where type_code=#{typeCode} and code=#{code}  and disabled=0 and state>-2 order by sort_no")
    Dict getByTypeCodeAndCode(@Param("typeCode") String typeCode, @Param("code") String code);

    @Select("select * from sys_dict where type_code=#{typeCode} and name=#{name}  and disabled=0 and state>-2 and  company_code=#{companyCode} order by sort_no")
    Dict getByTypeCodeAndName(@Param("typeCode") String typeCode, @Param("name") String name,@Param("companyCode") String companyCode);

    @Select({"<script>" ," select count(*) from sys_dict where type_code=#{typeCode} and name=#{name}  and disabled=0 and state>-2 and  company_code=#{companyCode}" +
            "<when test='id != null' >" ,
            "and id &lt;&gt; #{id}" ,
            "</when>" ,
            "</script>"})
    int getDictCount(Dict dict);

    @Select("select s.*,r.dept_id from sys_dict s  LEFT JOIN relationship  r on s.id=r.dict_id " +
            "where s.type_code=#{typeCode} and s.disabled=0 and r.dept_id=#{userDeptId} and s.name=#{name} and s.state>-2 and r.state>-2 order by s.sort_no")
    Dict getDictName(@Param("typeCode") String typeCode, @Param("name") String name,@Param("userDeptId") Integer userDeptId);

    @Insert({"<script>" +
            "insert into relationship(" +
            "dept_id," +
            "dict_id" +
            ") values " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.deptId}," +
            "#{item.dictId}" +
            ")" +
            "</foreach> </script>"})
    void    insertDeptId(List<Map> list);

    @InsertProvider(type = ProviderUtil.class, method = "insert")
    @Options(useGeneratedKeys = true)
    void insertReturnId(Dict dict);

    @Select("select * from sys_dict where state>-2 and id = #{id}")
    Dict getById(Integer id);

    @Select("select s.* from sys_dept s, relationship r where s.id=r.dept_id and s.state>-2 and r.state>-2 and r.dict_id=#{id}")
    List<Dept> queryDeptId(Integer id);

    @Insert(" <script>delete from relationship where dict_id=#{dictId} " +
            " and dept_id  in " +
            " <foreach item=\"item\" index=\"index\" collection=\"list\"\n" +
            "   open=\"(\" separator=\",\" close=\")\">\n" +
            "   #{item.id}\n" +
            " </foreach>" +
            " </script>")
    void delDeptAccountDept(Map map);

    @Update( "update relationship set state=-9 where dict_id=#{dictId}")
    void editDeptId (Integer dictId);

    @Insert({"<script>",
            " insert into relationship (" +
                    "dept_id," +
                    "dict_id" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.deptId}," +
                    "#{item.id})" +
                    "</foreach>",
            "</script>"})
    void insertDept(List<Map> list);

    /**
     * 建议类型管理
     * @param nameQc
     * @param id
     * @param companyCode
     * @return
     */
    @Select({"<script>" ,
            "SELECT a.*, GROUP_CONCAT(c.name) deptName FROM sys_dict a " +
                    "LEFT JOIN t_propose_relation b ON a.id = b.id " +
                    "LEFT JOIN sys_user c on b.user_id = c.id " +
                    "WHERE a.type_code = 'PROPOSE_TYPE' " +
                    "AND a.state > - 2 and c.state>-2 and c.handover_state=0 and a.company_code = #{companyCode}" +
            " <when test='nameQc!=null and nameQc!=\"\"'>",
            " AND a.name = #{nameQc} ",
            " </when>",
            " <when test='id!=null and id!=\"\"'>",
            " AND a.id != #{id} ",
            " </when>",
            " GROUP BY a.id ",
            " order by a.id asc ",
            "</script>"
    })
    List<Dict> queryProposeDict(@Param("nameQc")String nameQc,@Param("id")Integer id,@Param("companyCode")String companyCode);

    /**
     * 建议类型删除
     * @param id
     */
    @Update("update sys_dict set state=-9 where id = #{id} ")
    void delSuggest(@Param("id")Integer id);

    /**
     * 删除部门公司进行字典表数据删除
     * @param typeCode
     * @param code
     */
    @Update("update sys_dict set state=-9 where type_code = #{typeCode} and code=#{code}")
    void delCompanyDict(@Param("typeCode")String typeCode,@Param("code")String code);

    @Select("SELECT DISTINCT company_code_name from sys_dept where  company_code=#{code}")
    String selectCodeName(String code);
}
