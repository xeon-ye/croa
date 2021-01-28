package com.qinfei.qferp.service.impl.standardized;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.crm.CrmCompany;
import com.qinfei.qferp.entity.crm.CrmSearchCache;
import com.qinfei.qferp.entity.standardized.StandardizedCompany;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.crm.CrmCompanyMapper;
import com.qinfei.qferp.mapper.crm.CrmSearchCacheMapper;
import com.qinfei.qferp.mapper.standardized.StandardizedCompanyMapper;
import com.qinfei.qferp.service.crm.ICrmCompanyService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.service.outapi.ICompanyService;
import com.qinfei.qferp.service.standardized.IStandardizedCompanyService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author: 66
 * @Date: 2020/11/13 10:00
 * @Description: 标准化公司申请业务层
 */
@Service
public class StandardizedCompanyService implements IStandardizedCompanyService {

    @Resource
    StandardizedCompanyMapper standardizedCompanyMapper;

    @Resource
    ICompanyService companyService;

    @Resource
    ItemsService itemsService;

    @Resource
    CrmSearchCacheMapper crmSearchCacheMapper;

    @Resource
    IProcessService processService;

    @Resource
    UserService userService;

    @Resource
    ICrmCompanyService crmCompanyService;

    @Resource
    CrmCompanyMapper crmCompanyMapper;

    /**
     * 保存标准化公司
     *
     * @param standardizedCompany 入参
     * @param user                登录用户
     * @return 数据库影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveStandardizedCompany(StandardizedCompany standardizedCompany, User user) {
        int i;
        checkCompanyName(standardizedCompany);
        standardizedCompany.setApplyId(user.getId());
        standardizedCompany.setApplyName(user.getName());
        standardizedCompany.setApplyTime(DateUtils.now());
        standardizedCompany.setCompanyCode(user.getCompanyCode());
        // 不为空时，为修改
        if (standardizedCompany.getId() != null) {
            i = updateStandardizedCompany(standardizedCompany, user);
        } else {
            standardizedCompany.setDeptId(user.getDeptId());
            standardizedCompany.setDeptName(user.getDeptName());
            standardizedCompany.setCreateUserId(user.getId());
            standardizedCompany.setCreateTime(DateUtils.now());
            standardizedCompany.setNo(IConst.STANDARDIZED_COMPANY_APPLICATION + CodeUtil.getMonthStr() +
                    CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.STANDARDIZED_COMPANY_APPLICATION), 4));
            i = standardizedCompanyMapper.insert(standardizedCompany);
        }
        // 状态不为保存时添加流程
        if (standardizedCompany.getState() > 0) {
            processService.addStandardizedCompanyProcess(standardizedCompany, Const.ITEM_J3);
        }
        return i;
    }

    private void checkCompanyName(StandardizedCompany standardizedCompany) {
        // 原有名称标识位
        boolean flag = true;
        if (standardizedCompany.getId() != null) {
            StandardizedCompany byId = findById(standardizedCompany.getId());
            if (Objects.equals(byId.getCompanyName(), standardizedCompany.getCompanyName())) {
                flag = false;
            }
        }
        if (standardizedCompanyMapper.checkCompanyNameIsExit(standardizedCompany.getCompanyName()) && flag) {
            throw new QinFeiException(1002, "该公司已申请，请不要反复录入！");
        }
        ResponseData responseData = companyService.checkCompany(standardizedCompany.getCompanyName());
        if (responseData.getCode() == 200) {
            throw new QinFeiException(1002, "该公司是标准化公司，无需申请");
        }
    }

    /**
     * 修改标准化公司
     *
     * @param standardizedCompany 入参
     * @param user                登录用户
     * @return 数据库影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStandardizedCompany(StandardizedCompany standardizedCompany, User user) {
        standardizedCompany.setUpdateUserId(user.getId());
        standardizedCompany.setUpdateTime(DateUtils.now());
        return standardizedCompanyMapper.update(standardizedCompany);
    }

    /**
     * 标准化公司分页列表
     *
     * @param map 查询条件
     * @return 列表
     */
    @Override
    public List<StandardizedCompany> findList(Map map) {
        checkPermissions(map);
        return standardizedCompanyMapper.findList(map);
    }

    /**
     * 检查权限
     *
     * @param param 入参
     */
    private void checkPermissions(Map param) {
        User user = AppUtil.getUser();
        param.put("code", user.getDept().getCode());
        param.put("userId", user.getId());
        param.put("isMgr", user.getIsMgr());
        //当且仅指定了部门时
        if (param.get("deptId") != null) {
            Integer deptId = Integer.parseInt(String.valueOf(param.get("deptId")));//获取请求的部门ID
            String deptIds = userService.getChilds(deptId);
            if (deptIds.indexOf("$,") > -1) {
                deptIds = deptIds.substring(2);
            }
            //下面的用途 是将 角色为 非系统管理部  指定部门 与 用户所有部门及子部门id 取交集
            if (!IConst.DEPT_CODE_XT.equals(user.getDept().getCode())) {
                Set deptIdSet = user.getDeptIdSet();
                Set deptContain = new HashSet();
                deptContain.addAll(Arrays.asList(deptIds.split(",")));
                deptContain.retainAll(deptIdSet);
                deptIds = (deptContain.size() > 0) ? StringUtils.join(deptContain, ",") : "-1";
            }
            param.put("deptIds", deptIds);
        }
    }

    /**
     * 根据id查询标准化公司
     *
     * @param id 标准化公司id
     * @return 标准化公司
     */
    @Override
    public StandardizedCompany findById(Integer id) {
        return standardizedCompanyMapper.get(StandardizedCompany.class, id);
    }

    /**
     * 删除标准化公司
     *
     * @param id 实例对象
     * @return 数据库影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delStandardizedCompany(Integer id) {
        StandardizedCompany byId = findById(id);
        int i = 0;
        if (byId != null) {
            i = standardizedCompanyMapper.delById(StandardizedCompany.class, id);
            //待办变已办
            finishItem(byId.getItemId());
        }
        return i;
    }


    private int insertCrmSearchCache(String companyName) {
        CrmSearchCache searchCache = new CrmSearchCache();
        searchCache.setCompanyName(companyName);
        searchCache.setSearchKeyword(companyName);
        searchCache.setCompanyLegal("系统插入");
        return crmSearchCacheMapper.save(searchCache);
    }

    /**
     * 修改 crm客户公司表 对接人信息表 插入企查查表
     *
     * @param companyName 公司名
     * @return 数据库影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompany(String companyName) {
        int i = 0;
        // 修改 crm客户公司表 修改为标准公司
        CrmCompany crmCompany = crmCompanyService.getByName(companyName);
        Map<String, Object> map = new HashMap<>();
        map.put("id", crmCompany.getId());
        map.put("standardize", true);
        crmCompanyService.updateCompany(map);

        // 修改 对接人信息表
        i += crmCompanyMapper.updateByNormalize(crmCompany.getId());

        // 插入企查查表
        i += insertCrmSearchCache(companyName);
        return i;
    }

    //待办变已办
    private void finishItem(Integer itemId) {
        if (itemId != null) {
            Items items = new Items();
            items.setId(itemId);
            itemsService.finishItems(items);
        }
    }
}
