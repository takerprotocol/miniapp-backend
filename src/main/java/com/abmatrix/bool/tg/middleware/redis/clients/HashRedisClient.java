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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 键值对缓存客户端
 * 
 * @author wangjp31109
 * @date 2021年10月9日
 */
@Lazy
@Component
@Slf4j
@Order(4)
@DependsOn("boolRedisTemplate")
@SuppressWarnings("unchecked")
@ConditionalOnBean(LettuceConfig.class)
public class HashRedisClient extends CommonRedisClient {
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
	public HashRedisClient() {
		super();
		this.clientType = RedisClientTypeEnum.HASH.getTypeName();
	}

	/**
	 * 获取map中的某个值
	 * 
	 * @param redis key
	 * @param item  项，等价于map的key
	 * @return 值
	 */
	public Object hget(String key, String item) {
		return redisTemplate.opsForHash().get(key, item);
	}


	/**
	 * 获取key对应的map对象
	 * 
	 * @param key
	 * @return 整个map对象，对标redis键值对
	 */
	public Map<Object, Object> hmget(String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	/**
	 * 以map集合的形式添加键值对
	 * 
	 * @param key key 键
	 * @param map map 对应多个键值
	 * @return true 成功 false 失败
	 */
	public boolean hmset(String key, Map<String, Object> map) {
		try {
			redisTemplate.opsForHash().putAll(key, map);
			return true;
		} catch (Exception e) {
			String mapJsonStr = JSONUtil.toJsonStr(map);
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应map对象[", mapJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 存入map对象并设置过期时间，单位秒
	 * 
	 * @param key     键
	 * @param map     map对象
	 * @param seconds 时间，秒
	 * @return true成功 false失败
	 */
	public boolean hmset(String key, Map<String, Object> map, long seconds) {
		try {
			redisTemplate.opsForHash().putAll(key, map);
			if (seconds > 0) {
				expire(key, seconds);
			}
			return true;
		} catch (Exception e) {
			String mapJsonStr = JSONUtil.toJsonStr(map);
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应map对象[", mapJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 存入map对象并设置过期时间，单位自定义
	 * 
	 * @param key      键
	 * @param map      map对象
	 * @param time     时间
	 * @param timeUnit 时间单位
	 * @return true成功 false失败
	 */
	public boolean hmset(String key, Map<String, Object> map, long time, TimeUnit timeUnit) {
		try {
			redisTemplate.opsForHash().putAll(key, map);
			if (time > 0) {
				expire(key, time, timeUnit);
			}
			return true;
		} catch (Exception e) {
			String mapJsonStr = JSONUtil.toJsonStr(map);
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应map对象[", mapJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据，如果不存在将创建
	 * 
	 * @param key   键
	 * @param item  项
	 * @param value 值
	 * @return true成功 false失败
	 */
	public boolean hput(String key, String item, Object value) {
		try {
			redisTemplate.opsForHash().put(key, item, value);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis插入键位[", key, "]键值对数据[", item, "-", value, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据，如果不存在将创建，并且设置过期时间，单位秒
	 * 
	 * @param key     键
	 * @param item    项
	 * @param value   值
	 * @param seconds 时间，秒
	 * @return true成功 false失败
	 */
	public boolean hput(String key, String item, Object value, long seconds) {
		try {
			redisTemplate.opsForHash().put(key, item, value);
			if (seconds > 0) {
				expire(key, seconds);
			}
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis插入键位[", key, "]键值对数据[", item, "-", value, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据，如果不存在将创建，并且设置过期时间，单位自定义
	 * 
	 * @param key      键
	 * @param item     项
	 * @param value    值
	 * @param time     时间
	 * @param timeUnit 时间单位
	 * @return true成功 false失败
	 */
	public boolean hput(String key, String item, Object value, long time, TimeUnit timeUnit) {
		try {
			redisTemplate.opsForHash().put(key, item, value);
			if (time > 0) {
				expire(key, time, timeUnit);
			}
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis插入键位[", key, "]键值对数据[", item, "-", value, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 删除hash表中的值
	 * 
	 * @param key  键 不能为null
	 * @param item 项 可以使多个 不能为null
	 * @return true成功 false失败
	 */
	public boolean hdel(String key, Object... items) {
		try {
			redisTemplate.opsForHash().delete(key, items);
			return true;
		} catch (Exception e) {
			String itemStr = JSONUtil.toJsonStr(items);
			String errorMsg = StringUtils.join("删除redis键位[", key, "]各map项[", itemStr, "]失败");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 判断hash表中是否有该项的值
	 * 
	 * @param key  键 不能为null
	 * @param item 项 不能为null
	 * @return true 存在 false不存在
	 */
	public boolean hHasKey(String key, String item) throws Exception {
		return redisTemplate.opsForHash().hasKey(key, item);
	}

	/**
	 * hash递增 如果不存在,就会创建一个 并把新增后的值返回
	 * 
	 * @param key  键
	 * @param item 项
	 * @param by   要增加的数
	 * @return 新增后的值
	 */
	public long hincr(String key, String item, long by) throws Exception {
		return redisTemplate.opsForHash().increment(key, item, by);
	}

	/**
	 * hash递减
	 * 
	 * @param key
	 * @param item
	 * @param by   要减少的数
	 * @return 减少后的值
	 */
	public long hdecr(String key, String item, long by) {
		return redisTemplate.opsForHash().increment(key, item, -by);
	}

	/**
	 * 获取指定key中的hashMap的所有的值
	 * 
	 * @param key 键
	 * @return 值列表
	 */
	public List<Object> hvalues(String key) {
		return redisTemplate.opsForHash().values(key);
	}

	/**
	 * 获取指定key中hashMap所有的键
	 * 
	 * @param key 键
	 * @return map键集合
	 */
	public Set<Object> hkeys(String key) {
		return redisTemplate.opsForHash().keys(key);
	}

	/**
	 * 获取变量的长度
	 * 
	 * @param key 键
	 * @return 返回长度
	 */
	public long hsize(String key) {
		return redisTemplate.opsForHash().size(key);
	}

	/**
	 * 以集合的方式获取map对象中多个item的值
	 * 
	 * @param key  键
	 * @param list item列表
	 * @return
	 */
	public List<Object> hmultiGet(String key, List<Object> items) {
		return redisTemplate.opsForHash().multiGet(key, items);
	}

	/**
	 * 不存在map则新建map map中存在item则不插入且返回item对应的value, map中不存在item则插入item-value并返回null
	 * 
	 * @param key   键
	 * @param item  map键
	 * @param value 值
	 * @return true插入成功，false插入失败
	 */
	public boolean hputIfAbsent(String key, String item, Object value) {
		try {
			redisTemplate.opsForHash().putIfAbsent(key, item, value);
			return true;
		} catch (Exception e) {
			String errorMsg = StringUtils.join("redis插入键位[", key, "]键值对数据[", item, "-", value, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 匹配获取键值对，ScanOptions.NONE为获取全部键对，
	 * ScanOptions.scanOptions().match("map1").build() 匹配获取键位map1的键值对,不能模糊匹配。
	 * 类似于非关系数据库的条件查询
	 * 
	 * @param key
	 * @param options
	 * @return
	 */
	public Cursor<Map.Entry<Object, Object>> hscan(String key, ScanOptions options) {
		return redisTemplate.opsForHash().scan(key, options);
	}

}
