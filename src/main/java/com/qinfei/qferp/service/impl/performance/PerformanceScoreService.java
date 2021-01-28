package com.qinfei.qferp.service.impl.performance;

import java.util.*;

import com.qinfei.qferp.entity.sys.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.performance.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.performance.PerformanceScoreMapper;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.performance.*;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IEmployData;
import com.qinfei.qferp.utils.IPerformanceState;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import redis.clients.jedis.BinaryClient;

/**
 * 绩效考核的评分接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/4/14 0014 22:04；
 */
@Service
public class PerformanceScoreService implements IPerformanceScoreService {
	// 数据执行接口；
	@Autowired
	private PerformanceScoreMapper scoreMapper;
	// 考核计划业务接口；
	@Autowired
	private IPerformanceProportionService proportionService;
	// 绩效考核计划关联的考核方案的业务接口；
	@Autowired
	private IPerformanceProgramService programService;
	// 绩效考核方案业务接口；
	@Autowired
	private IPerformanceSchemeService schemeService;
	// 评分明细业务接口；
	@Autowired
	private IPerformanceDetailService detailService;
	// 流程执行业务接口；
	@Autowired
	private IProcessService processService;
	// 用户信息业务接口；
	@Autowired
	private IUserService userService;
	//绩效考核方案关联项目接口
//	@Autowired
//	private IPerformanceHistoryService performanceHistoryService;

	/**
	 * 更新绩效考核评分相关的数据；
	 *
	 * @param scoreData：考核评分数据的字符串数据；
	 */
	@Override
	@Transactional
	public void updatePerformance(String scoreData) {
		// 解析对象；
		JSONObject json = JSON.parseObject(scoreData);

		Object object = json.get("scoreId");
		if (object != null) {
			Integer scoreId = Integer.parseInt(object.toString());
			object = json.get("processState");
			if (object != null) {
				int state = Integer.parseInt(object.toString());
				String schemeType = json.get("schemeType").toString();
				// 只获取关键信息；
				PerformanceScore performanceScore = new PerformanceScore();

				performanceScore.setScoreId(scoreId);
				// 个人节点；
				if (state == IConst.STATE_GR) {
					performanceScore.setSchDesc(json.get("schName") == null ? null : json.getString("schName"));
					performanceScore.setScoreSelf(json.get("scoreSelfTotal") == null ? null : json.getFloat("scoreSelfTotal"));
				}
				// 部长节点；
				if (state == IConst.STATE_BZ) {
					performanceScore.setScoreLeader(json.get("scoreLeaderTotal") == null ? null : json.getFloat("scoreLeaderTotal"));
				}
				performanceScore.setUpdateInfo();
				performanceScore.setBeSuffice(json.getBoolean("beSuffice") == true ? 1 : 0);
				performanceScore.setScoreLevel(json.getString("scoreLevel"));
				// 更新相关的信息；
				scoreMapper.updateScoreData(performanceScore);

				// 获取关联的评分明细对象；
				Object object2 = json.get("scoreKey");
				Boolean flag = isArray(object2);
				Map<Integer, Float> scoreMap = null;
				Map<Integer, Float> totalMap = null;
				Map<Integer, String> remarkMap = null;
				Map<Integer, String> targetMap = null;
				Map<Integer, String> demandMap = null;
				String key = "plate";
				String totalKey = "total";
				String remarkKey = "remark";
				String targetKey = "plateTarget";
				String demandKey = "plateDemand";
				if (flag) {
					JSONArray jsonArray = json.getJSONArray("scoreKey");
					if("1".equals(schemeType)){
						//kpl备注字段
						remarkMap = new HashMap<>();
					}else {
						//okr未完成原因
						targetMap = new HashMap<>();
						demandMap = new HashMap<>();
					}
					if (jsonArray != null && jsonArray.size() > 0) {
						scoreMap = new HashMap<>();
						totalMap = new HashMap<>();
						// 集合对象；
						for (int i = 0; i < jsonArray.size(); i++) {
							// 获取填写的评分信息；
							object = jsonArray.get(i);
							scoreMap.put(Integer.parseInt(object.toString()), json.get(key + object) == null ? null : json.getFloat(key + object));
							totalMap.put(Integer.parseInt(object.toString()), json.get(totalKey + object) == null ? null : json.getFloat(totalKey + object));
							if("1".equals(schemeType)){
								//kpl备注字段
								remarkMap.put(Integer.parseInt(object.toString()), json.get(remarkKey + object) == null ? null : json.getString(remarkKey + object));
							}else {
								//okr未完成原因
								targetMap.put(Integer.parseInt(object.toString()),json.get(targetKey + object) == null ? null : json.getString(targetKey + object));
								demandMap.put(Integer.parseInt(object.toString()),json.get(demandKey + object) == null ? null : json.getString(demandKey + object));
							}
						}
					}
				} else {
					String scoreKey = json.getString("scoreKey");
					scoreMap = new HashMap<>();
					totalMap = new HashMap<>();
					scoreMap.put(Integer.parseInt(scoreKey), json.get(key + scoreKey) == null ? null : json.getFloat(key + scoreKey));
					totalMap.put(Integer.parseInt(scoreKey), json.get(totalKey + scoreKey) == null ? null : json.getFloat(totalKey + scoreKey));
					if("1".equals(schemeType)){
						//kpl备注字段
						remarkMap = new HashMap<>();
						remarkMap.put(Integer.parseInt(scoreKey), json.get(remarkKey + scoreKey) == null ? null : json.getString(remarkKey + scoreKey));
					}else {
						//okr未完成原因
						targetMap = new HashMap<>();
						demandMap = new HashMap<>();
						targetMap.put(Integer.parseInt(scoreKey),json.get(targetKey + scoreKey) == null ? null : json.getString(targetKey + scoreKey));
						demandMap.put(Integer.parseInt(scoreKey),json.get(demandKey + scoreKey) == null ? null : json.getString(demandKey + scoreKey));
					}
				}

				// 数据信息，获取方案关联的评分细则数据；
				List<PerformanceDetail> detailInfo = detailService.getDetailInfo(scoreId);
				// 封装属性；
				object = json.get("schId");
				if (object != null) {
					if (detailInfo == null || detailInfo.size() <= 0) {
						List<PerformanceHistory> schemeHistory = schemeService.getSchemeHistory(Integer.parseInt(object.toString()));
						detailInfo = new ArrayList<>();
						PerformanceHistory history;
						Integer plateId;
						// 明细对象；
						PerformanceDetail detail;
						for (int i = 0; i < schemeHistory.size(); i++) {
							history = schemeHistory.get(i);
							plateId = history.getPlateId();

							detail = new PerformanceDetail();
							detail.setScoreId(scoreId);
							detail.setPlateId(plateId);
							detail.setPlateLevel(history.getPlateLevel());
							detail.setPlateProportion(history.getPlateProportion());
							detail.setPlateParent(history.getPlateParent());
							detail.setPlateContent(history.getPlateContent());
							// 新增只有个人节点；
							detail.setScoreSelf(scoreMap.get(plateId));
							detail.setScoreTotal(totalMap.get(plateId));
							if("1".equals(schemeType)){
								//kpl备注字段
								detail.setRemark(remarkMap.get(plateId));
								detail.setPlateTarget(history.getPlateTarget());
								detail.setPlateDemand(history.getPlateDemand());
							}else {
								//okr未完成原因
								detail.setPlateTarget(targetMap.get(plateId));
								detail.setPlateDemand(demandMap.get(plateId));
							}
							detail.setCreateInfo();
							// 保存；
							detailInfo.add(detail);
						}
					} else {
						// 更新评分；
						for (PerformanceDetail detail : detailInfo) {
							// 个人节点；
							if (state == IConst.STATE_GR) {
								detail.setScoreSelf(scoreMap.get(detail.getPlateId()));
								detail.setScoreTotal(totalMap.get(detail.getPlateId()));
								if("1".equals(schemeType)){
									//kpl备注字段
									detail.setRemark(remarkMap.get(detail.getPlateId()));
								}else {
									//okr未完成原因
									detail.setPlateTarget(targetMap.get(detail.getPlateId()));
									detail.setPlateDemand(demandMap.get(detail.getPlateId()));
								}
							}
							// 部长节点；
							if (state == IConst.STATE_BZ) {
								detail.setScoreLeader(scoreMap.get(detail.getPlateId()));
								detail.setScoreTotal(totalMap.get(detail.getPlateId()));
							}
						}
					}
					// 新增或更新相关的信息；
					detailService.saveOrUpdate(detailInfo);
				}
			}
		}
	}

	/**
	 * 对象是否为数组对象
	 *
	 * @param obj 对象
	 * @return 是否为数组对象，如果为{@code null} 返回false
	 */
	public static boolean isArray(Object obj) {
		if (obj instanceof JSONArray) {
			return true;
		}else{
		    return false;
		}
	}

	/**
	 * 启动绩效考核流程；
	 *
	 * @param scoreId：考核评分ID；
	 * @return ：处理结果提示信息；
	 */
	@Override
	public String startSinglePerformanceProcess(int scoreId) {
		// 根据考核方案信息构建评分对象；
		List<PerformanceScore> scores = new ArrayList<>();
		scores.add(scoreMapper.selectByPrimaryKey(scoreId));
		// 批量发起考核流程；
		String result = processService.addPerformanceProcess(scores, Const.ITEM_J3);
		return StringUtils.isEmpty(result) ? "流程配置异常。" : result;
	}

	/**
	 * 启动绩效考核流程；
	 *
	 * @param proId：考核计划ID；
	 * @return ：处理结果提示信息；
	 */
	@Override
	@Transactional
	public String startPerformanceProcess(int proId) {
		// 获取关联的绩效考核方案；
		List<PerformanceProgram> performancePrograms = programService.selectByProId(proId);
		if (performancePrograms == null || performancePrograms.size() <= 0) {
			return "该计划未配置考核方案，请检查。";
		} else {
			StringBuffer buffer = new StringBuffer("");
			for(int i=0;i<performancePrograms.size();i++){
			    PerformanceProgram program = performancePrograms.get(i);
                if(program.getSchState()==-1){
                    buffer.append(program.getSchName());
                    buffer.append("|");
                }
            }
			if(!StringUtils.isEmpty(buffer.toString())){
				return String.format("抱歉，[%s方案]已删除，不能发起流程，请新建计划再重新发起",buffer.toString());
			}
			// 清空已存在的数据；
			PerformanceScore score;
			// 获取已有的数据；
			Map<String, Object> params = new HashMap<>();
			params.put("proId", proId);
			List<PerformanceScore> oldData = scoreMapper.selectPagePerformanceScore(params);
			if(!CollectionUtils.isEmpty(oldData)){
				return "抱歉，该计划已经发起流程了，请新建计划或拷贝该计划再重新发起。";
			}
//			if (oldData != null && oldData.size() > 0) {
//				// 清空流程；
//				for (PerformanceScore data : oldData) {
//					processService.deleteProcess(data.getTaskId(), data.getItemId());
//				}
//				// 先删除，确保不会重复；
//				score = new PerformanceScore();
//				score.setProId(proId);
//				score.setUpdateInfo();
//				score.setState(IEmployData.DATA_DELETE);
//				scoreMapper.deleteByParentId(score);
//			}

			// 根据考核方案信息构建评分对象；
			List<PerformanceScore> scores = new ArrayList<>();
			// 方案关联的用户ID；
			String userIds;
			// 方案关联的用户名称；
			String userNames;
			// 切割后的用户ID数组；
			String[] userIdArray;
			// 切割后的用户名称数组；
			String[] userNameArray;
			// ID数组的长度；
			int idLength;
			// 名称数组的长度；
			int nameLength;
			// 获取用户信息集合；
			Map<Integer, User> userMap = userService.listAllUserMap();
			// 用户对象；
			User user;
			for (PerformanceProgram program : performancePrograms) {
				userIds = program.getSchUserId();
				userNames = program.getSchUserName();
				// 校验数据，确保有效；
				if (StringUtils.isEmpty(userIds) || StringUtils.isEmpty(userNames)) {
					continue;
				} else {
					userIdArray = userIds.split(",");
					idLength = userIdArray.length;
					userNameArray = userNames.split(",");
					nameLength = userNameArray.length;
					// 校验数据，确保一致；
					if (idLength == nameLength) {
						for (int j = 0; j < idLength; j++) {
							userIds = userIdArray[j];
							userNames = userNameArray[j];

							// 获取部分关键信息即可，其他信息在个人评分时可获取到；
							score = new PerformanceScore();
							score.setProId(proId);
							score.setProDesc(program.getProName());
							score.setSchId(program.getSchId());
							score.setUserId(Integer.parseInt(userIds));
							score.setUserName(userNames);
                            score.setSchemeType(program.getSchemeType());
							// 获取性别、部门信息；
							user = userMap.get(score.getUserId());
							if (user != null) {
								score.setUserGender(user.getSex());
								score.setDeptId(user.getDeptId());
								score.setDeptName(user.getDeptName());
							}
							score.setPostId(program.getPostId());
							score.setPostName(program.getPostName());
							score.setCreateInfo();

							// 添加到集合；
							scores.add(score);
						}
					} else {
						continue;
					}
				}

			}

			// 存储考核评分对象数据；
			int size = scores.size();
			if (size > 0) {
				// 定义需要进行分割的尺寸；
				int subLength = 100;
				// 计算需要插入的次数，100条插入一次；
				int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
				// 100条以上才需要进行处理；
				if (insertTimes > 1) {
					List<PerformanceScore> insertData;
					// 计算需要循环插入的次数；
					for (int i = 0; i < insertTimes; i++) {
						insertData = new ArrayList<>();

						// 计算起始位置，且j的最大值应不能超过数据的总数；
						for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
							score = scores.get(j);
							insertData.add(score);
						}
						scoreMapper.insertBatch(insertData);
					}
				} else {
					scoreMapper.insertBatch(scores);
				}

				// 批量发起考核流程；
				String result = processService.addPerformanceProcess(scores, Const.ITEM_J3);
				return StringUtils.isEmpty(result) ? "流程配置异常。" : result;
			} else {
				return "考核方案未配置考核对象，请检查。";
			}
		}
	}

//	private static long getTimeDiffer(Date beginTime,Date endTime)  {
//		// getTime() 方法获取的是毫秒值  将其转为秒返回
//		long timeDifference = endTime.getTime()-beginTime.getTime();
//		return timeDifference/1000;
//	}

	/**
	 * 绩效考核的流程更新状态；
	 *
	 * @param scoreId：主键ID；
	 * @param code：当前使用的查询码；
	 * @param state：当前状态；
	 * @param taskId：任务ID；
	 * @param itemId：待办事项ID；
	 */
	@Override
	public void processPerformance(int scoreId, String code, int state, String taskId, Integer itemId) {
		// 根据状态处理数据；
		int scoreState;
		// 根据流程状态来更新数据的状态；
		switch (state) {
		// 拒绝的流程状态改为试用期；
		case IConst.STATE_REJECT:
			scoreState = IPerformanceState.REJECT;
			break;
		// 审核完成，更新状态为转正状态；
		case IConst.STATE_FINISH:
			scoreState = IPerformanceState.FINISH;
			break;
		default:
			scoreState = IPerformanceState.HANDING;
			break;
		}

		// 获取更新内容所需的数据；
		PerformanceScore score = new PerformanceScore();
		score.setScoreId(scoreId);
		if(state!=-1){
		   score.setValidCode(code);
        }
		score.setProcessState(state);
		score.setTaskId(taskId);
		score.setItemId(itemId);
		score.setUpdateInfo();
		score.setState(scoreState);
		scoreMapper.updateByPrimaryKeySelective(score);
	}

	/**
	 * 获取绩效考核流程所需的审核数据；
	 *
	 * @param data：返回给前端的数据；
	 * @param code：权限访问码；
	 */
	@Override
	public void setPerformanceApproveData(ResponseData data, String code) {
		Map<String, Object> params = new HashMap<>();
		params.put("code", code);
//		params.put("state", IPerformanceState.HANDING);
		PerformanceScore score = scoreMapper.selectApproveInfoByCode(params);
		if (score == null) {
			data.putDataValue("message", "未找到相关的信息，可能信息已过期。");
		} else {
			int processState = score.getProcessState();
			// 获取审核人；
			List<User> users = null;
			User user = AppUtil.getUser();
			// 获取考核方案的信息；
			PerformanceScheme performanceScheme = schemeService.getScheme(score.getSchId());
			// 个人的下个节点是部长；
			if (processState == IConst.STATE_GR) {
				//如果个人是业务部长的话审批人为总经办总经理或者业务总监，否则审批人为总经办总经理
				if(performanceScheme.getSchemeType()==1){
					if(userService.isDeptLeader(user.getId(),user.getDeptId())){
						Map map = new HashMap();
						map.put("companyCode",user.getDept().getCompanyCode());
						List<Role> list =user.getRoles();
						for(int i=0;i<list.size();i++){
							if("YW".equals(list.get(i).getType()) && "BZ".equals(list.get(i).getCode())){
								map.put("roleType","YW");
								map.put("roleCode","BZ");
								break;
							}
						}
						users = userService.queryUserByRoleType(map);
					}else{
						users = userService.listLeaderByState(IConst.STATE_BZ);
					}
				}else{
					Map map = new HashMap();
					map.put("companyCode",user.getDept().getCompanyCode());
					users = userService.queryUserByRoleType(map);
				}
			}

			// 部长审核的下个节点是人事；
			if (processState == IConst.STATE_BZ) {
//				下个节点为人事
				users = userService.listLeaderByState(IConst.STATE_RS);
			}

			// 部门ID用于审核；
			if (users != null && !users.isEmpty()) {
				data.putDataValue("deptId", users.get(0).getDeptId());
			}

			// 主键用于更新数据；
			data.putDataValue("score", score);
			// 数据信息，获取方案关联的评分细则数据；
			List<PerformanceDetail> detailInfo = detailService.getDetailInfo(score.getScoreId());
			if (detailInfo == null || detailInfo.size() <= 0) {
				data.putDataValue("scheme", schemeService.selectById(score.getSchId()));
			} else {
				data.putDataValue("detail", detailInfo);
				data.putDataValue("scheme", performanceScheme);
			}
			data.putDataValue("proportion", proportionService.getProportion(score.getProId()));
			// 审批人信息；
			data.putDataValue("user", users);
			// 任务ID给前端；
			data.putDataValue("taskId", score.getTaskId());
			// 状态给前端，用于判断当前审批节点；
			data.putDataValue("processState", processState);
		}
	}

	/**
	 * 获取绩效考核的详情记录；
	 *
	 * @param scoreId：主键ID；
	 */
	@Override
	public void getScoreInfo(ResponseData data, int scoreId) {
		PerformanceScore performanceScore = scoreMapper.selectByPrimaryKey(scoreId);
		if (performanceScore == null) {
			data.putDataValue("message", "查询无结果。");
		} else {
			data.putDataValue("score", performanceScore);
			data.putDataValue("detail", detailService.getDetailInfo(scoreId));
			data.putDataValue("scheme", schemeService.getScheme(performanceScore.getSchId()));
			data.putDataValue("proportion", proportionService.getProportion(performanceScore.getProId()));
		}
	}

	/**
	 * 分页查询绩效考核评分信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的评分信息集合；
	 */
	@Override
	public PageInfo<PerformanceScore> selectPagePerformanceScore(Map<String, Object> params, Pageable pageable) {
		// 非人事只能查看本人的信息；
		if (AppUtil.isRoleType(IConst.ROLE_TYPE_RS)) {
			// 公司代码过滤；
			params.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
		} else {
			User user = AppUtil.getUser();
			Integer userId = user.getId();
			Integer deptId = user.getDeptId();
			// 部门领导查看部门下的数据；
			boolean isDeptLeader = userService.isDeptLeader(userId, deptId);
			if (isDeptLeader) {
                String deptIds = userService.getChilds(deptId);
                if (deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                }
				params.put("deptArray", deptIds);
			} else {
				params.put("userId", userId);
			}
		}
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
		List<PerformanceScore> scores = scoreMapper.selectPagePerformanceScore(params);
        return new PageInfo<>(scores);
	}

	@Override
	public List<PerformanceScore> getAllApproveData(Map<String, Object> map) {
		map.put("companyCode",AppUtil.getUser().getDept().getCompanyCode());
		return scoreMapper.selectPagePerformanceScore(map);
	}
}