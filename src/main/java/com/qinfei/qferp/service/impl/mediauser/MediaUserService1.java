package com.qinfei.qferp.service.impl.mediauser;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.exception.ResultEnum;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleReturnDown;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.media1.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.biz.ArticleMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapperXML;
import com.qinfei.qferp.mapper.biz.ArticleReturnDownMapper;
import com.qinfei.qferp.mapper.media1.*;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.qinfei.qferp.service.mediauser.IMediaUserService1;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class MediaUserService1 extends BaseService implements IMediaUserService1 {

	@Autowired
	ArticleReturnDownMapper articleReturnDownMapper;
	@Autowired
	ArticleMapper articleMapper;
	@Autowired
	ArticleMapperXML articleMapperXML;
	@Autowired
	Media1Mapper media1Mapper;
	@Autowired
	private MediaPlateMapper mediaPlateMapper;
	@Autowired
	private MediaAuditMapper mediaAuditMapper;
	@Autowired
	private MediaExtendAuditMapper mediaExtendAuditMapper;
	@Autowired
	private MediaSupplierPriceAuditMapper mediaSupplierPriceAuditMapper;
	@Autowired
	private MediaSupplierRelateAuditMapper mediaSupplierRelateAuditMapper;
	@Autowired
	private MediaSupplierRelateMapper mediaSupplierRelateMapper;
	@Autowired
	IUserService userService;
	@Autowired
	IOrderService orderService;
	@Autowired
	private IMediaForm1Service mediaForm1Service;
	// 消息推送接口；
	@Autowired
	private IMessageService messageService;

	/**
	 * 驳回
	 * 
	 * @param article
	 */
	@Transactional
	public void turnDown(@RequestParam Article article) {
		try{
			User user = AppUtil.getUser();
			if(user == null){
				throw new QinFeiException(1002, "请先登录！");
			}
			Article art = articleMapper.get(Article.class, article.getId());
			ArticleReturnDown articleReturnDown = JSON.parseObject(JSON.toJSONString(art), ArticleReturnDown.class);
			articleReturnDown.setReturnDownDate(new Date());
			articleReturnDown.setReturnDownUser(user.getId());
			articleReturnDownMapper.insert(articleReturnDown);//储存原稿件一些内容信息
			articleMapper.delete(article);//此处是直接删除稿件，不是修改状态

			Order order = orderService.get(art.getOrderId());
			sendMessage(order, art.getTitle(), false);
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			e.printStackTrace();
			throw new QinFeiException(1002, "稿件驳回异常！");
		}
	}

	/**
	 * 媒介安排
	 * 
	 * @param article
	 * @return
	 */
	@Transactional
	public void arrange(Article article) {
		try{
			User user = AppUtil.getUser();
			if(user == null){
				throw new QinFeiException(1002, "请先登录！");
			}
			//发布状态：0-未下单、1-待安排、2-进行中、3-已驳回、4-已发布
			article.setIssueStates(2);
			articleMapperXML.changeIssueStates(article);
			Order order = orderService.get(article.getOrderId());
			sendMessage(order, article.getTitle(), true);// 安排稿件时给业务员发送消息
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			e.printStackTrace();
			throw new QinFeiException(1002, "稿件安排异常！");
		}
	}

	/**
	 * 媒介发布
	 * 
	 * @param map
	 * @param updatePrice
	 * @return
	 */
	@Transactional
	public int publish(Map map, Integer updatePrice) {
		try{
			if (map.get("id") == null) {
				throw new QinFeiException(1002, "稿件ID不存在！");
			}
			if(map.get("outgoAmount") == null){
				throw new QinFeiException(1002, "稿件执行后价格必须填写！");
			}
			Integer id =  Integer.parseInt(String.valueOf(map.get("id")));
			Article fullArticle = articleMapperXML.get(Article.class, id);
			map.put("artId", id);
			map.put("issueStates",4);
			int row = articleMapperXML.updateArticlePublish(map);
			// 需要更新媒体价格
			if (updatePrice != null && 1 == updatePrice) {
				updateMedia(fullArticle);
			}
			Order order = orderService.get(fullArticle.getOrderId());
			// 发布稿件时给业务员发送消息
			sendMessage(order, fullArticle.getTitle(), null);
			return row;
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			throw new QinFeiException(1002,"发布稿件失败");
		}
	}

	/**
	 * 更新媒体相关信息
	 * 
	 * @param fullArticle
	 * @return
	 */
	private void updateMedia(Article fullArticle) {
		User user = AppUtil.getUser();
		// 更新状态使下单的时候查询不到
//		media1Mapper.updateMediaIsDelete(fullArticle.getMediaId(),1,AppUtil.getUser().getId());
		// 得到稿件单价
		BigDecimal articleSign = new BigDecimal(fullArticle.getUnitPrice());//getArticlePayAmount(fullArticle);

		MediaPrice mediaPrice = mediaAuditMapper.getMediaSupplierInfoByMediaId(fullArticle.getMediaId(), fullArticle.getPriceColumn());//获取该媒体对应价格类型的所有价格
		MediaSupplierPriceAudit mediaSupplierPriceAudit = new MediaSupplierPriceAudit();
		MediaExtendAudit mediaExtendAudit = new MediaExtendAudit();
		mediaExtendAudit.setMediaId(mediaPrice.getMediaId());
		mediaExtendAudit.setCell(fullArticle.getPriceColumn());
		//设置供应商价格，并计算媒体最低价进行更新
		if(CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
			BigDecimal minPrice = null; //默认不存在最低价格，下面开始计算最低价格，忽略为0的价格
			for(MediaPriceCell mediaPriceCell: mediaPrice.getMediaPriceCellList()){
				BigDecimal currentPrice = new BigDecimal(String.valueOf(mediaPriceCell.getCellValue()));//mediaPriceCell.getCellValue()
				if(fullArticle.getSupplierId().equals(mediaPriceCell.getSupplierId())){
					mediaSupplierPriceAudit.setMediaSupplierRelateId(mediaPriceCell.getRelateId());
					mediaSupplierPriceAudit.setCell(mediaPriceCell.getCell());
					mediaSupplierPriceAudit.setCellName(mediaPriceCell.getCellName());
					mediaSupplierPriceAudit.setCellValue(articleSign.toPlainString());
					currentPrice = articleSign;
				}
				if((minPrice == null && currentPrice.compareTo(new BigDecimal(0)) > 0) || minPrice.compareTo(currentPrice) > 0){
					minPrice = currentPrice;
				}
			}
			if(minPrice == null){
				minPrice = new BigDecimal(0);
			}
            //先让当前媒体供应商关系不能使用
            mediaSupplierRelateMapper.updateIsDeleteByRelateId(1, user.getId(), mediaSupplierPriceAudit.getMediaSupplierRelateId());
			mediaExtendAudit.setCellValue(minPrice.toPlainString()); //设置媒体最低价格
			mediaExtendAuditMapper.updateMediaOnePrice(mediaExtendAudit); //更新媒体最低价
			mediaSupplierPriceAuditMapper.updateOnePrice(mediaSupplierPriceAudit); //更新对应供应商价格
            mediaSupplierRelateAuditMapper.updateStateById(0, user.getId(), mediaSupplierPriceAudit.getMediaSupplierRelateId());
//			mediaAuditMapper.updateMediaState(fullArticle.getMediaId(), 0, AppUtil.getUser().getId()); //更新媒体状态
		}
	}

	/**
	 * 媒介移交
	 * 
	 * @param artId
	 * @param mediaUserId
	 * @return
	 */
	@Transactional
	public void yj(String artId, Integer mediaUserId,String mediaUserName) {
		try{
			User user = AppUtil.getUser();
			if(user == null){
				throw new QinFeiException(1002, "请先登录！");
			}
			if(mediaUserId == null){
				throw new QinFeiException(1002, "移交媒介不能为空！");
			}
			Map map = new HashMap();
			map.put("artId", artId);
			map.put("mediaUserId", mediaUserId);
			map.put("mediaUserName", mediaUserName);
			articleMapperXML.updateArticle(map);
		}catch (QinFeiException e){
			throw e;
		}catch (Exception e){
			e.printStackTrace();
			throw new QinFeiException(1002, "稿件移交异常！");
		}
	}

	/**
	 * 通过稿件得到稿件的发布单价
	 * 
	 * @param article
	 * @return
	 */
//	private BigDecimal getArticlePayAmount(Article article) {
//		BigDecimal payAmount = media1Mapper.getArticleOutgoAmount(article);
//		BigDecimal num = new BigDecimal(article.getNum());
//		BigDecimal signPayAmout = payAmount.divide(num, 3, RoundingMode.HALF_UP);
//		return signPayAmout;
//	}

	/**
	 * 判断价格浮动是否需要修改媒体的单价
	 * 
	 * @return
	 */
	public boolean priceFloat(Article article) {
		Article fullArticle = articleMapperXML.get(Article.class, article.getId());
		BigDecimal signPayAmout = new BigDecimal(fullArticle.getUnitPrice());//getArticlePayAmount(fullArticle);
		if(signPayAmout == null){
			throw new QinFeiException(1002, "稿件单价不存在！");
		}
		BigDecimal mediaPrice = media1Mapper.getMediaSupplierOnePrice(fullArticle);
		if(mediaPrice == null){
			throw new QinFeiException(1002, "稿件对应媒体价格不存在！");
		}
		// 价格浮动
		BigDecimal priceFloat = signPayAmout.subtract(mediaPrice).abs().divide(mediaPrice, 3, RoundingMode.HALF_UP);
		return priceFloat.compareTo(new BigDecimal(0.05)) == 1;
	}

	@Override
	public void exportTemplate(Map<String, Object> map, OutputStream outputStream) {
		// 查询所有的业务员
		User user = AppUtil.getUser() ;
		Map<String, List<String>> siteMap = new HashMap<>();
		List<MediaForm1> mediaForm1List = mediaForm1Service.listAllPriceType();
		List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(user.getId());
		String[] provNameList = new String[mediaPlateList.size()] ;
		//遍历媒体板块
		for (int i = 0, len = mediaPlateList.size(); i < len; ++i) {
			MediaPlate mediaPlate = mediaPlateList.get(i);
			provNameList[i] = mediaPlate.getName();
			List<String> formList = new ArrayList<>() ;
			//遍历媒体板块价格类型
			if(CollectionUtils.isNotEmpty(mediaForm1List)){
				for(MediaForm1 mediaForm : mediaForm1List){
					if(mediaForm.getMediaPlateId().equals(mediaPlate.getId())){
						//报纸把标题费用去掉
						if(mediaForm.getMediaPlateId() == 3 && (IConst.NEWSPAPER_TITLE_PRICE.equals(mediaForm.getCellCode()))){
							continue ;
						}
						formList.add(mediaForm.getCellName()) ;
					}
				}
			}
			siteMap.put(mediaPlate.getName(), formList);
		}

		// 创建一个excel
		XSSFWorkbook book = new XSSFWorkbook();
		// 创建需要用户填写的数据页
		// 设计表头
		XSSFSheet sheet1 = book.createSheet("sheet1");
		XSSFCellStyle cellStyle = book.createCellStyle();// 创建提示样式

		XSSFRow row0 = sheet1.createRow(0);
		row0.setHeightInPoints((8 * sheet1.getDefaultRowHeightInPoints()));
		cellStyle.setWrapText(true);
		String info = "提示：1、有*号的列，必填，有*号的列为空时不会导入，导入不成功的会有弹框提示。\n"
				+ "2、媒体板块、价格类型建议通过下拉框选择；复制进去的内容必须是下拉框中已有的内容。\n"
				+ "3、发布日期格式为“yyyy/MM/dd”,例如：2018/01/12。\n" +
				"4、链接修改为必填。\n" +
				"5、去除单价列,单价由公式自动计算：单价=（请款金额-其他费用） ÷ 数量。\n" +
				"6、数量改为非必填，为空时导入后为默认值1。\n" +
				"7、新增稿件的时候可以不绑定供应商，如果供应商信息没找到或不填写也能创建稿件成功，但是需要后期线上编辑绑定。\n" +
				"8、*媒体名称/媒体唯一标识：如果是标准媒体板块则输入唯一标识，如果是非标准媒体板块则输入媒体名称。";
		row0.createCell(0).setCellValue(info);
		row0.getCell(0).setCellStyle(cellStyle);

		CellRangeAddress region = new CellRangeAddress(0, 0, 0, 18);
		sheet1.addMergedRegion(region);

		XSSFCellStyle headerStyle = book.createCellStyle();// 创建标题样式
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		XSSFRow row1 = sheet1.createRow(1);
		row1.setHeightInPoints(30);// 单元格高度
		sheet1.setColumnWidth(0, 3000);
		sheet1.setColumnWidth(1, 4000);
		sheet1.setColumnWidth(2, 3000);
		sheet1.setColumnWidth(3, 4000);
		sheet1.setColumnWidth(4, 3000);
		sheet1.setColumnWidth(5, 3000);
		sheet1.setColumnWidth(6, 5000);
		sheet1.setColumnWidth(7, 5000);
		sheet1.setColumnWidth(8, 5000);
		sheet1.setColumnWidth(9, 3000);
		sheet1.setColumnWidth(10, 4000);
		sheet1.setColumnWidth(11, 2000);
		sheet1.setColumnWidth(12, 3000);
		sheet1.setColumnWidth(13, 3000);
		sheet1.setColumnWidth(14, 5000);
		sheet1.setColumnWidth(15, 4000);
		sheet1.setColumnWidth(16, 3000);

		row1.createCell(0).setCellValue("*媒体板块");
		row1.createCell(1).setCellValue("供应商公司名称");
		row1.createCell(2).setCellValue("供应商联系人");
		row1.createCell(3).setCellValue("联系人手机号");
		row1.createCell(4).setCellValue("*业务员");
		row1.createCell(5).setCellValue("*发布日期");
		row1.createCell(6).setCellValue("*媒体名称/唯一标识");
		row1.createCell(7).setCellValue("*标题");
		row1.createCell(8).setCellValue("*链接");
		row1.createCell(9).setCellValue("*请款金额");
		row1.createCell(10).setCellValue("*价格类型");
		row1.createCell(11).setCellValue("数量");
		row1.createCell(12).setCellValue("其他费用");
		row1.createCell(13).setCellValue("备注");
		row1.createCell(14).setCellValue("电商商家");
		row1.createCell(15).setCellValue("内外部(内部/外部)");
		row1.createCell(16).setCellValue("频道");

		for (int i = 0; i < 17; i++) {
			row1.getCell(i).setCellStyle(headerStyle);
		}

		// 创建一个专门用来存放板块和报价信息的隐藏sheet页
		// 因此也不能在现实页之前创建，否则无法隐藏。
		XSSFSheet hideSheet = book.createSheet("site");
		book.setSheetHidden(book.getSheetIndex(hideSheet), true);

		int rowId = 1;
		// 设置第一行，存媒体类型的信息
		XSSFRow proviRow = hideSheet.createRow(rowId++);
		proviRow.createCell(0).setCellValue("媒体板块");

		String mediaTypeName = null ;
		String mediaFormName = null ;
		Boolean mediaTypeFlag = false ;
		Boolean mediaFormFlag = false ;
		if(mediaPlateList.size() == 1){
			mediaTypeName = mediaPlateList.get(0).getName() ;
			mediaTypeFlag = true ;
			if(CollectionUtils.isNotEmpty(siteMap.get(mediaTypeName)) && siteMap.get(mediaTypeName).size() == 1){
				mediaFormName = siteMap.get(mediaTypeName).get(0);
				mediaFormFlag = true ;
			}
		}
		if(mediaTypeFlag){
			for (int i = 2; i < 12; i++) {
				Row row = sheet1.createRow(i) ;
				row.createCell(0).setCellValue(mediaTypeName);
				if(mediaFormFlag){
					row.createCell(10).setCellValue(mediaFormName);
				}
			}
		}

		// 将具体的数据写入到每一行中，行开头为父级区域，后面是子区域。
		for (Map.Entry<String, List<String>> entry : siteMap.entrySet()) {
			String key = entry.getKey();
			if (!org.springframework.util.StringUtils.isEmpty(key) && !CodeUtil.isStartWithNumber(key)) {
				List<String> son = entry.getValue();
				Row row = hideSheet.createRow(rowId++);
				row.createCell(0).setCellValue(key);
				for (int i = 0; i < son.size(); i++) {
					row.createCell(i+1).setCellValue(son.get(i));
				}
				// 添加名称管理器
				String range = getRange(1, rowId, son.size());
				Name name = book.createName();
				try {
					name.setNameName(key);
					String formula = "site!" + range;
					name.setRefersToFormula(formula);
				} catch (Throwable t) {
//                    book.removeName(name);
				}
			}
		}

		XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet1);

//        参照 省-->市 的两级联动校验，此处是 媒体类型-->价格类型
		// 省规则 (媒体类型)
		DataValidationConstraint provConstraint = dvHelper.createExplicitListConstraint(provNameList);
		CellRangeAddressList provRangeAddressList = new CellRangeAddressList(2, 1001, 0, 0);
		DataValidation mediaTypeValid = dvHelper.createValidation(provConstraint, provRangeAddressList);
		mediaTypeValid.createErrorBox("error", "请选择正确的媒体类型");
		mediaTypeValid.setShowErrorBox(true);
		mediaTypeValid.setSuppressDropDownArrow(true);
		sheet1.addValidationData(mediaTypeValid);

		for (int i = 3; i < 1003; i++) {
			//        Row row2 = sheet1.createRow(i);
			// 市规则，（价格类型）
			// "INDIRECT($A$" + 2 + ")" 表示规则数据会从名称管理器中获取key与单元格 A2 值相同的数据，如果A2是浙江省，那么此处就是
			DataValidationConstraint formula = dvHelper.createFormulaListConstraint("INDIRECT($A$" + i + ")");
			CellRangeAddressList rangeAddressList = new CellRangeAddressList(i - 1, i - 1, 10, 10);
			DataValidation cacse = dvHelper.createValidation(formula, rangeAddressList);
			cacse.createErrorBox("error", "请选择正确的价格类型");
			cacse.setShowErrorBox(true);
			cacse.setSuppressDropDownArrow(true);
			sheet1.addValidationData(cacse);
		}
		try {
			book.write(outputStream);
		} catch (
				Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	/**
	 * @param offset   偏移量，如果给0，表示从A列开始，1，就是从B列
	 * @param rowId    第几行
	 * @param colCount 一共多少列
	 * @return 如果给入参 1,1,10. 表示从B1-K1。最终返回 $B$1:$K$1
	 */
	private String getRange(int offset, int rowId, int colCount) {
		char start = (char) ('A' + offset);
		if (colCount <= 25) {
			char end = (char) (start + colCount - 1);
			if (colCount == 0) {
				// 从B列开始，如果colCount=0，会导致出现B2B1的情况，导致本来没有数据的，把自己放进去了。所以colCount=0时加1，确保为空
				end = (char) (end + 1);
			}
			return "$" + start + "$" + rowId + ":$" + end + "$" + rowId;
		} else {
			char endPrefix = 'A';
			char endSuffix = 'A';
			if ((colCount - 25) / 26 == 0 || colCount == 51) {// 26-51之间，包括边界（仅两次字母表计算）
				if ((colCount - 25) % 26 == 0) {// 边界值
					endSuffix = (char) ('A' + 25);
				} else {
					endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
				}
			} else {// 51以上
				if ((colCount - 25) % 26 == 0) {
					endSuffix = (char) ('A' + 25);
					endPrefix = (char) (endPrefix + (colCount - 25) / 26 - 1);
				} else {
					endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
					endPrefix = (char) (endPrefix + (colCount - 25) / 26);
				}
			}
			return "$" + start + "$" + rowId + ":$" + endPrefix + endSuffix + "$" + rowId;
		}
	}

	/**
	 * 消息推送；
	 *
	 * @param order：订单信息；
	 * @param title：稿件标题；
	 * @param agree：是否同意；
	 */
	private void sendMessage(Order order, String title, Boolean agree) {
		User user = AppUtil.getUser();
		String operateUser = user.getName();
		Integer userId = user.getId();
		Integer acceptId = order.getCreator();

		String subject;
		String content;
		if (agree == null) {
			subject = "稿件发布完成。";
			content = String.format("[业务下单]恭喜你，你提交的稿件[%s]已经由[%s]发布完毕。", title, operateUser);
		} else {
			if (agree) {
				subject = "稿件安排完成。";
				content = String.format("[业务下单]恭喜你，你提交的稿件[%s]已经由[%s]安排完毕。", title, operateUser);
			} else {
				subject = "稿件已被驳回。";
				content = String.format("[业务下单]很遗憾，你提交的稿件[%s]在[%s]处审核未通过。", title, operateUser);
			}
		}

		// 推送WebSocket消息；
		WSMessage message = new WSMessage();
		message.setReceiveUserId(acceptId + "");
		message.setReceiveName(order.getUserName());
		message.setSendName(operateUser);
		message.setSendUserId(userId + "");
		message.setSendUserImage(user.getImage());
		message.setContent(content);
		message.setSubject(subject);
		WebSocketServer.sendMessage(message);

		// 推送系统的消息；
		Message newMessage = new Message();
		String userImage = user.getImage();
		// 获取消息显示的图片；
		String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
		newMessage.setPic(pictureAddress);
		newMessage.setContent(content);
		newMessage.setInitiatorDept(user.getDeptId());
		newMessage.setInitiatorWorker(userId);
		newMessage.setAcceptDept(order.getDepatId());
		newMessage.setAcceptWorker(acceptId);
		//消息分类
		newMessage.setParentType(3);//通知
		newMessage.setType(24);//业务下单
		newMessage.setUrl(null);
		newMessage.setUrlName(null);
		messageService.addMessage(newMessage);
	}
}
