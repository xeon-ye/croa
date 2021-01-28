package com.qinfei.qferp.service.impl.news;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.entity.news.RecommendResources;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.media1.MediaPlateMapper;
import com.qinfei.qferp.mapper.news.RecommendResourcesMapper;
import com.qinfei.qferp.service.news.IRecommendResourcesService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RecommendResourcesService implements IRecommendResourcesService {

	public static final String CACHE_KEY = "RECOMMEND_RESOURCES";

	@Autowired
	private RecommendResourcesMapper recommendResourcesMapper;
	@Autowired
	private MediaPlateMapper mediaPlateMapper;


	private void validateNews(RecommendResources recommendRes){
		if(ObjectUtils.isEmpty(recommendRes.getTitle())){
			throw new QinFeiException(1002,"推荐标题不能为空");
		}
		if(ObjectUtils.isEmpty(recommendRes.getResourceTitle())){
			throw new QinFeiException(1002,"资源标题不能为空");
		}
		if(ObjectUtils.isEmpty(recommendRes.getPlateId())){
			throw new QinFeiException(1002,"板块标识不能为空");
		}
		if(ObjectUtils.isEmpty(recommendRes.getHref())){
			throw new QinFeiException(1002,"链接不能为空");
		}

	}

	@Override
	@Transactional
	public Boolean save(RecommendResources recommendRes) {
		User user = AppUtil.getUser();
		validateNews(recommendRes);
		recommendRes.setCreateTime(new Date());
		recommendRes.setUpdateTime(new Date());
		recommendRes.setIsDelete(1);
		recommendRes.setCompanyCode(user.getCompanyCode());
		recommendRes.setCreateId(user.getId());
		recommendRes.setUpdateId(user.getId());
		return recommendResourcesMapper.insert(recommendRes)>0;
	}


	@Override
	@Transactional
	public Boolean update(RecommendResources recommendRes) {
		if(ObjectUtils.isEmpty(recommendRes.getId())){
			throw new QinFeiException(10001,"推荐资源唯一标识不能为空");
		}
		RecommendResources  oldRes = queryById(recommendRes.getId());
		if(oldRes == null){
			throw new QinFeiException(1002, "资源不存在！");
		}
		oldRes.setResourceTitle(recommendRes.getResourceTitle());
		oldRes.setTitle(recommendRes.getTitle());
		oldRes.setHref(recommendRes.getHref());
		oldRes.setPlateId(recommendRes.getPlateId());
		oldRes.setUpdateId(AppUtil.getUser().getId());
		oldRes.setUpdateTime(new Date());
		oldRes.setContent(recommendRes.getContent());
		return recommendResourcesMapper.update(oldRes)>0;
	}

	@Override
	public List<RecommendResources> getResByDto(RecommendResources recommendRes) {
		recommendRes.setIsDelete(1);
		recommendRes.setCompanyCode(AppUtil.getUser().getCompanyCode());
		return recommendResourcesMapper.getListByRes(recommendRes);
	}

	@Override
	public List<RecommendResources> getAdminResByDto(RecommendResources recommendRes,int pageNum, int pateSize, String sort) {
		recommendRes.setIsDelete(1);
		recommendRes.setCompanyCode(AppUtil.getUser().getCompanyCode());
		List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(AppUtil.getUser().getId());
		if(CollectionUtils.isEmpty(mediaPlateList)){
			return new ArrayList<>();
		}
		List<Integer> mediaPlateIdList = new ArrayList<>();
		for (MediaPlate mediaPlate : mediaPlateList) {
			mediaPlateIdList.add(mediaPlate.getId());
		}
		recommendRes.setMediaPlateIdList(mediaPlateIdList);
		PageHelper.startPage(pageNum,pateSize,sort);
		return recommendResourcesMapper.getListByRes(recommendRes);
	}

	@Override
	@Transactional
	public Boolean deleteById(Integer id) {
		RecommendResources  res = recommendResourcesMapper.get(RecommendResources.class,id);
		res.setIsDelete(0);
		return recommendResourcesMapper.update(res)>0;
	}



	@Override
	public RecommendResources queryById(Integer id) {
		return recommendResourcesMapper.getResourceById(id);
	}

	@Override
	public RecommendResources getNextResources(RecommendResources recommendResources) {
		return recommendResourcesMapper.getNextResources(recommendResources);
	}

	@Override
	public RecommendResources getPreResources(RecommendResources recommendResources) {
		return recommendResourcesMapper.getPreResources(recommendResources);
	}


}
