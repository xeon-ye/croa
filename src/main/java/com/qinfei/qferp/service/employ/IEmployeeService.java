package com.qinfei.qferp.service.employ;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.employ.Employee;
import com.qinfei.qferp.entity.employ.EmployeeConnect;
import com.qinfei.qferp.entity.employ.EmployeeLeave;
import com.qinfei.qferp.entity.employ.EmployeeTransfer;
import com.github.pagehelper.PageInfo;

/**
 * 员工花名册业务接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:23；
 */
public interface IEmployeeService {
    /**
     * 更新单条记录到数据库中；
     *
     * @param employeeInfo：员工对象字符串；
     * @return ：处理完毕的员工对象；
     */
    void updateEmployee(String employeeInfo);

    /**
     * 保存或更新单条记录到数据库中；
     *
     * @param record：员工对象；
     * @param empCode：身份证号码；
     * @return ：处理完毕的员工对象；
     */
    Employee saveOrUpdate(Employee record, String empCode);

    /**
     * 启动转正流程；
     *
     * @param data：返回给前端的数据；
     * @param empId：员工ID；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     */
    void startFormalProcess(ResponseData data, int empId, int userId, String userName);

    /**
     * 启动离职流程；
     *
     * @param data：返回给前端的数据；
     * @param leave：离职对象；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     * @param leaveCompany：选中的公司原因；
     * @param leavePerson：选中的个人原因；
     * @param otherReason：选中的其他原因；
     */
    void startLeaveProcess(ResponseData data, EmployeeLeave leave, Integer[] leaveCompany, Integer[] leavePerson, Integer[] otherReason, int userId, String userName);

    /**
     * 启动调岗流程；
     *
     * @param data：返回给前端的数据；
     * @param transfer：员工调岗对象；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     */
    void startTransferProcess(ResponseData data, EmployeeTransfer transfer, int userId, String userName);

    /**
     * 启动交接流程；
     *
     * @param data：返回给前端的数据；
     * @param connect：员工交接对象；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     */
    void startConnectProcess(ResponseData data, EmployeeConnect connect, int userId, String userName);

    /**
     * 审批流程中的交接数据更新；
     *
     * @param connect：员工交接对象；
     * @param processState：当前审批节点；
     * @return ：处理结果提示信息；
     */
    void updateConnect(ResponseData data, EmployeeConnect connect, int processState);

    /**
     * 获取转正流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     */
    void setFormalApproveData(ResponseData data, String code);

    /**
     * 获取离职流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     */
    void setLeaveApproveData(ResponseData data, String code);

    /**
     * 获取调岗流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     */
    void setTransferApproveData(ResponseData data, String code);

    /**
     * 获取交接流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     * @param conType：交接类型，定义参考com.qinfei.qferp.utils.IEmployConnect；
     */
    void setConnectApproveData(ResponseData data, String code, int conType);

    /**
     * 员工转正的流程更新状态；
     *
     * @param empId：主键ID；
     * @param code：当前使用的查询码；
     * @param state：当前状态；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     */
    void processFormal(int empId, String code, int state, String taskId, Integer itemId);

    /**
     * 员工离职的流程更新状态；
     *
     * @param empId：主键ID；
     * @param code：当前使用的查询码；
     * @param state：当前状态；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     */
    void processLeave(int empId, String code, int state, String taskId, Integer itemId);

    /**
     * 员工调岗的流程更新状态；
     *
     * @param empId：主键ID；
     * @param code：当前使用的查询码；
     * @param state：当前状态；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     */
    void processTransfer(int empId, String code, int state, String taskId, Integer itemId);

    /**
     * 员工交接的流程更新状态；
     *
     * @param empId：主键ID；
     * @param code：当前使用的查询码；
     * @param state：当前状态；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     * @param type：交接流程的类型，参考com.qinfei.qferp.utils.IEmployConnect；
     */
    void processConnect(int empId, String code, int state, String taskId, Integer itemId, int type);

    /**
     * 删除员工数据；
     *
     * @param empIds：员工ID数组；
     * @return ：处理结果；
     */
    String deleteEmployee(Integer[] empIds);

    /**
     * 生成员工登录账号；
     *
     * @param empId：员工ID；
     * @return ：操作结果提示；
     */
    String grantEmployee(int empId);

    /**
     * 根据身份证号码查询是否为已填写入职申请的人员；
     *
     * @param entryId：主键ID；
     * @param empCode：身份证号码；
     * @return ：查询结果，true为验证通过，false为已存在；
     */
    boolean checkRepeatByCode(Integer entryId, String empCode, String entryCompanyCode);

    /**
     * 查询工号是否已存在；
     *
     * @param record：查询参数，包含empId：主键ID，empNum：工号；
     * @return ：查询结果，true为验证通过，false为已存在；
     */
    boolean checkRepeatByNum(Employee record);

    /**
     * 根据身份证查询状态；
     *
     * @param empCode：身份证号码；
     * @return ：状态；
     */
    Integer checkStateByCode(String empCode);

    /**
     * 根据提供的姓名和手机号码获取ID；
     *
     * @param empName：姓名；
     * @param empPhone：联系电话；
     * @return ：员工ID；
     */
    Integer selectIdByNameAndPhone(String empName, String empPhone);

    /**
     * 查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    Map<String, Object> selectEmployeeInfoById(int empId);

    /**
     * 根据主键ID查询编辑所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    Map<String, Object> selectEditInfoById(int empId);

    /**
     * 根据主键ID查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    Map<String, Object> selectFormalInfoById(int empId);

    /**
     * 根据主键ID查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    Map<String, Object> selectLeaveInfoById(int empId);

    //根据empId获取客户信息
    long getCustByEmpId(int empId);

    /**
     * 根据主键ID查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    Map<String, Object> selectTransferInfoById(int empId);

    /**
     * 根据主键ID查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @param processId：流程标识，参考com.qinfei.qferp.utils.IProcess；
     * @return ：数据集合；
     */
    Map<String, Object> selectConnectInfoById(int empId, int processId);

    /**
     * 分页查询员工信息；
     *
     * @param params：查询参数；
     * @param pageable：分页参数；
     * @return ：查询的员工信息集合；
     */
    PageInfo<Map<String, Object>> selectPageEmployee(Map<String, Object> params, Pageable pageable);

    /**
     * 员工管理首页员工列表查询
     * @param params
     * @param pageable
     * @return
     *//*
    PageInfo<Map<String, Object>> getEmployee(Map<String, Object> params, Pageable pageable);

    *//**
     * 员工管理首页获取前5项统计信息
     * @param map
     * @return
     *//*
    Map topStatistics(Map map);

    //员工管理首页，获取学历饼图
    Map educationPie(Map map);

    //员工管理首页，获取年龄段饼图
    Map ageGroupPie(Map map);
*/
    void delFamily(int[] ids);

    void delEducation(int[] ids);

    void delExperience(int[] ids);

    int linkEmpUserId(Integer userId, String userName, Integer deptId, Integer empId, Integer empDept);

    ResponseData linkEmployee(Integer empId);

    /**
     * 状态更新Employee
     * @param empNum
     * @param state
     */
    void updateEmployeeState(String empNum, Integer state);
}