package com.qinfei.qferp.controller.fee;
import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.OfficeUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.fee.Reimbursement;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.IReimbursementService;
import com.qinfei.qferp.service.impl.fee.BorrowService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.runtime.regexp.joni.EncodingHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EncodingUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.xmlbeans.impl.common.EncodingMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.spec.EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/reimbursement")
@Api(description="费用报销接口")
class ReimbursementController {

    @Autowired
    private IReimbursementService reimbursementService;

    @Autowired
    private Config config ;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    OfficeUtils officeUtils;

    /**
     * 查询数据表中所有的数据，并将其返回给前台
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPg")
    public PageInfo<Map> listPg(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        PageInfo<Map> list = null ;
        try{
            //获取当前登录的对象
            User user = AppUtil.getUser() ;
            //获取当前对象的角色
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("user",user) ;
                list = reimbursementService.listPg(pageable.getPageNumber(), pageable.getPageSize(),map);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 预览文件
     * @return
     */
    @ResponseBody
    @RequestMapping("/previewFile")
    public ResponseData previewFile(@RequestParam("fileName")String fileName, @Param("filePath")String filePath) {
        BufferedReader in=null;
        try {
            ResponseData data = ResponseData.ok();
            if(StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(filePath)){
                StringBuilder buffer = new StringBuilder();
                filePath = filePath.replace("/statics/","");
                String path = config.getUploadDir()+filePath;
                if(fileName.lastIndexOf(".")==-1){
                    return ResponseData.customerError(1002,"该文件格式有误！");
                }
                String name = fileName.substring(0,fileName.lastIndexOf("."));
                //对txt文件进行特殊处理,pdf页面iframe展示
                if(fileName.toLowerCase().endsWith(".txt") || fileName.toLowerCase().endsWith(".sql")){
                    String enCodeing = codeString(path);
                    //加入编码字符集
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), enCodeing));
                    String line = null;
                    buffer.append("<div style='padding:10px 20px'>");
                    while ((line = in.readLine()) != null) {
                        buffer.append("<p style='font: 16px/30px Microsoft Yahei;'>");
                        buffer.append(line);
                        buffer.append("</p>");
                    }
                    buffer.append("</div>");
                    data.putDataValue("ext","txt");
                    data.putDataValue("stream",buffer.toString());
                    return data;
                }else if(fileName.toLowerCase().endsWith(".csv")) {
                    String enCodeing = codeString(path);
                    //加入编码字符集
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), enCodeing));
                    String line = null;
                    String title = in.readLine().replaceAll("\"","");
                    List<String> content = new ArrayList<>();
                    while ((line = in.readLine()) != null) {
                        line = line.replaceAll("\"","").replaceAll("=","");
                        String reg = "^,+$";
                        //去除空数据
                        if(!line.matches(reg)){
                            content.add(line);
                        }
                    }
                    data.putDataValue("ext","csv");
                    data.putDataValue("title",title);
                    data.putDataValue("content",content);
                    return data;
                }else {
                    String htmlPath = officeUtils.toHtml(name,config.getUploadDir(),path,config.getWebDir());
                    data.putDataValue("stream",htmlPath);
                    return data;
                }
//                else if(fileName.toLowerCase().endsWith(".csv")) {
//                    String htmlPath = officeUtils.toHtml(name,config.getUploadDir(),path,config.getWebDir());
//                    data.putDataValue("stream",htmlPath);
//                    return data;
//                }else if(fileName.toLowerCase().endsWith(".ppt") || fileName.toLowerCase().endsWith(".pptx")) {
//                    String htmlPath = officeUtils.toPDF(name,config.getUploadDir(),path,config.getWebDir());
//                    data.putDataValue("stream",new String(htmlPath.getBytes(),"UTF-8"));
//                    return data;
//                }else if(fileName.toLowerCase().endsWith(".doc") || fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".wps")){
////                    String htmlPath = officeUtils.toPDF(name,config.getUploadDir(),path,config.getWebDir());
////                    data.putDataValue("stream",new String(htmlPath.getBytes(),"UTF-8"));
////                    return data;
//                }else{
//                    String htmlPath = officeUtils.toHtml(name,config.getUploadDir(),path,config.getWebDir());
//                    data.putDataValue("stream",htmlPath);
//                    return data;
//                }
            }else{
                return ResponseData.customerError(1002,"抱歉，文件找不到。");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，文件流异常。");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，预览文件出错啦，请联系技术人员");
        }finally {
            try {
                if(in!=null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String codeString(String fileName) throws Exception {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
        int p = (bin.read() << 8) + bin.read();
        bin.close();
        String code = null;
        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            case 12074:
                code = "UTF-8";
                break;
            default:
                code = "GBK";
        }
        return code;
    }

    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM/");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }

    /**
     * 在新增报销页面，点击保存按钮即可触发此方法
     * 该方法会将报销的基本信息机器报销记录分别存储于一张主表Reimbursement与一张从表Reunbursement_d
     * 以达到数据的暂时保存
     * 往主表中添加数据时，注意reimbursement_ds这个属性是否为空值
     * 如果reimbursement_ds这个属性若有值，则会报列找不到异常，在进行主表数据插入的时候，一定得将reimbursement_ds置为null。
     * @param entity
     * @param costType
     * @param purpose
     * @param money
     * @param numberOfDocument
     * @param currentTotalPrice
     * @param multipartFile
     * @return
     */
    @RequestMapping(value="/add",produces="text/html;charset=utf-8")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "财务管理|报销管理", note = "新增报销")
    @Verify(code = "/reimbursement/add", module = "报销管理/新增报销")
    public ResponseData add(Reimbursement entity,@RequestParam(value="costType", required = false) List<String> costType,
                            @RequestParam(value="purpose", required = false) List<String> purpose,
                            @RequestParam(value="money", required = false) List<Double> money,
                            @RequestParam(value="numberOfDocument", required = false) List<Integer> numberOfDocument,
                            @RequestParam(value="currentTotalPrice", required = false) List<Double> currentTotalPrice,
                            @RequestParam(value="affix", required = false) MultipartFile[] multipartFile){
        try{
            if(entity.getTotalMoney()==null||entity.getReimbursedMoney()==null||(entity.getUnpaidLoan()==null)){
                throw new QinFeiException(1002,"应报销金额、实报销金额或冲抵借款金额为空，请核实后再试！");
            }
            if(entity.getTotalMoney()>entity.getReimbursedMoney()){
                throw new QinFeiException(1002,"实报销金额不能大于应报销金额！");
            }
            if(entity.getUnpaidLoan()>entity.getTotalMoney()){
                throw new QinFeiException(1002,"冲抵借款金额不能大于实报销金额！");
            }
            //判断附件是否为空
            if(multipartFile!=null) {
                StringBuilder fileNameSb = new StringBuilder();
                StringBuilder filenameLinkSb = new StringBuilder();
                for (int i = 0; i < multipartFile.length; i++) {
                    if(StringUtils.isNotEmpty(multipartFile[i].getOriginalFilename())){
                        String orginalName =  multipartFile[i].getOriginalFilename();
                        Integer extStart = orginalName.lastIndexOf(".");
                        String fileName = UUIDUtil.get32UUID();
                        if(extStart > -1){
                            fileName+=orginalName.substring(extStart);
                        }
                        String childPath =getStringData()+ "fee/reimbursement/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile[i].transferTo(destFile);
                        //将附件的名字及其存放的路径保存起来
                        fileNameSb.append(orginalName + ",");
                        filenameLinkSb.append(config.getWebDir() + childPath + fileName + ",");
                    }
                }
                entity.setAffixName(fileNameSb.toString());
                entity.setAffixLink(filenameLinkSb.toString());
            }
            //设定主键自动增长了，将id设为null
            entity.setId(null);

            reimbursementService.add(entity,costType,purpose,money,numberOfDocument,currentTotalPrice) ;

            ResponseData data = ResponseData.ok();
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", entity);
            return data;
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 报销的修改
     * 点击编辑页面中的保存按钮时，触发此方法
     * 对编辑页面的每一条数据进行更新
     * @param entity
     * @param costType
     * @param purpose
     * @param money
     * @param numberOfDocument
     * @param currentTotalPrice
     * @param multipartFile
     * @return
     */
    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|报销管理", note = "修改报销")
    @Verify(code = "/reimbursement/edit", module = "报销管理/修改报销")
    public ResponseData edit(Reimbursement entity,@RequestParam(value="costType", required = false) List<String> costType,
                             @RequestParam(value="purpose", required = false) List<String> purpose,
                             @RequestParam(value="money", required = false) List<Double> money,
                             @RequestParam(value="numberOfDocument", required = false) List<Integer> numberOfDocument,
                             @RequestParam(value="currentTotalPrice", required = false) List<Double> currentTotalPrice,
                             @RequestParam(value ="affix", required = false) MultipartFile[] multipartFile) {
        try{
            if(entity.getTotalMoney()==null||entity.getReimbursedMoney()==null||(entity.getUnpaidLoan()==null)){
                throw new QinFeiException(1002,"应报销金额、实报销金额或冲抵借款金额为空，请核实后再试！");
            }
            if(entity.getTotalMoney()>entity.getReimbursedMoney()){
                throw new QinFeiException(1002,"实报销金额不能大于应报销金额！");
            }
            if(entity.getUnpaidLoan()>entity.getTotalMoney()){
                throw new QinFeiException(1002,"冲抵借款金额不能大于实报销金额！");
            }
            //通过选中的记录的id，查询数据库，并判定该记录的状态是否可被删除
            Reimbursement old = reimbursementService.getById(entity.getId());
            if(old.getState()==IConst.STATE_SAVE||old.getState()==IConst.STATE_REJECT){
                //判断前台是否传过来文件，若为有，给予保存信息
                //判断附件是否为空
                if(multipartFile!=null) {
                    StringBuilder fileNameSb = new StringBuilder();
                    StringBuilder filenameLinkSb = new StringBuilder();
                    for (int i = 0; i < multipartFile.length; i++) {
                        if(StringUtils.isNotEmpty(multipartFile[i].getOriginalFilename())){
                            String orginalName =  multipartFile[i].getOriginalFilename();
                            Integer extStart = orginalName.lastIndexOf(".");
                            String fileName = UUIDUtil.get32UUID();
                            if(extStart > -1){
                                fileName+=orginalName.substring(extStart);
                            }
                            String childPath = getStringData()+"fee/reimbursement/";
                            File destFile = new File(config.getUploadDir() + childPath + fileName);
                            if (!destFile.getParentFile().exists()) {
                                destFile.getParentFile().mkdirs();
                            }
                            multipartFile[i].transferTo(destFile);
                            //将附件的名字及其存放的路径保存起来
                            fileNameSb.append(orginalName + ",");
                            filenameLinkSb.append(config.getWebDir() + childPath + fileName + ",");
                        }
                    }
                    if(StringUtils.isNotEmpty(fileNameSb.toString()) && StringUtils.isNotEmpty(filenameLinkSb.toString())){
                        entity.setAffixName(fileNameSb.toString());
                        entity.setAffixLink(filenameLinkSb.toString());
                    }
                }
                //将完整的实体类发送给reimbursementService，方便其调用更新方法
                reimbursementService.edit(entity,costType,purpose,money,numberOfDocument,currentTotalPrice);
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

    private Reimbursement dealAffix(MultipartFile[] multipartFiles,Reimbursement entity){
        try{
            List<String> picNames = new ArrayList<>();
            List<String> picPaths = new ArrayList<>();
            //附件处理逻辑：1、如果取得的multipartFiles.length>1,那么一定是上传了多个新附件，直接使用二进制存储
            //2、如果multipartFiles.length=1，那么可能没有上传附件，也可能上传了一个附件
            //3、如果上传了一个附件，multipartFile.getSize()=1,二进制存储
            if(multipartFiles.length>1){//表示上传了新附件
                for(MultipartFile multipartFile : multipartFiles) {
                    if(multipartFile.getSize()>0){
                        String temp = multipartFile.getOriginalFilename() ;
                        String ext = null ;
                        if(temp.indexOf(".")>-1){
                            ext = temp.substring(temp.lastIndexOf(".")) ;
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = getStringData()+"/fee/reimbursement/";
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
                    String temp = multipartFile.getOriginalFilename() ;
                    String ext = null ;
                    if(temp.indexOf(".")>-1){
                        ext = temp.substring(temp.lastIndexOf(".")) ;
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/fee/reimbursement/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                    entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                    entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
                }
//                else{//表示附件没有变化
//                    entity.setAffixName(old.getAffixName());
//                    entity.setAffixLink(old.getAffixLink());
//                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return entity ;
    }
    /**
     * 点击编辑，查询当前选中报销的基本信息
     * 把查询到的结果返回到前台，并进行结果的展示
     * @param id
     * @return
     */
    @RequestMapping(value="/view")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        try{
            ResponseData data = ResponseData.ok();
            Reimbursement entity = reimbursementService.getById(id);
            if(entity == null){
                return ResponseData.customerError(1002,"该报销记录已删除！") ;
            }
            String [] link ;
            if(StringUtil.isNotEmpty(entity.getAffixLink())){
                if(entity.getAffixLink().indexOf(",")>-1){
                    link = entity.getAffixLink().split(",");
                }else{
                    link = new String[1] ;
                    link[0] = entity.getAffixLink() ;
                }
                entity.setLinks(link);
            }else{
                entity.setLinks(null);
            }

            String [] name ;
            if(StringUtil.isNotEmpty(entity.getAffixName())){
                if(entity.getAffixName().indexOf(",")>-1){
                    name = entity.getAffixName().split(",") ;
                }else{
                    name = new String[1] ;
                    name[0] = entity.getAffixName() ;
                }
                entity.setNames(name);
            }else{
                entity.setNames(null);
            }

            data.putDataValue("entity",entity) ;
            if(entity.getUnpaidLoan()>0){
                List<Map> list = reimbursementService.queryBorrowMapByRemId(entity.getId()) ;
                data.putDataValue("list",list) ;
            }
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
     * 点击删除触发的方法,点击删除后，删除有关该报销的基本信息，以及报销记录
     * @param id
     * @return
     */
    @RequestMapping(value="/del")
    @ResponseBody
//    @Log(opType = OperateType.DELETE, module = "财务管理|费用报销", note = "删除报销")
    @Verify(code = "/reimbursement/del", module = "费用报销/删除报销")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            Reimbursement entity = reimbursementService.getById(id) ;
            if(entity==null) {
                return ResponseData.customerError(1002, "该报销流程已删除！");
            }
            if(entity.getState()== IConst.STATE_REJECT||entity.getState()==IConst.STATE_SAVE){
                reimbursementService.delById(entity) ;
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                return data;
            }else{
                return ResponseData.customerError(1001, "当前状态不支持删除操作");
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value="/queryBorrowByRemId")
    @ResponseBody
    public ResponseData queryBorrowById(@RequestParam("id") Integer id) {
        try{
            ResponseData data = ResponseData.ok();
            List<Map> list = reimbursementService.queryBorrowMapByRemId(id) ;
            data.putDataValue("list",list) ;
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
     * 保存冲抵的借款信息
     * @param map
     * @return
     */
    @RequestMapping("/saveBorrowInfo")
    @ResponseBody
    public ResponseData saveBorrowInfo(@RequestParam Map map) {
        try{
            ResponseData data = ResponseData.ok() ;
            if(ObjectUtils.isEmpty(map.get("id")) || ObjectUtils.isEmpty(map.get("borrowIds"))){
                return ResponseData.customerError(1001,"未选择借款信息！") ;
            }else{
                List<Map> list = reimbursementService.saveBorrowInfo(map);
                data.putDataValue("message","操作成功");
                data.putDataValue("list",list) ;
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

    /**
     * 清除冲抵的借款信息
     * @return
     */
    @RequestMapping("/cleanBorrowInfo")
    @ResponseBody
    public ResponseData cleanBorrowInfo(@RequestParam("id") Integer id) {
        try{
            ResponseData data = ResponseData.ok() ;
            reimbursementService.cleanBorrowInfo(id) ;
            data.putDataValue("message","操作成功");
            return data ;
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }


    @RequestMapping("/confirm")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|报销管理", note = "报销出纳出款")
    @Verify(code = "/reimbursement/confirm", module = "报销管理/报销出纳出款")
    public ResponseData confirm(@RequestParam Map map) {
        try{
            if(ObjectUtils.isEmpty("id")
                    || ObjectUtils.isEmpty("outAccountId")
                    || ObjectUtils.isEmpty("payAmount")){
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
            ResponseData data = ResponseData.ok();
            if(flag){
                Reimbursement entity = reimbursementService.getById(id) ;
                if(entity.getState()==IConst.STATE_CN){
                    reimbursementService.confirm(entity,map);
                    data.putDataValue("message","操作完成");
                    return data ;
                }else{
                    return ResponseData.customerError(1002,"当前状态不支持该操作") ;
                }
            }else{
                return ResponseData.customerError(1002,"当前用户不支持该操作") ;
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
//    @Log(opType = OperateType.UPDATE, module = "财务管理|报销管理", note = "报销复核公账出款")
    @Verify(code = "/reimbursement/checkBtoB", module = "报销管理/报销复核公账出款")
    public ResponseData checkBtoB(@RequestParam("id") Integer id) {
        try{
            User user = AppUtil.getUser() ;
            Boolean flag = false;

            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : user.getRoles()) {
                    if (IConst.ROLE_TYPE_CW.equals(role.getType())&&(IConst.ROLE_CODE_BZ.equals(role.getCode()) ||IConst.ROLE_CODE_KJ.equals(role.getCode())) ) {
                        flag = true;
                    }
                }
            }
            if(flag){//财务负责人确认出款操作
                Reimbursement  entity = reimbursementService.getById(id) ;
                if(entity.getState()==IConst.STATE_KJ){
                    reimbursementService.checkBtoB(entity);
                    ResponseData data = ResponseData.ok() ;
                    data.putDataValue("message","操作成功");
                    data.putDataValue("entity", entity) ;
                    return data ;
                }else{
                    return ResponseData.customerError(1002,"当前状态不支持复核出款操作") ;
                }
            }else{
                return ResponseData.customerError(1002,"当前用户不支持复核出款操作") ;
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
     * 导出报销
     * @param response
     * @param map
     */
    @RequestMapping("/export")
    @Log(opType = OperateType.QUERY, module = "财务管理|报销管理", note = "报销导出")
    @ResponseBody
    public void export(HttpServletResponse response, @RequestParam Map map){
        try{
            User user = AppUtil.getUser();
            List<Role> roleList = user.getRoles();
            if(roleList==null || roleList.size()==0){
                throw new Exception("未查到角色信息");
            }else{
                map.put("roleType",roleList.get(0).getType());
                map.put("roleCode",roleList.get(0).getCode());
                map.put("user",user);
                response.setContentType("application/binary;charset=utf-8");
                response.setHeader("Content-Disposition","attachment;fileName="+ URLEncoder.encode("报销导出"+ DateUtils.getNowTime()+".xls","utf-8"));
                OutputStream out = response.getOutputStream();
                reimbursementService.export(map,out);
            }
        }catch (Exception e){
            log.error("报销导出失败",e);
        }
    }

    @RequestMapping("/getByAdmId")
//    @Log(opType = OperateType.QUERY, module = "财务管理|报销管理", note = "通过出差id获取报销信息")
    @ResponseBody
    public ResponseData getByAdmId(Integer admId){
        return reimbursementService.getByAdmId(admId);
    }

    @RequestMapping("reimburseSum")
    @ResponseBody
    public Map reimburseSum(@RequestParam Map map){
        try{
            //获取当前登录的对象
            User user = AppUtil.getUser() ;
            //获取当前对象的角色
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("user",user) ;
                return reimbursementService.reimburseSum(map);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * state=2时财务撤回，此时，稿件状态和借款状态还未变更
     * 1、完成待办
     * 2、增加新的待办
     * 3、请款订单状态修改为-1
     * @param
     */
    @RequestMapping("/CWReject")
//    @Log(opType = OperateType.DELETE, module = "请款管理|财务撤回", note = "财务撤回")
    @ResponseBody
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
                Reimbursement entity = reimbursementService.getById(id) ;
                if(entity.getState()==IConst.STATE_PASS ||entity.getState() == IConst.STATE_CN){
                    reimbursementService.CWReject(entity) ;
                    ResponseData data = ResponseData.ok() ;
                    data.putDataValue("message","操作成功");
                    data.putDataValue("entity", entity) ;
                    return data ;
                }else{
                    return ResponseData.customerError(1002,"当前状态不能撤回！") ;
                }
            }else{
                return  ResponseData.customerError(1002,"当前用户无法撤回请款，请联系财务出纳撤回！") ;
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
     * 更改出款操作
     */
    @RequestMapping("/chaneAccount")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|请款更改出款账号", note = "请款更改出款账号")
    public ResponseData chaneAccount(@RequestParam Map map){
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
                Reimbursement entity = reimbursementService.getById(id);
                //审核通过才能出款
                if(entity.getState()==IConst.STATE_FINISH){
                    reimbursementService.changeAccount(entity,map);
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

    @ApiOperation(value = "报销管理", notes = "下载报销信息")
    @Verify(code = "/fee/flowPrint", module = "报销管理/下载报销信息")
    @PostMapping("downloadData")
    @ResponseBody
    public ResponseData downloadData(@RequestParam Map<String, Object> param){
        try{
            ResponseData responseData = ResponseData.ok();
            String fileName = reimbursementService.downloadData(param);
            if (StringUtils.isEmpty(fileName)) {
                responseData.putDataValue("message", "报销信息不存在！");
            } else {
                responseData.putDataValue("file", fileName);
            }
            return responseData;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "下载报销信息异常！");
        }
    }

    @ApiOperation(value = "报销管理", notes = "批量下载报销信息")
    @Verify(code = "/fee/flowPrint", module = "报销管理/批量下载报销信息")
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
            String fileName = reimbursementService.batchDownloadData(param);
            if (StringUtils.isEmpty(fileName)) {
                responseData.putDataValue("message", "借款信息不存在！");
            } else {
                responseData.putDataValue("file", fileName);
            }
            return responseData;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "批量下载报销信息异常！");
        }
    }

    @ApiOperation(value = "报销管理", notes = "批量打印报销信息")
    @Verify(code = "/fee/flowPrint", module = "报销管理/批量打印报销信息")
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
            return ResponseData.ok().putDataValue("list", reimbursementService.listReimburseData(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "批量打印报销信息异常！");
        }
    }
}
