package com.qinfei.qferp.entity.employ;

/**
 * 入职申请的家庭婚姻信息实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployEntryFamily extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -4483089547342602589L;

	/**
	 * 主键ID；
	 */
	private Integer famId;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 成员姓名；
	 */
	private String famName;

	/**
	 * 成员年龄；
	 */
	private Integer famAge;

	/**
	 * 成员职业；
	 */
	private String famProfession;

	/**
	 * 成员所在单位；
	 */
	private String famUnit;

	/**
	 * 成员生日；
	 */
	private String famBirthday;

	private String famHealth; //健康状态

	/**
	 * 成员备注；
	 */
	private String famDesc;

	/**
	 * 与成员的关系；
	 */
	private Integer famRelation;

	/**
	 * 主键ID；
	 * 
	 * @return ：famId 主键ID；
	 */
	public Integer getFamId() {
		return famId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param famId：主键ID；
	 */
	public void setFamId(Integer famId) {
		this.famId = famId;
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
	 * 成员姓名；
	 * 
	 * @return ：famName 成员姓名；
	 */
	public String getFamName() {
		return famName;
	}

	/**
	 * 成员姓名；
	 * 
	 * @param famName：成员姓名；
	 */
	public void setFamName(String famName) {
		this.famName = famName == null ? null : famName.trim();
	}

	/**
	 * 成员年龄；
	 * 
	 * @return ：famAge 成员年龄；
	 */
	public Integer getFamAge() {
		return famAge;
	}

	/**
	 * 成员年龄；
	 * 
	 * @param famAge：成员年龄；
	 */
	public void setFamAge(Integer famAge) {
		this.famAge = famAge;
	}

	/**
	 * 成员职业；
	 * 
	 * @return ：famProfession 成员职业；
	 */
	public String getFamProfession() {
		return famProfession;
	}

	/**
	 * 成员职业；
	 * 
	 * @param famProfession：成员职业；
	 */
	public void setFamProfession(String famProfession) {
		this.famProfession = famProfession == null ? null : famProfession.trim();
	}

	/**
	 * 成员所在单位；
	 * 
	 * @return ：famUnit 成员所在单位；
	 */
	public String getFamUnit() {
		return famUnit;
	}

	/**
	 * 成员所在单位；
	 * 
	 * @param famUnit：成员所在单位；
	 */
	public void setFamUnit(String famUnit) {
		this.famUnit = famUnit == null ? null : famUnit.trim();
	}

	/**
	 * 成员生日；
	 * 
	 * @return ：famBirthday 成员生日；
	 */
	public String getFamBirthday() {
		return famBirthday;
	}

	/**
	 * 成员生日；
	 * 
	 * @param famBirthday：成员生日；
	 */
	public void setFamBirthday(String famBirthday) {
		this.famBirthday = famBirthday == null ? null : famBirthday.trim();
	}

	/**
	 * 成员备注；
	 * 
	 * @return ：famDesc 成员备注；
	 */
	public String getFamDesc() {
		return famDesc;
	}

	/**
	 * 成员备注；
	 * 
	 * @param famDesc：成员备注；
	 */
	public void setFamDesc(String famDesc) {
		this.famDesc = famDesc == null ? null : famDesc.trim();
	}

	/**
	 * 与成员的关系；
	 * 
	 * @return ：famRelation 与成员的关系；
	 */
	public Integer getFamRelation() {
		return famRelation;
	}

	/**
	 * 与成员的关系；
	 * 
	 * @param famRelation：与成员的关系；
	 */
	public void setFamRelation(Integer famRelation) {
		this.famRelation = famRelation;
	}

	public String getFamHealth() {
		return famHealth;
	}

	public void setFamHealth(String famHealth) {
		this.famHealth = famHealth;
	}

	@Override
	public String toString() {
		return "EmployEntryFamily{" +
				"famId=" + famId +
				", entryId=" + entryId +
				", famName='" + famName + '\'' +
				", famAge=" + famAge +
				", famProfession='" + famProfession + '\'' +
				", famUnit='" + famUnit + '\'' +
				", famBirthday='" + famBirthday + '\'' +
				", famHealth='" + famHealth + '\'' +
				", famDesc='" + famDesc + '\'' +
				", famRelation=" + famRelation +
				'}';
	}
}