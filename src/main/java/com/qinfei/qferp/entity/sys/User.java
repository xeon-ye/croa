package com.qinfei.qferp.entity.sys;

import java.io.Serializable;
import java.util.*;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Getter;
import lombok.Setter;

@Table(name = "sys_user")
@Getter
@Setter
public class User implements Serializable {

	@Id
	private Integer id;
	private String userName;
	private String password;
	private String name;
	private String image;
	private Integer sex;
	private String phone;
	private String qq;
	private String wechat;
	private String email;
	private Integer isMgr;
	private Integer mgrId;
	private String remark;
	private Integer state;
	private Integer creator;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	private Integer updateUserId;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date loginTime;
	private String loginIp;
	private Integer failNum;
	private String no;
	private String mac;
	private Integer deptId;
	private String deptName;
	// 职位ID；
	private Integer postId;
	// 职位名称；
	private String postName;
	// 公司代码；
	private String companyCode;
	private Integer saveCustNum;
	private Integer protectedCustNum;
	@Transient
	private Set deptIdSet;// 存储所有自己的部门及子部门id
	private String sessionId;
	// @Relate(name = Role.class, fkName = "role_id")
	@Transient
	private List<Role> roles = new ArrayList<>();
	/**
	 * 是否媒介
	 */
	@Transient
	private boolean isMJ = false;
	/**
	 * 是否业务
	 */
	@Transient
	private boolean isYW = false;
	/**
	 * 用户对应的部门
	 */
	// @Relate(name = Dept.class, fkName = "dept_id")
	@Transient
	private Dept dept = new Dept();

	// 用户是否有当前整个部门的权限
	@Transient
	private boolean currentDeptQx = false;
	// 用户是否有当前公司的权限
	@Transient
	public boolean currentCompanyQx = false;

	@Transient
	private String message;

	@Transient
	private String verifyCode;
	
	
	
	private Integer handoverState;


//	public Integer getId() {
//		return id;
//	}
//
//	public void setId(Integer id) {
//		this.id = id;
//	}
//
//	public String getUserName() {
//		return userName;
//	}
//
//	public void setUserName(String userName) {
//		this.userName = userName;
//	}
//
//	public String getPassword() {
//		return password;
//	}
//
//	public void setPassword(String password) {
//		this.password = password;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public String getImage() {
//		return image;
//	}
//
//	public void setImage(String image) {
//		this.image = image;
//	}
//
//	public Integer getSex() {
//		return sex;
//	}
//
//	public void setSex(Integer sex) {
//		this.sex = sex;
//	}
//
//	public String getPhone() {
//		return phone;
//	}
//
//	public void setPhone(String phone) {
//		this.phone = phone;
//	}
//
//	public String getQq() {
//		return qq;
//	}
//
//	public void setQq(String qq) {
//		this.qq = qq;
//	}
//
//	public String getWechat() {
//		return wechat;
//	}
//
//	public void setWechat(String wechat) {
//		this.wechat = wechat;
//	}
//
//	public String getEmail() {
//		return email;
//	}
//
//	public void setEmail(String email) {
//		this.email = email;
//	}
//
//	public Integer getIsMgr() {
//		return isMgr;
//	}
//
//	public void setIsMgr(Integer isMgr) {
//		this.isMgr = isMgr;
//	}
//
//	public Integer getMgrId() {
//		return mgrId;
//	}
//
//	public void setMgrId(Integer mgrId) {
//		this.mgrId = mgrId;
//	}
//
//	public String getRemark() {
//		return remark;
//	}
//
//	public void setRemark(String remark) {
//		this.remark = remark;
//	}
//
//	public Integer getState() {
//		return state;
//	}
//
//	public void setState(Integer state) {
//		this.state = state;
//	}
//
//	public Integer getCreator() {
//		return creator;
//	}
//
//	public void setCreator(Integer creator) {
//		this.creator = creator;
//	}
//
//	public Date getCreateTime() {
//		return createTime;
//	}
//
//	public void setCreateTime(Date createTime) {
//		this.createTime = createTime;
//	}
//
//	public Integer getUpdateUserId() {
//		return updateUserId;
//	}
//
//	public void setUpdateUserId(Integer updateUserId) {
//		this.updateUserId = updateUserId;
//	}
//
//	public Date getUpdateTime() {
//		return updateTime;
//	}
//
//	public void setUpdateTime(Date updateTime) {
//		this.updateTime = updateTime;
//	}
//
//	public Date getLoginTime() {
//		return loginTime;
//	}
//
//	public void setLoginTime(Date loginTime) {
//		this.loginTime = loginTime;
//	}
//
//	public String getLoginIp() {
//		return loginIp;
//	}
//
//	public void setLoginIp(String loginIp) {
//		this.loginIp = loginIp;
//	}
//
//	public Integer getFailNum() {
//		return failNum;
//	}
//
//	public void setFailNum(Integer failNum) {
//		this.failNum = failNum;
//	}
//
//	public String getNo() {
//		return no;
//	}
//
//	public void setNo(String no) {
//		this.no = no;
//	}
//
//	public String getMac() {
//		return mac;
//	}
//
//	public void setMac(String mac) {
//		this.mac = mac;
//	}
//
//	public List<Role> getRoles() {
//		return roles;
//	}
//
//	public void setRoles(List<Role> roles) {
//		this.roles = roles;
//	}
//
//	public Dept getDept() {
//		return dept;
//	}
//
//	public void setDept(Dept dept) {
//		this.dept = dept;
//	}
//
//	public Integer getDeptId() {
//		return deptId;
//	}
//
//	public void setDeptId(Integer deptId) {
//		this.deptId = deptId;
//	}
//
//	public String getDeptName() {
//		return deptName;
//	}
//
//	public void setDeptName(String deptName) {
//		this.deptName = deptName;
//	}
//
//	public Integer getPostId() {
//		return postId;
//	}
//
//	public void setPostId(Integer postId) {
//		this.postId = postId;
//	}
//
//	public String getPostName() {
//		return postName;
//	}
//
//	public void setPostName(String postName) {
//		this.postName = postName;
//	}
//
//	public String getCompanyCode() {
//		return companyCode;
//	}
//
//	public void setCompanyCode(String companyCode) {
//		this.companyCode = companyCode;
//	}
//
//	public Set getDeptIdSet() {
//		return deptIdSet;
//	}
//
//	public void setDeptIdSet(Set deptIdSet) {
//		this.deptIdSet = deptIdSet;
//	}
//
//	public boolean isMJ() {
//		return isMJ;
//	}
//
//	public void setMJ(boolean MJ) {
//		isMJ = MJ;
//	}
//
	public boolean getCurrentDeptQx() {
		return currentDeptQx;
	}

	public void setCurrentDeptQx(boolean currentDeptQx) {
		this.currentDeptQx = currentDeptQx;
	}

//	public boolean getCurrentCompanyQx() {
//		return currentCompanyQx;
//	}
//
//	public void setCurrentCompanyQx(boolean currentCompanyQx) {
//		this.currentCompanyQx = currentCompanyQx;
//	}
//
//	public String getMessage() {
//		return message;
//	}
//
//	public void setMessage(String message) {
//		this.message = message;
//	}
//
//	public Integer getSaveCustNum() {
//		return saveCustNum;
//	}
//
//	public void setSaveCustNum(Integer saveCustNum) {
//		this.saveCustNum = saveCustNum;
//	}
//
//	public Integer getProtectedCustNum() {
//		return protectedCustNum;
//	}
//
//	public void setProtectedCustNum(Integer protectedCustNum) {
//		this.protectedCustNum = protectedCustNum;
//	}
//
//	public boolean isYW() {
//		return isYW;
//	}
//
//	public void setYW(boolean YW) {
//		isYW = YW;
//	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		User user = (User) o;
		return isMJ == user.isMJ && isYW == user.isYW && Objects.equals(id, user.id) && Objects.equals(userName, user.userName) && Objects.equals(password, user.password) && Objects.equals(name, user.name) && Objects.equals(image, user.image)
				&& Objects.equals(sex, user.sex) && Objects.equals(phone, user.phone) && Objects.equals(qq, user.qq) && Objects.equals(wechat, user.wechat) && Objects.equals(email, user.email) && Objects.equals(isMgr, user.isMgr)
				&& Objects.equals(mgrId, user.mgrId) && Objects.equals(remark, user.remark) && Objects.equals(state, user.state) && Objects.equals(creator, user.creator) && Objects.equals(createTime, user.createTime)
				&& Objects.equals(updateUserId, user.updateUserId) && Objects.equals(updateTime, user.updateTime) && Objects.equals(loginTime, user.loginTime) && Objects.equals(loginIp, user.loginIp) && Objects.equals(failNum, user.failNum)
				&& Objects.equals(no, user.no) && Objects.equals(mac, user.mac) && Objects.equals(deptId, user.deptId) && Objects.equals(deptName, user.deptName) && Objects.equals(deptIdSet, user.deptIdSet) && Objects.equals(roles, user.roles)
				&& Objects.equals(dept, user.dept);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, userName, password, name, image, sex, phone, qq, wechat, email, isMgr, mgrId, remark, state, creator, createTime, updateUserId, updateTime, loginTime, loginIp, failNum, no, mac, deptId, deptName, deptIdSet, roles, isMJ, isYW,
				dept);
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", userName='" + userName + '\'' +
				", password='" + password + '\'' +
				", name='" + name + '\'' +
				", image='" + image + '\'' +
				", sex=" + sex +
				", phone='" + phone + '\'' +
				", qq='" + qq + '\'' +
				", wechat='" + wechat + '\'' +
				", email='" + email + '\'' +
				", isMgr=" + isMgr +
				", mgrId=" + mgrId +
				", remark='" + remark + '\'' +
				", state=" + state +
				", creator=" + creator +
				", createTime=" + createTime +
				", updateUserId=" + updateUserId +
				", updateTime=" + updateTime +
				", loginTime=" + loginTime +
				", loginIp='" + loginIp + '\'' +
				", failNum=" + failNum +
				", no='" + no + '\'' +
				", mac='" + mac + '\'' +
				", deptId=" + deptId +
				", deptName='" + deptName + '\'' +
				", postId=" + postId +
				", postName='" + postName + '\'' +
				", companyCode='" + companyCode + '\'' +
				", saveCustNum=" + saveCustNum +
				", protectedCustNum=" + protectedCustNum +
				'}';
	}
}
