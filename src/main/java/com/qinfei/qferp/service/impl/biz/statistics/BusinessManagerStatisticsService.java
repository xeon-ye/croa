package com.qinfei.qferp.service.impl.biz.statistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.mapper.biz.statistics.BusinessManagerStatisticsMapper;
import com.qinfei.qferp.service.biz.statistics.IBusinessManagerStatisticsService;
import com.github.pagehelper.PageHelper;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class BusinessManagerStatisticsService extends BaseService implements IBusinessManagerStatisticsService {

	public static final String cacheKey = "businessManagerStatistics";

	@Autowired
	private BusinessManagerStatisticsMapper businessManagerStatisticsMapper;
	@Autowired
	private DeptMapper deptMapper;
	@Autowired
	private UserMapper userMapper;

	// @Cacheable(value = cacheKey)
	public List<Map> topOptionSetValue(Map map) {
		this.addSecurityByFore(map);
		return businessManagerStatisticsMapper.topOptionSetValue(map);
	}

	// 订单排名
	public List<Map> orderSort(Map map, Integer pageNum, Integer pageSize) {
		this.addSecurity(map);
		PageHelper.startPage(pageNum, pageSize);
		return businessManagerStatisticsMapper.orderSort(map);
	}

	@Override
	public PageInfo<Map> profitSort(Map map, Integer pageNum, Integer pageSize) {
		this.addSecurityByFore(map);
		PageHelper.startPage(pageNum, pageSize);
		//当期利润排序
		List<Map> currentList = businessManagerStatisticsMapper.profitSort(map);
		String businessUserIds = "";
		if(CollectionUtils.isNotEmpty(currentList)){
			for(Map currentProfitMap : currentList){
				currentProfitMap.put("trend",0.0);//趋势： 小于0:下降、等于0：不变、大于0：上升，默认上升
				if(currentProfitMap.get("businessUserId") != null && !StringUtils.isEmpty(currentProfitMap.get("businessUserId"))){
					businessUserIds += "," + currentProfitMap.get("businessUserId");
				}
			}
			//上一期期利润排序
			if(!StringUtils.isEmpty(businessUserIds)){
				businessUserIds = businessUserIds.substring(1);
				map.put("prevFlag",true);
				map.put("businessUserIds",businessUserIds);
				List<Map> prevList = businessManagerStatisticsMapper.profitSort(map);
				if(CollectionUtils.isNotEmpty(prevList)){
					for(Map currentProfitMap : currentList){
						for(Map prevProfitMap : prevList){
							if(currentProfitMap.get("businessUserId") != null && currentProfitMap.get("businessUserId").equals(prevProfitMap.get("businessUserId"))){
								BigDecimal currentProfit = new BigDecimal(String.valueOf(currentProfitMap.get("profit")));
								BigDecimal prevProfit = new BigDecimal(String.valueOf(prevProfitMap.get("profit")));
								//趋势计算方式：当期利润和上期利润都大于 0 时，才计算，否则默认为0
								if(currentProfit.compareTo(new BigDecimal(0)) > 0 && prevProfit.compareTo(new BigDecimal(0)) > 0){
									currentProfitMap.put("trend",currentProfit.subtract(prevProfit).divide(prevProfit,4,BigDecimal.ROUND_HALF_DOWN));
								}
								break;
							}
						}
					}
				}
			}
		}
		return new PageInfo<>(currentList);
	}

	@Override
	public PageInfo<Map> businessNoIncomeSort(Map map, Integer pageNum, Integer pageSize) {
		this.addSecurityByFore(map);
		PageHelper.startPage(pageNum, pageSize);
		//当期利润排序
		List<Map> currentList = businessManagerStatisticsMapper.businessNoIncomeSort(map);
		String businessUserIds = "";
		if(CollectionUtils.isNotEmpty(currentList)){
			for(Map currentNoIncomeMap : currentList){
				currentNoIncomeMap.put("trend",0.0);//趋势： 小于0:下降、等于0：不变、大于0：上升，默认上升
				if(currentNoIncomeMap.get("businessUserId") != null && !StringUtils.isEmpty(currentNoIncomeMap.get("businessUserId"))){
					businessUserIds += "," + currentNoIncomeMap.get("businessUserId");
				}
			}
			//上一期期利润排序
			if(!StringUtils.isEmpty(businessUserIds)){
				businessUserIds = businessUserIds.substring(1);
				map.put("prevFlag",true);
				map.put("businessUserIds",businessUserIds);
				List<Map> prevList = businessManagerStatisticsMapper.businessNoIncomeSort(map);
				if(CollectionUtils.isNotEmpty(prevList)){
					for(Map currentNoIncomeMap : currentList){
						for(Map prevNoIncomeMap : prevList){
							if(currentNoIncomeMap.get("businessUserId") != null && currentNoIncomeMap.get("businessUserId").equals(prevNoIncomeMap.get("businessUserId"))){
								BigDecimal currentNoIncome = new BigDecimal(String.valueOf(currentNoIncomeMap.get("noIncomeAmount")));
								BigDecimal prevNoIncome = new BigDecimal(String.valueOf(prevNoIncomeMap.get("noIncomeAmount")));
								//趋势计算方式：当期利润和上期利润都大于 0 时，才计算，否则默认为0
								if(currentNoIncome.compareTo(new BigDecimal(0)) > 0 && prevNoIncome.compareTo(new BigDecimal(0)) > 0){
									currentNoIncomeMap.put("trend",currentNoIncome.subtract(prevNoIncome).divide(prevNoIncome,4,BigDecimal.ROUND_HALF_DOWN));
								}
								break;
							}
						}
					}
				}
			}
		}
		return new PageInfo<>(currentList);
	}

	@Override
	public PageInfo<Map> custSaleAmountSort(Map map, Integer pageNum, Integer pageSize) {
		this.addSecurityByFore(map);
		PageHelper.startPage(pageNum, pageSize);
		//当期利润排序
		List<Map> currentList = businessManagerStatisticsMapper.custSaleAmountSort(map);
		String custUserIds = "";
		if(CollectionUtils.isNotEmpty(currentList)){
			for(Map currentProfitMap : currentList){
				currentProfitMap.put("trend",0.0);//趋势： 小于0:下降、等于0：不变、大于0：上升，默认上升
				if(currentProfitMap.get("custId") != null && !StringUtils.isEmpty(currentProfitMap.get("custId"))){
					custUserIds += "," + currentProfitMap.get("custId");
				}
			}
			//上一期应收金额
			if(!StringUtils.isEmpty(custUserIds)){
				custUserIds = custUserIds.substring(1);
				map.put("prevFlag",true);
				map.put("custUserIds",custUserIds);
				List<Map> prevList = businessManagerStatisticsMapper.custSaleAmountSort(map);
				if(CollectionUtils.isNotEmpty(prevList)){
					for(Map currentSaleAmountMap : currentList){
						for(Map prevSaleAmountMap : prevList){
							if(currentSaleAmountMap.get("custId") != null && currentSaleAmountMap.get("custId").equals(prevSaleAmountMap.get("custId"))){
								BigDecimal currentSaleAmount = new BigDecimal(String.valueOf(currentSaleAmountMap.get("saleAmount")));
								BigDecimal prevSaleAmount = new BigDecimal(String.valueOf(prevSaleAmountMap.get("saleAmount")));
								//趋势计算方式：当期应收金额和上期应收金额都大于 0 时，才计算，否则默认为0
								if(currentSaleAmount.compareTo(new BigDecimal(0)) > 0 && prevSaleAmount.compareTo(new BigDecimal(0)) > 0){
									currentSaleAmountMap.put("trend",currentSaleAmount.subtract(prevSaleAmount).divide(prevSaleAmount,4,BigDecimal.ROUND_HALF_DOWN));
								}
								break;
							}
						}
					}
				}
			}
		}
		return new PageInfo<>(currentList);
	}

	@Override
	public PageInfo<Map> custNoIncomeSort(Map map, Integer pageNum, Integer pageSize) {
		this.addSecurityByFore(map);
		PageHelper.startPage(pageNum, pageSize);
		//当期利润排序
		List<Map> currentList = businessManagerStatisticsMapper.custNoIncomeSort(map);
		String custUserIds = "";
		if(CollectionUtils.isNotEmpty(currentList)){
			for(Map currentNoIncomeMap : currentList){
				currentNoIncomeMap.put("trend",0.0);//趋势： 小于0:下降、等于0：不变、大于0：上升，默认上升
				if(currentNoIncomeMap.get("custId") != null && !StringUtils.isEmpty(currentNoIncomeMap.get("custId"))){
					custUserIds += "," + currentNoIncomeMap.get("custId");
				}
			}
			//上一期应收金额
			if(!StringUtils.isEmpty(custUserIds)){
				custUserIds = custUserIds.substring(1);
				map.put("prevFlag",true);
				map.put("custUserIds",custUserIds);
				List<Map> prevList = businessManagerStatisticsMapper.custNoIncomeSort(map);
				if(CollectionUtils.isNotEmpty(prevList)){
					for(Map currentNoIncomeMap : currentList){
						for(Map prevNoIncomeMap : prevList){
							if(currentNoIncomeMap.get("custId") != null && currentNoIncomeMap.get("custId").equals(prevNoIncomeMap.get("custId"))){
								BigDecimal currentNoIncome = new BigDecimal(String.valueOf(currentNoIncomeMap.get("noIncomeAmount")));
								BigDecimal prevNoIncome = new BigDecimal(String.valueOf(prevNoIncomeMap.get("noIncomeAmount")));
								//趋势计算方式：当期应收金额和上期应收金额都大于 0 时，才计算，否则默认为0
								if(currentNoIncome.compareTo(new BigDecimal(0)) > 0 && prevNoIncome.compareTo(new BigDecimal(0)) > 0){
									currentNoIncomeMap.put("trend",currentNoIncome.subtract(prevNoIncome).divide(prevNoIncome,4,BigDecimal.ROUND_HALF_DOWN));
								}
								break;
							}
						}
					}
				}
			}
		}
		return new PageInfo<>(currentList);
	}

	// 稿件排名
	public List<Map> articleSort(Map map, Integer pageNum, Integer pageSize) {
		this.addSecurity(map);
		PageHelper.startPage(pageNum, pageSize);
		return businessManagerStatisticsMapper.articleSort(map);
	}

	/**
	 * 统计各个部门的业务
	 * 
	 * @param list
	 * @return
	 */
	// @Cacheable(value = cacheKey)
	public List<Map> everyDeptBusiness(List<Integer> list, String dateSelect) {
		long startTime1 = System.currentTimeMillis();
		List<Dept> depts = deptMapper.listById(list);
		long startTime2 = System.currentTimeMillis();
		log.info("耗时："+(startTime2-startTime1)/1000);
		List<Map> result = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(depts)){
			for(Dept dept : depts){
				String deptIds = userMapper.getChilds(dept.getId());
				if (deptIds.indexOf("$,") > -1) {
					deptIds = deptIds.substring(2);
				}
				Map param = new HashMap();
				param.put("deptIds",deptIds);
				param.put("dateSelect",dateSelect);
				Map map = businessManagerStatisticsMapper.getDeptStatisticsByDeptId(param);
				map.put("deptId",dept.getId());
				map.put("deptName",dept.getName());
				result.add(map);
			}
		}
		long startTime3 = System.currentTimeMillis();
		log.info("耗时："+(startTime3-startTime2)/1000);
		return result;
	}

	/**
	 * 统计本部门业务排名
	 * 
	 * @param map
	 * @return
	 */
	// @Cacheable(value = cacheKey)
	public List<Map> businessTop(Map map) {
		User user = AppUtil.getUser();
		Integer deptId = MapUtils.getInteger(map, "deptId");//页面选择的部门
		deptId = deptId == null ? user.getDeptId() : deptId;
		String deptIds = "";
		long startTime1 = System.currentTimeMillis();
		if (deptId != null) {
			deptIds = userMapper.getChilds(deptId);
			if (deptIds.indexOf("$,") > -1) {
				deptIds = deptIds.substring(2);
			}
			map.put("deptIds", deptIds);
		}
		long startTime2 = System.currentTimeMillis();
		log.info("耗时："+(startTime2-startTime1)/1000);
		List<Map> list = businessManagerStatisticsMapper.businessTop(map);
		long startTime3 = System.currentTimeMillis();
		log.info("耗时："+(startTime3-startTime2)/1000);
		return list;
	}
}
