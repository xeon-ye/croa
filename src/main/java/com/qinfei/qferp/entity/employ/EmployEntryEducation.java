package com.qinfei.qferp.entity.employ;

import java.util.Date;

/**
 * 入职申请的教育经历实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployEntryEducation extends EmployCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 389288621841286027L;

	/**
	 * 主键ID；
	 */
	private Integer eduId;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 开始日期；
	 */
	private Date eduStart;

	/**
	 * 结束日期；
	 */
	private Date eduEnd;

	/**
	 * 教育机构名称；
	 */
	private String eduCollege;

	/**
	 * 地点；
	 */
	private String eduLocation;

	/**
	 * 学制时长；
	 */
	private Integer eduDuration;

	/**
	 * 专业；
	 */
	private String eduMajor;

	/**
	 * 学历；
	 */
	private String eduRecord;

	/**
	 * 最高学历，0为否，1为是；
	 */
	private Integer eduHighest;

	/**
	 * 主键ID；
	 * 
	 * @return ：eduId 主键ID；
	 */
	public Integer getEduId() {
		return eduId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param eduId：主键ID；
	 */
	public void setEduId(Integer eduId) {
		this.eduId = eduId;
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
	 * @return ：eduStart 开始日期；
	 */
	public Date getEduStart() {
		return eduStart;
	}

	/**
	 * 开始日期；
	 * 
	 * @param eduStart：开始日期；
	 */
	public void setEduStart(Date eduStart) {
		this.eduStart = eduStart;
	}

	/**
	 * 结束日期；
	 * 
	 * @return ：eduEnd 结束日期；
	 */
	public Date getEduEnd() {
		return eduEnd;
	}

	/**
	 * 结束日期；
	 * 
	 * @param eduEnd：结束日期；
	 */
	public void setEduEnd(Date eduEnd) {
		this.eduEnd = eduEnd;
	}

	/**
	 * 教育机构名称；
	 * 
	 * @return ：eduCollege 教育机构名称；
	 */
	public String getEduCollege() {
		return eduCollege;
	}

	/**
	 * 教育机构名称；
	 * 
	 * @param eduCollege：教育机构名称；
	 */
	public void setEduCollege(String eduCollege) {
		this.eduCollege = eduCollege == null ? null : eduCollege.trim();
	}

	/**
	 * 地点；
	 * 
	 * @return ：eduLocation 地点；
	 */
	public String getEduLocation() {
		return eduLocation;
	}

	/**
	 * 地点；
	 * 
	 * @param eduLocation：地点；
	 */
	public void setEduLocation(String eduLocation) {
		this.eduLocation = eduLocation == null ? null : eduLocation.trim();
	}

	/**
	 * 学制时长；
	 * 
	 * @return ：eduDuration 学制时长；
	 */
	public Integer getEduDuration() {
		return eduDuration;
	}

	/**
	 * 学制时长；
	 * 
	 * @param eduDuration：学制时长；
	 */
	public void setEduDuration(Integer eduDuration) {
		this.eduDuration = eduDuration;
	}

	/**
	 * 专业；
	 * 
	 * @return ：eduMajor 专业；
	 */
	public String getEduMajor() {
		return eduMajor;
	}

	/**
	 * 专业；
	 * 
	 * @param eduMajor：专业；
	 */
	public void setEduMajor(String eduMajor) {
		this.eduMajor = eduMajor == null ? null : eduMajor.trim();
	}

	/**
	 * 学历；
	 * 
	 * @return ：eduRecord 学历；
	 */
	public String getEduRecord() {
		return eduRecord;
	}

	/**
	 * 学历；
	 * 
	 * @param eduRecord：学历；
	 */
	public void setEduRecord(String eduRecord) {
		this.eduRecord = eduRecord == null ? null : eduRecord.trim();
	}

	/**
	 * 最高学历，0为否，1为是；
	 * 
	 * @return ：eduHighest 最高学历，0为否，1为是；
	 */
	public Integer getEduHighest() {
		return eduHighest;
	}

	/**
	 * 最高学历，0为否，1为是；
	 * 
	 * @param eduHighest：最高学历，0为否，1为是；
	 */
	public void setEduHighest(Integer eduHighest) {
		this.eduHighest = eduHighest;
	}

	@Override
	public String toString() {
		return "EmployEntryEducation{" +
				"eduId=" + eduId +
				", entryId=" + entryId +
				", eduStart=" + eduStart +
				", eduEnd=" + eduEnd +
				", eduCollege='" + eduCollege + '\'' +
				", eduLocation='" + eduLocation + '\'' +
				", eduDuration=" + eduDuration +
				", eduMajor='" + eduMajor + '\'' +
				", eduRecord='" + eduRecord + '\'' +
				", eduHighest=" + eduHighest +
				'}';
	}
}