package com.qinfei.core.utils;

import java.util.ArrayList;
import java.util.List;

import com.qinfei.core.data.DictiEnum;
import com.qinfei.core.entity.Dict;
import org.springframework.util.StringUtils;

class DictiUtils {
    /**
     * <p>Description: 根据数据字典code返回name，如未找到则返回null </p>
     *
     * @param code 如果code 为空null则返回null
     */
    public static String getName(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (DictiEnum typeEnum : DictiEnum.values()) {
            for (Dict dictionary : typeEnum.getDicties()) {
                if (code.equals(dictionary.getCode())) {
                    return dictionary.getName();
                }
            }
        }
        return null;
    }

    /**
     * <p>Description: 根据数据字典code和类别返回name，如未找到则返回null </p>
     *
     * @param code 如果code 为空null则返回null
     */
    public static String getName(String code, DictiEnum typeEnum) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (Dict dictionary : typeEnum.getDicties()) {
            if (code.equals(dictionary.getCode())) {
                return dictionary.getName();
            }
        }
        return null;
    }

    /**
     * <p>Description: 根据字典类型返回返回类型树，若该类型不是树返回普通的list </p>
     *
     * @param typeEnum
     * @Author 韩亚华
     * @Date 2017-5-16
     */
    public static List<Dict> getTrees(DictiEnum typeEnum) {
        if (!typeEnum.isTree()) {
            return typeEnum.getDicties();
        }
        List<Dict> trees = new ArrayList<Dict>();
        for (Dict dictionary : typeEnum.getDicties()) {
            if (-1 == dictionary.getParentId()) {
                trees.add(dictionary);
            }
        }
        return trees;
    }

    /**
     * <p>Description: 根据字典code返回字典元素，如果是树返回该元素和子树（未找到则返回null）</p>
     *
     * @param code 如果为null或""则返回null
     * @Author 韩亚华
     * @Date 2017-5-16
     */
    public static Dict get(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (DictiEnum typeEnum : DictiEnum.values()) {
            for (Dict dictionary : typeEnum.getDicties()) {
                if (code.equals(dictionary.getCode())) {
                    return dictionary;
                }
            }
        }
        return null;
    }

    /**
     * <p>Description: 根据字典code和字典类型返回字典元素，如果是树返回该元素和子树（未找到则返回null）</p>
     *
     * @param code 如果为null或""则返回null
     * @Author 韩亚华
     * @Date 2017-5-16
     */
    public static Dict get(String code, DictiEnum typeEnum) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (Dict dictionary : typeEnum.getDicties()) {
            if (code.equals(dictionary.getCode())) {
                return dictionary;
            }
        }
        return null;
    }
}