package com.abmatrix.bool.tg.middleware.redis.clients;

import cn.hutool.json.JSONUtil;
import com.abmatrix.bool.tg.middleware.redis.config.LettuceConfig;
import com.abmatrix.bool.tg.middleware.redis.enums.RedisClientTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
/**
 * 列表缓存客户端
 * 
 * @author wangjp31109
 * @date 2021年10月9日
 */
@Lazy
@Slf4j
@Component
@Order(3)
@DependsOn("boolRedisTemplate")
@SuppressWarnings("unchecked")
@ConditionalOnBean(LettuceConfig.class)
public class ListRedisClient extends CommonRedisClient {
	/**
	 * spring上下文
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 初始化方法
	 */
	@PostConstruct
	private void init() {
		super.init(applicationContext);
	}

	/**
	 * 构造
	 */
	public ListRedisClient() {
		super();
		this.clientType = RedisClientTypeEnum.LIST.getTypeName();
	}

	/**
	 * 将list以头插的方式放入缓存，不指定过期时间
	 * 
	 * @param key    键
	 * @param values 列表值
	 * @return true插入成功，false插入失败
	 */
	public boolean lpushAll(String key, List<Object> values) {
		try {
			redisTemplate.opsForList().leftPushAll(key, values);
			return true;
		} catch (Exception e) {
			String listJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis左侧存入键位[", key, "]对应列表对象[", listJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 将list以头插的方式放入缓存并指定过期时间，单位秒
	 * 
	 * @param key     键
	 * @param values  列表值
	 * @param seconds 时间
	 * @return true插入成功，false插入失败
	 */
	public boolean lpushAll(String key, List<Object> values, long seconds) {
		try {
			redisTemplate.opsForList().leftPushAll(key, values);
			if (seconds > 0) {
				expire(key, seconds);
			}
			return true;
		} catch (Exception e) {
			String listJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis左侧存入键位[", key, "]对应列表对象[", listJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 将list以头插的方式放入缓存并指定过期时间，单位自定义
	 * 
	 * @param key      键
	 * @param values   列表值
	 * @param time     时间
	 * @param timeUnit 时间单位
	 * @return true插入成功，false插入失败
	 */
	public boolean lpushAll(String key, List<Object> values, long time, TimeUnit timeUnit) {
		try {
			redisTemplate.opsForList().leftPushAll(key, values);
			if (time > 0) {
				expire(key, time, timeUnit);
			}
			return true;
		} catch (Exception e) {
			String listJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis左侧存入键位[", key, "]对应列表对象[", listJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 将list以尾插的方式放入缓存，不指定过期时间
	 * 
	 * @param key    键
	 * @param values 列表值
	 * @return true插入成功，false插入失败
	 */
	public boolean rpushAll(String key, List<Object> values) {
		try {
			redisTemplate.opsForList().rightPushAll(key, values);
			return true;
		} catch (Exception e) {
			String listJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis右侧存入键位[", key, "]对应列表对象[", listJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 将list以尾插的方式放入缓存并指定过期时间，单位秒
	 * 
	 * @param key     键
	 * @param values  列表值
	 * @param seconds 时间秒
	 * @return true插入成功，false插入失败
	 */
	public boolean rpushAll(String key, List<Object> values, long seconds) {
		try {
			redisTemplate.opsForList().rightPushAll(key, values);
			if (seconds > 0) {
				expire(key, seconds);
			}
			return true;
		} catch (Exception e) {
			String listJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis右侧存入键位[", key, "]对应列表对象[", listJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 将list以尾插的方式放入缓存并指定过期时间，单位自定义
	 * 
	 * @param key      键
	 * @param values   列表值
	 * @param time     时间
	 * @param timeUnit 时间单位
	 * @return true插入成功，false插入失败
	 */
	public boolean rpushAll(String key, List<Object> values, long time, TimeUnit timeUnit) {
		try {
			redisTemplate.opsForList().rightPushAll(key, values);
			if (time > 0) {
				expire(key, time, timeUnit);
			}
			return true;
		} catch (Exception e) {
			String listJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis右侧存入键位[", key, "]对应列表对象[", listJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 列表左侧插入单个元素，不指定过期时间
	 * 
	 * @param key    键
	 * @param object 元素对象
	 * @return true插入成功，false插入失败
	 */
	public boolean lpush(String key, Object object) {
		try {
			redisTemplate.opsForList().leftPush(key, object);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis左侧存入键位[", key, "]对应列表元素[", object, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 向左侧列表插入多值，参数三为不定参数，可传多值 指定超时时间，单位秒
	 * 
	 * @param key     键
	 * @param seconds 超时时间，单位秒
	 * @param objects 值，不定参数
	 * @return true插入成功，false插入失败
	 */
	public boolean lpush(String key, long seconds, Object... objects) {
		boolean flag = false;
		try {
			redisTemplate.opsForList().leftPush(key, objects);
			if (seconds > 0) {
				expire(key, seconds);
				flag = true;
			}
		} catch (Exception e) {
			String objectsStr = JSONUtil.toJsonStr(objects);
			String errorMsg = StringUtils.join("redis左侧存入键位[", key, "]对应列表元素[", objectsStr, "]异常");
			log.error(errorMsg, e);
			flag = false;
		}
		return flag;
	}

	/**
	 * 向左侧列表插入多值，参数三为不定参数，可传多值 指定超时时间，单位自定义
	 * 
	 * @param key     键
	 * @param 超时时间
	 * @param 时间单位
	 * @param objects 值，不定参数
	 * @return true插入成功，false插入失败
	 */
	public boolean lpush(String key, long time, TimeUnit timeUnit, Object... objects) {
		boolean flag = false;
		try {
			redisTemplate.opsForList().leftPush(key, objects);
			if (time > 0) {
				expire(key, time, timeUnit);
				flag = true;
			}
		} catch (Exception e) {
			String objectsStr = JSONUtil.toJsonStr(objects);
			String errorMsg = StringUtils.join("redis左侧存入键位[", key, "]对应列表元素[", objectsStr, "]异常");
			log.error(errorMsg, e);
			flag = false;
		}
		return flag;
	}

	/**
	 * 插入列表目标值左侧
	 * 
	 * @param key    键
	 * @param pivot  目标值
	 * @param object 要插入的值
	 * @return true插入成功，false插入失败
	 */
	public boolean lpush(String key, Object pivot, Object object) {
		try {
			redisTemplate.opsForList().leftPush(key, pivot, object);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应列表对象目标值[", pivot, "]左侧插入目标值[", object, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 插入列表目标值右侧
	 * 
	 * @param key    键
	 * @param pivot  目标值
	 * @param object 要插入的值
	 * @return true插入成功，false插入失败
	 */
	public boolean rpush(String key, Object pivot, Object object) {
		try {
			redisTemplate.opsForList().rightPush(key, pivot, object);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应列表对象目标值[", pivot, "]右侧插入目标值[", object, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 向集合最右边添加元素
	 * 
	 * @param key    键
	 * @param object 插入元素
	 * @return true插入成功，false插入失败
	 */
	public boolean rpush(String key, Object object) {
		try {
			redisTemplate.opsForList().rightPush(key, object);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis右侧存入键位[", key, "]对应列表元素[", object, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 向右侧列表插入多值，参数三为不定参数，可传多值 指定超时时间，单位秒
	 * 
	 * @param key     键
	 * @param seconds 超时时间，单位秒
	 * @param objects 值，不定参数
	 * @return true插入成功，false插入失败
	 */
	public boolean rpush(String key, long seconds, Object... objects) {
		boolean flag = false;
		try {
			redisTemplate.opsForList().rightPush(key, objects);
			if (seconds > 0) {
				expire(key, seconds);
				flag = true;
			}
		} catch (Exception e) {
			String objectsStr = JSONUtil.toJsonStr(objects);
			String errorMsg = StringUtils.join("redis右侧存入键位[", key, "]对应列表元素[", objectsStr, "]异常");
			log.error(errorMsg, e);
			flag = false;
		}
		return flag;
	}

	/**
	 * 向右侧列表插入多值，参数三为不定参数，可传多值 指定超时时间，单位自定义
	 * 
	 * @param key      键
	 * @param time     超时时间，单位自定义
	 * @param timeUnit 时间单位
	 * @param objects  值，不定参数
	 * @return true插入成功，false插入失败
	 */
	public boolean rpush(String key, long time, TimeUnit timeUnit, Object... objects) {
		boolean flag = false;
		try {
			redisTemplate.opsForList().rightPush(key, objects);
			if (time > 0) {
				expire(key, time, timeUnit);
				flag = true;
			}
		} catch (Exception e) {
			String objectsStr = JSONUtil.toJsonStr(objects);
			String errorMsg = StringUtils.join("redis右侧存入键位[", key, "]对应列表元素[", objectsStr, "]异常");
			log.error(errorMsg, e);
			flag = false;
		}
		return flag;
	}

	/**
	 * 如果存在集合则向左边添加元素，不存在不加
	 * 
	 * @param key    键
	 * @param object 插入元素
	 * @return true插入成功，false插入失败
	 */
	public boolean lPushIfPresent(String key, Object object) {
		try {
			redisTemplate.opsForList().leftPushIfPresent(key, object);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis左侧存入键位[", key, "]对应列表元素[", object, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 如果存在集合则向右边添加元素，不存在不加
	 * 
	 * @param key    键
	 * @param object 插入元素
	 * @return true插入成功，false插入失败
	 */
	public boolean rPushIfPresent(String key, Object object) {
		try {
			redisTemplate.opsForList().rightPushIfPresent(key, object);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis右侧存入键位[", key, "]对应列表元素[", object, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 移除并返回集合中的左边第一个元素
	 * 
	 * @param key 键
	 * @return 删除的元素
	 */
	public Object lpop(String key) {
		return redisTemplate.opsForList().leftPop(key);
	}

	/**
	 * 移除并返回集合中的左边第一个元素，且设置超时等待时间，如果等待超时则退出
	 * 
	 * @param key      键
	 * @param timeout  等待时间
	 * @param timeUnit 时间单位
	 * @return
	 */
	public Object lpop(String key, long timeout, TimeUnit timeUnit) {
		return redisTemplate.opsForList().leftPop(key, timeout, timeUnit);
	}

	/**
	 * 移除右边第一个元素
	 * 
	 * @param key 键
	 * @return 删除的元素
	 */
	public Object rpop(String key) {
		return redisTemplate.opsForList().rightPop(key);
	}

	/**
	 * 移除并返回集合中的右边第一个元素，且设置超时等待时间，如果等待超时则退出
	 * 
	 * @param key      键
	 * @param timeout  等待时间
	 * @param timeUnit 时间单位
	 * @return
	 */
	public Object rpop(String key, long timeout, TimeUnit timeUnit) {
		return redisTemplate.opsForList().rightPop(key);
	}

	/**
	 * 获取列表指定区间的值
	 * 
	 * @param key   键
	 * @param start 开始指针
	 * @param end   结束指针
	 * @return 区间列表
	 */
	public List<Object> lrange(String key, long start, long end) {
		return redisTemplate.opsForList().range(key, start, end);
	}

	/**
	 * 获取集合列表长度
	 * 
	 * @param key 键
	 * @return 长度
	 */
	public Long llen(String key) {
		return redisTemplate.opsForList().size(key);
	}

	/**
	 * 在指定索引处插入值
	 * 
	 * @param key   键
	 * @param index 要插入的索引
	 * @param value 要插入的值
	 */
	public boolean lset(String key, long index, Object value) {
		try {
			redisTemplate.opsForList().set(key, index, value);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis键位[", key, "]对应列表元素在[", index, "]处插入值[", value, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 获取集合指定位置的值
	 * 
	 * @param key   键
	 * @param index 索引位置
	 * @return 指定位置的值
	 */
	public Object lindex(String key, Long index) {
		return redisTemplate.opsForList().index(key, index);
	}

	/**
	 * 从存储在键中的列表中删除等于值的元素的第一个计数事件。 count> 0: 删除等于从左到右移动的值的第一个元素; count<
	 * 0:删除等于从右到左移动的值的第一个元素; count = 0:删除等于value的所有元素。
	 * 
	 * @param key    键
	 * @param count  标志位
	 * @param object 值
	 * @return
	 */
	public boolean remove(String key, long count, Object object) {
		try {
			redisTemplate.opsForList().remove(key, count, object);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("删除redis元素,键[", key, "]标志位[", count, "],元素值[", object, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 对列表进行修建，只保留范围内数据，其余全部删掉
	 * 
	 * @param key   键
	 * @param start
	 * @param end
	 */
	public boolean trim(String key, long start, long end) {
		try {
			redisTemplate.opsForList().trim(key, start, end);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("修剪redis元素,键位[", key, "]范围[(", start, ",", end, ")]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 迁移列表元素，在列表一右侧取出元素并存至列表二的左侧
	 * 
	 * @param sourceKey 源列表键
	 * @param targetKey 目标列表键
	 * @return 目标值
	 */
	public Object rightPopAndLeftPush(String sourceKey, String targetKey) {
		return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, targetKey);
	}

	/**
	 * 迁移列表元素，在列表一右侧取出元素并存至列表二的左侧，且指定超时时间，若超时则中断
	 * 
	 * @param sourceKey 源列表键
	 * @param targetKey 目标列表键
	 * @param timeout   等待时间
	 * @param timeUnit  时间单位
	 * @return 目标值
	 */
	public Object rightPopAndLeftPush(String sourceKey, String targetKey, long timeout, TimeUnit timeUnit) {
		return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, targetKey, timeout, timeUnit);
	}
}
