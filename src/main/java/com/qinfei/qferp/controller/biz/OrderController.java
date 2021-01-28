package com.qinfei.qferp.controller.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.biz.IArticleService;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.service.sys.IRoleService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.FloatUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * 订单管理
 */
@Slf4j
@Controller
@RequestMapping("order")
class OrderController {
	@Autowired
	IOrderService orderService;
	@Autowired
	IArticleService articleService;
	@Autowired
	IRoleService roleService;
	@Autowired
	IDictService dictService;
	@Autowired
	IUserService userService;

	/**
	 * 通过id获取订单
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
//	@Log(opType = OperateType.QUERY, module = "通过id获取订单", note = "通过id获取订单")
	@ResponseBody
	public ResponseData get(@PathVariable("id") Integer id) {
		ResponseData responseData = null;
		try {
			Order order = orderService.get(id);
			order.setArticles(articleService.listByOrderId(id));
			responseData = ResponseData.ok();
			responseData.putDataValue("order", order);
		} catch (Exception e) {
			responseData = ResponseData.customerError(1001, e.getMessage());
		}
		return responseData;
	}

	/**
	 * 根据订单ID查询订单信息
	 *
	 * @param id
	 * @param mv
	 * @return
	 */
	@GetMapping("/getById/{id}")
//	@Log(opType = OperateType.QUERY, module = "根据订单ID查询订单信息", note = "根据订单ID查询订单信息")
	public ModelAndView get(@PathVariable("id") Integer id, ModelAndView mv) {
		try {
			Order order = orderService.get(id);
			if (order == null) {
				mv.setViewName("redirect:media1/businessOrder");
				return mv;
			}
			mv.addObject("order", order);
			Integer userId = AppUtil.getUser().getId();
			boolean flag = roleService.isRole(userId, IConst.ROLE_TYPE_YW);
			/*List<Cust> custs = null;
			List<DockingPeople> dockpeoples = null;
			if (flag) {// 如果是业务员下单只查询自己负责的客户的对接人 和客户
				custs = custService.listByWorker(userId);
//				dockpeoples = dockingPeopleService.listByWorker(userId);
			} else {// 如果是媒介下单就查询所有客户和客户的对接人
				custs = custService.list();
//				dockpeoples = dockingPeopleService.listByWorker(null);
			}*/
			mv.addObject("industryList", dictService.listByTypeCode("industry"));//稿件行业类型
			mv.addObject("order", order);
//			mv.addObject("custs", custs);
//			mv.addObject("dockpeoples", dockpeoples);
			List<Dict> taxes = dictService.listDict("tax");
			mv.addObject("taxes", taxes);

			mv.setViewName("biz/order");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mv;
	}

	/**
	 * 根据订单编号查询订单信息
	 * @param no
	 * @return
	 */
	@GetMapping("/getByNo/{no}")
//	@Log(opType = OperateType.QUERY, module = "根据订单编号查询订单信息", note = "根据订单编号查询订单信息")
	@ResponseBody
	public ResponseData getByNo(@PathVariable("no") String no) {
		ResponseData responseData = null;
		try {
			Order order = orderService.getByNo(no);
			responseData = ResponseData.ok();
			responseData.putDataValue("order", order);
		} catch (Exception e) {
			responseData = ResponseData.customerError(1001, e.getMessage());
		}
		return responseData;
	}

	/**
	 * 通过公司id
	 * @param order
	 * @param pageable
	 * @return
	 */
	@GetMapping("/list")
//	@Log(opType = OperateType.QUERY, module = "查询订单列表", note = "查询订单列表")
	@ResponseBody
	public PageInfo<Order> list(Order order, Pageable pageable) {
		ResponseData responseData = null;
		try {
			PageInfo<Order> list = orderService.list(order, pageable);
			responseData = ResponseData.ok();
			responseData.putDataValue("list", list);
			return list;
		} catch (Exception e) {
			ResponseData.customerError(1001, e.getMessage());
		}
		return null;
	}

	/**
	 * 查询订单列表 订单管理
	 *
	 * @param mv
	 * @return
	 */
	@GetMapping
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "查询订单列表/订单管理", note = "查询订单列表/订单管理")
	public ModelAndView orders(ModelAndView mv) {
		try {
			mv.setViewName("biz/orders");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mv;
	}

	/**
	 * 添加订单
	 * @param order
	 * @return
	 */
	@PostMapping("/add")
	@Log(opType = OperateType.QUERY, module = "添加订单", note = "添加订单")
	@ResponseBody
	public ResponseData add(@RequestBody Order order) {
		ResponseData responseData = null;
		try {
			order.setCreator(AppUtil.getUser().getId());
			orderService.save(order);
			responseData = ResponseData.ok();
			// responseData.putDataValue("order", order);
		} catch (Exception e) {
			e.printStackTrace();
			responseData = ResponseData.customerError(1001, e.getMessage());
		}
		return responseData;
	}

	/**
	 * 添加订单
	 * @param param
	 * @return
	 */
	@PostMapping
	@ResponseBody
	public ResponseData save(@RequestParam("param") String param) {
		try {
			Order order = this.saveOrder(param);
			return  ResponseData.ok().putDataValue("orderId", order.getId());
		} catch (QinFeiException e){
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			return ResponseData.customerError(1002, "生成订单异常！");
		}
	}

	/**
	 * 修改订单信息
	 *
	 * @param order
	 * @return
	 */
	@PutMapping("/update")
	@Log(opType = OperateType.UPDATE, module = "修改订单", note = "修改订单")
	@ResponseBody
	public ResponseData update(@RequestBody Order order) {
		ResponseData responseData = null;
		try {
			orderService.update(order);
			responseData = ResponseData.ok();
		} catch (Exception e) {
			e.printStackTrace();
			responseData = ResponseData.customerError(1002, e.getMessage());
		}
		return responseData;
	}

	/**
	 * 添加
	 * @param param
	 * @return
	 */
	private Order saveOrder(@RequestParam("param") String param) {
		try{
			User user = AppUtil.getUser();
			if(user == null){
				throw new QinFeiException(1002, "请先登录！");
			}
			Order order = new Order();
			JSONObject json = JSON.parseObject(param);
			Object numObj = json.get("num");
			double totalPrice = 0.0;
			if (numObj instanceof JSONArray) {
				JSONArray nums = json.getJSONArray("num");
				JSONArray mediaIds = json.getJSONArray("mediaId");
				JSONArray mediaNames = json.getJSONArray("mediaName");
				JSONArray supplierIds = json.getJSONArray("supplierId");
				JSONArray supplierNames = json.getJSONArray("supplierName");
				JSONArray mediaUserIds = json.getJSONArray("mediaUserId");
				JSONArray mediaUserNames = json.getJSONArray("mediaUserName");
				JSONArray priceTypes = json.getJSONArray("priceType");// 价格类型
				JSONArray priceColumns = json.getJSONArray("priceColumn");// 价格类型列名
				JSONArray payAmounts = json.getJSONArray("payAmount");// 单个应付价(=成本价*折扣率)
				JSONArray prices = json.getJSONArray("price");// 报价
				JSONArray mediaTypeIds = json.getJSONArray("mediaTypeId");
				JSONArray mediaTypeNames = json.getJSONArray("mediaTypeName");
				JSONArray supplierContactors = json.getJSONArray("supplierContactor");
				JSONArray unitPrices = json.getJSONArray("unitPrice");
				for (int i = 0, len = nums.size(); i < len; i++) {
					Article article = new Article();
					Integer mediaId = mediaIds.getInteger(i);
					article.setMediaId(mediaId);// 媒体ID
					String mediaName = mediaNames.getString(i);
					article.setMediaName(mediaName);// 媒体名称
					Double price = prices.getDouble(i);// 销售价，客户报价
					String priceType = priceTypes.getString(i);
					article.setPriceType(priceType);// 价格类型
					String priceColumn = priceColumns.getString(i);
					article.setPriceColumn(priceColumn);// 价格类型列名
					Integer num = nums.getInteger(i);
					article.setNum(num);// 数量
					Double unitPrice = unitPrices.getDouble(i);//单价 = 媒体供应商价格
					article.setUnitPrice(unitPrice);
					Double payAmount = payAmounts.getDouble(i);// 应付价(=成本价*数量*折扣率)
					payAmount *= num;
					article.setPayAmount(payAmount);// 应付价
					article.setOutgoAmount(payAmount);
					article.setSaleAmount(price);// 销售价，客户报价，不含税（=成本价*数量*折扣率*利润率）
					Integer supplierId = supplierIds.getInteger(i);
					article.setSupplierId(supplierId);// 供应商ID
					String supplierName = supplierNames.getString(i);
					article.setSupplierName(supplierName);// 供应商NAME
					Integer mediaUserId = mediaUserIds.getInteger(i);
					article.setMediaUserId(mediaUserId);// 媒介人员ID
					String mediaUserName = mediaUserNames.getString(i);
					article.setMediaUserName(mediaUserName);// 媒介人员姓名
					article.setMediaTypeId(mediaTypeIds.getInteger(i));
					article.setMediaTypeName(mediaTypeNames.getString(i));
					article.setSupplierContactor(supplierContactors.getString(i));
					order.getArticles().add(article);
					totalPrice = FloatUtil.add(price, totalPrice);
				}
			} else {// 添加单个
				Article article = new Article();
				Integer mediaId = json.getInteger("mediaId");
				article.setMediaId(mediaId);// 媒体ID
				String mediaName = json.getString("mediaName");
				article.setMediaName(mediaName);// 媒体名称
				Double price = json.getDouble("price");// 销售价，客户报价
				Double payAmount = json.getDouble("payAmount");// 单个应付价
				String priceType = json.getString("priceType");
				article.setPriceType(priceType);// 价格类型
				String priceColumn = json.getString("priceColumn");
				article.setPriceColumn(priceColumn);// 价格类型列名
				Integer num = json.getInteger("num");
				article.setNum(num);// 数量
				Double unitPrice = json.getDouble("unitPrice") ;
				article.setUnitPrice(unitPrice);
				payAmount *= num;
				article.setPayAmount(payAmount);// 应付价
				article.setOutgoAmount(payAmount);
				article.setSaleAmount(price);// 销售价，客户报价
				Integer supplierId = json.getInteger("supplierId");
				article.setSupplierId(supplierId);// 供应商ID
				String supplierName = json.getString("supplierName");
				article.setSupplierName(supplierName);// 供应商NAME
				Integer mediaUserId = json.getInteger("mediaUserId");
				article.setMediaUserId(mediaUserId);// 媒介人员ID
				String mediaUserName = json.getString("mediaUserName");
				article.setMediaUserName(mediaUserName);// 媒介人员姓名
				article.setMediaTypeId(json.getInteger("mediaTypeId"));
				article.setMediaTypeName(json.getString("mediaTypeName"));
				article.setSupplierContactor(json.getString("supplierContactor"));
				order.getArticles().add(article);
				totalPrice = FloatUtil.add(price, totalPrice);
			}
			order.setAmount((float) totalPrice);
			orderService.save(order);
			return order;
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			e.printStackTrace();
			throw new QinFeiException(1002, "生成订单异常！");
		}
	}

	/**
	 * 删除订单
	 *
	 * @param orderId
	 * @return
	 */
	@GetMapping("del/{orderId}")
//	@Log(opType = OperateType.UPDATE, module = "删除订单", note = "删除订单")
	@ResponseBody
	public ResponseData del(@PathVariable("orderId") Integer orderId) {
		try{
			orderService.delById(orderId);
			return ResponseData.ok();
		}catch (QinFeiException e){
			return ResponseData.customerError(e.getCode(), e.getMessage());
		}catch (Exception e){
			return ResponseData.customerError(1002, "取消订单异常！");
		}
	}

	/**
	 * 媒体方案导出；
	 *
	 * @param params：报价数据；
	 */
	@RequestMapping(value = "dataExport")
	@ResponseBody
	@Log(opType = OperateType.QUERY, note = "媒体方案导出", module = "订单管理/媒体方案导出")
	public ResponseData dataExport(@RequestParam("params") String params) {
		ResponseData data = ResponseData.ok();
		String fileName = orderService.dataExport(params);
		if (StringUtils.isEmpty(fileName)) {
			data.putDataValue("message", "没有找到相关数据，请检查。");
		} else {
			data.putDataValue("file", fileName);
		}
		return data;
	}

	@PostMapping("listOrderByNotPlaced")
	@ResponseBody
	public PageInfo<Map<String, Object>> listOrderByNotPlaced(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
		return orderService.listOrderByNotPlaced(param, pageable);
	}

}