package com.qinfei.qferp.controller.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.media1.MediaBrand;
import com.qinfei.qferp.service.media1.IMediaBrandService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 媒体品牌控制层
 *
 * @author tsf
 * @since 2020-10-21 09:30:22
 */
@Controller
@RequestMapping("/mediaBrand")
public class MediaBrandController {
    @Autowired
    private IMediaBrandService mediaBrandService;

    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, Pageable pageable){
        return mediaBrandService.listPg(map,pageable);
    }

    @RequestMapping("/listPgForView")
    @ResponseBody
    public PageInfo<Map> listPgForView(@RequestParam Map map, Pageable pageable){
        return mediaBrandService.listPgForView(map,pageable);
    }

    @RequestMapping("/getById")
    @ResponseBody
    public ResponseData getById(@RequestParam("id")String id){
        try {
            ResponseData data = ResponseData.ok();
            if(!StringUtils.isEmpty(id)){
                MediaBrand mediaBrand = mediaBrandService.getById(id);
                data.putDataValue("entity",mediaBrand);
                return data;
            }else {
                return ResponseData.customerError(1002,"该品牌不存在,请刷新下页面");
            }
        } catch (Exception e) {
            return ResponseData.customerError(1002,"该品牌不存在,请刷新下页面");
        }
    }
}