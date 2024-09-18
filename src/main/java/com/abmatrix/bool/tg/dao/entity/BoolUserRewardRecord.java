package com.abmatrix.bool.tg.dao.entity;

import com.abmatrix.bool.tg.common.enuma.RewardType;
import com.abmatrix.bool.tg.common.enuma.TxStatus;
import com.alibaba.nacos.shaded.com.google.common.base.Objects;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户奖励记录表
 *
 * @author abm
 */
@Getter
@Setter
@TableName("bool_user_reward_record")
public class BoolUserRewardRecord extends BaseSqlDO {

	private static final long serialVersionUID = 1L;

	/**
	 * 奖励类型
	 */
	@EnumValue
	@TableField("reward_type")
	private RewardType type;

	/**
	 * 积分变动值
	 */
	@TableField("reward_value")
	private BigDecimal rewardValue;
	/**
	 * 用户 ID
	 */
	@TableField("user_id")
	private Long userId;
	/**
	 * 结算时间
	 */
	@TableField("reward_time")
	private LocalDateTime rewardTime;

	/**
	 * 奖励交易hash
	 */
	@TableField("tx_hash")
	private String txHash;

	/**
	 * 奖励交易状态
	 */
	@EnumValue
	@TableField("tx_status")
	private TxStatus txStatus;

	/**
	 * 上链时间
	 */
	@EnumValue
	@TableField("tx_time")
	private LocalDateTime txTime;

	/**
	 * 奖励补充信息
	 */
	@TableField("addition_info")
	private String additionInfo;

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof BoolUserRewardRecord)) {
			return false;
		}
		BoolUserRewardRecord record = (BoolUserRewardRecord) o;
		Long thisId = this.getId();
		Long oId = record.getId();
		return Objects.equal(thisId, oId);
	}

	@Override
	public int hashCode() {
		Long id = this.userId;
		if (id == null) {
			return 0;
		}
		return id.hashCode();
	}

}
