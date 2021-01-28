package com.qinfei.qferp.service.impl.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.biz.ArticleMapperXML;
import com.qinfei.qferp.mapper.biz.OrderMapper;
import com.qinfei.qferp.service.biz.IArticleService;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DataImportUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional
public class OrderService implements IOrderService {
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	IArticleService articleService;
	@Autowired
	ArticleMapperXML articleMapperXML;
	// 获取配置；
	@Autowired
	private Config config;

	@Override
	@Transactional
	public Order save(Order order) {
		try {
			order.setNo("MTGJ" + UUIDUtil.get16UUID().toUpperCase());
			order.setCreateDate(new Date());
			order.setState(0);
			Integer userId = AppUtil.getUser().getId();
			order.setCreator(userId);
			order.setUpdateUserId(userId);
			orderMapper.insert(order);
			order.getArticles().forEach(article -> {
				article.setOrderId(order.getId());
				article.setCreator(userId);
				article.setCreateTime(new Date());
				article.setUpdateUserId(userId);
			});
			articleService.saveBatch(order.getArticles());
			return order;
		} catch (QinFeiException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public Order insert(Order order) {
		orderMapper.insert(order);
		return order;
	}

	@Override
	public Order update(Order order) {
		order.setState(1);
		order.setUpdateUserId(AppUtil.getUser().getId());
		orderMapper.update(order);
		//设置稿件行业类型 和 品牌
		order.getArticles().forEach(article -> {
			article.setTypeCode(order.getTypeCode());
			article.setTypeName(order.getTypeName());
			article.setBrand(order.getBrand());
		});
		articleService.updateBatch(order.getArticles());
		return order;
	}

	@Override
	@Cacheable(value = CACHE_KEY, key = "'orderId='+#id")
	@Transactional(readOnly = true)
	public Order get(Integer id) {
		return orderMapper.get(Order.class, id);
	}

	@Override
	public Order getByNo(String no) {
		return orderMapper.getByNo(no);
	}

	@Override
	public PageInfo<Order> list(Order order, Pageable pageable) {
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
		List<Order> list = orderMapper.search(order);
        return new PageInfo(list);
	}

	@Override
	public List<Order> listByUserId(Integer userId) {
		return null;
	}

	/**
	 * 媒体方案数据导出；
	 *
	 * @param params：稿件信息；
	 * @return ：文件名称；
	 */
	@Override
	public String dataExport(String params) {
		JSONObject json = JSON.parseObject(params);
		Object numObj = json.get("num");
		// 存储数据内容；
		List<Object[]> exportData = new ArrayList<>();
		if (numObj instanceof JSONArray) {
			// 媒体类型；
			JSONArray mediaTypeName = json.getJSONArray("mediaTypeName");
			// 媒体名称；
			JSONArray mediaNames = json.getJSONArray("mediaName");
			// 价格类型
			JSONArray priceColumns = json.getJSONArray("priceType");
			// 报价；
			JSONArray prices = json.getJSONArray("price");
			// 责任人；
			JSONArray mediaUserNames = json.getJSONArray("mediaUserName");
			Object[] articleDatas;
			for (int i = 0; i < mediaTypeName.size(); i++) {
				articleDatas = new Object[5];

				articleDatas[0] = mediaTypeName.getString(i);
				articleDatas[1] = mediaNames.getString(i);
				articleDatas[2] = priceColumns.getString(i);
				articleDatas[3] = prices.getDouble(i);
				articleDatas[4] = mediaUserNames.getString(i);

				exportData.add(articleDatas);
			}
		} else {
			Object[] articleDatas = new Object[5];
			articleDatas[0] = json.getString("mediaTypeName");
			articleDatas[1] = json.getString("mediaName");
			articleDatas[2] = json.getString("priceType");
			articleDatas[3] = json.getDouble("price");
			articleDatas[4] = json.getString("mediaUserName");
			exportData.add(articleDatas);
		}
		// 拼接列头信息；
		List<String> rowTitles = new ArrayList<>();
		rowTitles.add("媒体类型");
		rowTitles.add("媒体名称");
		rowTitles.add("价格类型");
		rowTitles.add("报价");
		rowTitles.add("责任人");

		return DataImportUtil.createFile("媒体报价方案", config.getUploadDir(), config.getWebDir(), rowTitles, exportData);
	}

	@Override
	public PageInfo<Map<String, Object>> listOrderByNotPlaced(Map<String, Object> param, Pageable pageable) {
		List<Map<String, Object>> result = new ArrayList<>();
		try{
			User user = AppUtil.getUser();
			if(user != null){
				param.put("userId", user.getId());
				PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
				result = articleMapperXML.listOrderByNotPlaced(param);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return new PageInfo<>(result);
	}

	/**
	 * 根据订单ID删除订单信息
	 * 
	 * @param orderId
	 * @return
	 */
	@Override
	@Transactional
	@CacheEvict(value = CACHE_KEY, key = "'orderId='+#orderId")
	public void delById(Integer orderId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            orderMapper.delById(Order.class, orderId);
            articleService.deleteArticleByOrderId(orderId);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "取消订单异常！");
        }
	}

	// @Override
	@Transactional
	public boolean updateCustInfo(Integer custId, String custName) {
		orderMapper.updateCustInfo(custId, custName);
		return true;
	}
}
