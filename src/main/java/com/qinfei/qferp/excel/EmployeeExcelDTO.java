package com.qinfei.qferp.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.employ.EmployEntryExperience;
import com.qinfei.qferp.entity.employ.EmployEntryFamily;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * @CalssName EmployeeExcelDTO
 * @Description 员工导入对象
 * @Author xuxiong
 * @Date 2019/12/31 0031 16:25
 * @Version 1.0
 */
@Setter
@Getter
public class EmployeeExcelDTO extends BaseRowModel {
    //一级部门
    public static final Map<String, Integer> FIRST_DEPT_MAP = new HashMap<String, Integer>() {
        {
            put("业务部门", 0);
            put("媒介部门", 1);
            put("其他部门", 2);
        }
    };
    //性别
    public static final Map<String, Integer> ENTRY_SEX_MAP = new HashMap<String, Integer>() {
        {
            put("男", 1);
            put("女", 0);
        }
    };
    //婚否
    public static final Map<String, Integer> ENTRY_MARRIAGE_MAP = new HashMap<String, Integer>() {
        {
            put("未婚", 0);
            put("已婚", 1);
            put("离婚", 2);
            put("丧偶", 3);
        }
    };
    //岗位状态
    public static final Map<String, Integer> STATE_MAP = new HashMap<String, Integer>() {
        {
            put("试用", 0);
            put("正式", 1);
            put("离职", 2);
            put("实习", 5);
        }
    };
    //学历
    public static final Map<String, Integer> EMP_EDICATION_MAP = new HashMap<String, Integer>() {
        {
            put("初中", 0);
            put("高中", 1);
            put("专科", 2);
            put("本科", 3);
            put("硕士", 4);
            put("博士", 5);
            put("博士后", 6);
            put("其他", 7);
            put("小学", 8);
        }
    };
    //居住情况
    public static final Map<String, Integer> EMP_HOUSE_MAP = new HashMap<String, Integer>() {
        {
            put("住家", 0);
            put("租房", 2);
            put("宿舍", 1);
        }
    };
    //与本人关系:0-父亲、1-母亲、2-丈夫、3-妻子、4-儿子、5-女儿、6-哥哥、7-弟弟、8-姐姐、9-妹妹、10-叔叔、11-阿姨、12-舅舅、13-舅妈、14-姑姑、15-姑父
    public static final Map<String, Integer> RETATION_MAP = new HashMap<String, Integer>() {
        {
            put("父亲", 0);
            put("母亲", 1);
            put("丈夫", 2);
            put("妻子", 3);
            put("儿子", 4);
            put("女儿", 5);
            put("哥哥", 6);
            put("弟弟", 7);
            put("姐姐", 8);
            put("妹妹", 9);
            put("叔叔", 10);
            put("阿姨", 11);
            put("舅舅", 12);
            put("舅妈", 13);
            put("姑姑", 14);
            put("姑父", 15);
        }
    };
    //0-BOSS/BOSS直聘，1-社交媒体，2-离职再入职，3-人才市场，4-校园招聘，5-猎头推荐，6-内部推荐 ，7-其他，8-前程/前程无忧，9-智联/智联招聘、10-分子公司调岗
    public static final Map<String, Integer> ENTRY_CHANNEL_MAP = new HashMap<String, Integer>() {
        {
            put("BOSS", 0);
            put("BOSS直聘", 0);
            put("社交媒体", 1);
            put("离职再入职", 2);
            put("人才市场", 3);
            put("校园招聘", 4);
            put("猎头推荐", 5);
            put("内部推荐", 6);
            put("其他", 7);
            put("前程", 8);
            put("前程无忧", 8);
            put("智联", 9);
            put("智联招聘", 9);
            put("分子公司调岗", 10);
        }
    };
    //是否签署承诺书
    public static final Map<String, Integer> EMP_COMPLIANCE_MAP = new HashMap<String, Integer>() {
        {
            put("否", 1);
            put("是", 0);
        }
    };

    private Integer entryId; //入职申请ID
    private Integer empId; //员工ID
    private String entryValidate; //查询码
    private String entryCompanyCode; //入职申请公司
    private Integer entryHireState; //录用状态
    private Integer entryState; //入职申请状态
    private Integer createId; //创建人ID
    private String createName; //创建人名称
    private Date createTime; //创建时间
    private Integer updateId; //更新人ID
    private String updateName; //更新人名称
    private Date updateTime; //更新时间

    @ExcelProperty(index = 0, value = "序号")
    @NotBlank(message = "序号不能为空")
    private Integer index;

    @ExcelProperty(index = 1, value = "工号")
    @NotBlank(message = "工号不能为空")
    private String empNum;

    @ExcelProperty(index = 2, value = "入职时间")
    @NotBlank(message = "入职时间不能为空")
    private String entryExpectDateStr;
    private Date entryExpectDate;

    @ExcelProperty(index = 3, value = "司龄/年")
    @NotBlank(message = "司龄不能为空")
    private Float empWorkYear;

    //{"0-业务部门","1-媒介部门","2-其他部门"}
    @ExcelProperty(index = 4, value = "一级部门")
    @NotBlank(message = "一级部门不能为空")
    @ValidSelectField(message = "一级部门不支持该选项", selectOptionArr = {"业务部门","媒介部门","其他部门"})
    private String entryFirstDeptStr;
    private Integer entryFirstDept;

    @ExcelProperty(index = 5, value = "二级部门")
    @NotBlank(message = "二级部门不能为空")
    private String entryDeptStr;
    private Integer entryDept;

    @ExcelProperty(index = 6, value = "职位")
    @NotBlank(message = "职位不能为空")
    private String entryProfessionStr;
    private Integer entryProfession;

    @ExcelProperty(index = 7, value = "姓名")
    @NotBlank(message = "姓名不能为空")
    private String entryName;

    @ExcelProperty(index = 8, value = "性别")
    @NotBlank(message = "性别不能为空")
    @ValidSelectField(message = "性别不支持该选项", selectOptionArr = {"男","女"})
    private String empGenderStr;
    private Integer empGender;

    @ExcelProperty(index = 9, value = "身份证号")
    @NotBlank(message = "身份证号不能为空")
    private String empCode;

    @ExcelProperty(index = 10, value = "出生年月")
    @NotBlank(message = "出生年月不能为空")
    private String empBirthStr;
    private Date empBirth;

    @ExcelProperty(index = 11, value = "农历生日")
    @NotBlank(message = "农历生日不能为空")
    private String empBirthdayStr;
    private Date empBirthday;

    @ExcelProperty(index = 12, value = "年龄")
    @NotBlank(message = "年龄不能为空")
    private Integer empAge;

    @ExcelProperty(index = 13, value = "民族")
    @NotBlank(message = "民族不能为空")
    private String empRaceStr;
    private Integer empRace;

    //{"0-未婚","1-已婚","2-离婚","3-丧偶"}
    @ExcelProperty(index = 14, value = "婚否")
    @NotBlank(message = "婚否不能为空")
    @ValidSelectField(message = "婚否不支持该选项", selectOptionArr = {"未婚","已婚","离婚","丧偶"})
    private String entryMarriageStr;
    private Integer entryMarriage;

    @ExcelProperty(index = 15, value = "联系电话")
    @NotBlank(message = "联系电话不能为空")
    private String entryPhone;

    @ExcelProperty(index = 16, value = "紧急联系人及电话")
    @NotBlank(message = "紧急联系人及电话不能为空")
    private String entryUrgentPhoneStr;
    private String entryUrgent;
    private String entryUrgentPhone;
    private String entryUrgentRelation;

    @ExcelProperty(index = 17, value = "身份证地址")
    @NotBlank(message = "身份证地址不能为空")
    private String empCodeAddress;

    @ExcelProperty(index = 18, value = "籍贯")
    @NotBlank(message = "籍贯不能为空")
    private String empNative;
    private Integer empNativeProvince;
    private Integer empNativeCity;

    //{"0-试用","1-正式","2-离职","5-实习"}
    @ExcelProperty(index = 19, value = "岗位状态")
    @NotBlank(message = "岗位状态不能为空")
    @ValidSelectField(message = "岗位状态不支持该选项", selectOptionArr = {"试用","正式","离职","实习"})
    private String stateStr;
    private Integer state;

    //0-BOSS/BOSS直聘，1-社交媒体，2-离职再入职，3-人才市场，4-校园招聘，5-猎头推荐，6-内部推荐 ，7-其他，8-前程/前程无忧，9-智联/智联招聘、10-分子公司调岗
    @ExcelProperty(index = 20, value = "招聘渠道")
    @NotBlank(message = "招聘渠道不能为空")
    @ValidSelectField(message = "招聘渠道不支持该选项", selectOptionArr = {"BOSS","BOSS直聘","社交媒体","离职再入职","人才市场","校园招聘","猎头推荐","内部推荐",
            "其他","前程","前程无忧","智联","智联招聘","分子公司调岗"})
    private String entryChannelName;
    private Integer entryChannel;

    @ExcelProperty(index = 21, value = "推荐人")
    private String empRelativeName;
    private Integer empRelative; //推荐人ID
    private String empRelativePhone; //推荐人电话

    @ExcelProperty(index = 22, value = "与推荐人关系")
    private String empRelativeRelation;

    //{"0-初中","1-高中","2-专科","3-本科","4-硕士","5-博士","6-博士后","7-其他","8-小学"}
    @ExcelProperty(index = 23, value = "学历")
    @NotBlank(message = "学历不能为空")
    @ValidSelectField(message = "学历状态不支持该选项", selectOptionArr = {"初中","高中","专科","本科","硕士","博士","博士后","其他","小学"})
    private String empEducationStr;
    private Integer empEducation;

    @ExcelProperty(index = 24, value = "学校")
    @NotBlank(message = "学校不能为空")
    private String empCollege;

    @ExcelProperty(index = 25, value = "专业")
    @NotBlank(message = "专业不能为空")
    private String empMajor;

    //{"0-住家","2-租房","1-宿舍"}
    @ExcelProperty(index = 26, value = "居住情况")
    @NotBlank(message = "居住情况不能为空")
    @ValidSelectField(message = "居住情况不支持该选项", selectOptionArr = {"住家","租房","宿舍"})
    private String empHouseStr;
    private Integer empHouse;

    @ExcelProperty(index = 27, value = "父亲姓名")
    @NotBlank(message = "父亲姓名不能为空")
    private String famFatherName;

    @ExcelProperty(index = 28, value = "出生年月")
    @NotBlank(message = "父亲出生年月不能为空")
    private String famFatherBirthdayStr;
    private Date famFatherBirthday;

    @ExcelProperty(index = 29, value = "职业")
    @NotBlank(message = "父亲职业不能为空")
    private String famFatherProfession;

    @ExcelProperty(index = 30, value = "健康状况")
    @NotBlank(message = "父亲健康状况不能为空")
    private String famFatherHealth;
    private EmployEntryFamily famFather;

    @ExcelProperty(index = 31, value = "母亲姓名")
    @NotBlank(message = "母亲姓名不能为空")
    private String famMotherName;

    @ExcelProperty(index = 32, value = "出生年月")
    @NotBlank(message = "母亲出生年月不能为空")
    private String famMotherBirthdayStr;
    private Date famMotherBirthday;

    @ExcelProperty(index = 33, value = "职业")
    @NotBlank(message = "母亲职业不能为空")
    private String famMotherProfession;

    @ExcelProperty(index = 34, value = "健康状况")
    @NotBlank(message = "母亲健康状况不能为空")
    private String famMotherHealth;
    private EmployEntryFamily famMother;

    @ExcelProperty(index = 35, value = "配偶情况")
    private String spouseStatus;
    private EmployEntryFamily spouse;

    @ExcelProperty(index = 36, value = "兄弟姐妹情况")
    private String brotherStatus;
    @ExcelProperty(index = 37, value = "子女情况")
    private String sonStatus;
    private Integer  empSon; //儿子个数
    private Integer  empGirl; //女儿个数
    private Integer  empBrother; //哥哥个数
    private Integer  empYoungerBrother; //弟弟个数
    private Integer  empSister; //姐姐个数
    private Integer  empYoungerSister; //妹妹个数

    @ExcelProperty(index = 38, value = "合同签订单位")
    private String empContractUnit;

    @ExcelProperty(index = 39, value = "第一次合同签订时间")
    private String empContractDateStr;
    private Date empContractDate;

    @ExcelProperty(index = 40, value = "第二次合同签订时间")
    private String empTwoContractDateStr;
    private Date empTwoContractDate;

    @ExcelProperty(index = 41, value = "最后一次合同签订时间")
    private String empThreeContractDateStr;
    private Date empThreeContractDate;

    @ExcelProperty(index = 42, value = "社保号")
    private String empSecurityCode;

    @ExcelProperty(index = 43, value = "社保公司")
    private String empSecurityCompany;

    @ExcelProperty(index = 44, value = "承诺书")
    @ValidSelectField(message = "承诺书不支持该选项", selectOptionArr = {"是","否"})
    private String empComplianceStr;
    private Integer empCompliance;

    @ExcelProperty(index = 45, value = "职位工资")
    @NotBlank(message = "试用职位工资不能为空")
    private String trialPostStr;
    private Float trialPost;

    @ExcelProperty(index = 46, value = "绩效工资")
    @NotBlank(message = "试用绩效工资不能为空")
    private String trialPerformanceStr;
    private Float trialPerformance;

    @ExcelProperty(index = 47, value = "其他工资")
    @NotBlank(message = "试用其他工资不能为空")
    private String trialOtherStr;
    private Float trialOther;

    @ExcelProperty(index = 48, value = "职位工资")
    @NotBlank(message = "转正职位工资不能为空")
    private String formalPostStr;
    private Float formalPost;

    @ExcelProperty(index = 49, value = "绩效工资")
    @NotBlank(message = "转正绩效工资不能为空")
    private String formalPerformanceStr;
    private Float formalPerformance;

    @ExcelProperty(index = 50, value = "其他工资")
    @NotBlank(message = "转正其他工资不能为空")
    private String formalOtherStr;
    private Float formalOther;

    @ExcelProperty(index = 51, value = "工作履历")
    @NotBlank(message = "工作履历不能为空")
    private String empExperience;
    private List<EmployEntryExperience> employEntryExperienceList;

    @ExcelProperty(index = 52, value = "备注")
    private String empRemark;

    //将文本转换成日期
    public Date formatDate(String dateStr, String patten){
        Date result = null;
        try{
            //reserved-43553x1F Excel读取可能将日期变成该字符
            if(StringUtils.isNotEmpty(dateStr)){
                if(dateStr.contains("reserved-") && dateStr.contains("x1F")){
                    dateStr = dateStr.replaceAll("reserved-", "").replaceAll("x1F","");
                    Calendar calendar = new GregorianCalendar(1900,0,-1);
                    calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(dateStr));
                    result = calendar.getTime();
                }else {
                    //如果直接读取为数字，则直接加
                    if(StringUtils.isNumeric(dateStr)){
                        Calendar calendar = new GregorianCalendar(1900,0,-1);
                        calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(dateStr));
                        result = calendar.getTime();
                    }else{
                        result = DateUtils.parse(dateStr, patten);
                        if(result == null){
                            List<String> pattenList = new ArrayList<>(Arrays.asList("yyyy-MM-dd","yyyy.MM.dd","yyyy年MM月dd日","yyyy/MM/dd"));
                            pattenList.remove(patten);
                            if(CollectionUtils.isNotEmpty(pattenList)){
                                for(String fmt : pattenList){
                                    result = DateUtils.parse(dateStr, fmt);
                                    if(result != null){
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
