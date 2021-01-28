package com.qinfei.qferp.service.impl.employ;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.employ.*;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.excel.EmployeeEasyExcelListener;
import com.qinfei.qferp.excel.EmployeeExcelDTO;
import com.qinfei.qferp.mapper.employ.*;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.PostMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.employ.*;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 入职申请的业务接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:30；
 */
@Service
public class EmployEntryService implements IEmployEntryService {
    // 员工入职申请数据查询接口；
    @Autowired
    private EmployEntryMapper entryMapper;
    // 员工花名册信息接口；
    @Autowired
    private IEmployeeService employeeService;
    // 员工基础信息接口；
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
    // 录用记录的业务接口；
    @Autowired
    private IEmployeeHireService hireService;
    // 员工薪资的业务接口；
    @Autowired
    private IEmployeeSalaryService salaryService;
    // 入职申请的审核记录接口；
    @Autowired
    private IEmployEntryCommentService commentService;
    // 员工轨迹业务接口；
    @Autowired
    private IEmployeeTrajectoryService trajectoryService;
    // 基础资源接口；
    @Autowired
    private IEmployResourceService resourceService;
    // 用户业务接口；
    @Autowired
    private IUserService userService;
    // 流程业务接口；
    @Autowired
    private IProcessService processService;
    // 获取配置；
    @Autowired
    private Config config;

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private EmployEntryMapper employEntryMapper;
    @Autowired
    private EmployeeBasicMapper employeeBasicMapper;
    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmployResourceMapper employResourceMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private EmployEntryFamilyMapper familyMapper;
    @Autowired
    private EmployEntryExperienceMapper employEntryExperienceMapper;
    @Autowired
    private EmployeeSalaryMapper employeeSalaryMapper;
    @Autowired
    private EmployEntryEducationMapper employEntryEducationMapper;

    /**
     * 保存或更新单条记录到数据库中；
     *
     * @param record：入职申请对象；
     * @param empCode：身份证号码；
     * @return ：处理完毕的入职申请对象；
     */
    @Override
    public EmployEntry saveOrUpdate(EmployEntry record, String empCode,String entryCompanyCode) {
        // 部分关键信息不允许修改；
        record.setEntryCodeFile(null);
        record.setEntryResidenceFile(null);
        record.setEntryReport(null);
        record.setEntryFile(null);
        record.setEntryExpectDate(null);
        record.setEmpId(null);
        record.setTaskId(null);
        record.setItemId(null);
        record.setProcessState(null);

        Integer entryId = record.getEntryId();
        // 先检查库中是否有离职数据，离职再入职；
        if (entryId == null && !StringUtils.isEmpty(empCode)) {
            entryId = entryMapper.selectIdByCode(empCode);
            record.setEntryId(entryId);
        }
        // 先进行校验，防止并发，如果是更新数据且更新内容没有身份证信息可以通过；
        if ((StringUtils.isEmpty(empCode) && entryId != null) || checkRepeatByCode(entryId, empCode, entryCompanyCode)) {
            if (entryId == null) {
                // 生成查询码；
                record.setEntryValidate(PrimaryKeyUtil.getStringUniqueKey());
                // 默认无亲友在公司；
                record.setEntryHasRelative(IEntryRelative.RELATIVE_NO);
                // 默认资料是不完整的；
                record.setEntryComplete(IEntryComplete.ENTRY_DEFORM);
                record.setState(IEmployEntry.ENTRY_PENDING);

                // 设置创建人信息；
                record.setCreateInfo();

                entryMapper.insertSelective(record);
            } else {
                // 确认状态允许更新；V
                if (checkEnableUpdate(entryId)) {
                    // 设置更新人信息；
                    record.setUpdateInfo();
                    // 获取查询码；
                    String entryValidate = record.getEntryValidate();
                    // 查询码不允许修改；
                    record.setEntryValidate(null);
                    // 资料完成度不允许修改；
                    record.setEntryComplete(null);
                    entryMapper.updateByPrimaryKeySelective(record);
                    // 重新封装回属性；
                    record.setEntryValidate(entryValidate);
                }
            }
        }
        return record;
    }

    /**
     * 保存入职申请的基本信息；
     *
     * @param data：返回给前端的数据；
     * @param entry：提交的申请字符串；
     */
    @Override
    @Transactional
    public void saveBasic(ResponseData data, String entry) {
        // 解析对象；
        JSONObject json = JSON.parseObject(entry);
        // 封装属性；
        EmployEntry employEntry = (EmployEntry) EntityUtil.getNewObject(json, EmployEntry.class);
        EmployeeBasic employeeBasic = (EmployeeBasic) EntityUtil.getNewObject(json, EmployeeBasic.class);

        // 亲友不允许前端传值；
        employEntry.setEntryHasRelative(null);
        // 新增或更新到数据库；
        saveOrUpdate(employEntry, employeeBasic.getEmpCode(), employEntry.getEntryCompanyCode());
        // 关联数据；
        employeeBasic.setEntryId(employEntry.getEntryId());
        basicService.saveOrUpdate(employeeBasic);

        // 返回给前端；
        data.putDataValue("entryId", employEntry.getEntryId());
        data.putDataValue("entryValidate", employEntry.getEntryValidate());
        data.putDataValue("basId", employeeBasic.getBasId());
        // 查询状态，用于判断是否为离职在入职的员工，限制其求职渠道；
        data.putDataValue("state", employeeService.checkStateByCode(employeeBasic.getEmpCode()));
    }

    /**
     * 保存或更新录用记录；
     *
     * @param data：返回给前端的数据；
     * @param employeeHire：录用记录对象；
     */
    @Override
    public void saveHire(ResponseData data, EmployeeHire employeeHire) {
        hireService.saveOrUpdate(employeeHire);
        data.putDataValue("hireId", employeeHire.getHireId());
    }

    /**
     * 保存或更新薪资记录；
     *
     * @param data：返回给前端的数据；
     * @param employeeSalary：薪资对象；
     */
    @Override
    public void saveSalary(ResponseData data, EmployeeSalary employeeSalary) {
        salaryService.saveOrUpdate(employeeSalary);
        data.putDataValue("salId", employeeSalary.getSalId());
    }

    /**
     * 保存或更新薪资记录；
     *
     * @param data：返回给前端的数据；
     * @param entryComment：评估信息对象；
     * @param operate：操作类型，0为人事，1为测试；
     */
    @Override
    public void saveComment(ResponseData data, EmployEntryComment entryComment, int operate) {
        // 限制只能输入0或1；
        if (operate == 0 || operate == 1) {
            // 设置类型；
            entryComment.setComType(operate);
            commentService.saveOrUpdate(entryComment);
            data.putDataValue("comId", entryComment.getComId());
        }
    }

    /**
     * 保存入职申请的家庭婚姻信息；
     *
     * @param data：返回给前端的数据；
     * @param entryFamily：提交的家庭信息对象；
     */
    @Override
    public void saveFamily(ResponseData data, EmployEntryFamily entryFamily) {
        familyService.saveOrUpdate(entryFamily);
        data.putDataValue("family", familyService.selectByEntryId(entryFamily.getEntryId()));
        data.putDataValue("famId", entryFamily.getFamId());
    }

    /**
     * 保存入职申请的教育培训经历信息；
     *
     * @param data：返回给前端的数据；
     * @param entryEducation：提交的教育培训经历信息对象；
     */
    @Override
    public void saveEducation(ResponseData data, EmployEntryEducation entryEducation) {
        educationService.saveOrUpdate(entryEducation);
        data.putDataValue("education", educationService.selectByEntryId(entryEducation.getEntryId()));
        data.putDataValue("eduId", entryEducation.getEduId());
    }

    /**
     * 保存入职申请的工作经历信息；
     *
     * @param data：返回给前端的数据；
     * @param entryExperience：提交的工作经历信息对象；
     */
    @Override
    public void saveExperience(ResponseData data, EmployEntryExperience entryExperience) {
        experienceService.saveOrUpdate(entryExperience);
        data.putDataValue("experience", experienceService.selectByEntryId(entryExperience.getEntryId()));
        data.putDataValue("expId", entryExperience.getExpId());
    }

    @Override
    public void saveExperienceInJob(ResponseData data, EmployEntryExperience entryExperience) {
        experienceService.saveOrUpdateInJob(entryExperience);
    }

    /**
     * 保存入职申请的其他入职信息；
     *
     * @param data：返回给前端的数据；
     * @param employEntry：提交的其他入职信息对象；
     * @param empRelative：推荐人ID；
     * @param empRelativeName：推荐人姓名；
     * @param empRelativePhone：推荐人联系电话；
     * @param empRelativeRelation：与推荐人的关系；
     */
    @Override
    @Transactional
    public void saveOther(ResponseData data, EmployEntry employEntry, Integer empRelative, String empRelativeName, String empRelativePhone, String empRelativeRelation) {
        empRelative = selectRelative(empRelativeName, empRelativePhone);
        // 如果推荐人不为空；
        if (empRelative != null) {
            EmployeeBasic employeeBasic = new EmployeeBasic();
            employeeBasic.setEntryId(employEntry.getEntryId());
            employeeBasic.setEmpRelative(empRelative);
            employeeBasic.setEmpRelativeName(empRelativeName);
            employeeBasic.setEmpRelativePhone(empRelativePhone);
            employeeBasic.setEmpRelativeRelation(empRelativeRelation);

            // 更新推荐人信息；
            basicService.updateRelativeByParentId(employeeBasic);

            // 设置亲友信息；
            employEntry.setEntryHasRelative(IEntryRelative.RELATIVE_YES);
        }
        saveOrUpdate(employEntry, null,null);
        data.putDataValue("entryId", employEntry.getEntryId());
    }

    /**
     * 提交审核入职审核流程；
     *
     * @param data：返回给前端的数据；
     * @param entryId：入职申请的ID；
     * @param userId：审核人ID；
     * @param userName：审核人姓名；
     * @param deptId：审核人所在部门；
     */
    @Override
    public void startEmploy(ResponseData data, int entryId, int userId, String userName, int deptId) {
        EmployEntry employEntry = entryMapper.selectTaskById(entryId);
        if (employEntry == null) {
            data.putDataValue("message", "入职申请不存在或已在审核中。");
        } else {
            processService.addEmployProcess(employEntry, Const.ITEM_J3, userId, userName, deptId);
            data.putDataValue("message", "操作完成。");
        }
    }

    /**
     * 入职审批通过后，通知申请者；
     *
     * @param data：返回给前端的数据；
     * @param entryId：入职申请的ID；
     */
    @Override
    public void noticeEntry(ResponseData data, int entryId) {
        EmployEntry entry = new EmployEntry();
        entry.setEntryId(entryId);
        entry.setEntryValidate(PrimaryKeyUtil.getStringUniqueKey());
        entry.setUpdateInfo();
        entryMapper.updateByPrimaryKeySelective(entry);

        // TODO: 2019-3-31 0031 增加通知消息的模块，例如邮件或短信；

        data.putDataValue("message", "操作完成。");
    }

    /**
     * 通用的审核流转；
     *
     * @param data：返回给前端的数据；
     * @param approveData：审核信息Json字符串；
     */
    @Override
    public void completeApprove(ResponseData data, String approveData) {
        // 解析对象；
        JSONObject json = JSON.parseObject(approveData);
        // 确认有审核人提交过来；
        Object userId = json.get("userId");
        if (userId == null) {
            data.putDataValue("message", "审核人不能为空。");
        } else {
            String[] taskIds = new String[]{json.getString("taskId")};
            processService.approveProcess(taskIds, json.getString("desc"), json.getBoolean("agree"), json.getInteger("userId"), json.getString("userName"), json.getInteger("deptId"));
            data.putDataValue("message", "操作完成。");
        }
    }

    /**
     * 批量更新数据的状态；
     *
     * @param entryIds：入职申请ID数组；
     * @param operate：操作类型，0为删除，1为存档备用（不予考虑），2为恢复，3为离职再入职；
     * @return ：操作结果提示信息，0为异常，1为操作成功；
     */
    @Override
    public int updateStateByBatchId(Integer[] entryIds, int operate) {
        Integer state = null;
        String entryValidate = null;
        switch (operate) {
            case 0:
                state = IEmployEntry.ENTRY_DELETE;
                break;
            case 1:
                state = IEmployEntry.ENTRY_ARCHIVE;
                // 0为清空查询码；
                entryValidate = "0";
                break;
            case 2:
                state = IEmployEntry.ENTRY_PENDING;
                entryValidate = PrimaryKeyUtil.getStringUniqueKey();
                break;
            case 3:
                state = IEmployEntry.ENTRY_PENDING;
                entryValidate = PrimaryKeyUtil.getStringUniqueKey();
                break;
            default:
                break;
        }
        // 过滤不符合要求的参数；
        if (state == null) {
            return 0;
        } else {
            Map<String, Object> params = new HashMap<>();
            // 此处有清空状态操作，请注意提前设置；
            EntityUtil.setUpdateInfo(params);
            params.put("entryIds", Arrays.asList(entryIds));
            params.put("state", state);
            if (!StringUtils.isEmpty(entryValidate)) {
                params.put("entryValidate", entryValidate);
            }
            entryMapper.updateStateByBatchId(params);
            return 1;
        }
    }

    /**
     * 根据员工ID更新数据的状态；
     *
     * @param empId：员工ID；
     * @param operate：操作类型，0为删除，1为离职；
     * @return ：操作影响的记录数；
     */
    @Override
    public int updateStateByEmpId(int empId, int operate) {
        Integer state = null;
        switch (operate) {
            case IEmployee.EMPLOYEE_DELETE:
                state = IEmployEntry.ENTRY_DELETE;
                break;
            case IEmployee.EMPLOYEE_LEAVE:
                state = IEmployEntry.ENTRY_LEAVED;
                break;
            default:
                break;
        }
        // 过滤不符合要求的参数；
        if (state == null) {
            return 0;
        } else {
            EmployEntry employEntry = new EmployEntry();
            // 此处有清空状态操作，请注意提前设置；
            employEntry.setUpdateInfo();
            employEntry.setEmpId(empId);
            employEntry.setState(state);
            return entryMapper.updateStateByEmpId(employEntry);
        }
    }

    /**
     * 更新创建人信息；
     *
     * @param entryId：主键ID；
     * @param userId：创建人ID；
     * @param userName：创建人名称；
     * @return ：操作影响的记录数；
     */
    @Override
    public int updateCreateId(int entryId, int userId, String userName) {
        EmployEntry employEntry = new EmployEntry();
        employEntry.setEntryId(entryId);
        employEntry.setUpdateInfo();
        employEntry.setCreateId(userId);
        employEntry.setCreateName(userName);
        return entryMapper.updateCreateInfoById(employEntry);
    }

    /**
     * 根据主键ID完善资料；
     *
     * @param jsonData：数据Json对象；
     * @return ：操作结果提示信息；
     */
    @Override
    @Transactional
    public String completeEntry(JSONObject jsonData) {
        // 确定有查询码传递过来；
        if (jsonData.get("entryId") == null && jsonData.get("entryValidate") == null) {
            return "无权限操作。";
        } else {
            Integer entryId;
            if (jsonData.get("entryId") == null) {
                entryId = entryMapper.selectIdByValidate(jsonData.getString("entryValidate"));
            } else {
                entryId = jsonData.getInteger("entryId");
            }
            if (entryId == null) {
                return "查询无结果，入职申请可能不存在或审核未通过。";
            } else {
                // 检查状态；
                if (checkEnableComplete(entryId, IEntryComplete.ENTRY_DEFORM)) {
                    // 判断是否有文件；
                    boolean hasFile = false;
                    // 更新学历和离职证明；
                    if (!(StringUtils.isEmpty(jsonData.get("empEducationFile")) && StringUtils.isEmpty(jsonData.get("empExperienceFile")))) {
                        EmployeeBasic employeeBasic = (EmployeeBasic) EntityUtil.getNewObject(jsonData, EmployeeBasic.class);
                        employeeBasic.setEntryId(entryId);
                        basicService.completeEntryByParentId(employeeBasic);
                        // 有文件；
                        hasFile = true;
                    }

                    // 更新入职申请；
                    EmployEntry employEntry = null;
                    // 如果有文件则获取，否则实例化空对象；
                    if (!(StringUtils.isEmpty(jsonData.get("entryCodeFile")) && StringUtils.isEmpty(jsonData.get("entryResidenceFile")) && StringUtils.isEmpty(jsonData.get("entryImage")) && StringUtils.isEmpty(jsonData.get("entryReport")))) {
                        employEntry = (EmployEntry) EntityUtil.getNewObject(jsonData, EmployEntry.class);
                    } else {
                        if (hasFile) {
                            employEntry = new EmployEntry();
                        }
                    }
                    if (employEntry != null) {
                        employEntry.setEntryId(entryId);
                        employEntry.setUpdateInfo();

                        entryMapper.completeEntryById(employEntry);
                    }
                    return "操作完成。";
                } else {
                    return "该入职申请正在审核中，请勿重复提交资料。";
                }
            }
        }
    }

    /**
     * 设置入职申请的教育培训经历信息的最高学历；
     *
     * @param data：返回给前端的数据；
     * @param entryId：提交的入职申请ID；
     * @param eduId：提交的教育培训经历信息ID；
     * @param eduCollege：提交的教育培训经历学校名称；
     * @param eduMajor：提交的教育培训经历专业名称；
     */
    @Override
    public void setEducationHighest(ResponseData data, int entryId, int eduId, String eduCollege, String eduMajor) {
        educationService.setEducationHighest(entryId, eduId, eduCollege, eduMajor);
        data.putDataValue("education", educationService.selectByEntryId(entryId));
    }

    /**
     * 删除入职申请的家庭婚姻信息；
     *
     * @param entryId：提交的入职申请ID；
     * @param famId：提交的家庭信息对象ID；
     */
    @Override
    public void deleteFamily(int entryId, int famId) {
        familyService.deleteByPrimaryKeyAndParentId(entryId, famId);
    }

    /**
     * 删除入职申请的教育培训经历信息；
     *
     * @param entryId：提交的入职申请ID；
     * @param eduId：提交的教育培训经历信息ID；
     */
    @Override
    public void deleteEducation(int entryId, int eduId) {
        educationService.deleteByPrimaryKeyAndParentId(entryId, eduId);
    }

    /**
     * 删除入职申请的工作经历信息；
     *
     * @param entryId：提交的入职申请ID；
     * @param expId：提交的工作经历信息ID；
     */
    @Override
    public void deleteExperience(int entryId, int expId) {
        experienceService.deleteByPrimaryKeyAndParentId(entryId, expId);
    }

    /**
     * 入职申请的资料审核；
     *
     * @param record：入职申请的审核信息；
     * @param operate：操作类型，0为同意，1为拒绝；
     */
    @Override
    @Transactional
    public void approveEntry(EmployEntryComment record, int operate) {
        // 先查询关联的入职审核的主键ID；
        record.setComType(IEmployComment.COMMENT_PERSONAL);
        Integer entryId = record.getEntryId();
        Integer state = null;
        String empNumLeave = null;
        // 此处为人事审核入职人员提交的申请材料，因此一定会有之前的审核记录，做更新即可；
        if (entryId != null) {
            // 更新审核的数据信息；
            commentService.saveOrUpdate(record);

            // 更新入职申请；
            EmployEntry employEntry = new EmployEntry();

            employEntry.setEntryId(entryId);
            // 如果同意则需要获取入职申请的信息，在员工花名册，获取ID之后需要更新入职申请表的员工ID；
            if (operate == 0) {
                Map<String, Object> params = new HashMap<>();
                params.put("entryId", entryId);
                Map<String, Object> entryInfo = entryMapper.selectEntryInfo(params);
                // 先查询确定入职申请数据存在；
                if (entryInfo != null && !entryInfo.isEmpty()) {
                    Employee employee = new Employee();
                    // 构建对象；
                    EmployEntry oldEntry = (EmployEntry) EntityUtil.getNewObject(entryInfo, EmployEntry.class);
                    employee.setEmpName(oldEntry.getEntryName());
                    employee.setEmpDept(oldEntry.getEntryDept());
                    employee.setEmpProfession(oldEntry.getEntryProfession());
                    employee.setEmpPhone(oldEntry.getEntryPhone());
                    employee.setEmpMarriage(oldEntry.getEntryMarriage());
                    employee.setEmpUrgent(oldEntry.getEntryUrgent());
                    employee.setEmpUrgentPhone(oldEntry.getEntryUrgentPhone());
                    employee.setEntryId(entryId);
                    // 获取基础信息对象；
                    Object empBirth = entryInfo.get("empBirth");
                    if (empBirth != null) {
                        employee.setEmpAge(DateUtils.getAgeByBirthday((Date) empBirth));
                    }
                    if("1".equals(String.valueOf(entryInfo.get("entryState")))){
                        employee.setState(IEmployee.EMPLOYEE_INTERNSHIP); //实习
                    }else {
                        employee.setState(IEmployee.EMPLOYEE_PROBATION);  //试用
                    }
                    state = employee.getState();
                    // 获取身份证号码；
                    Object empCodeObject = entryInfo.get("empCode");
                    String empCode = empCodeObject == null ? null : empCodeObject.toString();
                    employeeService.saveOrUpdate(employee, empCode);

                    // 更新入职申请的日期；
                    EmployeeBasic employeeBasic = new EmployeeBasic();
                    employeeBasic.setEntryId(entryId);
                    employeeBasic.setEmpDate(new Date());
                    basicService.updateEmpDateByParentId(employeeBasic);

                    // 获取员工信息；
                    Integer empId = employee.getEmpId();
                    String empNum = employee.getEmpNum();
                    empNumLeave = empNum;
                    // 更新关联的录用记录的工号；
                    hireService.updateHireNum(entryId, empId, empNum);

                    // 更新关联的薪资记录的工号；
                    salaryService.updateSalaryNum(entryId, empId, empNum);

                    // 获取关联的数据ID集合；
                    // 更新关联的轨迹记录的员工ID和工号；
                    params = new HashMap<>();
                    params.put("empId", empId);
                    params.put("empNum", empNum);
                    params.put("entryId", entryId);
                    trajectoryService.updateByIds(params);

                    // 获取员工ID；
                    employEntry.setEmpId(empId);
                    // 清空查询码；
                    employEntry.setEntryValidate("0");
                    // 更新状态；
                    employEntry.setState(IEmployEntry.ENTRY_EMPLOY);

                }
                // 拒绝则更新入职申请的资料完成度为0；
            } else {
                employEntry.setEntryComplete(IEntryComplete.ENTRY_DEFORM);
            }
            //员工离职再入职状态更新标记
            employeeService.updateEmployeeState(empNumLeave,state);
            entryMapper.updateByPrimaryKeySelective(employEntry);
        }
    }

    /**
     * 入职申请的流程更新状态；
     *
     * @param entryId：主键ID；
     * @param code：当前使用的查询码；
     * @param state：当前状态；
     * @param taskId：任务ID；
     * @param itemId：待办事项ID；
     * @param agree：是否同意；
     * @param desc：审核意见；
     */
    @Override
    @Transactional
    public void processEntry(int entryId, String code, int state, String taskId, Integer itemId, boolean agree, String desc) {
        EmployEntry employEntry = new EmployEntry();
        employEntry.setEntryId(entryId);
        employEntry.setProcessState(state);
        // 更新流程当前的任务ID；
        employEntry.setTaskId(taskId);
        // 更新待办事项的ID；
        employEntry.setItemId(itemId);

        // 轨迹内容；
        String content = null;
        // 操作类型；
        int operate = 0;
        // 设置更新信息；
        employEntry.setUpdateInfo();
        // 根据流程状态来更新入职的状态；
        switch (state) {
            // 拒绝的流程状态改为待审核；
            case IConst.STATE_REJECT:
                employEntry.setState(IEmployEntry.ENTRY_PENDING);
                content = "提交的入职申请被拒绝";
                operate = ITrajectoryOperate.OPERATE_REJECT;

                // 清空查询码；
                employEntry.setEntryValidate("0");
                break;
            // 审核完成，更新状态为同意录用；
            case IConst.STATE_FINISH:
                employEntry.setState(IEmployEntry.ENTRY_AGREE);
                content = "入职申请审核通过，成为正式员工";
                operate = ITrajectoryOperate.OPERATE_PASS;

                // 清空查询码；
                employEntry.setEntryValidate("0");
                break;
            // 提交流程；
            case IConst.STATE_BZ:
                employEntry.setState(IEmployEntry.ENTRY_APPROVE);
                content = "提交入职申请";
                operate = ITrajectoryOperate.OPERATE_SUBMIT;
                employEntry.setEntryValidate(code);
                break;
            default:
                // 审核中的流程状态保持不变；
                employEntry.setState(IEmployEntry.ENTRY_APPROVE);
                employEntry.setEntryValidate(code);
                break;
        }

        // 判断是否为部长节点的操作，查询上个节点是否为部长；
        Integer processState = entryMapper.selectApproveNode(entryId);
        // 判断是否部长节点；
        if (processState != null && processState.intValue() == IConst.STATE_BZ) {
            commentService.saveLeaderComment(entryId, IEmployComment.COMMENT_LEADER, agree ? 0 : 1, desc);
        }

        // 更新关联的录用记录的状态；
        hireService.updateHireState(entryId, employEntry.getState());

        // 更新数据；
        entryMapper.updateByPrimaryKeySelective(employEntry);

        // 添加员工轨迹记录；
        if (!StringUtils.isEmpty(content)) {
            EmployeeHire record = hireService.selectByParentId(entryId);
            EmployeeTrajectory trajectory = trajectoryService.getTrajectory(record);
            trajectory.setEntryId(entryId);
            trajectory.setEmpContent(content);
            trajectory.setEmpTransaction(ITrajectoryTransaction.TRANSACTION_EMPLOY);
            trajectory.setEmpOperate(operate);
            trajectoryService.saveOrUpdate(trajectory);
        }
    }

    /**
     * 根据主键ID查询该数据是否为待审核状态；
     *
     * @param entryId：主键ID；
     * @return ：查询结果，true为验证通过，false为验证失败；
     */
    @Override
    public boolean checkEnableUpdate(int entryId) {
        return entryMapper.checkEnableUpdate(entryId) == 1;
    }

    /**
     * 根据主键ID查询该数据时否为审核中状态；
     *
     * @param entryId：主键ID；
     * @return ：统计数量；
     */
    @Override
    public boolean checkEnableApprove(int entryId) {
        return entryMapper.checkEnableApprove(entryId) == 1;
    }

    /**
     * 根据主键ID和状态查询该数据是否进入到资料审核阶段；
     *
     * @param entryId：主键ID；
     * @param entryComplete：是否资料完整0 或 1；
     * @return 查询结果，true为验证通过，false为验证失败；
     */
    @Override
    public boolean checkEnableComplete(int entryId, int entryComplete) {
        EmployEntry employEntry = new EmployEntry();
        employEntry.setEntryId(entryId);
        employEntry.setEntryComplete(entryComplete);
        return entryMapper.checkEnableComplete(employEntry) == 1;
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
            return (!StringUtils.isEmpty(empCode)) && entryMapper.checkRepeatByCode(params) == 0 && employeeService.checkRepeatByCode(entryId, empCode, entryCompanyCode);
        }
    }

    /**
     * 根据推荐人信息获取关联的用户ID；
     *
     * @param empRelativeName：推荐人姓名；
     * @param empRelativePhone：推荐人联系电话；
     * @return ：推荐人的用户ID；
     */
    @Override
    public Integer selectRelative(String empRelativeName, String empRelativePhone) {
        List<User> users = userService.selectUserId(empRelativeName, empRelativePhone);
        if (users.size() == 1) {
            return users.get(0).getId();
        } else {
            return null;
        }
    }

    /**
     * 查询发起录用流程所需的信息；
     *
     * @param entryId：主键ID；
     * @return ：查询结果的封装对象；
     */
    @Override
    public EmployEntry selectTaskById(int entryId) {
        return entryMapper.selectTaskById(entryId);
    }

    /**
     * 查询入职申请的创建信息；
     *
     * @param empId：员工ID；
     * @return ：入职申请对象；
     */
    @Override
    public EmployEntry selectCreateInfoById(int empId) {
        return entryMapper.selectCreateInfoById(empId);
    }

    /**
     * 根据查询码获取入职申请的ID和状态；
     *
     * @param entryValidate：查询码；
     * @return ：入职申请对象；
     */
    @Override
    public EmployEntry selectApproveInfo(String entryValidate) {
        return entryMapper.selectApproveInfo(entryValidate);
    }

    /**
     * 根据提供的姓名、身份证号码、联系电话找回查询码；
     *
     * @param entryName：姓名；
     * @param entryPhone：联系电话；
     * @param empCode：身份证号码；
     * @return ：入职申请的查询码；
     */
    @Override
    public String selectEntryValidate(String entryName, String entryPhone, String empCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("entryName", entryName);
        params.put("entryPhone", entryPhone);
        params.put("empCode", empCode);
        return entryMapper.selectEntryValidate(params);
    }

    /**
     * 获取入职申请的信息；
     *
     * @param params：查询参数集合；
     * @return ：入职申请信息；
     */
    @Override
    public Map<String, Object> getEntryInfo(Map<String, Object> params) {
        Map<String, Object> entryInfo = entryMapper.selectEntryInfo(params);
        if (entryInfo != null) {
            int entryId = Integer.parseInt(entryInfo.get("entryId").toString());
            // 获取子表的数据；
            entryInfo.put("family", familyService.selectByEntryId(entryId));
            entryInfo.put("education", educationService.selectByEntryId(entryId));
            entryInfo.put("experience", experienceService.selectByEntryId(entryId));

            // 获取基础信息数据；
            // 部门；
            Object resourceData = entryInfo.get("entryDept");
            if (resourceData != null) {
                entryInfo.put("dept", resourceService.getDeptNameById(Integer.parseInt(resourceData.toString())));
            }
            // 职位；
            resourceData = entryInfo.get("entryProfession");
            if (resourceData != null) {
                entryInfo.put("post", resourceService.getPostNameById(Integer.parseInt(resourceData.toString())));
            }
            // 民族；
            resourceData = entryInfo.get("empRace");
            if (resourceData != null) {
                entryInfo.put("nation", resourceService.getNationNameById(Integer.parseInt(resourceData.toString())));
            }

            // 如果查询参数使用的是主键，查询关联的审核记录；
            Object object = params.get("entryId");
            if (object != null) {
                entryInfo.put("comment", commentService.selectEmployInfo(entryId));
                entryInfo.put("file", entryMapper.selectEntryFileById(entryId));
            }
        }
        return entryInfo;
    }

    /**
     * 分页查询入职申请信息；
     *
     * @param params：查询参数；
     * @param pageable：分页参数；
     * @return ：查询的入职申请信息集合；
     */
    @Override
    public PageInfo<Map<String, Object>> selectPageEntry(Map<String, Object> params, Pageable pageable) {
        // 获取所有的部门数据；
        Map<Integer, String> allDept = resourceService.listAllDept();
        // 获取所有的职位数据；
        Map<Integer, String> allPost = resourceService.listAllPost();
        // 获取所有的民族数据；
        Map<Integer, String> allNation = resourceService.listAllNation();
        // 公司代码过滤；
        params.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String, Object>> pageEntry = entryMapper.selectPageEntry(params);
        for (Map<String, Object> entry : pageEntry) {
            entry.put("deptName", allDept.get(entry.get("entry_dept")));
            entry.put("postName", allPost.get(entry.get("entry_profession")));
            entry.put("raceName", allNation.get(entry.get("emp_race")));
        }
        return new PageInfo<>(pageEntry);
    }

    /**
     * 查询入职申请的状态；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请的状态；
     */
    @Override
    public EmployEntry selectStateById(int entryId) {
        return entryMapper.selectStateById(entryId);
    }

    /**
     * 根据入职申请ID查询入职申请相关的基本信息；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请的基本信息；
     */
    @Override
    public Map<String, Object> selectEmployInfoById(int entryId) {
        Map<String, Object> employInfo = entryMapper.selectEmployInfoById(entryId);
        Object object = employInfo.get("empDept");
        if (object != null) {
            employInfo.put("empDeptName", resourceService.getDeptNameById(Integer.parseInt(object.toString())));
        }
        object = employInfo.get("empProfession");
        if (object != null) {
            employInfo.put("empProfessionName", resourceService.getPostNameById(Integer.parseInt(object.toString())));
        }
        return employInfo;
    }

    /**
     * 根据入职申请ID查询入职申请相关的文件信息；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请的文件集合；
     */
    @Override
    public Map<String, Object> selectEntryFileById(int entryId) {
        Map<String, Object> entryData = entryMapper.selectEntryFileById(entryId);
        EmployEntryComment entryComment = new EmployEntryComment();
        entryComment.setEntryId(entryId);
        entryComment.setComType(IEmployComment.COMMENT_PERSONAL);
        entryData.put("comment", commentService.selectByParentId(entryComment));
        return entryData;
    }

    /**
     * 入职申请信息导出；
     *
     * @param entryId：主键ID；
     * @return ：文件下载路径；
     */
    @Override
    public String exportEntryData(int entryId) {
        Map<String, Object> params = new HashMap<>();
        params.put("entryId", entryId);
        Map<String, Object> entryInfo = getEntryInfo(params);
        List<Dept> deptList = deptMapper.listJTAllCompany(null);
        if(!CollectionUtils.isEmpty(deptList)){
            for(Dept dept : deptList){
                if(String.valueOf(entryInfo.get("entryCompanyCode")).equalsIgnoreCase(dept.getCode())){
                    entryInfo.put("entryCompanyName", dept.getName());
                    break;
                }
            }
        }
        //如果没有选择第二部门，则展示第一部门
        if(entryInfo.get("dept") == null){
            String firstDept = "";
            if("0".equalsIgnoreCase(String.valueOf(entryInfo.get("entryFirstDept")))){
                firstDept = "业务部门";
            }else if("1".equalsIgnoreCase(String.valueOf(entryInfo.get("entryFirstDept")))){
                firstDept = "媒介部门";
            }else {
                firstDept = "其他部门";
            }
            entryInfo.put("dept", firstDept);
        }
        if (entryInfo == null) {
            return null;
        } else {
            return DataImportUtil.createFile("入职申请详情", config.getUploadDir(), config.getWebDir(), entryInfo);
        }
    }

    /**
     * 入职申请信息导出；
     *
     * @param params：查询参数；
     * @return ：文件下载路径；
     */
    @Override
    public String exportEntryData(Map<String, Object> params) {
        // 获取查询结果；
        List<Map<String, Object>> entryList = entryMapper.selectPageEntry(params);
        int size = entryList.size();
        if (size > 0) {
            // 拼接表头显示信息；
            List<String> rowTitles = new ArrayList<>();
            rowTitles.add("一级部门");
            rowTitles.add("申请部门");
            rowTitles.add("申请职位");
            rowTitles.add("待遇要求");
            rowTitles.add("姓名");
            rowTitles.add("农历生日");
            rowTitles.add("性别");
            rowTitles.add("民族");
            rowTitles.add("身份证号码");
            rowTitles.add("户口性质");
            rowTitles.add("婚姻状况");
            rowTitles.add("身份证地址");
            rowTitles.add("籍贯");
            rowTitles.add("现住址");
            rowTitles.add("学历");
            rowTitles.add("个人邮箱");
            rowTitles.add("个人电话");
            rowTitles.add("紧急事件联络人");
            rowTitles.add("有效联系电话");
            rowTitles.add("与联络人的关系");
            rowTitles.add("工作经历");
            rowTitles.add("是否有驾照");
            rowTitles.add("驾照");
            rowTitles.add("司龄（年）");
            rowTitles.add("曾经病史");
            rowTitles.add("病史描述");
            rowTitles.add("求职途径");
            rowTitles.add("求职渠道名称");
            rowTitles.add("推荐人姓名");
            rowTitles.add("推荐人联系电话");
            rowTitles.add("与推荐人关系");
            rowTitles.add("兴趣爱好特长");
            rowTitles.add("申请日期");
            rowTitles.add("状态");

            // 获取部门和职位的数据；
            Map<Integer, String> allDept = resourceService.listAllDept();
            Map<Integer, String> allPost = resourceService.listAllPost();
            // 获取所有的民族数据；
            Map<Integer, String> allNation = resourceService.listAllNation();

            // 基础数据和变量；
            String[] entryFirstDept = new String[]{"业务部", "媒介部", "其他部门"};
            String[] marriage = new String[]{"未婚", "已婚", "离婚", "丧偶"};
            String[] education = new String[]{"初中", "高中", "专科", "本科", "硕士", "博士", "博士后","其他","小学"};
            String[] channel = new String[]{"BOSS直聘", "社交媒体", "离职再入职", "人才市场", "校园招聘", "猎头推荐", "内部推荐 ", "其他", "前程无忧", "智联招聘", "分子公司调岗"};
            String[] state = new String[]{"待审核", "审核中", "同意录用", "已入职", "不予考虑", "已离职"};
            Object object;
            int intValue;

            // 处理数据；
            List<Object[]> exportData = new ArrayList<>();
            Object[] datas;
            // 获取数据长度；
            int dataLength = rowTitles.size();
            for (Map<String, Object> data : entryList) {
                datas = new Object[dataLength];
                if(data.get("entry_first_dept") != null){
                    datas[0] = entryFirstDept[Integer.parseInt(String.valueOf(data.get("entry_first_dept")))];
                }else {
                    datas[0] = data.get("entry_first_dept");
                }
                datas[1] = allDept.get(data.get("entry_dept"));
                datas[2] = allPost.get(data.get("entry_profession"));
                datas[3] = data.get("entry_expect_salary");
                datas[4] = data.get("entry_name");
                datas[5] = data.get("emp_birthday");
                object = data.get("emp_gender");
                if (object == null) {
                    datas[6] = "";
                } else {
                    intValue = Integer.parseInt(object.toString());
                    if (intValue == 0) {
                        datas[6] = "女";
                    } else {
                        datas[6] = "男";
                    }
                }
                datas[7] = allNation.get(data.get("emp_race"));
                datas[8] = data.get("emp_code");
                object = data.get("entry_residence");
                if (object == null) {
                    datas[9] = "";
                } else {
                    intValue = Integer.parseInt(object.toString());
                    if (intValue == 0) {
                        datas[9] = "城镇户口";
                    } else {
                        datas[9] = "农村户口";
                    }
                }
                object = data.get("entry_marriage");
                if (object == null) {
                    datas[10] = "";
                } else {
                    intValue = Integer.parseInt(object.toString());
                    if (intValue >= 0 && intValue < marriage.length) {
                        datas[10] = marriage[intValue];
                    } else {
                        datas[10] = "";
                    }
                }
                datas[11] = data.get("emp_code_address");
                datas[12] = data.get("emp_native");
                datas[13] = data.get("entry_local_address");
                object = data.get("emp_education");
                if (object == null) {
                    datas[14] = "";
                } else {
//                    for (int i = 0; i < education.length; i++) {
//                        if (object.toString().equals(education[i])) {
//                            datas[14] = education[i];
//                            break;
//                        } else if (object.toString().equals((i + ""))) {
//                            datas[14] = education[i];
//                            break;
//                        } else {
//                            datas[14] = data.get("emp_education_other");
//                        }
//                    }
                    intValue = Integer.parseInt(String.valueOf(object));
                    if (intValue >= 0 && intValue < education.length) {
                        datas[14] = education[intValue];
                    } else {
                        datas[14] = data.get("emp_education_other");
                    }
                }
                datas[15] = data.get("entry_mail");
                datas[16] = data.get("entry_phone");
                datas[17] = data.get("entry_urgent");
                datas[18] = data.get("entry_urgent_phone");
                datas[19] = data.get("entry_urgent_relation");
                datas[20] = data.get("emp_experience");
                object = data.get("entry_has_licence");
                if (object == null) {
                    datas[21] = "";
                } else {
                    intValue = Integer.parseInt(object.toString());
                    if (intValue == 0) {
                        datas[21] = "是";
                    } else {
                        datas[21] = "否";
                    }
                }
                datas[22] = data.get("entry_licence");
                datas[23] = data.get("entry_drive_age");
                object = data.get("entry_has_sick");
                if (object == null) {
                    datas[24] = "";
                } else {
                    intValue = Integer.parseInt(object.toString());
                    if (intValue == 0) {
                        datas[24] = "否";
                    } else {
                        datas[24] = "是";
                    }
                }
                datas[25] = data.get("entry_sick");
                object = data.get("entry_channel");
                if (object == null) {
                    datas[26] = "";
                } else {
                    intValue = Integer.parseInt(object.toString());
                    if (intValue >= 0 && intValue < channel.length) {
                        datas[26] = channel[intValue];
                    } else {
                        datas[26] = "";
                    }
                }
                datas[27] = data.get("entry_channel_name");
                datas[28] = data.get("emp_relative_name");
                datas[29] = data.get("emp_relative_phone");
                datas[30] = data.get("emp_relative_relation");
                datas[31] = data.get("entry_interest");
                datas[32] = DateUtils.format((Date) data.get("create_time"), DateUtils.DATE_FULL);
                object = data.get("state");
                if (object == null) {
                    datas[33] = "";
                } else {
                    intValue = Integer.parseInt(object.toString());
                    if (intValue >= 0 && intValue < state.length) {
                        datas[33] = state[intValue];
                    } else {
                        datas[33] = "";
                    }
                }

                // 增加到集合中；
                exportData.add(datas);
            }

            return DataImportUtil.createFile("入职申请列表", config.getUploadDir(), config.getWebDir(), rowTitles, exportData);
        } else {
            return null;
        }
    }

    /**
     * 根据入职申请ID查询入职申请相关的员工录用信息；
     *
     * @param data：返回给前端的数据；
     * @param entryId：入职申请ID；
     */
    @Override
    public void getEmployInfo(ResponseData data, int entryId) {
        // 获取审核人；
//        List<User> users = userService.listLeaderByDeptId(deptId);
        // 获取录用的基本信息；
        data.putDataValue("entry", entryMapper.selectEmployInfoById(entryId));
        data.putDataValue("hire", hireService.selectByParentId(entryId));
        // 获取员工的薪资信息；
        data.putDataValue("salary", salaryService.selectByParentEntryId(entryId));
        // 获取入职申请的评论信息；
        data.putDataValue("comment", commentService.selectEmployInfo(entryId));
    }

    @Override
    public List<Map<String, Object>> listDeptByFirstDept(String companyCode, Integer firstDept) {
        return employResourceMapper.listDeptByFirstDept(companyCode, firstDept);
    }

    @Override
    public List<User> listLeaderByDeptId(Integer deptId) {
       return userService.listLeaderByDeptId(deptId);
    }

    /**
     * 获取转正流程所需的审核数据；
     *
     * @param data：返回给前端的数据；
     * @param code：权限访问码；
     */
    @Override
    public void setEmployApproveData(ResponseData data, String code) {
        EmployEntry employEntry = entryMapper.selectApproveInfo(code);
        if (employEntry == null) {
            data.putDataValue("message", "未找到相关的信息，可能信息已过期。");
        } else {
            int entryId = employEntry.getEntryId();
            // 任务ID给前端；
            data.putDataValue("taskId", employEntry.getTaskId());
            int processState = employEntry.getProcessState();
            // 状态给前端，用于保存部长审核意见；
            data.putDataValue("processState", processState);
            // 获取审核人；
            // 部长审核的下个节点是人事；
            List<User> users = null;
            if (processState == IConst.STATE_BZ) {
                users = userService.listLeaderByState(IConst.STATE_RS);
            }
            // 人事审核的下个节点为总经理；
            if (processState == IConst.STATE_RS) {
                User user = AppUtil.getUser();
                users = userMapper.selectByDeptCode(user.getCompanyCode());
            }
            data.putDataValue("user", users);
            // 部门ID用于审核；
            if (users != null && !users.isEmpty()) {
                data.putDataValue("deptId", users.get(0).getDeptId());
            }
            // 获取录用的日期信息；
            data.putDataValue("hire", hireService.selectByParentId(entryId));
            // 获取员工的薪资信息；
            data.putDataValue("salary", salaryService.selectByParentEntryId(entryId));
            // 获取入职申请的评论信息；
            data.putDataValue("comment", commentService.selectEmployInfo(entryId));
        }
    }

    /**
     * 文件上传；
     *
     * @param data：返回给前端的数据；
     * @param multipartFile：上传的文件对象；
     */
    @Override
    public void uploadFile(ResponseData data, MultipartFile multipartFile) {
        data.putDataValue("image", config.getWebDir() + FileUtil.uploadFile(multipartFile.getOriginalFilename(), multipartFile, config.getUploadDir(), EmployEntry.class));
    }

    /**
     * 根据入职申请的ID查询家庭成员信息集合；
     *
     * @param entryId：入职申请ID；
     * @return ：家庭成员信息集合；
     */
    @Override
    public List<EmployEntryFamily> selectFamily(int entryId) {
        return familyService.selectByEntryId(entryId);
    }

    /**
     * 根据入职申请的ID查询教育经历信息集合；
     *
     * @param entryId：入职申请ID；
     * @return ：教育信息集合；
     */
    @Override
    public List<EmployEntryEducation> selectEducation(int entryId) {
        return educationService.selectByEntryId(entryId);
    }

    /**
     * 根据入职申请的ID查询工作经历信息集合；
     *
     * @param entryId：入职申请ID；
     * @return ：工作经历信息集合；
     */
    @Override
    public List<EmployEntryExperience> selectExperience(int entryId) {
        return experienceService.selectByEntryId(entryId);
    }

    /**
     * 查询民族信息；
     *
     * @return ：民族信息集合；
     */
    @Override
    public List<Map<String, Object>> listNation() {
        return resourceService.listNation();
    }

    /**
     * 查询指定区域的地区信息；
     *
     * @param areaId：地区的区域ID；
     * @return ：地区信息集合；
     */
    @Override
    public List<Map<String, Object>> listDistrict(Integer areaId) {
        return resourceService.listDistrict(areaId);
    }

    /**
     * 查询部门信息；
     *
     * @return ：部门信息集合；
     */
    @Override
    public List<Map<String, Object>> listDept() {
        String companyCode = null;
        User user = AppUtil.getUser();
        if (user != null) {
            Dept dept = user.getDept();
            // 祥和可看所有部门；
            if (dept != null && !"XH".equals(dept.getCompanyCode())) {
                companyCode = dept.getCompanyCode();
            }
        }
        return resourceService.listDept(companyCode);
    }

    /**
     * 查询指定部门的职位信息；
     *
     * @param deptId：部门ID；
     * @return ：职位信息集合；
     */
    @Override
    public List<Map<String, Object>> listPost(Integer deptId) {
        return resourceService.listPost(deptId);
    }

    @Override
    public List<Map<String, Object>> listPostByCompanyCode() {
        return employResourceMapper.listPostByCompanyCode(AppUtil.getUser().getCompanyCode());
    }

    @Override
    @Transactional
    public ResponseData importEmployeeData(MultipartFile file) {
        try {
            ResponseData result = ResponseData.ok();
            EmployeeEasyExcelListener listener = new EmployeeEasyExcelListener(resourceService,userService,employResourceMapper,employeeBasicMapper,
                    familyMapper,employEntryExperienceMapper,employEntryMapper,employeeSalaryMapper,employeeMapper,employEntryEducationMapper);
            EasyExcelFactory.readBySax(new BufferedInputStream(file.getInputStream()), new Sheet(1,4, EmployeeExcelDTO.class),listener);
            if(listener.getErrorData() != null && listener.getErrorData().size() > 0){
                String filtPath = DataImportUtil.createFile("员工导入错误原因", config.getUploadDir(), config.getWebDir(), (List<String>) listener.getErrorData().get("rowTitles"), (List<Object[]>) listener.getErrorData().get("exportData"));
                result.putDataValue("message", "导入失败，请点击红色提示内容下载文件检查内容是否符合要求。");
                result.putDataValue("file", filtPath);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，员工导入异常！");
        }
    }

    @Override
    public PageInfo<EmployEntryFamily> familyInfo(int entryId, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(familyMapper.selectByEntryId(entryId));
    }

    @Override
    public PageInfo<Map<String, Object>> educationInfo(long empId, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(entryMapper.educationInfo(empId));
    }

    @Override
    public PageInfo<Map<String, Object>> experienceInfo(long empId, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(entryMapper.experienceInfo(empId));
    }
}