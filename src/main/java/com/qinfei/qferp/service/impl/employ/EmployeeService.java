package com.qinfei.qferp.service.impl.employ;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.StrUtil;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.employ.*;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.Post;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.employ.EmployeeBasicMapper;
import com.qinfei.qferp.mapper.employ.EmployeeMapper;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.mapper.sys.PostMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.employ.*;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.sys.IDeptService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.*;

/**
 * 员工花名册业务接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:26；
 */
@Service
public class EmployeeService implements IEmployeeService {
    // 数据执行接口；
    @Autowired
    private EmployeeMapper employeeMapper;
    // 入职申请业务接口；
    @Autowired
    private IEmployEntryService entryService;
    // 入职申请基本信息业务接口；
    @Autowired
    private IEmployeeBasicService basicService;
    // 入职申请的家庭婚姻信息业务接口；
    @Autowired
    private IEmployEntryFamilyService familyService;
    // 入职申请的教育培训经历业务接口；
    @Autowired
    private IEmployEntryEducationService educationService;
    // 入职申请的工作经历业务接口；
    @Autowired
    private IEmployEntryExperienceService experienceService;
    // 转正管理业务接口；
    @Autowired
    private IEmployeeFormalService formalService;
    // 离职管理业务接口；
    @Autowired
    private IEmployeeLeaveService leaveService;
    // 调岗管理业务接口；
    @Autowired
    private IEmployeeTransferService transferService;
    // 交接管理业务接口；
    @Autowired
    private IEmployeeConnectService connectService;
    // 薪资管理业务接口；
    @Autowired
    private IEmployeeSalaryService salaryService;
    // 入职申请的审批信息接口；
    @Autowired
    private IEmployEntryCommentService commentService;
    // 员工轨迹业务接口；
    @Autowired
    private IEmployeeTrajectoryService trajectoryService;
    // 基础资源接口；
    @Autowired
    private IEmployResourceService resourceService;
    // 部门业务接口；
    @Autowired
    private IDeptService deptService;
    // 用户业务接口；
    @Autowired
    private IUserService userService;
    // 流程业务接口；
    @Autowired
    private IProcessService processService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DeptZwMapper deptZwMapper;
    @Autowired
    private EmployeeBasicMapper employeeBasicMapper;
    @Autowired
    private PostMapper postMapper;

    /**
     * 保存或更新单条记录到数据库中；
     *
     * @param record：员工对象；
     * @param empCode：身份证号码；
     * @return ：处理完毕的员工对象；
     */
    @Override
    @Transactional
    public Employee saveOrUpdate(Employee record, String empCode) {
        User user = AppUtil.getUser();
        // 主键ID；
        Integer empId = record.getEmpId();
        // 入职申请ID；
        Integer entryId = record.getEntryId();

        // 判断是否为入职申请审批通过调用；
        boolean isSpecial = false;
        // 先查询一次是否已有记录；
        if (empId == null && entryId != null) {
            empId = employeeMapper.selectIdByEntryId(entryId);
            record.setEmpId(empId);

            isSpecial = true;
        }
        // 先检查库中是否有离职数据，离职再入职；
        if (empId == null && !StringUtils.isEmpty(empCode)) {
            empId = employeeMapper.selectIdByCode(empCode, user.getCompanyCode());
            record.setEmpId(empId);
        }
        if (empId == null) {
            Integer oldState = record.getState();
            EmployeeBasic employeeBasic = employeeBasicMapper.selectByEntryId(entryId);
            record.setCreateInfo(); //这里会将状态改为0-试用, 上面先缓存原来的状态，可能为5-实习
            if(oldState != null){
                record.setState(oldState);
            }else {
                record.setState(IEmployee.EMPLOYEE_PROBATION);
            }

            // 创建新的工号
            record.setEmpNum(buildEmpNum(user.getCompanyCode(), employeeBasic.getEmpDate() != null ? DateUtils.format(employeeBasic.getEmpDate(),"yyyy") : DateUtils.format(record.getCreateTime(),"yyyy")));
            // 先存储后获取生成的主键；
            employeeMapper.insertSelective(record);
            // 获取入职的部门；
            /*Dept dept = deptService.getById(record.getEmpDept());
            if (dept != null) {
                // 获取部门的公司代码；
                String companyCode = dept.getCompanyCode();
                if (!StringUtils.isEmpty(companyCode)) {
                    // 获取新增后的主键ID；
                    empId = record.getEmpId();

                    DecimalFormat format = new DecimalFormat("#000000");
                    String empNum = companyCode + format.format(empId);
                    // 公司代码 + 6位格式化的主键ID拼成工号；
                    record.setEmpNum(empNum);

                    // 使用新对象来更新，避免过多字段，此处插入更新人信息，视为新增操作；
                    Employee newData = new Employee();
                    newData.setEmpId(empId);
                    newData.setEmpNum(empNum);
                    // 更新数据；
                    employeeMapper.updateByPrimaryKeySelective(newData);
                }
            }*/
        } else {
            String empNum = record.getEmpNum();
            // 工号去重校验；
            if (StringUtils.isEmpty(empNum) || checkRepeatByNum(record)) {
                if (!isSpecial) {
                    record.setEntryId(null);
                }

                record.setUpdateInfo();
                employeeMapper.updateByPrimaryKeySelective(record);

                // 重新封装回对象；
                record.setEntryId(entryId);
            }
            // 如果没有工号，返回工号；
            if (StringUtils.isEmpty(empNum)) {
                record.setEmpNum(employeeMapper.selectNumById(empId));
            }
        }
        return record;
    }

    //创建新的工号
    private String buildEmpNum(String companyCode, String year){
        List<String> empNumList = employeeMapper.listEmpNumByCompanyCode(companyCode,year);
        if(CollectionUtils.isNotEmpty(empNumList)){
            String oldEmpNum = empNumList.get(empNumList.size() - 1);
            Integer maxNum = Integer.parseInt(oldEmpNum.substring(oldEmpNum.length()-3));
            //从头开始遍历，由于批量导入员工工号人事填写，可能会计算错误，出现工号不连续情况，入职申请时动态生成工号将漏掉的补全，当所有满足取max+1的工号
            String empNum = String.format("%s%s%03d",companyCode,year,maxNum+1); //默认最大值+1
            for(int count = 1; count <= (maxNum+1); count++){
                empNum = String.format("%s%s%03d",companyCode,year,count);
                if(empNumList.contains(empNum)){
                    continue;
                }else {
                    break;
                }
            }
            return empNum;
        }else {
            return String.format("%s%s%s",companyCode,year,"001");
        }
    }

    /**
     * 更新单条记录到数据库中；
     *
     * @param employeeInfo：员工对象字符串；
     * @return ：处理完毕的员工对象；
     */
    @Override
    @Transactional
    public void updateEmployee(String employeeInfo) {
        // 解析对象；
        JSONObject json = JSON.parseObject(employeeInfo);
        EmployeeBasic basic = (EmployeeBasic) EntityUtil.getNewObject(json, EmployeeBasic.class);
        basicService.saveOrUpdate(basic);
        Employee employee = (Employee) EntityUtil.getNewObject(json, Employee.class);
        saveOrUpdate(employee, basic.getEmpCode());
    }

    /**
     * 启动转正流程；
     *
     * @param data：返回给前端的数据；
     * @param empId：员工ID；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     */
    @Override
    @Transactional
    public void startFormalProcess(ResponseData data, int empId, int userId, String userName) {
        Map<String, Object> employeeMap = selectFormalInfoById(empId);
        if (employeeMap == null) {
            data.putDataValue("message", "该员工不存在或已在审核中。");
        } else {
            EmployeeFormal employeeFormal = (EmployeeFormal) EntityUtil.getNewObject(employeeMap, EmployeeFormal.class);
            // 新增或更新转正记录信息；
            formalService.saveOrUpdate(employeeFormal);
            Employee employee = (Employee) EntityUtil.getNewObject(employeeMap, Employee.class);
            processService.addFormalProcess(employee, Const.ITEM_J3, userId, userName, employeeFormal.getEmpDept());
            data.putDataValue("message", "操作完成。");
        }
    }

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
    @Override
    public void startLeaveProcess(ResponseData data, EmployeeLeave leave, Integer[] leaveCompany, Integer[] leavePerson, Integer[] otherReason, int userId, String userName) {
        // 防止注入，从数据库查询数据；
        Map<String, Object> params = new HashMap<>();
        params.put("empId", leave.getEmpId());
        Map<String, Object> employeeMap = getEmployeeInfo(params, false);
        if (employeeMap == null) {
            data.putDataValue("message", "该员工不存在或已在审核中。");
        } else {
            EmployeeLeave newLeave = (EmployeeLeave) EntityUtil.getNewObject(employeeMap, EmployeeLeave.class);
            String arrayContent;
            // 公司原因；
            if (leaveCompany != null) {
                arrayContent = Arrays.toString(leaveCompany);
                arrayContent = arrayContent.substring(1, arrayContent.length() - 1);
                newLeave.setLeaveCompany(arrayContent);
            }

            // 个人原因；
            if (leavePerson != null) {
                arrayContent = Arrays.toString(leavePerson);
                arrayContent = arrayContent.substring(1, arrayContent.length() - 1);
                newLeave.setLeavePerson(arrayContent);
            }

            // 其他原因；
            if (otherReason != null) {
                arrayContent = Arrays.toString(otherReason);
                arrayContent = arrayContent.substring(1, arrayContent.length() - 1);
                newLeave.setOtherReason(arrayContent);
            }
            // 复制属性；
            newLeave.setLeaveId(leave.getLeaveId());
            newLeave.setEmpState(leave.getEmpState());
            newLeave.setLeaveDate(leave.getLeaveDate());
            newLeave.setLeaveType(leave.getLeaveType());
            newLeave.setLeaveTypeContent(leave.getLeaveTypeContent());
            newLeave.setLeaveCompanyOther(leave.getLeaveCompanyOther());
            newLeave.setLeavePersonOther(leave.getLeavePersonOther());
            newLeave.setOtherReasonRemark(leave.getOtherReasonRemark());

            // 新增或更新离职记录信息；
            leaveService.saveOrUpdate(newLeave);
            Employee employee = (Employee) EntityUtil.getNewObject(employeeMap, Employee.class);
            processService.addLeaveProcess(employee, Const.ITEM_J3, userId, userName, leave.getEmpDept());
            data.putDataValue("message", "操作完成。");
        }
    }

    /**
     * 启动调岗流程；
     *
     * @param data：返回给前端的数据；
     * @param transfer：员工调岗对象；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     */
    @Override
    public void startTransferProcess(ResponseData data, EmployeeTransfer transfer, int userId, String userName) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", transfer.getEmpId());
        Map<String, Object> employeeMap = getEmployeeInfo(params, false);
        if (employeeMap == null) {
            data.putDataValue("message", "该员工不存在或已在审核中。");
        } else {
            EmployeeTransfer employeeTransfer = (EmployeeTransfer) EntityUtil.getNewObject(employeeMap, EmployeeTransfer.class);
            // 复制属性；
            employeeTransfer.setTranId(transfer.getTranId());
            employeeTransfer.setEmpState(transfer.getEmpState());
            employeeTransfer.setBeforeSalary(transfer.getBeforeSalary());
            employeeTransfer.setBeforePost(transfer.getBeforePost());
            employeeTransfer.setBeforePerformance(transfer.getBeforePerformance());
            employeeTransfer.setBeforeOther(transfer.getBeforeOther());
            employeeTransfer.setAfterSalary(transfer.getAfterSalary());
            employeeTransfer.setAfterPost(transfer.getAfterPost());
            employeeTransfer.setAfterPerformance(transfer.getAfterPerformance());
            employeeTransfer.setAfterOther(transfer.getAfterOther());
            employeeTransfer.setAfterDept(transfer.getAfterDept());
            employeeTransfer.setAfterDeptName(transfer.getAfterDeptName());
            employeeTransfer.setAfterProfession(transfer.getAfterProfession());
            employeeTransfer.setAfterProfessionName(transfer.getAfterProfessionName());
            employeeTransfer.setTransDate(transfer.getTransDate());
            employeeTransfer.setTransReason(transfer.getTransReason());
            employeeTransfer.setRoleId(transfer.getRoleId());
            employeeTransfer.setRoleType(transfer.getRoleType());
            employeeTransfer.setRoleName(transfer.getRoleName());

            // 新增或更新调岗记录信息；
            transferService.saveOrUpdate(employeeTransfer);
            // 启动流程；
            Employee employee = (Employee) EntityUtil.getNewObject(employeeMap, Employee.class);
            processService.addTransferProcess(employee, Const.ITEM_J3, userId, userName, employeeTransfer.getEmpDept(),transfer.getAfterDept(), employeeTransfer.getTranId());
            data.putDataValue("message", "操作完成。");
        }
    }

    /**
     * 启动交接流程；
     *
     * @param data：返回给前端的数据；
     * @param connect：员工交接对象；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     */
    @Override
    public void startConnectProcess(ResponseData data, EmployeeConnect connect, int userId, String userName) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", connect.getEmpId());
        Map<String, Object> employeeMap = getEmployeeInfo(params, false);
        if (employeeMap == null) {
            data.putDataValue("message", "该员工不存在或已在审核中。");
        } else {
            EmployeeConnect employeeConnect = (EmployeeConnect) EntityUtil.getNewObject(employeeMap, EmployeeConnect.class);
            // 复制属性；
            employeeConnect.setConId(connect.getConId());
            employeeConnect.setConType(connect.getConType());
            employeeConnect.setConData(connect.getConData());
            employeeConnect.setDeptLeader(connect.getDeptLeader());
            employeeConnect.setDeptLeaderName(connect.getDeptLeaderName());
            employeeConnect.setEmpDate(connect.getEmpDate());
            employeeConnect.setEmpState(connect.getEmpState());
            employeeConnect.setLeaveType(connect.getLeaveType());
            employeeConnect.setLeaveTypeContent(connect.getLeaveTypeContent());
            employeeConnect.setConDateType(connect.getConDateType());
            employeeConnect.setConDate(connect.getConDate());
            employeeConnect.setCompleteDate(connect.getCompleteDate());
            employeeConnect.setBreakMoney(connect.getBreakMoney());
            userService.onHandover(userId);

            // 新增或更新调岗记录信息；
            connectService.saveOrUpdate(employeeConnect);
            // 启动流程；
            Employee employee = (Employee) EntityUtil.getNewObject(employeeMap, Employee.class);
            processService.addHandOverProcess(employee, Const.ITEM_J3, userId, userName, employeeConnect.getEmpDept(), employeeConnect.getConType(),employeeConnect.getConId());
            data.putDataValue("message", "操作完成。");
        }
    }

    /**
     * 审批流程中的交接数据更新；
     *
     * @param connect：员工交接对象；
     * @param processState：当前审批节点；
     * @return ：处理结果提示信息；
     */
    @Override
    public void updateConnect(ResponseData data, EmployeeConnect connect, int processState) {
        EmployeeConnect employeeConnect = new EmployeeConnect();
        switch (processState) {
            // 个人节点更新交接清单；
            case IConst.STATE_GR:
                employeeConnect.setConId(connect.getConId());
                employeeConnect.setConList(connect.getConList());
                break;
            // 部长节点；
            case IConst.STATE_BZ:
                employeeConnect.setConId(connect.getConId());
                employeeConnect.setConEmpId(connect.getConEmpId());
                employeeConnect.setConEmpName(connect.getConEmpName());
                employeeConnect.setConDeptApprove(connect.getConDeptApprove());
                employeeConnect.setConDeptRemark(connect.getConDeptRemark());
                break;
            // 人事节点；
            case IConst.STATE_RS:
                employeeConnect.setConId(connect.getConId());
                employeeConnect.setConPersonalId(connect.getConPersonalId());
                employeeConnect.setConPersonalName(connect.getConPersonalName());
                employeeConnect.setConPersonal(connect.getConPersonal());
                employeeConnect.setConPersonalRemark(connect.getConPersonalRemark());
                break;
            // 财务节点；
            case IConst.STATE_CFO:
                employeeConnect.setConId(connect.getConId());
                employeeConnect.setConFinanceId(connect.getConFinanceId());
                employeeConnect.setConFinanceName(connect.getConFinanceName());
                employeeConnect.setConFinance(connect.getConFinance());
                employeeConnect.setConFinanceRemark(connect.getConFinanceRemark());
                break;
            case IConst.STATE_CEO:
                Integer userId = userMapper.selectUserId(connect.getEmpId());
                if (userId != null){
                    User user = userMapper.get(User.class, userId);
                    if (user !=null) {
                        user.setHandoverState(IConst.handOverStateOff);
                        user.setUpdateTime(new Date());
                        user.setUpdateUserId(AppUtil.getUser().getId());
                        userMapper.update(user);
                    }
                }
                break;
            default:
                break;
        }
        // 如果有内容提交则更新；
        if (employeeConnect.getConId() != null) {
            connectService.saveOrUpdate(employeeConnect);
        }
        data.putDataValue("message", "操作完成。");
    }

    /**
     * 获取转正流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     */
    @Override
    public void setFormalApproveData(ResponseData data, String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("processId", IProcess.PROCESS_FORMAL);
        params.put("stateList", Arrays.asList(IEmployee.EMPLOYEE_PROBATION,IEmployee.EMPLOYEE_INTERNSHIP)); //试用和实习
        Employee employee = employeeMapper.selectApproveInfoByCode(params);
        if (employee == null) {
            data.putDataValue("message", "未找到相关的信息，可能信息已过期。");
        } else {
            // 任务ID给前端；
            data.putDataValue("taskId", employee.getTaskId());
            int processState = employee.getProcessState();
            // 状态给前端，用于判断当前审批节点；
            data.putDataValue("processState", processState);
            // 获取审核人；
            List<User> users = null;
            // 人事审核的下个节点为总经理；
            if (processState == IConst.STATE_RS) {
                users = userService.listLeaderByState(IConst.STATE_CEO);
            }
            // 部长审核的下个节点是人事；
            if (processState == IConst.STATE_BZ) {
                users = userService.listLeaderByState(IConst.STATE_RS);
            }
            // 个人的下个节点是部长；
            if (processState == IConst.STATE_GR) {
                users = userService.listLeaderByState(IConst.STATE_BZ);
            }
            data.putDataValue("user", users);
            // 部门ID用于审核；
            if (users != null && !users.isEmpty()) {
                data.putDataValue("deptId", users.get(0).getDeptId());
            }
            data.putDataValue("employee", formalService.selectByParentId(employee.getEmpId()));
        }
    }

    /**
     * 获取离职流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     */
    @Override
    public void setLeaveApproveData(ResponseData data, String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("processId", IProcess.PROCESS_LEAVE);
        Employee employee = employeeMapper.selectApproveInfoByCode(params);
        if (employee == null) {
            data.putDataValue("message", "未找到相关的信息，可能信息已过期。");
        } else {
            int processState = employee.getProcessState();
            // 状态给前端，用于判断当前审批节点；
            data.putDataValue("processState", processState);
            // 任务ID给前端；
            data.putDataValue("taskId", employee.getTaskId());
            // 获取审核人；
            List<User> users = null;
            // 部长审核的下个节点是人事；
            if (processState == IConst.STATE_BZ) {
                users = userService.listLeaderByState(IConst.STATE_RS);
            }
            // 部门ID用于审核；
            if (users != null && !users.isEmpty()) {
                data.putDataValue("deptId", users.get(0).getDeptId());
            }
            data.putDataValue("user", users);
            data.putDataValue("employee", leaveService.selectByParentId(employee.getEmpId()));
        }
    }

    /**
     * 获取调岗流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     */
    @Override
    public void setTransferApproveData(ResponseData data, String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("processId", IProcess.PROCESS_TRANSFER);
        Employee employee = employeeMapper.selectApproveInfoByCode(params);
        if (employee == null) {
            data.putDataValue("message", "未找到相关的信息，可能信息已过期。");
        } else {
            // 获取关联的调岗记录；
            EmployeeTransfer employeeTransfer = transferService.selectByParentId(employee.getEmpId());
            // 存储；
            data.putDataValue("employee", employeeTransfer);

            // 任务ID给前端；
            data.putDataValue("taskId", employee.getTaskId());
            int processState = employee.getProcessState();
            // 状态给前端，用于判断当前审批节点；
            data.putDataValue("processState", processState);

            // 获取审核人；
            List<User> users = null;
            // 转入部门领导审批的下个节点为人事；
            if (processState == IConst.STATE_ZRBZ) {
                users = userService.listLeaderByState(IConst.STATE_RS);
            }
            // 部长审核的下个节点是转入部门的领导；
            if (processState == IConst.STATE_BZ) {
                if (employeeTransfer != null) {
                    users = userService.listLeaderByDeptId(employeeTransfer.getAfterDept());
                }
            }
            // 个人的下个节点是部长；
            if (processState == IConst.STATE_GR) {
                users = userService.listLeaderByState(IConst.STATE_BZ);
            }
            // 部门ID用于审核；
            if (users != null && !users.isEmpty()) {
                data.putDataValue("deptId", users.get(0).getDeptId());
            }
            // 审核人信息存入；
            data.putDataValue("user", users);
        }
    }

    /**
     * 获取交接流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     * @param conType：交接类型，定义参考com.qinfei.qferp.utils.IEmployConnect；
     */
    @Override
    public void setConnectApproveData(ResponseData data, String code, int conType) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        Integer processId = connectService.getHandOverProcessId(conType);
        // 类型不存在；
        if (processId == null) {
            data.putDataValue("message", "交接类型错误，请联系管理员。");
        } else {
            params.put("processId", processId);
            Employee employee = employeeMapper.selectApproveInfoByCode(params);
            if (employee == null) {
                data.putDataValue("message", "未找到相关的信息，可能信息已过期。");
            } else {
                // 获取关联的交接记录；
                params = new HashMap<>();
                params.put("conType", conType);
                params.put("empId", employee.getEmpId());
                EmployeeConnect employeeConnect = connectService.selectByRelateData(params);
                // 查询部门的其他员工，选择交接人；
                Integer empDept = employeeConnect.getEmpDept();
                // 存储审批信息；
                data.putDataValue("employee", employeeConnect);

                // 获取当前节点；
                int processState = employee.getProcessState();

                // 获取登录人公司代码；
                User user = AppUtil.getUser();
                Dept dept = user.getDept();
                String companyCode = "XH";
                if (dept != null) {
                    companyCode = dept.getCompanyCode();
                }

                // 获取审核人；
                List<User> users = null;
                // 个人的下个节点是部长；
                if (processState == IConst.STATE_GR) {
                    users = userService.listLeaderByState(IConst.STATE_BZ);
                }

                // 部长审核的下个节点是人事部；
                if (processState == IConst.STATE_BZ) {
                    users = userService.listLeaderByState(IConst.STATE_RS);
                    if (empDept != null) {
                        List<User> userList = userService.listByDeptId(empDept);//userService.queryUserByDeptIdONLY(empDept);
                        // 排除申请人；
                        Integer userId = employee.getUserId();
                        if (userId != null) {
                            Iterator<User> iterator = userList.iterator();
                            User tempUser;
                            while (iterator.hasNext()) {
                                tempUser = iterator.next();
                                if (tempUser.getId().equals(userId)) {
                                    iterator.remove();
                                }
                            }
                        }
                        data.putDataValue("colleague", userList);
                    }
                }

                // 人事部的下个审批节点是财务总监；
                if (processState == IConst.STATE_RS) {
                    users = userService.listLeaderByState(IConst.STATE_CFO);
                    if (empDept != null) {
                        List<User> userList = userService.queryRSList(companyCode);
                        userList.addAll(userService.queryXZList(companyCode));
                        data.putDataValue("colleague", userList);
                    }
                }

                // 财务总监的下个审批节点是总经理；
                if (processState == IConst.STATE_CFO) {
                    users = userService.listLeaderByState(IConst.STATE_CEO);
                    if (empDept != null) {
                        List<User> userList = userService.queryCWZLInfo(companyCode);
                        userList.addAll(userService.queryCWBZInfo(companyCode));
                        data.putDataValue("colleague", userList);
                    }
                }

                // 部门ID用于审核；
                if (users != null && !users.isEmpty()) {
                    data.putDataValue("deptId", users.get(0).getDeptId());
                }

                // 任务ID给前端；
                data.putDataValue("taskId", employee.getTaskId());

                // 状态给前端，用于判断当前审批节点；
                data.putDataValue("processState", processState);
                // 审核人信息存入；
                data.putDataValue("user", users);
            }
        }
    }

    /**
     * 员工转正的流程更新状态；
     *
     * @param empId：主键ID；
     * @param code：当前使用的查询码；
     * @param state：当前状态；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     */
    @Override
    @Transactional
    public void processFormal(int empId, String code, int state, String taskId, Integer itemId) {
        // 轨迹内容；
        String content = null;
        // 员工状态；
        Integer employeeState = null;
        // 操作类型；
        int operate = 0;
        // 根据流程状态来更新员工的状态；
        switch (state) {
            // 拒绝的流程状态改为试用期；
            case IConst.STATE_REJECT:
                Employee employee = employeeMapper.selectByPrimaryKey(empId);
                if(employee != null){
                    employeeState = employee.getState();
                }else {
                    employeeState = IEmployee.EMPLOYEE_PROBATION;
                }
                content = "提交的转正申请被拒绝";
                operate = ITrajectoryOperate.OPERATE_REJECT;

                // 清空查询码；
                code = "0";
                break;
            // 审核完成，更新状态为转正状态；
            case IConst.STATE_FINISH:
                employeeState = IEmployee.EMPLOYEE_FORMAL;
                content = "提交的转正申请审核通过";
                operate = ITrajectoryOperate.OPERATE_PASS;

                // 清空查询码；
                code = "0";
                break;
            // 提交流程；
            case IConst.STATE_GR:
                content = "提交转正申请";
                operate = ITrajectoryOperate.OPERATE_SUBMIT;
                break;
            default:
                break;
        }

        // 更新关联的转正记录状态；
        formalService.updateFormalState(empId, state);

        // 更新数据；
        updateDataState(empId, code, IProcess.PROCESS_FORMAL, state, taskId, itemId, employeeState);

        // 添加员工轨迹记录；
        if (!StringUtils.isEmpty(content)) {
            EmployeeFormal record = formalService.selectByParentId(empId);
            EmployeeTrajectory trajectory = trajectoryService.getTrajectory(record);
            trajectory.setEmpContent(content);
            trajectory.setEmpTransaction(ITrajectoryTransaction.TRANSACTION_FORMAL);
            trajectory.setEmpOperate(operate);
            trajectoryService.saveOrUpdate(trajectory);
        }
    }

    /**
     * 员工离职的流程更新状态；
     *
     * @param empId：主键ID；
     * @param code：当前使用的查询码；
     * @param state：当前状态；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     */
    @Override
    @Transactional
    public void processLeave(int empId, String code, int state, String taskId, Integer itemId) {
        // 轨迹内容；
        String content = null;
        // 员工状态；
        Integer employeeState = null;
        // 操作类型；
        int operate = 0;
        // 根据流程状态来更新员工的状态；
        switch (state) {
            case IConst.STATE_REJECT:
                content = "提交的离职申请被拒绝";
                operate = ITrajectoryOperate.OPERATE_REJECT;

                // 清空查询码；
                code = "0";
                break;
            // 审核完成，更新状态为交接状态；
            case IConst.STATE_FINISH:
                employeeState = IEmployee.EMPLOYEE_CONNECT_READY;
                content = "提交的离职申请审核通过";
                operate = ITrajectoryOperate.OPERATE_PASS;

                // 清空查询码；
                code = "0";
                break;
            // 提交流程；
            case IConst.STATE_BZ:
                content = "提交离职申请";
                operate = ITrajectoryOperate.OPERATE_SUBMIT;
                break;
            default:
                break;
        }

        // 更新关联的离职记录状态；
        leaveService.updateLeaveState(empId, state);

        // 更新员工状态；
        updateDataState(empId, code, IProcess.PROCESS_LEAVE, state, taskId, itemId, employeeState);

        // 添加员工轨迹记录；
        if (!StringUtils.isEmpty(content)) {
            EmployeeLeave record = leaveService.selectByParentId(empId);
            EmployeeTrajectory trajectory = trajectoryService.getTrajectory(record);
            trajectory.setEmpContent(content);
            trajectory.setEmpTransaction(ITrajectoryTransaction.TRANSACTION_LEAVE);
            trajectory.setEmpOperate(operate);
            trajectoryService.saveOrUpdate(trajectory);
        }
    }

    /**
     * 员工调岗的流程更新状态；
     *
     * @param empId：主键ID；
     * @param code：当前使用的查询码；
     * @param state：当前状态；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     */
    @Override
    @Transactional
    public void processTransfer(int empId, String code, int state, String taskId, Integer itemId) {
        // 员工状态；
        Integer employeeState = null;
        // 轨迹内容；
        String content = null;
        // 操作类型；
        int operate = 0;
        switch (state) {
            case IConst.STATE_REJECT:
                content = "提交的调岗申请被拒绝";
                operate = ITrajectoryOperate.OPERATE_REJECT;

                // 清空查询码；
                code = "0";
                break;
            case IConst.STATE_FINISH:
                employeeState = IEmployee.EMPLOYEE_CONNECT_READY;
                content = "提交的调岗申请审核通过";
                operate = ITrajectoryOperate.OPERATE_PASS;

                // 清空查询码；
                code = "0";
                break;
            // 提交流程；
            case IConst.STATE_GR:
                content = "提交调岗申请";
                operate = ITrajectoryOperate.OPERATE_SUBMIT;
                break;
            default:
                break;
        }

        // 更新关联的调岗记录状态；
        transferService.updateTransferState(empId, state);

        updateDataState(empId, code, IProcess.PROCESS_TRANSFER, state, taskId, itemId, employeeState);

        // 添加员工轨迹记录；
        if (!StringUtils.isEmpty(content)) {
            EmployeeTransfer record = transferService.selectByParentId(empId);
            EmployeeTrajectory trajectory = trajectoryService.getTrajectory(record);
            trajectory.setEmpContent(content);
            trajectory.setEmpTransaction(ITrajectoryTransaction.TRANSACTION_TRANSFER);
            trajectory.setEmpOperate(operate);
            trajectoryService.saveOrUpdate(trajectory);
        }
    }

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
    @Override
    @Transactional
    public void processConnect(int empId, String code, int state, String taskId, Integer itemId, int type) {
        // 轨迹内容；
        String content = null;
        // 员工状态；
        Integer employeeState = null;
        // 流程标识；
        Integer processId = null;
        // 操作类型；
        int operate = 0;
        switch (state) {
            case IConst.STATE_REJECT:
                content = "提交的交接申请被拒绝";
                operate = ITrajectoryOperate.OPERATE_REJECT;

                employeeState = IEmployee.EMPLOYEE_CONNECT_READY;

                // 清空查询码；
                code = "0";
                break;
            case IConst.STATE_FINISH:
                content = "提交的交接申请审核通过";
                operate = ITrajectoryOperate.OPERATE_PASS;

                // 清空查询码；
                code = "0";
                break;
            // 提交流程；
            case IConst.STATE_GR:
                content = "提交交接申请";
                operate = ITrajectoryOperate.OPERATE_SUBMIT;

                employeeState = IEmployee.EMPLOYEE_CONNECT;
                break;
            default:
                employeeState = IEmployee.EMPLOYEE_CONNECT;
                break;
        }

        // 根据类型确定最后的状态；
        switch (type) {
            // 离职；
            case IEmployConnect.CONNECT_LEAVE:
                // 审批的状态；
                if (state == IConst.STATE_FINISH) {
                    employeeState = IEmployee.EMPLOYEE_LEAVE;

                    // 查询关联的用户ID；
                    Integer userId = employeeMapper.selectUserIdById(empId);
                    if (userId != null) {
                        // 清空其角色信息；
                        userService.delUserRoleByUserId(userId);

                        // 删除账号；
                        userService.delById(userId);
                    }
                    //交接完成后清除Employee的userId和userName
                    employeeMapper.updateEmployeeUser(empId);
                }
                // 如果有相关的入职申请，也需更新记录；
                if (entryService.updateStateByEmpId(empId, IEmployee.EMPLOYEE_LEAVE) > 0) {
                    // 更新关联的入职申请审批记录；
                    commentService.updateStateByEmpId(empId, null, IEmployee.EMPLOYEE_LEAVE);

                    // 更新关联的薪资记录；
                    salaryService.updateStateByEmpId(empId, IEmployee.EMPLOYEE_LEAVE);
                }

                // 更新员工离职记录的状态；
                leaveService.updateConnectLeaveState(empId, state);
                // 流程标识；
                processId = IProcess.PROCESS_HANDOVER_LEAVE;
                break;
            // 调岗；
            case IEmployConnect.CONNECT_TRANSFER:
                // 更新关联数据；
                if (state == IConst.STATE_FINISH) {
                    // 查询调岗数据；
                    EmployeeTransfer record = transferService.selectByParentId(empId);
                    if (record != null) {
                        // 更新部门和职位；
                        Employee employee = new Employee();
                        employee.setEmpId(empId);
                        employee.setEmpDept(record.getAfterDept());
                        employee.setEmpProfession(record.getAfterProfession());
                        employee.setValidCode(code);
                        // 更新当前进行的流程标识和状态，用于查看进度；
                        employee.setProcessId(IProcess.PROCESS_HANDOVER_TRANSFER);
                        employee.setProcessState(state);
                        // 更新流程当前的任务ID；
                        employee.setTaskId(taskId);
                        // 更新待办事项的ID；
                        employee.setItemId(itemId);
                        // 设置更新信息；
                        employee.setUpdateInfo();

                        //从调岗记录获取调岗前员工状态
                        int oldState;
                        if (record.getEmpState() != null) {
                            oldState = record.getEmpState();
                        } else {
                            oldState = IEmployee.EMPLOYEE_FORMAL;
                        }
                        employee.setState(oldState);
                        employeeMapper.updateByPrimaryKeySelective(employee);

                        // 查询关联的用户ID；
                        Integer userId = employeeMapper.selectUserIdById(empId);
                        if (userId != null) {
                            // 清空其角色信息；
                            userService.delUserRoleByUserId(userId);

                            // 更新登录账号的部门信息；
                            User user = new User();
                            user.setId(userId);
                            user.setDeptId(record.getAfterDept());
                            user.setDeptName(record.getAfterDeptName());
                            user.setPostId(record.getAfterProfession());
                            user.setPostName(record.getAfterProfessionName());
                            //调岗后部门绑定角色
                            Integer rid = record.getRoleId();
                            if(rid!=null){
                                //调岗后绑定选择的职位
                                userService.addBatch(userId,rid,null);
                            }else{
                                //调岗后老数据绑定角色为其他员工
                                Integer roleId = userService.selectMRJS();
                                userService.addBatch(userId,roleId,null);
                            }
                            userService.update(user);
                        }

                        // 更新薪资数据；
                        EmployeeSalary salary = new EmployeeSalary();
                        salary.setSalId(salaryService.selectIdByParentEmpId(empId));
                        // 试用期/实习更新试用工资；
                        if (oldState == IEmployee.EMPLOYEE_PROBATION || oldState == IEmployee.EMPLOYEE_INTERNSHIP) {
                            salary.setTrialSalary(record.getAfterSalary());
                            salary.setTrialPost(record.getAfterPost());
                            salary.setTrialPerformance(record.getAfterPerformance());
                            salary.setTrialOther(record.getAfterOther());
                        } else {
                            salary.setFormalSalary(record.getAfterSalary());
                            salary.setFormalPost(record.getAfterPost());
                            salary.setFormalPerformance(record.getAfterPerformance());
                            salary.setFormalOther(record.getAfterOther());
                        }
                        salaryService.saveOrUpdate(salary);
                    }
                }
                // 更新关联的转岗记录状态；
                transferService.updateConnectTransferState(empId, state);
                // 流程标识；
                processId = IProcess.PROCESS_HANDOVER_TRANSFER;
                break;
            default:
                break;
        }

        // 更新关联的交接记录状态；
        connectService.updateConnectState(empId, type, state);

        // 更新数据，转岗交接完成后，已进行了单独更新处理；
        if (employeeState != null) {
            updateDataState(empId, code, processId, state, taskId, itemId, employeeState);
        }

        // 添加员工轨迹记录；
        if (!StringUtils.isEmpty(content)) {
            EmployeeConnect record = connectService.selectByParentId(empId, type);
            EmployeeTrajectory trajectory = trajectoryService.getTrajectory(record);
            trajectory.setEmpContent(content);
            trajectory.setEmpTransaction(ITrajectoryTransaction.TRANSACTION_CONNECT);
            trajectory.setEmpOperate(operate);
            trajectoryService.saveOrUpdate(trajectory);
        }
    }

    /**
     * 删除员工数据；
     *
     * @param empIds：员工ID数组；
     * @return ：处理结果；
     */
    @Override
    @Transactional
    public String deleteEmployee(Integer[] empIds) {
        if (empIds == null) {
            return "请选择要删除的员工。";
        } else {
            Map<String, Object> params = new HashMap<>();

            // 转化为集合；
            List<Integer> empIdList = Arrays.asList(empIds);
            params.put("empIds", empIdList);

            // 获取关联的用户ID；
            List<Map<String, Object>> userMapList = employeeMapper.selectUserIdsByIds(params);

            if (userMapList != null && userMapList.size() > 0) {
                // 处理数据；
                List<Integer> userIds = new ArrayList<>();
                for (Map<String, Object> userMap : userMapList) {
                    if (Objects.isNull(userMap)) continue;
                    userIds.add(Integer.parseInt(userMap.get("user_id").toString()));
                }

                params = new HashMap<>();
                if (userIds.size() > 0) {
                    params.put("userIds", userIds);
                    // 清空其角色信息；
                    userService.delUserRoleByUserIds(params);
                    // 删除账号；
                    userService.delByIds(params);
                }
            }

            params = new HashMap<>();
            params.put("state", IEmployee.EMPLOYEE_DELETE);
            params.put("empIds", empIdList);
            employeeMapper.updateStateByBatchId(params);
            return "操作完成。";
        }
    }

    /**
     * 生成员工登录账号；
     *
     * @param empId：员工ID；
     * @return ：操作结果提示；
     */
    @Override
    @Transactional
    public String grantEmployee(int empId) {
        Employee employee = employeeMapper.selectUserInfoById(empId);
        if (employee == null) {
            return "该员工不存在或已绑定登录账户。";
        } else {
            // 获取入职申请ID，用于更新入职申请关联的几个表的创建人信息；
            Integer entryId = employee.getEntryId();
            String empName = employee.getEmpName();
            //判断用户表中是否已经有账号信息
            Map map = new HashMap();
            String deptIds = userService.getChilds(employee.getEmpDept());//获取员工部门及子部门
            if (StringUtils.isEmpty(deptIds)) {
                throw new QinFeiException(1003, "无法获取该用户所在部门信息！");
            }
            deptIds = deptIds.replaceAll("\\$,", "");
            map.put("deptIds", deptIds);
            map.put("userName", empName);
            List<User> list = userMapper.listGetByNameAndDept(map);
            Integer num = 0;
            //如果用户表中存在该用户账号则关联用户，否则新建用户
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    User user = list.get(i);
                    //判断该用户账号是否已经关联员工账号
                    Employee employee1 = employeeMapper.selectUserInfoByUserId(user.getId());
                    if (employee1 != null) {
                        continue;
                    } else {
                        //没有关联，则关联账号
                        int postId= employee.getEmpProfession();
                        Map param=new HashMap();
                        param.put("userId",user.getId());
                        param.put("postId",postId);
                        param.put("postName",resourceService.getPostNameById(postId));
                        param.put("updateUserId",AppUtil.getUser().getId());
                        param.put("updateTime",new Date());
                        userMapper.updateUserPost(param);
                        employee.setUserId(user.getId());
                        employee.setUpdateInfo();
                        num = employeeMapper.updateByPrimaryKeySelective(employee);
                    }
                }
            }
            //没有关联成功，则新建账户
            if (num == 0) {
//            String userName = PinYinUtils.getFullCode(empName);
                String userName = StrUtil.getPingYin(empName);
                if (StringUtils.isEmpty(userName)) {
                    return "姓名转换错误，请手动添加。";
                } else {
                    List<User> users = userService.queryUserByUserName(userName);
                    // 存在重名；
                    if (users != null && !users.isEmpty()) {
                        // 获取身份证号码；
                        String empRemark = employee.getEmpRemark();
                        // 使用随机数；
                        if (StringUtils.isEmpty(empRemark)) {
                            userName = userName + PrimaryKeyUtil.getStringUniqueKey();
                        } else {
                            // 使用身份证号码后6位；
                            empRemark = empRemark.substring(empRemark.length() - 4);
                            userName = userName + empRemark;
                            users = userService.queryUserByUserName(userName);
                            // 确认如果重复则使用随机码；
                            if (users != null && !users.isEmpty()) {
                                userName = userName + PrimaryKeyUtil.getStringUniqueKey();
                            }
                        }
                    }
                    User user = new User();
                    user.setUserName(userName);
                    int suffix = 0;
                    //授权账号时若存在同名账号   处理同名账号
                    while (userMapper.countAllByName(empName) > 0) {
                        empName += ++suffix;
                    }
                    user.setName(empName);
                    user.setPhone(employee.getEmpPhone());
                    // 部门；
                    Integer empDept = employee.getEmpDept();
                    user.setDeptId(empDept);
                    user.setDeptName(resourceService.getDeptNameById(empDept));
                    // 职务；
                    Integer empProfession = employee.getEmpProfession();
                    user.setPostId(empProfession);
                    user.setPostName(resourceService.getPostNameById(empProfession));
                    user.setCompanyCode(resourceService.getCompanyCode(employee.getEmpDept()));
                    user.setNo(employee.getEmpNum());
                    user.setPassword("123456");//设置初始密码
                    // 创建用户；
                    userService.add(user);
                    Integer userId = user.getId();

                    // 绑定用户ID；
                    employee = new Employee();
                    employee.setEmpId(empId);
                    employee.setUserId(userId);
                    employee.setUserName(userName);
                    employee.setUpdateInfo();
                    employeeMapper.updateByPrimaryKeySelective(employee);

                    // 更新入职申请的创建人信息；
                    entryService.updateCreateId(entryId, userId, userName);

                    // 更新基本信息的创建人；
                    basicService.updateCreateInfoByParentId(entryId, userId, userName);

                    // 更新家庭信息的创建人；
                    familyService.updateCreateInfoByParentId(entryId, userId, userName);

                    // 更新教育经历的创建人；
                    educationService.updateCreateInfoByParentId(entryId, userId, userName);

                    // 更新工作经历的创建人；
                    experienceService.updateCreateInfoByParentId(entryId, userId, userName);
                }
            }
            return "操作完成。";
        }
    }

    /**
     * 根据身份证号码查询是否为已填写入职申请的人员；
     *
     * @param entryId：主键ID；
     * @param empCode：身份证号码；
     * @return ：查询结果，true为验证通过，false为已存在；
     */
    @Override
    public boolean checkRepeatByCode(Integer entryId, String empCode, String entryCompanyCode) {
        if (StringUtils.isEmpty(empCode)) {
            return false;
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("entryId", entryId);
            params.put("empCode", empCode);
            params.put("entryCompanyCode", entryCompanyCode);
            return employeeMapper.checkRepeatByCode(params) == 0;
        }
    }

    /**
     * 查询工号是否已存在；
     *
     * @param record：查询参数，包含empId：主键ID，empNum：工号；
     * @return ：查询结果，true为验证通过，false为已存在；
     */
    @Override
    public boolean checkRepeatByNum(Employee record) {
        return employeeMapper.checkRepeatByNum(record) == 0;
    }

    /**
     * 根据身份证查询状态；
     *
     * @param empCode：身份证号码；
     * @return ：状态；
     */
    @Override
    public Integer checkStateByCode(String empCode) {
        // 先检查是否有数据；
        return employeeMapper.checkStateByCode(empCode);
    }

    /**
     * 根据提供的姓名和手机号码获取ID；
     *
     * @param empName：姓名；
     * @param empPhone：联系电话；
     * @return ：员工ID；
     */
    @Override
    public Integer selectIdByNameAndPhone(String empName, String empPhone) {
        Map<String, Object> params = new HashMap<>();
        params.put("empName", empName);
        params.put("empPhone", empPhone);
        return employeeMapper.selectIdByNameAndPhone(params);
    }

    /**
     * 查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    @Override
    public Map<String, Object> selectEmployeeInfoById(int empId) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        return employeeMapper.selectEmployeeInfoById(params);
    }

    /**
     * 根据主键ID查询编辑所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    @Override
    public Map<String, Object> selectEditInfoById(int empId) {
        return employeeMapper.selectEditInfoById(empId);
    }

    /**
     * 根据主键ID查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    @Override
    public Map<String, Object> selectFormalInfoById(int empId) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("stateList", Arrays.asList(IEmployee.EMPLOYEE_PROBATION, IEmployee.EMPLOYEE_INTERNSHIP)); //实习或者试用
        return getEmployeeInfo(params, false);
    }

    /**
     * 根据主键ID查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    @Override
    public Map<String, Object> selectLeaveInfoById(int empId) {

        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        Map<String, Object> employeeInfo = getEmployeeInfo(params, true);
        EmployeeLeave employeeLeave = leaveService.selectByParentId(empId);
        if (employeeLeave != null) {
            employeeInfo.put("leave", employeeLeave);
        }
        return employeeInfo;
    }

    /**
     * 根据EmpId获取客户信息
     *
     * @param empId
     * @return
     */
    @Override
    public long getCustByEmpId(int empId) {
        return employeeMapper.getCustByEmpId(empId);
    }

    /**
     * 根据主键ID查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @return ：数据集合；
     */
    @Override
    public Map<String, Object> selectTransferInfoById(int empId) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        Map<String, Object> employeeInfo = getEmployeeInfo(params, false);
        try {
            // 查询关联的调岗记录；
            EmployeeTransfer employeeTransfer = transferService.selectByParentId(empId);
            // 如果没有记录则还需要查询薪资待遇数据；
            if (employeeTransfer != null) {
                employeeInfo.put("transfer", employeeTransfer);
            } else {
                // 查询关联的薪资待遇；
                EmployeeSalary employeeSalary = salaryService.selectByParentEmpId(empId);
                if (employeeSalary != null) {
                    employeeInfo.put("salary", employeeSalary);
                }
            }
        } catch (Exception e) {
            throw new QinFeiException(1002, "查询调岗所需数据出错！");
        }
        return employeeInfo;
    }

    /**
     * 根据主键ID查询发起流程所需的数据；
     *
     * @param empId：主键ID；
     * @param processId：流程标识，参考com.qinfei.qferp.utils.IProcess；
     * @return ：数据集合；
     */
    @Override
    public Map<String, Object> selectConnectInfoById(int empId, int processId) {
        Map<String, Object> data = new HashMap<>();
        // 获取该数据关联的员工的ID；
        data.put("userId", employeeMapper.selectUserIdById(empId));
        // 数据类型；
        Integer conType = null;
        // 根据流程ID来查询对应的数据；
        switch (processId) {
            // 离职；
            case IProcess.PROCESS_LEAVE:
                conType = IEmployConnect.CONNECT_LEAVE;
                break;
            // 离职交接；
            case IProcess.PROCESS_HANDOVER_LEAVE:
                conType = IEmployConnect.CONNECT_LEAVE;
                break;
            // 调岗；
            case IProcess.PROCESS_TRANSFER:
                conType = IEmployConnect.CONNECT_TRANSFER;
                break;
            // 调岗交接；
            case IProcess.PROCESS_HANDOVER_TRANSFER:
                conType = IEmployConnect.CONNECT_TRANSFER;
                break;
            default:
                break;
        }
        // 查询历史记录；
        if (conType != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("conType", conType);
            params.put("empId", empId);
            EmployeeConnect employeeConnect = connectService.selectByRelateData(params);
            data.put("connect", employeeConnect);

            // 查询无结果再去查询关联的数据；
            if (employeeConnect == null) {
                // 根据流程ID来查询对应的数据；
                switch (processId) {
                    // 离职；
                    case IProcess.PROCESS_LEAVE:
                        data.put("connect", leaveService.selectByParentId(empId));
                        break;
                    // 调岗；
                    case IProcess.PROCESS_TRANSFER:
                        data.put("connect", transferService.selectByParentId(empId));
                        break;
                    default:
                        break;
                }
            }
        }
        return data;
    }

    /**
     * 分页查询员工信息；
     *
     * @param params：查询参数；
     * @param pageable：分页参数；
     * @return ：查询的员工信息集合；
     */
    @Override
    public PageInfo<Map<String, Object>> selectPageEmployee(Map<String, Object> params, Pageable pageable) {
        // 获取所有的民族数据；
        Map<Integer, String> allNation = resourceService.listAllNation();
        // 获取所有的部门数据；
        Map<String, String> allDept = resourceService.listAllDeptNameAndCode();
        // 获取所有的职位数据；
        Map<Integer, String> allPost = resourceService.listAllPost();
        User user = AppUtil.getUser();
        if (user == null)
            throw new QinFeiException(1002, "会话失效，请重新登录！");
        // 公司代码过滤；
        params.put("companyCode", user.getDept().getCompanyCode());
        Integer deptId = null;
        List<Role> roles = user.getRoles();
        Boolean flag=false;
        if (roles.size() > 0) {
            for (int i = 0; i < roles.size(); i++) {
                String roleType = roles.get(i).getType();
                String roleCode = roles.get(i).getCode();
                if ("ZJB".equals(roleType) || ("CW".equals(roleType) && "BZ".equals(roleCode)) || ("YW".equals(roleType) && "ZJ".equals(roleCode)) || ("JT".equals(roleType) && "ZC".equals(roleCode)) || ("JT".equals(roleType) && "FZC".equals(roleCode))) {
                    flag = true;
                    params.put("roleType", roleType);
                    params.put("roleCode", roleCode);
                    break;
                } else {
                    if ("RS".equals(roleType) || "XZ".equals(roleType)) {
                        params.put("roleType", roleType);
                        params.put("roleCode", roleCode);
                    }
                    flag = false;
                }
            }
        }
        //除行政、人事、政委部门外，其他部门只能看到自己部门的数据
        if (!IConst.ROLE_TYPE_XZ.equals(user.getDept().getCode()) && !IConst.ROLE_TYPE_RS.equals(user.getDept().getCode()) && flag==false
                && !(IConst.ROLE_CODE_ZW.equals(user.getDept().getCode()) && AppUtil.isRoleCode(IConst.ROLE_CODE_ZW))) {
            //当前登录人只能查询自己部门的数据
            deptId = user.getDeptId();
        }
        if (params.get("deptId") != null) {//当且仅指定了部门时
            deptId = Integer.parseInt(String.valueOf(params.get("deptId")));//获取请求的部门ID
        }
        //如果是政委
        if(IConst.ROLE_CODE_ZW.equals(user.getDept().getCode()) && AppUtil.isRoleCode(IConst.ROLE_CODE_ZW)){
            //如果当前政委是部门负责人
            String zwChildDept = "";
            if(user.getId().equals(user.getDept().getMgrId())){
                zwChildDept = userService.getChilds(user.getDept().getId());
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(zwChildDept) && zwChildDept.indexOf("$,") > -1) {
                    zwChildDept = zwChildDept.substring(2);
                }
            }

            //前台没有传递指定部门，则查询当前政委所管理的所有部门下人员，否则，查看当前部门及其子部门（政委管理的）
            String deptCode = params.get("deptCode") != null ? String.valueOf(params.get("deptCode")) : null;
            if(deptId == null){
                List<Map<String, Object>> deptList = deptZwMapper.listDeptInfoByParam(null, user.getId(), deptCode);
                if(CollectionUtils.isEmpty(deptList) && StringUtils.isEmpty(zwChildDept)){
                    throw new QinFeiException(1002, "当前政委没有绑定对应部门！");
                }
                List<Integer> deptIds = new ArrayList<>();
                for(Map<String, Object> dept : deptList){
                    deptIds.add(Integer.parseInt(String.valueOf(dept.get("id"))));
                }
                String bindDepts = org.apache.commons.lang3.StringUtils.join(deptIds, ","); //获取绑定部门
                //如果自己是部门负责人，查询时还需加上自己管理的部门
                if(!StringUtils.isEmpty(zwChildDept)){
                    bindDepts = StringUtils.isEmpty(bindDepts) ? zwChildDept : (bindDepts + "," + zwChildDept);
                }
                params.put("deptIds", bindDepts);
            }else {
                String deptIds = userService.getChilds(deptId);
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                }
                //如果筛选的部门是当前用户所在部门的部门或子部门，则无需去查询绑定了  本来就是自己负责的
                if(!StringUtils.isEmpty(zwChildDept) && zwChildDept.contains(String.valueOf(deptId))){
                    params.put("deptIds", deptIds);
                }else {
                    params.put("deptIds", org.apache.commons.lang3.StringUtils.join(deptZwMapper.listChildDeptIdByUserId(deptIds, user.getId()), ","));
                }
                params.remove("deptId");
            }
        }else {
            if (deptId != null) {
                String deptIds = userService.getChilds(deptId);
                if (StringUtils.isEmpty(deptIds)) {
                    throw new QinFeiException(1002, "无法获取该用户部门信息！");
                }
                deptIds = deptIds.replaceAll("\\$,", "");
                params.put("deptIds", deptIds);
            }
        }
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String, Object>> pageEntry = employeeMapper.selectPageEmployee(params);
        for (Map<String, Object> entry : pageEntry) {
            entry.put("deptName", allDept.get(entry.get("emp_dept").toString()));
            entry.put("deptCode", allDept.get(entry.get("emp_dept").toString() + "code"));
            entry.put("postName", allPost.get(entry.get("emp_profession")));
            entry.put("raceName", allNation.get(entry.get("emp_race")));
            //如果在审核并且是调岗或者交接状态
            if(!(StringUtils.isEmpty(entry.get("process_state")) || Integer.parseInt(String.valueOf(entry.get("process_state"))) < 3)){
                if(IProcess.PROCESS_TRANSFER == Integer.parseInt(String.valueOf(entry.get("process_id")))){
                    EmployeeTransfer employeeTransfer = transferService.selectByParentId(Integer.parseInt(String.valueOf(entry.get("emp_id"))));
                    if(employeeTransfer != null){
                        entry.put("tranId", employeeTransfer.getTranId());
                        entry.put("tranCreateTime", employeeTransfer.getCreateTime());
                    }
                }
                if(IProcess.PROCESS_HANDOVER_LEAVE == Integer.parseInt(String.valueOf(entry.get("process_id"))) || IProcess.PROCESS_HANDOVER_TRANSFER == Integer.parseInt(String.valueOf(entry.get("process_id")))){
                    int conType = Integer.parseInt(String.valueOf(entry.get("process_id"))) == IProcess.PROCESS_HANDOVER_LEAVE ? IEmployConnect.CONNECT_LEAVE : IEmployConnect.CONNECT_TRANSFER;
                    EmployeeConnect employeeConnect = connectService.selectByParentId(Integer.parseInt(String.valueOf(entry.get("emp_id"))), conType);
                    if(employeeConnect != null){
                        entry.put("tranId", employeeConnect.getConId());
                        entry.put("tranCreateTime", employeeConnect.getCreateTime());
                    }
                }
            }
        }
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(pageEntry);
        return pageInfo;
    }

    @Override
    public void delFamily(int[] ids) {
        for (int id : ids) {
            familyService.deleteByPrimaryKey(id);
        }
    }

    @Override
    public void delEducation(int[] ids) {
        for (int id : ids) {
            educationService.deleteByPrimaryKey(id);
        }
    }

    @Override
    public void delExperience(int[] ids) {
        for (int id : ids) {
            experienceService.deleteByPrimaryKey(id);
        }
    }

    @Override
    @Transactional
    public ResponseData linkEmployee(Integer empId) {
        Employee employeeInfo = employeeMapper.selectUserInfoById(empId);
        Integer empDept = employeeInfo.getEmpDept(); //员工部门
        if (employeeInfo==null || Objects.nonNull(employeeInfo.getUserId())) return ResponseData.customerError(50000, "该员工已关联账号");
        String phone = employeeInfo.getEmpPhone();
        String name = employeeInfo.getEmpName();
        List<User> userLs = userMapper.getIdByPhoneAndName(phone, name);
        if (userLs.isEmpty()) {
            List<User> users = userMapper.getUserByPhone(phone);
            if (users.isEmpty()) {
                return ResponseData.customerError(50000, "找不到该员工【" + phone + "】");
            } else if (users.size() > 0) {
                //通过电话号码查询,有对应系统账号时需要确认选择
                ResponseData data = ResponseData.ok();
                data.putDataValue("users", users);
                data.putDataValue("msg", "通过电话号码查询");
                return data;
            }
        } else {
            if(userLs.size()==1){
                User user = userLs.get(0);
                //判断用户部门和员工部门是否相等
                if(user.getDeptId().equals(empDept)){
                    int postId= employeeInfo.getEmpProfession();
                    Map map=new HashMap();
                    map.put("userId",user.getId());
                    map.put("postId",postId);
                    map.put("postName",resourceService.getPostNameById(postId));
                    map.put("updateUserId",AppUtil.getUser().getId());
                    map.put("updateTime",new Date());
                    userMapper.updateUserPost(map);
                    employeeMapper.linkEmpUserId(user.getId(), user.getUserName(), empId);
                    return ResponseData.ok();
                }else {
                    return ResponseData.customerError(1002, String.format("通过【%s】和【%s】找到账户【%s】，但部门不一致，不能进行关联",phone, name,user.getUserName()));
                }
            }else {
                //对应多个系统账号时允许选择
                ResponseData data = ResponseData.ok();
                data.putDataValue("users",userLs);
                data.putDataValue("msg","通过员工姓名和电话号码查询");
                return data;
            }
        }
        return ResponseData.ok();
    }

    /**
     * 设置更新的员工对象信息；
     *
     * @param empId：员工ID；
     * @param code：权限访问码；
     * @param processId：当前进行的流程标识；
     * @param state：当前节点；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     * @param employeeState：员工状态；
     */
    private void updateDataState(int empId, String code, int processId, int state, String taskId, Integer itemId, Integer employeeState) {
        // 更新数据；
        Employee employee = new Employee();
        employee.setEmpId(empId);
        // 更新当前进行的流程标识和状态，用于查看进度；
        employee.setProcessId(processId);
        employee.setProcessState(state);
        // 更新流程当前的任务ID；
        employee.setTaskId(taskId);
        // 更新待办事项的ID；
        employee.setItemId(itemId);
        employee.setValidCode(code);
        // 设置更新信息；
        employee.setUpdateInfo();
        employee.setState(employeeState);
        employeeMapper.updateByPrimaryKeySelective(employee);
    }

    /**
     * 查询审批所需的员工信息集合；
     *
     * @param params：查询参数；
     * @param userNeed：是否需要查询审批用户，true为是，false为否；
     * @return ：员工信息；
     */
    private Map<String, Object> getEmployeeInfo(Map<String, Object> params, boolean userNeed) {
        Map<String, Object> employeeMap = employeeMapper.selectEmployeeInfoById(params);
        if (employeeMap != null && employeeMap.size() > 0) {
            Object empDept = employeeMap.get("empDept");
            if (empDept != null) {
                int deptId = Integer.parseInt(empDept.toString());
                employeeMap.put("empDeptName", resourceService.getDeptNameById(deptId));
                // 获取上级领导；
                Dept dept = deptService.getById(deptId);
                employeeMap.put("deptLeader", dept.getMgrLeaderId());
                employeeMap.put("deptLeaderName", dept.getMgrLeaderName());

                // 如果需要查询审批用户；
                if (userNeed) {
                    employeeMap.put("user", userService.listLeaderByDeptId(deptId));
                }

            }
            if (!StringUtils.isEmpty(employeeMap.get("empProfession"))) {
                employeeMap.put("empProfessionName", resourceService.getPostNameById(Integer.parseInt(employeeMap.get("empProfession").toString())));
            }
        }
        return employeeMap;
    }

    //TODO 查询员工是否有未交接的客户
    public boolean getCustByEmpId() {
        //通过员工id获取所有的客户

        return false;
    }

    @Override
    @Transactional
    public int linkEmpUserId(Integer userId, String userName, Integer deptId, Integer empId, Integer empDept) {
        //判断用户部门和员工部门是否相等
        if(deptId != null && deptId.equals(empDept)){
            //手动关联修改用户职位
            Employee employee = employeeMapper.selectByPrimaryKey(empId);
            Integer postId = employee.getEmpProfession();
            Post post = postMapper.queryPostById(postId);
            Map map=new HashMap();
            map.put("userId",userId);
            map.put("postId",postId);
            map.put("postName",post.getName());
            map.put("updateUserId",AppUtil.getUser().getId());
            map.put("updateTime",new Date());
            userMapper.updateUserPost(map);
            return employeeMapper.linkEmpUserId(userId,userName,empId);
        }else {
            throw new QinFeiException(1002, "员工和关联用户部门不一致，不能进行关联");
        }
    }

    @Override
    public void updateEmployeeState(String EmpNum, Integer state) {
        Employee employee = employeeMapper.selectByEmpNum(EmpNum);
        if (employee != null) {
            employee.setEmpId(employee.getEmpId());
            employee.setState(state);
            employeeMapper.updateStateByEmpId(employee.getEmpId(), employee.getState());
        }
    }
}
