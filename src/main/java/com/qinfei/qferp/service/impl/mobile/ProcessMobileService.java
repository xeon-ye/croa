package com.qinfei.qferp.service.impl.mobile;


import com.qinfei.qferp.entity.biz.ProjectNode;
import com.qinfei.qferp.entity.meeting.MeetingUser;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.employ.*;
import com.qinfei.qferp.entity.fee.Reimbursement_d;

import com.qinfei.qferp.mapper.employ.*;
import com.qinfei.qferp.mapper.fee.ReimbursementMapper;
import com.qinfei.qferp.mapper.meeting.MeetingUserMapper;
import com.qinfei.qferp.mapper.mobile.ProcessMobileMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.mobile.IProcessMobileService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.IConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import java.util.*;
/**
 * @CalssName: ProcessMobileService
 * @Description: 移动端流程接口
 * @Author: Xuxiong
 * @Date: 2020/6/8 0008 16:43
 * @Version: 1.0
 */

@Service
@Slf4j
public class ProcessMobileService implements IProcessMobileService {
    @Autowired
    private IUserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmployEntryCommentMapper employEntryCommentMapper;
    @Autowired
    private EmployeeSalaryMapper employeeSalaryMapper;
    @Autowired
    private EmployeeHireMapper employeeHireMapper;
    @Autowired
    private EmployeeFormalMapper employeeFormalMapper;
    @Autowired
    private EmployeeTransferMapper employeeTransferMapper;
    @Autowired
    private EmployeeConnectMapper employeeConnectMapper;
    @Autowired
    private EmployeeLeaveMapper employeeLeaveMapper;
    @Autowired
    private ReimbursementMapper reimbursementMapper;
    @Autowired
    private ProcessMobileMapper processMobileMapper;
    @Autowired
    private MeetingUserMapper meetingUserMapper;
    @Override
    public PageInfo<Map<String, Object>> listHireNotAudit(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                List<Map<String, Object>> hireList = processMobileMapper.listHireNotAudit(param);
                if(CollectionUtils.isNotEmpty(hireList)){
                    buildHireData(user, hireList, true);
                }
                pageInfo = new PageInfo<>(hireList);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listHireHasAudit(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                List<Map<String, Object>> hireList = processMobileMapper.listHireHasAudit(param);
                if(CollectionUtils.isNotEmpty(hireList)){
                    buildHireData(user, hireList, false);
                }
                pageInfo = new PageInfo<>(hireList);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listEmployeeNotAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null && param.get("process") != null){
                param.put("userId", user.getId());
                List<String> processList = Arrays.asList(String.valueOf(param.get("process")).split(","));
                param.put("processList", processList);
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                List<Map<String, Object>> employeeList = processMobileMapper.listEmployeeNotAuditByParam(param);
                if(CollectionUtils.isNotEmpty(employeeList)){
                    //转正数据
                    if(processList.contains("10")){
                        buildFormal(employeeList, true);
                    }
                    //调岗数据
                    if(processList.contains("14")){
                        buildTransfer(user, employeeList, true);
                    }
                    //交接数据：15-离职交接、16-调岗交接
                    if(processList.contains("15") || processList.contains("16")){
                        buildConnect(user, employeeList, true);
                    }
                    //离职数据
                    if(processList.contains("13")){
                        buildLeave(employeeList, true);
                    }
                }
                pageInfo = new PageInfo<>(employeeList);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listEmployeeHasAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null && param.get("process") != null){
                param.put("userId", user.getId());
                List<String> processList = Arrays.asList(String.valueOf(param.get("process")).split(","));
                param.put("processList", processList);
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                List<Map<String, Object>> employeeList = processMobileMapper.listHisTaskDataIdByUserId(param);
                if(CollectionUtils.isNotEmpty(employeeList)){
                    //转正数据
                    if(processList.contains("10")){
                        buildFormal(employeeList, false);
                    }
                    //调岗数据
                    if(processList.contains("14")){
                        buildTransfer(user, employeeList, false);
                    }
                    //交接数据：15-离职交接、16-调岗交接
                    if(processList.contains("15") || processList.contains("16")){
                        buildConnect(user, employeeList, false);
                    }
                    //离职数据
                    if(processList.contains("13")){
                        buildLeave(employeeList, false);
                    }
                }
                pageInfo = new PageInfo<>(employeeList);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listOutgoNotAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listOutgoNotAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listOutgoHasAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listOutgoHasAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listOutgoWorkUpNotAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listOutgoWorkUpNotAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listOutgoWorkUpHasAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listOutgoWorkUpHasAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listDropNotAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listDropNotAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listDropHasAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listDropHasAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listBorrowNotAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listBorrowNotAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listBorrowHasAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listBorrowHasAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listReimursementNotAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                List<Map<String, Object>> reimubursementList = processMobileMapper.listReimursementNotAuditByParam(param);
                if(CollectionUtils.isNotEmpty(reimubursementList)){
                    List<Integer> ids = new ArrayList<>();
                    reimubursementList.forEach(o -> {
                        ids.add(Integer.parseInt(String.valueOf(o.get("id"))));
                    });
                    List<Reimbursement_d> reimbursementDList = reimbursementMapper.listReimbursementDetailByIds(ids);
                    if(CollectionUtils.isNotEmpty(reimbursementDList)){
                        for(Map<String, Object> reimbure : reimubursementList){
                            Integer id = Integer.parseInt(String.valueOf(reimbure.get("id")));
                            List<Reimbursement_d> tmpList = new ArrayList<>();
                            for(Reimbursement_d reimbursementD : reimbursementDList){
                                if(id.equals(reimbursementD.getRemId())){
                                    tmpList.add(reimbursementD);
                                }
                            }
                            reimbure.put("reimbursementDs", tmpList);
                        }
                    }
                }
                pageInfo = new PageInfo<>(reimubursementList);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listReimursementHasAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                List<Map<String, Object>> reimubursementList = processMobileMapper.listReimursementHasAuditByParam(param);
                if(CollectionUtils.isNotEmpty(reimubursementList)){
                    List<Integer> ids = new ArrayList<>();
                    reimubursementList.forEach(o -> {
                        ids.add(Integer.parseInt(String.valueOf(o.get("id"))));
                    });
                    List<Reimbursement_d> reimbursementDList = reimbursementMapper.listReimbursementDetailByIds(ids);
                    if(CollectionUtils.isNotEmpty(reimbursementDList)){
                        for(Map<String, Object> reimbure : reimubursementList){
                            Integer id = Integer.parseInt(String.valueOf(reimbure.get("id")));
                            List<Reimbursement_d> tmpList = new ArrayList<>();
                            for(Reimbursement_d reimbursementD : reimbursementDList){
                                if(id.equals(reimbursementD.getRemId())){
                                    tmpList.add(reimbursementD);
                                }
                            }
                            reimbure.put("reimbursementDs", tmpList);
                        }
                    }
                }
                pageInfo = new PageInfo<>(reimubursementList);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listInvoiceNotAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listInvoiceNotAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listInvoiceHasAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listInvoiceHasAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listRefundNotAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listRefundNotAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Map<String, Object>> listRefundHasAuditByParam(Map<String, Object> param, Pageable pageable) {
        PageInfo<Map<String, Object>> pageInfo = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                pageInfo = new PageInfo<>(processMobileMapper.listRefundHasAuditByParam(param));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    //处理录用数据
    private void buildHireData(User user, List<Map<String, Object>> hireList, boolean auditFlag){
        List<User> rsUsers = null;
        List<User> zjlUsers = null;
        List<Integer> entryIdList = new ArrayList<>();
        for(Map<String, Object> hire : hireList){
            Integer entryId = Integer.parseInt(String.valueOf(hire.get("entryId")));
            if(auditFlag){
                Integer processState = Integer.parseInt(String.valueOf(hire.get("processState")));
                if (processState == IConst.STATE_BZ) {
                    if(CollectionUtils.isEmpty(rsUsers)){
                        rsUsers = userService.listLeaderByState(IConst.STATE_RS);
                    }
                    hire.put("user", rsUsers);
                    if(CollectionUtils.isNotEmpty(rsUsers)){
                        hire.put("deptId", rsUsers.get(0).getDeptId());
                    }
                }
                // 人事审核的下个节点为总经理；
                if (processState == IConst.STATE_RS) {
                    if(CollectionUtils.isEmpty(zjlUsers)){
                        zjlUsers = userMapper.selectByDeptCode(user.getCompanyCode());
                    }
                    hire.put("user", zjlUsers);
                    if(CollectionUtils.isNotEmpty(zjlUsers)){
                        hire.put("deptId", zjlUsers.get(0).getDeptId());
                    }
                }
            }
            entryIdList.add(entryId);
        }
        List<EmployeeHire> employeeHireList = employeeHireMapper.listHireByEntryId(entryIdList);
        List<EmployeeSalary> employeeSalaryList = employeeSalaryMapper.listByEntryId(entryIdList);
        List<EmployEntryComment> employEntryCommentList = employEntryCommentMapper.listByEntryId(entryIdList);
        for(Map<String, Object> hire : hireList){
            Integer entryId = Integer.parseInt(String.valueOf(hire.get("entryId")));
            if(CollectionUtils.isNotEmpty(employeeHireList)){
                for(EmployeeHire employeeHire : employeeHireList){
                    if(entryId.equals(employeeHire.getEntryId())){
                        hire.put("hire", employeeHire);
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(employeeSalaryList)){
                for(EmployeeSalary employeeSalary : employeeSalaryList){
                    if(entryId.equals(employeeSalary.getEntryId())){
                        hire.put("salary", employeeSalary);
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(employEntryCommentList)){
                List<EmployEntryComment> tmp = new ArrayList<>();
                for(EmployEntryComment employEntryComment : employEntryCommentList){
                    if(entryId.equals(employEntryComment.getEntryId())){
                        tmp.add(employEntryComment);
                    }
                }
                hire.put("comment", tmp);
            }
        }
    }

    //处理转正数据
    private void buildFormal(List<Map<String, Object>> employeeList, boolean auditFlag){
        List<User> bzUsers = null;
        List<User> rsUsers = null;
        List<User> zjlUsers = null;
        List<Integer> empIdList = new ArrayList<>();
        for(Map<String, Object> employee : employeeList){
            Integer empId = Integer.parseInt(String.valueOf(employee.get("dataId")));
            if(auditFlag){
                Integer processState = Integer.parseInt(String.valueOf(employee.get("processState")));
                // 人事审核的下个节点为总经理；
                if (processState == IConst.STATE_RS) {
                    if(CollectionUtils.isEmpty(zjlUsers)){
                        zjlUsers = userService.listLeaderByState(IConst.STATE_CEO);
                    }
                    employee.put("user", zjlUsers);
                    if(CollectionUtils.isNotEmpty(zjlUsers)){
                        employee.put("deptId", zjlUsers.get(0).getDeptId());
                    }
                }
                // 部长审核的下个节点是人事；
                if (processState == IConst.STATE_BZ) {
                    if(CollectionUtils.isEmpty(rsUsers)){
                        rsUsers = userService.listLeaderByState(IConst.STATE_RS);
                    }
                    employee.put("user", rsUsers);
                    if(CollectionUtils.isNotEmpty(rsUsers)){
                        employee.put("deptId", rsUsers.get(0).getDeptId());
                    }
                }
                // 个人的下个节点是部长；
                if (processState == IConst.STATE_GR) {
                    if(CollectionUtils.isEmpty(rsUsers)){
                        bzUsers = userService.listLeaderByState(IConst.STATE_BZ);
                    }
                    employee.put("user", bzUsers);
                    if(CollectionUtils.isNotEmpty(bzUsers)){
                        employee.put("deptId", bzUsers.get(0).getDeptId());
                    }
                }
            }
            empIdList.add(empId);
        }
        //获取转正数据
        List<EmployeeFormal> employeeFormalList = employeeFormalMapper.listByEmpId(empIdList);
        for(Map<String, Object> employee : employeeList){
            Integer empId = Integer.parseInt(String.valueOf(employee.get("dataId")));
            if(CollectionUtils.isNotEmpty(employeeFormalList)){
                for(EmployeeFormal employeeFormal : employeeFormalList){
                    if(empId.equals(employeeFormal.getEmpId())){
                        employee.put("employee", employeeFormal);
                    }
                }
            }
        }
        //获取员工信息
        List<Map<String, Object>> empList = processMobileMapper.listEmpByEmpIds(empIdList);
        for(Map<String, Object> employee : employeeList){
            Integer empId = Integer.parseInt(String.valueOf(employee.get("dataId")));
            if(CollectionUtils.isNotEmpty(empList)){
                for(Map<String, Object> emp : empList){
                    if(String.valueOf(empId).equals(String.valueOf(emp.get("empId")))){
                        employee.put("emp", emp);
                    }
                }
            }
        }
    }

    //处理转岗数据
    private void buildTransfer(User user, List<Map<String, Object>> employeeList, boolean auditFlag){
        List<User> bzUsers = null;
        List<User> zrbzUsers = null;
        List<User> rsUsers = null;
        List<Integer> empIdList = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        //获取调岗数据
        for(Map<String, Object> employee : employeeList){
            String dataId = String.valueOf(employee.get("dataId"));
            Integer empId = null;
            Integer id = null;
            if(!StringUtils.isEmpty(dataId)){
                String [] arr = dataId.split("_");
                empId = arr.length > 0 ? Integer.parseInt(arr[0]) : null;
                id = arr.length > 1 ? Integer.parseInt(arr[1]) : null;
            }
            if(empId != null){
                empIdList.add(empId);
            }
            if(id != null){
                ids.add(id);
            }
        }
        List<EmployeeTransfer> employeeTransferList = null;
        Map<Integer, List<User>> userMap = null;//公司部门负责人
        if(auditFlag){
            employeeTransferList = employeeTransferMapper.listByEmpId(empIdList);
            List<User> userList = userMapper.listDeptMgrByCompanyCode(user.getCompanyCode());
            if(CollectionUtils.isNotEmpty(userList)){
                userMap = new HashMap<>();
                for(User user1 : userList){
                    if(!userMap.containsKey(user1.getDeptId())){
                        userMap.put(user1.getDeptId(), new ArrayList<>());
                    }
                    userMap.get(user1.getDeptId()).add(user1);
                }
            }
        }else {
            if(CollectionUtils.isNotEmpty(ids)){
                employeeTransferList = employeeTransferMapper.listById(ids);
            }
        }
        //获取员工信息
        List<Map<String, Object>> empList = processMobileMapper.listEmpByEmpIds(empIdList);
        //数据封装
        for(Map<String, Object> employee : employeeList){
            Integer empId = Integer.parseInt(String.valueOf(employee.get("dataId")).split("_")[0]);
            EmployeeTransfer tmpTransfer = null;
            if(CollectionUtils.isNotEmpty(employeeTransferList)){
                for(EmployeeTransfer employeeTransfer : employeeTransferList){
                    if(empId.equals(employeeTransfer.getEmpId())){
                        tmpTransfer = employeeTransfer;
                        employee.put("employee", employeeTransfer);
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(empList)){
                for(Map<String, Object> emp : empList){
                    if(String.valueOf(empId).equals(String.valueOf(emp.get("empId")))){
                        employee.put("emp", emp);
                    }
                }
            }
            if(auditFlag){
                Integer processState = Integer.parseInt(String.valueOf(employee.get("processState")));
                // 个人的下个节点是部长；
                if (processState == IConst.STATE_GR) {
                    if(CollectionUtils.isEmpty(rsUsers)){
                        bzUsers = userService.listLeaderByState(IConst.STATE_BZ);
                    }
                    employee.put("user", bzUsers);
                    if(CollectionUtils.isNotEmpty(bzUsers)){
                        employee.put("deptId", bzUsers.get(0).getDeptId());
                    }
                }
                // 部长审核的下个节点是转入部门的领导；
                if (processState == IConst.STATE_BZ) {
                    if (tmpTransfer != null && userMap != null) {
                        zrbzUsers = userMap.get(tmpTransfer.getAfterDept());
                        employee.put("user", zrbzUsers);
                        if(CollectionUtils.isNotEmpty(zrbzUsers)){
                            employee.put("deptId", zrbzUsers.get(0).getDeptId());
                        }
                    }
                }
                // 转入部门领导审批的下个节点为人事；
                if (processState == IConst.STATE_ZRBZ) {
                    if(CollectionUtils.isEmpty(rsUsers)){
                        rsUsers = userService.listLeaderByState(IConst.STATE_RS);
                    }
                    employee.put("user", rsUsers);
                    if(CollectionUtils.isNotEmpty(rsUsers)){
                        employee.put("deptId", rsUsers.get(0).getDeptId());
                    }
                }
            }
        }
    }

    //处理交接数据
    private void buildConnect(User user, List<Map<String, Object>> employeeList, boolean auditFlag){
        List<User> bzUsers = null;
        List<User> rsUsers = null;
        List<User> cfoUsers = null;
        List<User> ceoUsers = null;
        List<User> rsxzList = null;
        List<User> cwzlList = null;
        List<Integer> empIdList = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        //获取调岗数据
        for(Map<String, Object> employee : employeeList){
            String dataId = String.valueOf(employee.get("dataId"));
            Integer empId = null;
            Integer id = null;
            if(!StringUtils.isEmpty(dataId)){
                String [] arr = dataId.split("_");
                empId = arr.length > 0 ? Integer.parseInt(arr[0]) : null;
                id = arr.length > 1 ? Integer.parseInt(arr[1]) : null;
            }
            if(empId != null){
                empIdList.add(empId);
            }
            if(id != null){
                ids.add(id);
            }
        }
        List<EmployeeConnect> employeeConnectList = null;
        if(auditFlag){
            employeeConnectList = employeeConnectMapper.listByEmpId(empIdList);
        }else {
            if(CollectionUtils.isNotEmpty(ids)){
                employeeConnectList = employeeConnectMapper.listByIds(ids);
            }
        }
        //获取员工信息
        List<Map<String, Object>> empList = processMobileMapper.listEmpByEmpIds(empIdList);
        //数据封装
        for(Map<String, Object> employee : employeeList){
            Integer empId = Integer.parseInt(String.valueOf(employee.get("dataId")).split("_")[0]);
            EmployeeConnect tmpConnect = null;
            if(CollectionUtils.isNotEmpty(employeeConnectList)){
                for(EmployeeConnect employeeConnect : employeeConnectList){
                    if(empId.equals(employeeConnect.getEmpId())){
                        tmpConnect = employeeConnect;
                        employee.put("employee", employeeConnect);
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(empList)){
                for(Map<String, Object> emp : empList){
                    if(String.valueOf(empId).equals(String.valueOf(emp.get("empId")))){
                        employee.put("emp", emp);
                    }
                }
            }
            if(auditFlag){
                Integer processState = Integer.parseInt(String.valueOf(employee.get("processState")));
                // 个人的下个节点是部长；
                if (processState == IConst.STATE_GR) {
                    if(CollectionUtils.isEmpty(rsUsers)){
                        bzUsers = userService.listLeaderByState(IConst.STATE_BZ);
                    }
                    employee.put("user", bzUsers);
                    if(CollectionUtils.isNotEmpty(bzUsers)){
                        employee.put("deptId", bzUsers.get(0).getDeptId());
                    }
                }
                // 部长审核的下个节点是人事部；
                if (processState == IConst.STATE_BZ) {
                    if(CollectionUtils.isEmpty(rsUsers)){
                        rsUsers = userService.listLeaderByState(IConst.STATE_RS);
                    }
                    employee.put("user", rsUsers);
                    if(CollectionUtils.isNotEmpty(rsUsers)){
                        employee.put("deptId", rsUsers.get(0).getDeptId());
                    }
                    if (tmpConnect != null && tmpConnect.getEmpDept() != null) {
                        List<User> userList = userService.listByDeptId(tmpConnect.getEmpDept());
                        // 排除申请人；
                        Integer userId = Integer.parseInt(String.valueOf(employee.get("userId")));
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
                        employee.put("colleague", userList);
                    }
                }
                // 人事部的下个审批节点是财务总监；
                if (processState == IConst.STATE_RS) {
                    if(CollectionUtils.isEmpty(cfoUsers)){
                        cfoUsers  = userService.listLeaderByState(IConst.STATE_CFO);
                    }
                    employee.put("user", cfoUsers);
                    if(CollectionUtils.isNotEmpty(cfoUsers)){
                        employee.put("deptId", cfoUsers.get(0).getDeptId());
                    }
                    if (tmpConnect != null && tmpConnect.getEmpDept() != null) {
                        if(CollectionUtils.isEmpty(rsxzList)){
                            rsxzList = userService.queryRSList(user.getCompanyCode());
                            rsxzList.addAll(userService.queryXZList(user.getCompanyCode()));
                        }
                        employee.put("colleague", rsxzList);
                    }
                }
                // 财务总监的下个审批节点是总经理；
                if (processState == IConst.STATE_CFO) {
                    if(CollectionUtils.isEmpty(ceoUsers)){
                        ceoUsers  = userService.listLeaderByState(IConst.STATE_CEO);
                    }
                    employee.put("user", ceoUsers);
                    if(CollectionUtils.isNotEmpty(ceoUsers)){
                        employee.put("deptId", ceoUsers.get(0).getDeptId());
                    }
                    if (tmpConnect != null && tmpConnect.getEmpDept() != null) {
                        if(CollectionUtils.isEmpty(cwzlList)){
                            cwzlList = userService.queryCWZLInfo(user.getCompanyCode());
                            cwzlList.addAll(userService.queryCWBZInfo(user.getCompanyCode()));
                        }
                        employee.put("colleague", cwzlList);
                    }
                }

            }
        }
    }

    //处理离职数据
    private void buildLeave(List<Map<String, Object>> employeeList, boolean auditFlag){
        List<User> rsUsers = null;
        List<Integer> empIdList = new ArrayList<>();
        for(Map<String, Object> employee : employeeList){
            Integer empId = Integer.parseInt(String.valueOf(employee.get("dataId")));
            if(auditFlag){
                Integer processState = Integer.parseInt(String.valueOf(employee.get("processState")));
                // 部长审核的下个节点是人事；
                if (processState == IConst.STATE_BZ) {
                    if(CollectionUtils.isEmpty(rsUsers)){
                        rsUsers = userService.listLeaderByState(IConst.STATE_RS);
                    }
                    employee.put("user", rsUsers);
                    if(CollectionUtils.isNotEmpty(rsUsers)){
                        employee.put("deptId", rsUsers.get(0).getDeptId());
                    }
                }
            }
            empIdList.add(empId);
        }
        //获取转正数据
        List<EmployeeLeave> employeeLeaveList = employeeLeaveMapper.listByEmpId(empIdList);
        for(Map<String, Object> employee : employeeList){
            Integer empId = Integer.parseInt(String.valueOf(employee.get("dataId")));
            if(CollectionUtils.isNotEmpty(employeeLeaveList)){
                for(EmployeeLeave employeeLeave : employeeLeaveList){
                    if(empId.equals(employeeLeave.getEmpId())){
                        employee.put("employee", employeeLeave);
                    }
                }
            }
        }
        //获取员工信息
        List<Map<String, Object>> empList = processMobileMapper.listEmpByEmpIds(empIdList);
        for(Map<String, Object> employee : employeeList){
            Integer empId = Integer.parseInt(String.valueOf(employee.get("dataId")));
            if(CollectionUtils.isNotEmpty(empList)){
                for(Map<String, Object> emp : empList){
                    if(String.valueOf(empId).equals(String.valueOf(emp.get("empId")))){
                        employee.put("emp", emp);
                    }
                }
            }
        }
    }


    @Override
    public PageInfo<Map<String,Object>> administrativeList(Map<String,Object> map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativeList= processMobileMapper.administrativeList(map);
        return new PageInfo<>(administrativeList);
    }


    @Override
    public PageInfo<Map<String,Object>> administrativeProcessAlready(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativeProcessAlready = processMobileMapper.administrativeProcessAlready(map);
        return new PageInfo<>(administrativeProcessAlready);
    }

    @Override
    public PageInfo<Map<String,Object>> administrativeMeeting(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativeMeeting = processMobileMapper.administrativeMeeting(map);
        if (CollectionUtils.isNotEmpty(administrativeMeeting)){
            for (Map m : administrativeMeeting) {
                List<MeetingUser> list = meetingUserMapper.getUserId(Integer.parseInt(m.get("id").toString()));
                m.put("userList",list);
            }
        }
        return new PageInfo<>(administrativeMeeting);
    }
    @Override
    public PageInfo<Map<String,Object>> administrativeMeetingAlready(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativeMeetingAlready = processMobileMapper.administrativeMeetingAlready(map);
        if (CollectionUtils.isNotEmpty(administrativeMeetingAlready)){
            for (Map m : administrativeMeetingAlready) {
                List<MeetingUser> list = meetingUserMapper.getUserId(Integer.parseInt(m.get("id").toString()));
                m.put("userList",list);
            }
        }
        return new PageInfo<>(administrativeMeetingAlready);
    }

    @Override
    public PageInfo<Map<String,Object>> administrativeSaveBuy(Map<String,Object> map, Pageable pageable ){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativeSaveBuy  =  processMobileMapper.administrativeSaveBuy(map);
        return new PageInfo<>(administrativeSaveBuy);
    }
    @Override
    public PageInfo<Map<String,Object>> administrativeSaveBuyAlready(Map<String,Object> map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativeSaveBuyAlready  =  processMobileMapper.administrativeSaveBuyAlready(map);
        return new PageInfo<>(administrativeSaveBuyAlready);
    }

    @Override
    public PageInfo<Map<String,Object>> administrativeApply(Map<String,Object> map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativeApply  =  processMobileMapper.administrativeApply(map);
        return new PageInfo<>(administrativeApply);

    }

    @Override
    public PageInfo<Map<String,Object>> administrativeApplyAlready(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativeApplyAlready  =  processMobileMapper.administrativeApplyAlready(map);
        return new PageInfo<>(administrativeApplyAlready);
    }

    @Override
    public PageInfo<Map<String,Object>> businessRefundList(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> businessRefundList  =  processMobileMapper.businessRefundList(map);
        return new PageInfo<>(businessRefundList);
    }
    @Override
    public PageInfo<Map<String,Object>>businessRefundListAlready(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> businessRefundListAlready  =  processMobileMapper.businessRefundListAlready(map);
        return new PageInfo<>(businessRefundListAlready);
    }

    @Override
    public PageInfo<Map<String,Object>> businessCommissionList(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> businessCommissionList  =  processMobileMapper.businessCommissionList(map);
        if (CollectionUtils.isNotEmpty(businessCommissionList)){
            for (Map m : businessCommissionList ){
                List<ProjectNode> projectList = processMobileMapper.selectProJect(Integer.parseInt(m.get("id").toString()));
                if (CollectionUtils.isNotEmpty(projectList)){
                    for (ProjectNode p : projectList){
                        if (p!=null){
                            m.put(p.getName(),p.getUserName());
                        }
                    }
                }
            }
        }
        return new PageInfo<>(businessCommissionList);
    }
    @Override
    public PageInfo<Map<String,Object>>businessCommissionListAlready(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> businessCommissionListAlready  =  processMobileMapper.businessCommissionListAlready(map);
        return new PageInfo<>(businessCommissionListAlready);
    }

    @Override
    public PageInfo<Map<String,Object>> administrativePerformance(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativePerformance  =  processMobileMapper.administrativePerformance(map);
        return new PageInfo<>(administrativePerformance);
    }
    @Override
    public PageInfo<Map<String,Object>> administrativePerformanceAlready(Map<String,Object>map,Pageable pageable){
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String,Object>> administrativePerformanceAlready  =  processMobileMapper.administrativePerformanceAlready(map);
        return new PageInfo<>(administrativePerformanceAlready);
    }
}
