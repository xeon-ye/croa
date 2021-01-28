package com.qinfei.qferp.service.impl.media;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.qferp.entity.media.MediaName;
import com.qinfei.qferp.mapper.media.MediaNameMapper;
import com.qinfei.qferp.service.media.IMediaNameService;

@Service
@Transactional
public class MediaNameService implements IMediaNameService {
	@Autowired
	MediaNameMapper mediaNameMapper;
	private static final String CACHE_KEY = "MediaName";

	@Override
	@Cacheable(value = CACHE_KEY)
	public List<MediaName> all() {
		return mediaNameMapper.all(MediaName.class);
	}

	@Override
	@Cacheable(value = CACHE_KEY)
	public List<MediaName> list(MediaName mediaName) {
		return mediaNameMapper.list(mediaName);
	}

	/**
	 * 获取直播平台集合，key为名称，用于导入数据获取ID；
	 *
	 * @param mediaType：媒体板块类型；
	 * @return ：直播平台集合；
	 */
	@Override
	public Map<String, Integer> listMediaNameMap(int mediaType) {
		Map<String, Integer> datas = new HashMap<>();
		MediaName mediaName = new MediaName();
		mediaName.setMediaTypeId(mediaType);
		List<MediaName> mediaNames = list(mediaName);
		for (MediaName data : mediaNames) {
			datas.put(data.getName(), data.getId());
		}
		return datas;
	}

}
