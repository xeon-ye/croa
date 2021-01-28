package com.qinfei.qferp.service.impl.outapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.crm.CrmSearchCache;
import com.qinfei.qferp.mapper.crm.CrmSearchCacheMapper;
import com.qinfei.qferp.service.outapi.ICompanyService;
import com.qinfei.qferp.utils.HttpHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @CalssName: CompanyService
 * @Description: 企查查接口服务
 * @Author: Xuxiong
 * @Date: 2020/6/5 0005 15:00
 * @Version: 1.0
 */
@Service
@Slf4j
public class CompanyService implements ICompanyService {
    @Value("${qichacha.appkey}")
    private String appkey;

    @Value("${qichacha.seckey}")
    private String seckey;

    private static final String[] keyWords = {"地区", "公司", "集团","北京市", "北京", "天津市", "天津", "河北", "石家庄", "唐山", "秦皇岛", "邯郸", "邢台", "保定", "张家口", "承德", "沧州", "廊坊", "衡水", "山西", "太原", "大同", "阳泉", "长治", "晋城", "朔州", "晋中", "运城", "忻州", "临汾", "吕梁", "内蒙古自治区", "呼和浩特", "包头", "乌海", "赤峰", "通辽", "鄂尔多斯", "呼伦贝尔", "巴彦淖尔", "乌兰察布", "兴安盟", "锡林郭勒盟", "阿拉善盟", "辽宁", "沈阳", "大连", "鞍山", "抚顺", "本溪", "丹东", "锦州", "营口", "阜新", "辽阳", "盘锦", "铁岭", "朝阳", "葫芦岛", "吉林", "长春", "吉林", "四平", "辽源", "通化", "白山", "松原", "白城", "延边朝鲜族自治州", "黑龙江", "哈尔滨", "齐齐哈尔", "鸡西", "鹤岗", "双鸭山", "大庆", "伊春", "佳木斯", "七台河", "牡丹江", "黑河", "绥化", "大兴安岭地区", "上海市", "上海", "江苏", "南京", "无锡", "徐州", "常州", "苏州", "南通", "连云港", "淮安", "盐城", "扬州", "镇江", "泰州", "宿迁", "浙江", "杭州", "宁波", "温州", "嘉兴", "湖州", "绍兴", "金华", "衢州", "舟山", "台州", "丽水", "安徽", "合肥", "芜湖", "蚌埠", "淮南", "马鞍山", "淮北", "铜陵", "安庆", "黄山", "滁州", "阜阳", "宿州", "六安", "亳州", "池州", "宣城", "福建", "福州", "厦门", "莆田", "三明", "泉州", "漳州", "南平", "龙岩", "宁德", "江西", "南昌", "景德镇", "萍乡", "九江", "新余", "鹰潭", "赣州", "吉安", "宜春", "抚州", "上饶", "山东", "济南", "青岛", "淄博", "枣庄", "东营", "烟台", "潍坊", "济宁", "泰安", "威海", "日照", "莱芜", "临沂", "德州", "聊城", "滨州", "菏泽", "河南", "郑州", "开封", "洛阳", "平顶山", "安阳", "鹤壁", "新乡", "焦作", "濮阳", "许昌", "漯河", "三门峡", "南阳", "商丘", "信阳", "周口", "驻马店", "湖北", "武汉", "黄石", "十堰", "宜昌", "襄阳", "鄂州", "荆门", "孝感", "荆州", "黄冈", "咸宁", "随州", "恩施土家族苗族自治州", "湖南", "长沙", "株洲", "湘潭", "衡阳", "邵阳", "岳阳", "常德", "张家界", "益阳", "郴州", "永州", "怀化", "娄底", "湘西土家族苗族自治州", "广东", "广州", "韶关", "深圳", "珠海", "汕头", "佛山", "江门", "湛江", "茂名", "肇庆", "惠州", "梅州", "汕尾", "河源", "阳江", "清远", "东莞", "中山", "潮州", "揭阳", "云浮", "广西壮族自治区", "南宁", "柳州", "桂林", "梧州", "北海", "防城港", "钦州", "贵港", "玉林", "百色", "贺州", "河池", "来宾", "崇左", "海南", "海口", "三亚", "三沙", "儋州", "重庆市", "重庆", "四川", "成都", "自贡", "攀枝花", "泸州", "德阳", "绵阳", "广元", "遂宁", "内江", "乐山", "南充", "眉山", "宜宾", "广安", "达州", "雅安", "巴中", "资阳", "阿坝藏族羌族自治州", "甘孜藏族自治州", "凉山彝族自治州", "贵州", "贵阳", "六盘水", "遵义", "安顺", "毕节", "铜仁", "黔西南布依族苗族自治州", "黔东南苗族侗族自治州", "黔南布依族苗族自治州", "云南", "昆明", "曲靖", "玉溪", "保山", "昭通", "丽江", "普洱", "临沧", "楚雄彝族自治州", "红河哈尼族彝族自治州", "文山壮族苗族自治州", "西双版纳傣族自治州", "大理白族自治州", "德宏傣族景颇族自治州", "怒江傈僳族自治州", "迪庆藏族自治州", "西藏自治区", "拉萨", "日喀则", "昌都", "林芝", "山南", "那曲地区", "阿里地区", "陕西", "西安", "铜川", "宝鸡", "咸阳", "渭南", "延安", "汉中", "榆林", "安康", "商洛", "甘肃", "兰州", "嘉峪关", "金昌", "白银", "天水", "武威", "张掖", "平凉", "酒泉", "庆阳", "定西", "陇南", "临夏回族自治州", "甘南藏族自治州", "青海", "西宁", "海东", "海北藏族自治州", "黄南藏族自治州", "海南藏族自治州", "果洛藏族自治州", "玉树藏族自治州", "海西蒙古族藏族自治州", "宁夏回族自治区", "银川", "石嘴山", "吴忠", "固原", "中卫", "新疆维吾尔自治区", "乌鲁木齐", "克拉玛依", "吐鲁番", "哈密", "昌吉回族自治州", "博尔塔拉蒙古自治州", "巴音郭楞蒙古自治州", "阿克苏地区", "克孜勒苏柯尔克孜自治州", "喀什地区", "和田地区", "伊犁哈萨克自治州", "塔城地区", "阿勒泰地区", "台湾", "香港特别行政区", "澳门特别行政区"};

    @Autowired
    private CrmSearchCacheMapper crmSearchCacheMapper;

    @Override
    @Transactional
    public PageInfo<CrmSearchCache> companySearch(String keyword, Pageable pageable) {
        PageInfo<CrmSearchCache> pageInfo = new PageInfo<>();
        try {
            // 关键字不合法返回空的结果集
            if (!checkKeyWord(keyword)) {
                return pageInfo;
            }
            //1、第一页查询公司缓存表
            if (pageable.getPageNumber() == 1) {
                List<CrmSearchCache> crmSearchCacheList = crmSearchCacheMapper.listCrmSearchByParam(keyword);
                pageInfo.setList(crmSearchCacheList);
                pageInfo.setTotal(crmSearchCacheList.size());
                pageInfo.setPageNum(pageable.getPageNumber());
                pageInfo.setPageSize(pageable.getPageSize());
                pageInfo.setPages(2);//设置一定有第二页
            } else {
                //2、从第二页开始查询企查查接口
                listCrmSearchByKeyword(keyword, pageable, pageInfo);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return pageInfo;
    }

    /**
     * 检测关键字是否合法
     *
     * @param keyword 关键字
     * @return true 合法 false 不合法
     */
    private boolean checkKeyWord(String keyword) {
        for (String keyWord : keyWords) {
            if (keyWord.indexOf(keyword) > -1) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public ResponseData checkCompany(String keyword) {
        ResponseData result = ResponseData.ok();
        //暂停企查查接口调用，默认所有客户信息都是标准的
//        try {
//            if (StringUtils.isEmpty(keyword)) {
//                throw new QinFeiException(1002, "公司名称不能为空！");
//            }
//            //1、去缓存表查询，如果存在则返回真
//            List<String> cacheCompanyNameList = crmSearchCacheMapper.listCrmSearchByCompanyName(Arrays.asList(keyword));
//            if (CollectionUtils.isEmpty(cacheCompanyNameList)) {
//                //2、缓存表查询无，调用企查查接口校验
//                checkCompanyByApi(keyword);
//            }
//        } catch (QinFeiException e) {
//            result = ResponseData.customerError(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            result = ResponseData.customerError(1002, "公司校验接口异常！");
//        }
        return result;
    }

    @Override
    @Transactional
    public CrmSearchCache checkCompanyByApi(String keyword) {
        CrmSearchCache crmSearchCache = null;
        try {
            if (StringUtils.isEmpty(keyword)) {
                throw new QinFeiException(1002, "公司名称不能为空！");
            }
            String url = "http://api.qichacha.com/ECIInfoVerify/GetInfo";
            String param = String.format("&searchKey=%s", keyword);
            String tokenJson = doGet(url, param);
            if (StringUtils.isEmpty(tokenJson)) {
                throw new QinFeiException(1002, "公司信息不存在！");
            }
            Map<String, Object> companyMap = JSON.parseObject(tokenJson, Map.class);
            if (companyMap == null || !"200".equals(companyMap.get("Status"))) {
                throw new QinFeiException(1002, companyMap == null ? "公司信息不存在！" : String.valueOf(companyMap.get("Message")));
            } else {
                Object result = companyMap.get("Result");
                if (result != null && result instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) result;
                    //保存公司信息到缓存表
                    try {
                        crmSearchCache = new CrmSearchCache();
                        crmSearchCache.setCacheDate(new Date());
                        crmSearchCache.setSearchKeyword(keyword);
                        crmSearchCache.setCompanyName(jsonObject.getString("Name"));
                        crmSearchCache.setCompanyLegal(jsonObject.getString("OperName"));
                        crmSearchCache.setEstablishDate(StringUtils.isEmpty(jsonObject.getString("StartDate")) ? null : DateUtils.parse(jsonObject.getString("StartDate"), "yyyy-MM-dd HH:mm:ss"));
                        crmSearchCache.setCompanyStatus(jsonObject.getString("Status"));
                        crmSearchCache.setRegisterNum(jsonObject.getString("No"));
                        crmSearchCache.setCreditCode(jsonObject.getString("CreditCode"));
                        JSONArray jsonArray = jsonObject.getJSONArray("OriginalName");
                        if (CollectionUtils.isNotEmpty(jsonArray)) {
                            List<String> originalNameList = new ArrayList<>();
                            jsonArray.forEach(o -> {
                                JSONObject original = (JSONObject) o;
                                originalNameList.add(original.getString("Name"));
                            });
                            crmSearchCache.setOriginalName(StringUtils.join(originalNameList, ","));
                        }
                        //保存或更新公司
                        saveOrUpdateCompany(crmSearchCache);
                    } catch (Exception e) {
                        crmSearchCache = null;
                        log.error(e.getMessage());
                    }
                    //校验公司名称 和 返回公司名称保持一致
                    if (!keyword.equals(jsonObject.getString("Name"))) {
                        throw new QinFeiException(1002, "公司名称与查询返回不一致！");
                    }
                } else {
                    throw new QinFeiException(1002, "公司信息为空！");
                }
            }
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new QinFeiException(1002, "公司校验接口异常！");
        }
        return crmSearchCache;
    }

    //企查查获取公司列表
    private void listCrmSearchByKeyword(String keyword, Pageable pageable, PageInfo<CrmSearchCache> pageInfo) {
        try {
            if (StringUtils.isNotEmpty(keyword)) {
                String url = "http://api.qichacha.com/ECIV4/Search";
                //由于企查查接口是从第二页开始查询，所以页码 - 1
                String param = String.format("&keyword=%s&pageSize=%s&pageIndex=%s", keyword, pageable.getPageSize(), pageable.getPageNumber() - 1);
                String tokenJson = doGet(url, param);
                if (StringUtils.isNotEmpty(tokenJson)) {
                    Map<String, Object> companyMap = JSON.parseObject(tokenJson, Map.class);
                    if (companyMap != null && "200".equals(companyMap.get("Status"))) {
                        Object paging = companyMap.get("Paging");
                        if (paging != null && paging instanceof JSONObject) {
                            int total = Integer.parseInt(((JSONObject) paging).getString("TotalRecords"));
                            pageInfo.setTotal(total);
                            pageInfo.setPageNum(Integer.parseInt(((JSONObject) paging).getString("PageIndex")));
                            pageInfo.setPageSize(Integer.parseInt(((JSONObject) paging).getString("PageSize")));
                            int pages = total % pageInfo.getPageSize() == 0 ? total / pageInfo.getPageSize() : total / pageInfo.getPageSize() + 1;
                            pageInfo.setPages(pages);
                        }
                        Object result = companyMap.get("Result");
                        if (result != null && result instanceof JSONArray) {
                            List<JSONObject> jsonObjectList = ((JSONArray) result).toJavaList(JSONObject.class);
                            List<CrmSearchCache> crmSearchCacheList = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(jsonObjectList)) {
                                Date currentDate = new Date();
                                jsonObjectList.forEach(jsonObject -> {
                                    CrmSearchCache crmSearchCache = new CrmSearchCache();
                                    crmSearchCache.setCacheDate(currentDate);
                                    crmSearchCache.setSearchKeyword(keyword);
                                    crmSearchCache.setCompanyName(jsonObject.getString("Name"));
                                    crmSearchCache.setCompanyLegal(jsonObject.getString("OperName"));
                                    crmSearchCache.setEstablishDate(StringUtils.isEmpty(jsonObject.getString("StartDate")) ? null : DateUtils.parse(jsonObject.getString("StartDate"), "yyyy-MM-dd HH:mm:ss"));
                                    crmSearchCache.setCompanyStatus(jsonObject.getString("Status"));
                                    crmSearchCache.setRegisterNum(jsonObject.getString("No"));
                                    crmSearchCache.setCreditCode(jsonObject.getString("CreditCode"));
                                    crmSearchCacheList.add(crmSearchCache);
                                });
                            }
                            pageInfo.setList(crmSearchCacheList);
                            //保存到缓存表
                            saveCompany(crmSearchCacheList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //保存公司信息
    private void saveCompany(List<CrmSearchCache> crmSearchCacheList) {
        try {
            if (CollectionUtils.isEmpty(crmSearchCacheList)) {
                return;
            }
            List<CrmSearchCache> searchCacheList = new ArrayList<>();
            List<String> companyNameList = new ArrayList<>();
            crmSearchCacheList.forEach(crmSearchCache -> {
                companyNameList.add(crmSearchCache.getCompanyName());
            });
            //1、根据公司名称查询缓存表
            List<String> cacheCompanyNameList = crmSearchCacheMapper.listCrmSearchByCompanyName(companyNameList);
            //2、移除已经存在的公司名称，判断条件格式：公司名称-社会统一信用代码
            crmSearchCacheList.forEach(crmSearchCache -> {
                String key = String.format("%s-%s", crmSearchCache.getCompanyName(), crmSearchCache.getCreditCode());
                //如果缓存表不存在该公司名称，就入库
                if (CollectionUtils.isEmpty(cacheCompanyNameList) || !cacheCompanyNameList.contains(key)) {
                    searchCacheList.add(crmSearchCache);
                }
            });
            //3、新增入库
            if (CollectionUtils.isNotEmpty(searchCacheList)) {
                crmSearchCacheMapper.batchSave(searchCacheList);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    //保存更新公司信息
    private void saveOrUpdateCompany(CrmSearchCache crmSearchCache) {
        List<String> crmSearchCacheList = crmSearchCacheMapper.listCrmSearchByCompanyName(Arrays.asList(crmSearchCache.getCompanyName()));
        if (CollectionUtils.isEmpty(crmSearchCacheList)) {
            crmSearchCacheMapper.save(crmSearchCache);
        } else {
            String key = String.format("%s-%s", crmSearchCache.getCompanyName(), crmSearchCache.getCreditCode());
            //如果存在，则更新记录（缓存日期、曾用名），否则新增
            if (crmSearchCacheList.contains(key)) {
                crmSearchCacheMapper.edit(crmSearchCache);
            } else {
                crmSearchCacheMapper.save(crmSearchCache);
            }
        }
    }

    // 获取Auth Code
    private String[] RandomAuthentHeader() {
        String timeSpan = String.valueOf(System.currentTimeMillis() / 1000);
        String[] authentHeaders = new String[]{DigestUtils.md5Hex(appkey.concat(timeSpan).concat(seckey)).toUpperCase(), timeSpan};
        return authentHeaders;
    }

    //请求外部接口
    private String doGet(String url, String param) throws Exception {
        HttpHead reqHeader = new HttpHead();
        String[] autherHeader = RandomAuthentHeader();
        reqHeader.setHeader("Token", autherHeader[0]);
        reqHeader.setHeader("Timespan", autherHeader[1]);
        url = url.concat("?key=").concat(appkey).concat(param);
        return HttpHelper.httpGet(url, reqHeader.getAllHeaders());
    }
}
