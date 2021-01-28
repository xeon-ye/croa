package com.qinfei.qferp.service.media;

public interface IMediaInfoService {
	String CACHE_KEY = "mediaInfo";
	String CACHE_KEYS = "mediaInfos";

/*	@Transactional
	void exportTemplate(Map map, OutputStream outputStream);*/

//	void importExcel(File file);

	/**
	 * 根据媒体信息条件 分页查询媒体信息
	 *
	 * @param mediaInfo
	 *            媒体信息
	 * @param pageable
	 *            分页对象
	 * @return
	 */
//	PageInfo<MediaInfo> list(MediaInfo mediaInfo, Pageable pageable);

	/**
	 * 根据页面传递的集合信息查询；
	 * 
	 * @param map：查询条件集合；
	 * @param pageable：分页对象；
	 * @return ：分页完成的数据集合；
	 */
//	PageInfo<MediaInfo> list(Map<String, Object> map, Pageable pageable);

//	void save(MediaInfo mediaInfo);

	/**
	 * 查询媒体表写入媒体信息表中
	 * 
	 * @param mediaId
	 */
//	void selectToSave(Integer mediaId);

	/**
	 * 批量复制媒体数据到下单媒体表；
	 *
	 * @param mediaIds：要复制的媒体ID；
	 */
//	void saveBatch(List<Integer> mediaIds);

/*	@Transactional
	void deleteBatch(List<Integer> mediaIds);*/

	/**
	 * 查询媒体表更新到媒体信息表中
	 * 
	 * @param mediaId
	 */
//	void selectToUpdate(Integer mediaId);

	/*@Transactional
	MediaInfo update(MediaInfo mediaInfo);*/

//	boolean getByName(int mType, String mediaName);

//	MediaInfo getById(Integer id);

	/**
	 * 审核通过
	 *
	 * @param id
	 * @return
	 */
//	boolean pass(Integer id);

	/**
	 * 审核驳回
	 *
	 * @param id
	 * @return
	 */
//	boolean reject(Integer id);

	/**
	 * 审核删除
	 *
	 * @param id
	 * @return
	 */
//	boolean del(Integer id);

//	void modifyStateById(int i, Integer id);


//	void updateInfo(Map map);

	/**
	 * 批量删除；
	 *
	 * @param ids：媒体ID数组；
	 */
//	void deleteBatch(Integer[] ids);

//    PageInfo<Map> getMediaInfoByTypeId(int pageNum, int pageSize, Map map);
}