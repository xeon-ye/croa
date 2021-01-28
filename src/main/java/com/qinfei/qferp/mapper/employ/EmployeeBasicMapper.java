package com.qinfei.qferp.mapper.employ;

import com.qinfei.qferp.entity.employ.EmployeeBasic;
import com.qinfei.qferp.excel.EmployeeExcelDTO;
import com.qinfei.qferp.excel.EmployeeExcelInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工基本信息数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeBasicMapper {
    /**
     * 根据主键删除单条记录；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int deleteByPrimaryKey(EmployeeBasic record);

    /**
     * 插入单条记录，插入前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insertSelective(EmployeeBasic record);

    int insertSelectiveExcelBatch(List<EmployeeBasic> list);

    int updateFromExcel(EmployeeBasic employeeBasic);

    int deleteByEntryId(EmployeeExcelInfo info);

    /**
     * 更新单条记录，更新前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKeySelective(EmployeeBasic record);

    /**
     * 更新单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKey(EmployeeBasic record);

    /**
     * 根据父表ID更新学历相关的信息；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateEducationByParentId(EmployeeBasic record);

    /**
     * 根据父表ID更新工作履历相关的信息；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateExperienceByParentId(EmployeeBasic record);

    /**
     * 根据父表ID更新推荐人相关的信息；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateRelativeByParentId(EmployeeBasic record);

    /**
     * 根据父表ID完善资料；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int completeEntryByParentId(EmployeeBasic record);

    /**
     * 根据父表ID更新入职日期；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateEmpDateByParentId(EmployeeBasic record);

    /**
     * 根据父表ID更新创建人信息；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateCreateInfoByParentId(EmployeeBasic record);

    /**
     * 根据主键查询单条记录；
     *
     * @param basId：主键ID；
     * @return ：查询结果的封装对象；
     */
    EmployeeBasic selectByPrimaryKey(Integer basId);

    EmployeeBasic selectByEntryId(@Param("entryId") Integer entryId);

    @Select("select entry_id from e_employee_basic where emp_code = #{v} and state > -1")
    Integer countByEmpCode(String empCode);

    //根据员工身份证号查询员工信息
    List<EmployeeExcelDTO> listEmpInfoByEmpCode(@Param("empCode") String empCode);
}