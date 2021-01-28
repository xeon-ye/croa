package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 员工基本信息实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployeeBasic extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -8136698179305196340L;

	/**
	 * 主键ID；
	 */
	private Integer basId;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 性别，0为女，1为男；
	 */
	private Integer empGender;

	/**
	 * 民族；
	 */
	private Integer empRace;

	/**
	 * 出生日期；
	 */
	private Date empBirth;

	/**
	 * 申请人生日（农历）；
	 */
	private String empBirthday;

	/**
	 * 身份证号码；
	 */
	private String empCode;

	/**
	 * 身份证地址；
	 */
	private String empCodeAddress;

	/**
	 * 籍贯省；
	 */
	private Integer empNativeProvince;

	/**
	 * 籍贯市；
	 */
	private Integer empNativeCity;

	/**
	 * 籍贯；
	 */
	private String empNative;

	/**
	 * 学历；
	 */
	private Integer empEducation;

	/**
	 * 毕业院校；
	 */
	private String empCollege;

	/**
	 * 专业；
	 */
	private String empMajor;

	/**
	 * 其他学历；
	 */
	private String empEducationOther;

	/**
	 * 学历证明文件；
	 */
	private String empEducationFile;

	/**
	 * 以往工作履历；
	 */
	private String empExperience;

	/**
	 * 离职证明文件；
	 */
	private String empExperienceFile;

	/**
	 * 试用期开始；
	 */
	private Date trialBegin;

	/**
	 * 试用期结束；
	 */
	private Date trialEnd;

	/**
	 * 推荐人ID；
	 */
	private Integer empRelative;

	/**
	 * 推荐人姓名；
	 */
	private String empRelativeName;

	/**
	 * 推荐人联系电话；
	 */
	private String empRelativePhone;

	/**
	 * 与推荐人关系；
	 */
	private String empRelativeRelation;

	/**
	 * 入职日期；
	 */
	private Date empDate;

	/**
	 * 离职日期；
	 */
	private Date empLeaveDate;

	private Integer  empSon; //儿子个数
	private Integer  empGirl; //女儿个数
	private Integer  empBrother; //哥哥个数
	private Integer  empYoungerBrother; //弟弟个数
	private Integer  empSister; //姐姐个数
	private Integer  empYoungerSister; //妹妹个数

	/**
	 * 主键ID；
	 * 
	 * @return ：basId 主键ID；
	 */
	public Integer getBasId() {
		return basId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param basId：主键ID；
	 */
	public void setBasId(Integer basId) {
		this.basId = basId;
	}

	/**
	 * 入职申请ID；
	 * 
	 * @return ：entryId 入职申请ID；
	 */
	public Integer getEntryId() {
		return entryId;
	}

	/**
	 * 入职申请ID；
	 * 
	 * @param entryId：入职申请ID；
	 */
	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	/**
	 * 性别，0为女，1为男；
	 * 
	 * @return ：empGender 性别，0为女，1为男；
	 */
	public Integer getEmpGender() {
		return empGender;
	}

	/**
	 * 性别，0为女，1为男；
	 * 
	 * @param empGender：性别，0为女，1为男；
	 */
	public void setEmpGender(Integer empGender) {
		this.empGender = empGender;
	}

	/**
	 * 民族；
	 * 
	 * @return ：empRace 民族；
	 */
	public Integer getEmpRace() {
		return empRace;
	}

	/**
	 * 民族；
	 * 
	 * @param empRace：民族；
	 */
	public void setEmpRace(Integer empRace) {
		this.empRace = empRace;
	}

	/**
	 * 出生日期；
	 * 
	 * @return ：empBirth 出生日期；
	 */
	public Date getEmpBirth() {
		return empBirth;
	}

	/**
	 * 出生日期；
	 * 
	 * @param empBirth：出生日期；
	 */
	public void setEmpBirth(Date empBirth) {
		this.empBirth = empBirth;
	}

	/**
	 * 申请人生日（农历）；
	 * 
	 * @return ：empBirthday 申请人生日（农历）；
	 */
	public String getEmpBirthday() {
		return empBirthday;
	}

	/**
	 * 申请人生日（农历）；
	 * 
	 * @param empBirthday：申请人生日（农历）；
	 */
	public void setEmpBirthday(String empBirthday) {
		this.empBirthday = empBirthday == null ? null : empBirthday.trim();
	}

	/**
	 * 身份证号码；
	 * 
	 * @return ：empCode 身份证号码；
	 */
	public String getEmpCode() {
		return empCode;
	}

	/**
	 * 身份证号码；
	 * 
	 * @param empCode：身份证号码；
	 */
	public void setEmpCode(String empCode) {
		this.empCode = empCode == null ? null : empCode.trim();
	}

	/**
	 * 身份证地址；
	 * 
	 * @return ：empCodeAddress 身份证地址；
	 */
	public String getEmpCodeAddress() {
		return empCodeAddress;
	}

	/**
	 * 身份证地址；
	 * 
	 * @param empCodeAddress：身份证地址；
	 */
	public void setEmpCodeAddress(String empCodeAddress) {
		this.empCodeAddress = empCodeAddress == null ? null : empCodeAddress.trim();
	}

	/**
	 * 籍贯省；
	 * 
	 * @return ：empNativeProvince 籍贯省；
	 */
	public Integer getEmpNativeProvince() {
		return empNativeProvince;
	}

	/**
	 * 籍贯省；
	 * 
	 * @param empNativeProvince：籍贯省；
	 */
	public void setEmpNativeProvince(Integer empNativeProvince) {
		this.empNativeProvince = empNativeProvince;
	}

	/**
	 * 籍贯市；
	 * 
	 * @return ：empNativeCity 籍贯市；
	 */
	public Integer getEmpNativeCity() {
		return empNativeCity;
	}

	/**
	 * 籍贯市；
	 * 
	 * @param empNativeCity：籍贯市；
	 */
	public void setEmpNativeCity(Integer empNativeCity) {
		this.empNativeCity = empNativeCity;
	}

	/**
	 * 籍贯；
	 * 
	 * @return ：empNative 籍贯；
	 */
	public String getEmpNative() {
		return empNative;
	}

	/**
	 * 籍贯；
	 * 
	 * @param empNative：籍贯；
	 */
	public void setEmpNative(String empNative) {
		this.empNative = empNative == null ? null : empNative.trim();
	}

	/**
	 * 学历；
	 * 
	 * @return ：empEducation 学历；
	 */
	public Integer getEmpEducation() {
		return empEducation;
	}

	/**
	 * 学历；
	 * 
	 * @param empEducation：学历；
	 */
	public void setEmpEducation(Integer empEducation) {
		this.empEducation = empEducation;
	}

	/**
	 * 毕业院校；
	 * 
	 * @return ：empCollege 毕业院校；
	 */
	public String getEmpCollege() {
		return empCollege;
	}

	/**
	 * 毕业院校；
	 * 
	 * @param empCollege：毕业院校；
	 */
	public void setEmpCollege(String empCollege) {
		this.empCollege = empCollege == null ? null : empCollege.trim();
	}

	/**
	 * 专业；
	 * 
	 * @return ：empMajor 专业；
	 */
	public String getEmpMajor() {
		return empMajor;
	}

	/**
	 * 专业；
	 * 
	 * @param empMajor：专业；
	 */
	public void setEmpMajor(String empMajor) {
		this.empMajor = empMajor == null ? null : empMajor.trim();
	}

	/**
	 * 其他学历；
	 * 
	 * @return ：empEducationOther 其他学历；
	 */
	public String getEmpEducationOther() {
		return empEducationOther;
	}

	/**
	 * 其他学历；
	 * 
	 * @param empEducationOther：其他学历；
	 */
	public void setEmpEducationOther(String empEducationOther) {
		this.empEducationOther = empEducationOther == null ? null : empEducationOther.trim();
	}

	/**
	 * 学历证明文件；
	 *
	 * @return ：empEducationFile 学历证明文件；
	 */
	public String getEmpEducationFile() {
		return empEducationFile;
	}

	/**
	 * 学历证明文件；
	 *
	 * @param empEducationFile：学历证明文件；
	 */
	public void setEmpEducationFile(String empEducationFile) {
		this.empEducationFile = empEducationFile;
	}

	/**
	 * 以往工作履历；
	 * 
	 * @return ：empExperience 以往工作履历；
	 */
	public String getEmpExperience() {
		return empExperience;
	}

	/**
	 * 以往工作履历；
	 * 
	 * @param empExperience：以往工作履历；
	 */
	public void setEmpExperience(String empExperience) {
		this.empExperience = empExperience == null ? null : empExperience.trim();
	}

	/**
	 * 离职证明文件；
	 *
	 * @return ：empExperienceFile 离职证明文件；
	 */
	public String getEmpExperienceFile() {
		return empExperienceFile;
	}

	/**
	 * 离职证明文件；
	 *
	 * @param empExperienceFile：离职证明文件；
	 */
	public void setEmpExperienceFile(String empExperienceFile) {
		this.empExperienceFile = empExperienceFile;
	}

	/**
	 * 试用期开始；
	 * 
	 * @return ：trialBegin 试用期开始；
	 */
	public Date getTrialBegin() {
		return trialBegin;
	}

	/**
	 * 试用期开始；
	 * 
	 * @param trialBegin：试用期开始；
	 */
	public void setTrialBegin(Date trialBegin) {
		this.trialBegin = trialBegin;
	}

	/**
	 * 试用期结束；
	 * 
	 * @return ：trialEnd 试用期结束；
	 */
	public Date getTrialEnd() {
		return trialEnd;
	}

	/**
	 * 试用期结束；
	 * 
	 * @param trialEnd：试用期结束；
	 */
	public void setTrialEnd(Date trialEnd) {
		this.trialEnd = trialEnd;
	}

	/**
	 * 推荐人ID；
	 * 
	 * @return ：empRelative 推荐人ID；
	 */
	public Integer getEmpRelative() {
		return empRelative;
	}

	/**
	 * 推荐人ID；
	 * 
	 * @param empRelative：推荐人ID；
	 */
	public void setEmpRelative(Integer empRelative) {
		this.empRelative = empRelative;
	}

	/**
	 * 推荐人姓名；
	 * 
	 * @return ：empRelativeName 推荐人姓名；
	 */
	public String getEmpRelativeName() {
		return empRelativeName;
	}

	/**
	 * 推荐人姓名；
	 * 
	 * @param empRelativeName：推荐人姓名；
	 */
	public void setEmpRelativeName(String empRelativeName) {
		this.empRelativeName = empRelativeName;
	}

	/**
	 * 推荐人联系电话；
	 * 
	 * @return ：empRelativePhone 推荐人联系电话；
	 */
	public String getEmpRelativePhone() {
		return empRelativePhone;
	}

	/**
	 * 推荐人联系电话；
	 * 
	 * @param empRelativePhone：推荐人联系电话；
	 */
	public void setEmpRelativePhone(String empRelativePhone) {
		this.empRelativePhone = empRelativePhone;
	}

	/**
	 * 与推荐人关系；
	 * 
	 * @return ：empRelativeRelation 与推荐人关系；
	 */
	public String getEmpRelativeRelation() {
		return empRelativeRelation;
	}

	/**
	 * 与推荐人关系；
	 * 
	 * @param empRelativeRelation：与推荐人关系；
	 */
	public void setEmpRelativeRelation(String empRelativeRelation) {
		this.empRelativeRelation = empRelativeRelation == null ? null : empRelativeRelation.trim();
	}

	/**
	 * 入职日期；
	 * 
	 * @return ：empDate 入职日期；
	 */
	public Date getEmpDate() {
		return empDate;
	}

	/**
	 * 入职日期；
	 * 
	 * @param empDate：入职日期；
	 */
	public void setEmpDate(Date empDate) {
		this.empDate = empDate;
	}

	/**
	 * 离职日期；
	 * 
	 * @return ：empLeaveDate 离职日期；
	 */
	public Date getEmpLeaveDate() {
		return empLeaveDate;
	}

	/**
	 * 离职日期；
	 * 
	 * @param empLeaveDate：离职日期；
	 */
	public void setEmpLeaveDate(Date empLeaveDate) {
		this.empLeaveDate = empLeaveDate;
	}


	public Integer getEmpSon() {
		return empSon;
	}

	public void setEmpSon(Integer empSon) {
		this.empSon = empSon;
	}

	public Integer getEmpGirl() {
		return empGirl;
	}

	public void setEmpGirl(Integer empGirl) {
		this.empGirl = empGirl;
	}

	public Integer getEmpBrother() {
		return empBrother;
	}

	public void setEmpBrother(Integer empBrother) {
		this.empBrother = empBrother;
	}

	public Integer getEmpYoungerBrother() {
		return empYoungerBrother;
	}

	public void setEmpYoungerBrother(Integer empYoungerBrother) {
		this.empYoungerBrother = empYoungerBrother;
	}

	public Integer getEmpSister() {
		return empSister;
	}

	public void setEmpSister(Integer empSister) {
		this.empSister = empSister;
	}

	public Integer getEmpYoungerSister() {
		return empYoungerSister;
	}

	public void setEmpYoungerSister(Integer empYoungerSister) {
		this.empYoungerSister = empYoungerSister;
	}

	@Override
	public String toString() {
		return "EmployeeBasic{" +
				"basId=" + basId +
				", entryId=" + entryId +
				", empGender=" + empGender +
				", empRace=" + empRace +
				", empBirth=" + empBirth +
				", empBirthday='" + empBirthday + '\'' +
				", empCode='" + empCode + '\'' +
				", empCodeAddress='" + empCodeAddress + '\'' +
				", empNativeProvince=" + empNativeProvince +
				", empNativeCity=" + empNativeCity +
				", empNative='" + empNative + '\'' +
				", empEducation=" + empEducation +
				", empCollege='" + empCollege + '\'' +
				", empMajor='" + empMajor + '\'' +
				", empEducationOther='" + empEducationOther + '\'' +
				", empEducationFile='" + empEducationFile + '\'' +
				", empExperience='" + empExperience + '\'' +
				", empExperienceFile='" + empExperienceFile + '\'' +
				", trialBegin=" + trialBegin +
				", trialEnd=" + trialEnd +
				", empRelative=" + empRelative +
				", empRelativeName='" + empRelativeName + '\'' +
				", empRelativePhone='" + empRelativePhone + '\'' +
				", empRelativeRelation='" + empRelativeRelation + '\'' +
				", empDate=" + empDate +
				", empLeaveDate=" + empLeaveDate +
				", empSon=" + empSon +
				", empGirl=" + empGirl +
				", empBrother=" + empBrother +
				", empYoungerBrother=" + empYoungerBrother +
				", empSister=" + empSister +
				", empYoungerSister=" + empYoungerSister +
				'}';
	}
}