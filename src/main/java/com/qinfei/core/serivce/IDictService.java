package com.qinfei.core.serivce;

import com.qinfei.core.annotation.Transient;
import com.qinfei.core.entity.Dict;
import com.qinfei.qferp.entity.sys.Dept;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface IDictService {
    String CACHE_KEY_LIST = "dictList";

    void initdictEnum() throws Exception;

    Dict getByTypeCodeAndCode(Dict dict);

    List<Dict> list(Dict dict);

    List<Dict> listByTypeCode(String typeCode);

    @SuppressWarnings("SpringCacheAnnotationsOnInterfaceInspection")
    @Cacheable(value = CACHE_KEY_LIST, key = "'typeCode='+#typeCode+'&companyCode='+#companyCode")
    List<Dict> listByTypeCodeAndCompanyCode(String typeCode, String companyCode);

    List<Dict> listDict(String typeCode);

//    List<Map<String, Object>> list(Map<String, Object> param);

//    List<Map<String, Object>> listByTypeCode(Map<String, Object> param);

    Dict getByTypeCodeAndName(String typeCode, String name, String companyCode);

    Dict getDictName(String typeCode, String name,Integer userDeptId);

    PageInfo<Dict> selectDict(Map map,Pageable pageable);

    Dict add ( Dict dict,Integer[] deptIds);

    Dict getById(Integer id);

    void delById(Dict dict);

    List<Dept> queryDeptId(Integer id);

    List<Dept> delDept(Integer dictId,Integer id);

    Dict edit(Dict dict);

    void editDeptId(Integer dictId);

    void insertDept (List<Map> file);

    /**
     * 建议类型管理
     * @param nameQc
     * @param id
     * @return
     */
    PageInfo<Dict> queryProposeDict(String nameQc,Integer id,Pageable pageable);

    /**
     * 建议类型添加
     * @param dict
     */
    Dict saveSuggest(Dict dict,String ids);

    /**
     * 建议类型修改
     * @param dict
     */
    void updateSuggest(Dict dict,String ids);

    /**
     * 建议类型删除
     * @param id
     */
    void delSuggest(Integer id);

    String code(String code);

    @Transient
    void updateCompanyParam(String typeCodeProtect, String level, Integer num, Integer audit, Integer eval,
                            String typeCodeTransfer, Integer TRACK_EVAL_DAY, Integer DEAL_EVAL_DAY, Integer EVAL_REMIND_DAY,
                            Integer TO_BLACK_TIMES, Integer CLAIM_TIMES_DAY, Integer CLAIM_START_TIME);
}
