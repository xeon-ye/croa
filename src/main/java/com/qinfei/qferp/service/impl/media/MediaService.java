package com.qinfei.qferp.service.impl.media;

import com.qinfei.core.config.Config;
import com.qinfei.qferp.mapper.media.MediaInfoMapper;
import com.qinfei.qferp.mapper.media.MediaMapper;
import com.qinfei.qferp.service.IDistrictService;
import com.qinfei.qferp.service.IIndustryService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.media.*;
import com.qinfei.qferp.service.media1.IMediaType1Service;
import com.qinfei.qferp.service.workbench.IMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 媒体服务类
 *
 * @author GZW
 */
@Slf4j
@Service
public class MediaService implements IMediaService {
	@Autowired
	private MediaMapper mediaMapper;
	@Autowired
	private MediaInfoMapper mediaInfoMapper;
	@Autowired
	private IMediaTermService mediaTermService;
	@Autowired
	private UserService userService;
	@Autowired
	private ISupplierService supplierService;
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private IMediaInfoService mediaInfoService;
	// 消息推送接口；
	@Autowired
	private IMessageService messageService;
	// 媒体表单数据服务；
	@Autowired
	private IMediaFormService mediaFormService;
	// 地区数据服务；
	@Autowired
	private IDistrictService districtService;
	// 媒体类型数据服务；
	@Autowired
	private IMediaTypeService mediaTypeService;
	@Autowired
	private IMediaType1Service mediaType1Service;
	// 行业类型数据服务；
	@Autowired
	private IIndustryService industryService;
	// 直播平台数据服务；
	@Autowired
	private IMediaNameService mediaNameService;
	// 资源筛选数据服务；
	@Autowired
	private IMediaScreenService mediaScreenService;
	// 获取配置；
	@Autowired
	private Config config;
	// 获取用户数据集合；
	private Map<String, Integer> userMap;
	// 获取供应商数据集合；
	private Map<String, Integer> supplierMap;
	// 获取所有的地区信息集合；
	private Map<String, Integer> districtMap;
	// 获取所有媒体类型的信息集合；
	private Map<String, Integer> typeNameMap;
	// 获取所有行业类型的信息集合；
	private Map<String, Integer> industryNameMap;
	// 获取所有直播平台的信息集合；
	private Map<String, Integer> mediaNameMap;
	// 获取所有资源筛选信息集合；
	private Map<String, Integer> screensMap;
	// 属性数组；
	private Field[] fields;

	/**
	 * 根据页面传递的集合信息查询；
	 *
	 * @param map：查询条件集合；
	 * @param pageable：分页对象；
	 * @return ：分页完成的数据集合；
	 */
	/*@Override
	@Transactional(readOnly = true)
	public PageInfo<Media> list(Map<String, Object> map, Pageable pageable) {
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
		Object object = map.get("state");
		boolean flag = true;
		if (object == null || StringUtils.isEmpty(object)) {
			map.put("state", "0");
		} else {
			int state = Integer.parseInt(object.toString());
			// 验证是否有权限；
			if (state == 1 && !(AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && (AppUtil.isRoleCode(IConst.ROLE_CODE_ZZ)) || AppUtil.isRoleCode(IConst.ROLE_CODE_BZ))) {
				flag = false;
			}
		}
		List<Media> list;
		if (flag) {
			object = map.get("sidx");
			if (object == null || StringUtils.isEmpty(object)) {
				map.put("sidx", "state");
				map.put("sord", "asc");
			}
			User user = AppUtil.getUser();
			map.put("companyCode",user.getCompanyCode());


			list = mediaMapper.listPage(map);
			Integer mType = map.get("mType") == null ? 1 : Integer.parseInt(map.get("mType").toString());
			Media mediaInfo = new Media();
			List<MediaTerm> mts = mediaTermService.list(mType);
			if (list != null && !list.isEmpty() && mts != null && !mts.isEmpty()) {
				Class<? extends Media> cls = mediaInfo.getClass();
				Map<Integer, User> usersMap = userService.listAllUserMap();
				Map<Integer, Supplier> suppliersMap = supplierService.listAllSupplier();
				for (Media m : list) {
					fillMedia(mts, cls, m, usersMap, suppliersMap);
				}
			}
		} else {
			list = new ArrayList<>();
		}
		return new PageInfo(list);
	}*/

	/**
	 * 填充Media
	 *
	 * @param mts
	 * @param cls
	 * @param m
	 */
//	private void fillMedia(List<MediaTerm> mts, Class<? extends Media> cls, Media m, Map<Integer, User> usersMap, Map<Integer, Supplier> suppliersMap) {
//		Integer supplierId = m.getSupplierId();
//		if (supplierId != null && supplierId != 0) {
//			m.setSupplier(suppliersMap.get(supplierId));
//		}
//		Cache cache = cacheManager.getCache(CACHE_KEY);
//		m.setCreator(usersMap.get(m.getCreatorId()));
//		m.setUser(usersMap.get(m.getUserId()));
//		for (MediaTerm mt : mts) {
//			String fieldName = mt.getField();
//			if (!StringUtils.isEmpty(fieldName)) {
//				String fieldName1 = StrUtil.camelCaseName(fieldName);
//				try {
//					Field field = cls.getDeclaredField(fieldName);
//					field.setAccessible(true);
//					Object val = field.get(m);
//					if (field != null && val != null) {
//						String sql = mt.getSql();
//						HashMap<String, Object> map = new HashMap<>();
//						map.put(fieldName, val);
//						if (!StringUtils.isEmpty(sql)) {
//							String sql1 = StrUtil.parse(sql, map);
//							List<Map<String, Object>> datas = (List<Map<String, Object>>) cache.get(sql1, new Callable<Object>() {
//								@Override
//								public Object call() throws Exception {
//									return mediaMapper.dictSQL(sql1);
//								}
//							});
//							try {
//								if (datas != null && !datas.isEmpty()) {// 如果有值则放到对应的字段中
//									Field dataField = cls.getDeclaredField(fieldName1 + "Data");
//									dataField.setAccessible(true);
//									dataField.set(m, datas.get(0));
//								}
//							} catch (Exception e) {
//								log.error("没有这个类型" + e.getMessage());
//							}
//						}
//					}
//				} catch (NoSuchFieldException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	/**
	 * 保存媒体；
	 *
	 * @param media：媒体对象；
	 */
	/*@Override
	@Transactional
	@CachePut(value = CACHE_KEY)
	public void save(Media media) {
		// 折扣率不能为空或者小于0；
		if (media.getDiscount() == null || media.getDiscount() <= 0) {
			media.setDiscount(100);
		}
		mediaMapper.insert(media);
		// System.out.println(id);
		// System.out.println(media);
		// Map<String, Object> map = new HashMap<>();
		// map.put("taskUser", "1001");
		// map.put("url", "1001");
		// map.put("type", "1001");
		// map.put("title", "1001");
		// map.put("createDate", new Date());
		// map.put("creator", AppUtil.getUser().getName());
		// map.put("creatorId", AppUtil.getUser().getId());
		// workFlowService.addExpense("test", map);
	}*/

	/**
	 * 修改媒体；
	 *
	 * @param media：媒体对象；
	 */
	/*@Override
	@Transactional
	public Media update(Media media,boolean auditsFlag) {
		Map map = new HashMap();
		// 折扣率不能为空或者小于0；
		if (media.getDiscount() == null || media.getDiscount() <= 0) {
			media.setDiscount(100);
		}
		if (!auditsFlag && media.getState()==1 ){
			media.setState(1);
			mediaMapper.update(media);
			//mediaInfoService.modifyStateById(1, media.getId());
			map.put("state",1);
			map.put("mediaId",media.getId());
			map.put("supplierId",media.getSupplierId());
			map.put("supplierName",media.getSupplierName());
			map.put("remarks",media.getRemarks());
			map.put("userId",media.getUserId());
			mediaInfoService.updateInfo(map);

		}else {
		media.setState(0);
			mediaMapper.update(media);
			mediaInfoService.modifyStateById(0, media.getId());
		}

		return media;
	}*/

//	@Override
//	@Transactional(readOnly = true)
//	public boolean getByName(int mType, String mediaName) {
//		return mediaMapper.getIdByName(mType, mediaName) > 0 ? true : false;
//	}

//	@Override
//	@Transactional(readOnly = true)
//	//@Cacheable(value = CACHE_KEY, key = "'id='+#id")
//	public Media getById(Integer id) {
//		Media media = mediaMapper.get(Media.class, id);
//		media.setSupplier(supplierService.getById(media.getSupplierId()));
//		List<MediaTerm> mts = mediaTermService.list(media.getmType());
//		Map<Integer, User> usersMap = userService.listAllUserMap();
//		Map<Integer, Supplier> suppliersMap = supplierService.listAllSupplier();
//
//		this.fillMedia(mts, Media.class, media, usersMap, suppliersMap);
//		return media;
//	}

	/**
	 * 审核通过
	 *
	 * @param id
	 * @return
	 */
//	@Override
//	@CacheEvict(value = CACHE_KEY, key = "'id='+#id")
//	@Transactional
//	public boolean pass(Integer id) {
//		try {
//			Media media = this.getById(id);
//			int state = media.getState();
//			// 判断是否是业务部长 如果是业务部长则将state改为1 业务组长就改为2
//			state = (AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && AppUtil.isRoleCode(IConst.ROLE_CODE_BZ)) ? 1 : (state == 0 ? 2 : state + 1);
//			mediaMapper.modifyStateById(state, id);
//			// state=1表示已经审核通过 状态，0未审核 1审核通过，大于1表示审核中，-1是审核不通过
//			if (state == 1) {// 查看媒体信息表中是否存在数据,如果存在就更新不存在则插入
//				// MediaInfo mediaInfo = new MediaInfo();
//				// BeanUtils.copyProperties(media, mediaInfo);
//				if (mediaInfoService.getById(id) == null) {
//					mediaInfoService.selectToSave(id);
//				} else {
//					// mediaInfoService.update(mediaInfo);
//					mediaInfoService.selectToUpdate(id);
//				}
//				sendMessage(media, true);
//			}
//			return true;
//		} catch (QinFeiException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

	/**
	 * 审核驳回
	 *
	 * @param id
	 * @return
	 */
/*	@Override
	@CacheEvict(value = CACHE_KEY, key = "'id='+#id")
	@Transactional
	public boolean reject(Integer id) {
		try {
			mediaMapper.modifyStateById(-1, id);
			// 审核驳回 将mediaInfo表中数据改成1的状态
			mediaInfoService.modifyStateById(-1, id);
			Media media = this.getById(id);
			sendMessage(media, false);
			return true;
		} catch (QinFeiException e) {
			e.printStackTrace();
			return false;
		}
	}*/

	/**
	 * 审核删除
	 *
	 * @param id
	 * @return
	 */
//	@Override
//	@CacheEvict(value = CACHE_KEY, key = "'id='+#id")
//	public boolean del(Integer id) {
//		try {
//			mediaMapper.modifyStateById(-9, id);
//			Media media = this.getById(id);
//			sendMessage(media, null);
//			return true;
//		} catch (QinFeiException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

	/**
	 * 批量通过；
	 *
	 * @param ids：媒体ID数组；
	 * @param mediaNames：媒体名称数组；
	 * @param userIds：用户ID数组；
	 */
	/*@Override
	@Transactional
	public void passBatch(Integer[] ids, String[] mediaNames, Integer[] userIds) {
		Map<String, Object> map = new HashMap<>();
		// 判断是否是业务部长 如果是业务部长则将state改为1 业务组长就改为2；
		int state = (AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && AppUtil.isRoleCode(IConst.ROLE_CODE_BZ)) ? 1 : 2;
		map.put("state", state);
		List<Integer> mediaIds = Arrays.asList(ids);
		map.put("ids", mediaIds);
		mediaMapper.stateBatchUpdate(map);
		mediaInfoService.deleteBatch(mediaIds);
		mediaInfoService.saveBatch(mediaIds);
		// 同步稿件和临时稿件的媒体信息
		mediaInfoMapper.updateArticleMediaInfoBatch(Arrays.asList(ids));
		mediaInfoMapper.updateArticleImportMediaInfoBatch(Arrays.asList(ids));
		// // 更新数据到下单表中；
		// List<Integer> mediaIds = new ArrayList<>();
		// for (int id : ids) {
		// if (mediaInfoService.getById(id) == null) {
		// mediaIds.add(id);
		// } else {
		// mediaInfoService.selectToUpdate(id);
		// }
		// }
		// // 新增下单媒体；
		// int size = mediaIds.size();
		// if (size > 0) {
		// if (size > 1) {
		// mediaInfoService.saveBatch(mediaIds);
		// } else {
		// mediaInfoService.selectToSave(mediaIds.get(0));
		// }
		// }
		sendMessage(mediaNames, userIds, true);
	}*/

	/**
	 * 批量驳回；
	 *
	 * @param ids：媒体ID数组；
	 * @param mediaNames：媒体名称数组；
	 * @param userIds：用户ID数组；
	 */
/*	@Override
	public void rejectBatch(Integer[] ids, String[] mediaNames, Integer[] userIds) {
		Map<String, Object> map = new HashMap<>();
		map.put("state", IConst.STATE_REJECT);
		map.put("ids", Arrays.asList(ids));
		mediaMapper.stateBatchUpdate(map);
		sendMessage(mediaNames, userIds, false);
	}*/

	/**
	 * 批量删除；
	 *
	 * @param ids：媒体ID数组；
	 * @param mediaNames：媒体名称数组；
	 * @param userIds：用户ID数组；
	 */
/*	@Override
	@Transactional
	public String deleteBatch(Integer[] ids, String[] mediaNames, Integer[] userIds) {
		Boolean flag = true;
		String message = null;
		List list = Arrays.asList(ids);
		if (mediaInfoMapper.getArticleCount(list) > 0 || mediaInfoMapper.getArticleImportCount(list) > 0) {
			flag = false;
			message = "选中的媒体中有关联的稿件，无法删除！";
		}
		if (flag) {
			Map<String, Object> map = new HashMap<>();
			map.put("state", IConst.STATE_DELETE);
			map.put("ids", Arrays.asList(ids));
			mediaMapper.stateBatchUpdate(map);
			mediaInfoService.deleteBatch(ids);
			sendMessage(mediaNames, userIds, null);
		}
		return message;
	}*/

	/**
	 * 生成指定板块的数据导入模板；
	 *
	 * @param mediaType：媒体板块类型；
	 * @param templateName：模板名称；
	 * @param outputStream：响应的输出流；
	 */
/*	@Override
	public void getDataImportTemplate(int mediaType, String templateName, OutputStream outputStream) {
		// 获取列头信息；
		List<String> rowTitles = getRowTitles(mediaFormService.listByMediaTypeId(mediaType));
		// 获取板块的表单数据；
		DataImportUtil.createExportFile(templateName, rowTitles, outputStream, null, getMediaNotices());
	}*/

	/**
	 * 批量保存媒体数据；
	 *
	 * @param mediaType：媒体板块类型；
	 * @param mediaTypeName：媒体板块类型名称；
	 * @param file：上传文件对象；
	 */
	/*@Override
	public String batchAddMedia(int mediaType, String mediaTypeName, File file) {
		String result = "0";
		List<MediaForm> mediaForms = mediaFormService.listByMediaTypeId(mediaType);
		int size = mediaForms.size();
		if (size > 0) {
			List<Object[]> excelContent = DataImportUtil.getExcelContent(file, 3, 2, size + 1);
			if (excelContent.size() > 0) {
				result = handleData(mediaType, mediaTypeName, mediaForms, excelContent);
			}
		}
		return result;
	}*/

	/**
	 * 处理上传的数据；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @param mediaTypeName：媒体板块类型名称；
	 * @param mediaForms：板块对应的数据表单集合；
	 * @param excelDatas：上传的数据；
	 * @return ：如果有数据导入不成功，会返回未成功导入数据的文件地址；
	 */
	/*private String handleData(int mediaType, String mediaTypeName, List<MediaForm> mediaForms, List<Object[]> excelDatas) {
		// 获取数据列长度；
		int formSize = mediaForms.size();
		MediaForm form;
		// 获取数据列信息；
		Map<String, Integer> formIndexs = new HashMap<>();
		Map<String, MediaForm> formColumns = new HashMap<>();
		for (int i = 0; i < formSize; i++) {
			form = mediaForms.get(i);
			formIndexs.put(form.getCode(), i);
			formColumns.put(form.getCode(), form);
		}

		// 获取板块类型下的媒体集合，用于校验媒体名称；
		Map<String, Integer> mediaMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("mType", mediaType);
		// 查询未删除的；
		params.put("state", 9);
		List<Media> mediaList = mediaMapper.listNormalData(params);
		for (Media data : mediaList) {
			// 不区分大小写；
			mediaMap.put(data.getName().toLowerCase(Locale.US), data.getId());
		}

		// 获取内容长度，用于遍历数据；
		int size = excelDatas.size();
		Object[] data;
		// 用于保存导入失败的数据；
		List<Object[]> invalidDatas = new ArrayList<>();
		// 用于保存导入成功的数据；
		List<Media> medias = new ArrayList<>();
		// 获取登录用户；
		int dataLength;
		Integer userId = AppUtil.getUser().getId();
		Media media;
		// 先清空数据；
		// 获取用户数据集合；
		userMap = null;
		// 获取供应商数据集合；
		supplierMap = null;
		// 获取所有的地区信息集合；
		districtMap = null;
		// 获取所有媒体类型的信息集合；
		typeNameMap = null;
		// 获取所有行业类型的信息集合；
		industryNameMap = null;
		// 获取所有直播平台的信息集合；
		mediaNameMap = null;
		// 获取所有资源筛选信息集合；
		screensMap = null;
		// 属性数组；
		fields = null;
		for (int i = 0; i < size; i++) {
			data = excelDatas.get(i);
			dataLength = data.length;
			// 文件底部的提示信息只有一列，优先过滤掉；
			if (dataLength > 1) {
				// 验证数据是否符合要求，有一列额外的联系人；
				if (dataLength == formSize + 1) {
					// 获取处理完毕的数据；
					media = createMedia(mediaType, formIndexs, formColumns, data);
					// 校验媒体的责任和供应商信息，验证数据是否存在；
					if (StringUtils.hasLength(media.getName()) && media.getUserId() != null && media.getSupplierId() != null && mediaMap.get(media.getName().toLowerCase(Locale.US)) == null) {
						media.setmType(mediaType);
						media.setState(0);
						media.setCreatorId(userId);
						media.setCreateDate(new Date());
						// 校验折扣率；
						if (media.getDiscount() == null || media.getDiscount() <= 0) {
							media.setDiscount(100);
						}
						// 公用字段校验；
						if (media.getN1() == null) {
							media.setN1(0);
						}
						if (media.getN2() == null) {
							media.setN2(0);
						}
						if (media.getN3() == null) {
							media.setN3(0);
						}
						if (media.getN4() == null) {
							media.setN4(0);
						}
						if (media.getN5() == null) {
							media.setN5(1);
						}
						if (media.getN6() == null) {
							media.setN6(1);
						}
						if (media.getN7() == null) {
							media.setN7(0);
						}
						if (media.getN8() == null) {
							media.setN8(0);
						}
						medias.add(media);

						// 添加到校验集合中，统一使用小写保存；
						mediaMap.put(media.getName().toLowerCase(Locale.US), 1);
					} else {
						data = Arrays.copyOf(data, data.length + 1);
						if (StringUtils.isEmpty(media.getName())) {
							data[data.length - 1] = "媒体名称不能为空";
						} else if (media.getUserId() == null) {
							data[data.length - 1] = "责任人未录入或不能为空";
						} else if (media.getSupplierId() == null) {
							data[data.length - 1] = "供应商未录入或不能为空";
						} else {
							data[data.length - 1] = "媒体已存在";
						}
						invalidDatas.add(data);
					}
				} else {
					data = Arrays.copyOf(data, data.length + 1);
					data[data.length - 1] = "上传的数据与媒体板块不对应";
					invalidDatas.add(data);
				}
			}
		}

		// 判断是否存在有效数据；
		size = medias.size();
		if (size > 0) {
			// 定义需要进行分割的尺寸；
			int subLength = 100;
			// 计算需要插入的次数，100条插入一次；
			int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
			// 100条以上才需要进行处理；
			if (insertTimes > 1) {
				List<Media> insertData;
				// 计算需要循环插入的次数；
				for (int i = 0; i < insertTimes; i++) {
					insertData = new ArrayList<>();
					// 计算起始位置，且j的最大值应不能超过数据的总数；
					for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
						insertData.add(medias.get(j));
					}
//					mediaMapper.saveBatch(insertData);
				}
			} else {
//				mediaMapper.saveBatch(medias);
			}
		}

		// 没有数据导入成功；
		size = invalidDatas.size();
		if (size > 0) {
			// 获取列头信息；
			List<String> rowTitles = getRowTitles(mediaForms);
			// 导入失败原因；
			rowTitles.add("失败原因");
			return DataImportUtil.createFile(mediaTypeName + "导入失败内容", config.getUploadDir(), config.getWebDir(), rowTitles, invalidDatas);
		} else {
			return null;
		}
	}*/

	/**
	 * 封装数据到对象中；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @param formIndexs：数据列位置信息；
	 * @param formColumns：数据列类别信息
	 * @param data：上传的数据；
	 * @return ：封装完毕的对象；
	 */
	/*private Media createMedia(int mediaType, Map<String, Integer> formIndexs, Map<String, MediaForm> formColumns, Object[] data) {
		// 实例化对象；
		Media media = new Media();
		// 获取行长度；
		int dataLength = data.length;
		// 用于校验手机号码；
		String regex = "^\\d+$";
		// 浮点数据正则；
		String numberRegex = "^(-?\\d+)(\\.\\d+)?$";
		// 获取声明的属性；
		Field[] cuurentfields;
		// 第一次循环，从反射中获取属性数组；
		if (fields == null) {
			cuurentfields = media.getClass().getDeclaredFields();
			// 有一列联系人；
			fields = new Field[dataLength - 1];
		} else {
			cuurentfields = fields;
		}
		// 定义循环使用的变量；
		MediaForm form;
		String filedName;
		String filedType;
		String valueString;
		Object value;
		Integer dataIndex;
		try {
			// 定义下标用于保存有效属性；
			int index = 0;
			// 遍历属性；
			for (Field field : cuurentfields) {
				// 获取属性名称；
				filedName = field.getName();
				// 获取属性值的下标；
				dataIndex = formIndexs.get(filedName);
				if (dataIndex != null) {
					// 获取属性类型；
					form = formColumns.get(filedName);
					// 只在初次循环中存储；
					if (form != null) {
						// 有一列联系人；
						if (fields[dataLength - 2] == null) {
							fields[index] = field;
							index++;
						}
					}
					// 获取属性值；
					value = data[dataIndex];
					// 验证属性；
					if (value != null && form != null) {
						field.setAccessible(true);
						// 获取数据库中指定的数据类型；
						filedType = form.getType();
						// 验证是否有内容；
						valueString = value.toString().trim();
						if (StringUtils.hasLength(valueString)) {
							// 供应商属性要额外处理；
							if ("supplierId".equals(filedName)) {
								// 数据最后一位一定是联系人名称；
								valueString = valueString + "*" + data[dataLength - 1];
							}
							// 单选类型；
							if ("select".equals(filedType)) {
								setSelectValue(mediaType, form.getName(), media, field, valueString);
								// 单选类型；
							} else if ("radio".equals(filedType)) {
								field.set(media, getRadioValue(valueString));
								// 多选类型；
							} else if ("checkbox".equals(filedType)) {
								setCheckBoxValue(mediaType, media, field, valueString);
								// 日期类型；
							} else if ("datetime".equals(filedType)) {
								if (value instanceof Date) {
									field.set(media, value);
								} else {
									field.set(media, DateUtils.parse(valueString, "yyyy/MM/dd"));
								}
								// 数字类型，校验数据；
							} else if ("number".equals(filedType)) {
								if ((valueString.matches(regex) || valueString.matches(numberRegex))) {
									if (Integer.class.equals(field.getType())) {
										field.set(media, (int) Float.parseFloat(valueString));
									} else {
										field.set(media, Float.parseFloat(valueString));
									}
								}
								// 默认是文本类型；
							} else {
								field.set(media, valueString);
							}
						}
					}
				}
			}
		} catch (

		IllegalArgumentException e) {
			log.error("参数异常。" + e);
		} catch (IllegalAccessException e) {
			log.error("类入口异常。" + e);
		}
		return media;
	}*/

	/**
	 * 设置多选框的值；
	 *
	 * @param mediaType：媒体板块类型；
	 * @param media：媒体对象；
	 * @param field：属性对象；
	 * @param value：属性值；
	 */
	/*private void setCheckBoxValue(int mediaType, Media media, Field field, String value) {
		try {
			switch (mediaType) {
			// 报纸类型出刊时间为多选框；
			case 3:
				field.set(media, Integer.parseInt(value));
				break;
			default:
				break;
			}
		} catch (IllegalArgumentException e) {
			log.error("参数异常。" + e);
		} catch (IllegalAccessException e) {
			log.error("类入口异常。" + e);
		}
	}*/

	/**
	 * 设置单选框的值；
	 * 
	 * @param mediaType：媒体板块类型；
	 * @param formName：字段描述；
	 * @param media：媒体对象；
	 * @param field：属性对象；
	 * @param value：属性值；
	 */
	/*private void setSelectValue(int mediaType, String formName, Media media, Field field, String value) {
		try {
			Map<String, Integer> map = null;
			if ("责任人".equals(formName)) {
				map = getUserMap(mediaType);
			}
			if ("供应商".equals(formName)) {
				map = getSupplierMap(mediaType);
				// 切割出供应商名称；
				media.setSupplierName(value.substring(0, value.indexOf("*")));
			}
			if ("地区".equals(formName) || "发行区域".equals(formName)) {
				map = getDistrictMap();
			}
			if ("媒体类型".equals(formName)||"导购力".equals(formName)) {
				map = getMediaTypeNameMap(mediaType);
			}
			if ("直播平台".equals(formName) || "门户媒体".equals(formName) || "客户端平台".equals(formName) || "自媒体平台".equals(formName) || "视频媒体".equals(formName) || "达人等级".equals(formName)) {
				map = getMediaNameMap(mediaType);
			}
			if ("资源类别筛选".equals(formName)) {
				map = getScreensMap(mediaType);
			}
			if ("行业类型".equals(formName)) {
				map = getIndustryNameMap(mediaType);
			}
			if (map != null) {
				field.set(media, map.get(value));
			}
			if ("发布端".equals(formName) || "收录（仅供参考）".equals(formName) || "发布页面".equals(formName) || "展现形式".equals(formName) || "出稿时间类型".equals(formName) || "发布效果".equals(formName)) {
				field.set(media, getStaticSelectData(value));
			}
		} catch (IllegalArgumentException e) {
			log.error("参数异常。" + e);
		} catch (IllegalAccessException e) {
			log.error("类入口异常。" + e);
		}
	}*/

	// 静态类型的下拉框数据获取；
	/*private Integer getStaticSelectData(String type) {
		if ("PC".equals(type) || "新闻源".equals(type) || "大首页".equals(type) || "全国可见".equals(type) || "广告".equals(type) || "秒出".equals(type)) {
			return 0;
		} else if ("APP".equals(type) || "网页".equals(type) || "频道".equals(type) || "智能推荐".equals(type) || "新闻".equals(type) || "当天".equals(type)) {
			return 1;
		} else if ("隔天".equals(type)) {
			return 2;
		} else {
			return null;
		}
	}*/

	// 判断类型的值获取；
	/*private int getRadioValue(String value) {
		if ("是".equals(value) || "男".equals(value)) {
			return 0;
		} else {
			return 1;
		}
	}*/

	// 获取用户数据集合；
	/*private Map<String, Integer> getUserMap(int mediaType) {
		if (userMap == null) {
			userMap = userService.listAllUserNameMap(mediaType);
		}
		return userMap;
	}*/

	// 获取供应商数据集合；
	/*private Map<String, Integer> getSupplierMap(int mediaType) {
		if (supplierMap == null) {
			supplierMap = supplierService.listAllSupplierName(mediaType);
		}
		return supplierMap;
	}*/

	// 获取所有的地区信息集合；
	/*private Map<String, Integer> getDistrictMap() {
		if (districtMap == null) {
			districtMap = districtService.listDistrictMap();
		}
		return districtMap;
	}*/

	// 获取所有媒体类型的信息集合；
	/*private Map<String, Integer> getMediaTypeNameMap(int mediaType) {
		if (typeNameMap == null) {
			typeNameMap = mediaType1Service.listMediaTypeByPlateId(mediaType);//mediaTypeService.listAllTypeNameMap(null, mediaType);
		}
		return typeNameMap;
	}*/

	// 获取所有行业类型的信息集合；
	/*private Map<String, Integer> getIndustryNameMap(int mediaType) {
		if (industryNameMap == null) {
			industryNameMap = industryService.listAllIndustryNameMap(mediaType);
		}
		return industryNameMap;
	}*/

	// 获取所有直播平台的信息集合；
	/*private Map<String, Integer> getMediaNameMap(int mediaType) {
		if (mediaNameMap == null) {
			mediaNameMap = mediaNameService.listMediaNameMap(mediaType);
		}
		return mediaNameMap;
	}*/

	// 获取所有资源筛选信息集合；
	/*private Map<String, Integer> getScreensMap(int mediaType) {
		if (screensMap == null) {
			screensMap = mediaScreenService.listAllNameMap(mediaType);
		}
		return screensMap;
	}*/

	/**
	 * 消息推送；
	 * 
	 * @param media：媒体信息；
	 * @param agree：是否同意，true为审核通过，false为拒绝，null为删除；
	 */
	/*private void sendMessage(Media media, Boolean agree) {
		User user = AppUtil.getUser();
		String operateUser = user.getName();
		String title = media.getName();
		Integer userId = user.getId();
		User creator = media.getCreator();
		Integer acceptId = creator.getId();

		// 拼接消息内容；
		String subject;
		String content;
		if (agree == null) {
			subject = "媒体信息审核通过。";
			content = String.format("很遗憾，你录入的媒体信息[%s]已被[%s]删除。", title, operateUser);
		} else {
			if (agree) {
				subject = "媒体信息审核通过。";
				content = String.format("恭喜你，你录入的媒体信息[%s]已经由[%s]审核通过。", title, operateUser);
			} else {
				subject = "媒体信息已被驳回。";
				content = String.format("很遗憾，你录入的媒体信息[%s]在[%s]处审核未通过。", title, operateUser);
			}
		}

		// 推送WebSocket消息；
		WSMessage message = new WSMessage();
		message.setReceiveUserId(acceptId + "");
		message.setReceiveName(creator.getName());
		message.setSendName(operateUser);
		message.setSendUserId(userId + "");
		message.setSendUserImage(user.getImage());
		message.setContent(content);
		message.setSubject(subject);
		WebSocketServer.sendMessage(message);

		// 推送系统的消息；
		Message newMessage = new Message();
		String userImage = user.getImage();
		// 获取消息显示的图片；
		String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
		newMessage.setPic(pictureAddress);
		newMessage.setContent(content);
		newMessage.setInitiatorDept(user.getDeptId());
		newMessage.setInitiatorWorker(userId);
		newMessage.setAcceptDept(creator.getDeptId());
		newMessage.setAcceptWorker(acceptId);
		messageService.addMessage(newMessage);
	}*/

	/**
	 * 批量操作的消息通知；
	 * 
	 * @param mediaNames：媒体信息数组；
	 * @param userIds：用户ID数组；
	 * @param agree：是否同意，true为审核通过，false为拒绝，null为删除；
	 */
	/*private void sendMessage(String[] mediaNames, Integer[] userIds, Boolean agree) {
		// 获取用户信息集合；
		Map<Integer, User> userMap = userService.listAllUserMap();
		int length = mediaNames.length;
		// 单条数据直接使用已有的方法；
		if (length == 1) {
			Media media = new Media();
			media.setName(mediaNames[0]);
			media.setCreator(userMap.get(userIds[0]));
			sendMessage(media, agree);
		} else {
			// 获取登录用户；
			User user = AppUtil.getUser();
			String operateUser = user.getName();
			Integer userId = user.getId();
			String title;
			User creator;
			Integer acceptId;
			// 拼接消息内容；
			String subject;
			String content;
			// 推送WebSocket消息；
			WSMessage message;
			// 推送系统的消息；
			Message newMessage;
			// 批量插入；
			List<Message> messages = new ArrayList<>();
			for (int i = 0; i < length; i++) {
				title = mediaNames[i];
				creator = userMap.get(userIds[i]);
				acceptId = creator.getId();

				if (agree == null) {
					subject = "媒体信息审核通过。";
					content = String.format("很遗憾，你录入的媒体信息[%s]已被[%s]删除。", title, operateUser);
				} else {
					if (agree) {
						subject = "媒体信息审核通过。";
						content = String.format("恭喜你，你录入的媒体信息[%s]已经由[%s]审核通过。", title, operateUser);
					} else {
						subject = "媒体信息已被驳回。";
						content = String.format("很遗憾，你录入的媒体信息[%s]在[%s]处审核未通过。", title, operateUser);
					}
				}

				// 推送WebSocket消息；
				message = new WSMessage();
				message.setReceiveUserId(acceptId + "");
				message.setReceiveName(creator.getName());
				message.setSendName(operateUser);
				message.setSendUserId(userId + "");
				message.setSendUserImage(user.getImage());
				message.setContent(content);
				message.setSubject(subject);
				WebSocketServer.sendMessage(message);

				// 推送系统的消息；
				newMessage = new Message();
				String userImage = user.getImage();
				// 获取消息显示的图片；
				String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
				newMessage.setPic(pictureAddress);
				newMessage.setContent(content);
				newMessage.setInitiatorDept(user.getDeptId());
				newMessage.setInitiatorWorker(userId);
				newMessage.setAcceptDept(creator.getDeptId());
				newMessage.setAcceptWorker(acceptId);
				messages.add(newMessage);
			}
			messageService.batchAddMessage(messages);
		}
	}*/

	/**
	 * 查询供应商下是否存在媒体
	 * 
	 * @param id
	 * @return
	 */
/*	public int findSupplierMediaCount(Integer id) {
		return mediaMapper.findSupplierMediaCount(id);
	}*/

	/**
	 * 验证媒体名称是否通过；
	 *
	 * @param media：媒体对象；
	 * @return ：是否存在重复，true为通过，false为拒绝；
	 */
/*	@Override
	public boolean checkRepeat(Media media) {
		boolean flag = false;
		// 拼接查询条件；
		Map map = new HashMap();
		map.put("id", media.getId());
		map.put("name", media.getName());
		map.put("mType", media.getmType());
		// 查询是否已存在；
		if (mediaMapper.checkRepeat(map) <= 0) {
			flag = true;
		}
		return flag;
	}*/

	/**
	 * 获取表格表头信息；
	 * 
	 * @param mediaForms：表单集合；
	 * @return ：表头信息集合；
	 */
//	private List<String> getRowTitles(List<MediaForm> mediaForms) {
//		List<String> rowTitles = new ArrayList<>();
//		String rowTitle;
//		for (MediaForm mediaForm : mediaForms) {
//			rowTitle = mediaForm.getName();
//			// 是否必填；
//			if (mediaForm.getRequired() == 1) {
//				rowTitle = "*" + rowTitle + "*";
//			}
//			rowTitles.add(rowTitle);
//		}
//		// 用于确定唯一供应商；
//		rowTitles.add("*联系人*");
//		return rowTitles;
//	}

	/**
	 * 媒体导入模板文件的操作提示信息；
	 *
	 * @return ：操作提示信息集合；
	 */
	/*private List<String> getMediaNotices() {
		List<String> notices = new ArrayList<>();
		notices.add("表格的第一行、第一列留空请勿删除；");
		notices.add("带星号标注的列必须有内容；");
		notices.add("无内容的单元格可不填写，留空即可；");
		notices.add("折扣率为百分制数字，如0.7折扣率，请填写70即可；");
		notices.add("价格有效期请填写截止日期；");
		notices.add("日期格式使用yyyy/MM/dd，例如：2019/06/01；");
		notices.add("有效的数据行请确保第一列（C列）有内容，否则系统会识别为无效不会进行处理；");
		notices.add("关联的供应商、责任人等信息请确保已录入到系统中且有权限使用；");
		notices.add("报纸板块的出刊时间请使用数字，例如：周一周二为出刊时间，内容则填写12；");
		notices.add("内容为附件或图片的列，请先将文件上传，然后把系统提供的文件地址填入对应的表格中。");
		return notices;
	}*/

	/*public List<Map> getMediaNumber() {
		List<Map> maps = new ArrayList<>();
		User user= AppUtil.getUser();
		Dept dept= user.getDept();
		if (dept.getCompanyCode().equals("XH") ) {
			maps = mediaMapper.getMediaNumber();
		}else {
			String companyCode = dept.getCompanyCode();
			maps=mediaMapper.statistical(companyCode);
		}
		return maps;
	}*/

	/*@Transactional
	public Map<String,Object> userDelete(String datas) throws Exception {
		User user = AppUtil.getUser();
		List<Media> mediaList = JSON.parseArray(datas, Media.class);
		List<Integer> list = new ArrayList<>();
		for (Media media : mediaList) {
			list.add(media.getId());
		}
		Map<String, Object> map = new HashMap();
		StringBuffer sb = new StringBuffer();
		if (mediaMapper.mediaState(list) > 0) {
			sb.append("该媒体审批通过，无法删除！");
		}
		if (mediaInfoMapper.getArticleCount(list) > 0 || mediaInfoMapper.getArticleImportCount(list) > 0) {
			sb.append("选中的媒体中有关联的稿件，无法删除！");
		}
		if (StringUtils.isEmpty(sb.toString())) {
			int row = mediaMapper.userDelete(list, user.getId());
			int size = list.size();
			int fail = row - size;
			if (list.size() != row) {
				sb.append("成功删除了" + row + "条，失败了" + fail + "条，只有该媒体的责任人才能删除！");
			} else {
				sb.append("成功删除了" + row + "条稿件！");
			}
			map.put("row", row);
		} else {
			int row = 0;
			map.put("row", row);
		}
		map.put("message", sb.toString());
		return map;
	}*/
}
