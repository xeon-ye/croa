package com.qinfei.qferp.controller.biz;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.biz.ArticleImport;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.IArticleImportService;
import com.qinfei.qferp.service.biz.IArticleImportForEasyExcelService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/articleImport")
class ArticleImportController {
    @Autowired
    IArticleImportService articleImportService;
    @Autowired
    IArticleImportForEasyExcelService articleImportForEasyExcelService;
    @Autowired
    Config config;
    @Autowired
    IDictService dictService;
    @Autowired
    UserMapper userMapper;

    /**
     * 媒介查询稿件
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgMJ")
    @Log(opType = OperateType.QUERY, note = "稿件查询/媒介查询", module = "稿件查询/媒介查询")
    public PageInfo<Map> listPgMJ(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        PageInfo<Map> list = null ;
        try{
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("user",user) ;
                list = articleImportService.listPgMJ(pageable.getPageNumber(), pageable.getPageSize(),map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 业务查询稿件
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgYW")
    @Log(opType = OperateType.QUERY, note = "稿件查询/业务查询", module = "稿件查询/业务查询")
    public PageInfo<Map> listPgYW(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        PageInfo<Map> list = null ;
        try{
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("user",user) ;
                list = articleImportService.listPgYW(pageable.getPageNumber(), pageable.getPageSize(),map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 通过id查询
     * @param id
     * @return
     */
    @RequestMapping(value="/view")
    @ResponseBody
//    @Verify(code = "/role/view", module = "系统管理/角色查看")
    @Log(opType = OperateType.QUERY, note = "稿件查询/通过id查询", module = "稿件查询/通过id查询")
    public ResponseData view(@RequestParam("id") Integer id) {
        try{
            ResponseData data = ResponseData.ok();
            ArticleImport entity = articleImportService.getById(id) ;
            data.putDataValue("entity",entity) ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value="/del")
    @Log(opType = OperateType.DELETE, note = "稿件查询/通过id删除", module = "稿件查询/通过id删除")
    @ResponseBody
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            Boolean flag = articleImportService.delById(id) ;
            ResponseData data = ResponseData.ok();
            if(flag){
                data.putDataValue("message", "操作成功!");
            }else{
//                data.putDataValue("message", "删除失败，录入人或稿件媒介才能删除该稿件！");
                return ResponseData.customerError(1001, "删除失败，录入人或稿件媒介才能删除该稿件！");
            }

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 删除稿件
     * @param ids
     * @return
     */
    @RequestMapping(value="/batchDel")
    @Log(opType = OperateType.DELETE, note = "稿件删除/录入人删除", module = "稿件删除/录入人删除")
    @ResponseBody
    public ResponseData batchDel(@RequestParam("ids") String ids) {
        try {
            Map<String,Integer> map = articleImportService.batchDel(ids) ;
            Integer succNum = map.get("succNum") ;
            Integer failNum = map.get("failNum") ;
            String message = "" ;
            if(succNum.equals(0)){
                message = "删除失败！只有录入人才能删除,请核实信息后重试！" ;
                return ResponseData.customerError(1001, message);
            }else if(failNum.equals(0)){
                message = "删除成功！删除了"+succNum+"条临时稿件！" ;
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", message);
                return data;
            }else{
                message = "删除了"+succNum+"条临时稿件；"+failNum+"条稿件删除失败。"+"只有录入人才能删除,请核实信息后重试！" ;
                return ResponseData.customerError(1111, message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 添加稿件
     * @param entity
     * @return
     */
    @RequestMapping("/add")
    @Log(opType = OperateType.ADD, note = "添加稿件", module = "添加稿件")
    @ResponseBody
    public ResponseData add(ArticleImport entity) {
        try{
            User user = AppUtil.getUser();
            articleImportService.add(entity);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", entity) ;

            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }

    }

    /**
     * 修改稿件
     * @param entity
     * @return
     */
    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, note = "修改稿件", module = "修改稿件")
    public ResponseData edit(ArticleImport entity) {
        try{
            ArticleImport oldEntity = articleImportService.getById(entity.getId()) ;
            User user = AppUtil.getUser();
            if (user.getId().equals(oldEntity.getCreator())||user.getId().equals(oldEntity.getMediaUserId())) {
                entity.setUpdateUserId(user.getId());
                entity.setUpdateTime(new Date());
                articleImportService.edit(entity);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message","操作成功");
                data.putDataValue("entity", entity) ;
                return data ;
            }else{
                return ResponseData.customerError(1001,"修改失败！只有稿件导入人和稿件中的媒介才能编辑！") ;
            }

        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 批量导入
     * @param multipartFile
     * @return
     */
    @RequestMapping("batchOrder")
    @ResponseBody
    @Log(opType = OperateType.ADD, note = "批量导入", module = "媒体管理/批量导入")
    public ResponseData batchOrder(@RequestParam(value="file") MultipartFile multipartFile) {
        try{
            String fileName = UUIDUtil.get32UUID() + multipartFile.getOriginalFilename();
            String childPath = File.separator+"media"+File.separator+"order"+File.separator;
            if(fileName.indexOf(".xls")>-1){
                File destFile = new File( config.getUploadDir() +childPath + fileName);
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                multipartFile.transferTo(destFile);
                String errorMsg = articleImportService.batchOrder(destFile);
                if(StringUtils.isEmpty(errorMsg)){
                    ResponseData data = ResponseData.ok() ;
                    data.putDataValue("message","导入成功！") ;
                    return data ;
                }else{
                    return ResponseData.customerError(1002,errorMsg) ;
                }
            }else{
                return ResponseData.customerError(1001,"上传的文件类型不正确！") ;
            }
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,"导入失败！") ;
        }
    }

    /**
     * 批量导入
     * @param multipartFile
     * @return
     */
    @RequestMapping("batchOrderForEasyExcel")
    @ResponseBody
    @Log(opType = OperateType.ADD, note = "批量导入", module = "媒介查询/批量导入")
    public ResponseData batchOrderForEasyExcel(@RequestParam(value="file") MultipartFile multipartFile) {
        try{
            String fileName = multipartFile.getOriginalFilename();
            if(fileName.indexOf(".xls") > -1){
                String errorMsg = articleImportForEasyExcelService.batchOrderForEasyExcel(multipartFile);
                if(StringUtils.isEmpty(errorMsg)){
                    ResponseData data = ResponseData.ok() ;
                    data.putDataValue("message","导入成功！") ;
                    return data ;
                }else{
                    return ResponseData.customerError(1002,errorMsg) ;
                }
            }else{
                return ResponseData.customerError(1001,"上传的文件类型不正确！") ;
            }
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseData.customerError(1001,"导入失败！") ;
        }
    }
    /**
     * 业务员完善订单
     */
    @RequestMapping("/complete")
    @Log(opType = OperateType.QUERY, module = "业务员完善订单", note = "业务员完善订单")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "财务管理|进账流水管理", note = "修改进账流水")
//    @Verify(code = "/income/add", module = "系统管理/角色提交")
    public ResponseData complete(Order order,@RequestParam Map map) {
        try{
            if(map.get("ids")==null){
                return ResponseData.customerError(1002,"没有选中的稿件信息！") ;
            }else{
                String ids = (String)map.get("ids") ;
                //不能有回款、开票、提成数据和其他人的稿件
                articleImportService.checkCustInfo(ids) ;
                User user = AppUtil.getUser() ;
                order.setUserId(user.getId());
                order.setUserName(user.getName());
                order.setDepatId(user.getDeptId());
                order.setOrderType(1);
                order.setCreator(AppUtil.getUser().getId());
                order.setCreateDate(new Date());
                order.setNo("MTGJ" + UUIDUtil.get16UUID().toUpperCase());
                order.setState(IConst.STATE_FINISH);
                articleImportService.complete(order,map);
                ResponseData data = ResponseData.ok() ;
                data.putDataValue("message","操作成功");
                return data ;
            }
        }catch(QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 更新进账
     * @param param
     * @return
     */
    @RequestMapping("/updateAmountAndBrand")
    @Log(opType = OperateType.UPDATE, module = "更新进账", note = "更新进账")
    @ResponseBody
    public ResponseData updateAmountAndBrand(@RequestParam Map param){
        try {
            articleImportService.updateAmountAndBrand(param);
            return ResponseData.ok();
        }catch (Exception e){
            log.error("更新稿件失败",e);
            return ResponseData.customerError(1001,e.getMessage());
        }
    }

    /**
     * 根据id查询临时稿件
     */

    @ResponseBody
    @RequestMapping("/queryArticleByIds")
//    @Log(opType = OperateType.QUERY, module = "查询临时稿件", note = "查询临时稿件")
    public PageInfo<Map> queryArticleByIds(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        PageInfo<Map> list = null ;
        try{
            if(map.get("ids")!=null){
                String ids = (String)map.get("ids") ;
                list = articleImportService.queryArticleByIds(pageable.getPageNumber(), pageable.getPageSize(),ids);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 检测稿件是否可以完善
     * @param ids
     * @return
     */
    @RequestMapping("/checkCustInfo")
//    @Log(opType = OperateType.QUERY, module = "检测稿件是否可以完善", note = "检测稿件是否可以完善")
    @ResponseBody
    public ResponseData checkCustInfo(@RequestParam("ids") String ids) {
        try{
            ResponseData data = ResponseData.ok();
            Map result = articleImportService.checkCustInfo(ids) ;
            data.putDataValue("entity", result);
            return data;
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 稿件统计应收等
     * @param ids
     * @return
     */
    @RequestMapping("/querySumArticleByIds")
//    @Log(opType = OperateType.QUERY, module = "稿件统计应收等", note = "稿件统计应收等")
    @ResponseBody
    public Map querySumArticleByIds(@RequestParam("ids") String ids) {
        return articleImportService.querySumArticleByIds(ids);
    }

    /**
     * 业务员导出全部
     */
    @RequestMapping("/exportArticleYW")
    @Log(opType = OperateType.QUERY, module = "稿件导出/业务员导出全部", note = "稿件导出/业务员导出全部")
    public void exportArticleYW(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("user",user) ;
            }
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("临时稿件列表.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            articleImportService.exportArticleYW(map, outputStream);
        } catch (Exception e) {
            log.error("导出临时稿件列表失败", e);
        }
    }

    /**
     * 按条件统计应收
     * @param param
     * @return
     */
    @RequestMapping("/getArticleImportSum")
//    @Log(opType = OperateType.QUERY, module = "按条件统计应收", note = "按条件统计应收")
    @ResponseBody
    public Map getArticleImportSum(@RequestParam Map param) {
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles() ;
        if(roles==null||roles.size()==0){
            return null ;
        }else{
            param.put("roleType",roles.get(0).getType()) ;
            param.put("roleCode",roles.get(0).getCode()) ;
            param.put("user",user) ;
        }
        return articleImportService.getArticleImportSum(param);
    }

    /**
     * 媒介导出全部
     */
    @RequestMapping("/exportArticleMJ")
    @Log(opType = OperateType.QUERY, module = "媒介导出全部", note = "媒介导出全部")
    public void exportArticleMJ(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("user",user) ;
            }
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("临时稿件列表.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            articleImportService.exportArticleMJ(map, outputStream);
        } catch (Exception e) {
            log.error("导出临时稿件列表失败", e);
        }
    }

    /**
     * 稿件导入
     * @param multipartFile
     * @return
     */
    @RequestMapping("batchSaleAmount")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, note = "稿件导入", module = "稿件导入")
    public ResponseData batchSaleAmount(@RequestParam(value="file") MultipartFile multipartFile) {
        try{
            String fileName = UUIDUtil.get32UUID() + multipartFile.getOriginalFilename();
            String childPath = File.separator+"media"+File.separator+"order"+File.separator;
            if(fileName.indexOf(".xls")>-1){
                File destFile = new File( config.getUploadDir() +childPath + fileName);
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                multipartFile.transferTo(destFile);
                ResponseData data = ResponseData.ok() ;
                articleImportService.batchSaleAmount(destFile);
                data.putDataValue("message","导入成功！") ;
                return data ;
            }else{
                return ResponseData.customerError(1001,"上传的文件类型不正确！") ;
            }
        }catch (QinFeiException e){
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage()) ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,"导入失败！") ;
        }
    }


}
