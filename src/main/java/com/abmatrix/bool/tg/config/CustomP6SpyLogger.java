package com.abmatrix.bool.tg.config;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Sets;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.Slf4JLogger;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 自定义p6spy日志
 * 
 * @author PeterWong
 * @date 2024年8月30日
 */
@Component
public class CustomP6SpyLogger extends Slf4JLogger {
	/**
	 * 慢sql时间阈值,单位毫秒
	 */
	private Integer slowSqlDuration = 2000;

	private static final Set<String> NO_MONITOR_SET = Sets.newHashSet("select 1");

	@Override
	public void logSQL(int connectionId, String now, long elapsed, Category category, String prepared, String sql,
			String url) {
		if (NO_MONITOR_SET.contains(prepared)) {
			return;
		}
		String sqlSlowValue = SpringUtil.getProperty("sql.slow");
		try {
			slowSqlDuration = Integer.valueOf(sqlSlowValue);
		} catch (NumberFormatException e) {
		}
		if (elapsed >= Integer.valueOf(slowSqlDuration)) {
			super.logSQL(connectionId, now, elapsed, category, prepared, sql, url);
		}
	}
}
