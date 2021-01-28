package com.qinfei.qferp.controller.announcementinform;
import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.announcementinform.MediaPass;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.service.announcementinform.IMediaPassService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/Mediapass")
class MediaPassController {
    @Autowired
    private Config config ;

    @Autowired
    IMediaPassService mediaPassService;


    //通知公告列表
    @RequestMapping("/notificationlist")
//    @Log(opType = OperateType.QUERY, module = "公告通知/通知公告列表", note = "公告通知/通知公告列表")
    @ResponseBody
    public PageInfo<MediaPass> businessList(@RequestParam Map map, @PageableDefault() Pageable pageable) {
        return mediaPassService.selectByPrimaryKey(map,pageable);
    }


    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }
    //新增通知公告
    @RequestMapping("/add")
//    @Log(opType = OperateType.ADD, module = "公告通知/新增通知公告", note = "公告通知/新增通知公告")
    @ResponseBody
    public ResponseData add (MediaPass mediaPass,@RequestParam(value = "file", required = false) MultipartFile[] multipartFile,Integer[] deptIds) {
        //参数校验
        try{
            //MediaPass mis = mediaPassService.getById(mediaPass.getId());
            //System.out.println("=======================" + Arrays.toString(deptIds));
//            if(multipartFile!=null) {
//                String fileName = UUIDUtil.get32UUID() + multipartFile.getOriginalFilename();
//                String childPath = "/media/announcement/";
//                File destFile = new File(config.getUploadDir() + childPath + fileName);
//                if (!destFile.getParentFile().exists()) {
//                    destFile.getParentFile().mkdirs();
//                }
//                multipartFile.transferTo(destFile);
//                mediaPass.setAttachment(multipartFile.getOriginalFilename());
//                mediaPass.setAttachmentLink(config.getWebDir() + childPath + fileName);
//            }
            List<String> fileNames = new ArrayList<>();
            List<String> filePaths = new ArrayList<>();
            if (multipartFile.length >1){
                for (MultipartFile file : multipartFile){
                    if (file.getSize()>0){
                        String temp = file.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".")>-1){
                            ext = temp.substring(temp.lastIndexOf("."));
                            String fileName = UUIDUtil.get32UUID() + ext;
                            String childPath = getStringData()+"/media/announcement/";
                            File destFile = new File(config.getUploadDir() + childPath + fileName);
                            if (!destFile.getParentFile().exists()) {
                                destFile.getParentFile().mkdirs();
                            }
                            file.transferTo(destFile);
                            fileNames.add(file.getOriginalFilename());
                            filePaths.add(config.getWebDir() + childPath + fileName);

                        }
                    }
                    mediaPass.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                    mediaPass.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                }
            }else {
                MultipartFile multipartFile1=multipartFile[0];
                if (multipartFile1.getSize()>0){
                    String temp = multipartFile1.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".")>-1){
                        ext = temp.substring(temp.lastIndexOf("."));

                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/media/announcement/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile1.transferTo(destFile);
                    fileNames.add(multipartFile1.getOriginalFilename());
                    filePaths.add(config.getWebDir() + childPath + fileName);
                    mediaPass.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                    mediaPass.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                }else {
                    if (mediaPass.getId() != null){
                        MediaPass old = mediaPassService.getById(mediaPass.getId());
                        mediaPass.setAttachment(old.getAttachment());
                        mediaPass.setAttachmentLink(old.getAttachmentLink());
                    }
                }

            }

            mediaPassService.add(mediaPass,deptIds);
            if (deptIds != null){
                Map map;
                List<Map>  file = new ArrayList<>();
                List<Integer>  ids = new ArrayList<>();
                for (int deptId :deptIds) {
                    map = new HashMap();
                    map.put("operationDeptId", deptId);
                    map.put("id", mediaPass.getId());

                    ids.add(deptId);
                    file.add(map);
                }
                mediaPassService.insertoperationDept(file);
            }

            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", mediaPass) ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    //编辑通知公告
    @RequestMapping("/edit")
//    @Log(opType = OperateType.UPDATE, module = "公告通知/编辑通知公告", note = "公告通知/编辑通知公告")
    @ResponseBody
    public ResponseData edit (MediaPass entity,@RequestParam(value = "file", required = false) MultipartFile[] multipartFile,Integer[] deptIds){
        try {
//            //通知公告中的附件
//            if(multipartFile!=null) {
//                String fileName = UUIDUtil.get32UUID() + multipartFile.getOriginalFilename();
//                String childPath = "/media/announcement/";
//                File destFile = new File(config.getUploadDir() + childPath + fileName);
//                if (!destFile.getParentFile().exists()) {
//                    destFile.getParentFile().mkdirs();
//                }
//                multipartFile.transferTo(destFile);
//                entity.setAttachment(multipartFile.getOriginalFilename());
//                entity.setAttachmentLink(config.getWebDir() + childPath + fileName);
//            }
            List<String> fileNames = new ArrayList<>();
            List<String> filePaths = new ArrayList<>();
            if (multipartFile.length >1){
                for (MultipartFile file : multipartFile){
                    if (file.getSize()>0){
                        String temp = file.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".")>-1){
                            ext = temp.substring(temp.lastIndexOf("."));
                            String fileName = UUIDUtil.get32UUID() + ext;
                            String childPath = getStringData()+ "/media/announcement/";
                            File destFile = new File(config.getUploadDir() + childPath + fileName);
                            if (!destFile.getParentFile().exists()) {
                                destFile.getParentFile().mkdirs();
                            }
                            file.transferTo(destFile);
                            fileNames.add(file.getOriginalFilename());
                            filePaths.add(config.getWebDir() + childPath + fileName);

                        }
                    }
                    entity.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                    entity.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                }
            }else {
                MultipartFile multipartFile1=multipartFile[0];
                if (multipartFile1.getSize()>0){
                    String temp = multipartFile1.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".")>-1){
                        ext = temp.substring(temp.lastIndexOf("."));

                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath =  getStringData()+"/media/announcement/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile1.transferTo(destFile);
                    fileNames.add(multipartFile1.getOriginalFilename());
                    filePaths.add(config.getWebDir() + childPath + fileName);
                    entity.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                    entity.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                }else {
                    if (entity.getId() != null){
                        MediaPass old = mediaPassService.getById(entity.getId());
                        entity.setAttachment(old.getAttachment());
                        entity.setAttachmentLink(old.getAttachmentLink());
                    }
                }

            }
            mediaPassService.edit(entity,deptIds);
            if (deptIds != null){
                Map map;
                List<Map>  file = new ArrayList<>();
                List<Integer>  ids = new ArrayList<>();
                for (int deptId :deptIds) {
                    map = new HashMap();
                    map.put("operationDeptId", deptId);
                    map.put("id", entity.getId());

                    ids.add(deptId);
                    file.add(map);
                }
                // mediaPassService.editoperationDept(ids);
                mediaPassService.insertoperationDept(file);
            }
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
     * 获取编辑、查看内容
     * @param id
     * @return
     */
    @RequestMapping(value="/editAjax")
//    @Log(opType = OperateType.QUERY, module = "公告通知/获取编辑、查看内容", note = "公告通知/获取编辑、查看内容")
    @ResponseBody
    public ResponseData editAjax(@RequestParam("id") Integer id) {
        try{
            ResponseData data = ResponseData.ok();
            MediaPass entity = mediaPassService.getById(id) ;

            List<Dept> list = mediaPassService.queryDeptByAccountId(entity.getId()) ;
            data.putDataValue("list",list) ;
            data.putDataValue("entity",entity) ;
            return data ;
        }

        catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/del")
//    @Log(opType = OperateType.DELETE, module = "公告通知/删除公告", note = "公告通知/删除公告")
    @ResponseBody
    public ResponseData del(@RequestParam("id") Integer id){
        try{
            MediaPass entity = mediaPassService.getById(id) ;
            ResponseData data = ResponseData.ok();
            mediaPassService.delById(entity);
            data.putDataValue("message","操作成功") ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 删除部门
     * @param operationDeptId
     * @param deptId
     * @return
     */
    @RequestMapping("/delDeptAccountDept")
//    @Log(opType = OperateType.DELETE, module = "公告通知/删除部门", note = "公告通知/删除部门")
    @ResponseBody
    public ResponseData delDeptAccountDept(@RequestParam("operationDeptId") Integer operationDeptId,@RequestParam("deptId") Integer deptId){
        try {
            List<Dept> list = mediaPassService.delDeptAccountDept(operationDeptId, deptId);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("list", list);
            return data;


        }catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage());
        }
    }

    /*
     *确认通知公告信息
     *
     * */
    @RequestMapping("/confirm")
//    @Log(opType = OperateType.UPDATE, module = "公告通知/确认通知公告信息", note = "公告通知/确认通知公告信息")
    @ResponseBody
    public ResponseData announcementConfirming (MediaPass mediaPass){
        ResponseData data = ResponseData.ok();
        mediaPassService.announcementConfirming(mediaPass);
        data.putDataValue("message","保存成功");
        return data;
    }

    @PostMapping("getResourcePermission")
    @ResponseBody
    @ApiOperation(value = "集团信息", notes = "获取当前用户集团信息权限")
    public Map<String, Object> getResourcePermission(HttpServletRequest request){
        return mediaPassService.getResourcePermission(request);
    }


}






