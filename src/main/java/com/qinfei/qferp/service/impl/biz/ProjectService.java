package com.qinfei.qferp.service.impl.biz;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.biz.Project;
import com.qinfei.qferp.entity.biz.ProjectNode;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.biz.ProjectMapper;
import com.qinfei.qferp.service.biz.IProjectService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
@Transactional
public class ProjectService implements IProjectService {
	@Autowired
	ProjectMapper projectMapper;
	@Autowired
	IProcessService processService;
	@Autowired
	ItemsService itemsService;

	@Override
	public PageInfo<Map> listPg(int pageNum, int pageSize, Map map){
        PageHelper.startPage(pageNum, pageSize);
		User user = AppUtil.getUser();
		if (user.getRoles() != null && user.getRoles().size() > 0) {
			for (Role role : user.getRoles()) {
				if ((IConst.ROLE_TYPE_CW.equals(role.getType())&&
						(IConst.ROLE_CODE_KJ.equals(role.getCode())||IConst.ROLE_CODE_BZ.equals(role.getCode())||IConst.ROLE_CODE_ZJ.equals(role.getCode()))
				)
						||IConst.ROLE_TYPE_ZJB.equals(role.getType())
				) {
					map.put("companyCode",user.getCompanyCode());
				} else if(IConst.ROLE_TYPE_JT.equals(role.getType()) && IConst.ROLE_CODE_KJ.equals(role.getCode())){
					map.put("jituan",user.getCompanyCode());
				}else{
					map.put("userId",user.getId());
				}
			}
		}
		if(map.containsKey("jituan")){
			map.remove("companyCode");
			map.remove("userId");
		}
		if(map.containsKey("companyCode")){
			map.remove("userId");
		}
		List<Map> list = projectMapper.listPg(map);
        return new PageInfo<>(list);
	}

	@Override
	public List<Map> initNodeConfig(Map map){
		return projectMapper.initNodeConfig(map);
	}

	@Override
	public Project add(Project entity, Map map) {
		User user = AppUtil.getUser();
		entity.setApplyId(user.getId());
		entity.setApplyName(user.getName());
		entity.setApplyDeptId(user.getDeptId());
		entity.setApplyDeptName(user.getDeptName());
		entity.setCompanyCode(user.getCompanyCode());
		entity.setApplyTime(new Date());
		entity.setCode(IConst.PROJECT_CODE+ DateUtils.format(new Date(),"yyyyMMdd") + CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.PROJECT_CODE),4));
		projectMapper.insert(entity);
		//处理项目成员
		List<ProjectNode> nodes = doWithNode(entity,map,user);
		if(nodes==null && nodes.size()<2){
			throw new QinFeiException(1002,"项目成员人数必须大于等于2");
		}
		projectMapper.saveNodeBatch(nodes);
		ProjectNode node = nodes.get(0);
		Integer auditUserId = node.getUserId();
		String auditUserName = node.getUserName();
		Integer auditDeptId = node.getDeptId();
		if(entity.getState()>0){
			processService.addProjctProcess(entity, 3,auditUserId,auditUserName,auditDeptId);
		}
		return entity;
	}

	@Override
	public Project edit(Project entity,Map map) {
		User user = AppUtil.getUser();
		entity.setUpdateUserId(user.getId());
		projectMapper.update(entity);
		//先删除旧的项目成员，
		projectMapper.delNodeByProjectId(entity.getId());
		List<ProjectNode> nodes = doWithNode(entity,map,user);
		if(nodes==null && nodes.size()<2){
			throw new QinFeiException(1002,"项目成员人数必须大于等于2");
		}
		//再插入新的项目成员
		projectMapper.saveNodeBatch(nodes);
		ProjectNode node = nodes.get(0);
		Integer auditUserId = node.getUserId();
		String auditUserName = node.getUserName();
		Integer auditDeptId = node.getDeptId();
		if(entity.getState()>0){
			processService.addProjctProcess(entity, 3,auditUserId,auditUserName,auditDeptId);
		}
		return entity;
	}

	private List<ProjectNode> doWithNode(Project entity,Map map,User user){
		List<ProjectNode> nodes = new ArrayList<>();
		List<Integer> keyList = new ArrayList<>();
		Iterator it = map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry)it.next();
			String key = entry.getKey().toString();
			if(!StringUtils.isEmpty(key)){
				if(key.indexOf(IConst.project_user_type_userName)>-1) {
					//key="userName_1"
					String[] keys = key.split("_");
					keyList.add(Integer.parseInt(keys[1]));
				}
			}
		}
		for(int i=0;i<keyList.size();i++){
			Integer value = keyList.get(i);
			ProjectNode node = new ProjectNode();
			StringBuffer nameKey = new StringBuffer();
			StringBuffer userNameKey = new StringBuffer();
			StringBuffer userIdKey = new StringBuffer();
			StringBuffer deptIdKey = new StringBuffer();
			StringBuffer deptNameKey = new StringBuffer();
			StringBuffer ratioKey = new StringBuffer();
			StringBuffer indexKey = new StringBuffer();
			nameKey.append(IConst.project_user_type_name).append("_").append(value);
			userNameKey.append(IConst.project_user_type_userName).append("_").append(value);
			userIdKey.append(IConst.project_user_type_userId).append("_").append(value);
			deptIdKey.append(IConst.project_user_type_deptId).append("_").append(value);
			deptNameKey.append(IConst.project_user_type_deptName).append("_").append(value);
			ratioKey.append(IConst.project_user_type_ratio).append("_").append(value);
			indexKey.append(IConst.project_user_type_index).append("_").append(value);

			node.setProjectId(entity.getId());
			node.setCode(userIdKey.toString());
			node.setName(MapUtils.getString(map,nameKey.toString()));
			node.setUserName(MapUtils.getString(map,userNameKey.toString()));
			node.setUserId(MapUtils.getInteger(map,userIdKey.toString()));
			node.setDeptId(MapUtils.getInteger(map,deptIdKey.toString()));
			node.setDeptName(MapUtils.getString(map,deptNameKey.toString()));
			node.setRatio(MapUtils.getDouble(map,ratioKey.toString()));
			node.setIndex(MapUtils.getInteger(map,indexKey.toString()));
			node.setCreateId(user.getId());
			node.setCreateTime(new Date());
			node.setCompanyCode(user.getCompanyCode());
			node.setState(IConst.STATE_FINISH);
			nodes.add(node);
		}
		return nodes;
	}

	@Override
	public Map view(Integer id){
		Map map = new HashMap();
		map.put("entity",projectMapper.get(Project.class,id));
		map.put("list",projectMapper.queryNodeList(id));
		return map;
	}

	@Override
	public Project getById(Integer id){
		return projectMapper.get(Project.class,id);
	}

	@Override
	@Transactional
	public void enableOrDisable(Project project, Integer flag){
		project.setDisabled(flag);
		project.setUpdateUserId(AppUtil.getUser().getId());
		projectMapper.update(project);
	}

	@Override
	@Transactional
	public void del(Integer id){
		Project project = projectMapper.get(Project.class,id);
		project.setState(IConst.STATE_DELETE);
		project.setUpdateUserId(AppUtil.getUser().getId());
		projectMapper.update(project);
	}

	@Override
	public PageInfo<Map> queryArticlesByProjectId(int pageNum, int pageSize, Map map){
		PageHelper.startPage(pageNum, pageSize);
		List<Map> list = projectMapper.queryArticlesByProjectId(map);
		return new PageInfo<>(list);
	}

	@Override
	public Map querySumByProjectId(Map map){
		return projectMapper.querySumByProjectId(map);
	}

	@Override
	public Project update(Project project){
		 projectMapper.update(project);
		 return project;
	}

	@Override
	public void confirm(Integer itemId) {
		Items items = new Items();
		items.setId(itemId);
		items.setTransactionState(Const.ITEM_Y);
		itemsService.finishItems(items);
	}

}
