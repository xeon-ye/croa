package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sys.IRoleService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/role")
@Api(description = "角色接口")
class RoleController {

    @Autowired
    private IRoleService roleService;
    @Autowired
    private IUserService userService;

    @RequestMapping("/list")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有角色", note = "查询所有角色")
    public List<Role> listAll() {
        return roleService.listAll();
    }

    @ResponseBody
    @RequestMapping("/listPg")
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有角色", note = "查询所有角色分页")
    public PageInfo<Role> listPg(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        return roleService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
    }

    @RequestMapping(value = "/view")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|根据id查询角色", note = "根据id查询角色")
    public ResponseData view(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            Role entity = roleService.getById(id);
            data.putDataValue("entity", entity);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping("/queryUserByRoleId")
//    @Log(opType = OperateType.QUERY, module = "系统管理|根据角色id查询用户", note = "根据角色id查询用户")
    public PageInfo<User> queryUserByRoleId(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        PageInfo<User> list = null;
        log.info((String) map.get("roleId"));
        try {
            if (ObjectUtils.isEmpty(map.get("roleId"))) {
                throw new Exception("未获取到角色id！");
            } else {
                list = userService.queryByRoleId(pageable.getPageNumber(), pageable.getPageSize(), map);
            }
        } catch (Exception e) {

        }
        return list;
    }

    @RequestMapping(value = "/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "系统管理|角色管理", note = "删除角色")
    @Verify(code = "/role/del", module = "系统管理/删除角色")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            roleService.delById(id);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/update")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|角色管理", note = "修改角色")
    @Verify(code = "/role/update", module = "系统管理/修改角色")
    public ResponseData update(Role role) {
        try {
            User user = AppUtil.getUser();
            role.setUpdateTime(new Date());
            role.setUpdateUserId(user.getId());
            roleService.update(role);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/add")
    @ResponseBody
    @Log(opType = OperateType.ADD, module = "系统管理|角色管理", note = "新增角色")
    @Verify(code = "/role/add", module = "系统管理/新增角色")
    public ResponseData add(Role role, HttpSession session) {
        try {
            User user = (User) session.getAttribute(IConst.USER_KEY);
            role.setCreator(user.getId());
            role.setId(null);
            role.setState(IConst.STATE_FINISH);
            role.setCreateTime(new Date());
            roleService.save(role);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

//    @RequestMapping("/submitRoleResource")
//    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "角色管理", note = "赋权")
//    @Verify(code = "/role/submitRoleResource", module = "系统管理/赋权")
//    public ResponseData submitRoleResource(@RequestParam("roleId") Integer roleId, @RequestParam("checkId") String checkId) {
//        ResponseData data = ResponseData.ok();
//        try {
//            roleService.submitRoleResource(roleId,checkId) ;
//            data.putDataValue("message", "操作成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseData.customerError(1001, e.getMessage());
//        }
//        return data;
//    }
    @RequestMapping("/submitRoleGroup")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "角色管理", note = "赋权")
//    @Verify(code = "/role/submitRoleGroup", module = "系统管理/赋权")
    public ResponseData submitRoleResource(@RequestParam("roleId") Integer roleId, @RequestParam("checkId") String checkId) {
        ResponseData data = ResponseData.ok();
        try {
            roleService.submitRoleResource(roleId,checkId) ;
            data.putDataValue("message", "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping(value = "/checkName")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|判断角色名是否重复", note = "判断角色名是否重复")
    public ResponseData checkName(@RequestParam("id") Integer id, @RequestParam("name") String name) {

        try {
            ResponseData data = ResponseData.ok();
            Boolean flag = false;
            if (id != null) {
                //编辑页面判断用户名是否重复，先排除自己
                Role role = roleService.getById(id);
                if (name.equals(role.getName())) {
                    flag = true;
                } else {
                    List<Role> list = roleService.queryRoleByName(name);
                    if (list.size() == 0) {
                        flag = true;
                    }
                }
            } else {
                //新增页面判断用户名是否重复
                List<Role> list = roleService.queryRoleByName(name);
                if (list.size() == 0) {
                    flag = true;
                }
            }
            data.putDataValue("flag", flag);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    @RequestMapping("/getUserByRoleId")
    @ResponseBody
    public ResponseData getUserByRoleId(@RequestParam("roleId") Integer roleId) {
        ResponseData data = ResponseData.ok();
        try {
            List<Integer> list = roleService.getUserByRoleId(roleId);
            if(list.size()>0){
                data.putDataValue("number", list.size());
            }else{
                data.putDataValue("number", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/editGroupRole")
    @ResponseBody
    public ResponseData editGroupRole(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        try {
            List<Role> selRole = roleService.queryRoleByGroupId(id);
            List<Role> allRole = roleService.listAll();
            data.putDataValue("selRole", selRole);
            data.putDataValue("allRole", allRole);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    /**
     * 内网查询角色接口
     * @param pageNum
     * @param pageSize
     * @return
     */
    @ResponseBody
    @RequestMapping("/nwlistRolePg/{pageNum}/{pageSize}")
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有角色", note = "查询所有角色分页")
    public PageInfo<Role> nwlistPg(@PathVariable Integer pageNum,
                                   @PathVariable Integer pageSize,
                                   @RequestParam(required = false)String keyword) {
        return roleService.nwRoleList(pageNum,pageSize,keyword);
    }
}
