package com.qinfei.qferp.service.impl.biz;

import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.mapper.DictMapper;
import com.qinfei.core.serivce.impl.DictService;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.biz.*;
import com.qinfei.qferp.entity.crm.CrmCompanyUser;
import com.qinfei.qferp.entity.media.Supplier;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.biz.*;
import com.qinfei.qferp.mapper.crm.CrmCompanyMapper;
import com.qinfei.qferp.mapper.crm.CrmCompanyUserMapper;
import com.qinfei.qferp.mapper.media.MediaInfoMapper;
import com.qinfei.qferp.mapper.media.SupplierMapper;
import com.qinfei.qferp.mapper.media1.Media1Mapper;
import com.qinfei.qferp.mapper.media1.MediaPlateMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.IArticleImportService;
import com.qinfei.qferp.service.crm.IStatisticsService;
import com.qinfei.qferp.service.impl.crm.CrmCompanyService;
import com.qinfei.qferp.service.impl.crm.CrmCompanyUserService;
import com.qinfei.qferp.service.impl.media.MediaTypeService;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ArticleImportService implements IArticleImportService {
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
    OrderService orderService;
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
    @Autowired
    private DictService dictService;
    @Autowired
    private ArticleHistoryMapper articleHistoryMapper;
    @Autowired
    private CrmCompanyUserService companyUserService;
    @Autowired
    private CrmCompanyUserMapper companyUserMapper;
    @Autowired
    private CrmCompanyMapper crmCompanyMapper;
    @Value("${media.onlineTime}")
    private String onlineTime;
/*    @Autowired
    private ArticleExtendMapper articleExtendMapper;
    @Autowired
    private ProjectMapper projectMapper;*/

    @Override
    public PageInfo<Map> listPgMJ(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = articleImportMapper.listPgMJ(map);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map> listPgYW(int pageNum, int pageSize, Map map) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = articleImportMapper.listPgYW(map);
        return new PageInfo<>(list);
    }

    @Override
    public ArticleImport getById(Integer id) {
        return articleImportMapper.getById(id);
    }

    @Override
    public synchronized ArticleImport add(ArticleImport entity) {
        articleImportMapper.insert(entity);
        return entity;
    }

    @Override
    public ArticleImport edit(ArticleImport entity) {
        articleImportMapper.update(entity);
        return entity;
    }

    @Override
    public Boolean delById(Integer id) {
        Boolean flag = false;
        User user = AppUtil.getUser();
        ArticleImport entity = getById(id);
        if (entity.getCreator().equals(user.getId()) || entity.getMediaUserId().equals(user.getId())) {
            entity.setState(IConst.STATE_DELETE);
            entity.setUpdateUserId(user.getId());
            articleImportMapper.update(entity);
            flag = true;
        }
        return flag;
    }

    @Override
    @Transactional
    public Map batchDel(String ids) {
        Map<String, Integer> resultMap = new HashMap();
        Integer succNum = 0;
        Integer failNum = 0;
        User user = AppUtil.getUser();
        if (!StringUtils.isEmpty(ids)) {
            if (ids.indexOf(",") > -1) {
                Set<Integer> set = new HashSet<>();
                String[] aids = ids.split(",");
                Map map = new HashMap();
                for (String id : aids) {
                    ArticleImport ai = articleImportMapper.getById(Integer.parseInt(id));
                    if (user.getId().equals(ai.getCreator()) || user.getId().equals(ai.getMediaUserId())) {
                        set.add(ai.getId());
                        // ai.setState(IConst.STATE_DELETE);
                        // articleImportMapper.update(ai);
                    } else {
                        failNum++;
                    }
                }
                if (set != null && set.size() > 0) {
                    map.put("collection", set);
                    succNum = articleImportMapper.batchDel(map);
                }
            } else {
                ArticleImport ai = articleImportMapper.getById(Integer.parseInt(ids));
                if (user.getId().equals(ai.getCreator()) || user.getId().equals(ai.getMediaUserId())) {
                    ai.setState(IConst.STATE_DELETE);
                    succNum = articleImportMapper.update(ai);

                } else {
                    failNum++;
                }
            }
        }
        resultMap.put("succNum", succNum);
        resultMap.put("failNum", failNum);
        return resultMap;
    }

    @Override
    @Transactional
    public String batchOrder(File file) {
        StringBuffer buf = new StringBuffer();
        User user = AppUtil.getUser();
        Workbook workbook = null;
        FormulaEvaluator formulaEvaluator = null;
        List<Article> imports = new ArrayList<>();
        try {
            ZipSecureFile.setMinInflateRatio(-1.0d);
            FileInputStream fis = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(fis);
                formulaEvaluator = new XSSFFormulaEvaluator((XSSFWorkbook) workbook);
            } else if (file.getName().toLowerCase().endsWith("xls")) {
                workbook = new HSSFWorkbook(fis);
                formulaEvaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
            }
            // 得到一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            // 获得数据的总行数
            int totalRowNum = sheet.getLastRowNum();

            List<User> mjlist = userMapper.listByTypeAndCompanyCode3(IConst.ROLE_TYPE_MJ, user.getCompanyCode());
            List<User> YWList = userMapper.listByType2(IConst.ROLE_TYPE_YW);
//            List<MediaType> MediaTypeList = mediaTypeService.getByParentId(0);
            List<Supplier> supplierList = supplierMapper.listSupplier(onlineTime);//不能查询历史数据，增加本次改动上线时间
//            List<MediaInfo> mediaInfoList = mediaInfoMapper.queryAll();
//            List<MediaForm> mediaFormList = mediaFormService.queryAllPriceColumns();
            List<MediaAudit> mediaAuditList = media1Mapper.listAllMedia(onlineTime);//不能查询历史数据，增加本次改动上线时间
            List<MediaForm1> mediaForm1List = mediaForm1Service.listAllPriceType();
            for (int i = 2; i <= totalRowNum; i++) {
                // 获得第i行对象
                Row row = sheet.getRow(i);
                //从第
                if (row != null || !StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(2)))) {
                    boolean flag = true;
                    for (int j = 0; j < 11; j++) {//前10列必填
                        if (row.getCell(j) == null || StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(j), formulaEvaluator))) {
                            int k = j + 1;
                            buf.append(i + 1).append("行").append(k).append("列为空、");
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        String mediaTypeName = DataImportUtil.getValue(row.getCell(0));
                        String mediaUserName = DataImportUtil.getValue(row.getCell(1));
                        String supplierName = DataImportUtil.getValue(row.getCell(2));
                        String supplierContactor = DataImportUtil.getValue(row.getCell(3));
                        String userName = DataImportUtil.getValue(row.getCell(4));
                        String mediaName = DataImportUtil.getValue(row.getCell(6));

                        Article article = new Article();
                        article.setMediaTypeName(mediaTypeName);
                        article.setSupplierName(supplierName);
                        article.setSupplierContactor(supplierContactor);
                        article.setMediaName(mediaName);
                        article.setMediaUserName(mediaUserName);

                        for (User MJ : mjlist) {
                            if (MJ.getName().equals(mediaUserName)) {
                                article.setMediaUserId(MJ.getId());
                                break;
                            }
                        }

                        if (article.getMediaUserId() == null) {
                            buf.append(i + 1).append("行B列、");
                            continue;
                        }

                        Integer orderId = null;
                        for (User YW : YWList) {
                            if (YW.getName().equals(userName)) {
                                List<Order> orderList = getOrderByUserId(YW.getId(), YW.getDeptId());
                                if (orderList != null && orderList.size() > 0) {
                                    orderId = orderList.get(0).getId();
                                } else {
                                    User YWUser = userMapper.getById(YW.getId());
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
                                break;
                            }
                        }
                        if (orderId == null) {
                            buf.append(i + 1).append("行E列、");
                            continue;
                        }
                        article.setOrderId(orderId);

//                        System.out.println("********************************mediaUserId="+article.getMediaUserId());
//                        List<MediaType> MediaTypeList = mediaTypeService.listByUserId(article.getMediaUserId()) ;
//                        for (MediaType mediaType : MediaTypeList) {
//                            if (mediaType.getName().equals(mediaTypeName)) {
//                                article.setMediaTypeId(mediaType.getId());
//                                break;
//                            }
//                        }
                        List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(article.getMediaUserId());
                        for (MediaPlate mediaPlate : mediaPlateList) {
                            if (mediaPlate.getName().equals(mediaTypeName)) {
                                article.setMediaTypeId(mediaPlate.getId());
                                break;
                            }
                        }

                        if (article.getMediaTypeId() != null) {
                            for (Supplier supplier : supplierList) {
                                // if(mediaInfo.getSupplierId().equals(6510)||mediaInfo.getName().equals("清南师兄")){
                                // System.out.println(mediaInfo.getSupplierId()+"\t"+ mediaInfo.getName());
                                // }
                                if (supplier.getMediaTypeId().toString().equals(article.getMediaTypeId().toString())
                                        && supplier.getName().equals(supplierName)
                                        && supplier.getContactor().equals(supplierContactor)
                                        && user.getCompanyCode().equals(supplier.getCompanyCode())) {
                                    article.setSupplierId(supplier.getId());
                                    break;
                                }
                            }
                        } else {
                            buf.append(i + 1).append("行A列（媒介无相应板块权限）、");
                            continue;
                        }

                        if (article.getSupplierId() != null) {
                          /*  for (MediaInfo mediaInfo : mediaInfoList) {
//                                if(mediaInfo.getName().equals(mediaName)){
//                                    System.out.println("mediaInfo.getSupplierId()="+article.getSupplierId());
//                                    System.out.println("articleImport.getSupplierId()="+article.getSupplierId());
//                                }
//                                if(mediaInfo.getSupplierId().toString().equals(article.getSupplierId().toString())){
//                                    System.out.println("mediaInfo.getName()="+mediaInfo.getName());
//                                    System.out.println("mediaName="+mediaName);
//                                }
                                if (mediaInfo.getSupplierId().toString().equals(article.getSupplierId().toString()) && mediaInfo.getName().equals(mediaName)) {
                                    article.setMediaId(mediaInfo.getId());
                                    break;
                                }
                            }*/
                            for (MediaAudit mediaAudit : mediaAuditList) {
                                if (mediaAudit.getSupplier().getId().equals(article.getSupplierId())
                                        && mediaAudit.getName().equals(mediaName)
                                        && user.getCompanyCode().equals(mediaAudit.getCompanyCode())) {
                                    article.setMediaId(mediaAudit.getId());
                                    break;
                                }
                            }
                        } else {
                            buf.append(i + 1).append("行C或D列、");
                            continue;
                        }

                        // 处理日期格式
                        Cell issueDateCell = row.getCell(5);
                        if (issueDateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(issueDateCell)) {
                            Date date = issueDateCell.getDateCellValue();
                            article.setIssuedDate(date);
                        } else {
                            buf.append(i + 1).append("行F列、");
                            continue;
                        }

                        if (article.getMediaId() == null) {
                            buf.append(i + 1).append("行G列、");
                            continue;
                        }

                        if (StringUtil.isNotEmpty(DataImportUtil.getValue(row.getCell(7)))) {
                            article.setTitle(DataImportUtil.getValue(row.getCell(7)));
                        } else {
                            buf.append(i + 1).append("行H列、");
                            continue;
                        }
                        if (StringUtil.isNotEmpty(DataImportUtil.getValue(row.getCell(8)))) {
                            article.setLink(DataImportUtil.getValue(row.getCell(8)));
                        } else {
                            buf.append(i + 1).append("行I列、");
                            continue;
                        }

                        Cell outgoAmountCell = row.getCell(9);
                        if (outgoAmountCell.getCellType() == CellType.NUMERIC || outgoAmountCell.getCellType() == CellType.FORMULA) {
                            article.setOutgoAmount(Double.parseDouble(DataImportUtil.getValue(outgoAmountCell)));
                        } else {
                            buf.append(i + 1).append("行J列、");
                            continue;//
                        }
                        if (!(article.getOutgoAmount() > 0)) {
                            buf.append(i + 1).append("行支付金额必须大于0！、");
                            continue;//
                        }
                        String priceType = DataImportUtil.getValue(row.getCell(10));
                        if (article.getMediaTypeId() != null) {
                            /*for (MediaForm mediaForm : mediaFormList) {
                                // if(mediaInfo.getSupplierId().equals(6510)||mediaInfo.getName().equals("清南师兄")){
                                // System.out.println(mediaInfo.getSupplierId()+"\t"+ mediaInfo.getName());
                                // }
                                if (mediaForm.getMediaTypeId().toString().equals(article.getMediaTypeId().toString())
                                        && mediaForm.getName().equals(priceTypeCell.getStringCellValue().trim())) {
                                    article.setPriceType(priceTypeCell.getStringCellValue().trim());
                                    break;
                                }
                            }*/
                            for (MediaForm1 mediaForm1 : mediaForm1List) {
                                if (mediaForm1.getMediaPlateId().equals(article.getMediaTypeId())
                                        && mediaForm1.getCellName().equals(priceType)) {
                                    article.setPriceType(priceType);
                                    break;
                                }
                            }
                        }
                        if (article.getPriceType() == null) {
                            buf.append(i + 1 + "行K列、");
                            continue;
                        }

                        // 处理数字格式
                        Cell numCell = row.getCell(11);
                        if (numCell == null) {
                            article.setNum(1);
                        } else if (numCell.getCellType() == CellType.NUMERIC) {
                            article.setNum(Integer.parseInt(new DecimalFormat("0").format(numCell.getNumericCellValue())));
                        } else {
                            article.setNum(1);
                        }

                        Cell otherExpensesCell = row.getCell(12);
                        if (otherExpensesCell == null) {
                            article.setOtherExpenses(0D);
                        } else if (otherExpensesCell.getCellType() == CellType.NUMERIC || otherExpensesCell.getCellType() == CellType.FORMULA) {
                            article.setOtherExpenses(Double.parseDouble(DataImportUtil.getValue(otherExpensesCell)));
                        } else {
                            article.setOtherExpenses(0D);
                        }

                        if (row.getCell(13) != null) {
                            article.setRemarks(row.getCell(13).toString().trim());
                        }

                        String electricityBusinesses = DataImportUtil.getValue(row.getCell(14));
                        article.setElectricityBusinesses(electricityBusinesses);

                        Cell innerOuterCell = row.getCell(15);
                        String innerOuter = DataImportUtil.getValue(innerOuterCell);
                        if (innerOuterCell != null && innerOuterCell.getCellType() == CellType.STRING) {
                            if (IConst.TYPE_INNER.equals(innerOuter)) {
                                article.setInnerOuter(IConst.TYPE_INNER);
                            } else if (IConst.TYPE_OUTER.equals(innerOuter)) {
                                article.setInnerOuter(IConst.TYPE_OUTER);
                            }
                        }
                        String channel = DataImportUtil.getValue(row.getCell(16));
                        article.setChannel(channel);
                        //单价=（总价-其他费用）/数量
                        Double unitPrice = (new BigDecimal(article.getOutgoAmount()).
                                subtract(new BigDecimal(article.getOtherExpenses())).
                                divide(new BigDecimal(article.getNum()), 2, BigDecimal.ROUND_HALF_UP)).doubleValue();
                        article.setUnitPrice(unitPrice);
                        article.setCreateTime(new Date());
                        article.setCreator(user.getId());
                        article.setState(IConst.STATE_FINISH);
                        article.setIssueStates(4);
                        imports.add(article);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
            buf.append("不正确。\n请核实信息后重试！");
            return buf.toString();
        } else {
            Collections.reverse(imports);//反序
            if (imports != null && imports.size() > 0) {
                int size = imports.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    List<Article> insertData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(imports.get(j));
                    }
                    articleImportMapper.saveBatch(insertData);
                }
            }
            return null;
        }
    }

    @Cacheable(value = "order", key = "'userId=' +#userId + '&deptId='+deptId")
    public List<Order> getOrderByUserId(Integer userId, Integer deptId) {
        return orderMapper.getOrderByUserId(userId, deptId);
    }

    @Override
    @Transactional
    public void complete(Order order, Map map) {
        String companyCode = AppUtil.getUser().getCompanyCode();
        String curMonth = DateUtils.getYearAndMonthStr2(new Date());//当前年月
        Integer taxType = null;//税种
        Double ratio = 1.0;//换算比
        Double taxPoint = 0.0;//税点
        Object tempTaxType = map.get("taxType1");
        String tempTax = (String) map.get("taxPoint2");
        //选择了开票就要税种、税点、换算比数据
        if (!StringUtils.isEmpty(tempTaxType)) {
            Dict taxes = dictMapper.getByTypeCodeAndName(IConst.DICT_TYPE_CODE_TAX, tempTaxType.toString(), companyCode);
            if (taxes != null) {
                taxType = taxes.getId();
                try {
                    try {
                        ratio = Double.parseDouble(taxes.getType());
                    } catch (Exception e) {
                        throw new QinFeiException(1002, "获取的换算比不正确，应该为数字，实际是：" + tempTax);
                    }
                } catch (Exception e) {
                    throw new QinFeiException(1002, "税种对应的换算比不正确！获取的换算比为：" + taxes.getType());
                }

            }
            if (!StringUtils.isEmpty(tempTax)) {
                try {
                    taxPoint = Double.parseDouble(tempTax);
                } catch (Exception e) {
                    throw new QinFeiException(1002, "获取的税点不正确，应该为数字，实际是：" + tempTax);
                }
            } else {
                throw new QinFeiException(1002, "获取的税点信息不正确！获取的税点为：" + tempTax);
            }
        }

        String promiseDayStr = (String) map.get("promiseDay");
        Integer promiseDay = null;
        if (!StringUtils.isEmpty(promiseDayStr)) {
            try {
                promiseDay = Integer.parseInt(promiseDayStr);
            } catch (Exception e) {
                throw new QinFeiException(1002, "获取的客户答应到款日期不正确！获取的客户答应到款日期为：" + promiseDayStr);
            }
        }
        String avgPriceStr = (String) map.get("avgPrice");
        String brand = (String) map.get("brand");
        String typeCode = (String) map.get("typeCode");
        String typeName = (String) map.get("typeName");
        User user = AppUtil.getUser();
        String ids = (String) map.get("ids");
        List<Article> list = new ArrayList<>();
        List<ArticleHistory> historyList = new ArrayList<>();
        /*List<ArticleExtend> extendList = new ArrayList<>();
        Boolean extendFlag = false ;
        if(map.containsKey("projectId") && !ObjectUtils.isEmpty(map.get("projectId"))){
            extendFlag = true ;
        }*/
        if (!StringUtils.isEmpty(ids)) {
            List<Integer> artIds = new ArrayList<>();
            if (ids.indexOf(",") > -1) {
                String[] articleIds = ids.split(",");
                Integer len = articleIds.length;
                for (int i = 0; i < len; i++) {
                    artIds.add(Integer.parseInt(articleIds[i]));
                }
            } else {
                artIds.add(Integer.parseInt(ids));
            }
            Integer len = artIds.size();//选中稿件的数量
            List<Integer> deptIdList = articleImportMapper.getDetpIdList(artIds);
            if (deptIdList == null || deptIdList.size() == 0) {
                throw new QinFeiException(1002, "没有获取到选中的稿件对应的订单。");
            }
            //保留历史的部门id，有些人可能调岗，订单表一个人有多个部门id
            List<Order> orders = new ArrayList<>();
            for (Integer item : deptIdList) {
                Order temp = new Order();
                BeanUtils.copyProperties(order, temp);
                temp.setDepatId(item);
                temp.setId(null);
                orderMapper.insert(temp);
                orders.add(temp);
            }

            //更新历史订单表中客户的最近成交时间
            List<Integer> custIdList = articleImportMapper.getCustIdList(artIds);
            if (custIdList == null || custIdList.size() == 0) {
                for (Integer item : custIdList) {
                    CrmCompanyUser crmCompanyUser = companyUserService.getById(item);
                    //A\B类保护要把该公司下所有的对接人都更新
                    if (crmCompanyUser.getProtectLevel() > IConst.PROTECT_LEVEL_C) {
                        Map temp = new HashMap();
                        temp.put("companyId", crmCompanyUser.getCompanyId());
                        temp.put("list", artIds);
                        //查询选中稿件的最晚发布日期作为成交时间
                        Date date = articleImportMapper.getMaxIssuedDateByCompanyIdNot(temp);
                        Map param = new HashMap();
                        param.put("companyId", crmCompanyUser.getCompanyId());
                        param.put("updateUserId", user.getId());
                        if (date != null) {
                            param.put("dealTime", date);
                            companyUserMapper.updateDealTimeByCompanyId(param);
                        } else {
                            companyUserMapper.initDealTimeByCompanyId(param);
                        }
                    } else {
                        Map temp = new HashMap();
                        temp.put("custId", crmCompanyUser.getId());
                        temp.put("list", artIds);
                        //查询选中稿件的最晚发布日期作为成交时间
                        Date date = articleImportMapper.getMaxIssuedDateByCustIdNot(temp);
                        if (date != null) {
                            CrmCompanyUser companyUser = companyUserService.getById(item);
                            companyUser.setDealTime(date);
                            companyUser.setUpdateUserId(user.getId());
                            companyUserMapper.update(companyUser);
                        } else {
                            companyUserMapper.initDealTimeByCompanyUserId(crmCompanyUser.getId(), user.getId());
                        }
                    }
                }
            }

            Date nowDate = new Date();
            //更新新订单表中客户的最近成交时间
            Map temp = new HashMap();
            Integer custId = MapUtils.getInteger(map, "custId");
            temp.put("custId", custId);
            temp.put("list", artIds);
            Date date = articleImportMapper.getMaxIssuedDateByCustIdNot(temp);
            Date end = DateUtils.parse(map.get("endTime").toString(), DateUtils.DATE_SMALL);
            Date dealTime;
            if (date != null) {
                dealTime = date.getTime() > end.getTime() ? date : end;
            } else {
                dealTime = end;
            }
            CrmCompanyUser crmCompanyUser = companyUserService.getById(custId);
            //A\B类保护要把该公司下所有的对接人都更新
            if (crmCompanyUser.getProtectLevel() > IConst.PROTECT_LEVEL_C) {
                Map param = new HashMap();
                param.put("evalTime", nowDate);//成交要更新跟进时间
                param.put("dealTime", dealTime);
                param.put("companyId", crmCompanyUser.getCompanyId());
                param.put("updateUserId", user.getId());
                companyUserMapper.updateDealTimeByCompanyId(param);

                //跟进记录表
                Map track = new HashMap();
                List<Map> result = companyUserMapper.listByCompanyIdAndState(crmCompanyUser.getCompanyId());
                for (Map data : result) {
                    track.put("companyUserId", data.get("id"));
                    track.put("content", IConst.TRACK_CONTENT_DEAL);
                    track.put("imageName", null);
                    track.put("imageLink", null);
                    track.put("affixName", null);
                    track.put("affixLink", null);
                    track.put("trackTime", nowDate);
                    track.put("creator", user.getId());
                    crmCompanyMapper.saveCompanyTrack(track);
                }
            } else {
                crmCompanyUser.setEvalTime(nowDate);//成交要更新跟进时间
                crmCompanyUser.setDealTime(dealTime);
                crmCompanyUser.setUpdateUserId(user.getId());
                companyUserMapper.update(crmCompanyUser);

                //跟进记录表
                Map track = new HashMap();
                track.put("companyUserId", crmCompanyUser.getId());
                track.put("content", IConst.TRACK_CONTENT_DEAL);
                track.put("imageName", null);
                track.put("imageLink", null);
                track.put("affixName", null);
                track.put("affixLink", null);
                track.put("trackTime", nowDate);
                track.put("creator", user.getId());
                crmCompanyMapper.saveCompanyTrack(track);
            }

            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    Article article = articleMapper.get(Article.class, artIds.get(i));
                    Order oldOrder = orderMapper.get(Order.class, article.getOrderId());
                    for (Order result : orders) {
                        if (result.getDepatId().equals(oldOrder.getDepatId())) {
                            article.setOrderId(result.getId());
                        }
                    }
                    //=============================处理历史记录开始=============================
                    ArticleHistory history = new ArticleHistory();
                    BeanUtils.copyProperties(article, history);
                    history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                    history.setUserId(order.getUserId());
                    history.setDeptId(order.getDepatId());
                    history.setArtId(article.getId());
                    history.setId(null);
                    history.setEditDesc(IConst.article_change_business_complete);
                    history.setCreator(user.getId());
                    history.setCreateTime(nowDate);

                    Double saleAmount;
                    if (!StringUtils.isEmpty(avgPriceStr)) {
                        saleAmount = Double.parseDouble(avgPriceStr);
                    } else {
                        saleAmount = article.getSaleAmount();
                    }
//                  满足三个条件报价就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置报价，3、报价有变动
                    String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
                    Double alterSale = saleAmount - article.getSaleAmount();
                    if (!(curMonth.equals(issueMonth))
                            && article.getSaleAmount() > 0
                            && Math.abs(alterSale) > 0.01) {
                        article.setAlterFlag(IConst.article_alter_flag_true);
                        history.setAlterSale(alterSale);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                    }

                    //税金计算方式：税点*报价/换算比
                    Double tax = taxPoint * (saleAmount / ratio);
//                  满足三个条件税金就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置税金，3、税金有变动
                    Double alterTax = tax - article.getTaxes();
                    if (!(curMonth.equals(issueMonth))
                            && article.getSaleAmount() > 0
                            && Math.abs(alterTax) > 0.01) {
                        article.setAlterFlag(IConst.article_alter_flag_true);
                        history.setAlterTax(alterTax);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                    }
                    //处理稿件更改信息
                    Double profit;
                    if (article.getIncomeAmount() < 0.01) {
                        article.setIncomeStates(IConst.FEE_STATE_SAVE);
                        profit = saleAmount - article.getOutgoAmount() - tax - article.getRefundAmount() - article.getOtherPay();
                    } else if (article.getIncomeAmount() < (article.getSaleAmount() - 1)) {
                        article.setIncomeStates(IConst.FEE_STATE_PROCESS);
                        profit = saleAmount - article.getOutgoAmount() - tax - article.getRefundAmount() - article.getOtherPay();
                    } else {
                        article.setIncomeStates(IConst.FEE_STATE_FINISH);
                        profit = article.getIncomeAmount() - article.getOutgoAmount() - tax - article.getRefundAmount() - article.getOtherPay();
                    }
                    //利润更改记录
                    if (IConst.article_alter_flag_true.equals(history.getAlterLabel())) {
                        Double alterProfit = profit - article.getProfit();
                        history.setAlterProfit(alterProfit);
                    }
                    historyList.add(history);

                    article.setTaxType(taxType);
                    article.setSaleAmount(saleAmount);
                    article.setTaxes(tax);
                    article.setProfit(profit);
                    if (!StringUtils.isEmpty(brand)) {
                        article.setBrand(brand);
                    }
                    article.setTypeCode(typeCode);
                    article.setTypeName(typeName);
                    article.setState(IConst.STATE_FINISH);
                    if(promiseDay != null){
                        Calendar calendar = Calendar.getInstance();
                        if (article.getIssuedDate() != null) {
                            calendar.setTime(article.getIssuedDate());
                        } else {
                            calendar.setTime(nowDate);
                        }
                        calendar.add(Calendar.DATE, promiseDay);
                        article.setPromiseDate(calendar.getTime());
                    }
                    article.setUpdateUserId(user.getId());
                    article.setUpdateTime(nowDate);
                    list.add(article);

                    /*if(extendFlag){
                        ArticleExtend extend = new ArticleExtend();
                        extend.setArticleId(article.getId());
                        extend.setProjectId(MapUtils.getInteger(map,"projectId"));
                        if(map.containsKey("projectCode") && !ObjectUtils.isEmpty(map.get("projectCode"))){
                            extend.setProjectCode(MapUtils.getString(map,"projectCode"));
                        }
                        if(map.containsKey("projectName") && !ObjectUtils.isEmpty(map.get("projectName"))){
                            extend.setProjectName(MapUtils.getString(map,"projectName"));
                        }
                        extendList.add(extend);
                    }*/
                }
            }

            if (list != null && list.size() > 0) {
                try {
//                    articleExtendMapper.deleteExtendBatch(list);
                    articleImportMapper.batchComplete(list);
                } catch (Exception e) {
                    throw new QinFeiException(1002, "批量更新稿件信息失败。");
                }
            } else {
                throw new QinFeiException(1002, "未获取到选中的稿件信息！");
            }

            if (historyList != null && historyList.size() > 0) {
                try {
                    articleHistoryMapper.saveBatch(historyList);
                } catch (Exception e) {
                    throw new QinFeiException(1002, "批量插入稿件更改历史记录失败。");
                }
            } else {
                throw new QinFeiException(1002, "未获取到选中的稿件信息！");
            }

           /* if(extendList != null && extendList.size() > 0){
                articleExtendMapper.saveExtendBatch(extendList);
            }*/
        }
    }

    @Override
    public int updateAmountAndBrand(Map map) {
        return articleImportMapper.updateAmountAndBrand(map);
    }

    /**
     * 业务员导出稿件
     *
     * @param map          参数
     * @param outputStream
     * @return
     */
    @Override
    public List<Map> exportArticleYW(Map map, OutputStream outputStream) {
        List<Map> list = new ArrayList<>();
        if (map.containsKey("datas")) {
            List<Integer> idList = new ArrayList<>();
            String ids = (String) map.get("datas");
            if (ids.indexOf(",") > -1) {
                String[] idss = ids.split(",");
                for (String temp : idss) {
                    idList.add(Integer.parseInt(temp));
                }
            } else {
                idList.add(Integer.parseInt(ids));
            }
            list = articleImportMapper.listByIds(idList);
        } else {

            list = articleImportMapper.listPgYWAsc(map);
        }
        String[] heads = {"标识列", "板块", "媒体", "媒介", "标题", "链接", "成本", "品牌", "*报价"};
        String[] fields = {"id", "media_type_name", "media_name", "media_user_name", "title", "link", "pay_amount", "brand", "sale_amount"};
        ExcelUtil.exportExcel("临时稿件信息", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("sale_amount".equals(field) || "pay_amount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else if ("saleAmount".equals(field) || "payAmount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    /**
     * @param ids
     * @return true选中的稿件有客户信息，false，选中的稿件都没有客户信息
     */
    @Override
    public Map checkCustInfo(String ids) {
        List<Integer> idList = strToList(ids);
        Map<String, Object> map = new HashMap<>();
        if (idList != null && idList.size() > 0) {
            map.put("list", idList);
            map.put("user", AppUtil.getUser());
            int row = articleImportMapper.checkCustInfo(idList);
            if (row > 0) {
                throw new QinFeiException(1002, "选中的稿件有回款、开票或提成数据!");
            }

            int num = articleImportMapper.checkUserId(map);
            if (num > 0) {
                throw new QinFeiException(1002, "选中的稿件中有其他业务员的稿件!");
            }

            Map result = articleImportMapper.getIssuedDateRange(map);
            int countPromise = articleImportMapper.countPromiseDateByIds(idList);
            Integer count = articleImportMapper.countZeroSales(map);
            if (count > 0) {//表示有报价为0的
                result.put("hasZeroSale", true);
            } else {
                result.put("hasZeroSale", false);
            }
            if (countPromise == idList.size()) { // 表示稿件都有答应到款时间
                result.put("hasPromise", true);
            } else {
                result.put("hasPromise", false);
            }

            return result;
        } else {
            throw new QinFeiException(1002, "没有选中的稿件！");
        }
    }

    @Override
    public PageInfo<Map> queryArticleByIds(int pageNum, int pageSize, String ids) {
        PageHelper.startPage(pageNum, pageSize);
        List<Integer> idList = strToList(ids);
        List<Map> list = articleImportMapper.listByIds(idList);
        return new PageInfo<>(list);
    }

    @Override
    public Map getArticleImportSum(Map map) {
        return articleImportMapper.getArticleImportSum(map);
    }

    @Override
    public Map querySumArticleByIds(String ids) {
        List<Integer> idList = strToList(ids);
        return articleImportMapper.querySumArticleByIds(idList);
    }

    private List<Integer> strToList(String ids) {
        List<Integer> idList = new ArrayList<>();
        try {
            if (ids.indexOf(",") > -1) {
                String[] idss = ids.split(",");
                for (String temp : idss) {
                    idList.add(Integer.parseInt(temp));
                }
            } else {
                idList.add(Integer.parseInt(ids));
            }
        } catch (Exception e) {
            throw e;
        }
        return idList;
    }

    /**
     * 媒介导出稿件
     *
     * @param map          参数
     * @param outputStream
     * @return
     */
    @Override
    public List<Map> exportArticleMJ(Map map, OutputStream outputStream) {
        List<Map> list = new ArrayList<>();
        if (map.containsKey("datas")) {
            List<Integer> idList = new ArrayList<>();
            String ids = (String) map.get("datas");
            if (ids.indexOf(",") > -1) {
                String[] idss = ids.split(",");
                for (String temp : idss) {
                    idList.add(Integer.parseInt(temp));
                }
            } else {
                idList.add(Integer.parseInt(ids));
            }
            list = articleImportMapper.listByIds(idList);
        } else {

            list = articleImportMapper.listPgMJAsc(map);
        }
        String[] heads = {"板块", "供应商名称", "供应商联系人", "媒体", "业务员", "媒介", "发布日期", "标题", "链接", "数量", "价格类型", "成本", "录入人"};
        String[] fields = {"media_type_name", "supplier_name", "supplier_contactor", "media_name", "user_name", "media_user_name", "issued_date", "title", "link", "num", "price_type", "pay_amount", "createName"};
        ExcelUtil.exportExcel("稿件列表", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("issuedDate".equals(field)) {
                    cell.setCellValue(DateUtils.format((Date) value, DateUtils.DEFAULT));
                } else if ("pay_amount".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else {
                    cell.setCellValue(value.toString());
                }

            }
        });
        return list;
    }

    @Override
    @Transactional
    public void batchSaleAmount(File file) {
        StringBuffer buf = new StringBuffer();
        User user = AppUtil.getUser();
        Workbook workbook = null;
        List<Map<String, Object>> list = new ArrayList<>();
        List<ArticleHistory> historyList = new ArrayList<>();

        String curMonth = DateUtils.getYearAndMonthStr2(new Date());//当前年月
        Map searchMap = new HashMap();
        searchMap.put("state", 1);
        searchMap.put("disabled", 0);
        searchMap.put("currentUserId", user.getId());
//        List<Map> projectList = projectMapper.listPg(searchMap);

//        List<ArticleExtend> extendList = new ArrayList<>();
        //获取所有的抬头，用于计算税金
        List<Dict> dicts = dictService.listByTypeCodeAndCompanyCode(IConst.DICT_TYPE_CODE_TAX, user.getCompanyCode());
        if (dicts == null || dicts.size() == 0) {
            throw new QinFeiException(1002, "未获取到该公司税种，请联系财务增加抬头！");
        }
        try {
            ZipSecureFile.setMinInflateRatio(-1.0d);
            FileInputStream fis = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (file.getName().toLowerCase().endsWith("xls")) {
                workbook = new HSSFWorkbook(fis);
            }
            // 得到一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            // 获得数据的总行数
            int totalRowNum = sheet.getLastRowNum();

            for (int i = 1; i <= totalRowNum; i++) {
                Map<String, Object> map = new HashMap();
                // 获得第i行对象
                int k = i + 1;
                Row row = sheet.getRow(i);
                if (row == null || StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(0)))) {
                    // buf.append(i + 1 + "、");
                    continue;
                } else if (StringUtils.isEmpty(DataImportUtil.getValue(row.getCell(13)))) {
                    buf.append("第" + k + "行报价为空、");
                    continue;
                } else {
                    Integer id = Integer.parseInt(DataImportUtil.getValue(row.getCell(0)));

                    Article article = articleMapper.get(Article.class, id);
                    Order order = orderService.get(article.getOrderId());
                    String issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
                    //存历史记录
                    ArticleHistory history = new ArticleHistory();
                    BeanUtils.copyProperties(article, history);
                    history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                    history.setUserId(order.getUserId());
                    history.setDeptId(order.getDepatId());
                    history.setId(null);
                    history.setArtId(article.getId());
                    history.setEditDesc(IConst.article_change_batch_saleAmount);
                    history.setCreator(user.getId());
                    history.setCreateTime(new Date());

                    Double saleAmount = 0.0;
                    Double incomeAmount = article.getIncomeAmount();
                    Double outgoAmount = article.getOutgoAmount();
                    Double refundAmount = article.getRefundAmount();
                    Double otherPayAmount = article.getOtherPay();
                    String brand = DataImportUtil.getValue(row.getCell(12));
                    Cell saleAmountCell = row.getCell(13);
                    try {
                        if ((saleAmountCell.getCellType() == CellType.FORMULA || saleAmountCell.getCellType() == CellType.NUMERIC)) {
                            saleAmount = Double.parseDouble(DataImportUtil.getValue(saleAmountCell));
                            if (!(saleAmount > 0)) {
                                buf.append("第" + k + "行报价必须大于0、");
                            }
                        } else {
                            buf.append("第" + k + "行报价不正确、");
                            continue;//
                        }
                    } catch (Exception e) {
                        throw new QinFeiException(1002, "获取报价出错：第" + k + "行");
                    }
                    if (article.getIncomeAmount() > 0 && (article.getIncomeAmount() - saleAmount) > 0.01) {
                        buf.append("第" + k + "行稿件有回款时更改报价，修改的报价金额不能小于回款金额!、");
                        continue;//
                    }
//              满足三个条件报价就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置报价，3、报价有变动
                    Double alterSale = saleAmount - article.getSaleAmount();
                    if (!(curMonth.equals(issueMonth))
                            && article.getSaleAmount() > 0
                            && Math.abs(alterSale) > 0.01) {
                        map.put("alterFlag", IConst.article_alter_flag_true);
                        history.setAlterSale(alterSale);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                    }
                    Double taxAmount = article.getTaxes();
                    Integer taxType = article.getTaxType();
                    if (ObjectUtils.isEmpty(taxType)) {
                        taxAmount = 0D;
                    } else {
                        for (Dict dict : dicts) {
                            if (dict.getId().equals(taxType)) {
                                String taxCode = dict.getCode();//税点
                                String taxType2 = dict.getType();//换算比
                                //税金=报价*税点/换算比
                                try {
                                    taxAmount = new BigDecimal(saleAmount.toString()).
                                            multiply(new BigDecimal(taxCode)).
                                            divide(new BigDecimal(taxType2), 8, BigDecimal.ROUND_HALF_UP).
                                            doubleValue();
                                } catch (Exception e) {
                                    throw new QinFeiException(1002, "计算税金错误，请联系财务核实抬头是否设置正确：税种=" + dict.getName());
                                }
                            }
                        }
                    }
                    //              满足三个条件税金就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置税金，3、税金有变动
                    if (!(curMonth.equals(issueMonth))
                            && !(article.getTaxes().equals(taxAmount))) {
                        Double alterTax = taxAmount - article.getTaxes();
                        map.put("alterFlag", IConst.article_alter_flag_true);
                        history.setAlterTax(alterTax);
                        history.setAlterLabel(IConst.article_alter_flag_true);
                    }
                    map.put("id", id);
                    map.put("userId", user.getId());
                    map.put("saleAmount", saleAmount);
                    map.put("taxAmount", taxAmount);
                    map.put("brand", brand);
                    Double profit;
                    if (incomeAmount > 0) {
                        if (!(saleAmount > (incomeAmount + 1))) {
                            map.put("incomeStates", IConst.FEE_STATE_FINISH);
                            profit = incomeAmount - outgoAmount - taxAmount - refundAmount - otherPayAmount;
                            map.put("profit", profit);
                        } else {
                            map.put("incomeStates", IConst.FEE_STATE_PROCESS);
                            profit = saleAmount - outgoAmount - taxAmount - refundAmount - otherPayAmount;
                            map.put("profit", profit);
                        }
                    } else {
                        map.put("incomeStates", IConst.FEE_STATE_SAVE);
                        profit = saleAmount - outgoAmount - taxAmount - refundAmount - otherPayAmount;
                        map.put("profit", profit);
                    }
                    if (IConst.article_alter_flag_true.equals(history.getAlterLabel())) {
                        Double alterProfit = profit - article.getProfit();
                        history.setAlterProfit(alterProfit);
                    }
                    historyList.add(history);
                    list.add(map);

                    /*String projectCode = DataImportUtil.getValue(row.getCell(25));
                    if(!StringUtils.isEmpty(projectCode)){
                        ArticleExtend extend = new ArticleExtend();
                        for(int j=0;j<projectList.size();j++){
                            Map temp = projectList.get(j) ;
                            if(temp.containsKey("code") && projectCode.equals(MapUtils.getString(temp,"code"))){
                                extend.setArticleId(id);
                                extend.setProjectId(MapUtils.getInteger(temp,"id"));
                                extend.setProjectCode(MapUtils.getString(temp,"code"));
                                extend.setProjectName(MapUtils.getString(temp,"name"));
                                break;
                            }
                        }
                        if(ObjectUtils.isEmpty(extend.getProjectId())){
                            buf.append("第" + k + "行项目编号不正确!、");
                            continue;//
                        }
                        extendList.add(extend);
                    }*/
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
            throw new QinFeiException(1002, "更新失败！错误提示：" + buf.toString());
        }
        if (list == null || list.size() == 0) {
            throw new QinFeiException(1002, "没有获取到稿件信息，请核实后重试！");
        }

        if (articleImportMapper.getCountByInvoiceAndCommissionStates(list) > 0) {
            throw new QinFeiException(1002, "导入的表格中有已提成或已开票的稿件，不支持修改报价，请核实后重试！");
        }

        int count = isMyArticleNum(AppUtil.getUser().getId(), list);
        if (list.size() > count) {
            throw new QinFeiException(1002, "导入的表格中有不属于自己的稿件");
        }

        try {
            if (historyList != null && historyList.size() > 0) {
                int size = historyList.size();
                int subLength = 100;
                // 计算需要插入的次数，100条插入一次；
                int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
                for (int i = 0; i < insertTimes; i++) {
                    List<ArticleHistory> insertData = new ArrayList<>();
                    // 计算起始位置，且j的最大值应不能超过数据的总数；
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                        insertData.add(historyList.get(j));

                    }
                    articleHistoryMapper.saveBatch(insertData);
                }
            }
        } catch (Exception e) {
            throw new QinFeiException(1002, "保存历史更改记录失败！");
        }
        try {
            int size = list.size();
            int subLength = 100;
            // 计算需要插入的次数，100条插入一次；
            int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1;
            for (int i = 0; i < insertTimes; i++) {
                List<Map<String, Object>> insertData = new ArrayList<>();
                // 计算起始位置，且j的最大值应不能超过数据的总数；
                for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {
                    insertData.add(list.get(j));
                }
                articleImportMapper.batchSaleAmount(insertData);
            }
        } catch (Exception e) {
            throw new QinFeiException(1002, "更新报价和品牌出错！");
        }

       /* //删除项目编号
        articleExtendMapper.deleteExtendBatch2(list);
        try{
            if(extendList!=null && extendList.size()>0){
                articleExtendMapper.saveExtendBatch(extendList);
            }
        }catch (Exception e){
            throw new QinFeiException(1002, "处理稿件扩展项目管理字段失败！");
        }*/
    }

    /**
     * 是自己的稿件的数量
     *
     * @param userId     用户id
     * @param articleIds 稿件id list
     * @return 稿件数量
     */
    public int isMyArticleNum(Integer userId, List<Map<String, Object>> articleIds) {
        return articleMapper.checkWhetherYourOwnManuscript(userId, articleIds);
    }
}
