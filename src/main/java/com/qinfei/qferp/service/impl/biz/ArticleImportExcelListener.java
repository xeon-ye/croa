package com.qinfei.qferp.service.impl.biz;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.qinfei.core.mapper.DictMapper;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.media.Supplier;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.biz.ArticleImportMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapper;
import com.qinfei.qferp.mapper.biz.OrderMapper;
import com.qinfei.qferp.mapper.media.MediaInfoMapper;
import com.qinfei.qferp.mapper.media.SupplierMapper;
import com.qinfei.qferp.mapper.media1.Media1Mapper;
import com.qinfei.qferp.mapper.media1.MediaPlateMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.crm.IStatisticsService;
import com.qinfei.qferp.service.impl.media.MediaTypeService;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ArticleImportExcelListener extends AnalysisEventListener {

    private List<ArticleImportExcelInfo> datas = new ArrayList<>();
    private List<String> resultMsg = new ArrayList<>();
    private int updateNum = 0;

    @Autowired
    ArticleImportMapper articleImportMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    MediaTypeService mediaTypeService;
    @Autowired
    SupplierMapper supplierMapper;
    @Autowired
    MediaInfoMapper mediaInfoMapper;
    @Autowired
    Media1Mapper media1Mapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    ArticleMapper articleMapper;
    @Autowired
    DictMapper dictMapper;
    @Autowired
    IStatisticsService statisticsService;
    @Autowired
    IMediaForm1Service mediaForm1Service;
    @Autowired
    private MediaPlateMapper mediaPlateMapper;
    private String onlineTime;

    public ArticleImportExcelListener(ArticleImportMapper articleImportMapper, UserMapper userMapper, MediaTypeService mediaTypeService,
                                      SupplierMapper supplierMapper, MediaInfoMapper mediaInfoMapper, Media1Mapper media1Mapper,
                                      OrderMapper orderMapper,ArticleMapper articleMapper, DictMapper dictMapper, IStatisticsService statisticsService,
                                      IMediaForm1Service mediaForm1Service, MediaPlateMapper mediaPlateMapper, String onlineTime) {
        this.articleImportMapper = articleImportMapper;
        this.userMapper = userMapper;
        this.mediaTypeService = mediaTypeService;
        this.supplierMapper = supplierMapper;
        this.mediaInfoMapper = mediaInfoMapper;
        this.media1Mapper = media1Mapper;
        this.orderMapper = orderMapper;
        this.articleMapper = articleMapper;
        this.dictMapper = dictMapper;
        this.statisticsService = statisticsService;
        this.mediaForm1Service = mediaForm1Service;
        this.mediaPlateMapper = mediaPlateMapper;
        this.onlineTime = onlineTime;
    }

    @Override
    public void invoke(Object object, AnalysisContext context) {
        ArticleImportExcelInfo info = (ArticleImportExcelInfo) object;
        info.setIndex(context.getCurrentRowNum());
        if (!StringUtils.isEmpty(info.getMediaName())) {
            datas.add(info);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (datas.size() > 0) {
//            System.out.println("稿件数量：" + datas.size());
//            System.out.println("稿件信息：" + JSON.toJSONString(datas));
            User user = AppUtil.getUser();
            List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(user.getId());
            Map<String, MediaPlate> mediaPlateMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(mediaPlateList)){
                mediaPlateList.forEach(mediaPlate -> {
                    mediaPlateMap.put(mediaPlate.getName(), mediaPlate);
                    mediaPlateMap.put(String.valueOf(mediaPlate.getId()), mediaPlate);
                });
            }
            mediaPlateList.clear();
            mediaPlateList = null;
            List<User> ywList = userMapper.listByType2(IConst.ROLE_TYPE_YW);
            Map<String, User> ywUserMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(ywList)){
                ywList.forEach(user1 -> {
                    ywUserMap.put(user1.getName(), user1);
                });
            }
            ywList.clear();
            ywList = null;
            //不能查询历史数据，增加本次改动上线时间
            List<Supplier> supplierList = supplierMapper.listSupplier(onlineTime);
            Map<String, Supplier> supplierMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(supplierList)){
                supplierList.forEach(supplier -> {
                    if(!StringUtils.isEmpty(supplier.getPhone())){
                        supplier.setPhone(EncryptUtils.decrypt(supplier.getPhone()));
                    }
                    supplierMap.put(String.format("%s-%s", supplier.getName(), supplier.getPhone()), supplier);
                });
            }
            supplierList.clear();
            supplierList = null;
            //不能查询历史数据，增加本次改动上线时间
            List<MediaAudit> mediaAuditList = media1Mapper.listAllMedia(onlineTime);
            Map<String, MediaAudit> mediaMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(mediaAuditList)){
                mediaAuditList.forEach(mediaAudit -> {
                    MediaPlate mediaPlate = mediaPlateMap.get(String.valueOf(mediaAudit.getPlateId()));
                    String key = String.format("%s-%s", mediaAudit.getPlateId(), mediaAudit.getName());
                    //如果是标准平台，则以 板块ID + 唯一标识为Key，否则 板块ID + 媒体名称为Key（默认）
                    if(mediaPlate != null && "1".equals(String.valueOf(mediaPlate.getStandarPlatformFlag()))){
                        key = String.format("%s-%s", mediaAudit.getPlateId(), mediaAudit.getMediaContentId());
                    }
                    //判断是否已经有该媒体，由于绑定供应商不同，所以需要添加到集合
                    if (mediaMap.containsKey(key)) {
                        if (CollectionUtils.isEmpty(mediaMap.get(key).getSuppliers())) {
                            mediaMap.get(key).setSuppliers(new ArrayList<>());
                        }
                        mediaMap.get(key).getSuppliers().add(mediaAudit.getSupplier());
                    } else {
                        if (CollectionUtils.isEmpty(mediaAudit.getSuppliers())) {
                            mediaAudit.setSuppliers(new ArrayList<>());
                        }
                        mediaAudit.getSuppliers().add(mediaAudit.getSupplier());
                        mediaMap.put(key, mediaAudit);
                    }

                });
            }
            mediaAuditList.clear();
            mediaAuditList = null;
            List<MediaForm1> mediaForm1List = mediaForm1Service.listAllPriceType();
            Map<Integer, Map<String, MediaForm1>> mediaFormMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(mediaForm1List)){
                mediaForm1List.forEach(mediaForm -> {
                    if(mediaFormMap.get(mediaForm.getMediaPlateId()) == null){
                        mediaFormMap.put(mediaForm.getMediaPlateId(), new HashMap<>());
                    }
                    mediaFormMap.get(mediaForm.getMediaPlateId()).put(mediaForm.getCellName(), mediaForm);
                });
            }
            mediaForm1List.clear();
            mediaForm1List = null;
            Integer orderId;
            for(ArticleImportExcelInfo info:datas) {
                orderId = null;//
                int rowNum = info.getIndex();//当前行号
                StringBuilder rowErrorInfo = new StringBuilder();
                //媒体板块校验
                if(mediaPlateMap.get(info.getMediaTypeName()) != null){
                    info.setMediaTypeId(mediaPlateMap.get(info.getMediaTypeName()).getId());
                }else {
                    rowErrorInfo.append("(").append(rowNum+1).append("行媒体板块错误或者您没有相应板块权限！").append(")");
                }
                //请款金额校验
                if(!StringUtils.isEmpty(info.getOutgoAmountStr())){
                    try {
                        if(Double.parseDouble(info.getOutgoAmountStr()) <= 0){
                            rowErrorInfo.append("(").append(rowNum+1).append("行请款金额必须大于0！").append(")");
                        }else {
                            info.setOutgoAmount(Double.parseDouble(info.getOutgoAmountStr()));
                        }
                    }catch (Exception e){
                        rowErrorInfo.append("(").append(rowNum+1).append("行请款金额数据格式错误！").append(")");
                    }
                }else {
                    rowErrorInfo.append("(").append(rowNum+1).append("行请款金额不能为空！").append(")");
                }
                //其他费用校验
                if (!StringUtils.isEmpty(info.getOtherExpensesStr())) {
                    try{
                        info.setOtherExpenses(Double.parseDouble(info.getOtherExpensesStr()));
                    }catch (Exception e){
                        rowErrorInfo.append("(").append(rowNum+1).append("行其他费用数据格式错误！").append(")");
                    }
                }
                //数量校验
                if (!StringUtils.isEmpty(info.getNumStr())) {
                    try{
                        info.setNum(Integer.parseInt(info.getNumStr()));
                    }catch (Exception e){
                        rowErrorInfo.append("(").append(rowNum+1).append("行数量数据格式错误！").append(")");
                    }
                }
                //业务员校验
                if(ywUserMap.get(info.getUserName()) != null){
                    List<Order> orderList = getOrderByUserId(ywUserMap.get(info.getUserName()).getId());
                    //如果存在订单，则使用第一个，否则默认
                    if (!CollectionUtils.isEmpty(orderList)) {
                        orderId = orderList.get(0).getId();
                    } else {
                        User YWUser = userMapper.getById(ywUserMap.get(info.getUserName()).getId());
                        Order order = new Order();
                        order.setUserId(YWUser.getId());
                        order.setUserName(YWUser.getName());
                        order.setDepatId(YWUser.getDeptId());
                        order.setNo("MTGJ" + UUIDUtil.get16UUID().toUpperCase());
                        order.setCreator(user.getId());
                        order.setCreateDate(new Date());
                        order.setState(0);
                        order.setOrderType(1);
                        orderMapper.insert(order);
                        orderId = order.getId();
                    }
                }else {
                    rowErrorInfo.append("(").append(rowNum+1).append("行业务员系统不存在或者状态为交接中！").append(")");
                }
                info.setOrderId(orderId);

                //如果供应商公司名称 和 电话号码都有填写，则判断供应商
                if(!StringUtils.isEmpty(info.getSupplierName()) && !StringUtils.isEmpty(info.getPhone())){
                    if(supplierMap.get(String.format("%s-%s", info.getSupplierName().trim(), info.getPhone())) != null){
                        info.setSupplierName(info.getSupplierName().trim());
                        info.setSupplierId(supplierMap.get(String.format("%s-%s", info.getSupplierName().trim(), info.getPhone())).getId());
                        info.setSupplierContactor(supplierMap.get(String.format("%s-%s", info.getSupplierName().trim(), info.getPhone())).getContactor());
                    }else {
                        rowErrorInfo.append("(").append(rowNum+1).append("行供应商公司名称和联系人手机号填写错误！").append(")");
                    }
                }
                //如果没有获取到供应商，则将供应商数据清空
                if(info.getSupplierId() == null){
                    info.setSupplierContactor("");
                    info.setSupplierName("");
                }

                //媒体校验
                if(!StringUtils.isEmpty(info.getMediaName())){
                    MediaPlate mediaPlate = mediaPlateMap.get(String.valueOf(info.getMediaTypeName()));
                    if(mediaPlate != null){
                        MediaAudit mediaAudit = mediaMap.get(String.format("%s-%s", info.getMediaTypeId(), info.getMediaName().trim()));
                        if(mediaAudit != null){
                            info.setMediaId(mediaAudit.getId());
                            info.setMediaName(StringUtils.isEmpty(mediaAudit.getName()) ? mediaAudit.getMediaContentId() : mediaAudit.getName());
                            if (info.getSupplierId() != null) {
                                if (CollectionUtils.isEmpty(mediaAudit.getSuppliers())) {
                                    rowErrorInfo.append("(").append(rowNum + 1).append("行媒体没有绑定供应商！").append(")");
                                } else {
                                    boolean validFlag = false;
//                                    System.out.println("info = " + JSON.toJSONString(info));
//                                    System.out.println("mediaAudit = " + JSON.toJSONString(mediaAudit));
                                    for (Supplier supplier : mediaAudit.getSuppliers()) {
                                        //如果供应商ID有值，则判断是否和当前媒体有关系，没关系则提示
                                        if (info != null && info.getSupplierId() != null && supplier != null && supplier.getId() != null && info.getSupplierId().equals(supplier.getId())) {
                                            validFlag = true;
                                            break;
                                        }
                                    }
                                    if (!validFlag) {
                                        rowErrorInfo.append("(").append(rowNum + 1).append("行媒体和供应商没有建立关系！").append(")");
                                    }
                                }
                            }
                        }else {
                            //如果是标准平台，则以 板块ID + 唯一标识为Key，否则 板块ID + 媒体名称为Key（默认）
                            if(mediaPlate != null && "1".equals(String.valueOf(mediaPlate.getStandarPlatformFlag()))){
                                rowErrorInfo.append("(").append(rowNum+1).append("行所属板块为标准媒体板块，唯一标识对应媒体不存在！").append(")");
                            }else {
                                rowErrorInfo.append("(").append(rowNum+1).append("行所属板块为非标准媒体板块，媒体名称对应媒体不存在！").append(")");
                            }
                        }
                    }else {
                        rowErrorInfo.append("(").append(rowNum+1).append("行所属板块错误或者没有相应板块权限，无法进行媒体校验！").append(")");
                    }
                }else {
                    rowErrorInfo.append("(").append(rowNum+1).append("行媒体名称/唯一标识不能为空！").append(")");
                }
                //价格类型
                if(!StringUtils.isEmpty(info.getPriceTypeStr())){
                    MediaPlate mediaPlate = mediaPlateMap.get(String.valueOf(info.getMediaTypeId()));
                    if(mediaPlate != null){
                        if(mediaFormMap.get(mediaPlate.getId()) != null && mediaFormMap.get(mediaPlate.getId()).get(info.getPriceTypeStr()) != null){
                            info.setPriceType(info.getPriceTypeStr());
                            info.setPriceColumn(mediaFormMap.get(mediaPlate.getId()).get(info.getPriceTypeStr()).getCellCode());
                        }else {
                            rowErrorInfo.append("(").append(rowNum+1).append("行价格类型填写错误！").append(")");
                        }
                    }else {
                        rowErrorInfo.append("(").append(rowNum+1).append("行所属板块错误或者没有相应板块权限，无法进行媒体价格类型校验！").append(")");
                    }
                }else {
                    rowErrorInfo.append("(").append(rowNum+1).append("行价格类型不能为空！").append(")");
                }
                //标题
                if(StringUtils.isEmpty(info.getTitle())){
                    rowErrorInfo.append("(").append(rowNum+1).append("行标题不能为空！").append(")");
                }
                //链接
                if(StringUtils.isEmpty(info.getLink())){
                    rowErrorInfo.append("(").append(rowNum+1).append("行链接不能为空！").append(")");
                }else if(info.getLink().length() > 1000){
                    rowErrorInfo.append("(").append(rowNum+1).append("行链接长度超限！").append(")");
                }
                String issuedDateStr = info.getIssuedDateStr();
                Date issuedDate = null;
                if (!StringUtils.isEmpty(issuedDateStr)) {
                    try{
                        if(issuedDateStr.indexOf("/") > 0 || issuedDateStr.indexOf("\\") > 0){
                            issuedDateStr = issuedDateStr.replaceAll("/", "").replaceAll("\\\\", "");
                            LocalDate localDate = LocalDate.parse(issuedDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                            issuedDate = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                        }else if(issuedDateStr.indexOf("-") > 0){
                            LocalDate localDate = LocalDate.parse(issuedDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            issuedDate = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                        }else{
//                            我们期望这个日期解析出来是：2019/10/30,而结果却是43768.什么原因呢？这个数字是什么呢？是以1900年为原点，到2019年10月30日，之间经过的天数。
//                            知道这个后，就很好处理了，我们拿到1900年的日期，在这个日期上加上43768天即可
                            Calendar calendar = new GregorianCalendar(1900,0,-1);
                            Date d = calendar.getTime();
////                            然后，利用DateUtils的方法，加上天数（这个天数被转为了字符串，值为43768）
                            issuedDate = DateUtils.getAfterDay(d,Integer.valueOf(issuedDateStr));
                        }
                        if(issuedDate != null){
                            info.setIssuedDate(issuedDate);
                        }else {
                            rowErrorInfo.append("(").append(rowNum+1).append("行发布日期格式错误！").append(")");
                        }
                    }catch (Exception e){
                        rowErrorInfo.append("(").append(rowNum+1).append("行发布日期格式错误！").append(")");
                    }
                }else {
                    rowErrorInfo.append("(").append(rowNum+1).append("行发布日期不能为空！").append(")");
                }
                String innerOuterStr = info.getInnerOuteStr();
                if(!StringUtils.isEmpty(innerOuterStr)){
                    if(IConst.TYPE_INNER.equals(innerOuterStr)){
                        info.setInnerOuter(IConst.TYPE_INNER);
                    }else if(IConst.TYPE_OUTER.equals(innerOuterStr)){
                        info.setInnerOuter(IConst.TYPE_OUTER);
                    }
                }
                //单价=（总价-其他费用）/数量
                Double unitPrice = (new BigDecimal(info.getOutgoAmount() != null ? info.getOutgoAmount() : 0).
                        subtract(new BigDecimal(info.getOtherExpenses() != null ? info.getOtherExpenses() : 0)).
                        divide(new BigDecimal(info.getNum() != null ? info.getNum() : 1), 2, BigDecimal.ROUND_HALF_UP)).doubleValue();
                info.setUnitPrice(unitPrice);
                info.setCreateTime(new Date());
                info.setCreator(user.getId());
                info.setMediaUserId(user.getId());//媒介设置成自己
                info.setMediaUserName(user.getName());

                //判断是否有错误信息
                if(!StringUtils.isEmpty(rowErrorInfo.toString())){
                    resultMsg.add(rowErrorInfo.toString());
                }
            }
            if(CollectionUtils.isEmpty(resultMsg)){
                Collections.reverse(datas) ;//反序
                int size = datas.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    List<ArticleImportExcelInfo> insertData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(datas.get(j));
                    }
                    articleImportMapper.saveBatchForEasyExcel(insertData);
                }
            }
        }else {
            resultMsg.add("稿件导入模板从第三行开始读取数据，当前导入数据为空！");
        }
    }

    @Cacheable(value = "order", key = "'userId=' +#userId")
    public List<Order> getOrderByUserId(Integer userId) {
        User user = userMapper.getById(userId);
        return orderMapper.getOrderByUserId(userId,user.getDeptId());
    }
    public List<ArticleImportExcelInfo> getDatas() {
        return datas;
    }

    public void setDatas(List<ArticleImportExcelInfo> datas) {
        this.datas = datas;
    }

    public List<String> getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(List<String> resultMsg) {
        this.resultMsg = resultMsg;
    }
}
