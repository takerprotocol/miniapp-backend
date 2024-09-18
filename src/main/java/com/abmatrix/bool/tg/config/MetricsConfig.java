package com.abmatrix.bool.tg.config;

import com.google.common.collect.Lists;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 指标配置
 * 
 * @author PeterWong
 * @date 2024年8月25日
 */
@Configuration
@Slf4j
public class MetricsConfig {
	@Bean
	public MeterFilter addCommonTags() {
		String hostAddress = "unknown";
		try {
			// 获取本机 IP 地址
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.error("获取本机IP异常", e);
		}
		// 返回一个 MeterFilter，用于全局添加标签
		Tag hostTag = Tag.of("host", hostAddress);
		List<Tag> tagList = Lists.newArrayList(hostTag);
		return MeterFilter.commonTags(tagList);
	}

}
