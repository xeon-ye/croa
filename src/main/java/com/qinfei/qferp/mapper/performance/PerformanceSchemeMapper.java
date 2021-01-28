package com.qinfei.qferp.mapper.performance;

import com.qinfei.qferp.entity.performance.PerformanceProgram;
import com.qinfei.qferp.entity.performance.PerformanceScheme;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 考核方案数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0025 15:07；
 */
public interface PerformanceSchemeMapper {

    @Select({"<script>", "select * from e_performance_scheme\n" +
            "        where state != -1\n" +
            "        <if test=\"companyCode != null\">\n" +
            "            and dept_id in (select id from sys_dept where company_code = #{companyCode} and state = 0)\n" +
            "        </if>\n" +
            "        <if test=\"schemeType != null\">\n" +
            "            and scheme_type=#{schemeType}\n" +
            "        </if>", "</script>"})
    @Results({
            @Result(property = "historyList", column = "id", javaType = List.class,
                    many = @Many(select = "com.qinfei.qferp.mapper.performance.selectBySchId")),
            @Result(property = "id", column = "id")
    })
    List<PerformanceScheme> selectAll(Map map);

    List<PerformanceScheme> listPg(Map map);

    @Update("update e_performance_scheme set state = -1 where sch_id = #{schId}")
    void delSchmeById(@Param("schId") Integer schId);

    List<PerformanceScheme> selectBySchId(Integer schId);
    /**
     * 根据主键删除单条记录；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int deleteByPrimaryKey(PerformanceScheme record);

    /**
     * 插入单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insert(PerformanceScheme record);

    /**
     * 插入单条记录，插入前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insertSelective(PerformanceScheme record);

    /**
     * 更新单条记录，更新前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKeySelective(PerformanceScheme record);

    /**
     * 更新单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKey(PerformanceScheme record);

    /**
     * 根据主键查询单条记录；
     *
     * @param schId：主键ID；
     * @return ：查询结果的封装对象；
     */
    PerformanceScheme selectByPrimaryKey(Integer schId);

    /**
     * 根据条件查询排除人员
     * @param map
     * @return
     */
    List<Map> listUserByParam(Map map);
}