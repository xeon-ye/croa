package com.qinfei.qferp.controller.media;

import com.qinfei.core.config.Config;
import com.qinfei.qferp.service.biz.IOrderService;
import com.qinfei.qferp.service.media.IMediaService;
import com.qinfei.qferp.service.sys.IRoleService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 媒体管理
 *
 * @author GZW
 */
@Slf4j
@Controller
@RequestMapping("/media")
@Api(description = "媒体管理接口")
class MediaController {
    @Autowired
    IMediaService mediaService;
    @Autowired
    IOrderService orderService;
    @Autowired
    Config config;
    @Autowired
    IRoleService roleService;

  /*  @GetMapping
    @ResponseBody
    @ApiOperation(value = "分页查询媒体列表", notes = "分页查询媒体列表", response = PageInfo.class)
    @Log(opType = OperateType.QUERY, note = "分页查询媒体列表", module = "媒体管理/分页查询媒体列表")
    // @Verify(code = "/media", action = "分页查询媒体列表", module = "媒体管理/分页查询媒体列表")
    public PageInfo<Media> list(@ApiParam("媒体筛选条件") @RequestParam Map<String, Object> map, @ApiParam("媒体筛选多选条件") @RequestParam(value = "n7[]", required = false) String[] n7, @ApiParam("分页对象") Pageable pageable) {
        // 处理多选；
        Object object = map.get("n7");
        if (n7 == null && object != null) {
            n7 = new String[]{object.toString()};
        }
        map.put("n7", n7);
        return mediaService.list(map, pageable);
    }*/

    /**
     * 根据媒体ID查询媒体信息
     *
     * @param id
     * @return
     */
//    @GetMapping("/{id}")
//    @ResponseBody
//    @ApiOperation(value = "根据媒体ID查询媒体信息", notes = "根据媒体ID查询媒体信息", response = ResponseData.class)
////    @Log(opType = OperateType.QUERY, note = "根据媒体ID查询媒体信息", module = "媒体管理/根据媒体ID查询媒体信息")
//    // @Verify(code = "/media/{id}", action = "根据媒体ID查询媒体信息", module =
//    // "媒体管理/根据媒体ID查询媒体信息")
//    public ResponseData get(@PathVariable("id") Integer id) {
//        Media media = mediaService.getById(id);
//        return ResponseData.ok().putDataValue("media", media);
//    }

    /**
     * 更新媒体信息
     *
     * @param media
     * @param session
     * @return
     */
//    @PostMapping("/update")
//    @ResponseBody
//    @ApiOperation(value = "更新媒体信息", notes = "更新媒体信息", response = ResponseData.class)
//    @Log(opType = OperateType.UPDATE, note = "更新媒体信息", module = "媒体管理/更新媒体信息")
//    @Verify(code = "/media/update", action = "更新媒体信息", module = "媒体管理/更新媒体信息")
//    public ResponseData update(@ApiParam("媒体实体类") Media media, @RequestParam(value = "auditsFlag") boolean auditsFlag, @RequestParam(value = "n7", required = false) String[] n7, HttpSession session) {
//        if (n7 != null && n7.length > 0) {
//            StringBuilder dayContent = new StringBuilder();
//            for (String day : n7) {
//                dayContent.append(day);
//            }
//            media.setN7(Integer.parseInt(dayContent.toString()));
//        }
//        User user = (User) session.getAttribute(IConst.USER_KEY);
//        media.setCreatorId(user.getId());
//        // 判断如果“更新责任人”未选择，默认就是当前用户
//        if (media.getUserId() == null) {
//            media.setUserId(user.getId());
//        }
//
//        ResponseData responseData = ResponseData.ok();
//        if (mediaService.checkRepeat(media)) {
//            mediaService.update(media,auditsFlag);
//        } else {
//            responseData.putDataValue("message", "操作失败，媒体已存在。");
//        }
//        return responseData;
//    }

    /*@PostMapping
    @ResponseBody
    @ApiOperation(value = "新增媒体", notes = "新增媒体", response = ResponseData.class)
    @Log(opType = OperateType.ADD, note = "新增媒体", module = "媒体管理/新增媒体")
    @Verify(code = "/media/add", action = "新增媒体", module = "媒体管理/新增媒体")
    public ResponseData save(@ApiParam("媒体实体类") Media media, @RequestParam(value = "n7", required = false) String[] n7, HttpSession session) {
        if (n7 != null && n7.length > 0) {
            StringBuilder dayContent = new StringBuilder();
            for (String day : n7) {
                dayContent.append(day);
            }
            media.setN7(Integer.parseInt(dayContent.toString()));
        }
        User user = (User) session.getAttribute(IConst.USER_KEY);
        media.setCreatorId(user.getId());
        // 判断如果“更新责任人”未选择，默认就是当前用户
        if (media.getUserId() == null) {
            media.setUserId(user.getId());
        }
        media.setCreateDate(new Date());

        ResponseData responseData = ResponseData.ok();
        media.setName(media.getName().trim());
        if (mediaService.checkRepeat(media)) {
            mediaService.save(media);
        } else {
            responseData.putDataValue("message", "操作失败，媒体已存在。");
        }
        return responseData;
    }*/

    /*@GetMapping("isDuplicateForName")
    @ResponseBody
    @ApiOperation(value = "媒体名称查询媒体", notes = "通过媒体名称查询是否有媒体记录")
    @Log(opType = OperateType.QUERY, note = "媒体名称查询媒体", module = "媒体管理/媒体名称查询媒体")
    // @Verify(code = "/media/isDuplicateForName", action = "媒体名称查询媒体", module =
    // "媒体管理/媒体名称查询媒体")
    public ResponseData isDuplicateForName(@ApiParam("媒体板块类型") int mType, @ApiParam("媒体名称") String mediaName) {
        boolean flag = mediaService.getByName(mType, mediaName);
        if (flag) {
            return ResponseData.customerError(1001, "媒体名称已存在!");
        }
        return ResponseData.ok();
    }*/

    /**
     * 查询媒体列表
     *
     * @param mv
     * @return
     */
/*
    @GetMapping("medias")
    @Log(opType = OperateType.QUERY, note = "查询媒体列表", module = "媒体管理/查询媒体列表")
    // @Verify(code = "/media/medias", action = "查询媒体列表", module = "媒体管理/查询媒体列表")
    public ModelAndView medias(ModelAndView mv) {
        mv.setViewName("/media/medias");
        return mv;
    }
*/

    /**
     * 审核驳回
     *
     * @param id
     * @return
     */
//    @GetMapping("reject/{id}")
//    @ResponseBody
//    @Log(opType = OperateType.UPDATE, note = "审核驳回", module = "媒体审核/审核驳回")
//    @Verify(code = "/media/reject/{id}", action = "审核驳回", module = "媒体管理/审核驳回")
//    public ResponseData reject(@PathVariable("id") Integer id) {
//        // Integer userId = AppUtil.getUser().getId();
//        Map<String, Object> map = new HashMap<>();
//        mediaService.reject(id);
//        return ResponseData.ok();
//    }

    /**
     * 审核通过
     *
     * @param id
     * @return
     */
/*    @GetMapping("pass/{id}")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, note = "审核通过", module = "媒体审核/审核通过")
    @Verify(code = "/media/pass/{id}", action = "审核通过", module = "媒体管理/审核通过")
    public ResponseData pass(@PathVariable("id") Integer id) {
        // Map<String, Object> map = new HashMap<>();
        mediaService.pass(id);
        return ResponseData.ok();
    }*/

    /**
     * 审核删除
     *
     * @param id
     * @return
     */
//    @GetMapping("del/{id}")
//    @ResponseBody
//    @Log(opType = OperateType.DELETE, note = "媒体删除", module = "媒体审核/媒体删除")
//    @Verify(code = "/media/del/{id}", action = "媒体删除", module = "媒体管理/媒体删除")
//    public ResponseData del(@PathVariable("id") Integer id) {
//        // Map<String, Object> map = new HashMap<>();
//        mediaService.del(id);
//        return ResponseData.ok();
//    }

    /**
     * 批量删除数据；
     *
     * @param ids：媒体ID数组；
     * @param mediaNames：媒体名称数组，用于消息通知；
     * @param userIds：用户ID数组；
     * @return ：处理结果；
     */
  /*  @RequestMapping("/batchDelete")
    @ResponseBody
    @Log(opType = OperateType.DELETE, note = "媒体删除", module = "媒体审核/媒体批量删除")
    @Verify(code = "/media/batchDelete", action = "媒体批量删除", module = "媒体管理/媒体批量删除")
    public ResponseData delete(@RequestParam(value = "ids[]") Integer[] ids, @RequestParam(value = "mediaNames[]") String[] mediaNames, @RequestParam(value = "userIds[]") Integer[] userIds) {
        // 验证权限；
        User user = AppUtil.getUser();
        ResponseData responseData = ResponseData.ok();
        if (user.getDept() != null && "MJ".equals(user.getDept().getCode()) && user.getCurrentDeptQx()) {
            String message = mediaService.deleteBatch(ids, mediaNames, userIds);
            if (StringUtils.isNotEmpty(message)) {
                responseData.putDataValue("message", message);
            } else {
                responseData.putDataValue("message", "操作完成。");
            }
        } else {
            responseData.putDataValue("message", "权限不足。");
        }
        return responseData;
    }
*/
    /**
     * 批量通过数据；
     *
     * @param ids：媒体ID数组；
     * @param mediaNames：媒体名称数组，用于消息通知；
     * @param userIds：用户ID数组；
     * @return ：处理结果；
     */
   /* @RequestMapping("/passBatch")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, note = "批量通过", module = "媒体管理/媒体审核")
    @Verify(code = "/media/passBatch", action = "媒体批量通过", module = "媒体审核/媒体批量通过")
    public ResponseData passBatch(@RequestParam(value = "ids[]") Integer[] ids, @RequestParam(value = "mediaNames[]") String[] mediaNames, @RequestParam(value = "userIds[]") Integer[] userIds) {
        ResponseData responseData = ResponseData.ok();
        mediaService.passBatch(ids, mediaNames, userIds);
        responseData.putDataValue("message", "操作完成。");
        return responseData;
    }*/

    /**
     * 批量驳回数据；
     *
     * @param ids：媒体ID数组；
     * @param mediaNames：媒体名称数组，用于消息通知；
     * @param userIds：用户ID数组；
     * @return ：处理结果；
     */
  /*  @RequestMapping("/rejectBatch")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, note = "批量驳回", module = "媒体管理/媒体审核")
    @Verify(code = "/media/rejectBatch", action = "媒体批量通过", module = "媒体审核/媒体批量驳回")
    public ResponseData rejectBatch(@RequestParam(value = "ids[]") Integer[] ids, @RequestParam(value = "mediaNames[]") String[] mediaNames, @RequestParam(value = "userIds[]") Integer[] userIds) {
        ResponseData responseData = ResponseData.ok();
        mediaService.rejectBatch(ids, mediaNames, userIds);
        responseData.putDataValue("message", "操作完成。");
        return responseData;
    }*/

    /**
     * 批量删除数据；
     *
     * @param ids：媒体ID数组；
     * @param mediaNames：媒体名称数组，用于消息通知；
     * @param userIds：用户ID数组；
     * @return ：处理结果；
     */
    /*@RequestMapping("/deleteBatch")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, note = "批量删除", module = "媒体管理/媒体审核")
    @Verify(code = "/media/deleteBatch", action = "媒体批量删除", module = "媒体审核/媒体批量删除")
    public ResponseData deleteBatch(@RequestParam(value = "ids[]") Integer[] ids, @RequestParam(value = "mediaNames[]") String[] mediaNames, @RequestParam(value = "userIds[]") Integer[] userIds) {
        ResponseData responseData = ResponseData.ok();
        mediaService.deleteBatch(ids, mediaNames, userIds);
        responseData.putDataValue("message", "操作完成。");
        return responseData;
    }*/

 /*   @GetMapping("/audit/{id}/{taskId}")
    @ResponseBody
    @ApiOperation(value = "根据媒体ID查询媒体信息", notes = "根据媒体ID查询媒体信息", response = ResponseData.class)
    @Log(opType = OperateType.QUERY, note = "根据媒体ID查询媒体信息", module = "媒体管理/根据媒体ID查询媒体信息")
    // @Verify(code = "/audit/{id}/{taskId}", action = "根据媒体ID查询媒体信息", module =
    // "媒体管理/根据媒体ID查询媒体信息")
    public ModelAndView get(@PathVariable("id") Integer id, @PathVariable("taskId") String taskId, ModelAndView mv) {
        Media media = mediaService.getById(id);
        mv.addObject("media", media);
        mv.addObject("taskId", taskId);
        mv.setViewName("/media/auditMedia");
        return mv;
    }*/

    /*@GetMapping("/audits")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "查询媒体审核列表", module = "媒体管理/查询媒体审核列表")
    @Verify(code = "/media/audits", action = "查询媒体审核列表", module = "媒体管理/查询媒体审核列表")
    public ModelAndView audit(ModelAndView mv) {
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        boolean bz = false, zz = false;
        for (Role role : roles) {
            if (IConst.ROLE_TYPE_MJ.equalsIgnoreCase(role.getType())) {
                if (IConst.ROLE_CODE_BZ.equalsIgnoreCase(role.getCode()))
                    bz = true;
                if (IConst.ROLE_CODE_ZZ.equalsIgnoreCase(role.getCode()))
                    zz = true;
            }
        }
        if (!bz && !zz) {
            throw new QinFeiException(ResultEnum.NOAUDIT);
        }
        // //查询状态 组长查询0的值 部长查询1的值
        // mv.addObject("state", bz ? 1 : 0);
        mv.setViewName("/media/audits");
        return mv;
    }*/

    /**
     * 获取指定板块的数据导入模板；
     *
     * @param request：请求对象；
     * @param response：响应对象；
     * @param mediaType：媒体板块类型；
     * @param mediaTypeName：媒体板块类型名称；
     */
 /*   @RequestMapping("downTemplate")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "下载模板", module = "媒体管理/下载模板")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response, @RequestParam int mediaType, @RequestParam String mediaTypeName) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String fileName = mediaTypeName + "批量导入模板";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(fileName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            mediaService.getDataImportTemplate(mediaType, fileName, outputStream);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }*/

    /**
     * 获取导入失败文件；
     *
     * @param request：请求对象；
     * @param response：响应对象；
     * @param fileName：文件名称；
     */
    /*@RequestMapping("downFailedFile")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "下载导入失败文件", module = "媒体管理/下载导入失败文件")
    public void downFailedFile(HttpServletRequest request, HttpServletResponse response, @RequestParam String fileName) {
        try {
            fileName = URLDecoder.decode(fileName, "UTF-8");
            response.setContentType("application/binary;charset=UTF-8");
            // 此处转码方式不一样，请注意；
            String agent = request.getHeader("user-agent");
            String downFileName;
            if (agent.indexOf("MSIE") >= 0 || agent.indexOf("Trident") >= 0) {
                downFileName = URLEncoder.encode(fileName, "UTF8");
            } else {
                downFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }
            response.setHeader("Content-Disposition", "attachment;fileName=" + downFileName);
            OutputStream outputStream = response.getOutputStream();

            StringBuilder filePath = new StringBuilder();
            filePath.append(config.getUploadDir()).append("download/").append(fileName);
            InputStream fileInputStream = new FileInputStream(filePath.toString());
            InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[1024];
            int i = bufferedInputStream.read(buffer);
            while (i != -1) {
                outputStream.write(buffer, 0, i);
                i = bufferedInputStream.read(buffer);
            }
        } catch (IOException e) {
            log.error("文件下载失败", e);
        }
    }*/

    /**
     * 批量导入数据；
     *
     * @param mediaType：媒体类型；
     * @param mediaTypeName：媒体板块类型名称；
     * @param multipartFile：上传的文件对象；
     * @return ：处理结果；
     */
   /* @RequestMapping("importData")
    @ResponseBody
    @Log(opType = OperateType.ADD, note = "批量导入数据", module = "媒体管理/批量新增")
    public ResponseData batchOrder(@RequestParam int mediaType, @RequestParam String mediaTypeName, @RequestParam("file") MultipartFile multipartFile) {
        String name = "media/" + UUIDUtil.get4UUID() + ".jpg";
        String dist = config.getUploadDir() + name;
        File file = new File(dist);
        String str = config.getWebDir() + name;
        String result = mediaService.batchAddMedia(mediaType, mediaTypeName, DataImportUtil.copyFile(multipartFile, config.getUploadDir(), "media/"));
        ResponseData data = ResponseData.ok();
        if (StringUtils.isNotEmpty(result)) {
            if ("0".equals(result)) {
                data.putDataValue("msg", "没有数据导入成功，请确认导入板块与模板对应，且内容符合要求，并检查数据是否已存在。");
            } else {
                data.putDataValue("msg", "部分内容未成功导入，请点击红色提示内容下载文件检查内容是否符合要求。");
                data.putDataValue("file", result);
            }
        }
        return data;
    }*/

    /**
     * 批量上传文件；
     *
     * @param multipartFiles：上传的文件对象；
     * @return ：处理结果；
     */
/*    @RequestMapping("fileUpload")
    @ResponseBody
    @Log(opType = OperateType.ADD, note = "批量上传文件", module = "媒体管理/批量上传文件")
    public ResponseData fileUpload(@RequestParam(value = "files") MultipartFile[] multipartFiles) {
        try {
            // 获取文件数量；
            int length = multipartFiles.length;
            MultipartFile multipartFile;
            String originalFilename;
            // 创建上传目录；
            StringBuilder fileDirPath = new StringBuilder();
            fileDirPath.append(config.getUploadDir()).append("upload/");
            File uploadFileDir = new File(fileDirPath.toString());
            if (!uploadFileDir.exists()) {
                uploadFileDir.mkdirs();
            }

            // 文件上传；
            File uploadFile;
            // 存储文件地址；
            List<String> filePaths = new ArrayList<>();
            // 文件后缀；
            String fileType;
            // 类型存储目录；
            File fileTypeDir;
            // 新文件名称；
            String newFileName;
            for (int i = 0; i < length; i++) {
                multipartFile = multipartFiles[i];
                originalFilename = multipartFile.getOriginalFilename();
                fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                // 生成新文件名称；
                newFileName = UUIDUtil.get32UUID() + "." + fileType;

                // 创建文件类型目录；
                fileTypeDir = new File(fileDirPath + fileType);
                if (!fileTypeDir.exists()) {
                    fileTypeDir.mkdirs();
                }

                uploadFile = new File(fileTypeDir + "/" + newFileName);
                multipartFile.transferTo(uploadFile);

                filePaths.add(config.getWebDir() + "upload/" + fileType + "/" + newFileName);
            }

            ResponseData data = ResponseData.ok();
            data.putDataValue("data", filePaths);
            return data;
        } catch (IOException e) {
            log.error("数据导入异常", e);
            return ResponseData.customerError(1001, "导入失败。");
        }
    }*/

  /*  @RequestMapping("/getMediaNumbers")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "统计类型", module = "媒体管理/媒体审核")
    public List<Map> getMediaNumbers() {
        return mediaService.getMediaNumber();
    }*/

    /*@RequestMapping("/userDelete")
    @ResponseBody
    public ResponseData userDelete(@RequestParam("datas") String datas){
        try{
            Map map = mediaService.userDelete(datas);
            Integer row = (Integer) map.get("row");
            String message = (String)map.get("message");
            if (row == 0){
                return ResponseData.customerError(1001,message);
            }else{
                ResponseData da = ResponseData.ok();
                da.putDataValue("message",message);
                return da;
            }

        }catch (Exception e){
            log.error("批量删除稿件失败", e);
            return ResponseData.customerError(1001, e.getMessage());
        }
    }*/

}
