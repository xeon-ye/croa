package com.qinfei.qferp.mapper.propose;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.propose.ProposeTips;
import java.util.List;
import java.util.Map;

/**
 * 提示内容数据库接口
 */
public interface ProposeTipsMapper extends BaseMapper<ProposeTips,Integer> {
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
     * 启用还原之前启用的状态（建议提示）
     */
    void editTipsState(String companyCode);

    /**
     * 启用还原之前启用的状态（建议制度）
     */
    void editDocumentState(String companyCode);

    /**
     * 查询已启用的制度跳转链接
     * @return
     */
    Integer getDocumentUrl(String companyCode);

    /**
     * 查询已启用的建议提示内容
     * @return
     */
    String getSuggestContent(String companyCode);

    /**
     * 查询建议提示信息
     * @param map
     * @return
     */
    List<ProposeTips> queryProposeTips(Map map);

    /**
     * 建议管理是否设置建议时间统计区间
     * @param companyCode
     * @return
     */
    List<ProposeTips> queryTipsByType(String companyCode);

    /**
     * 修改建议时间统计
     * @param proposeTips
     * @return
     */
    void updateTimeSection(ProposeTips proposeTips);
}
