package com.qinfei.qferp.controller.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.fee.Drop;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.IDropService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/drop")
@Api(description = "供应商退款接口")
class DropController {

    @Autowired
    private IDropService dropService;

    @ResponseBody
    @RequestMapping("/listPg")
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
                list = dropService.listPg(pageable.getPageNumber(), pageable.getPageSize(),map);
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
            Drop entity = dropService.getById(id) ;
            if(entity == null){
                return ResponseData.customerError(1002,"该退稿流程已删除！") ;
            }
            if (StringUtils.isNotEmpty(entity.getSupplierPhone())){
                String phone = EncryptUtils.decrypt(entity.getSupplierPhone());
                entity.setSupplierPhone(phone);
            }
            data.putDataValue("entity",entity) ;
            Map map = dropService.querySumAmount(id) ;
            data.putDataValue("outgoSum", map.get("outgoSum")==null?0:map.get("outgoSum"));
            data.putDataValue("saleSum", map.get("saleSum")==null?0:map.get("saleSum"));
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping(value="/del")
    @ResponseBody
//    @Log(opType = OperateType.DELETE, module = "财务管理|请款流水管理", note = "删除请款流水")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser() ;
            Drop entity = dropService.getById(id) ;
            if(user.getId().toString().equals(entity.getApplyId().toString())){
                if (entity.getState()==IConst.STATE_SAVE ||entity.getState()==IConst.STATE_REJECT) {
                    dropService.delById(entity) ;
                    ResponseData data = ResponseData.ok();
                    data.putDataValue("message", "操作成功");
                    return data;
                }else{
                    return ResponseData.customerError(1001, "删除失败，只能删除保存和驳回的请款！");
                }
            }else{
                return ResponseData.customerError(1001, "删除失败，只有申请人才能删除请款！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 新增功能暂不启用，使用saveStepOne()替代
     * @param entity
     * @return
     */
    @RequestMapping("/add")
//    @Log(opType = OperateType.ADD, module = "供应商退款|添加", note = "供应商退款|添加")
    @ResponseBody
    public ResponseData add(Drop entity) {
        try{
            User user = AppUtil.getUser();
            entity.setState(IConst.STATE_BZ);
            entity.setCreator(user.getId());
            entity.setCreateTime(new Date());
            dropService.add(entity);
            // 紧急程度字段暂不启用
            // taskId为空：首次提交审核；不为空：驳回后提交审核
//            processService.addMediaRefundProcess(entity, 0);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", entity) ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|请款管理", note = "修改请款")
    public ResponseData edit(Drop entity) {
        try{
            Drop old = dropService.getById(entity.getId()) ;
            if(old.getState()==IConst.STATE_SAVE ||old.getState()==IConst.STATE_REJECT ){
                entity.setItemId(old.getItemId());
                entity = dropService.edit(entity) ;
                ResponseData data = ResponseData.ok() ;
                data.putDataValue("message","操作成功");
                data.putDataValue("entity", entity) ;
                return data ;
            }else{
                return ResponseData.customerError(1002,"当前状态不能编辑！") ;
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }

    }

    /**
     * add方法
     * @param map
     * @return
     */
    @RequestMapping("/saveStepOne")
//    @Log(opType = OperateType.ADD, module = "供应商退款|添加", note = "供应商退款|添加")
    @ResponseBody
    public ResponseData saveStepOne(@RequestParam Map map) {
        try{
            ResponseData data = ResponseData.ok() ;
            if(ObjectUtils.isEmpty(map.get("supplierIdSec"))
                    || ObjectUtils.isEmpty(map.get("supplierNameSec"))
                    || ObjectUtils.isEmpty(map.get("supplierContactorSec"))
                    || ObjectUtils.isEmpty(map.get("articleIdsSec"))
                    || ObjectUtils.isEmpty(map.get("companyCode"))){
                return ResponseData.customerError(1002,"未选择供应商或稿件！") ;
            }else{
                String ids = (String)map.get("articleIdsSec");
                List<Integer> list = new ArrayList() ;
                if(ids.indexOf(",")>-1){
                    String[] idss = ids.split(",") ;
                    for(int i=0;i<idss.length;i++){
                        list.add(Integer.parseInt(idss[i])) ;
                    }
                }else{
                    list.add(Integer.parseInt(ids)) ;
                }
                //选中的稿件关联的请款数量，只能为1，其他都是异常
                List<Outgo> outgoList= dropService.queryOutgoByArticleIds(list) ;
                if(outgoList!=null && outgoList.size()>0){
                    if(outgoList.size()==1){
                        User user = AppUtil.getUser();
                        Drop entity = dropService.saveStepOne(map,user,outgoList.get(0));
                        data.putDataValue("entity",entity) ;
                        data.putDataValue("message","操作成功");
                        return data ;
                    }else{
                        return ResponseData.customerError(1002,"选中的稿件不属于同一条请款记录，无法操作，请选择同一个请款记录的稿件！") ;
                    }
                }else{
                    return ResponseData.customerError(1002,"未找到选中稿件的请款记录，请刷新后重试！") ;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 请款已经关联的稿件
     * @param pageable
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgForSelectedArticle")
//    @Log(opType = OperateType.QUERY, module = "退款已经关联的稿件", note = "退款已经关联的稿件")
    public PageInfo<Map> listPgForSelectedArticle(@PageableDefault(size = 5) Pageable pageable,@RequestParam("id") Integer id) {
        return dropService.listPgForSelectedArticle(pageable.getPageNumber(), pageable.getPageSize(),id);
    }

    /**
     * 根据稿件编号查询请款id
     * @param articleId
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryDropId")
    public Integer queryDropId(@RequestParam("articleId") Integer articleId){
        return dropService.queryDropId(articleId);
    }

    /**
     * 选择未请款的稿件(已发布，未请款)
     * @param pageable
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/listPgForSelectArticle")
    public PageInfo<Map> listPgForSelectArticle(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        map.put("user",AppUtil.getUser()) ;
        return dropService.listPgForSelectArticle(pageable.getPageNumber(), pageable.getPageSize(),map);
    }

    @RequestMapping("exportDrop")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "导出退款", note = "导出退款")
    public void exportDrop(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser() ;
            List<Role> roles = user.getRoles() ;
            if(roles==null||roles.size()==0){
                throw new Exception("未查询到角色信息") ;
            }else{
                map.put("roleType",roles.get(0).getType()) ;
                map.put("roleCode",roles.get(0).getCode()) ;
                map.put("user",user) ;
                response.setContentType("application/binary;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("请款导出" + DateUtils.getNowTime() + ".xls", "UTF-8"));
                OutputStream outputStream = response.getOutputStream();
                dropService.exportDrop(map, outputStream);
            }
        } catch (Exception e) {
            log.error("导出失败", e);
        }
    }
}
