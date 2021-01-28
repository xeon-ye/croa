package com.qinfei.qferp.controller.sys;

import com.alibaba.fastjson.JSONArray;
import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sys.IDeptService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/dept")
@Api(description = "部门接口")
class DeptController {

    @Autowired
    private IDeptService deptService;

    @RequestMapping("/list")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/查询所有部门")
    public ResponseData list() {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.list();
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/listByCompany")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/查询所有部门")
    public ResponseData listByCompany() {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.listByCompany();
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/childs/{parentId}")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/查询所有子部门")
    public ResponseData childs(@PathVariable("parentId") Integer parentId) {
        try {
            ResponseData data = ResponseData.ok();
            List list = deptService.listByParentId(parentId);
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/listForTreeView")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/查询所有部门")
    public ResponseData listForTreeView() {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.listForTreeView();
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/onlyDeptMent")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/查询所有部门")
    public ResponseData onlyDeptMent() {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.onlyDeptMent();
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/listDeptAllMJ")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/查询所有部门")
    public ResponseData listDeptAllMJ() {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.listDeptAllMJ();
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 获取指定部门下的部门数据；
     *
     * @param deptId：上级部门ID；
     * @return ：下属的部门信息；
     */
    @RequestMapping("/listForSonTreeView")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/获取指定部门下的部门数据")
    public ResponseData listForSonTreeView(@RequestParam Integer deptId) {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.listForTreeView(deptId);
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/listForTreeViewByCompanyCode")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/获取指定公司的部门数据")
    public ResponseData listForTreeViewByCompanyCode() {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.listForTreeViewByCompanyCode();
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|部门管理", note = "修改部门")
    @Verify(code = "/dept/edit", module = "系统管理/修改部门")
    public ResponseData edit(Dept dept) {
        try {
            ResponseData data = ResponseData.ok();
            deptService.edit(dept);
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "系统管理|部门管理", note = "删除部门")
    @Verify(code = "/dept/del", module = "系统管理/删除部门")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            deptService.delById(id);
            return ResponseData.ok();
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, "删除部门异常，请联系管理员！");
        }
    }

    @RequestMapping("/addChild")
    @ResponseBody
    @Log(opType = OperateType.ADD, module = "系统管理|部门管理", note = "添加子部门")
    @Verify(code = "/dept/addChild", module = "系统管理/添加子部门")
    public ResponseData addChildDept(@RequestParam Map map, HttpSession session) {
        try {
            User user = (User) session.getAttribute(IConst.USER_KEY);
            if (user == null) {
                return ResponseData.customerError(1001, "session失效！");
            } else {
                ResponseData data = ResponseData.ok();
                map.put("userId", user.getId());
                deptService.addChildDept(map);
                data.putDataValue("message", "操作成功");
                return data;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/checkName")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/核实部门姓名")
    public ResponseData checkName(@RequestParam("id") Integer id, @RequestParam("name") String name, @RequestParam("childName") String childName) {
        try {
            ResponseData data = ResponseData.ok();
            Boolean flag = false;

            if (StringUtils.isNotEmpty(childName)) {
                // childName非空，验证的添加子子部门
                List<Dept> list2 = deptService.queryDeptByName(childName);
                if (list2.size() == 0) {
                    flag = true;
                }
            } else {
                // 验证修改节点
                Dept dept = deptService.getById(id);
                if (name.equals(dept.getName())) {
                    flag = true;
                } else {
                    List<Dept> list = deptService.queryDeptByName(name);
                    if (list.size() == 0) {
                        flag = true;
                    }
                }
            }
            data.putDataValue("flag", flag);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 根据类型查找部门
     *
     * @param type
     * @return
     */
    @RequestMapping(value = "/listByType")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/根据类型查找部门")
    public List<Dept> listByType(@RequestParam(required = false, value = "type") String type,
                                 @RequestParam(required = false, value = "companyCode") String companyCode) {
        return deptService.listByTypeAndCompanyCode(type, companyCode);
    }

    /**
     * 查找所有的业务部门
     *
     * @return
     */
    @RequestMapping(value = "/queryYWDept")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/查找所有的业务部门")
    public List<Dept> queryYWDept() {
        return deptService.queryDeptByCompanyCodeAndCode(AppUtil.getUser().getDept().getCompanyCode(), "YW");
    }

    /**
     * 根据名称查找所有的业务部门
     *
     * @return
     */
    @RequestMapping(value = "/queryYWDeptByName")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/查找所有的业务部门")
    public List<Dept> queryYWDeptByName(String deptName) {
        return deptService.queryDeptByCompanyCodeAndCodeAndDeptName(AppUtil.getUser().getDept().getCompanyCode(), "YW", deptName);
    }

    /**
     * 根据部门ID获取完整的部门树数据，用于业务量统计；
     *
     * @return ：包含部门树数据用户对象；
     */
    @RequestMapping(value = "/getFullDeptTreeByDeptId")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/获取完整的部门树数据，用于业务量统计")
    public User getFullDeptTree(@RequestParam Map map) {
        return deptService.getFullDeptTree(map);
    }

    /**
     * 根据部门第一层级查询所有分公司名称
     *
     * @param level
     * @return
     */
    @RequestMapping(value = "/findAllCompany")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/根据部门第一层级查询所有分公司名称")
    public List<Dept> findAllCompany(@RequestParam("level") Integer level) {
        return deptService.listByLevelId(level);
    }
    /**
     * 增加用户时可以增加集团总经办角色
     *
     */
    @RequestMapping(value = "/allCompany")
    @ResponseBody
    public List<Dept> allCompany(@RequestParam("level") Integer level) {
        return deptService.allCompany(level);
    }

    /**
     * 根据登录人的公司code获取公司的所有部门
     *
     * @return
     */
    @RequestMapping(value = "/getDeptByCompany")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/根据登录人的公司code获取公司的所有部门")
    public List<Dept> getDeptByCompany() {
        return deptService.getDeptByCompany();
    }

    /**
     * 根据部门ID查询下方所有媒介部和业务部；
     *
     * @param deptId：上级部门ID；
     * @return ：下属的部门信息；
     */
    @PostMapping("/listAllMJYWByDeptId")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/根据部门ID查询下方所有媒介部和业务部")
    public ResponseData listAllMJYWByDeptId(@RequestParam Integer deptId) {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.listAllMJYWByDeptId(deptId);
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 根据部门ID查询下方所有媒介部和业务部；
     *
     * @param deptId：上级部门ID；
     * @return ：下属的部门信息；
     */
    @PostMapping("/listAllDeptByIdAndCode")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/根据部门ID查询下方所有媒介部和业务部")
    public ResponseData listAllDeptByIdAndCode(@RequestParam Integer deptId, @RequestParam String deptCode) {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.listAllDeptByIdAndCode(deptId, deptCode);
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @PostMapping("/listAllDeptByIdAndCodeAndLevel")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/根据部门ID查询下方所有媒介部和业务部")
    public ResponseData listAllDeptByIdAndCodeAndLevel(@RequestParam Integer deptId, @RequestParam String deptCode, @RequestParam String level) {
        try {
            ResponseData data = ResponseData.ok();
            JSONArray list = deptService.listAllDeptByIdAndCodeAndLevel(deptId, deptCode, level);
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 根据公司编码获取公司信息；
     *
     * @param companyCode：公司编码；
     */
    @PostMapping("/getCompanyByCode")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/根据公司编码获取公司信息")
    public ResponseData getCompanyByCode(@RequestParam String companyCode) {
        try {
            ResponseData data = ResponseData.ok();
            data.putDataValue("company", deptService.getCompanyByCode(companyCode));
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 获取集团信息；
     */
    @PostMapping("/getRootDept")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/获取集团信息")
    public ResponseData getRootDept() {
        try {
            ResponseData data = ResponseData.ok();
            data.putDataValue("root", deptService.getRootDept());
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 根据部门ID查询下方所有媒介部和业务部；
     */
    @GetMapping("/listAllCompany")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "部门管理", module = "部门管理/获取集团下所有公司")
    public ResponseData listAllCompany() {
        try {
            ResponseData data = ResponseData.ok();
            data.putDataValue("result", deptService.listAllCompany());
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002, e.getMessage());
        }
    }

    /**
     * 根据部门ID查询下方所有媒介部和业务部；
     */
    @GetMapping("/listJTAllCompany")
    @ResponseBody
    public ResponseData listJTAllCompany(@RequestParam(name = "companyCode", required = false) String companyCode) {
        try {
            ResponseData data = ResponseData.ok();
            data.putDataValue("result", deptService.listJTAllCompany(companyCode));
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002, e.getMessage());
        }
    }

    /**
     * 添加时判断公司代码是否重复listUserByDeptAndRole2
     * @param companyCode
     * @return
     */
    @RequestMapping(value = "/checkDeptCompanyCode")
    @ResponseBody
    public ResponseData checkDeptCompanyCode(@RequestParam("companyCode") String companyCode){
        ResponseData data = ResponseData.ok();
        data.putDataValue("list", deptService.checkDeptCompanyCode(companyCode));
        return data;
    }

    @RequestMapping(value = "/listByCode")
    @ResponseBody
    public List<Dept> listByCode(@RequestParam("code") String code){
        return deptService.listByCode(code);
    }
}