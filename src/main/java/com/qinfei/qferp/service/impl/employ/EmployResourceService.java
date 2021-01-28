package com.qinfei.qferp.service.impl.employ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.mapper.employ.EmployResourceMapper;
import com.qinfei.qferp.service.employ.IEmployResourceService;

/**
 * 基础信息的接口实现类；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/5 0005 17:44；
 */
@Service
public class EmployResourceService implements IEmployResourceService {
	// 数据库操作对象；
	@Autowired
	private EmployResourceMapper resourceMapper;

	/**
	 * 查询民族信息；
	 *
	 * @return ：民族信息集合；
	 */
	@Override
	public List<Map<String, Object>> listNation() {
		return resourceMapper.listNation();
	}

	/**
	 * 查询民族信息；
	 *
	 * @return ：民族信息集合；
	 */
	@Override
	public Map<Integer, String> listAllNation() {
		return getListMapData(resourceMapper.listNation());
	}

	/**
	 * 根据ID获取指定民族的名称；
	 *
	 * @param nationId：民族ID；
	 * @return ：民族的名称；
	 */
	@Override
	public String getNationNameById(Integer nationId) {
		return resourceMapper.getNationNameById(nationId);
	}

	/**
	 * 查询所有地区信息；
	 *
	 * @return ：地区信息集合；
	 */
	@Override
	public Map<Integer, String> listAllDistrict() {
		return getListMapData(resourceMapper.listDistrict(new HashMap<>()));
	}

	/**
	 * 查询指定区域的地区信息；
	 *
	 * @param areaId：地区的区域ID；
	 * @return ：地区信息集合；
	 */
	@Override
	public List<Map<String, Object>> listDistrict(Integer areaId) {
		Map<String, Object> params = new HashMap<>();
		params.put("areaId", areaId);
		return resourceMapper.listDistrict(params);
	}

	/**
	 * 查询部门信息；
	 *
	 * @return ：部门信息集合；
	 */
	@Override
	public List<Map<String, Object>> listDept(String companyCode) {
		Map<String, Object> params = new HashMap<>();
		params.put("companyCode", companyCode);
		return resourceMapper.listDept(params);
	}

	/**
	 * 查询部门信息；
	 *
	 * @return ：部门信息集合；
	 */
	@Override
	public Map<Integer, String> listAllDept() {
		return getListMapData(listDept(null));
	}

	@Override
	public Map<String, String> listAllDeptNameAndCode() {
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> deptList = resourceMapper.listDept(params);
		Map<String, String> data = new HashMap<>();
		for (Map<String, Object> map : deptList) {
			data.put(map.get("id").toString(), map.get("name").toString());
			if(map.get("code") != null){
				data.put(map.get("id").toString()+"code", map.get("code").toString());
			}
		}
		return data;
	}

	/**
	 * 根据ID获取指定部门的名称；
	 *
	 * @param deptId：部门ID；
	 * @return ：部门的名称；
	 */
	@Override
	public String getDeptNameById(Integer deptId) {
		return resourceMapper.getDeptNameById(deptId);
	}

	/**
	 * 查询所有职位信息；
	 *
	 * @return ：职位信息集合；
	 */
	@Override
	public Map<Integer, String> listAllPost() {
		Map map=new HashMap();
		map.put("companyCode", AppUtil.getUser().getCompanyCode());
		return getListMapData(resourceMapper.listPost(map));
	}

	/**
	 * 查询指定部门的职位信息；
	 *
	 * @param deptId：部门ID；
	 * @return ：职位信息集合；
	 */
	@Override
	public List<Map<String, Object>> listPost(Integer deptId) {
		Map<String, Object> params = new HashMap<>();
		params.put("deptId", deptId);
		params.put("companyCode", AppUtil.getUser().getCompanyCode());
		return resourceMapper.listPost(params);
	}

	@Override
	public List<Map<String, Object>> listPost(String companyCode, Integer firstDept) {
		return resourceMapper.listPostByCompanyAndDept(companyCode, firstDept);
	}

	@Override
	public List<Map<String, Object>> listPostByCompany(String companyCode) {
		return resourceMapper.listPostByCompany(companyCode);
	}

	/**
	 * 根据ID获取指定职位的名称；
	 *
	 * @param postId：职位ID；
	 * @return ：职位的名称；
	 */
	@Override
	public String getPostNameById(Integer postId) {
		return resourceMapper.getPostNameById(postId);
	}

	@Override
	public String getCompanyCode(Integer deptId) {
		return resourceMapper.getCompanyCode(deptId);
	}
	/**
	 * 获取Map集合中的键值数据；
	 * 
	 * @param mapList：Map集合；
	 * @return ：键值数据集合；
	 */
	private Map<Integer, String> getListMapData(List<Map<String, Object>> mapList) {
		Map<Integer, String> data = new HashMap<>();
		for (Map<String, Object> map : mapList) {
			data.put(Integer.parseInt(map.get("id").toString()), map.get("name").toString());
		}
		return data;
	}

}