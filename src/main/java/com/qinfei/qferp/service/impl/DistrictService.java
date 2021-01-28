package com.qinfei.qferp.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.qferp.entity.sys.District;
import com.qinfei.qferp.mapper.DistrictMapper;
import com.qinfei.qferp.service.IDistrictService;

@Service
@Transactional
public class DistrictService implements IDistrictService {
	@Autowired
	DistrictMapper districtMapper;
	private static final String CACHE_KEY = "district";
	private static final String CACHE_KEYS = "districts";

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = CACHE_KEYS)
	public List<District> all() {
		return districtMapper.all(District.class);
	}

	/**
	 * 查询区域信息集合，key为地区名字，用于导入数据获取对应的ID；
	 *
	 * @return ：区域信息集合；
	 */
	@Override
	public Map<String, Integer> listDistrictMap() {
		List<District> districts = all();
		Map<String, Integer> datas = new HashMap<>();
		for (District district : districts) {
			datas.put(district.getName(), district.getId());
		}
		return datas;
	}

}
