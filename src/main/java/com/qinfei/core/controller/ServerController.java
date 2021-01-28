package com.qinfei.core.controller;

import com.qinfei.core.utils.Server;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 服务器监控
 *
 * @author qinfei
 */
@Controller
@RequestMapping("/server")
public class ServerController {

    @GetMapping
    public ModelAndView server(ModelAndView mv) throws Exception {
        mv.addObject("server", new Server());
        mv.setViewName("system/server");
        return mv;
    }
}
