package com.qinfei.qferp.controller.document;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.document.TDocumentLibrary;
import com.qinfei.qferp.service.document.ITDocumentLibraryService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 制度管理接口
 */
@Slf4j
@Controller
@RequestMapping("/documentLibrary")
public class TDocumentLibraryController {
    @Autowired
    private ITDocumentLibraryService documentLibraryService;


    /**
     * 查询制度类型
     */
    @RequestMapping("/libraryType")
    @ResponseBody
    public ResponseData libraryType(){
        try{
            ResponseData data = ResponseData.ok();
            //返回json 格式，便于树状视图。
            JSONArray list = documentLibraryService.libraryType();
            data.putDataValue("list",list);
            return data;
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    /**
     * 查詢制度总数
     */
    @RequestMapping("/getLibraryTotal")
    @ResponseBody
    public ResponseData getLibraryTotal(@RequestParam Map<String,Object> map){
        try{
            return ResponseData.ok().putDataValue("total",documentLibraryService.getLibraryTotal(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取制度数量异常，请联系技术人员！");
        }
    }



    /**
     *
     * 查询制度列表（）
     */
    @RequestMapping("/selectLibraryList")
    @ResponseBody
    public PageInfo<TDocumentLibrary> selectLibraryList(@RequestParam Map<String,Object> map, @PageableDefault(size = 10) Pageable pageable){
        return documentLibraryService.selectLibraryList(map,pageable);

    }

    /**
     *
     * 建议配置所需制度数据
     */
    @RequestMapping("/getDocumentLibraryList")
    @ResponseBody
    public List<TDocumentLibrary> getDocumentLibraryList(@RequestParam Map map){
        return documentLibraryService.getDocumentLibraryList(map);

    }

    @RequestMapping("/list")
    @ResponseBody
    public Map<String,Object> list(@RequestParam Map<String,Object> map){
        return documentLibraryService.list(map);
    }

    /**
     * 查询制度发布人
     */
    @RequestMapping("/releaseUser")
    @ResponseBody
    public ResponseData releaseUser(){
        return documentLibraryService.releaseUser();
    }

    /**
     * 制度新增提交
     */
//    @PostMapping("/addLibrary")
    @RequestMapping(value="/addLibrary",produces="text/html;charset=utf-8")
    @ResponseBody
    public ResponseData addLibrary(TDocumentLibrary tDocumentLibrary,@RequestParam(value = "file1", required = false)MultipartFile[] files){
        return documentLibraryService.addLibrary(tDocumentLibrary,files);
    }

    /**
     * 制度权限
     */
    @RequestMapping("/listPermissions")
    @ResponseBody
    public List<Map<String,Object>> listPermissions(@RequestParam("signStr") String signStr, @RequestParam(name = "name", required = false) String name){
        return  documentLibraryService.listpermissions(signStr, name);
    }

    /***
     * 根据制度id 查询数据
     */
    @RequestMapping("/selectLibrary")
    @ResponseBody
    public ResponseData selectLibrary(@RequestBody Map<String,Object> map){
        try {
            if (StringUtils.isEmpty(map.get("id").toString())){
                throw new QinFeiException(1002,"未获取到制度的id");
            }
            Integer libraryid= Integer.parseInt(map.get("id").toString());
            return documentLibraryService.selectLibrary(libraryid);
        }catch (QinFeiException b){
            return ResponseData.customerError(b.getCode(),b.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"更据制度id查询制度错误，请联系系统管理员");
        }

    }
    /**
     * 新增类型
     */
    @RequestMapping("/addType")
    @ResponseBody
    public ResponseData addType(@RequestBody Map<String,Object> map){
        return documentLibraryService.addType(map);
    }

    /**
     * 编辑制度类型
     */
    @RequestMapping("/editTypeName")
    @ResponseBody
    public ResponseData editTypeName(@RequestBody Map<String,Object> map){
        return documentLibraryService.editTypeName(map);
    }

    /**
     * 刪除制度類型
     */
    @PostMapping("/delTypeName")
    @ResponseBody
    public ResponseData delType(@RequestBody Map<String,Object> map){
        try {
            if (StringUtils.isEmpty(map.get("typeId1").toString())){
                throw new QinFeiException(1002,"未获取到制度类型id");
            }
            String typeId = map.get("typeId1").toString();
            return documentLibraryService.delType(typeId);
        }catch (QinFeiException b){
            return ResponseData.customerError(b.getCode(),b.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"删除制度类型出错");
        }

    }

    /**
     * 判断所选择的节点类型中及子类型下是否有制度

     * @return
     */
    @RequestMapping("/selectTypeFlag")
    @ResponseBody
    public ResponseData selectTypeFlag(@RequestBody Map<String,Object>map){
        return documentLibraryService.selectTypeFlag(map);
    }

    /**
     * 根据制度id删除制度
     */
    @RequestMapping("/delLibrary")
    @ResponseBody
    public ResponseData delLibrary(@RequestBody Integer LibraryId){
        return documentLibraryService.delLibrary(LibraryId);
    }

    /**\
     * 查看制度详情页
     */
    @RequestMapping("/library/view/{id}")
    @ResponseBody
    public ModelAndView viewLibrary(@PathVariable Integer id){
        ModelAndView modelAndView =new ModelAndView("document/libraryView");
        TDocumentLibrary documentLibrary = documentLibraryService.selectLibraryview(id);
        modelAndView.addObject("library",documentLibrary);
        //点击链接跳转到制度详情，则该用户为已阅读制度用户
        documentLibraryService.updateLibraryReady(id);
        return modelAndView;
    }

    /***
     * 制度失效
     */
    @RequestMapping("/updatefailure")
    @ResponseBody
    public ResponseData  updatefailure(@RequestBody Integer id){
        return documentLibraryService.updatefailure(id);
    }
    /**
     * 附件删除
     */
    @RequestMapping("/deleteFile")
    @ResponseBody
    public ResponseData deleteFile(@RequestParam(name = "file") String file, @RequestParam("fileLink") String fileLink,@RequestParam("id") Integer id){
       return   documentLibraryService.deleteFile(file,fileLink,id);
    }

    /**
     * 制度详细名单
     */
    @RequestMapping("/checkList")
    @ResponseBody
    public ResponseData CheckList(@RequestParam(name = "id", required = false) Integer id){
        return documentLibraryService.CheckList(id);
    }



}
