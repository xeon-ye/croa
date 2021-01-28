package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 入职申请的工作经历实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployEntryExperience extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = -2097496402501287479L;

	/**
	 * 主键ID；
	 */
	private Integer expId;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 开始日期；
	 */
	private Date expStart;

	/**
	 * 结束日期；
	 */
	private Date expEnd;

	/**
	 * 公司名称；
	 */
	private String expCompany;

	/**
	 * 地点；
	 */
	private String expLocation;

	/**
	 * 职务；
	 */
	private String expProfession;

	/**
	 * 薪资待遇；
	 */
	private String expSalary;

	/**
	 * 证明人及联系电话；
	 */
	private String expContactor;

	/**
	 * 离职原因；
	 */
	private String expResignReason;

	/**
	 * 主键ID；
	 * 
	 * @return ：expId 主键ID；
	 */
	public Integer getExpId() {
		return expId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param expId：主键ID；
	 */
	public void setExpId(Integer expId) {
		this.expId = expId;
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
	 * 开始日期；
	 * 
	 * @return ：expStart 开始日期；
	 */
	public Date getExpStart() {
		return expStart;
	}

	/**
	 * 开始日期；
	 * 
	 * @param expStart：开始日期；
	 */
	public void setExpStart(Date expStart) {
		this.expStart = expStart;
	}

	/**
	 * 结束日期；
	 * 
	 * @return ：expEnd 结束日期；
	 */
	public Date getExpEnd() {
		return expEnd;
	}

	/**
	 * 结束日期；
	 * 
	 * @param expEnd：结束日期；
	 */
	public void setExpEnd(Date expEnd) {
		this.expEnd = expEnd;
	}

	/**
	 * 公司名称；
	 * 
	 * @return ：expCompany 公司名称；
	 */
	public String getExpCompany() {
		return expCompany;
	}

	/**
	 * 公司名称；
	 * 
	 * @param expCompany：公司名称；
	 */
	public void setExpCompany(String expCompany) {
		this.expCompany = expCompany == null ? null : expCompany.trim();
	}

	/**
	 * 地点；
	 * 
	 * @return ：expLocation 地点；
	 */
	public String getExpLocation() {
		return expLocation;
	}

	/**
	 * 地点；
	 * 
	 * @param expLocation：地点；
	 */
	public void setExpLocation(String expLocation) {
		this.expLocation = expLocation == null ? null : expLocation.trim();
	}

	/**
	 * 职务；
	 * 
	 * @return ：expProfession 职务；
	 */
	public String getExpProfession() {
		return expProfession;
	}

	/**
	 * 职务；
	 * 
	 * @param expProfession：职务；
	 */
	public void setExpProfession(String expProfession) {
		this.expProfession = expProfession == null ? null : expProfession.trim();
	}

	/**
	 * 薪资待遇；
	 * 
	 * @return ：expSalary 薪资待遇；
	 */
	public String getExpSalary() {
		return expSalary;
	}

	/**
	 * 薪资待遇；
	 * 
	 * @param expSalary：薪资待遇；
	 */
	public void setExpSalary(String expSalary) {
		this.expSalary = expSalary == null ? null : expSalary.trim();
	}

	/**
	 * 证明人及联系电话；
	 * 
	 * @return ：expContactor 证明人及联系电话；
	 */
	public String getExpContactor() {
		return expContactor;
	}

	/**
	 * 证明人及联系电话；
	 * 
	 * @param expContactor：证明人及联系电话；
	 */
	public void setExpContactor(String expContactor) {
		this.expContactor = expContactor == null ? null : expContactor.trim();
	}

	/**
	 * 离职原因；
	 * 
	 * @return ：expResignReason 离职原因；
	 */
	public String getExpResignReason() {
		return expResignReason;
	}

	/**
	 * 离职原因；
	 * 
	 * @param expResignReason：离职原因；
	 */
	public void setExpResignReason(String expResignReason) {
		this.expResignReason = expResignReason == null ? null : expResignReason.trim();
	}

	@Override
	public String toString() {
		return "EmployEntryExperience{" +
				"expId=" + expId +
				", entryId=" + entryId +
				", expStart=" + expStart +
				", expEnd=" + expEnd +
				", expCompany='" + expCompany + '\'' +
				", expLocation='" + expLocation + '\'' +
				", expProfession='" + expProfession + '\'' +
				", expSalary='" + expSalary + '\'' +
				", expContactor='" + expContactor + '\'' +
				", expResignReason='" + expResignReason + '\'' +
				'}';
	}
}