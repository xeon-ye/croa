package com.qinfei.qferp.utils;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public  class CodeUtil {
    private static final Calendar calendar = Calendar.getInstance() ;
    public static int getYear(){
        return calendar.get(Calendar.YEAR) ;
    }
    public static int getMonth(){
        return calendar.get(Calendar.MONTH)+1 ;
    }
    private static int getDay(){
        return calendar.get(Calendar.DATE) ;
    }
    //生成年月日
    public static String getDayStr(){
        return String.valueOf(getYear())+String.valueOf(getMonth())+String.valueOf(getDay()) ;
    }
    public static String getMonthStr(){
        return String.valueOf(getYear())+String.valueOf(getMonth()) ;
    }
    //生成0001
    public synchronized static String getFourCode(long value,int length) {
        String valueStr=String.valueOf(value);
        int end=length-(valueStr.length());
        for (int i=0;i<end;i++){
            valueStr ="0"+valueStr;
        }
        return valueStr;
    }

    public static boolean isStartWithNumber(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str.charAt(0)+"");
        return isNum.matches();
    }


}
