package com.qinfei.qferp.controller.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.fee.Income;
import com.qinfei.qferp.entity.fee.IncomeUser;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.IAccountService;
import com.qinfei.qferp.service.fee.IIncomeService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/income")
@Api(description = "进账流水接口")
class IncomeController {

    @Autowired
    private IIncomeService incomeService;  
    @Autowired
    private IAccountService accountService;
    @Autowired
    private Config config ;

    @ResponseBody
    @RequestMapping("/listPg")
    @Log(opType = OperateType.QUERY, module = "进账管理", note = "进账列表")
    public PageInfo<Map> listPg(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        PageInfo<Map> list = null ;
        try{
            User user = AppUtil.getUser() ;
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("deptId",user.getDeptId()) ;
                map.put("user",user) ;
                list = incomeService.listPg(pageable.getPageNumber(), pageable.getPageSize(),map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list ;
    }

    @RequestMapping(value="/view")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        try{
            ResponseData data = ResponseData.ok();
            Income entity = incomeService.getById(id) ;
            data.putDataValue("entity",entity) ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping(value="/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "财务管理|进账流水删除", note = "删除进账流水")
    @Verify(code = "/income/del", module = "财务管理/删除进账")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            List<IncomeUser> list = incomeService.queryIncomeUserByIncomeId(id) ;
            if(list==null || list.size()==0){
                Income entity = incomeService.getById(id) ;
                User user = AppUtil.getUser() ;
                Boolean flag = false;
                if (user.getRoles() != null && user.getRoles().size() > 0) {
                    for (Role role : user.getRoles()) {
                        if (IConst.ROLE_TYPE_CW.equals(role.getType())) {
                            flag = true;
                        }
                    }
                }
                if(flag){
                    incomeService.delById(entity) ;
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    return data;
                }else{
                    return ResponseData.customerError(1001,"当前用户没有删除权限，财务才能删除！") ;
                }
            }else{
                return ResponseData.customerError(1001, "该笔进账已被领取，无法删除！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/add")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "财务管理|进账流水管理", note = "新增进账流水")
    @Verify(code = "/income/add", module = "财务管理/新增进账")
    public ResponseData add(Income entity) {
        try{
            if(entity.getAccountId()!=null){
                Account account = accountService.getById(entity.getAccountId()) ;
                entity.setAccountId(account.getId());
                entity.setAccountName(account.getName());
                entity.setBankNo(account.getBankNo()) ;
                incomeService.add(entity);
                ResponseData data = ResponseData.ok() ;
                data.putDataValue("message","操作成功");
                data.putDataValue("entity", entity) ;
                return data ;
            }else{
                return ResponseData.customerError(1001, "没有获取到账号信息！");
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }

    }

    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|进账流水管理", note = "修改进账流水")
    @Verify(code = "/income/edit", module = "财务管理/编辑进账")
    public ResponseData edit(Income entity) {
        try{
            User user = AppUtil.getUser() ;
            Income oldEntity =  incomeService.getById(entity.getId()) ;
           //自己才能修改
            if(entity.getCreator().intValue() == user.getId().intValue()){
                //还没人领款才能编辑
                if(oldEntity.getUnclaimedAmount().toString().equals(oldEntity.getTradeAmount().toString())){
                    if(entity.getAccountId()!=null){
                        Account account = accountService.getById(entity.getAccountId()) ;
                        entity.setAccountId(account.getId());
                        entity.setAccountName(account.getName());
                        entity.setBankNo(account.getBankNo()) ;
                        entity.setUpdateUserId(user.getId());
                        entity.setUnclaimedAmount(entity.getTradeAmount());
                        incomeService.edit(entity);
                        ResponseData data = ResponseData.ok();
                        data.putDataValue("message", "操作成功");
                        data.putDataValue("entity", entity) ;
                        return data;
                    }else{
                        return ResponseData.customerError(1002, "没有获取到账号信息！");
                    }

                }else{
//                    throw new QinFeiException(50000,"当前进账流水已有人领取，无法修改！");
                    return ResponseData.customerError(1002,"当前进账流水已有人领取，无法修改！") ;
                }
            }else{
                return ResponseData.customerError(1002,"当前用户没有编辑权限，录入人才能编辑！") ;
            }
//            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }

    }
    @RequestMapping(value="/receive")
    @ResponseBody
    @Verify(code = "/income/receive", module = "财务管理/领款")
    @Log(opType = OperateType.UPDATE, module = "财务管理|领款", note = "领款")
    public ResponseData receive(@RequestParam("id") Integer id,@RequestParam("amount") Double amount) {
        try{
            ResponseData data = ResponseData.ok();
            Income old = incomeService.getById(id) ;
            if(amount>0){
                if(old.getUnclaimedAmount()<amount){
                    return ResponseData.customerError(1002,"金额不足") ;
                }else{
                    Income entity = incomeService.receive(old,amount) ;
                    data.putDataValue("entity",entity) ;
                    data.putDataValue("message","操作成功") ;
                    return data ;
                }
            }else{
                return ResponseData.customerError(1002,"领款金额必须大于0，当前领款金额="+amount) ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 帮别人领款，包括财务给业务员领款，部长给部门内部人员领款
     * 区别于assign：assign是给稿件分款
     * @param incomeId
     * @param userId
     * @param amount
     * @return
     */
    @RequestMapping(value="/dispatch")
    @ResponseBody
    @Verify(code = "/income/dispatch", module = "财务管理/分配领款")
    @Log(opType = OperateType.UPDATE, module = "财务管理|分配领款", note = "分配领款")
    public ResponseData dispatch(@RequestParam("incomeId") Integer incomeId,
                                    @RequestParam("userId") Integer userId,
                                    @RequestParam("amount") Double amount) {
        try{
            ResponseData data = ResponseData.ok();
            Income old = incomeService.getById(incomeId) ;
            if(amount>0) {
                if (old.getUnclaimedAmount() < amount) {
                    return ResponseData.customerError(1002, "金额不足");
                } else {
                    Income entity = incomeService.dispatch(old, userId, amount);
                    data.putDataValue("entity", entity);
                    data.putDataValue("message", "操作成功");
                    return data;
                }
            }else{
                return ResponseData.customerError(1001,"领款金额必须大于0，当前领款金额="+amount) ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 业务员领款撤回
     * @param id
     * @return
     */
    @RequestMapping(value="/withdraw")
    @Log(opType = OperateType.UPDATE, module = "财务管理|领款撤回", note = "领款撤回")
    @ResponseBody
    public ResponseData withdraw(@RequestParam("id") Integer id){
        try{
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser() ;
            List<IncomeUser> list = incomeService.queryIncomeUserByIncomeIdAndUserId(id,user.getId()) ;
            if(list!=null && list.size()>0){
                Map map = incomeService.querySumAmount(id,user.getId()) ;
                Double receiveAmount = (Double)map.get("receiveSum") ;
                Double remainAmount = (Double)map.get("remainSum") ;
                //领款金额=可用金额，表示还没有分款，业务员可以撤回领款
                if(receiveAmount.toString().equals(remainAmount.toString())){
                    incomeService.withdraw(id,user,receiveAmount,list) ;
                    data.putDataValue("message","操作成功") ;
                    return data ;
                }else{
                    return ResponseData.customerError(1001,"已分款，不能退回！") ;
                }
            }else{
                return ResponseData.customerError(1001,"您没有可退回的领款！") ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 根据进账id查看进账分款详情
     * @param incomeId 进账id
     * @return
     */
    @RequestMapping(value="/assignView")
    @ResponseBody
    public ResponseData assignView(@RequestParam("incomeId") Integer incomeId) {
        try{
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser() ;
            IncomeUser incomeUser = incomeService.getIncomeUser(incomeId,user.getId()) ;
            data.putDataValue("entity",incomeUser) ;
            data.putDataValue("message","操作成功") ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 业务员给稿件分款
     * 区别于dispatch：dispatch是给别人分款，包括财务给业务员分款，部长给部门内部人员分款
     * @param map
     * @return
     */
    @RequestMapping(value="/assignArticle")
    @ResponseBody
    @Verify(code = "/income/assignArticle", module = "财务管理/业务员分款")
    public ResponseData assignArticle(@RequestParam Map map) {
        try{
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser() ;
            incomeService.assignArticle(map,user) ;
            data.putDataValue("message","操作成功") ;
            return data ;
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 查询出当前业务员已领款列表
     * @param pageable
     * @param map
     * @return
     */
    @RequestMapping(value="/listPgForAssign")
    @ResponseBody
    public PageInfo<Map> listPgForAssign(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        User user = AppUtil.getUser();
        map.put("id",user.getId());
        return incomeService.listPgForAssign(pageable.getPageNumber(), pageable.getPageSize(),map);
    }

    /**
     * 分款管理（财务）页面的列表数据，根据进账关联多张表查询
     * @param pageable
     * @param map
     * @return
     */
    @RequestMapping(value="/listPgForAssignCW")
    @ResponseBody
    public PageInfo<Map> listPgForAssignCW(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        User user = AppUtil.getUser();
        map.put("id",user.getId());
        map.put("companyCode",user.getCompanyCode());
        return incomeService.listPgForAssignCW(pageable.getPageNumber(), pageable.getPageSize(),map);
    }

    /**
     * 查询分款未完成的稿件，用于分款
     * 条件：未分款或分款未完成 && 已发布 && 已完善客户 && 当前业务员 && 去除2019年1月份老系统的稿件
     * @param map
     * @return 未分款的稿件列表
     */
    @RequestMapping(value="/queryArticleForAssign")
    @ResponseBody
    public PageInfo<Map> queryArticleForAssign(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        User user = AppUtil.getUser();
        map.put("userId",user.getId()) ;
        return incomeService.queryArticleForAssign(pageable.getPageNumber(), pageable.getPageSize(),map);
    }

    /**
     * 根据进账id查询出该进账的已分款详情
     * @param incomeId 进账id
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgForSelectedArticle")
    public PageInfo<Map> listPgForSelectedArticle(@PageableDefault(size = 5) Pageable pageable,@RequestParam("incomeId") Integer incomeId) {
        return incomeService.listPgForSelectedArticle(pageable.getPageNumber(), pageable.getPageSize(),incomeId);
    }

    /**
     * 根据稿件id查询出当前稿件的进账列表
     * 一个稿件可能有多个进账
     * @param article
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgByArticleId")
    public PageInfo<Map> listPgByArticleId(@PageableDefault() Pageable pageable, Article article) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        List<Map> list = incomeService.listPgByArticleId(article);
        return new PageInfo<>(list);
    }

    /**
     * 根据进账id查询出当前进账的领款人列表
     * @param pageable
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgIncomeUserByIncomeId")
    public PageInfo<IncomeUser> listPgIncomeUserByIncomeId(@PageableDefault() Pageable pageable, @RequestParam("id") Integer id) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        List<IncomeUser> list = incomeService.queryIncomeUserByIncomeId(id);
        return new PageInfo<>(list);
    }

    @RequestMapping(value="/backAssign")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "财务管理/分款撤回", note = "财务管理/分款撤回")
    @Verify(code = "/income/backAssign", module = "财务管理/分款撤回")
    public ResponseData backAssign(@RequestParam("incomeId") Integer incomeId,@RequestParam("userId") Integer userId) {
        try{
            String message = incomeService.backAssign(incomeId,userId) ;
            if(StringUtils.isEmpty(message)){
                ResponseData data = ResponseData.ok();
                data.putDataValue("message","操作成功") ;
                return data ;
            }else{
                return ResponseData.customerError(1001,message) ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping(value="/backIncome")
    @ResponseBody
    @Verify(code = "/income/backIncome", module = "系统管理/领款撤回")
    @Log(opType = OperateType.QUERY, module = "财务管理/领款撤回", note = "财务管理/领款撤回")
    public ResponseData backIncome(@RequestParam("incomeId") Integer incomeId,@RequestParam("userId") Integer userId) {
        try{
            String message = incomeService.backIncome(incomeId,userId) ;
            if(StringUtils.isEmpty(message)){
                ResponseData data = ResponseData.ok();
                data.putDataValue("message","操作成功") ;
                return data ;
            }else{
                return ResponseData.customerError(1001,message) ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping(value="/backAssignArticle")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "财务管理/分款撤回", note = "财务管理/分款撤回")
    @Verify(code = "/income/backAssign", module = "财务管理/分款撤回")
    public ResponseData backAssignArticle(@RequestParam("incomeId") Integer incomeId,@RequestParam("articleId") Integer articleId) {
        try{
            String message = incomeService.backAssignArticle(incomeId,articleId) ;
            if(StringUtils.isEmpty(message)){
                ResponseData data = ResponseData.ok();
                data.putDataValue("message","操作成功") ;
                return data ;
            }else{
                return ResponseData.customerError(1001,message) ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }
    /**
     * 导出全部
     */
    @RequestMapping("/exportIncomeDetail")
    @Log(opType = OperateType.QUERY, module = "财务管理/导出分款详情", note = "导出分款详情")
    public void exportIncomeDetail(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("companyCode",user.getCompanyCode()) ;
            }
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("分款详情.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            incomeService.exportIncomeDetail(map, outputStream);
        } catch (Exception e) {
            log.error("导出分款详情失败", e);
        }
    }

    @RequestMapping("exportTemplate")
    @ResponseBody
    public void exportTemplate(HttpServletResponse response, @RequestParam Map map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("进账批量导入模板" + DateUtils.getNowTime() + ".xlsx", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            incomeService.exportTemplate(map, outputStream);
        } catch (Exception e) {
            log.error("导出模板失败", e);
        }
    }

    @RequestMapping("importIncome")
    @ResponseBody
    @Log(opType = OperateType.ADD, note = "批量导入", module = "进账管理/批量导入")
    @Verify(code = "/income/importIncome", module = "进账管理/批量导入")
    public ResponseData importIncome(@RequestParam(value = "file") MultipartFile multipartFile) {
        try {
            User user = AppUtil.getUser() ;
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType())) {
                        flag = true;
                    }
                }
            }
            if(flag){
                String fileName = UUIDUtil.get32UUID() + multipartFile.getOriginalFilename();
                String childPath = File.separator+"media"+File.separator+"order"+File.separator;
                if(fileName.indexOf(".xls")>-1){
                    File destFile = new File( config.getUploadDir() +childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    String errorMsg = incomeService.importIncome(destFile);
                    if(StringUtils.isEmpty(errorMsg)){
                        ResponseData data = ResponseData.ok() ;
                        data.putDataValue("message","导入成功！") ;
                        return data ;
                    }else{
                        return ResponseData.customerError(1002,errorMsg) ;
                    }
                }else{
                    return ResponseData.customerError(1003,"上传的文件类型不正确！") ;
                }
            }else{
                return ResponseData.customerError(1003,"当前用户不支持导入操作") ;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, "导入失败！");
        }
    }
    @RequestMapping("exportIncome")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "进账管理/导出", note = "进账管理/导出")
    @Verify(code = "/income/exportIncome", module = "进账管理/导出")
    public void exportIncome(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser() ;
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("deptId",user.getDeptId()) ;
                map.put("user",user) ;
                response.setContentType("application/binary;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("进账导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
                OutputStream outputStream = response.getOutputStream();
                incomeService.exportIncome(map, outputStream);
            }
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }
    @RequestMapping("reimburseSum")
    @ResponseBody
    public Map reimburseSum(@RequestParam Map map){
        return incomeService.reimburseSum(map);
    }

}
