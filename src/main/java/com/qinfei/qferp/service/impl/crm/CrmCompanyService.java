package com.qinfei.qferp.service.impl.crm;

import com.qinfei.core.config.Config;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.crm.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.crm.*;
import com.qinfei.qferp.service.crm.ICrmCompanyService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sun.org.apache.bcel.internal.generic.ICONST;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 表：crm客户公司表(TCrmCompany)表服务实现类
 *
 * @author jca
 * @since 2020-07-07 17:27:24
 */
@Service
public class CrmCompanyService implements ICrmCompanyService {
    @Resource
    private CrmCompanyMapper crmCompanyMapper;
    @Resource
    private CrmCompanyUserMapper companyUserMapper;
    @Resource
    private CrmCompanyUserSalesmanMapper companyUserSalesmanMapper;
    @Resource
    private CrmCompanyHistoryMapper crmCompanyHistoryMapper;
    @Resource
    private CrmCompanyUserService crmCompanyUserService;
    @Resource
    private CrmCompanyProtectMapper crmCompanyProtectMapper;
    @Autowired
    private IProcessService processService ;
    @Autowired
    private IDictService dictService ;
    @Autowired
    private Config config;

    /**
     * 查询列表
     */
    @Override
    public PageInfo list(Map map, int pageNum, int pageSize) {
        return null;
    }

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CrmCompany getById(Integer id) {
        return crmCompanyMapper.getById(id);
    }

    @Override
    public CrmCompany getByName(String name) {
        return crmCompanyMapper.getByName(name);
    }

    @Override
    public Map getByIdDetail(Integer id) {
        return this.crmCompanyMapper.getByIdDetail(id);
    }

    @Override
    @Transactional
    public void saveCompany(Map map){
        User user = AppUtil.getUser();
        String companyName = map.get("companyName") == null ? null : MapUtils.getString(map, "companyName").trim();
        Integer custProperty = MapUtils.getInteger(map, "custProperty");//公司性质，1公司，2个人
        String userName = MapUtils.getString(map, "userName").trim();
        String mobile = MapUtils.getString(map, "mobile").trim();
        String wechat = map.get("wechat") == null ? null : MapUtils.getString(map, "wechat").trim();
        String qq = map.get("qq") == null ? null : MapUtils.getString(map, "qq").trim();
        Integer standardize = MapUtils.getInteger(map, "standardize");//公司名称是否标准
        Integer normalize = MapUtils.getInteger(map, "normalize");//手机号是否规范

        if(IConst.CUST_TYPE_PERSONAL.equals(custProperty)){
            companyName = IConst.INDIVIDUAL_COMPNAY_NAME;//个人默认是个体工商户
        }
        if(IConst.CUST_TYPE_COMPANY.equals(custProperty) && IConst.INDIVIDUAL_COMPNAY_NAME.equals(companyName)){
            throw new QinFeiException(1002, "企业客户公司名称不能是个体工商户！");
        }
        if(IConst.COMMON_FLAG_TRUE.equals(standardize) && IConst.COMMON_FLAG_TRUE.equals(normalize)){
            Dict temp = new Dict();
            temp.setTypeCode(IConst.CUST_PROTECT_C);
            temp.setCode(IConst.CUST_PROTECT_NUM_C);
            Dict dict = dictService.getByTypeCodeAndCode(temp);
            Integer max = Integer.parseInt(dict.getType());
            Integer existNum = companyUserMapper.countByUserIdAndStateAndLevel(user.getId(), IConst.PROTECT_LEVEL_C);
            if(!(existNum < max)){
                throw new QinFeiException(1002, "C类保护客户每人最多只能添加："+max+"个，您的C类客户名额已用完，无法新增C类客户！");
            }
        }
//        如果客户公司名称已经保护，该客户公司名称不能再登记
        CrmCompany company = crmCompanyMapper.getByName(companyName);
        CrmCompanyUser companyUser = null;

        if(company != null){
            if(company.getProtectLevel() > IConst.PROTECT_LEVEL_C || IConst.COMMON_FLAG_TRUE.equals(company.getAuditFlag())){
                StringBuffer msg1 = new StringBuffer();
                msg1.append("公司名:").append(companyName);
                msg1.append("已申请保护，或保护审核中");
                msg1.append("\n该公司名下无法新增客户对接人！");
                throw new QinFeiException(1002, msg1.toString());
            }
            //处理历史公司数据
            if(!(company.getStandardize().equals(standardize) && company.getType().equals(custProperty))){
                // 存company历史表
                saveCompanyHistory(company, user);
                // 更新company表
                company.setStandardize(standardize);
                company.setType(custProperty);
                company.setUpdateUserId(user.getId());
                crmCompanyMapper.update(company);
            }
            //        如果公司名称+手机号进行了保护，该公司名称可以增加对接人，但是不能再增加该手机号
            //        删除之后的记录仍然做判重，需要新增的话就恢复当前对接人信息，对接人信息删除后只有系统管理员可以查询到；
            List<CrmCompanyUser> list = companyUserMapper.queryByCompanyIdAndMobile(company.getId(), EncryptUtils.encrypt(mobile));
            if(list == null || list.size() == 0){//公司注册了，手机号没注册，往companyUser表插数据
                companyUser = new CrmCompanyUser();
                companyUser.setCompanyId(company.getId());
                companyUser = doWithCompanyUser(companyUser, userName, mobile, wechat,
                        qq, normalize, user.getId(), standardize);
                companyUserMapper.insert(companyUser);
            }else{//公司注册了，手机号也注册了
                //如果有人占用了该公司名称和手机号
                List<Map> onList = companyUserMapper.queryByCompanyIdAndMobileOn(company.getId(), EncryptUtils.encrypt(mobile));
                if(onList != null && onList.size() > 0){
                    StringBuffer sb = new StringBuffer();
                    for(Map temp : onList){
                        sb.append(MapUtils.getString(temp, "userName")).append(",");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    throw new QinFeiException(1002, "公司名称和手机号码必须唯一，已被"+onList.size()+"人占用，请确认手机号或找占用该手机号的业务员沟通。占用业务员："+ sb.toString());
                }

                //如果没有人占用了该公司名称和手机号，但是公海中该公司名称和手机号
                List<Map> pubLicList = companyUserMapper.queryByCompanyIdAndMobilePublic(company.getId(), EncryptUtils.encrypt(mobile));
                if(pubLicList != null && pubLicList.size() > 0){
                    StringBuffer str = new StringBuffer();
                    for(Map temp : pubLicList){
                        str.append(MapUtils.getString(temp, "companyUserName")).append(",");
                    }
                    str.deleteCharAt(str.length() - 1);
                    throw new QinFeiException(1002, "公司名称和手机号码必须唯一，已被公海"+pubLicList.size()+"人占用，无法新增，请去公海认领。公司名称："+companyName+"，对接人："+ str.toString());
                }
                //如果没有人占用了该公司名称和手机号，公海中也没有，新增
                companyUser = new CrmCompanyUser();
                companyUser.setCompanyId(company.getId());
                companyUser = doWithCompanyUser(companyUser, userName, mobile, wechat,
                        qq, normalize, user.getId(), standardize);
                companyUserMapper.insert(companyUser);
               /* 不恢复了，直接新增
               //如果没有人占用了该公司名称和手机号，公海中也没有，这是被删除了，直接恢复
                List<CrmCompanyUser> offList = companyUserMapper.queryByCompanyIdAndMobileDel(company.getId(), EncryptUtils.encrypt(mobile));
                if(offList != null && offList.size() > 0){
                    companyUser= offList.get(0);
                    companyUser.setDeleteFlag(IConst.COMMON_FLAG_FALSE);
                    companyUser = doWithCompanyUser(companyUser, userName, mobile, wechat,
                            qq, normalize, user.getId(), normalize);
                    companyUserMapper.update(companyUser);
                }*/
            }
        }else {
            //客户公司表
            company = new CrmCompany();
            company.setName(companyName);
            company.setStandardize(standardize);
            company.setType(custProperty);
            company.setCreateTime(new Date());
            company.setCreator(user.getId());
            crmCompanyMapper.insert(company);

            //客户对接人表
            companyUser = new CrmCompanyUser();
            companyUser.setCompanyId(company.getId());
            companyUser = doWithCompanyUser(companyUser, userName, mobile, wechat,
                    qq, normalize, user.getId(), standardize);
            companyUserMapper.insert(companyUser);
        }

        //对接人对应的业务员表
        Map salesman = new HashMap();
        salesman.put("companyUserId", companyUser.getId());
        salesman.put("userId", user.getId());
        salesman.put("userName", user.getName());
        salesman.put("deptId", user.getDeptId());
        salesman.put("deptName", user.getDeptName());
        salesman.put("typeIn", IConst.TRANSFER_TYPE_IN_ADD);
        salesman.put("state", IConst.COMMON_FLAG_TRUE);
        salesman.put("deleteFlag", IConst.COMMON_FLAG_FALSE);
        salesman.put("creator", user.getId());
        companyUserSalesmanMapper.saveSalesman(salesman);

        //跟进记录表
        Map track = new HashMap();
        track.put("companyUserId", companyUser.getId());
        track.put("content", IConst.TRACK_CONTENT_ADD);
        track.put("imageName", null);
        track.put("imageLink", null);
        track.put("affixName", null);
        track.put("affixLink", null);
        track.put("trackTime", new Date());
        track.put("creator", user.getId());
        crmCompanyMapper.saveCompanyTrack(track);
    }

    private CrmCompanyUser doWithCompanyUser(CrmCompanyUser companyUser, String userName, String mobile, String wechat,
                                             String qq, Integer normalize, Integer creator, Integer standardize){
        companyUser.setName(userName);
        companyUser.setMobile(EncryptUtils.encrypt(mobile));
        companyUser.setWechat(EncryptUtils.encrypt(wechat));
        companyUser.setQq(EncryptUtils.encrypt(qq));
        companyUser.setNormalize(normalize);
        companyUser.setIsPublic(IConst.COMMON_FLAG_FALSE);
        companyUser.setIsPublicState(IConst.COMMON_FLAG_FALSE);
        Date date = new Date();
        companyUser.setEvalTime(date);
        companyUser.setDealTime(date);
        companyUser.setCreateTime(date);
        companyUser.setCreator(creator);
        if(IConst.COMMON_FLAG_TRUE.equals(standardize) && IConst.COMMON_FLAG_TRUE.equals(normalize)){//标准且规范，默认C级强保护
            companyUser.setProtectStrong(IConst.COMMON_FLAG_TRUE);
            companyUser.setProtectLevel(IConst.PROTECT_LEVEL_C);
        } else{
            companyUser.setProtectStrong(IConst.COMMON_FLAG_FALSE);
            companyUser.setProtectLevel(IConst.PROTECT_LEVEL_D);//否则不保护
        }
        return companyUser;
    }

    @Override
    @Transactional
    public void updateCompanyBasic(Map map) {
        User user = AppUtil.getUser();
        Integer newNormalize = MapUtils.getInteger(map, "normalize");
        Integer newStandardize = MapUtils.getInteger(map, "standardize");
        Integer newCustProperty = MapUtils.getInteger(map, "custProperty");
        String newCompanyName = map.get("companyName") == null ? null : MapUtils.getString(map, "companyName").trim();
        String userName = MapUtils.getString(map, "userName").trim();
        String mobile = MapUtils.getString(map, "mobile").trim();
        String qq = map.get("qq") == null ? null : MapUtils.getString(map, "qq").trim();
        String wechat = map.get("wechat") == null ? null : MapUtils.getString(map, "wechat").trim();
        Integer normalize = MapUtils.getInteger(map, "normalize");
        Integer userId = MapUtils.getInteger(map, "userId");

        Map old = companyUserMapper.getBasicById(userId);
        Integer oldNormalize = MapUtils.getInteger(old, "normalize");
        Integer oldStandardize = MapUtils.getInteger(old, "standardize");
        Integer oldCustProperty = MapUtils.getInteger(old, "custProperty");
        Integer oldCompanyId = MapUtils.getInteger(old, "companyId");
        String oldCompanyName = MapUtils.getString(old, "companyName");
        Integer oldAuditFlag = MapUtils.getInteger(old, "auditFlag");
        Integer oldProtectLevel = MapUtils.getInteger(old, "protectLevel");
        Integer oldCompanyProtectLevel = MapUtils.getInteger(old, "companyProtectLevel");

        if(oldCompanyProtectLevel > IConst.PROTECT_LEVEL_C && !oldProtectLevel.equals(oldCompanyProtectLevel)){
            StringBuffer sb = new StringBuffer();
            sb.append("该客户公司保护级别：");
            switch (oldCompanyProtectLevel) {
                case 3 :
                    sb.append("A类保护");
                    break;
                case 2:
                    sb.append("B类保护");
                    break;
                default:
                    break;
            }
            sb.append("；该对接人保护级别：");
            switch (oldProtectLevel) {
                case 3 :
                    sb.append("A类保护");
                    break;
                case 2:
                    sb.append("B类保护");
                    break;
                case 1:
                    sb.append("C类保护");
                    break;
                case 0:
                    sb.append("弱保护");
                    break;
                default:
                    break;
            }
            sb.append("。不满足修改条件！");
            throw new QinFeiException(1002, sb.toString());
        }
        //1、基本规则
        if(IConst.COMMON_FLAG_TRUE.equals(oldAuditFlag)){
            throw new QinFeiException(1002, "客户公司：" + oldCompanyName + "正在保护审核中，无法修改公司名称，请等待审核完成后刷新重试!");
        }
        if(IConst.COMMON_FLAG_TRUE.equals(oldStandardize) && IConst.COMMON_FLAG_FALSE.equals(newStandardize)){
            throw new QinFeiException(1002, "标准的客户公司名称不能修改成非标准的客户公司名称!");
        }
        if(IConst.COMMON_FLAG_TRUE.equals(oldNormalize) && IConst.COMMON_FLAG_FALSE.equals(newNormalize)){
            throw new QinFeiException(1002, "规范的对接人手机号不能修改成非规范的对接人手机号!");
        }
        if(IConst.CUST_TYPE_COMPANY.equals(oldCustProperty) && IConst.CUST_TYPE_PERSONAL.equals(newCustProperty)){
            throw new QinFeiException(1002, "企业客户不能更改为个人客户！");
        }
        if(IConst.CUST_TYPE_COMPANY.equals(newCustProperty) && IConst.INDIVIDUAL_COMPNAY_NAME.equals(newCompanyName)){
            throw new QinFeiException(1002, "企业客户公司名称不能是个体工商户！");
        }

        //如果之前是弱保护，现在是强保护，不能超过C类保护限额
        if(IConst.PROTECT_LEVEL_D.equals(oldProtectLevel) && IConst.COMMON_FLAG_TRUE.equals(newNormalize) && IConst.COMMON_FLAG_TRUE.equals(newStandardize)){
            Dict temp = new Dict();
            temp.setTypeCode(IConst.CUST_PROTECT_C);
            temp.setCode(IConst.CUST_PROTECT_NUM_C);
            Dict dict = dictService.getByTypeCodeAndCode(temp);
            Integer max = Integer.parseInt(dict.getType());
            Integer existNum = companyUserMapper.countByUserIdAndStateAndLevel(user.getId(), IConst.PROTECT_LEVEL_C);
            if(!(existNum < max)){
                throw new QinFeiException(1002, "C类保护客户每人最多只能添加："+max+"个，您的C类客户名额已用完，无法修改为C类客户！");
            }
        }

        //2、公司名称和手机号排重，不符合规则就不改
        List<CrmCompanyUser> list = companyUserMapper.queryByCompanyNameAndMobileNotId(newCompanyName, EncryptUtils.encrypt(mobile), userId);
        if(list != null && list.size() > 0){//公司名称和手机号重复
            //如果有人占用了该公司名称和手机号
            List<Map> onList = companyUserMapper.queryByCompanyNameAndMobileNotIdOn(newCompanyName, EncryptUtils.encrypt(mobile), userId);
            if(onList != null && onList.size() > 0){
                StringBuffer sb = new StringBuffer();
                for(Map temp : onList){
                    sb.append(MapUtils.getString(temp, "userName")).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                throw new QinFeiException(1002, "公司名称和手机号码必须唯一，已被"+onList.size()+"人占用，请确认手机号或找占用该手机号的业务员沟通。占用业务员："+ sb.toString());
            }

            //如果没有人占用了该公司名称和手机号，但是公海中该公司名称和手机号
            List<Map> pubLicList = companyUserMapper.queryByCompanyNameAndMobileNotIdPublic(newCompanyName, EncryptUtils.encrypt(mobile), userId);
            if(pubLicList != null && pubLicList.size() > 0){
                StringBuffer str = new StringBuffer();
                for(Map temp : pubLicList){
                    str.append(MapUtils.getString(temp, "companyUserName")).append(",");
                }
                str.deleteCharAt(str.length() - 1);
                throw new QinFeiException(1002, "公司名称和手机号码必须唯一，已被公海"+pubLicList.size()+"人占用，无法修改，请去公海认领。公司名称："+newCompanyName+"，对接人："+ str.toString());
            }
            //如果该公司名称和手机号删除了，直接更新companyUser表即可
        }

        //3、开始更改
        CrmCompanyUser companyUser = companyUserMapper.getById(userId);
        if(IConst.CUST_TYPE_COMPANY.equals(newCustProperty)) {//公司客户
            if(StringUtils.isEmpty(newCompanyName)){//公司名称没有改变，直接修改对接人表信息即可
                //存companyUser历史表
                crmCompanyUserService.saveCompanyUserHistory(companyUser, oldCompanyName, user.getId());
                //更新对接人信息
                updateCompanyUserBasic(companyUser, oldCompanyId, userName, mobile, qq, wechat, normalize, newStandardize, user.getId(), oldProtectLevel);
            }else{
                if(oldStandardize.equals(newStandardize) && oldCompanyName.equals(newCompanyName) && oldCustProperty.equals(newCustProperty)){//公司信息没有变
                    //存companyUser历史表
                    crmCompanyUserService.saveCompanyUserHistory(companyUser, oldCompanyName, user.getId());
                    //更新对接人信息
                    updateCompanyUserBasic(companyUser, oldCompanyId, userName, mobile, qq, wechat, normalize, newStandardize, user.getId(), oldProtectLevel);
                }else{//公司信息变更了
                    CrmCompany crmCompany = crmCompanyMapper.getByName(newCompanyName);
                    if(crmCompany == null){//新公司原来没有，插新数据
                        crmCompany = new CrmCompany();
                        if(IConst.CUST_TYPE_PERSONAL.equals(newCustProperty)){
                            crmCompany.setName(IConst.INDIVIDUAL_COMPNAY_NAME);
                        }else{
                            crmCompany.setName(newCompanyName);
                        }
                        crmCompany.setStandardize(newStandardize);
                        crmCompany.setType(newCustProperty);
                        crmCompany.setCreateTime(new Date());
                        crmCompany.setCreator(user.getId());
                        crmCompanyMapper.insert(crmCompany);
                    }else{//新公司原来就有，拿来用
                        // 存company历史表
                        saveCompanyHistory(crmCompany, user);
                        // 更新company表
                        crmCompany.setStandardize(newStandardize);
                        crmCompany.setType(newCustProperty);
                        crmCompany.setUpdateUserId(user.getId());
                        crmCompanyMapper.update(crmCompany);
                    }
                    // 存companyUser历史表
                    crmCompanyUserService.saveCompanyUserHistory(companyUser, oldCompanyName, user.getId());
                    //更新对接人信息
                    updateCompanyUserBasic(companyUser, crmCompany.getId(), userName, mobile, qq, wechat, normalize, newStandardize, user.getId(), oldProtectLevel);
                    if(!oldCompanyId.equals(crmCompany.getId())){//公司id变更了，同步业务表
                        int b = crmCompanyMapper.updateOrderCompanyInfo(companyUser.getId(), crmCompany.getId(), crmCompany.getName());
                        int c = crmCompanyMapper.updateInvoiceCompanyInfo(companyUser.getId(), crmCompany.getId(), crmCompany.getName());
                        int d = crmCompanyMapper.updateRefundCompanyInfo(companyUser.getId(), crmCompany.getId(), crmCompany.getName());
                        int e = crmCompanyMapper.updateMessCompanyInfo(companyUser.getId(), crmCompany.getId(), crmCompany.getName());
                        System.out.println("更新数据：订单：" + b + "条，开票：" + c + "条，退款：" + d);
                    }
                }
            }
        }else{//个人客户，公司名称默认是个体工商户
            CrmCompany crmCompany = crmCompanyMapper.getByName(IConst.INDIVIDUAL_COMPNAY_NAME);
            if(crmCompany == null){
                crmCompany = new CrmCompany();
                crmCompany.setName(IConst.INDIVIDUAL_COMPNAY_NAME);
                crmCompany.setStandardize(newStandardize);
                crmCompany.setType(newCustProperty);
                crmCompany.setCreateTime(new Date());
                crmCompany.setCreator(user.getId());
                crmCompanyMapper.insert(crmCompany);
            }
            // 存companyUser历史表
            crmCompanyUserService.saveCompanyUserHistory(companyUser, oldCompanyName, user.getId());
            //更新对接人信息
            updateCompanyUserBasic(companyUser, crmCompany.getId(), userName, mobile, qq, wechat, normalize, newStandardize, user.getId(), oldProtectLevel);
            //客户公司id变更了，要把业务和财务相关的客户公司id变更过来
            if(!oldCompanyId.equals(crmCompany.getId())){
                int b = crmCompanyMapper.updateOrderCompanyInfo(companyUser.getId(), crmCompany.getId(), crmCompany.getName());
                int c = crmCompanyMapper.updateInvoiceCompanyInfo(companyUser.getId(), crmCompany.getId(), crmCompany.getName());
                int d = crmCompanyMapper.updateRefundCompanyInfo(companyUser.getId(), crmCompany.getId(), crmCompany.getName());
                int e = crmCompanyMapper.updateMessCompanyInfo(companyUser.getId(), crmCompany.getId(), crmCompany.getName());
                System.out.println("更新数据：订单：" + b + "条，开票：" + c + "条，退款：" + d);
            }
        }
    }

    private void saveCompanyHistory(CrmCompany crmCompany, User user){
        CrmCompanyHistory crmCompanyHistory = new CrmCompanyHistory();
        BeanUtils.copyProperties(crmCompany, crmCompanyHistory);
        crmCompanyHistory.setId(null);
        crmCompanyHistory.setCompanyId(crmCompany.getId());
        crmCompanyHistory.setCreateTime(new Date());
        crmCompanyHistory.setCreator(user.getId());
        crmCompanyHistoryMapper.insert(crmCompanyHistory);
    }

    private void updateCompanyUserBasic(CrmCompanyUser companyUser, Integer companyId, String userName,
                                        String mobile, String qq, String wechat, Integer normalize,
                                        Integer standardize, Integer loginUserId, Integer protectLevel){
        companyUser.setCompanyId(companyId);
        companyUser.setName(userName);
        companyUser.setMobile(EncryptUtils.encrypt(mobile));
        companyUser.setWechat(EncryptUtils.encrypt(wechat));
        companyUser.setQq(EncryptUtils.encrypt(qq));
        companyUser.setNormalize(normalize);
        if(IConst.COMMON_FLAG_TRUE.equals(standardize) && IConst.COMMON_FLAG_TRUE.equals(normalize)){//标准且规范，默认C级强保护
            if(protectLevel > IConst.PROTECT_LEVEL_C){
                companyUser.setProtectLevel(protectLevel);
            }else{
                companyUser.setProtectLevel(IConst.PROTECT_LEVEL_C);
            }
            companyUser.setProtectStrong(IConst.COMMON_FLAG_TRUE);
        } else{
            companyUser.setProtectLevel(IConst.PROTECT_LEVEL_D);//否则不保护
            companyUser.setProtectStrong(IConst.COMMON_FLAG_FALSE);
        }
        companyUser.setUpdateUserId(loginUserId);
        companyUserMapper.update(companyUser);
    }

    @Override
    @Transactional
    public void updateCompany(Map map) {
        User user = AppUtil.getUser();
        //如果传了alterFlag参数，并且value=0，就不用保存公司表的历史记录了。
        if(!(map.containsKey("alterFlag") && IConst.COMMON_FLAG_FALSE.equals(map.get("alterFlag")))){
            CrmCompany crmCompany = crmCompanyMapper.getById(MapUtils.getInteger(map, "id"));
            // 存company历史表
            saveCompanyHistory(crmCompany, user);
        }

        map.put("loginUserId", user.getId());
        crmCompanyMapper.updateCompany(map);
        return;
    }

    @Override
    public PageInfo productList(Integer companyId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = crmCompanyMapper.queryCompanyProduct(companyId);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo consumerList(Integer companyId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = crmCompanyMapper.queryCompanyConsumer(companyId);
        return new PageInfo<>(list);
    }

    @Override
    public Map getProductById(Integer productId) {
        return crmCompanyMapper.getProductById(productId);
    }

    @Override
    public Map getConsumerById(Integer consumerId){
        return crmCompanyMapper.getConsumerById(consumerId);
    }

    @Override
    public void saveProduct(Map map){
        User user = AppUtil.getUser();
        map.put("creator", user.getId());
        crmCompanyMapper.saveProduct(map);
        return ;
    }

    @Override
    public void updateProduct(Map map){
        User user = AppUtil.getUser();
        map.put("loginUserId", user.getId());
        crmCompanyMapper.updateProduct(map);
        return ;
    }

    @Override
    public void delProduct(Map map){
        User user = AppUtil.getUser();
        map.put("loginUserId", user.getId());
        map.put("deleteFlag", IConst.COMMON_FLAG_TRUE);
        crmCompanyMapper.updateProduct(map);
        return ;
    }

    @Override
    public void saveConsumer(Map map){
        User user = AppUtil.getUser();
        map.put("creator", user.getId());
        crmCompanyMapper.saveConsumer(map);
        return ;
    }

    @Override
    public void updateConsumer(Map map){
        User user = AppUtil.getUser();
        map.put("loginUserId", user.getId());
        crmCompanyMapper.updateConsumer(map);
        return;
    }

    @Override
    public void delConsumer(Map map) {
        User user = AppUtil.getUser();
        map.put("loginUserId", user.getId());
        map.put("deleteFlag", IConst.COMMON_FLAG_TRUE);
        crmCompanyMapper.updateConsumer(map);
        return;
    }

    @Override
    public PageInfo listProtect(Map map, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = crmCompanyMapper.listProtect(map);
        return new PageInfo<>(list);
    }
    @Override
    public Map addProtect(Map param) {
        User user = AppUtil.getUser();
        Integer loginUserId = user.getId();
        Integer applyProtectLevel = IConst.PROTECT_LEVEL_B;//默认申请B级保护
        if (param.containsKey("companyId")) {
            Integer companyId = MapUtils.getInteger(param, "companyId");
            Map map = crmCompanyMapper.getByIdDetail(companyId);
            if(!ObjectUtils.isEmpty(map)){
                if(map.containsKey("auditFlag") && IConst.COMMON_FLAG_TRUE.equals(MapUtils.getInteger(map, "auditFlag"))){
                    throw new QinFeiException(1001, "该公司已保护或正在申请保护中，不能重复申请！");
                }else{
                    //如果没有申请过，就默认申请2（B类保护），如果已经是2（B类保护）了，就申请1（A类保护）,如果已经是1(A类保护)了，就不能再申请了
                    if(map.containsKey("protectLevel")){
                        Integer oldProtectLevel = MapUtils.getInteger(map, "protectLevel");
                        if(IConst.PROTECT_LEVEL_A.equals(oldProtectLevel)){
                            throw new  QinFeiException(1001, "该公司已经是A类保护了，不能再申请了！");
                        }else if(IConst.PROTECT_LEVEL_B.equals(oldProtectLevel)){
                            applyProtectLevel = IConst.PROTECT_LEVEL_A;
                        }
                    }
                }
            }else{
                throw new QinFeiException(1001, "公司信息不存在，公司id="+companyId+"！请刷新重试！");
            }
        }else {
            throw new QinFeiException(1001, "公司id不存在！请刷新重试！");
        }
        if (param.containsKey("companyUserId")) {
            Integer companyUserId = MapUtils.getInteger(param, "companyUserId");
            Map result = companyUserMapper.queryUserBasicInfoByCompanyId(companyUserId, loginUserId);
            if (result != null && result.size() > 0) {

                Integer remainNumA = IConst.PROTECT_NUM_A;//剩余A类保护数量
                Integer remainNumB = IConst.PROTECT_NUM_B;//剩余B类保护数量

                Dict dictParam = new Dict();
                dictParam.setTypeCode("CUST_PROTECT_A");
                dictParam.setCode("NUM_A");
                Dict dictA = dictService.getByTypeCodeAndCode(dictParam);

                if(dictA != null && dictA.getType() != null){
                    remainNumA = Integer.parseInt(dictA.getType());
                }
                dictParam.setTypeCode("CUST_PROTECT_B");
                dictParam.setCode("NUM_B");
                Dict dictB = dictService.getByTypeCodeAndCode(dictParam);
                if(dictB != null && dictB.getType() != null){
                    remainNumB = Integer.parseInt(dictB.getType());
                }
                List<Map> list = companyUserMapper.getProtectNum(loginUserId);
                if(list != null && list.size() > 0){
                    for(int i=0; i< list.size(); i++){
                        Map temp = list.get(i);
                        Integer key = MapUtils.getInteger(temp, "protectLevel");
                        Integer value = MapUtils.getInteger(temp, "protectNum");
                        if(IConst.PROTECT_LEVEL_A.equals(key)){
                            remainNumA = remainNumA - value;
                        }
                        if(IConst.PROTECT_LEVEL_B.equals(key)){
                            remainNumB = remainNumB - value;
                        }
                    }
                }
                result.put("remainNumA", remainNumA);
                result.put("remainNumB", remainNumB);
                result.put("protectLevel", applyProtectLevel);

                Object mobile = result.get("mobile");
                if(mobile != null && mobile.toString().length() > 15){
                    result.put("mobile", EncryptUtils.decrypt(mobile.toString()));
                }
                Object wechat = result.get("wechat");
                if(wechat != null && wechat.toString().length() > 15){
                    result.put("wechat", EncryptUtils.decrypt(wechat.toString()));
                }
                Object qq = result.get("qq");
                if(qq != null && qq.toString().length() > 15){
                    result.put("qq", EncryptUtils.decrypt(qq.toString()));
                }

                return result;
            } else {
                throw new QinFeiException(1001, "公司对接人信息不存在！对接人id="+companyUserId +"\t 登录人id="+loginUserId);
            }
        } else {
            throw new QinFeiException(1001, "未获取到公司对接人id。");
        }
    }

    @Override
    @Transactional
    public void saveProtect(CrmCompanyProtect protect) {
        User user = AppUtil.getUser();
        Integer loginUserId = user.getId();
        if (protect.getCompanyId() != null) {
            Integer companyId = protect.getCompanyId();
            CrmCompany company = crmCompanyMapper.getById(companyId);
            if(company !=null){
                if(IConst.COMMON_FLAG_TRUE.equals(company.getAuditFlag())){
                    throw new QinFeiException(1001, "该公司已保护或正在申请保护中，不能重复申请！");
                }else{
                    Integer protectLevel = company.getProtectLevel();
                    //这里company表的protectLevel只有3（A类保护）和2（B类保护）有用，C类保护和不保护都置为1
                    if(protectLevel < 1){
                        protectLevel = 1;//如果没有申请过，就默认为1（C级），申请2（B类保护）
                    }
                    if(protectLevel == 3){
                        throw new QinFeiException(1001, "该公司已经是A类保护了，无法申请！");
                    }else{
                        company.setAuditFlag(IConst.COMMON_FLAG_TRUE);
                        company.setUpdateUserId(loginUserId);
                    }
                    crmCompanyMapper.update(company);

                    protect.setProtectLevel(protectLevel + 1);//保护等级往上加1，B申请A,C申请B
                    protect.setApplyId(user.getId());
                    protect.setApplyName(user.getName());
                    protect.setApplyDeptId(user.getDeptId());
                    protect.setApplyDeptName(user.getDeptName());
                    protect.setApplyTime(new Date());
                    protect.setDeleteFlag(IConst.COMMON_FLAG_FALSE);
                    protect.setState(IConst.PROTECT_STATE_OPERATOR);
                    crmCompanyProtectMapper.insert(protect);
                    processService.addProtectProcess(protect, 3);
                }
            }else{
                throw new QinFeiException(1001, "公司信息不存在，公司id="+companyId+"！请刷新重试！");
            }
            crmCompanyMapper.update(company);
        }else {
            throw new QinFeiException(1001, "公司id不存在！请刷新重试！");
        }
    }

    @Override
    public CrmCompanyProtect getCompanyIdByProtectId(Integer protectId){
        return crmCompanyProtectMapper.getCompanyIdByProtectId(protectId);
    }

    @Override
    public void updateProtect(CrmCompanyProtect protect){
        crmCompanyProtectMapper.update(protect);
    }

    @Override
    public Map viewProtect(Map param){
        if (param.containsKey("protectId")) {
            Integer protectId = MapUtils.getInteger(param, "protectId");
            return crmCompanyMapper.queryProtectByProtectId(protectId);
        }else {
            throw new QinFeiException(1001, "申请保护记录id不存在！请刷新重试！");
        }
    }

    @Override
    public void auditProtectPass(Map param){
        if (param.containsKey("protectId") && param.containsKey("companyId")) {
            Integer protectId = MapUtils.getInteger(param, "protectId");
            Map protectMap = crmCompanyMapper.getProtect(protectId);
            if(protectMap != null){
                Map temp = new HashMap();
                temp.put("id", protectId);
                temp.put("loginUserId", AppUtil.getUser().getId());
                temp.put("state", 3);
                crmCompanyMapper.updateProtect(temp);
            }else{
                throw new QinFeiException(1001, "参数不正确，没有获取到保护申请记录！请刷新重试！");
            }
        }else {
            throw new QinFeiException(1001, "参数不正确，未获取到protectId或companyId！请刷新重试！");
        }
    }
    @Override
    @Transactional
    public void auditProtectSuc(CrmCompanyProtect protect){
        User user = AppUtil.getUser();
        Date date = new Date();
        //更新公司表信息
        Map map = new HashMap();
        map.put("id", protect.getCompanyId());
        map.put("protectLevel", protect.getProtectLevel());
        map.put("auditFlag", IConst.COMMON_FLAG_FALSE);
        map.put("loginUserId", user.getId());
        crmCompanyMapper.updateCompany(map);

        //把该公司下规范的对接人拿过来,
        List<CrmCompanyUser> list = crmCompanyUserService.queryByCompanyIdAndNormalize(protect.getCompanyId(), IConst.COMMON_FLAG_TRUE);
        if(list != null && list.size() > 0){
            Map<String, Date> dateMap = companyUserMapper.getMaxDate(protect.getCompanyId(), IConst.COMMON_FLAG_TRUE);
            Date evalDate = dateMap.get("evalDate") == null ? date : dateMap.get("evalDate");
            Date dealDate = dateMap.get("dealDate") == null ? date : dateMap.get("dealDate");
            for(CrmCompanyUser companyUser : list){
                crmCompanyUserService.saveCompanyUserHistory(companyUser, protect.getCompanyName(),user.getId());
                companyUser.setEvalTime(evalDate);
                companyUser.setDealTime(dealDate);
                companyUser.setProtectLevel(protect.getProtectLevel());
                companyUser.setUpdateUserId(user.getId());
                companyUserMapper.update(companyUser);

                CrmCompanyUserSalesman salesman = companyUserSalesmanMapper.getByCompanyUserId(companyUser.getId());
                if(salesman != null && !(protect.getApplyId().equals(salesman.getUserId()))){
                    //如果该对接人绑定的业务员不是申请保护的业务员，拿过来
                    Map temp = new HashMap();
                    temp.put("typeOut", IConst.TRANSFER_TYPE_OUT_PROTECT);
//                    temp.put("remark", "");
                    temp.put("loginUserId", user.getId());
                    temp.put("companyUserId", companyUser.getId());
                    companyUserSalesmanMapper.expireSalesmanSingle(temp);

                    CrmCompanyUserSalesman sale = new CrmCompanyUserSalesman();
                    sale.setCompanyUserId(companyUser.getId());
                    sale.setTypeIn(IConst.TRANSFER_TYPE_IN_PROTECT);
                    sale.setUserId(protect.getApplyId());
                    sale.setUserName(protect.getApplyName());
                    sale.setDeptId(protect.getApplyDeptId());
                    sale.setDeptName(protect.getApplyDeptName());
                    sale.setStartTime(date);
                    Calendar calendar=Calendar.getInstance();
                    calendar.set(2099, 11, 31, 23, 59, 59);  //年月日  也可以具体到时分秒如calendar.set(2015, 10, 12,11,32,52);
                    Date endTime=calendar.getTime();
                    sale.setEndTime(endTime);
                    sale.setState(IConst.COMMON_FLAG_TRUE);
                    sale.setDeleteFlag(IConst.COMMON_FLAG_FALSE);
                    sale.setCreator(user.getId());
                    sale.setCreateTime(date);
                    companyUserSalesmanMapper.insert(sale);
                }
            }
        }
    }
    @Override
    public void auditProtectFail(Map param){
        if (param.containsKey("protectId") && param.containsKey("companyId")) {
            Integer protectId = MapUtils.getInteger(param, "protectId");
            Map protectMap = crmCompanyMapper.getProtect(protectId);
            if(protectMap != null && protectMap.containsKey("protectLevel")){
                Map temp = new HashMap();
                temp.put("id", protectId);
                temp.put("loginUserId", AppUtil.getUser().getId());
                temp.put("state", 0);
                crmCompanyMapper.updateProtect(temp);

                Integer companyId = MapUtils.getInteger(param, "companyId");
                CrmCompany crmCompany = this.getById(companyId);
                crmCompany.setUpdateUserId(AppUtil.getUser().getId());
                crmCompany.setAuditFlag(IConst.COMMON_FLAG_FALSE);
                crmCompanyMapper.update(crmCompany);
                return ;
            }else{
                throw new QinFeiException(1001, "参数不正确，没有获取到保护申请记录！请刷新重试！");
            }
        }else {
            throw new QinFeiException(1001, "参数不正确，未获取到protectId或companyId！请刷新重试！");
        }
    }

    /**
     * 客户跟进列表
     *
     * @param map
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo trackList(Map map, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = crmCompanyMapper.trackList(map);
        return new PageInfo<>(list);
    }

    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }
    /**
     * 客户新增跟进
     * @param map
     * @param files
     * @param pics
     * @return
     */
    @Override
    public int saveCompanyTrack(Map map, MultipartFile[] files, MultipartFile[] pics) {
        Integer companyUserId = MapUtils.getInteger(map, "companyUserId");
        if(companyUserId == null){
            throw new QinFeiException(1002, "未获取到对接人id");
        }
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
        try {
            //将文件名和路径拼装成字符串
            for (MultipartFile multipartFile : files) {
                if (multipartFile.getSize() > 0) {
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath =getStringData()+ "crmcompany/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    fileNames.add(multipartFile.getOriginalFilename());
                    filePaths.add(config.getWebDir() + childPath + fileName);
                }
            }
            //将图片
            for (MultipartFile pic : pics) {
                if (pic.getSize() > 0) {
                    String temp = pic.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath =getStringData()+ "crmcompany/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    pic.transferTo(destFile);
                    picNames.add(pic.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                }
            }
            User user = AppUtil.getUser();
            map.put("creator", user.getId());
            map.put("affixName", fileNames.toString().replaceAll("\\[|\\]", ""));
            map.put("affixLink", filePaths.toString().replaceAll("\\[|\\]", ""));
            map.put("imageName", picNames.toString().replaceAll("\\[|\\]", ""));
            map.put("imageLink", picPaths.toString().replaceAll("\\[|\\]", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        CrmCompany crmCompany = crmCompanyMapper.getByCompanyUserId(companyUserId);
        Integer num = 0;
        if(crmCompany != null && crmCompany.getProtectLevel() > IConst.PROTECT_LEVEL_C){//A类或者B类保护，跟进了一个就跟进了所有对接人
            List<CrmCompanyUser> list = companyUserMapper.listByCompanyId(crmCompany.getId());
            if(list != null && list.size() > 0){
                for(int i=0; i<list.size(); i++){
                    CrmCompanyUser companyUser =  list.get(i);
                    map.put("companyUserId", companyUser.getId());
                    num += crmCompanyMapper.saveCompanyTrack(map);
                    //跟进时间同步到对接人表
                    companyUser.setEvalTime(new Date());
                    companyUser.setUpdateUserId(AppUtil.getUser().getId());
                    companyUserMapper.update(companyUser);
                }
            }
        }else{
            num = crmCompanyMapper.saveCompanyTrack(map);
            //跟进时间同步到对接人表
            CrmCompanyUser companyUser =  companyUserMapper.getById(companyUserId);
            if(companyUser != null){
                companyUser.setEvalTime(new Date());
                companyUser.setUpdateUserId(AppUtil.getUser().getId());
                companyUserMapper.update(companyUser);
            }
        }
        return num;
    }

    @Override
    public PageInfo queryHistoryCompanyId(Integer companyId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Map map = new HashMap();
        map.put("companyId", companyId);
        List<Map> list = crmCompanyHistoryMapper.listPg(map);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional
    public void doWithRepeatName(){
        List<Map> list = crmCompanyMapper.getRepeadCompanyName();
        if(list != null && list.size() > 0){
            for(Map map : list){
                Integer oldId = MapUtils.getInteger(map, "id");
                String name = MapUtils.getString(map, "name");
                List<CrmCompany> lists = crmCompanyMapper.getEarliestByName(name);
                for(CrmCompany crmCompany : lists){
                    if(!oldId.equals(crmCompany.getId())){//如果不是
                        crmCompanyMapper.deleteById(crmCompany.getId());
                        int a = crmCompanyMapper.updateUserCompanyId(oldId, crmCompany.getId());
                        int b = crmCompanyMapper.updateOrderCompanyId(oldId, crmCompany.getId());
                        int c = crmCompanyMapper.updateInvoiceCompanyId(oldId, crmCompany.getId());
                        int d = crmCompanyMapper.updateRefundCompanyId(oldId, crmCompany.getId());
                        System.out.println("公司名："+name + "，历史id="+oldId+",新id"+crmCompany.getId()+"更新数据：对接人：" + a + "条，订单：" + b + "条，开票：" + c + "条，退款：" + d);
                    }
                }
            }
        }
    }
}