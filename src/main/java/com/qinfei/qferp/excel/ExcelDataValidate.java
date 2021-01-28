package com.qinfei.qferp.excel;


import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @CalssName ExcelDataValidate
 * @Description Excel对象校验工具
 * @Author xuxiong
 * @Date 2020/1/2 0002 17:36
 * @Version 1.0
 */
public class ExcelDataValidate {

    //字段非空校验，仅支持当前对象属性，属性为对象不支持
    public static String validateNull(Object obj){
        String message = "";
        if(obj != null){
            try {
                Field [] fields = obj.getClass().getDeclaredFields();
                for(Field field : fields){
                    // 序列化ID需要跳过；
                    if ("serialVersionUID".equals(field.getName())) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    NotBlank notBlank = field.getAnnotation(NotBlank.class);
                    if(notBlank != null){
                        if(field.getType().equals(String.class)){
                            if(StringUtils.isEmpty(value)){
                                message = !StringUtils.isEmpty(notBlank.message()) ? notBlank.message() : String.format("%s不能为空", field.getName());
                                break;
                            }else {
                                field.set(obj, String.valueOf(value).trim()); //去空字符
                            }
                        }else {
                            if(value == null){
                                message = !StringUtils.isEmpty(notBlank.message()) ? notBlank.message() : String.format("%s不能为空", field.getName());
                                break;
                            }
                        }
                    }else {
                        if(field.getType().equals(String.class) && !StringUtils.isEmpty(value)){
                            field.set(obj, String.valueOf(value).trim()); //去空字符
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return message;
    }

    //字段非空和下拉选项值校验，仅支持当前对象属性，属性为对象不支持
    public static String valdateNullAndSelect(Object obj, Class<ValidSelectField> validSelectFieldClass){
        String message = "";
        if(obj != null){
            try {
                Field [] fields = obj.getClass().getDeclaredFields();
                for(Field field : fields){
                    // 序列化ID需要跳过；
                    if ("serialVersionUID".equals(field.getName())) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    NotBlank notBlank = field.getAnnotation(NotBlank.class);
                    if(notBlank != null){
                        if(field.getType().equals(String.class)){
                            if(StringUtils.isEmpty(value)){
                                message = !StringUtils.isEmpty(notBlank.message()) ? notBlank.message() : String.format("%s不能为空", field.getName());
                                return message;
                            }else {
                                field.set(obj, String.valueOf(value).trim()); //去空字符
                            }
                        }else {
                            if(value == null){
                                message = !StringUtils.isEmpty(notBlank.message()) ? notBlank.message() : String.format("%s不能为空", field.getName());
                                return message;
                            }
                        }
                    }else {
                        if(field.getType().equals(String.class) && !StringUtils.isEmpty(value)){
                            field.set(obj, String.valueOf(value).trim()); //去空字符
                        }
                    }
                    ValidSelectField validSelectField = field.getAnnotation(validSelectFieldClass);
                    if(validSelectField != null){
                        String [] selectValueArr = validSelectField.selectOptionArr();
                        if(selectValueArr != null && selectValueArr.length > 0 && value != null && "".equals(value)){
                            List<String> optionList = Arrays.asList(selectValueArr);
                            if(!optionList.contains(value)){
                                message = !StringUtils.isEmpty(validSelectField.message()) ? validSelectField.message() : String.format("%s不支持该选项", field.getName());
                                return message;
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return message;
    }

}

//下拉列表字段值校验
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface ValidSelectField{
    String message() default ""; //错误提示消息

    String [] selectOptionArr() default {}; //下拉列表描述值
}
