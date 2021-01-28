package com.qinfei.qferp.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: ObjectFieldCompare
 * @Description: 对象字段值比较工具类
 * @Author: Xuxiong
 * @Date: 2020/5/11 0011 10:24
 * @Version: 1.0
 */
public class ObjectFieldCompare {
    public static List<Map<String, String>> compare(Object oldObj, Object newObj){
        List<Map<String, String>> result = null;
        if(oldObj != null || newObj != null){
            try{
                Field [] fields = newObj.getClass().getDeclaredFields();
                for(Field field : fields){
                    field.setAccessible(true); //设置访问权限
                    //仅处理含有该注解的属性
                    CompareField compareField = field.getAnnotation(CompareField.class);
                    if(compareField != null){
                        String oldValue = oldObj == null ? "" : (field.get(oldObj) == null ? "" : String.valueOf(field.get(oldObj)));
                        String newValue = newObj == null ? "" : (field.get(newObj) == null ? "" : String.valueOf(field.get(newObj)));
                        //如果不相等，则记录
                        if(!oldValue.equals(newValue)){
                            //如果为空，则创建对象
                            if(result == null){
                                result = new ArrayList<>();
                            }
                            Map<String, String> tmp = new HashMap<>();
                            tmp.put("cell", field.getName()); //获取属性名
                            tmp.put("cellName", compareField.fieldName());//获取属性中文名
                            tmp.put("oldCellValue", oldValue);//旧值
                            tmp.put("newCellValue", newValue);//新值
                            result.add(tmp);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return  result;
    }


    //对象标记了该注解的字段才会参与比较
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CompareField{
        String fieldName(); //字段名称

        String message() default ""; //其他信息
    }
}
