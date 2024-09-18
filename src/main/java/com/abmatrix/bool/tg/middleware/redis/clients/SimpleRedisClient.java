package com.abmatrix.bool.tg.middleware.redis.clients;

import com.abmatrix.bool.tg.middleware.redis.config.LettuceConfig;
import com.abmatrix.bool.tg.middleware.redis.config.configbean.BoolRedisTemplate;
import com.abmatrix.bool.tg.middleware.redis.enums.RedisClientTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 获取简单数据
 * 
 * @author wangjp31109
 * @date 2021年10月9日
 */
@Lazy
@Slf4j
@Component(value = "simpleRedisClient")
@DependsOn("boolRedisTemplate")
@AutoConfigureAfter(BoolRedisTemplate.class)
@SuppressWarnings("unchecked")
@ConditionalOnBean(LettuceConfig.class)
public class SimpleRedisClient extends CommonRedisClient {

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
	 * 构造方法
	 */
	public SimpleRedisClient() {
		super();
		this.clientType = RedisClientTypeEnum.SIMPLE.getTypeName();
	}

	/**
	 * 存储数据
	 * 
	 * @param key
	 * @param value
	 * @param expireTime 过期时间，单位秒
	 * @return
	 */
	public Boolean set(String key, Object value, int expireTime) {
		try {
			redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
			return true;
		} catch (Exception e) {
			log.error("redis存储数据异常:", e);
			return false;
		}
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param expireTime 过期时间
	 * @param timeUnit   自定义时间单位
	 * @return
	 */
	public Boolean set(String key, Object value, int expireTime, TimeUnit timeUnit) {
		try {
			redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
			return true;
		} catch (Exception e) {
			log.error("redis存储数据异常:", e);
			return false;
		}
	}

	/**
	 * 不限制过期时间的存储数据
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Boolean set(String key, Object value) {
		try {
			redisTemplate.opsForValue().set(key, value);
			return true;
		} catch (Exception e) {
			log.error("redis存储数据异常:", e);
			return false;
		}
	}

	/**
	 * 获取数据
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		try {
			return redisTemplate.opsForValue().get(key);
		} catch (Exception e) {
			log.error("redis获取数据异常:", e);
			return null;
		}
	}

	/**
	 * 计数器添加一个数
	 * 
	 * @param key
	 * @param by
	 * @return
	 */
	public long incr(String key, long by) {
		return redisTemplate.opsForValue().increment(key, by);
	}

	/**
	 * 计数器加1
	 * 
	 * @param key
	 * @return
	 */
	public long incr1(String key) {
		return redisTemplate.opsForValue().increment(key);
	}

	/**
	 * 如果没有key,则创建一个并赋值，如果有，则什么都不足
	 * 
	 * @param key
	 * @param value
	 * @param expireTime
	 * @param timeUnit
	 * @return
	 */
	public Boolean setIfAbsent(String key, Object value, int expireTime, TimeUnit timeUnit) {
		return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, timeUnit);
	}

}
