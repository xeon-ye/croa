package com.qinfei.qferp.controller.home;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.news.News;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.news.INewsService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("news")
public class NewsController {

    @Autowired
    private INewsService  newsService;


    @PostMapping("list")
    @ResponseBody
    public PageInfo<News> list(@PageableDefault(value = 20, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable, News news){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize(),"update_time desc");
        User user = AppUtil.getUser();
        List<News> list = newsService.getNewsByDto(news);
        return new PageInfo<News>(list);
    }

    @GetMapping("search/{type}")
    @ResponseBody
    public PageInfo<News> listByType(@PageableDefault(value = 10, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,@PathVariable Integer type){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize(),"update_time desc");
        List<News> list = newsService.listByType(type);
        return new PageInfo<News>(list);
    }


    @GetMapping("view/{id}")
    public ModelAndView viewDetail(@PathVariable Integer id){
        ModelAndView model = new ModelAndView("news/newsDetail");
        News news = newsService.queryById(id);
        model.addObject("news",news);
        News searchNews = new News();
        searchNews.setUpdateTime(news.getUpdateTime());
        searchNews.setCompanyCode(AppUtil.getUser().getCompanyCode());
        model.addObject("pre",newsService.getPreNews(searchNews));
        model.addObject("next",newsService.getNextNews(searchNews));
        return model;
    }

    @GetMapping("/recommedResList")
    public ModelAndView viewResList(){
        ModelAndView model = new ModelAndView("news/recommedResList");
        return model;
    }
    @GetMapping("/newsList")
    public ModelAndView viewNewsList(){
        ModelAndView model = new ModelAndView("news/newsList");
        return model;
    }

    @PostMapping("getNewsTotal")
    @ResponseBody
    @ApiOperation(value = "新闻资讯", notes = "新闻总数")
    public ResponseData getCourseTotal(@RequestParam Map<String, Object> param){
        try {
            return ResponseData.ok().putDataValue("total",newsService.getAdminNewsCount(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取新闻资讯总数异常，请联系技术人员！");
        }
    }

    @PostMapping("admin")
    @ResponseBody
    public PageInfo<News> adminList(@PageableDefault(value = 15, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable, News news){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize(),"update_time desc");
        List<News> list = newsService.getAdminNewsByDto(news);
        return new PageInfo<>(list);
    }

    @GetMapping("admin/view/{id}")
    public ModelAndView viewAdminDetail(@PathVariable Integer id){
        ModelAndView model = new ModelAndView("news/newsAdminDetail");
        News news = newsService.queryById(id);
        model.addObject("news",news);
        News searchNews = new News();
        searchNews.setUpdateTime(news.getUpdateTime());
        searchNews.setCompanyCode(AppUtil.getUser().getCompanyCode());
        model.addObject("pre",newsService.getAdminPreNews(searchNews));
        model.addObject("next",newsService.getAdminNextNews(searchNews));
        return model;
    }

    @GetMapping("{id}")
    @ResponseBody
    public  ResponseData  newsDetail(@PathVariable Integer id){
        try{
            ResponseData ok = ResponseData.ok();
            ok.putDataValue("data",newsService.queryById(id));
            return ok;
        }catch (Exception e){
            return ResponseData.customerError(10000,e.getMessage());
        }
    }


    @PostMapping
    @ResponseBody
    public ResponseData save(News news){
        try {
            Boolean result = newsService.save(news);
            return ResponseData.ok();
        }  catch (QinFeiException e) {
            return ResponseData.customerError(10000,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(10000,"新闻添加出错！");
        }
    }

    @PostMapping("edit")
    @ResponseBody
    public ResponseData update(News news){
        try {
            Boolean result = newsService.update(news);
            return ResponseData.ok();
        }  catch (QinFeiException e) {
            return ResponseData.customerError(10000,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(10000,"新闻编辑出错！");
        }
    }

    @DeleteMapping("{id}")
    @ResponseBody
    public  ResponseData delete(@PathVariable Integer id){
        try {
            Boolean result = newsService.deleteById(id);
            return ResponseData.ok();
        }  catch (QinFeiException e) {
            return ResponseData.customerError(10000,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(10000,"服务器异常");
        }
    }

    @PostMapping("batchDel")
    @ApiOperation(value = "新闻管理", notes = "批量删除")
    @ResponseBody
    public ResponseData batchDel(@RequestParam("ids[]") List<Integer> ids){
        try{
            newsService.batchDel(ids);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "新闻删除异常，请联系技术人员！");
        }
    }

}
