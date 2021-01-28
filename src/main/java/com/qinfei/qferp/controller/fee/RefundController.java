package com.qinfei.qferp.controller.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.fee.Refund;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.IRefundService;
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

import javax.servlet.http.HttpServletRequest;
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
@RequestMapping("/refund")
@Api(description = "退款流水接口")
public class RefundController {

    @Autowired
    private IRefundService refundService;
    @Autowired
    private Config config ;

    @ResponseBody
    @RequestMapping("/listPg")
//    @Log(opType = OperateType.QUERY, module = "退款管理|列表", note = "列表")
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
                map.put("user",user) ;
                list = refundService.listPg(pageable.getPageNumber(), pageable.getPageSize(),map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    @RequestMapping(value="/view")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        try{
            ResponseData data = ResponseData.ok();
            Refund entity = refundService.getById(id) ;
            if(entity == null){
                return ResponseData.customerError(1002,"该退款流程已删除！") ;
            }
            Map map = refundService.querySumAmountById(id) ;
            if(map!=null){
                if(entity.getType() == IConst.REFUND_TYPE_REFUND){
                    Double refundAmount = (Double)map.get("sumRefundAmount") ;
                    entity.setApplyAmount(refundAmount);
                }else if(entity.getType() == IConst.REFUND_TYPE_OTHER_PAY){
                    Double otherPay = (Double)map.get("sumOtherPay") ;
                    entity.setApplyAmount(otherPay);
                }
            }
            data.putDataValue("entity",entity) ;
            return data ;
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping(value="/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "财务管理|退款管理", note = "删除退款")
    @Verify(code = "/refund/del", module = "财务管理/删除退款")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            Refund entity = refundService.getById(id) ;
            if(entity==null){
                return ResponseData.customerError(1001, "该流程已删除，请刷新后重试！");
            }
            //state=0||state=1才能删除
            if(entity.getState()==IConst.STATE_REJECT||entity.getState()==IConst.STATE_SAVE){
                refundService.delById(entity);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                return data;
            }else{
                return ResponseData.customerError(1001, "当前状态不支持删除！");
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
     * 暂不启用，使用saveStepOne()替代
     * @param entity
     * @return
     */
    @RequestMapping("/add")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "退款管理|添加", note = "添加")
    public ResponseData add(Refund entity) {
        try{
            refundService.add(entity);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", entity) ;

            return data ;
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 导出退款信息
     * @param map,response
     * @return
     */
    @RequestMapping("/exportRefund")
    @Log(opType = OperateType.ADD, module = "退款管理|导出退款信息", note = "导出退款信息")
    @ResponseBody
    public void exportRefund(HttpServletResponse response,@RequestParam Map map){
        try{
            User user = AppUtil.getUser();
            List<Role> roleList = user.getRoles();
            if(roleList==null && roleList.size()==0){
                throw new Exception("未查到角色信息");
            }else{
                map.put("roleType",roleList.get(0).getType()) ;
                map.put("roleCode",roleList.get(0).getCode()) ;
                map.put("user",user) ;
                response.setContentType("application/binary;charset=UTf-8");
                response.setHeader("Content-Disposition","attachment;fileName="+ URLEncoder.encode("退款导出"+ DateUtils.getNowTime()+".xls","UTF-8"));
                OutputStream out = response.getOutputStream();
                refundService.exportRefund(map,out);
            }
        }catch (Exception  e){
            log.error("导出失败", e);
        }finally {

        }
    }
    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }


    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|退款管理", note = "修改退款")
    @Verify(code = "/refund/edit", module = "财务管理/修改退款")
    public ResponseData edit(Refund entity,@RequestParam(value = "file", required = false) MultipartFile[] multipartFiles) {
        try{
            //state=0||state=-1才能编辑
            Refund old = refundService.getById(entity.getId());
            if(old.getState()==IConst.STATE_REJECT||old.getState()==IConst.STATE_SAVE){
                List<String> picNames = new ArrayList<>();
                List<String> picPaths = new ArrayList<>();
                //附件处理逻辑：1、如果取得的multipartFiles.length>1,那么一定是上传了多个新附件，直接使用二进制存储
                //2、如果multipartFiles.length=1，那么可能没有上传附件，也可能上传了一个附件
                //3、如果上传了一个附件，multipartFile.getSize()=1,二进制存储
                //4、如果没有上传新附件，直接把old中的附件拿过来
                if(multipartFiles.length>1){//表示上传了新附件
                    for(MultipartFile multipartFile : multipartFiles) {
                        if(multipartFile.getSize()>0){
                            String temp = multipartFile.getOriginalFilename();
                            String ext = null;
                            if (temp.indexOf(".") > -1) {
                                ext = temp.substring(temp.lastIndexOf("."));
                            }
                            String fileName = UUIDUtil.get32UUID() + ext;
                            String childPath = getStringData()+"/fee/refund/";
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
                }else{
                    MultipartFile multipartFile = multipartFiles[0] ;
                    if(multipartFile.getSize()>0){//表示上传了新附件
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath =getStringData()+ "/fee/refund/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        picNames.add(multipartFile.getOriginalFilename());
                        picPaths.add(config.getWebDir() + childPath + fileName);
                        entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                        entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
                    }else{//表示附件没有变化
                        entity.setAffixName(old.getAffixName());
                        entity.setAffixLink(old.getAffixLink());
                    }
                }
                entity.setItemId(old.getItemId());
                refundService.edit(entity) ;
                ResponseData data = ResponseData.ok() ;
                data.putDataValue("message","操作成功");
                data.putDataValue("entity", entity) ;
                return data ;
            }else{
                return ResponseData.customerError(1002,"当前状态不支持修改！") ;
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
     * 出纳出款
     * @param map
     * @return
     */
    @RequestMapping("/confirm")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|退款管理", note = "出纳确认退款")
    @Verify(code = "/refund/confirm", module = "财务管理/出纳确认退款")
    public ResponseData confirm(@RequestParam Map map) {
        try{
            if(ObjectUtils.isEmpty(map.get("id"))
                    || ObjectUtils.isEmpty(map.get("outAccountIds"))
                    || ObjectUtils.isEmpty(map.get("payAmount"))){
                return ResponseData.customerError(1002,"未获取到出账账户或金额！") ;
            }
            Integer id = Integer.parseInt((String)map.get("id")) ;
            User user = AppUtil.getUser() ;
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType())&&
                            IConst.ROLE_CODE_CN.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if(flag){
                Refund entity = refundService.getById(id) ;
                //state=2才能出款
                if(entity.getState()==IConst.STATE_PASS || entity.getState() == IConst.STATE_CN){
                    refundService.confirm(entity,map) ;
                    ResponseData data = ResponseData.ok() ;
                    data.putDataValue("message","操作成功");
                    data.putDataValue("entity", entity) ;
                    return data ;
                }else{
                    return ResponseData.customerError(1002,"当前状态不支持该操作！") ;
                }
            }else{
                return ResponseData.customerError(1002,"当前用户没有操作权限！") ;
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
     * 财务负责人确认出款操作
     * @param id
     * @return
     */
    @RequestMapping("/checkBtoB")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|退款管理", note = "退款财务负责人确认公账出款")
    @Verify(code = "/refund/checkBtoB", module = "财务管理/退款财务负责人复核公账出款")
    public ResponseData checkBtoB(@RequestParam("id") Integer id) {
        try{
            User user = AppUtil.getUser() ;
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType())&&IConst.ROLE_CODE_KJ.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if(flag){//财务负责人确认出款操作
                Refund entity = refundService.getById(id) ;
                if(entity.getState()==IConst.STATE_KJ){
                    entity.setState(IConst.STATE_FINISH);//1通过
                    entity.setUpdateUserId(user.getId());
                    refundService.checkBtoB(entity);
                    ResponseData data = ResponseData.ok() ;
                    data.putDataValue("message","操作成功");
                    data.putDataValue("entity", entity) ;
                    return data ;
                }else{
                    return ResponseData.customerError(1002,"当前状态不支持确认出款操作") ;
                }
            }else{
                return ResponseData.customerError(1002,"当前用户不支持确认出款操作") ;
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
     * 根据退款id查询出关联的稿件
     * @param pageable
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgForSelectedArticle")
    public PageInfo<Map> listPgForSelectedArticle(@PageableDefault(size = 5) Pageable pageable,@RequestParam("id") Integer id) {
        PageInfo<Map> list = refundService.listPgForSelectedArticle(pageable.getPageNumber(), pageable.getPageSize(),id);
        return list;
    }

    /**
     * 查询出可退款的稿件
     * @param pageable
     * @param map
     * @return
     */
    @RequestMapping(value="/listPgForSelectArticle")
    @ResponseBody
    public PageInfo<Map> listPgForSelectArticle(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        User user = AppUtil.getUser() ;
        map.put("id",user.getId());
        PageInfo<Map> list = refundService.listPgForSelectArticle(pageable.getPageNumber(), pageable.getPageSize(),map);
        return list;
    }
    /**
     * 查询出可其他支出的稿件
     * @param pageable
     * @param map
     * @return
     */
    @RequestMapping(value="/listPgForSelectArticle2")
    @ResponseBody
    public PageInfo<Map> listPgForSelectArticle2(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        User user = AppUtil.getUser() ;
        map.put("id",user.getId());
        PageInfo<Map> list = refundService.listPgForSelectArticle2(pageable.getPageNumber(), pageable.getPageSize(),map);
        return list;
    }

    @RequestMapping("/saveStepOne")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "财务管理|退款管理", note = "新增退款")
    @Verify(code = "/refund/saveStepOne", module = "财务管理/新增退款")
    public ResponseData saveStepOne(@RequestParam Map map) {
        try{
            ResponseData data = ResponseData.ok() ;
            if(ObjectUtils.isEmpty(map.get("custCompanyIdSec"))
                    || ObjectUtils.isEmpty(map.get("custIdSec"))
                    || ObjectUtils.isEmpty(map.get("typeSec"))){
                return ResponseData.customerError(1002,"未选择客户或类型！") ;
            }else{
                Refund entity = refundService.saveStepOne(map);
                data.putDataValue("entity",entity) ;
                data.putDataValue("message","操作成功");
                return data ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping("/queryRefundId")
    @ResponseBody
    public Integer queryRefundId(@RequestParam("articleId") Integer articleId){
        return refundService.queryRefundId(articleId);
    }

    @RequestMapping("/queryOtherPayId")
    @ResponseBody
    public Integer queryOtherPayId(@RequestParam("articleId") Integer articleId){
        return refundService.queryOtherPayId(articleId);
    }

    /**
     * state=2时财务撤回，此时，稿件状态还未变更
     * 1、完成待办
     * 2、增加新的待办
     * 3、请款订单状态修改为-1
     * @param
     */
    @RequestMapping("/CWReject")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "财务管理|退款管理", note = "财务撤回")
    public ResponseData CWReject(@RequestParam("id") Integer id) {
        try{
            User user = AppUtil.getUser() ;
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType())&&IConst.ROLE_CODE_CN.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if(flag){
                Refund entity = refundService.getById(id) ;

                if(entity.getState()==IConst.STATE_PASS || entity.getState()==IConst.STATE_CN ){
                    entity.setState(IConst.STATE_REJECT);
                    entity.setUpdateUserId(user.getId());
                    refundService.CWReject(entity) ;
                    ResponseData data = ResponseData.ok() ;
                    data.putDataValue("message","操作成功");
                    data.putDataValue("entity", entity) ;
                    return data ;
                }else{
                    return ResponseData.customerError(1002,"当前状态不能撤回！") ;
                }
            }else{
                return  ResponseData.customerError(1002,"当前用户无法撤回，请联系财务出纳撤回！") ;
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
     * state=1或state=12时财务撤回，需要变更以下内容
     * 1、关联的稿件请款状态修改为请款中2
     * 3、完成待办
     * 4、增加新的待办
     * 5、请款订单状态修改为-1
     * @param
     */
    @RequestMapping("/CWReturn")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "财务管理|退款管理", note = "财务驳回")
    public ResponseData CWReturn(@RequestParam("id") Integer id) {
        try{
            User user = AppUtil.getUser() ;
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType())&&IConst.ROLE_CODE_KJ.equals(role.getCode())) {
                        flag = true;
                    }
                }
            }
            if(flag){
                Refund entity = refundService.getById(id) ;
                if(entity.getState()==IConst.STATE_FINISH ||entity.getState()==IConst.STATE_KJ){

                    Boolean returnFlag = refundService.CWReturn(entity) ;
                    if(returnFlag){
                        ResponseData data = ResponseData.ok() ;
                        data.putDataValue("message","操作成功");
                        data.putDataValue("entity", entity) ;
                        return data ;
                    }else{
                        return ResponseData.customerError(1001,"撤回失败，请联系管理员！") ;
                    }
                }else{
                    return ResponseData.customerError(1001,"当前状态不能撤回！") ;
                }
            }else{
                return  ResponseData.customerError(1001,"当前用户无法撤回，请联系财务出纳撤回！") ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping("reimburseSum")
    @ResponseBody
    public Map reimburseSum (@RequestParam Map map){
       Map list = null ;
        try{
            User user = AppUtil.getUser() ;
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("user",user) ;
                list = refundService.reimburseSum(map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    @RequestMapping("/changeAccount")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|退款更改出款账号", note = "退款更改出款账号")
    public ResponseData changeAccount(@RequestParam Map map) {
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
                Refund entity = refundService.getById(id) ;
                //审核通过才能出款
                if(entity.getState()==IConst.STATE_FINISH){
                    refundService.changeAccount(entity,map);
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

    @ApiOperation(value = "退款管理", notes = "下载退款信息")
    @Verify(code = "/fee/flowPrint", module = "退款管理/下载退款信息")
    @PostMapping("downloadData")
    @ResponseBody
    public ResponseData downloadBorrowData(@RequestParam Map<String, Object> param){
        try{
            ResponseData responseData = ResponseData.ok();
            String fileName = refundService.downloadData(param);
            if (StringUtils.isEmpty(fileName)) {
                responseData.putDataValue("message", "退款信息不存在！");
            } else {
                responseData.putDataValue("file", fileName);
            }
            return responseData;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "下载退款信息异常！");
        }
    }

    @ApiOperation(value = "退款管理", notes = "批量下载退款信息")
    @Verify(code = "/fee/flowPrint", module = "退款管理/批量下载退款信息")
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
            String fileName = refundService.batchDownloadData(param);
            if (StringUtils.isEmpty(fileName)) {
                responseData.putDataValue("message", "退款信息不存在！");
            } else {
                responseData.putDataValue("file", fileName);
            }
            return responseData;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "批量下载退款信息异常！");
        }
    }

    @ApiOperation(value = "退款管理", notes = "批量打印退款信息")
    @Verify(code = "/fee/flowPrint", module = "退款管理/批量打印退款信息")
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
            return ResponseData.ok().putDataValue("list", refundService.listRefundData(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "批量打印退款信息异常！");
        }
    }

    @ApiOperation(value = "退款管理", notes = "获取打印权限")
    @PostMapping("getFlowPrintPermission")
    @ResponseBody
    public ResponseData getFlowPrintPermission(HttpServletRequest request){
        try{
            if(refundService.getFlowPrintPermission(request)){
                return ResponseData.ok();
            }else {
                return ResponseData.customerError(1002, "无打印权限！");
            }
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "获取打印权限异常！");
        }
    }

}
