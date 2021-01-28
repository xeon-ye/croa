package com.qinfei.qferp.service.standardized;

import com.qinfei.qferp.entity.standardized.StandardizedCompany;
import com.qinfei.qferp.entity.sys.User;

import java.util.List;
import java.util.Map;

/**
 * @author: 66
 * @Date: 2020/11/13 9:59
 * @Description: 标准化公司申请业务层接口
 */
public interface IStandardizedCompanyService {

    /**
     * 保存标准化公司
     *
     * @param standardizedCompany 入参
     * @param user                登录用户
     * @return 数据库影响行数
     */
    int saveStandardizedCompany(StandardizedCompany standardizedCompany, User user);

    /**
     * 修改标准化公司
     *
     * @param standardizedCompany 入参
     * @param user                登录用户
     * @return 数据库影响行数
     */
    int updateStandardizedCompany(StandardizedCompany standardizedCompany, User user);

    /**
     * 标准化公司分页列表
     *
     * @param map 查询条件
     * @return 列表
     */
    List<StandardizedCompany> findList(Map map);

    /**
     * 根据id查询标准化公司
     *
     * @param id 标准化公司id
     * @return 标准化公司
     */
    StandardizedCompany findById(Integer id);

    /**
     * 删除标准化公司
     *
     * @param id 实例对象
     * @return 数据库影响行数
     */
    int delStandardizedCompany(Integer id);


    /**
     * 修改 crm客户公司表 对接人信息表 插入企查查表
     *
     * @param companyName 公司名
     * @return 数据库影响行数
     */
    int updateCompany(String companyName);

}
