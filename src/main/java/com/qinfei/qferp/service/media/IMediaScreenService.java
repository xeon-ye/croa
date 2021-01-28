package com.qinfei.qferp.service.media;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;

import com.qinfei.qferp.entity.media.MediaScreen;

public interface IMediaScreenService {
	String CACHE_KEY = "MediaScreen";

	@Cacheable(value = CACHE_KEY)
	List<MediaScreen> all();

	@Cacheable(value = CACHE_KEY)
	List<MediaScreen> list(MediaScreen mediaScreen);

	@Cacheable(value = CACHE_KEY, key = "#mediaTypeId")
	List<MediaScreen> listByMediaTypeId(Integer mediaTypeId);

	/**
	 * 获取所有资源筛选类别信息，key为名称，用于导入数据获取ID；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @return ：资源筛选类别集合；
	 */
	Map<String, Integer> listAllNameMap(int mediaType);
}
