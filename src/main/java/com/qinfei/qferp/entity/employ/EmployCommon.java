package com.qinfei.qferp.entity.employ;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IEmployData;

/**
 * 通用的属性类；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/1 0001 9:49；
 */
public class EmployCommon implements Serializable {
	/**
	 * 序列化ID；
	 */
	private static final long serialVersionUID = 4760047586406895400L;

	/**
	 * 创建人ID；
	 */
	private Integer createId;

	/**
	 * 创建人名称；
	 */
	private String createName;

	/**
	 * 创建时间；
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
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
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	/**
	 * 状态，-1为删除；
	 */
	private Integer state;

	/**
	 * 版本号，每次更新会+1；
	 */
	private Integer version;

	/**
	 * 创建人ID；
	 *
	 * @return ：createId，创建人ID；
	 */
	public Integer getCreateId() {
		return createId;
	}

	/**
	 * 创建人ID；
	 *
	 * @param createId：
	 *            创建人ID；
	 */
	public void setCreateId(Integer createId) {
		this.createId = createId;
	}

	/**
	 * 创建人名称；
	 *
	 * @return ：createName，创建人名称；
	 */
	public String getCreateName() {
		return createName;
	}

	/**
	 * 创建人名称；
	 *
	 * @param createName：
	 *            创建人名称；
	 */
	public void setCreateName(String createName) {
		this.createName = createName == null ? null : createName.trim();
	}

	/**
	 * 创建时间；
	 *
	 * @return ：createTime，创建时间；
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * 创建时间；
	 *
	 * @param createTime：
	 *            创建时间；
	 */
    void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * 更新人ID；
	 *
	 * @return ：updateId，更新人ID；
	 */
	public Integer getUpdateId() {
		return updateId;
	}

	/**
	 * 更新人ID；
	 *
	 * @param updateId：
	 *            更新人ID；
	 */
    void setUpdateId(Integer updateId) {
		this.updateId = updateId;
	}

	/**
	 * 更新人名称；
	 *
	 * @return ：updateName，更新人名称；
	 */
	public String getUpdateName() {
		return updateName;
	}

	/**
	 * 更新人名称；
	 *
	 * @param updateName：
	 *            更新人名称；
	 */
    void setUpdateName(String updateName) {
		this.updateName = updateName == null ? null : updateName.trim();
	}

	/**
	 * 更新时间；
	 *
	 * @return ：updateTime，更新时间；
	 */
	public Date getUpdateTime() {
		return updateTime;
	}

	/**
	 * 更新时间；
	 *
	 * @param updateTime：
	 *            更新时间；
	 */
    void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 状态，-1为删除；
	 *
	 * @return ：state，状态，-1为删除；
	 */
	public Integer getState() {
		return state;
	}

	/**
	 * 状态，-1为删除；
	 *
	 * @param state：
	 *            状态，-1为删除；
	 */
	public void setState(Integer state) {
		this.state = state;
	}

	/**
	 * 版本号，每次更新会+1；
	 *
	 * @return ：version，版本号，每次更新会+1；
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * 版本号，每次更新会+1；
	 *
	 * @param version：
	 *            版本号，每次更新会+1；
	 */
    void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * 设置数据的创建信息；
	 */
	public void setCreateInfo() {
		// 获取登录人信息；
		User user = AppUtil.getUser();
		user = user == null ? new User() : user;
		// 登记人信息；
		this.setCreateId(user.getId());
		this.setCreateName(user.getName());
		this.setCreateTime(new Date());
		// 更新人信息不允许修改；
		this.setUpdateId(null);
		this.setUpdateName(null);
		this.setUpdateTime(null);
		this.setState(IEmployData.DATA_NORMAL);
		this.setVersion(0);
	}

	/**
	 * 设置数据的更新信息；
	 */
	public void setUpdateInfo() {
		// 获取登录人信息；
		User user = AppUtil.getUser();
		user = user == null ? new User() : user;
		// 创建人信息不允许修改；
		this.setCreateId(null);
		this.setCreateName(null);
		this.setCreateTime(null);
		// 更新人信息；
		this.setUpdateId(user.getId());
		this.setUpdateName(user.getName());
		this.setUpdateTime(new Date());
		this.setState(null);
		this.setVersion(null);
	}

	@Override
	public String toString() {
		return "EmployCommon{" +
				"createId=" + createId +
				", createName='" + createName + '\'' +
				", createTime=" + createTime +
				", updateId=" + updateId +
				", updateName='" + updateName + '\'' +
				", updateTime=" + updateTime +
				", state=" + state +
				", version=" + version +
				'}';
	}
}