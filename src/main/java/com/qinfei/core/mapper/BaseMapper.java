package com.qinfei.core.mapper;

import com.qinfei.core.utils.ProviderUtil;
import org.apache.ibatis.annotations.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface BaseMapper<T, Id extends Serializable> {


    @SelectProvider(type = ProviderUtil.class, method = "get")
    T get(@Param("arg0") Class<T> cls, @Param("id") Id id);

    @InsertProvider(type = ProviderUtil.class, method = "insert")
    @Options(useGeneratedKeys = true)
    Integer insert(T t);

    @DeleteProvider(type = ProviderUtil.class, method = "delById")
    int delById(@Param("arg0") Class<T> cls, @Param("id") Id id);

    @DeleteProvider(type = ProviderUtil.class, method = "delete")
    int delete(T t);

    @UpdateProvider(type = ProviderUtil.class, method = "update")
    int update(T t);

    @SelectProvider(type = ProviderUtil.class, method = "list")
    List<T> list(T t);

    @SelectProvider(type = ProviderUtil.class, method = "listByOrder")
    List<T> listByOrder(@Param("t") T t, @Param("orders") String... orders);

    @SelectProvider(type = ProviderUtil.class, method = "all")
    List<T> all(Class<T> cls);

    @SelectProvider(type = ProviderUtil.class, method = "dictSQL")
    List<Map<String, Object>> dictSQL(String sql);

    @SelectProvider(type = ProviderUtil.class, method = "listBySQLAndMap")
    List<Map<String, Object>> listBySQLAndMap(String sql, Map<String, Object> map);

    @SelectProvider(type = ProviderUtil.class, method = "dictSQLByObject")
    List<T> dictSQLByObject(String sql, @Param("t") T t);


}
