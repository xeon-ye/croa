package com.qinfei.qferp.service.impl.media;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.qferp.entity.media.MediaScreen;
import com.qinfei.qferp.mapper.media.MediaScreenMapper;
import com.qinfei.qferp.service.media.IMediaScreenService;

@Service
@Transactional
public class MediaScreenService implements IMediaScreenService {
	@Autowired
	MediaScreenMapper mediaScreenMapper;

	@Override
	public List<MediaScreen> all() {
		return mediaScreenMapper.all(MediaScreen.class);
	}

	@Override
	public List<MediaScreen> list(MediaScreen mediaScreen) {
		return mediaScreenMapper.list(mediaScreen);
	}

	@Override
	public List<MediaScreen> listByMediaTypeId(Integer mediaTypeId) {
		return mediaScreenMapper.listByMediaTypeId(mediaTypeId);
	}

	/**
	 * 获取所有资源筛选类别信息，key为名称，用于导入数据获取ID；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @return ：资源筛选类别集合；
	 */
	@Override
	public Map<String, Integer> listAllNameMap(int mediaType) {
		Map<String, Integer> datas = new HashMap<>();
		MediaScreen mediaScreen = new MediaScreen();
		mediaScreen.setMediaTypeId(mediaType);
		List<MediaScreen> mediaScreens = list(mediaScreen);
		for (MediaScreen data : mediaScreens) {
			datas.put(data.getName(), data.getId());
		}
		return datas;
	}
}
