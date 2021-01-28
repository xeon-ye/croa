package com.qinfei.qferp.controller.standardized;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.standardized.StandardizedCompany;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.standardized.IStandardizedCompanyService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author: 66
 * @Date: 2020/11/13 11:23
 * @Description: 标准化公司控制层
 */
@RestController
@RequestMapping("/standardizedCompany")
public class StandardizedCompanyController {

    @Resource
    IStandardizedCompanyService standardizedCompanyService;

    /**
     * 保存标准化公司
     *
     * @param standardizedCompany 入参
     * @return 结果集
     */
    @PostMapping("/saveStandardizedCompany")
    public ResponseData saveStandardizedCompany(StandardizedCompany standardizedCompany) {
        User user = AppUtil.getUser();
        if (user == null) {
            throw new QinFeiException(1002, "请先登录");
        }
        int i = standardizedCompanyService.saveStandardizedCompany(standardizedCompany, user);
        if (i == 0) {
            throw new QinFeiException(1002, "保存失败");
        }
        return ResponseData.ok("保存成功");
    }


    /**
     * 标准化公司分页列表
     *
     * @param map      入参
     * @param pageable 分页参数
     * @return 结果集
     */
    @GetMapping("/listPg")
    public PageInfo<StandardizedCompany> listPg(@RequestParam Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(standardizedCompanyService.findList(map));
    }

    /**
     * 根据id查询标准化公司
     *
     * @param id 标准化公司id
     * @return 结果集
     */
    @GetMapping("/findById")
    public ResponseData findById(@RequestParam Integer id) {
        return ResponseData.ok(standardizedCompanyService.findById(id));
    }

    /**
     * 根据id删除标准化公司
     *
     * @param id 标准化公司id
     * @return 结果集
     */
    @PostMapping("/delStandardizedCompany")
    public ResponseData delStandardizedCompany(@RequestParam Integer id) {
        int i = standardizedCompanyService.delStandardizedCompany(id);
        if (i == 0) {
            throw new QinFeiException(1002, "删除失败");
        }
        return ResponseData.ok("删除成功");
    }

}
