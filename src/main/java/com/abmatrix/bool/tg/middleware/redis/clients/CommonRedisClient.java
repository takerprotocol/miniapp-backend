package com.abmatrix.bool.tg.middleware.redis.clients;

import cn.hutool.json.JSONUtil;
import com.abmatrix.bool.tg.middleware.redis.config.configbean.BoolRedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * 普通redis客户端
 * 
 * @author PeterWong
 * @date 2023年1月22日
 */
@Slf4j
@SuppressWarnings("unchecked")
public class CommonRedisClient extends AbstractCommonRedisClient implements InitializingBean {

	/**
	 * redis
	 */
	@SuppressWarnings("rawtypes")
	protected RedisTemplate redisTemplate;

	/**
	 * spring上下文
	 */
	protected ApplicationContext applicationContext;
	/**
	 * 客户端类型名称
	 */
	protected String clientType;

	@SuppressWarnings("rawtypes")
	protected void init(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		BoolRedisTemplate boolRedisTemplate = (BoolRedisTemplate) applicationContext.getBean("boolRedisTemplate");
		if (boolRedisTemplate != null) {
			this.redisTemplate = boolRedisTemplate;
			return;
		}
		Map<String, RedisTemplate> rs = applicationContext.getBeansOfType(RedisTemplate.class);
		if (rs.size() == 0) {
			log.warn("未找到redistemplate的bean");
			this.redisTemplate = null;
			return;
		}
		boolRedisTemplate = (BoolRedisTemplate) rs.get("boolRedisTemplate");
		if (boolRedisTemplate != null) {
			log.info("通用客户端使用中间件版本的redis客户端,bean名称:[boolRedisTemplate]");
			this.redisTemplate = boolRedisTemplate;
			return;
		} else {
			log.warn("未找到服务内redisTemplate[name={},templatePool={}]", "boolRedisTemplate", rs.keySet().toString());
		}
		Iterator<Entry<String, RedisTemplate>> iterator = rs.entrySet().iterator();
		Entry<String, RedisTemplate> entry;
		String redisTemplateName;
		String[] primaryBeanNames = applicationContext.getBeanNamesForAnnotation(Primary.class);
		Set<String> primaryBeanNameSet = new HashSet<>(Arrays.asList(primaryBeanNames));
		int counter = 0;
		String finalRedisTemplateName = null;
		while (iterator.hasNext()) {
			entry = iterator.next();
			redisTemplateName = entry.getKey();
			if (counter == 0) {
				this.redisTemplate = entry.getValue();
				finalRedisTemplateName = entry.getKey();
				counter++;
			}
			if (primaryBeanNameSet.contains(redisTemplateName)) {
				this.redisTemplate = entry.getValue();
				finalRedisTemplateName = entry.getKey();
				log.info("通用客户端使用平台内部版本指定的redis客户端,bean名称:[{}]", entry.getKey());
				return;
			}
		}
		log.info("通用客户端使用平台内部版本随机redis客户端,bean名称:[{}]", finalRedisTemplateName);
	}

	@Override
	public String toString() {
		if (redisTemplate == null) {
			return "定制化客户端初始化未完成.";
		}
		String randomKey = (String) redisTemplate.randomKey();
		return StringUtils.join("定制化客户端初始化已完成，客户端类别:[", clientType, "]，类路径[", this.getClass().getSimpleName(),
				"]，校验随机标识：[", randomKey, "]");
	}

	/**
	 * 
	 * @param redisTemplate
	 */
	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 对redis key设置过期时间，单位秒
	 * 
	 * @param key     键
	 * @param seconds 时间，秒
	 * @return
	 */
	@Override
	public boolean expire(String key, long seconds) {
		return expire(key, seconds, TimeUnit.SECONDS);
	}

	/**
	 * 对redis key设置过期时间，单位自定义
	 * 
	 * @param key      键
	 * @param time     时间
	 * @param timeUnit 自定义单位
	 * @return true成功 false失败
	 */
	@Override
	public boolean expire(String key, long time, TimeUnit timeUnit) {
		return redisTemplate.expire(key, time, timeUnit);
	}

	/**
	 * 批量删除key对象
	 * 
	 * @param keys 键，多值
	 * @return true删除成功，false删除失败
	 */
	public boolean batchDelByKeys(String... keys) {
		try {
			if (keys != null && keys.length > 0) {
				if (keys.length == 1) {
					redisTemplate.delete(keys[0]);
				} else {
					redisTemplate.delete(new ArrayList<String>(Arrays.asList(keys)));
				}
			}
			return true;
		} catch (Exception e) {
			String keyStr = JSONUtil.toJsonStr(keys);
			String errorMsg = StringUtils.join("删除redis对象对应键，[", keyStr, "]异常");
			log.error(errorMsg, e);
			return false;
		}
	}

	/**
	 * 是否有当前key
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			log.info(toString());
		} catch (Exception e) {
			String errorMsg = StringUtils.join(this.getClass().getSimpleName(), "初始化错误");
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg);
		}
	}
}
