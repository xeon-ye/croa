package com.qinfei.qferp.service.employ;

import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.employ.*;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 入职申请的业务接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:30；
 */
public interface IEmployEntryService {
	/**
	 * 保存或更新单条记录到数据库中；
	 * 
	 * @param record：入职申请对象；
	 * @param empCode：身份证号码；
	 * @return ：处理完毕的入职申请对象；
	 */
	EmployEntry saveOrUpdate(EmployEntry record, String empCode, String entryCompanyCode);

	/**
	 * 保存入职申请的基本信息；
	 * 
	 * @param data：返回给前端的数据；
	 * @param entry：提交的申请字符串；
	 */
	void saveBasic(ResponseData data, String entry);

	/**
	 * 保存或更新录用记录；
	 *
	 * @param data：返回给前端的数据；
	 * @param employeeHire：录用记录对象；
	 */
	void saveHire(ResponseData data, EmployeeHire employeeHire);

	/**
	 * 保存或更新薪资记录；
	 *
	 * @param data：返回给前端的数据；
	 * @param employeeSalary：薪资对象；
	 */
	void saveSalary(ResponseData data, EmployeeSalary employeeSalary);

	/**
	 * 保存或更新审核记录；
	 *
	 * @param data：返回给前端的数据；
	 * @param entryComment：评估信息对象；
	 * @param operate：操作类型，0为人事，1为测试；
	 */
	void saveComment(ResponseData data, EmployEntryComment entryComment, int operate);

	/**
	 * 保存入职申请的家庭婚姻信息；
	 * 
	 * @param data：返回给前端的数据；
	 * @param entryFamily：提交的家庭信息对象；
	 */
	void saveFamily(ResponseData data, EmployEntryFamily entryFamily);

	/**
	 * 保存入职申请的教育培训经历信息；
	 * 
	 * @param data：返回给前端的数据；
	 * @param entryEducation：提交的教育培训经历信息对象；
	 */
	void saveEducation(ResponseData data, EmployEntryEducation entryEducation);

	/**
	 * 保存入职申请的工作经历信息；
	 * 
	 * @param data：返回给前端的数据；
	 * @param entryExperience：提交的工作经历信息对象；
	 */
	void saveExperience(ResponseData data, EmployEntryExperience entryExperience);
	void saveExperienceInJob(ResponseData data, EmployEntryExperience entryExperience);

	/**
	 * 保存入职申请的其他入职信息；
	 * 
	 * @param data：返回给前端的数据；
	 * @param employEntry：提交的其他入职信息对象；
	 * @param empRelative：推荐人ID；
	 * @param empRelativeName：推荐人姓名；
	 * @param empRelativePhone：推荐人联系电话；
	 * @param empRelativeRelation：与推荐人的关系；
	 */
	void saveOther(ResponseData data, EmployEntry employEntry, Integer empRelative, String empRelativeName, String empRelativePhone, String empRelativeRelation);

	/**
	 * 提交审核入职审核流程；
	 * 
	 * @param data：返回给前端的数据；
	 * @param entryId：入职申请的ID；
	 * @param userId：审核人ID；
	 * @param userName：审核人姓名；
	 * @param deptId：审核人所在部门；
	 */
	void startEmploy(ResponseData data, int entryId, int userId, String userName, int deptId);

	/**
	 * 入职审批通过后，通知申请者；
	 *
	 * @param data：返回给前端的数据；
	 * @param entryId：入职申请的ID；
	 */
	void noticeEntry(ResponseData data, int entryId);

	/**
	 * 通用的审核流转；
	 * 
	 * @param data：返回给前端的数据；
	 * @param approveData：审核信息Json字符串；
	 */
	void completeApprove(ResponseData data, String approveData);

	/**
	 * 批量更新数据的状态；
	 *
	 * @param entryIds：入职申请ID数组；
	 * @param operate：操作类型，0为删除，1为存档备用（不予考虑），2为恢复，3为离职再入职；
	 * @return ：操作结果提示信息，0为异常，1为操作成功；
	 */
	int updateStateByBatchId(Integer[] entryIds, int operate);

	/**
	 * 根据员工ID更新数据的状态；
	 *
	 * @param empId：员工ID；
	 * @param operate：操作类型，0为删除，1为离职；
	 * @return ：操作影响的记录数；
	 */
	int updateStateByEmpId(int empId, int operate);

	/**
	 * 更新创建人信息；
	 *
	 * @param entryId：主键ID；
	 * @param userId：创建人ID；
	 * @param userName：创建人名称；
	 * @return ：操作影响的记录数；
	 */
	int updateCreateId(int entryId, int userId, String userName);

	/**
	 * 根据主键ID完善资料；
	 *
	 * @param jsonData：数据Json对象；
	 * @return ：操作结果提示信息；
	 */
	String completeEntry(JSONObject jsonData);

	/**
	 * 设置入职申请的教育培训经历信息的最高学历；
	 * 
	 * @param data：返回给前端的数据；
	 * @param entryId：提交的入职申请ID；
	 * @param eduId：提交的教育培训经历信息ID；
	 * @param eduCollege：提交的教育培训经历学校名称；
	 * @param eduMajor：提交的教育培训经历专业名称；
	 */
	void setEducationHighest(ResponseData data, int entryId, int eduId, String eduCollege, String eduMajor);

	/**
	 * 删除入职申请的家庭婚姻信息；
	 *
	 * @param entryId：提交的入职申请ID；
	 * @param famId：提交的家庭信息对象ID；
	 */
	void deleteFamily(int entryId, int famId);

	/**
	 * 删除入职申请的教育培训经历信息；
	 *
	 * @param entryId：提交的入职申请ID；
	 * @param eduId：提交的教育培训经历信息ID；
	 */
	void deleteEducation(int entryId, int eduId);

	/**
	 * 删除入职申请的工作经历信息；
	 *
	 * @param entryId：提交的入职申请ID；
	 * @param expId：提交的工作经历信息ID；
	 */
	void deleteExperience(int entryId, int expId);

	/**
	 * 入职申请的资料审核；
	 * 
	 * @param record：入职申请的审核信息；
	 * @param operate：操作类型，0为同意，1为拒绝；
	 */
	void approveEntry(EmployEntryComment record, int operate);

	/**
	 * 入职申请的流程更新状态；
	 *
	 * @param entryId：主键ID；
	 * @param code：当前使用的查询码；
	 * @param state：当前状态；
	 * @param taskId：任务ID；
	 * @param itemId：待办事项ID；
	 * @param agree：是否同意；
	 * @param desc：审核意见；
	 */
	void processEntry(int entryId, String code, int state, String taskId, Integer itemId, boolean agree, String desc);

	/**
	 * 根据主键ID查询该数据是否为待审核状态；
	 *
	 * @param entryId：主键ID；
	 * @return ：查询结果，true为验证通过，false为验证失败；
	 */
	boolean checkEnableUpdate(int entryId);

	/**
	 * 根据主键ID查询该数据时否为审核中状态；
	 *
	 * @param entryId：主键ID；
	 * @return ：统计数量；
	 */
	boolean checkEnableApprove(int entryId);

	/**
	 * 根据主键ID和状态查询该数据是否进入到资料审核阶段；
	 * 
	 * @param entryId：主键ID；
	 * @param entryComplete：是否资料完整0
	 *            或 1；
	 * @return 查询结果，true为验证通过，false为验证失败；
	 */
	boolean checkEnableComplete(int entryId, int entryComplete);

	/**
	 * 根据身份证号码查询是否为已填写入职申请的人员；
	 *
	 * @param entryId：主键ID；
	 * @param empCode：身份证号码；
	 * @return ：查询结果，true为验证通过，false为已存在；
	 */
	boolean checkRepeatByCode(Integer entryId, String empCode, String entryCompanyCode);

	/**
	 * 根据推荐人信息获取关联的用户ID；
	 *
	 * @param empRelativeName：推荐人姓名；
	 * @param empRelativePhone：推荐人联系电话；
	 * @return ：推荐人的用户ID；
	 */
	Integer selectRelative(String empRelativeName, String empRelativePhone);

	/**
	 * 查询发起录用流程所需的信息；
	 *
	 * @param entryId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployEntry selectTaskById(int entryId);

	/**
	 * 查询入职申请的创建信息；
	 *
	 * @param empId：员工ID；
	 * @return ：入职申请对象；
	 */
	EmployEntry selectCreateInfoById(int empId);

	/**
	 * 根据查询码获取入职申请的ID和状态；
	 *
	 * @param entryValidate：查询码；
	 * @return ：入职申请对象；
	 */
	EmployEntry selectApproveInfo(String entryValidate);

	/**
	 * 根据提供的姓名、身份证号码、联系电话找回查询码；
	 *
	 * @param entryName：姓名；
	 * @param entryPhone：联系电话；
	 * @param empCode：身份证号码；
	 * @return ：入职申请的查询码；
	 */
	String selectEntryValidate(String entryName, String entryPhone, String empCode);

	/**
	 * 获取入职申请的信息；
	 *
	 * @param params：查询参数集合；
	 * @return ：入职申请信息；
	 */
	Map<String, Object> getEntryInfo(Map<String, Object> params);

	/**
	 * 分页查询入职申请信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的入职申请信息集合；
	 */
	PageInfo<Map<String, Object>> selectPageEntry(Map<String, Object> params, Pageable pageable);

	/**
	 * 查询入职申请的状态；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：入职申请对象；
	 */
	EmployEntry selectStateById(int entryId);

	/**
	 * 根据入职申请ID查询入职申请相关的基本信息；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：入职申请的基本信息；
	 */
	Map<String, Object> selectEmployInfoById(int entryId);

	/**
	 * 根据入职申请ID查询入职申请相关的文件信息；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：入职申请的文件集合；
	 */
	Map<String, Object> selectEntryFileById(int entryId);

	/**
	 * 入职申请信息导出；
	 *
	 * @param entryId：主键ID；
	 * @return ：文件下载路径；
	 */
	String exportEntryData(int entryId);

	/**
	 * 入职申请信息导出；
	 *
	 * @param params：查询参数；
	 * @return ：文件下载路径；
	 */
	String exportEntryData(Map<String, Object> params);

	/**
	 * 根据入职申请ID查询入职申请相关的员工录用信息；
	 * 
	 * @param data：返回给前端的数据；
	 * @param entryId：入职申请ID；
	 * @param deptId：入职申请的部门ID；
	 */
	void getEmployInfo(ResponseData data, int entryId);

	//根据一级部门获取二级部门信息
	List<Map<String, Object>> listDeptByFirstDept(String companyCode, Integer firstDept);

	//根据部门ID获取部门负责人列表
	List<User> listLeaderByDeptId(Integer deptId);

	/**
	 * 获取转正流程所需的审核数据；
	 *
	 * @param data：返回给前端的数据；
	 * @param code：权限访问码；
	 */
	void setEmployApproveData(ResponseData data, String code);

	/**
	 * 文件上传；
	 * 
	 * @param data：返回给前端的数据；
	 * @param multipartFile：上传的文件对象；
	 */
	void uploadFile(ResponseData data, MultipartFile multipartFile);

	/**
	 * 根据入职申请的ID查询家庭成员信息集合；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：家庭成员信息集合；
	 */
	List<EmployEntryFamily> selectFamily(int entryId);

	/**
	 * 根据入职申请的ID查询教育经历信息集合；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：教育信息集合；
	 */
	List<EmployEntryEducation> selectEducation(int entryId);

	/**
	 * 根据入职申请的ID查询工作经历信息集合；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：工作经历信息集合；
	 */
	List<EmployEntryExperience> selectExperience(int entryId);

	/**
	 * 查询民族信息；
	 *
	 * @return ：民族信息集合；
	 */
	List<Map<String, Object>> listNation();

	/**
	 * 查询指定区域的地区信息；
	 *
	 * @param areaId：地区的区域ID；
	 * @return ：地区信息集合；
	 */
	List<Map<String, Object>> listDistrict(Integer areaId);

	/**
	 * 查询部门信息；
	 *
	 * @return ：部门信息集合；
	 */
	List<Map<String, Object>> listDept();

	/**
	 * 查询指定部门的职位信息；
	 *
	 * @param deptId：部门ID；
	 * @return ：职位信息集合；
	 */
	List<Map<String, Object>> listPost(Integer deptId);

	List<Map<String, Object>> listPostByCompanyCode();

	ResponseData importEmployeeData(MultipartFile file);

	PageInfo<EmployEntryFamily> familyInfo(int entryId, Pageable pageable);

	PageInfo<Map<String, Object>> educationInfo(long empId, Pageable pageable);

    PageInfo<Map<String, Object>> experienceInfo(long empId, Pageable pageable);
}