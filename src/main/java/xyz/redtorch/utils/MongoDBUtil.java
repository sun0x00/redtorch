package xyz.redtorch.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sun0x00@gmail.com
 */
public class MongoDBUtil {
	private static Logger log = LoggerFactory.getLogger(MongoDBUtil.class);
	/**
	 * 将实体Bean对象转换成Mongo Document
	 * 
	 * @param bean
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static <T> Document beanToDocument(T bean) throws IllegalArgumentException, IllegalAccessException {
		if (bean == null) {
			return null;
		}
		Document document = new Document();
		// 获取对象类的属性域
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields) {
			// 获取变量的属性名
			String varName = field.getName();
			// 修改访问控制权限
			boolean accessFlag = field.isAccessible();
			if (!accessFlag) {
				field.setAccessible(true);
			}
			Object param = field.get(bean);
			if (param == null) {
				continue;
			} else if (param instanceof Integer) {
				// 判断变量的类型
				int value = ((Integer) param).intValue();
				document.put(varName, value);
			} else if (param instanceof String) {
				String value = (String) param;
				document.put(varName, value);
			} else if (param instanceof Double) {
				double value = ((Double) param).doubleValue();
				document.put(varName, value);
			} else if (param instanceof Float) {
				float value = ((Float) param).floatValue();
				document.put(varName, value);
			} else if (param instanceof Long) {
				long value = ((Long) param).longValue();
				document.put(varName, value);
			} else if (param instanceof Boolean) {
				boolean value = ((Boolean) param).booleanValue();
				document.put(varName, value);
			} else if (param instanceof Date) {
				Date value = (Date) param;
				document.put(varName, value);
			} else if (param instanceof DateTime) {
				DateTime dataTimeValue = (DateTime) param;
				Date value = dataTimeValue.toDate();
				document.put(varName, value);
			}
			// 恢复访问控制权限
			field.setAccessible(accessFlag);
		}
		return document;
	}

	/**
	 * 将Mongo Document转换成Bean对象
	 * 
	 * @param document
	 * @param bean
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static <T> T documentToBean(Document document, T bean)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (bean == null) {
			return null;
		}
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields) {
			String varName = field.getName();
			Object object = document.get(varName);
			if (object != null) {
				// BeanUtils.setProperty(bean, varName, object);
				setProperty(bean, varName, object);
			}

		}
		return bean;
	}

	/**
	 * 针对MongoDB设计的通过反射对成员变量赋值
	 * 
	 * @param bean
	 *            需要赋值的Class
	 * @param varName
	 *            属性名称
	 * @param object
	 *            值
	 */
	public static <T> void setProperty(T bean, String varName, T object) {
		String upperCaseVarName = varName.substring(0, 1).toUpperCase() + varName.substring(1);
		try {
			// 获取变量类型
			//String type = object.getClass().getName();
			if(bean.getClass().getDeclaredField(varName) == null) {
				log.error("Class-{}中无法找到对应成员变量{}",bean.getClass().getName(),varName);
				return;
			}
			String type = bean.getClass().getDeclaredField(varName).getType().getName();
			String objectType = object.getClass().getName();
			// 类型为String
			if (type.equals("java.lang.String")) {
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, String.class);
				if(objectType.equals("java.lang.String")) {
					m.invoke(bean, object);
				}else{
					log.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错",bean.getClass().getName(),varName,type,objectType);
					try {
						String castingObject = object.toString();
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						log.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错",bean.getClass().getName(),varName,type,objectType);
					}
				}
			} else if (type.equals("java.lang.Integer")) { // 类型为Integer
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Integer.class);
				if(objectType.equals("java.lang.Integer")) {
					m.invoke(bean, object);
				}else{
					log.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错",bean.getClass().getName(),varName,type,objectType);
					try {
						Integer castingObject = Integer.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						log.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错",bean.getClass().getName(),varName,type,objectType);
					}
				}
			} else if (type.equals("java.lang.Boolean")) {// 类型为Boolean
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Boolean.class);
				if(objectType.equals("java.lang.Boolean")) {
					m.invoke(bean, object);
				}else{
					log.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错",bean.getClass().getName(),varName,type,objectType);
					try {
						Boolean castingObject = Boolean.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						log.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错",bean.getClass().getName(),varName,type,objectType);
					}
				}
			} else if (type.equals("java.lang.Long")) { // 类型为Long
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Long.class);
				if(objectType.equals("java.lang.Long")) {
					m.invoke(bean, object);
				}else{
					log.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错",bean.getClass().getName(),varName,type,objectType);
					try {
						Long castingObject = Long.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						log.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错",bean.getClass().getName(),varName,type,objectType);
					}
				}
			} else if (type.equals("java.lang.Float")) {// 类型为Float
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Float.class);
				if(objectType.equals("java.lang.Float")) {
					m.invoke(bean, object);
				}else{
					log.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错",bean.getClass().getName(),varName,type,objectType);
					try {
						Float castingObject = Float.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						log.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错",bean.getClass().getName(),varName,type,objectType);
					}
				}
			}else if (type.equals("java.lang.Double")) {// 类型为Double
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Double.class);
				if(objectType.equals("java.lang.Double")) {
					m.invoke(bean, object);
				}else{
					log.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错",bean.getClass().getName(),varName,type,objectType);
					try {
						Double castingObject = Double.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						log.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错",bean.getClass().getName(),varName,type,objectType);
					}
				}
			} else if (type.equals("java.util.Date")) {// 类型为Date或者DateTime
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Date.class);
				m.invoke(bean, object);
			} else if(type.equals("org.joda.time.DateTime")) {
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, DateTime.class);
				if(objectType.equals("java.util.Date")) {
					Date date = (Date) object;
					DateTime newObject = new DateTime(date.getTime());
					m.invoke(bean, newObject);
				} else if(objectType.equals("java.lang.Long")) {
					DateTime newObject = new DateTime((Long)object);
					m.invoke(bean, newObject);
				}else {
					log.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,不可赋值",bean.getClass().getName(),varName,type,objectType);
				}
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
}
