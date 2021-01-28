package com.qinfei.qferp.service.impl.performance;

import java.util.*;
import java.util.stream.IntStream;

import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.flow.command.DeleteHistoryTaskCommand;
import com.qinfei.qferp.flow.listener.JumpTaskCommand;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.core.ResponseData;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.performance.PerformanceProgram;
import com.qinfei.qferp.entity.performance.PerformanceProportion;
import com.qinfei.qferp.entity.performance.PerformanceScore;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.performance.PerformanceProgramMapper;
import com.qinfei.qferp.mapper.performance.PerformanceProportionMapper;
import com.qinfei.qferp.mapper.performance.PerformanceScoreMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.performance.IPerformanceProportionService;
import com.qinfei.qferp.service.performance.IPerformanceScoreService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class PerformanceProportionService implements IPerformanceProportionService {

	@Autowired
	private PerformanceProportionMapper proportionMapper;
	@Autowired
	private PerformanceProgramMapper programMapper;
	@Autowired
	private IPerformanceScoreService scoreService;
	@Autowired
	private IMessageService messageService;
	@Autowired
	UserMapper userMapper;
	@Autowired
	private PerformanceScoreMapper scoreMapper;
	@Autowired
	private ManagementService managementService;

	/**
	 * 添加考核计划
	 * 
	 * @param proportion
	 * @return
	 */
	@Override
	@Transactional
	public ResponseData saveProportion(PerformanceProportion proportion) {
		User user= AppUtil.getUser();
		ResponseData data = ResponseData.ok();
		String proCode = "JXJH" + DateUtils.getStr();
		proportion.setProCode(proCode);
		proportion.setDeptId(user.getDeptId());
		proportion.setCompanyCode(user.getCompanyCode());
		proportion.setProNotice(0);
		proportion.setProMessage(0);
		proportion.setCreateInfo();
		// 保存计划
		proportionMapper.insertSelective(proportion);
		List<Integer> postId = proportion.getPostId();
		List<String> postName = proportion.getPostName();
		List<Integer> schId = proportion.getSchId();
		List<String> schName = proportion.getSchName();
		List<String> schUserId = proportion.getSchUserId();
		List<String> schUserName = proportion.getSchUserName();

		// 将数据保存到计划-方案关系表中
		IntStream.range(0, proportion.getSchId().size()).forEach(i -> {
			PerformanceProgram program = new PerformanceProgram();
			program.setProId(proportion.getProId());
			program.setProName(proportion.getProName());
			if(CollectionUtils.isNotEmpty(postId)){
			   program.setPostId(postId.get(i));
			}
			program.setPostName(postName.get(i));
			program.setSchId(schId.get(i));
			program.setSchName(schName.get(i));
			if(CollectionUtils.isNotEmpty(schUserId)){
				//如果考核人员为空，为防止报错
				if(schUserName.size()!=schId.size()){
					String schUserIds = StringUtils.join(proportion.getSchUserId(), ",");
					program.setSchUserId(schUserIds);
				}else{
					program.setSchUserId(schUserId.get(i));
				}
			}
			if(CollectionUtils.isNotEmpty(schUserName)){
				if(schUserName.size()!=schId.size()){
					String schUserNames = StringUtils.join(proportion.getSchUserName(), ",");
					program.setSchUserName(schUserNames);
				}else{
					program.setSchUserName(schUserName.get(i));
				}
			}
			program.setCreateInfo();
			programMapper.insertSelective(program);
		});

		// 操作成功
		data.putDataValue("message", "操作成功");
		return data;
	}

	/**
	 * 通过id伪删除
	 *
	 * @param proportionId
	 * @return
	 */
	@Override
	public ResponseData deleteProportion(Integer proportionId) {
		ResponseData data = ResponseData.ok();
		Map map = new HashMap();
		map.put("proId",proportionId);
		//获取绩效计划的审核数据
		List<PerformanceScore> list = scoreService.getAllApproveData(map);
		List<Integer> itemIds=null;
		if(CollectionUtils.isNotEmpty(list)){//已发起流程
			itemIds = new ArrayList<>();
			for(PerformanceScore score:list){
				itemIds.add(score.getItemId());
				String taskId = score.getTaskId();
				if(StringUtils.isNotEmpty(taskId)){
				    // 结束老的流程；
				    managementService.executeCommand(new JumpTaskCommand(taskId, "endEvent"));
				    // 删除驳回前的任务；
				    managementService.executeCommand(new DeleteHistoryTaskCommand(taskId));
				}
			}
			//step1:删除流程相关待办
            if(CollectionUtils.isNotEmpty(itemIds)){
				//删除发送的消息
				List<Integer> ids=messageService.queryIdsByItemIds(itemIds);
				if(CollectionUtils.isNotEmpty(ids)){
				    messageService.updateMessage(ids);
				}
            	Map param = new HashMap();
				param.put("list",itemIds);
				param.put("transactionState", Const.ITEM_Y);
				param.put("finishWorker", AppUtil.getUser().getId());
            	scoreMapper.updateItemData(param);
			}
			//step2:删除考核计划关联的结果数据（伪删除）
			map.put("updateTime",new Date());
			map.put("updateId",AppUtil.getUser().getId());
			scoreMapper.deleteByProId(map);
			//step3:删除绩效计划
			proportionMapper.deleteById(proportionId);
		}else {
           //未发起流程：删除绩效计划
		   proportionMapper.deleteById(proportionId);
		}
		data.putDataValue("message", "操作成功");
		return data;
	}

	/**
	 * 根据条件获取绩效考核计划
	 *
	 * @return
	 */
	@Override
	public PageInfo<PerformanceProportion> selectProportion(Map<String, Object> params, Pageable pageable) {
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
		List<PerformanceProportion> list = new ArrayList<>();
		list = proportionMapper.getProportionList(params);
        return new PageInfo<>(list);
	}

	/**
	 * 更新绩效考核计划
	 *
	 * @param proportion
	 * @return
	 */
	@Override
	@Transactional
	public ResponseData updateProportion(PerformanceProportion proportion) {
		ResponseData data = ResponseData.ok();
		proportion.setUpdateInfo();
		// 部门ID不允许修改；
		proportion.setDeptId(null);
		proportionMapper.updateByPrimaryKeySelective(proportion);
		// 删除计划-方案联系表数据
		programMapper.deleteByProId(proportion.getProId());

		List<Integer> postId = proportion.getPostId();
		List<String> postName = proportion.getPostName();
		List<Integer> schId = proportion.getSchId();
		List<String> schName = proportion.getSchName();
		List<String> schUserId = proportion.getSchUserId();
		List<String> schUserName = proportion.getSchUserName();

		// 将数据保存到计划-方案关系表中
		IntStream.range(0, proportion.getPostId().size()).forEach(i -> {
			PerformanceProgram program = new PerformanceProgram();
			program.setProId(proportion.getProId());
			program.setProName(proportion.getProName());
			program.setPostId(postId.get(i));
			program.setPostName(postName.get(i));
			program.setSchId(schId.get(i));
			program.setSchName(schName.get(i));
			if(CollectionUtils.isNotEmpty(schUserId)){
				if(schUserName.size()!=postId.size()){
					String schUserIds = StringUtils.join(proportion.getSchUserId(), ",");
					program.setSchUserId(schUserIds);
				}else{
					program.setSchUserId(schUserId.get(i));
				}
			}
			if(CollectionUtils.isNotEmpty(schUserName)){
				if(schUserName.size()!=postId.size()){
					String schUserNames = StringUtils.join(proportion.getSchUserName(), ",");
					program.setSchUserName(schUserNames);
				}else{
 			       program.setSchUserName(schUserName.get(i));
				}
			}
			program.setCreateInfo();
			programMapper.insertSelective(program);
		});
		data.putDataValue("message", "操作成功");
		return data;
	}

	/**
	 * 更新绩效考核计划启用状态
	 *
	 * @param proportionId
	 * @return
	 */
	@Override
	public ResponseData updateProportionUserState(Integer proportionId, Integer proUsed) {
		ResponseData data = ResponseData.ok();
		proportionMapper.updateById(proportionId, proUsed);
		data.putDataValue("message", "操作成功");
		return data;

	}

	/**
	 * 复制绩效考核计划
	 * 
	 * @param entity
	 * @return
	 */
	@Override
	@Transactional
	public ResponseData copyProportion(PerformanceProportion entity) {
		ResponseData data = ResponseData.ok();
		String proCode = "JXJH" + DateUtils.getStr();
		// 获取绩效考核计划
		PerformanceProportion proportion = proportionMapper.selectByPrimaryKey(entity.getProId());
		// 获取绩效考核方案
		List<PerformanceProgram> list = new ArrayList();
		// 获取计划-方案详情
		list = programMapper.selectByProId(entity.getProId());
		PerformanceProportion pro = new PerformanceProportion();
		pro.setProBegin(entity.getProBegin());
		pro.setProCode(proCode);
		pro.setProDesc(proportion.getProDesc());
		pro.setProEnd(entity.getProEnd());
		pro.setProMessage(proportion.getProMessage());
		pro.setProName(entity.getProName());
		pro.setProNotice(proportion.getProNotice());
		pro.setProportionGroup(proportion.getProportionGroup());
		pro.setProportionLeader(proportion.getProportionLeader());
		pro.setProportionSelf(proportion.getProportionSelf());
		pro.setProType(proportion.getProType());
		pro.setProUsed(proportion.getProUsed());
		proportion.setProNotice(0);
		proportion.setProMessage(0);
		pro.setCreateInfo();
		pro.setDeptId(AppUtil.getUser().getDeptId());
		pro.setCompanyCode(proportion.getCompanyCode());
		proportionMapper.insertSelective(pro);
		for (PerformanceProgram program : list) {
			PerformanceProgram gram = new PerformanceProgram();
			gram.setProId(pro.getProId());
			gram.setProName(entity.getProName());
			gram.setSchId(program.getSchId());
			gram.setSchName(program.getSchName());
			gram.setSchUserId(program.getSchUserId());
			gram.setSchUserName(program.getSchUserName());
			gram.setPostId(program.getPostId());
			gram.setPostName(program.getPostName());
			gram.setCreateInfo();
			programMapper.insertSelective(gram);
		}
		data.putDataValue("message", "操作成功");
		return data;
	}

	/**
	 * 通过计划id获取考核计划
	 * 
	 * @param proportionId
	 * @return
	 */
	@Override
	public ResponseData getProportionById(Integer proportionId) {
		ResponseData data = ResponseData.ok();
		PerformanceProportion pro = proportionMapper.selectByPrimaryKey(proportionId);
		// 通过计划id获取所有关联的方案信息
		List<PerformanceProgram> list = programMapper.selectByProId(proportionId);
		pro.setProgramList(list);
		data.putDataValue("message", "操作成功");
		data.putDataValue("entity", pro);
		return data;
	}

	@Override
	public PerformanceProportion getByProId(int proId) {
		return proportionMapper.getByProId(proId);
	}

	@Override
	public PerformanceProportion findProportionByCondition(Map map) {
		map.put("companyCode",AppUtil.getUser().getCompanyCode());
		return proportionMapper.findProportionByCondition(map);
	}

	/**
	 * 通过类型获取所有的考核计划
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public PageInfo<PerformanceProportion> getProportionByType(Integer type) {
		List<PerformanceProportion> list = proportionMapper.getList(type,AppUtil.getUser().getCompanyCode());
        return new PageInfo<>(list);
	}

	/**
	 * 获取所有考核计划
	 * 
	 * @return
	 */
	@Override
	public List<PerformanceProportion> getAllProportion() {
		Map map = new HashMap();
		// 公司代码过滤；
		map.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
        return (List<PerformanceProportion>) proportionMapper.getProportionList(map);
	}

	/**
	 * 根据主键获取相关的考核计划信息；
	 *
	 * @param proportionId：主键ID；
	 * @return ：考核计划数据；
	 */
	@Override
	public PerformanceProportion getProportion(int proportionId) {
		return proportionMapper.selectByPrimaryKey(proportionId);
	}

	/**
	 * 考核计划流程发起
	 */
	public void starformance() {
		// 查询出开始时间是当天的所有数据
		List<PerformanceProportion> listToday = proportionMapper.getTodayData();
		if (listToday.size() > 0) {
			for (PerformanceProportion proportion : listToday) {
				// 开启流程
				scoreService.startPerformanceProcess(proportion.getProId());
			}
		}
	}

	/**
	 * 绩效考核发送通知
	 */
	public void sendMessage() {
		User user = userMapper.getAdmin();
		String userImage = user.getImage();
		String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
		// 查询出开始时间小于当前时间，结束时间大于当前时间的数据
		List<PerformanceProportion> list = proportionMapper.getData();
		List<Message> messagesList = new ArrayList<>();
		String content = "您的绩效考核还未提交，请尽快提交！";
		if (list.size() > 0) {
			for (PerformanceProportion proportion : list) {
				// 获取该计划所有没有提交绩效考核的人
				List<PerformanceScore> listScore = scoreMapper.listScore(proportion.getProId());
				if (listScore.size() > 0) {
					for (PerformanceScore score : listScore) {
						Message newMessage = new Message();
						newMessage.setPic(pictureAddress);
						newMessage.setContent(content);
						newMessage.setInitiatorDept(user.getDeptId());
						newMessage.setInitiatorWorker(user.getId());
						newMessage.setAcceptWorker(score.getUserId());
						newMessage.setAcceptDept(score.getDeptId());
						//消息分类
						newMessage.setParentType(3);//通知
						newMessage.setType(16);//绩效
						newMessage.setUrl(null);
						newMessage.setUrlName(null);
						messagesList.add(newMessage);
					}
				}
			}
			messageService.batchAddMessage(messagesList);
		}
	}

}
