package com.abmatrix.bool.tg.common.enuma;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务错误码合计
 * 
 * @author PeterWong
 * @date 2024年8月20日
 */
@Getter
@AllArgsConstructor
public enum AssignmentErrorEnum {
	TG_IDENTIFIER_LOST(52339, "User TG identifier lost."), USER_INFO_LOST(52340, "User info lost."),
	USER_UN_VERIFY(52341, "User unverified."),
	// 操作太频繁
	OPERATION_BUSY(52343, "Operation too frequent"),
	// 助力校验结果为失败
	UN_BOOST(52348, "Boost not executed"),
	// 代码逻辑错误
	LOGIC_ERR(52334, "Logic error."),
	// 未加群
	UN_JOIN(52349, "Not subscribed to the channel."),
	// 没有徽章
	NO_BADGE(52350, "No badge.")

	;

	private Integer code;
	private String message;
}
