package com.qinfei.qferp.service.impl.biz;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleHistory;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.media.FileEntitys;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.qinfei.qferp.entity.media1.MediaExtendParamJson;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.enumUtils.FilesEnum;
import com.qinfei.qferp.excelListener.ArticleExcel;
import com.qinfei.qferp.excelListener.ArticleExcelCSY;
import com.qinfei.qferp.excelListener.ArticleExcelMJ;
import com.qinfei.qferp.excelListener.ArticleExcelYW;
import com.qinfei.qferp.mapper.biz.ArticleHistoryMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapper;
import com.qinfei.qferp.mapper.biz.ArticleMapperXML;
import com.qinfei.qferp.mapper.biz.OrderMapper;
import com.qinfei.qferp.mapper.media.FileEntityMapper;
import com.qinfei.qferp.mapper.media1.MediaAuditMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.biz.IArticleService;
import com.qinfei.qferp.service.crm.IStatisticsService;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.service.media.IMediaTypeService;
import com.qinfei.qferp.service.media1.IMediaPlateService;
import com.qinfei.qferp.service.sys.IDeptService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.SysConfigUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ArticleService extends BaseService implements IArticleService {
    @Autowired
    ArticleMapperXML articleMapperXML;
    @Autowired
    ArticleMapper articleMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    IStatisticsService statisticsService;
    @Autowired
    IMediaTypeService mediaTypeService;
    @Autowired
    IUserService userService;
    @Autowired
    IDeptService deptService;
    // 获取配置；
    @Autowired
    private Config config;
    @Autowired
    private MediaAuditMapper mediaAuditMapper;
    @Autowired
    private FileEntityMapper fileEntityMapper;
    @Autowired
    private IMediaPlateService mediaPlateService;
    @Autowired
    ArticleHistoryMapper articleHistoryMapper;
    @Autowired
    UserMapper userMapper;

    @Value("${media.onlineTime}")
    private String onlineTime;

    /**
     * 反馈列表信息导出；
     *
     * @param map：查询参数；
     * @param companyName：客户公司名称，用于文件的标题显示；
     * @return ：文件下载路径；
     */
//    @Override
//    public String exportFeedBack(Map map, String companyName) {
//        // 获取查询结果；
//        List<Map<String, Object>> businessList = getBusinessList(map);
//        int size = businessList.size();
//        if (size > 0) {
//            // 拼接表头显示信息；
//            List<String> rowTitles = new ArrayList<>();
//            rowTitles.add("客户公司名称");
//            rowTitles.add("对接人名称");
//            rowTitles.add("媒体");
//            rowTitles.add("稿件标题");
//            rowTitles.add("稿件链接");
//            rowTitles.add("业绩金额（含税）");
//            rowTitles.add("发布时间");
//            rowTitles.add("负责人（业务员）");
//            // 处理数据；
//            List<Object[]> exportData = new ArrayList<>();
//            Object[] datas;
//            // 获取数据长度；
//            int dataLength = rowTitles.size();
//            for (Map<String, Object> data : businessList) {
//                datas = new Object[dataLength];
//                datas[0] = data.get("companyName");
//                datas[1] = data.get("dockingName");
//                datas[2] = data.get("mediaName");
//                datas[3] = data.get("title");
//                datas[4] = data.get("link");
//                datas[5] = data.get("saleAmount");
//                datas[6] = data.get("issuedDate");
//                datas[7] = data.get("userName");
//
//                // 增加到集合中；
//                exportData.add(datas);
//            }
//            return DataImportUtil.createFile(companyName + "反馈信息列表", config.getUploadDir(), config.getWebDir(), rowTitles, exportData);
//        } else {
//            return null;
//        }
//    }

    /**
     * 设置查询的权限范围；
     *
     * @param params：查询的参数集合；
     */
    private void setDeptAuth(Map params) {
        this.addSecurity(params);
        User user = AppUtil.getUser();
        if (!AppUtil.isRoleType(IConst.ROLE_TYPE_CW)) {
            params.put("selectType", AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && AppUtil.isRoleType(IConst.ROLE_TYPE_YW) ? "YWMJ" : (AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) ? IConst.ROLE_TYPE_MJ : IConst.ROLE_TYPE_YW));
        }
        if (!user.getCurrentDeptQx()) {
            if (AppUtil.isRoleType(IConst.ROLE_TYPE_MJ)) {
                params.put("mediaUserId", user.getId());
            }
            if (AppUtil.isRoleType(IConst.ROLE_TYPE_YW)) {
                params.put("userId", user.getId());
            }
        }
    }

    /**
     * 设置查询范围
     * 这个是给业务部权限用的deptYWList
     * 业务部查询稿件跟人走，其他部门查询稿件根据部门走，
     *
     * @param params
     */
    private void setAuth(Map params) {
        if (params.containsKey("userId")) {
            params.remove("deptId");
        } else {
            if (params.get("deptId") != null) {//如果选择了部门，就查询选择的部门数据
                Integer deptId = Integer.parseInt((String) params.get("deptId"));
                List<Dept> deptList = deptService.listByParentId(deptId);
                params.put("deptYWList", deptList);
            } else {//一般都会串deptId过来，下面的用不到
                User user = AppUtil.getUser();
                if (user.getCurrentDeptQx()) {//没有选择部门，如果是组部长，就查询权限内所有的部门数据
                    Integer deptId = user.getDeptId();
                    String deptIds = "";
                    if (deptId != null) {
                        deptIds = userService.getChilds(deptId);
                        if (deptIds.indexOf("$,") > -1) {
                            deptIds = deptIds.substring(2);
                        }
                        params.put("deptYWIds", deptIds);
                    }
                } else {//如果是业务员，就只查询自己的稿件，因为存在业务员调岗的情况，所以不能放deptIds,只能放userId
                    params.put("userId", user.getId());
                }
            }
        }
    }

    @Override
    public PageInfo<Map<String, Object>> articleList(Map params, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String, Object>> list = articleMapperXML.articleList(params);
        if(CollectionUtils.isNotEmpty(list)){
            for (Map<String, Object> supplier : list){
                if(supplier.get("supplierPhone") != null){
                    supplier.put("supplierPhone",EncryptUtils.decrypt(supplier.get("supplierPhone").toString()));
                }
            }
        }
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map<String, Object>> articleListCSY(Map params, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(articleMapperXML.articleListCSY(params));
    }

    @Override
    public PageInfo<Map<String, Object>> articleListYW(Map params, Pageable pageable) {
        setAuth(params);
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(articleMapperXML.articleListYW(params));
    }

    @Override
    public PageInfo<Map<String, Object>> articleListMJ(Map params, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Map<String, Object>> list = articleMapperXML.articleListMJ(params);
        for (int i=0;i<list.size();i++){
            //解密电话
            if(list.get(i).get("supplierPhone")!=null){
               String phone = EncryptUtils.decrypt(list.get(i).get("supplierPhone").toString());
               list.get(i).put("supplierPhone",phone);
            }
        }
        return new PageInfo<>(list);
    }

    @Override
    public Map articleResult(Map map) {
        Map<String, Object> resultMap = articleMapperXML.articleResult(map);
        return initResult(resultMap);
    }

    @Override
    public Map articleResultCSY(Map map) {
        Map<String, Object> resultMap = articleMapperXML.articleResultCSY(map);
        return initResult(resultMap);
    }

    @Override
    public Map articleResultYW(Map map) {
        setAuth(map);
        Map<String, Object> resultMap = articleMapperXML.articleResultYW(map);
        return initResult(resultMap);
    }

    @Override
    public Map articleResultMJ(Map map) {
        Map<String, Object> resultMap = articleMapperXML.articleResultMJ(map);
        return initResult(resultMap);
    }

    private Map initResult(Map resultMap) {
        if (resultMap == null || resultMap.size() == 0) {
            resultMap = new HashMap<>();
            resultMap.put("saleAmountSum", 0);
            resultMap.put("incomeAmountSum", 0);
            resultMap.put("payAmountSum", 0);
            resultMap.put("outgoAmountSum", 0);
            resultMap.put("refundAmountSum", 0);
            resultMap.put("taxesSum", 0);
            resultMap.put("otherPaySum", 0);
            resultMap.put("profitSum", 0);
            resultMap.put("commissionSum", 0);
        }
        return resultMap;
    }

    private List<ArticleExcel> doWithEasyExcel(List<Map> list) {
        List<ArticleExcel> excelList = new ArrayList<>();
        for (Map temp : list) {
            ArticleExcel articleExcel = new ArticleExcel();
            String messDetails = "";
            if (!ObjectUtils.isEmpty(temp.get("messState"))) {
                if (Integer.parseInt(temp.get("messState").toString()) == 0) {
                    messDetails = "无烂账";
                } else if (Integer.parseInt(temp.get("messState").toString()) == 1) {
                    messDetails = "已烂账";
                } else {
                    messDetails = "烂账中";
                }
            }
//            articleExcel.setId(i);
            articleExcel.setCreateTime(ObjectUtils.isEmpty(temp.get("createTime")) ? null : MapUtils.getString(temp, "createTime").substring(0, 10));
            articleExcel.setDeptName(MapUtils.getString(temp, "deptName"));
            articleExcel.setUserName(MapUtils.getString(temp, "userName"));
            articleExcel.setMediaUserName(MapUtils.getString(temp, "mediaUserName"));
            articleExcel.setDockingName(MapUtils.getString(temp, "dockingName"));
            articleExcel.setBrand(MapUtils.getString(temp, "brand"));
            articleExcel.setCompanyName(MapUtils.getString(temp, "companyName"));
            articleExcel.setCustCompanyType(MapUtils.getString(temp, "custCompanyType"));
            articleExcel.setMediaTypeName(MapUtils.getString(temp, "mTypeName"));
            articleExcel.setMediaName(MapUtils.getString(temp, "mediaName"));
            articleExcel.setTitle(MapUtils.getString(temp, "title"));
            articleExcel.setLink(MapUtils.getString(temp, "link"));
            articleExcel.setTypeName(MapUtils.getString(temp, "typeName"));
            articleExcel.setIssuedDate(ObjectUtils.isEmpty(temp.get("issuedDate")) ? null : MapUtils.getString(temp, "issuedDate").substring(0, 10));
            articleExcel.setNum(ObjectUtils.isEmpty(temp.get("num")) ? 1 : Integer.parseInt(String.valueOf(temp.get("num"))));
            articleExcel.setSaleAmount(ObjectUtils.isEmpty(temp.get("saleAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("saleAmount"))));
            articleExcel.setIncomeAmount(ObjectUtils.isEmpty(temp.get("incomeAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("incomeAmount"))));
            articleExcel.setPromiseDate(ObjectUtils.isEmpty(temp.get("promiseDate")) ? null : MapUtils.getString(temp, "promiseDate").substring(0, 10));
            articleExcel.setIncomeCode(MapUtils.getString(temp, "incomeCode"));
            articleExcel.setIncomeAccount(MapUtils.getString(temp, "incomeAccount"));
            articleExcel.setIncomeMan(MapUtils.getString(temp, "incomeMan"));
            articleExcel.setIncomeDate(ObjectUtils.isEmpty(temp.get("incomeDate")) ? null : MapUtils.getString(temp, "incomeDate").substring(0, 10));
            articleExcel.setIncomeTotalAmount(ObjectUtils.isEmpty(temp.get("incomeTotalAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("incomeTotalAmount"))));
            articleExcel.setAssignDate(ObjectUtils.isEmpty(temp.get("assignDate")) ? null : MapUtils.getString(temp, "assignDate").substring(0, 10));
            articleExcel.setTaxes(ObjectUtils.isEmpty(temp.get("taxes")) ? 0 : Double.parseDouble(String.valueOf(temp.get("taxes"))));
            articleExcel.setRefundAmount(ObjectUtils.isEmpty(temp.get("refundAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("refundAmount"))));
            articleExcel.setOtherPay(ObjectUtils.isEmpty(temp.get("otherPay")) ? 0 : Double.parseDouble(String.valueOf(temp.get("otherPay"))));
            articleExcel.setOutgoAmount(ObjectUtils.isEmpty(temp.get("outgoAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgoAmount"))));
            articleExcel.setOutgoCode(MapUtils.getString(temp, "outgoCode"));
            articleExcel.setOutgoTotalAmount(ObjectUtils.isEmpty(temp.get("outgoTotalAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgoTotalAmount"))));
            articleExcel.setProfit(ObjectUtils.isEmpty(temp.get("profit")) ? null : Double.parseDouble(String.valueOf(temp.get("profit"))));
            articleExcel.setCommission(ObjectUtils.isEmpty(temp.get("commission")) ? 0 : Double.parseDouble(String.valueOf(temp.get("commission"))));
            articleExcel.setYear(ObjectUtils.isEmpty(temp.get("year")) ? null : Integer.parseInt(String.valueOf(temp.get("year"))));
            articleExcel.setMonth(ObjectUtils.isEmpty(temp.get("month")) ? null : Integer.parseInt(String.valueOf(temp.get("month"))));
            articleExcel.setSupplierName(MapUtils.getString(temp, "supplierName"));
            articleExcel.setSupplierContactor(MapUtils.getString(temp, "supplierContactor"));
            if(ObjectUtils.isEmpty(MapUtils.getString(temp,"supplierPhone"))){
                articleExcel.setSupplierPhone("");
            }else {
                String phone = EncryptUtils.decrypt(MapUtils.getString(temp,"supplierPhone"));
                if (phone.length() >= 11) {
                    String start = phone.length() > 11 ? "*****" : "****";
                    phone = phone.substring(0, 3) + start + phone.substring(phone.length() - 4, phone.length());
                } else if(phone.length()<11 && phone.length()>=3) {
                    phone = phone.substring(0,1) + "***" + phone.substring(phone.length()-1);
                }else {
                    phone = "**";
                }
                articleExcel.setSupplierPhone(phone);
            }
            articleExcel.setElectricityBusinesses(MapUtils.getString(temp, "electricityBusinesses"));
            articleExcel.setChannel(MapUtils.getString(temp, "channel"));
            articleExcel.setInnerOuter(MapUtils.getString(temp, "innerOuter"));
//            articleExcel.setProjectCode(MapUtils.getString(temp,"projectCode"));
//            articleExcel.setProjectName(MapUtils.getString(temp,"projectName"));
            articleExcel.setRemarks(MapUtils.getString(temp, "remarks"));
            articleExcel.setMessState(messDetails);
            excelList.add(articleExcel);
        }
        return excelList;
    }

    @Override
    public List<ArticleExcel> articleListPage(Map map) {
        List list = articleMapperXML.articleListPage(map);
        return doWithEasyExcel(list);
    }

    @Override
    public int articleListCount(Map map) {
        return articleMapperXML.articleListCount(map);
    }

    @Override
    public int articleListCountCSY(Map map) {
        return articleMapperXML.articleListCountCSY(map);
    }

    @Override
    public List<ArticleExcelCSY> articleListPageCSY(Map map) {
        List list = articleMapperXML.articleListPageCSY(map);
        return doWithEasyExcelCSY(list);
    }

    private List<ArticleExcelCSY> doWithEasyExcelCSY(List<Map> list) {
        List<ArticleExcelCSY> excelList = new ArrayList<>();
        for (Map temp : list) {
            String messDetails = "";
            if (!ObjectUtils.isEmpty(temp.get("messState"))) {
                if (Integer.parseInt(temp.get("messState").toString()) == 0) {
                    messDetails = "无烂账";
                } else if (Integer.parseInt(temp.get("messState").toString()) == 1) {
                    messDetails = "已烂账";
                } else {
                    messDetails = "烂账中";
                }
            }
            ArticleExcelCSY articleExcel = new ArticleExcelCSY();
//            articleExcel.setId(i);
            articleExcel.setDeptName(MapUtils.getString(temp, "deptName"));
            articleExcel.setUserName(MapUtils.getString(temp, "userName"));
            articleExcel.setMediaUserName(MapUtils.getString(temp, "mediaUserName"));
            articleExcel.setDockingName(MapUtils.getString(temp, "dockingName"));
            articleExcel.setBrand(MapUtils.getString(temp, "brand"));
            articleExcel.setCompanyName(MapUtils.getString(temp, "companyName"));
            articleExcel.setCustCompanyType(MapUtils.getString(temp, "custCompanyType"));
            articleExcel.setMediaTypeName(MapUtils.getString(temp, "mTypeName"));
            articleExcel.setMediaName(MapUtils.getString(temp, "mediaName"));
            articleExcel.setTitle(MapUtils.getString(temp, "title"));
            articleExcel.setLink(MapUtils.getString(temp, "link"));
            articleExcel.setTypeName(MapUtils.getString(temp, "typeName"));
            articleExcel.setIssuedDate(ObjectUtils.isEmpty(temp.get("issuedDate")) ? null : MapUtils.getString(temp, "issuedDate").substring(0, 10));
            articleExcel.setSaleAmount(ObjectUtils.isEmpty(temp.get("saleAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("saleAmount"))));
            articleExcel.setIncomeAmount(ObjectUtils.isEmpty(temp.get("incomeAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("incomeAmount"))));
            articleExcel.setPromiseDate(ObjectUtils.isEmpty(temp.get("promiseDate")) ? null : MapUtils.getString(temp, "promiseDate").substring(0, 10));
            articleExcel.setTaxes(ObjectUtils.isEmpty(temp.get("taxes")) ? 0 : Double.parseDouble(String.valueOf(temp.get("taxes"))));
            articleExcel.setOutgoAmount(ObjectUtils.isEmpty(temp.get("outgoAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgoAmount"))));
            articleExcel.setRemarks(MapUtils.getString(temp, "remarks"));
            articleExcel.setElectricityBusinesses(MapUtils.getString(temp, "electricityBusinesses"));
            articleExcel.setChannel(MapUtils.getString(temp, "channel"));
            articleExcel.setInnerOuter(MapUtils.getString(temp, "innerOuter"));
            articleExcel.setMessState(messDetails);
            excelList.add(articleExcel);
        }
        return excelList;
    }

    @Override
    public int articleListCountYW(Map map) {
        setAuth(map);
        return articleMapperXML.articleListCountYW(map);
    }

    @Override
    public List<ArticleExcelYW> articleListPageYW(Map map) {
        setAuth(map);
        List list = articleMapperXML.articleListPageYW(map);
        return doWithEasyExcelYW(list);
    }

    private List<ArticleExcelYW> doWithEasyExcelYW(List<Map> list) {
        List<ArticleExcelYW> excelList = new ArrayList<>();
        //如果用户是项目总监，则只能看到自己客户的对接人
        List<String> PD = SysConfigUtils.getConfigValue("projectDirector", List.class);
        boolean b = false;
        boolean a = false;
        Integer userId = AppUtil.getUser().getId();
        if (CollectionUtils.isNotEmpty(PD) && PD.contains(userId.toString())) {
            b = true;
        }
        for (Map temp : list) {
            String messDetails = "";
            if (!ObjectUtils.isEmpty(temp.get("messState"))) {
                if (Integer.parseInt(temp.get("messState").toString()) == 0) {
                    messDetails = "无烂账";
                } else if (Integer.parseInt(temp.get("messState").toString()) == 1) {
                    messDetails = "已烂账";
                } else {
                    messDetails = "烂账中";
                }
            }
            //因为完善客户只能使用自己的客户 和 只能完善自己的稿件 ， 所以稿件的业务员和用户不同 并且为项目经理时 看不到  客户对接人
            if (b && !userId.toString().equals(temp.get("userId").toString())) {
                a = true;
            }
            ArticleExcelYW articleExcel = new ArticleExcelYW();
            articleExcel.setArtId(MapUtils.getString(temp, "artId"));
            articleExcel.setPromiseDate(ObjectUtils.isEmpty(temp.get("promiseDate")) ? null : MapUtils.getString(temp, "promiseDate").substring(0, 10));
            articleExcel.setCompanyName(MapUtils.getString(temp, "companyName"));
            articleExcel.setDockingName(a == true ? "*****" : MapUtils.getString(temp, "dockingName"));
            articleExcel.setMediaTypeName(MapUtils.getString(temp, "mTypeName"));
            articleExcel.setUserName(MapUtils.getString(temp, "userName"));
            articleExcel.setMediaUserName(MapUtils.getString(temp, "mediaUserName"));
            articleExcel.setMediaName(MapUtils.getString(temp, "mediaName"));
            articleExcel.setIssuedDate(ObjectUtils.isEmpty(temp.get("issuedDate")) ? null : MapUtils.getString(temp, "issuedDate").substring(0, 10));
            articleExcel.setTitle(MapUtils.getString(temp, "title"));
            articleExcel.setLink(MapUtils.getString(temp, "link"));
            articleExcel.setNum(ObjectUtils.isEmpty(temp.get("num")) ? 1 : Integer.parseInt(String.valueOf(temp.get("num"))));
            articleExcel.setBrand(MapUtils.getString(temp, "brand"));
            articleExcel.setSaleAmount(ObjectUtils.isEmpty(temp.get("saleAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("saleAmount"))));
            articleExcel.setIncomeAmount(ObjectUtils.isEmpty(temp.get("incomeAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("incomeAmount"))));
            articleExcel.setOutgoAmount(ObjectUtils.isEmpty(temp.get("outgoAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgoAmount"))));
            articleExcel.setTaxes(ObjectUtils.isEmpty(temp.get("taxes")) ? 0 : Double.parseDouble(String.valueOf(temp.get("taxes"))));
            articleExcel.setRefundAmount(ObjectUtils.isEmpty(temp.get("refundAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("refundAmount"))));
            articleExcel.setOtherPay(ObjectUtils.isEmpty(temp.get("otherPay")) ? 0 : Double.parseDouble(String.valueOf(temp.get("otherPay"))));
            articleExcel.setProfit(ObjectUtils.isEmpty(temp.get("profit")) ? null : Double.parseDouble(String.valueOf(temp.get("profit"))));
            articleExcel.setCommission(ObjectUtils.isEmpty(temp.get("commission")) ? 0 : Double.parseDouble(String.valueOf(temp.get("commission"))));
            articleExcel.setYear(ObjectUtils.isEmpty(temp.get("year")) ? null : Integer.parseInt(String.valueOf(temp.get("year"))));
            articleExcel.setMonth(ObjectUtils.isEmpty(temp.get("month")) ? null : Integer.parseInt(String.valueOf(temp.get("month"))));
            articleExcel.setCustCompanyType(MapUtils.getString(temp, "custCompanyType"));
            articleExcel.setTypeName(MapUtils.getString(temp, "typeName"));
            articleExcel.setElectricityBusinesses(MapUtils.getString(temp, "electricityBusinesses"));
            articleExcel.setChannel(MapUtils.getString(temp, "channel"));
            articleExcel.setInnerOuter(MapUtils.getString(temp, "innerOuter"));
//            articleExcel.setProjectCode(MapUtils.getString(temp,"projectCode"));
//            articleExcel.setProjectName(MapUtils.getString(temp,"projectName"));
            articleExcel.setRemarks(MapUtils.getString(temp, "remarks"));
            articleExcel.setMessState(messDetails);
            excelList.add(articleExcel);
        }
        return excelList;
    }

    @Override
    public int articleListCountMJ(Map map) {
        return articleMapperXML.articleListCountMJ(map);
    }

    @Override
    public List<ArticleExcelMJ> articleListPageMJ(Map map) {
        List list = articleMapperXML.articleListPageMJ(map);
        return doWithEasyExcelMJ(list);
    }

    private List<ArticleExcelMJ> doWithEasyExcelMJ(List<Map> list) {
        List<ArticleExcelMJ> excelList = new ArrayList<>();
        for (Map temp : list) {
            ArticleExcelMJ articleExcel = new ArticleExcelMJ();
            articleExcel.setMediaTypeName(MapUtils.getString(temp, "mTypeName"));
            articleExcel.setUserName(MapUtils.getString(temp, "userName"));
            articleExcel.setCompanyCodeName(MapUtils.getString(temp, "companyCodeName"));
            articleExcel.setMediaName(MapUtils.getString(temp, "mediaName"));
            articleExcel.setMediaUserName(MapUtils.getString(temp, "mediaUserName"));
            articleExcel.setSupplierName(MapUtils.getString(temp, "supplierName"));
            articleExcel.setSupplierContactor(MapUtils.getString(temp, "supplierContactor"));
            if(ObjectUtils.isEmpty(MapUtils.getString(temp,"supplierPhone"))){
                articleExcel.setSupplierPhone("");
            }else {
                String phone = EncryptUtils.decrypt(MapUtils.getString(temp,"supplierPhone"));
                if (phone.length() >= 11) {
                    String start = phone.length() > 11 ? "*****" : "****";
                    phone = phone.substring(0, 3) + start + phone.substring(phone.length() - 4, phone.length());
                } else if(phone.length()<11 && phone.length()>=3) {
                   phone = phone.substring(0,1) + "***" + phone.substring(phone.length()-1);
                }else {
                    phone = "**";
                }
                articleExcel.setSupplierPhone(phone);
            }
            articleExcel.setElectricityBusinesses(MapUtils.getString(temp, "electricityBusinesses"));
            articleExcel.setChannel(MapUtils.getString(temp, "channel"));
            articleExcel.setInnerOuter(MapUtils.getString(temp, "innerOuter"));
            articleExcel.setTitle(MapUtils.getString(temp, "title"));
            articleExcel.setLink(MapUtils.getString(temp, "link"));
            articleExcel.setIssuedDate(ObjectUtils.isEmpty(temp.get("issuedDate")) ? null : MapUtils.getString(temp, "issuedDate").substring(0, 10));
            articleExcel.setNum(ObjectUtils.isEmpty(temp.get("num")) ? 1 : Integer.parseInt(String.valueOf(temp.get("num"))));
            articleExcel.setPriceType(MapUtils.getString(temp, "priceType"));
            articleExcel.setOtherExpense(ObjectUtils.isEmpty(temp.get("otherExpense")) ? 0 : Double.parseDouble(String.valueOf(temp.get("otherExpense"))));
            articleExcel.setOutgoStatus(MapUtils.getString(temp, "outgoStatus"));
            articleExcel.setSaleAmount(ObjectUtils.isEmpty(temp.get("saleAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("saleAmount"))));
            articleExcel.setOutgoAmount(ObjectUtils.isEmpty(temp.get("outgoAmount")) ? 0 : Double.parseDouble(String.valueOf(temp.get("outgoAmount"))));
            articleExcel.setUnitPrice(ObjectUtils.isEmpty(temp.get("unitPrice")) ? 0 : Double.parseDouble(String.valueOf(temp.get("unitPrice"))));
            articleExcel.setRemarks(MapUtils.getString(temp, "remarks"));
            articleExcel.setArtId(MapUtils.getInteger(temp, "artId"));
            excelList.add(articleExcel);
        }
        return excelList;
    }

    @Override
    public Map editArticle(Map map) {
        Map obj = articleMapperXML.editArticle(map);
        //电话解码
        if(obj.get("supplierPhone")!=null){
            String phone = EncryptUtils.decrypt(obj.get("supplierPhone").toString());
            obj.put("supplierPhone",phone);
        }
        return obj;
    }

    @Override
    @Transactional
    public int add(Order order, Article article, MultipartFile[] files) {
        User user = AppUtil.getUser();
        Integer orderId;
        User YWUser = userService.getById(order.getUserId());
        List<Order> orders = orderMapper.getOrderByUserId(order.getUserId(), YWUser.getDeptId());
        if (orders != null && orders.size() > 0) {
            orderId = orders.get(0).getId();
        } else {
            order = new Order();
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
        if (article.getLink() == null) {
            article.setLink("");
        }
        article.setOrderId(orderId);
        article.setUpdateTime(new Date());
        article.setCreateTime(new Date());
        article.setCreator(user.getId());
        article.setState(IConst.STATE_FINISH);
        article.setIssueStates(4);
        article.setProfit(-article.getOutgoAmount());
        int num = articleMapper.insert(article);
        if (files != null && files.length > 0) {
            //添加稿件差价附件
            FileEntitys fileEntitys = new FileEntitys();
            //文件父路径
            String childPath = DateUtils.format(new Date(), "/yyyy/MM/dd") + "/article/";
            //将文件名和路径拼装成字符串
            List<String> fileNames = new ArrayList<>();
            List<String> filePaths = new ArrayList<>();
            if (files != null && files.length > 0) {
                for (MultipartFile multipartFile : files) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        try {
                            multipartFile.transferTo(destFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fileNames.add(multipartFile.getOriginalFilename());
                        filePaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
            }
            fileEntitys.setFilesName(fileNames.toString().replaceAll("\\[|\\]", ""));
            fileEntitys.setFilesLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            fileEntitys.setRelevanceId(article.getId());
            fileEntitys.setCreateId(user.getId());
            fileEntitys.setCreateName(user.getName());
            fileEntitys.setCreateTime(new Date());
            fileEntitys.setType(FilesEnum.ARTICLEDIFFILES.getType());
            fileEntitys.setUpdateId(user.getId());
            fileEntitys.setUpdateName(user.getName());
            fileEntitys.setState(0);
            fileEntityMapper.insertSelective(fileEntitys);
        }
        return num;
    }

    @Override
    public int updateArticle(Map map) {
        // 获取稿件id；
        Object object = map.get("artId");
        if (ObjectUtils.isEmpty(object)) {
            throw new QinFeiException(1002, "未获取到稿件id");
        }
        Integer id = Integer.parseInt(object.toString());
        Article article = articleMapper.get(Article.class, id);
        Order order = orderMapper.get(Order.class, article.getOrderId());
        if (ObjectUtils.isEmpty(article)) {
            throw new QinFeiException(1002, "未获取到稿件id");
        }
        // 获取登录用户；
        User user = AppUtil.getUser();
        if (ObjectUtils.isEmpty(user)) {
            throw new QinFeiException(1002, "未获取到登录信息");
        }
        String curMonth = DateUtils.getYearAndMonthStr2(new Date());//当前年月
        String issueMonth = "";
        if (map.get("issuedDate") != null) {
            issueMonth = DateUtils.getYearAndMonthStr2(DateUtils.parse(String.valueOf(map.get("issuedDate")).replaceAll("/", "-"), "yyyy-MM-dd"));//发布日期年月
        } else {
            if (article.getIssuedDate() != null) {
                issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
            }
        }

        Boolean isYW = userService.queryRoleByUserIdAndRoleType(user.getId(), IConst.ROLE_TYPE_YW);
        Boolean isMJ = userService.queryRoleByUserIdAndRoleType(user.getId(), IConst.ROLE_TYPE_MJ);
        // 当前登录人为不能是业务员；
        if (!isYW) {
            // 获取数据状态；
            Integer outgoStates = article.getOutgoStates();
            Integer commissionStates = article.getCommissionStates();
            Integer incomeStates = article.getIncomeStates();
            Double outgoAmount;
            try {
                Object outgoObject = map.get("outgoAmount");
                if (null != outgoObject) {
                    outgoAmount = Double.parseDouble(outgoObject.toString());
                } else {
                    throw new QinFeiException(1002, "获取执行后价格失败！");
                }
            } catch (Exception e) {
                throw new QinFeiException(1002, "获取的执行后价格不正确，请确认后重试！");
            }

            // 00：未请款且操作人不是业务员，所有内容均可修改；
            if (outgoStates == 0 && commissionStates == 0) {// 00：未提成未请款，都能改；
                //存更改记录表
                ArticleHistory history = new ArticleHistory();
                BeanUtils.copyProperties(article, history);
                history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                history.setUserId(order.getUserId());
                history.setDeptId(order.getDepatId());
                history.setId(null);
                history.setArtId(article.getId());
                history.setEditDesc(IConst.article_change_media_edit);
                history.setCreator(user.getId());
                history.setCreateTime(new Date());


                Double profit;
                if (incomeStates == IConst.FEE_STATE_FINISH) {
                    profit = article.getIncomeAmount() - outgoAmount - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                    map.put("profit", profit);
                } else {
                    profit = article.getSaleAmount() - outgoAmount - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                    map.put("profit", profit);
                }
//              满足三个条件报价就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置报价，3、报价有变动
                Double alterOutgo = outgoAmount - article.getOutgoAmount();
                if (!(curMonth.equals(issueMonth))
                        && article.getOutgoAmount() > 0
                        && Math.abs(alterOutgo) > 0.01) {
                    map.put("alterFlag", IConst.article_alter_flag_true);
                    history.setAlterOutgo(alterOutgo);
                    Double alterProfit = profit - article.getProfit();
                    history.setAlterProfit(alterProfit);
                    history.setAlterLabel(IConst.article_alter_flag_true);
                }
                articleHistoryMapper.insert(history);
                if (StringUtils.isEmpty(map.get("supplierId"))) {
                    map.put("supplierId", null);
                }
                return articleMapperXML.updateArticle(map);
            } else if ((outgoStates == 0 && commissionStates > 0) || (outgoStates > 0 && commissionStates == 0)) {//// 01或10：已请款或已提成，只能改链接和发布日期；
                //存更改记录表
                ArticleHistory history = new ArticleHistory();
                BeanUtils.copyProperties(article, history);
                history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                history.setUserId(order.getUserId());
                history.setDeptId(order.getDepatId());
                history.setId(null);
                history.setArtId(article.getId());
                history.setEditDesc(IConst.article_change_media_edit);
                history.setCreator(user.getId());
                history.setCreateTime(new Date());
                articleHistoryMapper.insert(history);

                Map data = new HashMap();
                data.put("artId", id);
                data.put("link", map.get("link"));
                data.put("remarks", map.get("remarks"));
                data.put("issuedDate", map.get("issuedDate"));
                data.put("innerOuter", map.get("innerOuter"));
                data.put("channel", map.get("channel"));
                data.put("electricityBusinesses", map.get("electricityBusinesses"));
                data.put("supplierId", map.get("supplierId"));
                if (incomeStates == IConst.FEE_STATE_FINISH) {
                    Double profit = article.getIncomeAmount() - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                    data.put("profit", profit);
                } else {
                    Double profit = article.getSaleAmount() - article.getOutgoAmount() - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                    data.put("profit", profit);
                }
                return articleMapperXML.updateArticle(data);
            }
        } else if (!isMJ) {// 当前角色不能是媒介
            // 获取数据状态；
            int invoiceStates = article.getInvoiceStates();
            Integer commissionStates = article.getCommissionStates();
            // 00：未提成未开票且操作人不是媒介，所有内容均可修改；
            if (invoiceStates == 0 && commissionStates == 0) {
                Double saleAmount;
                try {
                    Object saleObject = map.get("saleAmount");
                    if (null != saleObject) {
                        saleAmount = Double.parseDouble(saleObject.toString());
                    } else {
                        throw new QinFeiException(1002, "获取报价失败，请确认报价后重试！");
                    }
                } catch (Exception e) {
                    throw new QinFeiException(1002, "获取报价出错，请确认报价后重试！");
                }
                //稿件有回款时更改报价，修改的报价金额不能小于回款金额
                if (article.getIncomeAmount() > 0 && (article.getIncomeAmount() - saleAmount) > 0.01) {
                    throw new QinFeiException(1002, "稿件有回款时更改报价，修改的报价金额不能小于回款金额!");
                }
                //存更改记录表
                ArticleHistory history = new ArticleHistory();
                BeanUtils.copyProperties(article, history);
                history.setCompanyCode(articleMapper.getCompanyCodeByArtId(article.getId()));
                history.setUserId(order.getUserId());
                history.setDeptId(order.getDepatId());
                history.setArtId(article.getId());
                history.setId(null);
                history.setEditDesc(IConst.article_change_business_edit);
                history.setCreator(user.getId());
                history.setCreateTime(new Date());

                //处理业务
                Map data = new HashMap();
                data.put("artId", id);
                data.put("supplierId", article.getSupplierId());
                data.put("supplierName", article.getSupplierName());
                data.put("supplierContactor", article.getSupplierContactor());
                data.put("taxType", ObjectUtils.isEmpty(map.get("taxType")) ? 0 : map.get("taxType"));
                Double taxes;
                try {
                    Object taxesObject = map.get("taxes");
                    if (null != taxesObject) {
                        taxes = Double.parseDouble(taxesObject.toString());
                    } else {
                        throw new QinFeiException(1002, "获取税金失败！");
                    }
                } catch (Exception e) {
                    throw new QinFeiException(1002, "获取税金出错！");
                }
                data.put("taxes", taxes);

//              满足三个条件税金就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置税金，3、税金有变动
                Double alterTax = taxes - article.getTaxes();
                if (!(curMonth.equals(issueMonth))
                        && Math.abs(alterTax) > 0.01) {
                    data.put("alterFlag", IConst.article_alter_flag_true);
                    history.setAlterTax(alterTax);
                    history.setAlterLabel(IConst.article_alter_flag_true);
                }


//              满足三个条件报价就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置报价，3、报价有变动
                Double alterSale = saleAmount - article.getSaleAmount();
                if (!(curMonth.equals(issueMonth))
                        && article.getSaleAmount() > 0
                        && Math.abs(alterSale) > 0.01) {
                    data.put("alterFlag", IConst.article_alter_flag_true);
                    history.setAlterSale(alterSale);
                    history.setAlterLabel(IConst.article_alter_flag_true);
                }
                data.put("saleAmount", saleAmount);
                Double incomeAmount = article.getIncomeAmount();
                Double profit = 0D;
                if (incomeAmount < 0.01) {
                    data.put("incomeStates", IConst.FEE_STATE_SAVE);
                    profit = saleAmount - article.getOutgoAmount() - taxes - article.getRefundAmount() - article.getOtherPay();
                } else if (incomeAmount < (saleAmount - 1)) {
                    //少于1元也算已回款
                    data.put("incomeStates", IConst.FEE_STATE_PROCESS);
                    profit = saleAmount - article.getOutgoAmount() - taxes - article.getRefundAmount() - article.getOtherPay();
                } else {
                    data.put("incomeStates", IConst.FEE_STATE_FINISH);
                    profit = article.getIncomeAmount() - article.getOutgoAmount() - taxes - article.getRefundAmount() - article.getOtherPay();
                }
                data.put("profit", profit);
                if (IConst.article_alter_flag_true.equals(history.getAlterLabel())) {
                    Double alterProfit = profit - article.getProfit();
                    history.setAlterProfit(alterProfit);
                }
                data.put("brand", map.get("brand"));
                data.put("typeCode", map.get("typeCode"));
                data.put("typeName", map.get("typeName"));
                data.put("promiseDate", map.get("promiseDate"));
                data.put("remarks", map.get("remarks"));
                data.put("electricityBusinesses", map.get("electricityBusinesses"));
                data.put("innerOuter", map.get("innerOuter"));
                data.put("channel", map.get("channel"));
                articleHistoryMapper.insert(history);
                return articleMapperXML.updateArticle(data);
            }
        }
        return 0;
    }

    @Transactional
    @Override
    public Map<String, Object> deleteArticle(Integer id) {
        User user = AppUtil.getUser();
        List<Integer> list = new ArrayList<>();
        list.add(id);
        Map<String, Object> map = new HashMap();
        StringBuffer sb = new StringBuffer();
        if (articleMapper.getIncomeInfoByArticleIds(list) > 0) {
            sb.append("稿件已有进账信息，无法删除！");
        }
        if (articleMapper.getOutgoInfoByArticleIds(list) > 0) {
            sb.append("稿件已有请款信息，无法删除！");
        }
        if (articleMapper.getInvoiceInfoByArticleIds(list) > 0) {
            sb.append("稿件已有开票信息，无法删除！");
        }
        if (articleMapper.getRefundInfoByArticleIds(list) > 0) {
            sb.append("稿件已有退款信息，无法删除！");
        }
        if (articleMapper.getMessAccountIds(list) > 0) {
            sb.append("稿件已有烂账信息，无法删除！");
        }
        if (StringUtils.isEmpty(sb.toString())) {
            int row = articleMapperXML.deleteArticle(id, user.getId());
            if (row == 0) {
                sb.append("只有稿件中的媒介才能删除稿件！");
            }
            map.put("row", row);
        } else {
            int row = 0;
            map.put("row", row);
        }
        map.put("message", sb.toString());
        return map;
    }

    @Override
    @Transactional
    public void deleteArticleByOrderId(Integer orderId) {
        articleMapperXML.deleteArticleByOrderId(orderId);
    }

    @Override
    @Transactional
    public Map<String, Object> batchDelete(String datas) throws Exception {
        User user = AppUtil.getUser();
        List<Article> articleList = JSON.parseArray(datas, Article.class);
        List<Integer> list = new ArrayList<>();
        for (Article article : articleList) {
            list.add(article.getId());
        }
        Map<String, Object> map = new HashMap();
        StringBuffer sb = new StringBuffer();
        if (articleMapper.getIncomeInfoByArticleIds(list) > 0) {
            sb.append("稿件已有进账信息，无法删除！");
        }
        if (articleMapper.getOutgoInfoByArticleIds(list) > 0) {
            sb.append("稿件已有请款信息，无法删除！");
        }
        if (articleMapper.getInvoiceInfoByArticleIds(list) > 0) {
            sb.append("稿件已有开票信息，无法删除！");
        }
        if (articleMapper.getRefundInfoByArticleIds(list) > 0) {
            sb.append("稿件已有退款信息，无法删除！");
        }
        if (articleMapper.getMessAccountIds(list) > 0) {
            sb.append("稿件已有烂账信息，无法删除！");
        }
        if (StringUtils.isEmpty(sb.toString())) {
            int row = articleMapper.deleteBatchArticle(list, user.getId());
            int size = list.size();
            int fail = size - row;
            if (list.size() != row) {
                sb.append("成功删除了" + row + "条，失败了" + fail + "条，只有稿件中的媒介才能删除稿件！");
            } else {
                sb.append("成功删除了" + row + "条稿件！");
            }
            map.put("row", row);
        } else {
            int row = 0;
            map.put("row", row);
        }
        map.put("message", sb.toString());
        return map;
    }

    @Override
    public Article save(Article article) {
        Integer id = articleMapper.insert(article);
        article.setId(id);
        return article;
    }

    @Override
    public void update(Article article) {
        articleMapper.update(article);
    }

    @Override
    public boolean saveBatch(List<Article> article) {
        articleMapperXML.saveBatch(article);
        return true;
    }

    @Override
    public boolean updateBatch(List<Article> article) {
        articleMapperXML.updateBatch(article);
        return true;
    }

    @Override
    public PageInfo<Article> listByOrderId(Integer orderId, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<Article> list = articleMapper.listByOrderId(orderId);
        //稿件表含有media_type_id 和 media_type_name字段
       /* list.forEach(article -> {
            article.setMediaType(mediaPlateService.getByMediaId(article.getMediaId()));//mediaTypeService.getByMediaId(article.getMediaId())
        });*/
        return new PageInfo(list);
    }

    @Override
    @Transactional
    @Cacheable(value = CACHE_KEY_LIST, key = "#orderId")
    public List<Article> listByOrderId(Integer orderId) {
        return articleMapper.listByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_KEY, key = "'id='+#id")
    public Article getById(Integer id) {
        return articleMapper.get(Article.class, id);
    }

    @Override
    public boolean updatePathById(Integer id, String path) {
        articleMapper.updatePathById(id, path);
        return true;
    }

    /**
     * 根据订单ID删除稿件信息
     *
     * @param orderId
     * @return
     */
    @Override
    @Transactional
    @CacheEvict(value = CACHE_KEY)
    public int delByOrderId(Integer orderId) {
        return articleMapper.delByOrderId(orderId);
    }


    /**
     * 此接口用于获取业务数据集合，原接口businessList返回类型已改为分页对象； 媒介查询用
     *
     * @return ：业务数据集合；
     */
//    @Override
//    public List<Map<String, Object>> getBusinessList(Map params) {
//        List<Map<String, Object>> datas = null;
//        User user = AppUtil.getUser();
//        params.put("user", user);
//        if (params.containsKey("datas")) {
//            List<Article> articles = JSON.parseArray((String) params.get("datas"), Article.class);
//            datas = new ArrayList<>();
//            for (Article article : articles) {
//                Map art = businessOne(article.getId());
//                if (art != null) {
//                    datas.add(art);
//                }
//            }
//        }
//        if (datas == null) {
//            datas = articleMapperXML.businessListMJ(params);
//        }
//        return datas;
//    }
    @Override
    public Map<String, Object> getMediaTypeStatistics(Map<String, Object> map) {
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        Map mediaTypeStatistics = articleMapperXML.getMediaTypeStatistics(map);
        map.remove("mediaType");//移除板块条件字段，板块占比不随页面板块板块改变而改变
        List<Map> mediaTypeList = articleMapperXML.listStatisticsByMediaType(map);
        Map<String, Object> result = new HashMap<>();
        result.put("mediaTypeStatistics", mediaTypeStatistics);
        result.put("mediaTypeList", mediaTypeList);
        return result;
    }

    @Override
    public PageInfo<Map> listStatisticsByMediaType(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        List<Map> list = articleMapperXML.listStatisticsByMediaType(map);
        return new PageInfo<Map>(list);
    }

    @Override
    public List<Map> listStatisticsByDate(Map<String, Object> map) {
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        return articleMapperXML.listStatisticsByDate(map);
    }

    @Override
    public PageInfo<Map> listStatisticsByMedia(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        List<Map> list = articleMapperXML.listStatisticsByMedia(map);
        return new PageInfo<Map>(list);
    }

    @Override
    public PageInfo<Map> listStatisticsByBusiness(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        List<Map> list = articleMapperXML.listStatisticsByBusiness(map);
        return new PageInfo<Map>(list);
    }

    @Override
    public PageInfo<Map> listStatisticsByMediaUser(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        List<Map> list = articleMapperXML.listStatisticsByMediaUser(map);
        return new PageInfo<Map>(list);
    }

    @Override
    public PageInfo<Map> listStatisticsBySupplier(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        List<Map> list = articleMapperXML.listStatisticsBySupplier(map);
        return new PageInfo<Map>(list);
    }

    @Override
    public PageInfo<Map> listStatisticsByCust(Map<String, Object> map, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        List<Map> list = articleMapperXML.listStatisticsByCust(map);
        return new PageInfo<Map>(list);
    }

    @Override
    public void statisticsRankingExport(HttpServletResponse response, Map map) throws Exception {
        try {
            if (map == null || map.get("exportFileType") == null) {
                throw new RuntimeException("导出文件类型不存在");
            }
            String fileName = String.valueOf(map.get("exportFileName"));
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            exportHandleByType(fileName, String.valueOf(map.get("exportFileType")), outputStream, map, "mediaType");
        } catch (Exception e) {
            throw new RuntimeException("导出文件失败");
        }
    }

    @Override
    public void mediaStatisticsRankingExport(HttpServletResponse response, Map map) throws Exception {
        try {
            if (map == null || map.get("exportFileType") == null) {
                throw new RuntimeException("导出文件类型不存在");
            }
            String fileName = String.valueOf(map.get("exportFileName"));
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName + ".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            exportHandleByType(fileName, String.valueOf(map.get("exportFileType")), outputStream, map, "media");
        } catch (Exception e) {
            throw new RuntimeException("导出文件失败");
        }
    }

    @Override
    public Map<String, Object> getMediaStatistics(Map<String, Object> map) {
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        Map mediaStatistics = articleMapperXML.getMediaTypeStatistics(map);//统计信息
        Map<String, Object> result = new HashMap<>();
        result.put("mediaStatistics", mediaStatistics);
        return result;
    }

    @Override
    public List<MediaAudit> listMediaByType(Map<String, Object> map) {
        return mediaAuditMapper.listMediaByParam(map);
    }

    @Override
    public PageInfo<Map<String, Object>> listArtFeedback(Map<String, Object> param, Pageable pageable) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            User user = AppUtil.getUser();
            if (user != null) {
                param.put("userId", user.getId());
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                result = articleMapperXML.listArtFeedback(param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PageInfo<>(result);
    }

    @Override
    public List<Map<String, Object>> exportFeedbackList(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            User user = AppUtil.getUser();
            if (user != null) {
                param.put("userId", user.getId());
                result = articleMapperXML.listArtFeedback(param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 批量修改稿件
     *
     * @param param 入参
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchEditArticle(Map<String, String> param) {
        // outgoAmount  title issuedDate remarks update_user_id update_time order_id userId
        // 批量稿件id
        String[] ids = param.get("ids").split(",");

        // 稿件判空判断
        if (ids == null || ids.length <= 0) {
            throw new QinFeiException(1002, "未获取到稿件id");
        }

        User user = AppUtil.getUser();

        // 只有媒介可以批量修改，并且只能修改自己的稿件
        Boolean isMJ = userService.queryRoleByUserIdAndRoleType(user.getId(), IConst.ROLE_TYPE_MJ);
        if (!isMJ) {
            throw new QinFeiException(1002, "您不是媒介，无法批量修改");
        }
        // 查找数据自己的稿件数量
        int isIsOwnManuscript = articleMapper.findIsOwnArticleNum(ids, user.getId());
        if (isIsOwnManuscript != ids.length) {
            throw new QinFeiException(1002, "请剔除不属于您的稿件，否则无法修改！");
        }

        //稿件list
        List<Article> articles = articleMapper.listByIds(ids);
        Boolean outgoFlag = false;
        Boolean invoiceFlag = false;
        Boolean incomeFlag = false;
        Boolean commFlag = false;
        Boolean unitPriceFlag = false ;
        if(param.get("outgoAmount") != null){
            unitPriceFlag = true;
        }
        for(Article article : articles){
            if(article.getOutgoStates() > 0){
                outgoFlag = true;
            }
            if(article.getCommissionStates() > 0){
                commFlag = true;
            }
            if(article.getInvoiceStates() > 0){
                invoiceFlag = true;
            }
            if(article.getIncomeStates() > 0){
                incomeFlag = true;
            }
            if(unitPriceFlag){
                Double otherExpense = article.getOtherExpenses() == null ? 0 : article.getOtherExpenses();
                Integer num = article.getNum() == null ? 1 : article.getNum();
                Double unitPrice = (MapUtils.getDouble(param, "outgoAmount") - otherExpense) / num;
                article.setUnitPrice(unitPrice);
            }
        }
        if((param.get("userId") != null || param.get("outgoAmount") != null) && (outgoFlag || commFlag)){
            throw new QinFeiException(1002, "选中的稿件有请款或提成，不能修改业务员、请款金额！");
        }
        if(param.get("userId") != null && (incomeFlag || invoiceFlag)){
            throw new QinFeiException(1002, "选中的稿件有回款或开票，不能修改业务员");
        }
        if(outgoFlag && commFlag){
            throw new QinFeiException(1002, "已请款且已提成的稿件，不支持修改！");
        }
        Integer userId = null;
        if (!StringUtils.isEmpty(param.get("userId"))) {
            userId = Integer.parseInt(param.get("userId"));
        }

        User YWUser = null;
        if (userId != null) { // 不改业务员的情况下不进行订单操作
            //创建新订单，并将订单id拿到
            YWUser = userMapper.getById(Integer.parseInt(param.get("userId")));
            Order order = new Order();
            order.setUserId(userId);
            order.setUserName(YWUser.getName());
            order.setDepatId(YWUser.getDeptId());
            order.setNo("MTGJ" + UUIDUtil.get16UUID().toUpperCase());
            order.setCreator(user.getId());
            order.setCreateDate(new Date());
            order.setState(0);
            order.setOrderType(1);
            orderMapper.insert(order);
            Integer orderId = order.getId();

            // 放置订单id 批量修改稿件的订单id
            param.put("orderId", orderId + "");
        }


        param.put("updateUserId", user.getId() + "");

        // 需要插入的 存更改记录 list
        List<ArticleHistory> historyList = new ArrayList<>();

        Double outgoAmount = null;
        if (!StringUtils.isEmpty(param.get("outgoAmount"))) {
            outgoAmount = Double.valueOf(param.get("outgoAmount"));
        }
        String curMonth = DateUtils.getYearAndMonthStr2(new Date());//当前年月
        String issueMonth = "";
        for (Article article : articles) {
            ArticleHistory history = new ArticleHistory();
            BeanUtils.copyProperties(article, history);
            if (YWUser != null) {
                history.setCompanyCode(YWUser.getCompanyCode());
                history.setDeptId(YWUser.getDeptId());
                history.setUserId(userId);
            }
            history.setId(null);
            history.setArtId(article.getId());
            history.setEditDesc(IConst.article_change_media_edit);
            history.setCreator(user.getId());
            history.setCreateTime(new Date());

            if (outgoAmount != null) {
                Double profit;
                if (article.getIncomeStates() == IConst.FEE_STATE_FINISH) {
                    profit = article.getIncomeAmount() - outgoAmount - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                } else {
                    profit = article.getSaleAmount() - outgoAmount - article.getTaxes() - article.getRefundAmount() - article.getOtherPay();
                }
                article.setProfit(profit);

                if (param.get("issuedDate") != null) {
                    issueMonth = DateUtils.getYearAndMonthStr2(
                            DateUtils.parse(String.valueOf(param.get("issuedDate")),
                                    "yyyy-MM-dd"));//发布日期年月
                } else {
                    if (article.getIssuedDate() != null) {
                        issueMonth = DateUtils.getYearAndMonthStr2(article.getIssuedDate());//发布日期年月
                    }
                }

                // 满足三个条件报价就有异动：1、更改日期和稿件发布日期不一致，2、不是第一次设置报价，3、报价有变动
                Double alterOutgo = outgoAmount - article.getOutgoAmount();
                if (!(curMonth.equals(issueMonth))
                        && article.getOutgoAmount() > 0
                        && Math.abs(alterOutgo) > 0.01) {
                    history.setAlterOutgo(alterOutgo);
                    Double alterProfit = profit - article.getProfit();
                    history.setAlterProfit(alterProfit);
                    history.setAlterLabel(IConst.article_alter_flag_true);
                }
            }
            historyList.add(history);
        }

        // 批量插入
        int i = articleHistoryMapper.saveBatch(historyList);

        // 批量修改稿件
        int row = articleMapperXML.updateBatchArticle(articles, param);

        if (i > 0 && row > 0) {
            return row;
        }
        return 0;
    }

    /**
     * 根据稿件id拿到请款记录
     * @param articleId 稿件id
     * @return 请款记录
     */
    @Override
    public Map<String, Object> getFeeOutgo(Integer articleId) {
        return articleMapper.getFeeOutgo(articleId);
    }

    /**
     * 板块统计：根据类型导出不同列表
     *
     * @param exportType     导出文件类型
     * @param statisticsType 统计类型：media-媒体统计、mediaType-板块统计
     */
    private void exportHandleByType(String exportFileName, String exportType, OutputStream outputStream, Map
            map, String statisticsType) {
//        handleMap(map);//处理请求参数-权限
        this.addSecurityByFore(map);
        if (map.get("extendParams") != null) {
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")), MediaExtendParamJson.class);
            if (CollectionUtils.isNotEmpty(mediaExtendParamJsonList)) {
                map.put("extendParams", mediaExtendParamJsonList);
                map.put("extendNum", mediaExtendParamJsonList.size());
            } else {
                map.remove("extendParams");
            }
        }
        User user = AppUtil.getUser();
        if ("mediaType".equals(exportType)) {
            if ("mediaType".equals(statisticsType)) {
                map.remove("mediaTypeRowSelect");//板块统计：导出板块不取决于板块列表选中的媒体
            }
            List<Map> list = articleMapperXML.listStatisticsByMediaType(map);
            String[] heads = {"板块名称", "稿件数量", "业务员数量", "媒介数量", "供应商数量", "客户数量", "报价", "成本", "利润"};
            String[] fields = {"mediaTypeName", "articleNum", "businessUserNum", "mediaUserNum", "supplierNum", "custNum", "saleAmount", "payAmount", "profit"};
            ExcelUtil.exportExcel(exportFileName, heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if (value != null) {
                    if ("articleNum".equals(field) || "businessUserNum".equals(field) || "mediaUserNum".equals(field) || "supplierNum".equals(field)
                            || "custNum".equals(field) || "saleAmount".equals(field) || "payAmount".equals(field) || "profit".equals(field)) {
                        if (value != null) {
                            cell.setCellValue(Double.parseDouble(value.toString()));
                        } else {
                            cell.setCellValue(0);
                        }
                    } else {
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        } else {
                            cell.setCellValue("");
                        }
                    }
                }
            });
        }
        if ("media".equals(exportType)) {
            if ("media".equals(statisticsType)) {
                map.remove("mediaRowSelect");//媒体统计：导出媒体不取决于媒体列表选中的媒体
            }
            if ("mediaType".equals(statisticsType)) {
                map.remove("mediaType");//板块统计：导出媒体不取决于板块下拉列表
            }
            List<Map> list = articleMapperXML.listStatisticsByMedia(map);
            String[] heads = {"媒体名称", "稿件数量", "业务员数量", "媒介数量", "供应商数量", "客户数量", "报价", "成本", "利润"};
            String[] fields = {"mediaName", "articleNum", "businessUserNum", "mediaUserNum", "supplierNum", "custNum", "saleAmount", "payAmount", "profit"};
            ExcelUtil.exportExcel(exportFileName, heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if ("articleNum".equals(field) || "businessUserNum".equals(field) || "mediaUserNum".equals(field) || "supplierNum".equals(field)
                        || "custNum".equals(field) || "saleAmount".equals(field) || "payAmount".equals(field) || "profit".equals(field)) {
                    if (value != null) {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } else {
                        cell.setCellValue(0);
                    }
                } else {
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            });
        }
        if ("business".equals(exportType)) {
            if ("mediaType".equals(statisticsType)) {
                map.remove("mediaType");//板块统计：导出业务员不取决于板块下拉列表
            }
            List<Map> list = articleMapperXML.listStatisticsByBusiness(map);
            String[] heads = {"业务员名称", "稿件数量", "媒介数量", "供应商数量", "客户数量", "报价", "成本", "利润"};
            String[] fields = {"businessUserName", "articleNum", "mediaUserNum", "supplierNum", "custNum", "saleAmount", "payAmount", "profit"};
            ExcelUtil.exportExcel(exportFileName, heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if ("articleNum".equals(field) || "mediaUserNum".equals(field) || "supplierNum".equals(field) || "custNum".equals(field)
                        || "saleAmount".equals(field) || "payAmount".equals(field) || "profit".equals(field)) {
                    if (value != null) {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } else {
                        cell.setCellValue(0);
                    }
                } else {
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            });
        }
        if ("mediaUser".equals(exportType)) {
            if ("mediaType".equals(statisticsType)) {
                map.remove("mediaType");//板块统计：导出媒介不取决于板块下拉列表
            }
            List<Map> list = articleMapperXML.listStatisticsByMediaUser(map);
            String[] heads = {"媒介名称", "稿件数量", "业务员数量", "供应商数量", "客户数量", "报价", "成本", "利润"};
            String[] fields = {"mediaUserName", "articleNum", "businessUserNum", "supplierNum", "custNum", "saleAmount", "payAmount", "profit"};
            ExcelUtil.exportExcel(exportFileName, heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if ("articleNum".equals(field) || "businessUserNum".equals(field) || "supplierNum".equals(field) || "custNum".equals(field)
                        || "saleAmount".equals(field) || "payAmount".equals(field) || "profit".equals(field)) {
                    if (value != null) {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } else {
                        cell.setCellValue(0);
                    }
                } else {
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            });
        }
        if ("supplier".equals(exportType)) {
            if ("mediaType".equals(statisticsType)) {
                map.remove("mediaType");//板块统计：导出供应商不取决于板块下拉列表
            }
            List<Map> list = articleMapperXML.listStatisticsBySupplier(map);
            String[] heads = {"供应商名称", "稿件数量", "业务员数量", "媒介数量", "客户数量", "报价", "成本", "利润"};
            String[] fields = {"supplierName", "articleNum", "businessUserNum", "mediaUserNum", "custNum", "saleAmount", "payAmount", "profit"};
            ExcelUtil.exportExcel(exportFileName, heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if ("articleNum".equals(field) || "businessUserNum".equals(field) || "mediaUserNum".equals(field) || "custNum".equals(field)
                        || "saleAmount".equals(field) || "payAmount".equals(field) || "profit".equals(field)) {
                    if (value != null) {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } else {
                        cell.setCellValue(0);
                    }
                } else if ("supplierName".equals(field)) {
                    if (value != null) {
                        if (user.getDept().getCompanyCode().equals(list.get(rowIndex).get("supplierCompanyCode"))) {
                            cell.setCellValue(value.toString());
                        } else {
                            cell.setCellValue(value.toString().substring(0, 1) + "****");
                        }
                    } else {
                        cell.setCellValue("");
                    }
                } else {
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            });
        }
        if ("cust".equals(exportType)) {
            if ("mediaType".equals(statisticsType)) {
                map.remove("mediaType");//板块统计：导出客户不取决于板块下拉列表
            }
            List<Map> list = articleMapperXML.listStatisticsByCust(map);
            String[] heads = {"客户公司名称", "客户名称", "稿件数量", "业务员数量", "媒介数量", "供应商数量", "应收", "成本", "利润"};
            String[] fields = {"custCompanyName", "custName", "articleNum", "businessUserNum", "mediaUserNum", "supplierNum", "saleAmount", "payAmount", "profit"};
            ExcelUtil.exportExcel(exportFileName, heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if ("articleNum".equals(field) || "businessUserNum".equals(field) || "mediaUserNum".equals(field) || "supplierNum".equals(field)
                        || "saleAmount".equals(field) || "payAmount".equals(field) || "profit".equals(field)) {
                    if (value != null) {
                        cell.setCellValue(Double.parseDouble(value.toString()));
                    } else {
                        cell.setCellValue(0);
                    }
                } else {
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            });
        }
    }

    /**
     * 处理请求参数-权限
     */
    /*private void handleMap(Map<String, Object> map){
        User user = AppUtil.getUser();
        //当页面没选公司 且 部门
        //当没选部门 和 人员 ：该情况一般在领导角色才会出现，我们查询该人员所在公司下的所有信息
        if(map.get("deptId") == null && map.get("userId") == null && map.get("companyCode") == null && user.getCurrentDeptQx()){
            if((IConst.ROLE_TYPE_YW.equals(user.getDept().getCode()) || IConst.ROLE_TYPE_MJ.equals(user.getDept().getCode())) && !IConst.ROLE_TYPE_JT.equals(user.getCompanyCode())){
                map.put("deptId",user.getDept().getId());//当用户为业务部和媒介部时，仅能查询当前公司下，该领导人部门下面的信息
            }
            map.put("companyCode",user.getCompanyCode());
        }
        //当选择部门没选用户时，查询该部门下所有信息
        if(map.get("deptId") != null && map.get("userId") == null){//当且仅指定了部门时
            Integer deptId = Integer.parseInt(String.valueOf(map.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            map.put("deptIds", deptIds);
        }
    }*/
}
