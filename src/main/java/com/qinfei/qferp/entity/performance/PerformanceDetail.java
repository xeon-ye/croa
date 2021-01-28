package com.qinfei.qferp.entity.performance;

import com.qinfei.qferp.entity.employ.EmployCommon;

/**
 * 个人评分明细实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0001 9:49；
 */
public class PerformanceDetail extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 140877253500621082L;
	/**
	 * 主键ID；
	 */
	private Integer detailId;

	/**
	 * 关联的个人评分ID；
	 */
	private Integer scoreId;

	/**
	 * 关联的考核项目ID；
	 */
	private Integer plateId;

	/**
	 * 项目层级，0为板块，1为项目，2为细则；
	 */
	private Integer plateLevel;

	/**
	 * 权重、分值；
	 */
	private Float plateProportion;

	/**
	 * 上级ID；
	 */
	private Integer plateParent;

	/**
	 * 考核内容；
	 */
	private String plateContent;

	/**
	 * 考核指标
	 */
	private String plateTarget;

	/**
	 * 考核需求
	 */
	private String plateDemand;

	/**
	 * 个人评分；
	 */
	private Float scoreSelf;

	/**
	 * 上级评分；
	 */
	private Float scoreLeader;

	/**
	 * 小组评分；
	 */
	private Float scoreGroup;

	/**
	 * 总分；
	 */
	private Float scoreTotal;

	/**
	 * 备注/绩效方案类型
	 */
	private String remark;

	/**
	 * 主键ID；
	 *
	 * @return ：detail_id 主键ID；
	 */
	public Integer getDetailId() {
		return detailId;
	}

	/**
	 * 主键ID；
	 *
	 * @param detailId：主键ID；
	 */
	public void setDetailId(Integer detailId) {
		this.detailId = detailId;
	}

	/**
	 * 关联的个人评分ID；
	 *
	 * @return ：score_id 关联的个人评分ID；
	 */
	public Integer getScoreId() {
		return scoreId;
	}

	/**
	 * 关联的个人评分ID；
	 *
	 * @param scoreId：关联的个人评分ID；
	 */
	public void setScoreId(Integer scoreId) {
		this.scoreId = scoreId;
	}

	/**
	 * 关联的考核项目ID；
	 *
	 * @return ：plate_id 关联的考核项目ID；
	 */
	public Integer getPlateId() {
		return plateId;
	}

	/**
	 * 关联的考核项目ID；
	 *
	 * @param plateId：关联的考核项目ID；
	 */
	public void setPlateId(Integer plateId) {
		this.plateId = plateId;
	}

	/**
	 * 项目层级，0为板块，1为项目，2为细则；
	 *
	 * @return ：plate_level 项目层级，0为板块，1为项目，2为细则；
	 */
	public Integer getPlateLevel() {
		return plateLevel;
	}

	/**
	 * 项目层级，0为板块，1为项目，2为细则；
	 *
	 * @param plateLevel：项目层级，0为板块，1为项目，2为细则；
	 */
	public void setPlateLevel(Integer plateLevel) {
		this.plateLevel = plateLevel;
	}

	/**
	 * 权重、分值；
	 *
	 * @return ：plate_proportion 权重、分值；
	 */
	public Float getPlateProportion() {
		return plateProportion;
	}

	/**
	 * 权重、分值；
	 *
	 * @param plateProportion：权重、分值；
	 */
	public void setPlateProportion(Float plateProportion) {
		this.plateProportion = plateProportion;
	}

	/**
	 * 上级ID；
	 *
	 * @return ：plate_parent 上级ID；
	 */
	public Integer getPlateParent() {
		return plateParent;
	}

	/**
	 * 上级ID；
	 *
	 * @param plateParent：上级ID；
	 */
	public void setPlateParent(Integer plateParent) {
		this.plateParent = plateParent;
	}

	/**
	 * 考核内容；
	 *
	 * @return ：plate_content 考核内容；
	 */
	public String getPlateContent() {
		return plateContent;
	}

	/**
	 * 考核内容；
	 *
	 * @param plateContent：考核内容；
	 */
	public void setPlateContent(String plateContent) {
		this.plateContent = plateContent == null ? null : plateContent.trim();
	}

	/**
	 * 个人评分；
	 *
	 * @return ：score_self 个人评分；
	 */
	public Float getScoreSelf() {
		return scoreSelf;
	}

	/**
	 * 个人评分；
	 *
	 * @param scoreSelf：个人评分；
	 */
	public void setScoreSelf(Float scoreSelf) {
		this.scoreSelf = scoreSelf;
	}

	/**
	 * 上级评分；
	 *
	 * @return ：score_leader 上级评分；
	 */
	public Float getScoreLeader() {
		return scoreLeader;
	}

	/**
	 * 上级评分；
	 *
	 * @param scoreLeader：上级评分；
	 */
	public void setScoreLeader(Float scoreLeader) {
		this.scoreLeader = scoreLeader;
	}

	/**
	 * 小组评分；
	 *
	 * @return ：score_group 小组评分；
	 */
	public Float getScoreGroup() {
		return scoreGroup;
	}

	/**
	 * 小组评分；
	 *
	 * @param scoreGroup：小组评分；
	 */
	public void setScoreGroup(Float scoreGroup) {
		this.scoreGroup = scoreGroup;
	}

	/**
	 * 总分；
	 *
	 * @return ：score_total 总分；
	 */
	public Float getScoreTotal() {
		return scoreTotal;
	}

	/**
	 * 总分；
	 *
	 * @param scoreTotal：总分；
	 */
	public void setScoreTotal(Float scoreTotal) {
		this.scoreTotal = scoreTotal;
	}

	public String getPlateTarget() {
		return plateTarget;
	}

	public void setPlateTarget(String plateTarget) {
		this.plateTarget = plateTarget;
	}

	public String getPlateDemand() {
		return plateDemand;
	}

	public void setPlateDemand(String plateDemand) {
		this.plateDemand = plateDemand;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		return "PerformanceDetail{" +
				"detailId=" + detailId +
				", scoreId=" + scoreId +
				", plateId=" + plateId +
				", plateLevel=" + plateLevel +
				", plateProportion=" + plateProportion +
				", plateParent=" + plateParent +
				", plateContent='" + plateContent + '\'' +
				", plateTarget='" + plateTarget + '\'' +
				", plateDemand='" + plateDemand + '\'' +
				", scoreSelf=" + scoreSelf +
				", scoreLeader=" + scoreLeader +
				", scoreGroup=" + scoreGroup +
				", scoreTotal=" + scoreTotal +
				", remark=" + remark +
				'}';
	}
}