package com.qinfei.qferp.controller.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaForm1Controller
 * @Description 媒体扩展表单服务
 * @Author xuxiong
 * @Date 2019/6/26 0026 10:23
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/mediaForm1")
@Api(description = "媒体扩展表单管理接口1")
public class MediaForm1Controller {
    @Autowired
    private IMediaForm1Service mediaForm1Service;

    @PostMapping
    @ApiOperation(value = "媒体扩展表单新增", notes = "媒体扩展表单新增")
    @Verify(code = "/media1/media_extend_manage", module = "媒体扩展表单管理/新增", action = "1")
    @ResponseBody
    public ResponseData save(@ApiParam("媒体表单实体类")MediaForm1 mediaForm1){
        try{
            saveValidation(mediaForm1);
            mediaForm1Service.save(mediaForm1);
            return ResponseData.ok();
        }catch (QinFeiException qinFeiException){
            return ResponseData.customerError(qinFeiException.getCode(),qinFeiException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002,"媒体表单新增错误！");
        }
    }

    /**
     * 新增数据校验
     * @param mediaForm1
     */
    private void saveValidation(MediaForm1 mediaForm1){
        List<String> mediaCommonFields = Arrays.asList("id","name","no","link","commStart","picPath","platform","price","profitRate","outPrice","plateId","regionId","typeId",
                "contact", "contactor","phone","creatorId","creatorName","createDate","updatedId","userId","discount","supplierId","supplierName","remarks","state","enabled",
                "isCopy","copyRemarks","companyCode","companyCodeName","mediaId","cell","cellName","cellValue","cellValueText","dbType","type","updateDate","isDelete", "versions");
        if(mediaForm1 == null || mediaForm1.getMediaPlateId() == null){
            throw new QinFeiException(1002,"媒体板块不存在！");
        }
        if(StringUtils.isBlank(mediaForm1.getCellCode())){
            throw new QinFeiException(1002,"列名不能为空！");
        }
        if(StringUtils.isBlank(mediaForm1.getCellName())){
            throw new QinFeiException(1002,"中文列名不能为空！");
        }
        if(mediaCommonFields.contains(mediaForm1.getCellCode())){
            throw new QinFeiException(1002,"列名为公共使用列名，请重新命名！");
        }
    }

    @PutMapping
    @ApiOperation(value = "媒体扩展表单修改", notes = "媒体扩展表单修改")
    @Verify(code = "/media1/media_extend_manage", module = "媒体扩展表单管理/修改", action = "2")
    @ResponseBody
    public ResponseData update(@RequestBody MediaForm1 mediaForm1) {
        try {
            Integer id = mediaForm1.getId();
            if (id == null || id == 0) {
                return ResponseData.customerError(1001, "请传入数据");
            }
            saveValidation(mediaForm1);
            mediaForm1Service.update(mediaForm1);
            return ResponseData.ok();
        }catch (QinFeiException qinFeiException){
            return ResponseData.customerError(qinFeiException.getCode(),qinFeiException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体表单修改错误！");
        }
    }

    @DeleteMapping
    @ApiOperation(value = "媒体扩展表单删除", notes = "媒体扩展表单删除")
    @Verify(code = "/media1/media_extend_manage", module = "媒体扩展表单管理/删除", action = "3")
    @ResponseBody
    public ResponseData deleteInBatch(@RequestBody List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return ResponseData.customerError(1001, "请传入数据");
        }
        mediaForm1Service.deleteBatch(ids);
        return ResponseData.ok();
    }

    @GetMapping("list/{mediaPlateId}")
    @ApiOperation(value = "媒体扩展表单分页列表", notes = "媒体扩展表单分页列表")
    @Verify(code = "/media1/media_extend_manage", module = "媒体扩展表单管理/分页列表", action = "4")
    @ResponseBody
    public PageInfo<Map> list(@PathVariable("mediaPlateId") Integer mediaPlateId, @RequestParam Map map, @PageableDefault(page = 1) Pageable pageable){
        map.put("mediaPlateId",mediaPlateId);
        return mediaForm1Service.list(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @GetMapping("{mediaPlateId}")
    @ResponseBody
    public List<MediaForm1> findAllByMediaPlateId(@PathVariable("mediaPlateId") Integer mediaPlateId) {
        List<MediaForm1> mediaForms = mediaForm1Service.findAllByMediaPlateId(mediaPlateId);
        return mediaForms;
    }

    @GetMapping("listPriceTypeByPlateId/{mediaPlateId}")
    @ResponseBody
    public ResponseData listPriceTypeByPlateId(@PathVariable("mediaPlateId") Integer mediaPlateId){
        return ResponseData.ok().putDataValue("mediaPrices", mediaForm1Service.listPriceTypeByPlateId(mediaPlateId));
    }
}
