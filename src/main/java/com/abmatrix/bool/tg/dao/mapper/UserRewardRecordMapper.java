package com.abmatrix.bool.tg.dao.mapper;


import com.abmatrix.bool.tg.dao.entity.BoolUserRewardRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 用户奖励记录表
 Mapper 接口
 * </p>
 *
 * @author abm
 * @since 2024-07-15
 */
public interface UserRewardRecordMapper extends BaseMapper<BoolUserRewardRecord> {

    BigDecimal selectRewardAmount(@Param("userId") Long userId);
}
