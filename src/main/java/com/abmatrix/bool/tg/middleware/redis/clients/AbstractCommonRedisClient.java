package com.abmatrix.bool.tg.middleware.redis.clients;

import java.util.concurrent.TimeUnit;

/**
 * 抽象版本的redis客户端
 * 
 * @author wangjp31109
 * @date 2021年10月10日
 */
public abstract class AbstractCommonRedisClient {
	/**
	 * 设置过期时间
	 * 
	 * @param key
	 * @param seconds
	 * @return
	 */
	public abstract boolean expire(String key, long seconds);

	/**
	 * 设置过期时间并指定单位
	 * 
	 * @param key
	 * @param time
	 * @param timeUnit
	 * @return
	 */
	public abstract boolean expire(String key, long time, TimeUnit timeUnit);
}
