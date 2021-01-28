package com.qinfei.qferp.service;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.media.Industry;

public interface IIndustryService {
	List<Industry> list(Industry industry);

	/**
	 * 获取所有的行业类型数据，key为名称，用于导入数据获取ID；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @return ：行业类型数据；
	 */
	Map<String, Integer> listAllIndustryNameMap(int mediaType);
}
