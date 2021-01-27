package xyz.redtorch.common.mongo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.common.util.CommonUtils;

public class MongoDBUtils {
	private static final Logger logger = LoggerFactory.getLogger(MongoDBUtils.class);

	/**
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
			if (param == null || "serialVersionUID".equals(field.getName())) {
				continue;
			} else if (param instanceof Integer) {
				// 判断变量的类型
				int value = ((Integer) param).intValue();
				document.put(varName, value);
			} else if (param.getClass() == int.class) {
				// 判断变量的类型
				int value = (int) param;
				document.put(varName, value);
			} else if (param instanceof String) {
				String value = (String) param;
				document.put(varName, value);
			} else if (param instanceof Double) {
				double value = ((Double) param).doubleValue();
				document.put(varName, value);
			} else if (param.getClass() == double.class) {
				double value = (double) param;
				document.put(varName, value);
			} else if (param instanceof Float) {
				float value = ((Float) param).floatValue();
				document.put(varName, value);
			} else if (param.getClass() == float.class) {
				float value = (float) param;
				document.put(varName, value);
			} else if (param instanceof Long) {
				long value = ((Long) param).longValue();
				document.put(varName, value);
			} else if (param.getClass() == long.class) {
				long value = (long) param;
				document.put(varName, value);
			} else if (param instanceof Boolean) {
				boolean value = ((Boolean) param).booleanValue();
				document.put(varName, value);
			} else if (param.getClass() == boolean.class) {
				boolean value = (boolean) param;
				document.put(varName, value);
			} else if (param instanceof Date) {
				Date value = (Date) param;
				// 统一使用long存储时间戳避免时间读取错位问题
				document.put(varName, value.getTime());
			} else if (param instanceof LocalDateTime) {
				LocalDateTime dataTimeValue = (LocalDateTime) param;
				// 统一使用long存储时间戳避免时间读取错位问题
				document.put(varName, CommonUtils.localDateTimeToMills(dataTimeValue));
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
	 * @param bean    需要赋值的Class
	 * @param varName 属性名称
	 * @param object  值
	 */
	public static <T> void setProperty(T bean, String varName, T object) {
		String upperCaseVarName = varName.substring(0, 1).toUpperCase() + varName.substring(1);
		try {
			if (bean.getClass().getDeclaredField(varName) == null) {
				logger.error("Class-{}中无法找到对应成员变量{}", bean.getClass().getName(), varName);
				return;
			}
			Class<?> beanFieldClazz = bean.getClass().getDeclaredField(varName).getType();
			Class<?> objectClzz = object.getClass();
			// 类型为String
			if (beanFieldClazz == String.class) {
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, String.class);
				if (objectClzz == String.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						String castingObject = object.toString();
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == Integer.class) { // 类型为Integer
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Integer.class);
				if (objectClzz == Integer.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						Integer castingObject = Integer.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == int.class) { // 类型为integer
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, int.class);
				if (objectClzz == int.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						int castingObject = Integer.valueOf(object.toString()).intValue();
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == Boolean.class) {// 类型为Boolean
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Boolean.class);
				if (objectClzz == Boolean.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						Boolean castingObject = Boolean.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == boolean.class) {// 类型为boolean
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, boolean.class);
				if (objectClzz == boolean.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						boolean castingObject = Boolean.valueOf(object.toString()).booleanValue();
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == Long.class) { // 类型为Long
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Long.class);
				if (objectClzz == Long.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						Long castingObject = Long.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == long.class) { // 类型为long
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, long.class);
				if (objectClzz == long.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						long castingObject = Long.valueOf(object.toString()).longValue();
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == Float.class) {// 类型为Float
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Float.class);
				if (objectClzz == Float.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						Float castingObject = Float.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == float.class) {// 类型为float
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, float.class);
				if (objectClzz == float.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						float castingObject = Float.valueOf(object.toString()).floatValue();
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == Double.class) {// 类型为Double
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Double.class);
				if (objectClzz == Double.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						Double castingObject = Double.valueOf(object.toString());
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == double.class) {// 类型为double
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, double.class);
				if (objectClzz == double.class) {
					m.invoke(bean, object);
				} else {
					logger.debug("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,将尝试转换,可能丢失精度、溢出或出错", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
					try {
						double castingObject = Double.valueOf(object.toString()).doubleValue();
						m.invoke(bean, castingObject);
					} catch (Exception e) {
						logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,强制转换出错", bean.getClass().getName(), varName,
								beanFieldClazz, objectClzz);
					}
				}
			} else if (beanFieldClazz == Date.class) {// 类型为Date或者DateTime
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, Date.class);
				if (objectClzz == Date.class) {
					Date date = (Date) object;
					Date newObject = new Date(CommonUtils.changeDateTimeZoneFromLondonToShanghai(date).getTime());
					m.invoke(bean, newObject);
				} else if (objectClzz == Long.class) {
					Date newObject = new Date((Long) object);
					m.invoke(bean, newObject);
				} else if (objectClzz == long.class) {
					Date newObject = new Date((long) object);
					m.invoke(bean, newObject);
				} else {
					logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,不可赋值", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
				}
			} else if (beanFieldClazz == LocalDateTime.class) {
				Method m = bean.getClass().getMethod("set" + upperCaseVarName, LocalDateTime.class);
				if (objectClzz == Date.class) {
					Date date = (Date) object;
					LocalDateTime newObject = CommonUtils.millsToLocalDateTime(date.getTime());
					m.invoke(bean, newObject);
				} else if (objectClzz == Long.class) {
					// 使用Long存储时间戳不存在时间错位问题
					LocalDateTime newObject = CommonUtils.millsToLocalDateTime((Long) object);
					m.invoke(bean, newObject);
				} else if (objectClzz == long.class) {
					// 使用long存储时间戳不存在时间读取错位问题
					LocalDateTime newObject = CommonUtils.millsToLocalDateTime((long) object);
					m.invoke(bean, newObject);
				} else {
					logger.error("Class-{}中成员变量{}的类型{}与当前值的类型{}不匹配,不可赋值", bean.getClass().getName(), varName,
							beanFieldClazz, objectClzz);
				}
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchFieldException | ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
