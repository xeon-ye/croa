package com.qinfei.qferp.mapper.employ;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.employ.EmployeePerformancePk;
import com.qinfei.qferp.service.dto.PerformancePKProfitDto;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * Created by yanhonghao on 2019/4/23 17:30.
 */
public interface EmployeePerformancePkMapper extends BaseMapper<EmployeePerformancePk, Integer> {

    @Select("select * from e_employee_performance_pk where state <> -1 and id=#{v}")
    @Results({
            @Result(property = "relates", column = "id", javaType = List.class,
                    many = @Many(select = "com.qinfei.qferp.mapper.employ.EmployeePerformancePkEmployeeRelateMapper.selectByPKId")),
            @Result(property = "id", column = "id")
    })
    EmployeePerformancePk findById(int id);

    @Insert("insert into e_employee_performance_pk(start_date," +
            "end_date," +
            "name," +
            "background_img," +
            "create_id," +
            "create_name," +
            "create_time," +
            "update_id," +
            "update_name," +
            "update_time," +
            "state," +
            "version) values(#{startDate},#{endDate},#{name}" +
            ",#{backgroundImg},#{createId},#{createName},#{createTime}" +
            ",#{updateId},#{updateName},#{updateTime}" +
            ",#{state},#{version})")
    @Options(useGeneratedKeys = true)
    int insertEmployeePerformancePk(EmployeePerformancePk performancePk);

    /**
     * pk设置列表
     *
     * @return EmployeePerformancePk列表
     */
    @Select({"<script>", "select a.* from e_employee_performance_pk a, sys_user b where a.create_id = b.id and a.state != -1  ",
            "<if test='currentDate != null'>and a.start_date between #{currentDate} and DATE_ADD(#{currentDate}, INTERVAL 1 year)</if>",
            "<if test=\"companyCode != null and companyCode != ''\">and b.company_code = #{companyCode,jdbcType=VARCHAR}</if> order by a.start_date desc", "</script>"})
    @Results({
            @Result(property = "relates", column = "id", javaType = List.class,
                    many = @Many(select = "com.qinfei.qferp.mapper.employ.EmployeePerformancePkEmployeeRelateMapper.selectByPKId")),
            @Result(property = "id", column = "id")
    })
    List<EmployeePerformancePk> listAll(Map<String, Object> map);

    /**
     * pk设置列表id start_date name
     *
     * @return PerformancePKProfitDto列表
     */
    @Select({"<script>", "select a.id,a.pk_type,a.start_date,a.end_date,a.name,a.background_img from e_employee_performance_pk a,sys_user b where a.create_id = b.id and a.state != -1 ",
            " <if test='currentDate != null'>and a.start_date between #{currentDate,jdbcType=DATE} and DATE_ADD(#{currentDate,jdbcType=DATE}, INTERVAL 1 year)</if>",
            " <if test='currentDateTime != null'>and #{currentDateTime} between a.start_date and a.end_date </if>",
            " <if test='companyCode != null'>and b.company_code = #{companyCode,jdbcType=VARCHAR}</if>",
            " order by a.start_date desc", "</script>"})
    List<PerformancePKProfitDto> listAllWithoutRelate(Map<String, Object> map);

    @Update("update e_employee_performance_pk set state = -1 where id = #{v}")
    void deleteById(int id);

    @Select("select c.createYear from (select DISTINCT year(a.start_date) as createYear from e_employee_performance_pk a," +
            "sys_user b where a.create_id = b.id and a.state <> -1 and b.company_code = #{v}) c order by c.createYear desc")
    List<EmployeePerformancePk> getYears(String companyCode);

    @Select({"<script>",
            "      select a.id,a.start_date,a.end_date from e_employee_performance_pk a,sys_user b where a.create_id = b.id and b.company_code = #{companyCode} and a.state != -1 and a.start_date between (#{startDate}) and (#{endDate}) ",
            "<if test='id != null'> and a.id != #{id} </if> ",
            "<if test='pkType != null'> and a.pk_type = #{pkType} </if> ",
            "union select a.id,a.start_date,a.end_date from e_employee_performance_pk a,sys_user b where a.create_id = b.id and b.company_code = #{companyCode} and a.state != -1 and a.end_date between (#{startDate}) and (#{endDate}) ",
            "<if test='id != null'> and a.id != #{id} </if> ",
            "<if test='pkType != null'> and a.pk_type = #{pkType} </if> ",
            "union select a.id,a.start_date,a.end_date from e_employee_performance_pk a,sys_user b where a.create_id = b.id and b.company_code = #{companyCode} and a.state != -1 and a.start_date &lt; (#{startDate}) and a.end_date &gt; (#{endDate}) ",
            "<if test='id != null'> and a.id != #{id} </if> ",
            "<if test='pkType != null'> and a.pk_type = #{pkType} </if> ",
            "</script>"})
    List<EmployeePerformancePk> countDateConflict(EmployeePerformancePk pk);
}
