package com.abmatrix.bool.tg;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * 启动类
 *
 * @author PeterWong
 * @date 2024年7月13日
 */
@SpringBootApplication(scanBasePackages = {"com.abmatrix.bool.tg"})
@EnableConfigurationProperties
@EnableDiscoveryClient
@EnableAspectJAutoProxy
@EnableWebFlux
@EnableScheduling
public class ApplicationStart {
    /**
     * 初始化方法
     *
     * @param args
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder().web(WebApplicationType.REACTIVE).sources(ApplicationStart.class).run(args);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
