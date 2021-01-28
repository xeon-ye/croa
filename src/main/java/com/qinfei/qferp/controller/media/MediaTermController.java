package com.qinfei.qferp.controller.media;

import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.media.MediaTerm;
import com.qinfei.qferp.service.media.IMediaTermService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/mediaTerm")
@Api(description = "媒体管理接口")
class MediaTermController {
    @Autowired
    IMediaTermService mediaTermService;

    @GetMapping("/{typeId}")
    @ResponseBody
    @Log(opType = OperateType.QUERY, note = "媒体类型筛选", module = "媒体管理/根据媒介用户ID获取媒体板块类型列表")
    public List<MediaTerm> list(@PathVariable("typeId") Integer typeId) {
        return mediaTermService.list(typeId);
    }

    @GetMapping("/get")
    @ResponseBody
    public MediaTerm get(MediaTerm mediaTerm) {
        return mediaTermService.getByTerm(mediaTerm);
    }
}
