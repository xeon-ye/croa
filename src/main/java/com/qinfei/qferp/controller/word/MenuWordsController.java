package com.qinfei.qferp.controller.word;

import com.qinfei.core.ResponseData;

import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.word.Keywords;
import com.qinfei.qferp.entity.word.MenuWords;
import com.qinfei.qferp.service.word.IKeywordsService;
import com.qinfei.qferp.service.word.IMenuWordsService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MenuWordsController
 * @Description 菜单关键字关系接口
 * @Author tsf
 * @Date 2019/9/23
 */
@Slf4j
@Controller
@RequestMapping("/menuWords")
@Api(description = "菜单关键字接口")
public class MenuWordsController {
    @Autowired
    private IMenuWordsService menuWordsService;
    @Autowired
    private IKeywordsService keywordsService;

    /**
     * 根据id查询菜单关键字
     * @param id
     * @return
     */
    @RequestMapping("/getById")
    @ResponseBody
    public ResponseData getById(Integer id){
        ResponseData data = ResponseData.ok() ;
        data.putDataValue("message","操作成功");
        MenuWords menuWords = menuWordsService.getById(id);
        List<Keywords> list = keywordsService.getByGroupId(id);
        data.putDataValue("entity", menuWords) ;
        data.putDataValue("list", list) ;
        return data;
    }

    /**
     * 查询菜单信息
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Map> listPg(@RequestParam Map map, Pageable pageable){
         return menuWordsService.listPg(map,pageable);
    }
    /**
     * 判断菜单是否重复
     */
    @RequestMapping("/checkRepeat")
    @ResponseBody
    public Boolean checkRepeat(@RequestParam Map map){
        return menuWordsService.checkRepeat(map);
    }

    /**
     * 增加菜单关键字
     * @param menuWords
     * @param words
     * @return
     */
    @RequestMapping("/add")
    @ResponseBody
    public ResponseData add(MenuWords menuWords,@Param("words") String words){
        try{
            menuWordsService.saveMenuWords(menuWords,words);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", menuWords) ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 修改菜单关键字
     * @param menuWords
     * @return
     */
    @RequestMapping("/edit")
    @ResponseBody
    public ResponseData edit(@RequestBody MenuWords menuWords){
        try{
            menuWordsService.editMenuWords(menuWords);
            if (menuWords.getInputKeyName() != null){
                User user = AppUtil.getUser();
                Map map;
                Integer keyId= menuWords.getId();
                List<Map> file = new ArrayList<>();
                for (String keyName : menuWords.getInputKeyName()){
                    map= new HashMap();
                    map.put("name",keyName);
                    map.put("groupId",menuWords.getId());
                    map.put("companyCode",user.getCompanyCode());
                    file.add(map);
                }
                menuWordsService.updateState(keyId);
                menuWordsService.insertKeyId(file);

            }
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", menuWords) ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,"关键字修改失败") ;
        }
    }

    /**
     * 删除菜单关键字
     */
    @RequestMapping("/del")
    @ResponseBody
    public ResponseData del(@RequestParam("id") Integer id){
        ResponseData data = ResponseData.ok() ;
        try{
            menuWordsService.deleteMenuWords(id);
            data.putDataValue("message","删除成功");
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

}
