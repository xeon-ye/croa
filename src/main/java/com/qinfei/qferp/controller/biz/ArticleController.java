package com.qinfei.qferp.controller.biz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.qinfei.core.excel.StyleExcelHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.OfficeUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.media.FileEntitys;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.enumUtils.FilesEnum;
import com.qinfei.qferp.excelListener.ArticleExcel;
import com.qinfei.qferp.excelListener.ArticleExcelCSY;
import com.qinfei.qferp.excelListener.ArticleExcelMJ;
import com.qinfei.qferp.excelListener.ArticleExcelYW;
import com.qinfei.qferp.mapper.media.FileEntityMapper;
import com.qinfei.qferp.service.biz.IArticleService;
import com.qinfei.qferp.service.sys.IDeptService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/article")
class ArticleController {
    @Autowired
    IArticleService articleService;
    @Autowired
    Config config;
    @Autowired
    IDictService dictService;
    @Autowired
    FileEntityMapper fileEntityMapper;
    @Autowired
    IDeptService deptService;
    @Autowired
    OfficeUtils officeUtils;

    /**
     * 查询税种
     *
     * @return
     */
    @RequestMapping("/tax")
//	@Log(opType = OperateType.QUERY, module = "稿件管理/查询税种", note = "稿件管理/查询税种")
    @ResponseBody
    public List<Dict> tax() {
        return dictService.listDict("tax");
    }


    @PostMapping("listArtFeedback")
    @ResponseBody
    public PageInfo<Map<String, Object>> listArtFeedback(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable) {
        return articleService.listArtFeedback(param, pageable);
    }

    /**
     * 反馈列表信息导出；
     */
    @RequestMapping("/exportData")
    @Log(opType = OperateType.QUERY, module = "稿件管理/反馈列表信息导出", note = "稿件管理/反馈列表信息导出")
    @ResponseBody
    public ResponseData exportData(@RequestParam Map<String, Object> param) {
        try {
            return ResponseData.ok().putDataValue("list", articleService.exportFeedbackList(param));
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002, "反馈列表导出异常！");
        }
    }

    @RequestMapping("/articleList")
    @Log(opType = OperateType.QUERY, module = "稿件管理", note = "稿件列表")
    @ResponseBody
    public PageInfo<Map<String, Object>> articleList(@RequestParam Map param, @PageableDefault() Pageable pageable) {
        param.put("user", AppUtil.getUser());
        if (param.get("deptId") != null) {
            Integer deptId = Integer.parseInt((String) param.get("deptId"));
            List<Dept> deptList = deptService.listByParentId(deptId);
            param.put("deptList", deptList);
        }
        return articleService.articleList(param, pageable);
    }

    @RequestMapping("/articleListCSY")
    @Log(opType = OperateType.QUERY, module = "稿件管理", note = "催收员稿件列表")
    @ResponseBody
    public PageInfo<Map<String, Object>> articleListCSY(@RequestParam Map param, @PageableDefault() Pageable pageable) {
        param.put("user", AppUtil.getUser());
        if (param.get("deptId") != null) {
            Integer deptId = Integer.parseInt((String) param.get("deptId"));
            List<Dept> deptList = deptService.listByParentId(deptId);
            param.put("deptList", deptList);
        }
        return articleService.articleListCSY(param, pageable);
    }

    @RequestMapping("/articleListYW")
    @Log(opType = OperateType.QUERY, module = "业务管理", note = "业务查询")
    @ResponseBody
    public PageInfo<Map<String, Object>> articleListYW(@RequestParam Map param, @PageableDefault() Pageable pageable) {
        param.put("user", AppUtil.getUser());
        return articleService.articleListYW(param, pageable);
    }

    @RequestMapping("/articleListMJ")
    @Log(opType = OperateType.QUERY, module = "媒介管理", note = "媒介查询")
    @ResponseBody
    public PageInfo<Map<String, Object>> articleListMJ(@RequestParam Map param, @PageableDefault() Pageable pageable) {
        param.put("user", AppUtil.getUser());
        //业务部门
        if (param.get("deptId") != null) {
            Integer deptId = Integer.parseInt((String) param.get("deptId"));
            List<Dept> deptList = deptService.listByParentId(deptId);
            param.put("deptList", deptList);
        }
        //媒介部门
        if (param.get("mjDeptId") != null) {
            Integer deptId = Integer.parseInt((String) param.get("mjDeptId"));
            List<Dept> mjDeptList = deptService.listByParentId(deptId);
            param.put("mjDeptList", mjDeptList);
        }
        return articleService.articleListMJ(param, pageable);
    }

    @RequestMapping("/articleResult")
    @ResponseBody
    public Map articleResultJT(@RequestParam Map param) {
        param.put("user", AppUtil.getUser());
        if (param.get("deptId") != null) {
            Integer deptId = Integer.parseInt((String) param.get("deptId"));
            List<Dept> deptList = deptService.listByParentId(deptId);
            param.put("deptList", deptList);
        }
        return articleService.articleResult(param);
    }

    @RequestMapping("/articleResultCSY")
    @ResponseBody
    public Map businessResultCSY(@RequestParam Map param) {
        param.put("user", AppUtil.getUser());
        if (param.get("deptId") != null) {
            Integer deptId = Integer.parseInt((String) param.get("deptId"));
            List<Dept> deptList = deptService.listByParentId(deptId);
            param.put("deptList", deptList);
        }
        return articleService.articleResultCSY(param);
    }

    @RequestMapping("/articleResultYW")
    @ResponseBody
    public Map articleResultYW(@RequestParam Map param) {
        param.put("user", AppUtil.getUser());
        return articleService.articleResultYW(param);
    }

    @RequestMapping("/articleResultMJ")
    @ResponseBody
    public Map articleResultMJ(@RequestParam Map param) {
        param.put("user", AppUtil.getUser());
        //业务部门筛选
        if (param.get("deptId") != null) {
            Integer deptId = Integer.parseInt((String) param.get("deptId"));
            List<Dept> deptList = deptService.listByParentId(deptId);
            param.put("deptList", deptList);
        }
        //媒介部门筛选
        if (param.get("mjDeptId") != null) {
            Integer deptId = Integer.parseInt((String) param.get("mjDeptId"));
            List<Dept> mjDeptList = deptService.listByParentId(deptId);
            param.put("mjDeptList", mjDeptList);
        }
        return articleService.articleResultMJ(param);
    }

    @RequestMapping("/exportArticle")
    @Log(opType = OperateType.QUERY, module = "稿件管理", note = "导出稿件列表")
    @Verify(code = "/article/exportArticle", module = "稿件查询/导出稿件")
    public void exportArticle(HttpServletResponse response, @RequestParam Map map) {
        ServletOutputStream out = null;
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        ExcelWriter writer = null;
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("稿件信息" + DateUtils.format(new Date()), "UTF-8") + ".xlsx");
            out = response.getOutputStream();
            writer = new ExcelWriter(null,out, ExcelTypeEnum.XLSX, true,new StyleExcelHandler());
            
            //writer = EasyExcel.write(out).autoCloseStream(Boolean.TRUE).excelType(ExcelTypeEnum.XLSX).needHead(Boolean.TRUE).password("wanpuya").build();
            
            Sheet sheet = new Sheet(1, 0, ArticleExcel.class);
            sheet.setAutoWidth(Boolean.TRUE);
            map.put("user", AppUtil.getUser());
            List<Article> articles = JSON.parseArray((String) map.get("datas"), Article.class);
            map.put("articles", articles);
            if (map.get("deptId") != null) {
                Integer deptId = Integer.parseInt((String) map.get("deptId"));
                List<Dept> deptList = deptService.listByParentId(deptId);
                map.put("deptList", deptList);
            }
            Integer count = articleService.articleListCount(map);
            Integer pageSize = 6000;
            Integer no = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
            map.put("pageSize", pageSize);
            for (int i = 0; i < no; i++) {
                map.put("index", i * pageSize);
                List<ArticleExcel> result = articleService.articleListPage(map);
                writer.write(result, sheet);
                out.flush();
            }
            writer.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Objects.requireNonNull(writer).finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
            IOUtils.close(out);
        }
    }

    @RequestMapping("/exportArticleCSY")
    @Log(opType = OperateType.QUERY, module = "稿件管理", note = "催收员导出稿件")
    @Verify(code = "/article/exportArticleCSY", module = "稿件查询/导出稿件(催收员)")
    public void exportArticleCSY(HttpServletResponse response, @RequestParam Map map) {
        ServletOutputStream out = null;
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        ExcelWriter writer = null;
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("稿件信息" + DateUtils.format(new Date()), "UTF-8") + ".xlsx");
            out = response.getOutputStream();
            writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
      //    writer = EasyExcel.write(out).autoCloseStream(Boolean.TRUE).excelType(ExcelTypeEnum.XLSX).needHead(Boolean.TRUE).password("wanpuya").build();
            
            
            
            Sheet sheet = new Sheet(1, 0, ArticleExcelCSY.class);
           // sheet.setAutoWidth(Boolean.TRUE);
            List<Article> articles = JSON.parseArray((String) map.get("datas"), Article.class);
            map.put("articles", articles);
            if (map.get("deptId") != null) {
                Integer deptId = Integer.parseInt((String) map.get("deptId"));
                List<Dept> deptList = deptService.listByParentId(deptId);
                map.put("deptList", deptList);
            }
            map.put("user", AppUtil.getUser());
            Integer count = articleService.articleListCountCSY(map);
            Integer pageSize = 6000;
            Integer no = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
            map.put("pageSize", pageSize);
            for (int i = 0; i < no; i++) {
                map.put("index", i * pageSize);
                List<ArticleExcelCSY> result = articleService.articleListPageCSY(map);
                writer.write(result, sheet);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Objects.requireNonNull(writer).finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
            IOUtils.close(out);
        }
    }

    @RequestMapping("/exportArticleYW")
    @Log(opType = OperateType.QUERY, module = "业务管理", note = "业务查询导出稿件")
    @Verify(code = "/article/exportArticleYW", module = "业务查询/导出稿件")
    public void exportArticleYW(HttpServletResponse response, @RequestParam Map map) {
        User user = AppUtil.getUser();
        ServletOutputStream out = null;
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        //通知浏览器以附件的形式下载处理
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("稿件信息" + DateUtils.format(new Date()), "UTF-8") + ".xlsx");
            out = response.getOutputStream();
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
            if (map.containsKey("datas")) {
                List<Integer> ids = new ArrayList<>();
                if (map.get("datas").toString().indexOf(",") > -1) {
                    String[] idss = map.get("datas").toString().split(",");
                    for (int i = 0; i < idss.length; i++) {
                        ids.add(Integer.parseInt(idss[i]));
                    }
                    map.put("ids", ids);
                } else {
                    ids.add(Integer.parseInt(map.get("datas").toString()));
                    map.put("ids", ids);
                }
            }

            map.put("user", user);
            Integer count = articleService.articleListCountYW(map);
            Integer pageSize = 3000;
            Integer no = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
            map.put("pageSize", pageSize);
            Sheet sheet = new Sheet(1, 0, ArticleExcelYW.class);
            sheet.setAutoWidth(Boolean.TRUE);
            for (int i = 0; i < no; i++) {
                map.put("index", i * pageSize);
                List<ArticleExcelYW> result = articleService.articleListPageYW(map);
                writer.write(result, sheet);
                out.flush();
            }
            writer.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(out);
        }
    }

    @RequestMapping("/exportArticleMJ")
    @Log(opType = OperateType.QUERY, module = "媒介管理", note = "媒介查询导出稿件")
    @Verify(code = "/article/exportArticleMJ", module = "媒介查询/导出稿件")
    public void exportArticleMJ(HttpServletResponse response, @RequestParam Map map) {
        User user = AppUtil.getUser();
        ServletOutputStream out = null;
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        //通知浏览器以附件的形式下载处理
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("稿件信息" + DateUtils.format(new Date()), "UTF-8") + ".xlsx");
            out = response.getOutputStream();
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);

            List<Article> articles = JSON.parseArray((String) map.get("datas"), Article.class);
            map.put("articles", articles);
            //业务部门筛选
            if (map.get("deptId") != null) {
                Integer deptId = Integer.parseInt((String) map.get("deptId"));
                List<Dept> deptList = deptService.listByParentId(deptId);
                map.put("deptList", deptList);
            }
            //媒介部门
            if (map.get("mjDeptId") != null) {
                Integer deptId = Integer.parseInt((String) map.get("mjDeptId"));
                List<Dept> mjDeptList = deptService.listByParentId(deptId);
                map.put("mjDeptList", mjDeptList);
            }
            map.put("user", user);
            Integer count = articleService.articleListCountMJ(map);
            Integer pageSize = 3000;
            Integer no = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
            map.put("pageSize", pageSize);

            Sheet sheet = new Sheet(1, 0, ArticleExcelMJ.class);
            sheet.setAutoWidth(Boolean.TRUE);
            for (int i = 0; i < no; i++) {
                map.put("index", i * pageSize);
                List<ArticleExcelMJ> result = articleService.articleListPageMJ(map);
                writer.write(result, sheet);
                out.flush();
            }
            writer.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(out);
        }
    }

    @RequestMapping("/editArticle")
    @ResponseBody
    public Map editArticle(@RequestParam Map param) {
        return articleService.editArticle(param);
    }


    /**
     * 批量修改稿件
     *
     * @param param 入参
     * @return message
     */
    @RequestMapping("/batchEditArticle")
    @ResponseBody
    public ResponseData batchEditArticle(@RequestParam Map<String, String> param) {
        int num = articleService.batchEditArticle(param);
        if (num == 0) {
            return ResponseData.customerError(1001, "批量修改失败！");
        } else {
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "批量操作成功！");
            return data;
        }
    }

    @RequestMapping("/addArticle")
    @ResponseBody
    public ResponseData addArticle(Order order, Article article, @RequestParam(value = "file", required = false) MultipartFile[] files) {
        int num = articleService.add(order, article, files);
        if (num == 0) {
            return ResponseData.customerError(1001, "保存失败！");
        } else {
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功！");
            return data;
        }
    }

    @RequestMapping("/updateArticle")
    @ResponseBody
    public ResponseData updateArticle(@RequestParam Map param, @RequestParam(value = "file", required = false) MultipartFile[] files) {
        try {
            User user = AppUtil.getUser();
            Object object = param.get("userId");
            if (object != null) {
                Object artId = param.get("artId");
                Integer id = Integer.parseInt(object.toString());
                Integer articleId = Integer.parseInt(artId.toString());
                // 查询对象；
                Article article = articleService.getById(articleId);
                if (user.getId().equals(id) || user.getId().equals(article.getMediaUserId())) {
                    //修改附件
                    if (files != null && files.length > 0) {
                        String dateDir = DateUtils.format(new Date(), "/yyyy/MM/dd");
                        //文件父路径
                        String childPath = dateDir + "/article/";
                        //将文件名和路径拼装成字符串
                        List<String> fileNames = new ArrayList<>();
                        List<String> filePaths = new ArrayList<>();
                        for (MultipartFile multipartFile : files) {
                            if (multipartFile.getSize() > 0) {
                                String temp = multipartFile.getOriginalFilename();
                                String ext = null;
                                if (temp.indexOf(".") > -1) {
                                    ext = temp.substring(temp.lastIndexOf("."));
                                }
                                String fileName = UUIDUtil.get32UUID() + ext;
                                File destFile = new File(config.getUploadDir() + childPath + fileName);
                                if (!destFile.getParentFile().exists()) {
                                    destFile.getParentFile().mkdirs();
                                }
                                try {
                                    multipartFile.transferTo(destFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                fileNames.add(multipartFile.getOriginalFilename());
                                filePaths.add(config.getWebDir() + childPath + fileName);
                            }
                        }
                        //查询稿件表中是否有该稿件的附件
                        FileEntitys file = fileEntityMapper.selectByArtId(articleId, FilesEnum.ARTICLEDIFFILES.getType());
                        FileEntitys fileEntitys = new FileEntitys();
                        fileEntitys.setFilesName(fileNames.toString().replaceAll("\\[|\\]", ""));
                        fileEntitys.setFilesLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                        if (file != null) {
                            //修改附件
                            fileEntitys.setId(file.getId());
                            fileEntitys.setUpdateId(user.getId());
                            fileEntitys.setUpdateTime(new Date());
                            fileEntitys.setUpdateName(user.getName());
                            fileEntityMapper.updateByrelevanceIdSelective(fileEntitys);
                        } else {
                            //创建附件
                            fileEntitys.setCreateId(user.getId());
                            fileEntitys.setCreateTime(new Date());
                            fileEntitys.setCreateName(user.getName());
                            fileEntitys.setRelevanceId(articleId);
                            fileEntitys.setType(FilesEnum.ARTICLEDIFFILES.getType());
                            fileEntitys.setState(0);
                            fileEntityMapper.insertSelective(fileEntitys);
                        }
                    }
                    articleService.updateArticle(param);
                    return ResponseData.ok();
                } else {
                    return ResponseData.customerError(1002, "修改失败，只有稿件中的媒介和业务员才能修改稿件！");
                }
            }
        } catch (QinFeiException e) {
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新稿件失败", e);
            return ResponseData.customerError(1002, "更新稿件失败!");
        }
        return ResponseData.customerError(1002, "系统异常!");
    }

    @RequestMapping("/deleteArticle")
    @ResponseBody
    public ResponseData deleteArticle(@RequestParam("artId") Integer id) {
        try {
            Map map = articleService.deleteArticle(id);
            if (map.get("row").equals(0)) {
                return ResponseData.customerError(1001, (String) map.get("message"));
            } else {
                ResponseData da = ResponseData.ok();
                da.putDataValue("message", "操作成功");
                return da;
            }
        } catch (Exception e) {
            log.error("删除稿件失败", e);
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping("/batchDelete")
    @ResponseBody
    public ResponseData batchDelete(@RequestParam("datas") String datas) {
        try {
            Map map = articleService.batchDelete(datas);
            Integer row = (Integer) map.get("row");
            String message = (String) map.get("message");
            if (row == 0) {
                return ResponseData.customerError(1001, message);
            } else {
                ResponseData da = ResponseData.ok();
                da.putDataValue("message", message);
                return da;
            }
        } catch (Exception e) {
            log.error("批量删除稿件失败", e);
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @GetMapping("list/{orderId}")
    @ResponseBody
    public PageInfo<Article> businessList(@PathVariable("orderId") Integer orderId, Pageable pageable) {
        return articleService.listByOrderId(orderId, pageable);
    }

    @PostMapping("upload")
    @ResponseBody
    public ResponseData upload(@RequestParam("id") Integer id, @RequestParam("file") MultipartFile multipartFile) {
        String originalFileName = multipartFile.getOriginalFilename();
        String uploadDir = config.getUploadDir();
        String wordPath = "";
        String fileName = UUIDUtil.get32UUID();
        if (StringUtils.isNotEmpty(originalFileName)) {
            String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
            // fileName = fileName + ext;
            File file = new File(uploadDir + "/words", fileName + ext);
            wordPath = file.getAbsolutePath();
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            try {
                multipartFile.transferTo(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String path = config.getWebDir() + fileName + ext;
            articleService.updatePathById(id, path);
        }
        OutputStreamWriter writer = null;
        FileInputStream in = null;
        try {
            ResponseData ok = ResponseData.ok();
//            String htmlPath = POIUtil.wordToHtml(originalFileName, fileName, uploadDir, wordPath, config.getWebDir());
            String htmlPath = officeUtils.toHtml(fileName, uploadDir, wordPath, config.getWebDir());
            byte[] bytes = FileCopyUtils.copyToByteArray(new File(htmlPath));
            ok.putDataValue("stream", new String(bytes));
            return ok;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ResponseData.customerError(1002, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseData.customerError(1003, e.getMessage());
        } finally {
            try {
                if (in != null)
                    in.close();
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseData.customerError(1004, e.getMessage());
            }

        }
    }

    @GetMapping("loadHtml")
    @ResponseBody
    public ResponseData loadHtml(@RequestParam("htmlPath") String htmlPath) {
        try {
            String uploadDir = config.getUploadDir();
            htmlPath = uploadDir + "htmls" + htmlPath.substring(htmlPath.lastIndexOf("/"), htmlPath.lastIndexOf(".")) + ".html";
            byte[] bytes = FileCopyUtils.copyToByteArray(new File(htmlPath));
            ResponseData ok = ResponseData.ok();
            ok.putDataValue("stream", new String(bytes));
            return ok;
        } catch (Exception e) {
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    /**
     * 根据稿件id拿到请款记录
     * @param articleId 稿件id
     * @return 请款记录
     */
    @RequestMapping("/getFeeOutgo")
    @ResponseBody
    public Map<String, Object> getFeeOutgo(@RequestParam("articleId") Integer articleId) {
        return articleService.getFeeOutgo(articleId);
    }

}
