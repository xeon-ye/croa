package com.qinfei.qferp.service.impl.fee;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.fee.AccountChange;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.fee.AccountChangeMapper;
import com.qinfei.qferp.mapper.fee.AccountMapper;
import com.qinfei.qferp.mapper.media.SupplierMapper;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.service.crm.IStatisticsService;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.ObjectFieldCompare;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Slf4j
public class AccountService implements IAccountService {
	@Autowired
	private AccountMapper accountMapper;
	@Autowired
	private SupplierMapper supplierMapper;
	@Autowired
	private AccountChangeMapper accountChangeMapper;
	@Autowired
	private DeptMapper deptMapper;
	@Autowired
	IStatisticsService statisticsService;
	@Autowired
	IUserService userService;
	@Autowired
	IDictService dictService;
	@Value("${media.onlineTime}")
	private String onlineTime;

	@Override
	public PageInfo<Map> listPg(int pageNum, int pageSize, Map map) {
		setDeptAuth(map);
		PageHelper.startPage(pageNum, pageSize);
		List<Map> list = accountMapper.listPg(map);
		for (Map temp : list) {
			if (temp.get("id") != null) {
				StringBuffer sb = new StringBuffer();
				Integer id = (Integer) temp.get("id");
				List<Dept> deptList = this.queryDeptByAccountId(id);
				if (deptList.size() > 0) {
					for (Dept dept : deptList) {
						sb.append(dept.getName() + "，");
					}
				}
				if (sb.length() > 0 && '，' == sb.charAt(sb.length() - 1)) {
					sb.delete(sb.length() - 1, sb.length());
				}
				temp.put("deptNames", sb.toString());
			}
		}
		return new PageInfo<>(list);
	}

	/**
	 * 设置查询的权限范围；
	 *
	 * @param params：查询的参数集合；
	 */
	private void setDeptAuth(Map params) {
		User user = AppUtil.getUser();
		// 获取用户的所有角色信息；
		List<Role> roles = user.getRoles();
		if (roles != null && !roles.isEmpty()) {
			List<String> codes = new ArrayList<>();
			for (Role role : roles) {
				codes.add(role.getType());
			}
			// 主管可以获取下属所有下属的订单；
			if (user.getCurrentDeptQx() && codes.contains(IConst.ROLE_TYPE_MJ)) {
				// 如果是查询部门下的所有媒介人员；
//					params.put("creators", getDeptUserIds(IConst.ROLE_TYPE_MJ));
					params.put("creators", userService.listUserIdsByRoleType(IConst.ROLE_TYPE_MJ));

//				else{
//					params.put("creators", userService.listByDeptId(user.getDeptId()));
//				}
				// 如果是查询部门下的所有业务人员；
//				if (codes.contains(IConst.ROLE_TYPE_YW)) {
////					params.put("creators", getDeptUserIds(IConst.ROLE_TYPE_YW));
//					params.put("creators", userService.listByDeptId(user.getDeptId()));
//				}
			} else {
				// 普通员工只能查询自己的信息；
				params.put("creator", user.getId());
				List<User> list = new ArrayList<>();
				list.add(user) ;
				params.put("creators", list);
			}
		}
	}

	@Override
	public Account getById(Integer id) {
		return accountMapper.getById(id);
	}

	@Override
	public Integer supplierType(Integer id){
		return accountMapper.supplierType(id);
	}

	@Override
	public Account add(Account entity) {
		Integer countFlag = accountMapper.selectAccount(entity.getBankNo(),entity.getCompanyId());
			if (countFlag==0) {
				accountMapper.insert(entity);
				//处理账户异动，增加异常捕获，使其不影响正常操作
				try {
					List<AccountChange> accountChangeList = accountChangeHandler(Arrays.asList(entity), null);
					if (CollectionUtils.isNotEmpty(accountChangeList)) {
						accountChangeMapper.saveBatch(accountChangeList);
					}
				} catch (Exception e) {
					log.error("【账户新增】账户异动记录异常: {}", e.getMessage());
				}

			}else {
				throw new QinFeiException(1003, "该供应商下中的该账号已存在，请重新输入！" );
			}

		return entity;

	}
	//*请款中 增加供应商账户
	// */
	@Override
	public Account outgoAccountAdd(Account entity) {
		Integer countFlag = accountMapper.selectAccount(entity.getBankNo(),entity.getCompanyId());
		if (countFlag==0) {
			accountMapper.insert(entity);
			//处理账户异动，增加异常捕获，使其不影响正常操作
			try {
				List<AccountChange> accountChangeList = accountChangeHandler(Arrays.asList(entity), null);
				if (CollectionUtils.isNotEmpty(accountChangeList)) {
					accountChangeMapper.saveBatch(accountChangeList);
				}
			} catch (Exception e) {
				log.error("【账户新增】账户异动记录异常: {}", e.getMessage());
			}

		}
		return entity;

	}

	@Override
	@Transactional
	public void delById(Account entity) {
		// 先删除关系表
		accountMapper.deleteAccountDeptByAccountId(entity.getId());

		// 删除账户表
		entity.setState(IConst.STATE_DELETE);
		entity.setUpdateUserId(AppUtil.getUser().getId());
		entity.setUpdateTime(new Date());
		accountMapper.update(entity);
	}

	@Override
	public Account edit(Account entity) {
		//异动前数据
		Account oldAccount = accountMapper.getById(entity.getId());

		accountMapper.update(entity);

		//处理账户异动，增加异常捕获，使其不影响正常操作
		try{
			Account newAccount = accountMapper.getById(entity.getId());
			List<AccountChange> accountChangeList = accountChangeHandler(Arrays.asList(newAccount), Arrays.asList(oldAccount));
			if(CollectionUtils.isNotEmpty(accountChangeList)){
				accountChangeMapper.saveBatch(accountChangeList);
			}
		}catch (Exception e){
			log.error("【账户编辑】账户异动记录异常: {}", e.getMessage());
		}
		return entity;
	}

	@Override
	@Transactional
	public void addPersonalAccount(Account entity) {
		entity.setId(null);
		User user = AppUtil.getUser();
		entity.setCompanyId(0);
		List<Dict> list = dictService.listByTypeCode("COMPANY_CODE");
		for(Dict dict:list){
			if(dict.getCode().equals(user.getCompanyCode())){
				entity.setCompanyName(dict.getName());
				break;
			}
		}
		entity.setOwner(user.getName());
		entity.setContactor(user.getName());
		entity.setCompanyCode(user.getCompanyCode());
		entity.setType(IConst.ACCOUNT_TYPE_PERSONAL);
		entity.setCreator(user.getId());
		entity.setCreateTime(new Date());
		entity.setState(IConst.STATE_FINISH);
		accountMapper.insert(entity);
		return ;
	}

	@Override
	public PageInfo<Map> listPgForSelectAccount(int pageNum, int pageSize, Integer companyId, Integer type, Map map) {
		PageHelper.startPage(pageNum, pageSize);
		map.put("companyId", companyId);
		map.put("type", type);
		List<Map> list = accountMapper.listPgForSelectAccount(map);
		return new PageInfo<>(list);
	}

	@Override
	public PageInfo<Map> listPgForSelectAccountNotCompanyCode(int pageNum, int pageSize, Integer companyId, Integer type, Map map) {
		PageHelper.startPage(pageNum, pageSize);
		map.put("companyId", companyId);
		map.put("type", type);
		List<Map> list = accountMapper.listPgForSelectAccountNotCompanyCode(map);
		return new PageInfo<>(list);
	}

	@Override
	@Transactional
	public List<Dept> insertAccountDept(Integer accountId, Integer deptId) {
		List<Dept> existList = accountMapper.queryDeptByAccountId(accountId) ;//该账号已关联的部门
		List<Dept> list = deptMapper.listByParentId(deptId);//该账号需要关联的部门
		List<Map> data = new ArrayList<>() ;//去重后需要关联的数据
		List<Dept> resultList = new ArrayList<>();//返回去重的部门列表
		for(Dept dept:list){
			Boolean flag = true;
			for(Dept temp:existList){
				if(dept!=null && temp!=null && dept.getId().equals(temp.getId())){
					flag = false;
				}
			}
			//有重复时，flag=false,不要了，不重复的，放入data，插入数据库
			if(flag){
				Map<String, Object> map = new HashMap();
				map.put("accountId", accountId);
				map.put("deptId", dept.getId());
				data.add(map);
				resultList.add(dept);
			}
		}
		//批量插入
		if(data!=null && data.size()>0){
			accountMapper.insertAccountDeptBatch(data);
		}
		return resultList;
	}

	@Override
	@Transactional
	public List<Dept> deleteAccountDept(Integer accountId, Integer deptId) {
		List<Dept> list = deptMapper.listByParentId(deptId);
		Map<String, Object> map = new HashMap<>();
		map.put("accountId", accountId);
		map.put("list", list);
		if (list != null && list.size() > 0) {
			accountMapper.deleteAccountDept(map);
		}
		return list;
	}

	@Override
	public List<Dept> queryDeptByAccountId(Integer id) {
		return accountMapper.queryDeptByAccountId(id);
	}

	@Override
	public List<Account> queryCompanyAccountList(String companyCode) {
		return accountMapper.queryCompanyAccountList(companyCode);
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
			buildChangeExportData(rowFieldList,rowTitleList, result, fieldList);
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
	private void buildChangeExportData(List<String> rowFieldList, List<String> rowTitleList, Map<String, Map<String, Object>> result, List<Map<String, String>> fieldList){
		List<String> rowContentList = new ArrayList<>();//导出文件内容
		Map<String, Map<String, String>> fieldMap = new HashMap<>();
		for(Map<String, String> map : fieldList){
			fieldMap.put(map.get("cell"), map);
		}
		for(String cellCode : rowFieldList){
			//如果包含，说明该字段有异动，否则没有
			if(fieldMap.containsKey(cellCode)){
				String oldCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellText")) ? (StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellValue")) ? "空" : fieldMap.get(cellCode).get("oldCellValue")) : fieldMap.get(cellCode).get("oldCellText");
				String newCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellText")) ? (StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellValue")) ? "空" : fieldMap.get(cellCode).get("newCellValue")) : fieldMap.get(cellCode).get("newCellText");
				rowContentList.add(String.format("%s->%s", oldCellValue, newCellValue));
			}else {
				rowContentList.add("");
			}
		}
		Map<String, Object> export = new HashMap<>();
		export.put("titleList", rowTitleList);
		export.put("dataList", rowContentList);
		result.put("export", export);
	}
}
