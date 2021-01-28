package com.qinfei.qferp.entity.employ;

/**
 * 员工轨迹实体类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public class EmployeeTrajectory extends EmployeeCommon {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 5077559710162060778L;

	/**
	 * 主键ID；
	 */
	private Integer trajId;

	/**
	 * 入职申请ID；
	 */
	private Integer entryId;

	/**
	 * 操作内容；
	 */
	private String empContent;

	/**
	 * 事务类型，0为入职，1为录用，2为转正，3为离职，4为调岗，5为交接，为其他；
	 */
	private Integer empTransaction;

	/**
	 * 操作类型，0为提起申请，1为申请通过，2为申请被拒，3为内容变更；
	 */
	private Integer empOperate;

	/**
	 * 主键ID；
	 * 
	 * @return ：trajId 主键ID；
	 */
	public Integer getTrajId() {
		return trajId;
	}

	/**
	 * 主键ID；
	 * 
	 * @param trajId：主键ID；
	 */
	public void setTrajId(Integer trajId) {
		this.trajId = trajId;
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
	 * 操作内容；
	 *
	 * @return ：empContent 操作内容；
	 */
	public String getEmpContent() {
		return empContent;
	}

	/**
	 * 操作内容；
	 * 
	 * @param empContent：操作内容；
	 */
	public void setEmpContent(String empContent) {
		this.empContent = empContent == null ? null : empContent.trim();
	}

	/**
	 * 事务类型，0为入职，1为录用，2为转正，3为离职，4为调岗，5为其他；
	 * 
	 * @return ：empTransaction 事务类型，0为入职，1为录用，2为转正，3为离职，4为调岗，5为其他；
	 */
	public Integer getEmpTransaction() {
		return empTransaction;
	}

	/**
	 * 事务类型，0为入职，1为录用，2为转正，3为离职，4为调岗，5为其他；
	 * 
	 * @param empTransaction：事务类型，0为入职，1为录用，2为转正，3为离职，4为调岗，5为其他；
	 */
	public void setEmpTransaction(Integer empTransaction) {
		this.empTransaction = empTransaction;
	}

	/**
	 * 操作类型，0为提起申请，1为申请通过，2为申请被拒，3为内容变更；
	 * 
	 * @return ：empOperate 操作类型，0为提起申请，1为申请通过，2为申请被拒，3为内容变更；
	 */
	public Integer getEmpOperate() {
		return empOperate;
	}

	/**
	 * 操作类型，0为提起申请，1为申请通过，2为申请被拒，3为内容变更；
	 * 
	 * @param empOperate：操作类型，0为提起申请，1为申请通过，2为申请被拒，3为内容变更；
	 */
	public void setEmpOperate(Integer empOperate) {
		this.empOperate = empOperate;
	}

	@Override
	public String toString() {
		return "EmployeeTrajectory{" +
				"trajId=" + trajId +
				", entryId=" + entryId +
				", empContent='" + empContent + '\'' +
				", empTransaction=" + empTransaction +
				", empOperate=" + empOperate +
				'}';
	}
}