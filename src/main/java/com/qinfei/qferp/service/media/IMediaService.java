package com.qinfei.qferp.service.media;

public interface IMediaService {
	String CACHE_KEY = "media";

	/**
	 * 根据页面传递的集合信息查询；
	 *
	 * @param map：查询条件集合；
	 * @param pageable：分页对象；
	 * @return ：分页完成的数据集合；
	 */
//	PageInfo<Media> list(Map<String, Object> map, Pageable pageable);

	/*
	* 根据页面查询
	*
	* */
//	List<Map> getMediaNumber();

//	void save(Media media);

//	@Transactional
//	Media update(Media media,boolean auditsFlag);

//	boolean getByName(int mType, String mediaName);

//	Media getById(Integer id);

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

	/**
	 * 批量通过；
	 *
	 * @param ids：媒体ID数组；
	 * @param mediaNames：媒体名称数组；
	 * @param userIds：用户ID数组；
	 */
//	void passBatch(Integer[] ids, String[] mediaNames, Integer[] userIds);

	/**
	 * 批量驳回；
	 *
	 * @param ids：媒体ID数组；
	 * @param mediaNames：媒体名称数组；
	 * @param userIds：用户ID数组；
	 */
//	void rejectBatch(Integer[] ids, String[] mediaNames, Integer[] userIds);

	/**
	 * 批量删除；
	 *
	 * @param ids：媒体ID数组；
	 * @param mediaNames：媒体名称数组；
	 * @param userIds：用户ID数组；
	 */
//	String deleteBatch(Integer[] ids, String[] mediaNames, Integer[] userIds);

	/**
	 * 生成指定板块的数据导入模板；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @param templateName：模板名称；
	 * @param outputStream：响应的输出流；
	 */
//	void getDataImportTemplate(int mediaType, String templateName, OutputStream outputStream);

	/**
	 * 批量保存媒体数据；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @param mediaTypeName：媒体板块类型名称；
	 * @param file：上传文件对象；
	 */
//	String batchAddMedia(int mediaType, String mediaTypeName, File file);

	/**
	 * 查询供应商是否与媒体关联
	 * 
	 * @param id
	 * @return
	 */
//	int findSupplierMediaCount(Integer id);

	/**
	 * 验证媒体名称是否通过；
	 * 
	 * @param media：媒体对象；
	 * @return ：是否存在重复，true为通过，false为拒绝；
	 */
//	boolean checkRepeat(Media media);

//	Map<String,Object> userDelete(String datas) throws Exception;
}