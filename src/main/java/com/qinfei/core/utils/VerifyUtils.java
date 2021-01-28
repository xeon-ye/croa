package com.qinfei.core.utils;

import com.qinfei.qferp.entity.sys.Resource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * 判断是否有权限访问
 *
 * @author GZW
 */
public class VerifyUtils {
    /**
     * 判断权限是否存在
     */
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public static boolean isContains(Collection<Resource> resources, String code) {
        if (resources != null)
            for (Resource resource : resources) {
                List<Resource> childs = resource.getChilds();
                String url = resource.getUrl();
//                if (StringUtils.isEmpty(url))
//                    return true;
                if (childs == null || childs.size() == 0) {
                    if (url.indexOf("/") > 0) url = "/" + url;
                    if (resource.getState() == 0 && antPathMatcher.match(url.trim(), code.trim())) {// 有权访问该资源
                        return true;
                    }
                } else {
                    if (isContains(childs, code)) return true;
                }
            }
        return false;
    }

    public boolean urlIsContains(Collection<String> urls, String code) {
        if (urls != null)
            for (String url : urls) {
                if (antPathMatcher.match(url.trim(), code.trim())) {// 有权访问该资源
                    return true;
                }
            }
        return false;
    }
}
