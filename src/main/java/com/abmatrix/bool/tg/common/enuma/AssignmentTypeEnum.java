package com.abmatrix.bool.tg.common.enuma;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类别
 */
@AllArgsConstructor
@Getter
public enum AssignmentTypeEnum implements IEnum<Integer> {
	TWITTER(1),
	COMMUNITY(2),
	LAUNCH_BOT(3),
	PLAY_GAME(4),
	PARTICIPATE_CAMPAIGN(5),
	VISIT_WEBSITE(6),
	SUBSCRIBE_YOUTUBE(7),
	READ_MEDIUM(8),
	// 助力
	BOOST(9),
	/**
	 * 每日任务
	 */
	DAILY(10);

	private final int code;

	@Override
	public Integer getValue() {
		return this.code;
	}
}
