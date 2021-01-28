package com.qinfei.qferp.controller;

import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.media.Industry;
import com.qinfei.qferp.service.IIndustryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
@Slf4j
@Controller
@RequestMapping("industry")
class IndustryController {

    @Autowired
    IIndustryService industryService;

    @GetMapping("list")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "行业", note = "查询所有行业")
    public List<Industry> list(Industry industry) {
        return industryService.list(industry);
    }
}
