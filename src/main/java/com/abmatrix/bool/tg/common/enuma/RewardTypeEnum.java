package com.abmatrix.bool.tg.common.enuma;

import lombok.Getter;

/**
 * @author abm
 */
@Getter
public enum RewardTypeEnum {
	/**
	 * 账号评分奖励
	 */
	ACCOUNT(RewardType.REGISTER),

	/**
	 * 邀请奖励
	 */
	INVITER(RewardType.INVITATION, RewardType.INVITATION2),

	/**
	 * 任务奖励
	 */
	TASK(RewardType.JOIN_COMMUNITY, RewardType.TWITTER, RewardType.DAILY_TASK, RewardType.BEGINNER_TASK),

	/**
	 * 质押奖励
	 */
	STAKING(RewardType.STAKING),

	/**
	 * 宣传大使奖励
	 */
	AMBASSADOR(RewardType.AMBASSADOR_INCENTIVE),;

	private final RewardType[] elements;

	RewardTypeEnum(RewardType... elements) {
		this.elements = elements;
	}
}
