package com.qinfei.qferp.controller.media;

import com.qinfei.qferp.entity.media.MediaForm;
import com.qinfei.qferp.service.media.IMediaFormService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 媒体表单信息
 */
@Controller
@RequestMapping("mediaForm")
class MediaFormController {

    @Autowired
    IMediaFormService mediaFormService;

    @GetMapping("all")
    @ResponseBody
    @ApiOperation(value = "查询所有媒体表单信息", notes = "查询所有媒体表单信息", response = List.class)
//    @Log(opType = OperateType.QUERY, note = "查询所有媒体表单信息", module = "媒体管理/查询所有媒体表单信息")
//    @Verify(code = "/mediaForm/all", action = "查询所有媒体表单信息", module = "媒体管理/查询所有媒体表单信息")
    public List<MediaForm> all() {
        return mediaFormService.all();
    }

    @GetMapping("list")
    @ResponseBody
    @ApiOperation(value = "根据媒体表单信息查询媒体表单信息列表", notes = "根据媒体表单信息查询媒体表单信息列表", response = List.class)
//    @Log(opType = OperateType.QUERY, note = "根据媒体表单信息查询媒体表单信息列表", module = "媒体管理/根据媒体表单信息查询媒体表单信息列表")
//    @Verify(code = "/mediaForm/list", action = "根据媒体表单信息查询媒体表单信息列表", module = "媒体管理/根据媒体表单信息查询媒体表单信息列表")
    public List<MediaForm> list(MediaForm mediaForm) {
        return mediaFormService.list(mediaForm);
    }

    @GetMapping("list/{mediaTypeId}")
    @ResponseBody
    @ApiOperation(value = "媒体类型查询媒体表单信息", notes = "媒体类型查询媒体表单信息", response = List.class)
//    @Log(opType = OperateType.QUERY, note = "媒体类型查询媒体表单信息", module = "媒体管理/媒体类型查询媒体表单信息")
//    @Verify(code = "/mediaForm/list/{mediaTypeId}", action = " 媒体类型查询媒体表单信息", module = "媒体管理/媒体类型查询媒体表单信息")
    public List<MediaForm> listByMediaTypeId(@PathVariable("mediaTypeId") Integer mediaTypeId) {
        return mediaFormService.listByMediaTypeId(mediaTypeId);
    }

    @GetMapping("queryPriceColumnsByTypeId/{mediaTypeId}")
    @ResponseBody
//    @ApiOperation(value = "媒体类型查询媒体表单信息", notes = "媒体类型查询媒体表单信息", response = List.class)
//   @Log(opType = OperateType.QUERY, note = "媒体类型查询媒体表单信息", module = "媒体管理/媒体类型查询媒体表单信息")
//    @Verify(code = "/mediaForm/list/{mediaTypeId}", action = " 媒体类型查询媒体表单信息", module = "媒体管理/媒体类型查询媒体表单信息")
    public List<MediaForm> queryPriceColumnsByTypeId(@PathVariable("mediaTypeId") Integer mediaTypeId) {
        return mediaFormService.queryPriceColumnsByTypeId(mediaTypeId);
    }
}
