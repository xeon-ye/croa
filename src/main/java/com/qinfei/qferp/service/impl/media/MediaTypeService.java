package com.qinfei.qferp.service.impl.media;

import com.qinfei.qferp.mapper.media.MediaTypeMapper;
import com.qinfei.qferp.service.media.IMediaTypeService;
import com.qinfei.qferp.service.sys.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaTypeService implements IMediaTypeService {

	@Autowired
	MediaTypeMapper mediaTypeMapper;
	@Autowired
	IRoleService roleService;

//	@Override
//	@Cacheable(value = CACHE_KEY_LIST)
//	public List<MediaType> list(MediaType mediaType) {
//        return mediaTypeMapper.list(mediaType);
//	}

	/**
	 * 根据父级媒体ID类型查询媒体类型列表parentId=0表示查询板块列表
	 */
//	@Override
//	public List<MediaType> listByParentId(Integer parentId, User user) {
//		if (parentId > 0)//
//			return mediaTypeMapper.getByParentId(parentId);
//		List<MediaType> list = null;
//		if (user != null) {
//			list = this.listByUserId(user.getId());
//		} else {
//			list = mediaTypeMapper.getByParentId(parentId);
//		}
//		return list;
//	}

//	@Override
//	public List<MediaType> listByParentId(Integer parentId) {
//		return mediaTypeMapper.getByParentId(parentId);
//	}

//	public List<MediaType> listByParentId(Integer parentId, User user, String isFlag) {
//		if (parentId > 0)//
//			return mediaTypeMapper.getByParentId(parentId);
//		boolean flag = false;
//		if (isFlag != null)
//			flag = roleService.isRole(user.getId(), IConst.ROLE_TYPE_MJ);
//		List<MediaType> list = null;
//		if (flag) {
//			list = this.listByUserId(user.getId());
//		} else {
//			list = mediaTypeMapper.getByParentId(parentId);
//		}
//		return list;
//	}

//	@Override
//	public MediaType getById(Integer id) {
//		return mediaTypeMapper.getById(id);
//	}

//	@Override
//	public MediaType getByMediaId(Integer mediaId) {
//		return mediaTypeMapper.getByMediaId(mediaId);
//	}

	/**
	 * 根据用户Id查询媒体类型
	 *
	 * @param userId
	 *            媒介ID
	 * @return
	 */
//	@Override
//	@Cacheable(value = CACHE_KEY, key = "'userId='+#userId")
//	public List<MediaType> listByUserId(Integer userId) {
//		return mediaTypeMapper.listByUserId(userId);
//	}

	/**
	 * 查询所有的媒体类型数据，key为名称，用于导入数据的ID获取；
	 *
	 * @param userId：用户ID；
	 * @param userId：用户ID；
	 * @return ：媒体类型数据集合；
	 */
	/*@Override
	public Map<String, Integer> listAllTypeNameMap(Integer userId, Integer parentId) {
		List<MediaType> mediaTypes = new ArrayList<>();
		// 查询用户可访问的媒体类型；
		if (userId != null) {
			mediaTypes = listByUserId(userId);
		}
		// 查询子媒体类型；
		if (parentId != null) {
			mediaTypes = listByParentId(parentId);
		}
		Map<String, Integer> datas = new HashMap<>();
		if (mediaTypes.size() > 0) {
			for (MediaType data : mediaTypes) {
				datas.put(data.getName(), data.getId());
			}
		}
		return datas;
	}*/

//    @Cacheable(value = CACHE_KEY_LIST, key = "'parentId'+#parentId")
//	@Override
//	public List<MediaType> getByParentId(int parentId) {
//		return mediaTypeMapper.getByParentId(parentId);
//	}
}
