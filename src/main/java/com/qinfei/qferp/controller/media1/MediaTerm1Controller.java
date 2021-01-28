package com.qinfei.qferp.controller.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media.MediaTerm;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.media1.MediaTerm1;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.qinfei.qferp.service.media1.IMediaTerm1Service;
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
 * @CalssName MediaTerm1Controller
 * @Description 媒体查询条件表服务1
 * @Author xuxiong
 * @Date 2019/6/26 0026 10:23
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/mediaTerm1")
@Api(description = "媒体查询条件表服务1")
public class MediaTerm1Controller {
    @Autowired
    private IMediaTerm1Service mediaTerm1Service;

    @PostMapping
    @ApiOperation(value = "媒体查询条件增加", notes = "媒体查询条件增加")
    @Verify(code = "/media1/media_extend_manage", module = "媒体搜索条件管理/添加条件", action = "1")
    @ResponseBody
    public ResponseData save(@ApiParam("媒体查询条件实体类")MediaTerm1 mediaTerm1){
        try{
            saveValidation(mediaTerm1);
            mediaTerm1Service.save(mediaTerm1);
            return ResponseData.ok();
        }catch (QinFeiException qinFeiException){
            return ResponseData.customerError(qinFeiException.getCode(),qinFeiException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体查询条件新增错误！");
        }
    }

    /**
     * 新增数据校验
     * @param mediaTerm1
     */
    private void saveValidation(MediaTerm1 mediaTerm1){
        List<String> mediaCommonFields = Arrays.asList("id","name","no","link","commStart","picPath","platform","price","profitRate","outPrice","plateId","regionId","typeId",
                "contact", "contactor","phone","creatorId","creatorName","createDate","updatedId","userId","discount","supplierId","supplierName","remarks","state","enabled",
                "isCopy","copyRemarks","companyCode","companyCodeName","mediaId","cell","cellName","cellValue","cellValueText","dbType","type","updateDate","isDelete", "versions");
        if(mediaTerm1 == null || mediaTerm1.getMediaPlateId() == null){
            throw new QinFeiException(1002,"媒体板块不存在！");
        }
        if(StringUtils.isBlank(mediaTerm1.getCell())){
            throw new QinFeiException(1002,"列名不能为空！");
        }
        if(StringUtils.isBlank(mediaTerm1.getCellName())){
            throw new QinFeiException(1002,"中文列名不能为空！");
        }
        if(mediaCommonFields.contains(mediaTerm1.getCell())){
            throw new QinFeiException(1002,"列名为公共使用列名，请重新命名！");
        }
    }

    @PutMapping
    @ApiOperation(value = "媒体查询条件修改", notes = "媒体查询条件修改")
    @Verify(code = "/media1/media_extend_manage", module = "媒体搜索条件管理/修改条件", action = "2")
    @ResponseBody
    public ResponseData update(@RequestBody MediaTerm1 mediaTerm1) {
        try {
            Integer id = mediaTerm1.getId();
            if (id == null || id == 0) {
                return ResponseData.customerError(1001, "请传入数据");
            }
            saveValidation(mediaTerm1);
            mediaTerm1Service.update(mediaTerm1);
            return ResponseData.ok();
        }catch (QinFeiException qinFeiException){
            return ResponseData.customerError(qinFeiException.getCode(),qinFeiException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseData.customerError(1002, "媒体查询条件修改错误！");
        }
    }

    @DeleteMapping
    @ApiOperation(value = "媒体查询条件删除", notes = "媒体查询条件删除")
    @Verify(code = "/media1/media_extend_manage", module = "媒体搜索条件管理/删除条件", action = "3")
    @ResponseBody
    public ResponseData deleteInBatch(@RequestBody List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return ResponseData.customerError(1001, "请传入数据");
        }
        mediaTerm1Service.deleteBatch(ids);
        return ResponseData.ok();
    }

    @GetMapping("list/{mediaPlateId}")
    @ApiOperation(value = "媒体查询条件分页查询", notes = "媒体查询条件分页查询")
    @Verify(code = "/media1/media_extend_manage", module = "媒体搜索条件管理/分页列表", action = "4")
    @ResponseBody
    public PageInfo<Map> list(@PathVariable("mediaPlateId") Integer mediaPlateId, @RequestParam Map map, @PageableDefault(page = 1) Pageable pageable){
        map.put("mediaPlateId",mediaPlateId);
        return mediaTerm1Service.list(map,pageable.getPageNumber(),pageable.getPageSize());
    }

    @GetMapping("{mediaPlateId}")
    @ResponseBody
    public List<MediaTerm1> findAllByMediaPlateId(@PathVariable("mediaPlateId") Integer mediaPlateId) {
        List<MediaTerm1> mediaTerms = mediaTerm1Service.findAllByMediaPlateId(mediaPlateId);
        return mediaTerms;
    }
}
