package com.qinfei.qferp.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by yanhonghao on 2019/4/29 16:24.
 */
@Setter
@Getter
public class EmployeeExcelInfo extends BaseRowModel {

    @ExcelProperty(index = 0, value = "序号")
    private int index;
    private int entryId;
    private int empId;
    private int entryState = 3;//默认都是已经入职
    /**
     * 工号
     */
    @ExcelProperty(index = 1, value = "工号")
    private String empNum;
    /**
     * 入职时间
     */
    @ExcelProperty(index = 2, value = "入职时间")
    private String entryDateStr;
    private Date entryDate;
    /**
     * 部门名称
     */
    @ExcelProperty(index = 3, value = "部门")
    private String empDeptStr;
    @ExcelProperty(index = 4, value = "职位")
    private String empPostStr;

    /**
     * 职位id
     */
    private Integer empPostId;
    /**
     * 部门id
     */
    private Integer empDept;
    /**
     * 姓名
     */
    @ExcelProperty(index = 5, value = "姓名")
    private String empName;
    /**
     * 性别
     */
    @ExcelProperty(index = 6, value = "性别")
    private String empSex;
    private int empGender;
    private int sex;
    /**
     * 民族
     */
    @ExcelProperty(index = 7, value = "民族")
    private String empRaceStr;
    private Integer empRace;
    /**
     * 婚姻状况
     */
    @ExcelProperty(index = 8, value = "婚否")
    private String empMarriageStr;
    private Integer empMarriage;
    /**
     * 生日
     */
    @ExcelProperty(index = 9, value = "生日")
    private String empBirthStr;
    private Date empBirth;
    /**
     * 农历生日
     */
    @ExcelProperty(index = 10, value = "农历生日")
    private String empBirthday;
    /**
     * 身份证
     */
    @ExcelProperty(index = 11, value = "身份证")
    private String empCode;
    /**
     * 电话
     */
    @ExcelProperty(index = 12, value = "电话")
    private String empPhone;
    /**
     * 联系人及电话
     */
    @ExcelProperty(index = 13, value = "联系人及电话")
    private String empUrgent;
    private String empUrgentPhone;
    /**
     * 身份证地址
     */
    @ExcelProperty(index = 14, value = "身份证地址")
    private String empCodeAddress;
    /**
     * 籍贯
     */
    @ExcelProperty(index = 15, value = "籍贯")
    private String empNative;
    /**
     * 推荐人
     */
    @ExcelProperty(index = 16, value = "推荐人")
    private String empRelativeName;
    //推荐人Id
    private Integer empRelative;
    /**
     * 推荐人电话
     */
    private String empRelativePhone;
    /**
     * 推荐人关系
     */
    @ExcelProperty(index = 17, value = "推荐人关系")
    private String empRelativeRelation;
    /**
     * 年龄
     */
    @ExcelProperty(index = 18, value = "年龄")
    private String empAgeStr;
    private Integer empAge;
    /**
     * 学历
     */
    @ExcelProperty(index = 19, value = "学历")
    private String empEducation;
    /**
     * 学校
     */
    @ExcelProperty(index = 20, value = "学校")
    private String empCollege;
    /**
     * 专业
     */
    @ExcelProperty(index = 21, value = "专业")
    private String empMajor;
    /**
     * 家庭状况
     */
    @ExcelProperty(index = 22, value = "家庭状况")
    private String empFamily;
    /**
     * 工作履历
     */
    @ExcelProperty(index = 23, value = "工作履历")
    private String empResume;
    /**
     * 试用期
     */
    @ExcelProperty(index = 24, value = "试用期")
    private String empTry;
    /**
     * 转正期
     */
    @ExcelProperty(index = 25, value = "转正期")
    private String empPositive;
    /**
     * 备注字段
     */
    @ExcelProperty(index = 26, value = "备注字段")
    private String empRemark;

    /**
     * 创建人ID；
     */
    private Integer createId;
    private Integer userId;
    private String userName;

    /**
     * 创建人名称；
     */
    private String createName;

    /**
     * 创建时间；
     */
    private Date createTime;

    /**
     * 更新人ID；
     */
    private Integer updateId;

    /**
     * 更新人名称；
     */
    private String updateName;

    /**
     * 更新时间；
     */
    private Date updateTime;

    /**
     * 状态，-1为删除；
     */
    private Integer state;

    /**
     * 版本号，每次更新会+1；
     */
    private Integer version;
}
