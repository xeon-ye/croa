package com.qinfei.qferp.controller.media;

import com.qinfei.qferp.entity.media.MediaScreen;
import com.qinfei.qferp.service.media.IMediaScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("mediaScreen")
class MediaScreenController {

    @Autowired
    IMediaScreenService mediaScreenService;

    @GetMapping("all")
    @ResponseBody
    public List<MediaScreen> all() {
        return mediaScreenService.all();
    }

    @GetMapping("list")
    @ResponseBody
    public List<MediaScreen> list(MediaScreen mediaScreen) {
        return mediaScreenService.list(mediaScreen);

    }
    @GetMapping("listByMediaTypeId/{mediaTypeId}")
    @ResponseBody
    public List<MediaScreen> listByMediaTypeId(@PathVariable("mediaTypeId") Integer mediaTypeId) {
        return mediaScreenService.listByMediaTypeId(mediaTypeId);
    }
}
