package com.qinfei.qferp.mapper.employ;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.employ.EmployEntry;
import com.qinfei.qferp.excel.EmployeeExcelDTO;
import com.qinfei.qferp.excel.EmployeeExcelInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 入职申请数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployEntryMapper {
    /**
     * 根据主键删除单条记录；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int deleteByPrimaryKey(EmployEntry record);

    /**
     * 插入单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insert(EmployEntry record);

    /**
     * 插入单条记录，插入前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insertSelective(EmployEntry record);

    /**
     * 插入单条记录，插入前会判断对应属性是否为空；
     *
     * @return ：操作影响的记录数；
     */
    int insertSelectiveFromExcel(List<EmployeeExcelDTO> list);

    int updateEmpIdByEntryId(List<EmployeeExcelDTO> employeeExcelDTOList);

    int updateFromExcel(EmployeeExcelInfo info);

    int deleteByEntryId(EmployeeExcelInfo info);

    @Select("select entry_id from e_entry where entry_name = #{name} and entry_phone = #{phone} and state != -1 order by create_time asc")
    List<Integer> selectByNameAndPhone(@Param("name") String name, @Param("phone") String phone);

    /**
     * 更新单条记录，更新前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKeySelective(EmployEntry record);

    /**
     * 更新单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKey(EmployEntry record);

    /**
     * 根据ID更新创建人信息；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateCreateInfoById(EmployEntry record);

    /**
     * 根据主键ID完善资料；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int completeEntryById(EmployEntry record);

    /**
     * 批量更新数据的状态；
     *
     * @param params：参数集合，包含状态和ID集合；
     * @return ：操作印象的记录数；
     */
    int updateStateByBatchId(Map<String, Object> params);

    /**
     * 根据员工ID更新数据状态；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateStateByEmpId(EmployEntry record);

    /**
     * 根据主键查询单条记录；
     *
     * @param entryId：主键ID；
     * @return ：查询结果的封装对象；
     */
    EmployEntry selectByPrimaryKey(Integer entryId);

    /**
     * 查询发起录用流程所需的信息；
     *
     * @param entryId：主键ID；
     * @return ：查询结果的封装对象；
     */
    EmployEntry selectTaskById(Integer entryId);

    /**
     * 根据查询码获取入职申请的ID和状态；
     *
     * @param entryValidate：查询码；
     * @return ：入职申请对象；
     */
    EmployEntry selectApproveInfo(String entryValidate);

    /**
     * 获取审核状态下的数据当前的审核节点；
     *
     * @param entryId：数据ID；
     * @return ：当前状态代码；
     */
    Integer selectApproveNode(Integer entryId);

    /**
     * 根据主键ID查询该数据是否为待审核状态；
     *
     * @param entryId：主键ID；
     * @return ：统计数量；
     */
    int checkEnableUpdate(Integer entryId);

    /**
     * 根据主键ID查询该数据时否为审核中状态；
     *
     * @param entryId：主键ID；
     * @return ：统计数量；
     */
    int checkEnableApprove(Integer entryId);

    /**
     * 根据主键ID和状态查询该数据是否进入到资料审核阶段；
     *
     * @param record：入职申请对象，包含entryId：主键ID，entryComplete：是否资料完整0 或 1；
     * @return ：统计数量；
     */
    int checkEnableComplete(EmployEntry record);

    /**
     * 根据身份证号码查询是否为已填写入职申请的人员；
     *
     * @param params：查询参数，包含entryId：主键ID，empCode：身份证号码；
     * @return ：统计数量；
     */
    int checkRepeatByCode(Map<String, Object> params);

    /**
     * 根据身份证查询主键ID；
     *
     * @param empCode：身份证号码；
     * @return ：主键ID；
     */
    Integer selectIdByCode(String empCode);

    /**
     * 根据提供的姓名、身份证号码、联系电话找回查询码；
     *
     * @param params：查询参数，包含entryName：姓名，entryPhone：联系电话，empCode：身份证号码；
     * @return ：入职申请的查询码；
     */
    String selectEntryValidate(Map<String, Object> params);

    /**
     * 获取入职申请的信息；
     *
     * @param params：参数集合，包含entryId：主键ID，entryValidate：查询码；
     * @return ：入职申请信息；
     */
    Map<String, Object> selectEntryInfo(Map<String, Object> params);

    /**
     * 分页查询入职申请信息；
     *
     * @param params：查询参数；
     * @return ：查询的入职申请信息集合；
     */
    List<Map<String, Object>> selectPageEntry(Map<String, Object> params);

    /**
     * 根据查询码获取同意录用的入职申请ID；
     *
     * @param entryValidate：查询码；
     * @return ：入职申请ID；
     */
    Integer selectIdByValidate(String entryValidate);

    /**
     * 查询入职申请的状态；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请对象；
     */
    EmployEntry selectStateById(Integer entryId);

    /**
     * 查询入职申请的创建信息；
     *
     * @param empId：员工ID；
     * @return ：入职申请对象；
     */
    EmployEntry selectCreateInfoById(Integer empId);

    /**
     * 根据入职申请ID查询入职申请相关的基本信息；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请的基本信息；
     */
    Map<String, Object> selectEmployInfoById(Integer entryId);

    /**
     * 根据入职申请ID查询入职申请相关的文件信息；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请的文件集合；
     */
    Map<String, Object> selectEntryFileById(Integer entryId);

    @Select("select b.edu_id as eduId,b.edu_highest as eduHighest,a.entry_id as entryId,b.edu_start as eduStart,b.edu_end as eduEnd,a.emp_num as empNum" +
            " ,a.emp_name as empName,b.edu_college as eduCollege,b.edu_duration as eduDuration,b.edu_major as eduMajor,b.edu_record as eduRecord,b.edu_location as eduLocation " +
            "from e_employee a,e_entry_education b where a.entry_id = b.entry_id and a.state > -1 and b.state > -1 and a.emp_id = #{v}")
    List<Map<String, Object>> educationInfo(long empId);

    @Select("select a.entry_id as entryId,b.exp_id as expId,a.emp_num as empNum, a.emp_name as empName,b.exp_start as expStart,b.exp_end as expEnd," +
            " b.exp_company as expCompany, b.exp_location as expLocation,b.exp_profession as expProfession,b.exp_salary as expSalary," +
            " b.exp_contactor as expContactor,b.exp_resign_reason as expResignReason" +
            " from e_employee a,e_entry_experience b where a.entry_id = b.entry_id and a.state > -1 and b.state > -1 and a.emp_id = #{v}")
    List<Map<String, Object>> experienceInfo(long empId);
}