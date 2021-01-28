package com.qinfei.qferp.service.media;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.media.MediaName;

public interface IMediaNameService {
	List<MediaName> all();

	List<MediaName> list(MediaName mediaName);

	/**
	 * 获取直播平台集合，key为名称，用于导入数据获取ID；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @return ：直播平台集合；
	 */
	Map<String, Integer> listMediaNameMap(int mediaType);
}
