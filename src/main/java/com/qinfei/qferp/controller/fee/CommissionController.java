package com.qinfei.qferp.controller.fee;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.util.IOUtils;
import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.fee.Commission;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.excelListener.ArticleExcelCommissionRegister;
import com.qinfei.qferp.excelListener.ArticleExcelCommissionUnRegister;
import com.qinfei.qferp.service.fee.ICommissionService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/commission")
@Api(description = "提成管理接口")
class CommissionController {

    @Autowired
    private ICommissionService commissionService;
    // 获取配置；
    @Autowired
    private Config config;

    @ResponseBody
    @RequestMapping("/listPg")
    public PageInfo<Map> listPg(@PageableDefault() Pageable pageable, @RequestParam Map map) {
        PageInfo<Map> list = null;
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
//                map.put("roleType", roles.get(0).getType());
//                map.put("roleCode", roles.get(0).getCode());
//                map.put("user", user);
//                list = commissionService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
                list = commissionService.listFeeCommissionByPage(pageable.getPageNumber(), pageable.getPageSize(), map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @RequestMapping(value = "/view")
    @ResponseBody
    public ResponseData view(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            Commission entity = commissionService.getById(id);
            data.putDataValue("entity", entity);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "提成管理|通过id删除", note = "提成管理|通过id删除")
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            commissionService.del(id);
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
    @Log(opType = OperateType.ADD, module = "提成管理|添加", note = "提成管理|添加")
    public ResponseData add(Commission entity) {
        try {
            commissionService.add(entity);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", entity);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    @RequestMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.ADD, module = "提成管理|修改", note = "提成管理|修改")
    public ResponseData edit(Commission entity) {
        try {
            commissionService.edit(entity);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", entity);

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/initCommissionInfo")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "提成管理|按userId修改提成信息", note = "提成管理|按userId修改提成信息")
    public ResponseData initCommissionInfo(Integer userId) {
        try {
            List<Commission> list = commissionService.checkCommissionInfo(userId);
            if (list != null || list.size() > 0) {
                Commission entity = commissionService.initCommissionInfo(userId);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1001, "该业务员上月已提成，不能再更改提成信息！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/confirmOprate")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|提成管理", note = "财务批量发送提成给业务员确认")
    public ResponseData confirmOprate(@RequestParam(value = "ids[]") Integer[] ids) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            Set<String> set = new HashSet<>();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                for (Role role : roles) {
                    set.add(role.getType());
                }
            }
            if (set.contains(IConst.ROLE_TYPE_CW) || set.contains(IConst.ROLE_TYPE_RS) || set.contains(IConst.ROLE_TYPE_XZ)) {
                for (int i = 0; i < ids.length; i++) {
                    Commission entity = commissionService.getById(ids[i]);
                    entity.setState(IConst.STATE_YW);
                    entity.setReleaseId(user.getId());
                    entity.setReleaseTime(new Date());
                    commissionService.confirm(entity, user);
                }
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                return data;
            } else {
                return ResponseData.customerError(1001, "只有财务人事行政才能发起提成确认流程！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/confirm")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|提成管理", note = "财务发送提成给业务员确认")
    @Verify(code = "/commission/confirm", module = "财务管理/财务发送提成给业务员确认")
    public ResponseData confirm(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            Set<String> set = new HashSet<>();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                for (Role role : roles) {
                    set.add(role.getType());
                }
            }
            if (set.contains(IConst.ROLE_TYPE_CW) || set.contains(IConst.ROLE_TYPE_RS) || set.contains(IConst.ROLE_TYPE_XZ)) {
                Commission entity = commissionService.getById(id);
                entity.setState(IConst.STATE_YW);
                entity.setReleaseId(user.getId());
                entity.setReleaseTime(new Date());

                commissionService.confirm(entity, user);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1001, "只有财务人事行政才能发起提成确认流程！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/pass")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|提成管理", note = "业务员确认提成通过")
    @Verify(code = "/commission/pass", module = "财务管理/业务员确认提成通过")
    public ResponseData pass(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser();
            Commission entity = commissionService.getById(id);
            if (user.getId().intValue() == entity.getUserId().intValue()) {
                commissionService.pass(entity, user);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1001, "当前用户没有权限，需要业务员本人操作！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/reject")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|提成管理", note = "业务员确认提成驳回")
    @Verify(code = "/commission/reject", module = "财务管理/业务员确认提成驳回")
    public ResponseData reject(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser();
            Commission entity = commissionService.getById(id);
            if (user.getId().intValue() == entity.getUserId().intValue()) {
                commissionService.reject(entity, user);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1001, "当前用户没有权限，需要业务员本人操作！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/release")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|提成管理", note = "财务发放提成")
    @Verify(code = "/commission/release", module = "财务管理/财务发放提成")
    public ResponseData release(@RequestParam("id") Integer id) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            Set<String> set = new HashSet<>();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                for (Role role : roles) {
                    set.add(role.getType());
                }
            }
            Commission entity = commissionService.getById(id);
            if ((set.contains(IConst.ROLE_TYPE_CW) || set.contains(IConst.ROLE_TYPE_RS) || set.contains(IConst.ROLE_TYPE_XZ)) && entity.getState() == IConst.STATE_PASS) {
                commissionService.release(entity, user);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1001, "权限不足或当前状态不允许提成！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /*    *//**
     * 导出详情
     *//*
    @RequestMapping("/exportDetail")
    @Log(opType = OperateType.UPDATE, module = "财务管理|提成导出", note = "提成导出")
    public void exportDetail(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
            }
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("提成详情.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            commissionService.exportDetail(map, outputStream);
        } catch (Exception e) {
            log.error("导出提成详情失败", e);
        }
    }*/

    /**
     * 导出全部
     */
    @RequestMapping("/exportAll")
    @Log(opType = OperateType.UPDATE, module = "财务管理|导出全部", note = "导出全部")
    public void exportAll(HttpServletResponse response, @RequestParam Map map) {
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            }
          /*  else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
            }*/
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("提成列表.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            commissionService.exportAll(map, outputStream);
        } catch (Exception e) {
            log.error("导出提成列表失败", e);
        }
    }

    /**
     * 导出未登记稿件
     */
//    @RequestMapping("/exportUnRegister")
//    @Log(opType = OperateType.UPDATE, module = "财务管理|导出未登记稿件", note = "导出未登记稿件")
//    @ResponseBody
//    public ResponseData exportUnRegister(@RequestParam Map map) {
//        // 获取输出流；
//        OutputStream outputStream = null;
//        ResponseData data = ResponseData.ok();
//        try {
//            String fileName = "未登记稿件列表";
//            StringBuilder filePath = new StringBuilder();
//            String uploadDir = config.getUploadDir();
//            String webDir = config.getWebDir();
//            // 先创建目录；
//            File dir = new File(uploadDir + "/download");
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            // 获取登录用户；
//            User user = AppUtil.getUser();
//
//            filePath.append(uploadDir).append("/download/").append(fileName).append(".xls");
//            String fullPath = filePath.toString();
//            // 获取输出流；
//            outputStream = new FileOutputStream(fullPath);
//
//            List<Role> roles = user.getRoles();
//            if (roles != null && roles.size() > 0) {
//                map.put("roleType", roles.get(0).getType());
//                map.put("roleCode", roles.get(0).getCode());
//                map.put("user", user);
//                map.put("commissionStates", 0);
//                map.put("companyCode", AppUtil.getUser().getCompanyCode());
////                response.setContentType("application/binary;charset=UTF-8");
////                response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("未登记提成导出.xls", "UTF-8"));
////                OutputStream out = response.getOutputStream();
//                commissionService.exportUnRegister(map, outputStream);
//                fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
//                data.putDataValue("file", webDir + "download/" + fullPath);
//            }
//        } catch (Exception e) {
//            log.error("文件未找到", e);
//            data.putDataValue("message", "导出异常，请联系管理员。");
//        } finally {
//            IOUtils.closeQuietly(outputStream);
//        }
//        return data;
//    }
    @RequestMapping("/exportUnRegisterNew")
    @Log(opType = OperateType.QUERY, module = "财务管理|导出未登记稿件", note = "导出未登记稿件")
    @ResponseBody
    public void exportUnRegisterNew(HttpServletResponse response, @RequestParam Map map) {
        //调用service查询方法返回结果集
        ServletOutputStream out = null;
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        //通知浏览器以附件的形式下载处理
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("未登记提成稿件信息" + DateUtils.getStr(), "UTF-8") + ".xlsx");
            out = response.getOutputStream();
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
            Sheet sheet = new Sheet(1, 0, ArticleExcelCommissionUnRegister.class);
            sheet.setAutoWidth(Boolean.TRUE);
            List<ArticleExcelCommissionUnRegister> list = commissionService.exportUnRegisterNew(map);
            writer.write(list, sheet);
            out.flush();
            writer.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(out);
        }
    }

    /**
     * 导出已登记稿件
     */
//    @RequestMapping("/exportRegister")
//    @Log(opType = OperateType.UPDATE, module = "财务管理|导出已登记稿件", note = "导出已登记稿件")
//    @ResponseBody
//    public ResponseData exportRegister(@RequestParam Map map) {
//        // 获取输出流；
//        OutputStream outputStream = null;
//        ResponseData data = ResponseData.ok();
//        try {
//            String fileName = "已登记稿件列表";
//            StringBuilder filePath = new StringBuilder();
//            String uploadDir = config.getUploadDir();
//            String webDir = config.getWebDir();
//            // 先创建目录；
//            File dir = new File(uploadDir + "/download");
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//            // 获取登录用户；
//            User user = AppUtil.getUser();
//            filePath.append(uploadDir).append("/download/").append(fileName).append(".xls");
//            String fullPath = filePath.toString();
//             //获取输出流；
//            outputStream = new FileOutputStream(fullPath);
//            List<Role> roles = user.getRoles();
//            if (roles != null && roles.size() > 0) {
//                map.put("roleType", roles.get(0).getType());
//                map.put("roleCode", roles.get(0).getCode());
//                map.put("user", user);
//                map.put("commissionStates", 2);
//                map.put("companyCode", AppUtil.getUser().getCompanyCode());
////                response.setContentType("application/binary;charset=UTF-8");
////                response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("已登记提成导出.xls", "UTF-8"));
////                OutputStream out = response.getOutputStream();
//                commissionService.exportRegister(map, outputStream);
//                fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
//                data.putDataValue("file", webDir + "download/" + fullPath);
//            }
//        } catch (Exception e) {
//            log.error("文件未找到", e);
//          data.putDataValue("message", "导出异常，请联系管理员。");
//        } finally {
//            IOUtils.closeQuietly(outputStream);
//        }
//        return data;
//    }
    @RequestMapping("/exportRegisterNew")
    @Log(opType = OperateType.QUERY, module = "财务管理|导出已登记稿件", note = "导出已登记稿件")
    @ResponseBody
    public ResponseData exportRegisterNew(HttpServletResponse response, @RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        String fileName = "已登记提成稿件信息" + DateUtils.getStr();
        StringBuilder filePath = new StringBuilder();
        String uploadDir = config.getUploadDir();
        String webDir = config.getWebDir();
        // 先创建目录；
        File dir = new File(uploadDir + "/download");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        filePath.append(uploadDir).append("/download/").append(fileName).append(".xls");
        String fullPath = filePath.toString();

        int size = 0;
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath.toString());
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
            Sheet sheet = new Sheet(1, 0, ArticleExcelCommissionRegister.class);
            sheet.setAutoWidth(Boolean.TRUE);
            List<ArticleExcelCommissionRegister> list = commissionService.exportRegisterNew(map);
            size = list.size();
            writer.write(list, sheet);
            out.flush();
            writer.finish();
            fullPath = fullPath.substring(fullPath.lastIndexOf("/") + 1);
            data.putDataValue("file", webDir + "download/" + fullPath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        data.putDataValue("size", size);
        return data;
    }

    @RequestMapping("/batchRegister")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|提成管理", note = "提成登记")
    @Verify(code = "/commission/batchRegister", module = "提成管理/财务登记提成")
    public ResponseData batchRegister(@RequestParam("ids") String ids, @RequestParam("userId") Integer userId) {
        try {
            User user = AppUtil.getUser();
            Set<String> set = getRoleInfo(user);
            if (set.contains(IConst.ROLE_TYPE_CW) || set.contains(IConst.ROLE_TYPE_RS) || set.contains(IConst.ROLE_TYPE_XZ)) {
                Commission entity = commissionService.batchRegister(ids, userId);

                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1001, "权限不足！");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/batchRegisterOff")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "财务管理|提成管理", note = "提成取消登记")
    @Verify(code = "/commission/batchRegister", module = "提成管理/财务取消登记提成")
    public ResponseData batchRegisterOff(@RequestParam("ids") String ids, @RequestParam("userId") Integer userId) {
        try {
            User user = AppUtil.getUser();
            Set<String> set = getRoleInfo(user);
            if (set.contains(IConst.ROLE_TYPE_CW) || set.contains(IConst.ROLE_TYPE_RS) || set.contains(IConst.ROLE_TYPE_XZ)) {
                Commission entity = commissionService.batchRegisterOff(ids, userId);
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", entity);
                return data;
            } else {
                return ResponseData.customerError(1001, "权限不足！");
            }
        } catch (QinFeiException e) {
//            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    private Set<String> getRoleInfo(User user) {
        Set<String> set = new HashSet<>();
        try {
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                for (Role role : roles) {
                    set.add(role.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }

    /**
     * 财务手动登记稿件提成用
     *
     * @param param
     * @param pageable
     * @return
     */
    @RequestMapping("/queryArticleByCommStates")
    @ResponseBody
    public PageInfo<Map> queryArticleByCommStates(@RequestParam Map param, @PageableDefault() Pageable pageable) {
        PageInfo<Map> list = null;
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                list = commissionService.queryArticleByCommStates(pageable, param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 提成查看页面的稿件列表
     *
     * @param param
     * @param pageable
     * @return
     */
    @RequestMapping("/queryArticleByYearAndMonth")
    @ResponseBody
    public PageInfo<Map> queryArticleByYearAndMonth(@RequestParam Map param, @PageableDefault() Pageable pageable) {
        PageInfo<Map> list = null;
        try {
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                throw new Exception("未查询到角色信息");
            } else {
                list = commissionService.queryArticleByYearAndMonth(pageable, param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
