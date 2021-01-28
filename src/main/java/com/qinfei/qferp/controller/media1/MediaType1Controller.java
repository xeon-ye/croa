package com.qinfei.qferp.controller.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.media1.MediaType1;
import com.qinfei.qferp.service.media1.IMediaType1Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @CalssName MediaType1Controller
 * @Description 媒体类型接口1
 * @Author xuxiong
 * @Date 2019/6/26 0026 17:14
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/mediaType1")
@Api(description = "媒体类型接口1")
public class MediaType1Controller {
    @Autowired
    private IMediaType1Service mediaType1Service;

    @GetMapping("/{plateId}")
    @ResponseBody
    @ApiOperation(value = "根据媒体板块ID查询媒体类型列表", notes = "根据媒体板块ID查询媒体类型列表", response = ResponseData.class)
    public List<MediaType1> listByPlateId(@PathVariable("plateId") Integer plateId) {
        return mediaType1Service.listByPlateId(plateId);
    }
}
