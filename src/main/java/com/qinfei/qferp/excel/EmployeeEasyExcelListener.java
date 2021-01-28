package com.qinfei.qferp.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.employ.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.employ.*;
import com.qinfei.qferp.service.employ.IEmployEntryService;
import com.qinfei.qferp.service.employ.IEmployResourceService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.PrimaryKeyUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * @CalssName EmployeeEasyExcelListener
 * @Description 员工批量导入处理器
 * @Author xuxiong
 * @Date 2020/1/2 0002 8:44
 * @Version 1.0
 */
@Getter
public class EmployeeEasyExcelListener extends AnalysisEventListener {
    private List<EmployeeExcelDTO> addDatas = new ArrayList<>(); //Excel数据集
    private List<EmployeeExcelDTO> editDatas = new ArrayList<>(); //Excel数据集
    private Map<String, Object> errorData = new HashMap<>(); //错误信息
    private List<Object[]> exportData = new ArrayList<>(); //错误提示信息集合
    private Map<String, Integer> nationMap = new HashMap<>(); //key:nationName value:nativeId
    private Map<Integer, Map<String, Integer>> deptMap = new HashMap<>(); // key:firstDept value:{key:deptName  value:deptId}
    private Map<String, Integer> postMap = new HashMap<>(); //key:deptId-postName  value: postId
    private List<String> excelEmpNumList = new ArrayList<>(); //缓存当前Excel存在的工号，排除工号重复情况
    private List<String> existEmpNumList = new ArrayList<>(); //系统中存在的工号

    private final IEmployResourceService employResourceService;
    private final IUserService userService;
    private final EmployResourceMapper employResourceMapper;
    private final EmployeeBasicMapper employeeBasicMapper;
    private final EmployEntryFamilyMapper employEntryFamilyMapper;
    private final EmployEntryExperienceMapper employEntryExperienceMapper;
    private final EmployEntryMapper employEntryMapper;
    private final EmployeeSalaryMapper employeeSalaryMapper;
    private final EmployeeMapper employeeMapper;
    private final EmployEntryEducationMapper employEntryEducationMapper;

    public EmployeeEasyExcelListener(IEmployResourceService employResourceService, IUserService userService, EmployResourceMapper employResourceMapper,
                                     EmployeeBasicMapper employeeBasicMapper, EmployEntryFamilyMapper employEntryFamilyMapper,
                                     EmployEntryExperienceMapper employEntryExperienceMapper, EmployEntryMapper employEntryMapper,
                                     EmployeeSalaryMapper employeeSalaryMapper, EmployeeMapper employeeMapper,EmployEntryEducationMapper employEntryEducationMapper) {
        this.employResourceService = employResourceService;
        this.userService = userService;
        this.employResourceMapper = employResourceMapper;
        this.employeeBasicMapper = employeeBasicMapper;
        this.employEntryFamilyMapper = employEntryFamilyMapper;
        this.employEntryExperienceMapper = employEntryExperienceMapper;
        this.employEntryMapper = employEntryMapper;
        this.employeeSalaryMapper = employeeSalaryMapper;
        this.employeeMapper = employeeMapper;
        this.employEntryEducationMapper = employEntryEducationMapper;
    }

    @Override
    public void invoke(Object o, AnalysisContext analysisContext) {
        EmployeeExcelDTO employeeExcelDTO = (EmployeeExcelDTO)o;
        String message = ExcelDataValidate.valdateNullAndSelect(o, ValidSelectField.class); //非空校验 和 校验下拉列表项
        if(StringUtils.isNotEmpty(message)){
            Object [] errorInfo = new Object[]{String.format("第%s行",(analysisContext.getCurrentRowNum()+1)), message};
            this.exportData.add(errorInfo);
        }else {
            //字段赋值
            message = convertData(employeeExcelDTO);
            if(StringUtils.isNotEmpty(message)){
                Object [] errorInfo = new Object[]{String.format("第%s行",(analysisContext.getCurrentRowNum()+1)), message};
                this.exportData.add(errorInfo);
                return;
            }

            //部门、职位、民族、工号校验并赋值
            message = validateDeptAndPostAndNation(employeeExcelDTO);
            if(StringUtils.isNotEmpty(message)){
                Object [] errorInfo = new Object[]{String.format("第%s行",(analysisContext.getCurrentRowNum()+1)), message};
                this.exportData.add(errorInfo);
                return;
            }

            //根据身份证和公司代码判断是否存在员工信息，判断是否可修改员工，系统存在一个身份证多个员工数据
            List<EmployeeExcelDTO> employeeExcelDTOList = employeeBasicMapper.listEmpInfoByEmpCode(employeeExcelDTO.getEmpCode());
            if(CollectionUtils.isNotEmpty(employeeExcelDTOList)){
                boolean updateFlag = false;
                for(EmployeeExcelDTO empInfo : employeeExcelDTOList){
                    //如果工号一致，，还需要岗位状态一致才更新
                    if(employeeExcelDTO.getEmpNum().equals(empInfo.getEmpNum())){
                        //判断岗位状态是否一致，一致才可更新
                        if(!employeeExcelDTO.getState().equals(empInfo.getState())){
                            message = "员工岗位状态与系统不一致";
                        }else{
                            employeeExcelDTO.setEntryId(empInfo.getEntryId()); //设置更新主键ID
                            employeeExcelDTO.setEntryValidate(empInfo.getEntryValidate()); //设置原查询码
                            updateFlag = true;
                        }
                        break;
                    }
                }
                if(!updateFlag){
                    message = StringUtils.isNotEmpty(message) ? message : "系统存在该身份证的人员信息，但不满足更新信息条件(工号、身份证、岗位状态均一致)";
                    Object [] errorInfo = new Object[]{String.format("第%s行",(analysisContext.getCurrentRowNum()+1)), message};
                    this.exportData.add(errorInfo);
                }
                //如果有错误提示数据，则不缓存Excel行信息，否则，根据分类（新增、修改）缓存
                if(CollectionUtils.isEmpty(this.exportData)){
                    this.editDatas.add(employeeExcelDTO); //缓存添加
                }
            }else {
                //如果有错误提示数据，则不缓存Excel行信息，否则，根据分类（新增、修改）缓存
                if(CollectionUtils.isEmpty(this.exportData)){
                    setEmpNumList(employeeExcelDTO.getEntryCompanyCode());
                    //校验工号是否存在
                    if(this.existEmpNumList.contains(employeeExcelDTO.getEmpNum())){
                        Object [] errorInfo = new Object[]{String.format("第%s行",(analysisContext.getCurrentRowNum()+1)), "系统不存在该身份证的人员信息，但已存在该工号，请变更工号"};
                        this.exportData.add(errorInfo);
                    }else {
                        employeeExcelDTO.setEntryValidate(PrimaryKeyUtil.getStringUniqueKey()); //设置新的查询码
                        this.addDatas.add(employeeExcelDTO); //缓存添加
                    }
                }
            }
        }
    }

    @Transactional
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        try{
            if(CollectionUtils.isNotEmpty(this.exportData)){
                buildTipExcel(this.exportData);
                return; //如果有问题，直接退出，不进行处理
            }
            //如果有更新的数据
            if(CollectionUtils.isNotEmpty(this.editDatas)){
                for(EmployeeExcelDTO employeeExcelDTO : this.editDatas){
                    //更新入职申请表
                    EmployEntry employEntry = buildEmployEntry(employeeExcelDTO);
                    employEntry.setUpdateInfo();
                    employEntryMapper.updateByPrimaryKeySelective(employEntry);
                    //更新员工表
                    Employee employee = buildEmployee(employeeExcelDTO);
                    employee.setUpdateInfo();
                    employeeMapper.updateFromExcel(employee);
                    //更新员工基本信息表
                    EmployeeBasic employeeBasic = buildEmployeeBasic(employeeExcelDTO);
                    employeeBasic.setUpdateInfo();
                    employeeBasicMapper.updateFromExcel(employeeBasic);
                    //更新教育经历（仅最高学历）
                    EmployEntryEducation employEntryEducation = buildEmployEducation(employeeExcelDTO);
                    if(employEntryEducation != null){
                        List<EmployEntryEducation> employEntryEducationList = employEntryEducationMapper.listByHighestAndEntryId(employEntryEducation);
                        if(CollectionUtils.isNotEmpty(employEntryEducationList)){
                            employEntryEducation.setUpdateInfo();
                            employEntryEducationMapper.updateByHighestAndEntryId(employEntryEducation);
                        }else {
                            employEntryEducationMapper.insertSelective(employEntryEducation);
                        }
                    }

                    //更新员工薪资
                    EmployeeSalary employeeSalary = buildEmployeeSalary(employeeExcelDTO);
                    employeeSalary.setUpdateInfo();
                    employeeSalaryMapper.updateByEntryId(employeeSalary);
                    //更新工作经历
                    if(CollectionUtils.isNotEmpty(employeeExcelDTO.getEmployEntryExperienceList())){
                        //删除原来的，保留最新的
                        EmployEntryExperience employEntryExperience = new EmployEntryExperience();
                        employEntryExperience.setEntryId(employeeExcelDTO.getEntryId());
                        employEntryExperienceMapper.deleteByEntryId(employEntryExperience);
                        employeeExcelDTO.getEmployEntryExperienceList().forEach(entryExperience -> {
                            entryExperience.setEntryId(employeeExcelDTO.getEntryId());
                            entryExperience.setCreateInfo();
                        });
                        employEntryExperienceMapper.insertSelectiveExcelBatch(employeeExcelDTO.getEmployEntryExperienceList());
                    }
                    //更新家庭成员，更新家庭成员需要根据entryId、成员名称、关系三者为条件进行更新
                    if(employeeExcelDTO.getSpouse() != null){
                        employeeExcelDTO.getSpouse().setEntryId(employeeExcelDTO.getEntryId());
                        updateFamInfo(employeeExcelDTO.getSpouse());
                    }
                    employeeExcelDTO.getFamFather().setEntryId(employeeExcelDTO.getEntryId());
                    updateFamInfo(employeeExcelDTO.getFamFather());
                    employeeExcelDTO.getFamMother().setEntryId(employeeExcelDTO.getEntryId());
                    updateFamInfo(employeeExcelDTO.getFamMother());
                }
            }
            //如果有新增的数据
            if(CollectionUtils.isNotEmpty(this.addDatas)){
                User user = AppUtil.getUser();
                //先入库e_entry缓存主键ID
                for(EmployeeExcelDTO employeeExcelDTO : this.addDatas){
                    employeeExcelDTO.setEntryCompanyCode(user.getCompanyCode());//申请公司
                    employeeExcelDTO.setEntryState(employeeExcelDTO.getState() == 2 ? 5 : 3); //3-已入职、5-已离职
                    employeeExcelDTO.setCreateId(user.getId());
                    employeeExcelDTO.setCreateName(user.getName());
                    employeeExcelDTO.setCreateTime(new Date());
                }
                employEntryMapper.insertSelectiveFromExcel(this.addDatas);
                List<Employee> employeeList = new ArrayList<>(); //员工表
                List<EmployeeBasic> employeeBasicList = new ArrayList<>(); //员工基本信息表
                List<EmployEntryEducation> employEntryEducationList = new ArrayList<>(); //员工教育经历表
                List<EmployEntryFamily> employEntryFamilyList = new ArrayList<>(); //家庭成员表
                List<EmployeeSalary> employeeSalaryList = new ArrayList<>(); //员工薪资
                List<EmployEntryExperience> employEntryExperienceList = new ArrayList<>(); //工作经历
                for(EmployeeExcelDTO employeeExcelDTO : this.addDatas){
                    Employee employee = buildEmployee(employeeExcelDTO);
                    employee.setCreateInfo(); //设置创建人及默认状态
                    employee.setState(employeeExcelDTO.getState());//重新覆盖岗位状态
                    employeeList.add(employee); //员工信息
                    EmployeeBasic employeeBasic = buildEmployeeBasic(employeeExcelDTO);
                    employeeBasic.setCreateInfo();//设置创建人及默认状态
                    employeeBasicList.add(employeeBasic); //员工基本信息
                    employEntryEducationList.add(buildEmployEducation(employeeExcelDTO)); //教育经历
                    EmployeeSalary employeeSalary = buildEmployeeSalary(employeeExcelDTO);
                    employeeSalary.setCreateInfo();//设置创建人及默认状态
                    employeeSalaryList.add(employeeSalary); //员工薪资
                    if(employeeExcelDTO.getSpouse() != null){
                        employeeExcelDTO.getSpouse().setEntryId(employeeExcelDTO.getEntryId());
                        employeeExcelDTO.getSpouse().setCreateInfo();//设置创建人及默认状态
                        employEntryFamilyList.add(employeeExcelDTO.getSpouse());
                    }
                    employeeExcelDTO.getFamFather().setEntryId(employeeExcelDTO.getEntryId());
                    employeeExcelDTO.getFamFather().setCreateInfo();//设置创建人及默认状态
                    employEntryFamilyList.add(employeeExcelDTO.getFamFather());
                    employeeExcelDTO.getFamMother().setEntryId(employeeExcelDTO.getEntryId());
                    employeeExcelDTO.getFamMother().setCreateInfo();//设置创建人及默认状态
                    employEntryFamilyList.add(employeeExcelDTO.getFamMother());
                    if(CollectionUtils.isNotEmpty(employeeExcelDTO.getEmployEntryExperienceList())){
                        employeeExcelDTO.getEmployEntryExperienceList().forEach(employEntryExperience -> {
                            employEntryExperience.setEntryId(employeeExcelDTO.getEntryId());
                            employEntryExperience.setCreateInfo();//设置创建人及默认状态
                        });
                        employEntryExperienceList.addAll(employeeExcelDTO.getEmployEntryExperienceList());
                    }
                }
                if(CollectionUtils.isNotEmpty(employeeList)){
                    employeeMapper.insertSelectiveFormExcel(employeeList);
                }
                employeeList.clear();
                employeeList = null;
                if(CollectionUtils.isNotEmpty(employeeSalaryList)){
                    employeeSalaryMapper.insertSelectiveFormExcel(employeeSalaryList);
                }
                employeeSalaryList.clear();
                employeeSalaryList = null;
                if(CollectionUtils.isNotEmpty(employeeBasicList)){
                    employeeBasicMapper.insertSelectiveExcelBatch(employeeBasicList);
                }
                employeeBasicList.clear();
                employeeBasicList = null;
                if(CollectionUtils.isNotEmpty(employEntryEducationList)){
                    employEntryEducationMapper.insertSelectiveExcelBatch(employEntryEducationList);
                }
                employEntryEducationList.clear();
                employEntryEducationList = null;
                if(CollectionUtils.isNotEmpty(employEntryFamilyList)){
                    employEntryFamilyMapper.insertSelectiveExcelBatch(employEntryFamilyList);
                }
                employEntryFamilyList.clear();
                employEntryFamilyList = null;
                if(CollectionUtils.isNotEmpty(employEntryExperienceList)){
                    employEntryExperienceMapper.insertSelectiveExcelBatch(employEntryExperienceList);
                }
                employEntryExperienceList.clear();
                employEntryExperienceList = null;
                //更新e_employee主键ID到e_employee_salary、e_entry表
                employeeSalaryMapper.updateEmpIdByEntryId(this.addDatas);
                employEntryMapper.updateEmpIdByEntryId(this.addDatas);
                this.addDatas.clear();
                this.addDatas = null;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "员工导入数据处理异常！");
        }
    }

    //转换Excel记录成对应值
    private String convertData(EmployeeExcelDTO employeeExcelDTO){
        String message = "";
        try{
            employeeExcelDTO.setEntryExpectDate(employeeExcelDTO.formatDate(employeeExcelDTO.getEntryExpectDateStr(), "yyyy年MM月dd日"));//入职时间
            employeeExcelDTO.setEmpGender(employeeExcelDTO.ENTRY_SEX_MAP.get(employeeExcelDTO.getEmpGenderStr()));//性别
            employeeExcelDTO.setEmpBirth(employeeExcelDTO.formatDate(employeeExcelDTO.getEmpBirthStr(), "yyyy年MM月dd日"));//出生年月
            employeeExcelDTO.setEmpBirthday(employeeExcelDTO.formatDate(employeeExcelDTO.getEmpBirthdayStr(), "yyyy年MM月dd日"));//农历生日
            employeeExcelDTO.setEntryMarriage(employeeExcelDTO.ENTRY_MARRIAGE_MAP.get(employeeExcelDTO.getEntryMarriageStr()));//婚否
            String [] urgentInfo = employeeExcelDTO.getEntryUrgentPhoneStr().split("，");
            int index = urgentInfo[0].trim().indexOf("（") == -1 ? urgentInfo[0].trim().indexOf("(") : urgentInfo[0].trim().indexOf("（");
            int lastIndex = urgentInfo[0].trim().lastIndexOf("）") == -1 ? urgentInfo[0].trim().lastIndexOf(")") : urgentInfo[0].trim().lastIndexOf("）");
            if(index > -1 && index < lastIndex){
                employeeExcelDTO.setEntryUrgent(urgentInfo[0].trim().substring(0, index));  //紧急联系人
                employeeExcelDTO.setEntryUrgentRelation(urgentInfo[0].trim().substring(index+1, lastIndex));//与紧急联系人关系
            }else {
                employeeExcelDTO.setEntryUrgent(urgentInfo[0].trim());  //紧急联系人
            }
            employeeExcelDTO.setEntryUrgentPhone(urgentInfo.length > 1 ? urgentInfo[1].trim() : ""); //紧急联系人电话
            employeeExcelDTO.setState(EmployeeExcelDTO.STATE_MAP.get(employeeExcelDTO.getStateStr()));//岗位状态
            if(employeeExcelDTO.getState() == 5){
                employeeExcelDTO.setEntryHireState(1); //1-实习
            }else {
                employeeExcelDTO.setEntryHireState(0); //0-试用
            }
            employeeExcelDTO.setEntryChannel(EmployeeExcelDTO.ENTRY_CHANNEL_MAP.get(employeeExcelDTO.getEntryChannelName()));//招聘渠道
            //如果招聘渠道为内部推荐，则判断推荐人是否存在
            if(employeeExcelDTO.getEntryChannel() == 6){
                if(StringUtils.isEmpty(employeeExcelDTO.getEmpRelativeName()) || "无".equals(employeeExcelDTO.getEmpRelativeName())){
                    throw new QinFeiException(1002, "推荐人不能为空");
                }
                if(StringUtils.isEmpty(employeeExcelDTO.getEmpRelativeRelation())){
                    throw new QinFeiException(1002, "与推荐人关系不能为空");
                }
                List<User> userList = userService.selectUserId(employeeExcelDTO.getEmpRelativeName(), null);
                if(CollectionUtils.isEmpty(userList)){
                    throw new QinFeiException(1002, "输入的推荐人信息不存在，请核对");
                }
                employeeExcelDTO.setEmpRelative(userList.get(0).getId());
                employeeExcelDTO.setEmpRelativeName(userList.get(0).getName());
                employeeExcelDTO.setEmpRelativePhone(userList.get(0).getPhone());
                employeeExcelDTO.setEmpRelativeRelation(employeeExcelDTO.getEmpRelativeRelation());
            }
            employeeExcelDTO.setEmpEducation(EmployeeExcelDTO.EMP_EDICATION_MAP.get(employeeExcelDTO.getEmpEducationStr()));//学历
            employeeExcelDTO.setEmpHouse(EmployeeExcelDTO.EMP_HOUSE_MAP.get(employeeExcelDTO.getEmpHouseStr()));//居住情况
            employeeExcelDTO.setFamFatherBirthday(employeeExcelDTO.formatDate(employeeExcelDTO.getFamFatherBirthdayStr(), "yyyy.MM.dd"));
            employeeExcelDTO.setFamMotherBirthday(employeeExcelDTO.formatDate(employeeExcelDTO.getFamMotherBirthdayStr(), "yyyy.MM.dd"));
            employeeExcelDTO.setFamFather(buildEmployEntryFamily(employeeExcelDTO, true)); //父亲
            employeeExcelDTO.setFamMother(buildEmployEntryFamily(employeeExcelDTO, false)); //母亲
            //配偶情况
            if(StringUtils.isNotEmpty(employeeExcelDTO.getSpouseStatus())){
                EmployEntryFamily spouse = new EmployEntryFamily();
                String [] spouseInfo = employeeExcelDTO.getSpouseStatus().split("，");
                //如果当前用户性别是男，则关系是妻子，否则丈夫，2-丈夫、3-妻子
                if(employeeExcelDTO.getEmpGender() == 1){
                    if("妻子".equals(spouseInfo[0].trim())){
                        spouse.setFamRelation(3); //与本人关系
                    }else {
                        throw new QinFeiException(1002, "配偶关系必须为妻子");
                    }
                }else {
                    if("丈夫".equals(spouseInfo[0].trim())){
                        spouse.setFamRelation(2); //与本人关系
                    }else {
                        throw new QinFeiException(1002, "配偶关系必须为丈夫");
                    }
                }
                spouse.setFamName(spouseInfo.length > 1 ? spouseInfo[1].trim() : ""); //姓名
                spouse.setFamAge(Integer.parseInt(spouseInfo.length > 2 ? spouseInfo[2].trim() : "0"));//年龄
                spouse.setFamProfession(spouseInfo.length > 3 ? spouseInfo[3].trim() : ""); //职业
                employeeExcelDTO.setSpouse(spouse);
            }
            //兄弟姐妹情况
            if(StringUtils.isNotEmpty(employeeExcelDTO.getBrotherStatus())){
                String brotherStr = employeeExcelDTO.getBrotherStatus()
                        .replaceAll("兄","-")
                        .replaceAll("弟","-")
                        .replaceAll("姐","-")
                        .replaceAll("妹","-")
                        .replaceAll("X", "0")
                        .replaceAll("x","0");
                String [] brotherInfo = brotherStr.split("-");
                if(brotherInfo != null && brotherInfo.length > 0){
                    employeeExcelDTO.setEmpBrother(StringUtils.isNotEmpty(brotherInfo[0].trim()) ? Integer.parseInt(brotherInfo[0].trim()) : 0); //兄
                    employeeExcelDTO.setEmpYoungerBrother(brotherInfo.length > 1 ? (StringUtils.isNotEmpty(brotherInfo[1].trim()) ? Integer.parseInt(brotherInfo[1].trim()) : 0) : 0); //弟
                    employeeExcelDTO.setEmpSister(brotherInfo.length > 2 ? (StringUtils.isNotEmpty(brotherInfo[2].trim()) ? Integer.parseInt(brotherInfo[2].trim()) : 0) : 0); //姐
                    employeeExcelDTO.setEmpYoungerSister(brotherInfo.length > 3 ? (StringUtils.isNotEmpty(brotherInfo[3].trim()) ? Integer.parseInt(brotherInfo[3].trim()) : 0) : 0); //妹
                }
            }
            //子女情况
            if(StringUtils.isNotEmpty(employeeExcelDTO.getSonStatus())){
                String sonStr = employeeExcelDTO.getSonStatus()
                        .replaceAll("子","-")
                        .replaceAll("女","-")
                        .replaceAll("X", "0")
                        .replaceAll("x","0");
                String [] sonInfo = sonStr.split("-");
                if(sonInfo != null && sonInfo.length > 0){
                    employeeExcelDTO.setEmpSon(StringUtils.isNotEmpty(sonInfo[0].trim()) ? Integer.parseInt(sonInfo[0].trim()) : 0); //子
                    employeeExcelDTO.setEmpGirl(sonInfo.length > 1 ? (StringUtils.isNotEmpty(sonInfo[1].trim()) ? Integer.parseInt(sonInfo[1].trim()) : 0) : 0); //女
                }
            }
            employeeExcelDTO.setEmpContractDate(employeeExcelDTO.formatDate(employeeExcelDTO.getEmpContractDateStr(), "yyyy年MM月dd日"));//第一次合同签订时间
            employeeExcelDTO.setEmpTwoContractDate(employeeExcelDTO.formatDate(employeeExcelDTO.getEmpTwoContractDateStr(), "yyyy年MM月dd日"));//第二次合同签订时间
            employeeExcelDTO.setEmpThreeContractDate(employeeExcelDTO.formatDate(employeeExcelDTO.getEmpThreeContractDateStr(), "yyyy年MM月dd日"));//最后一次合同签订时间
            if(StringUtils.isNotEmpty(employeeExcelDTO.getEmpComplianceStr())){
                employeeExcelDTO.setEmpCompliance(EmployeeExcelDTO.EMP_COMPLIANCE_MAP.get(employeeExcelDTO.getEmpComplianceStr()));
            }else {
                employeeExcelDTO.setEmpCompliance(1); //1-未签署承诺
            }
            employeeExcelDTO.setTrialPost(Float.parseFloat(employeeExcelDTO.getTrialPostStr()));//职位工资
            employeeExcelDTO.setTrialPerformance(Float.parseFloat(employeeExcelDTO.getTrialPerformanceStr()));//职位工资
            employeeExcelDTO.setTrialOther(Float.parseFloat(employeeExcelDTO.getTrialOtherStr()));//其他工资
            employeeExcelDTO.setFormalPost(Float.parseFloat(employeeExcelDTO.getFormalPostStr()));//职位工资
            employeeExcelDTO.setFormalPerformance(Float.parseFloat(employeeExcelDTO.getFormalPerformanceStr()));//职位工资
            employeeExcelDTO.setFormalOther(Float.parseFloat(employeeExcelDTO.getFormalOtherStr()));//其他工资
            //承诺书
            if(StringUtils.isNotEmpty(employeeExcelDTO.getEmpComplianceStr())){
                employeeExcelDTO.setEmpCompliance(EmployeeExcelDTO.EMP_COMPLIANCE_MAP.get(employeeExcelDTO.getEmpComplianceStr()));
            }
            //工作履历
            String [] empExperienceArr = employeeExcelDTO.getEmpExperience().split("；"); //工作履历可能有多个
            List<EmployEntryExperience> employEntryExperienceList = new ArrayList<>();
            for(String empExperience : empExperienceArr){
                try{
                    String [] empExperienceInfo = empExperience.split("，");
                    if(empExperienceInfo != null && empExperienceInfo.length > 0){
                        EmployEntryExperience employEntryExperience = new EmployEntryExperience();
                        String [] timeInfo = empExperienceInfo[0].trim().split("-");
                        if(timeInfo != null && timeInfo.length > 0){
                            employEntryExperience.setExpStart(DateUtils.parse(timeInfo[0], "yyyy.MM")); //开始时间
                            employEntryExperience.setExpEnd(timeInfo.length > 1 ? DateUtils.parse(timeInfo[1], "yyyy.MM") : null); //结束时间
                        }
                        employEntryExperience.setEntryId(employeeExcelDTO.getEntryId());
                        employEntryExperience.setExpLocation(empExperienceInfo.length > 1 ? empExperienceInfo[1].trim() : ""); //工作地址
                        employEntryExperience.setExpCompany(empExperienceInfo.length > 2 ? empExperienceInfo[2].trim() : ""); //公司名称
                        employEntryExperience.setExpProfession(empExperienceInfo.length > 3 ? empExperienceInfo[3].trim() : ""); //职务
                        employEntryExperience.setExpSalary(empExperienceInfo.length > 4 ? empExperienceInfo[4].trim() : ""); //薪资待遇
                        employEntryExperience.setExpContactor(empExperienceInfo.length > 5 ? empExperienceInfo[5].trim() : ""); //证明人及电话
                        employEntryExperience.setExpResignReason(empExperienceInfo.length > 6 ? empExperienceInfo[6].trim() : ""); ////离职原因
                        employEntryExperienceList.add(employEntryExperience);
                    }
                }catch (Exception e){
                    throw new QinFeiException(1002, "工作履历信息解码错误");
                }
            }
            employeeExcelDTO.setEmployEntryExperienceList(employEntryExperienceList);
        }catch (QinFeiException e){
            e.printStackTrace();
            message = e.getMessage();
        } catch (Exception e){
            e.printStackTrace();
            message = "员工信息解析异常，请检查是否与模板案例一致";
        }
        return message;
    }

    //构建提示excel所需数据
    private void buildTipExcel(List<Object[]> exportData){
        List<String> rowTitles = new ArrayList<>();
        rowTitles.add("行号");
        rowTitles.add("错误信息");
        errorData.put("rowTitles", rowTitles);
        errorData.put("exportData",exportData);
    }

    //构建员工信息(e_employee)
    private Employee buildEmployee(EmployeeExcelDTO employeeExcelDTO){
        Employee employee = new Employee();
        employee.setEntryId(employeeExcelDTO.getEntryId());
        employee.setEmpNum(employeeExcelDTO.getEmpNum()); //工号
        employee.setEmpName(employeeExcelDTO.getEntryName()); //员工姓名
        employee.setEmpDept(employeeExcelDTO.getEntryDept()); //入职部门
        employee.setEmpProfession(employeeExcelDTO.getEntryProfession()); //入职职位
        employee.setEmpPhone(employeeExcelDTO.getEntryPhone()); //联系电话
        employee.setEmpMarriage(employeeExcelDTO.getEntryMarriage()); //婚否
        employee.setEmpUrgent(employeeExcelDTO.getEntryUrgent()); //紧急联系人
        employee.setEmpUrgentPhone(employeeExcelDTO.getEntryUrgentPhone());//紧急联系人电话
        employee.setEmpAge(employeeExcelDTO.getEmpAge()); //年龄
        employee.setEmpWorkYear(employeeExcelDTO.getEmpWorkYear()); //司龄
        employee.setEmpHouse(employeeExcelDTO.getEmpHouse()); //居住情况
        employee.setEmpContractUnit(employeeExcelDTO.getEmpContractUnit()); //合同签订单位
        employee.setEmpContractDate(employeeExcelDTO.getEmpContractDate()); //第一次签署日期
        employee.setEmpTwoContractDate(employeeExcelDTO.getEmpTwoContractDate()); //第二次签署日期
        employee.setEmpThreeContractDate(employeeExcelDTO.getEmpThreeContractDate()); //第三次签署日期
        employee.setEmpSecurityCode(employeeExcelDTO.getEmpSecurityCode()); //社保账号
        employee.setEmpSecurityCompany(employeeExcelDTO.getEmpSecurityCompany()); //社保公司
        employee.setEmpCompliance(employeeExcelDTO.getEmpCompliance()); //是否已签署承诺书
        employee.setState(employeeExcelDTO.getState()); //岗位状态
        return employee;
    }

    //构建员工薪资信息(e_employee_salary)
    private EmployeeSalary buildEmployeeSalary(EmployeeExcelDTO employeeExcelDTO){
        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setEntryId(employeeExcelDTO.getEntryId());
        employeeSalary.setEmpId(employeeExcelDTO.getEmpId());
        employeeSalary.setEmpNum(employeeExcelDTO.getEmpNum());
        employeeSalary.setTrialPost(employeeExcelDTO.getTrialPost());
        employeeSalary.setTrialPerformance(employeeExcelDTO.getTrialPerformance());
        employeeSalary.setTrialOther(employeeExcelDTO.getTrialOther());
        employeeSalary.setTrialSalary(employeeSalary.getTrialPost()+employeeSalary.getTrialPerformance()+employeeSalary.getTrialOther());
        employeeSalary.setFormalPost(employeeExcelDTO.getFormalPost());
        employeeSalary.setFormalPerformance(employeeExcelDTO.getFormalPerformance());
        employeeSalary.setFormalOther(employeeExcelDTO.getFormalOther());
        employeeSalary.setFormalSalary(employeeSalary.getFormalPost()+employeeSalary.getFormalPerformance()+employeeSalary.getFormalOther());
        return employeeSalary;
    }

    //构建入职基本信息(e_employee_basic)
    private EmployeeBasic buildEmployeeBasic(EmployeeExcelDTO employeeExcelDTO){
        EmployeeBasic employeeBasic = new EmployeeBasic();
        employeeBasic.setEntryId(employeeExcelDTO.getEntryId());
        employeeBasic.setEmpGender(employeeExcelDTO.getEmpGender()); //性别
        employeeBasic.setEmpRace(employeeExcelDTO.getEmpRace()); //民族
        employeeBasic.setEmpBirth(employeeExcelDTO.getEmpBirth()); //出生
        if(employeeExcelDTO.getEmpBirthday() != null){
            employeeBasic.setEmpBirthday(DateUtils.format(employeeExcelDTO.getEmpBirthday(), "yyyy-MM-dd")); //农历生日
        }
        employeeBasic.setEmpCode(employeeExcelDTO.getEmpCode()); //身份证号码
        employeeBasic.setEmpCodeAddress(employeeExcelDTO.getEmpCodeAddress()); //身份证地址
        //TODO 籍贯省 籍贯市 无法解析
        employeeBasic.setEmpNative(employeeExcelDTO.getEmpNative()); //籍贯
        employeeBasic.setEmpEducation(employeeExcelDTO.getEmpEducation()); //学历
        employeeBasic.setEmpCollege(employeeExcelDTO.getEmpCollege()); //学校
        employeeBasic.setEmpMajor(employeeExcelDTO.getEmpMajor()); //专业
        employeeBasic.setEmpExperience(employeeExcelDTO.getEmpExperience()); //工作履历不能为空
        employeeBasic.setEmpRelative(employeeExcelDTO.getEmpRelative()); //推荐人ID
        employeeBasic.setEmpRelativeName(employeeExcelDTO.getEmpRelativeName()); //推荐人名称
        employeeBasic.setEmpRelativePhone(employeeExcelDTO.getEmpRelativePhone()); //推荐人电话
        employeeBasic.setEmpRelativeRelation(employeeExcelDTO.getEmpRelativeRelation()); //与推荐人的关系
        employeeBasic.setEmpDate(employeeExcelDTO.getEntryExpectDate()); //入职时间
        employeeBasic.setEmpSon(employeeExcelDTO.getEmpSon()); //儿子个数
        employeeBasic.setEmpGirl(employeeExcelDTO.getEmpGirl()); //女儿个数
        employeeBasic.setEmpBrother(employeeExcelDTO.getEmpBrother()); //哥哥个数
        employeeBasic.setEmpYoungerBrother(employeeExcelDTO.getEmpYoungerBrother()); //弟弟个数
        employeeBasic.setEmpSister(employeeExcelDTO.getEmpSister()); //姐姐个数
        employeeBasic.setEmpYoungerSister(employeeExcelDTO.getEmpYoungerSister()); //妹妹个数
        return employeeBasic;
    }

    //构建员工教育经历
    private EmployEntryEducation buildEmployEducation(EmployeeExcelDTO employeeExcelDTO){
        EmployEntryEducation employEntryEducation = new EmployEntryEducation();
        employEntryEducation.setEntryId(employeeExcelDTO.getEntryId());
        employEntryEducation.setEduHighest(1); //1-最高学历
        employEntryEducation.setEduCollege(employeeExcelDTO.getEmpCollege());
        employEntryEducation.setEduMajor(employeeExcelDTO.getEmpMajor());
        employEntryEducation.setEduRecord(employeeExcelDTO.getEmpEducationStr());
        employEntryEducation.setCreateInfo();
        return employEntryEducation;
    }

    //构建入职信息(e_entry)
    private EmployEntry buildEmployEntry(EmployeeExcelDTO employeeExcelDTO){
        User user = AppUtil.getUser();
        EmployEntry employEntry = new EmployEntry();
        employEntry.setEntryId(employeeExcelDTO.getEntryId());
        employEntry.setEntryCompanyCode(user.getCompanyCode()); //申请公司
        employEntry.setEntryFirstDept(employeeExcelDTO.getEntryFirstDept()); //一级部门
        employEntry.setEntryDept(employeeExcelDTO.getEntryDept()); //二级部门
        employEntry.setEntryProfession(employeeExcelDTO.getEntryProfession()); //职位
        employEntry.setEntryName(employeeExcelDTO.getEntryName()); //申请人姓名
        employEntry.setEntryMarriage(employeeExcelDTO.getEntryMarriage()); //婚否
        employEntry.setEntryPhone(employeeExcelDTO.getEntryPhone()); //联系电话
        employEntry.setEntryUrgent(employeeExcelDTO.getEntryUrgent()); //紧急联系人
        employEntry.setEntryUrgentPhone(employeeExcelDTO.getEntryUrgentPhone());//紧急联系人电话
        employEntry.setEntryUrgentRelation(employeeExcelDTO.getEntryUrgentRelation()); //与紧急人关系
        employEntry.setEntryChannel(employeeExcelDTO.getEntryChannel()); //渠道
        employEntry.setEntryChannelName(employeeExcelDTO.getEntryChannelName()); //渠道名称
        employEntry.setEntryValidate(employeeExcelDTO.getEntryValidate()); //查询码
        employEntry.setEntryExpectDate(employeeExcelDTO.getEntryExpectDate()); //入职时间
        employEntry.setState(employeeExcelDTO.getState() == 2 ? 5 : 3); //3-已入职、5-已离职
        employEntry.setEntryState(employeeExcelDTO.getEntryHireState());
        return employEntry;
    }

    //构建家庭成员信息(e_entry_family)
    private EmployEntryFamily buildEmployEntryFamily(EmployeeExcelDTO employeeExcelDTO, boolean isFather){
        EmployEntryFamily employEntryFamily = new EmployEntryFamily();
        employEntryFamily.setEntryId(employeeExcelDTO.getEntryId());
        employEntryFamily.setFamRelation(isFather ? 0 : 1); //成员关系
        employEntryFamily.setFamName(isFather ? employeeExcelDTO.getFamFatherName() : employeeExcelDTO.getFamMotherName()); //姓名
        employEntryFamily.setFamProfession(isFather ? employeeExcelDTO.getFamFatherProfession() : employeeExcelDTO.getFamMotherProfession()); //职业
        if(employeeExcelDTO.getFamFatherBirthday() != null){
            employEntryFamily.setFamBirthday(isFather ? DateUtils.format(employeeExcelDTO.getFamFatherBirthday(), "yyyy-MM-dd") :
                    DateUtils.format(employeeExcelDTO.getFamMotherBirthday(), "yyyy-MM-dd"));  //生日
            employEntryFamily.setFamAge(isFather ? getAgeByBirth(employeeExcelDTO.getFamFatherBirthday()) : getAgeByBirth(employeeExcelDTO.getFamMotherBirthday())); //年龄
        }
        employEntryFamily.setFamHealth(isFather ? employeeExcelDTO.getFamFatherHealth() : employeeExcelDTO.getFamMotherHealth()); //健康状况
        return employEntryFamily;
    }

    //校验部门、职位、民族、工号格式是否正确
    private String validateDeptAndPostAndNation(EmployeeExcelDTO employeeExcelDTO){
        String message = "";
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录");
            }
            //验证工号格式
            if(employeeExcelDTO.getEntryExpectDate() == null){
                throw new QinFeiException(1002, "入职日期转换错误，请检查");
            }
            String patten = String.format("^%s%s\\d{3}$", user.getCompanyCode(),DateUtils.format(employeeExcelDTO.getEntryExpectDate(), "yyyy"));
            if(!employeeExcelDTO.getEmpNum().matches(patten)){
                throw new QinFeiException(1002, "工号格式(公司代码 + 年份 + 三位数字)不正确");
            }
            if(this.excelEmpNumList.contains(employeeExcelDTO.getEmpNum())){
                throw new QinFeiException(1002, "文件存在重复工号问题，请检查");
            }else {
                this.excelEmpNumList.add(employeeExcelDTO.getEmpNum());
            }

            //设置一级部门值
            employeeExcelDTO.setEntryFirstDept(EmployeeExcelDTO.FIRST_DEPT_MAP.get(employeeExcelDTO.getEntryFirstDeptStr()));
            //校验民族
            if(!employeeExcelDTO.getEmpRaceStr().contains("族")){
                employeeExcelDTO.setEmpRaceStr(employeeExcelDTO.getEmpRaceStr() + "族");
            }
            setNationMap();
            if(!nationMap.containsKey(employeeExcelDTO.getEmpRaceStr())){
                throw new QinFeiException(1002, "民族不支持该选项");
            }else {
                employeeExcelDTO.setEmpRace(nationMap.get(employeeExcelDTO.getEmpRaceStr()));
            }
            //校验部门
            setDeptMap(user.getCompanyCode(), employeeExcelDTO.getEntryFirstDept());
            if(!deptMap.get(employeeExcelDTO.getEntryFirstDept()).containsKey(employeeExcelDTO.getEntryDeptStr())){
                throw new QinFeiException(1002, "【部门信息错误】没有找到【" + employeeExcelDTO.getEntryDeptStr() + "】的相关信息");
            }else {
                employeeExcelDTO.setEntryDept(deptMap.get(employeeExcelDTO.getEntryFirstDept()).get(employeeExcelDTO.getEntryDeptStr()));
            }
            //校验职位
            setPostMap(user.getCompanyCode());
            if(!postMap.containsKey(String.format("%s-%s",employeeExcelDTO.getEntryDept(), employeeExcelDTO.getEntryProfessionStr()))){
                throw new QinFeiException(1002, "请检查部门【" + employeeExcelDTO.getEntryDeptStr() + "】下职位【" + employeeExcelDTO.getEntryProfessionStr() + "】是否存在");
            }else {
                employeeExcelDTO.setEntryProfession(postMap.get(String.format("%s-%s",employeeExcelDTO.getEntryDept(), employeeExcelDTO.getEntryProfessionStr())));
            }
        }catch (QinFeiException e){
            message = e.getMessage();
        }catch (Exception e){
            e.printStackTrace();
            message = "校验部门、职位、民族、工号格式异常";
        }
        return message;
    }

    //设置民族
    private void setNationMap(){
        if(nationMap.size() < 1){
            List<Map<String, Object>> nationList = employResourceService.listNation();
            if(CollectionUtils.isNotEmpty(nationList)){
                for(Map<String, Object> nation : nationList){
                    nationMap.put(String.valueOf(nation.get("name")), Integer.parseInt(String.valueOf(nation.get("id"))));
                }
                nationList.clear();
                nationList = null;
            }
        }
    }

    //设置民族
    private void setDeptMap(String companyCode, Integer firstDept){
       if(deptMap.get(firstDept) == null || deptMap.get(firstDept).size() < 1){
           List<Map<String, Object>> deptList =  employResourceMapper.listDeptByFirstDept(companyCode, firstDept);
           if(CollectionUtils.isNotEmpty(deptList)){
               for(Map<String, Object> dept : deptList){
                   if(!deptMap.containsKey(firstDept)){
                       deptMap.put(firstDept, new HashMap<>());
                   }
                   deptMap.get(firstDept).put(String.valueOf(dept.get("name")), Integer.parseInt(String.valueOf(dept.get("id"))));
               }
               deptList.clear();
               deptList = null;
           }
       }
    }

    //设置部门职位
    private void setPostMap(String companyCode){
        if(postMap.size() < 1){
            List<Map<String, Object>> postList = employResourceService.listPostByCompany(companyCode);
            if(CollectionUtils.isNotEmpty(postList)){
                for(Map<String, Object> post : postList){
                    postMap.put(String.format("%s-%s", String.valueOf(post.get("deptId")),String.valueOf(post.get("postName"))),
                            Integer.parseInt(String.valueOf(post.get("postId"))));
                }
                postList.clear();
                postList = null;
            }
        }
    }

    //设置系统工号
    private void setEmpNumList(String companyCode){
        if(CollectionUtils.isEmpty(this.existEmpNumList)){
            this.existEmpNumList = employeeMapper.listEmpNumByCompanyCode(companyCode, null);
        }
    }

    //更新家庭成员信息
    private void updateFamInfo(EmployEntryFamily employEntryFamily){
        List<EmployEntryFamily> employEntryFamilyList = employEntryFamilyMapper.listFamInfoByRelationAndEntryId(employEntryFamily);
        //根据关系去查，如果存在则更新，否则新增
        if(CollectionUtils.isNotEmpty(employEntryFamilyList)){
            employEntryFamily.setUpdateInfo();
            employEntryFamilyMapper.updateByRelationAndEntryId(employEntryFamily);
        }else {
            employEntryFamily.setCreateInfo();
            employEntryFamilyMapper.insertSelective(employEntryFamily);
        }
    }

    //计算年龄
    private int getAgeByBirth(Date birthDay){
        int age = 0;
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
            throw new QinFeiException(1002, "出生年月不能晚于当前时间");
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        age = yearNow - yearBirth;   //计算整岁数
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;//当前日期在生日之前，年龄减一
            } else {
                age--;//当前月份在生日之前，年龄减一
            }
        }
        return age;
    }
}
