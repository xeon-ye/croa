package com.qinfei.qferp.service.fee;

import com.qinfei.qferp.entity.fee.Commission;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.excelListener.ArticleExcelCommissionRegister;
import com.qinfei.qferp.excelListener.ArticleExcelCommissionUnRegister;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface ICommissionService {


    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    PageInfo<Map> listFeeCommissionByPage(int pageNum, int pageSize, Map map);

    Commission getById(Integer id);

    Commission add(Commission entity);

    Commission edit(Commission entity);

    Commission update(Commission entity);

    void del(Integer id);

    List<Commission> checkCommissionInfo(Integer userId);

    Commission initCommissionInfo(Integer userId);

    Commission batchRegister(String ids, Integer userId);

    Commission batchRegisterOff(String ids, Integer userId);

//    List<Map> exportUnRegister(Map map, OutputStream outputStream);

    List<ArticleExcelCommissionUnRegister> exportUnRegisterNew(Map map);

//    List<Map> exportRegister(Map map, OutputStream outputStream);

    List<ArticleExcelCommissionRegister> exportRegisterNew(Map map);

//    List<Map> exportDetail(Map map, OutputStream outputStream);

    List<Map> exportAll(Map map, OutputStream outputStream);

    Commission confirm(Commission entity, User user);

    Commission pass(Commission entity, User user);

    Commission reject(Commission entity, User user);

    Commission release(Commission entity, User user);

/*    @Transactional
    void backCommInfo(Integer flag, Article article, User user);

    @Transactional
    void updateCommInfo(Integer flag, Article article, User user);*/

    PageInfo<Map> queryArticleByCommStates(Pageable pageable, Map map);

    PageInfo<Map> queryArticleByYearAndMonth(Pageable pageable, Map map);
}
