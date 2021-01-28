package com.qinfei.qferp.controller.word;

import com.qinfei.core.ResponseData;

import com.qinfei.qferp.entity.word.Keywords;
import com.qinfei.qferp.service.word.IKeywordsService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @CalssName KeywordsController
 * @Description 关键字控制器
 * @Author tsf
 * @Date 2019/9/26
 */
@Slf4j
@Controller
@RequestMapping("/keywords")
@Api(description = "关键字控制器")
public class KeywordsController {
    @Autowired
    private IKeywordsService keywordsService;

    /**
     * 增加菜单关键字
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public List<Keywords> listPg(@RequestParam Map map){
         return null;
    }

    /**
     * 根据关键字名称去重
     * @param keyName
     * @param groupId
     * param id
     * @return
     */
    @RequestMapping("/getByName")
    @ResponseBody
    public ResponseData getByName(@RequestParam("keyName") String keyName,@RequestParam("groupId") Integer groupId,@RequestParam(value = "id",required = false) Integer id){
        ResponseData data = ResponseData.ok() ;
        Keywords keywords = keywordsService.getByName(keyName,groupId,id);
        data.putDataValue("entity",keywords);
        return data;
    }

    /**
     * 增加菜单关键字
     */
    @RequestMapping("/add")
    @ResponseBody
    public ResponseData add(@RequestParam("name")String name,@RequestParam("groupId")Integer groupId){
        ResponseData data = ResponseData.ok() ;
        data.putDataValue("message","操作成功");
        keywordsService.saveKeywords(name,groupId);
        return data;
    }
}
