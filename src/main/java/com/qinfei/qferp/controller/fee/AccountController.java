package com.qinfei.qferp.controller.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.fee.IIncomeService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/account")
@Api(description = "账户管理接口")
public class AccountController {

    @Autowired
    private IAccountService accountService;
    @Autowired
    private IIncomeService incomeService;

    @ResponseBody
    @RequestMapping("/listPg")
//    @Log(opType = OperateType.QUERY, module = "银行账户管理/账户查询", note = "银行账户管理/账户查询")
    //@Verify(code = "/account/listPg", module = "银行账户管理/账户查询", action = "4")
    public PageInfo<Map> listPg(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        PageInfo<Map> list = null ;
        try{
            if(!map.containsKey("typeQc")){
                return null ;
            }
            User user = AppUtil.getUser();
            List<Role> roles = Objects.requireNonNull(user).getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
                list = accountService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 通过账户id获取账户信息
     * 如果是公司账户，还要查询出该账户关联的部门
     *
     * @param id 账户id
     * @return
     */
    @RequestMapping(value = "/view")
//    @Log(opType = OperateType.QUERY, module = "银行账户管理/通过部门获取账号", note = "银行账户管理/通过部门获取账号")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            Account entity = accountService.getById(id);
            List<Dept> list = accountService.queryDeptByAccountId(entity.getId());
            data.putDataValue("entity", entity);
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 删除账户时删除对应的账户关联部门信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "财务管理", note = "删除账户")
    @Verify(code = "/account/del", module = "财务管理/删除账户")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = Objects.requireNonNull(user).getRoles();
            Set<String> set = new HashSet<>();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                for (Role role : roles) {
                    set.add(role.getType());
                }
            }
            Account entity = accountService.getById(id);
            if (user.getId().equals(entity.getCreator()) || set.contains(IConst.ROLE_TYPE_CW)) {
                ResponseData data = ResponseData.ok();
                List list = incomeService.queryIncomeByAccountId(id);
                if (list != null && list.size() > 0) {
                    return ResponseData.customerError(1002, "该账号已有金额流水记录，不支持删除操作！");
                } else {
                    accountService.delById(entity);
                    data.putDataValue("message", "操作成功");
                }
                return data;
            } else {
                return ResponseData.customerError(1002, "您没有权限删除本条记录！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/add")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "财务管理|账户管理", note = "新增账户")
    @Verify(code = "/account/add", module = "财务管理/新增账户")
    public ResponseData add(Account entity) {
        try {
            //编辑和新增公用一个页面，导致页面id可能有值，以防万一，新增时删掉id
            entity.setId(null);
            User user = AppUtil.getUser();
            if(entity.getCompanyId()==0){
                entity.setContactor(user.getName());
                if (user.getRoles() != null && user.getRoles().size() > 0) {
                    Integer type = IConst.ACCOUNT_TYPE_PERSONAL ;
                    for (Role role : user.getRoles()) {
                        String code = role.getCode() ;
                        if (IConst.ROLE_TYPE_CW.equals(role.getType())&&
                                (IConst.ROLE_CODE_KJ.equals(code)||IConst.ROLE_CODE_CN.equals(code)||IConst.ROLE_CODE_ZZ.equals(code)||IConst.ROLE_CODE_BZ.equals(code)||IConst.ROLE_CODE_ZJ.equals(code))) {
                            type = IConst.ACCOUNT_TYPE_COMPANY ;
                        }
                    }
                    entity.setType(type);
                }
            }
            entity.setCreator(user.getId());
            entity.setCreateTime(new Date());
            entity.setState(IConst.STATE_FINISH);
            accountService.add(entity);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", entity);

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|账户管理", note = "修改账户")
    @Verify(code = "/account/edit", module = "财务管理/修改账户")
    public ResponseData edit(Account entity) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = Objects.requireNonNull(user).getRoles();
            Set<String> set = new HashSet<>();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                for (Role role : roles) {
                    set.add(role.getType());
                }
            }
            Account old = accountService.getById(entity.getId());
            if (user.getId().equals(old.getCreator()) || set.contains(IConst.ROLE_TYPE_CW)) {
                entity.setUpdateUserId(user.getId());
                entity.setState(IConst.STATE_FINISH);
                accountService.edit(entity);
                List<Dept> list = accountService.queryDeptByAccountId(entity.getId());
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                data.putDataValue("list", list);
                return data;
            } else {
                return ResponseData.customerError(1002, "当前用户没有编辑权限，录入人才能编辑！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    @RequestMapping("/insertAccountDept")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "财务管理|添加关联部门", note = "添加关联部门")
    @Verify(code = "/account/insertAccountDept", module = "账户管理/账户关联部门")
    public ResponseData insertAccountDept(@RequestParam("accountId") Integer accountId, @RequestParam("deptId") Integer deptId) {
        try {

            List<Dept> list = accountService.insertAccountDept(accountId, deptId);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/deleteAccountDept")
    @ResponseBody
//    @Log(opType = OperateType.DELETE, module = "财务管理|删除账户关联部门", note = "删除账户关联部门")
    @Verify(code = "/account/deleteAccountDept", module = "账户管理/删除账户关联部门")
    public ResponseData deleteAccountDept(@RequestParam("accountId") Integer accountId, @RequestParam("deptId") Integer deptId) {
        try {
            List<Dept> list = accountService.deleteAccountDept(accountId, deptId);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * @param 'type=3 客户账户'
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryCustAccount")
//    @Log(opType = OperateType.QUERY, module = "系统管理|客户账户", note = "客户账户")
    public PageInfo<Map> queryCustAccount(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        PageInfo<Map> list = null;
        try {
            if (map.get("custId") != null) {
                Integer custId = Integer.parseInt((String) map.get("custId"));
                map.put("user", AppUtil.getUser());
                list = accountService.listPgForSelectAccount(pageable.getPageNumber(), pageable.getPageSize(), custId, IConst.ACCOUNT_TYPE_CUST, map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @param map:key 'type=2 供应商账户'
     * @return
     */
    @ResponseBody
    @RequestMapping("/querySupplierAccount")
//    @Log(opType = OperateType.QUERY, module = "账户管理|供应商账户", note = "账户管理|供应商账户")
    public PageInfo<Map> querySupplierAccount(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        PageInfo<Map> list = null;
        try {
            if (map.get("supplierId") != null) {
                Integer supplierId = Integer.parseInt((String) map.get("supplierId"));
                map.put("user", AppUtil.getUser());
                list = accountService.listPgForSelectAccountNotCompanyCode(pageable.getPageNumber(), pageable.getPageSize(), supplierId, IConst.ACCOUNT_TYPE_SUPPLIER, map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取公司账户
     * @param 'companyId=0,type=1' 公司公用账户
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryCompanyAccount")
//    @Log(opType = OperateType.QUERY, module = "账户管理|公司公用账户", note = "账户管理|公司公用账户")
    public PageInfo<Map> queryInnerAccount(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        map.put("user", AppUtil.getUser());
        return accountService.listPgForSelectAccount(pageable.getPageNumber(), pageable.getPageSize(), 0, IConst.ACCOUNT_TYPE_COMPANY, map);
    }

    /**
     * 获取内部个人账户
     *
     * @param 'companyId=0,type=4' 个人账户
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryPersonalAccount")
    public PageInfo<Map> queryInnerAccountForUser(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        //把用户id传进去，如果是借款到个人账户，就只选择显示个人账户信息，
        // 不传用户id了，显示全部公司内部的账户，
//        map.put("dockingId",user.getId()) ;
        map.put("user", AppUtil.getUser());
        return accountService.listPgForSelectAccount(pageable.getPageNumber(), pageable.getPageSize(), 0, IConst.ACCOUNT_TYPE_PERSONAL, map);
    }

    /**
     *  获取个人私人账户
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryIndividualAccount")
    public PageInfo<Map> queryIndividualAccount(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        if (AppUtil.getUser() != null) {
            map.put("user", AppUtil.getUser());
            map.put("userId", AppUtil.getUser().getId());
        }
        return accountService.listPgForSelectAccount(pageable.getPageNumber(), pageable.getPageSize(), 0, IConst.ACCOUNT_TYPE_PERSONAL, map);
    }

    /**
     * 根据公司代码获取公司账户列表，
     * 出纳出款时下拉框选择公司账户用，没有分页
     *
     * @param companyCode
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryCompanyAccountList")
    public List<Account> queryCompanyAccountList(@RequestParam("companyCode") String companyCode) {
        return accountService.queryCompanyAccountList(companyCode);
    }

    @RequestMapping("/addPersonalAccount")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "财务管理|账户管理", note = "新增个人账户")
    public ResponseData addPersonalAccount(Account entity) {
        try {
            if(StringUtils.isEmpty(entity.getName())||StringUtils.isEmpty(entity.getBankNo())||StringUtils.isEmpty(entity.getBankName())){
                throw new QinFeiException(1002,"收款账户、账号或开户行为空！");
            }
            accountService.addPersonalAccount(entity);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }
}
