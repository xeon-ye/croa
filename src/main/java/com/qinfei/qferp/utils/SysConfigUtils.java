package com.qinfei.qferp.utils;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.service.sys.ISysConfigService;

import java.util.Map;

/**
 * @CalssName: SysConfigUtils
 * @Description: 系统配置参数工具类，如果需要在mapper.xml中ognl判断if条件，请在此类增加静态方法或者静态常量，运用方式如下：
 * 1、使用静态方法
 * <if test="@com.qinfei.qferp.utils.SysConfigUtils@methodName(param)==true">
 *   select * from tableA
 * </if>
 *<select id='testSelectA' .....>
 *   select * from tableA where year=${@com.qinfei.core.utils.SysConfigUtils@methodName()}
 * </select>
 * 2、使用静态常量
 * <if test=year==@com.qinfei.qferp.utils.SysConfigUtils@CONST_NAME>
 *   select * from tableC
 * </if>
 * <select id='testSelectB' .....>
 *   select * from tableA where year=${@com.qinfei.core.utils.SysConfigUtils@CONST_NAME}
 * </select>
 * @Author: Xuxiong
 * @Date: 2020/3/14 0014 14:25
 * @Version: 1.0
 */
public class SysConfigUtils {
    /**
     * 根据Key获取系统配置的值
     * @param key 系统配置项键
     * @param cls 返回值类型：支持Integer、Float、Double、Date、List、Map类型
     */
    public static <T> T getConfigValue(String key, Class<T> cls){
        ISysConfigService sysConfigService = SpringUtils.getBean(ISysConfigService.class);
        try{
            Map<String, Map<String, Object>> config = sysConfigService.getAllConfig();
            if(config != null && config.size() > 0){
                Map<String, Object> obj = config.get(key);
                if(obj != null){
                    return (T) obj.get("value");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

