package com.qinfei.qferp.utils;

import java.util.UUID;

/**
 * 生成主键ID；
 */
public class PrimaryKeyUtil {
	/**
	 * 获取UUID主键；
	 * 
	 * @return 生成的主键ID；
	 */
	public static String getPrimary() {
		return UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}

	/**
	 * 获得Long类型的唯一数字；
	 * 
	 * @return 生成的唯一16位数字；
	 */
	public static Long getLongUniqueKey() {
		return Long.parseLong(getStringUniqueKey());
	}

	/**
	 * 获取16位无重复的数字主键ID；
	 * 
	 * @return 16位数字字符串；
	 */
	public static String getStringUniqueKey() {
		return getUniqueKey(1);
	}

	/**
	 * 获取16位无重复的数字主键ID；
	 * 
	 * @param machine
	 *            ：服务器集群编号，1-9之间；
	 * @return 16位数字字符串；
	 */
	private static String getUniqueKey(int machine) {
		if (machine < 1) {
			machine = 1;
		}
		if (machine > 9) {
			machine = 9;
		}
		// 最大支持1-9个集群机器部署；
		int hashCodeV = UUID.randomUUID().toString().hashCode();
		if (hashCodeV < 0) {
			hashCodeV = -hashCodeV;
		}
		// 0 代表前面补充0；
		// 4 代表长度为4；
		// d 代表参数为正数型；
		return machine + String.format("%0" + (16 - 1) + "d", hashCodeV);
	}
}