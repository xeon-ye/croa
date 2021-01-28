package com.qinfei.qferp.service.impl.media;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.fee.AccountChange;
import com.qinfei.qferp.entity.media.Supplier;
import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.entity.media1.SupplierChange;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.fee.AccountChangeMapper;
import com.qinfei.qferp.mapper.media.SupplierMapper;
import com.qinfei.qferp.mapper.media1.MediaPlateMapper;
import com.qinfei.qferp.mapper.media1.MediaSupplierRelateAuditMapper;
import com.qinfei.qferp.mapper.media1.SupplierChangeMapper;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.media.ISupplierService;
import com.qinfei.qferp.service.outapi.ICompanyService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.ObjectFieldCompare;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.util.*;

@Slf4j
@Service
public class SupplierService implements ISupplierService {
	// 供应商数据库注入接口；
	@Autowired
	private SupplierMapper supplierMapper;
	// 媒体类型数据库接口；
	@Autowired
	private MediaPlateMapper mediaPlateMapper;
	@Autowired
	private SupplierChangeMapper supplierChangeMapper;
	@Autowired
	private AccountChangeMapper accountChangeMapper;
	@Autowired
	private MediaSupplierRelateAuditMapper mediaSupplierRelateAuditMapper;
	@Autowired
	private ICompanyService companyService;
	@Value("${media.onlineTime}")
	private String onlineTime;

	@Autowired
	private UserService userService;
	@Override
	public void checkSupplier(Integer id, String name, String phone) {
		try{
			if(StringUtils.isEmpty(name)){
				throw new QinFeiException(1002, "供应商公司名称不能为空！");
			}
			if(StringUtils.isEmpty(phone)){
                throw new QinFeiException(1002, "供应商手机号不能为空！");
			}
			if(supplierMapper.checkSupplier(id, name, EncryptUtils.encrypt(phone.trim()), onlineTime) != null){
				throw new QinFeiException(1002, "很抱歉，供应商公司名称和手机号已经存在！");
			}
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			throw new QinFeiException(1002, "供应商校验异常！");
		}
	}

	@Transactional
	@Override
	public void addSupplier(Supplier supplier) {
		try{
			User user = AppUtil.getUser();
			validateSupplier(supplier, user);
			supplier.setCompanyCode(user.getCompanyCode());
			supplier.setCreator(user.getId());
			supplier.setCreateTime(new Date());
			supplier.setContactor(supplier.getContactor().trim());
			supplierMapper.insert(supplier);

			//处理供应商异动，增加异常捕获，使其不影响正常操作
			try{
				//媒体新增成功，才能进行媒体异动 和 供应商异动的操作
				if(supplier != null && supplier.getId() != null){
					List<SupplierChange> supplierChangeList = supplierChangeHandler(Arrays.asList(supplier), null);
					if(CollectionUtils.isNotEmpty(supplierChangeList)){
						supplierChangeMapper.saveBatch(supplierChangeList);
					}
				}
			}catch (Exception e){
				log.error("【供应商登记】供应商异动记录异常: {}", e.getMessage());
			}
		}catch (QinFeiException e){
			throw e;
		} catch (DuplicateKeyException e) {
			throw new QinFeiException(1002, "存在相同的供应商公司名称和手机号！");
		}catch (Exception e){
			throw new QinFeiException(1002, "供应商登记异常！");
		}
	}

	@Transactional
	@Override
	public void editSupplier(Supplier supplier) {
		try{
			User user = AppUtil.getUser();
			validateSupplier(supplier, user);
			update(supplier);
		}catch (QinFeiException e){
			throw e;
		} catch (DuplicateKeyException e) {
			throw new QinFeiException(1002, "存在相同的供应商公司名称和手机号！");
		}catch (Exception e){
			throw new QinFeiException(1002, "供应商联系人编辑异常！");
		}
	}

	@Transactional
	@Override
	public void updateSupplier(Map<String, Object> param) {
		try{
			User user = AppUtil.getUser();
			validateSupplier(param, user);

			//异动前数据
			List<Integer> supplierIds = new ArrayList<>();
			supplierIds.add(Integer.parseInt(String.valueOf(param.get("id"))));
			List<Supplier> oldSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, supplierIds, null);
			List<Account> oldAccountList = null;

			int row = supplierMapper.updateSupplier(param);
			if(row > 0){
				Supplier supplier = new Supplier();
				supplier.setId(Integer.parseInt(String.valueOf(param.get("id"))));
				supplier.setName(String.valueOf(param.get("name")));
				supplier.setContactor(String.valueOf(param.get("contactor")));
				//同步供应商账户，先统计出该供应商有没有增加供应商账户，有就执行更新
				Integer id = supplierMapper.getSupplierAccount(supplier.getId());
				if ( id > 0){
					oldAccountList = supplierMapper.listAccountBySupplierIds(onlineTime, supplierIds);

					String oldSupplierName = supplier.getName();
					//供应商名称对应账户名称及户主，个体供应商则使用联系人
					if("1".equals(String.valueOf(supplier.getSupplierNature()))){
						supplier.setName(supplier.getContactor());
					}
					supplierMapper.updateSupplierAccount(supplier);
					supplier.setName(oldSupplierName);
				}
				// 同步临时稿件和正式稿件表
				if (supplierMapper.getArticleCount(supplier.getId()) > 0) {
					supplierMapper.updateArticleSupplierInfo(supplier);
				}
				//如果原来是企业供应商，并且名称发生变更，则更新其他联系人的供应商名称
				if("0".equals(String.valueOf(param.get("oldSupplierNature"))) && !String.valueOf(param.get("oldCompanyName")).equals(String.valueOf(param.get("name")))){
					//异动前数据
					List<Supplier> tmpSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, null, String.valueOf(param.get("oldCompanyName")));
					List<Integer> tmpSupplierIds = new ArrayList<>();
					List<String> phoneList = new ArrayList<>();
					if(CollectionUtils.isNotEmpty(tmpSupplierList)){
						oldSupplierList.addAll(tmpSupplierList);
						tmpSupplierList.forEach(o -> {
							phoneList.add(o.getPhone());
							if(!supplierIds.contains(o.getId())){
								tmpSupplierIds.add(o.getId());
							}
						});
						supplierIds.addAll(tmpSupplierIds);
					}

					//根据新名称 + 手机号判断是否有重复的
					if (CollectionUtils.isNotEmpty(phoneList) && supplierMapper.checkSupplierByPhoneList(String.valueOf(param.get("name")), phoneList, onlineTime) != null) {
						throw new QinFeiException(1002, "供应商公司下已存在相同联系人，不能进行公司名称修改！");
					}

                    if (CollectionUtils.isNotEmpty(tmpSupplierIds)) {
                        if (CollectionUtils.isNotEmpty(oldAccountList)) {
                            oldAccountList.addAll(supplierMapper.listAccountBySupplierIds(onlineTime, tmpSupplierIds));
                        } else {
                            oldAccountList = supplierMapper.listAccountBySupplierIds(onlineTime, tmpSupplierIds);
                        }
					}

					//这里更新不能涉及到老数据，所以需要添加本次改动上线时间
					param.put("onlineTime", onlineTime);
					row = supplierMapper.editSupplierCompany(param);
					if(row > 0){
						//由于历史数据可能出现名称相同问题，所以仅更新新数据的文件
						supplierMapper.updateSupplierAccountByCompanyName(String.valueOf(param.get("name")), onlineTime);
						supplierMapper.updateSupplierArtByCompanyName(String.valueOf(param.get("name")), onlineTime);
					}
				}

				//处理供应商异动，增加异常捕获，使其不影响正常操作
				try{
					//媒体编辑成功，才能进行账号异动 和 供应商异动的操作
					if(CollectionUtils.isNotEmpty(supplierIds)){
						List<Supplier> newSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, supplierIds, null);
						List<SupplierChange> supplierChangeList = supplierChangeHandler(newSupplierList, oldSupplierList);
						if(CollectionUtils.isNotEmpty(supplierChangeList)){
							supplierChangeMapper.saveBatch(supplierChangeList);
						}
						//如果有老账号
						if(CollectionUtils.isNotEmpty(oldAccountList)){
							List<Account> newAccountList = supplierMapper.listAccountBySupplierIds(onlineTime, supplierIds);
							List<AccountChange> accountChangeList = accountChangeHandler(newAccountList, oldAccountList);
							if(CollectionUtils.isNotEmpty(accountChangeList)){
								accountChangeMapper.saveBatch(accountChangeList);
							}
						}
					}
				}catch (Exception e){
					log.error("【请款/退稿管理】供应商编辑异动记录异常: {}", e.getMessage());
				}
			}
		}catch (QinFeiException e){
			throw e;
		} catch (DuplicateKeyException e) {
			throw new QinFeiException(1002, "存在相同的供应商公司名称和手机号！");
		}catch (Exception e){
			throw new QinFeiException(1002, "供应商编辑异常！");
		}
	}

	@Transactional
	@Override
	public void editSupplierCompany(Map<String, Object> param) {
		try{
			User user = AppUtil.getUser();
			if(param.get("oldCompanyName") == null || StringUtils.isEmpty(String.valueOf(param.get("oldCompanyName")))){
				throw new QinFeiException(1002, "原供应商公司名称不能为空！");
			}
			if(param.get("name") == null || StringUtils.isEmpty(String.valueOf(param.get("name")))){
				throw new QinFeiException(1002, "供应商公司名称不能为空！");
			}
			//判断当前公司名是否已经被使用,不能查询历史数据，增加本次改动上线时间
			/*if(supplierMapper.checkSupplierCompany(String.valueOf(param.get("name")), onlineTime) != null){
				throw new QinFeiException(1002, "供应商公司名称已存在！");
			}*/

			//异动前数据
			List<Integer> supplierIds = new ArrayList<>();
			List<String> phoneList = new ArrayList<>();
			List<Supplier> oldSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, null, String.valueOf(param.get("oldCompanyName")));
			if (CollectionUtils.isNotEmpty(oldSupplierList)) {
				oldSupplierList.forEach(supplier -> {
					supplierIds.add(supplier.getId());
					phoneList.add(supplier.getPhone());
				});
			}
			//根据新名称 + 手机号判断是否有重复的
            if (CollectionUtils.isNotEmpty(phoneList) && !String.valueOf(param.get("oldCompanyName")).trim().equals(String.valueOf(param.get("name")).trim()) && supplierMapper.checkSupplierByPhoneList(String.valueOf(param.get("name")), phoneList, onlineTime) != null) {
				throw new QinFeiException(1002, "供应商公司下已存在相同联系人，不能进行公司名称修改！");
			}

			param.put("updateUserId", user.getId());
			//这里更新不能涉及到老数据，所以需要添加本次改动上线时间
			param.put("onlineTime", onlineTime);
			int row = supplierMapper.editSupplierCompany(param);
			//如果有记录更新，则取更新稿件表
			if(row > 0){
				List<Account> oldAccountList = null;
				if (CollectionUtils.isNotEmpty(supplierIds)) {
					//异动前账户
					oldAccountList = supplierMapper.listAccountBySupplierIds(onlineTime, supplierIds);
				}

				supplierMapper.updateSupplierAccountByCompanyName(String.valueOf(param.get("name")), onlineTime);
				supplierMapper.updateSupplierArtByCompanyName(String.valueOf(param.get("name")), onlineTime);

				//处理账户异动，增加异常捕获，使其不影响正常操作
				try{
					if(CollectionUtils.isNotEmpty(oldAccountList)){
						List<Account> newAccountList = supplierMapper.listAccountBySupplierIds(onlineTime, supplierIds);;
						List<AccountChange> accountChangeList = accountChangeHandler(newAccountList, oldAccountList);
						if(CollectionUtils.isNotEmpty(accountChangeList)){
							accountChangeMapper.saveBatch(accountChangeList);
						}
					}
				}catch (Exception e){
					log.error("【供应商公司编辑】账户异动记录异常: {}", e.getMessage());
				}
			}

			//处理供应商异动，增加异常捕获，使其不影响正常操作
			try{
				//媒体编辑成功，才能进行媒体异动 和 供应商异动的操作
				if(CollectionUtils.isNotEmpty(oldSupplierList)){
					List<Supplier> newSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, supplierIds, null);
					List<SupplierChange> supplierChangeList = supplierChangeHandler(newSupplierList, oldSupplierList);
					if(CollectionUtils.isNotEmpty(supplierChangeList)){
						supplierChangeMapper.saveBatch(supplierChangeList);
					}
				}
			}catch (Exception e){
				log.error("【供应商公司编辑】供应商异动记录异常: {}", e.getMessage());
			}
		}catch (QinFeiException e){
			throw e;
		} catch (DuplicateKeyException e) {
			throw new QinFeiException(1002, "存在相同的供应商公司名称和手机号！");
		}catch (Exception e){
			throw new QinFeiException(1002, "供应商公司编辑异常！");
		}
	}

	@Override
	public PageInfo<Map<String, Object>> supplierAccountList(Pageable pageable, Map<String, Object> param) {
		List<Map<String, Object>> result = new ArrayList<>();
		try{
			User user = AppUtil.getUser();
			if(user != null){
				PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
				//由于可能老数据供应商公司名称和新数据相同，所以增加本次改动上线时间
				param.put("onlineTime", onlineTime);
				result = supplierMapper.supplierAccountList(param);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return new PageInfo<>(result);
	}

	@Transactional
	@Override
	public void updateSupplierUserId(Integer id, Integer userId) {
		try{
			User user = AppUtil.getUser();
			if(user == null){
				throw new QinFeiException(1002, "请先登录！");
			}
			if(id == null){
				throw new QinFeiException(1002, "供应商ID为空！");
			}
			if(userId == null){
				throw new QinFeiException(1002, "指派责任人不能为空！");
			}

			//异动前数据
			List<Supplier> oldSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, Arrays.asList(id), null);

			supplierMapper.updateSupplierUserId(id, userId, user.getId());

			//处理供应商异动，增加异常捕获，使其不影响正常操作
			try{
				List<Supplier> newSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, Arrays.asList(id), null);
				List<SupplierChange> supplierChangeList = supplierChangeHandler(newSupplierList, oldSupplierList);
				if(CollectionUtils.isNotEmpty(supplierChangeList)){
					supplierChangeMapper.saveBatch(supplierChangeList);
				}
			}catch (Exception e){
				log.error("【供应商指派】供应商异动记录异常: {}", e.getMessage());
			}
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			log.error(e.getMessage());
			throw new QinFeiException(1002, "供应商责任人指派异常！");
		}
	}

	@Override
	public List<SupplierChange> listSupplierChange(String supplierIds) {
		List<SupplierChange> supplierChangeList = new ArrayList<>();
		try{
			if(AppUtil.getUser() != null && !StringUtils.isEmpty(supplierIds)){
				List<Integer> param = new ArrayList<>();
				for(String supplierId : supplierIds.split(",")){
					param.add(Integer.parseInt(supplierId));
				}
				supplierChangeList = supplierChangeMapper.listSupplierChangeByParam(param);
			}
		}catch (Exception e){
			log.error("【供应商异动列表】供应商异动列表异常: {}", e.getMessage());
		}
		return supplierChangeList;
	}

	@Override
	public List<AccountChange> listSupplierAccountChange(String accountIds) {
		List<AccountChange> accountChangeList = new ArrayList<>();
		try{
			if(AppUtil.getUser() != null && !StringUtils.isEmpty(accountIds)){
				List<Integer> param = new ArrayList<>();
				for(String accountId : accountIds.split(",")){
					param.add(Integer.parseInt(accountId));
				}
				accountChangeList = accountChangeMapper.listAccountChangeByParam(param);
			}
		}catch (Exception e){
			log.error("【供应商账号异动列表】供应商账号异动列表异常: {}", e.getMessage());
		}
		return accountChangeList;
	}

	//供应商校验
	private void validateSupplier(Supplier supplier, User user){
		if(user == null){
			throw new QinFeiException(1002, "请先登录！");
		}
		if(supplier.getSupplierNature() != 1 && StringUtils.isEmpty(supplier.getName())){
			throw new QinFeiException(1002, "供应商公司名称不能为空！");
		}
		if(StringUtils.isEmpty(supplier.getContactor())){
			throw new QinFeiException(1002, "供应商联系人不能为空！");
		}
		if(StringUtils.isEmpty(supplier.getPhone())){
			throw new QinFeiException(1002, "供应商联系人手机号不能为空！");
		}
		if(!StringUtils.isEmpty(supplier.getQqwechat())){
			supplier.setQqwechat(EncryptUtils.encrypt(supplier.getQqwechat().trim()));//联系人微信加密
		}
		if(!StringUtils.isEmpty(supplier.getQq())){
			supplier.setQq(EncryptUtils.encrypt(supplier.getQq().trim()));//联系人QQ加密
		}
        supplier.setName(supplier.getName().trim());
		supplier.setPhone(EncryptUtils.encrypt(supplier.getPhone().trim()));//联系人电话号码加密
		supplier.setUpdateUserId(user.getId());
		supplier.setUpdateTime(new Date());
		checkSupplier(supplier.getId(), supplier.getName(), supplier.getPhone());
	}

	//供应商校验
	private void validateSupplier(Map<String, Object> supplier, User user){
		if(user == null){
			throw new QinFeiException(1002, "请先登录！");
		}
		if(supplier.get("id") == null){
			throw new QinFeiException(1002, "供应商ID不能为空！");
		}
		if("1".equals(String.valueOf(supplier.get("supplierNature")))  && StringUtils.isEmpty(String.valueOf(supplier.get("name")))){
			throw new QinFeiException(1002, "供应商公司名称不能为空！");
		}
		if(StringUtils.isEmpty(String.valueOf(supplier.get("contactor")))){
			throw new QinFeiException(1002, "供应商联系人不能为空！");
		}
		if(StringUtils.isEmpty(String.valueOf(supplier.get("phone")))){
			throw new QinFeiException(1002, "供应商联系人手机号不能为空！");
		}
		if(!StringUtils.isEmpty(String.valueOf(supplier.get("qqwechat")))){
			supplier.put("qqwechat", EncryptUtils.encrypt(String.valueOf(supplier.get("qqwechat")).trim()));//联系人微信加密
		}
		if(!StringUtils.isEmpty(String.valueOf(supplier.get("qq")))){
			supplier.put("qq", EncryptUtils.encrypt(String.valueOf(supplier.get("qq")).trim()));//联系人QQ加密
		}
		supplier.put("updateUserId", user.getId());
		supplier.put("updateTime", new Date());
		Integer id = null;
		if(supplier.get("id") != null && !StringUtils.isEmpty(String.valueOf(supplier.get("id")))){
			id = Integer.parseInt(String.valueOf(supplier.get("id")));
		}
		//如果是企业供应商，判断是否改了名称，如果改了则校验名称是否存在
		/*if("0".equals(String.valueOf(supplier.get("oldSupplierNature"))) && !String.valueOf(supplier.get("oldCompanyName")).equals(String.valueOf(supplier.get("name")))){
			//不能查询历史数据，增加本次改动上线时间
			if(supplierMapper.checkSupplierCompany(String.valueOf(supplier.get("name")), onlineTime) != null){
				throw new QinFeiException(1002, "供应商公司名称已存在！");
			}
		}*/
		checkSupplier(id, String.valueOf(supplier.get("name")), String.valueOf(supplier.get("phone")));
		supplier.put("phone", EncryptUtils.encrypt(String.valueOf(supplier.get("phone")).trim()));//联系人电话号码加密
	}

	//查询媒体列表
	public PageInfo<Supplier> listall(Map<String, Object> map, Pageable pageable) {
		/*User user = AppUtil .getUser();
		//如果没有输入条件，则只能查看自己的，否则可以根据条件精准查询
		if(StringUtils.isEmpty(map.get("name")) && StringUtils.isEmpty(map.get("contactor")) && StringUtils.isEmpty(map.get("phone"))
				&& StringUtils.isEmpty(map.get("qqwechat")) && StringUtils.isEmpty(map.get("creator"))){
			map.put("currentUserId",user.getId());
		}*/

		// 拼接查询参数；
		map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
		map.put("state", -9);
		if(map.get("phone") != null){
			map.put("phone", EncryptUtils.encrypt((String) map.get("phone")));
		}
		if(map.get("qqwechat") != null){
			map.put("qqwechat", EncryptUtils.encrypt((String) map.get("qqwechat")));
		}
		if(map.get("qq") != null){
			map.put("qq", EncryptUtils.encrypt((String) map.get("qq")));
		}

		// 获取用户有权限访问的媒体板块；
		/*List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(AppUtil.getUser().getId());
		List<Integer> mediaTypeIds = new ArrayList<>();
		Map<Integer, MediaPlate> mediaTypeMap = new HashMap<>(); //缓存用户板块
		for (MediaPlate mediaPlate : mediaPlateList) {
			mediaTypeIds.add(mediaPlate.getId());
			mediaTypeMap.put(mediaPlate.getId(), mediaPlate);
		}
		map.put("companyCode",dept.getCompanyCode());
		map.put("mediaTypes", mediaTypeIds.isEmpty() ? null : mediaTypeIds);*/
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());// 设置分页属性；
		List<Supplier> list = supplierMapper.listPage(map);// 获取查询结果；
		User user = AppUtil.getUser();
//		String depts  = userService.getChilds(user.getDeptId());
//		if (!StringUtils.isEmpty(depts) && depts.indexOf("$,") > -1) {
//			depts = depts.substring(2);
//		}
		for (Supplier data : list) {
			//对手机号码和QQ微信进行解密
			if(!StringUtils.isEmpty(data.getPhone())){
				String phone = EncryptUtils.decrypt(data.getPhone());
				data.setPhone(phone);
			}
			if(!StringUtils.isEmpty(data.getQqwechat())){
				String qqwechat = EncryptUtils.decrypt(data.getQqwechat());
				data.setQqwechat(qqwechat);
			}
			if(!StringUtils.isEmpty(data.getQq())){
				String qq = EncryptUtils.decrypt(data.getQq());
				data.setQq(qq);
			}
			data.setFlag(false);
			if(user.getCompanyCode().equals(data.getCompanyCode())){
				data.setFlag(true);
			}
//			if (user.getIsMgr()==1 && !StringUtils.isEmpty(depts) && depts.contains(data.getUser().getDeptId().toString())){
//				data.setFlag(true);
//			}
			/*if(mediaTypeMap.get(data.getMediaTypeId()) != null){
				data.setMediaTypeName(mediaTypeMap.get(data.getMediaTypeId()).getName());
			}*/
		}
        return new PageInfo(list);
	}

	@Override
	@Transactional
	public Supplier update(Supplier supplier) {
		//异动前数据
		List<Supplier> oldSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, Arrays.asList(supplier.getId()), null);
		supplier.setContactor(supplier.getContactor().trim());
		supplierMapper.update(supplier);
		//同步供应商账户，先统计出该供应商有没有增加供应商账户，有就执行更新
		Integer id = supplierMapper.getSupplierAccount(supplier.getId());
		if ( id > 0){
			String oldSupplierName = supplier.getName();
			//供应商名称对应账户名称及户主，个体供应商则使用联系人
			if("1".equals(String.valueOf(supplier.getSupplierNature()))){
				supplier.setName(supplier.getContactor());
			}

			//异动前账户
			List<Account> oldAccountList = supplierMapper.listAccountBySupplierIds(onlineTime, Arrays.asList(supplier.getId()));

			supplierMapper.updateSupplierAccount(supplier);
			supplier.setName(oldSupplierName);

			//处理账户异动，增加异常捕获，使其不影响正常操作
			try{
				List<Account> newAccountList = supplierMapper.listAccountBySupplierIds(onlineTime, Arrays.asList(supplier.getId()));
				List<AccountChange> accountChangeList = accountChangeHandler(newAccountList, oldAccountList);
				if(CollectionUtils.isNotEmpty(accountChangeList)){
					accountChangeMapper.saveBatch(accountChangeList);
				}
			}catch (Exception e){
				log.error("【供应商编辑】账户异动记录异常: {}", e.getMessage());
			}
		}

		// 同步临时稿件和正式稿件表
		if (supplierMapper.getArticleCount(supplier.getId()) > 0) {
			supplierMapper.updateArticleSupplierInfo(supplier);
		}

		//处理供应商异动，增加异常捕获，使其不影响正常操作
		try{
			//供应商编辑成功，才能供应商异动的操作
			if(supplier != null && supplier.getId() != null){
				List<Supplier> newSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, Arrays.asList(supplier.getId()), null);
				List<SupplierChange> supplierChangeList = supplierChangeHandler(newSupplierList, oldSupplierList);
				if(CollectionUtils.isNotEmpty(supplierChangeList)){
					supplierChangeMapper.saveBatch(supplierChangeList);
				}
			}
		}catch (Exception e){
			log.error("【供应商编辑】供应商异动记录异常: {}", e.getMessage());
		}
		return (supplier);
	}

	/**
	 * 删除供应商；
	 *
	 * @param id：供应商ID；
	 */
	@Override
	@Transactional
	public ResponseData delete(Integer id) {
		if(id == null){
			throw new QinFeiException(1002,"请选择需要删除的供应商！");
		}
		List<Integer> mediaIdList = mediaSupplierRelateAuditMapper.listMediaIdBySupplierId(id);
		// 验证是否有关联媒体；
		if (CollectionUtils.isNotEmpty(mediaIdList)) {
			throw new QinFeiException(1002,"该供应商尚有媒体关联，无法删除！");
		}
		//验证是否管理稿件
		if(supplierMapper.getArtCountBySupplierId(id) > 0){
			throw new QinFeiException(1002,"该供应商尚有稿件关联，无法删除！");
		}

		supplierMapper.physicalDeletionSupplier(id);
		return ResponseData.ok().putDataValue("message","操作成功");
	}

	@Override
	@Transactional
	public void active(Integer id) {
		try{
			User user = AppUtil.getUser();
			if(user == null){
				throw new QinFeiException(1002,"请先登录！");
			}

            //异动前数据
            List<Supplier> oldSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, Arrays.asList(id), null);

			supplierMapper.updateMediaSupplierState(id,0, user.getId());

            //处理供应商异动，增加异常捕获，使其不影响正常操作
            try {
                List<Supplier> newSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, Arrays.asList(id), null);
                List<SupplierChange> supplierChangeList = supplierChangeHandler(newSupplierList, oldSupplierList);
                if (CollectionUtils.isNotEmpty(supplierChangeList)) {
                    supplierChangeMapper.saveBatch(supplierChangeList);
                }
            } catch (Exception e) {
                log.error("【供应商编辑】供应商异动记录异常: {}", e.getMessage());
            }
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			throw new QinFeiException(1002, "供应商联系人启用异常！");
		}
	}

	@Override
	@Transactional
	public void stop(Integer id) {
		try{
			User user = AppUtil.getUser();
			if(user == null){
				throw new QinFeiException(1002,"请先登录！");
			}

            //异动前数据
            List<Supplier> oldSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, Arrays.asList(id), null);

			supplierMapper.updateMediaSupplierState(id,1, user.getId());

            //处理供应商异动，增加异常捕获，使其不影响正常操作
            try {
                List<Supplier> newSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, Arrays.asList(id), null);
                List<SupplierChange> supplierChangeList = supplierChangeHandler(newSupplierList, oldSupplierList);
                if (CollectionUtils.isNotEmpty(supplierChangeList)) {
                    supplierChangeMapper.saveBatch(supplierChangeList);
                }
            } catch (Exception e) {
                log.error("【供应商编辑】供应商异动记录异常: {}", e.getMessage());
            }
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			throw new QinFeiException(1002, "供应商联系人禁用异常！");
		}
	}

	@Override
	public List<Supplier> listAllSupplierByPlateCompany(Integer mediaPlateId, Integer standarPhoneFlag) {
		Map param = new HashMap();
		param.put("mediaTypeId", mediaPlateId);
		param.put("standarPhoneFlag", standarPhoneFlag);
		param.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
		return supplierMapper.listSupplierByPlateCompany(param);
	}

	@Override
	public Supplier getById(Integer id) {
		Supplier supplier = supplierMapper.getById(id);
		//对手机号码和QQ微信进行解密
		if(!StringUtils.isEmpty(supplier.getPhone())){
			String phone = EncryptUtils.decrypt(supplier.getPhone());
			supplier.setPhone(phone);
		}
		if(!StringUtils.isEmpty(supplier.getQqwechat())){
			String qqwechat = EncryptUtils.decrypt(supplier.getQqwechat());
			supplier.setQqwechat(qqwechat);
		}
		return supplier;
	}

	/**
	 * 判断供应商+联系人是否重复
	 *
	 * @returnat
	 */
	public boolean isRepeat(String supplierName, String contactor, Integer mediaTypeId, String phone, String qqwechat) {
		Boolean flag = false;
		String companyCode = AppUtil.getUser().getDept().getCompanyCode();
		if (supplierMapper.getIdBySupplierNameAndContactor(supplierName, contactor, mediaTypeId, companyCode) > 0) {
			flag = true;
		}
		if (!flag && !StringUtils.isEmpty(phone)) {
			phone = EncryptUtils.encrypt(phone); //加密
			if (supplierMapper.getIdByContactorAndPhone(contactor, phone, mediaTypeId, companyCode) > 0) {
				flag = true;
			}
		}
		if (!flag && !StringUtils.isEmpty(qqwechat)) {
			qqwechat = EncryptUtils.encrypt(qqwechat); //加密
			if (supplierMapper.getIdByContactorAndQqwechat(contactor, qqwechat, mediaTypeId, companyCode) > 0) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 判断供应商+联系人是否重复
	 *
	 * @returnat
	 *
	 */
	@Override
	public boolean isRepeat(String supplierName, String contactor, Integer mediaTypeId, String phone, String qqwechat, Integer id) {
		Boolean flag = false;
		String companyCode = AppUtil.getUser().getDept().getCompanyCode();
		if (supplierMapper.getCountBySupplierNameAndContactor(supplierName, contactor, mediaTypeId, companyCode,id) > 0) {
			flag = true;
		}
		if (!flag && !StringUtils.isEmpty(phone)) {
			phone = EncryptUtils.encrypt(phone); //加密
			if (supplierMapper.getCountByContactorAndPhone(contactor, phone, mediaTypeId, companyCode, id) > 0) {
				flag = true;
			}
		}
		if (!flag && !StringUtils.isEmpty(qqwechat)) {
			qqwechat = EncryptUtils.encrypt(qqwechat); //加密
			if (supplierMapper.getCountByContactorAndQqwechat(contactor, qqwechat, mediaTypeId, companyCode, id) > 0) {
				flag = true;
			}
		}
		return flag;
	}

	@Override
	public PageInfo<Map> querySupplierList(int pageNum, int pageSize, Map map) {
		PageHelper.startPage(pageNum, pageSize);
		List<Map> list = supplierMapper.querySupplierList(map);
        return new PageInfo<>(list);
	}

	@Override
	public PageInfo<Map> querySupplierListByTypeNew(int pageNum, int pageSize, Map map) {
		PageHelper.startPage(pageNum, pageSize);
		map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
		List<Map> list = supplierMapper.querySupplierListByTypeNew(map);
		User user = AppUtil.getUser();
//		String depts  = userService.getChilds(user.getDeptId());
//		if (!StringUtils.isEmpty(depts) && depts.indexOf("$,") > -1) {
//			depts = depts.substring(2);
//		}
		for (Map data : list) {
			//对手机号码和QQ微信进行解密
			if(data.get("phone") != null && !StringUtils.isEmpty(String.valueOf(data.get("phone")))){
				String phone = EncryptUtils.decrypt(String.valueOf(data.get("phone")));
				data.put("phone", phone);
			}
			if(data.get("qqwechat") != null && !StringUtils.isEmpty(String.valueOf(data.get("qqwechat")))){
				String qqwechat = EncryptUtils.decrypt(String.valueOf(data.get("qqwechat")));
				data.put("qqwechat", qqwechat);
			}
			if(data.get("qq") != null && !StringUtils.isEmpty(String.valueOf(data.get("qq")))){
				String qq = EncryptUtils.decrypt(String.valueOf(data.get("qq")));
				data.put("qq", qq);
			}
			data.put("flag",false);
			if(data.get("companyCode") != null && !StringUtils.isEmpty(String.valueOf(data.get("companyCode")))){
				String companyCode = String.valueOf(data.get("companyCode"));
				if(user.getCompanyCode().equals(companyCode)){
				   data.put("flag", true);
				}
			}
//			if (user.getIsMgr()==1 && !StringUtils.isEmpty(depts) && depts.contains(data.get("deptId").toString())){
//				data.put("flag",true);
//			}
		}
		return new PageInfo<>(list);
	}

	@Override
	public Map<String, Integer> listAllSupplierMap() {
		//不能查询历史数据，增加本次改动上线时间
		List<Supplier> suppliers = supplierMapper.listSupplier(onlineTime);
		Map<String, Integer> phoneDatas = new HashMap<>();	//供应商公司+手机号；
		for (Supplier data : suppliers) {
			if(!StringUtils.isEmpty(data.getPhone())){
                phoneDatas.put(String.format("%s*%s", data.getName(), data.getPhone()), data.getId());
			}
		}
		return phoneDatas;
	}

	/**
	 * 批量保存媒体数据；
	 *
	 * @param excelContent：上传文件数据；
	 */
	@Transactional
	@Override
	public Map<String, Object> batchAddSupplier(List<Object[]> excelContent) {
		String companyCode = AppUtil.getUser().getDept().getCompanyCode();
		//模板列名
		List<String> rowTitles = getSupplierForms();
		// 定义集合保存数据；
		List<Supplier> suppliers = new ArrayList<>();
		// 用于保存导入失败的数据；
		List<Object[]> invalidDatas = new ArrayList<>();
		// 登录人ID；
		Integer userId = AppUtil.getUser().getId();
		//获取数据库的所有供应商集合，用来校验上传的内容是否有重复数据； 联系人+联系方式；
		Map<String, Integer> phoneMap = listAllSupplierMap();
		Object [] row = null;
		for(int i = 0; i < excelContent.size(); i++){  //Excel行数据循环
			row = excelContent.get(i);
			if(row.length <= 1){
				continue; //如果行数据仅有一列，则为Excel文件中说明信息，直接处理下一行数据
			}
			if(row.length != rowTitles.size()){  //如果行数据与媒体模板列个数不一致，直接判断下一个
				row = Arrays.copyOf(row, row.length + 1);
				row[row.length - 1] = "第"+(i+1)+"行数据与模板列格式不对应";
				invalidDatas.add(row);
				continue;
			}
			boolean isValidSuccess = true; // 校验成功标志，默认校验成功
            List<String> rowErrorMsgList = null; //记录整行的错误信息
			Supplier currentSupplier = new Supplier();  //当前待保存的供应商信息
			currentSupplier.setCompanyCode(companyCode); //设置当前公司
			currentSupplier.setCreateTime(new Date());
			currentSupplier.setUpdateTime(new Date());
			currentSupplier.setCreator(userId);
			currentSupplier.setUpdateUserId(userId);
			for(int j = 0; j < row.length; j++){  //Excel列数据循环
				String errorInfo = null;
				String columnValue = String.valueOf(row[j]); //获取列值
				boolean requiredFlag = validField(rowTitles, j);//校验字段是否必输
				if(!StringUtils.isEmpty(columnValue)){
				    //供应商性质
                    if(j == 0){
                        if("企业供应商".equals(columnValue) || "个体供应商".equals(columnValue)){
                            if("企业供应商".equals(columnValue)){
                                currentSupplier.setSupplierNature((byte) 0);
                            }else {
                                currentSupplier.setSupplierNature((byte) 1);
                            }
                        }else {
                            errorInfo = "填写错误，仅支持【企业供应商 和 个体供应商】";
                        }
                    }else if(j == 1){  //供应商公司名称
						currentSupplier.setName(columnValue);
                        byte standarCompanyFlag = 0;//是否标准公司：0-非标准、1-标准，默认0
						if(currentSupplier.getSupplierNature() != null){
							if(currentSupplier.getSupplierNature() == 0){//供应商性质：0-企业供应商、1-个体供应商
								ResponseData result = companyService.checkCompany(currentSupplier.getName());//公司标准校验
								if (result.getCode() == 200) {
									standarCompanyFlag = 1;
								} else {
									standarCompanyFlag = 0;
								}
							}else {
								if(!"个体供应商".equals(columnValue)){
									errorInfo = "填写错误，供应商性质为个体供应商公司名称仅能填写个体供应商";
								}
							}
						}
                        currentSupplier.setStandarCompanyFlag(standarCompanyFlag);
					}else if(j == 2){ //供应商联系人名称
						currentSupplier.setContactor(columnValue);
					}else if(j == 3){ //手机号
						//是否标准手机号：0-非标准、1-标准
						if(columnValue.matches("^[1]([3-9])[0-9]{9}$")){
							currentSupplier.setStandarPhoneFlag((byte) 1);
						}else {
							currentSupplier.setStandarPhoneFlag((byte) 0);
						}
						currentSupplier.setPhone(EncryptUtils.encrypt(columnValue));
						if (phoneMap.get(currentSupplier.getName() + "*" + currentSupplier.getPhone()) != null){
                            errorInfo = "和供应商公司名称已存在";
						}
					}else if(j == 4){ //微信号
						currentSupplier.setQqwechat(EncryptUtils.encrypt(columnValue));
					}else if(j == 5){ //QQ号
						currentSupplier.setQq(EncryptUtils.encrypt(columnValue));
					}else {  //联系人备注
						currentSupplier.setContactorDesc(columnValue);
					}
				}else{
					if(requiredFlag){ //必输
						errorInfo = "不能为空";
					}
				}
				if(!StringUtils.isEmpty(errorInfo)){
                    if(CollectionUtils.isEmpty(rowErrorMsgList)){
                        rowErrorMsgList = new ArrayList<>();
                    }
                    if(rowTitles.get(j).matches("^\\*.+\\*$")){ //如果是必输字段，则去除开头和结尾的*
                        rowErrorMsgList.add(rowTitles.get(j).substring(1,rowTitles.get(j).length()-1) + errorInfo);
                    }else{
                        rowErrorMsgList.add(rowTitles.get(j) + errorInfo);
                    }
                    isValidSuccess = false;
				}
			}
			if(!isValidSuccess){  //数据校验不成功继续下一个媒体
                row = Arrays.copyOf(row, row.length + 1);
                row[row.length - 1] = String.valueOf(rowErrorMsgList);
                invalidDatas.add(row); //缓存校验未通过数据
				continue;
			}
			//添加供应商
			suppliers.add(currentSupplier);
			//将当前供应商存入校验供应商中
            phoneMap.put(currentSupplier.getName() + "*" + currentSupplier.getPhone(), 1);
		}
		// 判断是否存在有效数据；
		// 如果有错误，则数据不入库，提示错误信息
		int errorSize = invalidDatas.size();
		if (errorSize > 0) {
			// 导入失败原因；
			rowTitles.add("失败原因");
			Map<String, Object> errorMap = new HashMap<>();
			errorMap.put("templateName","供应商导入失败内容");
			errorMap.put("rowTitles",rowTitles);
			errorMap.put("exportData",invalidDatas);
			return errorMap;
		}

		int successSize = suppliers.size();
		List<Integer> supplierIds = new ArrayList<>();//保存的供应商ID集合
		if (successSize > 0) {
			// 定义需要进行分割的尺寸；
			int subLength = 100;
			// 计算需要插入的次数，100条插入一次；
			int insertTimes = successSize % subLength == 0 ? successSize / subLength : successSize / subLength + 1;
			// 100条以上才需要进行处理；
			if (insertTimes > 1) {
				List<Supplier> insertData;
				// 计算需要循环插入的次数；
				for (int i = 0; i < insertTimes; i++) {
					insertData = new ArrayList<>();
					// 计算起始位置，且j的最大值应不能超过数据的总数；
					for (int j = i * subLength; j < (i + 1) * subLength && j < successSize; j++) {
						insertData.add(suppliers.get(j));
					}
					supplierMapper.saveBatch(insertData);
					insertData.forEach(supplier -> {
						supplierIds.add(supplier.getId());
					});
				}
			} else {
				supplierMapper.saveBatch(suppliers);
				suppliers.forEach(supplier -> {
					supplierIds.add(supplier.getId());
				});
			}

			//处理供应商异动，增加异常捕获，使其不影响正常操作
			try{
				//媒体编辑成功，才能进行媒体异动 和 供应商异动的操作
				if(CollectionUtils.isNotEmpty(supplierIds)){
					List<Supplier> newSupplierList = supplierMapper.listSupplierByIdsOrName(onlineTime, supplierIds, null);
					List<SupplierChange> supplierChangeList = supplierChangeHandler(newSupplierList, null);
					if(CollectionUtils.isNotEmpty(supplierChangeList)){
						supplierChangeMapper.saveBatch(supplierChangeList);
					}
				}
			}catch (Exception e){
				log.error("【供应商导入】供应商异动记录异常: {}", e.getMessage());
			}
		}
		return null;
	}

	/**
	 * 获取用户的媒体板块Map
	 * @param userId
	 * @return
	 */
	private Map<String, Integer> getUserPlateMap(Integer userId){
		List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(userId);
		Map<String, Integer> userPlateMap = new HashMap<>();
		if(CollectionUtils.isNotEmpty(mediaPlateList)){
			for(MediaPlate mediaPlate : mediaPlateList){
				userPlateMap.put(mediaPlate.getName(), mediaPlate.getId());
			}
		}
		return userPlateMap;
	}

	/**
	 * 判断指定列值是否必输（根据**判断）
	 * @param rowTitles 所有列名称
	 * @param columnIndex 列索引
	 */
	private boolean validField(List<String> rowTitles, int columnIndex){
		if(rowTitles.get(columnIndex).matches("^\\*.+\\*$")){
			return true;
		}
		return false;
	}

	/**
	 * 获取供应商导入模板的列名集合；
	 *
	 * @return ：列名信息集合；
	 */
	@Override
	public List<String> getSupplierForms() {
		List<String> rowTitles = new ArrayList<>();
		rowTitles.add("*供应商性质*");
		rowTitles.add("*供应商公司名称*");
		rowTitles.add("*联系人*");
		rowTitles.add("*手机号*");
		rowTitles.add("*微信号*");
		rowTitles.add("QQ号");
		rowTitles.add("备注");
		return rowTitles;
	}

	@Override
	public PageInfo<Supplier> listSupplierByPlateCompany(Map<String, Object> map, Pageable pageable) {
		// 获取传入的媒体板块；
		User user = AppUtil.getUser();
		List<Supplier> list; // 获取查询结果；
		if (user == null) {
			list = new ArrayList<>();
		} else {
//			String depts  = userService.getChilds(user.getDeptId());
//			if (!StringUtils.isEmpty(depts) && depts.indexOf("$,") > -1) {
//				depts = depts.substring(2);
//			}
			// 设置分页属性；
			PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
			map.put("standarPhoneFlag", 1);//供应商联系人标准才能绑定媒体
			map.put("onlineTime", onlineTime); //不能查询历史数据，增加本次改动上线时间
			list = supplierMapper.listSupplierByPlateCompany(map);
			for (Supplier data : list) {
				//对手机号码和QQ微信进行解密
				if(!StringUtils.isEmpty(data.getPhone())){
					String phone = EncryptUtils.decrypt(data.getPhone());
					data.setPhone(phone);
				}
				data.setFlag(false);
				if(user.getCompanyCode().equals(data.getCompanyCode())){
					data.setFlag(true);
				}
//				if (user.getIsMgr() == 1  && !StringUtils.isEmpty(depts) && depts.contains(data.getDeptId().toString())){
//					data.setFlag(true);
//				}
			}
		}
		return  new PageInfo(list);
	}

	/**
	 * 供应商模板文件的操作提示信息；
	 *
	 * @return ：操作提示信息集合；
	 */
	@Override
	public List<String> getSupplierNotices() {
		List<String> notices = new ArrayList<>();
		notices.add("表格的第一行、第一列留空请勿删除；");
		notices.add("表格数据第一列不能为空；");
		notices.add("带星号标注的列必须有内容；");
		notices.add("供应商性质：企业供应商、个体供应商；");
		notices.add("个体供应商，供应商公司名称必须填写个体供应商；");
		notices.add("联系人地址信息可以填入联系人备注中；");
		return notices;
	}

	/**
	 * 导出供应商
	 * @param map
	 * @param outputStream
	 * @return
	 */
	@Override
	public List<Map> export(Map map, OutputStream outputStream) {
		User user = AppUtil .getUser();
		map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
		map.put("state", -9);
		if(map.get("phone") != null){
			map.put("phone", EncryptUtils.encrypt((String) map.get("phone")));
		}
		if(map.get("qqwechat") != null){
			map.put("qqwechat", EncryptUtils.encrypt((String) map.get("qqwechat")));
		}
		if(map.get("qq") != null){
			map.put("qq", EncryptUtils.encrypt((String) map.get("qq")));
		}

		//如果没有输入供应商名称，则只能查看自己的，否则可以根据供应商精准查询
		/*if(StringUtils.isEmpty(map.get("name"))){
			map.put("currentUserId",AppUtil.getUser().getId());
		}*/
		// 获取用户有权限访问的媒体板块；
//		List<MediaType> mediaTypes = mediaTypeService.listByUserId(AppUtil.getUser().getId());
		List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(AppUtil.getUser().getId());
		List<Integer> mediaTypeIds = new ArrayList<>();
		for (MediaPlate mediaPlate : mediaPlateList) {
			mediaTypeIds.add(mediaPlate.getId());
		}
//		User user = AppUtil .getUser();
//		map.put("companyCode",user.getCompanyCode());
//		map.put("mediaTypes", mediaTypeIds.isEmpty() ? null : mediaTypeIds);
		List<Map> list = supplierMapper.listAllSupplier(map);

		if(CollectionUtils.isNotEmpty(list)){
			String [] titles = {"供应商性质","供应商公司名称","是否标准公司","供应商联系人","是否规范联系人","联系电话","微信号","QQ号","责任人","登记时间","更新时间","状态"};
			String [] obj = {"supplier_nature","name","standar_company_flag","contactor","standar_phone_flag","phone","qqwechat","qq","userName","create_time","update_time","state"};
			ExcelUtil.exportExcel("媒体供应商导出",titles,obj,list,outputStream,"yyyy-MM-dd",(sheet, rowIndex, cellIndex, row, cell, field, value)->{
				if(value!=null){
					if("create_time".equals(field) || "update_time".equals(field)){
						cell.setCellValue(value.toString());
					}else if("supplier_nature".equals(field)){
						if("1".equals(value.toString())){
							cell.setCellValue("个体供应商");
						}else{
							cell.setCellValue("企业供应商");
						}
					} else if("standar_company_flag".equals(field)){
						if("1".equals(value.toString())){
							cell.setCellValue("标准");
						}else{
							cell.setCellValue("非标准");
						}
					}  else if("standar_phone_flag".equals(field)){
						if("1".equals(value.toString())){
							cell.setCellValue("规范");
						}else{
							cell.setCellValue("不规范");
						}
					} else if("state".equals(field)){
						if("1".equals(value.toString())){
							cell.setCellValue("禁用");
						}else{
							cell.setCellValue("启用");
						}
					}else if ("phone".equals(field) || "qqwechat".equals(field) || "qq".equals(field)){ //导出时对手机号 和 QQ微信解密
						String creator = String.valueOf(list.get(rowIndex).get("creator"));
						boolean flag =false;
						boolean mediaFlagMgr = false;

						// 该供应商 所绑定的板块
						if (!StringUtils.isEmpty(list.get(rowIndex).get("plateIds"))){
							String plateIds = String.valueOf(list.get(rowIndex).get("plateIds"));
							String[] plateIdList = plateIds.split(",");
							if (plateIdList.length>0){
								for (int i=0; i< plateIdList.length ; i++){
									if (mediaTypeIds.contains(Integer.parseInt(plateIdList[i]))){
										flag = true;
									}
								}
							}
						}
						String depts  = userService.getChilds(user.getDeptId());
						if (!org.springframework.util.StringUtils.isEmpty(depts) && depts.indexOf("$,") > -1) {
							depts = depts.substring(2);
						}
						String phone = EncryptUtils.decrypt(value.toString());
						if (user.getIsMgr()==1 && depts.contains(String.valueOf(list.get(rowIndex).get("deptId")))){
							mediaFlagMgr= true;
						}
						if((user.getId().toString().equals(creator)) || (flag && mediaFlagMgr)){
							cell.setCellValue(phone);
						}else{
							if(phone.length() >= 11){
								String start = phone.length() > 11 ? "*****" : "****";
								phone = phone.substring(0, 3) + start + phone.substring(phone.length() - 4, phone.length());
							}else if(phone.length() >= 3){
								phone = phone.substring(0, 1) + "***" + phone.substring(phone.length() - 1);
							}else {
								phone = "**";
							}
							cell.setCellValue(phone);
						}
					}else{
						cell.setCellValue(value.toString());
					}
				}
			});
		}
		return list;
	}

	@Override
	@Transactional
	public void copy(Integer id) {
		User user = AppUtil.getUser();
		Date currentDate = new Date();
		if(user == null){
			throw new QinFeiException(1002,"请先登录！");
		}
        if(!IConst.COMPANY_CODE_XH.equals(user.getDept().getCompanyCode())){
            throw new QinFeiException(1002,"媒体拷贝功能本公司暂时不支持！");
        }
		Supplier supplier = supplierMapper.getById(id);
		if(isRepeat(supplier.getName(), supplier.getContactor(), supplier.getMediaTypeId(), supplier.getPhone(), supplier.getQqwechat())){
			throw new QinFeiException(1002, "【供应商名称+联系人】已经存在或【联系人+电话】已经存在或【联系人+QQ微信】已经存在！");
		}
		Supplier currentSupplier = new Supplier();
		BeanUtils.copyProperties(supplier, currentSupplier);
		currentSupplier.setId(null);
		currentSupplier.setCreator(user.getId());
		currentSupplier.setIsCopy(1);
		currentSupplier.setCopyRemarks("【"+ DateUtils.format(currentDate,"yyyy-MM-dd HH:mm:ss")+"】拷贝["+supplier.getCompanyCode()+"]供应商.");
		currentSupplier.setCompanyCode(user.getDept().getCompanyCode());
		currentSupplier.setCreateTime(currentDate);
		currentSupplier.setUpdateUserId(user.getId());
		currentSupplier.setUpdateTime(currentDate);
		supplierMapper.insert(currentSupplier);
	}

	@Override
	@Transactional
	public ResponseData handlerSameSupplierPhone() {
		try {
			List<Supplier> supplierList = supplierMapper.listSameSupplierId(onlineTime);
			if (CollectionUtils.isNotEmpty(supplierList)) {
				for (int i = 0; i < supplierList.size(); i++) {
					Supplier supplier = supplierList.get(i);
					if (!StringUtils.isEmpty(supplier.getPhone())) {
						supplier.setPhone(EncryptUtils.encrypt(String.format("%s,id=%s", EncryptUtils.decrypt(supplier.getPhone()), supplier.getId())));
					} else {
						supplier.setPhone(EncryptUtils.encrypt(String.format("id=%s", String.valueOf(supplier.getId()))));
					}
				}

				int size = supplierList.size();
				int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
				int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1; // 计算需要插入的次数，100条插入一次；
				if (insertTimes > 1) {
					List<Supplier> insertData;
					for (int i = 0; i < insertTimes; i++) {
						insertData = new ArrayList<>();
						for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
							insertData.add(supplierList.get(j));
						}
						supplierMapper.batchUpdateSameSupplier(insertData);
					}
				} else {
					supplierMapper.batchUpdateSameSupplier(supplierList);
				}
			}
			ResponseData responseData = ResponseData.ok();
			responseData.putDataValue("size", supplierList.size());
			return responseData;
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(102, "处理历史稿件异常！");
		}
	}

	/**
	 * 供应商异动处理
	 * @param newSupplierList 修改后数据
	 * @param oldSupplierList 修改前数据
	 * @return 异动列表
	 */
	private List<SupplierChange> supplierChangeHandler(List<Supplier> newSupplierList, List<Supplier> oldSupplierList){
		//如果新供应商数据不存在，则直接返回
		if(CollectionUtils.isEmpty(newSupplierList)){
			return null;
		}
		Map<Integer, Supplier> newSupplierMap = new HashMap<>();
		Map<Integer, Supplier> oldSupplierMap = new HashMap<>();
		newSupplierList.forEach(supplier -> {
			newSupplierMap.put(supplier.getId(), supplier);
		});
		if(CollectionUtils.isNotEmpty(oldSupplierList)){
			oldSupplierList.forEach(supplier -> {
				oldSupplierMap.put(supplier.getId(), supplier);
			});
		}
		//返回的异动记录
		List<SupplierChange> supplierChangeList = new ArrayList<>();
		for(Integer supplierId : newSupplierMap.keySet()){
			Supplier newSupplier = newSupplierMap.get(supplierId);
			Supplier oldSupplier = oldSupplierMap.get(supplierId);
			SupplierChange supplierChange = new SupplierChange();
			supplierChange.setSupplierId(supplierId);
			supplierChange.setSupplierName(newSupplier.getName());
			supplierChange.setUserId(AppUtil.getUser().getId());
			supplierChange.setUserName(AppUtil.getUser().getName());
			supplierChange.setSupplierContactor(newSupplier.getContactor());
			Map<String, Map<String, Object>> result = getSupplierChangeField(supplierChange, newSupplier, oldSupplier);
			if(result != null){
				supplierChange.setChangeContent(JSON.toJSONString(result));
				supplierChangeList.add(supplierChange);
			}
		}
		return supplierChangeList;
	}

	/**
	 * 获取媒体改变数据，数据格式如下:recover代表要恢复的字段数据，change代表异动详情展示的字段
	 * {
	 *     recover(恢复字段):{
	 *         baseData:{}
	 *     },
	 *     change(异动数据):{
	 *         fieldList(异动字段):[
	 *             {cell:'', cellName:'', oldCellValue:'', oldCellText:'', newCellValue:'', newCellText:''}
	 * 	    ],
	 *         opDesc:'',
	 *         op:''
	 *     },
	 *     export(导出异动数据):{
	 *         titleList:[],
	 *         dataList:[]
	 *     }
	 * }
	 */
	private Map<String, Map<String, Object>> getSupplierChangeField(SupplierChange supplierChange, Supplier newSupplier, Supplier oldSupplier){
		Map<String, Map<String, Object>> result = new HashMap<>();
		boolean changeFlag = false;//默认没有改变
		result.put("recover", new HashMap<>());
		result.put("change", new HashMap<>());
		//如果有历史记录，则为新增
		if(oldSupplier == null){
			result.get("change").put("opDesc", "新增供应商");
			result.get("change").put("op", "add");
		}else {
			result.get("change").put("opDesc", "修改供应商");
			result.get("change").put("op", "update");
		}
		List<Map<String, String>> fieldList = new ArrayList<>();//改变的字段列表
		List<Map<String, String>> baseChangeList = ObjectFieldCompare.compare(oldSupplier, newSupplier);
		if(CollectionUtils.isNotEmpty(baseChangeList)){
			changeFlag = true;//发生改变
			for(Map<String, String> baseChange : baseChangeList){
				if("supplierNature".equals(baseChange.get("cell"))){
					if("1".equals(baseChange.get("oldCellValue"))){
						baseChange.put("oldCellText", "个体供应商");
					}else if("0".equals(baseChange.get("oldCellValue"))){
						baseChange.put("oldCellText", "企业供应商");
					}else {
						baseChange.put("oldCellText", "");
					}
					if("1".equals(baseChange.get("newCellValue"))){
						baseChange.put("newCellText", "个体供应商");
					}else if("0".equals(baseChange.get("newCellValue"))){
						baseChange.put("newCellText", "企业供应商");
					}else {
						baseChange.put("newCellText", "");
					}
				}
				if("standarCompanyFlag".equals(baseChange.get("cell"))){
					if("1".equals(baseChange.get("oldCellValue"))){
						baseChange.put("oldCellText", "标准");
					}else if("0".equals(baseChange.get("oldCellValue"))){
						baseChange.put("oldCellText", "非标准");
					}else {
						baseChange.put("oldCellText", "");
					}
					if("1".equals(baseChange.get("newCellValue"))){
						baseChange.put("newCellText", "标准");
					}else if("0".equals(baseChange.get("newCellValue"))){
						baseChange.put("newCellText", "非标准");
					}else {
						baseChange.put("newCellText", "");
					}
				}
				if("standarPhoneFlag".equals(baseChange.get("cell"))){
					if("1".equals(baseChange.get("oldCellValue"))){
						baseChange.put("oldCellText", "规范");
					}else if("0".equals(baseChange.get("oldCellValue"))){
						baseChange.put("oldCellText", "不规范");
					}else {
						baseChange.put("oldCellText", "");
					}
					if("1".equals(baseChange.get("newCellValue"))){
						baseChange.put("newCellText", "规范");
					}else if("0".equals(baseChange.get("newCellValue"))){
						baseChange.put("newCellText", "不规范");
					}else {
						baseChange.put("newCellText", "");
					}
				}
				if("state".equals(baseChange.get("cell"))){
					if("1".equals(baseChange.get("oldCellValue"))){
						baseChange.put("oldCellText", "停用");
					}else if("0".equals(baseChange.get("oldCellValue"))){
						baseChange.put("oldCellText", "启用");
					}else {
						baseChange.put("oldCellText", "");
					}
					if("1".equals(baseChange.get("newCellValue"))){
						baseChange.put("newCellText", "停用");
					}else if("0".equals(baseChange.get("newCellValue"))){
						baseChange.put("newCellText", "启用");
					}else {
						baseChange.put("newCellText", "");
					}
				}
				if("phone".equals(baseChange.get("cell"))){
					if(!StringUtils.isEmpty(baseChange.get("oldCellValue"))){
						String phone = EncryptUtils.decrypt(baseChange.get("oldCellValue"));
						baseChange.put("oldCellValue", phone);//如果是则责任人自己，则可以看到电话号码
						baseChange.put("oldCellText", getPhone(phone));//非责任人
					}
					if(!StringUtils.isEmpty(baseChange.get("newCellValue"))){
						String phone = EncryptUtils.decrypt(baseChange.get("newCellValue"));
						baseChange.put("newCellValue", phone);//如果是则责任人自己，则可以看到电话号码
						baseChange.put("newCellText", getPhone(phone));//非责任人
					}
				}
				if("qqwechat".equals(baseChange.get("cell"))){
					if(!StringUtils.isEmpty(baseChange.get("oldCellValue"))){
						String qqwechat = EncryptUtils.decrypt(baseChange.get("oldCellValue"));
						baseChange.put("oldCellValue", qqwechat);//如果是则责任人自己，则可以看到微信
						baseChange.put("oldCellText", getPhone(qqwechat));//非责任人
					}
					if(!StringUtils.isEmpty(baseChange.get("newCellValue"))){
						String qqwechat = EncryptUtils.decrypt(baseChange.get("newCellValue"));
						baseChange.put("newCellValue", qqwechat);//如果是则责任人自己，则可以看到微信
						baseChange.put("newCellText", getPhone(qqwechat));//非责任人
					}
				}
				if("qq".equals(baseChange.get("cell"))){
					if(!StringUtils.isEmpty(baseChange.get("oldCellValue"))){
						String qq = EncryptUtils.decrypt(baseChange.get("oldCellValue"));
						baseChange.put("oldCellValue", qq);//如果是则责任人自己，则可以看到QQ
						baseChange.put("oldCellText", getPhone(qq));//非责任人
					}
					if(!StringUtils.isEmpty(baseChange.get("newCellValue"))){
						String qq = EncryptUtils.decrypt(baseChange.get("newCellValue"));
						baseChange.put("newCellValue", qq);//如果是则责任人自己，则可以看到QQ
						baseChange.put("newCellText", getPhone(qq));//非责任人
					}
				}
				if(result.get("recover").get("baseData") == null){
					result.get("recover").put("baseData", new HashMap<>());
				}
				((Map<String, String>)result.get("recover").get("baseData")).put(baseChange.get("cell"), baseChange.get("oldCellValue"));//记录恢复更新的数据
				fieldList.add(baseChange);//添加改变的字段
			}
		}
		if(CollectionUtils.isNotEmpty(fieldList)){
			changeFlag = true;//发生改变
			result.get("change").put("fieldList", fieldList);

			//构建导出数据
			List<String> rowFieldList = Arrays.asList("creator","supplierNature","name","standarCompanyFlag","contactor","standarPhoneFlag","phone","qqwechat","qq","contactorDesc","state");//导出标题字段
			List<String> rowTitleList = Arrays.asList("责任人","供应商性质","供应商公司名称","是否标准公司","供应商联系人","是否规范联系人","手机号","微信号","QQ号","备注","状态");//导出文件标题
			buildChangeExportData(rowFieldList,rowTitleList, result, fieldList, true);
		}
		//如果发生改变，则记录，否则不记录到数据库
		if(changeFlag){
			return result;
		}else {
			return null;
		}
	}

	/**
	 * 供应商账户异动处理
	 * @param newAccountList 修改后数据
	 * @param oldAccountList 修改前数据
	 * @return 异动列表
	 */
	private List<AccountChange> accountChangeHandler(List<Account> newAccountList, List<Account> oldAccountList){
		//如果新供应商账户数据不存在，则直接返回
		if(CollectionUtils.isEmpty(newAccountList)){
			return null;
		}
		Map<Integer, Account> newAccountMap = new HashMap<>();
		Map<Integer, Account> oldAccountMap = new HashMap<>();
		newAccountList.forEach(account -> {
			newAccountMap.put(account.getId(), account);
		});
		if(CollectionUtils.isNotEmpty(oldAccountList)){
			oldAccountList.forEach(account -> {
				oldAccountMap.put(account.getId(), account);
			});
		}
		//返回的异动记录
		List<AccountChange> accountChangeList = new ArrayList<>();
		for(Integer accountId : newAccountMap.keySet()){
			Account newAccount = newAccountMap.get(accountId);
			Account oldAccount = oldAccountMap.get(accountId);
			AccountChange accountChange = new AccountChange();
			accountChange.setAccountId(accountId);
			accountChange.setAccountName(newAccount.getName());
			accountChange.setAccountOwner(newAccount.getOwner());
			accountChange.setUserId(AppUtil.getUser().getId());
			accountChange.setUserName(AppUtil.getUser().getName());
			Map<String, Map<String, Object>> result = getAccountChangeField(accountChange, newAccount, oldAccount);
			if(result != null){
				accountChange.setChangeContent(JSON.toJSONString(result));
				accountChangeList.add(accountChange);
			}
		}
		return accountChangeList;
	}

	/**
	 * 获取媒体改变数据，数据格式如下:recover代表要恢复的字段数据，change代表异动详情展示的字段
	 * {
	 *     recover(恢复字段):{
	 *         baseData:{}
	 *     },
	 *     change(异动数据):{
	 *         fieldList(异动字段):[
	 *             {cell:'', cellName:'', oldCellValue:'', oldCellText:'', newCellValue:'', newCellText:''}
	 * 	    ],
	 *         opDesc:'',
	 *         op:''
	 *     },
	 *     export(导出异动数据):{
	 *         titleList:[],
	 *         dataList:[]
	 *     }
	 * }
	 */
	private Map<String, Map<String, Object>> getAccountChangeField(AccountChange accountChange, Account newAccount, Account oldAccount){
		Map<String, Map<String, Object>> result = new HashMap<>();
		boolean changeFlag = false;//默认没有改变
		result.put("recover", new HashMap<>());
		result.put("change", new HashMap<>());
		//如果有历史记录，则为新增
		if(oldAccount == null){
			result.get("change").put("opDesc", "新增供应商账户");
			result.get("change").put("op", "add");
		}else {
			result.get("change").put("opDesc", "修改供应商账户");
			result.get("change").put("op", "update");
		}
		List<Map<String, String>> fieldList = new ArrayList<>();//改变的字段列表
		List<Map<String, String>> baseChangeList = ObjectFieldCompare.compare(oldAccount, newAccount);
		if(CollectionUtils.isNotEmpty(baseChangeList)){
			changeFlag = true;//发生改变
			for(Map<String, String> baseChange : baseChangeList){
				if(result.get("recover").get("baseData") == null){
					result.get("recover").put("baseData", new HashMap<>());
				}
				((Map<String, String>)result.get("recover").get("baseData")).put(baseChange.get("cell"), baseChange.get("oldCellValue"));//记录恢复更新的数据
				fieldList.add(baseChange);//添加改变的字段
			}
		}
		if(CollectionUtils.isNotEmpty(fieldList)){
			changeFlag = true;//发生改变
			result.get("change").put("fieldList", fieldList);

			//构建导出数据
			List<String> rowFieldList = Arrays.asList("companyName","contactor","name","bankNo","bankName","owner","phone");//导出标题字段
			List<String> rowTitleList = Arrays.asList("供应商公司名称","供应商联系人","账户名称","账号","开户行","户主","预留电话");//导出文件标题
			buildChangeExportData(rowFieldList,rowTitleList, result, fieldList, false);
		}
		//如果发生改变，则记录，否则不记录到数据库
		if(changeFlag){
			return result;
		}else {
			return null;
		}
	}

	/**
	 * 构建异动导出数据
	 * @param rowFieldList 导出标题字段
	 * @param rowTitleList 导出文件标题
	 * @param result 异动JSON数据
	 * @param fieldList 异动字段列表
	 */
	private void buildChangeExportData(List<String> rowFieldList, List<String> rowTitleList, Map<String, Map<String, Object>> result, List<Map<String, String>> fieldList, boolean supplierFlag){
		List<String> rowContentList = new ArrayList<>();//导出文件内容
		List<String> rowContentListTmp = new ArrayList<>();//导出文件内容，加密
		Map<String, Map<String, String>> fieldMap = new HashMap<>();
		for(Map<String, String> map : fieldList){
			fieldMap.put(map.get("cell"), map);
		}
		for(String cellCode : rowFieldList){
			//如果包含，说明该字段有异动，否则没有
			if(fieldMap.containsKey(cellCode)){
				//如果是供应商异动，还需要缓存一套加密的，给非责任人使用
				if(supplierFlag){
					if("qqwechat".equals(cellCode) || "phone".equals(cellCode) || "qq".equals(cellCode)){
						String oldCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellValue")) ? "空" : fieldMap.get(cellCode).get("oldCellValue");
						String newCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellValue")) ? "空" : fieldMap.get(cellCode).get("newCellValue");
						rowContentList.add(String.format("%s->%s", oldCellValue, newCellValue));
						oldCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellText")) ? "空" : fieldMap.get(cellCode).get("oldCellText");
						newCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellText")) ? "空" : fieldMap.get(cellCode).get("newCellText");
						rowContentListTmp.add(String.format("%s->%s", oldCellValue, newCellValue));
					}else {
						String oldCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellText")) ? (StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellValue")) ? "空" : fieldMap.get(cellCode).get("oldCellValue")) : fieldMap.get(cellCode).get("oldCellText");
						String newCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellText")) ? (StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellValue")) ? "空" : fieldMap.get(cellCode).get("newCellValue")) : fieldMap.get(cellCode).get("newCellText");
						rowContentList.add(String.format("%s->%s", oldCellValue, newCellValue));
						rowContentListTmp.add(String.format("%s->%s", oldCellValue, newCellValue));
					}
				}else {
					String oldCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellText")) ? (StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellValue")) ? "空" : fieldMap.get(cellCode).get("oldCellValue")) : fieldMap.get(cellCode).get("oldCellText");
					String newCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellText")) ? (StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellValue")) ? "空" : fieldMap.get(cellCode).get("newCellValue")) : fieldMap.get(cellCode).get("newCellText");
					rowContentList.add(String.format("%s->%s", oldCellValue, newCellValue));
				}
			}else {
				rowContentList.add("");
				if(supplierFlag){
					rowContentListTmp.add("");
				}
			}
		}
		Map<String, Object> export = new HashMap<>();
		export.put("titleList", rowTitleList);
		export.put("dataList", rowContentList);
		if(CollectionUtils.isNotEmpty(rowContentListTmp)){
			export.put("dataListTmp", rowContentListTmp);
		}
		result.put("export", export);
	}

	//电话号码处理
	private String getPhone(String phone){
		phone = StringUtils.isEmpty(phone) ? "" : phone;
		if(phone.length() >= 11){
			String start = phone.length() > 11 ? "*****" : "****";
			phone = phone.substring(0, 3) + start + phone.substring(phone.length() - 4, phone.length());
		}else if(phone.length() >= 3){
			phone = phone.substring(0, 1) + "***" + phone.substring(phone.length() - 1);
		}else {
			phone = "**";
		}
		return phone;
	}
}