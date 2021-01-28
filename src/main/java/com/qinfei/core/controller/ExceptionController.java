package com.qinfei.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("error")
@EnableConfigurationProperties({ServerProperties.class})
class ExceptionController implements ErrorController {
    private static final String ERROR_PATH = "/error";

    @RequestMapping
    public ModelAndView errorHtml(HttpServletRequest request) {
        Map<String, Object> map = getAttributes(request, false);
        ModelAndView mv = null;
        Integer status = (Integer) map.get("status");
        if (status == 404) {
            mv = new ModelAndView("common/404", map);
        } else if (status == 403) {
            mv = new ModelAndView("common/403", map);
        } else if (status == 500) {
            mv = new ModelAndView("common/500", map);
        } else {
            mv = new ModelAndView("common/error", map);
//            mv = new ModelAndView("/404", map);
        }
        return mv;
    }

    private Map<String, Object> getAttributes(HttpServletRequest request,
                                              boolean stackTrace) {
        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> map = this.errorAttributes.getErrorAttributes(webRequest, stackTrace);
        String URL = request.getRequestURL().toString();
        map.put("URL", URL);
        return map;
    }

    /**
     * 初始化ExceptionController
     *
     * @param errorAttributes
     */
    @Autowired
    private ErrorAttributes errorAttributes;

    /**
     * 实现错误路径,暂时无用
     *
     * @return
     */
    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

}