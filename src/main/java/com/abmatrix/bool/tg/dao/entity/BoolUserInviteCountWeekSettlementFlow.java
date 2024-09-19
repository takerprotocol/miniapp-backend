package com.abmatrix.bool.tg.dao.entity;

import com.abmatrix.bool.tg.common.enuma.WeekSettleStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 周榜结算流水
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-28
 */
@Getter
@Setter
@TableName("bool_user_invite_count_week_settlement_flow")
public class BoolUserInviteCountWeekSettlementFlow extends Model<BoolUserInviteCountWeekSettlementFlow> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 版本号
     */
    @TableField("version")
    private Long version;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField("create_user")
    private Long createUser;

    /**
     * 修改人
     */
    @TableField("update_user")
    private Long updateUser;

    /**
     * 用户ID
     */
    @TableField("f_user_id")
    private Long userId;

    /**
     * 邀请人数量1级+2级
     */
    @TableField("f_amount")
    private Long amount;

    /**
     * 业务时间
     */
    @TableField("f_calculate_timestamp")
    private Long calculateTimestamp;

    /**
     * 对应快照记录ID
     */
    @TableField("f_snapshot_id")
    private Long snapshotId;

    /**
     * 奖励
     */
    @TableField("f_award")
    private String award;

    /**
     * 排名 1-10
     */
    @TableField("f_rank")
    private Integer rank;

    /**
     * 对应抽奖记录ID，周结算奖也放入抽奖记录中，方便提币
     */
    @TableField("f_draw_id")
    private Long drawId;
    /**
     * 交割状态
     */
    @TableField("f_status")
    private WeekSettleStatusEnum status;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
