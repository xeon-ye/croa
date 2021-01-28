package com.qinfei.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;

/**
 * 自定义访问对象工具类
 * <p>
 * 获取对象的IP地址等信息
 *
 * @author GZW
 */
@Slf4j
public class IpUtils {
    final static String MAC_ADDRESS_PREFIX = "MAC Address = ";
    final static String LOOPBACK_ADDRESS = "127.0.0.1";
//    final static UdpGetClientMacAddr umac = new UdpGetClientMacAddr(sip);

    /**
     * 获取用户真实IP地址，
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            log.error(ip);
            return ip;
        }
        ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ip = inet.getHostAddress();
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) { //"***.***.***.***".length() = 15
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    /**
     * 通过IP地址获取MAC地址
     *
     * @param ip String,127.0.0.1格式
     * @return mac String
     * @throws Exception
     */
//    public static String getMACAddress(String ip) throws Exception {
//        String line = "";
//        String macAddress = "";
//        //如果为127.0.0.1,则获取本地MAC地址。
//        if (LOOPBACK_ADDRESS.equals(ip)) {
//            InetAddress inetAddress = InetAddress.getLocalHost();
//            byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
//            //下面代码是把mac地址拼装成String
//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < mac.length; i++) {
//                if (i != 0) {
//                    sb.append("-");
//                }
//                //mac[i] & 0xFF 是为了把byte转化为正整数
//                String s = Integer.toHexString(mac[i] & 0xFF);
//                sb.append(s.length() == 1 ? 0 + s : s);
//            }
//            //把字符串所有小写字母改为大写成为正规的mac地址并返回
//            macAddress = sb.toString().trim().toUpperCase();
//            return macAddress;
//        }
//        //获取非本地IP的MAC地址
//        try {
//            Process p = Runtime.getRuntime().exec("nbtstat -A " + ip);
//            InputStreamReader isr = new InputStreamReader(p.getInputStream());
//            BufferedReader br = new BufferedReader(isr);
//            while ((line = br.readLine()) != null) {
//                if (line != null) {
//                    int index = line.indexOf(MAC_ADDRESS_PREFIX);
//                    if (index != -1) {
//                        macAddress = line.substring(index + MAC_ADDRESS_PREFIX.length()).trim().toUpperCase();
//                    }
//                }
//            }
//            br.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return macAddress;
//    }

    /**
     * 通过HttpServletRequest获取MAC地址
     *
     * @param request HttpServletRequest
     * @return mac String
     * @throws Exception
     */
//    public static String getMACAddress(HttpServletRequest request) throws Exception {
//        String ip = getIpAddress(request);
//        String line = "";
//        String macAddress = "";
//        final String MAC_ADDRESS_PREFIX = "MAC Address = ";
//        final String LOOPBACK_ADDRESS = "127.0.0.1";
//        //如果为127.0.0.1,则获取本地MAC地址。
//        if (LOOPBACK_ADDRESS.equals(ip)) {
//            InetAddress inetAddress = InetAddress.getLocalHost();
//            byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
//            //下面代码是把mac地址拼装成String
//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < mac.length; i++) {
//                if (i != 0) {
//                    sb.append("-");
//                }
//                //mac[i] & 0xFF 是为了把byte转化为正整数
//                String s = Integer.toHexString(mac[i] & 0xFF);
//                sb.append(s.length() == 1 ? 0 + s : s);
//            }
//            //把字符串所有小写字母改为大写成为正规的mac地址并返回
//            macAddress = sb.toString().trim().toUpperCase();
//            return macAddress;
//        }
//        //获取非本地IP的MAC地址
//        try {
//            Process p = Runtime.getRuntime().exec("nbtstat -A " + ip);
//            InputStreamReader isr = new InputStreamReader(p.getInputStream());
//            BufferedReader br = new BufferedReader(isr);
//            while ((line = br.readLine()) != null) {
//                if (line != null) {
//                    int index = line.indexOf(MAC_ADDRESS_PREFIX);
//                    if (index != -1) {
//                        macAddress = line.substring(index + MAC_ADDRESS_PREFIX.length()).trim().toUpperCase();
//                    }
//                }
//            }
//            br.close();
//        } catch (IOException e) {
//            e.printStackTrace(System.out);
//        }
//        return macAddress;
//    }

    /**
     * 通过HttpServletRequest获取MAC地址
     *
     * @param request HttpServletRequest
     * @return mac String
     * @throws Exception
     */
    public static String getMac(HttpServletRequest request) {
        String ip = getIpAddress(request);
        return getMac(ip);
    }

    /**
     * 通过IP地址获取MAC地址
     *
     * @param ip String,127.0.0.1格式
     * @return mac String
     * @throws Exception
     */
    public static String getMac(String ip) {
        // 获取非本地IP的MAC地址
        Process p = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {
            p = Runtime.getRuntime().exec("nbtstat -A " + ip);
            ir = new InputStreamReader(p.getInputStream());
            br = new BufferedReader(ir);
            String line = "";
            while ((line = br.readLine()) != null) {
                int index = line.indexOf(MAC_ADDRESS_PREFIX);
                if (index > 1) {
                    return line.substring(index + MAC_ADDRESS_PREFIX.length()).trim().toUpperCase();
                }
            }

            final UdpGetClientMacAddr umac = new IpUtils.UdpGetClientMacAddr(ip);
            //---长时间获取不到MAC地址则放弃
            ExecutorService exec = Executors.newFixedThreadPool(1);
            Callable<String> call = () -> umac.getRemoteMacAddr();
            try {
                Future<String> future = exec.submit(call);
                return future.get(1000 * 1, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ex) {
                ex.printStackTrace();
            } finally {
                exec.shutdown();
            }
        } catch (Exception e) {
        } finally {
            try {
                if (p != null)
                    p.destroy();
                if (br != null)
                    br.close();
                if (ir != null)
                    ir.close();
            } catch (IOException e) {
            }
        }
        return "";
    }


    /**
     * 主机A向主机B发送“UDP－NetBIOS－NS”询问包，即向主机B的137端口，发Query包来询问主机B的NetBIOS Names信息。
     * 其次，主机B接收到“UDP－NetBIOS－NS”询问包，假设主机B正确安装了NetBIOS服务........... 而且137端口开放，则主机B会向主机A发送一个“UDP－NetBIOS－NS”应答包，即发Answer包给主机A。
     * 并利用UDP(NetBIOS Name Service)来快速获取远程主机MAC地址的方法
     */
    static class UdpGetClientMacAddr {
        private final String sRemoteAddr;
        private final int iRemotePort = 137;
        private final byte[] buffer = new byte[1024];
        private DatagramSocket ds;

        UdpGetClientMacAddr(String strAddr) throws Exception {
            sRemoteAddr = strAddr;
            ds = new DatagramSocket();
        }

        final DatagramPacket send(final byte[] bytes) throws IOException {
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(sRemoteAddr), iRemotePort);
            ds.send(dp);
            return dp;
        }

        final DatagramPacket receive() throws Exception {
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            ds.receive(dp);
            return dp;
        }

        byte[] GetQueryCmd() throws Exception {
            byte[] t_ns = new byte[50];
            t_ns[0] = 0x00;
            t_ns[1] = 0x00;
            t_ns[2] = 0x00;
            t_ns[3] = 0x10;
            t_ns[4] = 0x00;
            t_ns[5] = 0x01;
            t_ns[6] = 0x00;
            t_ns[7] = 0x00;
            t_ns[8] = 0x00;
            t_ns[9] = 0x00;
            t_ns[10] = 0x00;
            t_ns[11] = 0x00;
            t_ns[12] = 0x20;
            t_ns[13] = 0x43;
            t_ns[14] = 0x4B;

            for (int i = 15; i < 45; i++) {
                t_ns[i] = 0x41;
            }
            t_ns[45] = 0x00;
            t_ns[46] = 0x00;
            t_ns[47] = 0x21;
            t_ns[48] = 0x00;
            t_ns[49] = 0x01;
            return t_ns;
        }

        final String getMacAddr(byte[] brevdata) throws Exception {
            // 获取计算机名
            int i = brevdata[56] * 18 + 56;
            String sAddr = "";
            StringBuffer sb = new StringBuffer(17);
            // 先从第56字节位置，读出Number Of Names（NetBIOS名字的个数，其中每个NetBIOS Names Info部分占18个字节）
            // 然后可计算出“Unit ID”字段的位置＝56＋Number Of Names×18，最后从该位置起连续读取6个字节，就是目的主机的MAC地址。
            for (int j = 1; j < 7; j++) {
                sAddr = Integer.toHexString(0xFF & brevdata[i + j]);
                if (sAddr.length() < 2) {
                    sb.append(0);
                }
                sb.append(sAddr.toUpperCase());
                if (j < 6) sb.append(':');
            }
            return sb.toString();
        }

        final void close() {
            ds.close();
        }

        final String getRemoteMacAddr() throws Exception {
            byte[] bqcmd = GetQueryCmd();
            send(bqcmd);
            DatagramPacket dp = receive();
            String smac = getMacAddr(dp.getData());
            close();

            return smac;
        }
    }

    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return "127.0.0.1";
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        return "未知";
    }

}
