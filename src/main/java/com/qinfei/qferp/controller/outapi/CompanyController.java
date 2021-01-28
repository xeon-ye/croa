package com.qinfei.qferp.controller.outapi;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.crm.CrmSearchCache;
import com.qinfei.qferp.service.outapi.ICompanyService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @CalssName: CompanyController
 * @Description: 企查查接口服务
 * @Author: Xuxiong
 * @Date: 2020/6/5 0005 15:18
 * @Version: 1.0
 */
@RestController
@RequestMapping("company")
public class CompanyController {
    @Autowired
    private ICompanyService companyService;

    @PostMapping("companySearch")
    public PageInfo<CrmSearchCache> companySearch(@RequestParam(name = "keyword") String keyword,
                                                  @PageableDefault(size = 20, page = 1) Pageable pageable) {
        return companyService.companySearch(keyword, pageable);
    }

    @PostMapping("checkCompany")
    public ResponseData checkCompany(@RequestParam(name = "keyword") String keyword){
        return companyService.checkCompany(keyword);
    }
}
