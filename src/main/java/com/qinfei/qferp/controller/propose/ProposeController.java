package com.qinfei.qferp.controller.propose;
import com.qinfei.core.ResponseData;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.propose.Propose;
import com.qinfei.qferp.entity.propose.ProposeRemark;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.propose.ProposeRelationMapper;
import com.qinfei.qferp.mapper.propose.ProposeRemarkMapper;
import com.qinfei.qferp.service.propose.IProposeRemarkService;
import com.qinfei.qferp.service.propose.IProposeService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 建议模块控制层
 * @Autor :tsf;
 */
@Slf4j
@Controller
@RequestMapping("/propose")
class ProposeController {
    @Autowired
    private IProposeService proposeService;
    @Autowired
    private IDictService dictService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ProposeRelationMapper proposeRelationMapper;
    @Autowired
    private IProposeRemarkService proposeRemarkService;

    /**
     * 查询所有的建议信息
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, Pageable pageable){
        return proposeService.queryPropose(map,pageable);
    }

    /**
     * 查询自己的建议信息
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/listPgByself")
//    @Log(opType = OperateType.QUERY, module = "建议管理", note = "查询自己的建议信息查询")
    @ResponseBody
    public PageInfo<Propose> listPgByself(@RequestParam Map map, Pageable pageable){
        return proposeService.queryProposeByself(map,pageable);
    }

    /**
     * 查询所有的建议类型
     * @param typeCode
     * @return
     */
    @RequestMapping("/queryProposeType")
//    @Log(opType = OperateType.QUERY, module = "建议管理", note = "查询所有的建议类型")
    @ResponseBody
    public ResponseData queryProposeType(@RequestParam("typeCode") String typeCode){
        ResponseData data = ResponseData.ok();
        String companyCode = AppUtil.getUser().getCompanyCode();
        List<Dict> list = dictService.listByTypeCodeAndCompanyCode(typeCode,companyCode);
        data.putDataValue("list",list);
        data.putDataValue("number",list.size());
        return data;
    }

    /**
     * 增加建议信息
     * @param propose
     * @return
     */
    @RequestMapping("/addPropose")
//    @Log(opType = OperateType.ADD, module = "建议管理", note = "增加建议信息")
    @ResponseBody
    public ResponseData savePropose(Propose propose){
        try {
            ResponseData data = ResponseData.ok();
            Propose advice = proposeService.save(propose);
            data.putDataValue("message","保存成功");
            data.putDataValue("advice",advice);
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            throw new QinFeiException(1002,"抱歉，保存建议出错啦，请联系技术人员");
        }
    }

    /**
     * 处理建议信息
     * @param propose
     * @return
     */
    @RequestMapping("/handleAdvice")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "建议管理|处理建议信息", note = "处理建议信息")
    public ResponseData handleAdvice(Propose propose){
        ResponseData data = ResponseData.ok();
        try {
            Propose entity = proposeService.queryProposeById(propose.getId());
            if(entity.getState()==0 || entity.getState()==2 || entity.getState()==4){
                data.putDataValue("message","保存成功");
                proposeService.update(propose);
            }else{
                throw new QinFeiException(1001,"该状态不支持修改");
            }
        } catch (QinFeiException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 确认建议信息
     * @param propose
     * @return
     */
    @RequestMapping("/confirmAdvice")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "建议管理", note = "确认建议信息")
    public ResponseData confirmAdvice(Propose propose){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","操作成功");
        proposeService.confirmReject(propose);
        return data;
    }

    /**
     * 删除时判断是否存在建议
     * @param id
     * @return
     */
    @RequestMapping("/queryAdviceById")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "建议管理", note = "确认建议信息")
    public ResponseData queryAdviceById(Integer id){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","操作成功");
        List<Propose> list = proposeService.queryAdviceById(id);
        data.putDataValue("number",list.size());
        return data;
    }

    /**
     * 建议查询修改建议信息
     * @param propose
     * @return
     */
    @RequestMapping("/modifyPropose")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "建议管理", note = "建议查询修改建议信息")
    public ResponseData modifyPropose(Propose propose){
        ResponseData data = ResponseData.ok();
        try {
            Propose entity = proposeService.queryProposeById(propose.getId());
            data.putDataValue("entity",entity);
            if(entity.getState()==0){
                data.putDataValue("message","操作成功");
                proposeService.modifyPropose(propose);
            }else{
                throw new QinFeiException(1001,"该状态不支持修改");
            }
        } catch (QinFeiException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 根据建议id查询
     * @param proposeId
     * @return
     */
    @RequestMapping("/getById")
//    @Log(opType = OperateType.QUERY, module = "建议管理", note = "根据建议id查询")
    @ResponseBody
    public ResponseData getById(@RequestParam("proposeId") Integer proposeId){
        ResponseData data = ResponseData.ok();
        Propose propose = proposeService.queryProposeById(proposeId);
        List<ProposeRemark> list = proposeRemarkService.queryProposeRemark(proposeId,"desc");
        data.putDataValue("list",list);
        data.putDataValue("number",list.size());
        data.putDataValue("propose",propose);
        return data;
    }

    /**
     * 删除建议信息(建议查询)
     * @param proposeId
     * @return
     */
    @RequestMapping("/deletePropose")
    @ResponseBody
//    @Log(opType = OperateType.DELETE, module = "建议查询|删除建议信息", note = "删除建议信息")
    public ResponseData deletePropose(@RequestParam("id") Integer proposeId){
        ResponseData data = ResponseData.ok();
        try {
            Propose entity = proposeService.queryProposeById(proposeId);
            if(entity.getState()==0){
                data.putDataValue("message","删除成功");
                proposeService.deletePropose(proposeId);
            }else{
                throw new QinFeiException(1001,"该状态不支持修改");
            }
        } catch (QinFeiException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 删除建议信息(建议管理)
     * @param proposeId
     * @return
     */
    @RequestMapping("/cutPropose")
    @ResponseBody
//    @Log(opType = OperateType.DELETE, module = "建议查询|删除建议信息", note = "删除建议信息")
    public ResponseData delPropose(@RequestParam("id") Integer proposeId){
        ResponseData data = ResponseData.ok();
        data.putDataValue("message","删除成功");
        proposeService.deletePropose(proposeId);
        return data;
    }

    /**
     * 论坛根据公司查询公司下的所有用户
     * @param companyCode
     * @return
     */
    @RequestMapping("/listByForum")
    @ResponseBody
//    @Log(opType = OperateType.DELETE, module = "建议查询", note = "根据公司查询所有用户")
    public ResponseData listByForum(@RequestParam("companyCode") String companyCode){
        ResponseData data = ResponseData.ok();
        List<User> list = userService.queryByCompanyCode(companyCode);
        data.putDataValue("number",list.size());
        data.putDataValue("list",list);
        return data;
    }

    /**
     * 建议类型负责人select2分页
     * @param name
     * @param pageable
     * @return
     */
    @RequestMapping("/listByAdvice")
    @ResponseBody
//    @Log(opType = OperateType.DELETE, module = "建议查询", note = "根据公司查询所有用户")
    public PageInfo<User> listByAdvice(@RequestParam(value = "name",required = false)String name,Pageable pageable){
        Map map = new HashMap();
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        map.put("name",name);
        List<User> list = userService.queryUserByCondition(map);
        PageInfo<User> page = new PageInfo<>(list);
        return page;
    }

    /**
     * 查找建议类型所对应的负责人
     * @param id
     * @return
     */
    @RequestMapping("/showUsers")
    @ResponseBody
    public ResponseData showUsers(Integer id){
        List<User> list = proposeRelationMapper.queryProposeUsers(id);
        ResponseData data = ResponseData.ok();
        data.putDataValue("list",list);
        return data;
    }

    /**
     * 根据公司代码查询建议类型所有的负责人
     * @return
     */
    @RequestMapping("/showUsersByCompanyCode")
    @ResponseBody
    public ResponseData showUsersByCompanyCode(){
        List<Map> list = proposeService.queryChargeUsers();
        List<Integer> ids = proposeRelationMapper.queryAdviceId(AppUtil.getUser().getId());
        ResponseData data = ResponseData.ok();
        data.putDataValue("list",list);
        data.putDataValue("ids",ids);
        return data;
    }

    /**
     * 根据公司查询所有用户
     * @return
     */
    @RequestMapping("/queryByCompanyCode")
    @ResponseBody
    public ResponseData queryByCompanyCode(){
        ResponseData data = ResponseData.ok();
        String companyCode = AppUtil.getUser().getCompanyCode();
        List<User> list = userService.queryByCompanyCode(companyCode);
        data.putDataValue("number",list.size());
        data.putDataValue("list",list);
        return data;
    }

    /**
     * 导出建议
     * @param response
     * @param map
     */
    @RequestMapping("/exportPropose")
    @ResponseBody
//    @Log(opType = OperateType.DELETE, module = "建议查询", note = "导出建议")
    public void exportPropose(HttpServletResponse response, @RequestParam Map map){
        try{
            User user = AppUtil.getUser();
            List<Role> roleList = user.getRoles();
            if(roleList==null || roleList.size()==0){
                throw new Exception("未查到角色信息");
            }else{
                map.put("roleType",roleList.get(0).getType());
                map.put("roleCode",roleList.get(0).getCode());
                map.put("companyCode",user.getCompanyCode());
                map.put("userId",user.getId());
                response.setContentType("application/binary;charset=utf-8");
                response.setHeader("Content-Disposition","attachment;fileName="+ URLEncoder.encode("已录建议导出"+ DateUtils.getNowTime()+".xls","utf-8"));
                OutputStream out = response.getOutputStream();
                proposeService.exportPropose(map,out);
            }
        }catch (Exception e){
            log.error("建议管理已录建议导出失败",e);
        }finally {

        }
    }

    /**
     * 导出未录入建议人员
     * @param response
     * @param map
     */
    @RequestMapping("/exportUserNoPropose")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "建议管理|导出未录入建议人员信息", note = "导出未录入建议人员信息")
    public void exportUserNoPropose(HttpServletResponse response, @RequestParam Map map){
        try{
            User user = AppUtil.getUser();
            List<Role> roleList = user.getRoles();
            if(roleList==null || roleList.size()==0){
                throw new Exception("未查到角色信息");
            }else{
                map.put("roleType",roleList.get(0).getType());
                map.put("roleCode",roleList.get(0).getCode());
                map.put("companyCode",user.getCompanyCode());
                response.setContentType("application/binary;charset=utf-8");
                response.setHeader("Content-Disposition","attachment;fileName="+ URLEncoder.encode("未录入建议人员导出"+ DateUtils.getNowTime()+".xls","utf-8"));
                OutputStream out = response.getOutputStream();
                proposeService.exportUserNoPropose(map,out);
            }
        }catch (Exception e){
            log.error("未录入建议人员导出失败",e);
        }finally {

        }
    }
}
