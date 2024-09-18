package com.abmatrix.bool.tg.common.enuma;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户个人信息类别枚举
 * 
 * @author PeterWong
 * @date 2024年8月8日
 */
@AllArgsConstructor
@Getter
public enum PersonalInfoTypeEnum implements IEnum<Integer> {
	URL(1),;

	private final int code;

	@Override
	public Integer getValue() {
		return this.code;
	}
}
