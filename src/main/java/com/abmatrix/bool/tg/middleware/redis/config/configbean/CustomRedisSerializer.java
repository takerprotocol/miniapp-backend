package com.abmatrix.bool.tg.middleware.redis.config.configbean;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 自定义redis序列化
 * 
 * @param <T>
 * @author PeterWong
 * @date 2024年8月3日
 */
public class CustomRedisSerializer<T> implements RedisSerializer<T> {
	private final Jackson2JsonRedisSerializer<T> jacksonSerializer;
	private final FastJsonRedisSerializer<T> fastJsonSerializer;

	public CustomRedisSerializer(Class<T> type) {
		JavaTimeModule module = new JavaTimeModule();
		LocalDateTimeDeserializer dateTimeDeserializer = new LocalDateTimeDeserializer(
				DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN));
		LocalDateTimeSerializer dateTimeSerializer = new LocalDateTimeSerializer(
				DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN));
		module.addDeserializer(LocalDateTime.class, dateTimeDeserializer);
		module.addSerializer(LocalDateTime.class, dateTimeSerializer);
		ObjectMapper om = new ObjectMapper();
		om.registerModule(module);
		om.setVisibility(PropertyAccessor.ALL, com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY);
		om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.PROPERTY);
		this.jacksonSerializer = new Jackson2JsonRedisSerializer<>(type);
		this.fastJsonSerializer = new FastJsonRedisSerializer<>(type);
	}

	@Override
	public byte[] serialize(T t) throws SerializationException {
		return jacksonSerializer.serialize(t);
	}

	@Override
	public T deserialize(byte[] bytes) throws SerializationException {
		T obj = null;
		try {
			obj = jacksonSerializer.deserialize(bytes);
		} catch (Exception e) {
			obj = fastJsonSerializer.deserialize(bytes);
		}
		return obj;
	}
}
