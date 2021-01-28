package com.qinfei.qferp.mapper.media;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.media.Media;

public interface MediaMapper extends BaseMapper<Media, Integer> {
	// @SelectProvider(type = ProviderUtil.class, method = "listByOrder")
	// @Select({"<script>select * from t_media a where a where 1=1" + "<when
	// test='id!=null and id!=\"\"'>", "AND id = #{id}", "</when>", "</script>"})
	// List<Media> medias(Media media, String... order);

	/**
	 * 查询所有正常的数据；
	 *
	 * @param map：媒体信息；
	 * @return ：媒体集合；
	 */
//	List<Media> listNormalData(Map map);

	/**
	 * 查询指定条件下的媒体数据；
	 *
	 * @param map：查询条件集合；
	 * @return ：查询结果；
	 */
//	List<Media> listPage(Map map);

	/**
	 * 查询供应商下是否与媒体关联
	 *
	 * @param id
	 * @return
	 */
//	@Select("select count(*) from t_media where state > -2 and supplier_id = #{id}")
//	int findSupplierMediaCount(Integer id);

	// @Select("select * from t_media where m_type=#{mType}")
	// @Results({
	// @Result(property = "user", column = "user_id", one = @One(select =
	// "com.qinfei.qferp.mapper.sys.UserMapper.getById")),
	// @Result(property = "creator", column = "user_id", one = @One(select =
	// "com.qinfei.qferp.mapper.sys.UserMapper.getById"))}
	// )
	// List<Media> mediasByMediaType(@Param("mType") Integer mType);

/*	@Select("select count(id) from t_media where state >= 0 and m_type = #{mType} and name = #{name}")
	int getIdByName(@Param("mType") Integer mType, @Param("name") String name);*/

/*	@Update("update t_media set state=${state} where id=#{id}")
	int modifyStateById(@Param("state") int state, @Param("id") Integer id);*/

	/*
	*媒体审核查询数量
	*如果部长的code 是xh，然后就统计所有的媒体，如果不是XH，就统计自己公司的媒体数量（需要查询到媒体供应商的公司标志）。
	*
	* */
//	@Select("select count(m_type) total,m_type type from t_media where (state = 0 or state = 2)  group by m_type")
//	List<Map> getMediaNumber();

//	@Select("select count(t.m_type) total, t.m_type type from t_media t left join t_media_supplier s on t.supplier_id = s.id where (t.state =0 or t.state = 2) and " +
//			"s.company_code = #{companyCode} group by t.m_type")
//			List<Map> statistical (String companyCode);


	/*@Select({"<script>" +
			" SELECT count(id) FROM t_media " +
			" where state = 1 and id in " +
			"   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">\n" +
			"       #{item}" +
			"   </foreach>" +
			"</script>"})
	Integer mediaState(List<Integer> list);

	@Update({"<script>" +
			" update t_media set state= -9  " +
			" where user_id=#{userId} and id in " +
			"   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">\n" +
			"       #{item}" +
			"   </foreach>" +
			"</script>"})
	Integer userDelete(@Param("list")List<Integer> list,@Param("userId")Integer userId);*/

	/**
	 * 批量保存数据；
	 * 
	 * @param medias：媒体数据集合；
	 */
//	void saveBatch(List<Media> medias);

	/**
	 * 批量删除；
	 * 
	 * @param map：参数集合；
	 */
//	void stateBatchUpdate(Map<String, Object> map);

	/**
	 * 查询是否存在重复；
	 * 
	 * @param map：查询条件；
	 * @return ：查询结果数量；
	 */
//	int checkRepeat(Map map);
}