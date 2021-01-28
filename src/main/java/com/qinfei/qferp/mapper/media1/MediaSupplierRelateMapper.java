package com.qinfei.qferp.mapper.media1;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media1.MediaSupplierRelate;
import com.qinfei.qferp.entity.media1.MediaSupplierRelateAudit;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MediaSupplierRelateMapper
 * @Description 媒体供应商关系表
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:37
 * @Version 1.0
 */
public interface MediaSupplierRelateMapper extends BaseMapper<MediaSupplierRelateAudit,Integer> {
    /**
     * 根据媒体Id获取媒体供应商关系表主键集合
     * @param mediaId
     * @return
     */
    List<Integer> listIdByMediaId(@Param("mediaId")Integer mediaId);

    List<Integer> listIdById(@Param("ids") List<Integer> ids);

    /**
     * 根据媒体Id获取媒体供应商关系表主键集合
     * @param mediaIds
     * @return
     */
    List<Integer> listIdByMediaIds(@Param("mediaIds")List<Integer> mediaIds);

    List<MediaSupplierRelate> listMediaSupplierRelateByMediaIds(@Param("mediaIds")List<Integer> mediaIds);

    /**
     * 更新状态
     */
    int updateMediaRelateIsDelete(@Param("isDelete") Integer isDelete,@Param("updateId") Integer updateId,@Param("id") Integer id);

    int updateIsDeleteByRelateId(@Param("isDelete") Integer isDelete,@Param("updateId") Integer updateId,@Param("id") Integer id);

    int updateMediaRelateIsDeleteBySupplierId(@Param("isDelete") Integer isDelete,@Param("updateId") Integer updateId,@Param("supplierId") Integer supplierId);

    /**
     * 更新状态
     */
    int batchUpdateMediaRelateIsDelete(@Param("isDelete") Integer isDelete,@Param("updateId") Integer updateId,@Param("ids") List<Integer> ids);

    /**
     * 根据媒体ID批量删除关系表
     */
    int deleteByMediaIds(@Param("mediaIds") List<Integer> mediaIds);

    /**
     * 根据媒体ID删除关系表
     * @param mediaId
     * @return
     */
    int deleteByMediaId(@Param("mediaId") Integer mediaId);

    int deleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 批量拷贝媒体供应商关系审核表到媒体供应商关系表
     * @param mediaIds
     * @return
     */
    int copySupplierRelateByMediaIds(@Param("mediaIds") List<Integer> mediaIds);

    /**
     * 拷贝媒体供应商关系审核表到媒体供应商关系表
     * @param mediaId
     * @return
     */
    int copySupplierRelateByMediaId(@Param("mediaId") Integer mediaId);

    int copySupplierRelateByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据供应商ID获取关联的媒体ID列表
     * @param supplierId
     */
    List<Integer> listMediaIdBySupplierId(@Param("supplierId") Integer supplierId);
}
