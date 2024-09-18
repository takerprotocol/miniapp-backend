package com.abmatrix.bool.tg.dao.mapper;

import com.abmatrix.bool.tg.dao.entity.BoolUser;
import com.abmatrix.bool.tg.dao.entity.BoolUserRewardRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author abm
 * @since 2024-07-15
 */
public interface UserMapper extends BaseMapper<BoolUser> {

	Long queryUserRank(@Param("userId") Long userId);


    /**
     * 全量批量修改用户奖励
     *
     * @param list
     */
    void batchUpdateUserRewardForFull(@Param("list") List<BoolUserRewardRecord> list);

    /**
     * 增量批量修改用户奖励
     *
     * @param list
     */
    void batchUpdateUserRewardForIncrease(@Param("list") List<BoolUserRewardRecord> list);

    List<Long> querUserList(@Param("userId") String userId);
}
