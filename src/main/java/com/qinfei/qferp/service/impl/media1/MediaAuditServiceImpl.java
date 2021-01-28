package com.qinfei.qferp.service.impl.media1;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.config.Config;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleReplace;
import com.qinfei.qferp.entity.media.Media;
import com.qinfei.qferp.entity.media.Supplier;
import com.qinfei.qferp.entity.media1.*;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.biz.ArticleReplaceMapper;
import com.qinfei.qferp.mapper.media.SupplierMapper;
import com.qinfei.qferp.mapper.media1.*;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.impl.workbench.MessageService;
import com.qinfei.qferp.service.media.ISupplierService;
import com.qinfei.qferp.service.media1.IMediaAuditService;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
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
@Slf4j
public class MediaAuditServiceImpl implements IMediaAuditService {
    @Autowired
    private Media1Mapper media1Mapper;
    @Autowired
    private MediaAuditMapper mediaAuditMapper;
    @Autowired
    private MediaExtendAuditMapper mediaExtendAuditMapper;
    @Autowired
    private MediaExtendMapper mediaExtendMapper;
    @Autowired
    private IMediaForm1Service mediaForm1Service;
    @Autowired
    private UserMapper userMapper;
    // 地区数据服务；
    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private MediaSupplierRelateAuditMapper mediaSupplierRelateAuditMapper;

    @Autowired
    private MediaSupplierRelateMapper mediaSupplierRelateMapper;

    @Autowired
    private MediaSupplierPriceAuditMapper mediaSupplierPriceAuditMapper;

    @Autowired
    private MediaSupplierPriceMapper mediaSupplierPriceMapper;

    @Autowired
    private MediaChangeMapper mediaChangeMapper;
    @Autowired
    private MediaSupplierChangeMapper mediaSupplierChangeMapper;
    @Autowired
    private ArticleReplaceMapper articleReplaceMapper;
    @Autowired
    private MediaPlateMapper mediaPlateMapper;

    @Autowired
    private Config config;

    @Value("${media.onlineTime}")
    private String onlineTime;

    // 获取指定媒体板块的责任人信息集合
    private Map<String,Integer> userNameMap;
    // 获取指定媒体板块的供应商信息集合
    private Map<String,Integer> supplierNameMap;
    //其他下拉列表、单选框、复选框缓存集合，key为mediaForm1表的cell值
    private Map<String,Map<String,String>> otherFieldMap;

    //标准板块媒体公共字段
    private final static Map<Integer, List<String>> standarPlateCommonFieldMap = new HashMap<Integer, List<String>>(){
        {
            put(0, Arrays.asList("mediaContentId", "name", "link", "discount", "remarks"));
            put(1, Arrays.asList("*唯一标识*", "*媒体名称*", "案例链接", "折扣率", "备注"));
        }
    };

    //媒体导入模板公共列标题
    List<String> mediaCommonFieldName = Arrays.asList("*媒体名称*","*案例链接*","折扣率","备注");
    List<String> mediaCommonField = Arrays.asList("name","link","discount","remarks");

    //标准板块媒体公共字段
    private final static Map<Integer, List<String>> containIdStandarPlateCommonFieldMap = new HashMap<Integer, List<String>>(){
        {
            put(0,Arrays.asList("id","mediaContentId","link","discount","remarks"));
            put(1,Arrays.asList("*媒体主键ID*","*唯一标识*","案例链接","折扣率","备注"));
        }
    };

    //媒体导出/替换模板公共列标题
    List<String> mediaExportCommonFieldName = Arrays.asList("*媒体主键ID*","*媒体名称*","*案例链接*","折扣率","备注");
    List<String> mediaExportCommonField = Arrays.asList("id","name","link","discount","remarks");

    //媒体供应商导入模板公共列标题
    List<String> mediaSupplierCommonFieldName = Arrays.asList("*媒体名称*","*供应商公司名称*","*供应商联系人*", "*手机号*");
    List<String> mediaSupplierCommonField = Arrays.asList("name","supplierCompany","supplierId", "phone");

    //媒体供应商导入模板公共列标题
    List<String> standarMediaSupplierCommonFieldName = Arrays.asList("*唯一标识*","*供应商公司名称*","*供应商联系人*", "*手机号*");
    List<String> standarMediaSupplierCommonField = Arrays.asList("mediaContentId","supplierCompany","supplierId", "phone");

    @Override
    public PageInfo<MediaAudit> listAuditMedia(Map<String, Object> map, Pageable pageable) {
        List<MediaAudit> result = new ArrayList<>();
        if(map.get("extendParams") != null){
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")),MediaExtendParamJson.class);
            if(CollectionUtils.isNotEmpty(mediaExtendParamJsonList)){
                map.put("extendParams",mediaExtendParamJsonList);
                map.put("extendNum",mediaExtendParamJsonList.size());
            }else{
                map.remove("extendParams");
            }
        }
        //只有媒介部长和组长才能查到数据
        PageInfo<MediaAudit> pageInfo = null;
        if((AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && (AppUtil.isRoleCode(IConst.ROLE_CODE_ZZ)) || AppUtil.isRoleCode(IConst.ROLE_CODE_BZ))){
            Integer state = 0; //部长查询“0-未审核”
            map.put("state",state);
            map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            List<MediaAudit> media1List = mediaAuditMapper.listByParamPage(map);
            pageInfo = new PageInfo<>(media1List);
            if(CollectionUtils.isNotEmpty(media1List)){
                List<Integer> ids = new ArrayList<>();
                for(MediaAudit mediaAudit : media1List){
                    ids.add(mediaAudit.getId());
                }
                Map param = new HashMap();
                param.put("ids",ids);
                result = mediaAuditMapper.listByParam(param);
            }
            pageInfo.setList(result);
        }
        return pageInfo;
    }

    @Override
    public PageInfo<MediaAudit> listAuditMediaSupplier(Map<String, Object> map, Pageable pageable) {
        List<MediaAudit> result = new ArrayList<>();
        if(map.get("extendParams") != null){
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")),MediaExtendParamJson.class);
            if(CollectionUtils.isNotEmpty(mediaExtendParamJsonList)){
                map.put("extendParams",mediaExtendParamJsonList);
                map.put("extendNum",mediaExtendParamJsonList.size());
            }else{
                map.remove("extendParams");
            }
        }
        //只有媒介部长和组长才能查到数据
        PageInfo<MediaAudit> pageInfo = null;
        if((AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && (AppUtil.isRoleCode(IConst.ROLE_CODE_ZZ)) || AppUtil.isRoleCode(IConst.ROLE_CODE_BZ))){
            Integer state = 0; //部长查询“0-未审核”
            map.put("state",state);
            map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            List<MediaAudit> media1List = mediaAuditMapper.listAllRelateByParam(map);
            pageInfo = new PageInfo<>(media1List);
            if(CollectionUtils.isNotEmpty(media1List)){
                List<Integer> ids = new ArrayList<>();
                for(MediaAudit mediaAudit : media1List){
                    ids.add(mediaAudit.getId());
                }
                Map param = new HashMap();
                param.put("ids",ids);
                List<MediaAudit> mediaAuditList = mediaAuditMapper.listByParam(param);
                Map<String, List<MediaPriceCell>> supplierMap = new HashMap<>(); //缓存媒体对应供应商价格
                Map<Integer, MediaAudit> mediaAuditMap = new HashMap<>(); //记录媒体和ID对应关系
                if(CollectionUtils.isNotEmpty(mediaAuditList)){
                    for(MediaAudit mediaAudit : mediaAuditList){
                        mediaAuditMap.put(mediaAudit.getId(), mediaAudit);
                    }

                    Map mediaSupplierParam = new HashMap();
                    mediaSupplierParam.put("mediaIds",ids);
                    mediaSupplierParam.put("state", state);
                    List<MediaPrice> mediaPriceList = mediaAuditMapper.listMediaSupplierInfoByMediaIds(mediaSupplierParam);
                    User user = AppUtil.getUser();
//                    String depts  = userService.getChilds(user.getDeptId());
//                    if (!org.springframework.util.StringUtils.isEmpty(depts) && depts.indexOf("$,") > -1) {
//                        depts = depts.substring(2);
//                    }
                    if(CollectionUtils.isNotEmpty(mediaPriceList)){
                        for(MediaPrice mediaPrice : mediaPriceList){
                            if(CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
                                for(MediaPriceCell mediaPriceCell : mediaPrice.getMediaPriceCellList()){
                                    String key = mediaPrice.getMediaId() + "-"+mediaPriceCell.getSupplierId();
                                    if(!supplierMap.containsKey(key)){
                                        List<MediaPriceCell> mediaPriceCellList = new ArrayList<>();
                                        supplierMap.put(key,mediaPriceCellList);
                                    }
                                    //电话解码
                                    if(StringUtils.isNotEmpty(mediaPriceCell.getSupplierPhone())){
                                       String phone = EncryptUtils.decrypt(mediaPriceCell.getSupplierPhone());
                                       mediaPriceCell.setSupplierPhone(phone);
                                    }
                                    mediaPriceCell.setFlag(false);
                                    if (user.getCompanyCode().equals(mediaPriceCell.getCompanyCode())){
                                        mediaPriceCell.setFlag(true);
                                    }
                                    //获取供应商对应的所有的媒体板块id
                                    String plateIds = mediaAuditMapper.selectPlateIdsForSupplierId(mediaPriceCell.getSupplierId(),onlineTime);
                                    mediaPriceCell.setPlateIds(plateIds);
                                    supplierMap.get(key).add(mediaPriceCell);
                                }
                            }
                        }
                    }
                }
                for(MediaAudit mediaAudit : media1List){
                    String key = mediaAudit.getId() + "-" + mediaAudit.getSupplierId();
                    MediaAudit mediaAudit1 = new MediaAudit();
                    BeanUtils.copyProperties(mediaAuditMap.get(mediaAudit.getId()), mediaAudit1);
                    if(CollectionUtils.isNotEmpty(supplierMap.get(key))){
                        mediaAudit1.setRelateId(supplierMap.get(key).get(0).getRelateId());
                    }
                    mediaAudit1.setMediaPriceCellList(supplierMap.get(key));
                    result.add(mediaAudit1);
                }
            }
            pageInfo.setList(result);
        }
        return pageInfo;
    }

    @Override
    public PageInfo<MediaAudit> listMedia(Map<String, Object> map, Pageable pageable) {
        List<MediaAudit> result = new ArrayList<>();
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
        map.put("isDelete", 1); //排除掉删除的
        map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> media1List = mediaAuditMapper.listByParamPage(map);
        PageInfo<MediaAudit> pageInfo = new PageInfo<>(media1List);
        if(CollectionUtils.isNotEmpty(media1List)){
            List<Integer> ids = new ArrayList<>();
            for(MediaAudit mediaAudit : media1List){
                ids.add(mediaAudit.getId());
            }
            Map param = new HashMap();
            param.put("ids",ids);
            result = mediaAuditMapper.listByParam(param);
            calMediaFT(ids, result); //计算媒体的复投率
        }
        pageInfo.setList(result);
        return pageInfo;
    }

    //计算媒体的复投率
    private void calMediaFT(List<Integer> mediaIdList, List<MediaAudit> result){
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
                for(MediaAudit mediaAudit : result){
                    mediaAudit.setFtRecord(ftGroup.get(mediaAudit.getId()));
                }
            }
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

    @Override
    public PageInfo<MediaAudit> listMediaSupplier(Map<String, Object> map, Pageable pageable) {
        List<MediaAudit> result = new ArrayList<>();
        if(map.get("extendParams") != null){
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")),MediaExtendParamJson.class);
            if(CollectionUtils.isNotEmpty(mediaExtendParamJsonList)){
                map.put("extendParams",mediaExtendParamJsonList);
                map.put("extendNum",mediaExtendParamJsonList.size());
            }else{
                map.remove("extendParams");
            }
        }
        map.put("isDelete", 1); //排除掉删除的
        map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<MediaAudit> media1List = mediaAuditMapper.listAllRelateByParam(map);
        PageInfo<MediaAudit> pageInfo = new PageInfo<>(media1List);
        if(CollectionUtils.isNotEmpty(media1List)){
            List<Integer> ids = new ArrayList<>();
            for(MediaAudit mediaAudit : media1List){
                ids.add(mediaAudit.getId());
            }
            Map param = new HashMap();
            param.put("ids",ids);
            List<MediaAudit> mediaAuditList = mediaAuditMapper.listByParam(param);
            Map<String, List<MediaPriceCell>> supplierMap = new HashMap<>(); //缓存媒体对应供应商价格
            Map<Integer, MediaAudit> mediaAuditMap = new HashMap<>(); //记录媒体和ID对应关系
            if(CollectionUtils.isNotEmpty(mediaAuditList)){
                for(MediaAudit mediaAudit : mediaAuditList){
                    mediaAuditMap.put(mediaAudit.getId(), mediaAudit);
                }

                //获取媒体对应所有供应商价格
                Map mediaSupplierParam = new HashMap();
                mediaSupplierParam.put("mediaIds",ids);
                List<MediaPrice> mediaPriceList = mediaAuditMapper.listMediaSupplierInfoByMediaIds(mediaSupplierParam);
                User user = AppUtil.getUser();
//                String depts  = userService.getChilds(user.getDeptId());
//                if (!org.springframework.util.StringUtils.isEmpty(depts) && depts.indexOf("$,") > -1) {
//                    depts = depts.substring(2);
//                }
                if(CollectionUtils.isNotEmpty(mediaPriceList)){
                    for(MediaPrice mediaPrice : mediaPriceList){
                        if(CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
                            for(MediaPriceCell mediaPriceCell : mediaPrice.getMediaPriceCellList()){
                                String key = mediaPrice.getMediaId() + "-" +mediaPriceCell.getSupplierId();
                                if(!supplierMap.containsKey(key)){
                                    List<MediaPriceCell> mediaPriceCellList = new ArrayList<>();
                                    supplierMap.put(key,mediaPriceCellList);
                                }
                                //电话解码
                                if(StringUtils.isNotEmpty(mediaPriceCell.getSupplierPhone())){
                                   String phone = EncryptUtils.decrypt(mediaPriceCell.getSupplierPhone());
                                   mediaPriceCell.setSupplierPhone(phone);
                                }
                                mediaPriceCell.setFlag(false);
                                if(user.getCompanyCode().equals(mediaPriceCell.getCompanyCode())){
                                    mediaPriceCell.setFlag(true);
                                }
//                                if (user.getIsMgr()==1 && !org.springframework.util.StringUtils.isEmpty(depts) && depts.contains(mediaPriceCell.getDeptId().toString())){
//                                    mediaPriceCell.setFlag(true);
//                                }
                                //获取供应商对应的所有的媒体板块id
                                String plateIds = mediaAuditMapper.selectPlateIdsForSupplierId(mediaPriceCell.getSupplierId(),onlineTime);
                                mediaPriceCell.setPlateIds(plateIds);
                                supplierMap.get(key).add(mediaPriceCell);
                            }
                        }
                    }
                }
            }
            //组装数据
            for(MediaAudit mediaAudit : media1List){
                String key = mediaAudit.getId() + "-" + mediaAudit.getSupplierId();
                MediaAudit mediaAudit1 = new MediaAudit();
                BeanUtils.copyProperties(mediaAuditMap.get(mediaAudit.getId()), mediaAudit1);
                if(CollectionUtils.isNotEmpty(supplierMap.get(key))){
                    mediaAudit1.setRelateId(supplierMap.get(key).get(0).getRelateId());
                }
                mediaAudit1.setMediaPriceCellList(supplierMap.get(key));
                result.add(mediaAudit1);
            }
            pageInfo.setList(result);
        }
        return pageInfo;
    }

    @Override
    public MediaAudit getEditMediaById(Integer id) {
        MediaAudit mediaAudit = mediaAuditMapper.getEditMediaById(id);
        if(mediaAudit == null){
            throw new QinFeiException(1002, "媒体不存在！");
        }
        List<MediaSupplierRelateAudit> mediaSupplierRelateAuditList = mediaSupplierRelateAuditMapper.listSupplierInfoByMediaId(id);
        List<MediaSupplierPriceExtend> supplierList = new ArrayList<>(); //封装成新增媒体时的格式返回回去
        if(CollectionUtils.isNotEmpty(mediaSupplierRelateAuditList)){
            for(MediaSupplierRelateAudit mediaSupplierRelateAudit : mediaSupplierRelateAuditList){
                if(mediaSupplierRelateAudit.getSupplier() == null){
                    throw new QinFeiException(1002, "媒体对应供应商不存在！");
                }
                MediaSupplierPriceExtend mediaSupplierPriceExtend = new MediaSupplierPriceExtend();
                mediaSupplierPriceExtend.setId(mediaSupplierRelateAudit.getId());
                mediaSupplierPriceExtend.setRelateUserId(mediaSupplierRelateAudit.getCreateId());
                mediaSupplierPriceExtend.setSupplierId(mediaSupplierRelateAudit.getSupplier().getId());
                String phone = mediaSupplierRelateAudit.getSupplier().getPhone();
                phone = StringUtils.isEmpty(phone) ? "" : EncryptUtils.decrypt(phone);
                //如果供应商责任人为当前用户，则手机号不加密
                if (AppUtil.getUser().getId().equals(mediaSupplierRelateAudit.getSupplier().getCreator())) {
                    mediaSupplierPriceExtend.setSupplierName(String.format("%s(%s-%s)", mediaSupplierRelateAudit.getSupplier().getName(), mediaSupplierRelateAudit.getSupplier().getContactor(), phone));
                } else {
                    mediaSupplierPriceExtend.setSupplierName(String.format("%s(%s-%s)", mediaSupplierRelateAudit.getSupplier().getName(), mediaSupplierRelateAudit.getSupplier().getContactor(), getPhone(phone)));
                }
                mediaSupplierPriceExtend.setEnabled(mediaSupplierRelateAudit.getEnabled());
                mediaSupplierPriceExtend.setMediaPriceExtends(mediaSupplierRelateAudit.getMediaSupplierPriceAuditList());
                supplierList.add(mediaSupplierPriceExtend);
            }
            mediaAudit.setSupplierList(supplierList);
        }
        return mediaAudit;
    }

    @Override
    public List<Map> getMediaSupplierInfoByMediaId(Integer id) {
        MediaPrice mediaPrice = mediaAuditMapper.getMediaSupplierInfoByMediaId(id, null);
        List<Map> result = null;
        User user = AppUtil.getUser();
        if(mediaPrice != null && CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
            List<Dept> deptList = deptMapper.listAllCompany();
            Map<String, String> companyMap = new HashMap<>();
            if(CollectionUtils.isNotEmpty(deptList)){
                for(Dept dept : deptList){
                    companyMap.put(dept.getCode(),dept.getName());
                }
            }
            result = new ArrayList<>();
            Map<Integer, Map> temp = new HashMap<>();
            for(MediaPriceCell mediaPriceCell: mediaPrice.getMediaPriceCellList()){
                //电话解码
                if(StringUtils.isNotEmpty(mediaPriceCell.getSupplierPhone())){
                   String phone = EncryptUtils.decrypt(mediaPriceCell.getSupplierPhone());
                   mediaPriceCell.setSupplierPhone(phone);
                }
                mediaPriceCell.setFlag(false);
                if(user.getCompanyCode().equals(mediaPriceCell.getCompanyCode())){
                    mediaPriceCell.setFlag(true);
                }
//                if (user.getIsMgr()==1 && !org.springframework.util.StringUtils.isEmpty(depts) && depts.contains(mediaPriceCell.getDeptId().toString())){
//                    mediaPriceCell.setFlag(true);
//                }
                //获取供应商对应的所有的媒体板块id
                String plateIds = mediaAuditMapper.selectPlateIdsForSupplierId(mediaPriceCell.getSupplierId(),onlineTime);
                mediaPriceCell.setPlateIds(plateIds);
                if(temp.get(mediaPriceCell.getSupplierId()) == null){
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", mediaPrice.getUserId());
                    map.put("mediaId", mediaPrice.getMediaId());
                    map.put("mediaContentId", mediaPrice.getMediaContentId());
                    map.put("mediaName", mediaPrice.getMediaName());
                    map.put("companyCode", mediaPrice.getCompanyCode());
                    map.put("state", mediaPrice.getState());
                    map.put("supplierRelateState", mediaPriceCell.getSupplierRelateState());
                    map.put("relateId", mediaPriceCell.getRelateId());
                    map.put("supplierId", mediaPriceCell.getSupplierId());
                    String supplireCompanyCodeName = companyMap.get(mediaPriceCell.getCompanyCode());
                    supplireCompanyCodeName = StringUtils.isNotEmpty(supplireCompanyCodeName) ? supplireCompanyCodeName : mediaPriceCell.getCompanyCode();
                    map.put("supplireCompanyCodeName",supplireCompanyCodeName);
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
    public List<Map> getMediaNumbers(Integer auditPageFlag) {
        List<Map> list = new ArrayList<>();
        //如果是审核管理，则只有组长和部长能查到数据
        if(auditPageFlag == 1){
            if((AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && (AppUtil.isRoleCode(IConst.ROLE_CODE_ZZ)) || AppUtil.isRoleCode(IConst.ROLE_CODE_BZ))){
                list = mediaAuditMapper.listMediaCountByPlateId(onlineTime,0,auditPageFlag); //查询“0-未审核”
            }
        }else{
            list = mediaAuditMapper.listMediaCountByPlateId(onlineTime,null,auditPageFlag); //查询所有
        }
        return list;
    }

    @Override
    public List<Map> getMediaSupplierNumbers(Integer auditPageFlag) {
        User user= AppUtil.getUser();
        Dept dept= user.getDept();
        List<Map> list = new ArrayList<>();
        //如果是审核管理，则只有组长和部长能查到数据
        if(auditPageFlag == 1){
            if((AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && (AppUtil.isRoleCode(IConst.ROLE_CODE_ZZ)) || AppUtil.isRoleCode(IConst.ROLE_CODE_BZ))){
                list = mediaAuditMapper.listMediaSupplierCountByPlateId(onlineTime,0,auditPageFlag); //查询“0-未审核”
            }
        }else{
            list = mediaAuditMapper.listMediaSupplierCountByPlateId(onlineTime,null,auditPageFlag); //查询所有
        }
        return list;
    }

    @Transactional
    @Override
    public void save(MediaAudit mediaAudit) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }

        //如果是标准平台，不需要审核
        if(mediaAudit.getStandarPlatformFlag() == 1){
            mediaAudit.setState(1);
        }else {
            mediaAudit.setState(0);
        }
        mediaAudit.setCreatorId(user.getId());
        mediaAudit.setUpdatedId(user.getId());
        mediaAudit.setCompanyCode(user.getDept().getCompanyCode());
        mediaAudit.setCompanyCodeName(user.getDept().getCompanyCodeName());
        mediaAudit.setDiscount((mediaAudit.getDiscount() == null || mediaAudit.getDiscount() <= 0) ? 100 : mediaAudit.getDiscount());

        if(StringUtils.isEmpty(mediaAudit.getPicPath())){
            mediaAudit.setPicPath("/img/mrt.png");
        }

        //如果是标准平台校验唯一标识、否则校验媒体名称
        if(mediaAudit.getStandarPlatformFlag() == 1){
            validationMediaContentId(mediaAudit.getId(), mediaAudit.getMediaContentId(), mediaAudit.getPlateId());
        }else {
            validationMediaName(mediaAudit.getId(),mediaAudit.getName(),mediaAudit.getPlateId());
        }
        List<Integer> relateIds = new ArrayList<>();//媒体供应商关系主键ID
        int rowNum = mediaAuditMapper.save(mediaAudit);
        if(rowNum > 0 && CollectionUtils.isNotEmpty(mediaAudit.getMediaExtends())){
            for (MediaExtendAudit mediaExtendAudit : mediaAudit.getMediaExtends()){
                mediaExtendAudit.setMediaId(mediaAudit.getId());
            }
            mediaExtendAuditMapper.saveBatch(mediaAudit.getMediaExtends());

            //批量保存供应商媒体关系
            if(CollectionUtils.isNotEmpty(mediaAudit.getSupplierList())){
                List<MediaSupplierRelateAudit> mediaSupplierRelateAuditList = new ArrayList<>(); //媒体供应商关系列表
                Map<Integer,MediaSupplierRelateAudit> mediaSupplierRelateAuditMap = new HashMap<>();
                for(MediaSupplierPriceExtend mediaSupplierPriceExtend : mediaAudit.getSupplierList()){
                    MediaSupplierRelateAudit mediaSupplierRelateAudit = getMediaSupplierRelateAudit(user.getId(),mediaAudit.getId(),mediaSupplierPriceExtend.getSupplierId(),mediaSupplierPriceExtend.getEnabled());

                    //如果是标准平台，不需要审核
                    if(mediaAudit.getStandarPlatformFlag() == 1){
                        mediaSupplierRelateAudit.setState(1);
                    }else {
                        mediaSupplierRelateAudit.setState(0);
                    }

                    mediaSupplierRelateAuditMap.put(mediaSupplierPriceExtend.getSupplierId(),mediaSupplierRelateAudit);
                    mediaSupplierRelateAuditList.add(mediaSupplierRelateAudit);
                }
                mediaSupplierRelateAuditMapper.saveBatch(mediaSupplierRelateAuditList);
                //批量保存供应商价格
                List<MediaSupplierPriceAudit> mediaSupplierPriceAuditList = new ArrayList<>();
                for(MediaSupplierPriceExtend mediaSupplierPriceExtend : mediaAudit.getSupplierList()){
                    if(CollectionUtils.isNotEmpty(mediaSupplierPriceExtend.getMediaPriceExtends())){
                        for(MediaSupplierPriceAudit mediaSupplierRelateAudit : mediaSupplierPriceExtend.getMediaPriceExtends()){
                            mediaSupplierRelateAudit.setMediaSupplierRelateId(mediaSupplierRelateAuditMap.get(mediaSupplierPriceExtend.getSupplierId()).getId());
                            mediaSupplierPriceAuditList.add(mediaSupplierRelateAudit);
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(mediaSupplierPriceAuditList)){
                    mediaSupplierPriceAuditMapper.saveBatch(mediaSupplierPriceAuditList);
                }

                //记录主键ID
                mediaSupplierRelateAuditList.forEach(o -> {
                    relateIds.add(o.getId());
                });

                //如果是标准平台，不需要审核
                if(mediaAudit.getStandarPlatformFlag() == 1 && CollectionUtils.isNotEmpty(mediaSupplierRelateAuditList)){
                    copyMediaRelateAudit2MediaRelate(relateIds);
                }
            }

            //如果是标准平台，不需要审核
            if(mediaAudit.getStandarPlatformFlag() == 1 && mediaAudit.getId() != null){
                copyMediaAuditBaseInfo2Media(mediaAudit.getId());
            }
        }

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            //媒体新增成功，才能进行媒体异动 和 供应商异动的操作
            if(mediaAudit != null && mediaAudit.getId() != null){
                List<Media1> newMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaAudit.getId()));
                List<MediaChange> mediaChangeList = mediaChangeHandler(newMediaList, null, user);
                if(CollectionUtils.isNotEmpty(mediaChangeList)){
                    mediaChangeMapper.saveBatch(mediaChangeList);
                }

                //如果有新增媒体供应商关系
                if(CollectionUtils.isNotEmpty(relateIds)){
                    List<MediaSupplierRelate> newMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);
                    List<MediaSupplierChange> mediaSupplierChangeList = mediaSupplierChangeHandler(Arrays.asList(mediaAudit), newMediaRelateList, null, user);
                    if(CollectionUtils.isNotEmpty(mediaSupplierChangeList)){
                        mediaSupplierChangeMapper.saveBatch(mediaSupplierChangeList);
                    }
                }

            }
        }catch (Exception e){
            log.error("【媒体登记】媒体异动记录异常: {}", e.getMessage());
        }
    }

    /**
     * 校验媒体名称
     * @param id 媒体主键ID
     * @param mediaName 媒体名称
     * @param plateId 媒体板块ID
     */
    public void validationMediaName(Integer id, String mediaName, Integer plateId){
        if(StringUtils.isEmpty(mediaName)){
            throw new QinFeiException(1002, "媒体名称不能为空！");
        }
        //不能查询历史数据，增加本次改动上线时间
        MediaAudit mediaAudit = mediaAuditMapper.checkMediaForName(onlineTime, mediaName.toLowerCase(Locale.US), plateId, id);
        if(mediaAudit != null){
            throw new QinFeiException(1002, "媒体板块下存在相同名称的媒体！");
        }
    }

    @Override
    public void validationMediaContentId(Integer id, String mediaContentId, Integer plateId) {
        if(StringUtils.isEmpty(mediaContentId)){
            throw new QinFeiException(1002, "媒体唯一标识不能为空！");
        }
        //不能查询历史数据，增加本次改动上线时间
        MediaAudit mediaAudit = mediaAuditMapper.checkMediaForMediaContentId(onlineTime, mediaContentId, plateId, id);
        if(mediaAudit != null){
            throw new QinFeiException(1002, "媒体板块下存在相同唯一标识的媒体！");
        }
    }

    /**
     * 获取媒体供应商关系对象
     */
    private MediaSupplierRelateAudit getMediaSupplierRelateAudit(Integer userId, Integer mediaId, Integer supplierId, Integer enabled){
        MediaSupplierRelateAudit mediaSupplierRelateAudit = new MediaSupplierRelateAudit();
        mediaSupplierRelateAudit.setCreateId(userId);
        mediaSupplierRelateAudit.setUpdateId(userId);
        mediaSupplierRelateAudit.setMediaId(mediaId);
        mediaSupplierRelateAudit.setSupplierId(supplierId);
        mediaSupplierRelateAudit.setIsCopy(0); //0-自建
        mediaSupplierRelateAudit.setEnabled(enabled);
        return mediaSupplierRelateAudit;
    }

    @Transactional
    @Override
    public void update(MediaAudit mediaAudit) {
        User user = AppUtil.getUser();

        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }

        if(mediaAudit == null || mediaAudit.getId() == null){
            throw new QinFeiException(1002,"很抱歉，无媒体更新信息！");
        }

        //仅支持责任人自己修改媒体信息
      /*  if(!user.getId().equals(mediaAudit.getUserId())){
            throw new QinFeiException(1002,"很抱歉，您没有操作他人媒体的权限！");
        }*/
        mediaAudit.setUpdatedId(user.getId());

        //记录异动前的媒体信息
        List<Media1> oldMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaAudit.getId()));

        if(!mediaAudit.isAuditsFlag()){ //如果仅改了不需要审核的字段（图标、备注、供应商、责任人 ），则不需要修改扩展表，同步修改t_media1 和 t_media_audit表
            mediaAuditMapper.updateMediaNotAudit(mediaAudit);
            media1Mapper.updateMediaNotAudit(mediaAudit);
            //1、更新媒体扩展字段
            updateMediaExtend(mediaAudit, true);

            //2、更新媒体供应商关系表
            updateMediaRelate(mediaAudit, user);
        }else{
            //如果是标准平台校验唯一标识、否则校验媒体名称
            if(mediaAudit.getStandarPlatformFlag() == 1){
                validationMediaContentId(mediaAudit.getId(), mediaAudit.getMediaContentId(), mediaAudit.getPlateId());
            }else {
                validationMediaName(mediaAudit.getId(),mediaAudit.getName(),mediaAudit.getPlateId());
            }

            mediaAudit.setDiscount((mediaAudit.getDiscount() == null || mediaAudit.getDiscount() <= 0) ? 100 : mediaAudit.getDiscount());

            //如果是标准平台，不需要审核
            if(mediaAudit.getStandarPlatformFlag() == 1){
                mediaAudit.setState(1); //不需要审核
            }else {
                mediaAudit.setState(0); //需要重新审核
            }

            mediaAuditMapper.updateMedia(mediaAudit);

            //如果是标准平台，不需要审核
            if(mediaAudit.getStandarPlatformFlag() != 1){
                //更新媒体的删除字段，当媒体修改后，先不直接修改state字段为0，先用这个字段代替，方便媒体驳回后返回之前状态使用
                media1Mapper.updateMediaIsDelete(mediaAudit.getId(), 1, user.getId());
            }
            //1、更新媒体扩展字段
            updateMediaExtend(mediaAudit, false);

            //2、如果是标准平台，不需要审核
            if(mediaAudit.getStandarPlatformFlag() == 1 && mediaAudit.getId() != null){
                copyMediaAuditBaseInfo2Media(mediaAudit.getId());
            }

            //3、更新媒体供应商关系表
            updateMediaRelate(mediaAudit, user);
        }

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            List<Media1> newMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaAudit.getId()));
            List<MediaChange> mediaChangeList = mediaChangeHandler(newMediaList, oldMediaList, user);
            if(CollectionUtils.isNotEmpty(mediaChangeList)){
                mediaChangeMapper.saveBatch(mediaChangeList);
            }
        }catch (Exception e){
            log.error("【媒体编辑】媒体异动记录异常: {}", e.getMessage());
        }
    }

    //更新扩展字段
    private void updateMediaExtend(MediaAudit mediaAudit, boolean isOnlyPrice){
        if(CollectionUtils.isNotEmpty(mediaAudit.getMediaExtends())){
            List<MediaExtendAudit> addMediaExtendList = new ArrayList<>(); //新增供应商的价格列表
            List<MediaExtendAudit> editMediaExtendList = new ArrayList<>(); //修改供应商价格列表
            for (MediaExtendAudit mediaExtendAudit : mediaAudit.getMediaExtends()){
                if(isOnlyPrice && !"price".equals(mediaExtendAudit.getType())){
                    continue;
                }
                if(mediaExtendAudit.isEditAddStatus()){
                    mediaExtendAudit.setMediaId(mediaAudit.getId());
                    addMediaExtendList.add(mediaExtendAudit);
                }else{
                    editMediaExtendList.add(mediaExtendAudit);
                }
            }
            if(CollectionUtils.isNotEmpty(addMediaExtendList)){
                mediaExtendAuditMapper.saveBatch(addMediaExtendList);
            }
            if(CollectionUtils.isNotEmpty(editMediaExtendList)){
                Map param = new HashMap();
                param.put("mediaId",mediaAudit.getId());
                param.put("mediaExtends",editMediaExtendList);
                mediaExtendAuditMapper.updateBatch(param);
            }
        }
    }

    //更新媒体关系
    private void updateMediaRelate(MediaAudit mediaAudit, User user){
        Set<Integer> supplierRelateSet = new HashSet<>(); //缓存已有的供应商关系，根据列表去删除供应商价格表
        List<Integer> relateIds = new ArrayList<>();//缓存已存在的媒体供应商关系ID
        List<Integer> newRelateIds = new ArrayList<>();//缓存新增的媒体供应商关系ID
        List<MediaSupplierRelate> existOldMediaRelateList = new ArrayList<>();//缓存异动前的媒体供应商关系

        if(CollectionUtils.isNotEmpty(mediaAudit.getDeleteSupplierRelateIds())){
            supplierRelateSet.addAll(mediaAudit.getDeleteSupplierRelateIds());
            //获取关系对应供应商ID，判断稿件是否有使用该媒体和该供应商
            List<Integer> supplierIds = mediaSupplierRelateAuditMapper.listSupplierIdByIds(mediaAudit.getDeleteSupplierRelateIds());
            if(CollectionUtils.isNotEmpty(supplierIds) && CollectionUtils.isNotEmpty(mediaAuditMapper.listArticleBySupplierId(supplierIds,mediaAudit.getId()))){
                throw new QinFeiException(1002,"删除的供应商关系已存在稿件，不允许删除，您可以进行禁用！");
            }

            //异动前数据处理
            relateIds.addAll(supplierRelateSet);
            existOldMediaRelateList.addAll(mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds));//获取异动前的数据

            Map<String, Object> param = new HashMap<>();
            param.put("updateId", user.getId());
            param.put("state", 0);
            param.put("isDelete", 1);
            param.put("ids", supplierRelateSet);
            //如果是标准平台，不需要审核
            if (mediaAudit.getStandarPlatformFlag() == 1) {
                param.put("state", 1);
                mediaSupplierRelateAuditMapper.updateIsDeleteByIds(param); //删除先用isDelete这个字段代替,然后将状态设置为0-待审核
                mediaSupplierRelateMapper.deleteByIds(relateIds);
                mediaSupplierRelateMapper.copySupplierRelateByIds(relateIds);
            } else {
                mediaSupplierRelateAuditMapper.updateIsDeleteByIds(param); //删除先用isDelete这个字段代替,然后将状态设置为0-待审核
                //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
                mediaSupplierRelateMapper.batchUpdateMediaRelateIsDelete(1, user.getId(), relateIds);
            }
        }
        if(CollectionUtils.isNotEmpty(mediaAudit.getSupplierList())){
            List<MediaSupplierRelateAudit> addSupplierRelate = new ArrayList<>(); //媒体供应商关系列表
            List<Integer> addSupplierIds = new ArrayList<>(); //新增的媒体供应商
            List<MediaSupplierRelateAudit> editSupplierRelate = new ArrayList<>(); //媒体供应商关系列表
            List<Integer> auditReleteIds = new ArrayList<>(); //修改后，需要审核的媒体关系列表
            Map<Integer,MediaSupplierRelateAudit> addSupplierRelateAuditMap = new HashMap<>();
            for(MediaSupplierPriceExtend mediaSupplierPriceExtend : mediaAudit.getSupplierList()){
                //新增的供应商
                if(mediaSupplierPriceExtend.isEditAddStatus()){
                    MediaSupplierRelateAudit mediaSupplierRelateAudit = getMediaSupplierRelateAudit(user.getId(),mediaAudit.getId(),mediaSupplierPriceExtend.getSupplierId(),mediaSupplierPriceExtend.getEnabled());

                    //如果是标准平台，不需要审核
                    if(mediaAudit.getStandarPlatformFlag() == 1){
                        mediaSupplierRelateAudit.setState(1);
                    }else {
                        mediaSupplierRelateAudit.setState(0);
                    }

                    addSupplierRelateAuditMap.put(mediaSupplierPriceExtend.getSupplierId(),mediaSupplierRelateAudit);
                    addSupplierRelate.add(mediaSupplierRelateAudit);
                    addSupplierIds.add(mediaSupplierPriceExtend.getSupplierId());
                }else {
                    //如果修改启用/禁用状态
                    if(mediaSupplierPriceExtend.isEditEnableStatus()){
                        MediaSupplierRelateAudit mediaSupplierRelateAudit = new MediaSupplierRelateAudit();
                        mediaSupplierRelateAudit.setId(mediaSupplierPriceExtend.getId());
                        mediaSupplierRelateAudit.setEnabled(mediaSupplierPriceExtend.getEnabled());
                        editSupplierRelate.add(mediaSupplierRelateAudit);
                    }
                    //判断是否有修改数据，进行审核
                    if(mediaSupplierPriceExtend.isAuditsFlag()){
                        auditReleteIds.add(mediaSupplierPriceExtend.getId()); //添加需要进行审核的媒体关系
                    }
                }
            }

            //异动前数据处理
            if(CollectionUtils.isNotEmpty(auditReleteIds)){
                relateIds.addAll(auditReleteIds);
                existOldMediaRelateList.addAll(mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(auditReleteIds));//获取异动前的数据
            }

            if(CollectionUtils.isNotEmpty(addSupplierRelate)){ //添加新增的供应商媒体关系
                List<MediaSupplierRelateAudit> mediaSupplierRelateAuditList = mediaSupplierRelateAuditMapper.listRelateByMediaIdAndSupplierIds(mediaAudit.getId(),addSupplierIds);
                if(CollectionUtils.isNotEmpty(mediaSupplierRelateAuditList)){
                    throw new QinFeiException(1002, "媒体存在该供应商，不能进行添加，请检查是否有未审核的媒体关系导致！");
                }
                mediaSupplierRelateAuditMapper.saveBatch(addSupplierRelate);

                //记录新增的供应商关系ID
                addSupplierRelate.forEach(supplierRelate -> {
                    newRelateIds.add(supplierRelate.getId());
                });
            }
            if(CollectionUtils.isNotEmpty(editSupplierRelate)){ //更新供应商媒体启动状态
                mediaSupplierRelateAuditMapper.updateBatch(editSupplierRelate);
            }

            //2、更新供应商价格表
            List<MediaSupplierPriceAudit> addSupplierPriceAuditList = new ArrayList<>(); //新增供应商的价格列表
            List<MediaSupplierPriceAudit> editSupplierPriceAuditList = new ArrayList<>(); //修改供应商价格列表
            for(MediaSupplierPriceExtend mediaSupplierPriceExtend : mediaAudit.getSupplierList()){
                if(CollectionUtils.isNotEmpty(mediaSupplierPriceExtend.getMediaPriceExtends())){
                    if(mediaSupplierPriceExtend.isEditAddStatus()){ //如果是新增的供应商，则价格都是新增的
                        for(MediaSupplierPriceAudit mediaSupplierRelateAudit : mediaSupplierPriceExtend.getMediaPriceExtends()){
                            mediaSupplierRelateAudit.setMediaSupplierRelateId(addSupplierRelateAuditMap.get(mediaSupplierPriceExtend.getSupplierId()).getId());
                            addSupplierPriceAuditList.add(mediaSupplierRelateAudit);
                        }
                    }else{  //否则为修改的价格
                        for(MediaSupplierPriceAudit mediaSupplierPriceAudit : mediaSupplierPriceExtend.getMediaPriceExtends()){
//                                    supplierRelateSet.add(mediaSupplierRelateAudit.getMediaSupplierRelateId());
                            if(mediaSupplierPriceAudit.isEditAddStatus()){
                                addSupplierPriceAuditList.add(mediaSupplierPriceAudit);
                            }else{
                                editSupplierPriceAuditList.add(mediaSupplierPriceAudit);
                            }
                        }
                    }
                }
            }
            /* if(CollectionUtils.isNotEmpty(supplierRelateSet)){
                 mediaSupplierPriceAuditMapper.deleteByRelateId(supplierRelateSet); //删除所有供应商价格
             }*/
            if(CollectionUtils.isNotEmpty(addSupplierPriceAuditList)){
                mediaSupplierPriceAuditMapper.saveBatch(addSupplierPriceAuditList); //添加新增的供应商价格
            }
            if(CollectionUtils.isNotEmpty(editSupplierPriceAuditList)){
                mediaSupplierPriceAuditMapper.updateBatch(editSupplierPriceAuditList); //批量修改供应商价格
            }

            //3、如果是标准平台，不需要审核
            Map<String, Object> param = new HashMap<>();
            param.put("state", 0);
            param.put("updateId", user.getId());
            param.put("ids", auditReleteIds);
            if (mediaAudit.getStandarPlatformFlag() == 1 && (CollectionUtils.isNotEmpty(addSupplierRelate) || CollectionUtils.isNotEmpty(auditReleteIds))) {
                param.put("state", 1);
                if (CollectionUtils.isNotEmpty(auditReleteIds)) {
                    mediaSupplierRelateAuditMapper.updateStateByIds(param);
                }
                List<Integer> tmpRelateIds = new ArrayList<>();
                tmpRelateIds.addAll(newRelateIds);
                tmpRelateIds.addAll(auditReleteIds);
                copyMediaRelateAudit2MediaRelate(tmpRelateIds);
            } else {
                if (CollectionUtils.isNotEmpty(auditReleteIds)) { //更新媒体关系审核状态
                    mediaSupplierRelateAuditMapper.updateStateByIds(param);
                    //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
                    mediaSupplierRelateMapper.batchUpdateMediaRelateIsDelete(1, user.getId(), auditReleteIds);
                }
            }
        }

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            List<MediaSupplierChange> mediaSupplierChangeList = new ArrayList<>();
            //如果有新增媒体供应商关系
            if(CollectionUtils.isNotEmpty(relateIds)){
                List<MediaSupplierRelate> newMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);
                mediaSupplierChangeList.addAll(mediaSupplierChangeHandler(Arrays.asList(mediaAudit), newMediaRelateList, existOldMediaRelateList, user));
            }
            if(CollectionUtils.isNotEmpty(newRelateIds)){
                List<MediaSupplierRelate> newMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(newRelateIds);
                mediaSupplierChangeList.addAll(mediaSupplierChangeHandler(Arrays.asList(mediaAudit), newMediaRelateList, null, user));
            }
            if(CollectionUtils.isNotEmpty(mediaSupplierChangeList)){
                mediaSupplierChangeMapper.saveBatch(mediaSupplierChangeList);
            }
        }catch (Exception e){
            log.error("【媒体关系更新】媒体关系异动记录异常: {}", e.getMessage());
        }
    }

    /**
     * 权限校验
     * @param user 当前用户
     * @param mediaAuditList 媒体信息
     */
    private void validationQX(User user, List<MediaAudit> mediaAuditList){
        if(CollectionUtils.isNotEmpty(mediaAuditList)){
            for(MediaAudit mediaAudit : mediaAuditList){
                if(!(AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && (AppUtil.isRoleCode(IConst.ROLE_CODE_BZ) || AppUtil.isRoleCode(IConst.ROLE_CODE_ZZ))) && !mediaAudit.getUserId().equals(user.getId())){
                    throw new QinFeiException(1002, "很抱歉，您没有操作别人媒体的权限！");
                }
            }
        }
    }

    /**
     * 更新稿件信息
     * @param mediaAudit
     */
    private void updateArticle(MediaAudit mediaAudit){
        Map param = new HashMap();
        param.put("mediaName",mediaAudit.getName());
        param.put("mediaId",mediaAudit.getId());
        media1Mapper.updateArticle(param);
        media1Mapper.updateArticleImport(param);
    }

    @Override
    public MediaAudit getById(Integer id) {
        MediaAudit mediaAudit = mediaAuditMapper.getMediaById(id);
        if(mediaAudit == null){
            throw new QinFeiException(1002,"媒体不存在！");
        }
        User user = userMapper.getById(mediaAudit.getUserId());
        mediaAudit.setUser(user);
        return mediaAudit;
    }

    @Transactional
    @Override
    public void pass(Integer id) {
        MediaAudit mediaAudit = this.getById(id);
        if(mediaAudit == null){
            throw new QinFeiException(1002,"媒体信息不存在");
        }
        validationQX(AppUtil.getUser(),Arrays.asList(mediaAudit)); //权限判断

        // 判断是否是业务部长 如果是业务部长则将state改为1 业务组长就改为2
//        int state = (AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && AppUtil.isRoleCode(IConst.ROLE_CODE_BZ)) ? 1 : 1;
        mediaAuditMapper.updateMediaState(id,1,AppUtil.getUser().getId());
        //1、先更新稿件表（t_biz_article、t_biz_article_import）中和媒体相关的数据
        updateArticle(mediaAudit);
        //2、拷贝对应审核表信息到实际使用表
        copyMediaAuditBaseInfo2Media(id);

        //3、计算最小值
//        setMedia1MinPrice(Arrays.asList(id));

        sendMessage(mediaAudit, true);
    }

    @Transactional
    @Override
    public void passRelate(Integer mediaId, Integer relateId) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        MediaAudit mediaAudit = this.getById(mediaId);
        if(mediaAudit == null){
            throw new QinFeiException(1002,"媒体信息不存在");
        }
        validationQX(AppUtil.getUser(),Arrays.asList(mediaAudit)); //权限判断

        //更新关系状态
        mediaSupplierRelateAuditMapper.updateStateById(1, user.getId(), relateId);

        //拷贝对应审核表信息到实际使用表
        copyMediaRelateAudit2MediaRelate(Arrays.asList(relateId));

        //拷贝完后，更新媒体最低价
//        setMedia1MinPrice(Arrays.asList(mediaId));

        sendMessage(mediaAudit, true);
    }

    /**
     * 拷贝媒体审核基本信息 到 实际媒体基本信息（不拷贝价格扩展，价格扩展由媒体关系审核时设置）
     * @param mediaId 媒体ID
     */
    private void copyMediaAuditBaseInfo2Media(Integer mediaId){
        media1Mapper.deleteByMediaId(mediaId);
        media1Mapper.copyMediaById(mediaId);

        mediaExtendMapper.deleteByMediaId(mediaId);
        mediaExtendMapper.copyMediaExtendByMediaId(mediaId);
//        mediaExtendMapper.updateMediaPriceAsZero(Arrays.asList(mediaId));//由于不需要计算最低价格，所以无需设置默认值
    }

    /**
     * 拷贝媒体审核信息 到 实际媒体(媒体和关系拷贝使用)
     * @param mediaId 媒体ID
     */
    private void copyMediaAudit2Media(Integer mediaId){
        List<Integer> auditRelateIds = mediaSupplierRelateAuditMapper.listIdByMediaId(mediaId);
        List<Integer> relateIds = mediaSupplierRelateMapper.listIdByMediaId(mediaId);
        if(CollectionUtils.isNotEmpty(relateIds)){
            Set<Integer> param = new HashSet<>();
            param.addAll(relateIds);
            mediaSupplierPriceMapper.deleteByRelateIds(param);
            mediaSupplierRelateMapper.deleteByMediaId(mediaId);
        }
        if(CollectionUtils.isNotEmpty(auditRelateIds)){
            Set<Integer> auditParam = new HashSet<>();
            auditParam.addAll(auditRelateIds);
            mediaSupplierRelateMapper.copySupplierRelateByMediaId(mediaId);
            mediaSupplierPriceMapper.copySupplierPriceByRelateIds(auditParam);
        }
        media1Mapper.deleteByMediaId(mediaId);
        media1Mapper.copyMediaById(mediaId);

        mediaExtendMapper.deleteByMediaId(mediaId);
        mediaExtendMapper.copyMediaExtendByMediaId(mediaId);
        mediaExtendMapper.updateMediaPriceAsZero(Arrays.asList(mediaId));
    }

    /**
     * 拷贝媒体关系审核信息 到 实际媒体关系
     * @param relateIds 媒体关系ID
     */
    private void copyMediaRelateAudit2MediaRelate(List<Integer> relateIds){
        if(CollectionUtils.isNotEmpty(relateIds)){
            Set<Integer> param = new HashSet<>();
            param.addAll(relateIds);
            mediaSupplierPriceMapper.deleteByRelateIds(param);
            mediaSupplierRelateMapper.deleteByIds(relateIds);
            mediaSupplierRelateMapper.copySupplierRelateByIds(relateIds);
            mediaSupplierPriceMapper.copySupplierPriceByRelateIds(param);
        }
    }

    /**
     * 实际媒体 到 拷贝媒体审核信息
     * @param mediaId 媒体ID
     */
    private void copyMedia2MediaAudit(Integer mediaId){
        //由于关系专门有关系管理，所以此处仅处理媒体基本信息 2019-09-10
       /* List<Integer> auditRelateIds = mediaSupplierRelateAuditMapper.listIdByMediaId(mediaId);
        List<Integer> relateIds = mediaSupplierRelateMapper.listIdByMediaId(mediaId);
        //如果实际媒体表有数据，说明不是新增的媒体驳回，而是被修改的媒体驳回，可以删除审核表后再拷贝，否不能删除审核表媒体，不然数据就没了
        if(CollectionUtils.isNotEmpty(relateIds) && CollectionUtils.isNotEmpty(auditRelateIds)){
            Set<Integer> auditParam = new HashSet<>();
            auditParam.addAll(auditRelateIds);
            mediaSupplierPriceAuditMapper.deleteByRelateId(auditParam);
            mediaSupplierRelateAuditMapper.deleteByMediaId(mediaId);
        }
        if(CollectionUtils.isNotEmpty(relateIds)){
            Set<Integer> param = new HashSet<>();
            param.addAll(relateIds);
            mediaSupplierRelateAuditMapper.copySupplierRelateByMediaId(mediaId);
            mediaSupplierPriceAuditMapper.copySupplierPriceByRelateIds(param);
        }*/

        Media1 media1 = media1Mapper.getMediaById(mediaId);
        if(media1 != null){
            media1Mapper.updateMediaIsDelete(mediaId, 0, AppUtil.getUser().getId());//先将媒体实际表的状态更改回来，在进行拷贝

            mediaAuditMapper.deleteByMediaId(mediaId);
            mediaAuditMapper.copyMediaById(mediaId);

            mediaExtendAuditMapper.deleteByMediaId(mediaId);
            mediaExtendAuditMapper.copyMediaExtendByMediaId(mediaId);
        }
    }

    /**
     * 实际媒体关系 到 拷贝媒体关系审核信息
     * @param relateIds 媒体ID
     */
    private void copyMediaRelate2MediaRelateAudit(List<Integer> relateIds){
        List<Integer> ids = mediaSupplierRelateMapper.listIdById(relateIds);
        //如果实际媒体关系表有数据，说明不是新增的媒体关系驳回，而是被修改的媒体关系驳回，可以删除审核表后再拷贝，否不能删除审核表媒体，不然数据就没了
        if(CollectionUtils.isNotEmpty(ids)){
            Set<Integer> auditParam = new HashSet<>();
            auditParam.addAll(ids);
            mediaSupplierPriceAuditMapper.deleteByRelateId(auditParam);
            mediaSupplierRelateAuditMapper.deleteByIds(ids);

            mediaSupplierRelateMapper.batchUpdateMediaRelateIsDelete(0, AppUtil.getUser().getId(),relateIds); //设置原来关系可使用，然后再进行拷贝

            mediaSupplierRelateAuditMapper.copySupplierRelateByIds(ids);
            mediaSupplierPriceAuditMapper.copySupplierPriceByRelateIds(auditParam);
        }
    }

    /**
     * 复制媒体 到 新的媒体
     * @param sourceMedia
     * @param newMedia
     */
    private void sourceMedia2NewMedia(MediaAudit sourceMedia, MediaAudit newMedia, User user, Date currentDate){
        BeanUtils.copyProperties(sourceMedia,newMedia);
        newMedia.setId(null);
        newMedia.setCompanyCode(user.getDept().getCompanyCode());
        newMedia.setCompanyCodeName(user.getDept().getCompanyCodeName());
        newMedia.setState(1); //不需要审核
        newMedia.setIsCopy(1); //1-拷贝
        newMedia.setCopyRemarks("【"+DateUtils.format(currentDate,"yyyy-MM-dd HH:mm:ss")+"】拷贝["+sourceMedia.getCompanyCodeName()+"]媒体.");
        newMedia.setCompanyCode(user.getDept().getCompanyCode());
        newMedia.setCompanyCodeName(user.getDept().getCompanyCodeName());
        newMedia.setUserId(user.getId());
        newMedia.setCreatorId(user.getId());
        newMedia.setCreateDate(currentDate);
        newMedia.setUpdatedId(user.getId());
        newMedia.setUpdateDate(currentDate);
    }

    @Override
    @Transactional
    public void copy(Integer id) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        if(!IConst.COMPANY_CODE_XH.equals(user.getDept().getCompanyCode())){
            throw new QinFeiException(1002,"媒体拷贝功能本公司暂时不支持！");
        }
        Date currentDate = new Date();
        //0、获取当前媒体信息
        MediaAudit sourceMedia = mediaAuditMapper.getMediaSupplierById(id);
        if(sourceMedia == null){
            throw new QinFeiException(1002,"媒体信息不存在！");
        }
        //1、先判断本公司本板块是否存在该名称媒体，存在提示错误
        validationMediaName(null, sourceMedia.getName(),sourceMedia.getPlateId());
        //2、判断媒体的所有供应商是否在本公司存在，存在提示错误
        List<Supplier> suppliers = new ArrayList<>(); //供应商列表
        Map<Integer, String> supplierNameMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(sourceMedia.getSuppliers())){
            List<Supplier> existsSuppliers = supplierService.listAllSupplierByPlateCompany(sourceMedia.getPlateId(), null); //获取本公司所有供应商
            Map<String, Integer> v1 = new HashMap<>(); //供应商 + 联系人
            Map<String, Integer> v2 = new HashMap<>(); //联系人 + 电话
            Map<String, Integer> v3 = new HashMap<>(); //联系人 + QQ/微信
            for (Supplier data : existsSuppliers) {
                v1.put(data.getName() + "*" + data.getContactor(), data.getId());
                if(StringUtils.isNotEmpty(data.getPhone())){
                    v2.put(data.getContactor() + "*" + data.getPhone(), data.getId());
                }
                if(StringUtils.isNotEmpty(data.getQqwechat())){
                    v3.put(data.getContactor() + "*" + data.getQqwechat(), data.getId());
                }
            }
            existsSuppliers.clear();
            existsSuppliers = null; //对象已使用完毕，对其进行释放

            for(Supplier supplier : sourceMedia.getSuppliers()){
                if(v1.get(supplier.getName()+"*"+supplier.getContactor()) != null || v2.get(supplier.getContactor()+"*"+supplier.getPhone()) != null ||
                        v3.get(supplier.getContactor()+"*"+supplier.getQqwechat()) != null){
                    throw new QinFeiException(1002,"很抱歉，【供应商名称+联系人】已经存在或【联系人+电话】已经存在或【联系人+QQ微信】已经存在，拷贝失败！");
                }
                Supplier addSupplier = new Supplier();
                BeanUtils.copyProperties(supplier,addSupplier);
                addSupplier.setId(null);
                addSupplier.setCreator(user.getId());
                addSupplier.setIsCopy(1);
                addSupplier.setCopyRemarks("【"+ DateUtils.format(currentDate,"yyyy-MM-dd HH:mm:ss")+"】拷贝["+supplier.getCompanyCode()+"]媒体.");
                addSupplier.setCreateTime(currentDate);
                addSupplier.setUpdateUserId(user.getId());
                addSupplier.setUpdateTime(currentDate);
                addSupplier.setCompanyCode(user.getDept().getCompanyCode());
                suppliers.add(addSupplier);
                supplierNameMap.put(supplier.getId(),supplier.getName()+"*"+supplier.getContactor());//原媒体供应商ID，对应名称，后面通过这里取供应商价格对应价格

            }
        }
        //3、拷贝供应商
        supplierMapper.saveBatch(suppliers);
        //4、拷贝媒体，不需要经过审核
        MediaAudit addMedia = new MediaAudit();
        sourceMedia2NewMedia(sourceMedia, addMedia, user, currentDate);
        mediaAuditMapper.save(addMedia);
        //5、拷贝媒体扩展
        List<MediaExtendAudit> mediaExtendAuditList = mediaExtendAuditMapper.listExtendByMediaId(sourceMedia.getId());
        if(CollectionUtils.isNotEmpty(mediaExtendAuditList)){
           for(MediaExtendAudit mediaExtendAudit : mediaExtendAuditList){
               mediaExtendAudit.setMediaId(addMedia.getId());
               mediaExtendAudit.setCreateDate(currentDate);
               mediaExtendAudit.setUpdateDate(currentDate);
           }
           mediaExtendAuditMapper.saveBatch(mediaExtendAuditList);
           mediaExtendAuditList.clear();
           mediaExtendAuditList = null; //对象已使用完毕，对其进行释放
        }
        //6、拷贝媒体供应商关系
        List<MediaSupplierRelateAudit> mediaSupplierRelateAuditList = new ArrayList<>(); //媒体供应商关系列表
        Map<String, MediaSupplierRelateAudit> mediaSupplierRelateAuditMap = new HashMap<>(); //缓存新供应商对应的关系
        if(CollectionUtils.isNotEmpty(suppliers)){
            for(Supplier supplier : suppliers){
                MediaSupplierRelateAudit mediaSupplierRelateAudit = new MediaSupplierRelateAudit();
                mediaSupplierRelateAudit.setMediaId(addMedia.getId());
                mediaSupplierRelateAudit.setSupplierId(supplier.getId());
                mediaSupplierRelateAudit.setState(1); //不需要审核
                mediaSupplierRelateAudit.setIsCopy(1);  //1-拷贝
                mediaSupplierRelateAuditList.add(mediaSupplierRelateAudit);
                mediaSupplierRelateAuditMap.put(supplier.getName()+"*"+supplier.getContactor(),mediaSupplierRelateAudit);//通过供应商名称，对应新的关系，后面拷贝价格需要取关系ID
            }
            mediaSupplierRelateAuditMapper.saveBatch(mediaSupplierRelateAuditList);
            mediaSupplierRelateAuditList.clear();
            mediaSupplierRelateAuditList = null; //对象已使用完毕，对其进行释放
        }
        suppliers.clear();
        suppliers = null; //对象已使用完毕，对其进行释放
        //7、拷贝供应商价格
        Map param = new HashMap();
        param.put("id",sourceMedia.getId());
        List<MediaPrice> supplierPrices = mediaAuditMapper.listMediaSupplierPriceByParam(param);
        if(CollectionUtils.isNotEmpty(supplierPrices)){
            List<MediaSupplierPriceAudit> mediaSupplierPriceAuditList = new ArrayList<>();
            MediaPrice supplierPrice = supplierPrices.get(0);
            if(CollectionUtils.isNotEmpty(supplierPrice.getMediaPriceCellList())){
                for(MediaPriceCell mediaPriceCell : supplierPrice.getMediaPriceCellList()){
                    MediaSupplierPriceAudit mediaSupplierPriceAudit = new MediaSupplierPriceAudit();
                    mediaSupplierPriceAudit.setCell(mediaPriceCell.getCell());
                    mediaSupplierPriceAudit.setCellName(mediaPriceCell.getCellName());
                    mediaSupplierPriceAudit.setCellValue(mediaPriceCell.getCellValue());
                    mediaSupplierPriceAudit.setMediaSupplierRelateId(mediaSupplierRelateAuditMap.get(supplierNameMap.get(mediaPriceCell.getSupplierId())).getId());
                    mediaSupplierPriceAuditList.add(mediaSupplierPriceAudit);
                }
                mediaSupplierPriceAuditMapper.saveBatch(mediaSupplierPriceAuditList);
                mediaSupplierPriceAuditList.clear();
                mediaSupplierPriceAuditList = null; //对象已使用完毕，对其进行释放
            }
            supplierPrices.clear();
            supplierPrices = null; //对象已使用完毕，对其进行释放
        }
        //8、拷贝t_media_audit 数据 到 t_media1
        copyMediaAudit2Media(addMedia.getId());
    }

    @Override
    @Transactional
    public void copyMedia(Integer id) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        if(!IConst.COMPANY_CODE_XH.equals(user.getDept().getCompanyCode())){
            throw new QinFeiException(1002,"媒体拷贝功能本公司暂时不支持！");
        }
        Date currentDate = new Date();
        //0、获取当前媒体信息
        MediaAudit sourceMedia = mediaAuditMapper.getMediaById(id);
        if(sourceMedia == null){
            throw new QinFeiException(1002,"媒体信息不存在！");
        }
        //1、先判断本公司本板块是否存在该名称媒体，存在提示错误
        validationMediaName(null, sourceMedia.getName(),sourceMedia.getPlateId());
        //2、拷贝媒体，不需要经过审核
        MediaAudit addMedia = new MediaAudit();
        sourceMedia2NewMedia(sourceMedia, addMedia, user, currentDate);
        mediaAuditMapper.save(addMedia);
        //3、拷贝媒体扩展，由于价格属于供应商，所以此处拷贝价格默认为0
        List<MediaExtendAudit> mediaExtendAuditList = mediaExtendAuditMapper.listExtendByMediaId(sourceMedia.getId());
        List<MediaExtendAudit> addMediaExtendAuditList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(mediaExtendAuditList)){
            for(MediaExtendAudit mediaExtendAudit : mediaExtendAuditList){
                mediaExtendAudit.setMediaId(addMedia.getId());
                mediaExtendAudit.setCreateDate(currentDate);
                mediaExtendAudit.setUpdateDate(currentDate);
                if("price".equals(mediaExtendAudit.getType())){
                    mediaExtendAudit.setCellValue("0.00");
                }
                addMediaExtendAuditList.add(mediaExtendAudit);
            }
            mediaExtendAuditMapper.saveBatch(addMediaExtendAuditList);
            mediaExtendAuditList.clear();
            mediaExtendAuditList = null; //对象已使用完毕，对其进行释放
            addMediaExtendAuditList.clear();
            addMediaExtendAuditList = null; //对象已使用完毕，对其进行释放
        }

        //4、拷贝媒体到t_media1表
        media1Mapper.deleteByMediaId(addMedia.getId());
        media1Mapper.copyMediaById(addMedia.getId());

        mediaExtendMapper.deleteByMediaId(addMedia.getId());
        mediaExtendMapper.copyMediaExtendByMediaId(addMedia.getId());
        mediaExtendMapper.updateMediaPriceAsZero(Arrays.asList(addMedia.getId()));
    }

    @Override
    @Transactional
    public void copyMediaRelate(Integer id) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        if(!IConst.COMPANY_CODE_XH.equals(user.getDept().getCompanyCode())){
            throw new QinFeiException(1002,"媒体拷贝功能本公司暂时不支持！");
        }
        Date currentDate = new Date();
        String companyCode = user.getDept().getCompanyCode();
        //1、获取当前被拷贝的媒体和供应商信息
        MediaAudit mediaAudit = mediaAuditMapper.getMediaSupplierByRelateId(id);
        if(mediaAudit == null){
            throw new QinFeiException(1002,"媒体不存在或者媒体未审核通过");
        }
        //2、判断媒体是否存在，存在，如果未审核则提示错误，否则使用，不存在则新建，对于存在的需要获取最低价格进行比较
        MediaAudit existsMedia = mediaAuditMapper.checkMediaForName(onlineTime, mediaAudit.getName().toLowerCase(Locale.US), mediaAudit.getPlateId(), null);
        Integer mediaId = null;  //通过该字段是否为空，判断是否需要新建媒体
        if(existsMedia != null){
            mediaId = existsMedia.getId();
            if(existsMedia.getState() != 1){
                throw new QinFeiException(1002,"媒体存在，但是未审核通过，请审核通过后再拷贝媒体供应商关系");
            }
        }

        //3、判断供应商是否存在（三种方式判断），不存在新建，存在则使用
        if(mediaAudit.getSupplier() == null){
            throw new QinFeiException(1002,"供应商不存在");
        }
        Integer supplierId = supplierMapper.checkSupplierForParam(mediaAudit.getPlateId(), companyCode, mediaAudit.getSupplier().getContactor(), mediaAudit.getSupplier().getName(), null, null, null);
        if(supplierId == null && StringUtils.isNotEmpty(mediaAudit.getSupplier().getPhone())){ //如果校验供应商+联系人失败，则校验联系人+电话号码
            supplierId = supplierMapper.checkSupplierForParam(mediaAudit.getPlateId(), companyCode, mediaAudit.getSupplier().getContactor(), null, mediaAudit.getSupplier().getPhone(), null, null);
        }
        if(supplierId == null && StringUtils.isNotEmpty(mediaAudit.getSupplier().getQqwechat())){ //如果校验联系人+电话号码失败，则校验联系人+QQ微信
            supplierId = supplierMapper.checkSupplierForParam(mediaAudit.getPlateId(), companyCode, mediaAudit.getSupplier().getContactor(), null, null, mediaAudit.getSupplier().getQqwechat(), null);
        }
        //4、判断供应商和媒体关系是否存在（如果媒体和供应商存在一者新建的则不需要判断该项），存在则提示错误，不存在则新建
        if(mediaId != null && supplierId != null){  //都存在，需要判断关系是否已经建立，建立则提示错误
            MediaSupplierRelateAudit mediaSupplierRelateAudit = mediaSupplierRelateAuditMapper.getRelateByMediaIdAndSupplierId(mediaId,supplierId);
            if(mediaSupplierRelateAudit != null){
                throw new QinFeiException(1002,"已存在媒体和供应商的关系");
            }
        }
        //5、是否新建供应商（供应商不存在）
        Supplier addSupplier = new Supplier();
        if(supplierId == null){
            BeanUtils.copyProperties(mediaAudit.getSupplier(), addSupplier);
            addSupplier.setId(null);
            addSupplier.setCreator(user.getId());
            addSupplier.setIsCopy(1);
            addSupplier.setCopyRemarks("【"+ DateUtils.format(currentDate,"yyyy-MM-dd HH:mm:ss")+"】拷贝["+mediaAudit.getCompanyCodeName()+"]供应商.");
            addSupplier.setCompanyCode(user.getDept().getCompanyCode());
            addSupplier.setCreateTime(currentDate);
            addSupplier.setUpdateUserId(user.getId());
            addSupplier.setUpdateTime(currentDate);
            supplierMapper.insert(addSupplier);
        }
        //6、是否新建媒体（媒体不存在）
        MediaAudit addMedia = new MediaAudit();
        if(mediaId == null){
            sourceMedia2NewMedia(mediaAudit, addMedia, user, currentDate);
            mediaAuditMapper.save(addMedia);

            //拷贝媒体扩展，由于价格属于供应商，所以此处不拷贝价格
            List<MediaExtendAudit> mediaExtendAuditList = mediaExtendAuditMapper.listExtendByMediaId(mediaAudit.getId());
            if(CollectionUtils.isNotEmpty(mediaExtendAuditList)){
                for(MediaExtendAudit mediaExtendAudit : mediaExtendAuditList){
                    mediaExtendAudit.setMediaId(addMedia.getId());
                    mediaExtendAudit.setCreateDate(currentDate);
                    mediaExtendAudit.setUpdateDate(currentDate);
                    if("price".equals(mediaExtendAudit.getType())){
                        mediaExtendAudit.setCellValue("0.00");
                    }
                }
                mediaExtendAuditMapper.saveBatch(mediaExtendAuditList);
                mediaExtendAuditList.clear();
                mediaExtendAuditList = null; //对象已使用完毕，对其进行释放
            }
        }else {
            BeanUtils.copyProperties(existsMedia, addMedia);
        }
        //7、新建媒体供应商关系
        Integer tempMediaId = mediaId == null ? addMedia.getId() : mediaId;
        Integer tempSupplierId = supplierId == null ? addSupplier.getId() : supplierId;
        MediaSupplierRelateAudit mediaSupplierRelateAudit = new MediaSupplierRelateAudit();
        mediaSupplierRelateAudit.setState(1); //不需要审核
        mediaSupplierRelateAudit.setIsCopy(1);
        mediaSupplierRelateAudit.setCopyRemarks("【"+ DateUtils.format(currentDate,"yyyy-MM-dd HH:mm:ss")+"】拷贝["+mediaAudit.getCompanyCodeName()+"]媒体供应商关系.");
        mediaSupplierRelateAudit.setSupplierId(tempSupplierId);
        mediaSupplierRelateAudit.setMediaId(tempMediaId);
        mediaSupplierRelateAudit.setCreateId(user.getId());
        mediaSupplierRelateAudit.setUpdateId(user.getId());
        mediaSupplierRelateAudit.setCreateDate(currentDate);
        mediaSupplierRelateAudit.setUpdateDate(currentDate);
        mediaSupplierRelateAuditMapper.insert(mediaSupplierRelateAudit);
        //8、新建供应商价格
//        Map<String, BigDecimal> supplierPriceMap = new HashMap<>(); //缓存当前供应商价格，用于后面计算最低价格
        List<MediaSupplierPriceAudit> mediaSupplierPriceAuditList = mediaSupplierPriceAuditMapper.listSupplierPriceByRelateId(id);//根据关系ID获取原来的供应商价格
        if(CollectionUtils.isNotEmpty(mediaSupplierPriceAuditList)){
            for (MediaSupplierPriceAudit mediaSupplierPriceAudit : mediaSupplierPriceAuditList){
                mediaSupplierPriceAudit.setMediaSupplierRelateId(mediaSupplierRelateAudit.getId());
                mediaSupplierPriceAudit.setCreateDate(currentDate);
                mediaSupplierPriceAudit.setUpdateDate(currentDate);
//                supplierPriceMap.put(tempSupplierId+"-"+mediaSupplierPriceAudit.getCell(),mediaSupplierPriceAudit.getCellValue());
            }
            mediaSupplierPriceAuditMapper.saveBatch(mediaSupplierPriceAuditList);
            mediaSupplierPriceAuditList.clear();
            mediaSupplierPriceAuditList = null; //对象已使用完毕，对其进行释放
        }
        //8、设置媒体最低价格（新建的媒体不需要）
//        setMediaAuditMinPrice(Arrays.asList(tempMediaId));
        /*if(mediaId != null){
            //获取媒体价格，用于比较价格是否是新增的还是需要编辑的
            Map<String,Object> map = getMediaExtendFieldsGroupByPrice(mediaAudit.getPlateId());
            List<MediaForm1> priceFields = (List<MediaForm1>) map.get("priceFields"); //获取当前媒体板块的价格字段，用于下面判断是否新增价格字段，防止后期表单扩展添加字段
            Map param = new HashMap();
            param.put("id", mediaId);
            List<MediaPrice> existsMediaPriceList = mediaAuditMapper.listMediaPriceByParam(param);
            Map<String,BigDecimal> existsMediaPriceMap = new HashMap<>(); //key = cell, value = cellValue
            for(MediaPrice mediaPrice : existsMediaPriceList){
                if(CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
                    for(MediaPriceCell mediaPriceCell : mediaPrice.getMediaPriceCellList()){
                        existsMediaPriceMap.put(mediaPriceCell.getCell(),mediaPriceCell.getCellValue());
                    }
                }
            }

            //获取媒体已有的供应商价格 + 新的价格
            List<MediaPrice> existsMediaSupplierPriceList = mediaAuditMapper.listMediaSupplierPriceByParam(param);
            for(MediaPrice mediaPrice : existsMediaSupplierPriceList){
                if(CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
                    for(MediaPriceCell mediaPriceCell : mediaPrice.getMediaPriceCellList()){
                        if(StringUtils.isNotEmpty(mediaPriceCell.getCell())){
                            supplierPriceMap.put(mediaPriceCell.getSupplierId()+"-"+mediaPriceCell.getCell(),mediaPriceCell.getCellValue());
                        }
                    }
                }
            }

            //计算媒体最低价格
            Map<String, BigDecimal> mediaMinPrice = new HashMap<>();
            if(supplierPriceMap.size() > 0){
                for(String key : supplierPriceMap.keySet()){
                    String [] keys = key.split("-");
                    String cell = keys[1];
                    //计算Excel中媒体对应类型的最低价格，如果价格小于等于0， 不考虑，由于供应商的价格存在覆盖，所以这计算Excel最低价格
                    if(supplierPriceMap.get(key).compareTo(new BigDecimal(0)) > 0){
                        //如果Excel中，当前媒体对应类型价格为空，则设置当前类型为最低价格
                        if(mediaMinPrice.get(cell) == null ||
                                mediaMinPrice.get(cell).compareTo(supplierPriceMap.get(key)) > 0){
                            mediaMinPrice.put(cell,supplierPriceMap.get(key));
                        }
                    }
                }
            }

            List<MediaExtendAudit> addMediaExtendPriceList = new ArrayList<>(); //新增的扩展价格
            List<MediaExtendAudit> editMediaExtendPriceList = new ArrayList<>(); //编辑的扩展价格
            for(MediaForm1 mediaForm1 : priceFields){
                MediaExtendAudit mediaExtendAudit = new MediaExtendAudit();
                mediaExtendAudit.setMediaId(mediaId);
                mediaExtendAudit.setCell(mediaForm1.getCellCode());
                mediaExtendAudit.setCellName(mediaForm1.getCellName());
                mediaExtendAudit.setDbType(mediaForm1.getType());
                mediaExtendAudit.setType(mediaForm1.getType());
                //当前媒体对应价格最小值为空，则赋值为0，因为上面计算最小值时，没使用0的情况，这是是为了防止出现0之后，后面计算最小值失效，所以在此处设置
                if(mediaMinPrice.get(mediaForm1.getCellCode()) == null){
                    mediaMinPrice.put(mediaForm1.getCellCode(), new BigDecimal(0));
                }
                mediaExtendAudit.setCellValue(String.valueOf(mediaMinPrice.get(mediaForm1.getCellCode())));
                //如果媒体原来有价格，则为修改
                if(existsMediaPriceMap.get(mediaForm1.getCellCode()) != null){
                    editMediaExtendPriceList.add(mediaExtendAudit);
                }else{  //如果媒体原来没有价格，则为新增价格
                    addMediaExtendPriceList.add(mediaExtendAudit);
                }
            }

            if(CollectionUtils.isNotEmpty(addMediaExtendPriceList)){
                mediaExtendAuditMapper.saveBatch(addMediaExtendPriceList);
            }
            if(CollectionUtils.isNotEmpty(editMediaExtendPriceList)){
                mediaExtendAuditMapper.updateBatchMoreMedia(editMediaExtendPriceList);
            }
        }*/

        //9、拷贝媒体到t_media1表
        copyMediaAudit2Media(tempMediaId);

        //10、计算最小价格
//        setMedia1MinPrice(Arrays.asList(tempMediaId));
    }

    @Override
    @Transactional
    public void stop(Integer id,  Integer standarPlatformFlag) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        //启用和激活都需要经过审核
        MediaAudit mediaAudit = mediaAuditMapper.getMediaById(id);
        //仅能责任人自己能停用
        if(!user.getId().equals(mediaAudit.getUserId())){
            throw new QinFeiException(1002, "很抱歉，您没有操作别人媒体的权限！");
        }

        //媒体异动前数据
        List<Media1> oldMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaAudit.getId()));

        //标准板块不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            mediaAuditMapper.updateMediaEnabled(id,1,1,user.getId()); //1-停用
            media1Mapper.deleteByMediaId(mediaAudit.getId());
            media1Mapper.copyMediaById(mediaAudit.getId());
        }else {
            mediaAuditMapper.updateMediaEnabled(id,1,0,user.getId()); //1-停用
            //更新媒体是删除字段，当媒体修改后，先不直接修改state字段为0，先用这个字段代替，方便媒体驳回后返回之前状态使用
            media1Mapper.updateMediaIsDelete(mediaAudit.getId(), 1, user.getId());
        }

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            //媒体新增成功，才能进行媒体异动 和 供应商异动的操作
            if(mediaAudit != null && mediaAudit.getId() != null){
                List<Media1> newMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaAudit.getId()));
                List<MediaChange> mediaChangeList = mediaChangeHandler(newMediaList, oldMediaList, user);
                if(CollectionUtils.isNotEmpty(mediaChangeList)){
                    mediaChangeMapper.saveBatch(mediaChangeList);
                }
            }
        }catch (Exception e){
            log.error("【媒体停用】媒体异动记录异常: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void stopRelate(Integer mediaId, Integer relateId, Integer standarPlatformFlag) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        //启用和激活都需要经过审核
        MediaAudit mediaAudit = mediaAuditMapper.getMediaById(mediaId);
        //仅能责任人自己能停用
        if(!user.getId().equals(mediaAudit.getUserId())){
            throw new QinFeiException(1002, "很抱歉，您没有操作别人媒体的权限！");
        }

        List<Integer> relateIds = Arrays.asList(relateId);
        //异动前数据
        List<MediaSupplierRelate> oldMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);

        //标准平台不进行审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            mediaSupplierRelateAuditMapper.updateEnableById(1,1, user.getId(), relateId);//1-停用
            mediaSupplierRelateMapper.deleteByIds(relateIds);
            mediaSupplierRelateMapper.copySupplierRelateByIds(relateIds);
        }else {
            mediaSupplierRelateAuditMapper.updateEnableById(1,0, user.getId(), relateId);//1-停用
            //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
            mediaSupplierRelateMapper.updateMediaRelateIsDelete(1, user.getId(),relateId);
        }

        //处理媒体关系异动，增加异常捕获，使其不影响正常操作
        try{
            if(CollectionUtils.isNotEmpty(Arrays.asList(relateId))){
                List<MediaSupplierRelate> newMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);
                List<MediaSupplierChange> mediaSupplierChangeList = mediaSupplierChangeHandler(Arrays.asList(mediaAudit), newMediaRelateList, oldMediaRelateList, user);
                if(CollectionUtils.isNotEmpty(mediaSupplierChangeList)){
                    mediaSupplierChangeMapper.saveBatch(mediaSupplierChangeList);
                }
            }
        }catch (Exception e){
            log.error("【媒体关系停用】媒体异动记录异常: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void active(Integer id, Integer standarPlatformFlag) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        //启用和激活都需要经过审核
        MediaAudit mediaAudit = mediaAuditMapper.getMediaById(id);
        //仅能责任人自己能启用
        if(!user.getId().equals(mediaAudit.getUserId())){
            throw new QinFeiException(1002, "很抱歉，您没有操作别人媒体的权限！");
        }

        //媒体异动前数据
        List<Media1> oldMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaAudit.getId()));

        //标准板块不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            mediaAuditMapper.updateMediaEnabled(id,0,1,user.getId()); //1-停用
            media1Mapper.deleteByMediaId(mediaAudit.getId());
            media1Mapper.copyMediaById(mediaAudit.getId());
        }else {
            mediaAuditMapper.updateMediaEnabled(id,0,0,user.getId()); //0-启用
            //更新媒体是删除字段，当媒体修改后，先不直接修改state字段为0，先用这个字段代替，方便媒体驳回后返回之前状态使用
            media1Mapper.updateMediaIsDelete(mediaAudit.getId(), 1, user.getId());
        }

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            //媒体新增成功，才能进行媒体异动 和 供应商异动的操作
            if(mediaAudit != null && mediaAudit.getId() != null){
                List<Media1> newMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaAudit.getId()));
                List<MediaChange> mediaChangeList = mediaChangeHandler(newMediaList, oldMediaList, user);
                if(CollectionUtils.isNotEmpty(mediaChangeList)){
                    mediaChangeMapper.saveBatch(mediaChangeList);
                }
            }
        }catch (Exception e){
            log.error("【媒体启用】媒体异动记录异常: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void activeRelate(Integer mediaId, Integer relateId, Integer standarPlatformFlag) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        //启用和激活都需要经过审核
        MediaAudit mediaAudit = mediaAuditMapper.getMediaById(mediaId);
        //仅能责任人自己能启用
        if(!user.getId().equals(mediaAudit.getUserId())){
            throw new QinFeiException(1002, "很抱歉，您没有操作别人媒体的权限！");
        }

        List<Integer> relateIds = Arrays.asList(relateId);
        //异动前数据
        List<MediaSupplierRelate> oldMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);

        //标准板块不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            mediaSupplierRelateAuditMapper.updateEnableById(0, 1, user.getId(), relateId);//0-启用
            mediaSupplierRelateMapper.deleteByIds(relateIds);
            mediaSupplierRelateMapper.copySupplierRelateByIds(relateIds);
        }else {
            mediaSupplierRelateAuditMapper.updateEnableById(0, 0, user.getId(), relateId);//0-启用
            //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
            mediaSupplierRelateMapper.updateMediaRelateIsDelete(1, user.getId(),relateId);
        }

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            //如果有新增媒体供应商关系
            if(CollectionUtils.isNotEmpty(Arrays.asList(relateId))){
                List<MediaSupplierRelate> newMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);
                List<MediaSupplierChange> mediaSupplierChangeList = mediaSupplierChangeHandler(Arrays.asList(mediaAudit), newMediaRelateList, oldMediaRelateList, user);
                if(CollectionUtils.isNotEmpty(mediaSupplierChangeList)){
                    mediaSupplierChangeMapper.saveBatch(mediaSupplierChangeList);
                }
            }
        }catch (Exception e){
            log.error("【媒体关系启用】媒体异动记录异常: {}", e.getMessage());
        }
    }

    @Transactional
    @Override
    public void reject(Integer id) {
        MediaAudit mediaAudit = this.getById(id);
        if(mediaAudit == null){
            throw new QinFeiException(1002,"媒体信息不存在");
        }
        try{
            validationQX(AppUtil.getUser(),Arrays.asList(mediaAudit)); //权限判断
            copyMedia2MediaAudit(id); //信息回到修改前信息
            mediaAuditMapper.updateMediaState(id,-1,AppUtil.getUser().getId()); //更新媒体状态为驳回
            sendMessage(mediaAudit, false);
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"驳回失败，出现异常！");
        }
    }

    @Transactional
    @Override
    public void rejectRelate(Integer mediaId, Integer relateId) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        MediaAudit mediaAudit = this.getById(mediaId);
        if(mediaAudit == null){
            throw new QinFeiException(1002,"媒体信息不存在");
        }
        try{
            validationQX(AppUtil.getUser(),Arrays.asList(mediaAudit)); //权限判断
            copyMediaRelate2MediaRelateAudit(Arrays.asList(relateId)); //信息回到修改前信息
            mediaSupplierRelateAuditMapper.updateStateById(-1, user.getId(), relateId);

            //重新设置实际表媒体最低价
//            setMediaAuditMinPrice(Arrays.asList(mediaId));
            sendMessage(mediaAudit, false);
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"驳回失败，出现异常！");
        }
    }

    @Transactional
    @Override
    public void deleteMedia(Integer id, Integer standarPlatformFlag) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        MediaAudit mediaAudit = this.getById(id);
        if(mediaAudit == null){
            throw new QinFeiException(1002,"媒体信息不存在");
        }
        //仅能责任人自己能删除
        if(!user.getId().equals(mediaAudit.getUserId())){
            throw new QinFeiException(1002, "很抱歉，您没有操作别人媒体的权限！");
        }
        List<Integer> mediaIds = mediaAuditMapper.getArticleByMediaId(Arrays.asList(id));
        List<Integer> mediaIds1 = mediaAuditMapper.getArticleImportByMediaId(Arrays.asList(id));
        if(CollectionUtils.isNotEmpty(mediaIds) || CollectionUtils.isNotEmpty(mediaIds1)){  //需要判断待删除的媒体ID是否已经有创建稿件，有的话提示不能删除
            throw new QinFeiException(1002,"选中的媒体中有关联的稿件，无法删除！");
        }
        User currentUser = AppUtil.getUser();

        //标准板块不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            //删除先用isDelete这个字段代替,然后将状态设置为0-待审核
            mediaAuditMapper.updateMediaIsDeleteById(id, 1, 1, currentUser.getId());
            media1Mapper.deleteByMediaId(mediaAudit.getId());
            media1Mapper.copyMediaById(mediaAudit.getId());
        }else {
            //删除先用isDelete这个字段代替,然后将状态设置为0-待审核
            mediaAuditMapper.updateMediaIsDeleteById(id, 1, 0, currentUser.getId());
            //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
            media1Mapper.updateMediaIsDelete(id, 1, currentUser.getId());
            sendMessage(mediaAudit, null);
        }
    }

    @Transactional
    @Override
    public void deleteMediaRelate(Integer mediaId, Integer supplierId, Integer relateId, Integer standarPlatformFlag) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        MediaAudit mediaAudit = this.getById(mediaId);
        if(mediaAudit == null){
            throw new QinFeiException(1002,"媒体信息不存在");
        }
        //权限判断
        if(!user.getId().equals(mediaAudit.getUserId())){
            throw new QinFeiException(1002, "很抱歉，您没有操作别人媒体的权限！");
        }
        List<Integer> supplierIds = mediaAuditMapper.listArticleBySupplierId(Arrays.asList(supplierId), mediaId);
        if(CollectionUtils.isNotEmpty(supplierIds)){
            throw new QinFeiException(1002,"删除的供应商关系已存在稿件，不允许删除，您可以进行停用！");
        }
        User currentUser = AppUtil.getUser();

        //标准板块不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            mediaSupplierRelateAuditMapper.updateIsDeleteById(1,1,currentUser.getId(),relateId);
            mediaSupplierRelateMapper.deleteByIds(Arrays.asList(relateId));
            mediaSupplierRelateMapper.copySupplierRelateByIds(Arrays.asList(relateId));
        }else {
            mediaSupplierRelateAuditMapper.updateIsDeleteById(1,0,currentUser.getId(),relateId);
            //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
            mediaSupplierRelateMapper.updateMediaRelateIsDelete(1, currentUser.getId(),relateId);
            //重新设置实际表媒体最低价
//        setMediaAuditMinPrice(Arrays.asList(mediaId));
            sendMessage(mediaAudit, null);
        }
    }

    @Transactional
    @Override
    public void passBatch(List<Integer> ids, List<String> mediaNames, List<Integer> userIds) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new QinFeiException(1002, "媒体审核ID不能为空");
        }
        int subLength = 50;  // 定义需要进行分割的尺寸，每次批量处理媒体数。
        int insertTimes = ids.size() % subLength == 0 ? ids.size() / subLength : ids.size() / subLength + 1; // 计算需要插入的次数，100条插入一次；
        if (insertTimes > 1) {
            List<Integer> tempIds;
            List<String> tempMediaNames;
            List<Integer> tempUserIds;
            for (int i = 0; i < insertTimes; i++) {
                tempIds = new ArrayList<>();
                tempMediaNames = new ArrayList<>();
                tempUserIds = new ArrayList<>();
                for (int j = i * subLength; j < (i + 1) * subLength && j < ids.size(); j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                    tempIds.add(ids.get(j));
                    tempMediaNames.add(mediaNames.get(j));
                    tempUserIds.add(userIds.get(j));
                }
                onceTimeBatchPass(tempIds, tempMediaNames, tempUserIds);
            }
        } else {
            onceTimeBatchPass(ids, mediaNames, userIds);
        }
    }

    //单次审核媒体
    private void onceTimeBatchPass(List<Integer> ids, List<String> mediaNames, List<Integer> userIds) {
        //不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> mediaAuditList = mediaAuditMapper.listMediaByIds(ids, onlineTime);
        if(CollectionUtils.isEmpty(mediaAuditList) || ids.size() != mediaAuditList.size()){
            throw new QinFeiException(1002,"媒体信息不存在或媒体操作数量与查询数量不一致");
        }
        validationQX(AppUtil.getUser(),mediaAuditList); //权限判断
        // 判断是否是业务部长 如果是业务部长则将state改为1 业务组长就改为2；
//        int state = (AppUtil.isRoleType(IConst.ROLE_TYPE_MJ) && AppUtil.isRoleCode(IConst.ROLE_CODE_BZ)) ? 1 : 2;

        mediaAuditMapper.batchUpdateMediaState(ids,1,AppUtil.getUser().getId());
        //1、先更新稿件表（t_biz_article、t_biz_article_import）中和媒体相关的数据
        Map param = new HashMap();
        param.put("mediaList",mediaAuditList);
        media1Mapper.batchUpdateArticle(param);
        media1Mapper.batchUpdateArticleImport(param);
        //2、拷贝对应审核表信息到实际使用表
        batchCopyMediaAuditBaseInfo2Media(ids);
        //3、计算最小值
//        setMedia1MinPrice(ids);
        sendMessage(mediaNames, userIds, true);
    }

    @Transactional
    @Override
    public void passBatchRelate(List<Integer> mediaIds, List<String> mediaNames, List<Integer> userIds, List<Integer> relateIds) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        if (CollectionUtils.isEmpty(mediaIds) || CollectionUtils.isEmpty(relateIds)) {
            throw new QinFeiException(1002, "媒体ID或媒体供应商关系ID不能为空！");
        }
        int subLength = 50;  // 定义需要进行分割的尺寸，每次批量处理媒体数。
        int insertTimes = relateIds.size() % subLength == 0 ? relateIds.size() / subLength : relateIds.size() / subLength + 1; // 计算需要插入的次数，100条插入一次；
        if (insertTimes > 1) {
            List<Integer> tempMediaIds;
            List<String> tempMediaNames;
            List<Integer> tempUserIds;
            List<Integer> tempRelateIds;
            for (int i = 0; i < insertTimes; i++) {
                tempMediaIds = new ArrayList<>();
                tempMediaNames = new ArrayList<>();
                tempUserIds = new ArrayList<>();
                tempRelateIds = new ArrayList<>();
                for (int j = i * subLength; j < (i + 1) * subLength && j < relateIds.size(); j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                    tempMediaIds.add(mediaIds.get(j));
                    tempMediaNames.add(mediaNames.get(j));
                    tempUserIds.add(userIds.get(j));
                    tempRelateIds.add(relateIds.get(j));
                }
                onceTimeRelateBatchPass(user, tempMediaIds, tempMediaNames, tempUserIds, tempRelateIds);
            }
        } else {
            onceTimeRelateBatchPass(user, mediaIds, mediaNames, userIds, relateIds);
        }
    }

    //单次审核媒体供应商关系
    public void onceTimeRelateBatchPass(User user, List<Integer> mediaIds, List<String> mediaNames, List<Integer> userIds, List<Integer> relateIds) {
        //不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> mediaAuditList = mediaAuditMapper.listMediaByIds(mediaIds, onlineTime);
        //由于一个媒体对于多个供应商，这里mediaIds可能存在重复的，所以需要去重，下面在进行数量比较
        Set<Integer> mediaIdSet = new HashSet<>();
        mediaIds.forEach(mediaId -> {
            mediaIdSet.add(mediaId);
        });
        if (CollectionUtils.isEmpty(mediaAuditList) || mediaAuditList.size() != mediaIdSet.size()) {
            throw new QinFeiException(1002, "媒体信息不存在或媒体操作数量与查询数量不一致");
        }
        validationQX(AppUtil.getUser(), mediaAuditList); //权限判断

        //更新关系状态
        Map<String, Object> param = new HashMap<>();
        param.put("state", 1);
        param.put("updateId", user.getId());
        param.put("ids", relateIds);
        mediaSupplierRelateAuditMapper.updateStateByIds(param);

        //拷贝对应审核表信息到实际使用表
        copyMediaRelateAudit2MediaRelate(relateIds);

        //重新设置实际表媒体最低价
//        setMedia1MinPrice(mediaIds);

        sendMessage(mediaNames, userIds, true);
    }

    //更新媒体供应商价格后，媒体不需要审核，审核关系通过后，重置实际表媒体最低值
    private void setMedia1MinPrice(List<Integer> mediaIds){
        Map<String, Object> param = new HashMap<>();
        param.put("mediaIds", mediaIds);
        List<MediaPrice> mediaPriceList = media1Mapper.getMediaSupplierInfoByMediaIds(param); //获取当前媒体，通过验证的价格
        Map<String, MediaExtend> minPrice = new HashMap<>(); //计算媒体已审核关系的最小价格
        if(CollectionUtils.isNotEmpty(mediaPriceList)){
            List<String> existsMediaPriceList = mediaExtendMapper.listMediaPriceTypeByMediaIds(mediaIds);
            //此处不处理价格为0的结果
            for(MediaPrice mediaPrice : mediaPriceList){
                for(MediaPriceCell mediaPriceCell : mediaPrice.getMediaPriceCellList()){
                    String key = mediaPrice.getMediaId() + "-" +mediaPriceCell.getCell();
                    if(!minPrice.containsKey(key)){
                        MediaExtend mediaExtend = new MediaExtend();
                        mediaExtend.setMediaId(mediaPrice.getMediaId());
                        mediaExtend.setCell(mediaPriceCell.getCell());
                        mediaExtend.setCellName(mediaPriceCell.getCellName());
                        if(new BigDecimal(mediaPriceCell.getCellValue()).compareTo(new BigDecimal(0)) > 0){
                            mediaExtend.setCellValue(mediaPriceCell.getCellValue());
                        }
                        mediaExtend.setDbType("price");
                        mediaExtend.setType("price");
                        if(!existsMediaPriceList.contains(key)){ //如果当前媒体扩展字段没有这个价格，则新增
                            mediaExtend.setEditAddStatus(true);
                        }
                        minPrice.put(key, mediaExtend);
                    }
                    //当前价格大于0时，才做比较，否则用0比较最小值一定为0，出现错误
                    if(new BigDecimal(mediaPriceCell.getCellValue()).compareTo(new BigDecimal(0)) > 0 &&
                            (StringUtils.isEmpty(minPrice.get(key).getCellValue()) || new BigDecimal(minPrice.get(key).getCellValue()).compareTo(new BigDecimal(mediaPriceCell.getCellValue())) > 0)){
                        minPrice.get(key).setCellValue(String.valueOf(mediaPriceCell.getCellValue()));
                    }
                }
            }
            mediaPriceList.clear();
            mediaPriceList = null; //对象已使用完毕，对其进行释放

            List<MediaExtend> editMediaExtentList = new ArrayList<>(); //修改的扩展价格
            List<MediaExtend> addMediaExtentList = new ArrayList<>(); //新增的扩展价格
            //此处当价格值为空时，设置为0
            for(String key : minPrice.keySet()){
                if(StringUtils.isEmpty(minPrice.get(key).getCellValue())){
                    minPrice.get(key).setCellValue("0.00");
                }
                if(minPrice.get(key).isEditAddStatus()){
                    addMediaExtentList.add(minPrice.get(key));
                }else{
                    editMediaExtentList.add(minPrice.get(key));
                }
            }
            minPrice.clear();
            minPrice = null; //对象已使用完毕，对其进行释放

            if(CollectionUtils.isNotEmpty(editMediaExtentList)){
                mediaExtendMapper.updateBatchMoreMedia(editMediaExtentList);
            }
            editMediaExtentList.clear();
            editMediaExtentList = null; //对象已使用完毕，对其进行释放

            if(CollectionUtils.isNotEmpty(addMediaExtentList)){
                mediaExtendMapper.saveBatch(addMediaExtentList);
            }
            addMediaExtentList.clear();
            addMediaExtentList = null; //对象已使用完毕，对其进行释放

        }else {
            mediaExtendMapper.updateMediaPriceAsZero(mediaIds);
        }
    }

    //更新媒体供应商价格后，媒体不需要审核，审核关系通过后，重置实际表媒体最低值
    private void setMediaAuditMinPrice(List<Integer> mediaIds){
        Map<String, Object> param = new HashMap<>();
        param.put("mediaIds", mediaIds);
        List<MediaPrice> mediaPriceList = mediaAuditMapper.listMediaSupplierPriceByMediaIds(param); //获取当前媒体，通过验证的价格
        Map<String, MediaExtendAudit> minPrice = new HashMap<>(); //计算媒体已审核关系的最小价格
        if(CollectionUtils.isNotEmpty(mediaPriceList)){
            List<String> existsMediaPriceList = mediaExtendAuditMapper.listMediaPriceTypeByMediaIds(mediaIds);
            //此处不处理价格为0的结果
            for(MediaPrice mediaPrice : mediaPriceList){
                for(MediaPriceCell mediaPriceCell : mediaPrice.getMediaPriceCellList()){
                    String key = mediaPrice.getMediaId() + "-" +mediaPriceCell.getCell();
                    if(!minPrice.containsKey(key)){
                        MediaExtendAudit mediaExtend = new MediaExtendAudit();
                        mediaExtend.setMediaId(mediaPrice.getMediaId());
                        mediaExtend.setCell(mediaPriceCell.getCell());
                        mediaExtend.setCellName(mediaPriceCell.getCellName());
                        if(new BigDecimal(mediaPriceCell.getCellValue()).compareTo(new BigDecimal(0)) > 0){
                            mediaExtend.setCellValue(String.valueOf(mediaPriceCell.getCellValue()));
                        }
                        mediaExtend.setDbType("price");
                        mediaExtend.setType("price");
                        if(!existsMediaPriceList.contains(key)){ //如果当前媒体扩展字段没有这个价格，则新增
                            mediaExtend.setEditAddStatus(true);
                        }
                        minPrice.put(key, mediaExtend);
                    }
                    //当前价格大于0时，才做比较，否则用0比较最小值一定为0，出现错误
                    if(new BigDecimal(mediaPriceCell.getCellValue()).compareTo(new BigDecimal(0)) > 0 &&
                            (StringUtils.isEmpty(minPrice.get(key).getCellValue()) || new BigDecimal(minPrice.get(key).getCellValue()).compareTo(new BigDecimal(mediaPriceCell.getCellValue())) > 0)){
                        minPrice.get(key).setCellValue(String.valueOf(mediaPriceCell.getCellValue()));
                    }
                }
            }
            mediaPriceList.clear();
            mediaPriceList = null; //对象已使用完毕，对其进行释放

            List<MediaExtendAudit> editMediaExtentList = new ArrayList<>(); //修改的扩展价格
            List<MediaExtendAudit> addMediaExtentList = new ArrayList<>(); //新增的扩展价格
            //此处当价格值为空时，设置为0
            for(String key : minPrice.keySet()){
                if(StringUtils.isEmpty(minPrice.get(key).getCellValue())){
                    minPrice.get(key).setCellValue("0.00");
                }
                if(minPrice.get(key).isEditAddStatus()){
                    addMediaExtentList.add(minPrice.get(key));
                }else{
                    editMediaExtentList.add(minPrice.get(key));
                }
            }
            minPrice.clear();
            minPrice = null; //对象已使用完毕，对其进行释放

            if(CollectionUtils.isNotEmpty(editMediaExtentList)){
                mediaExtendAuditMapper.updateBatchMoreMedia(editMediaExtentList);
            }
            editMediaExtentList.clear();
            editMediaExtentList = null; //对象已使用完毕，对其进行释放
            if(CollectionUtils.isNotEmpty(addMediaExtentList)){
                mediaExtendAuditMapper.saveBatch(addMediaExtentList);
            }
            addMediaExtentList.clear();
            addMediaExtentList = null; //对象已使用完毕，对其进行释放
        }else{ //如果没有，则设置扩展价格为0
            mediaExtendAuditMapper.updateMediaPriceAsZero(mediaIds);
        }
    }

    /**
     * 拷贝媒体审核基本信息 到 实际媒体基本信息(不拷贝价格扩展，价格扩展由媒体关系审核通过设置)
     * @param mediaIds 媒体ID集合
     */
    private void batchCopyMediaAuditBaseInfo2Media(List<Integer> mediaIds){
        //由于媒体审核仅审核媒体，不审核关系，所以此处不处理关系数据。2019-09-10
       /* List<Integer> auditRelateIds = mediaSupplierRelateAuditMapper.listIdByMediaIds(mediaIds);
        List<Integer> relateIds = mediaSupplierRelateMapper.listIdByMediaIds(mediaIds);
        if(CollectionUtils.isNotEmpty(relateIds)){
            Set<Integer> relateIdSet = new HashSet<>();
            relateIdSet.addAll(relateIds);
            mediaSupplierPriceMapper.deleteByRelateIds(relateIdSet);
            mediaSupplierRelateMapper.deleteByMediaIds(mediaIds);
        }
        if(CollectionUtils.isNotEmpty(auditRelateIds)){
            Set<Integer> auditParam = new HashSet<>();
            auditParam.addAll(auditRelateIds);
            mediaSupplierRelateMapper.copySupplierRelateByMediaIds(mediaIds);
            mediaSupplierPriceMapper.copySupplierPriceByRelateIds(auditParam);
        }*/
        media1Mapper.deleteBatch(mediaIds);
        media1Mapper.copyMediaByIds(mediaIds);

        mediaExtendMapper.deleteBatch(mediaIds);
        mediaExtendMapper.copyMediaExtendByMediaIds(mediaIds);
//        mediaExtendMapper.updateMediaPriceAsZero(mediaIds); //设置扩展价格为0
    }

    /**
     * 实际媒体 到 拷贝媒体审核信息
     * @param mediaIds 媒体ID集合
     */
    private void batchCopyMedia2MediaAudit(List<Integer> mediaIds){
        //由于媒体关系有专门的关系管理，所以此处仅处理媒体基本信息 2019-09-10
        //1、根据媒体集合获取有关系的供应商记录
        /*List<MediaSupplierRelate> mediaSupplierRelates = mediaSupplierRelateMapper.listMediaSupplierRelateByMediaIds(mediaIds);
        Map<Integer, Set<Integer>> mediaSupplierRelateMap = new HashMap<>();
        Set<Integer> relateIds = new HashSet();  //实际关系表的关系ID
        List mediaList = new ArrayList(); //实际有关系的媒体列表
        List<Integer> auditRelateIds = new ArrayList<>(); //审核关系表的关系ID
        if(CollectionUtils.isNotEmpty(mediaSupplierRelates)){
            for(MediaSupplierRelate mediaSupplierRelate : mediaSupplierRelates){
                if(mediaSupplierRelateMap.get(mediaSupplierRelate.getMediaId()) == null){
                    mediaSupplierRelateMap.put(mediaSupplierRelate.getMediaId(), new HashSet<>());
                }
                mediaSupplierRelateMap.get(mediaSupplierRelate.getMediaId()).add(mediaSupplierRelate.getId());
                relateIds.add(mediaSupplierRelate.getId());
            }

            //获取有关系媒体的审核关系表的关系集合
            mediaList.addAll(mediaSupplierRelateMap.keySet());
            auditRelateIds = mediaSupplierRelateAuditMapper.listIdByMediaIds(mediaList);
        }

        //如果实际媒体表有数据，说明不是新增的媒体驳回，而是被修改的媒体驳回，可以删除审核表后再拷贝，否不能删除审核表媒体，不然数据就没了
        if(CollectionUtils.isNotEmpty(mediaSupplierRelates)){
            Set<Integer> auditParam = new HashSet<>();
            auditParam.addAll(auditRelateIds);
            mediaSupplierPriceAuditMapper.deleteByRelateId(auditParam);
            mediaSupplierRelateAuditMapper.deleteByMediaIds(mediaList);

            mediaSupplierRelateAuditMapper.copySupplierRelateByMediaIds(mediaList);
            mediaSupplierPriceAuditMapper.copySupplierPriceByRelateIds(relateIds);
        }*/
       List<Integer> ids = media1Mapper.getMediaByIds(mediaIds);
        if(CollectionUtils.isNotEmpty(ids)){
            media1Mapper.batchUpdateMediaIsDelete(ids, 0, AppUtil.getUser().getId()); //先更新实际表状态，然后再进行拷贝

            mediaAuditMapper.deleteBatch(ids);
            mediaAuditMapper.copyMediaByIds(ids);

            mediaExtendAuditMapper.deleteBatch(ids);
            mediaExtendAuditMapper.copyMediaExtendByMediaIds(ids);
        }
    }

    @Transactional
    @Override
    public void rejectBatch(List<Integer> ids, List<String> mediaNames, List<Integer> userIds) {
        //不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> mediaAuditList = mediaAuditMapper.listMediaByIds(ids, onlineTime);
        if(CollectionUtils.isEmpty(mediaAuditList) || ids.size() != mediaAuditList.size()){
            throw new QinFeiException(1002,"媒体信息不存在或媒体操作数量与查询数量不一致");
        }
        validationQX(AppUtil.getUser(),mediaAuditList); //权限判断
        batchCopyMedia2MediaAudit(ids);  //恢复成修改前状态
        mediaAuditMapper.batchUpdateMediaState(ids,-1,AppUtil.getUser().getId()); //更新状态为驳回
        sendMessage(mediaNames, userIds, false);
    }

    @Transactional
    @Override
    public void rejectBatchRelate(List<Integer> mediaIds, List<String> mediaNames, List<Integer> userIds, List<Integer> relateIds) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002,"请先登录！");
        }
        //不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> mediaAuditList = mediaAuditMapper.listMediaByIds(mediaIds, onlineTime);
        if(CollectionUtils.isEmpty(mediaAuditList) || mediaAuditList.size() != mediaIds.size()){
            throw new QinFeiException(1002,"媒体信息不存在或者媒体操作数量与查询数量不一致");
        }
        validationQX(AppUtil.getUser(),mediaAuditList); //权限判断
        copyMediaRelate2MediaRelateAudit(relateIds);  //恢复成修改前状态
        Map<String, Object> param = new HashMap<>();
        param.put("state", -1);
        param.put("updateId", user.getId());
        param.put("ids", relateIds);
        mediaSupplierRelateAuditMapper.updateStateByIds(param);

        //重新设置实际表媒体最低价
//        setMediaAuditMinPrice(mediaIds);
        sendMessage(mediaNames, userIds, false);
    }

    @Transactional
    @Override
    public void deleteBatch(List<Integer> ids, List<String> mediaNames, List<Integer> userIds, Integer standarPlatformFlag) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        //不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> mediaAuditList = mediaAuditMapper.listMediaByIds(ids, onlineTime);
        if(CollectionUtils.isEmpty(mediaAuditList) || ids.size() != mediaAuditList.size()){
            throw new QinFeiException(1002,"媒体信息不存在或媒体操作数量与查询数量不一致");
        }
        //权限校验
        if(CollectionUtils.isNotEmpty(mediaAuditList)){
            for(MediaAudit mediaAudit : mediaAuditList){
                if(!mediaAudit.getUserId().equals(user.getId())){
                    throw new QinFeiException(1002, "很抱歉，您删除的媒体中含有他人媒体，您没有操作别人媒体的权限！");
                }
            }
        }

        List<Integer> mediaIds = mediaAuditMapper.getArticleByMediaId(ids);
        List<Integer> mediaIds1 = mediaAuditMapper.getArticleImportByMediaId(ids);
        if(CollectionUtils.isNotEmpty(mediaIds) || CollectionUtils.isNotEmpty(mediaIds1)){  //需要判断待删除的媒体ID是否已经有创建稿件，有的话提示不能删除
            throw new QinFeiException(1002,"选中的媒体中有关联的稿件，无法删除！");
        }
        User currentUser = AppUtil.getUser();

        //标准板块不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            //删除先用isDelete这个字段代替,然后将状态设置为0-待审核
            mediaAuditMapper.batchUpdateMediaIsDeleteById(ids, 1, 1, currentUser.getId());
            media1Mapper.deleteBatch(ids);
            media1Mapper.copyMediaByIds(ids);
        }else {
            //删除先用isDelete这个字段代替,然后将状态设置为0-待审核
            mediaAuditMapper.batchUpdateMediaIsDeleteById(ids, 1, 0, currentUser.getId());
            //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
            media1Mapper.batchUpdateMediaIsDelete(ids, 1, currentUser.getId());
            sendMessage(mediaNames, userIds, null);
        }
    }

    @Transactional
    @Override
    public void deleteBatchRelate(List<Integer> mediaIds, List<String> mediaNames, List<Integer> userIds, List<String> mediaSupplierRelates, List<Integer> relateIds, Integer standarPlatformFlag) {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        //不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> mediaAuditList = mediaAuditMapper.listMediaByIds(mediaIds, onlineTime);
        if(CollectionUtils.isEmpty(mediaAuditList) || mediaIds.size() != mediaAuditList.size()){
            throw new QinFeiException(1002,"媒体信息不存在或媒体操作数量与查询数量不一致");
        }
        //权限判断
        if(CollectionUtils.isNotEmpty(mediaAuditList)){
            for(MediaAudit mediaAudit : mediaAuditList){
                if(!mediaAudit.getUserId().equals(user.getId())){
                    throw new QinFeiException(1002, "很抱歉，您删除媒体关系中含有他人媒体关系，您没有操作别人媒体的权限！");
                }
            }
        }
        List<String> mediaSupplierRelateList = mediaAuditMapper.getArticleInfoByMediaId(mediaIds);
        for(String key : mediaSupplierRelates){
            if(mediaSupplierRelateList.contains(key)){
                throw new QinFeiException(1002,"选中的媒体关系中有关联的稿件，无法删除！");
            }
        }
        User currentUser = AppUtil.getUser();
        Map<String, Object> param = new HashMap<>();
        param.put("updateId", currentUser.getId());
        param.put("state", 0);
        param.put("isDelete", 1);
        param.put("ids", relateIds);
        //标准板块不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            param.put("state", 1);
            mediaSupplierRelateAuditMapper.updateIsDeleteByIds(param);
            mediaSupplierRelateMapper.deleteByIds(relateIds);
            mediaSupplierRelateMapper.copySupplierRelateByIds(relateIds);
        }else {
            mediaSupplierRelateAuditMapper.updateIsDeleteByIds(param); //删除先用isDelete这个字段代替,然后将状态设置为0-待审核
            //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
            mediaSupplierRelateMapper.batchUpdateMediaRelateIsDelete(1, currentUser.getId(),relateIds);
        }
        //重新设置实际表媒体最低价
//        setMediaAuditMinPrice(mediaIds);
    }

    @Override
    public void getDataImportTemplate(Integer standarPlatformFlag, int plateId, String plateName, String templateName, OutputStream outputStream) {
       //多表列头组建
        List<Map<String, Object>> sheetInfo = new ArrayList<>();
        Map<String, Object> supplierSheet = new HashMap();
        List<String> supplierRowTitles = new ArrayList<>();
        supplierRowTitles.addAll(supplierService.getSupplierForms());
        supplierSheet.put("templateName","供应商信息导入模板");
        supplierSheet.put("rowTitles",supplierRowTitles);
        supplierSheet.put("notices",supplierService.getSupplierNotices());
        Map<String, Object> extendFieldMap = getMediaExtendFieldsGroupByPrice(plateId);
        Map<String, Object> mediaSheet = new HashMap();
        List<String> mediaRowTitles = new ArrayList<>();

        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            mediaRowTitles.addAll(standarPlateCommonFieldMap.get(1));
        }else {
            mediaRowTitles.addAll(mediaCommonFieldName);
        }
        mediaRowTitles.addAll((Collection<? extends String>) extendFieldMap.get("fieldNames"));
        mediaSheet.put("templateName",templateName);
        mediaSheet.put("rowTitles",mediaRowTitles);
        mediaSheet.put("notices",getMediaNotices());
        Map<String, Object> mediaSupplierSheet = new HashMap();
        List<String> mediaSupplierRowTitles = new ArrayList<>();

        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            mediaSupplierRowTitles.addAll(standarMediaSupplierCommonFieldName);
        }else {
            mediaSupplierRowTitles.addAll(mediaSupplierCommonFieldName);
        }

        mediaSupplierRowTitles.addAll((Collection<? extends String>) extendFieldMap.get("priceFieldNames"));
        mediaSupplierSheet.put("templateName",plateName+"媒体供应商价格导入模板");
        mediaSupplierSheet.put("rowTitles",mediaSupplierRowTitles);
        mediaSupplierSheet.put("notices",getMediaSupplierPriceNotices());
        sheetInfo.add(supplierSheet);
        sheetInfo.add(mediaSheet);
        sheetInfo.add(mediaSupplierSheet);
        // 获取板块的表单数据；
        DataImportUtil.createMoreSheetFile(sheetInfo, outputStream);
    }

    //获取板块所有扩展字段
    private Map<String,Object> getALLMediaExtendFields(Integer plateId){
        List<MediaForm1> extendFormList = mediaForm1Service.listMediaFormByPlateId(plateId);
        List<String> templateColumnFieldNames = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(extendFormList)){
            for(MediaForm1 mediaForm1 : extendFormList){
                String cellName = mediaForm1.getCellName();
                // 是否必填；
                if (mediaForm1.getRequired() == 1) {
                    cellName = "*" + cellName + "*";
                }
                templateColumnFieldNames.add(cellName);
            }
        }
        Map<String,Object> result = new HashMap<>();
        result.put("fieldNames",templateColumnFieldNames);
        result.put("fields",extendFormList);
        return result;
    }

    //获取指定模板的列标题，区分出扩展中的价格
    private Map<String,Object> getMediaExtendFieldsGroupByPrice(Integer plateId){
        List<MediaForm1> extendFormList = mediaForm1Service.listMediaFormByPlateId(plateId);
        List<MediaForm1> extendSupplierList = new ArrayList<>(); //供应商列单独存放，给供应商媒体关系模板使用
        List<MediaForm1> extendMediaList = new ArrayList<>(); //媒体列，给媒体模板使用
        List<String> templateColumnFieldNames = new ArrayList<>();

        List<MediaForm1> extendMediaPriceList = new ArrayList<>(); //媒体价格列，给媒体模板使用
        List<String> extentMediaPriceColumnList = new ArrayList<>();

        List<String> templateSupplierColumnFieldNames = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(extendFormList)){
            for(MediaForm1 mediaForm1 : extendFormList){
                String cellName = mediaForm1.getCellName();
                // 是否必填；
                if (mediaForm1.getRequired() == 1) {
                    cellName = "*" + cellName + "*";
                }
                //扩展字段标识：0-仅媒体用、1-仅供应商用，对于媒体价格字段，默认供应商也可使用
                if("1".equals(String.valueOf(mediaForm1.getExtendFlag()))){
                    templateSupplierColumnFieldNames.add(cellName);
                    extendSupplierList.add(mediaForm1);
                }else {
                    if("price".equals(mediaForm1.getType())){
                        extendMediaPriceList.add(mediaForm1);
                        extentMediaPriceColumnList.add(cellName);
                    }else {
                        //是否爬取标识：0-手工填写、1-仅脚本爬取、2-手工+爬取，仅爬取字段不展示
                        if(!"1".equals(String.valueOf(mediaForm1.getClimbFlag()))){
                            templateColumnFieldNames.add(cellName);
                            extendMediaList.add(mediaForm1);
                        }
                    }
                }
            }
        }
        //媒体末尾添加价格
        if(CollectionUtils.isNotEmpty(extendMediaPriceList)){
            //是否爬取标识：0-手工填写、1-仅脚本爬取、2-手工+爬取，爬取字段不展示
            extendMediaPriceList.forEach(o -> {
                if(!"1".equals(String.valueOf(o.getClimbFlag()))){
                    String cellNameTmp = o.getCellName();
                    // 是否必填；
                    if (o.getRequired() == 1) {
                        cellNameTmp = "*" + cellNameTmp + "*";
                    }
                    templateColumnFieldNames.add(cellNameTmp);
                    extendMediaList.add(o);
                }
            });
            //供应商末尾添加媒体价格
            templateSupplierColumnFieldNames.addAll(extentMediaPriceColumnList);
            extendSupplierList.addAll(extendMediaPriceList);
        }

        Map<String,Object> result = new HashMap<>();
        result.put("fieldNames",templateColumnFieldNames);
        result.put("fields",extendMediaList);
        result.put("priceFieldNames",templateSupplierColumnFieldNames);
        result.put("priceFields",extendSupplierList);
        return result;
    }

    /**
     * 媒体导入模板文件的操作提示信息；
     * @return ：操作提示信息集合；
     */
    private List<String> getMediaNotices() {
        List<String> notices = new ArrayList<>();

        notices.add("表格数据第一列不能为空；");
        notices.add("带星号标注的列必须有内容；");
        notices.add("价格有效期请填写截止日期；");
        notices.add("无内容的单元格可不填写，留空即可；");
        notices.add("表格的第一行、第一列留空请勿删除；");
        notices.add("日期格式使用yyyy/MM/dd，例如：2019/06/01；");
        notices.add("折扣率为百分制数字，如0.7折扣率，请填写70即可；");
        notices.add("关联的供应商、责任人等信息请确保已录入到系统中且有权限使用；");
        notices.add("案例链接为必输项，有些板块没有该字段，可填写任意值，不留空即可；");
        notices.add("有效的数据行请确保第一列（C列）有内容，否则系统会识别为无效不会进行处理；");
        notices.add("内容为附件或图片的列，请先将文件上传，然后把系统提供的文件地址填入对应的表格中。");
        notices.add("报纸板块的出刊时间请使用中文并且英文逗号分隔，例如：周一周二为出刊时间，内容则填写“周一,周二”；");
        return notices;
    }

    /**
     * 媒体供应商价格导入模板文件的操作提示信息；
     * @return ：操作提示信息集合；
     */
    private List<String> getMediaSupplierPriceNotices() {
        List<String> notices = new ArrayList<>();
        notices.add("表格数据第一列不能为空；");
        notices.add("非必输价格不填，默认为0；");
        notices.add("供应商价格允许两位小数位；");
        notices.add("带星号标注的列必须有内容；");
        notices.add("表格的第一行、第一列留空请勿删除；");
        notices.add("相同的媒体和供应商会覆盖原有的供应商价格；");
        notices.add("供应商必须规范才能进行绑定，否则不进行数据入库操作；");
        return notices;
    }


    @Transactional
    @Override
    public String batchAddMedia(String fileName, Integer standarPlatformFlag, Integer plateId,String plateName) throws IOException{
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        String s  = fileName.substring(config.getWebDir().length());
        File file = new File(config.getUploadDir() + s);
        Map extendFormMap = getALLMediaExtendFields(plateId);
        List<String> mediaExtendFieldNames = (List<String>)extendFormMap.get("fieldNames");//获取指定媒体板块模板扩展列名称列表
        List<MediaForm1> mediaExtendFields = (List<MediaForm1>)extendFormMap.get("fields");//获取指定媒体板块模板扩展列对应属性值列表
        String result = "0";
        if (file.exists()) {
//            List<Object[]> excelContent = DataImportUtil.getExcelContent(file, 3, 2, (mediaCommonFieldName.size() + mediaExtendFieldNames.size()));
            List<Object[]> excelContent = EasyExcelUtil.getExcelContent(file, 1, 3, 2);
            if (CollectionUtils.isNotEmpty(excelContent)) {
                Map<String, Object>  handResult = handleMeidaData(user, standarPlatformFlag, plateId, plateName, mediaExtendFieldNames,mediaExtendFields, excelContent);
                if(handResult != null){
                    result = DataImportUtil.createFile(String.valueOf(handResult.get("templateName")), config.getUploadDir(), config.getWebDir(), (List<String>) handResult.get("rowTitles"), (List<Object[]>) handResult.get("exportData"));
                }else {
                    result = null;
                }
            }
            return result;
        }else{
            throw  new QinFeiException(1002, "导入文件不存在！");
        }
    }

    @Override
    @Transactional
    public String batchAddMedia1(File file, Integer standarPlatformFlag, Integer plateId, String plateName, Integer fileType) throws IOException {
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if (file.exists()){
            String result = null;
            if(fileType == 0){
                Map<String, Object>  handResult = handleDataByFileType(file, fileType, standarPlatformFlag, plateId, plateName, null);
                if(handResult != null){
                    result = DataImportUtil.createFile(String.valueOf(handResult.get("templateName")), config.getUploadDir(), config.getWebDir(), (List<String>) handResult.get("rowTitles"), (List<Object[]>) handResult.get("exportData"));
                }
            }else if(fileType == 1){
                Map<String, Object> extendFormGroup = getMediaExtendFieldsGroupByPrice(plateId); //获取板块对应媒体表单数据
                Map<String, Object>  handResult = handleDataByFileType(file, fileType, standarPlatformFlag, plateId, plateName, extendFormGroup);
                if(handResult != null){
                    result = DataImportUtil.createFile(String.valueOf(handResult.get("templateName")), config.getUploadDir(), config.getWebDir(), (List<String>) handResult.get("rowTitles"), (List<Object[]>) handResult.get("exportData"));
                }
            }else if(fileType == 2){
                Map<String, Object> extendFormGroup = getMediaExtendFieldsGroupByPrice(plateId); //获取板块对应媒体表单数据
                Map<String, Object>  handResult = handleDataByFileType(file, fileType, standarPlatformFlag, plateId, plateName, extendFormGroup);
                if(handResult != null){
                    result = DataImportUtil.createFile(String.valueOf(handResult.get("templateName")), config.getUploadDir(), config.getWebDir(), (List<String>) handResult.get("rowTitles"), (List<Object[]>) handResult.get("exportData"));
                }
            }else {
                Map<String, Object> extendFormGroup = getMediaExtendFieldsGroupByPrice(plateId); //获取板块对应媒体表单数据
                Map<String, Object>  supplierResult = handleDataByFileType(file, 0, standarPlatformFlag, plateId, plateName, null);
                Map<String, Object>  mediaResult = handleDataByFileType(file, 1,standarPlatformFlag,  plateId, plateName, extendFormGroup);
                Map<String, Object>  mediaSupplierResult = handleDataByFileType(file, 2, standarPlatformFlag, plateId, plateName, extendFormGroup);
                List<Map<String, Object>> sheetInfo = new ArrayList<>();
                if(supplierResult != null || mediaResult != null || mediaSupplierResult != null){
                    sheetInfo.add(supplierResult);
                    sheetInfo.add(mediaResult);
                    sheetInfo.add(mediaSupplierResult);
                    result = DataImportUtil.createMoreSheetFile("媒体导入所有类型失败内容", sheetInfo, config.getUploadDir(), config.getWebDir());
                }
            }
            if(result != null){
                throw new QinFeiException(1003, result); //如果有内容错误，则抛出异常，事务失效，防止一次导入三个Excel的sheet表时，前面sheet数据正常而导致数据入库
            }
            return result;
        }else{
            throw  new QinFeiException(1002, "导入文件不存在！");
        }
    }

    /**
     * 根据类型处理不同上传文件数据
     * @param file 文件对象
     * @param type 0-供应商处理、1-媒体处理、2-供应商价格处理
     * @param standarPlatformFlag 媒体板块标准标识
     * @param plateId 板块Id：仅对1-媒体处理和2-供应商价格处理有效
     * @param plateName 板块名称：仅对1-媒体处理和2-供应商价格处理有效
     * @param extendFormGroup 扩展字段组：仅对1-媒体处理和2-供应商价格处理有效
     */
    private Map<String, Object> handleDataByFileType(File file, Integer type, Integer standarPlatformFlag, Integer plateId, String plateName, Map<String, Object> extendFormGroup){
        List<Object[]> excelContent = null;
        Map<String, Object>  handResult = null;
        if(type == 0){
//            excelContent = DataImportUtil.getExcelContent(file, type,3, 2, 7);
            excelContent = EasyExcelUtil.getExcelContent(file, type+1, 3, 2);
            if (CollectionUtils.isNotEmpty(excelContent)){
                handResult = supplierService.batchAddSupplier(excelContent);
            }
        }else if(type == 1){
            List<String> mediaExtendFieldNames = (List<String>)extendFormGroup.get("fieldNames");//获取指定媒体板块模板扩展列名称列表
            List<MediaForm1> mediaExtendFields = (List<MediaForm1>)extendFormGroup.get("fields");//获取指定媒体板块模板扩展列对应属性值列表
//            excelContent = DataImportUtil.getExcelContent(file, type,3, 2, (commonFieldSize + mediaExtendFieldNames.size()));
            excelContent = EasyExcelUtil.getExcelContent(file, type+1, 3, 2);
            if (CollectionUtils.isNotEmpty(excelContent)){
                handResult = handleMeidaData(AppUtil.getUser(), standarPlatformFlag, plateId, plateName+"媒体", mediaExtendFieldNames,mediaExtendFields, excelContent);
            }else {
                throw new QinFeiException(1002, "媒体sheet表没有数据导入成功，请确认导入板块与模板对应，且内容符合要求，并检查数据是否已存在！");
            }
        }else {
            List<String> mediaSupplierPriceFiledNames = (List<String>)extendFormGroup.get("priceFieldNames");//获取指定媒体板块模板扩展列名称列表
            List<MediaForm1> mediaSupplierPriceExtendFields = (List<MediaForm1>)extendFormGroup.get("priceFields");//获取指定媒体板块模板扩展列对应属性值列表
//            excelContent = DataImportUtil.getExcelContent(file, type,3, 2, (commonFieldSize + mediaSupplierPriceFiledNames.size()));
            excelContent = EasyExcelUtil.getExcelContent(file, type+1, 3, 2);
            if (CollectionUtils.isNotEmpty(excelContent)) {
               handResult = handleSupplierPriceData(AppUtil.getUser(),standarPlatformFlag, plateId, plateName+"供应商价格", mediaSupplierPriceFiledNames,mediaSupplierPriceExtendFields, excelContent);
            }else {
                throw new QinFeiException(1002, "媒体供应商价格sheet表没有数据导入成功，请确认导入板块与模板对应，且内容符合要求，并检查数据是否已存在！");
            }
        }
        return handResult;
    }

    /**
     * 处理媒体导入逻辑
     * @param user 当前登入用户
     * @param plateId 媒体板块ID
     * @param plateName 媒体板块名称
     * @param mediaExtendFieldNames 媒体导入模板列对应列名列表
     * @param mediaExtendFields 媒体导入模板列对应属性值列表
     * @param excelContent 媒体导入文件内容
     */
    private Map<String, Object> handleMeidaData(User user,Integer standarPlatformFlag, Integer plateId, String plateName, List<String> mediaExtendFieldNames, List<MediaForm1> mediaExtendFields, List<Object[]> excelContent){
//        if(CollectionUtils.isEmpty(excelContent)){
//            return "0";
//        }
        Date currentDate = new Date();
        List<String> mediaCommonFieldTemp = new ArrayList<>(); //导入模板列名称
        List<String> rowTitles = new ArrayList<>(); //导入模板总列数

        //根据是否标准媒体决定公共字段
        if(standarPlatformFlag != null && 1 == standarPlatformFlag){
            rowTitles.addAll(standarPlateCommonFieldMap.get(1));
            mediaCommonFieldTemp = standarPlateCommonFieldMap.get(0);
        }else {
            rowTitles.addAll(mediaCommonFieldName);
            mediaCommonFieldTemp = mediaCommonField;
        }
        rowTitles.addAll(mediaExtendFieldNames);
        int totalColumnNum = rowTitles.size();//总列数
        int commonColumnNum = mediaCommonFieldTemp.size();//公共列数
        int rowNum = excelContent.size(); //导入媒体行数

        //-------下拉列表值缓存-开始-------
        userNameMap = null; // 获取所有媒体类型的责任人信息集合；
        supplierNameMap = null; //获取当前板块供应商信息集合
        otherFieldMap = new HashMap<>(); // 其他复选框、下拉列表、单选框。

        Map param = new HashMap();
        param.put("plateId",plateId);
        param.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> existsMediaList = mediaAuditMapper.listMediaByParam(param);//获取板块所有媒体
        Map<String,Integer> existsMediaMap = new HashMap<>(); //获取当前媒体板块下有用的媒体，用于判断名称是否存在,key = name

        //如果是标准板块，媒体唯一判断标准是：板块 + 唯一标识，否则：板块 + 媒体名称
        if(standarPlatformFlag != null && 1 == standarPlatformFlag){
            for(MediaAudit media1 : existsMediaList){
                if(!StringUtils.isEmpty(media1.getMediaContentId())){
                    existsMediaMap.put(media1.getMediaContentId().toLowerCase(Locale.US), media1.getId());
                }
            }
        }else {
            for(MediaAudit media1 : existsMediaList){
                if(!StringUtils.isEmpty(media1.getName())){
                    existsMediaMap.put(media1.getName().toLowerCase(Locale.US), media1.getId());
                }
            }
        }
        existsMediaList.clear();
        existsMediaList = null; //对象已使用完毕，对其进行释放
        //-------下拉列表值缓存-结束-------
        List<Map> validSuccessMedia = new ArrayList<>(); // 用于保存校验通过的数据；
        // 用于缓存保存成功的媒体扩展表单数据，key为临时主键,从0开始递增，后期当批量导入媒体成功后。根据返回媒体主键ID列表下标进行替换；
        Map<Integer,List<Map>> mediaExtendMap = new HashMap<>();
        List<Object[]> validErrorMedia = new ArrayList<>(); // 用于保存校验未通过的数据；
        Object [] row = null;
        for(int j = 0; j < rowNum; j++){  //遍历行数据
           row = excelContent.get(j);
           if(row.length <= 1){
               continue; //如果行数据仅有一列，则为Excel文件中说明信息，直接处理下一行数据
           }
           if(row.length != totalColumnNum){  //如果行数据与媒体模板列个数不一致，直接判断下一个
               row = Arrays.copyOf(row, totalColumnNum + 1);
               row[totalColumnNum] = "第"+(j+1)+"行导入数据模板列与媒体模板列格式不对应";
               validErrorMedia.add(row);
               continue;
           }
           Map<String,Object> currentMediaTemp = new HashMap<>();//缓存媒体数据
           List<Map> mediaExtendTemp = new ArrayList<>(); //缓存当前媒体涉及的扩展数据
           boolean isValidSuccess = true; // 校验成功标志，默认校验成功
           List<String> rowErrorMsgList = null; //记录整行的错误信息
//           boolean isPriceEmpty = true; //假设价格都为空，必须输入一个价格
            //如果是标准平台，则不需要审核，否则需要审核
            currentMediaTemp.put("standarPlatformFlag", standarPlatformFlag);
            if(standarPlatformFlag == 1){
                currentMediaTemp.put("state", 1);
            }else {
                currentMediaTemp.put("state", 0);
            }

           for(int i = 0; i < row.length; i++){
               Object columnValue = row[i]; //获取列值
               boolean requiredFlag = validField(rowTitles, i);//校验字段是否必输
               String errorInfo = null;
               if(!Objects.isNull(columnValue) && !"".equals(columnValue)){
                   if(i < mediaCommonFieldTemp.size()){ //媒体公共字段
                       errorInfo = setMediaCommonValue(existsMediaMap, currentMediaTemp, rowTitles, i, standarPlatformFlag, plateId, columnValue,mediaCommonFieldTemp); //设置媒体公共值
                   }else {
                       errorInfo = setMediaExtendMap(mediaExtendFields.get(i - commonColumnNum),columnValue,mediaExtendTemp); //媒体对于扩展字段，每个字段对应一个扩展对象
//                       //必须输入一个价格
//                       if(StringUtils.isEmpty(errorInfo) && "price".equals(mediaExtendFields.get(i - commonColumnNum).getType())){
//                           BigDecimal mediaPrice = new BigDecimal(String.valueOf(columnValue));
//                           if(mediaPrice.compareTo(new BigDecimal(0)) > 0){
//                               isPriceEmpty = false;
//                           }
//                       }
                   }
               }else{
                   if(requiredFlag){ //必输
                       errorInfo = "不能为空";
                   }
                   if(i >= mediaCommonFieldTemp.size()){  //扩展字段，为空值也记录
                       Map mediaExtend = new HashMap();
                       mediaExtend.put("cell",mediaExtendFields.get(i - commonColumnNum).getCellCode());
                       mediaExtend.put("cellName",mediaExtendFields.get(i - commonColumnNum).getCellName());
                       mediaExtend.put("type",mediaExtendFields.get(i - commonColumnNum).getType());
                       mediaExtend.put("cellValue",columnValue);
                       mediaExtendTemp.add(mediaExtend);
                   }
               }
               if(StringUtils.isNotEmpty(errorInfo)){
                   if(CollectionUtils.isEmpty(rowErrorMsgList)){
                       rowErrorMsgList = new ArrayList<>();
                   }
                   if(rowTitles.get(i).matches("^\\*.+\\*$")){ //如果是必输字段，则去除开头和结尾的*
                       rowErrorMsgList.add(rowTitles.get(i).substring(1,rowTitles.get(i).length()-1) + errorInfo);
                   }else{
                       rowErrorMsgList.add(rowTitles.get(i) + errorInfo);
                   }
                   isValidSuccess = false;
               }
           }
          /* //价格校验
           if(isPriceEmpty){
               if(CollectionUtils.isEmpty(rowErrorMsgList)){
                   rowErrorMsgList = new ArrayList<>();
               }
               rowErrorMsgList.add("媒体价格必须输入一个， 且必须大于0");
               isValidSuccess = false;
           }*/

           if(!isValidSuccess){  //数据校验不成功继续下一个媒体
               row = Arrays.copyOf(row, row.length + 1);
               row[row.length - 1] = String.valueOf(rowErrorMsgList);
               validErrorMedia.add(row); //缓存校验未通过数据
               continue;
           }

            //如果是标准板块，媒体唯一判断标准是：板块 + 唯一标识，否则：板块 + 媒体名称
            if(standarPlatformFlag != null && 1 == standarPlatformFlag){
                existsMediaMap.put(String.valueOf(currentMediaTemp.get("mediaContentId")).toLowerCase(Locale.US),0); //媒体校验成功，当成已存在的媒体，下面出现重名媒体则不会入库
            }else {
                existsMediaMap.put(String.valueOf(currentMediaTemp.get("name")),0); //媒体校验成功，当成已存在的媒体，下面出现重名媒体则不会入库
            }

           setMediaDefaultValue(currentMediaTemp, plateId, user, currentDate); //设置媒体默认值
           validSuccessMedia.add(currentMediaTemp);   //缓存校验成功媒体
           mediaExtendMap.put((validSuccessMedia.size() - 1),mediaExtendTemp); //缓存校验成功媒体的扩展数据
        }

        //先判断是否有错误信息，有提示错误，数据不入库
        int errorSize = validErrorMedia.size();
        if (errorSize > 0) {
            rowTitles.add("失败原因"); // 添加导入失败原因列；
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("templateName",plateName+"导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorMedia);
            return errorMap;
//            return DataImportUtil.createFile(plateName + "导入失败内容", config.getUploadDir(), config.getWebDir(), rowTitles, validErrorMedia);
        }

        // 处理校验成功待插入的数据；
        int size = validSuccessMedia.size();
        List<Integer> mediaIdList = new ArrayList<>();//新增的媒体ID
        if (size > 0) {
            int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
            int insertTimes = size % subLength == 0 ? size / subLength : size / subLength + 1; // 计算需要插入的次数，100条插入一次；
            if (insertTimes > 1) {
                List<Map> insertData;
                for (int i = 0; i < insertTimes; i++) {
                    insertData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < size; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                        insertData.add(validSuccessMedia.get(j));
                    }
                    mediaIdList.addAll(saveBatchMedia(standarPlatformFlag, mediaExtendMap,insertData,currentDate,true,i,subLength));
                }
            } else {
                mediaIdList.addAll(saveBatchMedia(standarPlatformFlag, mediaExtendMap,validSuccessMedia,currentDate,false,0,subLength));
            }
        }

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            //媒体新增成功，才能进行媒体异动 和 供应商异动的操作
            if(CollectionUtils.isNotEmpty(mediaIdList)){
                List<Media1> newMediaList = mediaAuditMapper.listMediaDetailByIds(mediaIdList);
                List<MediaChange> mediaChangeList = mediaChangeHandler(newMediaList, null, user);
                if(CollectionUtils.isNotEmpty(mediaChangeList)){
                    mediaChangeMapper.saveBatch(mediaChangeList);
                }
            }
        }catch (Exception e){
            log.error("【媒体登记】媒体异动记录异常: {}", e.getMessage());
        }

       return null;
    }

    /**
     * 处理媒体导入逻辑
     * @param user 当前登入用户
     * @param plateId 媒体板块ID
     * @param plateName 媒体板块名称
     * @param mediaSupplierPriceFiledNames 媒体供应商导入模板列对应列名列表,
     * @param mediaSupplierPriceExtendFields 媒体供应商导入模板列对应属性值列表
     * @param excelContent 媒体导入文件内容
     */
    private Map<String, Object> handleSupplierPriceData(User user,Integer standarPlatformFlag, Integer plateId, String plateName, List<String> mediaSupplierPriceFiledNames, List<MediaForm1> mediaSupplierPriceExtendFields, List<Object[]> excelContent){
        List<String> rowTitles = new ArrayList<>(); //导入模板总列数
        List<String> supplierCommonField = new ArrayList<>();
        String primaryKey = "";
        //标准平台通过唯一标识
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            rowTitles.addAll(standarMediaSupplierCommonFieldName);
            rowTitles.addAll(mediaSupplierPriceFiledNames);
            supplierCommonField = standarMediaSupplierCommonField;
            primaryKey = "mediaContentId";

        }else {
            rowTitles.addAll(mediaSupplierCommonFieldName);
            rowTitles.addAll(mediaSupplierPriceFiledNames);
            supplierCommonField = mediaSupplierCommonField;
            primaryKey = "name";
        }

        int totalColumnNum = rowTitles.size();//总列数
        int rowNum = excelContent.size(); //导入媒体行数
        int commonColumnNum = supplierCommonField.size();//公共列数
        //-------下拉列表值缓存-开始-------
        supplierNameMap = null; //获取当前板块供应商信息集合
        otherFieldMap = new HashMap<>(); // 其他复选框、下拉列表、单选框。

        //获取媒体
        Map param = new HashMap();
        param.put("plateId",plateId);
        param.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> existsMediaList = mediaAuditMapper.listMediaByParam(param);
        Map<String,MediaAudit> existsMediaMap = new HashMap<>(); //获取当前媒体板块下有用的媒体，用于判断名称是否存在,key = name
        //如果是标准板块，媒体唯一判断标准是：板块 + 唯一标识，否则：板块 + 媒体名称
        if(standarPlatformFlag != null && 1 == standarPlatformFlag){
            for(MediaAudit media1 : existsMediaList){
                if(!StringUtils.isEmpty(media1.getMediaContentId())){
                    existsMediaMap.put(media1.getMediaContentId().toLowerCase(Locale.US), media1);
                }
            }
        }else {
            for(MediaAudit media1 : existsMediaList){
                if(!StringUtils.isEmpty(media1.getName())){
                    existsMediaMap.put(media1.getName().toLowerCase(Locale.US), media1);
                }
            }
        }
        existsMediaList.clear();
        existsMediaList = null; //对象已使用完毕，对其进行释放
        //获取媒体价格(用于和导入供应商比较价格)
        /*List<MediaPrice> existsMediaPriceList = mediaAuditMapper.listMediaPriceByParam(param);
        Map<String,BigDecimal> existsMediaPriceMap = new HashMap<>(); //key = mediaId + - +cell, value = cellValue
        for(MediaPrice mediaPrice : existsMediaPriceList){
            if(CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
                for(MediaPriceCell mediaPriceCell : mediaPrice.getMediaPriceCellList()){
                    existsMediaPriceMap.put(mediaPrice.getMediaId()+"-"+mediaPriceCell.getCell(),new BigDecimal(StringUtils.isNotEmpty(mediaPriceCell.getCellValue()) ? mediaPriceCell.getCellValue() : "0"));
                }
            }
        }
        existsMediaPriceList.clear();
        existsMediaPriceList = null; //对象已使用完毕，对其进行释放*/

        //获取媒体供应商字段
        List<MediaPrice> existsMediaSupplierPriceList = mediaAuditMapper.listMediaSupplierPriceByParam(param);
        Map<String,MediaPriceCell> existsMediaSupplierFieldMap = new HashMap<>(); //key = mediaId + - + supplierId + - +cell, value = relateId，判断供应商字段是否存在
        Map<String, MediaPriceCell> existsMediaSupplierRelateMap = new HashMap<>(); //key = mediaId + - + supplierId , value = relateId 判断供应商媒体关系是否存在
//        Map<Integer, Map<String, BigDecimal>> oldMeidaSupplierPriceMap = new HashMap<>();//原媒体对应供应商价格，用于后面计算媒体最小价格
        for(MediaPrice mediaPrice : existsMediaSupplierPriceList){
            if(CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
//                Map<String, BigDecimal> supplierPriceMap = new HashMap<>();
                for(MediaPriceCell mediaPriceCell : mediaPrice.getMediaPriceCellList()){
                    if(StringUtils.isNotEmpty(mediaPriceCell.getCell())){
                        existsMediaSupplierFieldMap.put(mediaPriceCell.getSupplierId()+"-"+mediaPrice.getMediaId()+"-"+mediaPriceCell.getCell(),mediaPriceCell);
//                        if("price".equals(mediaPriceCell.getCellType())){
//                            supplierPriceMap.put(mediaPriceCell.getSupplierId()+"-"+mediaPrice.getMediaId()+"-"+mediaPriceCell.getCell(), new BigDecimal(mediaPriceCell.getCellValue()));
//                        }
                    }
                    existsMediaSupplierRelateMap.put(mediaPrice.getMediaId() + "-" + mediaPriceCell.getSupplierId(), mediaPriceCell);
                }
//                oldMeidaSupplierPriceMap.put(mediaPrice.getMediaId(),supplierPriceMap);//缓存媒体对应老的供应商价格
            }
        }
        existsMediaSupplierPriceList.clear();
        existsMediaSupplierPriceList = null; //对象已使用完毕，对其进行释放

        //-------下拉列表值缓存-结束-------

        List<Object[]> validErrorMedia = new ArrayList<>(); // 用于保存校验未通过的数据；
        Map<String, BigDecimal> mediaMinPrice = new HashMap<>(); //计算最小的媒体价格(不统计为空和0的情况)，//key = mediaId + - +cell, value = cellValue
//        List<MediaExtendAudit> addMediaExtendPriceList = new ArrayList<>(); //添加媒体扩展价格
//        List<MediaExtendAudit> editMediaExtendPriceList = new ArrayList<>(); //编辑媒体扩展价格
        List<MediaSupplierRelateAudit>  addMediaSupplierRelateList = new ArrayList<>(); //新增的媒体供应商关系
        List<String> existsAddMediaSupplierRelateList = new ArrayList<>(); //新增媒体供应商关系，用于判断是否已有，防止重复添加
        Map<String,MediaSupplierPriceAudit> addMediaSupplierPriceMap = new HashMap<>(); //新增的媒体供应商价格（媒体供应商关系存在）,如果存在两条相同媒体供应商价格，则后面覆盖前面
        Map<String, MediaSupplierPriceAudit> editMediaSupplierPriceMap = new HashMap<>(); //修改的媒体供应商价格,如果存在两条相同媒体供应商价格，则后面覆盖前面
        Map<String, List<MediaSupplierPriceAudit>> mediaSupplierPriceMap = new HashMap<>();//缓存新增的媒体供应商关系对应的供应商价格列表
        List<Integer> relateIds = new ArrayList<>(); //缓存修改供应商价格的关系ID，设置关系需要进行审核
        List<Integer> addRelateIds = new ArrayList<>(); //缓存修改供应商价格的关系ID，设置关系需要进行审核
        List<MediaAudit> mediaAuditList = new ArrayList<>();//缓存匹配的媒体
//        Set<Integer> mediaIdSet = new HashSet<>(); //缓存当前导入价格中媒体ID

        Object [] row = null;
        for(int j = 0; j < rowNum; j++){  //遍历行数据
            row = excelContent.get(j);
            if(row.length <= 1){
                continue; //如果行数据仅有一列，则为Excel文件中说明信息，直接处理下一行数据
            }
            if(row.length != totalColumnNum){  //如果行数据与媒体模板列个数不一致，直接判断下一个
                row = Arrays.copyOf(row, totalColumnNum + 1);
                row[totalColumnNum] = "第"+(j+1)+"行导入数据模板列与媒体模板列格式不对应";
                validErrorMedia.add(row);
                continue;
            }
            Map<String,Object> currentMediaSupplierTemp = new HashMap<>();//缓存媒体Id和供应商Id
            List<MediaSupplierPriceAudit> currentMediaSupplierPriceList = new ArrayList<>(); //当前的媒体供应商价格（媒体供应商关系不存在）,如果存在两条相同媒体供应商价格，则后面覆盖前面
            boolean isValidSuccess = true; // 校验成功标志，默认校验成功
            boolean isPriceEmpty = true; //假设价格都为空，必须输入一个价格
            List<String> rowErrorMsgList = null; //记录行错误信息
            for(int i = 0; i < row.length; i++){
                Object columnValue = row[i];
                //获取列值
                boolean requiredFlag = validField(rowTitles, i);//校验字段是否必输
                if((columnValue == null || "".equals(columnValue)) && i >= supplierCommonField.size() && !requiredFlag){ //如果是价格字段，并且是非必输的字段，则不填默认为0
                    columnValue = 0;
                }
                String errorInfo = null;
                if(!Objects.isNull(columnValue) && !"".equals(columnValue)){
                    if(i < commonColumnNum){ //媒体公共字段
                        if(primaryKey.equals(supplierCommonField.get(i))){
                            columnValue = existsMediaMap.get(String.valueOf(columnValue).toLowerCase(Locale.US));
                            if(columnValue == null){
                                errorInfo = "不存在";
                            }else {
                                MediaAudit tmp = (MediaAudit)columnValue;
                               /* if(!user.getId().equals(tmp.getUserId())){
                                    errorInfo = "所属媒体责任人不是您，您不可以操作媒体供应商价格";
                                }*/
                                mediaAuditList.add(tmp);
                                currentMediaSupplierTemp.put("mediaId",tmp.getId());
                            }
                        }else if("phone".equals(supplierCommonField.get(i))){
                            currentMediaSupplierTemp.put(supplierCommonField.get(i),columnValue);
                            supplierNameMap = getSupplierNameMap();
                            //供应商联系人先保存在supplierId字段中
                            currentMediaSupplierTemp.put("supplierName",currentMediaSupplierTemp.get("supplierId"));
                            if(supplierNameMap != null && supplierNameMap.size() > 0){
                                //判断公司名称是否有值
                                if(currentMediaSupplierTemp.get("supplierCompany") != null && StringUtils.isNotEmpty(String.valueOf(currentMediaSupplierTemp.get("supplierCompany")))){
                                    columnValue = supplierNameMap.get(currentMediaSupplierTemp.get("supplierCompany")+"*"+columnValue);
                                    if(columnValue == null){
                                        errorInfo = "和供应商公司名称不存在或者供应商不规范";
                                    }else {
                                        currentMediaSupplierTemp.put("supplierId",columnValue);//保存供应商主键ID
                                    }
                                }else {
                                    errorInfo = "和供应商公司名称不存在";
                                }
                            }else {
                                errorInfo = "和供应商公司名称不存在";
                            }
                        }else{
                            currentMediaSupplierTemp.put(supplierCommonField.get(i),columnValue);
                        }
                    }else {
                        MediaForm1 extendField = mediaSupplierPriceExtendFields.get(i - commonColumnNum);
                        //校验金额格式
                        if("price".equals(extendField.getType()) && !String.valueOf(columnValue).matches("^(\\d+)(\\.\\d+)?$")){
                            errorInfo = "格式错误";
                        }else{
                            MediaSupplierPriceAudit mediaSupplierPriceAudit = new MediaSupplierPriceAudit();
                            mediaSupplierPriceAudit.setCell(extendField.getCellCode());
                            mediaSupplierPriceAudit.setCellName(extendField.getCellName());
                            mediaSupplierPriceAudit.setCellType(extendField.getType());
                            if("price".equals(extendField.getType())){
                                BigDecimal supplierPrice = new BigDecimal(String.valueOf(columnValue));
                                mediaSupplierPriceAudit.setCellValue(supplierPrice.toPlainString());
                                if(supplierPrice.compareTo(new BigDecimal(0)) > 0){
                                    isPriceEmpty = false;
                                }
                            }else {
                                if("select".equals(extendField.getType()) || "radio".equals(extendField.getType()) || "checkbox".equals(extendField.getType())){
                                    setOtherFieldMap(extendField); //设置下拉列表、单选框、复选框值列表
                                    //赋值
                                    Map<String,String> currentFieldMap = otherFieldMap.get(extendField.getCellCode());
                                    String columnValueText = null;
                                    if(currentFieldMap != null && currentFieldMap.size() > 0){  //如果存在列表值,说明页面限制了，则获取，否则没限制，随便用户输入
                                        columnValueText = String.valueOf(columnValue); //单选框、复选框、下拉列表中文描述值
                                        if("checkbox".equals(extendField.getType()) && columnValueText.indexOf(",") != -1){ //如果是复选框，并且有多个值
                                            String [] columnTexts = columnValueText.split(",");
                                            List<String> columnValueTemp = new ArrayList<>();
                                            for(String text : columnTexts){
                                                columnValueTemp.add(currentFieldMap.get(text));
                                            }
                                            columnValue = StringUtils.join(columnValueTemp,",");
                                        }else{
                                            columnValue = currentFieldMap.get(columnValue);
                                        }
                                    }

                                    if(columnValue == null && currentFieldMap.size() > 0){
                                        errorInfo = "值错误";
                                    }else{
                                        mediaSupplierPriceAudit.setCellValue(String.valueOf(columnValue));
                                        mediaSupplierPriceAudit.setCellValueText(columnValueText);
                                    }
                                }else{
                                    mediaSupplierPriceAudit.setCellValue(String.valueOf(columnValue));
                                }
                            }
                            //首先判断媒体供应商关系是否存在，存在则判断是否供应商是否存在这个字段，存在为编辑，否则为新增
                            if(existsMediaSupplierRelateMap.get(currentMediaSupplierTemp.get("mediaId")+"-"+currentMediaSupplierTemp.get("supplierId")) != null){
                                mediaSupplierPriceAudit.setMediaSupplierRelateId(existsMediaSupplierRelateMap.get(currentMediaSupplierTemp.get("mediaId") + "-" + currentMediaSupplierTemp.get("supplierId")).getRelateId());
                                if(existsMediaSupplierFieldMap.get(currentMediaSupplierTemp.get("supplierId")+"-"+currentMediaSupplierTemp.get("mediaId")+"-"+extendField.getCellCode()) != null){
                                    mediaSupplierPriceAudit.setEditAddStatus(false);
//                                editMediaSupplierPriceMap.put(currentMediaSupplierTemp.get("mediaId")+"-"+currentMediaSupplierTemp.get("supplierId")+"-"+extendField.getCellCode(),mediaSupplierPriceAudit);
                                }else {
                                    mediaSupplierPriceAudit.setEditAddStatus(true);
//                                addMediaSupplierPriceMap.put(currentMediaSupplierTemp.get("mediaId")+"-"+currentMediaSupplierTemp.get("supplierId")+"-"+extendField.getCellCode(),mediaSupplierPriceAudit);
                                }
                            }
                            currentMediaSupplierPriceList.add(mediaSupplierPriceAudit); //整行记录校验成功后，进行存储
                        }
                    }
                }else{
                    if(requiredFlag){ //必输
                        errorInfo = "不能为空";
                    }
                }

                if(StringUtils.isNotEmpty(errorInfo)){
                    if(CollectionUtils.isEmpty(rowErrorMsgList)){
                        rowErrorMsgList = new ArrayList<>();
                    }
                    if(rowTitles.get(i).matches("^\\*.+\\*$")){ //如果是必输字段，则去除开头和结尾的*
                        rowErrorMsgList.add(rowTitles.get(i).substring(1,rowTitles.get(i).length()-1) + errorInfo);
                    }else{
                        rowErrorMsgList.add(rowTitles.get(i) + errorInfo);
                    }
                    isValidSuccess = false;
                }
            }

            //校验是否可以修改，媒体供应商关系修改，只能由绑定人可修改
            if (existsMediaSupplierRelateMap.get(currentMediaSupplierTemp.get("mediaId") + "-" + currentMediaSupplierTemp.get("supplierId")) != null) {
                if (!user.getId().equals(existsMediaSupplierRelateMap.get(currentMediaSupplierTemp.get("mediaId") + "-" + currentMediaSupplierTemp.get("supplierId")).getSupplierCreator())) {
                    if (CollectionUtils.isEmpty(rowErrorMsgList)) {
                        rowErrorMsgList = new ArrayList<>();
                    }
                    rowErrorMsgList.add("媒体与该供应商的关系绑定人不是您，您不可以操作媒体供应商价格");
                    isValidSuccess = false;
                }
            }

            if(isPriceEmpty){ //如果价格都没输入或者都为0则默认错误
                if(CollectionUtils.isEmpty(rowErrorMsgList)){
                    rowErrorMsgList = new ArrayList<>();
                }
                rowErrorMsgList.add("必须输入一个有效价格");
                isValidSuccess = false;
            }
            if(!isValidSuccess){  //数据校验不成功继续下一个媒体
                row = Arrays.copyOf(row, row.length + 1);
                row[row.length - 1] = String.valueOf(rowErrorMsgList);
                validErrorMedia.add(row); //缓存校验未通过数据
                continue;
            }
            //如果没有关系则为新增的媒体供应商关系， 并且之前没有放到该list列表中，这里处理每行数据，存在相同的会进行覆盖
            if(existsMediaSupplierRelateMap.get(currentMediaSupplierTemp.get("mediaId")+"-"+currentMediaSupplierTemp.get("supplierId")) == null){
                //如果不存在，则新增
                if(!existsAddMediaSupplierRelateList.contains(currentMediaSupplierTemp.get("mediaId")+"-"+currentMediaSupplierTemp.get("supplierId"))){
                    MediaSupplierRelateAudit mediaSupplierRelateAudit = new MediaSupplierRelateAudit();
                    mediaSupplierRelateAudit.setSupplierId(Integer.valueOf(String.valueOf(currentMediaSupplierTemp.get("supplierId"))));
                    mediaSupplierRelateAudit.setMediaId(Integer.valueOf(String.valueOf(currentMediaSupplierTemp.get("mediaId"))));
                    mediaSupplierRelateAudit.setCreateId(user.getId());
                    mediaSupplierRelateAudit.setUpdateId(user.getId());
                    mediaSupplierRelateAudit.setIsCopy(0); //0-自建
                    mediaSupplierRelateAudit.setEnabled(0); //0-启用

                    //如果是标准平台，不需要审核
                    if(standarPlatformFlag == 1){
                        mediaSupplierRelateAudit.setState(1);
                    }else {
                        mediaSupplierRelateAudit.setState(0);
                    }

                    existsAddMediaSupplierRelateList.add(mediaSupplierRelateAudit.getMediaId()+"-"+mediaSupplierRelateAudit.getSupplierId()); //当没有关系是新增，用于判断是否重复
                    addMediaSupplierRelateList.add(mediaSupplierRelateAudit); //缓存新增的供应商媒体关系
                }
                //无论是否存在，都替换价格，缓存新增供应商媒体关系价格，后面媒体供应商关系入库后，需要设置关系ID
                if(CollectionUtils.isNotEmpty(currentMediaSupplierPriceList)){
                    mediaSupplierPriceMap.put(currentMediaSupplierTemp.get("mediaId")+"-"+currentMediaSupplierTemp.get("supplierId"), currentMediaSupplierPriceList);
                }
            }else{ //有关系则判断是否是新增的还是修改的
                for(MediaSupplierPriceAudit mediaSupplierPriceAudit : currentMediaSupplierPriceList){
                    if(!relateIds.contains(mediaSupplierPriceAudit.getMediaSupplierRelateId())){
                        relateIds.add(mediaSupplierPriceAudit.getMediaSupplierRelateId()); //设置需要审核的关系集合
                    }
                    if(mediaSupplierPriceAudit.isEditAddStatus()){
                        addMediaSupplierPriceMap.put(currentMediaSupplierTemp.get("supplierId")+"-"+currentMediaSupplierTemp.get("mediaId")+"-"+mediaSupplierPriceAudit.getCell(),mediaSupplierPriceAudit);
                    }else{
                        editMediaSupplierPriceMap.put(currentMediaSupplierTemp.get("supplierId")+"-"+currentMediaSupplierTemp.get("mediaId")+"-"+mediaSupplierPriceAudit.getCell(),mediaSupplierPriceAudit);
                    }
                }
            }

//            mediaIdSet.add(Integer.valueOf(String.valueOf(currentMediaSupplierTemp.get("mediaId")))); //缓存媒体ID，用于更新媒体价格扩展
        }

        //Excel读取完毕，开始数据处理
        // 先判断是否有错误信息，有提示错误，数据不入库
        int errorSize = validErrorMedia.size();
        if (errorSize > 0) {
            rowTitles.add("失败原因"); // 添加导入失败原因列；
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("templateName",plateName+"导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorMedia);
            return errorMap;
//            return DataImportUtil.createFile(plateName + "导入失败内容", config.getUploadDir(), config.getWebDir(), rowTitles, validErrorMedia);
        }

        //媒体所有供应商价格（Excel 和 数据库），用于计算Excel中媒体最低价格，先保存数据库媒体供应商价格，在保存Excel中有效媒体供应商价格，修改的供应商价格会进行覆盖
       /* Map<String, BigDecimal> mediaAllSupplierPriceMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(mediaIdSet)){
            for(Integer mediaId : mediaIdSet){
                Map<String, BigDecimal> oldSupplierPrice = oldMeidaSupplierPriceMap.get(mediaId);
                if(oldSupplierPrice != null && oldSupplierPrice.size() > 0){
                    for(String key : oldSupplierPrice.keySet()){
                        mediaAllSupplierPriceMap.put(key,oldSupplierPrice.get(key)); //缓存Excel供应商有效价格
                    }
                }
            }
        }*/

        List<MediaSupplierPriceAudit> addMediaSupplierPriceList = new ArrayList<>();
        if(addMediaSupplierPriceMap.size() > 0){
            for(String key : addMediaSupplierPriceMap.keySet()){
                addMediaSupplierPriceList.add(addMediaSupplierPriceMap.get(key));//汇总所有新增字段
//                if("price".equals(addMediaSupplierPriceMap.get(key).getCellType())){
//                    mediaAllSupplierPriceMap.put(key,new BigDecimal(addMediaSupplierPriceMap.get(key).getCellValue())); //缓存Excel供应商有效价格
//                }
            }
        }
        List<MediaSupplierPriceAudit> editMediaSupplierPriceList = new ArrayList<>();
        if(editMediaSupplierPriceMap.size() > 0){
            for(String key : editMediaSupplierPriceMap.keySet()){
                editMediaSupplierPriceList.add(editMediaSupplierPriceMap.get(key));//汇总所有修改字段
//                if("price".equals(editMediaSupplierPriceMap.get(key).getCellType())){
//                    mediaAllSupplierPriceMap.put(key, new BigDecimal(editMediaSupplierPriceMap.get(key).getCellValue())); //缓存Excel供应商有效价格
//                }
            }
        }

        //保存媒体新增供应商
        int mediaSupplierRelateSize = addMediaSupplierRelateList.size();
        if(mediaSupplierRelateSize > 0){
            int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
            int insertTimes = mediaSupplierRelateSize % subLength == 0 ? mediaSupplierRelateSize / subLength : mediaSupplierRelateSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
            if (insertTimes > 1) {
                List<MediaSupplierRelateAudit> insertData;
                for (int i = 0; i < insertTimes; i++) {
                    insertData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < mediaSupplierRelateSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                        insertData.add(addMediaSupplierRelateList.get(j));
                    }
                    mediaSupplierRelateAuditMapper.saveBatch(insertData);
                    for(MediaSupplierRelateAudit mediaSupplierRelateAudit : insertData){
                        addRelateIds.add(mediaSupplierRelateAudit.getId());//保存新增的供应商关系ID
                        for(MediaSupplierPriceAudit mediaSupplierPriceAudit : mediaSupplierPriceMap.get(mediaSupplierRelateAudit.getMediaId()+"-"+mediaSupplierRelateAudit.getSupplierId())){
                            mediaSupplierPriceAudit.setMediaSupplierRelateId(mediaSupplierRelateAudit.getId());
                            if("price".equals(mediaSupplierPriceAudit.getCellType())){
//                                mediaAllSupplierPriceMap.put(mediaSupplierRelateAudit.getSupplierId()+"-"+mediaSupplierRelateAudit.getMediaId()+"-"+mediaSupplierPriceAudit.getCell(), new BigDecimal(mediaSupplierPriceAudit.getCellValue()));//记录Excel有效供应商价格
                            }
                        }
                        addMediaSupplierPriceList.addAll(mediaSupplierPriceMap.get(mediaSupplierRelateAudit.getMediaId()+"-"+mediaSupplierRelateAudit.getSupplierId()));
                    }
                }
            } else {
                mediaSupplierRelateAuditMapper.saveBatch(addMediaSupplierRelateList);
                for(MediaSupplierRelateAudit mediaSupplierRelateAudit : addMediaSupplierRelateList){
                    addRelateIds.add(mediaSupplierRelateAudit.getId());//保存新增的供应商关系ID
                    for(MediaSupplierPriceAudit mediaSupplierPriceAudit : mediaSupplierPriceMap.get(mediaSupplierRelateAudit.getMediaId()+"-"+mediaSupplierRelateAudit.getSupplierId())){
                        mediaSupplierPriceAudit.setMediaSupplierRelateId(mediaSupplierRelateAudit.getId());
                        if("price".equals(mediaSupplierPriceAudit.getCellType())){
//                            mediaAllSupplierPriceMap.put(mediaSupplierRelateAudit.getSupplierId()+"-"+mediaSupplierRelateAudit.getMediaId()+"-"+mediaSupplierPriceAudit.getCell(), new BigDecimal(mediaSupplierPriceAudit.getCellValue()));//记录Excel有效供应商价格
                        }
                    }
                    addMediaSupplierPriceList.addAll(mediaSupplierPriceMap.get(mediaSupplierRelateAudit.getMediaId()+"-"+mediaSupplierRelateAudit.getSupplierId()));
                }
            }
        }
        addMediaSupplierRelateList.clear();
        addMediaSupplierRelateList = null; //对象已使用完毕，对其进行释放

        //处理异动前供应商关系
        List<MediaSupplierRelate> oldMediaRelateList = null;
        if(CollectionUtils.isNotEmpty(relateIds)){
            oldMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);
        }

        //保存媒体新增的供应商字段
        int addMediaSupplierPriceSize = addMediaSupplierPriceList.size();
        if(addMediaSupplierPriceSize > 0){
            int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
            int insertTimes = addMediaSupplierPriceSize % subLength == 0 ? addMediaSupplierPriceSize / subLength : addMediaSupplierPriceSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
            if (insertTimes > 1) {
                List<MediaSupplierPriceAudit> insertData;
                for (int i = 0; i < insertTimes; i++) {
                    insertData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < addMediaSupplierPriceSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                        insertData.add(addMediaSupplierPriceList.get(j));
                    }
                   mediaSupplierPriceAuditMapper.saveBatch(insertData);
                }
            } else {
                mediaSupplierPriceAuditMapper.saveBatch(addMediaSupplierPriceList);
            }
        }
        addMediaSupplierPriceList.clear();
        addMediaSupplierPriceList = null; //对象已使用完毕，对其进行释放

        //保存媒体编辑的供应商字段
        int editMediaSupplierPriceSize = editMediaSupplierPriceList.size();
        if(editMediaSupplierPriceSize > 0){
            int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
            int insertTimes = editMediaSupplierPriceSize % subLength == 0 ? editMediaSupplierPriceSize / subLength : editMediaSupplierPriceSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
            if (insertTimes > 1) {
                List<MediaSupplierPriceAudit> updateData;
                for (int i = 0; i < insertTimes; i++) {
                    updateData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < editMediaSupplierPriceSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                        updateData.add(editMediaSupplierPriceList.get(j));
                    }
                    mediaSupplierPriceAuditMapper.updateBatch(updateData);
                }
            } else {
                mediaSupplierPriceAuditMapper.updateBatch(editMediaSupplierPriceList);
            }
        }
        editMediaSupplierPriceList.clear();
        editMediaSupplierPriceList = null; //对象已使用完毕，对其进行释放

        //计算Excel中所有媒体对应的最低价格，最后和原媒体最低价格比较，决定媒体最低价格
        /*if(mediaAllSupplierPriceMap.size() > 0){
            for(String key : mediaAllSupplierPriceMap.keySet()){
                String [] keys = key.split("-");
                String mediaId = keys[1];
                String cell = keys[2];
                //计算Excel中媒体对应类型的最低价格，如果价格小于等于0， 不考虑，由于供应商的价格存在覆盖，所以这计算Excel最低价格
                if(mediaAllSupplierPriceMap.get(key).compareTo(new BigDecimal(0)) > 0){
                    //如果Excel中，当前媒体对应类型价格为空，则设置当前类型为最低价格
                    if(mediaMinPrice.get(mediaId+"-"+cell) == null ||
                            mediaMinPrice.get(mediaId+"-"+cell).compareTo(mediaAllSupplierPriceMap.get(key)) > 0){
                        mediaMinPrice.put(mediaId+"-"+cell,mediaAllSupplierPriceMap.get(key));
                    }
                }
            }
        }*/

        //处理媒体最低价格
        /*if(CollectionUtils.isNotEmpty(mediaIdSet)){
            for(Integer mediaId : mediaIdSet){
                for(MediaForm1 mediaForm1 : mediaSupplierPriceExtendFields){
                    if(mediaForm1.getExtendFlag() == 1){ //排除仅供应商使用的字段
                        continue;
                    }
                    MediaExtendAudit mediaExtendAudit = new MediaExtendAudit();
                    mediaExtendAudit.setMediaId(mediaId);
                    mediaExtendAudit.setCell(mediaForm1.getCellCode());
                    mediaExtendAudit.setCellName(mediaForm1.getCellName());
                    mediaExtendAudit.setDbType(mediaForm1.getType());
                    mediaExtendAudit.setType(mediaForm1.getType());
                    //如果当前媒体的Excel中对应价格最小值为空，则赋值为0，因为上面计算Excel最小值时，没使用0的情况，这是是为了防止出现0之后，后面计算最小值失效，所以在此处设置
                    if(mediaMinPrice.get(mediaId+"-"+mediaForm1.getCellCode()) == null){
                        mediaMinPrice.put(mediaId+"-"+mediaForm1.getCellCode(), new BigDecimal(0));
                    }
                    mediaExtendAudit.setCellValue(String.valueOf(mediaMinPrice.get(mediaId+"-"+mediaForm1.getCellCode())));
                    //如果媒体原来有价格，则为修改
                    if(existsMediaPriceMap.get(mediaId+"-"+mediaForm1.getCellCode()) != null){
                        editMediaExtendPriceList.add(mediaExtendAudit);
                    }else{  //如果媒体原来没有价格，则为新增价格
                        addMediaExtendPriceList.add(mediaExtendAudit);
                    }
                }
            }
        }*/

        //通过导入供应商价格，都设置媒体需要重新审核
       /* if(CollectionUtils.isNotEmpty(mediaIdSet)){
            List<Integer> mediaIds = new ArrayList<>();
            mediaIds.addAll(mediaIdSet);
            mediaAuditMapper.batchUpdateMediaState(mediaIds,0,user.getId());
            media1Mapper.batchUpdateMediaIsDelete(mediaIds, 1,user.getId()); //审核时，设置媒体删除状态为1，方便驳回时恢复成原来数据
        }*/
        //通过导入供应商价格，都设置媒体关系需要重新审核
        //如果是标准平台，不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            List<Integer> tmpRelateIds = new ArrayList<>();
            tmpRelateIds.addAll(addRelateIds);
            tmpRelateIds.addAll(relateIds);
            copyMediaRelateAudit2MediaRelate(tmpRelateIds);//拷贝媒体供应商关系
        }else {
            if(CollectionUtils.isNotEmpty(relateIds)){
                Map<String, Object> map = new HashMap<>();
                map.put("updateId", user.getId());
                map.put("state", 0);
                map.put("ids", relateIds);
                mediaSupplierRelateAuditMapper.updateStateByIds(map); //将状态设置为0-待审核
                //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
                mediaSupplierRelateMapper.batchUpdateMediaRelateIsDelete(1, user.getId(),relateIds);
            }
        }

        // 保存媒体新增扩展价格
        /*int addMediaExtendSize = addMediaExtendPriceList.size();
        if(addMediaExtendSize > 0){
            int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
            int insertTimes = addMediaExtendSize % subLength == 0 ? addMediaExtendSize / subLength : addMediaExtendSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
            if (insertTimes > 1) {
                List<MediaExtendAudit> insertData;
                for (int i = 0; i < insertTimes; i++) {
                    insertData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < addMediaExtendSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                        insertData.add(addMediaExtendPriceList.get(j));
                    }
                    mediaExtendAuditMapper.saveBatch(insertData);
                }
            } else {
                mediaExtendAuditMapper.saveBatch(addMediaExtendPriceList);
            }
        }
        addMediaExtendPriceList.clear();
        addMediaExtendPriceList = null; //对象已使用完毕，对其进行释放*/

        // 保存媒体编辑扩展价格
        /*int editMediaExtendSize = editMediaExtendPriceList.size();
        if(editMediaExtendSize > 0){
            int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
            int insertTimes = editMediaExtendSize % subLength == 0 ? editMediaExtendSize / subLength : editMediaExtendSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
            if (insertTimes > 1) {
                List<MediaExtendAudit> updateData;
                for (int i = 0; i < insertTimes; i++) {
                    updateData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < editMediaExtendSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                        updateData.add(editMediaExtendPriceList.get(j));
                    }
                    mediaExtendAuditMapper.updateBatchMoreMedia(updateData);
                }
            } else {
                mediaExtendAuditMapper.updateBatchMoreMedia(editMediaExtendPriceList);
            }
        }
        editMediaExtendPriceList.clear();
        editMediaExtendPriceList = null; //对象已使用完毕，对其进行释放*/

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            List<MediaSupplierChange> mediaSupplierChangeList = new ArrayList<>();
            //如果有编辑媒体供应商关系
            if(CollectionUtils.isNotEmpty(relateIds)){
                List<MediaSupplierRelate> newMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);
                mediaSupplierChangeList.addAll(mediaSupplierChangeHandler(mediaAuditList, newMediaRelateList, oldMediaRelateList, user));
            }
            //如果有新增媒体供应商关系
            if(CollectionUtils.isNotEmpty(addRelateIds)){
                List<MediaSupplierRelate> newMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(addRelateIds);
                mediaSupplierChangeList.addAll(mediaSupplierChangeHandler(mediaAuditList, newMediaRelateList, null, user));
            }
            if(CollectionUtils.isNotEmpty(mediaSupplierChangeList)){
                mediaSupplierChangeMapper.saveBatch(mediaSupplierChangeList);
            }
        }catch (Exception e){
            log.error("【媒体关系导入】媒体关系异动记录异常: {}", e.getMessage());
        }

        return null;
    }
    /**
     * 判断指定列值是否必输（根据**判断）
     * @param rowTitles 所有列名称
     * @param columnIndex 列索引
     */
    private boolean validField(List<String> rowTitles, int columnIndex){
        if(rowTitles.get(columnIndex).matches("^\\*.+\\*$")){
            return true;
        }
        return false;
    }

    /**
     * 设置公共字段值
     * @param existsMediaMap 当前板块下存在的媒体，用于校验名称重复性
     * @param currentMediaTemp 缓存的当前媒体对象
     * @param rowTitles 导入模板列标题
     * @param columnIndex 当前列下标
     * @param plateId 媒体板块类型
     * @param columnValue 当前列值
     */
    private String setMediaCommonValue(Map<String,Integer> existsMediaMap,Map<String,Object> currentMediaTemp, List<String> rowTitles, int columnIndex, int standarPlatformFlag, int plateId, Object columnValue, List<String> mediaCommonFieldTemp){
        String flag = null;

        //如果是标准板块，媒体唯一判断标准是：板块 + 唯一标识，否则：板块 + 媒体名称
        if(standarPlatformFlag == 1){
//            currentMediaTemp.put("name", "");//标准板块不填写媒体名称
            if("mediaContentId".equals(mediaCommonFieldTemp.get(columnIndex))){
                if(existsMediaMap.get(String.valueOf(columnValue).toLowerCase(Locale.US)) != null){
                    return "已存在";
                }
            }
        }else {
            if("name".equals(mediaCommonFieldTemp.get(columnIndex))){
                if(existsMediaMap.get(String.valueOf(columnValue).toLowerCase(Locale.US)) != null){
                    return "已存在";
                }
            }
        }
    /*    if("userId".equals(mediaCommonFieldTemp.get(columnIndex))){
            userNameMap = getUserNameMap(plateId);
            columnValue = userNameMap.get(columnValue);
            if(userNameMap != null && userNameMap.size() > 0){
                if(columnValue == null){
                    flag = "在本公司不存在";
                }else{
                    currentMediaTemp.put(mediaCommonFieldTemp.get(columnIndex),columnValue);
                }
            } else{
                flag = "在本公司不存在";
            }
        } */
        if("phone".equals(mediaCommonFieldTemp.get(columnIndex))){
            currentMediaTemp.put(mediaCommonFieldTemp.get(columnIndex),columnValue);
            supplierNameMap = getSupplierNameMap();
            //供应商联系人先保存在supplierId字段中
            currentMediaTemp.put("supplierName",currentMediaTemp.get("supplierId"));
            if(supplierNameMap != null && supplierNameMap.size() > 0){
                //判断公司名称是否有值
                if(currentMediaTemp.get("supplierCompany") != null && StringUtils.isNotEmpty(String.valueOf(currentMediaTemp.get("supplierCompany")))){
                    columnValue = supplierNameMap.get(currentMediaTemp.get("supplierCompany")+"*"+columnValue);
                    if(columnValue == null){
                        flag = "和供应商公司名称不存在";
                    }else {
                        currentMediaTemp.put("supplierId",columnValue);//保存供应商主键ID
                    }
                }else {
                    flag = "和供应商公司名称不存在";
                }
            }else {
                flag = "和供应商公司名称不存在";
            }
        }else if("discount".equals(mediaCommonFieldTemp.get(columnIndex))){
            Integer discount = Integer.parseInt(String.valueOf(columnValue));
            columnValue = (discount == null || discount <= 0) ? 100 : discount;
            currentMediaTemp.put(mediaCommonFieldTemp.get(columnIndex),columnValue);
        }
        else{
            currentMediaTemp.put(mediaCommonFieldTemp.get(columnIndex),columnValue);
        }
        return flag;
    }

    /**
     * 设置媒体扩展
     * @param extendField 媒体表单
     * @param columnValue 字段对应值，Excel中单元格值
     * @param mediaExtendTemp 当前媒体对应的扩展字段
     */
    private String setMediaExtendMap(MediaForm1 extendField, Object columnValue, List<Map> mediaExtendTemp){
        String errorInfo = null;
        Map mediaExtend = new HashMap();
        mediaExtend.put("cell",extendField.getCellCode());
        mediaExtend.put("cellName",extendField.getCellName());
        mediaExtend.put("type",extendField.getType());
        String numberRegex = "^(\\d+)(\\.\\d+)?$"; // 浮点数据正则；
        if("select".equals(extendField.getType()) || "radio".equals(extendField.getType()) || "checkbox".equals(extendField.getType())){
            setOtherFieldMap(extendField); //设置下拉列表、单选框、复选框值列表
            //赋值
            Map<String,String> currentFieldMap = otherFieldMap.get(extendField.getCellCode());
            String columnValueText = null;
            if(currentFieldMap != null && currentFieldMap.size() > 0){  //如果存在列表值,说明页面限制了，则获取，否则没限制，随便用户输入
                columnValueText = String.valueOf(columnValue); //单选框、复选框、下拉列表中文描述值
                if("checkbox".equals(extendField.getType()) && columnValueText.indexOf(",") != -1){ //如果是复选框，并且有多个值
                    String [] columnTexts = columnValueText.split(",");
                    List<String> columnValueTemp = new ArrayList<>();
                    for(String text : columnTexts){
                        columnValueTemp.add(currentFieldMap.get(text));
                    }
                    columnValue = StringUtils.join(columnValueTemp,",");
                }else{
                    columnValue = currentFieldMap.get(columnValue);
                }

                if(columnValue == null){
                    errorInfo = "值错误, 可填写值，如：" + currentFieldMap.keySet();
                }else{
                    mediaExtend.put("cellValue",columnValue);
                    mediaExtend.put("cellValueText",columnValueText);
                }
            }else {
                errorInfo = "没有配置可选值";
            }
        }else if("price".equals(extendField.getType())){
            if(String.valueOf(columnValue).matches(numberRegex)){
                mediaExtend.put("cellValue",columnValue);
            }else {
                errorInfo = "格式错误或金额小于0";
            }
        }else{
            mediaExtend.put("cellValue",columnValue);
        }
        mediaExtendTemp.add(mediaExtend);
        return errorInfo;
    }

    /**
     * 设置下拉列表、单选框、复选框值列表，key为mediaForm1表的cell值
     * @param extendField
     */
    private void setOtherFieldMap(MediaForm1 extendField){
        if(otherFieldMap.get(extendField.getCellCode()) == null){  //当没有缓存值，则进行查询
            if("html".equals(extendField.getDataType())){
                if(StringUtils.isNotEmpty(extendField.getDbSql())){
                    Map<String,String> temp = sql2Map(extendField.getDbSql());
                    if(temp != null && temp.size() > 0){
                        otherFieldMap.put(extendField.getCellCode(), temp);//设置值
                    }
                }else if(StringUtils.isNotEmpty(extendField.getDbJson())){
                    Map<String,String> temp = json2Map(extendField.getDbJson());
                    if(temp != null && temp.size() > 0){
                        otherFieldMap.put(extendField.getCellCode(), temp);//设置值
                    }
                }
            }else if("json".equals(extendField.getDataType()) && StringUtils.isNotEmpty(extendField.getDbJson())){
                Map<String,String> temp = json2Map(extendField.getDbJson());
                if(temp != null && temp.size() > 0){
                    otherFieldMap.put(extendField.getCellCode(), temp);//设置值
                }
            }else if("sql".equals(extendField.getDataType()) && StringUtils.isNotEmpty(extendField.getDbSql())){
                Map<String,String> temp = sql2Map(extendField.getDbSql());
                if(temp != null && temp.size() > 0){
                    otherFieldMap.put(extendField.getCellCode(), temp);//设置值
                }
            }
        }
    }

    /**
     * t_media_form1表中JSON字符串转MAP
     * @param json
     * @return
     */
    private Map<String,String> json2Map(String json){
        Map<String,String> temp = null;
        List<MediaExtendFieldJson> mediaExtendFieldJsonList = JSON.parseArray(json,MediaExtendFieldJson.class);
        if(CollectionUtils.isNotEmpty(mediaExtendFieldJsonList)){
            temp = new HashMap<>();
            for(MediaExtendFieldJson mediaExtendFieldJson : mediaExtendFieldJsonList){
                temp.put(mediaExtendFieldJson.getText(),mediaExtendFieldJson.getValue());
            }
        }
        return temp;
    }

    /**
     * t_media_form1表中sql字符串转MAP
     * @param sql
     * @return
     */
    private Map<String,String> sql2Map(String sql){
        Map<String,String> temp = null;
        List<Map<String, Object>> list =  mediaAuditMapper.dictSQL(sql);
        if(CollectionUtils.isNotEmpty(list)){
            temp = new HashMap<>();
            for(Map map1 : list){
                temp.put(String.valueOf(map1.get("name")),String.valueOf(map1.get("id")));
            }
        }
        return temp;
    }

    /**
     * 设置媒体默认值
     * @param media
     */
    private void setMediaDefaultValue(Map media, int plateId, User user, Date currentDate){
        media.put("isDelete",0);
        media.put("versions",0);
        media.put("picPath","/img/mrt.png");
        media.put("plateId",plateId);
        media.put("userId",user.getId());
        media.put("creatorId",user.getId());
        media.put("updatedId",user.getId());
        media.put("createDate",currentDate);
        media.put("updateDate",currentDate);
        media.put("companyCode",user.getDept().getCompanyCode());
        media.put("companyCodeName",user.getDept().getCompanyCodeName());
        if(media.get("discount") == null){
            media.put("discount", 100);
        }
    }

    /**
     * Map 转 MediaExtend
     */
    private MediaExtendAudit map2MediaExtend(Map map, Object mediaId, Date currentDate){
        MediaExtendAudit mediaExtend = new MediaExtendAudit();
        mediaExtend.setVersions(0);
        mediaExtend.setIsDelete(0);
        mediaExtend.setCreateDate(currentDate);
        mediaExtend.setUpdateDate(currentDate);
        mediaExtend.setCell(String.valueOf(map.get("cell")));
        mediaExtend.setCellName(String.valueOf(map.get("cellName")));
        mediaExtend.setCellValue(String.valueOf(map.get("cellValue")));
        mediaExtend.setCellValueText(map.get("cellValueText") != null ? String.valueOf(map.get("cellValueText")) : "");
        mediaExtend.setType(String.valueOf(map.get("type")));
        mediaExtend.setDbType(String.valueOf(map.get("type")));
        mediaExtend.setMediaId(Integer.parseInt(String.valueOf(mediaId)));
        return mediaExtend;
    }

    /**
     * 保存媒体  和  扩展记录
     * @param standarPlatformFlag 媒体平台标准标识
     * @param mediaExtendMap 所有媒体的扩展记录集合
     * @param validSuccessMedia 待插入的媒体
     * @param currentDate 当前时间
     * @param loopFlag 是否有循环
     * @param loopNum 循环次数
     * @param subLength 每次批量插入最大媒体数
     */
    private List<Integer> saveBatchMedia(Integer standarPlatformFlag, Map<Integer,List<Map>> mediaExtendMap, List<Map> validSuccessMedia, Date currentDate, boolean loopFlag, int loopNum, int subLength){
        List<MediaExtendAudit> mediaExtendList = new ArrayList<>();
        int rowCount = mediaAuditMapper.saveBatch(validSuccessMedia);
        List<Integer> mediaIds = new ArrayList<>();
        if(rowCount > 0 && rowCount == validSuccessMedia.size()){
            for(int index = 0; index < rowCount; index++){
                Object mediaId = validSuccessMedia.get(index).get("id");

                mediaIds.add(Integer.parseInt(String.valueOf(mediaId)));

                int extendIndex = index;
                if(loopFlag){
                    extendIndex = loopNum * subLength + index;
                }
                for(Map map : mediaExtendMap.get(extendIndex)){
                    mediaExtendList.add(map2MediaExtend(map,mediaId,currentDate));
                }
            }
            if(CollectionUtils.isNotEmpty(mediaExtendList)){
                mediaExtendAuditMapper.saveBatch(mediaExtendList);
            }

            //如果是标准平台，拷贝媒体去扩展表
            if(standarPlatformFlag == 1 && CollectionUtils.isNotEmpty(mediaIds)){
                batchCopyMediaAuditBaseInfo2Media(mediaIds);
            }
        }else {
            throw new QinFeiException(1002, "媒体新增记录数与模板不一致！");
        }
        return  mediaIds;
    }

    // 获取用户数据集合；
    private Map<String, Integer> getUserNameMap(int mediaType) {
        if (userNameMap == null) {
            List<User> users = userService.listMJByPlateId(mediaType);
            userNameMap = new HashMap<>();
            for (User user : users) {
                userNameMap.put(user.getName(), user.getId());
            }
        }
        return userNameMap;
    }

    // 获取所有媒体类型的信息集合；
    private Map<String, Integer> getSupplierNameMap() {
        if (supplierNameMap == null) {
            List<Supplier> suppliers = supplierService.listAllSupplierByPlateCompany(null, 1);
            supplierNameMap = new HashMap<>();
            for (Supplier data : suppliers) {
                // 供应商名称 + 手机号；
                if(StringUtils.isNotEmpty(data.getPhone())){
                    supplierNameMap.put(data.getName() + "*" + EncryptUtils.decrypt(data.getPhone()), data.getId());
                }
            }
        }
        return supplierNameMap;
    }

    @Override
    public void batchExport(OutputStream outputStream, Map<String, Object> map) {
        if(map.get("extendParams") != null){
            List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")),MediaExtendParamJson.class);
            if(CollectionUtils.isNotEmpty(mediaExtendParamJsonList)){
                map.put("extendParams",mediaExtendParamJsonList);
                map.put("extendNum",mediaExtendParamJsonList.size());
            }else{
                map.remove("extendParams");
            }
        }
        map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> media1List = mediaAuditMapper.listAllByParam(map);
        List<Integer> ids = new ArrayList<>(); //所有媒体对应ID
        List<Map<String, Object>> sheetInfo = new ArrayList<>(); //导出模板数据
        if(CollectionUtils.isNotEmpty(media1List)){
            for(MediaAudit mediaAudit : media1List){
                ids.add(mediaAudit.getId());
            }
            handleMediaExport(Integer.parseInt(String.valueOf(map.get("standarPlatformFlag"))), Integer.parseInt(String.valueOf(map.get("plateId"))),String.valueOf(map.get("plateName")), ids, null,sheetInfo);
            //导出
            DataImportUtil.createMoreSheetFile(sheetInfo, outputStream);
        }
    }

    @Override
    public void batchChooseExport(OutputStream outputStream, Integer standarPlatformFlag, Integer plateId,String plateName,List<Integer> mediaIds, List<String> relateIds) {
        if(CollectionUtils.isNotEmpty(mediaIds)){
            List<Map<String, Object>> sheetInfo = new ArrayList<>(); //导出模板数据
            handleMediaExport(standarPlatformFlag, plateId,plateName, mediaIds, relateIds,sheetInfo);
            //导出
            DataImportUtil.createMoreSheetFile(sheetInfo, outputStream);
        }
    }

    /**
     * 处理媒体导出
     */
    private void handleMediaExport(Integer standarPlatformFlag, Integer plateId, String plateName, List<Integer> mediaIds, List<String> relateIds, List<Map<String, Object>> sheetInfo){
        //获取板块对应列字段
        Map<String, Object> plateField = getMediaExtendFieldsGroupByPrice(plateId);
        //获取所有媒体基本信息
        Map param = new HashMap();
        param.put("ids",mediaIds);
        List<MediaAudit> mediaAuditList = mediaAuditMapper.listByParam(param);
        List<Map<String, Object>> mediaMapList = new ArrayList<>();  //媒体转换成Map
        if(CollectionUtils.isNotEmpty(mediaAuditList)){
            List<String> mediaRowTitles = new ArrayList<>(); //媒体导出模板列标题
            List<String> mediaRowTitleFields = new ArrayList<>(); //媒体导出模板列
            //如果是标准平台
            if(standarPlatformFlag != null && standarPlatformFlag == 1){
                mediaRowTitleFields.addAll(containIdStandarPlateCommonFieldMap.get(0));
                mediaRowTitles.addAll(containIdStandarPlateCommonFieldMap.get(1));
            }else {
                mediaRowTitles.addAll(mediaExportCommonFieldName);
                mediaRowTitleFields.addAll(mediaExportCommonField);
            }
            mediaRowTitles.addAll((List<String>) plateField.get("fieldNames"));
            List<MediaForm1> mediaForm1List = (List<MediaForm1>) plateField.get("fields");
            for (MediaForm1 mediaForm1 : mediaForm1List){
                mediaRowTitleFields.add(mediaForm1.getCellCode());
            }
            for(MediaAudit mediaAudit : mediaAuditList){
                Map<String, Object> media = new HashMap<>();
                media.put("id", mediaAudit.getId());
                media.put("userId", mediaAudit.getUser().getUserName());
                media.put("name", mediaAudit.getName());
                if(standarPlatformFlag != null && standarPlatformFlag == 1){
                    media.put("mediaContentId", mediaAudit.getMediaContentId());
                    media.put("link", mediaAudit.getLink());
                }else {
                    media.put("link", mediaAudit.getLink());
                }
                media.put("discount", mediaAudit.getDiscount());
                media.put("remarks", mediaAudit.getRemarks());
                for(MediaExtendAudit mediaExtendAudit : mediaAudit.getMediaExtends()){
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
            mediaMap.put("notices",getMediaNotices());
            sheetInfo.add(mediaMap);
        }

        //获取媒体供应商价格信息
        Map<String, Object> param1 = new HashMap<>();
        param1.put("mediaIds",mediaIds);
        List<MediaPrice> mediaPrices = mediaAuditMapper.listMediaSupplierInfoByMediaIds(param1);
        List<Map<String, Object>> mediaSupplierMapList = new ArrayList<>();  //媒体供应商转换成Map
        if(CollectionUtils.isNotEmpty(mediaPrices)){
            List<String> mediaSupplierRowTitles = new ArrayList<>(); //媒体供应商价格导出模板列标题
            List<String> mediaSupplierRowTitleFields = new ArrayList<>(); //媒体供应商价格导出模板列

            if(standarPlatformFlag != null && standarPlatformFlag == 1){
                mediaSupplierRowTitles.addAll(standarMediaSupplierCommonFieldName);
                mediaSupplierRowTitleFields.addAll(standarMediaSupplierCommonField);
            }else {
                mediaSupplierRowTitles.addAll(mediaSupplierCommonFieldName);
                mediaSupplierRowTitleFields.addAll(mediaSupplierCommonField);
            }
            mediaSupplierRowTitles.addAll((List<String>) plateField.get("priceFieldNames"));
            List<MediaForm1> mediaForm1PriceList = (List<MediaForm1>) plateField.get("priceFields");
            for (MediaForm1 mediaForm1 : mediaForm1PriceList){
                mediaSupplierRowTitleFields.add(mediaForm1.getCellCode());
            }
            List<MediaPlate> mediaPlateList = mediaPlateMapper.listMediaPlateByUserId(AppUtil.getUser().getId());
            List<Integer> mediaTypeIds = new ArrayList<>();
            for (MediaPlate mediaPlate : mediaPlateList) {
                mediaTypeIds.add(mediaPlate.getId());
            }
            User user = AppUtil.getUser();
            String depts  = userService.getChilds(user.getDeptId());
            if (!org.springframework.util.StringUtils.isEmpty(depts) && depts.indexOf("$,") > -1) {
                depts = depts.substring(2);
            }
            for(MediaPrice mediaPrice : mediaPrices){
                Map<Integer, Map<String, Object>> temp = new HashMap<>(); //缓存一个媒体多个供应商信息
                if(CollectionUtils.isNotEmpty(mediaPrice.getMediaPriceCellList())){
                    for(MediaPriceCell mediaPriceCell: mediaPrice.getMediaPriceCellList()){
                        String key = mediaPrice.getMediaId() + "-" + mediaPriceCell.getSupplierId();
                        if(CollectionUtils.isEmpty(relateIds) || (CollectionUtils.isNotEmpty(relateIds) && relateIds.contains(key))){
                            if(temp.get(mediaPriceCell.getSupplierId()) == null){
                                Map<String, Object> mediaSupplier = new HashMap<>();
                                mediaSupplier.put("name", mediaPrice.getMediaName());
                                mediaSupplier.put("mediaContentId", mediaPrice.getMediaContentId());
                                mediaSupplier.put("supplierCompany", mediaPriceCell.getSupplierCompanyName());
                                mediaSupplier.put("supplierId", mediaPriceCell.getSupplierName());
                                if(StringUtils.isNotEmpty(mediaPriceCell.getSupplierPhone())){
                                    Integer creator = mediaPriceCell.getSupplierCreator();
                                    //获取供应商对应的所有的媒体板块id
                                    String plateIds = mediaAuditMapper.selectPlateIdsForSupplierId(mediaPriceCell.getSupplierId(),onlineTime);
                                    String [] plateIdList = plateIds.split(",");
                                    boolean flag =false;
                                    boolean mediaFlagMgr = false;
                                    if(plateIdList.length>0){
                                        for (int i=0; i< plateIdList.length ; i++){
                                            if (mediaTypeIds.contains(Integer.parseInt(plateIdList[i]))){
                                                flag = true;
                                            }
                                        }
                                    }
                                    if (user.getIsMgr()==1 && !org.springframework.util.StringUtils.isEmpty(depts) && depts.contains(mediaPriceCell.getDeptId().toString())){
                                        mediaFlagMgr= true;
                                    }

                                    String phone = EncryptUtils.decrypt(mediaPriceCell.getSupplierPhone());
                                    if((user.getId().toString().equals(creator.toString())) || (flag && mediaFlagMgr)){

                                    }else{
                                        if(phone.length() >= 11){
                                            String start = phone.length() > 11 ? "*****" : "****";
                                            phone = phone.substring(0, 3) + start + phone.substring(phone.length() - 4, phone.length());
                                        }else if(phone.length() >= 3){
                                            phone = phone.substring(0, 1) + "***" + phone.substring(phone.length() - 1);
                                        }
                                    }
                                    mediaSupplier.put("phone", phone);
                                }
                                temp.put(mediaPriceCell.getSupplierId(), mediaSupplier);
                            }
                            String tempValue = mediaPriceCell.getCellValue();
                            if("select".equals(mediaPriceCell.getCellType()) || "radio".equals(mediaPriceCell.getCellType()) || "checkbox".equals(mediaPriceCell.getCellType())){
                                if(StringUtils.isNotEmpty(mediaPriceCell.getCellValue())){
                                    tempValue = mediaPriceCell.getCellValueText();
                                }else{
                                    tempValue = "";
                                }
                            }
                            temp.get(mediaPriceCell.getSupplierId()).put(mediaPriceCell.getCell(), tempValue);
                        }
                    }
                    mediaSupplierMapList.addAll(temp.values());
                }
            }
            List<Object[]> mediaSupplierList = new ArrayList<>(); //媒体基本信息
            for(Map<String, Object> mediaSupplier : mediaSupplierMapList){
                Object[] objects = new Object[mediaSupplierRowTitleFields.size()];
                for(int i = 0; i < mediaSupplierRowTitleFields.size(); i++){
                    objects[i] = mediaSupplier.get(mediaSupplierRowTitleFields.get(i));
                }
                mediaSupplierList.add(objects);
            }
            Map<String, Object> mediaSupplierMap = new HashMap<>();
            mediaSupplierMap.put("templateName",plateName+"媒体供应商价格导出信息");
            mediaSupplierMap.put("rowTitles",mediaSupplierRowTitles);
            mediaSupplierMap.put("exportData",mediaSupplierList);
            mediaSupplierMap.put("notices",getMediaSupplierPriceNotices());
            sheetInfo.add(mediaSupplierMap);
        }
    }

    @Override
    @Transactional
    public String batchReplaceImport(String fileName, Integer standarPlatformFlag, Integer plateId, String plateName, Integer fileType){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        String s  = fileName.substring(config.getWebDir().length());
        File file = new File(config.getUploadDir() + s);
        if (file.exists()){
            String result = null;
            if(fileType == 0){
                Map<String, Object> extendFormGroup = getMediaExtendFieldsGroupByPrice(plateId); //获取板块对应媒体表单数据
                Map<String, Object>  handResult = handleReplaceDataByFileType(file, fileType, standarPlatformFlag, plateId, plateName, extendFormGroup, mediaExportCommonFieldName.size());
                if(handResult != null){
                    result = DataImportUtil.createFile(String.valueOf(handResult.get("templateName")), config.getUploadDir(), config.getWebDir(), (List<String>) handResult.get("rowTitles"), (List<Object[]>) handResult.get("exportData"));
                }
            }else if(fileType == 1){
                Map<String, Object> extendFormGroup = getMediaExtendFieldsGroupByPrice(plateId); //获取板块对应媒体表单数据
                Map<String, Object>  handResult = handleReplaceDataByFileType(file, fileType, standarPlatformFlag, plateId, plateName, extendFormGroup, mediaSupplierCommonFieldName.size());
                if(handResult != null){
                    result = DataImportUtil.createFile(String.valueOf(handResult.get("templateName")), config.getUploadDir(), config.getWebDir(), (List<String>) handResult.get("rowTitles"), (List<Object[]>) handResult.get("exportData"));
                }
            }else {
                Map<String, Object> extendFormGroup = getMediaExtendFieldsGroupByPrice(plateId); //获取板块对应媒体表单数据
                Map<String, Object>  mediaResult = handleReplaceDataByFileType(file, 0, standarPlatformFlag, plateId, plateName, extendFormGroup, mediaExportCommonFieldName.size());
                Map<String, Object>  mediaSupplierResult = handleReplaceDataByFileType(file, 1, standarPlatformFlag, plateId, plateName, extendFormGroup, mediaSupplierCommonFieldName.size());
                List<Map<String, Object>> sheetInfo = new ArrayList<>();
                if(mediaResult != null || mediaSupplierResult != null){
                    sheetInfo.add(mediaResult);
                    sheetInfo.add(mediaSupplierResult);
                    result = DataImportUtil.createMoreSheetFile("媒体导入所有类型失败内容", sheetInfo, config.getUploadDir(), config.getWebDir());
                }
            }
            if(result != null){
                throw new QinFeiException(1003, result); //如果有内容错误，则抛出异常，事务失效，防止一次导入三个Excel的sheet表时，前面sheet数据正常而导致数据入库
            }
            return result;
        }else{
            throw  new QinFeiException(1002, "导入文件不存在！");
        }
    }

    @Override
    public List<MediaForm1> listMediaField(int plateId) {
        List<MediaForm1> extendFormList = mediaForm1Service.listMediaFormByPlateId(plateId);
        return extendFormList;
    }

    @Override
    public List<MediaChange> listMediaChange(String mediaIds) {
        List<MediaChange> mediaChangeList = new ArrayList<>();
        try{
            if(AppUtil.getUser() != null && StringUtils.isNotEmpty(mediaIds)){
                List<Integer> param = new ArrayList<>();
                for(String mediaId : mediaIds.split(",")){
                    param.add(Integer.parseInt(mediaId));
                }
                mediaChangeList = mediaChangeMapper.listMediaChangeByParam(param);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mediaChangeList;
    }

    @Override
    @Transactional
    public void mediaChangeRecover(int mediaChangeId, Integer standarPlatformFlag) {
        try{
            boolean recoverFlag = false;//恢复标识，默认没有恢复
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            MediaChange mediaChange = mediaChangeMapper.getMediaChangeById(mediaChangeId);
            if(mediaChange == null || StringUtils.isEmpty(mediaChange.getChangeContent())){
                throw new QinFeiException(1002, "异动信息不存在！");
            }

            //异动前数据
            List<Media1> oldMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaChange.getMediaId()));

            Map<String, Object> jsonData = JSON.parseObject(mediaChange.getChangeContent(), Map.class);
            if(jsonData.get("recover") != null && jsonData.get("recover") instanceof Map){
                Map<String, Object> beseDataMap = null;//媒体基本信息
                Map<String, Object> recoverMap = (Map<String, Object>) jsonData.get("recover");//恢复字段
                if(recoverMap.get("extendList") != null && recoverMap.get("extendList") instanceof List){
                    List extendList = (List) recoverMap.get("extendList");//上次异动前的媒体扩展字段
                    List<Map<String, Object>> mediaExtends = new ArrayList<>();
                    if(CollectionUtils.isNotEmpty(extendList)){
                        extendList.forEach(o -> {
                            if(o instanceof Map){
                                Map<String, Object> map = (Map<String, Object>) o;
                                mediaExtends.add(map);
                            }
                        });
                        Map<String, Object> param = new HashMap<>();
                        param.put("mediaId", mediaChange.getMediaId());
                        param.put("mediaExtends", mediaExtends);
                        mediaExtendAuditMapper.updateBatch(param);
                        //更新扩展表
                        recoverFlag = true;//更新基础表状态
                    }
                }
                if(recoverMap.get("baseData") != null && recoverMap.get("baseData") instanceof Map){
                    beseDataMap = (Map<String, Object>) recoverMap.get("baseData");//上次异动前的媒体基础字段
                    if(beseDataMap.size() > 0){
                        recoverFlag = true;//更新基础表状态
                    }
                }
                //更新媒体实际表为待审核
                if(recoverFlag){
                    if(beseDataMap == null){
                        beseDataMap = new HashMap<>();
                    }
                    beseDataMap.put("id", mediaChange.getMediaId());
                    beseDataMap.put("state", 0); //0-待审核
                    beseDataMap.put("updatedId", user.getId());


                    //标准板块不需要审核
                    if(standarPlatformFlag != null && standarPlatformFlag == 1){
                        beseDataMap.put("state", 1); //1-已审批
                        mediaAuditMapper.updateMediaForMap(beseDataMap);
                        copyMediaAuditBaseInfo2Media(mediaChange.getMediaId());
                    }else {
                        mediaAuditMapper.updateMediaForMap(beseDataMap);
                        //更新媒体的删除字段，当媒体修改后，先不直接修改state字段为0，先用这个字段代替，方便媒体驳回后返回之前状态使用
                        media1Mapper.updateMediaIsDelete(mediaChange.getMediaId(), 1, user.getId());
                    }

                    //处理媒体异动，增加异常捕获，使其不影响正常操作
                    try{
                        List<Media1> newMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(mediaChange.getMediaId()));
                        List<MediaChange> mediaChangeList = mediaChangeHandler(newMediaList, oldMediaList, user);
                        if(CollectionUtils.isNotEmpty(mediaChangeList)){
                            mediaChangeMapper.saveBatch(mediaChangeList);
                        }
                    }catch (Exception e){
                        log.error("【媒体异动恢复】媒体异动记录异常: {}", e.getMessage());
                    }
                }
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "异动恢复异常！");
        }
    }

    @Override
    public List<MediaSupplierChange> listMediaSupplierChange(String relateIds) {
        List<MediaSupplierChange> mediaSupplierChangeList = new ArrayList<>();
        try{
            if(AppUtil.getUser() != null && StringUtils.isNotEmpty(relateIds)){
                List<Integer> param = new ArrayList<>();
                for(String relateId : relateIds.split(",")){
                    param.add(Integer.parseInt(relateId));
                }
                mediaSupplierChangeList = mediaSupplierChangeMapper.listMediaSupplierChangeByParam(param);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mediaSupplierChangeList;
    }

    @Override
    @Transactional
    public void mediaSupplierChangeRecover(int mediaSupplierChangeId, Integer standarPlatformFlag) {
        try{
            boolean recoverFlag = false;//恢复标识，默认没有恢复
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            MediaSupplierChange mediaSupplierChange = mediaSupplierChangeMapper.getMediaSupplierChangeById(mediaSupplierChangeId);
            if(mediaSupplierChange == null || StringUtils.isEmpty(mediaSupplierChange.getChangeContent())){
                throw new QinFeiException(1002, "异动信息不存在！");
            }
            //异动前数据
            List<Integer> relateIds = Arrays.asList(mediaSupplierChange.getMediaSupplierRelateId());
            List<MediaSupplierRelate> oldMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);
            Map<String, Object> jsonData = JSON.parseObject(mediaSupplierChange.getChangeContent(), Map.class);
            if(jsonData.get("recover") != null && jsonData.get("recover") instanceof Map){
                Map<String, Object> beseDataMap = null;//媒体基本信息
                Map<String, Object> recoverMap = (Map<String, Object>) jsonData.get("recover");//恢复字段
                if(recoverMap.get("extendList") != null && recoverMap.get("extendList") instanceof List){
                    List extendList = (List) recoverMap.get("extendList");//上次异动前的媒体供应商扩展字段
                    List<Map<String, Object>> mediaExtends = new ArrayList<>();
                    if(CollectionUtils.isNotEmpty(extendList)){
                        extendList.forEach(o -> {
                            if(o instanceof Map){
                                Map<String, Object> map = (Map<String, Object>) o;
                                map.put("mediaSupplierRelateId", mediaSupplierChange.getMediaSupplierRelateId());//媒体供应商关系ID
                                mediaExtends.add(map);
                            }
                        });
                        mediaSupplierPriceAuditMapper.updateBatchForMap(mediaExtends);
                        //更新扩展表
                        recoverFlag = true;//更新基础表状态
                    }
                }
                if(recoverMap.get("baseData") != null && recoverMap.get("baseData") instanceof Map){
                    beseDataMap = (Map<String, Object>) recoverMap.get("baseData");//上次异动前的媒体基础字段
                    if(beseDataMap.size() > 0){
                        recoverFlag = true;//更新基础表状态
                    }
                }
                //更新媒体实际表为待审核
                if(recoverFlag){
                    if(beseDataMap == null){
                        beseDataMap = new HashMap<>();
                    }
                    beseDataMap.put("id", mediaSupplierChange.getMediaSupplierRelateId());
                    beseDataMap.put("state", 0); //0-待审核
                    beseDataMap.put("updatedId", user.getId());

                    //标准板块不需要审核
                    if(standarPlatformFlag != null && standarPlatformFlag == 1){
                        beseDataMap.put("state", 1); //1-已审核
                        mediaSupplierRelateAuditMapper.updateMediaSupplierRelateForMap(beseDataMap);
                        copyMediaRelateAudit2MediaRelate(Arrays.asList(mediaSupplierChange.getMediaSupplierRelateId()));
                    }else {
                        mediaSupplierRelateAuditMapper.updateMediaSupplierRelateForMap(beseDataMap);
                        //先不直接修改state字段为0，先用这个字段代替，方便媒体关系驳回后返回之前状态使用
                        mediaSupplierRelateMapper.updateMediaRelateIsDelete(1, user.getId(),mediaSupplierChange.getMediaSupplierRelateId());
                        //更新媒体审核报最低价格
//                    setMediaAuditMinPrice(Arrays.asList(mediaSupplierChange.getMediaId()));
                    }

                    //处理媒体异动，增加异常捕获，使其不影响正常操作
                    try{
                        //如果有新增媒体供应商关系
                        if(CollectionUtils.isNotEmpty(relateIds)){
                            MediaAudit mediaAudit = mediaAuditMapper.getMediaById(mediaSupplierChange.getMediaId());
                            List<MediaSupplierRelate> newMediaRelateList = mediaSupplierRelateAuditMapper.listMediaSupplierDetailByIds(relateIds);
                            List<MediaSupplierChange> mediaSupplierChangeList = mediaSupplierChangeHandler(Arrays.asList(mediaAudit), newMediaRelateList, oldMediaRelateList, user);
                            if(CollectionUtils.isNotEmpty(mediaSupplierChangeList)){
                                mediaSupplierChangeMapper.saveBatch(mediaSupplierChangeList);
                            }
                        }
                    }catch (Exception e){
                        log.error("【媒体登记】媒体异动记录异常: {}", e.getMessage());
                    }
                }
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "异动恢复异常！");
        }
    }

    @Override
    public void batchChangeChooseExport(OutputStream outputStream, String plateName, List<Integer> mediaIds) {
        List<Map<String, Object>> sheetInfo = new ArrayList<>(); //导出模板数据
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            handlerMediaChangeExport(plateName, sheetInfo, mediaIds);//处理异动导出数据
        }catch (QinFeiException e){
            buildTipExcel(e.getMessage(), sheetInfo);
        }catch (Exception e){
            buildTipExcel(e.getMessage(), sheetInfo);
        }
        if(CollectionUtils.isEmpty(sheetInfo)){
            buildTipExcel("没有找到异动数据", sheetInfo);
        }
        DataImportUtil.createMoreSheetFile(sheetInfo, outputStream);
    }

    @Override
    public void mediaChangeBatchExport(OutputStream outputStream, Map<String, Object> map) {
        List<Map<String, Object>> sheetInfo = new ArrayList<>(); //导出模板数据
        try{
            String plateName = String.valueOf(map.get("plateName"));
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(map.get("extendParams") != null){
                List<MediaExtendParamJson> mediaExtendParamJsonList = JSON.parseArray(String.valueOf(map.get("extendParams")),MediaExtendParamJson.class);
                if(CollectionUtils.isNotEmpty(mediaExtendParamJsonList)){
                    map.put("extendParams",mediaExtendParamJsonList);
                    map.put("extendNum",mediaExtendParamJsonList.size());
                }else{
                    map.remove("extendParams");
                }
            }
            map.put("onlineTime", onlineTime);//不能查询历史数据，增加本次改动上线时间
            List<MediaAudit> media1List = mediaAuditMapper.listAllByParam(map);
            List<Integer> ids = new ArrayList<>(); //所有媒体对应ID
            if(CollectionUtils.isNotEmpty(media1List)){
                for(MediaAudit mediaAudit : media1List){
                    ids.add(mediaAudit.getId());
                }
                handlerMediaChangeExport(plateName, sheetInfo, ids);//处理异动导出数据
            }
        }catch (QinFeiException e){
            buildTipExcel(e.getMessage(), sheetInfo);
        }catch (Exception e){
            buildTipExcel(e.getMessage(), sheetInfo);
        }
        if(CollectionUtils.isEmpty(sheetInfo)){
            buildTipExcel("没有找到异动数据", sheetInfo);
        }
        DataImportUtil.createMoreSheetFile(sheetInfo, outputStream);
    }

    @Transactional
    @Override
    public void updateMediaUserId(Integer id, Integer userId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(id == null){
                throw new QinFeiException(1002, "媒体ID为空！");
            }
            if(userId == null){
                throw new QinFeiException(1002, "指派责任人不能为空！");
            }

            //媒体异动前数据
            List<Media1> oldMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(id));

            mediaAuditMapper.updateMediaUserId(id, userId, user.getId());
            media1Mapper.updateMediaUserId(id, userId, user.getId());

            //处理媒体异动，增加异常捕获，使其不影响正常操作
            try{
                List<Media1> newMediaList = mediaAuditMapper.listMediaDetailByIds(Arrays.asList(id));
                List<MediaChange> mediaChangeList = mediaChangeHandler(newMediaList, oldMediaList, user);
                if(CollectionUtils.isNotEmpty(mediaChangeList)){
                    mediaChangeMapper.saveBatch(mediaChangeList);
                }
            }catch (Exception e){
                log.error("【媒体停用】媒体异动记录异常: {}", e.getMessage());
            }
        }catch (QinFeiException e){
           throw e;
        }catch (Exception e){
           log.error(e.getMessage());
           throw new QinFeiException(1002, "媒体责任人指派异常！");
        }
    }

    @Override
    public PageInfo<Map<String, Object>> listHistoryArtByParam(Integer mediaTypeId, String keyword, Pageable pageable) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            User user = AppUtil.getUser();
            if (user != null) {
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                //查询历史稿件，所以增加本次改动上线时间
                result = mediaAuditMapper.listHistoryArtByParam(mediaTypeId, keyword, onlineTime);
            }
        } catch (Exception e) {
            log.error("【稿件媒体供应商替换】分页查询历史稿件异常：{}", e.getMessage());
        }
        return new PageInfo<>(result);
    }

    @Transactional
    @Override
    public void artMediaSupplierReplace(ArticleReplace articleReplace) {
        try {
            User user = AppUtil.getUser();
            if (user == null) {
                throw new QinFeiException(1002, "请先登录！");
            }
            if (articleReplace.getArticleIdList() == null) {
                throw new QinFeiException(1002, "历史稿件ID不存在！");
            }
            if (articleReplace.getPlateId() == null) {
                throw new QinFeiException(1002, "媒体板块不存在！");
            }
            if (articleReplace.getOldMediaId() == null) {
                throw new QinFeiException(1002, "稿件没有关联媒体不允许替换！");
            }
            if (articleReplace.getOldSupplierId() == null) {
                throw new QinFeiException(1002, "稿件没有关联供应商不允许替换！");
            }
            if (articleReplace.getNewMediaId() == null) {
                throw new QinFeiException(1002, "请选择替换媒体！");
            }
            if (articleReplace.getNewSupplierId() == null) {
                throw new QinFeiException(1002, "请选择替换供应商！");
            }
            Media1 media = media1Mapper.getMediaById(articleReplace.getNewMediaId());
            if (media == null) {
                throw new QinFeiException(1002, "选择替换的媒体信息未审核或不存在！");
            }
            Supplier supplier = supplierMapper.getById(articleReplace.getNewSupplierId());
            if (supplier == null) {
                throw new QinFeiException(1002, "选择替换的供应商信息不存在！");
            }
            List<Integer> artIdList = new ArrayList<>();
            for (String artId : articleReplace.getArticleIdList().split(",")) {
                artIdList.add(Integer.parseInt(artId));
            }
            Map<String, Object> param = new HashMap<>();
            param.put("updateUserId", user.getId());
            param.put("mediaId", media.getId());
            param.put("mediaName", StringUtils.isNotEmpty(media.getName()) ? media.getName() : media.getMediaContentId());
            param.put("supplierId", supplier.getId());
            param.put("supplierName", supplier.getName());
            param.put("supplierContactor", supplier.getContactor());
            param.put("mediaPersonId", media.getUserId());
            param.put("supplierPersonId", supplier.getCreator());
            param.put("artIds", artIdList);
            //1、替换稿件记录中的媒体供应商
            int row = mediaAuditMapper.updateArtByArtIds(param);
            if (row > 0) {
                mediaAuditMapper.updateArtHistoryByArtIds(param);
                //2、替换请款记录中的供应商
                String outgoIdList = mediaAuditMapper.listOutgoIdByArtIds(artIdList);
                if (StringUtils.isNotEmpty(outgoIdList)) {
                    List<Integer> outgoIds = new ArrayList<>();
                    for (String outgoId : outgoIdList.split(",")) {
                        outgoIds.add(Integer.parseInt(outgoId));
                    }
                    param.put("outgoIds", outgoIds);
                    row = mediaAuditMapper.updateOutgoByIds(param);
                    if (row > 0) {
                        articleReplace.setOutgoIdList(outgoIdList);
                    }
                }
                //3、替换删稿记录中的供应商
                String dropIdList = mediaAuditMapper.listOutgoIdByArtIds(artIdList);
                if (StringUtils.isNotEmpty(dropIdList)) {
                    List<Integer> dropIds = new ArrayList<>();
                    for (String dropId : dropIdList.split(",")) {
                        dropIds.add(Integer.parseInt(dropId));
                    }
                    param.put("dropIds", dropIds);
                    row = mediaAuditMapper.updateDropByIds(param);
                    if (row > 0) {
                        articleReplace.setDropIdList(dropIdList);
                    }
                }
                //4、保存替换记录
                articleReplace.setUserId(user.getId());
                articleReplace.setUserName(user.getName());
                articleReplaceMapper.saveBatch(Arrays.asList(articleReplace));
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            log.error("【历史稿件媒体供应商替换】替换异常：{}", e.getMessage());
            throw new QinFeiException(1002, "历史稿件媒体供应商替换异常！");
        }
    }

    private void handlerMediaChangeExport(String plateName, List<Map<String, Object>> sheetInfo, List<Integer> ids){
        //媒体异动导出
        List<MediaChange> mediaChangeList = mediaChangeMapper.listMediaChangeByParam(ids);
        if(CollectionUtils.isNotEmpty(mediaChangeList)){
            List<String> rowTitleList = new ArrayList<>();
            List<Object[]> excelContent = new ArrayList<>();
            for(MediaChange mediaChange : mediaChangeList){
                MediaSupplierChange mediaSupplierChange = new MediaSupplierChange();
                BeanUtils.copyProperties(mediaChange, mediaSupplierChange);
                buildExportData(mediaSupplierChange, rowTitleList, excelContent, false);
            }
            Map<String, Object> mediaChangeMap = new HashMap<>();
            mediaChangeMap.put("templateName",plateName + "媒体异动详情");
            mediaChangeMap.put("rowTitles",rowTitleList);
            mediaChangeMap.put("exportData",excelContent);
            mediaChangeMap.put("notices", Arrays.asList("没有发生异动的媒体异动信息不展示！"));
            sheetInfo.add(mediaChangeMap);
        }

        //媒体供应商异动导出
        List<MediaSupplierChange> mediaSupplierChangeList = mediaSupplierChangeMapper.listMediaSupplierChangeByMediaIds(ids);
        if(CollectionUtils.isNotEmpty(mediaSupplierChangeList)){
            List<String> rowTitleList = new ArrayList<>();
            List<Object[]> excelContent = new ArrayList<>();
            for(MediaSupplierChange mediaSupplierChange : mediaSupplierChangeList){
                buildExportData(mediaSupplierChange, rowTitleList, excelContent, true);
            }
            Map<String, Object> mediaSupplierChangeMap = new HashMap<>();
            mediaSupplierChangeMap.put("templateName",plateName + "媒体供应商异动详情");
            mediaSupplierChangeMap.put("rowTitles",rowTitleList);
            mediaSupplierChangeMap.put("exportData",excelContent);
            mediaSupplierChangeMap.put("notices", Arrays.asList("没有发生异动的媒体异动信息不展示！"));
            sheetInfo.add(mediaSupplierChangeMap);
        }
    }

    //构建异动导出数据
    private void buildExportData(MediaSupplierChange mediaSupplierChange, List<String> rowTitleList, List<Object[]> excelContent, boolean isRelate){
        Map<String, Object> jsonData = JSON.parseObject(mediaSupplierChange.getChangeContent(), Map.class);
        if(jsonData.get("export") != null && jsonData.get("export") instanceof Map){
            Map<String, Object> exportMap = (Map<String, Object>) jsonData.get("export");//恢复字段
            if(exportMap.get("titleList") != null && exportMap.get("titleList") instanceof List){
                if(rowTitleList == null || rowTitleList.size() < 1){
                    //供应商异动多有这几列
                    rowTitleList.add("当前媒体名称");
                    if(isRelate){
                        rowTitleList.add("供应商名称");
                        rowTitleList.add("供应商联系人");
                    }
                    rowTitleList.addAll((List<String>) exportMap.get("titleList"));
                    rowTitleList.add("异动人");
                    rowTitleList.add("异动审核人");
                    rowTitleList.add("异动时间");
                }
            }
            if(exportMap.get("dataList") != null && exportMap.get("dataList") instanceof List){
                Object[] objects = new Object[rowTitleList.size()];
                int index = 0;
                objects[index++] = mediaSupplierChange.getMediaName();
                if(isRelate){
                    objects[index++] = mediaSupplierChange.getSupplierName();
                    objects[index++] = mediaSupplierChange.getContactor();
                }
                List<String> dataList = (List<String>) exportMap.get("dataList");
                for(int i = 0; i < dataList.size(); i++){
                    objects[index++] = dataList.get(i);
                }
                objects[index++] = mediaSupplierChange.getAuditUserName();
                objects[index++] = mediaSupplierChange.getUserName();
                objects[index++] = DateUtils.format(mediaSupplierChange.getCreateDate(), "yyyy-MM-dd HH:mm:ss");
                excelContent.add(objects);
            }
        }
    };

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

    /**
     * 根据类型处理不同上传文件数据
     * @param file 文件对象
     * @param type 0-供应商处理、1-媒体处理、2-供应商价格处理
     * @param plateId 板块Id：仅对1-媒体处理和2-供应商价格处理有效
     * @param plateName 板块名称：仅对1-媒体处理和2-供应商价格处理有效
     * @param extendFormGroup 扩展字段组：仅对1-媒体处理和2-供应商价格处理有效
     * @param commonFieldSize 公共字段长度：仅对1-媒体处理和2-供应商价格处理有效
     */
    private Map<String, Object> handleReplaceDataByFileType(File file, Integer type, Integer standarPlatformFlag, Integer plateId, String plateName, Map<String, Object> extendFormGroup, Integer commonFieldSize){
        List<Object[]> excelContent = null;
        Map<String, Object>  handResult = null;
        if(type == 0){
            List<String> mediaExtendFieldNames = (List<String>)extendFormGroup.get("fieldNames");//获取指定媒体板块模板扩展列名称列表
            List<MediaForm1> mediaExtendFields = (List<MediaForm1>)extendFormGroup.get("fields");//获取指定媒体板块模板扩展列对应属性值列表
//            excelContent = DataImportUtil.getExcelContent(file, type,3, 2, (commonFieldSize + mediaExtendFieldNames.size()));
            excelContent = EasyExcelUtil.getExcelContent(file, type+1, 3, 2);
            if (CollectionUtils.isNotEmpty(excelContent)){
                handResult = handleMeidaReplaceData(AppUtil.getUser(), standarPlatformFlag, plateId, plateName+"媒体", mediaExtendFieldNames,mediaExtendFields, excelContent);
            }
        }else {
            List<String> mediaSupplierPriceFiledNames = (List<String>)extendFormGroup.get("priceFieldNames");//获取指定媒体板块模板扩展列名称列表
            List<MediaForm1> mediaSupplierPriceExtendFields = (List<MediaForm1>)extendFormGroup.get("priceFields");//获取指定媒体板块模板扩展列对应属性值列表
//            excelContent = DataImportUtil.getExcelContent(file, type,3, 2, (commonFieldSize + mediaSupplierPriceFiledNames.size()));
            excelContent = EasyExcelUtil.getExcelContent(file, type+1, 3, 2);
            if (CollectionUtils.isNotEmpty(excelContent)) {
                handResult = handleSupplierPriceData(AppUtil.getUser(), standarPlatformFlag, plateId, plateName+"供应商价格", mediaSupplierPriceFiledNames,mediaSupplierPriceExtendFields, excelContent);
            }
        }
        return handResult;
    }

    /**
     * 处理媒体替换功能，不允许新增，只能替换并且不能替换媒体名称
     * @param user 当前登入用户
     * @param plateId 媒体板块ID
     * @param plateName 媒体板块名称
     * @param mediaExtendFieldNames 媒体导入模板列对应列名列表
     * @param mediaExtendFields 媒体导入模板列对应属性值列表
     * @param excelContent 媒体导入文件内容
     */
    private Map<String, Object> handleMeidaReplaceData(User user,Integer standarPlatformFlag, Integer plateId, String plateName, List<String> mediaExtendFieldNames, List<MediaForm1> mediaExtendFields, List<Object[]> excelContent){
        Date currentDate = new Date();
        List<String> rowTitles = new ArrayList<>(); //导入模板总列数
        List<String> mediaExportCommonFieldTemp = new ArrayList<>();
        String primaryKey = "";
        //标准平台媒体替换
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            rowTitles.addAll(containIdStandarPlateCommonFieldMap.get(1));
            mediaExportCommonFieldTemp = containIdStandarPlateCommonFieldMap.get(0);
            primaryKey = "mediaContentId";
        }else {
            rowTitles.addAll(mediaExportCommonFieldName);
            mediaExportCommonFieldTemp = mediaExportCommonField;
            primaryKey = "name";
        }
        rowTitles.addAll(mediaExtendFieldNames);
        int totalColumnNum = rowTitles.size();//总列数
        int commonColumnNum = mediaExportCommonFieldTemp.size();//公共列数
        int rowNum = excelContent.size(); //导入媒体行数

        //-------下拉列表值缓存-开始-------
        userNameMap = null; // 获取所有媒体类型的责任人信息集合；
        supplierNameMap = null; //获取当前板块供应商信息集合
        otherFieldMap = new HashMap<>(); // 其他复选框、下拉列表、单选框。

        //获取导入数据媒体ID
        List<Integer> mediaIds = new ArrayList<>();
        for(int i = 0; i < rowNum; i++){
            Object id = excelContent.get(i)[0];
            if(id != null){
                mediaIds.add(Integer.parseInt(String.valueOf(id)));
            }
        }
        //不能查询历史数据，增加本次改动上线时间
        List<MediaAudit> currentMediaList = mediaAuditMapper.listMediaByIds(mediaIds, onlineTime);
        Map<String,MediaAudit> currentMediaMap = new HashMap<>(); //获取当前媒体板块下有用的媒体，用于判断名称是否存在,key = name
        for(MediaAudit media1 : currentMediaList){
            currentMediaMap.put(media1.getId()+"",media1);
        }
        currentMediaList.clear();
        currentMediaList = null; //对象已使用完毕，对其进行释放

        //-------下拉列表值缓存-结束-------
        List<Map<String, Object>> validSuccessMedia = new ArrayList<>(); // 用于保存校验通过的数据；
        List<Object[]> validErrorMedia = new ArrayList<>(); // 用于保存校验未通过的数据；
        List<MediaExtendAudit> validSuccessMediaExtendList = new ArrayList<>();// 用于缓存保存成功的媒体扩展表单数据
        Object [] row = null;
        for(int j = 0; j < rowNum; j++){  //遍历行数据
            row = excelContent.get(j); //获取行数据
            if(row.length <= 1){
                continue; //如果行数据仅有一列，则为Excel文件中说明信息，直接处理下一行数据
            }
            if(row.length != totalColumnNum){  //如果行数据与媒体模板列个数不一致，直接判断下一个
                row = Arrays.copyOf(row, row.length + 1);
                row[row.length - 1] = "第"+(j+1)+"行数据与模板列格式不对应";
                validErrorMedia.add(row);
                continue;
            }
            Map<String,Object> currentMediaTemp = new HashMap<>();//缓存媒体数据
            List<MediaExtendAudit> mediaExtendTemp = new ArrayList<>();
            boolean isValidSuccess = true; // 校验成功标志，默认校验成功
            List<String> rowErrorMsgList = null; //行记录错误信息
            for(int i = 0; i < row.length; i++){
                Object columnValue = row[i]; //获取列值
                boolean requiredFlag = validField(rowTitles, i);//校验字段是否必输
                String errorInfo = null;
                if(!Objects.isNull(columnValue) && !"".equals(columnValue)){
                    if(i < mediaExportCommonFieldTemp.size()){ //媒体公共字段
                        errorInfo = setReplaceMediaCommonValue(primaryKey, currentMediaMap, currentMediaTemp, rowTitles, i, plateId, columnValue, mediaExportCommonFieldTemp); //设置媒体公共值
                    }else {
                        errorInfo = setReplaceMediaExtendMap(currentMediaTemp.get("id"),mediaExtendFields.get(i - commonColumnNum),columnValue,mediaExtendTemp); //媒体对于扩展字段，每个字段对应一个扩展对象
                    }
                }else{
                    if(requiredFlag){ //必输
                        errorInfo = "不能为空";
                    }
                    if(i >= mediaExportCommonFieldTemp.size()){  //扩展字段，为空值也记录
                        MediaExtendAudit mediaExtendAudit = new MediaExtendAudit();
                        mediaExtendAudit.setMediaId(Integer.parseInt(String.valueOf(currentMediaTemp.get("id"))));
                        mediaExtendAudit.setCell(mediaExtendFields.get(i - commonColumnNum).getCellCode());
                        mediaExtendAudit.setCellName(mediaExtendFields.get(i - commonColumnNum).getCellName());
                        mediaExtendAudit.setType(mediaExtendFields.get(i - commonColumnNum).getType());
                        mediaExtendAudit.setCellValue((String) columnValue);
                        mediaExtendAudit.setCellValueText((String) columnValue);
                        mediaExtendTemp.add(mediaExtendAudit);
                    }
                }
                if(StringUtils.isNotEmpty(errorInfo)){
                    if(CollectionUtils.isEmpty(rowErrorMsgList)){
                        rowErrorMsgList = new ArrayList<>();
                    }
                    if(rowTitles.get(i).matches("^\\*.+\\*$")){ //如果是必输字段，则去除开头和结尾的*
                        rowErrorMsgList.add(rowTitles.get(i).substring(1,rowTitles.get(i).length()-1) + errorInfo);
                    }else{
                        rowErrorMsgList.add(rowTitles.get(i) + errorInfo);
                    }
                    isValidSuccess = false;
                }
            }
            if(!isValidSuccess){  //数据校验不成功继续下一个媒体
                row = Arrays.copyOf(row, row.length + 1);
                row[row.length - 1] = String.valueOf(rowErrorMsgList);
                validErrorMedia.add(row); //缓存校验未通过数据
                continue;
            }
//            existsMediaMap.put(String.valueOf(currentMediaTemp.get("name")),0); //媒体校验成功，当成已存在的媒体，下面出现重名媒体则不会入库
            if(standarPlatformFlag != null && standarPlatformFlag == 1){
                currentMediaTemp.put("state",1);
            }else {
                currentMediaTemp.put("state",0);
            }
            setReplaceMediaDefaultValue(currentMediaTemp,user, currentDate); //设置媒体默认值
            validSuccessMedia.add(currentMediaTemp);   //缓存校验成功媒体
            validSuccessMediaExtendList.addAll(mediaExtendTemp); //缓存校验成功媒体的扩展数据
        }

        //先判断是否有错误信息，有提示错误，数据不入库
        int errorSize = validErrorMedia.size();
        if (errorSize > 0) {
            rowTitles.add("失败原因"); // 添加导入失败原因列；
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("templateName",plateName+"导入失败内容");
            errorMap.put("rowTitles",rowTitles);
            errorMap.put("exportData",validErrorMedia);
            return errorMap;
        }

        //记录异动前的媒体信息
        List<Media1> oldMediaList = mediaAuditMapper.listMediaDetailByIds(mediaIds);

        //处理成功媒体
        int mediaSize = validSuccessMedia.size();
        if (mediaSize > 0) {
            int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
            int insertTimes = mediaSize % subLength == 0 ? mediaSize / subLength : mediaSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
            if (insertTimes > 1) {
                List<Map<String, Object>> insertData;
                for (int i = 0; i < insertTimes; i++) {
                    insertData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < mediaSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                        insertData.add(validSuccessMedia.get(j));
                    }
                    mediaAuditMapper.batchUpdateMedia(insertData);
                }
            } else {
                mediaAuditMapper.batchUpdateMedia(validSuccessMedia);
            }
        }
        validSuccessMedia.clear();
        validSuccessMedia = null; //对象已使用完毕，对其进行释放

        // 处理校验成功待插入的数据；
        int mediaExtendSize = validSuccessMediaExtendList.size();
        if(mediaExtendSize > 0){
            int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
            int insertTimes = mediaExtendSize % subLength == 0 ? mediaExtendSize / subLength : mediaExtendSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
            if (insertTimes > 1) {
                List<MediaExtendAudit> insertData;
                for (int i = 0; i < insertTimes; i++) {
                    insertData = new ArrayList<>();
                    for (int j = i * subLength; j < (i + 1) * subLength && j < mediaExtendSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                        insertData.add(validSuccessMediaExtendList.get(j));
                    }
                    mediaExtendAuditMapper.updateBatchMoreMedia(insertData);
                }
            } else {
                mediaExtendAuditMapper.updateBatchMoreMedia(validSuccessMediaExtendList);
            }
        }
        validSuccessMediaExtendList.clear();
        validSuccessMediaExtendList = null; //对象已使用完毕，对其进行释放

        //如果是标准媒体，就不需要审核
        if(standarPlatformFlag != null && standarPlatformFlag == 1){
            batchCopyMediaAuditBaseInfo2Media(mediaIds);
        }else {
            //更新媒体是删除字段，当媒体修改后，先不直接修改state字段为0，先用这个字段代替，方便媒体驳回后返回之前状态使用
            media1Mapper.batchUpdateMediaIsDelete(mediaIds,1,user.getId());
        }

        //处理媒体异动，增加异常捕获，使其不影响正常操作
        try{
            //媒体新增成功，才能进行媒体异动 和 供应商异动的操作
            if(CollectionUtils.isNotEmpty(mediaIds)){
                List<Media1> newMediaList = mediaAuditMapper.listMediaDetailByIds(mediaIds);
                List<MediaChange> mediaChangeList = mediaChangeHandler(newMediaList, oldMediaList, user);
                if(CollectionUtils.isNotEmpty(mediaChangeList)){
                    mediaChangeMapper.saveBatch(mediaChangeList);
                }
            }
        }catch (Exception e){
            log.error("【媒体替换】媒体异动记录异常: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 设置公共字段值
     * @param currentMediaMap 当前导入替换的媒体
     * @param currentMediaTemp 缓存的当前媒体对象
     * @param rowTitles 导入模板列标题
     * @param columnIndex 当前列下标
     * @param plateId 媒体板块类型
     * @param columnValue 当前列值
     */
    private String setReplaceMediaCommonValue(String primaryKey, Map<String,MediaAudit> currentMediaMap,Map<String,Object> currentMediaTemp, List<String> rowTitles, int columnIndex, int plateId, Object columnValue, List<String> mediaExportCommonFieldTemp){
        String flag = null;
        if("id".equals(mediaExportCommonFieldTemp.get(columnIndex))){
            if(currentMediaMap.get(String.valueOf(columnValue)) == null){
                flag = "填写错误，对应媒体数据不存在";
            } else {
                if(!AppUtil.getUser().getId().equals(currentMediaMap.get(String.valueOf(columnValue)).getUserId())){
                    flag = "所属媒体责任人不是您，您不可以操作媒体数据";
                }
            }
        }
        if(primaryKey.equals(mediaExportCommonFieldTemp.get(columnIndex))){ //名称不能更改
            if(currentMediaMap.get(currentMediaTemp.get("id")) != null){
                if("mediaContentId".equals(primaryKey)){
                    if(!String.valueOf(columnValue).equals(currentMediaMap.get(currentMediaTemp.get("id")).getMediaContentId())){
                        flag = "不支持替换";
                    }
                }else {
                    if(!String.valueOf(columnValue).equals(currentMediaMap.get(currentMediaTemp.get("id")).getName())){
                        flag = "不支持替换";
                    }
                }
            }
        }
        if("supplierId".equals(mediaExportCommonFieldTemp.get(columnIndex))){
            supplierNameMap = getSupplierNameMap();
            currentMediaTemp.put("supplierName",columnValue);
            columnValue = supplierNameMap.get(currentMediaTemp.get(mediaExportCommonFieldTemp.get(columnIndex-1))+"*"+columnValue);
            if(columnValue == null && supplierNameMap.size() > 0){
                flag = "该媒体板块不存在";
            }else{
                currentMediaTemp.put(mediaExportCommonFieldTemp.get(columnIndex),columnValue);
            }
        }else if("discount".equals(mediaExportCommonFieldTemp.get(columnIndex))){
            Integer discount = Integer.parseInt(String.valueOf(columnValue));
            columnValue = (discount == null || discount <= 0) ? 100 : discount;
            currentMediaTemp.put(mediaExportCommonFieldTemp.get(columnIndex),columnValue);
        }
        else{
            currentMediaTemp.put(mediaExportCommonFieldTemp.get(columnIndex),columnValue);
        }
        return flag;
    }

    /**
     * 设置媒体扩展
     * @param mediaId 当前媒体ID
     * @param extendField 媒体表单
     * @param columnValue 字段对应值，Excel中单元格值
     * @param mediaExtendTemp 当前媒体对应的扩展字段
     */
    private String setReplaceMediaExtendMap(Object mediaId, MediaForm1 extendField, Object columnValue, List<MediaExtendAudit> mediaExtendTemp){
        String errorInfo = null;
        MediaExtendAudit mediaExtendAudit = new MediaExtendAudit();
        mediaExtendAudit.setMediaId(Integer.parseInt(String.valueOf(mediaId)));
        mediaExtendAudit.setCell(extendField.getCellCode());
        mediaExtendAudit.setCellName(extendField.getCellName());
        mediaExtendAudit.setType(extendField.getType());
        if("select".equals(extendField.getType()) || "radio".equals(extendField.getType()) || "checkbox".equals(extendField.getType())){
            setOtherFieldMap(extendField); //设置下拉列表、单选框、复选框值列表
            //赋值
            Map<String,String> currentFieldMap = otherFieldMap.get(extendField.getCellCode());
            String columnValueText = "";
            if(currentFieldMap != null && currentFieldMap.size() > 0){  //如果存在列表值,说明页面限制了，则获取，否则没限制，随便用户输入
                columnValueText = String.valueOf(columnValue); //单选框、复选框、下拉列表中文描述值
                if("checkbox".equals(extendField.getType()) && columnValueText.indexOf(",") != -1){ //如果是复选框，并且有多个值
                    String [] columnTexts = columnValueText.split(",");
                    List<String> columnValueTemp = new ArrayList<>();
                    for(String text : columnTexts){
                        columnValueTemp.add(currentFieldMap.get(text));
                    }
                    columnValue = StringUtils.join(columnValueTemp,",");
                }else{
                    columnValue = currentFieldMap.get(columnValue);
                }
            }

            if(columnValue == null && currentFieldMap.size() > 0){
                errorInfo = "值错误";
            }else{
                mediaExtendAudit.setCellValue(String.valueOf(columnValue));
                mediaExtendAudit.setCellValueText(columnValueText);
            }
        }else if("price".equals(extendField.getType())){
            if(String.valueOf(columnValue).matches("^(\\d+)(\\.\\d+)?$")){
                mediaExtendAudit.setCellValue((String) columnValue);
            }else {
                errorInfo = "格式错误或金额小于0";
            }
        }else{
            mediaExtendAudit.setCellValue((String) columnValue);
        }
        mediaExtendTemp.add(mediaExtendAudit);
        return errorInfo;
    }

    /**
     * 设置替换媒体默认值
     */
    private void setReplaceMediaDefaultValue(Map media, User user, Date currentDate){
        media.put("updatedId",user.getId());
        media.put("updateDate",currentDate);
    }

    @Override
    @Transactional
    public String transfer() {
            long startTime = System.currentTimeMillis();
            String result = "";
            Map<Integer, Map<String, MediaForm1>> mediaFormMap = mediaForm1Service.getAllMediaForm();//获取所有表单数据
            Map<Integer, Map<String, String>> mediaFieldRelateMap = mediaForm1Service.getFormRelete(); //媒体表字段对应关系
            List<Media> mediaList = mediaAuditMapper.listAllOldMedia(); // 获取所有媒体列表
            result += "媒体总数："+mediaList.size();
            //1、媒体扩展字段迁移
            List<MediaExtendAudit> mediaExtendAuditList = new ArrayList<>(); //待迁移的扩展字段
//        List<MediaSupplierRelateAudit> mediaSupplierRelateAuditList = new ArrayList<>(); //待迁移的媒体供应商关系
            List<MediaSupplierPriceAudit> mediaSupplierPriceAuditList = new ArrayList<>(); //待迁移的媒体供应商价格
            int count = 0;
            try{
                for (Media media : mediaList){
                    count++;
                    Map<String, MediaForm1> mediaForm = mediaFormMap.get(media.getmType());
                    Map<String, String> mediaFieldRelate = mediaFieldRelateMap.get(media.getmType());
                    int cellIndex = 0;
                    for(String cell : mediaForm.keySet()){
                        cellIndex ++ ;
                        //媒体扩展字段
                        //关系表统一规则ID，媒体ID + 固定值 + 扩展字段序号，t_media_info迁移同样规则，不然驳回报错，主键ID不对应
                        int id = Integer.parseInt(media.getId() + "0" + cellIndex);
                        MediaExtendAudit mediaExtendAudit = new MediaExtendAudit();
                        mediaExtendAudit.setId(id);
                        mediaExtendAudit.setMediaId(media.getId());
                        mediaExtendAudit.setCell(mediaForm.get(cell).getCellCode());
                        mediaExtendAudit.setCellName(mediaForm.get(cell).getCellName());
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
                                mediaExtendAudit.setCellValue(value);
                                mediaExtendAudit.setCellValueText(cellText);
                            }else{
                                mediaExtendAudit.setCellValue("");
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
                                    mediaExtendAudit.setCellValue(StringUtils.join(arr,","));
                                    mediaExtendAudit.setCellValueText(StringUtils.join(texts,","));
                                }else {
                                    mediaExtendAudit.setCellValue("");
                                }
                            }
                        }else if("price".equals(mediaForm.get(cell).getType())){
                            value = StringUtils.isEmpty(value) ? "0" : value;
                            mediaExtendAudit.setCellValue(value);
                        }else{
                            mediaExtendAudit.setCellValue(value);
                        }
                        mediaExtendAudit.setType(mediaForm.get(cell).getType());
                        mediaExtendAudit.setDbType(mediaForm.get(cell).getType());
                        mediaExtendAuditList.add(mediaExtendAudit);
                        //媒体价格字段
                        if("price".equals(mediaForm.get(cell).getType())){
                            MediaSupplierPriceAudit mediaSupplierPriceAudit = new MediaSupplierPriceAudit();
                            mediaSupplierPriceAudit.setId(id);
                            mediaSupplierPriceAudit.setMediaSupplierRelateId(media.getId()); //迁移时，关系默认成媒体主键ID
                            mediaSupplierPriceAudit.setCell(mediaForm.get(cell).getCellCode());
                            mediaSupplierPriceAudit.setCellName(mediaForm.get(cell).getCellName());
                            BigDecimal priceValue = StringUtils.isEmpty(value) ? new BigDecimal(0) : new BigDecimal(value);
                            mediaSupplierPriceAudit.setCellValue(priceValue.toPlainString());
                            mediaSupplierPriceAuditList.add(mediaSupplierPriceAudit);
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
                }

                // 处理待插入的数据；
                int extendSize = mediaExtendAuditList.size();
                if (extendSize > 0) {
                    int subLength = 100;  // 定义需要进行分割的尺寸，每次批量处理媒体数
                    int insertTimes = extendSize % subLength == 0 ? extendSize / subLength : extendSize / subLength + 1; // 计算需要插入的次数，100条插入一次；
                    if (insertTimes > 1) {
                        List<MediaExtendAudit> insertData;
                        for (int i = 0; i < insertTimes; i++) {
                            insertData = new ArrayList<>();
                            for (int j = i * subLength; j < (i + 1) * subLength && j < extendSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                                insertData.add(mediaExtendAuditList.get(j));
                            }
                            mediaExtendAuditMapper.saveBatchForId(insertData);
                        }
                    } else {
                        mediaExtendAuditMapper.saveBatchForId(mediaExtendAuditList);
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
                        List<MediaSupplierPriceAudit> insertData;
                        for (int i = 0; i < insertTimes; i++) {
                            insertData = new ArrayList<>();
                            for (int j = i * subLength; j < (i + 1) * subLength && j < supplierPriceSize; j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                                insertData.add(mediaSupplierPriceAuditList.get(j));
                            }
                            mediaSupplierPriceAuditMapper.saveBatchForId(insertData);
                        }
                    } else {
                        mediaSupplierPriceAuditMapper.saveBatchForId(mediaSupplierPriceAuditList);
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
    private Object getFieldValueByName(Media media, String fieldName){
        try {
            Field field = media.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);//设置对象的访问权限，保证对private的属性的访问
            return  field.get(media);
        } catch (Exception e) {
            return new QinFeiException(1002,"老媒体实例中没有该【"+fieldName+"】属性名");
        }
    }

    /**
     * 媒体异动处理
     * @param newMediaList 修改后媒体列表
     * @param oldMediaList 修改前媒体列表
     * @param user 当前用户
     * @return 媒体异动列表
     */
    private List<MediaChange> mediaChangeHandler(List<Media1> newMediaList, List<Media1> oldMediaList, User user){
        //如果新媒体数据不存在，则直接返回
        if(CollectionUtils.isEmpty(newMediaList)){
            return null;
        }
        Map<Integer, Media1> mediaAuditMap = new HashMap<>();//缓存媒体新数据
        for(Media1 mediaAudit : newMediaList){
            mediaAuditMap.put(mediaAudit.getId(), mediaAudit);
        }
        Map<Integer, Media1> media1Map = new HashMap<>();//缓存媒体老数据
        if(CollectionUtils.isNotEmpty(oldMediaList)){
            for(Media1 media1 : oldMediaList){
                media1Map.put(media1.getId(), media1);
            }
        }
        //解析异动字段
        List<MediaChange> mediaChangeList = new ArrayList<>();
        for(Integer mediaId : mediaAuditMap.keySet()){
            Media1 mediaAudit = mediaAuditMap.get(mediaId);
            MediaChange mediaChange = new MediaChange();
            mediaChange.setPlateId(mediaAudit.getPlateId());
            Map<String, Map<String, Object>> result = getChangeField(mediaChange, mediaAudit, media1Map.get(mediaId));
            if(result != null){
                mediaChange.setMediaId(mediaAudit.getId());
                mediaChange.setMediaName(StringUtils.isNotEmpty(mediaAudit.getName()) ? mediaAudit.getName() : mediaAudit.getMediaContentId());
                mediaChange.setUserId(mediaAudit.getUpdatedId());
                mediaChange.setCompanyCode(mediaAudit.getCompanyCode());
                mediaChange.setCreateDate(new Date());
                mediaChange.setAuditUserId(user.getId());
                mediaChange.setAuditUserName(user.getName());
                mediaChange.setChangeContent(JSON.toJSONString(result));
                mediaChangeList.add(mediaChange);
            }
        }
        return mediaChangeList;
    }

    //媒体供应商异动处理
    private List<MediaSupplierChange> mediaSupplierChangeHandler(List<MediaAudit> mediaAuditList, List<MediaSupplierRelate> newMediaRelateList, List<MediaSupplierRelate> oldMediaRelateList, User user){
        //如果新媒体供应商数据不存在，则直接返回
        if(CollectionUtils.isEmpty(newMediaRelateList)){
            return null;
        }
        Map<Integer, MediaAudit> mediaAuditMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(mediaAuditList)){
            mediaAuditList.forEach(mediaAudit -> {
                mediaAuditMap.put(mediaAudit.getId(), mediaAudit);
            });
        }
        Map<Integer, MediaSupplierRelate> mediaSupplierRelateAuditMap = new HashMap<>();//缓存媒体供应商新数据
        for(MediaSupplierRelate mediaSupplierRelateAudit : newMediaRelateList){
            mediaSupplierRelateAuditMap.put(mediaSupplierRelateAudit.getId(), mediaSupplierRelateAudit);
        }
        Map<Integer, MediaSupplierRelate> mediaSupplierRelateMap = new HashMap<>();//缓存媒体老数据
        if(CollectionUtils.isNotEmpty(oldMediaRelateList)){
            for(MediaSupplierRelate mediaSupplierRelate : oldMediaRelateList){
                mediaSupplierRelateMap.put(mediaSupplierRelate.getId(), mediaSupplierRelate);
            }
        }
        //解析异动字段
        List<MediaSupplierChange> mediaSupplierChangeList = new ArrayList<>();
        for(Integer relateId : mediaSupplierRelateAuditMap.keySet()){
            MediaSupplierRelate mediaSupplierRelate = mediaSupplierRelateAuditMap.get(relateId);
            MediaSupplierChange mediaSupplierChange = new MediaSupplierChange();
            Map<String, Map<String, Object>> result = getSupplierChangeField(mediaSupplierChange, mediaSupplierRelate, mediaSupplierRelateMap.get(relateId));
            if(result != null){
                MediaAudit mediaAudit = mediaAuditMap.get(mediaSupplierRelate.getMediaId());
                mediaSupplierChange.setPlateId(mediaAudit.getPlateId());
                mediaSupplierChange.setMediaId(mediaAudit.getId());
                mediaSupplierChange.setMediaName(StringUtils.isNotEmpty(mediaAudit.getName()) ? mediaAudit.getName() : mediaAudit.getMediaContentId());
                mediaSupplierChange.setMediaSupplierRelateId(relateId);
                mediaSupplierChange.setSupplierId(mediaSupplierRelate.getSupplierId());
                mediaSupplierChange.setUserId(mediaSupplierRelate.getUpdateId());
                mediaSupplierChange.setCompanyCode(mediaAudit.getCompanyCode());
                mediaSupplierChange.setCreateDate(new Date());
                mediaSupplierChange.setAuditUserId(user.getId());
                mediaSupplierChange.setAuditUserName(user.getName());
                mediaSupplierChange.setChangeContent(JSON.toJSONString(result));
                mediaSupplierChangeList.add(mediaSupplierChange);
            }
        }
        return mediaSupplierChangeList;
    }

    /**
     * 获取媒体改变数据，数据格式如下:recover代表要恢复的字段数据，change代表异动详情展示的字段
     * {
     *     recover:{
     *         baseData:{},
     *         extendList:[
     *             {cell:'', cellName:'', cellValue:'', cellValueText:''}
     *         ]
     *     },
     *     change:{
     *         fieldList:[
     *             {cell:'', cellName:'', oldCellValue:'', oldCellText:'', newCellValue:'', newCellText:''}
     * 	    ],
     *         opDesc:'',
     *         op:''
     *     },
     *     export:{
     *         titleList:[],
     *         dataList:[]
     *     }
     * }
     *
     *  @return
     */
    private Map<String, Map<String, Object>> getChangeField(MediaChange mediaChange, Media1 mediaAudit, Media1 media1){
        Map<String, Map<String, Object>> result = new HashMap<>();

        boolean changeFlag = false;//默认没有改变

        result.put("recover", new HashMap<>());
        result.put("change", new HashMap<>());
        //获取媒体基本信息改变值
        if(media1 != null){
            media1.setIsDelete(0);//由于待审核状态，会把媒体该字段设置成1，所以重置
        }
        //如果该值有改变 且 改变后为1时，说明是删除媒体
        if(media1 == null){
            result.get("change").put("opDesc", "新增媒体");
            result.get("change").put("op", "add");
        }else {
            result.get("change").put("opDesc", "修改媒体");
            result.get("change").put("op", "update");
        }
        List<Map<String, String>> extendList = new ArrayList<>();//待恢复的媒体扩展字段列表
        List<Map<String, String>> fieldList = new ArrayList<>();//改变的媒体扩展字段列表
        List<Map<String, String>> baseChangeList = ObjectFieldCompare.compare(media1, mediaAudit);
        if(CollectionUtils.isNotEmpty(baseChangeList)){
            changeFlag = true;//发生改变
            for(Map<String, String> baseChange : baseChangeList){
                //如果该值有改变 且 改变后为1时，说明是删除媒体
                if("isDelete".equals(baseChange.get("cell")) && "1".equals(baseChange.get("newCellValue"))){
                    result.get("change").put("opDesc", "删除媒体");
                    result.get("change").put("op", "delete");
                }
                /*if("mediaContentId".equals(baseChange.get("cell")) && CollectionUtils.isNotEmpty(containIdPlateCommonFieldMap.get(mediaAudit.getPlateId()))){
                    String cellName = containIdPlateCommonFieldMap.get(mediaAudit.getPlateId()).get(2);
                    baseChange.put("cellName", cellName.substring(1,cellName.length()-1));
                }*/
                if("enabled".equals(baseChange.get("cell"))){
                    if("1".equals(baseChange.get("oldCellValue"))){
                        baseChange.put("oldCellText", "停用");
                    }else if("0".equals(baseChange.get("oldCellValue"))){
                        baseChange.put("oldCellText", "启用");
                    }else {
                        baseChange.put("oldCellText", "");
                    }
                    if("1".equals(baseChange.get("newCellValue"))){
                        baseChange.put("newCellText", "停用");
                    }else if("0".equals(baseChange.get("newCellValue"))){
                        baseChange.put("newCellText", "启用");
                    }else {
                        baseChange.put("newCellText", "");
                    }
                }
                if("isDelete".equals(baseChange.get("cell"))){
                    if("1".equals(baseChange.get("oldCellValue"))){
                        baseChange.put("oldCellText", "删除");
                    }else if("0".equals(baseChange.get("oldCellValue"))){
                        baseChange.put("oldCellText", "正常");
                    }else {
                        baseChange.put("oldCellText", "");
                    }
                    if("1".equals(baseChange.get("newCellValue"))){
                        baseChange.put("newCellText", "删除");
                    }else if("0".equals(baseChange.get("newCellValue"))){
                        baseChange.put("newCellText", "正常");
                    }else {
                        baseChange.put("newCellText", "");
                    }
                }
                if(result.get("recover").get("baseData") == null){
                    result.get("recover").put("baseData", new HashMap<>());
                }
                ((Map<String, String>)result.get("recover").get("baseData")).put(baseChange.get("cell"), baseChange.get("oldCellValue"));//记录恢复更新的数据
                fieldList.add(baseChange);//添加改变的字段
            }
        }

        List<MediaExtend> newMediaExtendList = mediaAudit != null ? mediaAudit.getMediaExtends() : new ArrayList<>();
        List<MediaExtend> oldMediaExtendList = media1 != null ? media1.getMediaExtends() : null;//如果是新增的媒体，该列表为空

        for(MediaExtend newMediaExtend : newMediaExtendList){
            MediaExtend oldMediaExtend = null;
            //媒体价格字段不参与比较
//            if("price".equals(newMediaExtend.getType())){
//                continue;
//            }
            if(CollectionUtils.isNotEmpty(oldMediaExtendList)){
                for(MediaExtend mediaExtend : oldMediaExtendList){
                    if(newMediaExtend.getCell().equals(mediaExtend.getCell())){
                        oldMediaExtend = mediaExtend;
                        break;
                    }
                }
            }
            List<Map<String, String>> tmpList = ObjectFieldCompare.compare(oldMediaExtend, newMediaExtend);//获取新旧值
            //如果不为空，说明发生了变化
            if(tmpList != null){
                Map<String, String> tmp = tmpList.get(0);
                tmp.put("cell", newMediaExtend.getCell());
                tmp.put("cellName", newMediaExtend.getCellName());
                tmp.put("oldCellText", oldMediaExtend != null ? (oldMediaExtend.getCellValueText() == null ? "" : oldMediaExtend.getCellValueText()) : "");
                tmp.put("newCellText", newMediaExtend != null ? (newMediaExtend.getCellValueText() == null ? "" : newMediaExtend.getCellValueText()) : "");
                fieldList.add(tmp);//添加改变的字段
                Map<String, String> extend = new HashMap<>();
                extend.put("cell", tmp.get("cell"));
                extend.put("cellName", tmp.get("cellName"));
                extend.put("cellValue", tmp.get("oldCellValue"));
                extend.put("cellValueText", tmp.get("oldCellText"));
                extendList.add(extend);//记录恢复更新的数据
            }
        }
        if(CollectionUtils.isNotEmpty(extendList)){
            changeFlag = true;//发生改变
            result.get("recover").put("extendList", extendList);
        }
        if(CollectionUtils.isNotEmpty(fieldList)){
            changeFlag = true;//发生改变
            result.get("change").put("fieldList", fieldList);

            //构建导出数据
            buildExportChangeData(mediaChange.getPlateId(), result, fieldList, false);
        }
        //如果发生改变，则记录，否则不记录到数据库
        if(changeFlag){
            return result;
        }else {
            return null;
        }
    }

    /**
     * 获取媒体改变数据，数据格式如下:recover代表要恢复的字段数据，change代表异动详情展示的字段
     * {
     *     recover:{
     *         baseData:{},
     *         extendList:[
     *             {cell:'', cellName:'', cellValue:'', cellValueText:''}
     *         ]
     *     },
     *     change:{
     *         fieldList:[
     *             {cell:'', cellName:'', oldCellValue:'', oldCellText:'', newCellValue:'', newCellText:''}
     * 	    ],
     *         opDesc:'',
     *         op:''
     *     },
     *     export:{
     *         titleList:[],
     *         dataList:[]
     *     }
     * }
     *
     *  @return
     */
    private Map<String, Map<String, Object>> getSupplierChangeField(MediaSupplierChange mediaSupplierChange, MediaSupplierRelate newMediaSupplierRelate, MediaSupplierRelate oldMediaSupplierRelate){
        Map<String, Map<String, Object>> result = new HashMap<>();

        boolean changeFlag = false;//默认没有改变

        result.put("recover", new HashMap<>());
        result.put("change", new HashMap<>());
        //获取媒体基本信息改变值
        if(oldMediaSupplierRelate != null){
            oldMediaSupplierRelate.setIsDelete(0);//由于待审核状态，会把媒体该字段设置成1，所以重置
        }
        //如果该值有改变 且 改变后为1时，说明是删除媒体
        if(oldMediaSupplierRelate == null){
            result.get("change").put("opDesc", "新增媒体供应商");
            result.get("change").put("op", "add");
        }else {
            result.get("change").put("opDesc", "修改媒体供应商");
            result.get("change").put("op", "update");
        }
        List<Map<String, String>> extendList = new ArrayList<>();//待恢复的媒体扩展字段列表
        List<Map<String, String>> fieldList = new ArrayList<>();//改变的媒体扩展字段列表
        List<Map<String, String>> baseChangeList = ObjectFieldCompare.compare(oldMediaSupplierRelate, newMediaSupplierRelate);
        if(baseChangeList != null){
            changeFlag = true;//发生改变
            for(Map<String, String> baseChange : baseChangeList){
                //如果该值有改变 且 改变后为1时，说明是删除媒体
                if("isDelete".equals(baseChange.get("cell")) && "1".equals(baseChange.get("newCellValue"))){
                    result.get("change").put("opDesc", "删除媒体供应商");
                    result.get("change").put("op", "delete");
                }
                if("enabled".equals(baseChange.get("cell"))){
                    if("1".equals(baseChange.get("oldCellValue"))){
                        baseChange.put("oldCellText", "停用");
                    }else if("0".equals(baseChange.get("oldCellValue"))){
                        baseChange.put("oldCellText", "启用");
                    }else {
                        baseChange.put("oldCellText", "");
                    }
                    if("1".equals(baseChange.get("newCellValue"))){
                        baseChange.put("newCellText", "停用");
                    }else if("0".equals(baseChange.get("newCellValue"))){
                        baseChange.put("newCellText", "启用");
                    }else {
                        baseChange.put("newCellText", "");
                    }
                }
                if("isDelete".equals(baseChange.get("cell"))){
                    if("1".equals(baseChange.get("oldCellValue"))){
                        baseChange.put("oldCellText", "删除");
                    }else if("0".equals(baseChange.get("oldCellValue"))){
                        baseChange.put("oldCellText", "正常");
                    }else {
                        baseChange.put("oldCellText", "");
                    }
                    if("1".equals(baseChange.get("newCellValue"))){
                        baseChange.put("newCellText", "删除");
                    }else if("0".equals(baseChange.get("newCellValue"))){
                        baseChange.put("newCellText", "正常");
                    }else {
                        baseChange.put("newCellText", "");
                    }
                }
                if(result.get("recover").get("baseData") == null){
                    result.get("recover").put("baseData", new HashMap<>());
                }
                ((Map<String, String>)result.get("recover").get("baseData")).put(baseChange.get("cell"), baseChange.get("oldCellValue"));//记录恢复更新的数据
                fieldList.add(baseChange);//添加改变的字段
            }
        }

        List<MediaSupplierPrice> newMediaSupplierPriceList = newMediaSupplierRelate != null ? newMediaSupplierRelate.getMediaSupplierPriceList() : new ArrayList<>();
        List<MediaSupplierPrice> oldMediaSupplierPriceList = oldMediaSupplierRelate != null ? oldMediaSupplierRelate.getMediaSupplierPriceList() : null;//如果是新增的媒体，该列表为空

        for(MediaSupplierPrice newMediaSupplierPrice : newMediaSupplierPriceList){
            MediaSupplierPrice oldMediaSupplierPrice = null;

            if(CollectionUtils.isNotEmpty(oldMediaSupplierPriceList)){
                for(MediaSupplierPrice mediaSupplierPrice : oldMediaSupplierPriceList){
                    if(newMediaSupplierPrice.getCell().equals(mediaSupplierPrice.getCell())){
                        oldMediaSupplierPrice = mediaSupplierPrice;
                        break;
                    }
                }
            }
            List<Map<String, String>> tmpList = ObjectFieldCompare.compare(oldMediaSupplierPrice, newMediaSupplierPrice);//获取新旧值
            //如果不为空，说明发生了变化
            if(tmpList != null){
                Map<String, String> tmp = tmpList.get(0);
                tmp.put("cell", newMediaSupplierPrice.getCell());
                tmp.put("cellName", newMediaSupplierPrice.getCellName());
                tmp.put("oldCellText", oldMediaSupplierPrice != null ? (oldMediaSupplierPrice.getCellValueText() == null ? "" : oldMediaSupplierPrice.getCellValueText()) : "");
                tmp.put("newCellText", newMediaSupplierPrice != null ? (newMediaSupplierPrice.getCellValueText() == null ? "" : newMediaSupplierPrice.getCellValueText()) : "");
                fieldList.add(tmp);//添加改变的字段
                Map<String, String> extend = new HashMap<>();
                extend.put("cell", tmp.get("cell"));
                extend.put("cellName", tmp.get("cellName"));
                extend.put("cellValue", tmp.get("oldCellValue"));
                extend.put("cellValueText", tmp.get("oldCellText"));
                extendList.add(extend);//记录恢复更新的数据
            }
        }
        if(CollectionUtils.isNotEmpty(extendList)){
            changeFlag = true;//发生改变
            result.get("recover").put("extendList", extendList);
        }
        if(CollectionUtils.isNotEmpty(fieldList)){
            changeFlag = true;//发生改变
            result.get("change").put("fieldList", fieldList);

            //构建导出数据
            buildExportChangeData(mediaSupplierChange.getPlateId(), result, fieldList, true);
        }
        //如果发生改变，则记录，否则不记录到数据库
        if(changeFlag){
            return result;
        }else {
            return null;
        }
    }

    //构建导出数据
    private void buildExportChangeData(Integer plateId, Map<String, Map<String, Object>> result, List<Map<String, String>> fieldList, boolean isRelate){
        List<String> rowFieldList = new ArrayList<>();//导出标题字段
        List<String> rowTitleList = new ArrayList<>();//导出文件标题
        List<String> rowContentList = new ArrayList<>();//导出文件内容
        List<MediaForm1> extendFormList = mediaForm1Service.listMediaFormByPlateId(plateId);
        //如果是媒体
        if(!isRelate){
            rowFieldList.addAll(Arrays.asList("userId","name","mediaContentId","link","discount","remarks"));
            rowFieldList.add("enabled");
            rowFieldList.add("isDelete");
            rowTitleList.addAll(Arrays.asList("责任人","媒体名称","唯一标识","案例链接","折扣率","备注"));

            rowTitleList.add("是否可用");
            rowTitleList.add("是否删除");
        }else {
            rowFieldList.add("enabled");
            rowFieldList.add("isDelete");
            rowTitleList.add("是否可用");
            rowTitleList.add("是否删除");
        }
        if(CollectionUtils.isNotEmpty(extendFormList)){
            List<String> rowFieldTmpList = new ArrayList<>();//导出标题字段
            List<String> rowTitleTmpList = new ArrayList<>();//导出文件标题
            extendFormList.forEach(mediaForm1 -> {
                //如果是媒体供应商关系，则获取仅媒体的价格 和  仅供应商的， 否则媒体只获取仅媒体的且排除价格
                if(isRelate){
                    if(mediaForm1.getExtendFlag() == 1 || "price".equals(mediaForm1.getType())){
                        rowFieldList.add(mediaForm1.getCellCode());
                        rowTitleList.add(mediaForm1.getCellName());
                    }
                }else {
                    if(mediaForm1.getExtendFlag() != 1){
                        if(!"price".equals(mediaForm1.getType())){
                            rowFieldList.add(mediaForm1.getCellCode());
                            rowTitleList.add(mediaForm1.getCellName());
                        }else {
                            rowFieldTmpList.add(mediaForm1.getCellCode());
                            rowTitleTmpList.add(mediaForm1.getCellName());
                        }
                    }
                }
            });
            rowFieldList.addAll(rowFieldTmpList);
            rowTitleList.addAll(rowTitleTmpList);
        }
        Map<String, Map<String, String>> fieldMap = new HashMap<>();
        for(Map<String, String> map : fieldList){
            fieldMap.put(map.get("cell"), map);
        }
        for(String cellCode : rowFieldList){
            //如果包含，说明该字段有异动，否则没有
            if(fieldMap.containsKey(cellCode)){
                String oldCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellText")) ? (StringUtils.isEmpty(fieldMap.get(cellCode).get("oldCellValue")) ? "空" : fieldMap.get(cellCode).get("oldCellValue")) : fieldMap.get(cellCode).get("oldCellText");
                String newCellValue = StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellText")) ? (StringUtils.isEmpty(fieldMap.get(cellCode).get("newCellValue")) ? "空" : fieldMap.get(cellCode).get("newCellValue")) : fieldMap.get(cellCode).get("newCellText");
                rowContentList.add(String.format("%s->%s", oldCellValue, newCellValue));
            }else {
                rowContentList.add("");
            }
        }
        Map<String, Object> export = new HashMap<>();
        export.put("titleList", rowTitleList);
        export.put("dataList", rowContentList);
        result.put("export", export);
    }

    /**
     * 消息推送；
     *
     * @param media：媒体信息；
     * @param agree：是否同意，true为审核通过，false为拒绝，null为删除；
     */
    private void sendMessage(MediaAudit media, Boolean agree) {
        User user = AppUtil.getUser();
        String operateUser = user.getName();
        String title = media.getName();
        Integer userId = user.getId();
        User creator = media.getUser();
        Integer acceptId = creator.getId();
        // 拼接消息内容；
        String subject;
        String content;
        if (agree == null) {
            subject = "媒体信息["+title+"]审核通过。";
            content = String.format("[媒体审核]很遗憾，你录入的媒体信息[%s]已被[%s]删除。", title, operateUser);
        } else {
            if (agree) {
                subject = "媒体信息["+title+"]审核通过。";
                content = String.format("[媒体审核]恭喜你，你录入的媒体信息[%s]已经由[%s]审核通过。", title, operateUser);
            } else {
                subject = "媒体信息["+title+"]已被驳回。";
                content = String.format("[媒体审核]很遗憾，你录入的媒体信息[%s]在[%s]处审核未通过。", title, operateUser);
            }
        }

        // 推送WebSocket消息；
        WSMessage message = new WSMessage();
        message.setReceiveUserId(acceptId + "");
        message.setReceiveName(creator.getName());
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
        newMessage.setAcceptDept(creator.getDeptId());
        newMessage.setAcceptWorker(acceptId);
        //消息分类
        newMessage.setParentType(3);//通知
        newMessage.setType(11);//媒体审核
        messageService.addMessage(newMessage);
    }

    /**
     * 批量操作的消息通知；
     *
     * @param mediaNames：媒体信息数组；
     * @param userIds：用户ID数组；
     * @param agree：是否同意，true为审核通过，false为拒绝，null为删除；
     */
    private void sendMessage(List<String> mediaNames, List<Integer> userIds, Boolean agree) {
        // 获取用户信息集合；
        Map<Integer, User> userMap = userService.listAllUserMap();
        int length = mediaNames.size();
        // 单条数据直接使用已有的方法；
        if (length == 1) {
            MediaAudit media1 = new MediaAudit();
            media1.setName(mediaNames.get(0));
            media1.setUser(userMap.get(userIds.get(0)));
            sendMessage(media1, agree);
        } else {
            // 获取登录用户；
            User user = AppUtil.getUser();
            String operateUser = user.getName();
            Integer userId = user.getId();
            String title;
            User creator;
            Integer acceptId;
            // 拼接消息内容；
            String subject;
            String content;
            // 推送WebSocket消息；
            WSMessage message;
            // 推送系统的消息；
            Message newMessage;
            // 批量插入；
            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                title = mediaNames.get(i);
                creator = userMap.get(userIds.get(i));
                acceptId = creator.getId();

                if (agree == null) {
                    subject = "媒体信息审核通过。";
                    content = String.format("[媒体审核]很遗憾，你录入的媒体信息[%s]已被[%s]删除。", title, operateUser);
                } else {
                    if (agree) {
                        subject = "媒体信息审核通过。";
                        content = String.format("[媒体审核]恭喜你，你录入的媒体信息[%s]已经由[%s]审核通过。", title, operateUser);
                    } else {
                        subject = "媒体信息已被驳回。";
                        content = String.format("[媒体审核]很遗憾，你录入的媒体信息[%s]在[%s]处审核未通过。", title, operateUser);
                    }
                }
                // 推送WebSocket消息；
                message = new WSMessage();
                message.setReceiveUserId(acceptId + "");
                message.setReceiveName(creator.getName());
                message.setSendName(operateUser);
                message.setSendUserId(userId + "");
                message.setSendUserImage(user.getImage());
                message.setContent(content);
                message.setSubject(subject);
                WebSocketServer.sendMessage(message);

                // 推送系统的消息；
                newMessage = new Message();
                String userImage = user.getImage();
                // 获取消息显示的图片；
                String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
                newMessage.setPic(pictureAddress);
                newMessage.setContent(content);
                newMessage.setInitiatorDept(user.getDeptId());
                newMessage.setInitiatorWorker(userId);
                newMessage.setAcceptDept(creator.getDeptId());
                newMessage.setAcceptWorker(acceptId);
                //消息分类
                newMessage.setParentType(3);//通知
                newMessage.setType(11);//媒体审核
                newMessage.setUrl(null);
                newMessage.setUrlName(null);
                messages.add(newMessage);
            }
            messageService.batchAddMessage(messages);
        }
    }

    //电话号码处理
    private String getPhone(String phone) {
        phone = StringUtils.isEmpty(phone) ? "" : phone;
        if (phone.length() >= 11) {
            String start = phone.length() > 11 ? "*****" : "****";
            phone = phone.substring(0, 3) + start + phone.substring(phone.length() - 4, phone.length());
        } else if (phone.length() >= 3) {
            phone = phone.substring(0, 1) + "***" + phone.substring(phone.length() - 1);
        } else {
            phone = "**";
        }
        return phone;
    }



    //媒体标准化媒体稿件媒体统一规则
    @Override
    public void updateArticle(){
        try{
            SlaveMediaReplace slaveMediaReplace = new SlaveMediaReplace();
            //查出所有 从媒体
            List<Media1> media1List = mediaAuditMapper.selectMediaList(onlineTime);
            if (CollectionUtils.isNotEmpty(media1List)){
                List<Integer> mediaIds = new ArrayList<>();
                for (Media1 m: media1List){
                    //mediaIds 中不重复
                    if (!mediaIds.contains(m.getMasterMediaId())){
                        //提出所有 从媒体的主媒体 id
                        mediaIds.add(m.getMasterMediaId());
                    }
                }
                List<Map<String,Object>> articleList = new ArrayList<>();
                List<Map<String,Object>> supplierList = new ArrayList<>();
                List<Map<String,Object>> supplierListAudit = new ArrayList<>();
                List<Media1> masterMediaList = new ArrayList<>();
                //为了防止主键ID太多了，分批次查询
                int subLength = 500;  // 定义需要进行分割的尺寸，每次批量处理媒体数。
                int insertTimes = mediaIds.size() % subLength == 0 ? mediaIds.size() / subLength : mediaIds.size() / subLength + 1; // 计算需要插入的次数，100条插入一次；
                if (insertTimes > 1) {
                    //如果主媒体id大于500条
                    for (int i = 0; i < insertTimes; i++) {
                        List<Integer> tempIds = new ArrayList<>();
                        for (int j = i * subLength; j < (i + 1) * subLength && j < mediaIds.size(); j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                            tempIds.add(mediaIds.get(j));
                        }
                        // 分媒体数量大于500条
                        //根据分媒体的master_media_id 查询主媒体名称
                        masterMediaList.addAll(mediaAuditMapper.selectMediaName(tempIds,onlineTime));
                    }
                } else {
                    //如果分媒体小于500条
                    masterMediaList.addAll(mediaAuditMapper.selectMediaName(mediaIds,onlineTime));
                }

                //缓存主媒体
                Map<Integer, Media1> media1Map = new HashMap<>();
                for(Media1 media1 : masterMediaList){
                    media1Map.put(media1.getId(), media1);
                }

                insertTimes = media1List.size() % subLength == 0 ? media1List.size() / subLength : media1List.size() / subLength + 1; // 计算需要插入的次数，100条插入一次；
                if (insertTimes > 1) {
                    for (int i = 0; i < insertTimes; i++) {
                        List<Media1> tempUpdateData = new ArrayList<>();
                        for (int j = i * subLength; j < (i + 1) * subLength && j < media1List.size(); j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                            tempUpdateData.add(media1List.get(j));
                        }
                        //获取稿件更新前的 稿件信息  根据从媒体id
                        articleList.addAll(mediaAuditMapper.selectArtList(tempUpdateData,onlineTime));

                        //获取媒体供应商  更新前的信息
                        supplierList.addAll(mediaAuditMapper.selectSupplierList(tempUpdateData,onlineTime));

                        //获取媒体供应商审核 更新前的信息
                        supplierListAudit.addAll(mediaAuditMapper.selectSupplierAuditList(tempUpdateData,onlineTime));
                    }
                } else {
                    //获取稿件更新前的 稿件信息  根据从媒体id
                    articleList = mediaAuditMapper.selectArtList(media1List,onlineTime);

                    //获取媒体供应商  更新前的信息
                    supplierList =mediaAuditMapper.selectSupplierList(media1List,onlineTime);

                    //获取媒体供应商审核 更新前的信息
                    supplierListAudit = mediaAuditMapper.selectSupplierAuditList(media1List,onlineTime);
                }

                List<Map<String,Object>> mediaRelateContent = new ArrayList<>();
                List<Map<String,Object>> mediaRelateAuditContent = new ArrayList<>();

                List<Map<String,Object>> artContentList = new ArrayList<>();
                //封装更新数据
                List<Map<String, Object>> updateDataList = new ArrayList<>();

                for (Media1 m: media1List){
                   if(media1Map.containsKey(m.getMasterMediaId())){
                       Map<String, Object> media = new HashMap<>();
                       Map<String,Object> artMap = new HashMap<>();
                       Map<String,Object> supMap = new HashMap<>();
                       Map<String,Object> supAuditMap = new HashMap<>();
                       // 查出该分媒体的主媒体的媒体供应商
                       List<Integer> masterSupplierList = mediaAuditMapper.masterSupplierList(m.getMasterMediaId(),onlineTime);

                       //查询出媒体供应商审核
                       List<Integer> masterSupplierAudit = mediaAuditMapper.masterSupplierListAudit(m.getMasterMediaId(),onlineTime);
                       media.put("oldMediaId", m.getId());
                       media.put("masterMediaId", m.getMasterMediaId());
                       media.put("masterMediaName", media1Map.containsKey(m.getMasterMediaId()) ? media1Map.get(m.getMasterMediaId()).getName() : m.getName());
                       updateDataList.add(media);
                       for (Map art : articleList){
                           if (art.get("mediaId").equals(m.getId())){
                               artMap.put("artId",art.get("id"));
                               artMap.put("oldMediaId",m.getId());
                               artMap.put("oldMediaName",m.getName());
                               artMap.put("newMediaId", m.getMasterMediaId());
                               artMap.put("newMediaName", media1Map.containsKey(m.getMasterMediaId()) ? media1Map.get(m.getMasterMediaId()).getName() : m.getName());
                               artContentList.add(artMap);
                           }
                       }
                       for (Map ms : supplierList){
                           if( ms.get("mediaId").equals(m.getId()) && masterSupplierList.contains(ms.get("supplierId"))){
//                               supplierList.remove(ms);
//                               media1List.remove(m);
                               break;
                           }
                           if (ms.get("mediaId").equals(m.getId())){
                               //更新媒体供应商
                               mediaAuditMapper.updateMediaSupplier(masterSupplierList,m.getId(),m.getMasterMediaId(),onlineTime);

                               supMap.put("relateId",ms.get("id"));
                               supMap.put("oldMediaId",m.getId());
                               supMap.put("newMediaId",m.getMasterMediaId());
                               mediaRelateContent.add(supMap);
                           }
                       }
                       for (Map ms : supplierListAudit){
                           if( ms.get("mediaId").equals(m.getId()) && masterSupplierList.contains(ms.get("supplierId"))){
//                               supplierList.remove(ms);
//                               media1List.remove(m);
                               break;
                           }

                           if (ms.get("mediaId").equals(m.getId())){
                               //更新媒体供应商审核
                               mediaAuditMapper.updateMediaSupplierAudit(masterSupplierAudit,m.getId(),m.getMasterMediaId(),onlineTime);

                               supAuditMap.put("relateId",ms.get("id"));
                               supAuditMap.put("oldMediaId",m.getId());
                               supAuditMap.put("newMediaId",m.getMasterMediaId());
                               mediaRelateAuditContent.add(supAuditMap);
                           }
                       }
                   }
                }
                String artcontent1= JSON.toJSONString(artContentList);
                String mediaRelateContent1 = JSON.toJSONString(mediaRelateContent);
                String mediaRelateContentAudit1 = JSON.toJSONString(mediaRelateAuditContent);
                if(!(artcontent1.equals("[]") && mediaRelateContent1.equals("[]") && mediaRelateContentAudit1.equals("[]"))){
                    slaveMediaReplace.setArtContent(artcontent1);
                    slaveMediaReplace.setMediaRelateContent(mediaRelateContent1);
                    slaveMediaReplace.setMediaRelateAuditContent(mediaRelateContentAudit1);
                    //保存更新媒体稿件 记录
                    mediaAuditMapper.updateSlaveMediaRelace(slaveMediaReplace);
                }

                if (!artcontent1.equals("[]")) {
                    //为了防止主键ID太多了，分批次查询
                    insertTimes = updateDataList.size() % subLength == 0 ? updateDataList.size() / subLength : updateDataList.size() / subLength + 1; // 计算需要插入的次数，100条插入一次；
                    if (insertTimes > 1) {
                        for (int i = 0; i < insertTimes; i++) {
                            List<Map<String, Object>> tempUpdateData = new ArrayList<>();
                            for (int j = i * subLength; j < (i + 1) * subLength && j < updateDataList.size(); j++) {  // 计算起始位置，且j的最大值应不能超过数据的总数；
                                tempUpdateData.add(updateDataList.get(j));
                            }
                            //更新稿件媒体信息
                            mediaAuditMapper.batchUpdateArtMedia(tempUpdateData,onlineTime);
                        }
                    } else {
                        //更新稿件媒体信息
                        mediaAuditMapper.batchUpdateArtMedia(updateDataList,onlineTime);
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("【定时主媒体更新】更新稿件/媒体供应商关系主媒体失败：{}", e.getMessage());
        }

    }

}
