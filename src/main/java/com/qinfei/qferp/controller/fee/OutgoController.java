package com.qinfei.qferp.controller.fee;

import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.serivce.impl.FileService;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.IOutgoService;
import com.qinfei.qferp.service.fee.IUserAccountService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/outgo")
@Api(description = "请款流水接口")
class OutgoController {

    @Autowired
    private IOutgoService outgoService;

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private FileService<Outgo> fileService;

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
                String rolelist = roles.toString();
                String roleType = "";
                String roleCode = "";
                if (rolelist.contains(IConst.ROLE_TYPE_CW)) {
                    roleType = IConst.ROLE_TYPE_CW;
                } else {
                    roleType = roles.get(0).getType();
                    roleCode = roles.get(0).getCode();
                }
                map.put("roleType", roleType);
                map.put("roleCode", roleCode);
                map.put("user", user);
                list = outgoService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @RequestMapping(value = "/view")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            Outgo entity = outgoService.getById(id);
            if (StringUtils.isNotEmpty(entity.getSupplierPhone())) {
                String phone = EncryptUtils.decrypt(entity.getSupplierPhone());
                entity.setSupplierPhone(phone);
            }
            if (entity == null) {
                return ResponseData.customerError(1002, "该请款流程已删除！");
            } else {
                data.putDataValue("entity", entity);
                List<Map> list = outgoService.queryBorrowById(entity.getId());
                data.putDataValue("list", list);
                Map map = outgoService.querySumAmount(id);
                if (map == null) {
                    data.putDataValue("outgoSum", 0);
                    data.putDataValue("saleSum", 0);
                } else {
                    data.putDataValue("outgoSum", map.get("outgoSum") == null ? 0 : map.get("outgoSum"));
                    data.putDataValue("saleSum", map.get("saleSum") == null ? 0 : map.get("saleSum"));
                }
                return data;
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "财务管理|请款流水管理", note = "删除请款流水")
    @Verify(code = "/outgo/del", module = "财务管理/删除请款")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser();
            Outgo entity = outgoService.getById(id);
            if (entity == null) {
                return ResponseData.customerError(1002, "该流程已删除，请刷新后重试！");
            }
            if (user.getId().toString().equals(entity.getApplyId().toString())) {
                if (entity.getState() == IConst.STATE_SAVE || entity.getState() == IConst.STATE_REJECT) {
                    outgoService.delById(entity);
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    return data;
                } else {
                    return ResponseData.customerError(1002, "删除失败，只能删除保存和驳回的请款！");
                }
            } else {
                return ResponseData.customerError(1002, "删除失败，只有申请人才能删除请款！");
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
     * 新增功能暂不启用，使用saveStepOne()替代
     *
     * @param entity
     * @return
     */
    @RequestMapping("/add")
//    @Log(opType = OperateType.DELETE, module = "财务管理|请款流水管理", note = "添加请款")
    @ResponseBody
    public ResponseData add(Outgo entity) {
        try {
            User user = AppUtil.getUser();
            entity.setState(IConst.STATE_BZ);
            entity.setCreator(user.getId());
            entity.setCreateTime(new Date());
            outgoService.add(entity);
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
    @Log(opType = OperateType.UPDATE, module = "财务管理|请款管理", note = "修改请款")
    @Verify(code = "/outgo/edit", module = "财务管理/修改请款")
    public ResponseData edit(Outgo entity, @RequestParam(value = "file", required = false) MultipartFile[] multipartFiles) {
        try {
            Outgo old = outgoService.getById(entity.getId());
            if (old.getState() == IConst.STATE_SAVE || old.getState() == IConst.STATE_REJECT) {
                Double applyAmount = entity.getApplyAmount();
                Double fundAmount = entity.getFundAmount();
                if (applyAmount < fundAmount) {
                    //抵消的备用金比申请金额大
                    return ResponseData.customerError(1002, "抵消的备用金比申请金额大！");
                }
                ;
                if (fundAmount > 0 && !(fundAmount.equals(applyAmount))) {
                    //抵消的备用金比申请金额大
                    return ResponseData.customerError(1002, "备用金大于0时，备用金的金额必须和实际请款金额一致！");
                }
                ;
                // 处理文件
                fileService.handleFile(entity, multipartFiles, old, "/fee/outgo");
                entity.setCompanyCode(old.getCompanyCode());
                entity.setItemId(old.getItemId());
                entity.setMediaTypeId(old.getMediaTypeId());
                entity = outgoService.edit(entity);
                userAccountService.save(entity.getAccountBankName(), entity.getAccountBankNo(), AppUtil.getUser().getId());
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1002, "当前状态不能编辑！");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    @RequestMapping("/workupOutgo")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|请款唤醒", note = "请款唤醒")
    @Verify(code = "/outgo/edit", module = "财务管理/请款唤醒")
    public ResponseData workupOutgo(@RequestBody Outgo entity) {
        try {
            Outgo old = outgoService.getById(entity.getId());
            //仅已完成的请款能被唤醒
            if (old.getState() == IConst.STATE_FINISH) {
                Double applyAmount = entity.getApplyAmount();
                Double fundAmount = entity.getFundAmount();
                if (applyAmount != null && fundAmount != null) {
                    if (applyAmount < fundAmount) {
                        //抵消的备用金比申请金额大
                        return ResponseData.customerError(1002, "抵消的备用金比申请金额大！");
                    }
                    if (fundAmount > 0 && !(fundAmount.equals(applyAmount))) {
                        //抵消的备用金比申请金额大
                        return ResponseData.customerError(1002, "备用金大于0时，备用金的金额必须和实际请款金额一致！");
                    }
                }
                entity.setCompanyCode(old.getCompanyCode()); //设置请款公司代码
                outgoService.workupOutgo(entity, entity.getWorkupRequestParam());
                ;
                return ResponseData.ok();
            } else {
                return ResponseData.customerError(1002, "当前状态不能进行唤醒！");
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
     * 出纳出款操作
     *
     * @return
     */
    @RequestMapping("/confirm")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|请款管理", note = "出纳确认请款")
    @Verify(code = "/outgo/confirm", module = "财务管理/请款管理")
    public ResponseData confirm(@RequestParam Map map) {
        try {
            if (ObjectUtils.isEmpty("id")
                    || ObjectUtils.isEmpty("outAccountIds")
                    || ObjectUtils.isEmpty("payAmount")) {
                return ResponseData.customerError(1002, "未获取到出账账户信息！");
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
            if (flag) {
                Outgo entity = outgoService.getById(id);
                //审核通过才能出款
                if (entity.getState() == IConst.STATE_PASS || entity.getState() == IConst.STATE_CN) {
                    outgoService.confirm(entity, map);
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    data.putDataValue("entity", entity);
                    return data;
                } else {
                    return ResponseData.customerError(1002, "当前状态不支持出款操作");
                }
            } else {
                return ResponseData.customerError(1002, "当前用户不支持出款操作");
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
     * 出纳出款操作
     *
     * @return
     */
    @RequestMapping("/changeAccount")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|请款更改出款账号", note = "请款更改出款账号")
    public ResponseData changeAccount(@RequestParam Map map) {
        try {
            if (ObjectUtils.isEmpty("id")
                    || ObjectUtils.isEmpty("outAccountIds")) {
                return ResponseData.customerError(1002, "未获取到出账账户信息！");
            }
            Integer id = Integer.parseInt((String) map.get("id"));
            User user = AppUtil.getUser();
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType()) &&
                            IConst.ROLE_CODE_BZ.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if (flag) {
                Outgo entity = outgoService.getById(id);
                //审核通过才能出款
                if (entity.getState() == IConst.STATE_FINISH) {
                    outgoService.changeAccount(entity, map);
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    data.putDataValue("entity", entity);
                    return data;
                } else {
                    return ResponseData.customerError(1002, "当前状态不支持出款操作");
                }
            } else {
                return ResponseData.customerError(1002, "只允许财务部长更改出款账号");
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
     * state=2时财务撤回，此时，稿件状态和借款状态还未变更
     * 1、完成待办
     * 2、增加新的待办
     * 3、请款订单状态修改为-1
     *
     * @param
     */
    @RequestMapping("/CWReject")
//    @Log(opType = OperateType.DELETE, module = "请款管理|财务撤回", note = "财务撤回")
    @ResponseBody
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
                Outgo entity = outgoService.getById(id);
                if (entity.getState() == IConst.STATE_PASS || entity.getState() == IConst.STATE_CN) {
                    outgoService.CWReject(entity);
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    data.putDataValue("entity", entity);
                    return data;
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
     * 1、关联的稿件请款状态修改为请款中2
     * 2、关联的借款还原
     * 3、完成待办
     * 4、增加新的待办
     * 5、请款订单状态修改为-1,出款信息还原
     *
     * @param
     */
    @RequestMapping("/CWReturn")
//    @Log(opType = OperateType.DELETE, module = "请款管理|财务驳回", note = "财务驳回")
    @ResponseBody
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
                Outgo entity = outgoService.getById(id);
                if (entity.getState() == IConst.STATE_FINISH || entity.getState() == IConst.STATE_KJ) {
                    Boolean returnFlag = outgoService.CWReturn(entity);
                    if (returnFlag) {
                        ResponseData data = ResponseData.ok();
                        data.putDataValue("message", "操作成功");
                        data.putDataValue("entity", entity);
                        return data;
                    } else {
                        return ResponseData.customerError(1002, "选中的稿件有提成信息，不能撤回！");
                    }
                } else {
                    return ResponseData.customerError(1002, "当前状态不能撤回！");
                }
            } else {
                return ResponseData.customerError(1002, "当前用户无法撤回请款，请联系财务部长撤回！");
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
//    @Log(opType = OperateType.UPDATE, module = "财务管理|请款管理", note = "请款财务会计确认公账出款")
    @Verify(code = "/outgo/checkBtoB", module = "财务管理/请款财务会计复核公账出款")
    public ResponseData checkBtoB(@RequestParam("id") Integer id, @RequestParam("desc") String desc) {
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
            if (flag) {//财务会计确认出款操作
                Outgo entity = outgoService.getById(id);
                if (entity.getState() == IConst.STATE_KJ) {
                    outgoService.checkBtoB(entity, desc);
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    data.putDataValue("entity", entity);
                    return data;
                } else {
                    return ResponseData.customerError(1002, "当前状态不支持确认出款操作");
                }
            } else {
                return ResponseData.customerError(1002, "当前用户不支持确认出款操作");
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
     * add方法
     *
     * @param map
     * @return
     */
    @RequestMapping("/saveStepOne")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "财务管理|请款管理", note = "新增请款")
    @Verify(code = "/outgo/saveStepOne", module = "财务管理/新增请款")
    public ResponseData saveStepOne(@RequestParam Map map) {
        try {
            ResponseData data = ResponseData.ok();
            //articleIdsSec或checkState不能同时为空
            if (ObjectUtils.isEmpty(map.get("supplierIdSec"))
                    || (ObjectUtils.isEmpty(map.get("articleIdsSec")) && ObjectUtils.isEmpty(map.get("checkState")))
                    || ObjectUtils.isEmpty(map.get("companyCode"))) {
                return ResponseData.customerError(1002, "未选择供应商或稿件！");
            } else {
                User user = AppUtil.getUser();
                Outgo entity = outgoService.saveStepOne(map, user);
                data.putDataValue("entity", entity);
                List<Map> list = outgoService.queryBorrowById(entity.getId());
                data.putDataValue("list", list);
                data.putDataValue("message", "操作成功");
                return data;
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
     * 请款已经关联的稿件
     *
     * @param pageable
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgForSelectedArticle")
    public PageInfo<Map> listPgForSelectedArticle(@PageableDefault(size = 5) Pageable pageable, @RequestParam("id") Integer id) {
        return outgoService.listPgForSelectedArticle(pageable.getPageNumber(), pageable.getPageSize(), id);
    }

    /**
     * 根据稿件编号查询请款id
     *
     * @param articleId
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryOutgoId")
    public Integer queryOutgoId(@RequestParam("articleId") Integer articleId) {
        return outgoService.queryOutgoId(articleId);
    }

    /**
     * 选择未请款的稿件
     *
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgForSelectArticle")
    public PageInfo<Map> listPgForSelectArticle(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        map.put("user", AppUtil.getUser());
        return outgoService.listPgForSelectArticle(pageable.getPageNumber(), pageable.getPageSize(), map);
    }

    @ResponseBody
    @RequestMapping("/listPgForSelectArticleSum")
    public Map listPgForSelectArticleSum(@RequestParam Map map) {
        map.put("user", AppUtil.getUser());
        return outgoService.listPgForSelectArticleSum(map);
    }

    /**
     * 保存请款关联的备用金信息
     *
     * @param map
     * @return
     */
    @RequestMapping("/saveOutgoBorrow")
    @ResponseBody
    public ResponseData saveOutgoBorrow(@RequestParam Map map) {
        try {
            ResponseData data = ResponseData.ok();
            if (ObjectUtils.isEmpty("outgoId") || ObjectUtils.isEmpty("borrowIds")) {
                return ResponseData.customerError(1002, "未选择备付金信息！");
            } else {
                Integer outgoId = Integer.parseInt((String) map.get("outgoId"));
                Double amount = outgoService.saveOutgoBorrow(map);
                data.putDataValue("amount", amount);
                List<Map> list = outgoService.queryBorrowById(outgoId);
                data.putDataValue("list", list);
                data.putDataValue("message", "操作成功");
                return data;
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
     * 清除选中的备用金信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/cleanOutgoBorrow")
    @ResponseBody
    public ResponseData cleanOutgoBorrow(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            outgoService.cleanOutgoBorrow(id);
            data.putDataValue("message", "操作成功");
            return data;
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("exportOutgo")
    @Log(opType = OperateType.DELETE, module = "请款管理|导出", note = "导出")
    @ResponseBody
    public void exportOutgo(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
                response.setContentType("application/binary;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("请款导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
                OutputStream outputStream = response.getOutputStream();
                outgoService.exportOutgo(map, outputStream);
            }
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }

    /**
     * 计算抹零统计求和接口
     *
     * @param map
     * @return
     */
    @RequestMapping("calculationOfTotal")
    @ResponseBody
    public Map calculationOfTotal(@RequestParam Map map) {
        Map list = null;
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                String rolelist = roles.toString();
                String roleType = "";
                String roleCode = "";
                if (rolelist.contains(IConst.ROLE_TYPE_CW)) {
                    roleType = IConst.ROLE_TYPE_CW;
                } else {
                    roleType = roles.get(0).getType();
                    roleCode = roles.get(0).getCode();
                }
                map.put("roleType", roleType);
                map.put("roleCode", roleCode);
                map.put("user", user);
                list = outgoService.calculationOfTotal(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 计算请款求和 接口
     *
     * @param map
     * @return
     */
    @RequestMapping("aggregateAmount")
    @ResponseBody
    public Map aggregateAmount(@RequestParam Map map) {
        Map list = null;
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                String rolelist = roles.toString();
                String roleType = "";
                String roleCode = "";
                if (rolelist.contains(IConst.ROLE_TYPE_CW)) {
                    roleType = IConst.ROLE_TYPE_CW;
                } else {
                    roleType = roles.get(0).getType();
                    roleCode = roles.get(0).getCode();
                }
                map.put("roleType", roleType);
                map.put("roleCode", roleCode);
                map.put("user", user);
                list = outgoService.aggregateAmount(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据板块类型查板块组长
     */
    @RequestMapping("/mediaGroupLeader")
    @ResponseBody
    public List<Map> mediaGroupLeader(Integer mediaTypeId) {
        return outgoService.mediaGroupLeader(mediaTypeId);
    }

    /**
     * 更据板块id 判断板块类型
     */
    @RequestMapping("/selectMediaType")
    @ResponseBody
    public int selectMediaType(Integer mediaTypeId) {
        return outgoService.selectMediaType(mediaTypeId);
    }

    /**
     * 媒介回填操作按钮
     *
     * @param outgo
     * @return
     */
    @RequestMapping("/backfill")
    @ResponseBody
    public ResponseData backfill(@RequestBody Outgo outgo) {
        try {
            ResponseData data = ResponseData.ok();
            outgoService.backfill(outgo);
            data.putDataValue("message", "操作成功");
            return data;
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    /**
     * 抹零统计管理页面
     *
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/resetListPg")
    public PageInfo<Map> resetListPg(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        PageInfo<Map> list = null;
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                String rolelist = roles.toString();
                String roleType = "";
                String roleCode = "";
                if (rolelist.contains(IConst.ROLE_TYPE_CW)) {
                    roleType = IConst.ROLE_TYPE_CW;
                } else {
                    roleType = roles.get(0).getType();
                    roleCode = roles.get(0).getCode();
                }
                map.put("roleType", roleType);
                map.put("roleCode", roleCode);
                map.put("user", user);
                list = outgoService.resetListPg(pageable.getPageNumber(), pageable.getPageSize(), map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @ResponseBody
    @RequestMapping("/processCompanyCode")
    public List<String> processCompanyCode() {
        return outgoService.HTLScompanyCode();
    }

    @ApiOperation(value = "请款管理", notes = "下载请款信息")
    @Verify(code = "/fee/flowPrint", module = "请款管理/下载请款信息")
    @PostMapping("downloadData")
    @ResponseBody
    public ResponseData downloadData(@RequestParam Map<String, Object> param) {
        try {
            ResponseData responseData = ResponseData.ok();
            String fileName = outgoService.downloadData(param);
            if (StringUtils.isEmpty(fileName)) {
                responseData.putDataValue("message", "请款信息不存在！");
            } else {
                responseData.putDataValue("file", fileName);
            }
            return responseData;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002, "下载请款信息异常！");
        }
    }

    @ApiOperation(value = "请款管理", notes = "批量下载请款信息")
    @Verify(code = "/fee/flowPrint", module = "请款管理/批量下载请款信息")
    @PostMapping("batchDownloadData")
    @ResponseBody
    public ResponseData batchDownloadData(@RequestParam Map<String, Object> param) {
        try {
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
            String fileName = outgoService.batchDownloadData(param);
            if (StringUtils.isEmpty(fileName)) {
                responseData.putDataValue("message", "请款信息不存在！");
            } else {
                responseData.putDataValue("file", fileName);
            }
            return responseData;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002, "批量下载请款信息异常！");
        }
    }

    @ApiOperation(value = "请款管理", notes = "批量打印请款信息")
    @Verify(code = "/fee/flowPrint", module = "请款管理/批量打印请款信息")
    @PostMapping("batchPrintData")
    @ResponseBody
    public ResponseData batchPrintData(@RequestParam Map<String, Object> param) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                param.put("roleType", roles.get(0).getType());
                param.put("roleCode", roles.get(0).getCode());
                param.put("user", user);
            }
            return ResponseData.ok().putDataValue("list", outgoService.listOutgoData(param));
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002, "批量打印请款信息异常！");
        }
    }

    /**
     * 获取用户银行账户信息
     *
     * @return
     */
    @GetMapping("/getAccountBankData")
    @ResponseBody
    public List<JSONObject> getAccountBankData() {
        return userAccountService.findListByUserId(AppUtil.getUser().getId());
    }

}
