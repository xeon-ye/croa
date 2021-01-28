package com.qinfei.core.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;

/**
 * 图片处理工具类
 * @author GZW
 */
class Base64Utils {
    //图片到byte数组
    public static byte[] image2byte(String path) {
        byte[] data = null;
        FileImageInputStream input = null;
        try {
            input = new FileImageInputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return data;
    }

    //byte数组到图片
    public static void byte2image(byte[] data, String path) {
        if (data.length < 3 || path.equals("")) return;
        try {
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();
            System.out.println("Make Picture success,Please find image in " + path);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }
    }


    //byte数组到16进制字符串
    public static String byte2string(byte[] data) {
        if (data == null || data.length <= 1) return "0x";
        if (data.length > 200000) return "0x";
        StringBuffer sb = new StringBuffer();
        int buf[] = new int[data.length];
        //byte数组转化成十进制
        for (int k = 0; k < data.length; k++) {
            buf[k] = data[k] < 0 ? (data[k] + 256) : (data[k]);
        }
        //十进制转化成十六进制
        for (int k = 0; k < buf.length; k++) {
            if (buf[k] < 16) sb.append("0" + Integer.toHexString(buf[k]));
            else sb.append(Integer.toHexString(buf[k]));
        }
        return "0x" + sb.toString().toUpperCase();
    }

    /**
     * @return
     * @Description: 根据图片地址转换为base64编码字符串
     * @Author:
     * @CreateTime:
     */
    public static String getImageStr(String imgFile) {
        ByteArrayOutputStream baos = null;
        byte[] data = null;
        try {

//            URL u = new URL(imgFile);
            File file = new File(imgFile);
            BufferedImage image = ImageIO.read(file);

            baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);

            baos.flush();
            data = baos.toByteArray();

            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                baos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        // 加密
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    /**
     * @param imgStr base64编码字符串
     * @param path   图片路径-具体到文件
     * @return
     * @Description: 将base64编码字符串转换为图片
     * @Author:
     * @CreateTime:
     */
    public static boolean generateImage(String imgStr, String path) {
        File tempFile = new File(path);
        if (!tempFile.exists()) {
            tempFile.getParentFile().mkdirs();
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (imgStr == null)
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // 解密
            byte[] b = decoder.decodeBuffer(imgStr);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //base64字符串转化成图片
    public static String createImage(String imgStr, String path) {   //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return null;
        String ext = "jpg";
        if (imgStr.indexOf(";base64,") > 0) {//data:image/png;base64,
            String[] datas = imgStr.split(";base64,");
            if (datas[0].indexOf("image/") > 0) {
                ext = datas[0].split("image/")[1];
                switch (ext) {
                    case "jpeg":
                        ext = "jpg";
                        break;
                    case "jpg":
                        ext = "jpg";
                        break;
                    case "gif":
                        ext = "gif";
                        break;
                    case "png":
                        ext = "png";
                        break;
                    default:
                        ext = "jpg";
                }
            }
            imgStr = datas[1];
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }
            String name = UUID.randomUUID().toString() + "." + ext;
            //生成jpeg图片
            File file = new File(path, name);
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            OutputStream out = new FileOutputStream(file);
            out.write(b);
            out.flush();
            out.close();
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static void main(String[] args) throws IOException {
////        String path = "C:\\Users\\Administrator\\Desktop\\base64.txt";
//        String path = "C:\\Users\\Administrator\\Desktop\\图片.txt";
////        String path = "H:\\works\\遵义智慧工地\\遵义市智慧工地监管云平台UI界面及切图V1.1-20180518\\遵义市智慧工地监管云平台\\切图\\proj_bg.png";
//        InputStream in = new FileInputStream(path);
//        BufferedReader sr = new BufferedReader(new FileReader(path));
//        String str = null;
//        StringBuilder sb = new StringBuilder();
//        while ((str = sr.readLine()) != null) {
//            sb.append(str);
//        }
//
////        String base = getImageStr(path);
////        generateImage(sb.toString(),"H:\\works\\a.jpg");
//        createImage(sb.toString(), "H:\\works\\");
////        system.out.println(base);
//    }
}

