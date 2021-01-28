package com.qinfei.qferp.service.impl.mediauser.statistics;

import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.mediauser.RankingUserStatisticsMapper;
import com.qinfei.qferp.service.mediauser.statistics.IRankingUserStatisticsService;
import com.qinfei.qferp.utils.AppUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RankingUserStatisticsService implements IRankingUserStatisticsService {
	@Autowired
	RankingUserStatisticsMapper rankingUserStatisticsMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 获取个人当前公司业绩排名
	 * @return
	 */
	@Override
	public String getSelfRanking(Map map) {
		String ranking = "0";
		Map<String, Object> result = getSalesmanRanking(map);
		if(result != null && result.get("selfRanking") != null){
			ranking = String.valueOf(result.get("selfRanking"));
		}
		return ranking;
	}

	/**
	 * 获取本公司业务员排名前30名/当前人排名
	 * @param map
	 * @return
	 */
	@Override
	public Map<String, Object> getSalesmanRanking(Map map) {
		User user = AppUtil.getUser();
		Map<String, Object> result = new HashMap<>();
		result.put("selfRanking", 0);
		result.put("rankingList", new ArrayList<>());
		if(user != null){
			//获取缓存，数据缓存半小时
			String key = String.format("userProfitRanking%s%s%s", map.get("companyCode"), user.getId(),map.get("timeQuantum"));
			//如果是时间区间，则缓存对应开始结束时间
			if("3".equals(map.get("timeQuantum"))){
				String time = String.format("%s%s", String.valueOf(map.get("issuedDateStart")).replaceAll("/",""), String.valueOf(map.get("issuedDateEnd")).replaceAll("/",""));
				key = String.format("%s%s", key, time);
			}
			Object obj = redisTemplate.opsForValue().get(key);
			if(obj != null){
				return (Map<String, Object>) obj;
			}
			List<Map<String, Object>> rankingList = rankingUserStatisticsMapper.getRanking(map);
			int seq = 0;
			if(CollectionUtils.isNotEmpty(rankingList)){
				for(int i = 0; i < rankingList.size(); i++){
					//没有用户ID的记录不计算
					if(!StringUtils.isEmpty(rankingList.get(i).get("user_id"))){
						//如果等于当前用户，保存排名
						if(String.valueOf(user.getId()).equals(String.valueOf(rankingList.get(i).get("user_id")))){
							result.put("selfRanking", seq+1);
						}
						//仅保留前30名
						if(seq < 30){
							((List)result.get("rankingList")).add(rankingList.get(i));
						}
						seq ++ ;
					}
				}
			}

			//添加缓存，设置过期时间为半小时
			redisTemplate.opsForValue().set(key, result, 30, TimeUnit.MINUTES);
		}
		return result;
	}

	/**
	 * 获取部门排名
	 * @param map
	 * @return
	 */
	@Override
	public List<Map<String, Object>> getDeptRanking(Map<String, Object> map) {
		List<Map<String, Object>> result = new ArrayList<>();
		User user = AppUtil.getUser();
		if(user == null){
			return result;
		}

		//获取缓存，数据缓存半小时
		String key = String.format("deptRanking%s%s%s", map.get("companyCode"), map.get("level"), map.get("timeQuantum"));
		//如果是时间区间，则缓存对应开始结束时间
		if("3".equals(map.get("timeQuantum"))){
			String time = String.format("%s%s", String.valueOf(map.get("issuedDateStart")).replaceAll("/",""), String.valueOf(map.get("issuedDateEnd")).replaceAll("/",""));
			key = String.format("%s%s", key, time);
		}
		Object obj = redisTemplate.opsForValue().get(key);
		if(obj != null){
			return (List<Map<String, Object>>) obj;
		}

		//由于没写递归算法，此处查询部门必须按照层级倒序排序，不然计算会出问题，存在只统计直属子级部门，所以需要从低层级往层级计算
		List<Map<String, Object>> rankingList = rankingUserStatisticsMapper.getDeptRanking(map);
		if(CollectionUtils.isNotEmpty(rankingList)){
			//查询List转换成Map
			Map<String, Map<String, Object>> tempMap = new HashMap<>();
			for(Map<String, Object> rankingMap : rankingList){
				//由于数据存在许多level为空的无效数据，所以需要排除
				if(rankingMap.get("grade") != null){
					String deptId = String.valueOf(rankingMap.get("deptId"));
					tempMap.put(deptId, new HashMap<>());
					tempMap.get(deptId).put("deptName",String.valueOf(rankingMap.get("deptName")));
					tempMap.get(deptId).put("artNum",String.valueOf(rankingMap.get("artNum")));
					tempMap.get(deptId).put("profit",String.valueOf(rankingMap.get("profit")));
					tempMap.get(deptId).put("companyCode", String.valueOf(rankingMap.get("companyCode")));
					tempMap.get(deptId).put("companyCodeName", String.valueOf(rankingMap.get("companyCodeName")));
					tempMap.get(deptId).put("level", String.valueOf(rankingMap.get("grade"))); //当前部门层级
					//缓存需要统计的部门
					if(String.valueOf(map.get("level")).equals(String.valueOf(rankingMap.get("grade")))){
						result.add(tempMap.get(deptId));
					}
				}
			}
			//统计子部门数据到父级部门
			for(Map<String, Object> rankingMap : rankingList){
				//由于数据存在许多level为空的无效数据，所以需要排除
				if(rankingMap.get("grade") != null){
					String deptId = String.valueOf(rankingMap.get("deptId"));
					String parentId = String.valueOf(rankingMap.get("parentId"));
					//如果直属父级部门被查询出来了，则把子部门稿件数和业绩利润计算进去
					if(tempMap.containsKey(parentId)){
						tempMap.get(parentId).put("artNum", String.valueOf(Integer.parseInt(String.valueOf(tempMap.get(parentId).get("artNum"))) + Integer.parseInt(String.valueOf(tempMap.get(deptId).get("artNum")))));
						tempMap.get(parentId).put("profit", String.valueOf(Double.parseDouble(String.valueOf(tempMap.get(parentId).get("profit"))) + Double.parseDouble(String.valueOf(tempMap.get(deptId).get("profit")))));
					}
				}
			}
			//利润排序
			Collections.sort(result, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> deptMap1, Map<String, Object> deptMap2) {
					Double profit1 = Double.parseDouble(String.valueOf(deptMap1.get("profit")));
					Double profit2 = Double.parseDouble(String.valueOf(deptMap2.get("profit")));
					//升序排的话就是第一个参数.compareTo(第二个参数);
					//降序排的话就是第二个参数.compareTo(第一个参数);
					return profit2.compareTo(profit1);
				}
			});
			for(int i = 0; i < result.size(); i++){
				result.get(i).put("rownum", result.size()-i);
			}
		}

		//添加缓存，设置过期时间为半小时
		redisTemplate.opsForValue().set(key, result, 30, TimeUnit.MINUTES);

		return result;
	}
}
