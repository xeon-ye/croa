package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.media.MediaInfo;
import com.qinfei.qferp.entity.media1.Media1;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.qinfei.qferp.entity.media1.MediaPrice;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @CalssName Media1Mapper
 * @Description 媒体表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:10
 * @Version 1.0
 */
public interface Media1Mapper extends BaseMapper<Media1, Integer> {
    List<Media1> listByParamPage(Map map);

    /**
     * 根据板块和状态获取媒体数量
     */
    int getMediaCountByParam(Map<String, Object> map);

    /**
     * 根据条件获取媒体列表
     * @param map 条件
     */
    List<Media1> listByParam(Map map);

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
     * 批量拷贝媒体审核表到媒体表
     * @param mediaIds
     * @return
     */
    int copyMediaByIds(List<Integer> mediaIds);

    /**
     * 拷贝媒体审核表到媒体表
     * @param mediaId
     * @return
     */
    int copyMediaById(@Param("mediaId") Integer mediaId);

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
    int updateMediaState(@Param("id") Integer id, @Param("isDelete") Integer isDelete, @Param("state") Integer state, @Param("updatedId") Integer updateId);

    /**
     * 更新媒体是删除字段，当媒体修改后，先不直接修改state字段为0，先用这个字段代替，方便媒体驳回后返回之前状态使用
     */
    int updateMediaIsDelete(@Param("id") Integer id, @Param("isDelete") Integer isDelete, @Param("updatedId") Integer updateId);

    int batchUpdateMediaIsDelete(@Param("ids") List<Integer> ids, @Param("isDelete") Integer isDelete, @Param("updatedId") Integer updateId);

    /**
     * 批量更新媒体状态
     * @param ids  媒体主键ID集合
     * @param state 媒体状态
     * @param updateId 更新用户ID
     */
    int batchUpdateMediaState(@Param("ids")List<Integer> ids, @Param("isDelete") Integer isDelete, @Param("state")Integer state, @Param("updatedId")Integer updateId);

    /**
     * 更新稿件信息
     */
    int updateArticle(Map map);
    int updateArticleImport(Map map);

    /**
     * 批量更新稿件信息
     */
    int batchUpdateArticle(Map map);
    int batchUpdateArticleImport(Map map);

    /**
     * 根据条件查询媒体供应商（新增稿件媒体列表使用）
     */
    List<Map<String, Object>> listMediaSupplierByParam(Map map);

    //获取媒体列表（新增稿件媒体列表使用）
    List<Map<String, Object>> listMediaByParam(Map<String, Object> param);

    /**
     * 查询所有媒体（导入稿件使用）
     */
    List<MediaAudit> listAllMedia(@Param("onlineTime") String onlineTime);

    /**
     * 根据媒体ID获取媒体信息
     */
    Media1 getMediaById(@Param("id") Integer id);

    List<Integer> getMediaByIds(@Param("mediaIds") List<Integer> mediaIds);

    /**
     * 查询媒体供应商单价：给业务在线下单后，媒体发布计算价格使用
     * @param article
     */
    BigDecimal getMediaSupplierOnePrice(Article article);

    /**
     * 查询稿件的发布价：给业务在线下单后，媒体发布计算价格使用
     * @param article
     * @return
     */
    BigDecimal getArticleOutgoAmount(Article article);

    /**
     * 根据媒体ID获取供应商信息和价格
     */
    MediaPrice getMediaSupplierInfoByMediaId(Map map);

    MediaPrice getSupplierInfoByMediaId(@Param("mediaId") Integer mediaId, @Param("cell") String cell);

    List<MediaPrice> getMediaSupplierInfoByMediaIds(Map<String, Object> map);

    //根据媒体ID获取媒体发布稿件数量-趋势图
    List<Map<String, Object>> listMediaFTByMediaId(Map<String, Object> param);

    //根据媒体ID集合获取媒体发布稿件数量
    List<Map<String, Object>> listMediaFT(Map<String, Object> param);

    //根据媒体ID获取媒体发布稿件数量-分页查询、导出
    List<Map<String, Object>> listMediaFTByPage(Map<String, Object> param);

    //获取老媒体表所有媒体数据（数据迁移使用）
    List<MediaInfo> listAllOldMedia();
    //修改公司名称处理媒体表公司名称字段
    void editMedia1CompanyName(@Param("companyCode") String companyCode,@Param("companyCodeName")String companyCodeName);

    //更新媒体责任人
    int updateMediaUserId(@Param("id") Integer id, @Param("userId") Integer userId, @Param("updatedId") Integer updateId);
}
