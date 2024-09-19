package com.abmatrix.bool.tg.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计算排名数据偏移量
 * 
 * @author PeterWong
 * @date 2024年8月12日
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BoolCalculateRankOffset extends BaseSqlDO {

	/**
	 * 最终偏移量ID
	 */
	@TableField("f_latest_reward_id")
	private Long latestRewardId;
	/**
	 * 类别 1积分排行 2全量邀请排行
	 */
	@TableField("f_type")
	private Integer rankType;
}
