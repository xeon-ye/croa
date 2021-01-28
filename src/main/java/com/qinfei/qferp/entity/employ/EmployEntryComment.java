package com.qinfei.qferp.entity.employ;

/**
 * 入职申请审核信息实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployEntryComment extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 9007652821119716600L;

	/**
	 * 主键ID；
	 */
	private Integer comId;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 评论类型，0为人事部，1为部门专业测试员，2为部门负责人；
	 */
	private Integer comType;

	/**
	 * 形象气质1-5分；
	 */
	private Integer comFigure;

	/**
	 * 沟通表达1-5分；
	 */
	private Integer comCommunicate;

	/**
	 * 应聘诚意1-5分；
	 */
	private Integer comFaith;

	/**
	 * 职业素质1-5分；
	 */
	private Integer comQuality;

	/**
	 * 工作经验1-5分；
	 */
	private Integer comExperience;

	/**
	 * 上述5项评分累加；
	 */
	private Integer comTotalScore;

	/**
	 * 面试评估建议；
	 */
	private String comAdvice;

	/**
	 * 身份证说明；
	 */
	private String comCode;

	/**
	 * 学历说明；
	 */
	private String comEducation;

	/**
	 * 户口说明；
	 */
	private String comResidence;

	/**
	 * 离职报告说明；
	 */
	private String comExperienceDesc;

	/**
	 * 照片说明；
	 */
	private String comImage;

	/**
	 * 体检报告说明；
	 */
	private String comReport;

	/**
	 * 其他说明；
	 */
	private String comOther;

	/**
	 * 状态，0为正常，5为已离职，-1为已删除；
	 */

	/**
	 * 主键ID；
	 * 
	 * @return ：comId 主键ID；
	 */
	public Integer getComId() {
		return comId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param comId：主键ID；
	 */
	public void setComId(Integer comId) {
		this.comId = comId;
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
	 * 评论类型，0为人事部，1为部门专业测试员，2为部门负责人；
	 * 
	 * @return ：comType 评论类型，0为人事部，1为部门专业测试员，2为部门负责人；
	 */
	public Integer getComType() {
		return comType;
	}

	/**
	 * 评论类型，0为人事部，1为部门专业测试员，2为部门负责人；
	 * 
	 * @param comType：评论类型，0为人事部，1为部门专业测试员，2为部门负责人；
	 */
	public void setComType(Integer comType) {
		this.comType = comType;
	}

	/**
	 * 形象气质1-5分；
	 * 
	 * @return ：comFigure 形象气质1-5分；
	 */
	public Integer getComFigure() {
		return comFigure;
	}

	/**
	 * 形象气质1-5分；
	 * 
	 * @param comFigure：形象气质1-5分；
	 */
	public void setComFigure(Integer comFigure) {
		this.comFigure = comFigure;
	}

	/**
	 * 沟通表达1-5分；
	 * 
	 * @return ：comCommunicate 沟通表达1-5分；
	 */
	public Integer getComCommunicate() {
		return comCommunicate;
	}

	/**
	 * 沟通表达1-5分；
	 * 
	 * @param comCommunicate：沟通表达1-5分；
	 */
	public void setComCommunicate(Integer comCommunicate) {
		this.comCommunicate = comCommunicate;
	}

	/**
	 * 应聘诚意1-5分；
	 * 
	 * @return ：comFaith 应聘诚意1-5分；
	 */
	public Integer getComFaith() {
		return comFaith;
	}

	/**
	 * 应聘诚意1-5分；
	 * 
	 * @param comFaith：应聘诚意1-5分；
	 */
	public void setComFaith(Integer comFaith) {
		this.comFaith = comFaith;
	}

	/**
	 * 职业素质1-5分；
	 * 
	 * @return ：comQuality 职业素质1-5分；
	 */
	public Integer getComQuality() {
		return comQuality;
	}

	/**
	 * 职业素质1-5分；
	 * 
	 * @param comQuality：职业素质1-5分；
	 */
	public void setComQuality(Integer comQuality) {
		this.comQuality = comQuality;
	}

	/**
	 * 工作经验1-5分；
	 * 
	 * @return ：comExperience 工作经验1-5分；
	 */
	public Integer getComExperience() {
		return comExperience;
	}

	/**
	 * 工作经验1-5分；
	 * 
	 * @param comExperience：工作经验1-5分；
	 */
	public void setComExperience(Integer comExperience) {
		this.comExperience = comExperience;
	}

	/**
	 * 上述5项评分累加；
	 * 
	 * @return ：comTotalScore 上述5项评分累加；
	 */
	public Integer getComTotalScore() {
		return comTotalScore;
	}

	/**
	 * 上述5项评分累加；
	 * 
	 * @param comTotalScore：上述5项评分累加；
	 */
	public void setComTotalScore(Integer comTotalScore) {
		this.comTotalScore = comTotalScore;
	}

	/**
	 * 面试评估建议；
	 * 
	 * @return ：comAdvice 面试评估建议；
	 */
	public String getComAdvice() {
		return comAdvice;
	}

	/**
	 * 面试评估建议；
	 * 
	 * @param comAdvice：面试评估建议；
	 */
	public void setComAdvice(String comAdvice) {
		this.comAdvice = comAdvice == null ? null : comAdvice.trim();
	}

	/**
	 * 身份证说明；
	 * 
	 * @return ：comCode 身份证说明；
	 */
	public String getComCode() {
		return comCode;
	}

	/**
	 * 身份证说明；
	 * 
	 * @param comCode：身份证说明；
	 */
	public void setComCode(String comCode) {
		this.comCode = comCode == null ? null : comCode.trim();
	}

	/**
	 * 学历说明；
	 *
	 * @return ：comEducation 学历说明；
	 */
	public String getComEducation() {
		return comEducation;
	}

	/**
	 * 学历说明；
	 *
	 * @param comEducation：学历说明；
	 */
	public void setComEducation(String comEducation) {
		this.comEducation = comEducation;
	}

	/**
	 * 户口说明；
	 * 
	 * @return ：comResidence 户口说明；
	 */
	public String getComResidence() {
		return comResidence;
	}

	/**
	 * 户口说明；
	 * 
	 * @param comResidence：户口说明；
	 */
	public void setComResidence(String comResidence) {
		this.comResidence = comResidence == null ? null : comResidence.trim();
	}

	/**
	 * 离职报告说明；
	 * 
	 * @return ：comExperienceDesc 离职报告说明；
	 */
	public String getComExperienceDesc() {
		return comExperienceDesc;
	}

	/**
	 * 离职报告说明；
	 * 
	 * @param comExperienceDesc：离职报告说明；
	 */
	public void setComExperienceDesc(String comExperienceDesc) {
		this.comExperienceDesc = comExperienceDesc == null ? null : comExperienceDesc.trim();
	}

	/**
	 * 照片说明；
	 * 
	 * @return ：comImage 照片说明；
	 */
	public String getComImage() {
		return comImage;
	}

	/**
	 * 照片说明；
	 * 
	 * @param comImage：照片说明；
	 */
	public void setComImage(String comImage) {
		this.comImage = comImage == null ? null : comImage.trim();
	}

	/**
	 * 体检报告说明；
	 * 
	 * @return ：comReport 体检报告说明；
	 */
	public String getComReport() {
		return comReport;
	}

	/**
	 * 体检报告说明；
	 * 
	 * @param comReport：体检报告说明；
	 */
	public void setComReport(String comReport) {
		this.comReport = comReport == null ? null : comReport.trim();
	}

	/**
	 * 其他说明；
	 * 
	 * @return ：comOther 其他说明；
	 */
	public String getComOther() {
		return comOther;
	}

	/**
	 * 其他说明；
	 * 
	 * @param comOther：其他说明；
	 */
	public void setComOther(String comOther) {
		this.comOther = comOther == null ? null : comOther.trim();
	}

	@Override
	public String toString() {
		return "EmployEntryComment{" +
				"comId=" + comId +
				", entryId=" + entryId +
				", comType=" + comType +
				", comFigure=" + comFigure +
				", comCommunicate=" + comCommunicate +
				", comFaith=" + comFaith +
				", comQuality=" + comQuality +
				", comExperience=" + comExperience +
				", comTotalScore=" + comTotalScore +
				", comAdvice='" + comAdvice + '\'' +
				", comCode='" + comCode + '\'' +
				", comEducation='" + comEducation + '\'' +
				", comResidence='" + comResidence + '\'' +
				", comExperienceDesc='" + comExperienceDesc + '\'' +
				", comImage='" + comImage + '\'' +
				", comReport='" + comReport + '\'' +
				", comOther='" + comOther + '\'' +
				'}';
	}
}