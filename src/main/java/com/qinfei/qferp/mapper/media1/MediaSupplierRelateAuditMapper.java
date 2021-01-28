package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaSupplierPriceAudit;
import com.qinfei.qferp.entity.media1.MediaSupplierRelate;
import com.qinfei.qferp.entity.media1.MediaSupplierRelateAudit;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaSupplierRelateAuditMapper
 * @Description 媒体供应商关系表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:37
 * @Version 1.0
 */
public interface MediaSupplierRelateAuditMapper extends BaseMapper<MediaSupplierRelateAudit,Integer> {

    /**
     * 根据媒体ID和供应商ID获取关系表信息
     */
    MediaSupplierRelateAudit getRelateByMediaIdAndSupplierId(@Param("mediaId") Integer mediaId, @Param("supplierId") Integer supplierId);

    List<MediaSupplierRelateAudit> listRelateByMediaIdAndSupplierIds(@Param("mediaId") Integer mediaId, @Param("supplierIds") List<Integer> supplierIds);

    /**
     * 根据媒体Id获取媒体供应商关系表主键集合
     * @param mediaId
     * @return
     */
    List<Integer> listIdByMediaId(@Param("mediaId")Integer mediaId);

    /**
     * 根据媒体Id获取媒体供应商关系表主键集合
     * @param mediaIds
     * @return
     */
    List<Integer> listIdByMediaIds(@Param("mediaIds")List<Integer> mediaIds);

    /**
     * 批量插入媒体供应商价格扩展数据
     * @param mediaSupplierRelateAuditList 媒体扩展集合
     */
    int saveBatch(List<MediaSupplierRelateAudit> mediaSupplierRelateAuditList);

    /**
     * 批量插入媒体供应商价格扩展数据
     * @param mediaSupplierRelateAuditList 媒体扩展集合
     */
    int updateBatch(@Param("mediaSupplierRelateAuditList") List<MediaSupplierRelateAudit> mediaSupplierRelateAuditList);

    //更新媒体供应商关系
    int updateMediaSupplierRelateForMap(Map<String, Object> param);

    /**
     * 更新状态
     * @param map
     * @return
     */
    int updateStateByIds(Map map);

    int updateStateById(@Param("state") Integer state, @Param("updateId") Integer updateId, @Param("id") Integer id);

    int updateStateBySupplierId(@Param("state") Integer state, @Param("updateId") Integer updateId, @Param("supplierId") Integer supplierId);

    int updateEnableByIds(Map map);

    int updateEnableById(@Param("enabled") Integer enabled, @Param("state") Integer state, @Param("updateId") Integer updateId, @Param("id") Integer id);

    int updateIsDeleteByIds(Map map);

    int updateIsDeleteById(@Param("isDelete") Integer isDelete, @Param("state") Integer state, @Param("updateId") Integer updateId, @Param("id") Integer id);

    /**
     * 根据媒体供应商关系ID获取供应商列表
     */
    List<Integer> listSupplierIdByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据媒体ID获取供应商信息
     * @param mediaId
     * @return
     */
    List<MediaSupplierRelateAudit> listSupplierInfoByMediaId(@Param("mediaId") Integer mediaId);

    /**
     * 批量拷贝媒体供应商关系审核表到媒体供应商关系表
     * @param mediaIds
     * @return
     */
    int copySupplierRelateByMediaIds(@Param("mediaIds") List<Integer> mediaIds);

    int copySupplierRelateByIds(@Param("ids") List<Integer> ids);

    /**
     * 拷贝媒体供应商关系审核表到媒体供应商关系表
     * @param mediaId
     * @return
     */
    int copySupplierRelateByMediaId(@Param("mediaId") Integer mediaId);

    /**
     * 根据媒体ID删除关系表
     * @param mediaId
     * @return
     */
    int deleteByMediaId(@Param("mediaId") Integer mediaId);

    /**
     * 根据媒体ID批量删除关系表
     */
    int deleteByMediaIds(@Param("mediaIds") List<Integer> mediaIds);

    int deleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据供应商ID获取关联的媒体ID列表
     * @param supplierId
     */
    List<Integer> listMediaIdBySupplierId(@Param("supplierId") Integer supplierId);

    //根据ID获取媒体供应商详细信息-用户媒体供应商异动
    List<MediaSupplierRelate> listMediaSupplierDetailByIds(@Param("ids") List<Integer> ids);
}
