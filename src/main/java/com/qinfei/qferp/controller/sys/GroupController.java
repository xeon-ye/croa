package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.sys.Group;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sys.IGroupService;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Slf4j
/**
 * 资源接口
 */
@Controller
@RequestMapping("/group")
@Api(description = "权限接口")
class GroupController {
    @Autowired
    private IGroupService groupService;

    @RequestMapping("/listPg")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "资源管理", note = "资源列表")
//    @Verify(code = "/group/listPg", module = "系统管理/资源列表")
    public PageInfo<Group> list(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        return groupService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
    }

    @RequestMapping("/list")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有角色", note = "查询所有角色")
    public List<Group> listAll() {
        return groupService.listAll();
    }

    @RequestMapping("/listAllChild")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有角色", note = "查询所有角色")
    public List<Group> listAllChild() {
        return groupService.listAllChild();
    }


    @PostMapping("/search")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "资源管理", note = "资源列表")
    public PageInfo<Group> search(Pageable pageable, Group group) {
        return groupService.search(pageable, group);
    }

    @RequestMapping(value = "/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "系统管理|资源管理", note = "删除资源")
    @Verify(code = "/group/del", module = "系统管理/删除资源")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            groupService.delById(id);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            return data;
        } catch (QinFeiException e) {
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/add")
    @ResponseBody
    @Log(opType = OperateType.ADD, module = "系统管理|资源管理", note = "新增资源")
    @Verify(code = "/group/add", module = "系统管理/新增资源")
    public ResponseData add(Group sr, HttpSession session) {
        try {
            sr.setId(null);
            User user = (User)session.getAttribute(IConst.USER_KEY) ;
            Group group = groupService.add(sr, user.getId());
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("group", group);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/update")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|资源管理", note = "修改资源")
    @Verify(code = "/group/update", module = "系统管理/修改资源")
    public ResponseData update(Group sr, HttpSession session) {
        try {
            User user = (User) session.getAttribute(IConst.USER_KEY);
            Group group = groupService.edit(sr, user.getId());
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("group", group);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/view")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|资源管理", note = "查看资源")
    public ResponseData view(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            Group group = groupService.getById(id);
            data.putDataValue("entity", group);
            List<Group> menus = groupService.queryGroupByParentId(0);
            data.putDataValue("menus", menus);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 根据id和name排重
     * @param id
     * @param name
     * @return
     */
    @RequestMapping(value = "/checkName")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|资源管理", note = "资源排重")
    public ResponseData checkName(@RequestParam(name = "id", required = false) Integer id, @RequestParam("name") String name) {
        try {
            ResponseData data = ResponseData.ok();
            Boolean flag = false;
            if (id!=null) {
                //编辑页面判断是否重复，先排除自己
                Group group = groupService.getById(id);
                if (name.equals(group.getName())) {
                    flag = true;
                } else {
                    List<Group> list = groupService.queryGroupByName(name);
                    if (list.size() == 0) {
                        flag = true;
                    }
                }
            } else {
                //新增页面判断是否重复
                List<Group> list = groupService.queryGroupByName(name);
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

    /**
     * 根据roleId查询资源
     * @param roleId
     * @return
     */
    @GetMapping("/queryGroupByRoleId/{roleId}")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|根据roleId查询资源", note = "根据roleId查询资源")
    public List<Map> queryGroupByRoleId(@PathVariable("roleId")Integer roleId){
        return groupService.queryGroupByRoleId(roleId);
    }

    /**
     * 根据roleId查询资源
     * @param resourceId
     * @return
     */
    @GetMapping("/queryGroupByResourceId/{resourceId}")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|根据roleId查询资源", note = "根据roleId查询资源")
    public List<Map> queryGroupByResourceId(@PathVariable("resourceId")Integer resourceId){
        return groupService.queryGroupByResourceId(resourceId);
    }

    /**
     * @return 所有的父级资源
     */
    @PostMapping("/listParentGroups")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|根据roleId查询资源", note = "所有的父级资源")
    public ResponseData listParentGroups() {
        try {
            ResponseData data = ResponseData.ok();
            List<Group> groups = groupService.queryGroupByParentId(0);
            data.putDataValue("groups", groups);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/submitGroupResource")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "角色管理", note = "资源分组")
//    @Verify(code = "/group/submitGroupResource", module = "系统管理/资源分组")
    public ResponseData submitRoleResource(@RequestParam("groupId") Integer groupId, @RequestParam("checkId") String checkId) {
        ResponseData data = ResponseData.ok();
        try {
            groupService.submitGroupResource(groupId,checkId) ;
            data.putDataValue("message", "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }

    @RequestMapping("/submitGroupRole")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|用户管理", note = "修改用户的角色")
//    @Verify(code = "/user/submitGroupRole", module = "系统管理/修改用户的角色")
    public ResponseData submitGroupRole(@RequestParam("groupId") Integer groupId, @RequestParam("checkId") String checkId) {
        try {
            groupService.submitGroupRole(groupId, checkId);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("groupId", groupId);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }
}
