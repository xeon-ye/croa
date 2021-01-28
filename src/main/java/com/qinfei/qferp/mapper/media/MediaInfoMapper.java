package com.qinfei.qferp.mapper.media;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media.MediaInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaInfoMapper extends BaseMapper<MediaInfo, Integer> {

//    @SelectProvider(type = ProviderUtil.class, method = "listByOrder")
//    // @Results({
//    // @Result(property = "user", column = "user_id", one = @One(select =
//    // "com.qinfei.qferp.mapper.sys.UserMapper.getById")),
//    // @Result(property = "creator", column = "user_id", one = @One(select =
//    // "com.qinfei.qferp.mapper.sys.UserMapper.getById"))}
//    // )
//    List<MediaInfo> medias(@Param("t") MediaInfo mediaInfo, @Param("orders") String... order);

    /**
     * 查询指定条件下的媒体数据；
     *
     * @param map：查询条件集合；
     * @return ：查询结果；
     */
//    List<MediaInfo> listPage(Map map);

    // @Select("select * from t_media_info where m_type=#{mType}")
    // @Results({
    // @Result(property = "user", column = "user_id", one = @One(select =
    // "com.qinfei.qferp.mapper.sys.UserMapper.getById")),
    // @Result(property = "creator", column = "user_id", one = @One(select =
    // "com.qinfei.qferp.mapper.sys.UserMapper.getById"))}
    // )
    // List<MediaPass> mediasByMediaType(@Param("mType") Integer mType);

//    @Select("select count(id) from t_media_info where m_type = #{mType} and name=#{name}")
//    int getIdByName(@Param("mType") Integer mType, @Param("name") String name);

//    @Update("update t_media_info set state=#{state} where id=#{id}")
//    int modifyStateById(@Param("state") int state, @Param("id") Integer id);


//    void updateInfo(Map<String, Object> map);

//    @Insert("insert into t_media_info select * from t_media where id=#{mediaId} ")
//    int selectToSave(@Param("mediaId") Integer mediaId);

 /*   @Update("UPDATE t_media_info a, t_media b SET a.`name`=b.`name`,a.`remarks`=b.`remarks`,a.`creator_id`=b.`creator_id`," +
            "a.`create_date`=b.`create_date`,a.`user_id`=b.`user_id`,a.`update_date`=b.`update_date`,a.`supplier_id`=b.`supplier_id`," +
            " a.`supplier_name`=b.`supplier_name`,a.`pic_path`=b.`pic_path`,a.`comm_start`=b.`comm_start`,a.`m_type`=b.`m_type`, " +
            " a.`state`=b.`state`,a.`discount`=b.`discount`,a.`d1`=b.`d1`, a.`d2`=b.`d2`,a.`n1`=b.`n1`,a.`c1`=b.`c1`,  " +
            "a.`c2`=b.`c2`,a.`c3`=b.`c3`,a.`c4`=b.`n1`,a.`n2`=b.`n2`,a.`n3`=b.`n3`, a.`n4`=b.`n4`,a.`n5`=b.`n5`," +
            "a.`n6`=b.`n6`,a.`n7`=b.`n7`,a.`n8`=b.`n8`,a.`f1`=b.f1, a.`f2`=b.`f2`, a.`f3`=b.`f3`,a.`f4`=b.`f4`,  " +
            "a.`f5`=b.`f5`,a.`f6`=b.`f6`,a.`f7`=b.`f7`,a.`f8`=b.`f8`,a.`f9`=b.`f9`,a.`f10`=b.`f10` WHERE a.`id`=b.`id` AND a.`id`=#{mediaId}")
    int selectToUpdate(@Param("mediaId") Integer mediaId);*/

/*    @Select("SELECT a.id mediaId,a.name mediaName,b.id supplierId,b.name supplierName," +
            " c.id typeId,c.name typeName,d.id mediaUserId,d.name mediaUserName\n" +
            " FROM t_media_info a \n" +
            " left join t_media_supplier b on a.supplier_id=b.id \n" +
            " left join t_media_plate c on b.media_type_id=c.id\n" +
            " left join sys_user d on a.user_id=d.id " + " WHERE a.name=#{mediaName} and c.name=#{typeName}")
    List<Map> queryMediaByNameAndType(@Param("mediaName") String mediaName, @Param("typeName") String typeName);*/

 /*   @Select("SELECT name FROM t_media_info where supplier_id = #{supplierId}")
    List<String> queryBySupplierId(@Param("supplierId") Integer supplierId);*/

//    @Select("SELECT * FROM t_media_info where state=1  ")
//    List<MediaInfo> queryAll();

    /**
     * 批量删除；
     *
     * @param map：参数集合；
     */
//    void stateBatchUpdate(Map<String, Object> map);

    /**
     * 批量复制媒体数据到下单媒体表；
     *
     * @param mediaIds：要复制的媒体ID；
     */
//    void saveBatch(List<Integer> mediaIds);
    /**
     * 批量删除单媒体表；
     *
     * @param mediaIds：要复制的媒体ID；
     */
//    void deleteBatch(List<Integer> mediaIds);

    /**
     * 查询该供应商下是否有稿件
     * @param mediaId
     * @return
     */
/*    @Select("select count(id) from t_biz_article where state>-2 and media_id=#{mediaId}")
    int getArticleMediaCount(Integer mediaId);*/

    /**
     * 查询该供应商下是否有临时稿件
     * @param mediaId
     * @return
     */
/*    @Select("select count(id) from t_biz_article_import where state>-2 and media_id=#{mediaId}")
    int getArticleImportMediaCount(Integer mediaId);*/
    /**
     * 更新媒体信息时同步更新稿件中的媒体字段
     * @param mediaInfo
     */
/*    @Update("update t_media set media_name=#{name} where media_id = #{id}")
    void updateArticleMediaInfo(MediaInfo mediaInfo);*/
    /**
     * 更新媒体信息时同步更新临时稿件中的媒体字段
     * @param mediaInfo
     */
/*    @Update("update t_media_info set media_name=#{name} where media_id = #{id}")
    void updateArticleImportMediaInfo(MediaInfo mediaInfo);*/
    /**
     * 更新媒体信息时同步更新稿件中的媒体字段
     * @param list
     */
/*    @Update({" <script>" +
            " update t_biz_article a set a.media_name=(select b.name from t_media_info b where a.media_id=b.id) " +
            " where" +
            " <foreach collection='list' separator='or' item='item' index='index'>" +
            " a.media_id=#{item} " +
            " </foreach>",
            " </script>"
    })
    void updateArticleMediaInfoBatch(List<Integer> list);*/
    /**
     * 更新媒体信息时同步更新临时稿件中的媒体字段
     * @param list
     */
 /*   @Update({" <script>" +
            " update t_biz_article_import a set a.media_name=(select b.name from t_media_info b where a.media_id=b.id) " +
            " where" +
            " <foreach collection='list' separator='or' item='item' index='index'>" +
            " a.media_id=#{item} " +
            " </foreach>",
            " </script>"
    })
    void updateArticleImportMediaInfoBatch(List<Integer> list);*/

/*    @Select({"<script>select count(id) from t_biz_article where state >= -2 and " +
            " <foreach collection='list' separator='or' item='item' index='index'>" +
            " media_id=#{item}  " +
            " </foreach>" ,
            " </script>"})
    int getArticleCount(List<Integer> list);*/
/*    @Select({"<script>select count(id) from t_biz_article_import where state >= -2 and " +
            " <foreach collection='list' separator='or' item='item' index='index'>" +
            " media_id=#{item}  " +
            " </foreach>" ,
            " </script>"})
    int getArticleImportCount(List<Integer> list);*/

    /*@Select("<script>SELECT b.name supplierName,b.contactor supplierContactor,b.phone phone," +
            " b.qqwechat qqwechat,a.name mediaName,c.name mediaTypeName,b.company_code supplierCompanyCode," +
            " a.id mediaId,b.id supplierId\n" +
            " FROM t_media_info a " +
            " left join t_media_supplier b on a.supplier_id = b.id and b.state > -2 \n" +
            " left join t_media_plate c on b.media_type_id = c.id\n" +
            " where a.state = 1 and b.media_type_id = #{mediaTypeId} " +
            " <choose>\n" +
            "  <when test=\"companyCode != 'XH'\">\n" +
            "  AND b.company_code = #{companyCode, jdbcType = VARCHAR}\n" +
            "   </when>\n" +
            "   <otherwise>\n" +
            "   </otherwise>\n" +
            "   </choose>" +
            " <when test='supplierName!=null and supplierName!=\"\"'>" +
            " AND b.name like '%${supplierName}%' " +
            " </when>" +
            " <when test='supplierContactor!=null and supplierContactor!=\"\"'>" +
            " AND b.contactor like '%${supplierContactor}%' " +
            " </when>" +
            " <when test='mediaName!=null and mediaName!=\"\"'>" +
            " AND a.name like '%${mediaName}%' " +
            " </when>" +
            "</script>")
    List<Map> getMediaInfoByTypeId(Map map);*/
}