package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sys.IResourceService;
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
@RequestMapping("/resource")
@Api(description = "权限接口")
class ResourceController {
    @Autowired
    private IResourceService resourceService;

    @RequestMapping("/listChild")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有角色", note = "查询所有角色")
    public List<Resource> listChild() {
        return resourceService.listChild();
    }

    @RequestMapping("/list")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有角色", note = "查询所有角色")
    public List<Resource> listAll() {
        return resourceService.listAll();
    }

    @RequestMapping("/listPg")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "资源管理", note = "资源列表")
//    @Verify(code = "/resource/listPg", module = "系统管理/资源列表")
    public PageInfo<Resource> list(@PageableDefault(size = 5) Pageable pageable, @RequestParam Map map) {
        return resourceService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
    }

    @PostMapping("/search")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "资源管理", note = "资源列表")
    public PageInfo<Resource> search(Pageable pageable, Resource resource) {
        return resourceService.search(pageable, resource);
    }

    @RequestMapping(value = "/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "系统管理|资源管理", note = "删除资源")
    @Verify(code = "/resource/del", module = "系统管理/删除资源")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            resourceService.delById(id);
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
    @Log(opType = OperateType.ADD, module = "系统管理|资源管理", note = "新增资源")
    @Verify(code = "/resource/add", module = "系统管理/新增资源")
    public ResponseData add(Resource sr, HttpSession session) {
        try {
            sr.setId(null);
            User user = (User)session.getAttribute(IConst.USER_KEY) ;
            Resource resource = resourceService.add(sr, user.getId());
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("resource", resource);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/update")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|资源管理", note = "修改资源")
    @Verify(code = "/resource/update", module = "系统管理/修改资源")
    public ResponseData update(Resource sr, HttpSession session) {
        try {
            User user = (User) session.getAttribute(IConst.USER_KEY);
            Resource resource = resourceService.edit(sr, user.getId());
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("resource", resource);
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
            Resource resource = resourceService.getById(id);
            data.putDataValue("resource", resource);
//            List<Resource> menus = resourceService.queryResourceByParentId(0);
//            data.putDataValue("menus", menus);
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
                Resource resource = resourceService.getById(id);
                if (name.equals(resource.getName())) {
                    flag = true;
                } else {
                    List<Resource> list = resourceService.queryResourceByName(name);
                    if (list.size() == 0) {
                        flag = true;
                    }
                }
            } else {
                //新增页面判断是否重复
                List<Resource> list = resourceService.queryResourceByName(name);
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
    @GetMapping("/queryResourceByRoleId/{roleId}")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|根据roleId查询资源", note = "根据roleId查询资源")
    public List<Map> queryResourceByRoleId(@PathVariable("roleId")Integer roleId){
        return resourceService.queryResourceByRoleId(roleId);
    }

    /**
     * groupId
     * @param groupId
     * @return
     */
    @GetMapping("/queryResourceByGroupId/{groupId}")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|根据roleId查询资源", note = "根据roleId查询资源")
    public List<Map<String, Object>> queryResourceByGroupId(@PathVariable("groupId")Integer groupId){
        return resourceService.queryResourceByGroupId(groupId);
    }

    /**
     * @return 所有的父级资源
     */
    @PostMapping("/listParentResources")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|根据roleId查询资源", note = "所有的父级资源")
    public ResponseData listParentResources() {
        try {
            ResponseData data = ResponseData.ok();
            List<Resource> resources = resourceService.queryResourceByParentId(0);
            data.putDataValue("resources", resources);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/submitResourceGroup")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "角色管理", note = "赋权")
//    @Verify(code = "/role/submitRoleGroup", module = "系统管理/赋权")
    public ResponseData submitResourceGroup(@RequestParam("resourceId") Integer resourceId, @RequestParam("checkId") String checkId) {
        ResponseData data = ResponseData.ok();
        try {
            resourceService.submitResourceGroup(resourceId,checkId) ;
            data.putDataValue("message", "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
        return data;
    }


    @PostMapping("/listAllMenu")
    @ResponseBody
    public ResponseData listAllMenu() {
        try {
            ResponseData data = ResponseData.ok();
            data.putDataValue("resources", resourceService.listAllMenu());
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }
}
