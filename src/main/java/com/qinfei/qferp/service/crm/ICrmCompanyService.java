package com.qinfei.qferp.service.crm;

import com.qinfei.qferp.entity.crm.CrmCompany;
import com.qinfei.qferp.entity.crm.CrmCompanyProtect;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 表：crm客户公司表(TCrmCompany)表服务接口
 *
 * @author jca
 * @since 2020-07-07 17:27:23
 */
public interface ICrmCompanyService {

    /**
     * 查询列表
     */
    PageInfo list(Map map, int pageNum, int pageSize);
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CrmCompany getById(Integer id);

    CrmCompany getByName(String name);

    Map getByIdDetail(Integer id);

    void saveCompany(Map map);

    void updateCompanyBasic(Map map);

    void updateCompany(Map map);

    PageInfo productList(Integer companyId, int pageNum, int pageSize);

    PageInfo consumerList(Integer companyId, int pageNum, int pageSize);

    Map getProductById(Integer productId);

    Map getConsumerById(Integer consumerId);

    void saveProduct(Map map);

    void updateProduct(Map map);

    void delProduct(Map map);

    void saveConsumer(Map map);

    void updateConsumer(Map map);

    void delConsumer(Map map);

    void saveProtect(CrmCompanyProtect protect);

    PageInfo listProtect(Map map, int pageNum, int pageSize);

    Map addProtect(Map param);

    CrmCompanyProtect getCompanyIdByProtectId(Integer protectId);

    void updateProtect(CrmCompanyProtect protect);

    Map viewProtect(Map param);

    void auditProtectPass(Map param);

    void auditProtectSuc(CrmCompanyProtect protect);

    void auditProtectFail(Map param);

    PageInfo trackList(Map map, int pageNumber, int pageSize);

    int saveCompanyTrack(Map map, MultipartFile[] files, MultipartFile[] pics);

    PageInfo queryHistoryCompanyId(Integer companyId, int pageNum, int pageSize);

    void doWithRepeatName();
}