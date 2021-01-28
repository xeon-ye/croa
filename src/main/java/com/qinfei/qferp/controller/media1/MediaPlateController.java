package com.qinfei.qferp.controller.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.media1.MediaPlate;
import com.qinfei.qferp.service.media1.IMediaForm1Service;
import com.qinfei.qferp.service.media1.IMediaPlateService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.record.ExVideoContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaForm1Controller
 * @Description 媒体板块服务
 * @Author xuxiong
 * @Date 2019/6/26 0026 10:23
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/mediaPlate")
@Api(description = "媒体板块服务")
public class MediaPlateController {
    @Autowired
    private IMediaPlateService mediaPlateService;

    @PostMapping("save")
    @ResponseBody
    @ApiOperation(value = "媒体板块管理", notes = "新增媒体板块", response = ResponseData.class)
    public ResponseData save(@RequestBody MediaPlate mediaPlate) {
        try{
            mediaPlateService.save(mediaPlate);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "添加媒体板块异常，请联系技术人员！");
        }
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value = "媒体板块管理", notes = "修改媒体板块", response = ResponseData.class)
    public ResponseData update(@RequestBody MediaPlate mediaPlate) {
        try{
            mediaPlateService.update(mediaPlate);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "修改媒体板块异常，请联系技术人员！");
        }
    }

    @PostMapping("updateState")
    @ResponseBody
    @ApiOperation(value = "媒体板块管理", notes = "修改媒体板块状态", response = ResponseData.class)
    public ResponseData update(@RequestParam("id") Integer id, @RequestParam("state") Integer state) {
        try{
            mediaPlateService.updateState(id, state);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "修改媒体板块状态异常，请联系技术人员！");
        }
    }
    @PostMapping("mediaAllPlateList")
    @ResponseBody
    public ResponseData mediaAllPlateList(){
        return mediaPlateService.mediaAllPlateList();

    }

    @PostMapping("listPlate")
    @ApiOperation(value = "媒体板块管理", notes = "媒体板块列表", response = ResponseData.class)
    @ResponseBody
    public PageInfo<MediaPlate> listPlate(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return mediaPlateService.listPlate(param, pageable);
    }


    @GetMapping("{plateTypeId}")
    @ResponseBody
    @ApiOperation(value = "根据媒介用户ID获取媒体板块类型列表", notes = "根据媒介用户ID获取媒体板块类型列表", response = ResponseData.class)
    public List<MediaPlate> listByPlateTypeId(@PathVariable("plateTypeId") Integer plateTypeId) {
        return mediaPlateService.listByPlateTypeId(plateTypeId);
    }

    @GetMapping("userId")
    @ResponseBody
    @ApiOperation(value = "根据媒介用户ID获取媒体板块类型列表", notes = "根据媒介用户ID获取媒体板块类型列表", response = ResponseData.class)
    public List<MediaPlate> listMediaPlateByUserId() {
        Integer userId = AppUtil.getUser().getId();
        return mediaPlateService.listMediaPlateByUserId(userId);
    }

    @PostMapping("userId")
    @ResponseBody
    @ApiOperation(value = "根据媒介用户ID获取媒体板块类型列表", notes = "根据媒介用户ID获取媒体板块类型列表", response = ResponseData.class)
    public List<MediaPlate> listMediaPlateByUserId(@RequestParam("userId") Integer userId) {
        return mediaPlateService.listMediaPlateByUserId(userId);
    }


}
