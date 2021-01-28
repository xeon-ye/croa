package com.qinfei.qferp.service;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.sys.District;

public interface IDistrictService {
	List<District> all();

	/**
	 * 查询区域信息集合，key为地区名字，用于导入数据获取对应的ID；
	 * 
	 * @return ：区域信息集合；
	 */
	Map<String, Integer> listDistrictMap();
}
