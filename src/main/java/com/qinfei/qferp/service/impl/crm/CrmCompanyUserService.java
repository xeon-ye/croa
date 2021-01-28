package com.qinfei.qferp.service.impl.crm;

import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.serivce.impl.DictService;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.crm.CrmCompany;
import com.qinfei.qferp.entity.crm.CrmCompanyUser;
import com.qinfei.qferp.entity.crm.CrmCompanyUserHistory;
import com.qinfei.qferp.entity.crm.CrmCompanyUserSalesman;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.crm.CrmCompanyMapper;
import com.qinfei.qferp.mapper.crm.CrmCompanyUserHistoryMapper;
import com.qinfei.qferp.mapper.crm.CrmCompanyUserMapper;
import com.qinfei.qferp.mapper.crm.CrmCompanyUserSalesmanMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.crm.ICrmCompanyUserService;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 表：对接人信息(TCrmCompanyUser)表服务实现类
 *
 * @author jca
 * @since 2020-07-09 09:55:34
 */
@Service
public class CrmCompanyUserService extends BaseService implements ICrmCompanyUserService {
    @Resource
    private CrmCompanyUserMapper crmCompanyUserMapper;
    @Resource
    private CrmCompanyUserSalesmanMapper companyUserSalesmanMapper;
    @Resource
    private CrmCompanyMapper crmCompanyMapper;
    @Resource
    private CrmCompanyUserHistoryMapper crmCompanyUserHistoryMapper;
    @Resource
    private DictService dictService;
    @Resource
    private UserService userService;

    /**
     * 查询列表
     */
    @Override
    public PageInfo list(Map map, int pageNum, int pageSize) {
        User user = AppUtil.getUser();
        PageHelper.startPage(pageNum, pageSize);
        //业务员只能查看到自己的
        if (IConst.DEPT_CODE_YW.equals(user.getDept().getCode()) && !user.getCurrentDeptQx()) {
            map.put("userId", user.getId());
            if (map.containsKey("deptId")) {
                map.remove("deptId");
            }
        }

        if (map.get("deptId") != null) {
            Integer deptId = MapUtils.getInteger(map, "deptId");
            deptId = deptId == null ? user.getDeptId() : deptId;
            String deptIds = "";
            if (deptId != null) {
                deptIds = userService.getChilds(deptId);
                if (deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                }
                map.put("deptIds", deptIds);
            }
        }

        //处理排序字段
        if (map.get("sidx") != null) {
            String sidx = MapUtils.getString(map, "sidx");
            switch (sidx) {
                case "companyName":
                    map.put("sidx", "cc.name");
                    break;
                case "type":
                    map.put("sidx", "cc.type");
                    break;
                case "standardize":
                    map.put("sidx", "cc.standardize");
                    break;
                case "auditState":
                    map.put("sidx", "cc.audit_flag");
                    break;
                case "companyUserName":
                    map.put("sidx", "ccu.name");
                    break;
                case "normalize":
                    map.put("sidx", "ccu.normalize");
                    break;
                case "protectLevel":
                    map.put("sidx", "ccu.protect_level");
                    break;
                case "ywUserName":
                    map.put("sidx", "ccus.user_name");
                    break;
                case "trackLimit":
                    map.put("sidx", "ccu.protect_strong desc, ccu.eval_time");
                    break;
                case "dealLimit":
                    map.put("sidx", "ccu.protect_strong desc, ccu.deal_time");
                    break;
                case "startTime":
                    map.put("sidx", "ccus.start_time");
                    break;
                case "endTime":
                    map.put("sidx", "ccus.end_time");
                    break;

                default:
                    map.put("sidx", "ccu.protect_strong desc, ccu.eval_time");
                    break;
            }
        }

        //客户考核参数
        List<Dict> dictList = dictService.listByTypeCode(IConst.CUST_TRANSFER);
        Integer TRACK_EVAL_DAY = 30;//跟进考核天数
        Integer DEAL_EVAL_DAY = 90;//成交考核天数
        Integer EVAL_REMIND_DAY = 3;//考核提醒提前天数
        if (dictList != null && dictList.size() > 0) {
            for (int i = 0; i < dictList.size(); i++) {
                Dict dict = dictList.get(i);
                switch (dict.getCode()) {
                    case IConst.TRACK_EVAL_DAY:
                        TRACK_EVAL_DAY = Integer.parseInt(dict.getType());
                        break;
                    case IConst.DEAL_EVAL_DAY:
                        DEAL_EVAL_DAY = Integer.parseInt(dict.getType());
                        break;
                    case IConst.EVAL_REMIND_DAY:
                        EVAL_REMIND_DAY = Integer.parseInt(dict.getType());
                        break;
                    default:
                        break;
                }
            }
        }
        map.put("TRACK_EVAL_DAY", TRACK_EVAL_DAY);
        map.put("DEAL_EVAL_DAY", DEAL_EVAL_DAY);
        map.put("EVAL_REMIND_DAY", EVAL_REMIND_DAY);

        if (map.get("mobile") != null && map.get("mobile") != "") {
            map.put("mobile", EncryptUtils.encrypt(map.get("mobile").toString().trim()));
        }
        if (map.get("wechat") != null && map.get("wechat") != "") {
            map.put("wechat", EncryptUtils.encrypt(map.get("wechat").toString().trim()));
        }
        if (map.get("qq") != null && map.get("qq") != "") {
            map.put("qq", EncryptUtils.encrypt(map.get("qq").toString().trim()));
        }
        List<Map> list = crmCompanyUserMapper.listPg(map);

        for (Map temp : list) {
            Integer companyUserId = MapUtils.getInteger(temp, "companyUserId");
            Integer count = crmCompanyUserMapper.countArticleByCompanyUserId(companyUserId);
            temp.put("dealFlag", count > 0 ? true : false);
        }
        return new PageInfo<>(list);
    }

    /**
     * 查询列表
     */
    @Override
    public PageInfo listPublic(Map map, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        map.put("loginDeptId", AppUtil.getUser().getDeptId());
        List<Map> list = crmCompanyUserMapper.listPublic(map);
        return new PageInfo<>(list);
    }

    /**
     * 查询列表
     *
     * @param map        入参
     * @param pageNumber 当前页
     * @param pageSize   最大条数
     * @return 结果集
     */
    @Override
    public PageInfo listInquire(Map map, int pageNumber, int pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<Map> list = crmCompanyUserMapper.listInquire(map);
        return new PageInfo<>(list);
    }

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CrmCompanyUser getById(Integer id) {
        return this.crmCompanyUserMapper.getById(id);
    }

    @Override
    public Boolean getYwFlag(Integer companyUserId, Integer userId) {
        Integer ywUserId = crmCompanyUserMapper.getYwInfo(companyUserId, userId);
        if (ywUserId > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Map getBasicById(Integer id) {
        return crmCompanyUserMapper.getBasicById(id);
    }

    @Override
    @Transactional
    public int updateCompanyUser(Map map) {
        CrmCompanyUser companyUser = crmCompanyUserMapper.getById(MapUtils.getInteger(map, "id"));
        CrmCompany crmCompany = crmCompanyMapper.getById(companyUser.getCompanyId());
        User user = AppUtil.getUser();

        // 存companyUser历史表
        saveCompanyUserHistory(companyUser, crmCompany.getName(), user.getId());

        map.put("loginUserId", user.getId());

        if (map.get("mobile") != null) {
            map.put("mobile", EncryptUtils.encrypt(map.get("mobile").toString()));
        }
        if (map.get("wechat") != null) {
            map.put("wechat", EncryptUtils.encrypt(map.get("wechat").toString()));
        }
        if (map.get("qq") != null) {
            map.put("qq", EncryptUtils.encrypt(map.get("qq").toString()));
        }
        if (map.get("phone") != null) {
            map.put("phone", EncryptUtils.encrypt(map.get("phone").toString()));
        }
        return crmCompanyUserMapper.updateCompanyUser(map);
    }

    @Override
    public void saveCompanyUserHistory(CrmCompanyUser companyUser, String oldCompanyName, Integer userId) {
        CrmCompanyUserHistory crmCompanyUserHistory = new CrmCompanyUserHistory();
        BeanUtils.copyProperties(companyUser, crmCompanyUserHistory);
        crmCompanyUserHistory.setId(null);
        crmCompanyUserHistory.setCompanyUserId(companyUser.getId());
        crmCompanyUserHistory.setCompanyName(oldCompanyName);
        crmCompanyUserHistory.setCreateTime(new Date());
        crmCompanyUserHistory.setCreator(userId);
        crmCompanyUserHistoryMapper.insert(crmCompanyUserHistory);
    }

    @Override
    @Transactional
    public void savePublic(Integer id) {
        CrmCompanyUser companyUser = this.getById(id);
        User user = AppUtil.getUser();
        Integer loginUserId;
        if (ObjectUtils.isEmpty(user)) {
            loginUserId = 1;
        } else {
            loginUserId = user.getId();
        }
        if (companyUser.getProtectLevel() > 1) {
            CrmCompany crmCompany = crmCompanyMapper.getById(companyUser.getCompanyId());
            if (IConst.COMMON_FLAG_TRUE.equals(crmCompany.getAuditFlag())) {
                throw new QinFeiException(1002, "该客户公司正在保护审核中，无法抛入公海！");
            }
            crmCompany.setProtectLevel(IConst.PROTECT_LEVEL_C);
            crmCompanyMapper.update(crmCompany);
            //如果是A、B类保护，把其他对接人的保护状态也要解除
            List<Map> list = crmCompanyUserMapper.listByCompanyIdAndState(companyUser.getCompanyId());
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    Map temp = list.get(i);
                    temp.put("isPublic", IConst.COMMON_FLAG_TRUE);
                    temp.put("isPublicState", IConst.COMMON_FLAG_FALSE);
                    temp.put("loginUserId", loginUserId);
                    crmCompanyUserMapper.savePublic(temp);

                    CrmCompanyUserSalesman salesman = new CrmCompanyUserSalesman();
                    if (!temp.containsKey("salesmanId")) {
                        throw new QinFeiException(1002, "未获取到对接人关联的业务员，对接人id=" + MapUtils.getInteger(temp, "id"));
                    }
                    salesman.setId(MapUtils.getInteger(temp, "salesmanId"));
                    salesman.setTypeOut(IConst.TRANSFER_TYPE_OUT_PUBLIC);
                    salesman.setUpdateUserId(loginUserId);
                    salesman.setState(IConst.COMMON_FLAG_FALSE);
                    salesman.setEndTime(new Date());
                    companyUserSalesmanMapper.update(salesman);
                }
            }
        } else {
            Map map = crmCompanyUserMapper.getByIdAndState(companyUser.getId());
            map.put("isPublic", IConst.COMMON_FLAG_TRUE);
            map.put("isPublicState", IConst.COMMON_FLAG_FALSE);
            map.put("loginUserId", loginUserId);
            crmCompanyUserMapper.savePublic(map);

            CrmCompanyUserSalesman salesman = new CrmCompanyUserSalesman();
            if (!map.containsKey("salesmanId")) {
                throw new QinFeiException(1002, "未获取到对接人关联的业务员，对接人id=" + companyUser.getId());
            }
            salesman.setId(MapUtils.getInteger(map, "salesmanId"));
            salesman.setTypeOut(IConst.TRANSFER_TYPE_OUT_PUBLIC);
            salesman.setState(IConst.COMMON_FLAG_FALSE);
            salesman.setUpdateUserId(loginUserId);
            salesman.setEndTime(new Date());
            companyUserSalesmanMapper.update(salesman);
        }
    }

    @Override
    @Transactional
    public synchronized void bind(Integer id) {
        User user = AppUtil.getUser();
        //先查一下是不是被别人领取了
        Map map = crmCompanyUserMapper.getByIdAndState(id);
        if (!MapUtils.isEmpty(map)) {
            throw new QinFeiException(1002, "该客户对接人已被其他业务员领取，无法重复领取，请刷新重试！");
        }
        Map result = crmCompanyUserMapper.getBasicById(id);
        Integer companyProtectLevel = MapUtils.getInteger(result, "companyProtectLevel");
        if (companyProtectLevel > IConst.PROTECT_LEVEL_C) {
            throw new QinFeiException(1002, "该客户公司是A类或B类保护客户，无法认领！");
        }
        Integer standardize = MapUtils.getInteger(result, "standardize");
        Integer normalize = MapUtils.getInteger(result, "normalize");
        //每天领取的客户数量不能超过限定
        Integer existCount = crmCompanyUserMapper.countByUserIdToday(AppUtil.getUser().getId());//查询今天认领了多少客户
        Dict param = new Dict();
        param.setTypeCode(IConst.CUST_TRANSFER);
        param.setCode(IConst.CLAIM_TIMES_DAY);
        Dict tempDict = dictService.getByTypeCodeAndCode(param);
        if (ObjectUtils.isEmpty(tempDict)) {
            throw new QinFeiException(1002, "未获取到字典表数据：客户认领单天限额！");
        }
        Integer count = Integer.parseInt(tempDict.getType());
        if (!(existCount < count)) {
            throw new QinFeiException(1002, "每日认领客户限额：" + count + "个，已认领：" + existCount + "个，不能再领取了！");
        }

        //如果该客户是C类，不能超过C类保护限制
        if (IConst.COMMON_FLAG_TRUE.equals(standardize) && IConst.COMMON_FLAG_TRUE.equals(normalize)) {//规范且标准，默认C类保护
            Dict temp = new Dict();
            temp.setTypeCode(IConst.CUST_PROTECT_C);
            temp.setCode(IConst.CUST_PROTECT_NUM_C);
            Dict dict = dictService.getByTypeCodeAndCode(temp);
            if (ObjectUtils.isEmpty(dict)) {
                throw new QinFeiException(1002, "未获取到字典表数据：C类客户数量！");
            }
            Integer num = Integer.parseInt(dict.getType());//C类客户数量限制
            //如果是管理员来改客户，那user.getId()就是管理员的id了，existNum一直等于0，就一直可以改，相当于不限制
            Integer existNum = crmCompanyUserMapper.countByUserIdAndStateAndLevel(user.getId(), IConst.PROTECT_LEVEL_C);
            if (!(existNum < num)) {
                throw new QinFeiException(1002, "您有C类保护客户名额：" + num + "个，已有客户：" + existNum + "个，不能再领取C类保护客户了！");
            }
        }
        //通过验证了，开始领取客户，step1：重置考核时间
        CrmCompanyUser companyUser = new CrmCompanyUser();
        companyUser.setId(id);
        Date date = new Date();
        companyUser.setEvalTime(date);
        companyUser.setDealTime(date);
        if (IConst.COMMON_FLAG_TRUE.equals(standardize) && IConst.COMMON_FLAG_TRUE.equals(normalize)) {//规范且标准，默认C类保护
            companyUser.setProtectStrong(IConst.COMMON_FLAG_TRUE);
            companyUser.setProtectLevel(IConst.PROTECT_LEVEL_C);
        } else {
            companyUser.setProtectLevel(IConst.PROTECT_LEVEL_D);
        }
        companyUser.setIsPublic(IConst.COMMON_FLAG_FALSE);
        companyUser.setIsPublicState(IConst.COMMON_FLAG_FALSE);
        companyUser.setUpdateUserId(user.getId());
        crmCompanyUserMapper.update(companyUser);

        //通过验证了，开始领取客户，step2：关联新的业务员
        CrmCompanyUserSalesman salesman = new CrmCompanyUserSalesman();
        salesman.setCompanyUserId(id);
        salesman.setTypeIn(IConst.TRANSFER_TYPE_IN_CLAIM);
        salesman.setUserId(user.getId());
        salesman.setUserName(user.getName());
        salesman.setDeptId(user.getDeptId());
        salesman.setDeptName(user.getDeptName());
        salesman.setStartTime(date);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2099, 11, 31, 23, 59, 59);  //年月日  也可以具体到时分秒如calendar.set(2015, 10, 12,11,32,52);
        calendar.set(Calendar.MILLISECOND, 0);
        Date endTime = calendar.getTime();
        salesman.setEndTime(endTime);

        salesman.setState(IConst.COMMON_FLAG_TRUE);
        salesman.setDeleteFlag(IConst.COMMON_FLAG_FALSE);
        salesman.setCreator(user.getId());
        salesman.setCreateTime(date);
        companyUserSalesmanMapper.insert(salesman);
    }

    @Override
    @Transactional
    public int delCompanyUser(Map map) {
        Integer id = MapUtils.getInteger(map, "id");
        User user = AppUtil.getUser();
        CrmCompany crmCompany = crmCompanyMapper.getByUserId(id);
        if (crmCompany.getAuditFlag() > 0) {
            throw new QinFeiException(1002, "该客户正在保护审核中，请等待审核完成后刷新重试！");
        }
        //先判断是否有稿件，有稿件不能删除
        Integer count = crmCompanyUserMapper.countArticleByCompanyUserId(id);
        if (count > 0) {
            throw new QinFeiException(1002, "该客户已有稿件，无法删除，如不需要，请抛入公海！");
        }

        CrmCompanyUserSalesman salesman = companyUserSalesmanMapper.getByCompanyUserId(id);
        if (salesman == null) {
            throw new QinFeiException(1002, "该客户关联的业务员不存在，请刷新重试！");
        }

        if (!user.getId().equals(salesman.getUserId())) {
            throw new QinFeiException(1002, "只有该客户关联的的业务员才能删除客户！");
        }
        //把对接人对应的业务员删掉
        salesman.setTypeOut(IConst.TRANSFER_TYPE_OUT_DEL);
        salesman.setState(IConst.COMMON_FLAG_FALSE);
        salesman.setEndTime(new Date());
        salesman.setUpdateUserId(user.getId());
        salesman.setDeleteFlag(IConst.COMMON_FLAG_TRUE);
        companyUserSalesmanMapper.update(salesman);
        //把对接人表数据删掉
        map.put("loginUserId", user.getId());
        map.put("deleteFlag", IConst.COMMON_FLAG_TRUE);
        return crmCompanyUserMapper.updateCompanyUser(map);
    }

    @Override
    @Transactional
    public void saveTransfer(Map param) {
        User user = AppUtil.getUser();
        Integer loginUserId = user.getId();
        if (!param.containsKey("ids")) {
            throw new QinFeiException(1002, "未获取到客户对接人信息！请刷新重试！");
        }
        if (!(param.containsKey("deptId") && param.containsKey("deptName") && param.containsKey("userId") && param.containsKey("userName"))) {
            throw new QinFeiException(1002, "没有获取到对接人或业务员信息！请刷新重试！");
        }
        Integer deptId = MapUtils.getInteger(param, "deptId");
        String deptName = MapUtils.getString(param, "deptName");
        Integer userId = MapUtils.getInteger(param, "userId");
        String userName = MapUtils.getString(param, "userName");
        String remark = MapUtils.getString(param, "remark");
        String ids = MapUtils.getString(param, "ids");
        List<Map> list = new ArrayList<>();

        if (ids.indexOf(",") > -1) {
            String idss[] = ids.split(",");
            for (int i = 0; i < idss.length; i++) {
                Map temp = new HashMap();
                Integer companyUserId;
                try {
                    companyUserId = Integer.parseInt(idss[i]);
                } catch (Exception e) {
                    throw new QinFeiException(1002, "获取对接人id出错，对接人id=" + idss[i] + "，请刷新重试！");
                }
                Map basicMap = crmCompanyUserMapper.getBasicById(companyUserId);
                Integer protectLevel = MapUtils.getInteger(basicMap, "protectLevel");
                if (IConst.PROTECT_LEVEL_A.equals(protectLevel) || IConst.PROTECT_LEVEL_B.equals(protectLevel)) {
                    throw new QinFeiException(1002, "选中的客户有A类或B类客户，并且选中了多个对接人，A、B类客户移交时请只勾选一个对接人！");
                }
                if (IConst.COMMON_FLAG_TRUE.equals(MapUtils.getInteger(basicMap, "auditFlag"))) {
                    throw new QinFeiException(1002, "选中的客户正在保护审核中！");
                }
                CrmCompanyUserSalesman salesman = companyUserSalesmanMapper.getByCompanyUserId(companyUserId);
                if (salesman == null) {
                    throw new QinFeiException(1002, "未获取到该对接人绑定的业务员，对接人id=" + idss[i] + "，请刷新重试！");
                }
                temp.put("companyUserId", companyUserId);
                temp.put("userId", userId);
                temp.put("userName", userName);
                temp.put("deptId", deptId);
                temp.put("deptName", deptName);
                temp.put("loginUserId", loginUserId);
                temp.put("typeIn", IConst.TRANSFER_TYPE_IN_POINT);
                list.add(temp);
            }
        } else {
            Map temp = new HashMap();
            Integer companyUserId;
            try {
                companyUserId = Integer.parseInt(ids);
            } catch (Exception e) {
                throw new QinFeiException(1002, "获取对接人id出错，对接人id=" + ids + "，请刷新重试！");
            }
            Map basicMap = crmCompanyUserMapper.getBasicById(companyUserId);
            Integer protectLevel = MapUtils.getInteger(basicMap, "protectLevel");
            Integer companyId = MapUtils.getInteger(basicMap, "companyId");
            if (IConst.PROTECT_LEVEL_A.equals(protectLevel) || IConst.PROTECT_LEVEL_B.equals(protectLevel)) {
                //如果有A、B类保护，要把其他对接人都移交
                List<CrmCompanyUser> userList = crmCompanyUserMapper.listByCompanyId(companyId);
                if (userList != null && userList.size() > 0) {
                    for (CrmCompanyUser data : userList) {
                        Map tempMap = new HashMap();
                        tempMap.put("companyUserId", data.getId());
                        tempMap.put("userId", userId);
                        tempMap.put("userName", userName);
                        tempMap.put("deptId", deptId);
                        tempMap.put("deptName", deptName);
                        tempMap.put("typeIn", IConst.TRANSFER_TYPE_IN_POINT);
                        tempMap.put("loginUserId", loginUserId);
                        list.add(tempMap);
                    }
                }
            } else {
                CrmCompanyUserSalesman salesman = companyUserSalesmanMapper.getByCompanyUserId(companyUserId);
                if (salesman == null) {
                    throw new QinFeiException(1002, "未获取到该对接人绑定的业务员，对接人id=" + ids + "，请刷新重试！");
                }
                temp.put("companyUserId", companyUserId);
                temp.put("userId", userId);
                temp.put("userName", userName);
                temp.put("deptId", deptId);
                temp.put("deptName", deptName);
                temp.put("typeIn", IConst.TRANSFER_TYPE_IN_POINT);
                temp.put("loginUserId", loginUserId);
                list.add(temp);
            }
        }

        if (list == null || list.size() == 0) {
            throw new QinFeiException(1002, "对接人信息不正确！请刷新重试！");
        }

        Integer count = crmCompanyUserMapper.getAuditCount(list);
        if (count > 0) {
            throw new QinFeiException(1002, "选中的客户有保护审核中的客户，请等待审核完成后流转！");
        }

        //把对接人原有的负责人有效期过期
        Map temp = new HashMap();
        temp.put("list", list);
        temp.put("loginUserId", user.getId());
        temp.put("typeOut", IConst.TRANSFER_TYPE_OUT_POINT);
        temp.put("remark", remark);
        companyUserSalesmanMapper.expireSalesman(temp);
        //新增新的负责人
        companyUserSalesmanMapper.saveBatch(list);
    }

    @Override
    public Map queryUserBasicInfoByCompanyId(Integer companyUserId, Integer loginUserId) {
        return crmCompanyUserMapper.queryUserBasicInfoByCompanyId(companyUserId, loginUserId);
    }

    @Override
    public List<CrmCompanyUser> queryByCompanyIdAndNormalize(Integer companyId, Integer normalize) {
        return crmCompanyUserMapper.queryByCompanyIdAndNormalize(companyId, normalize);
    }

    @Override
    public PageInfo queryUserByCompanyId(Map map, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = crmCompanyUserMapper.queryUserByCompanyId(map);
        if (list != null && list.size() > 0) {
            for (Map temp : list) {
                Object mobile = temp.get("mobile");
                if (mobile != null && mobile.toString().length() > 15) {
                    temp.put("mobile", EncryptUtils.decrypt(mobile.toString()));
                }
                Object wechat = temp.get("wechat");
                if (wechat != null && wechat.toString().length() > 15) {
                    temp.put("wechat", EncryptUtils.decrypt(wechat.toString()));
                }
                Object qq = temp.get("qq");
                if (qq != null && qq.toString().length() > 15) {
                    temp.put("qq", EncryptUtils.decrypt(qq.toString()));
                }
            }
        }
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo querySalesmanByCompanyUserId(Integer companyUserId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = companyUserSalesmanMapper.queryByCompanyUserId(companyUserId);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo queryHistoryCompanyUserId(Integer companyUserId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Map map = new HashMap();
        map.put("companyUserId", companyUserId);
        List<Map> list = crmCompanyUserHistoryMapper.listPg(map);
        if (list != null && list.size() > 0) {
            for (Map temp : list) {
                Object mobile = temp.get("mobile");
                if (mobile != null && mobile.toString().length() > 15) {
                    temp.put("mobile", EncryptUtils.decrypt(mobile.toString()));
                }
                Object wechat = temp.get("wechat");
                if (wechat != null && wechat.toString().length() > 15) {
                    temp.put("wechat", EncryptUtils.decrypt(wechat.toString()));
                }
                Object qq = temp.get("qq");
                if (qq != null && qq.toString().length() > 15) {
                    temp.put("qq", EncryptUtils.decrypt(qq.toString()));
                }
                Object phone = temp.get("phone");
                if (phone != null && phone.toString().length() > 15) {
                    temp.put("phone", EncryptUtils.decrypt(phone.toString()));
                }
            }
        }
        return new PageInfo<>(list);
    }

    @Override
    public void doWithPublic() {
        crmCompanyUserMapper.doWithPublic();
    }


    @Override
    @Transactional
    public void doWithEval() {
        List<Dict> dictList = dictService.listByTypeCode(IConst.CUST_TRANSFER);
        Integer TRACK_EVAL_DAY = 30;//跟进考核天数
        Integer DEAL_EVAL_DAY = 90;//成交考核天数
        if (dictList != null && dictList.size() > 0) {
            for (int i = 0; i < dictList.size(); i++) {
                Dict dict = dictList.get(i);
                switch (dict.getCode()) {
                    case IConst.TRACK_EVAL_DAY:
                        TRACK_EVAL_DAY = Integer.parseInt(dict.getType());
                        break;
                    case IConst.DEAL_EVAL_DAY:
                        DEAL_EVAL_DAY = Integer.parseInt(dict.getType());
                        break;
                    default:
                        break;
                }
            }
        }

        Dict paramA = new Dict();
        paramA.setTypeCode(IConst.CUST_PROTECT_A);
        paramA.setCode("EVAL_A");
        Dict dictA = dictService.getByTypeCodeAndCode(paramA);
        if (dictA != null && IConst.COMMON_FLAG_TRUE.equals(Integer.parseInt(dictA.getType()))) {
            //A类客户考核
            evalCommon(3, TRACK_EVAL_DAY, DEAL_EVAL_DAY);
        }

        Dict paramB = new Dict();
        paramB.setTypeCode(IConst.CUST_PROTECT_B);
        paramB.setCode("EVAL_B");
        Dict dictB = dictService.getByTypeCodeAndCode(paramB);
        if (dictB != null && IConst.COMMON_FLAG_TRUE.equals(Integer.parseInt(dictB.getType()))) {
            //B类客户考核
            evalCommon(2, TRACK_EVAL_DAY, DEAL_EVAL_DAY);
        }

        Dict paramC = new Dict();
        paramC.setTypeCode(IConst.CUST_PROTECT_C);
        paramC.setCode("EVAL_C");
        Dict dictC = dictService.getByTypeCodeAndCode(paramC);
        if (dictC != null && IConst.COMMON_FLAG_TRUE.equals(Integer.parseInt(dictC.getType()))) {
            //C类客户考核
            evalCommon(1, TRACK_EVAL_DAY, DEAL_EVAL_DAY);
        }
    }

    private void evalCommon(Integer protectLevel, Integer TRACK_EVAL_DAY, Integer DEAL_EVAL_DAY) {
        //客户考核
        List<Map> list = crmCompanyUserMapper.queryByProtectLevel(protectLevel, TRACK_EVAL_DAY, DEAL_EVAL_DAY);
        //考核表增加数据
        for (int i = 0; i < list.size(); i++) {
            Map temp = list.get(i);
            Integer companyUserId = MapUtils.getInteger(temp, "id");
            Integer companyId = MapUtils.getInteger(temp, "companyId");
            Integer ywUserId = MapUtils.getInteger(temp, "userId");
            Integer ywDeptId = MapUtils.getInteger(temp, "deptId");
            Map map = new HashMap();
            map.put("company_user_id", companyUserId);
            map.put("user_id", ywUserId);

            map.put("track_time", temp.get("trackTime"));
            map.put("no_track_days", temp.get("noTrackDays"));
            map.put("track_limit_days", TRACK_EVAL_DAY);
            Integer trackFlag = MapUtils.getInteger(temp, "trackFlag");
            map.put("track_flag", trackFlag);

            map.put("deal_time", temp.get("dealTime"));
            map.put("no_deal_days", temp.get("noDealDays"));
            map.put("deal_limit_days", DEAL_EVAL_DAY);
            Integer dealFlag = MapUtils.getInteger(temp, "dealFlag");
            map.put("deal_flag", dealFlag);

            Boolean flag = false;
            if (trackFlag == 1 || dealFlag == 1) {
                flag = true;
                map.put("flag", IConst.COMMON_FLAG_TRUE);
            } else {
                map.put("flag", IConst.COMMON_FLAG_FALSE);
            }
            //插入评估表
            crmCompanyUserMapper.saveEval(map);

            if (flag) {//考核没通过
                CrmCompany crmCompany = crmCompanyMapper.getById(companyId);
                if (crmCompany.getProtectLevel() > IConst.PROTECT_LEVEL_C) {
                    crmCompany.setProtectLevel(IConst.PROTECT_LEVEL_C);
                    crmCompanyMapper.update(crmCompany);
                }
                CrmCompanyUser companyUser = new CrmCompanyUser();
                companyUser.setId(companyUserId);
                companyUser.setYwDeptId(ywDeptId);
                companyUser.setProtectLevel(IConst.PROTECT_LEVEL_D);
                companyUser.setProtectStrong(IConst.COMMON_FLAG_FALSE);
                companyUser.setUpdateUserId(0);
                companyUser.setIsPublic(IConst.COMMON_FLAG_TRUE);
                companyUser.setIsPublicState(IConst.COMMON_FLAG_FALSE);
                crmCompanyUserMapper.update(companyUser);

                saveCompanyUserHistory(companyUser, MapUtils.getString(temp, "companyName"), 0);

                CrmCompanyUserSalesman salesman = new CrmCompanyUserSalesman();
                salesman.setId(MapUtils.getInteger(temp, "salesmanId"));
                salesman.setTypeOut(IConst.TRANSFER_TYPE_OUT_EVAL);
                salesman.setUpdateUserId(0);
                salesman.setState(IConst.COMMON_FLAG_FALSE);
                salesman.setEndTime(new Date());
                companyUserSalesmanMapper.update(salesman);
            }
        }
    }

    @Override
    public PageInfo listCustForYW(Map map, int pageNum, int pageSize) {
        map.put("userId", AppUtil.getUser().getId());
        PageHelper.startPage(pageNum, pageSize);
        List<Map> list = crmCompanyUserMapper.listCustForYW(map);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo listCustForFee(Map map, int pageNum, int pageSize) {
        map.put("userId", AppUtil.getUser().getId());
        PageHelper.startPage(pageNum, pageSize);
        map.put("feeFlag", IConst.COMMON_FLAG_TRUE);
        List<Map> list = crmCompanyUserMapper.listCustForYW(map);
        return new PageInfo<>(list);
    }

    @Override
    public Map getCustByCompanyUserIdAndUserId(Integer companyUserId, Integer userId) {
        return crmCompanyUserMapper.getCustByCompanyUserIdAndUserId(companyUserId, userId);
    }

    @Override
    public List<Map> listCompanyUser(Map map) {
        User user = AppUtil.getUser();
        if (user.getCurrentDeptQx()) {
            Integer deptId = MapUtils.getInteger(map, "currentDeptId");
            deptId = deptId == null ? user.getDeptId() : deptId;
            String deptIds = "";
            if (deptId != null) {
                deptIds = userService.getChilds(deptId);
                if (deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                }
                map.put("deptIds", deptIds);
            }
        } else {
            //只统计当前用户自己的数据
            map.put("currentUserId", user.getId());
        }
        return crmCompanyUserMapper.listCompanyUser(map);
    }


}