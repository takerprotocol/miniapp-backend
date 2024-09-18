package com.abmatrix.bool.tg.aop;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.abmatrix.bool.tg.common.exception.CustomException;
import com.abmatrix.bool.tg.common.model.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 异常拦截
 * 
 * @author PeterWong
 * @date 2023年8月18日
 */
@ControllerAdvice
@Slf4j
public class SpringControllerAdvice {
	/**
	 * 异常处理
	 * 
	 * @param ex
	 * @return
	 */
	@ResponseBody
	@ExceptionHandler(value = Exception.class)
	public Mono<ResponseEntity<?>> errorHandler(Throwable ex, ServerWebExchange exchange) {
		// 获取请求的路径
		String path = exchange.getRequest().getPath().pathWithinApplication().value();
		// 打印日志或处理异常信息
		log.error("Error occurred at path: {}", path, ex);
		if (ex instanceof CustomException) {
			return Mono.just(ResponseEntity.internalServerError().body(ResultVo.fail("System busy...")));
		}
		if (ex instanceof ResponseStatusException) {
			ResponseStatusException exr = (ResponseStatusException) ex;
			return Mono.just(ResponseEntity.status(exr.getStatusCode())
					.body(ResultVo.fail(exr.getStatusCode().value(), exr.getReason())));
		}
		if (ex instanceof IllegalArgumentException) {
			IllegalArgumentException exr = (IllegalArgumentException) ex;
			return Mono.just(
					ResponseEntity.internalServerError().body(ResultVo.fail(ExceptionUtil.getRootCauseMessage(exr))));
		}
		return Mono.just(ResponseEntity.internalServerError().body(ResultVo.fail("System busy...")));
	}

}
