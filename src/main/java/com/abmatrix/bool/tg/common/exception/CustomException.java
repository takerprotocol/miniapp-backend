package com.abmatrix.bool.tg.common.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 自定义业务一处
 * 
 * @author PeterWong
 * @date 2023年7月5日
 */
public class CustomException extends RuntimeException {
	private static final long serialVersionUID = -7702037246756860455L;
	/**
	 * 异常信息
	 */
	@Getter
	private String message;

	public CustomException() {
		super();
	}

	public CustomException(Throwable t) {
		super(t);
	}

	public CustomException(String message) {
		super(message);
		this.message = message;
	}

	public static CustomException of(Throwable t) {
		if (t instanceof CustomException) {
			return (CustomException) t;
		}
		return new CustomException(t);
	}

	public static CustomException of(String message) {
		return new CustomException(message);
	}

	public static CustomException of(String messageFormat, Object... values) {
		return new CustomException(StrUtil.format(messageFormat, values));
	}

	public static CustomException of(Throwable t, String message) {
		if (t instanceof CustomException) {
			CustomException e = (CustomException) t;
			e.message = StringUtils.join(message, StrUtil.COLON, e.getMessage());
			return e;
		} else {
			CustomException e = new CustomException(t);
			e.message = StringUtils.join(message, StrUtil.COLON, e.getMessage());
			return e;
		}

	}

	/**
	 * 报错模板类型的初始化
	 * 
	 * @param t
	 * @param message
	 * @param values
	 * @return
	 */
	public static CustomException of(Throwable t, String message, Object... values) {
		if (t instanceof CustomException) {
			CustomException e = (CustomException) t;
			e.message = StringUtils.join(StrUtil.format(message, values), StrUtil.COLON, e.getMessage());
			return e;
		} else {
			CustomException e = new CustomException(t);
			e.message = StringUtils.join(StrUtil.format(message, values), StrUtil.COLON, e.getMessage());
			return e;
		}

	}
}
