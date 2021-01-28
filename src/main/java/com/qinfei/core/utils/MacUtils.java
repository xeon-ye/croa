//package com.qinfei.core.utils;
//
//import org.springframework.util.StringUtils;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.LineNumberReader;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * @author gzw
// * create by Administrator on 2019/5/31
// */
//public class MacUtils {
//
//    public static String callCmd(String[] cmd) {
//        StringBuffer result = new StringBuffer();
//        String line = "";
//        try {
//            Process pro = Runtime.getRuntime().exec(cmd);
//            InputStreamReader input = new InputStreamReader(pro.getInputStream());
//            BufferedReader buffer = new BufferedReader(input);
//            while ((line = buffer.readLine()) != null) {
//                result.append(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return result.toString();
//    }
//
//
//    public static String callCmd(String[] cmd, String[] another) {
//        StringBuffer result = new StringBuffer();
//        String line;
//        try {
//            Runtime rt = Runtime.getRuntime();
//            Process pro = rt.exec(cmd);
//            pro.waitFor();
//            pro = rt.exec(another);
//            InputStreamReader ins = new InputStreamReader(pro.getInputStream());
//            BufferedReader bf = new BufferedReader(ins);
//            while ((line = bf.readLine()) != null) {
//                result.append(line);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result.toString();
//    }
//
//    /*
//     * @param ip target ip
//     * @param sourceString the result string of the command executed
//     * @param macSeparator
//     * @return mac address
//     */
//
//
//    public static String filterMacAddress(String ip, String sourceString, String macSeparator) {
//        StringBuffer result = new StringBuffer();
//        String regExp = "((([0-9,A-F,a-f]{1,2}" + macSeparator + "){1,5})[0-9,A-F,a-f]{1,2})";
//        Pattern parttern = Pattern.compile(regExp);
//        Matcher matcher = parttern.matcher(sourceString);
//        while (matcher.find()) {
//            result = result.append(matcher.group(1));
//            if (sourceString.indexOf(ip) <= sourceString.lastIndexOf(ip)) {
//                break;
//            }
//        }
//        return result.toString();
//    }
//
//
//    public static String getMacWindows(final String ip) {
//        String result = "";
//        String[] cmd = {"cmd", "/c", "ping " + ip};
//        String[] another = {"cmd", "/c", "arp -a"};
//        String cmdResult = callCmd(cmd, another);
//        result = filterMacAddress(ip, cmdResult, "-");
//        return result;
//    }
//
//
//    public static String getMacLinux(final String ip) {
//        String result = "";
//        String[] cmd = {"/bin/sh", "-c", "ping " + ip + " -c 2 && arp -a"};
//        String cmdResult = callCmd(cmd);
//        result = filterMacAddress(ip, cmdResult, ":");
//        return result;
//    }
//
//
//
//    public static String getMac(String ip) {
//        String macAddress = getMacWindows(ip).trim();
//        if (macAddress == null || "".equals(macAddress)) {
//            macAddress = getMacLinux(ip);
//        }
//        return macAddress.toUpperCase();
//    }
//
//    public static void main(String[] args) {
//
//    }
//
//}
//
//
