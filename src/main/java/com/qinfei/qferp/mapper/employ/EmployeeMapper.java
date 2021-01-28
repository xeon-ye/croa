package com.qinfei.qferp.mapper.employ;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.employ.Employee;
import com.qinfei.qferp.excel.EmployeeExcelInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 员工信息数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeMapper {
    /**
     * 根据主键删除单条记录；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int deleteByPrimaryKey(Employee record);

    /**
     * 插入单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insert(Employee record);

    /**
     * 插入单条记录，插入前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insertSelective(Employee record);

    /**
     * 更新单条记录，更新前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKeySelective(Employee record);

    /**
     * 更新单条记录的一些额外信息，更新前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateExtraInfoByPrimaryKey(Employee record);

    /**
     * 更新单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKey(Employee record);

    List<String> listEmpNumByCompanyCode(@Param("companyCode") String companyCode, @Param("year") String year);

    /**
     * 批量更新数据的状态；
     *
     * @param params：参数集合，包含状态和ID集合；
     * @return ：操作印象的记录数；
     */
    int updateStateByBatchId(Map<String, Object> params);

    /**
     * 根据主键查询单条记录；
     *
     * @param empId：主键ID；
     * @return ：查询结果的封装对象；
     */
    Employee selectByPrimaryKey(Integer empId);

    /**
     * 根据员工ID查询相关的信息；
     *
     * @param empId：主键ID；
     * @return ：员工的信息集合；
     */
    Map<String, Object> selectEditInfoById(Integer empId);

    /**
     * 根据身份证号码查询是否为已填写入职申请的人员；
     *
     * @param params：查询参数，包含entryId：主键ID，empCode：身份证号码；
     * @return ：统计数量；
     */
    int checkRepeatByCode(Map<String, Object> params);

    /**
     * 查询工号是否已存在；
     *
     * @param record：查询参数，包含empId：主键ID，empNum：工号；
     * @return ：统计数量；
     */
    int checkRepeatByNum(Employee record);

    /**
     * 根据身份证查询状态；
     *
     * @param empCode：身份证号码；
     * @return ：状态；
     */
    Integer checkStateByCode(String empCode);

    /**
     * 根据主键ID查询状态；
     *
     * @param empId：主键ID；
     * @return ：状态；
     */
    Integer checkStateById(Integer empId);

    /**
     * 根据身份证查询主键ID；
     *
     * @param empCode：身份证号码；
     * @return ：主键ID；
     */
    Integer selectIdByCode(@Param("empCode") String empCode, @Param("companyCode") String companyCode);

    /**
     * 根据入职申请ID查询主键ID；
     *
     * @param entryId：入职申请ID；
     * @return ：主键ID；
     */
    Integer selectIdByEntryId(Integer entryId);

    /**
     * 根据主键ID查询工号；
     *
     * @param empId：主键ID；
     * @return ：工号；
     */
    String selectNumById(Integer empId);

    /**
     * 根据提供的姓名和手机号码获取ID；
     *
     * @param params：查询参数，包含empName：姓名，empPhone：联系电话；
     * @return ：员工ID；
     */
    Integer selectIdByNameAndPhone(Map<String, Object> params);

    /**
     * 根据主键ID查询关联的登录账户ID；
     *
     * @param empId：主键ID；
     * @return ：员工关联的账户ID；
     */
    Integer selectUserIdById(Integer empId);

    /**
     * 根据主键ID查询关联的登录账户ID；
     *
     * @param params：查询参数；
     * @return ：员工关联的账户ID；
     */
    List<Map<String, Object>> selectUserIdsByIds(Map<String, Object> params);

    /**
     * 根据主键ID查询生成系统登录用户所需的信息；
     *
     * @param empId：主键ID；
     * @return ：员工信息；
     */
    Employee selectUserInfoById(Integer empId);

    Employee selectUserInfoByUserId(Integer userId);

    /**
     * 查询发起流程所需的数据；
     *
     * @param params：查询参数；
     * @return ：数据集合；
     */
    Map<String, Object> selectEmployeeInfoById(Map<String, Object> params);

    /**
     * 根据权限查询码查询发起流程审核所需的数据；
     *
     * @param params：查询参数；
     * @return ：数据集合；
     */
    Employee selectApproveInfoByCode(Map<String, Object> params);

    /**
     * 分页查询员工信息；
     *
     * @param params：查询参数；
     * @return ：查询的员工信息集合；
     */
    List<Map<String, Object>> selectPageEmployee(Map<String, Object> params);

    //批量新增员工并返回员工ID
    void insertSelectiveFormExcel(List<Employee> list);

    int updateFromExcel(Employee employee);

    int deleteByEntryId(EmployeeExcelInfo info);

    @Select("select count(1) from e_employee where emp_num = #{v} and state > -1")
    long countByEmpNum(String empNum);

    @Select("select count(1) from e_employee where emp_num = #{v} and user_id is null and state > -1")
    long countByEmpNumAndUserIdIsNull(String empNum);

    @Select("select count(1) from e_employee where emp_num = #{v} and emp_dept is null and state > -1")
    long countByEmpNumAndDeptIdIsNull(String empNum);

    @Update("update e_employee set user_id = #{userId}, user_name = #{userName} where emp_id = #{empId} and state > -1")
    int linkEmpUserId(@Param("userId") int userId, @Param("userName") String userName, @Param("empId") Integer empId);

    @Update("update e_employee set emp_dept = #{deptId} where emp_num = #{empNum} and state > -1")
    int linkEmpDeptId(@Param("deptId") int deptId, @Param("empNum") String empNum);

    @Select("SELECT count(ccus.id) " +
            " FROM e_employee emp " +
            " left join t_crm_company_user_salesman ccus on emp.user_id = ccus.user_id and ccus.state = 1 and ccus.delete_flag = 0" +
            " WHERE emp.emp_id = #{empId}")
    long getCustByEmpId(@Param("empId") int empId);

    /**
     * 根据empNum查询记录
     * @param empNum
     * @return
     */
    @Select("SELECT emp_id FROM e_employee WHERE emp_num = #{empNum} limit 1")
    Employee selectByEmpNum(@Param("empNum") String empNum);

    @Update("Update e_employee set state = #{state} WHERE emp_id=#{empId}")
    void updateStateByEmpId(@Param("empId")Integer empId,@Param("state")Integer state);

    @Update("update e_employee set user_id = null,user_name=null WHERE emp_id=#{empId}")
    void updateEmployeeUser(@Param("empId") int empId);
}