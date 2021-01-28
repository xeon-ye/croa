package com.qinfei.qferp.controller.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.fee.Borrow;
import com.qinfei.qferp.entity.fee.BorrowRepay;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.IBorrowService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/borrow")
@Api(description = "借款流水接口")
public class BorrowController {

    @Autowired
    private IBorrowService borrowService;
    @Autowired
    private Config config;

    /**
     * 获取借款列表
     *
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPg")
    public PageInfo<Map> listPg(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        PageInfo<Map> list = null;
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
                list = borrowService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询出已借款并且未还款的借款
     * 用于请款
     *
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgForOutgo")
    public PageInfo<Map> listPgForOutgo(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        map.put("user", AppUtil.getUser());
        if(!map.containsKey("outgoId")){
            return null ;
        }
        PageInfo<Map> list = borrowService.listPgForOutgo(pageable.getPageNumber(), pageable.getPageSize(), map);
        return list;
    }

    /**
     * 查询出已借款并且未还款的借款
     * 用于报销时冲抵
     *
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgForReimbursement")
    public PageInfo<Map> listPgForReimbursement(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        map.put("user", AppUtil.getUser());
        PageInfo<Map> list = borrowService.listPgForReimbursement(pageable.getPageNumber(), pageable.getPageSize(), map);
        return list;
    }

    @RequestMapping(value = "/view")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            Borrow entity = borrowService.getById(id);
            if(entity==null){
                return ResponseData.customerError(1002,"该借款流程已删除！") ;
            }
            data.putDataValue("entity", entity);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "财务管理|借款管理", note = "删除借款")
    @Verify(code = "/borrow/del", module = "借款管理/删除借款")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            Borrow entity = borrowService.getById(id);
            if (entity.getState() == IConst.STATE_REJECT || entity.getState() == IConst.STATE_SAVE) {
                borrowService.delById(entity);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                return data;
            } else {
                return ResponseData.customerError(1001, "当前状态不支持删除操作");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }
    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }

    @RequestMapping("/add")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "财务管理|借款管理", note = "新增借款")
    @Verify(code = "/borrow/add", module = "借款管理/新增借款")
    public ResponseData add(Borrow entity, @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles) {
        try {
            List<String> picNames = new ArrayList<>();
            List<String> picPaths = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                if (multipartFile.getSize() > 0) {
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/fee/borrow/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                }
            }
            entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
            entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            borrowService.add(entity);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", entity);
            return data;
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|借款管理", note = "修改借款")
    @Verify(code = "/borrow/edit", module = "借款管理/修改借款")
    public ResponseData edit(Borrow entity, @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles) {
        try {
            Borrow old = borrowService.getById(entity.getId());
            if (old.getState() == IConst.STATE_SAVE || old.getState() == IConst.STATE_REJECT) {
                dealAffix(entity, old, multipartFiles);
                entity.setItemId(old.getItemId());
                borrowService.edit(entity);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1002, "当前状态不支持修改！");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    private Borrow dealAffix(Borrow entity, Borrow old, MultipartFile[] multipartFiles) {
        try {
            List<String> picNames = new ArrayList<>();
            List<String> picPaths = new ArrayList<>();
            //附件处理逻辑：1、如果取得的multipartFiles.length>1,那么一定是上传了多个新附件，直接使用二进制存储
            //2、如果multipartFiles.length=1，那么可能没有上传附件，也可能上传了一个附件
            //3、如果上传了一个附件，multipartFile.getSize()=1,二进制存储
            //4、如果没有上传新附件，直接把old中的附件拿过来
            if (multipartFiles.length > 1) {//表示上传了新附件
                for (MultipartFile multipartFile : multipartFiles) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath =getStringData()+ "/fee/borrow/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        picNames.add(multipartFile.getOriginalFilename());
                        picPaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            } else {
                MultipartFile multipartFile = multipartFiles[0];
                if (multipartFile.getSize() > 0) {//表示上传了新附件
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/fee/borrow/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                    entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                    entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
                } else {//表示附件没有变化
                    entity.setAffixName(old.getAffixName());
                    entity.setAffixLink(old.getAffixLink());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    @RequestMapping("/confirm")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|借款管理", note = "借款出纳出款")
    @Verify(code = "/borrow/confirm", module = "借款管理/借款出纳出款")
    public ResponseData confirm(@RequestParam Map map) {
        try {
            if (ObjectUtils.isEmpty(map.get("id"))
                    || ObjectUtils.isEmpty(map.get("outAccountIds"))
                    || ObjectUtils.isEmpty(map.get("payAmount"))) {
                return ResponseData.customerError(1002, "未获取到出账账户或金额！");
            }
            Integer id = Integer.parseInt((String) map.get("id"));
            User user = AppUtil.getUser();
            Boolean flag = false;
            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType()) &&
                            IConst.ROLE_CODE_CN.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            ResponseData data = ResponseData.ok();
            if(flag){
                Borrow entity = borrowService.getById(id) ;
                if(entity.getState()==IConst.STATE_PASS || entity.getState()==IConst.STATE_CN){
                    borrowService.confirm(map,entity);
                    data.putDataValue("message", "操作成功");
                    data.putDataValue("entity", entity);
                    return data;
                } else {
                    return ResponseData.customerError(1002, "当前状态不支持该操作");
                }
            } else {
                return ResponseData.customerError(1002, "当前用户不支持该操作");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 财务负责人确认出款操作
     *
     * @param id
     * @return
     */
    @RequestMapping("/checkBtoB")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|借款管理", note = "借款财务负责人复核公账出款")
    @Verify(code = "/borrow/checkBtoB", module = "借款管理/借款复核公账出款")
    public ResponseData checkBtoB(@RequestParam("id") Integer id,@RequestParam("desc") String desc) {
        try {
            User user = AppUtil.getUser();
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType()) && IConst.ROLE_CODE_KJ.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if (flag) {//财务负责人确认出款操作
                Borrow entity = borrowService.getById(id);
                if (entity.getState() == IConst.STATE_KJ) {

                    borrowService.checkBtoB(entity,desc);
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    data.putDataValue("entity", entity);
                    return data;
                } else {
                    return ResponseData.customerError(1002, "当前状态不支持复核出款操作");
                }
            } else {
                return ResponseData.customerError(1002, "当前用户不支持复核出款操作");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 申请还款
     */
    @RequestMapping("/repay")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|借款管理", note = "申请还款")
    @Verify(code = "/borrow/repay", module = "借款管理/申请还款")
    public ResponseData repay(@RequestParam("id") Integer id,
                              @RequestParam("amount") Double amount,
                              @RequestParam("repayRemark") String repayRemark
    ) {
        try {
            Borrow entity = borrowService.getById(id);
            if (entity.getRepaying() == IConst.REPAYING_FALSE) {
                entity.setRepaying(IConst.REPAYING_TRUE);
                entity.setAmount(amount);
                entity.setRepayRemark(repayRemark);
                borrowService.repay(entity);

                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1002, "请勿重复提交");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 还款确认
     */
    @RequestMapping("/repayConfirm")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|借款管理", note = "还款出纳确认通过")
    @Verify(code = "/borrow/repayConfirm", module = "借款管理/还款出纳确认通过")
    public ResponseData repayConfirm(@RequestParam("id") Integer id,
                                     @RequestParam("amount") Double amount
    ) {
        try {
            User user = AppUtil.getUser();
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType()) && IConst.ROLE_CODE_CN.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if (flag) {//财务才能执行该操作
                Borrow entity = borrowService.getById(id);
                if (entity.getRepaying() == IConst.REPAYING_TRUE) {//还款中
                    borrowService.repayConfirm(entity);
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    data.putDataValue("entity", entity);
                    return data;
                } else {
                    return ResponseData.customerError(1002, "当前状态不支持该操作");
                }
            } else {
                return ResponseData.customerError(1002, "当前用户不支持该操作，只有出纳才能确认还款！");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 还款确认
     */
    @RequestMapping("/repayReject")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|借款管理", note = "还款出纳确认驳回")
    @Verify(code = "/borrow/repayReject", module = "借款管理/还款出纳确认驳回")
    public ResponseData repayReject(@RequestParam("id") Integer id, @RequestParam("amount") Double amount
    ) {
        try {
            User user = AppUtil.getUser();
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType()) && IConst.ROLE_CODE_CN.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if (flag) {//财务才能执行该操作
                Borrow entity = borrowService.getById(id);
                if (entity.getRepaying() == IConst.REPAYING_TRUE) {//还款中
                    borrowService.repayReject(entity);
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    data.putDataValue("entity", entity);
                    return data;
                } else {
                    return ResponseData.customerError(1002, "当前状态不支持该操作");
                }
            } else {
                return ResponseData.customerError(1002, "当前用户不支持该操作，只有出纳才能确认还款！");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * state=2时财务撤回，此时，借款状态还未变更
     * 1、完成待办
     * 2、增加新的待办
     * 3、请款订单状态修改为-1
     *
     * @param
     */
    @RequestMapping("/CWReject")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|财务撤回", note = "驳回请款")
    public ResponseData CWReject(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser();
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType()) && IConst.ROLE_CODE_CN.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if (flag) {
                Borrow entity = borrowService.getById(id);
                if (entity.getState() == IConst.STATE_PASS || entity.getState() == IConst.STATE_CN) {
                    if (entity.getRepayFlag() == IConst.REPAYING_FALSE || entity.getRepayAmount() > 0) {
                        borrowService.CWReject(entity);
                        ResponseData data = ResponseData.ok();
                        data.putDataValue("message", "操作成功");
                        data.putDataValue("entity", entity);
                        return data;
                    } else {
                        return ResponseData.customerError(1002, "借款有还款记录，不能撤回！");
                    }
                } else {
                    return ResponseData.customerError(1002, "当前状态不能撤回！");
                }
            } else {
                return ResponseData.customerError(1002, "当前用户无法撤回请款，请联系财务出纳撤回！");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    /**
     * state=1或state=12时财务撤回，需要变更以下内容
     * 3、完成待办
     * 4、增加新的待办
     * 5、请款订单状态修改为-1，出款账户及时间还原
     *
     * @param
     */
    @RequestMapping("/CWReturn")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|请款管理", note = "驳回请款")
    public ResponseData CWReturn(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser();
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType()) && IConst.ROLE_CODE_KJ.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if (flag) {
                Borrow entity = borrowService.getById(id);
                if (entity.getState() == IConst.STATE_FINISH || entity.getState() == IConst.STATE_KJ) {
                    if (entity.getRepayFlag() == IConst.REPAYING_FALSE || entity.getRepayAmount() > 0) {
                        Boolean returnFlag = borrowService.CWReturn(entity);
                        if (returnFlag) {
                            ResponseData data = ResponseData.ok();
                            data.putDataValue("message", "操作成功");
                            data.putDataValue("entity", entity);
                            return data;
                        } else {
                            return ResponseData.customerError(1001, "撤回失败，请联系管理员！");
                        }
                    } else {
                        return ResponseData.customerError(1001, "当前借款有还款记录，不能撤回！");
                    }
                } else {
                    return ResponseData.customerError(1001, "当前状态不能撤回！");
                }
            } else {
                return ResponseData.customerError(1001, "当前用户无法撤回请款，请联系财务部长撤回！");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/queryRepayByBorrowId")
    @ResponseBody
    public ResponseData queryRepayByBorrowId(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        List<BorrowRepay> list = borrowService.queryRepayByBorrowId(id);
        Borrow entity = borrowService.getById(id);
        data.putDataValue("message", "操作成功");
        data.putDataValue("list", list);
        data.putDataValue("entity", entity);
        return data;
    }

    @RequestMapping("exportBorrow")
    @ResponseBody
    public void exportBorrow(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
                response.setContentType("application/binary;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("请款导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
                OutputStream outputStream = response.getOutputStream();

                borrowService.exportBorrow(map, outputStream);

            }
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

    @RequestMapping("reimburseSum")
    @ResponseBody
    public Map reimburseSum(@RequestParam Map map) {
        Map list = null;
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
                list = borrowService.remburseSum(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @RequestMapping("changeAccount")
    @ResponseBody
    public ResponseData changeAccount(@RequestParam Map<Object,String> map){
        try{
            if(ObjectUtils.isEmpty("id")
                    || ObjectUtils.isEmpty("outAccountIds")){
                return ResponseData.customerError(1002,"未获取到出账账户信息！") ;
            }
            Integer id = Integer.parseInt((String)map.get("id")) ;
            User user = AppUtil.getUser() ;
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType())&&
                            IConst.ROLE_CODE_BZ.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if(flag){
                Borrow entity = borrowService.getById(id) ;
                //审核通过才能出款
                if(entity.getState()==IConst.STATE_FINISH){
                    borrowService.changeAccount(entity,map);
                    ResponseData data = ResponseData.ok() ;
                    data.putDataValue("message","操作成功");
                    data.putDataValue("entity", entity) ;
                    return data ;
                }else{
                    return ResponseData.customerError(1002,"当前状态不支持出款操作") ;
                }
            }else{
                return ResponseData.customerError(1002,"只允许财务部长更改出款账号") ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @ApiOperation(value = "借款管理", notes = "下载借款信息")
    @Verify(code = "/fee/flowPrint", module = "借款管理/下载借款信息")
    @PostMapping("downloadBorrowData")
    @ResponseBody
    public ResponseData downloadBorrowData(@RequestParam Map<String, Object> param){
        try{
            ResponseData responseData = ResponseData.ok();
            String fileName = borrowService.downloadBorrowData(param);
            if (StringUtils.isEmpty(fileName)) {
                responseData.putDataValue("message", "借款信息不存在！");
            } else {
                responseData.putDataValue("file", fileName);
            }
            return responseData;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "下载借款信息异常！");
        }
    }

    @ApiOperation(value = "借款管理", notes = "批量下载借款信息")
    @Verify(code = "/fee/flowPrint", module = "借款管理/批量下载借款信息")
    @PostMapping("batchDownloadData")
    @ResponseBody
    public ResponseData batchDownloadData(@RequestParam Map<String, Object> param){
        try{
            ResponseData responseData = ResponseData.ok();
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                param.put("roleType", roles.get(0).getType());
                param.put("roleCode", roles.get(0).getCode());
                param.put("user", user);
            }
            String fileName = borrowService.batchDownloadData(param);
            if (StringUtils.isEmpty(fileName)) {
                responseData.putDataValue("message", "借款信息不存在！");
            } else {
                responseData.putDataValue("file", fileName);
            }
            return responseData;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "批量下载借款信息异常！");
        }
    }

    @ApiOperation(value = "借款管理", notes = "批量打印借款信息")
    @Verify(code = "/fee/flowPrint", module = "借款管理/批量打印借款信息")
    @PostMapping("batchPrintData")
    @ResponseBody
    public ResponseData batchPrintData(@RequestParam Map<String, Object> param){
        try{
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                param.put("roleType", roles.get(0).getType());
                param.put("roleCode", roles.get(0).getCode());
                param.put("user", user);
            }
            return ResponseData.ok().putDataValue("list", borrowService.listBorrowData(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "批量打印借款信息异常！");
        }
    }

}
