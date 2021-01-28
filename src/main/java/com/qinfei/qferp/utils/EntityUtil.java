package com.qinfei.qferp.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.sys.User;

import lombok.extern.slf4j.Slf4j;

/**
 * 实体类工具；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/11 0011 19:35；
 */
@Slf4j
public class EntityUtil {
	/**
	 * 封装Map的值到指定类型的POJO对象；
	 *
	 * @param json：前台传递的Json对象；
	 * @param object：需要反射的对象类型；
	 * @return ：封装完毕的对象；
	 */
	public static Object getNewObject(JSONObject json, Class<?> object) {
		Object target = null;
		try {
			target = object.newInstance();
			Field[] fields = getAllFiled(object);
			Object value;
			for (Field field : fields) {
				// 序列化ID需要跳过；
				if ("serialVersionUID".equals(field.getName())) {
					continue;
				}
				field.setAccessible(true);
				value = json.get(field.getName());
				if (value != null) {
					// 判断类型；
					if (field.getType().equals(Integer.class)) {
						field.set(target, json.getInteger(field.getName()));
					}
					if (field.getType().equals(Float.class)) {
						field.set(target, json.getFloat(field.getName()));
					}
					if (field.getType().equals(Double.class)) {
						field.set(target, json.getDouble(field.getName()));
					}
					if (field.getType().equals(String.class)) {
						field.set(target, json.getString(field.getName()));
					}
					if (field.getType().equals(Date.class)) {
						field.set(target, DateUtils.parse(json.getString(field.getName()), "yyyy-MM-dd"));
					}
				}
			}
		} catch (InstantiationException e) {
			log.error("类初始化异常。" + e);
		} catch (IllegalArgumentException e) {
			log.error("参数异常。" + e);
		} catch (IllegalAccessException e) {
			log.error("类入口异常。" + e);
		}
		return target;
	}

	/**
	 * 封装Map的值到指定类型的POJO对象；
	 *
	 * @param map：保存类审核信息的集合；
	 * @param object：需要反射的对象类型；
	 * @return ：封装完毕的对象；
	 */
	public static Object getNewObject(Map<String, Object> map, Class<?> object) {
		Object target = null;
		try {
			target = object.newInstance();
			Field[] fields = getAllFiled(object);
			for (Field field : fields) {
				// 序列化ID需要跳过；
				if ("serialVersionUID".equals(field.getName())) {
					continue;
				}
				field.setAccessible(true);
				field.set(target, map.get(field.getName()));
			}
		} catch (InstantiationException e) {
			log.error("类初始化异常。");
		} catch (IllegalArgumentException e) {
			log.error("参数异常。");
		} catch (IllegalAccessException e) {
			log.error("类入口异常。");
		}
		return target;
	}

	/**
	 * 设置数据的更新信息；
	 *
	 * @param params：数据集合；
	 */
	public static void setUpdateInfo(Map<String, Object> params) {
		// 获取登录人信息；
		User user = AppUtil.getUser();
		user = user == null ? new User() : user;
		params.put("updateId", user.getId());
		params.put("updateName", user.getName());
		params.put("updateTime", new Date());

		params.remove("createId");
		params.remove("createName");
		params.remove("createTime");
		params.remove("state");
		params.remove("version");
	}

	/**
	 * 设置数据的创建信息；
	 *
	 * @param params：数据集合；
	 */
	public static void setCreateInfo(Map<String, Object> params) {
		// 获取登录人信息；
		User user = AppUtil.getUser();
		user = user == null ? new User() : user;
		params.put("createId", user.getId());
		params.put("createName", user.getName());
		params.put("createTime", new Date());
		params.put("version", 0);

		params.remove("updateId");
		params.remove("updateName");
		params.remove("updateTime");
	}

	/**
	 * 获取类的所有属性数组；
	 * 
	 * @param object：类型；
	 * @return ：类属性数组；
	 */
	private static Field[] getAllFiled(Class<?> object) {
		if (object == Object.class) {
			return new Field[] {};
		} else {
			Field[] fields = object.getDeclaredFields();
			object = object.getSuperclass();
			// 递归获取父类属性；
			Field[] supperClassField = getAllFiled(object);
			int newLength = supperClassField.length;
			// 如果父类是基类，结束递归；
			if (newLength > 0) {
				int length = fields.length;
				fields = Arrays.copyOf(fields, length + newLength);
				for (int i = 0; i < newLength; i++) {
					fields[length + i] = supperClassField[i];
				}
			}
			return fields;
		}
	}
}