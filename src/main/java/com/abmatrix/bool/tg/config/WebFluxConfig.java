package com.abmatrix.bool.tg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * webflux缓冲配置
 * 
 * @author PeterWong
 * @date 2024年8月9日
 */
@Configuration
public class WebFluxConfig {
	/**
	 * 跨域通配符
	 */
	@Value("${cors.patterns:https://*.bool.network}")
	private String originPatterns;

	@Bean
	public WebFluxConfigurer webFluxConfigurer() {
		return new WebFluxConfigurer() {
			@Override
			public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
				configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024); // 增加内存缓冲区大小
			}

		};
	}
}
