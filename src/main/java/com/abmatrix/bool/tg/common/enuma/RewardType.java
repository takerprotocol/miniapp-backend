package com.abmatrix.bool.tg.common.enuma;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author abm
 */
@Getter
public enum RewardType {
	/**
	 * 用户首次注册
	 */
	REGISTER,
	/**
	 * 邀请用户
	 */
	INVITATION,
	/**
	 * 二级邀请用户
	 */
	INVITATION2,
	/**
	 * 加入官方社群
	 */
	JOIN_COMMUNITY,
	/**
	 * 加入twitter
	 */
	TWITTER,
	/**
	 * 宣传大使奖励
	 */
	AMBASSADOR_INCENTIVE,
	/**
	 * 质押奖励
	 */
	STAKING,
	/**
	 * 每日任务
	 */
	DAILY_TASK,
	/**
	 * 新手任务
	 */
	BEGINNER_TASK,
	;

	public static RewardType ofType(String type) {

		return Arrays.stream(values()).filter(t -> t.name().equals(type)).findAny().orElse(null);
	}
}
