package com.qinfei.qferp.utils;


import com.qinfei.core.utils.MD5Utils;
import org.springframework.util.StringUtils;

/**
 * 数据安全校验工具类
 */
public class ValidateUtil {

	//密码校验正则表达式
	private static final String PWD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+`\\-={}:\";'<>?,.\\/]).{6,16}$";

	/**
	 * 密码校验
	 * @param pwd
	 * @return
	 */
	public static Boolean checkPwd(String pwd){
          if(StringUtils.isEmpty(pwd)){
          	 return false;
		  }
          return pwd.matches(PWD_PATTERN);
	}

//	public static void main(String[] args) {
//		System.out.println(ValidateUtil.checkPwd("liuhui?"));
//		System.out.println(MD5Utils.encode("liuhui"));
//	}

}
