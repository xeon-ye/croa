package com.qinfei.core.utils;

import com.qinfei.qferp.entity.crm.CrmCompanyUser;
import com.qinfei.qferp.entity.media.Supplier;
import com.qinfei.qferp.service.impl.crm.CrmCompanyUserService;
import com.qinfei.qferp.service.impl.media.SupplierService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AES对称加密和解密
 */
@Configuration
//@SpringBootApplication
//@ServletComponentScan
public class EncryptUtils {


//    private static ApplicationContext applicationContext;//启动类set入，调用下面set方法
//
//    public static void setApplicationContext(ApplicationContext context) {
//        applicationContext = context;
//    }
//

    //    @Value("${spring.rule}")
    private static String rule;

    @Value("${spring.rule}")
    public void setRule(String rule) {
        EncryptUtils.rule = rule;
    }

    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "AES";
    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";
    /**
     * SecretKeySpec类是KeySpec接口的实现类,用于构建秘密密钥规范
     */
    /**
     * AES加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) {
        if (StringUtils.isEmpty(data)) {
            return "";
        }
        try {
//            KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
//            kgen.init(128, new SecureRandom(password.getBytes()));
//            SecretKey secretKey = kgen.generateKey();
//            byte[] enCodeFormat = secretKey.getEncoded();
//            SecretKeySpec key = new SecretKeySpec(enCodeFormat, ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(rule.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM_STR); // 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//            return new BASE64Encoder().encode(bytes);
            return parseByte2HexStr(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * AES解密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String decrypt(String data) {
        if (StringUtils.isEmpty(data)) {
            return "";
        }
        try {
//            KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
//            kgen.init(128, new SecureRandom(password.getBytes()));
//            SecretKey secretKey = kgen.generateKey();
//            byte[] enCodeFormat = secretKey.getEncoded();
//            SecretKeySpec key = new SecretKeySpec(enCodeFormat, ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(rule.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(parseHexStr2Byte(data));
//            byte[] bytes = cipher.doFinal(new BASE64Decoder().decodeBuffer(data));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     * @throws
     * @method parseByte2HexStr
     * @since v1.0
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     * @throws
     * @method parseHexStr2Byte
     * @since v1.0
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * AES解密-不使用
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String oldDecrypt(String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(rule.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(new BASE64Decoder().decodeBuffer(data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void main(String[] args) {
//        getCust();
        rule = "0LdyywSl1X3iBru456uBdA==";
        String content = "加密前123加密前";
//        String desc = se.AESEncode(content);//Wk49s1RZVLWo4GQPs8hJBQ== ,0LdyywSl1X3iBru456uBdA== wjy2nANGHwRuQRASTxw9fw==
        String desc = encrypt(content);
        System.out.println("加密前：[" + content + "]加密后:" + desc);
        System.out.println("解密前:[" + desc + "]解密后:" + decrypt(desc));
    }

    /**
     * 对客户老数据进行加密
     */
    private static void getCust() {

        Map map = new HashMap();
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("classpath*:application*.xml");
        context.refresh();
        CrmCompanyUserService companyUserService = context.getBean(CrmCompanyUserService.class);
        List<Map> userList = companyUserService.listCompanyUser(map);
        for (Map temp : userList) {
            //将客户的联系方式和电话进行加密
           /* temp.put("")
            dockingPeople.setConnectionType(encrypt(dockingPeople.getConnectionType()));
            dockingPeople.setPhone(encrypt(dockingPeople.getPhone()));
            dockingPeopleService.update(dockingPeople);*/
        }
    }
}