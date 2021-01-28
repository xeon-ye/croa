package com.qinfei.qferp.service.impl.news;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.news.News;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.news.NewsMapper;
import com.qinfei.qferp.service.news.INewsService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class NewsService implements INewsService{

	public static final String CACHE_KEY = "NEWS";

	@Autowired
	private NewsMapper  newsMapper;


	private void validateNews(News news){
		if(ObjectUtils.isEmpty(news.getTitle())){
			throw new QinFeiException(10001,"新闻标题不能为空");
		}
		if(ObjectUtils.isEmpty(news.getContent())){
			throw new QinFeiException(10001,"新闻内容不能为空");
		}
		if(ObjectUtils.isEmpty(news.getType())){
			throw new QinFeiException(10001,"标签类型不能为空");
		}

	}

	@Override
	@Transactional
	public boolean save(News news) {
		User user = AppUtil.getUser();
		validateNews(news);
		news.setCreateTime(new Date());
		news.setUpdateTime(new Date());
		news.setIsDelete(1);
		news.setCompanyCode(user.getCompanyCode());
		news.setCreateId(user.getId());
		news.setUpdateId(user.getId());
		return newsMapper.insert(news)>0;
	}

	@Override
	@Transactional
	public boolean update(News news) {
		if(ObjectUtils.isEmpty(news.getId())){
			throw new QinFeiException(1002,"新闻唯一标识不能为空");
		}
		if(ObjectUtils.isEmpty(news.getType())){
			throw new QinFeiException(1002,"新闻类型不能为空");
		}
		News  oldnNews = newsMapper.get(News.class,news.getId());
		if(news.getType().equals(3)){
			oldnNews.setImageUrl(news.getImageUrl());
		}
        oldnNews.setContent(news.getContent());
        oldnNews.setTitle(news.getTitle());
        oldnNews.setType(news.getType());
        oldnNews.setUpdateId(AppUtil.getUser().getId());
        oldnNews.setUpdateTime(new Date());
		return newsMapper.update(oldnNews)>0;
	}

	@Override
	public List<News> getNewsByDto(News news) {
		//非集团用户查看所有的新闻
		User user = AppUtil.getUser();
		if(!"JT".equalsIgnoreCase(user.getCompanyCode())){
			//查看本公司新闻
			news.setCompanyCode(user.getCompanyCode());
			//查看类型为集团新闻
		}
		news.setIsDelete(1);
		return newsMapper.getNewsByNews(news);
	}

	@Override
	public int getAdminNewsCount(Map<String, Object> param) {
		int result = 0;
		User user = AppUtil.getUser();
		if(user != null){
			if(!"JT".equalsIgnoreCase(user.getCompanyCode())){
				param.put("companyCode", user.getCompanyCode());
			}
			result = newsMapper.getAdminNewsCount(param);
		}
		return result;
	}

	@Override
	public List<News> getAdminNewsByDto(News news) {
		//用户管理自有新闻的新闻
		User user = AppUtil.getUser();
		if(!"JT".equalsIgnoreCase(user.getCompanyCode())){
			news.setCompanyCode(user.getCompanyCode());
		}
		news.setIsDelete(1);
		return newsMapper.getAdminNewsByNews(news);
	}

	@Override
	@Transactional
	public boolean deleteById(Integer id) {
		News  oldnNews = newsMapper.get(News.class,id);
		oldnNews.setIsDelete(0);
		return newsMapper.update(oldnNews)>0;
	}

	@Override
//	@Cacheable(value = CACHE_KEY, key = "'type=' + #type")
	public List<News> listByType(Integer type) {
		//非集团用户查看所有的新闻
		User user = AppUtil.getUser();
		News news = new News();
		if(1!=type){
			if(!"JT".equalsIgnoreCase(user.getCompanyCode())) {
				news.setCompanyCode(user.getCompanyCode());
			}
		}
		news.setType(type);
		news.setIsDelete(1);
		return newsMapper.list(news);
	}

	@Override
	public News queryById(Integer id) {
		return newsMapper.get(News.class,id);
	}

	@Override
	public News getPreNews(News news) {
		return newsMapper.getPreNews(news);
	}

	@Override
	public News getNextNews(News news) {
		return  newsMapper.getNextNews(news);
	}

	@Override
	public News getAdminPreNews(News news) {
		return newsMapper.getAdminPreNews(news);
	}

	@Override
	public News getAdminNextNews(News news) {
		return newsMapper.getAdminNextNews(news);
	}

	@Override
	public void batchDel(List<Integer> ids) {
		try{
			User user = AppUtil.getUser();
			if(user == null){
				throw new QinFeiException(1002, "请先登录！");
			}
			if(CollectionUtils.isEmpty(ids)){
				throw new QinFeiException(1002, "没有选择需要删除的新闻！");
			}
			newsMapper.batchUpdateStateByIds((byte) 0, user.getId(), ids);
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			throw new QinFeiException(1002, "新闻删除异常，请联系技术人员！");
		}
	}
}
