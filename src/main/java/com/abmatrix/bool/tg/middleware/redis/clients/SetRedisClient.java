package com.abmatrix.bool.tg.middleware.redis.clients;

import cn.hutool.json.JSONUtil;
import com.abmatrix.bool.tg.middleware.redis.config.LettuceConfig;
import com.abmatrix.bool.tg.middleware.redis.enums.RedisClientTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
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
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 无序集合控制类客户端
 * 
 * @author wangjp31109
 * @param <V>
 * @date 2021年10月10日
 */
@Lazy
@Slf4j
@Component
@Order(5)
@DependsOn("boolRedisTemplate")
@SuppressWarnings("unchecked")
@ConditionalOnBean(LettuceConfig.class)
public class SetRedisClient<V> extends CommonRedisClient {
	/**
	 * spring上下文
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * redission
	 */
	@Autowired
	private RedissonClient redissonClient;

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
	public SetRedisClient() {
		super();
		this.clientType = RedisClientTypeEnum.SET.getTypeName();
	}

	/**
	 * 往set对象中插入值
	 * 
	 * @param key     键
	 * @param objects 值，可传多值
	 * @return true插入成功，false插入失败
	 */
	public boolean add(String key, Object... values) {
		try {
			redisTemplate.opsForSet().add(key, values);
			return true;
		} catch (Exception e) {
			String setJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应set对象[", setJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * set对象中插入值，并设置过期时间，单位秒
	 * 
	 * @param key     键
	 * @param seconds 持续时间
	 * @param values  值，可传多值
	 * @return
	 */
	public boolean addForExpire(String key, long seconds, Object... values) {
		try {
			redisTemplate.opsForSet().add(key, values);
			if (seconds > 0) {
				expire(key, seconds);
			}
			return true;
		} catch (Exception e) {
			String setJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应set对象[", setJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * set对象中插入值，并设置过期时间，单位自定义
	 * 
	 * @param key      键
	 * @param time     持续时间
	 * @param timeUnit 时间单位
	 * @param values   值，可传多值
	 * @return
	 */
	public boolean addForExpire(String key, long time, TimeUnit timeUnit, Object... values) {
		try {
			redisTemplate.opsForSet().add(key, values);
			if (time > 0) {
				expire(key, time, timeUnit);
			}
			return true;
		} catch (Exception e) {
			String setJsonStr = JSONUtil.toJsonStr(values);
			String errorMsg = StringUtils.join("redis存入键位[", key, "]对应set对象[", setJsonStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 直接通过key获取无序set对象
	 * 
	 * @param key 键
	 * @return set对象
	 */
	public Set<Object> members(String key) {
		return redisTemplate.opsForSet().members(key);
	}

	/**
	 * 获取set长度
	 * 
	 * @param key 键
	 * @return 长度
	 */
	public long size(String key) {
		return redisTemplate.opsForSet().size(key);
	}

	/**
	 * 检查元素是否在set集合中
	 * 
	 * @param key 键
	 * @param o   元素对象
	 * @return
	 */
	public boolean isMember(String key, Object o) {
		return redisTemplate.opsForSet().isMember(key, o);
	}

	/**
	 * 移动目标元素到新set
	 * 
	 * @param key     源集合key
	 * @param value   值
	 * @param destKey 目标key
	 * @return
	 */
	public boolean move(String key, Object value, String destKey) {
		return redisTemplate.opsForSet().move(key, value, destKey);
	}

	/**
	 * 弹出元素并返回，该操作随机无序
	 * 
	 * @param key 键
	 * @return 弹出的元素
	 */
	public Object pop(String key) {
		return redisTemplate.opsForSet().pop(key);
	}

	/**
	 * 批量移除目标元素
	 * 
	 * @param key    键
	 * @param values 要移除的值
	 * @return 删除掉值的个数
	 */
	public long remove(String key, Object... values) {
		return redisTemplate.opsForSet().remove(key, values);
	}

	/**
	 * 条件获取结果集
	 * 
	 * @param key     对应集合key
	 * @param options 搜索条件
	 * @return
	 */
	public Cursor<Object> scan(String key, ScanOptions options) {
		return redisTemplate.opsForSet().scan(key, options);
	}

	/**
	 * 目标集合与其它集合的差异结果
	 * 
	 * @param key  目标集合key
	 * @param list 其它集合的kye列表
	 * @return
	 */
	public Set<Object> difference(String key, List<String> otherKeyList) {
		return redisTemplate.opsForSet().difference(key, otherKeyList);
	}

	/**
	 * 获取目标key的set与另一个key的set的差异集合
	 * 
	 * @param key        源键
	 * @param anotherKey 目标键
	 * @return
	 */
	public Set<Object> difference(String key, String anotherKey) {
		return redisTemplate.opsForSet().difference(key, anotherKey);
	}

	/**
	 * 求出源set与目标set差异值并存储至新的set集合
	 * 
	 * @param key      源set键
	 * @param otherKey 对比set键
	 * @param destKey  存储set的键
	 */
	public void differenceAndStore(String key, String otherKey, String destKey) {
		redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
	}

	/**
	 * 求出源set与多目标set差异值并存储至新的set集合
	 * 
	 * @param key       源set 的键
	 * @param otherKeys 比较set键的列表
	 * @param destKey   存储目标set的键
	 */
	public void differenceAndStore(String key, List<String> otherKeys, String destKey) {
		redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
	}

	/**
	 * 获取去重的随机元素
	 * 
	 * @param key   键
	 * @param count 指定返回的个数
	 * @return
	 */
	public Set<Object> distinctRandomMembers(String key, long count) {
		return redisTemplate.opsForSet().distinctRandomMembers(key, count);
	}

	/**
	 * 获取2个set对象的交集
	 * 
	 * @param key
	 * @param otherKey
	 * @return
	 */
	public Set<Object> intersect(String key, String otherKey) {
		return redisTemplate.opsForSet().intersect(key, otherKey);
	}

	/**
	 * 获取源set对象与多目标set对象的交集
	 * 
	 * @param key  键
	 * @param list 比较对象的键列表
	 * @return
	 */
	public Set<Object> intersect(String key, List<String> list) {
		return redisTemplate.opsForSet().intersect(key, list);
	}

	/**
	 * 获取两set交集并存储到新的set中
	 * 
	 * @param key      源键
	 * @param otherKey 比较键
	 * @param destKey  目标键
	 */
	public void intersectAndStore(String key, String otherKey, String destKey) {
		redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
	}

	/**
	 * 获取源键set对象与多个键的set对象交集并存储至新的set中
	 * 
	 * @param key       源键
	 * @param otherKeys 比较键集合
	 * @param destKey   目标键
	 */
	public void intersectAndStore(String key, List<String> otherKeys, String destKey) {
		redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
	}

	/**
	 * 获取两个set的并集
	 * 
	 * @param key      源键
	 * @param otherKey 比较键
	 * @return
	 */
	public Set<Object> union(String key, String otherKey) {
		return redisTemplate.opsForSet().union(key, otherKey);
	}

	/**
	 * 获取源set与多个比较set的并集
	 * 
	 * @param key       源键
	 * @param otherKeys 比较集合的键
	 * @return
	 */
	public Set<Object> union(String key, List<String> otherKeys) {
		return redisTemplate.opsForSet().union(key, otherKeys);
	}

	/**
	 * 获取两set并集并存储到新的set中
	 * 
	 * @param key      源键
	 * @param otherKey 比较键
	 * @param destKey  目标新集合的键
	 */
	public void unionAndStore(String key, String otherKey, String destKey) {
		redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
	}

	/**
	 * 获取源set与多个比较set的并集并存储到新的集合中
	 * 
	 * @param key       源set键
	 * @param otherKeys 比较set键
	 * @param destKey   目标set键
	 */
	public void unionAndStore(String key, List<String> otherKeys, String destKey) {
		redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
	}

	/**
	 * 返回随机元素
	 * 
	 * @param key 键
	 * @return
	 */
	public Object randomMember(String key) {
		return redisTemplate.opsForSet().randomMember(key);
	}

	/**
	 * 返回指定个数的随机元素
	 * 
	 * @param key   键
	 * @param count 指定个数
	 * @return
	 */
	public List<Object> randomMembers(String key, long count) {
		return redisTemplate.opsForSet().randomMembers(key, count);
	}

	/**
	 * 
	 * @param <T>
	 * @param key
	 * @param value
	 * @param ttl
	 * @param timeUnit
	 */
	public <T> void addAndItemExpire(String key, T value, Integer ttl, TimeUnit timeUnit) {
		RSetCache<T> set = redissonClient.getSetCache(key);
		set.add(value, ttl, timeUnit);
	}

	/**
	 * 从定时的set中删除元素
	 * 
	 * @param key
	 * @param value
	 */
	public <T> void removeFromExpiredItemSet(String key, T value) {
		RSetCache<T> set = redissonClient.getSetCache(key);
		set.remove(value);
	}

	/**
	 * 从定时的set中删除多个元素
	 * 
	 * @param <T>
	 * @param key
	 * @param values
	 */
	public <T> void removeMultiFromExpiredItemSet(String key, List<T> values) {
		RSetCache<T> set = redissonClient.getSetCache(key);
		set.removeAll(values);
	}

	/**
	 * 从定时的set中读取多个元素
	 * 
	 * @param key
	 * @return
	 */
	public Set<Object> getAllFromExpiredItemSet(String key) {
		RSetCache<Object> set = redissonClient.getSetCache(key);
		Set<Object> result = set.readAll();
		return result;
	}
}
