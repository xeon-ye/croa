package com.qinfei.qferp.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.qferp.entity.media.Industry;
import com.qinfei.qferp.mapper.IndustryMapper;
import com.qinfei.qferp.service.IIndustryService;

@Service
@Transactional
public class IndustryService implements IIndustryService {
	@Autowired
	IndustryMapper industryMapper;
	private static final String CACHE_KEYS = "industrys";

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = CACHE_KEYS)
	public List<Industry> list(Industry industry) {
		return industryMapper.list(industry);
	}

	/**
	 * 获取所有的行业类型数据，key为名称，用于导入数据获取ID；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @return ：行业类型数据；
	 */
	@Override
	public Map<String, Integer> listAllIndustryNameMap(int mediaType) {
		Map<String, Integer> datas = new HashMap<>();
		Industry industry = new Industry();
		industry.setMediaTypeId(mediaType);
		List<Industry> industries = list(industry);
		for (Industry data : industries) {
			datas.put(data.getName(), data.getId());
		}
		return datas;
	}

}
