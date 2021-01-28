package com.qinfei.qferp.service.employ;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;

/**
 * 基础信息的数据接口获取；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/5 0005 17:42；
 */
public interface IEmployResourceService {
	/**
	 * 查询民族信息；
	 *
	 * @return ：民族信息集合；
	 */
	@Cacheable(value = "EntryNation")
	List<Map<String, Object>> listNation();

	/**
	 * 查询民族信息；
	 *
	 * @return ：民族信息集合；
	 */
	@Cacheable(value = "EntryAllNation")
	Map<Integer, String> listAllNation();

	/**
	 * 根据ID获取指定民族的名称；
	 *
	 * @param nationId：民族ID；
	 * @return ：民族的名称；
	 */
	@Cacheable(value = "EntryNationName", key = "'nationId=' + #nationId")
	String getNationNameById(Integer nationId);

	/**
	 * 查询所有地区信息；
	 *
	 * @return ：地区信息集合；
	 */
	@Cacheable(value = "EntryAllDistrict")
	Map<Integer, String> listAllDistrict();

	/**
	 * 查询指定区域的地区信息；
	 *
	 * @param areaId：地区的区域ID；
	 * @return ：地区信息集合；
	 */
	@Cacheable(value = "EntryDistrict", key = "'areaId=' + #areaId")
	List<Map<String, Object>> listDistrict(Integer areaId);

	/**
	 * 查询部门信息；
	 *
	 * @return ：部门信息集合；
	 */
	@Cacheable(value = "EntryDept", key = "'companyCode=' + #companyCode")
	List<Map<String, Object>> listDept(String companyCode);

	/**
	 * 查询部门信息；
	 *
	 * @return ：部门信息集合；
	 */
	@Cacheable(value = "EntryAllDept")
	Map<Integer, String> listAllDept();

	/**
	 * 查询部门名 和  部门编码；
	 *
	 * @return ：部门信息集合；
	 */
	@Cacheable(value = "EntryAllDeptNameAndCode")
	Map<String, String> listAllDeptNameAndCode();

	/**
	 * 根据ID获取指定部门的名称；
	 *
	 * @param deptId：部门ID；
	 * @return ：部门的名称；
	 */
	@Cacheable(value = "EntryDeptName", key = "'deptId=' + #deptId")
	String getDeptNameById(Integer deptId);

	/**
	 * 查询所有职位信息；
	 *
	 * @return ：职位信息集合；
	 */
	@Cacheable(value = "EntryAllPost")
	Map<Integer, String> listAllPost();

	/**
	 * 查询指定部门的职位信息；
	 *
	 * @param deptId：部门ID；
	 * @return ：职位信息集合；
	 */
//	@Cacheable(value = "EntryPost", key = "'deptId=' + #deptId")
	List<Map<String, Object>> listPost(Integer deptId);

	/**
	 * 查询指定公司一级部门涉及的所有职位信息
	 * @param companyCode 公司代码
	 * @param firstDept 一级部门：0-业务部门、1-媒介部门、2-其他部门
	 * @return 职位信息集合
	 */
	List<Map<String, Object>> listPost(String companyCode, Integer firstDept);

	/**
	 * 查询指定公司所有职位信息
	 * @param companyCode 公司代码
	 * @return 职位信息集合
	 */
	List<Map<String, Object>> listPostByCompany(String companyCode);

	/**
	 * 根据ID获取指定职位的名称；
	 *
	 * @param postId：职位ID；
	 * @return ：职位的名称；
	 */
	@Cacheable(value = "EntryPostName", key = "'postId=' + #postId")
	String getPostNameById(Integer postId);

	String getCompanyCode(Integer deptId);
}