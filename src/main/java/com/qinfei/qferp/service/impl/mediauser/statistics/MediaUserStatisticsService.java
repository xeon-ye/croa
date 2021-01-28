package com.qinfei.qferp.service.impl.mediauser.statistics;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.media.SupplierMapper;
import com.qinfei.qferp.mapper.mediauser.MediaUserStatisticsMapper;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.service.mediauser.statistics.IMediaUserStatisticsService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MediaUserStatisticsService extends BaseService implements IMediaUserStatisticsService {

	public static final String cacheKey = "mediaUserStatistics";

	@Autowired
	MediaUserStatisticsMapper mediaUserStatisticsMapper;
	@Autowired
	private SupplierMapper supplierMapper;
    @Autowired
    private DeptZwMapper deptZwMapper;
    @Autowired
    private IUserService userService;
    @Value("${media.onlineTime}")
    private String onlineTime;

	@Override
	public List<Map> listSupplierByMediaUser(Map map) {
		this.addSecurityByFore(map);
		List<Map> mediaSupplierList = new ArrayList<>();
		map.put("onlineTime",onlineTime);
		//查询新的供应商媒体
        List<Map> newMediaSupplierList = supplierMapper.listSupplierByMediaUser(map);
        for (Map data : newMediaSupplierList) {
            if(data.get("phone") != null && !org.springframework.util.StringUtils.isEmpty(String.valueOf(data.get("phone")))){
                String phone = EncryptUtils.decrypt(String.valueOf(data.get("phone")));
                data.put("phone", phone);
            }
        }
        mediaSupplierList.addAll(newMediaSupplierList);
        //查询旧的供应商
        List<Map> oldMedidSupplierList = supplierMapper.oldlistSupplierByMediaUser(map);
        mediaSupplierList.addAll(oldMedidSupplierList);

		return  mediaSupplierList;
	}

	// @Cacheable(value=cacheKey)
	public Map<String, Object> mediaUserResult(Map map) {
		this.addSecurityByFore(map);
		Map mediaUserStatistics = mediaUserStatisticsMapper.getMediaUserStatisticsByParam(map);
		List<Map> trend = mediaUserStatisticsMapper.listMediaUserTrendStatisticsByParam(map);//按时间分组获取媒介趋势图
		List<Map> mediaTypeRate = mediaUserStatisticsMapper.listMediaUserMediaTypeStatisticsByParam(map);
        List<Map> artTypeRate = mediaUserStatisticsMapper.listMediaUserArtTypeStatisticsByParam(map);
		Map<String,Object> result = new HashMap<>();
		result.put("mediaUserStatistics",mediaUserStatistics);
		result.put("trend",trend);
		result.put("mediaTypeRate",mediaTypeRate);
        result.put("artTypeRate",artTypeRate);
		return result; //mediaUserStatisticsMapper.mediaUserResult(map);
	}

    @Override
    public Map<String, Object> zwMediaUserResult(Map map) {
//        this.addSecurityByFore(map);
        Map<String,Object> result = new HashMap<>();
        try{
            map.put("deptCode", "MJ");
            this.addSecurityByZw(map);
            Map mediaUserStatistics = mediaUserStatisticsMapper.getMediaUserStatisticsByParam(map);
            List<Map> trend = mediaUserStatisticsMapper.listMediaUserTrendStatisticsByParam(map);//按时间分组获取媒介趋势图
            result.put("mediaUserStatistics",mediaUserStatistics);
            result.put("trend",trend);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result; //mediaUserStatisticsMapper.mediaUserResult(map);
    }

    @Override
    public PageInfo<Map> listMediaUserMediaStatisticsByParam(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        this.addSecurityByFore(map);//权限添加
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(mediaUserStatisticsMapper.listMediaUserMediaStatisticsByParam(map));
    }

    @Override
    public void mediaStatisticsRankingExport(HttpServletResponse response, Map map) {
        this.addSecurityByFore(map);
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("媒体列表.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            List<Map> list = mediaUserStatisticsMapper.listMediaUserMediaStatisticsByParam(map);
            String[] heads = {"媒体名称", "板块名称", "稿件数量", "供应商数量", "报价金额", "合作金额", "请款金额","已支付金额", "未支付金额","利润"};
            String[] fields={"mediaName","mediaTypeName","articleNum","supplierNum","saleAmount","payAmount","applyAmount","outgoAmount","unpaid","profit"};
            ExcelUtil.exportExcel("媒体列表",heads, fields, list, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if("articleNum".equals(field) || "supplierNum".equals(field) || "saleAmount".equals(field) || "payAmount".equals(field)
                        || "applyAmount".equals(field) || "outgoAmount".equals(field) || "unpaid".equals(field) || "profit".equals(field)){
                    if(value != null){
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    }else{
                        cell.setCellValue(0);
                    }
                }else{
                    if(value != null){
                        cell.setCellValue(value.toString());
                    }else{
                        cell.setCellValue("");
                    }
                }
            });
        }catch (Exception e){
            throw new RuntimeException("导出文件失败");
        }
    }

    // @Cacheable(value=cacheKey)
	public List<Map> supplierResult(Map map) {
		this.addSecurityByFore(map);
		return mediaUserStatisticsMapper.supplierResult(map);
	}
}
