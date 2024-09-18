package com.abmatrix.bool.tg.middleware.redis.config;

import com.abmatrix.bool.tg.middleware.redis.config.configbean.BoolRedisTemplate;
import com.abmatrix.bool.tg.middleware.redis.config.configbean.CustomRedisSerializer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * spring redis template配置
 * 
 * @author PeterWong
 * @date 2023年7月7日
 */
@Slf4j
@Configuration
@Order(Integer.MIN_VALUE)
@ConditionalOnProperty(prefix = "spring.data.redis", name = { "database" })
public class LettuceConfig {
	/**
	 * 初始化
	 */
	@PostConstruct
	private void init() {
		log.info("redis配置初始化.");
	}

	/**
	 * redis客户端实例化
	 * 
	 * @param factory
	 * @return
	 */
	@Bean(name = "boolRedisTemplate")
	@Order(Integer.MIN_VALUE)
	public BoolRedisTemplate lettuceRedisTemplate(RedisConnectionFactory factory) {
		log.info("开始实例化基础lettuce客户端.");
		try {
			BoolRedisTemplate template = new BoolRedisTemplate();
			if (factory instanceof LettuceConnectionFactory) {
				LettuceConnectionFactory c = (LettuceConnectionFactory) factory;
				c.setValidateConnection(true);
				int dbIndex = c.getDatabase();
				log.info("db id:{}", dbIndex);
			}
			template.setConnectionFactory(factory);
			// 使用自定义序列化器
			CustomRedisSerializer<Object> customSerializer = new CustomRedisSerializer<>(Object.class);
			StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
			// key采用String的序列化方式
			template.setKeySerializer(stringRedisSerializer);
			// hash的key也采用String的序列化方式
			template.setHashKeySerializer(stringRedisSerializer);
			// value序列化方式采用jackson
			template.setValueSerializer(customSerializer);
			// hash的value序列化方式采用jackson
			template.setHashValueSerializer(customSerializer);
			template.afterPropertiesSet();
			log.info("lettuce客户端实例化成功.");

			return template;
		} catch (Exception e) {
			log.info("基础lettuce客户端实例化失败.", e);
			throw (e);
		}
	}

}
