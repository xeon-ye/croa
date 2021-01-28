package com.qinfei.qferp.controller.media;

import com.qinfei.qferp.entity.media.MediaName;
import com.qinfei.qferp.service.media.IMediaNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("mediaName")
class MediaNameController {

    @Autowired
    IMediaNameService mediaNameService;

    @GetMapping("all")
    @ResponseBody
    public List<MediaName> all() {
        return mediaNameService.all();
    }

    @GetMapping("list")
    @ResponseBody
    public List<MediaName> list(MediaName mediaName) {
        return mediaNameService.list(mediaName);
    }
}
