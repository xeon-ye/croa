package com.qinfei.qferp.controller.crm;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.crm.CrmCompany;
import com.qinfei.qferp.entity.crm.CrmCompanyProtect;
import com.qinfei.qferp.entity.crm.CrmCompanyUser;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.crm.ICrmCompanyService;
import com.qinfei.qferp.service.crm.ICrmCompanyUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表：crm客户公司表(TCrmCompany)表控制层
 *
 * @author jca
 * @since 2020-07-07 17:27:24
 */
@RestController
@RequestMapping("/crm/company")
public class CrmCompanyController {
    /**
     * 服务对象
     */
    @Resource
    private ICrmCompanyService crmCompanyService;
    @Resource
    private ICrmCompanyUserService companyUserService;

    @Autowired
    private Config config ;

    /**
     * 列表查询
     *
     */
    @RequestMapping("/list")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "客户管理", note = "客户管理/我的客户")
    public PageInfo list(@PageableDefault(size = 10) Pageable pageable, @RequestParam Map map) {
        return companyUserService.list(map, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * 列表查询
     *
     */
    @RequestMapping("/listPublic")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "客户管理", note = "客户管理/公海客户")
    public PageInfo listPublic(@PageableDefault(size = 10) Pageable pageable, @RequestParam("companyName") String companyName) {
        if(companyName.length() < 2){
            return new PageInfo();
        }
        Map map = new HashMap();
        map.put("companyName", companyName);
        return companyUserService.listPublic(map, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * 列表查询
     *
     */
    @RequestMapping("/listInquire")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "客户管理", note = "客户管理/客户查询")
    public PageInfo listInquire(@PageableDefault Pageable pageable, @RequestParam Map map) {
        if(map.get("companyName") == null){
            return null;
        }
        return companyUserService.listInquire(map, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/view")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        try{
            CrmCompany company = crmCompanyService.getById(id);
            data.putDataValue("entity",company);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @GetMapping("/viewDetail")
    @ResponseBody
    public ResponseData viewDetail(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        try{
            Map company = crmCompanyService.getByIdDetail(id);
            data.putDataValue("entity",company);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/saveCompany")
    @ResponseBody
    @Verify(code = "/crm/company/saveCompany", module = "客户管理/登记客户")
    public ResponseData saveCompany(@RequestParam Map map) {
        try{
            crmCompanyService.saveCompany(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return ResponseData.ok();
    }

    @RequestMapping("/getBasicById")
    @ResponseBody
    public ResponseData getBasicById(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        try{
            Map map = companyUserService.getBasicById(id);
            Object mobile = map.get("mobile");
            if(mobile != null && mobile.toString().length() > 15){
                map.put("mobile", EncryptUtils.decrypt(mobile.toString()));
            }
            Object wechat = map.get("wechat");
            if(wechat != null && wechat.toString().length() > 15){
                map.put("wechat", EncryptUtils.decrypt(wechat.toString()));
            }
            Object qq = map.get("qq");
            if(qq != null && qq.toString().length() > 15){
                map.put("qq", EncryptUtils.decrypt(qq.toString()));
            }
            data.putDataValue("entity", map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/updateCompanyBasic")
    @ResponseBody
    @Verify(code = "/crm/company/updateCompanyBasic", module = "客户管理/修改公司名")
    public ResponseData updateCompanyBasic(@RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.updateCompanyBasic(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }

    @RequestMapping("/updateCompany")
    @ResponseBody
    public ResponseData updateCompany(@RequestParam Map map, @RequestParam(value ="image", required = false) MultipartFile image) {
        ResponseData data = ResponseData.ok();
        try{
            if(image != null && image.getSize()>0){
                String temp = image.getOriginalFilename() ;
                String ext = null ;
                if(temp.indexOf(".")>-1){
                    ext = temp.substring(temp.lastIndexOf(".")) ;
                }
                String fileName = UUIDUtil.get32UUID() + ext;
                String childPath =getStringData()+ "/crm/company/";
                File destFile = new File(config.getUploadDir() + childPath + fileName);
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                image.transferTo(destFile);
                String path = config.getWebDir() + childPath + fileName;
                map.put("image", path);
            }
            crmCompanyService.updateCompany(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @GetMapping("/viewCompanyUser")
    @ResponseBody
    public ResponseData viewCompanyUser(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        try{
            CrmCompanyUser companyUser = companyUserService.getById(id);
            String mobile = companyUser.getMobile();
            if(mobile != null && mobile.length() > 15){
                companyUser.setMobile(EncryptUtils.decrypt(mobile));
            }
            String wechat = companyUser.getWechat();
            if(wechat != null && wechat.length() > 15){
                companyUser.setWechat(EncryptUtils.decrypt(wechat));
            }
            String qq = companyUser.getQq();
            if(qq != null && qq.length() > 15){
                companyUser.setQq(EncryptUtils.decrypt(qq));
            }
            String phone = companyUser.getPhone();
            if(phone != null && phone.length() > 15){
                companyUser.setPhone(EncryptUtils.decrypt(phone));
            }
            data.putDataValue("entity",companyUser);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/updateCompanyUser")
    @ResponseBody
    public ResponseData updateCompanyUser(@RequestParam Map map, @RequestParam(value ="photo", required = false) MultipartFile photo) {
        ResponseData data = ResponseData.ok();
        try{
            if(photo != null && photo.getSize()>0){
                String temp = photo.getOriginalFilename() ;
                String ext = null ;
                if(temp.indexOf(".")>-1){
                    ext = temp.substring(temp.lastIndexOf(".")) ;
                }
                String fileName = UUIDUtil.get32UUID() + ext;
                String childPath = getStringData()+"/crm/companyUser/";
                File destFile = new File(config.getUploadDir() + childPath + fileName);
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                photo.transferTo(destFile);
                String path = config.getWebDir() + childPath + fileName;
                map.put("photo", path);
            }
            companyUserService.updateCompanyUser(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/delCompanyUser")
    @ResponseBody
    public ResponseData delCompanyUser(@RequestParam("companyUserId") Integer companyUserId) {
        ResponseData data = ResponseData.ok();
        try{
            Map map = new HashMap();
            map.put("id", companyUserId);
            companyUserService.delCompanyUser(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @GetMapping("/addPublic")
    @ResponseBody
    @Verify(code = "/crm/company/savePublic", module = "客户管理/抛入公海")
    public ResponseData addPublic(@RequestParam("companyUserId") Integer companyUserId) {
        ResponseData data = ResponseData.ok();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                Map map = companyUserService.queryUserBasicInfoByCompanyId(companyUserId, user.getId());
                Object mobile = map.get("mobile");
                if(mobile != null && mobile.toString().length() > 15){
                    map.put("mobile", EncryptUtils.decrypt(mobile.toString()));
                }
                Object wechat = map.get("wechat");
                if(wechat != null && wechat.toString().length() > 15){
                    map.put("wechat", EncryptUtils.decrypt(wechat.toString()));
                }
                Object qq = map.get("qq");
                if(qq != null && qq.toString().length() > 15){
                    map.put("qq", EncryptUtils.decrypt(qq.toString()));
                }
                data.putDataValue("entity",map);
            }
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/savePublic")
    @ResponseBody
    @Verify(code = "/crm/company/savePublic", module = "客户管理/抛入公海")
    public ResponseData savePublic(@RequestParam("companyUserId") Integer companyUserId) {
        ResponseData data = ResponseData.ok();
        try{
            companyUserService.savePublic(companyUserId);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/bind")
    @ResponseBody
    @Verify(code = "/crm/company/bind", module = "客户管理/公海认领客户")
    public ResponseData bind(@RequestParam("companyUserId") Integer companyUserId) {
        ResponseData data = ResponseData.ok();
        try{
            companyUserService.bind(companyUserId);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @GetMapping("/viewCompanyAll")
    @ResponseBody
    public ResponseData viewCompanyAll(@RequestParam("companyId") Integer companyId, @RequestParam("companyUserId") Integer companyUserId) {
        ResponseData data = ResponseData.ok();
        try{
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            Map company = crmCompanyService.getByIdDetail(companyId);
            CrmCompanyUser companyUser = companyUserService.getById(companyUserId);
            Boolean ywFlag = companyUserService.getYwFlag(companyUserId, user.getId());
            Boolean adminFlag = false;
            if(roles != null && roles.size() > 0){
                for(Role role : roles){
                    if(IConst.ROLE_TYPE_XT.equals(role.getType()) && IConst.ROLE_CODE_ZY.equals(role.getCode())){
                        adminFlag = true;
                    }
                }
            }
            String mobile = companyUser.getMobile();
            if(mobile != null && mobile.length() > 15){
                companyUser.setMobile(EncryptUtils.decrypt(mobile));
            }
            String wechat = companyUser.getWechat();
            if(wechat != null && wechat.length() > 15){
                companyUser.setWechat(EncryptUtils.decrypt(wechat));
            }
            String qq = companyUser.getQq();
            if(qq != null && qq.length() > 15){
                companyUser.setQq(EncryptUtils.decrypt(qq));
            }
            String phone = companyUser.getPhone();
            if(phone != null && phone.length() > 15){
                companyUser.setPhone(EncryptUtils.decrypt(phone));
            }
            data.putDataValue("company",company);
            data.putDataValue("companyUser",companyUser);
            data.putDataValue("ywFlag", ywFlag);
            data.putDataValue("adminFlag", adminFlag);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/productList")
    @ResponseBody
    public PageInfo productList(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        if(map.containsKey("companyId")){
            Integer companyId = MapUtils.getInteger(map, "companyId");
            return crmCompanyService.productList(companyId, pageable.getPageNumber(), pageable.getPageSize());
        }else{
            return null;
        }
    }

    @GetMapping("/viewProduct")
    @ResponseBody
    public ResponseData viewProduct(@RequestParam("productId") Integer productId) {
        ResponseData data = ResponseData.ok();
        try{
            Map map = crmCompanyService.getProductById(productId);
            data.putDataValue("entity",map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @GetMapping("/saveProduct")
    @ResponseBody
    public ResponseData saveProduct(@RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.saveProduct(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/updateProduct")
    @ResponseBody
    public ResponseData updateProduct(@RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.updateProduct(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/delProduct")
    @ResponseBody
    public ResponseData delProduct(@RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.delProduct(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/consumerList")
    @ResponseBody
    public PageInfo consumerList(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        if(map.containsKey("companyId")){
            Integer companyId = MapUtils.getInteger(map, "companyId");
            return crmCompanyService.consumerList(companyId, pageable.getPageNumber(), pageable.getPageSize());
        }else{
            return null;
        }
    }

    @GetMapping("/viewConsumer")
    @ResponseBody
    public ResponseData viewCompanyConsumer(@RequestParam("consumerId") Integer consumerId) {
        ResponseData data = ResponseData.ok();
        try{
            Map map = crmCompanyService.getConsumerById(consumerId);
            data.putDataValue("entity",map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @GetMapping("/saveConsumer")
    @ResponseBody
    public ResponseData saveConsumer(@RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.saveConsumer(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/updateConsumer")
    @ResponseBody
    public ResponseData updateConsumer(@RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.updateConsumer(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/delConsumer")
    @ResponseBody
    public ResponseData delConsumer(@RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.delConsumer(map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @GetMapping("/addProtect")
    @ResponseBody
    @Verify(code = "/crm/company/savePublic", module = "客户管理/申请保护")
    public ResponseData addProtect(@RequestParam Map param) {
        ResponseData data = ResponseData.ok();
        try{
            Map map = crmCompanyService.addProtect(param);
            data.putDataValue("entity",map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/queryUserByCompanyId")
    @ResponseBody
    public PageInfo queryUserByCompanyId(@PageableDefault(size = 10) Pageable pageable, @RequestParam Map map) {
        return companyUserService.queryUserByCompanyId(map, pageable.getPageNumber(), pageable.getPageSize());
    }

    @RequestMapping("/saveProtect")
    @ResponseBody
    @Verify(code = "/crm/company/savePublic", module = "客户管理/申请保护")
    public ResponseData saveProtect(CrmCompanyProtect protect) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.saveProtect(protect);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/listProtect")
    @ResponseBody
    public PageInfo listProtect(@PageableDefault(size = 10) Pageable pageable, @RequestParam Map map) {
        return crmCompanyService.listProtect(map, pageable.getPageNumber(), pageable.getPageSize());
    }

    @GetMapping("/viewProtect")
    @ResponseBody
    public ResponseData viewProtect(@RequestParam Map param) {
        ResponseData data = ResponseData.ok();
        try{
            Map map = crmCompanyService.viewProtect(param);
            data.putDataValue("entity",map);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/auditProtectPass")
    @ResponseBody
    public ResponseData auditProtectPass(@RequestParam Map param) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.auditProtectPass(param);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    /*@RequestMapping("/auditProtectSuc")
    @ResponseBody
    public ResponseData auditProtectSuc(@RequestParam Map param) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.auditProtectSuc(param);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }*/

    @RequestMapping("/auditProtectFail")
    @ResponseBody
    public ResponseData auditProtectFail(@RequestParam Map param) {
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.auditProtectFail(param);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/saveTransfer")
    @ResponseBody
    @Verify(code = "/crm/company/saveTransfer", module = "客户管理/客户流转")
    public ResponseData saveTransfer(@RequestParam Map param) {
        ResponseData data = ResponseData.ok();
        try{
            companyUserService.saveTransfer(param);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/transferList")
    @ResponseBody
    public PageInfo list(@PageableDefault(size = 10) Pageable pageable, @RequestParam("companyUserId") Integer companyUserId) {
        return companyUserService.querySalesmanByCompanyUserId(companyUserId, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * 客户跟进表列表查询
     *
     */
    @RequestMapping("/trackList")
    @ResponseBody
    public PageInfo trackList(@PageableDefault(size = 10) Pageable pageable, @RequestParam Map map) {
        return crmCompanyService.trackList(map, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * 客户新增跟进
     * @param map
     * @param files
     * @param pics
     * @return
     */
    @PostMapping("/saveCompanyTrack")
    @ResponseBody
    @Verify(code = "/crm/company/saveCompanyTrack", module = "客户管理/客户跟进")
    public ResponseData saveCompanyTrack(@RequestParam Map map,
                                         @RequestParam(value="files", required = false)MultipartFile[] files,
                                         MultipartFile[] pics) {

        int i = crmCompanyService.saveCompanyTrack(map, files, pics);
        if (i>0){
            return ResponseData.ok();
        }else {
            return ResponseData.customerError(1001,"操作失败");
        }
    }

    @RequestMapping("/companyHistory")
    @ResponseBody
    public PageInfo companyHistory(@PageableDefault(size = 10) Pageable pageable, @RequestParam("companyId") Integer companyId) {
        return crmCompanyService.queryHistoryCompanyId(companyId, pageable.getPageNumber(), pageable.getPageSize());
    }

    @RequestMapping("/companyUserHistory")
    @ResponseBody
    public PageInfo companyUserHistory(@PageableDefault(size = 10) Pageable pageable, @RequestParam("companyUserId") Integer companyUserId) {
        return companyUserService.queryHistoryCompanyUserId(companyUserId, pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * 完善客户查询客户信息
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listCustForYW")
    @ResponseBody
    public PageInfo listCustForYW(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable) {
        return companyUserService.listCustForYW(map, pageable.getPageNumber(), pageable.getPageSize());
    }
    /**
     * 开票退款查询客户信息
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listCustForFee")
    @ResponseBody
    public PageInfo listCustForFee(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable) {
        return companyUserService.listCustForFee(map, pageable.getPageNumber(), pageable.getPageSize());
    }

    @RequestMapping("/listCompanyUser")
    @ResponseBody
    public List<Map> listCompanyUser(@RequestParam Map map){
        return companyUserService.listCompanyUser(map);
    }

    @RequestMapping("/doWithRepeatName")
    @ResponseBody
    public ResponseData doWithRepeatName(){
        ResponseData data = ResponseData.ok();
        try{
            crmCompanyService.doWithRepeatName();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }
}