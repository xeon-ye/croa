package com.qinfei.qferp.controller.media;


import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.media.FileEntitys;
import com.qinfei.qferp.enumUtils.FilesEnum;
import com.qinfei.qferp.service.impl.media.FileEntityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/mediaFiles")
@Api(description = "稿件差价附件接口")
class MediaFileController {
    @Autowired
    private FileEntityService fileEntityService;

    /**
     * 通过稿件id获取差价附件
     * @param articId
     * @return
     */
    @PostMapping("/getFilesByArticleId")
    @ResponseBody
    public ResponseData getFilesByArticleId(Integer articId){
        FileEntitys fileEntitys = fileEntityService.getByArticleId(articId, FilesEnum.ARTICLEDIFFILES.getType());
        return ResponseData.ok().putDataValue("entity", fileEntitys);
    }
}
