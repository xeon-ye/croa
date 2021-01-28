package com.qinfei.qferp.service.impl.media1;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.media.MediaInfo;
import com.qinfei.qferp.entity.media1.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.media1.Media1Mapper;
import com.qinfei.qferp.mapper.media1.MediaAuditMapper;
import com.qinfei.qferp.mapper.media1.MediaExtendMapper;
import com.qinfei.qferp.mapper.media1.MediaSupplierPriceMapper;
import com.qinfei.qferp.service.media1.IMedia1Service;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * @CalssName Media1ServiceImpl
 * @Description 媒体表
 * @Author xuxiong
 * @Date 2019/6/26 0026 17:32
 * @Version 1.0
 */
@Service
public class Media1ServiceImpl implements IMedia1Service {
    @Autowired
    private Media1Mapper media1Mapper;
    @Autowired
    private IMediaForm1Service mediaForm1Service;
    @Autowired
    private MediaExtendMapper mediaExtendMapper;
    @Autowired
    private MediaSupplierPriceMapper mediaSupplierPriceMapper;

    @Autowired
    private MediaAuditMapper mediaAuditMapper;

    @Value("${media.onlineTime}")
    private String onlineTime;

    //含有ID字段的媒体板块，后期若添加新的板块，需要维护
    private final static Map<Integer, List<String>> containIdPlateExportCommonFieldMap = new HashMap<Integer, List<String>>(){
        {
            put(0,Arrays.asList("userId","name","mediaContentId","link","discount","remarks"));
            put(1,Arrays.asList("*责任人*","*媒体名称*","*微信ID*","案例链接","折扣率","备注"));
            put(12,Arrays.asList("*责任人*","*媒体名称*","*抖音ID*","案例链接","折扣率","备注"));
            put(445,Arrays.asList("*责任人*","*媒体名称*","*快手ID*","案例链接","折扣率","备注"));
        }
    };
    List<String> mediaExportCommonFieldName = Arrays.asList("*责任人*","*媒体名称*","*案例链接*","折扣率","备注");
    List<String> mediaExportCommonField = Arrays.asList("userId","name","link","discount","remarks");

    @Override
    public PageInfo<Map<String, Object>> listMediaSupplierByParam(Map<String, Object> map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String, Object>> result = media1Mapper.listMediaSupplierByParam(map);
        //对联系电话进行解密
        if(CollectionUtils.isNotEmpty(result)){
            for (Map<String, Object> supplier : result){
                if(supplier.get("phone") != null){
                    supplier.put("phone",EncryptUtils.decrypt(supplier.get("phone").toString()));
                    //获取供应商对应的所有的媒体板块id
                    String plateIds = mediaAuditMapper.selectPlateIdsForSupplierId(Integer.valueOf(supplier.get("supplierId").toString()),onlineTime);
                    supplier.put("plateIds",plateIds);
                }
            }
        }
        return new PageInfo<>(result);
    }

    @Override
    public PageInfo<Map<String, Object>> listMediaByParam(Map<String, Object> map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
        return new PageInfo<>(media1Mapper.listMediaByParam(map));
    }

    @Override
    public List<Map> getMediaSupplierInfoByMediaId(Integer id, String cell) {
        Map param = new HashMap();
        param.put("mediaId",id);
        //当含有这些标识时,如，a<1>b、c<n>d
        // <1>：表示后面的价格是当前价格附属价格，报价 = a * 数量 + b，比如：报纸板块的参考报价和标题费用
        // <n>：表示后面的价格是当前价格附属价格，报价 = c * num + d * num 数量，比如：目前没有
        if(cell.indexOf("<1>") > 0 || cell.indexOf("<n>") > 0 ){
            String a = cell.split("<[1|n]>")[0];
            String b = cell.split("<[1|n]>")[1];
            List<String> cellList = new ArrayList<>();
            cellList.add(cell);
            cellList.add(b+"->"+a);
            param.put("cellList",cellList);
        }else {
            param.put("cell",cell);
        }
        MediaPrice mediaPrice = media1Mapper.getMediaSupplierInfoByMediaId(param);
        List<Map> result = null;
        if(mediaPrice != null && CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
            result = new ArrayList<>();
            Map<Integer, Map> temp = new HashMap<>();
            for(MediaPriceCell mediaPriceCell: mediaPrice.getMediaPriceCellList()){
                if(temp.get(mediaPriceCell.getSupplierId()) == null){
                    Map<String, Object> map = new HashMap<>();
                    map.put("mediaId", mediaPrice.getMediaId());
                    map.put("plateId", mediaPrice.getPlateId());
                    map.put("userId", mediaPrice.getUserId());
                    map.put("picPath", mediaPrice.getPicPath());
                    map.put("discount", mediaPrice.getDiscount());
                    map.put("updateDate", DateUtils.format(mediaPrice.getUpdateDate(),"yyyy-MM-dd HH:mm:ss"));
                    map.put("mediaName", mediaPrice.getMediaName());
                    map.put("companyCode", mediaPrice.getCompanyCode());
                    map.put("supplierId", mediaPriceCell.getSupplierId());
                    map.put("supplierCompanyName", mediaPriceCell.getSupplierCompanyName());
                    map.put("supplierName", mediaPriceCell.getSupplierName());
                    map.put("prices", new ArrayList<MediaPriceCell>());
                    temp.put(mediaPriceCell.getSupplierId(), map);
                }
                ((List<MediaPriceCell>)temp.get(mediaPriceCell.getSupplierId()).get("prices")).add(mediaPriceCell);
            }
            result.addAll(temp.values());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMediaSupplierInfoByMediaId(Integer id) {
        MediaPrice mediaPrice = media1Mapper.getSupplierInfoByMediaId(id, null);
        List<Map<String, Object>> result = null;
        if(mediaPrice != null && CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
            result = new ArrayList<>();
            Map<Integer, Map<String, Object>> temp = new HashMap<>();
            for(MediaPriceCell mediaPriceCell: mediaPrice.getMediaPriceCellList()){
                if(temp.get(mediaPriceCell.getSupplierId()) == null){
                    Map<String, Object> map = new HashMap<>();
                    map.put("mediaId", mediaPrice.getMediaId());
                    map.put("mediaName", mediaPrice.getMediaName());
                    map.put("companyCode", mediaPrice.getCompanyCode());
                    map.put("state", mediaPrice.getState());
                    map.put("supplierRelateState", mediaPriceCell.getSupplierRelateState());
                    map.put("relateId", mediaPriceCell.getRelateId());
                    map.put("supplierId", mediaPriceCell.getSupplierId());
                    map.put("supplierCompanyName", mediaPriceCell.getSupplierCompanyName());
                    map.put("supplierName", mediaPriceCell.getSupplierName());
                    map.put("isCopy", mediaPriceCell.getIsCopy());
                    map.put("copyRemarks", mediaPriceCell.getCopyRemarks());
                    map.put("enabled", mediaPriceCell.getEnabled());
                    map.put("prices", new ArrayList<MediaPriceCell>());
                    temp.put(mediaPriceCell.getSupplierId(), map);
                }
                ((List<MediaPriceCell>)temp.get(mediaPriceCell.getSupplierId()).get("prices")).add(mediaPriceCell);
            }
            result.addAll(temp.values());
        }
        return result;
    }

    @Override
    public PageInfo<Media1> listMedia(Map<String, Object> map, Pageable pageable) {
        List<Media1> result = new ArrayList<>();
        if(map.get("extendParams") != null){
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")),MediaExtendParamJson.class);
            if(CollectionUtils.isNotEmpty(mediaExtendParamJsonList)){
                map.put("extendParams",mediaExtendParamJsonList);
                map.put("extendNum",mediaExtendParamJsonList.size());
            }else{
                map.remove("extendParams");
            }
        }
        handleParam(map); //处理参数
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
        List<Media1> media1List = media1Mapper.listByParamPage(map);
        PageInfo<Media1> pageInfo = new PageInfo<>(media1List);
        if(CollectionUtils.isNotEmpty(media1List)){
            List<Integer> ids = new ArrayList<>();
            for(Media1 media1 : media1List){
                ids.add(media1.getId());
            }
            Map param = new HashMap();
            param.put("ids",ids);
            result = media1Mapper.listByParam(param);
            calMediaFT(ids, result); //计算媒体的复投率
        }
        pageInfo.setList(result);
        return pageInfo;
    }

    //计算媒体的复投率
    private void calMediaFT(List<Integer> mediaIdList, List<Media1> result){
        //获取到媒体信息才进行操作，由于每个媒体都要统计近三月、近半年、近一年，所以获取近一年的所有，然后分类计算
        if(CollectionUtils.isNotEmpty(result)){
            Map<String, Object> param = new HashMap<>();
            param.put("timeQuantum", "3");
            handleParam(param); //处理参数
            param.put("mediaIdList", mediaIdList);
            List<Map<String, Object>> mediaFTList = media1Mapper.listMediaFT(param);
            if(CollectionUtils.isNotEmpty(mediaFTList)){
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = Calendar.getInstance().get(Calendar.MONTH);
                int treeMonthStart = month - 2; //近三月开始
                int sixMonthStart = month - 5; //近半年开始
                calendar.set(year, treeMonthStart, 1,0,0,0);
                calendar.set(Calendar.MILLISECOND, 0);//毫秒也设置为0
                Date threeStartDate = calendar.getTime();
                calendar.set(year, sixMonthStart, 1,0,0,0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date sixStartDate = calendar.getTime();
                Map<Integer, Map<String, Integer>> ftGroup = new HashMap<>();
                for(Map<String, Object> record : mediaFTList){
                    Integer mediaId = Integer.parseInt(String.valueOf(record.get("mediaId")));
                    Date recordDate = DateUtils.parse(String.format("%s-01",String.valueOf(record.get("time"))), "yyyy-MM-dd");
                    int artNum = Integer.parseInt(String.valueOf(record.get("artNum")));
                    if(!ftGroup.containsKey(mediaId)){
                        Map<String, Integer> ftRecord = new HashMap<>();
                        ftRecord.put("three", 0);
                        ftRecord.put("six", 0);
                        ftRecord.put("year", 0);
                        ftGroup.put(mediaId, ftRecord);
                    }
                    //如果当前大于近三月的最小月，则累加
                    if(recordDate.compareTo(threeStartDate) >= 0){
                        ftGroup.get(mediaId).put("three", ftGroup.get(mediaId).get("three") + artNum);
                    }
                    //如果当前大于近半年的最小月，则累加
                    if(recordDate.compareTo(sixStartDate) >= 0){
                        ftGroup.get(mediaId).put("six", ftGroup.get(mediaId).get("six") + artNum);
                    }
                    //近一年累加
                    ftGroup.get(mediaId).put("year", ftGroup.get(mediaId).get("year") + artNum);
                }
                for(Media1 media1 : result){
                    media1.setFtRecord(ftGroup.get(media1.getId()));
                }
            }
        }
    }

    @Override
    public void batchExport(OutputStream outputStream, Map<String, Object> map) {
        List<Map<String, Object>> sheetInfo = new ArrayList<>(); //导出模板数据
        map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
        try{
            int total = media1Mapper.getMediaCountByParam(map);
            if(total > 0){
                if(map.get("extendParams") != null){
                    List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")),MediaExtendParamJson.class);
                    if(CollectionUtils.isNotEmpty(mediaExtendParamJsonList)){
                        map.put("extendParams",mediaExtendParamJsonList);
                        map.put("extendNum",mediaExtendParamJsonList.size());
                    }else{
                        map.remove("extendParams");
                    }
                }
                handleParam(map); //处理参数
                List<Media1> media1List = media1Mapper.listByParamPage(map);
                if(CollectionUtils.isNotEmpty(media1List)){
                    //判断筛选数据是否大于总数的95%
                    if(total*0.95 > media1List.size()){
                        List<Integer> ids = new ArrayList<>(); //所有媒体对应ID
                        for(Media1 mediaAudit : media1List){
                            ids.add(mediaAudit.getId());
                        }
                        handleMediaExport(Integer.parseInt(String.valueOf(map.get("plateId"))),String.valueOf(map.get("plateName")),ids,sheetInfo);
                    }else {
                        buildTipExcel("筛选条件太少，查询数据量过多，请增加其他筛选条件后再操作", sheetInfo);
                    }
                }else {
                    buildTipExcel("当前筛选条件下不存在媒体数据", sheetInfo);
                }
            }else {
                buildTipExcel("当前板块不存在媒体数据", sheetInfo);
            }
        }catch (Exception e){
            e.printStackTrace();
            buildTipExcel(e.getMessage(), sheetInfo);
        }
        //导出
        DataImportUtil.createMoreSheetFile(sheetInfo, outputStream);
    }

    @Override
    public List<Map<String, Object>> listMediaFT(Map<String, Object> param) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try{
            validateParam(param); //校验参数
            Calendar calendar = Calendar.getInstance();
            Date endDate = calendar.getTime();
            Date startDate =  handleParam(param); //处理请求参数;
            List<String> allDateList = findDates(startDate, endDate);
            List<Map<String, Object>> result = media1Mapper.listMediaFTByMediaId(param);
            Map<String, Object> map = new HashMap<>();
            for (Map<String, Object> ftObj : result){
                map.put(String.valueOf(ftObj.get("time")), ftObj.get("artNum"));
            }
            for(String time : allDateList){
                Map<String, Object> data = new HashMap<>();
                data.put("ftDate", time);
                data.put("artNum", map.get(time));
                resultList.add(data);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultList;
    }

    @Override
    public PageInfo<Map<String, Object>> listMediaFTByPage(Map<String, Object> param, Pageable pageable) {
        try{
            validateParam(param); //校验参数
            handleParam(param); //处理请求参数
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            List<Map<String, Object>> result = media1Mapper.listMediaFTByPage(param);
            return new PageInfo<>(result);
        }catch (Exception e){
            e.printStackTrace();
            return new PageInfo<>();
        }
    }

    @Override
    public void batchFTExport(OutputStream outputStream, Map<String, Object> param) {
        List<Map<String, Object>> sheetInfo = new ArrayList<>(); //导出模板数据
        try{
            validateParam(param); //校验参数
            handleParam(param); //处理请求参数
            List<Map<String, Object>> result = media1Mapper.listMediaFTByPage(param);
            if(CollectionUtils.isNotEmpty(result)){
                String [] heads = null;
                String [] fields = null;
                if("MJ".equals(AppUtil.getUser().getDept().getCode())){
                    String[] heads1 = {"发布日期","媒体名称", "供应商名称", "供应商联系人", "稿件标题", "稿件链接","请款状态","发布状态"};
                    String[] fields1={"ftTime","mediaName","supplierContactor","supplierName","title","link","outgoState","issueState"};
                    heads = heads1;
                    fields = fields1;
                }else {
                    String[] heads2 = {"发布日期","媒体名称", "稿件标题", "稿件链接", "回款状态", "开票状态","发布状态"};
                    String[] fields2={"ftTime","mediaName","title","link","incomeState","invoiceState","issueState"};
                    heads = heads2;
                    fields = fields2;
                }
                ExcelUtil.exportExcel(String.format("%s-媒体复投详情", param.get("mediaName")),heads, fields, result, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                    if(value != null){
                        if("incomeState".equals(field)){
                            if("1".equals(String.valueOf(value))){
                                cell.setCellValue("已回款");
                            }else if("2".equals(String.valueOf(value))){
                                cell.setCellValue("部分回款");
                            }else {
                                cell.setCellValue("未回款");
                            }
                        }else if("invoiceState".equals(field)){
                            if("1".equals(String.valueOf(value))){
                                cell.setCellValue("已开票");
                            }else if("2".equals(String.valueOf(value))){
                                cell.setCellValue("开票中");
                            }else {
                                cell.setCellValue("未开票");
                            }
                        }else if("issueState".equals(field)){
                            if("4".equals(String.valueOf(value))){
                                cell.setCellValue("已发布");
                            }else if("3".equals(String.valueOf(value))){
                                cell.setCellValue("已驳回");
                            }else if("2".equals(String.valueOf(value))){
                                cell.setCellValue("进行中");
                            }else if("1".equals(String.valueOf(value))){
                                cell.setCellValue("待安排");
                            } else {
                                cell.setCellValue("未下单");
                            }
                        }else if("outgoState".equals(field)){
                            if("1".equals(String.valueOf(value))){
                                cell.setCellValue("已请款");
                            }else if("2".equals(String.valueOf(value))){
                                cell.setCellValue("请款中");
                            }else {
                                cell.setCellValue("未请款");
                            }
                        }else {
                            cell.setCellValue(String.valueOf(value));
                        }
                    }else {
                        cell.setCellValue("");
                    }
                });
            }else {
                buildTipExcel("没有复投记录", sheetInfo);
            }
        }catch (Exception e){
            buildTipExcel(e.getMessage(), sheetInfo);
            DataImportUtil.createMoreSheetFile(sheetInfo, outputStream);
        }
    }

    //校验查询条件
    private void validateParam(Map<String, Object> param){
        if(param.get("timeQuantum") == null){
            throw new QinFeiException(1002, "请选择统计时间");
        }
        if(param.get("mediaId") == null){
            throw new QinFeiException(1002, "媒体ID不存在");
        }
        if(AppUtil.getUser() == null){
            throw new QinFeiException(1002, "请先登录");
        }
    }

    //封装查询条件
    private Date handleParam(Map<String, Object> param){
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        Date startDate = null;
        if("3".equals(param.get("timeQuantum"))){
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) - 11;
            calendar.set(year, month, 1);
            startDate = calendar.getTime();
        }else if("2".equals(param.get("timeQuantum"))){
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) - 5;
            calendar.set(year, month, 1);
            startDate = calendar.getTime();
        }else{
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) - 2;
            calendar.set(year, month, 1);
            startDate = calendar.getTime();
        }
        param.put("startDate", DateUtils.format(startDate, "yyyy-MM-dd"));
        param.put("endDate", DateUtils.format(endDate, "yyyy-MM-dd"));
        return startDate;
    }

    //获取时间段内的所有日期
    private List<String> findDates(Date startDate, Date endDate) {
        String pattern = "yyyy-MM";
        if(startDate.compareTo(endDate) > 0){
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        result.add(DateUtils.format(startDate, pattern)); //将开始时间保存
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        Date currentDate = startDate;
        while (currentDate.compareTo(endDate) < 0){
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            currentDate = calendar.getTime(); //重新设置日期
            String dateStr = DateUtils.format(calendar.getTime(), pattern);
            if(!result.contains(dateStr)){
                result.add(dateStr);
            }
        }
        return result;
    }

    /**
     * 获取指定模板的列标题，区分出扩展中的价格
     * @param plateId 媒体板块
     */
    private Map<String,Object> getMediaExtendFieldsGroupByPrice(Integer plateId){
        List<MediaForm1> extendFormList = mediaForm1Service.listMediaFormByPlateId(plateId);
        List<MediaForm1> extendSupplierList = new ArrayList<>(); //供应商列单独存放，给供应商媒体关系模板使用
        List<MediaForm1> extendNotPriceList = new ArrayList<>(); //媒体非价格列，给媒体模板使用
        List<MediaForm1> extendMediaPriceList = new ArrayList<>(); //仅媒体价格的扩展
        List<MediaForm1> extendMediaList = new ArrayList<>(); //仅媒体价格的扩展
        List<String> templateColumnFieldNames = new ArrayList<>();
        List<String> templateSupplierColumnFieldNames = new ArrayList<>();
        List<String> templateMediaPriceExtendNames = new ArrayList<>(); //仅媒体价格的扩展
        List<String> templateMediaExtendNames = new ArrayList<>(); //仅媒体价格的扩展
        if(CollectionUtils.isNotEmpty(extendFormList)){
            for(MediaForm1 mediaForm1 : extendFormList){
                String cellName = mediaForm1.getCellName();
                // 是否必填；
                if (mediaForm1.getRequired() == 1) {
                    cellName = "*" + cellName + "*";
                }
                //扩展字段标识：0-仅媒体用、1-仅供应商用，对于媒体价格字段，默认供应商也可使用
                if("price".equals(mediaForm1.getType()) || mediaForm1.getExtendFlag() == 1){
                    templateSupplierColumnFieldNames.add(cellName);
                    extendSupplierList.add(mediaForm1);
                }else{
                    templateColumnFieldNames.add(cellName);
                    extendNotPriceList.add(mediaForm1);
                }
                if(mediaForm1.getExtendFlag() == 0 && "price".equals(mediaForm1.getType())){
                    templateMediaPriceExtendNames.add(cellName);
                    extendMediaPriceList.add(mediaForm1);
                }
            }
            extendMediaList.addAll(extendNotPriceList);
            extendMediaList.addAll(extendMediaPriceList);
            templateMediaExtendNames.addAll(templateColumnFieldNames);
            templateMediaExtendNames.addAll(templateMediaPriceExtendNames);
        }
        Map<String,Object> result = new HashMap<>();
        result.put("fieldNames",templateColumnFieldNames);
        result.put("fields",extendNotPriceList);
        result.put("priceFieldNames",templateSupplierColumnFieldNames);
        result.put("priceFields",extendSupplierList);
        result.put("mediaFieldNames",templateMediaExtendNames);
        result.put("mediaFields",extendMediaList);
        return result;
    }

    //处理媒体导出
    private void handleMediaExport(Integer plateId, String plateName, List<Integer> mediaIds, List<Map<String, Object>> sheetInfo){
        //获取板块对应列字段
        Map<String, Object> plateField = getMediaExtendFieldsGroupByPrice(plateId);
        //获取所有媒体基本信息
        Map param = new HashMap();
        param.put("ids",mediaIds);
        List<Media1> mediaAuditList = media1Mapper.listByParam(param);
        List<Map<String, Object>> mediaMapList = new ArrayList<>();  //媒体转换成Map
        if(CollectionUtils.isNotEmpty(mediaAuditList)){
            calMediaFT(mediaIds, mediaAuditList); //计算媒体的复投率
            List<String> mediaRowTitles = new ArrayList<>(); //媒体导出模板列标题
            if(CollectionUtils.isNotEmpty(containIdPlateExportCommonFieldMap.get(plateId))){
                mediaRowTitles.addAll(containIdPlateExportCommonFieldMap.get(plateId));
            }else {
                mediaRowTitles.addAll(mediaExportCommonFieldName);
            }
            mediaRowTitles.addAll((List<String>) plateField.get("mediaFieldNames"));
            mediaRowTitles.add("复投率(近三月)");
            mediaRowTitles.add("复投率(近半年)");
            mediaRowTitles.add("复投率(近一年)");

            List<String> mediaRowTitleFields = new ArrayList<>(); //媒体导出模板列
            if(CollectionUtils.isNotEmpty(containIdPlateExportCommonFieldMap.get(plateId))){
                mediaRowTitleFields.addAll(containIdPlateExportCommonFieldMap.get(0));
            }else {
                mediaRowTitleFields.addAll(mediaExportCommonField);
            }
            List<MediaForm1> mediaForm1List = (List<MediaForm1>) plateField.get("mediaFields");
            for (MediaForm1 mediaForm1 : mediaForm1List){
                mediaRowTitleFields.add(mediaForm1.getCellCode());
            }
            mediaRowTitleFields.add("ftRecord1"); //复投率
            mediaRowTitleFields.add("ftRecord2"); //复投率
            mediaRowTitleFields.add("ftRecord3"); //复投率

            for(Media1 mediaAudit : mediaAuditList){
                Map<String, Object> media = new HashMap<>();
                String userName = "";
                if(mediaAudit.getUser() != null){
                   userName = mediaAudit.getUser().getUserName();
                }
                media.put("userId", userName);
                media.put("name", mediaAudit.getName());
                if(CollectionUtils.isNotEmpty(containIdPlateExportCommonFieldMap.get(plateId))){
                    media.put("mediaContentId", mediaAudit.getMediaContentId());
                    media.put("link", mediaAudit.getLink());
                }else {
                    media.put("link", mediaAudit.getLink());
                }
                media.put("discount", mediaAudit.getDiscount());
                media.put("remarks", mediaAudit.getRemarks());
                for(MediaExtend mediaExtendAudit : mediaAudit.getMediaExtends()){
                    if("select".equals(mediaExtendAudit.getType()) || "radio".equals(mediaExtendAudit.getType()) || "checkbox".equals(mediaExtendAudit.getType())){
                        if(StringUtils.isNotEmpty(mediaExtendAudit.getCellValue())){
                            media.put(mediaExtendAudit.getCell(), mediaExtendAudit.getCellValueText());
                        }else{
                            media.put(mediaExtendAudit.getCell(), "");
                        }
                    }else{
                        media.put(mediaExtendAudit.getCell(), mediaExtendAudit.getCellValue());
                    }
                }
                media.put("ftRecord1",0); //默认值
                media.put("ftRecord2",0); //默认值
                media.put("ftRecord3",0); //默认值
                if(mediaAudit.getFtRecord() != null){
                    if(mediaAudit.getFtRecord().get("three") != null){
                        media.put("ftRecord1",mediaAudit.getFtRecord().get("three"));
                    }
                    if(mediaAudit.getFtRecord().get("six") != null){
                        media.put("ftRecord2",mediaAudit.getFtRecord().get("six"));
                    }
                    if(mediaAudit.getFtRecord().get("year") != null){
                        media.put("ftRecord3",mediaAudit.getFtRecord().get("year"));
                    }
                }
                mediaMapList.add(media);
            }
            List<Object[]> mediaBaseInfo = new ArrayList<>(); //媒体基本信息
            for(Map<String, Object> media : mediaMapList){
                Object[] objects = new Object[mediaRowTitleFields.size()];
                for(int i = 0; i < mediaRowTitleFields.size(); i++){
                    objects[i] = media.get(mediaRowTitleFields.get(i));
                }
                mediaBaseInfo.add(objects);
            }
            Map<String, Object> mediaMap = new HashMap<>();
            mediaMap.put("templateName",plateName+"媒体导出信息");
            mediaMap.put("rowTitles",mediaRowTitles);
            mediaMap.put("exportData",mediaBaseInfo);
            sheetInfo.add(mediaMap);
        }
    }

    //构建提示excel
    private void buildTipExcel(String message, List<Map<String, Object>> sheetInfo){
        Map<String, Object> data = new HashMap<>();
        data.put("templateName","提示");
        List<String> rowTitles = new ArrayList<>();
        rowTitles.add("提示信息");
        data.put("rowTitles", rowTitles);
        Object[] objects = new Object[]{message};
        List<Object[]> exportData = new ArrayList<>();
        exportData.add(objects);
        data.put("exportData",exportData);
        sheetInfo.add(data);
    }

    @Override
    @Transactional
    public String transfer() {
        long startTime = System.currentTimeMillis();
        String result = "";
        Map<Integer, Map<String, MediaForm1>> mediaFormMap = mediaForm1Service.getAllMediaForm();//获取所有表单数据
        Map<Integer, Map<String, String>> mediaFieldRelateMap = mediaForm1Service.getFormRelete(); //媒体表字段对应关系
        List<MediaInfo> mediaList = media1Mapper.listAllOldMedia(); // 获取所有媒体列表
        result += "媒体总数："+mediaList.size();
        //1、媒体扩展字段迁移
        List<MediaExtend> mediaExtendAuditList = new ArrayList<>(); //待迁移的扩展字段
//        List<MediaSupplierRelateAudit> mediaSupplierRelateAuditList = new ArrayList<>(); //待迁移的媒体供应商关系
        List<MediaSupplierPrice> mediaSupplierPriceAuditList = new ArrayList<>(); //待迁移的媒体供应商价格
        int count = 0;
        try{
            for (MediaInfo media : mediaList){
                Map<String, MediaForm1> mediaForm = mediaFormMap.get(media.getmType());
                Map<String, String> mediaFieldRelate = mediaFieldRelateMap.get(media.getmType());
                int cellIndex = 0;
                for(String cell : mediaForm.keySet()){
                    cellIndex ++ ;
                    //媒体扩展字段
                    //关系表统一规则ID，媒体ID + 固定值 + 扩展字段序号，t_media_info迁移同样规则，不然驳回报错，主键ID不对应
                    int id = Integer.parseInt(media.getId() + "0" + cellIndex);
                    MediaExtend mediaExtend = new MediaExtend();
                    mediaExtend.setId(id);
                    mediaExtend.setMediaId(media.getId());
                    mediaExtend.setCell(mediaForm.get(cell).getCellCode());
                    mediaExtend.setCellName(mediaForm.get(cell).getCellName());
                    Object objVal = getFieldValueByName(media, mediaFieldRelate.get(mediaForm.get(cell).getCellName().trim()));
                    String value = "";
                    if(objVal != null){
                        if(objVal instanceof Date){
                            value = DateUtils.format((Date)objVal,"yyyy-MM-dd HH:mm:ss");
                        }else{
                            value = objVal.toString();
                        }
                    }
                    if("select".equals(mediaForm.get(cell).getType()) || "radio".equals(mediaForm.get(cell).getType())){
                        String cellText = mediaForm.get(cell).getCellValueMap().get(value);
                        if(StringUtils.isNotEmpty(cellText)){
                            mediaExtend.setCellValue(value);
                            mediaExtend.setCellValueText(cellText);
                        }else{
                            mediaExtend.setCellValue("");
                        }

                    }else if("checkbox".equals(mediaForm.get(cell).getType())){
                        if(StringUtils.isNotEmpty(value)){
                            String [] arr = value.split("");
                            List<String> texts = new ArrayList<>();
                            boolean flag = true;
                            for(String val : arr){
                                String cellText = mediaForm.get(cell).getCellValueMap().get(val);
                                if(StringUtils.isEmpty(cellText)){  //如果有错误数据，则设置空
                                    flag = false;
                                    break;
                                }
                                texts.add(cellText);
                            }
                            if(flag){
                                mediaExtend.setCellValue(StringUtils.join(arr,","));
                                mediaExtend.setCellValueText(StringUtils.join(texts,","));
                            }else {
                                mediaExtend.setCellValue("");
                            }
                        }
                    }else if("price".equals(mediaForm.get(cell).getType())){
                        value = StringUtils.isEmpty(value) ? "0" : value;
                        mediaExtend.setCellValue(value);
                    }else{
                        mediaExtend.setCellValue(value);
                    }
                    mediaExtend.setType(mediaForm.get(cell).getType());
                    mediaExtend.setDbType(mediaForm.get(cell).getType());
                    mediaExtendAuditList.add(mediaExtend);
                    //媒体价格字段
                    if("price".equals(mediaForm.get(cell).getType())){
                        MediaSupplierPrice mediaSupplierPrice = new MediaSupplierPrice();
                        mediaSupplierPrice.setId(id);
                        mediaSupplierPrice.setMediaSupplierRelateId(media.getId()); //迁移时，关系默认成媒体主键ID
                        mediaSupplierPrice.setCell(mediaForm.get(cell).getCellCode());
                        mediaSupplierPrice.setCellName(mediaForm.get(cell).getCellName());
                        BigDecimal priceValue = StringUtils.isEmpty(value) ? new BigDecimal(0) : new BigDecimal(value);
                        mediaSupplierPrice.setCellValue(priceValue.toPlainString());
                        mediaSupplierPriceAuditList.add(mediaSupplierPrice);
                    }
                }

                //媒体供应商关系
//            MediaSupplierRelateAudit mediaSupplierRelateAudit = new MediaSupplierRelateAudit();
//            mediaSupplierRelateAudit.setId(media.getId());
//            mediaSupplierRelateAudit.setUpdateId(media.getUserId());
//            mediaSupplierRelateAudit.setCreateId(media.getUserId());
//            mediaSupplierRelateAudit.setMediaId(media.getId());
//            mediaSupplierRelateAudit.setSupplierId(media.getSupplierId());
//            mediaSupplierRelateAudit.setIsCopy(0);
                count++;
            }

            // 处理待插入的数据；
            int extendSize = mediaExtendAuditList.size();
            if (extendSize > 0) {
                int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
                int insertTimes = extendSize % subLength == 0 ? extendSize / subLength : extendSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
                if (insertTimes > 1) {
                    List<MediaExtend> insertData;
                    for (int i = 0; i < insertTimes; i++) {
                        insertData = new ArrayList<>();
                        for (int j = i * subLength; j < (i + 1) * subLength && j < extendSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                            insertData.add(mediaExtendAuditList.get(j));
                        }
                        mediaExtendMapper.saveBatchForId(insertData);
                    }
                } else {
                    mediaExtendMapper.saveBatchForId(mediaExtendAuditList);
                }
            }

            //2、媒体供应商关系迁移(采用SQL语句迁移，主键ID设置成媒体ID)
//        mediaSupplierRelateAuditMapper.saveBatch(mediaSupplierRelateAuditList);
            //3、媒体供应商价格迁移
            int supplierPriceSize = mediaSupplierPriceAuditList.size();
            if (supplierPriceSize > 0) {
                int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数。
                int insertTimes = supplierPriceSize % subLength == 0 ? supplierPriceSize / subLength : supplierPriceSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
                if (insertTimes > 1) {
                    List<MediaSupplierPrice> insertData;
                    for (int i = 0; i < insertTimes; i++) {
                        insertData = new ArrayList<>();
                        for (int j = i * subLength; j < (i + 1) * subLength && j < supplierPriceSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                            insertData.add(mediaSupplierPriceAuditList.get(j));
                        }
                        mediaSupplierPriceMapper.saveBatchForId(insertData);
                    }
                } else {
                    mediaSupplierPriceMapper.saveBatchForId(mediaSupplierPriceAuditList);
                }
            }
            long endTime = System.currentTimeMillis();
            result += ", 迁移总数："+count+", 耗时："+((endTime-startTime)/1000)+"秒。";
            return result;
        }catch (Exception e){
            long endTime = System.currentTimeMillis();
            result += ", 当前数量："+count+", 耗时："+((endTime-startTime)/1000)+"秒，出现异常！";
            e.printStackTrace();
            throw new QinFeiException(1002,result);
        }
    }

    /**
     * 根据属性名获取对象属性
     */
    private Object getFieldValueByName(MediaInfo media, String fieldName){
        try {
            Field field = media.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);//设置对象的访问权限，保证对private的属性的访问
            return  field.get(media);
        } catch (Exception e) {
            return new QinFeiException(1002,"老媒体实例中没有该【"+fieldName+"】属性名");
        }
    }

}
