package com.qinfei.qferp.service.impl.crm;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.utils.PageOrder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.mapper.crm.CustManagerStatisticsMapper;
import com.qinfei.qferp.service.crm.ICustManagerStatisticsService;

@Service
public class CustManagerStatisticsService extends BaseService implements ICustManagerStatisticsService {

	public static final String cacheKey = "custManagerStatistics";

	@Autowired
	CustManagerStatisticsMapper custManagerStatisticsMapper;

	// @Cacheable(value = cacheKey)
	@Override
	public Map topStatistics(Map map) {
		this.addSecurityByFore(map);
		Map<String, Object> result = custManagerStatisticsMapper.getCustStatisticsByParam(map);
		int djkhs = custManagerStatisticsMapper.getCustNumByParam(map);
		if(result != null){
			result.put("djkhs", djkhs);
		}
		return result;
	}

	/**
	 * 统计各部门的男女人数
	 * 
	 * @param list
	 * @return
	 */
	// @Cacheable(value = cacheKey)
	@Override
	public List<Map> everyDeptUserCount(List<String> list) {
		return custManagerStatisticsMapper.everyDeptUserCount(list);
	}

	// 统计各种类型的客户
	// @Cacheable(value = cacheKey)
	@Override
	public Map custPie(Map map) {
		this.addSecurityByFore(map);
		return custManagerStatisticsMapper.custPie(map);
	}

	@Override
	public PageInfo<Map<String,Object>> listCustByParam(Integer pageNum, Integer pageSize, Map map) {
		PageHelper.startPage(pageNum, pageSize, PageOrder.getOrderStr(map));
		this.addSecurityByFore(map);
		List<Map<String, Object>> list = custManagerStatisticsMapper.listCustByParam(map);
		return new PageInfo<>(list);
	}
}
