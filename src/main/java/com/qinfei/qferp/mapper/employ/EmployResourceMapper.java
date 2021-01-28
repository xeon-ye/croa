package com.qinfei.qferp.mapper.employ;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 常用的信息库数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/03/05 0025 15:07；
 */
public interface EmployResourceMapper {
    /**
     * 查询民族信息；
     *
     * @return ：民族信息集合；
     */
    List<Map<String, Object>> listNation();

    /**
     * 根据ID获取指定民族的名称；
     *
     * @param nationId：民族ID；
     * @return ：民族的名称；
     */
    String getNationNameById(Integer nationId);

    @Select("select id from sys_nation where name = #{v}")
    Integer getIdByNationName(String name);

    /**
     * 查询指定区域的地区信息；
     *
     * @param params：参数集合，areaId，地区的区域ID；
     * @return ：地区信息集合；
     */
    List<Map<String, Object>> listDistrict(Map<String, Object> params);

    /**
     * 查询部门信息；
     *
     * @param params：参数集合，companyCode，部门代码；
     * @return ：部门信息集合；
     */
    List<Map<String, Object>> listDept(Map<String, Object> params);

    /**
     * 根据ID获取指定部门的名称；
     *
     * @param deptId：部门ID；
     * @return ：部门的名称；
     */
    String getDeptNameById(Integer deptId);

    /**
     * 查询指定部门的职位信息；
     *
     * @param params：参数集合，deptId，部门ID；
     * @return ：职位信息集合；
     */
    List<Map<String, Object>> listPost(Map<String, Object> params);

    List<Map<String, Object>> listPostByCompanyCode(String companyCode);

    //根据公司代码和一级部门获取职位信息
    List<Map<String, Object>> listPostByCompanyAndDept(@Param("companyCode") String companyCode, @Param("firstDept") Integer firstDept);

    //根据公司代码获取职位信息
    List<Map<String, Object>> listPostByCompany(@Param("companyCode") String companyCode);

    //根据公司代码和一级部门获取部门信息
    List<Map<String, Object>> listDeptByFirstDept(@Param("companyCode") String companyCode, @Param("firstDept") Integer firstDept);

    /**
     * 根据ID获取指定职位的名称；
     *
     * @param postId：职位ID；
     * @return ：职位的名称；
     */
    String getPostNameById(Integer postId);

    String getCompanyCode(Integer deptId);
}