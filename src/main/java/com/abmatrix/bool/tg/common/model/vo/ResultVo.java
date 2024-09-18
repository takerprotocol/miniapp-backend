package com.abmatrix.bool.tg.common.model.vo;

import cn.hutool.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局返回值结果对象
 * 
 * @param <T>
 * @author PeterWong
 * @date 2023年7月4日
 */
@Data
@AllArgsConstructor
public class ResultVo<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 返回码
	 */
	private Integer code;
	/**
	 * 返回消息
	 */
	private String message;
	/**
	 * 数据实体
	 */
	private T data;

	/**
	 * 成功消息
	 */
	private static final String SUCCESS_MSG = "success";

	/**
	 * 失败消息
	 */
	private static final String FAIL_MSG = "error";

	/**
	 * 返回成功复合对象
	 * 
	 * @return
	 */
	public static final <T> ResultVo<T> success() {
		return new ResultVo<T>(HttpStatus.HTTP_OK, SUCCESS_MSG, null);
	}

	/**
	 * 返回成功对象
	 * 
	 * @param data
	 * @return
	 */
	public static final <T> ResultVo<T> success(T data) {
		return new ResultVo<T>(HttpStatus.HTTP_OK, SUCCESS_MSG, data);
	}

	/**
	 * 请求失败对象
	 * 
	 * @return
	 */
	public static final <T> ResultVo<T> fail() {
		return new ResultVo<T>(HttpStatus.HTTP_INTERNAL_ERROR, FAIL_MSG, null);
	}

	/**
	 * 请求失败对象
	 * 
	 * @param errMsg
	 * @return
	 */
	public static final <T> ResultVo<T> fail(String errMsg) {
		return new ResultVo<T>(HttpStatus.HTTP_INTERNAL_ERROR, errMsg, null);
	}

	/**
	 * 请求失败对象
	 * 
	 * @param code
	 * @param errMsg
	 * @return
	 */
	public static final <T> ResultVo<T> fail(Integer code, String errMsg) {
		return new ResultVo<T>(code, errMsg, null);
	}

	/**
	 * 提示成功，但是不提交data且自定义message
	 * 
	 * @param <T>
	 * @param msg
	 * @return
	 */
	public static final <T> ResultVo<T> successWithMsg(String msg) {
		return new ResultVo<T>(HttpStatus.HTTP_OK, msg, null);
	}

	/**
	 * 提示失败，但是有失败返回值
	 * 
	 * @param <T>
	 * @param code
	 * @param errMsg
	 * @param data
	 * @return
	 */
	public static final <T> ResultVo<T> fail(Integer code, String errMsg, T data) {
		return new ResultVo<T>(code, errMsg, data);
	}

}
