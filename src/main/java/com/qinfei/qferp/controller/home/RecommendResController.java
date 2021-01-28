package com.qinfei.qferp.controller.home;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.news.News;
import com.qinfei.qferp.entity.news.RecommendResources;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.news.INewsService;
import com.qinfei.qferp.service.news.IRecommendResourcesService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("recommendRes")
public class RecommendResController {

    @Autowired
    private IRecommendResourcesService recommendResourcesService;


    @PostMapping("list")
    @ResponseBody
    public PageInfo<RecommendResources> list(@PageableDefault(value = 20, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable, RecommendResources res){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize(),"update_time desc");
        User user = AppUtil.getUser();
        List<RecommendResources> list = recommendResourcesService.getResByDto(res);
        return new PageInfo<RecommendResources>(list);
    }

    @PostMapping("index")
    @ResponseBody
    public List<RecommendResources> indexlist(@PageableDefault(value = 15, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable, RecommendResources res){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize(),"update_time desc");
        User user = AppUtil.getUser();
        return recommendResourcesService.getResByDto(res);
    }

    @PostMapping("admin")
    @ResponseBody
    public PageInfo< RecommendResources> adminList(@PageableDefault(value = 20, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,  RecommendResources res){
        List< RecommendResources> list = recommendResourcesService.getAdminResByDto(res,pageable.getPageNumber(),pageable.getPageSize(),"update_time desc");
        return new PageInfo< RecommendResources>(list);
    }


    @PostMapping
    @ResponseBody
    public ResponseData save( RecommendResources res){
        try {
            Boolean result = recommendResourcesService.save(res);
            return ResponseData.ok();
        }  catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(10000,"新增资源出错！");
        }
    }

    @PostMapping("edit")
    @ResponseBody
    public ResponseData update(RecommendResources res){
        try {
            Boolean result = recommendResourcesService.update(res);
            return ResponseData.ok();
        }  catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(10000,"编辑资源出错！");
        }
    }

    @GetMapping("{id}")
    @ResponseBody
    public  ResponseData  resDetail(@PathVariable Integer id){
        try{
            ResponseData ok = ResponseData.ok();
            ok.putDataValue("data",recommendResourcesService.queryById(id));
            return ok;
        }catch (Exception e){
            return ResponseData.customerError(1002,e.getMessage());
        }
    }

    @GetMapping("view/{id}/{adminFlag}")
    public ModelAndView viewDetail(@PathVariable Integer id, @PathVariable boolean adminFlag){
        ModelAndView model = new ModelAndView("news/resourceDetail");
        RecommendResources recommendResources = recommendResourcesService.queryById(id);
        model.addObject("resources",recommendResources);
        RecommendResources searchResources = new RecommendResources();
        searchResources.setUpdateTime(recommendResources.getUpdateTime());
        searchResources.setCompanyCode(AppUtil.getUser().getCompanyCode());
        model.addObject("adminFlag", adminFlag); //根据此标识判断是否是管理列表还是首页列表
        model.addObject("pre",recommendResourcesService.getPreResources(searchResources));
        model.addObject("next",recommendResourcesService.getNextResources(searchResources));
        return model;
    }

    @DeleteMapping("{id}")
    @ResponseBody
    public  ResponseData delete(@PathVariable Integer id){
        try {
            Boolean result = recommendResourcesService.deleteById(id);
            return ResponseData.ok();
        }  catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"删除资源出错！");
        }
    }

}
