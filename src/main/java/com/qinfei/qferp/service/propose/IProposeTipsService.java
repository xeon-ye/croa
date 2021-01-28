package com.qinfei.qferp.service.propose;

import com.qinfei.qferp.entity.propose.ProposeTips;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 建议提示内容接口
 * @Author:tsf
 */
public interface IProposeTipsService {
    String PROPOSE_CACHE_LIST = "proposeTipsUsers";
    /**
     * 根据id查询建议提示信息
     * @param id
     * @return
     */
    ProposeTips getById(Integer id);

    /**
     * 添加建议提示内容
     * @param proposeTips
     */
    void saveProposeTips(ProposeTips proposeTips);

    /**
     * 修改建议提示内容
     * @param proposeTips
     */
    void editProposeTips(ProposeTips proposeTips);

    /**
     * 修改建议提示状态
     * @param proposeTips
     */
    void editTipsData(ProposeTips proposeTips);

    /**
     * 修改建议制度数据
     * @param proposeTips
     */
    void editDocumentData(ProposeTips proposeTips);

    /**
     * 查询已启用的制度跳转链接
     * @return
     */
    Integer getDocumentUrl();

    /**
     * 查询已启用的建议提示内容
     * @return
     */
    String getSuggestContent();

    /**
     * 删除建议提示内容
     * @param id
     */
    void delProposeTips(Integer id);

    /**
     * 查询建议提示信息
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<ProposeTips> queryProposeTips(Map map, Pageable pageable);

    /**
     * 修改建议提醒缓存数据
     */
    @CachePut(value = PROPOSE_CACHE_LIST, key = "'companyCode'+#companyCode")
    List<Integer> updateProposeCache(Integer userId,String companyCode);

    /**
     * 添加建议管理时间范围
     * @param
     */
    @CacheEvict(value = PROPOSE_CACHE_LIST,key = "'companyCode'+#companyCode")
    void saveTimeSection(Integer state,String startTime,String endTime,String companyCode);

    /**
     * 查询建议管理是否设置了统计时间值
     * @return
     */
    List<ProposeTips> queryTipsByType(String companyCode);
}
