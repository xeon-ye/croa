package com.qinfei.qferp.utils;

import org.springframework.util.StringUtils;

import java.util.Map;

public class PageOrder {

    public static String getOrderStr(Map map){
        if(!StringUtils.isEmpty(map.get("sidx"))){
            String sidx = (String)map.get("sidx");
            String sord = (String)map.get("sord");
            return " "+sidx+" "+sord;
        }
        return "";
    }
}
