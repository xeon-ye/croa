package com.qinfei.qferp.service.impl.propose;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.propose.ProposeTips;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.propose.ProposeTipsMapper;
import com.qinfei.qferp.service.propose.IProposeTipsService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * 建议提示内容接口实现类
 * @author tsf
 */
@Service
public class ProposeTipsService implements IProposeTipsService {

    @Autowired
    private ProposeTipsMapper proposeTipsMapper;
    @Autowired
    private IUserService userService;

    @Override
    public ProposeTips getById(Integer id) {
        return proposeTipsMapper.getById(id);
    }

    @Override
    public void saveProposeTips(ProposeTips proposeTips) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录！");
            }
            proposeTips.setState(0);
            proposeTips.setUpdateTime(new Date());
            proposeTips.setUpdateUserId(user.getId());
            proposeTips.setCompanyCode(user.getCompanyCode());
            proposeTipsMapper.saveProposeTips(proposeTips);
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，新增建议提示出错啦，请联系技术人员！");
        }
    }

    @Override
    public void editProposeTips(ProposeTips proposeTips) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录！");
            }
            proposeTips.setUpdateTime(new Date());
            proposeTips.setUpdateUserId(user.getId());
            proposeTipsMapper.editProposeTips(proposeTips);
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，修改建议提示出错啦，请联系技术人员！");
        }
    }

    @Override
    public void editTipsData(ProposeTips proposeTips) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录！");
            }
            if(proposeTipsMapper.getById(proposeTips.getId())==null){
                throw new QinFeiException(1002,"建议提示不存在，请刷新页面");
            }
            Integer state = proposeTips.getState();
            proposeTips.setUpdateTime(new Date());
            proposeTips.setUpdateUserId(user.getId());
            //启用操作
            if(state==1){
                //还原其他启用数据
                proposeTipsMapper.editTipsState(user.getCompanyCode());
                //启用
                proposeTipsMapper.editProposeTips(proposeTips);
            }else{
                //停用
                proposeTipsMapper.editProposeTips(proposeTips);
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，修改建议提示出错啦，请联系技术人员！");
        }
    }

    @Override
    public void editDocumentData(ProposeTips proposeTips) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录！");
            }
            if(proposeTipsMapper.getById(proposeTips.getId())==null){
                throw new QinFeiException(1002,"建议制度不存在，请刷新页面");
            }
            Integer state = proposeTips.getState();
            proposeTips.setUpdateTime(new Date());
            proposeTips.setUpdateUserId(user.getId());
            //启用操作
            if(state==1){
                //还原其他启用数据
                proposeTipsMapper.editDocumentState(user.getCompanyCode());
                //启用
                proposeTipsMapper.editProposeTips(proposeTips);
            }else{
                //停用
                proposeTipsMapper.editProposeTips(proposeTips);
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，修改建议制度跳转出错啦，请联系技术人员！");
        }
    }

    @Override
    public Integer getDocumentUrl() {
        return proposeTipsMapper.getDocumentUrl(AppUtil.getUser().getCompanyCode());
    }

    @Override
    public String getSuggestContent() {
        return proposeTipsMapper.getSuggestContent(AppUtil.getUser().getCompanyCode());
    }

    @Override
    public void delProposeTips(Integer id) {
        User user = AppUtil.getUser();
        if(user==null){
            throw new QinFeiException(1002,"请先登录！");
        }
        if(proposeTipsMapper.getById(id)==null){
            throw new QinFeiException(1002,"建议提示不存在");
        }
        ProposeTips entity=new ProposeTips();
        entity.setId(id);
        entity.setState(IConst.STATE_DELETE);
        entity.setUpdateTime(new Date());
        entity.setUpdateUserId(user.getId());
        proposeTipsMapper.editProposeTips(entity);
    }

    @Override
    public PageInfo<ProposeTips> queryProposeTips(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        List<ProposeTips> list = proposeTipsMapper.queryProposeTips(map);
        return new PageInfo<>(list);
    }

    @Override
    public List<Integer> updateProposeCache(Integer userId,String companyCode) {
        List<Integer> list = userService.querySuggestHintData(AppUtil.getUser().getCompanyCode());
        if(CollectionUtils.isNotEmpty(list)){
            Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()){
                Integer id = iterator.next();
                if(userId.equals(id)){
                    iterator.remove();
                }
            }
        }
        return list;
    }

    @Override
    public void saveTimeSection(Integer state,String startTime,String endTime,String companyCode){
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录！");
            }
            ProposeTips proposeTips = new ProposeTips();
            //查询建议管理是否设置了统计时间值，存在则修改，不存在则新增
            List<ProposeTips> list = proposeTipsMapper.queryTipsByType(companyCode);
            String remark = startTime+","+endTime;
            if(CollectionUtils.isNotEmpty(list)){
                proposeTips.setState(state);//1：1个月，2：2个月，3：3个月
                proposeTips.setUpdateTime(new Date());
                proposeTips.setUpdateUserId(user.getId());
                proposeTips.setCompanyCode(user.getCompanyCode());
                proposeTips.setType(3);//建议时间范围设置
                proposeTips.setRemark(remark);
                proposeTipsMapper.updateTimeSection(proposeTips);
            }else{
                proposeTips.setState(state);
                proposeTips.setCreateId(user.getId());
                proposeTips.setCreateName(user.getName());
                proposeTips.setCreateTime(new Date());
                proposeTips.setUpdateTime(new Date());
                proposeTips.setUpdateUserId(user.getId());
                proposeTips.setCompanyCode(companyCode);
                proposeTips.setType(3);//建议时间范围设置
                proposeTips.setRemark(remark);
                proposeTipsMapper.saveProposeTips(proposeTips);
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public List<ProposeTips> queryTipsByType(String companyCode) {
        try {
            return proposeTipsMapper.queryTipsByType(companyCode);
        } catch (QinFeiException e) {
            throw e;
        }
    }
}
