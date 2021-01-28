package com.qinfei.qferp.service.impl.mediauser.statistics;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.mapper.mediauser.MediaUserManagerStatisticsMapper;
import com.qinfei.qferp.service.mediauser.statistics.IMediaUsereManagerStatisticsService;
import com.qinfei.qferp.utils.PageOrder;
import com.github.pagehelper.PageHelper;

@Service
public class MediaUsereManagerStatisticsService extends BaseService implements IMediaUsereManagerStatisticsService {

	public static final String cacheKey = "mediaUsereManagerStatistics";

	@Autowired
	MediaUserManagerStatisticsMapper mediaUserManagerStatisticsMapper;

	// @Cacheable(value = cacheKey)
	public List<Map> topOptionSetValue(Map map) {
		this.addSecurityByFore(map);
		return mediaUserManagerStatisticsMapper.topOptionSetValue(map);
	}

	/**
	 * 供应商排名
	 * 
	 * @param map
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public List<Map> supplierSort(Map map, Integer pageNumber, Integer pageSize) {
		this.addSecurityByFore(map);
		PageHelper.startPage(pageNumber, pageSize);
		return mediaUserManagerStatisticsMapper.supplierSort(map);
	}

	/**
	 * 请款排名
	 * 
	 * @param map
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public List<Map> outgoSort(Map map, Integer pageNumber, Integer pageSize) {
		this.addSecurityByFore(map);
		PageHelper.startPage(pageNumber, pageSize);
		return mediaUserManagerStatisticsMapper.outgoSort(map);
	}

	/**
	 * 稿件类型分布
	 * 
	 * @param map
	 * @return
	 */
	// @Cacheable(value = cacheKey)
	public List<Map> artTypeFb(Map map) {
		this.addSecurityByFore(map);
		return mediaUserManagerStatisticsMapper.artTypeFb(map);
	}

	/**
	 * 供应商排名统计
	 * 
	 * @param map
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public List<Map> supplierListSort(Map map, Integer pageNumber, Integer pageSize) {
		this.addSecurity(map);
		PageHelper.startPage(pageNumber, pageSize, PageOrder.getOrderStr(map));
		return mediaUserManagerStatisticsMapper.supplierListSort(map);
	}

	/**
	 * 供应商统计信息导出
	 * @param map
	 * @param out
	 * @return
	 */
	@Override
	public List<Map> exportSupplier(Map map, OutputStream out) {
//		this.addSecurity(map);
		this.addSecurityByFore(map);
		User user = AppUtil.getUser();
		List<Map> supplierList = mediaUserManagerStatisticsMapper.listSupplierStatisticsByParam(map);
		String [] titles = {"板块名称", "供应商", "联系人","稿件总数","应付金额", "已付金额","未付金额","请款金额","利润"};
		String [] object = {"mediaTypeName","supplierName","supplierContactor","articleNum","outgoAmount","paid","unpaid","applyAmount","profit"};
		ExcelUtil.exportExcel("供应商统计导出",titles,object,supplierList,out,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value)->{
				if(value!=null){
					if("articleNum".equals(field) || "outgoAmount".equals(field) || "paid".equals(field)  || "unpaid".equals(field) || "applyAmount".equals(field) || "profit".equals(field)){
						if(value != null){
							cell.setCellValue(Double.parseDouble(value.toString()));
						}else{
							cell.setCellValue(0);
						}
					}else if("supplierName".equals(field) || "supplierContactor".equals(field)){
						if(value != null){
							if(user.getDept().getCompanyCode().equals(supplierList.get(rowIndex).get("supplierCompanyCode"))){
								cell.setCellValue(value.toString());
							}else {
								cell.setCellValue(value.toString().substring(0,1)+"****");
							}
						}else {
							cell.setCellValue("");
						}
					}else{
						if(value != null){
							cell.setCellValue(value.toString());
						}else{
							cell.setCellValue("");
						}
					}
				}
		});
		return supplierList;
	}

	@Override
	public Map<String, Object> supplierStatisticsResult(Map map) {
		this.addSecurityByFore(map);
		Map supplierStatistics = mediaUserManagerStatisticsMapper.getSupplierStatisticsByParam(map);
		List<Map> trend = mediaUserManagerStatisticsMapper.listSupplierTrendStatisticsByParam(map);//按时间分组获取供应商趋势图
		List<Map> mediaTypeRate = mediaUserManagerStatisticsMapper.listSupplierMediaTypeStatisticsByParam(map);
		Map<String,Object> result = new HashMap<>();
		result.put("supplierStatistics",supplierStatistics);
		result.put("trend",trend);
		result.put("mediaTypeRate",mediaTypeRate);
		return result;
	}

	@Override
	public PageInfo<Map> listSupplierStatisticsByParam(Map<String, Object> map, Integer pageNum, Integer pageSize) {
		this.addSecurityByFore(map);//权限添加
		PageHelper.startPage(pageNum, pageSize);
		List<Map> list = mediaUserManagerStatisticsMapper.listSupplierStatisticsByParam(map);
		for (Map data : list) {
			if(data.get("phone") != null && !org.springframework.util.StringUtils.isEmpty(String.valueOf(data.get("phone")))){
				String phone = EncryptUtils.decrypt(String.valueOf(data.get("phone")));
				data.put("phone", phone);
			}
		}
		return new PageInfo<>(list);
	}
}
