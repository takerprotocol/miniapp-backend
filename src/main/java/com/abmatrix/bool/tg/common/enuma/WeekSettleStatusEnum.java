package com.abmatrix.bool.tg.common.enuma;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 周榜结算记录状态
 * 
 * @author PeterWong
 * @date 2024年9月3日
 */
@AllArgsConstructor
@Getter
public enum WeekSettleStatusEnum implements IEnum<Integer> {
	// 初始化未发放
	INIT(0),
	// 已发放未提币
	GRANTED(1),
	// 已提币
	WITHDRAWED(2);

	private final int code;

	@Override
	public Integer getValue() {
		return this.code;
	}
}
