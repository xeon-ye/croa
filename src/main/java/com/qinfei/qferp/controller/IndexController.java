package com.qinfei.qferp.controller;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.DeviceUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sys.IResourceService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 默认页
 */
@Slf4j
@Controller
class IndexController {

    @Autowired
    IResourceService resourceService;
    @Autowired
    IUserService userService;

    @Autowired
    Config config;


    @GetMapping(value = {"/", "/mobile/"})
    public ModelAndView index(ModelAndView mv, HttpServletRequest request) {
//        mv.addObject("AppName", APP_NAME);
        User user = AppUtil.getUser();
        Integer id = user.getId();
        if ("超级管理员".equals(user.getName()) && "admin".equals(user.getUserName()))
            id = -1;
        List<Resource> menus = resourceService.queryMenuByUserId(id);
        mv.addObject("menus", menus);
        mv.setViewName(DeviceUtils.isMobile(request)?"mobile/index":"index");
//        mv.setViewName("index");
        return mv;
    }

    @GetMapping("/csrf")
    public ModelAndView csrf(ModelAndView mv) {
//        mv.addObject("AppName", APP_NAME);
        mv.setViewName("index");
        return mv;
    }

    /**
     * 注销
     *
     * @param session
     * @return
     */
    @GetMapping("/logout")
    @Log(opType = OperateType.DELETE, module = "系统管理/用户注销，退出登录", note = "用户注销，退出登录")
    public String logout(HttpSession session, HttpServletRequest request) {
        User user = AppUtil.getUser();
        if (user != null) {
            userService.logout(user);
        }
        session.invalidate();
        return DeviceUtils.isMobile(request) ? "redirect:mobile/login" : "redirect:login";
//        return "login";
    }

    /**
     * @param file     文件数组
     * @param filePart 文件属于哪个部分
     */
    @PostMapping("fileUpload")
    @ResponseBody
    public ResponseData fileUpload(MultipartFile[] file, @RequestParam(required = false) String filePart) throws IOException {
        String dateDir = getCurrentDateDir();
        String uploadDir = config.getUploadDir() + dateDir + filePart;
        String webdir = config.getWebDir() + dateDir + filePart;
        File fileDir = new File(uploadDir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        List<Map<String, String>> fileList = new ArrayList<>();
        if(file != null && file.length > 0){
            for (MultipartFile multipartFile : file) {
                String suffix = "";
                String originalFilename = multipartFile.getOriginalFilename();
                if (!StringUtils.isEmpty(originalFilename)) {
                    int index = originalFilename.lastIndexOf(".");
                    if (index != -1) {
                        suffix = originalFilename.substring(index);
                    }
                }
                Map<String, String> map = new HashMap<>();
                String fileName = UUIDUtil.get12UUID() + suffix;
                File f = new File(uploadDir, fileName);
                multipartFile.transferTo(f);
                map.put("file", webdir + "/" + fileName);
                map.put("fileName", fileName);
                map.put("uploadPath", f.getAbsolutePath());
                map.put("oriName", originalFilename);
                fileList.add(map);
            }
        }
        return ResponseData.ok().putDataValue("result", fileList);
    }

    /**
     * 富文本编辑器上传文件
     *
     * @param imgFile  文件, 变量名称固定，这个是kindeditor提供的
     * @param dir      上传类型，分别为image、flash、media、file, 变量名称固定，这个是kindeditor提供的
     * @param filePart 文件属于功能文件目录名
     */
    @PostMapping("editUpload")
    @ResponseBody
    public Map<String, Object> editUpload(MultipartFile imgFile, @RequestParam(value = "dir", required = false) String dir,
                                          @RequestParam(value = "filePart", required = false) String filePart) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!StringUtils.isEmpty(dir)) {
                filePart += File.separator + dir;
            }
            String dateDir = getCurrentDateDir();
            String uploadDir = config.getUploadDir() + dateDir + filePart;
            String webdir = config.getWebDir() + dateDir + filePart;
            File fileDir = new File(uploadDir);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            String suffix = "";
            String originalFilename = imgFile.getOriginalFilename();
            if (!StringUtils.isEmpty(originalFilename)) {
                int index = originalFilename.lastIndexOf(".");
                if (index != -1) {
                    suffix = originalFilename.substring(index);
                }
            }
            String fileName = UUIDUtil.get12UUID() + suffix;
            File f = new File(uploadDir, fileName);
            imgFile.transferTo(f);
            result.put("url", webdir + "/" + fileName);
            result.put("error", 0);
        } catch (Exception e) {
            result.put("error", 1);
            result.put("message", "上传文件失败！");
        }
        return result;
    }

    //获取年/月/日目录结构
    private String getCurrentDateDir(){
        return DateUtils.format(new Date(), "yyyy-MM/");
    }

    /*public static boolean encodeFlag = false; //默认未编码，用于下面接口只能调用一次
    @GetMapping("encodeData")
    @ResponseBody
    @Transactional
    public ResponseData encodeData(){
        try {
            if(!IndexController.encodeFlag){
                long startTime = System.currentTimeMillis();
                DockingPeopleMapper dockingPeopleMapper = SpringUtils.getBean(DockingPeopleMapper.class);
                List<DockingPeople> dockingPeopleList = dockingPeopleMapper.listAllPeople();
                if(!CollectionUtils.isEmpty(dockingPeopleList)){
                    for(DockingPeople dockingPeople : dockingPeopleList){
                        if(!StringUtils.isEmpty(dockingPeople.getPhone())){
                            dockingPeople.setPhone(EncryptUtils.encrypt(EncryptUtils.oldDecrypt(dockingPeople.getPhone())));
                        }
                        if(!StringUtils.isEmpty(dockingPeople.getConnectionType())){
                            dockingPeople.setConnectionType(EncryptUtils.encrypt(EncryptUtils.oldDecrypt(dockingPeople.getConnectionType())));
                        }
                        dockingPeopleMapper.update(dockingPeople);
                    }
                }
                SupplierMapper supplierMapper = SpringUtils.getBean(SupplierMapper.class);
                List<Supplier> supplierList = supplierMapper.listOldAllSupplier();
                if(!CollectionUtils.isEmpty(supplierList)){
                    for(Supplier supplier : supplierList){
                        if(!StringUtils.isEmpty(supplier.getPhone())){
                            supplier.setPhone(EncryptUtils.encrypt(EncryptUtils.oldDecrypt(supplier.getPhone())));
                        }
                        if(!StringUtils.isEmpty(supplier.getQqwechat())){
                            supplier.setQqwechat(EncryptUtils.encrypt(EncryptUtils.oldDecrypt(supplier.getQqwechat())));
                        }
                        supplierMapper.update(supplier);
                    }
                }
                long endTime = System.currentTimeMillis();
                IndexController.encodeFlag = true;
                return ResponseData.ok().putDataValue("time", "耗时"+(endTime-startTime)/1000+"秒");
            }else {
                return ResponseData.customerError(1002, "已经进行过一次解码编码操作！");
            }
        }catch (Exception e){
            IndexController.encodeFlag = false;
            throw e;
        }
    }*/

}
