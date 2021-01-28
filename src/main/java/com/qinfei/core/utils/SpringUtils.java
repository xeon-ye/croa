package com.qinfei.core.utils;

import com.qinfei.core.config.Config;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class SpringUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    private static Map<String, String> propertiesMap = null;

    public SpringUtils() {
        super();
    }

    /**
     * 根据key 获取值
     *
     * @param key key
     * @return 返回配置文件的值
     */
    public static String get(String key) {
        if (propertiesMap == null) {
            Config ymlConfig = applicationContext.getBean(Config.class);
            propertiesMap = ymlConfig.getCustConfig();
        }
        return propertiesMap.get(key);
    }

    /**
     * 根据Bean名称获取JavaBean
     *
     * @param beanName
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 根据Bean类型获取JavaBean
     *
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> cls) {
        return (T) applicationContext.getBean(cls);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtils.applicationContext == null) {
            SpringUtils.applicationContext = applicationContext;
        }
    }
}
