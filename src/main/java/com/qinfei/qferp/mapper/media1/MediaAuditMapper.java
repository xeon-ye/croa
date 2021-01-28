package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.media.Media;
import com.qinfei.qferp.entity.media1.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaAuditMapper
 * @Description 媒体表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:10
 * @Version 1.0
 */
public interface MediaAuditMapper extends BaseMapper<Media1, Integer> {

    List<MediaAudit> listByParamPage(Map map);

    /**
     * 导出所有条件筛选的媒体
     */
    List<MediaAudit> listAllByParam(Map<String, Object> map);

    /**
     * 根据媒体Id集合获取供应商价格信息
     */
    List<MediaPrice> listMediaSupplierInfoByMediaIds(Map<String, Object> map);

    /**
     * 媒体供应商关系审核：导出所有条件筛选的媒体
     */
    List<MediaAudit> listAllRelateByParam(Map<String, Object> map);

    /**
     * 根据条件获取媒体列表
     * @param map 条件
     */
    List<MediaAudit> listByParam(Map map);

    /**
     * 获取指定媒体及其供应商信息
     */
    List<MediaAudit> listMediaByIds(@Param("mediaIds") List<Integer> mediaIds, @Param("onlineTime") String onlineTime);

    /**
     * 根据条件查询媒体信息
     * @param map 条件
     */
    List<MediaAudit> listMediaByParam(Map map);

    /**
     * 根据条件获取媒体价格
     * @param map 条件
     */
    List<MediaPrice> listMediaPriceByParam(Map map);

    /**
     * 根据条件获取媒体供应商价格
     * @param map 条件
     */
    List<MediaPrice> listMediaSupplierPriceByParam(Map map);

    List<MediaPrice> listMediaSupplierPriceByMediaIds(Map<String, Object> map);

    /**
     * 根据媒体ID获取供应商信息和价格
     */
    MediaPrice getMediaSupplierInfoByMediaId(@Param("id") Integer mediaId, @Param("cell") String cell);

    /**
     * 校验媒体名称是否已存在
     */
    MediaAudit checkMediaForName(@Param("onlineTime") String onlineTime, @Param("name") String name, @Param("plateId") Integer plateId, @Param("id") Integer id);

    MediaAudit checkMediaForMediaContentId(@Param("onlineTime") String onlineTime, @Param("mediaContentId") String mediaContentId, @Param("plateId") Integer plateId, @Param("id") Integer id);

    /**
     * 根据媒体ID获取媒体信息
     */
    MediaAudit getMediaById(@Param("id") Integer id);

    /**
     * 根据媒体ID删除扩展表
     * @param mediaId
     * @return
     */
    int deleteByMediaId(@Param("mediaId") Integer mediaId);

    /**
     * 根据媒体ID批量删除扩展表
     * @param mediaIds
     * @return
     */
    int deleteBatch(List<Integer> mediaIds);

    /**
     * 根据ID获取媒体所有信息
     */
    MediaAudit getEditMediaById(@Param("id") Integer id);

    /**
     * 根据媒体Id获取供应商信息
     */
    MediaAudit getMediaSupplierById(@Param("mediaId") Integer mediaId);

    /**
     * 根据媒体供应商关系ID获取审批通过的媒体和供应商信息
     */
    MediaAudit getMediaSupplierByRelateId(@Param("relateId") Integer relateId);

    /**
     * 新增媒体
     * @param mediaAudit
     */
    int save(MediaAudit mediaAudit);

    /**
     * 批量新增媒体
     * @param mediaList
     */
    int saveBatch(List<Map> mediaList);

    /**
     * 批量拷贝媒体表到媒体审核表
     * @param mediaIds
     * @return
     */
    int copyMediaByIds(List<Integer> mediaIds);

    /**
     * 拷贝媒体表到媒体审核表
     * @param mediaId
     * @return
     */
    int copyMediaById(@Param("mediaId") Integer mediaId);

    /**
     * 更新媒体
     * @param mediaAudit
     */
    int updateMedia(MediaAudit mediaAudit);

    //更新媒体
    int updateMediaForMap(Map<String, Object> param);

    /**
     * 批量更新媒体
     */
    int batchUpdateMedia(@Param("mediaList") List<Map<String, Object>> list);

    /**
     * 更新媒体（不需要审核）
     * @param mediaAudit
     */
    int updateMediaNotAudit(MediaAudit mediaAudit);

    /**
     * 更新媒体状态
     * @param id  媒体主键ID
     * @param state 媒体状态
     * @param updateId 更新用户ID
     */
    int updateMediaState(@Param("id") Integer id, @Param("state") Integer state, @Param("updatedId") Integer updateId);

    /**
     * 批量更新媒体状态
     * @param ids  媒体主键ID集合
     * @param state 媒体状态
     * @param updateId 更新用户ID
     */
    int batchUpdateMediaState(@Param("ids") List<Integer> ids, @Param("state") Integer state, @Param("updatedId") Integer updateId);

    /**
     * 更新媒体可用状态
     * @param id  媒体主键ID
     * @param enabled 0-可用、1-停用
     * @param state 媒体状态
     * @param updateId 更新用户ID
     */
    int updateMediaEnabled(@Param("id") Integer id, @Param("enabled") Integer enabled, @Param("state") Integer state, @Param("updatedId") Integer updateId);

    /**
     * 批量更新媒体可用状态
     * @param ids  媒体主键ID集合
     * @param enabled 0-可用、1-停用
     * @param state 媒体状态
     * @param updateId 更新用户ID
     */
    int batchUpdateMediaEnabled(@Param("ids") List<Integer> ids, @Param("enabled") Integer enabled, @Param("state") Integer state, @Param("updatedId") Integer updateId);

    int updateMediaIsDeleteById(@Param("id") Integer id, @Param("isDelete") Integer isDelete, @Param("state") Integer state, @Param("updatedId") Integer updateId);

    int batchUpdateMediaIsDeleteById(@Param("ids") List<Integer> ids, @Param("isDelete") Integer isDelete, @Param("state") Integer state, @Param("updatedId") Integer updateId);

    /**
     * 根据媒体Id获取稿件
     */
    List<Integer> getArticleByMediaId(@Param("ids") List<Integer> ids);

    List<String> getArticleInfoByMediaId(@Param("ids") List<Integer> ids);

    /**
     * 根据供应商ID获取稿件供应商ID
     */
    List<Integer> listArticleBySupplierId(@Param("ids") List<Integer> ids, @Param("mediaId")Integer mediaId);

    /**
     * 根据媒体Id获取稿件
     */
    List<Integer> getArticleImportByMediaId(@Param("ids") List<Integer> ids);

    /**
     * 获取媒体板块对应媒体数量
     * @param state 状态
     */
    List<Map> listMediaCountByPlateId(@Param("onlineTime") String onlineTime, @Param("state") Integer state, @Param("auditFlag") Integer auditFlag);

    /**
     * 获取媒体板块对应媒体供应商数量
     */
    List<Map> listMediaSupplierCountByPlateId(@Param("onlineTime") String onlineTime, @Param("state") Integer state, @Param("autitFlag") Integer autitFlag);

    //获取所有老媒体表的媒体-数据迁移使用
    List<Media> listAllOldMedia();
    //修改公司名称时处理媒体审核表公司名称字段
    void editMediaAuditCompanyName(@Param("companyCode") String companyCode,@Param("companyCodeName")String companyCodeName);

    //根据ID获取媒体详细信息-用户媒体异动
    List<Media1> listMediaDetailByIds(@Param("ids") List<Integer> ids);

    //更新媒体责任人
    int updateMediaUserId(@Param("id") Integer id, @Param("userId") Integer userId, @Param("updatedId") Integer updateId);

    //历史稿件列表
    List<Map<String, Object>> listHistoryArtByParam(@Param("mediaTypeId") Integer mediaTypeId, @Param("keyword") String keyword, @Param("onlineTime") String onlineTime);

    //根据稿件ID获取请款ID集合
    String listOutgoIdByArtIds(@Param("artIds") List<Integer> artIds);

    //根据稿件ID获取删稿ID集合
    String listDropIdByArtIds(@Param("artIds") List<Integer> artIds);

    //更新稿件表媒体供应商信息
    int updateArtByArtIds(Map<String, Object> param);

    //更新稿件历史表媒体供应商信息
    int updateArtHistoryByArtIds(Map<String, Object> param);

    //更新请款供应商信息
    int updateOutgoByIds(Map<String, Object> param);

    //更新删稿供应商信息
    int updateDropByIds(Map<String, Object> param);

    //查询所有从媒体
    List<Media1> selectMediaList(@Param("onlineTime") String onlineTime);

    List<Integer> masterSupplierList(@Param("masterMediaId")Integer masterMediaId,@Param("onlineTime") String onlineTime);

    List<Integer> masterSupplierListAudit(@Param("masterMediaId")Integer masterMediaId,@Param("onlineTime") String onlineTime);

    int updateMediaSupplier(@Param("masterSupplierList") List<Integer> masterSupplierList,@Param("mediaId") Integer mediaId,@Param("masterMediaId") Integer masterMediaId,@Param("onlineTime") String onlineTime);

    int updateMediaSupplierAudit(@Param("masterSupplierList") List<Integer> masterSupplierList,@Param("mediaId") Integer mediaId,@Param("masterMediaId") Integer masterMediaId,@Param("onlineTime") String onlineTime);
    //查询从媒体的主媒体
    List<Media1> selectMediaName(@Param("mediaIds") List<Integer> mediaIds,@Param("onlineTime") String onlineTime);
    //批量更新稿件
    int batchUpdateArtMedia(@Param("mediaList") List<Map<String, Object>> mediaList,@Param("onlineTime") String onlineTime);

    int updateSlaveMediaRelace(SlaveMediaReplace slaveMediaReplace);

    //查询更新前稿件信息
    List<Map<String,Object>> selectArtList(@Param("mediaList") List<Media1> mediaList,@Param("onlineTime") String onlineTime);

    //
    List<Map<String,Object>> selectSupplierList(@Param("mediaList") List<Media1> mediaList,@Param("onlineTime") String onlineTime);

    List<Map<String,Object>> selectSupplierAuditList(@Param("mediaList") List<Media1> mediaList,@Param("onlineTime") String onlineTime);
    //根据供应商id查询绑定媒体对应的板块id
    String selectPlateIdsForSupplierId(@Param("supplierId") Integer supplierId,@Param("onlineTime") String onlineTime);
}
