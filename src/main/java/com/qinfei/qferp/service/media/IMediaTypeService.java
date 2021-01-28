package com.qinfei.qferp.service.media;

import com.qinfei.qferp.entity.media.MediaType;
import com.qinfei.qferp.entity.sys.User;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

public interface IMediaTypeService {

	String CACHE_KEY = "MediaType";
	String CACHE_KEY_LIST = "MediaType_list";

//	@Cacheable(value = CACHE_KEY_LIST)
//	List<MediaType> list(MediaType mediaType);

//	@Cacheable(value = CACHE_KEY_LIST, key = "'id='+#parentId+',userId='+#user.id")
//	List<MediaType> listByParentId(Integer parentId, User user);

//	@Cacheable(value = CACHE_KEY_LIST, key = "'id='+#parentId")
//	List<MediaType> listByParentId(Integer parentId);

//	@Cacheable(value = CACHE_KEY_LIST, key = "'id='+#parentId+',userId='+#user.id+',isFlag='+#isFlag")
//	List<MediaType> listByParentId(Integer parentId, User user, String isFlag);

//	@Cacheable(value = CACHE_KEY, key = "#id")
//	MediaType getById(Integer id);

//	@Cacheable(value = CACHE_KEY, key = "'mediaId'+#mediaId")
//	MediaType getByMediaId(Integer mediaId);

	/**
	 * 根据用户Id查询媒体类型
	 *
	 * @param userId
	 *            媒介ID
	 * @return
	 */
	// @Cacheable(value = CACHE_KEY, key = "'userId'+#userId")
//	List<MediaType> listByUserId(Integer userId);

	/**
	 * 查询所有的媒体类型数据，key为名称，用于导入数据的ID获取；
	 * 
	 * @param userId：用户ID；
	 * @param userId：用户ID；
	 * @return ：媒体类型数据集合；
	 */
//	Map<String, Integer> listAllTypeNameMap(Integer userId, Integer parentId);

//    List<MediaType> getByParentId(int parentId);
}
