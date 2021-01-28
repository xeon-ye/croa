package com.qinfei.core.utils;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Relate;
import com.qinfei.core.annotation.Table;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import com.qinfei.core.annotation.Column;
import com.qinfei.core.annotation.Transient;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author gong.zhiwei
 */
public class ProviderUtil {

    public <T> String dictSQL(String sql) {
        return sql;
    }

    public <T> String dictSQLByObject(String sql) {
        return sql;
    }

    /**
     * 根据条件查询数据列表
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> String list(T t) throws IllegalAccessException {
        Class<?> cls = t.getClass();
        String tableName = cls.getAnnotation(Table.class).name();
        Field[] fields = cls.getDeclaredFields();
        return new SQL() {{
            FROM(tableName);
            for (Field field : fields) {
                Transient trans = field.getAnnotation(Transient.class);
                if (trans != null)
                    continue;
                Column column = field.getAnnotation(Column.class);
                String colName = null;
                if (column != null) {
                    colName = column.name();
                } else {
                    String name = field.getName();
                    colName = StrUtil.underscoreName(name);
                }
                if (colName != null) {
                    String name = field.getName();
                    SELECT("`" + colName + "`");
                    field.setAccessible(true);
                    Object value = field.get(t);
                    if (value != null) {
                        Id id = field.getAnnotation(Id.class);
                        if (id != null)
                            continue;
                        if (value.toString().indexOf('%') < 0)
                            WHERE("`" + colName + "` = #{" + name + "}");
                        else
                            WHERE("`" + colName + "` like #{" + name + "}");
                    }
                }
            }
        }}.toString();
    }

    /**
     * 根据条件查询数据列表
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> String listByOrder(@Param("t") T t, @Param("orders") String... orders) throws IllegalAccessException {
        Class<?> cls = t.getClass();
        String tableName = cls.getAnnotation(Table.class).name();
        Field[] fields = cls.getDeclaredFields();
        return new SQL() {{
            FROM(tableName);
            for (Field field : fields) {
                Transient trans = field.getAnnotation(Transient.class);
                if (trans != null)
                    continue;
                Column column = field.getAnnotation(Column.class);
                String colName = null;
                if (column != null) {
                    colName = column.name();
                } else {
                    String name = field.getName();
                    colName = StrUtil.underscoreName(name);
                }
                if (colName != null) {
                    String name = field.getName();
                    field.setAccessible(true);
                    Object value = field.get(t);
                    SELECT("`" + colName + "`");
                    if (value != null) {
                        Id id = field.getAnnotation(Id.class);
                        if (id != null)
                            continue;
                        WHERE("`" + colName + "` = #{param1." + name + "}");
                    }
                }
            }
            if (orders != null)
                ORDER_BY(orders);
        }}.toString();
    }

    /**
     * 根据条件查询数据列表
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> String page(T t) throws IllegalAccessException {
        Class<?> cls = t.getClass();
        String tableName = cls.getAnnotation(Table.class).name();
        Field[] fields = cls.getDeclaredFields();
        return new SQL() {{
            FROM(tableName);
            for (Field field : fields) {
                Transient trans = field.getAnnotation(Transient.class);
                if (trans != null)
                    continue;
                Column column = field.getAnnotation(Column.class);
                String colName = null;
                if (column != null) {
                    colName = column.name();
                } else {
                    String name = field.getName();
                    colName = StrUtil.underscoreName(name);
                }
                if (colName != null) {
                    String name = field.getName();
                    field.setAccessible(true);
                    Object value = field.get(t);
                    SELECT('`' + colName + "`");
                    if (value != null) {
                        WHERE('`' + colName + "`=#{" + name + "}");
                    }
                }
            }
        }}.toString();
    }

    /**
     * 根据条件查询数据列表
     *
     * @param map
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> String listBySQLAndMap(String sql, Map<String, Object> map) {
        if (map == null)
            return sql;
        String[] sql1s = sql.split("(?i)order by");
        sql = sql1s[0];
        String order = "";
        if (sql1s.length > 1) {
            order = sql1s[1];
        }
        StringBuilder sqls = new StringBuilder(sql);
        if (map != null && !map.isEmpty()) {
            if (sql.toLowerCase().indexOf("where") < 0)
                sqls.append(" where ");
            else
                sqls.append(" and ");
            map.forEach((k, v) -> {
                sqls.append("`" + k.replace("+", ".") + "`=#{param2." + k + "}").append(" and ");
            });
            sqls.delete(sqls.length() - 4, sqls.length());
        }
        sqls.append(" order by ").append(order);
        return sqls.toString();
    }

    /**
     * 查询所有数据
     *
     * @param cls
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> String all(Class<T> cls) throws IllegalAccessException {
        String tableName = cls.getAnnotation(Table.class).name();
        return new SQL() {{
            SELECT("*");
            FROM(tableName);
        }}.toString();
    }

    /**
     * 保存数据
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    @Options(useGeneratedKeys = true,keyProperty="id")
    public <T> String insert(T t) throws IllegalAccessException {
        Class<?> cls = t.getClass();
        String tableName = cls.getAnnotation(Table.class).name();
        Field[] fields = cls.getDeclaredFields();
        return new SQL() {{
            INSERT_INTO(tableName);
            for (Field field : fields) {
                Relate relate = field.getAnnotation(Relate.class);
                if (relate != null) {
                    field.setAccessible(true);
                    Object relateObj = field.get(t);
                    if (relateObj != null) {
                        Class<?> name = relate.name();
                        String fkName = relate.fkName();
                        Field[] fields1 = name.getDeclaredFields();
                        for (Field field1 : fields1) {
                            Id id = field1.getAnnotation(Id.class);
                            String idName = null;
                            if (id != null) {
                                idName = field1.getName();
                            } else {
                                String cname = field.getName();
                                idName = StrUtil.underscoreName(cname);
                            }
//                            Column idColumn = field1.getAnnotation(Column.class);
                            field1.setAccessible(true);
                            Object value = field1.get(relateObj);
                            if (value != null && fkName != null) {
                                VALUES("`" + fkName + "`", "#{" + field.getName() + "." + idName + "}");
                                break;
                            }
                        }
                    }
                } else {
                    Transient trans = field.getAnnotation(Transient.class);
                    if (trans != null)
                        continue;
                    Column column = field.getAnnotation(Column.class);
                    String colName = null;
                    if (column != null) {
                        colName = column.name();
                    } else {
                        String name = field.getName();
                        colName = StrUtil.underscoreName(name);
                    }
                    if (colName != null) {
                        String name = field.getName();
                        field.setAccessible(true);
                        Object value = field.get(t);
                        if (value != null) {
                            VALUES("`" + colName + "`", "#{" + name + "}");
                        }
                    }
                }

            }
        }}.toString();
    }

    /**
     * 根据主键获取唯一数据
     *
     * @param cls
     * @param id
     * @param <T>
     * @return
     */
    public <T> String get(@Param("arg0") Class<T> cls, @Param("id") Serializable id) {
        String tableName = cls.getAnnotation(Table.class).name();
        Field[] fields = cls.getDeclaredFields();
        return new SQL() {{
            SELECT("*");
            FROM(tableName);
            for (Field field : fields) {
                Transient trans = field.getAnnotation(Transient.class);
                if (trans != null)
                    continue;
                Id fid = field.getAnnotation(Id.class);
                if (fid != null && id != null) {
                    Column column = field.getAnnotation(Column.class);
                    String colName = null;
                    if (column != null) {
                        colName = column.name();
                    } else {
                        String name = field.getName();
                        colName = StrUtil.underscoreName(name);
                    }
                    WHERE("`" + colName + "`= #{id}");
                    break;
                }
            }
        }}.toString();
    }

    /**
     * 根据主键删除数据方法
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> String delete(T t) throws IllegalAccessException {
        Class<?> cls = t.getClass();
        String tableName = cls.getAnnotation(Table.class).name();
        Field[] fields = cls.getDeclaredFields();
        return new SQL() {{
            DELETE_FROM(tableName);
            for (Field field : fields) {
                Transient trans = field.getAnnotation(Transient.class);
                if (trans != null)
                    continue;
                Column column = field.getAnnotation(Column.class);
                Id idCol = field.getAnnotation(Id.class);
                String colName = null;
                if (column != null) {
                    colName = column.name();
                } else {
                    String name = field.getName();
                    colName = StrUtil.underscoreName(name);
                }
                if (colName != null && idCol != null) {
                    String name = field.getName();
                    field.setAccessible(true);
                    Object value = field.get(t);
                    if (value != null) {
                        WHERE("`" + colName + "`=#{" + name + " }");
                    }
                }
            }
        }}.toString();
    }

    /**
     * 根据主键删除数据方法
     *
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> String delById(@Param("arg0") Class<T> cls, @Param("id") Integer id) throws IllegalAccessException, InstantiationException {
        String tableName = cls.getAnnotation(Table.class).name();
        Field[] fields = cls.getDeclaredFields();
        return new SQL() {{
            DELETE_FROM(tableName);
            for (Field field : fields) {
                field.setAccessible(true);
                Id idCol = field.getAnnotation(Id.class);
                if (id != null && idCol != null) {
                    String colName = field.getName();
                    WHERE("`" + colName + "`=#{id}");
                }

            }
        }}.toString();
    }

    /**
     * 根据主键更新数据方法
     *
     * @param t
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public <T> String update(T t) throws IllegalAccessException {
        Class<?> cls = t.getClass();
        String tableName = cls.getAnnotation(Table.class).name();
        Field[] fields = getAllFields(cls);
        return new SQL() {
            {
                UPDATE(tableName);
                for (Field field : fields) {
                    Transient trans = field.getAnnotation(Transient.class);
                    if (trans != null)
                        continue;
                    Relate relate = field.getAnnotation(Relate.class);
                    if (relate != null) {
                        field.setAccessible(true);
                        Object relateObj = field.get(t);
                        if (relateObj != null) {
                            Class<?> name = relate.name();
                            String fkName = relate.fkName();
                            Field[] fields = name.getDeclaredFields();
                            for (Field field1 : fields) {
                                Id id = field1.getAnnotation(Id.class);
                                String idName = null;
                                if (id != null) {
                                    idName = field1.getName();
                                } else {
                                    String cname = field.getName();
                                    idName = StrUtil.underscoreName(cname);
                                }
                                field1.setAccessible(true);
                                Object value = field1.get(relateObj);
                                if (value != null && fkName != null) {
                                    SET("`" + fkName + "`= #{" + field.getName() + "." + idName + "}");
//                                    VALUES("`" + fkName + "`", "#{" + field.getName() + "." + idName + "}");
                                    break;
                                }
                            }
                        }
                    } else {
                        Column column = field.getAnnotation(Column.class);
                        String colName = null;
                        if (column != null) {
                            colName = column.name();
                        } else {
                            String name = field.getName();
                            colName = StrUtil.underscoreName(name);
                        }
                        if (colName != null) {
                            field.setAccessible(true);
                            Object value = field.get(t);
                            if (value != null) {
                                String name = field.getName();
                                Id fid = field.getAnnotation(Id.class);
                                if (fid != null) {
                                    WHERE("`" + colName + "`= #{" + name + "}");
                                } else {
                                    SET("`" + colName + "`= #{" + name + "}");
                                }
                            }
                        }
                    }
                }
            }
        }.toString();
    }

    /**
     * 获取本类及其父类的属性的方法
     * @param clazz 当前类对象
     * @return 字段数组
     */
    private static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null){
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        return fieldList.toArray(fields);
    }
}
