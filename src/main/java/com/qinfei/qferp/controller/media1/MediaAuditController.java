package com.qinfei.qferp.controller.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.biz.ArticleReplace;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.qinfei.qferp.entity.media1.MediaChange;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.media1.MediaSupplierChange;
import com.qinfei.qferp.service.media1.IMediaAuditService;
import com.qinfei.qferp.utils.DataImportUtil;
import com.qinfei.qferp.utils.FileUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @CalssName MediaAuditController
 * @Description 媒体审核表接口
 * @Author xuxiong
 * @Date 2019/6/26 0026 17:14
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/mediaAudit")
@Api(description = "媒体审核接口")
public class MediaAuditController {
    @Autowired
    private IMediaAuditService mediaAuditService;

    @Autowired
    private Config config;

    @PostMapping("validationMediaName")
    @ApiOperation(value = "媒体名称校验", notes = "媒体名称校验")
    @Verify(code = "/media1/mediaAdd", module = "媒体新增/媒体名称校验", action = "4")
    @ResponseBody
    public ResponseData validationMediaName(@RequestParam("id") Integer id, @RequestParam("name") String name, @RequestParam("plateId") Integer plateId) {
        try {
            if (StringUtils.isEmpty(name)) {
                return ResponseData.ok();
            } else {
                mediaAuditService.validationMediaName(id, name.toLowerCase(Locale.US), plateId);
                return ResponseData.ok();
            }
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体名称校验错误！");
        }
    }

    @PostMapping("validationMediaContentId")
    @ApiOperation(value = "媒体唯一标识校验", notes = "媒体唯一标识校验")
    @Verify(code = "/media1/mediaAdd", module = "媒体新增/媒体唯一标识校验", action = "4")
    @ResponseBody
    public ResponseData validationMediaContentId(@RequestParam("id") Integer id, @RequestParam("mediaContentId") String mediaContentId, @RequestParam("plateId") Integer plateId) {
        try {
            if (StringUtils.isEmpty(mediaContentId)) {
                return ResponseData.ok();
            } else {
                mediaAuditService.validationMediaContentId(id, mediaContentId.toLowerCase(Locale.US), plateId);
                return ResponseData.ok();
            }
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体唯一标识校验错误！");
        }
    }

    @PostMapping("save")
    @ApiOperation(value = "媒体新增", notes = "媒体新增")
    @Verify(code = "/media1/mediaAdd", module = "媒体新增/新增媒体", action = "1")
    @ResponseBody
    public ResponseData save(@RequestBody MediaAudit mediaAudit) {
        try {
            mediaAuditService.save(mediaAudit);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "新增媒体错误！");
        }
    }

    @PostMapping("update")
    @ApiOperation(value = "媒体更新", notes = "媒体更新")
    @Verify(code = "/media1/mediaEdit", module = "媒体管理/更新媒体信息", action = "2")
    @ResponseBody
    public ResponseData update(@RequestBody MediaAudit mediaAudit) {
        if (mediaAudit == null || mediaAudit.getId() == null) {
            return ResponseData.customerError(1002, "请求参数错误！");
        }
        try {
            mediaAuditService.update(mediaAudit);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体编辑错误！");
        }
    }

    @GetMapping("listAuditMedia")
    @ApiOperation(value = "媒体审核列表", notes = "分页查询媒体未审核成功的媒体列表")
    @Verify(code = "/media1/audits", module = "媒体审核/媒体审核列表", action = "4")
    @ResponseBody
    public PageInfo<MediaAudit> listAuditMedia(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable) {
        return mediaAuditService.listAuditMedia(map, pageable);
    }

    @GetMapping("listAuditMediaSupplier")
    @ApiOperation(value = "媒体供应商关系审核列表", notes = "分页查询媒体未审核成功的媒体供应商关系列表")
    @Verify(code = "/media1/audits", module = "媒体审核/媒体审核列表", action = "4")
    @ResponseBody
    public PageInfo<MediaAudit> listAuditMediaSupplier(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable) {
        return mediaAuditService.listAuditMediaSupplier(map, pageable);
    }

    @GetMapping("listMedia")
    @ApiOperation(value = "媒体管理", notes = "分页查询媒体列表")
    @Verify(code = "/media1/mediaList", module = "媒体管理/分页查询媒体列表", action = "4")
    @ResponseBody
    public PageInfo<MediaAudit> listMedia(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable) {
        return mediaAuditService.listMedia(map, pageable);
    }

    @GetMapping("listMediaSupplier")
    @ApiOperation(value = "媒体供应商关系列表", notes = "分页查询媒体关系列表")
    @Verify(code = "/media1/mediaList", module = "媒体关系管理/媒体关系列表", action = "4")
    @ResponseBody
    public PageInfo<MediaAudit> listMediaSupplier(@RequestParam Map map, @PageableDefault(size = 10) Pageable pageable) {
        return mediaAuditService.listMediaSupplier(map, pageable);
    }

    @PostMapping("getEditMediaById")
    @ApiOperation(value = "媒体信息", notes = "根据媒体ID获取媒体信息")
    @Verify(code = "/media1/mediaEdit", module = "媒体更新/根据媒体ID获取媒体信息", action = "2")
    @ResponseBody
    public ResponseData getEditMediaById(@RequestParam(value = "id", required = true) Integer id) {
        try {
            return ResponseData.ok().putDataValue("media", mediaAuditService.getEditMediaById(id));
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "获取媒体信息错误！");
        }
    }

    @GetMapping("getMediaSupplierInfoByMediaId/{mediaId}")
    @ApiOperation(value = "媒体供应商价格", notes = "根据媒体ID获取媒体供应商价格信息")
    @ResponseBody
    public ResponseData getMediaSupplierInfoByMediaId(@PathVariable("mediaId") Integer mediaId) {
        try {
            return ResponseData.ok().putDataValue("media", mediaAuditService.getMediaSupplierInfoByMediaId(mediaId));
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "获取媒体供应商价格错误!");
        }
    }

    @GetMapping("getMediaNumbers/{auditPageFlag}")
    @ApiOperation(value = "媒体数量", notes = "根据媒体板块ID该板块下媒体数量")
    @ResponseBody
    public ResponseData getMediaNumbers(@PathVariable("auditPageFlag") Integer auditPageFlag) {
        return ResponseData.ok().putDataValue("data", mediaAuditService.getMediaNumbers(auditPageFlag));
    }

    ;

    @GetMapping("getMediaSupplierNumbers/{auditPageFlag}")
    @ApiOperation(value = "媒体关系数量", notes = "根据媒体板块ID该板块下媒体关系数量")
    @ResponseBody
    public ResponseData getMediaSupplierNumbers(@PathVariable("auditPageFlag") Integer auditPageFlag) {
        return ResponseData.ok().putDataValue("data", mediaAuditService.getMediaSupplierNumbers(auditPageFlag));
    }

    ;

    @GetMapping("pass/{id}")
    @ApiOperation(value = "媒体审核通过", notes = "媒体审核通过")
    @Verify(code = "/media/pass/{id}", module = "媒体管理/审核通过", action = "2")
    @ResponseBody
    public ResponseData pass(@PathVariable("id") Integer id) {
        try {
            mediaAuditService.pass(id);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体审核错误！");
        }
    }

    @GetMapping("passRelate")
    @ApiOperation(value = "媒体关系审核通过", notes = "媒体关系审核通过")
    @Verify(code = "/media/pass/{id}", module = "媒体关系管理/审核通过", action = "2")
    @ResponseBody
    public ResponseData passRelate(@RequestParam("mediaId") Integer mediaId, @RequestParam("relateId") Integer relateId) {
        try {
            mediaAuditService.passRelate(mediaId, relateId);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体关系审核错误！");
        }
    }

    @GetMapping("copy/{id}")
    @ApiOperation(value = "媒体拷贝", notes = "媒体拷贝（会拷贝所有，但是不会去覆盖，暂时不用）")
    @Verify(code = "/media/copy", module = "媒体管理/媒体拷贝", action = "1")
    @ResponseBody
    public ResponseData copy(@PathVariable("id") Integer id) {
        try {
            mediaAuditService.copy(id);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体拷贝错误！");
        }
    }

    @GetMapping("copyMedia/{id}")
    @ApiOperation(value = "媒体拷贝", notes = "仅媒体拷贝")
    @Verify(code = "/media/copy", module = "媒体管理/媒体拷贝", action = "1")
    @ResponseBody
    public ResponseData copyMedia(@PathVariable("id") Integer id) {
        try {
            mediaAuditService.copyMedia(id);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体拷贝错误！");
        }
    }

    @GetMapping("copyMediaRelate/{id}")
    @ApiOperation(value = "媒体拷贝", notes = "拷贝媒体供应商关系")
    @Verify(code = "/media/copy", module = "媒体管理/拷贝媒体供应商关系", action = "1")
    @ResponseBody
    public ResponseData copyMediaRelate(@PathVariable("id") Integer id) {
        try {
            mediaAuditService.copyMediaRelate(id);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体供应商价格拷贝错误！");
        }
    }

    @GetMapping("stop/{id}/{standarPlatformFlag}")
    @ApiOperation(value = "媒体停用", notes = "媒体停用")
    @Verify(code = "/media1/mediaList", module = "媒体管理/媒体停用", action = "2")
    @ResponseBody
    public ResponseData stop(@PathVariable("id") Integer id, @PathVariable("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.stop(id, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体停用错误！");
        }
    }

    @GetMapping("stopRelate")
    @ApiOperation(value = "媒体关系停用", notes = "媒体关系停用")
    @Verify(code = "/media1/mediaList", module = "媒体关系管理/媒体关系停用", action = "2")
    @ResponseBody
    public ResponseData stopRelate(@RequestParam("mediaId") Integer mediaId, @RequestParam("relateId") Integer relateId, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.stopRelate(mediaId, relateId, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体关系停用错误！");
        }
    }

    @GetMapping("active/{id}/{standarPlatformFlag}")
    @ApiOperation(value = "媒体启用", notes = "媒体管理")
    @Verify(code = "/media1/mediaList", module = "媒体管理/媒体启用", action = "2")
    @ResponseBody
    public ResponseData active(@PathVariable("id") Integer id, @PathVariable("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.active(id, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体启用错误！");
        }
    }

    @GetMapping("activeRelate")
    @ApiOperation(value = "媒体关系启用", notes = "媒体关系启用")
    @Verify(code = "/media1/mediaList", module = "媒体关系管理/媒体关系启用", action = "2")
    @ResponseBody
    public ResponseData activeRelate(@RequestParam("mediaId") Integer mediaId, @RequestParam("relateId") Integer relateId, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.activeRelate(mediaId, relateId, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体关系启用错误！");
        }
    }

    @GetMapping("reject/{id}")
    @ApiOperation(value = "媒体审核驳回", notes = "媒体审核驳回")
    @Verify(code = "/media/reject/{id}", module = "媒体管理/审核驳回", action = "2")
    @ResponseBody
    public ResponseData reject(@PathVariable("id") Integer id) {
        try {
            mediaAuditService.reject(id);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体审核驳回错误！");
        }
    }

    @GetMapping("rejectRelate")
    @ApiOperation(value = "媒体关系审核驳回", notes = "媒体关系审核驳回")
    @Verify(code = "/media/reject/{id}", module = "媒体关系审核/审核驳回", action = "2")
    @ResponseBody
    public ResponseData rejectRelate(@RequestParam("mediaId") Integer mediaId, @RequestParam("relateId") Integer relateId) {
        try {
            mediaAuditService.rejectRelate(mediaId, relateId);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体关系审核驳回错误！");
        }
    }

    @GetMapping("delete/{id}/{standarPlatformFlag}")
    @ApiOperation(value = "媒体审核删除", notes = "媒体审核删除")
    @Verify(code = "/media/del/{id}", module = "媒体管理/媒体删除", action = "3")
    @ResponseBody
    public ResponseData deleteMedia(@PathVariable("id") Integer id, @PathVariable("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.deleteMedia(id, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException be) {
            return ResponseData.customerError(be.getCode(), be.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体删除错误！");
        }
    }

    @GetMapping("deleteRelate")
    @ApiOperation(value = "媒体关系删除", notes = "媒体关系删除")
    @Verify(code = "/media/del/{id}", module = "媒体关系管理/媒体关系删除", action = "3")
    @ResponseBody
    public ResponseData deleteRelate(@RequestParam("mediaId") Integer mediaId, @RequestParam("supplierId") Integer supplierId, @RequestParam("relateId") Integer relateId, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.deleteMediaRelate(mediaId, supplierId, relateId, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException be) {
            return ResponseData.customerError(be.getCode(), be.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体关系删除错误！");
        }
    }

    @GetMapping("passBatch")
    @ApiOperation(value = "媒体批量审核通过", notes = "媒体批量审核通过")
    @ResponseBody
    @Verify(code = "/media/passBatch", module = "媒体审核/媒体批量通过", action = "2")
    public ResponseData passBatch(@RequestParam(value = "ids[]") List<Integer> ids, @RequestParam(value = "mediaNames[]") List<String> mediaNames, @RequestParam(value = "userIds[]") List<Integer> userIds) {
        try {
            mediaAuditService.passBatch(ids, mediaNames, userIds);
            return ResponseData.ok();
        } catch (QinFeiException be) {
            return ResponseData.customerError(be.getCode(), be.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体批量审核通过错误！");
        }
    }

    @GetMapping("passBatchRelate")
    @ApiOperation(value = "媒体关系批量审核通过", notes = "媒体关系批量审核通过")
    @ResponseBody
    @Verify(code = "/media/passBatch", module = "媒体审核/媒体批量通过", action = "2")
    public ResponseData passBatchRelate(@RequestParam(value = "mediaIds[]") List<Integer> mediaIds, @RequestParam(value = "mediaNames[]") List<String> mediaNames,
                                        @RequestParam(value = "userIds[]") List<Integer> userIds, @RequestParam(value = "relateIds[]") List<Integer> relateIds) {
        try {
            mediaAuditService.passBatchRelate(mediaIds, mediaNames, userIds, relateIds);
            return ResponseData.ok();
        } catch (QinFeiException be) {
            return ResponseData.customerError(be.getCode(), be.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体关系批量审核通过错误！");
        }
    }

    @GetMapping("rejectBatch")
    @ApiOperation(value = "媒体批量审核驳回", notes = "媒体批量审核驳回")
    @ResponseBody
    @Verify(code = "/media/rejectBatch", module = "媒体审核/媒体批量驳回", action = "2")
    public ResponseData rejectBatch(@RequestParam(value = "ids[]") List<Integer> ids, @RequestParam(value = "mediaNames[]") List<String> mediaNames, @RequestParam(value = "userIds[]") List<Integer> userIds) {
        try {
            mediaAuditService.rejectBatch(ids, mediaNames, userIds);
            return ResponseData.ok();
        } catch (QinFeiException be) {
            return ResponseData.customerError(be.getCode(), be.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体审核驳回错误！");
        }
    }

    @GetMapping("rejectBatchRelate")
    @ApiOperation(value = "媒体关系批量审核驳回", notes = "媒体关系批量审核驳回")
    @ResponseBody
    @Verify(code = "/media/rejectBatch", module = "媒体关系审核/媒体关系批量驳回", action = "2")
    public ResponseData rejectBatchRelate(@RequestParam(value = "mediaIds[]") List<Integer> mediaIds, @RequestParam(value = "mediaNames[]") List<String> mediaNames,
                                          @RequestParam(value = "userIds[]") List<Integer> userIds, @RequestParam(value = "relateIds[]") List<Integer> relateIds) {
        try {
            mediaAuditService.rejectBatchRelate(mediaIds, mediaNames, userIds, relateIds);
            return ResponseData.ok();
        } catch (QinFeiException be) {
            return ResponseData.customerError(be.getCode(), be.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体关系审核驳回错误！");
        }
    }

    @GetMapping("deleteBatch")
    @ApiOperation(value = "媒体批量删除", notes = "媒体批量删除")
    @ResponseBody
    @Verify(code = "/media/batchDelete", module = "媒体管理/媒体批量删除", action = "3")
    public ResponseData deleteBatch(@RequestParam(value = "ids[]") List<Integer> ids, @RequestParam(value = "mediaNames[]") List<String> mediaNames, @RequestParam(value = "userIds[]") List<Integer> userIds, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.deleteBatch(ids, mediaNames, userIds, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体批量删除出错！");
        }
    }

    @GetMapping("deleteBatchRelate")
    @ApiOperation(value = "媒体关系批量删除", notes = "媒体关系批量删除")
    @ResponseBody
    @Verify(code = "/media/batchDelete", module = "媒体关系管理/媒体关系批量删除", action = "3")
    public ResponseData deleteBatchRelate(@RequestParam(value = "mediaIds[]") List<Integer> mediaIds, @RequestParam(value = "mediaNames[]") List<String> mediaNames,
                                          @RequestParam(value = "userIds[]") List<Integer> userIds, @RequestParam(value = "mediaSupplierRelates[]") List<String> mediaSupplierRelates,
                                          @RequestParam(value = "relateIds[]") List<Integer> relateIds, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.deleteBatchRelate(mediaIds, mediaNames, userIds, mediaSupplierRelates, relateIds, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体关系批量删除出错！");
        }
    }

    /**
     * 获取指定板块的数据导入模板；
     *
     * @param request：请求对象；
     * @param response：响应对象；
     * @param plateId：媒体板块类型；
     * @param fileName：媒体板块类型名称；
     */
    @RequestMapping("exportTemplate")
    @ApiOperation(value = "媒体导入模板下载", notes = "获取指定板块媒体导入模板")
    @Verify(code = "/media1/mediaAdd", module = "新增媒体/获取指定板块媒体导入模板", action = "4")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, note = "下载模板", module = "媒体管理/下载模板")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag, @RequestParam("plateId") Integer plateId, @RequestParam String fileName) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = fileName + "批量导入模板";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            mediaAuditService.getDataImportTemplate(standarPlatformFlag, plateId, fileName, sheetName, outputStream);

        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }

    /**
     * 获取指定板块的数据导入模板；
     *
     * @param request：请求对象；
     * @param response：响应对象；
     */
    @GetMapping("batchExport")
    @ApiOperation(value = "媒体导出", notes = "根据条件导出所有媒体")
    @Verify(code = "/media1/export", module = "媒体管理/媒体导出", action = "4")
    @ResponseBody
    public void batchExport(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = map.get("plateName") + "导出媒体信息";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            mediaAuditService.batchExport(outputStream, map);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }

    /**
     * 获取指定板块的数据导入模板；
     *
     * @param request：请求对象；
     * @param response：响应对象；
     */
    @GetMapping("batchChooseMediaExport")
    @ApiOperation(value = "媒体导出", notes = "根据条件导出选择的媒体")
    @Verify(code = "/media1/export", module = "媒体管理/媒体导出", action = "4")
    @ResponseBody
    public void batchChooseMediaExport(HttpServletRequest request, HttpServletResponse response, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag, @RequestParam("plateId") Integer plateId, @RequestParam("plateName") String plateName, @RequestParam("mediaIds[]") List<Integer> mediaIds) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = plateName + "导出媒体信息";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            mediaAuditService.batchChooseExport(outputStream, standarPlatformFlag, plateId, plateName, mediaIds, null);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }

    /**
     * 获取指定板块的数据导入模板；
     *
     * @param request：请求对象；
     * @param response：响应对象；
     */
    @GetMapping("batchChooseRelateExport")
    @ApiOperation(value = "媒体导出", notes = "根据条件导出选择的媒体")
    @Verify(code = "/media1/export", module = "媒体管理/媒体导出", action = "4")
    @ResponseBody
    public void batchChooseRelateExport(HttpServletRequest request, HttpServletResponse response, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag, @RequestParam("plateId") Integer plateId, @RequestParam("plateName") String plateName, @RequestParam("mediaIds[]") List<Integer> mediaIds,
                                        @RequestParam("relateIds[]") List<String> relateIds) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = plateName + "导出媒体信息";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            mediaAuditService.batchChooseExport(outputStream, standarPlatformFlag, plateId, plateName, mediaIds, relateIds);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }

    /**
     * 媒体批量导入
     *
     * @param filePath  导入文件路径
     * @param plateId   媒体板块ID
     * @param plateName 媒体板块名称
     */
    @GetMapping("importMedia")
    @ApiOperation(value = "媒体批量导入", notes = "批量导入指定板块的媒体")
    @Verify(code = "/media1/mediaAdd", module = "新增媒体/媒体批量导入", action = "1")
    @ResponseBody
    public ResponseData importExcel(@RequestParam("filePath") String filePath, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag, @RequestParam("plateId") int plateId, @RequestParam("plateName") String plateName) {
        String fileName = filePath.replaceAll("\\\\", "/");//linux系统读取/斜杠
        try {
            ResponseData data = ResponseData.ok();
            String result = mediaAuditService.batchAddMedia(fileName, standarPlatformFlag, plateId, plateName);
            if (StringUtils.isNotEmpty(result)) {
                if ("0".equals(result)) {
                    data.putDataValue("message", "没有数据导入成功，请确认导入板块与模板对应，且内容符合要求，并检查数据是否已存在。");
                } else {
                    data.putDataValue("message", "导入失败，请点击红色提示内容下载文件检查内容是否符合要求。");
                    data.putDataValue("file", result);
                }
            }
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            return data;
        } catch (QinFeiException e) {
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (IOException e) {
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "导入文件解析异常！");
        } catch (Exception e) {
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "很抱歉，批量导入出错啦！");
        }
    }

    /**
     * 媒体批量导入
     *
     * @param multipartFile 文件
     * @param plateId       媒体板块ID
     * @param plateName     媒体板块名称
     * @param fileType      导入文件类型： 1-供应商、2-媒体、3-供应商价格
     */
    @PostMapping("importMedia1")
    @Verify(code = "/media1/mediaAdd", module = "媒体管理/媒体批量导入", action = "1")
    @ApiOperation(value = "媒体批量导入", notes = "批量导入指定板块的媒体相关信息")
    @ResponseBody
    public ResponseData importExcel1(@RequestParam(value = "file") MultipartFile multipartFile, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag, @RequestParam("plateId") int plateId, @RequestParam("plateName") String plateName, @RequestParam("fileType") Integer fileType) {
        String filePath = null;
        try {
            File destFile = new File("");
            if (multipartFile.getSize() > 0) {
                String temp = multipartFile.getOriginalFilename();
                String ext = null;
                if (temp.indexOf(".") > -1) {
                    ext = temp.substring(temp.lastIndexOf("."));
                }
                String fileName = UUIDUtil.get32UUID() + ext;
                filePath = config.getUploadDir() + "media1/" + fileName;
                destFile = new File(filePath);
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                multipartFile.transferTo(destFile);
            }
            mediaAuditService.batchAddMedia1(destFile, standarPlatformFlag, plateId, plateName, fileType);
            FileUtil.delFiles(Arrays.asList(filePath)); //文件删除操作
            return ResponseData.ok();
        } catch (QinFeiException e) {
            FileUtil.delFiles(Arrays.asList(filePath)); //文件删除操作
            if (e.getCode() == 1003) {  //1003：message值为文件下载路径
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "导入失败，请点击红色提示内容下载文件检查内容是否符合要求。");
                data.putDataValue("file", e.getMessage());
                return data;
            } else {
                return ResponseData.customerError(e.getCode(), e.getMessage());
            }
        } catch (IOException e) {
            FileUtil.delFiles(Arrays.asList(filePath)); //文件删除操作
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "导入文件解析异常！");
        } catch (DuplicateKeyException e) {
            return ResponseData.customerError(1002, "供应商模板中存在相同的供应商公司名称和手机号！");
        } catch (Exception e) {
            FileUtil.delFiles(Arrays.asList(filePath)); //文件删除操作
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "很抱歉，批量导入出错啦！");
        }
    }

    /**
     * 媒体批量导入替换
     *
     * @param filePath  导入文件路径
     * @param plateId   媒体板块ID
     * @param plateName 媒体板块名称
     * @param fileType  导入文件类型： 1-供应商、2-媒体、3-供应商价格
     */
    @GetMapping("importReplace")
    @ApiOperation(value = "媒体批量导入替换", notes = "批量导入替换指定板块的媒体相关信息")
    @Verify(code = "/media1/importReplace", module = "媒体管理/媒体替换", action = "1")
    @ResponseBody
    public ResponseData importReplace(@RequestParam("filePath") String filePath, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag, @RequestParam("plateId") int plateId, @RequestParam("plateName") String plateName, @RequestParam("fileType") Integer fileType) {
        String fileName = filePath.replaceAll("\\\\", "/");//linux系统读取/斜杠
        try {
            mediaAuditService.batchReplaceImport(fileName, standarPlatformFlag, plateId, plateName, fileType);
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            return ResponseData.ok();
        } catch (QinFeiException e) {
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            if (e.getCode() == 1003) {  //1003：message值为文件下载路径
                ResponseData data = ResponseData.ok();
                data.putDataValue("message", "导入失败，请点击红色提示内容下载文件检查内容是否符合要求。");
                data.putDataValue("file", e.getMessage());
                return data;
            } else {
                return ResponseData.customerError(e.getCode(), e.getMessage());
            }
        } catch (IOException e) {
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "导入文件解析异常！");
        } catch (Exception e) {
            FileUtil.delFiles(Arrays.asList(fileName)); //文件删除操作
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "很抱歉，批量导入出错啦！");
        }
    }

    @PostMapping("listMediaField/{plateId}")
    @ResponseBody
    public List<MediaForm1> listMediaField(@PathVariable("plateId") Integer plateId) {
        return mediaAuditService.listMediaField(plateId);
    }


    @PostMapping("getMediaChangePermission")
    @ApiOperation(value = "媒体资源", notes = "获取媒体异动访问权限")
    @Verify(code = "/media1/mediaChangeView", module = "媒体资源/获取媒体异动访问权限", action = "1")
    @ResponseBody
    public ResponseData getMediaChangePermission() {
        return ResponseData.ok();
    }

    @PostMapping("listMediaChange")
    @ApiOperation(value = "媒体管理", notes = "媒体异动详情")
    @ResponseBody
    public List<MediaChange> listMediaChange(@RequestParam String mediaIds) {
        return mediaAuditService.listMediaChange(mediaIds);
    }

    @PostMapping("mediaChangeRecover")
    @ApiOperation(value = "媒体管理", notes = "媒体异动恢复")
    @ResponseBody
    public ResponseData mediaChangeRecover(@RequestParam("id") Integer id, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.mediaChangeRecover(id, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002, "异动恢复异常！");
        }
    }

    @PostMapping("listMediaSupplierChange")
    @ApiOperation(value = "媒体管理", notes = "媒体供应商异动详情")
    @ResponseBody
    public List<MediaSupplierChange> listMediaSupplierChange(@RequestParam String relateIds) {
        return mediaAuditService.listMediaSupplierChange(relateIds);
    }

    @PostMapping("mediaSupplierChangeRecover")
    @ApiOperation(value = "媒体管理", notes = "媒体供应商异动恢复")
    @ResponseBody
    public ResponseData mediaSupplierChangeRecover(@RequestParam("id") Integer id, @RequestParam("standarPlatformFlag") Integer standarPlatformFlag) {
        try {
            mediaAuditService.mediaSupplierChangeRecover(id, standarPlatformFlag);
            return ResponseData.ok();
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002, "异动恢复异常！");
        }
    }

    /**
     * 获取指定板块的数据导入模板；
     *
     * @param request：请求对象；
     * @param response：响应对象；
     */
    @GetMapping("batchChangeChooseExport")
    @ApiOperation(value = "媒体导出", notes = "根据条件导出选择的媒体")
    @ResponseBody
    public void batchChangeChooseExport(HttpServletRequest request, HttpServletResponse response, @RequestParam("plateName") String plateName,
                                        @RequestParam("mediaIds[]") List<Integer> mediaIds) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = plateName + "导出媒体异动信息";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            mediaAuditService.batchChangeChooseExport(outputStream, plateName, mediaIds);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }

    /**
     * 获取指定板块的数据导入模板；
     *
     * @param request：请求对象；
     * @param response：响应对象；
     */
    @GetMapping("mediaChangeBatchExport")
    @ApiOperation(value = "媒体异动导出", notes = "根据条件导出所有媒体异动")
    @ResponseBody
    public void mediaChangeBatchExport(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = map.get("plateName") + "导出媒体异动信息";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            mediaAuditService.mediaChangeBatchExport(outputStream, map);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }

    @PostMapping("updateMediaUserId")
    @ApiOperation(value = "媒体管理", notes = "媒体责任人指派")
    @ResponseBody
    public ResponseData updateMediaUserId(@RequestParam("id") Integer id, @RequestParam("userId") Integer userId) {
        try {
            mediaAuditService.updateMediaUserId(id, userId);
            return ResponseData.ok();
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002, "媒体责任人指派异常！");
        }
    }

    @PostMapping("listHistoryArtByParam")
    @ApiOperation(value = "媒体管理", notes = "分页查询历史稿件列表")
    @Verify(code = "/media1/mediaList", module = "媒体管理/分页查询历史稿件列表", action = "4")
    @ResponseBody
    public PageInfo<Map<String, Object>> listHistoryArtByParam(@RequestParam("mediaTypeId") Integer mediaTypeId, @RequestParam(name = "keyword", required = false) String keyword, @PageableDefault(size = 10) Pageable pageable) {
        return mediaAuditService.listHistoryArtByParam(mediaTypeId, keyword, pageable);
    }

    @PostMapping("artMediaSupplierReplace")
    @ApiOperation(value = "媒体管理", notes = "历史稿件媒体供应商替换")
    @Verify(code = "/media1/mediaList", module = "媒体管理/历史稿件媒体供应商替换", action = "4")
    @ResponseBody
    public ResponseData artMediaSupplierReplace(@RequestBody ArticleReplace articleReplace) {
        try {
            mediaAuditService.artMediaSupplierReplace(articleReplace);
            return ResponseData.ok();
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("【历史稿件媒体供应商替换】替换异常：{}", e.getMessage());
            return ResponseData.customerError(1002, "历史稿件媒体供应商替换异常！");
        }
    }

    @RequestMapping("updateMediaArt")
    @ResponseBody
    public void updateMediaArt() {
//        Long start = System.currentTimeMillis();
        mediaAuditService.updateArticle();
//        Long end = System.currentTimeMillis();
//        System.out.println("**************更新主从媒体任务总耗时："+(end-start)+"ms");

    }

    /**
     * 媒体审核表数据迁移
     * @return
     */
  /*  @GetMapping("transfer")
    @ResponseBody
    public ResponseData transfer(){
        try{
            return ResponseData.ok().putDataValue("result",mediaAuditService.transfer());
        }catch (QinFeiException e){
            e.printStackTrace();
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002,"数据迁移错误！");
        }
    }*/

}
