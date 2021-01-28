package com.qinfei.qferp.service.media1;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.biz.ArticleReplace;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.qinfei.qferp.entity.media1.MediaChange;
import com.qinfei.qferp.entity.media1.MediaForm1;
import com.qinfei.qferp.entity.media1.MediaSupplierChange;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @CalssName IMediaAuditService
 * @Description 媒体接口
 * @Author xuxiong
 * @Date 2019/6/26 0026 9:55
 * @Version 1.0
 */
public interface IMediaAuditService {
    String CACHE_KEY = "mediaAudit";
    String CACHE_KEY1 = "media1";

    /**
     * 根据页面传递的集合信息查询待审核的媒体；
     *
     * @param map：查询条件集合；
     * @param pageable：分页对象；
     * @return ：分页完成的数据集合；
     */
    PageInfo<MediaAudit> listAuditMedia(Map<String, Object> map, Pageable pageable);

    /**
     * 根据页面传递的集合信息查询待审核的媒体供应商关系；
     *
     * @param map：查询条件集合；
     * @param pageable：分页对象；
     * @return ：分页完成的数据集合；
     */
    PageInfo<MediaAudit> listAuditMediaSupplier(Map<String, Object> map, Pageable pageable);

    /**
     * 根据页面传递的集合信息查询所有媒体；
     *  -> 媒体管理不区分公司编码
     * @param map：查询条件集合；
     * @param pageable：分页对象；
     * @return ：分页完成的数据集合；
     */
    PageInfo<MediaAudit> listMedia(Map<String, Object> map, Pageable pageable);

    /**
     * 根据页面传递的集合信息查询所有媒体关系；
     *  -> 媒体关系管理不区分公司编码
     * @param map：查询条件集合；
     * @param pageable：分页对象；
     * @return ：分页完成的数据集合；
     */
    PageInfo<MediaAudit> listMediaSupplier(Map<String, Object> map, Pageable pageable);

    /**
     * 根据ID获取媒体所有信息
     */
    @Cacheable(value = CACHE_KEY, key = "'mediaId='+#id")
    MediaAudit getEditMediaById(Integer id);

    /**
     * 根据媒体ID获取供应商信息和价格
     */
//    @Cacheable(value = CACHE_KEY, key = "'supplierInfo='+#id")
    List<Map> getMediaSupplierInfoByMediaId(Integer id);

    /**
     * 根据页面查询
     * @param auditPageFlag 是否审核页面标识
     * @return
     */
    List<Map> getMediaNumbers(Integer auditPageFlag);

    /**
     * 根据页面查询
     * @param auditPageFlag 是否审核页面标识
     * @return
     */
    List<Map> getMediaSupplierNumbers(Integer auditPageFlag);

    void validationMediaName(Integer id, String mediaName, Integer plateId);

    void validationMediaContentId(Integer id, String mediaContentId, Integer plateId);

    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void save(MediaAudit mediaAudit);

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void update(MediaAudit mediaAudit);

    @Cacheable(value = CACHE_KEY, key = "'id='+#id")
    MediaAudit getById(Integer id);

    /**
     * 审核通过
     *
     * @param id
     * @return
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void pass(Integer id);

    /**
     * 审核通过
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void passRelate(Integer mediaId, Integer relateId);

    /**
     * 媒体拷贝（会拷贝所有，但是不会去覆盖，暂时不用）
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void copy(Integer id);

    /**
     * 仅媒体拷贝
     * @param id
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void copyMedia(Integer id);

    /**
     * 拷贝媒体供应商关系
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void copyMediaRelate(Integer id);

    /**
     * 停用
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void stop(Integer id,  Integer standarPlatformFlag);

    /**
     * 停用
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void stopRelate(Integer mediaId, Integer relateId, Integer standarPlatformFlag);

    /**
     * 启用
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void active(Integer id, Integer standarPlatformFlag);

    /**
     * 启用
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void activeRelate(Integer mediaId, Integer relateId, Integer standarPlatformFlag);

    /**
     * 审核驳回
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void reject(Integer id);

    /**
     * 审核驳回
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void rejectRelate(Integer mediaId, Integer relateId);

    /**
     * 审核删除
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void deleteMedia(Integer id, Integer standarPlatformFlag);

    /**
     * 媒体关系删除
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void deleteMediaRelate(Integer mediaId, Integer supplierId, Integer relateId, Integer standarPlatformFlag);

    /**
     * 批量通过；
     *
     * @param ids：媒体ID数组；
     * @param mediaNames：媒体名称数组；
     * @param userIds：用户ID数组；
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void passBatch(List<Integer> ids, List<String> mediaNames, List<Integer> userIds);

    /**
     * 批量通过；
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void passBatchRelate(List<Integer> mediaIds, List<String> mediaNames, List<Integer> userIds, List<Integer> relateIds);

    /**
     * 批量驳回；
     *
     * @param ids：媒体ID数组；
     * @param mediaNames：媒体名称数组；
     * @param userIds：用户ID数组；
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void rejectBatch(List<Integer> ids, List<String> mediaNames, List<Integer> userIds);

    /**
     * 批量驳回；
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void rejectBatchRelate(List<Integer> mediaIds, List<String> mediaNames, List<Integer> userIds, List<Integer> relateIds);

    /**
     * 批量删除；
     *
     * @param ids：媒体ID数组；
     * @param mediaNames：媒体名称数组；
     * @param userIds：用户ID数组；
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void deleteBatch(List<Integer> ids, List<String> mediaNames, List<Integer> userIds, Integer standarPlatformFlag);

    /**
     * 批量删除；
     *
     * @param mediaIds：媒体ID数组；
     * @param mediaNames：媒体名称数组；
     * @param userIds：用户ID数组；
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void deleteBatchRelate(List<Integer> mediaIds, List<String> mediaNames, List<Integer> userIds, List<String> mediaSupplierRelates, List<Integer> relateIds, Integer standarPlatformFlag);

    /**
     * 生成指定板块的数据导入模板；
     * @param standarPlatformFlag 板块标准标识
     * @param mediaType：媒体板块类型；
     * @param plateName 媒体板块名称
     * @param templateName：模板名称；
     * @param outputStream：响应的输出流；
     */
    void getDataImportTemplate(Integer standarPlatformFlag, int mediaType,String plateName, String templateName, OutputStream outputStream);

    /**
     * 批量保存媒体数据；
     *
     * @param fileName：文件路径；
     * @param plateId：媒体板块ID；
     * @param plateName 媒体板块名称
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    String batchAddMedia(String fileName, Integer standarPlatformFlag, Integer plateId, String plateName) throws IOException;

    /**
     * 批量保存媒体数据；
     *
     * @param fileName：文件路径；
     * @param plateId：媒体板块ID；
     * @param plateName 媒体板块名称
     * @param fileType 导入文件类型： 1-供应商、2-媒体、3-供应商价格
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    String batchAddMedia1(File file, Integer standarPlatformFlag, Integer plateId, String plateName, Integer fileType) throws IOException;

    /**
     * 导出媒体数据
     */
    void batchExport(OutputStream outputStream, Map<String, Object> map);

    /**
     * 导出选择的媒体数据
     */
    void batchChooseExport(OutputStream outputStream, Integer standarPlatformFlag, Integer plateId,String plateName,List<Integer> mediaIds,List<String> relateIds);

    /**
     * 媒体导入替换
     */
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    String batchReplaceImport(String fileName, Integer standarPlatformFlag, Integer plateId, String plateName, Integer fileType) throws IOException;

    //获取媒体字段列表
    List<MediaForm1> listMediaField(int plateId);

    //根据媒体ID获取异动列表
    List<MediaChange> listMediaChange(String mediaIds);

    //媒体异动恢复
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void mediaChangeRecover(int mediaChangeId, Integer standarPlatformFlag);

    //根据媒体关系ID获取异动列表
    List<MediaSupplierChange> listMediaSupplierChange(String relateIds);

    //媒体供应商异动恢复
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY, allEntries = true),
            @CacheEvict(value = CACHE_KEY1, allEntries = true)
    })
    void mediaSupplierChangeRecover(int mediaSupplierChangeId, Integer standarPlatformFlag);

    /**
     * 导出媒体数据
     */
    void batchChangeChooseExport(OutputStream outputStream, String plateName, List<Integer> mediaIds);

    /**
     * 导出媒体数据
     */
    void mediaChangeBatchExport(OutputStream outputStream, Map<String, Object> map);

    //更新媒体责任人
    void updateMediaUserId(Integer id, Integer userId);

    //分页查询历史稿件列表
    PageInfo<Map<String, Object>> listHistoryArtByParam(Integer mediaTypeId, String keyword, Pageable pageable);

    //历史稿件媒体供应商替换
    void artMediaSupplierReplace(ArticleReplace articleReplace);

    //数据迁移
    String transfer();

    void  updateArticle();
}
