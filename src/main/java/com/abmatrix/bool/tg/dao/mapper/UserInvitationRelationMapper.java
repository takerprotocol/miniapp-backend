package com.abmatrix.bool.tg.dao.mapper;

import com.abmatrix.bool.tg.common.model.vo.InvitationRankVo;
import com.abmatrix.bool.tg.dao.entity.BoolUserInvitationRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author abm
 * @since 2024-07-15
 */
public interface UserInvitationRelationMapper extends BaseMapper<BoolUserInvitationRelation> {

	List<InvitationRankVo> queryRankByInviters(@Param("startTime") LocalDate startTime,
			@Param("endTime") LocalDate endTime, @Param("orderType") String type);

	/**
	 * 查询用户的二级邀请信息
	 * 
	 * @param userId
	 * @return
	 */
	List<BoolUserInvitationRelation> queryLevel2InvitationRelations(@Param("userId") Long userId,
                                                                    @Param("offset") Integer offset, @Param("limit") Integer limit);

	/**
	 * 查询用户二级邀请信息数量
	 * 
	 * @param userId
	 * @return
	 */
	Integer queryLevel2InvitationRelationsCount(@Param("userId") Long userId);
	/**
	 * 查询本周合格邀请关系
	 *
	 * @param thisMondayAtEight
	 * @return
	 */
	List<BoolUserInvitationRelation> currentWeekRelations(@Param("thisMondayAtEight") LocalDateTime thisMondayAtEight);

	/**
	 * 查询某一周内的邀请记录
	 *
	 * @param thisMondayAtEight
	 * @param nextMondayAtEight
	 * @return
	 */
	List<BoolUserInvitationRelation> weekRelationsByRange(@Param("thisMondayAtEight") LocalDateTime thisMondayAtEight,
                                                          @Param("nextMondayAtEight") LocalDateTime nextMondayAtEight);
}
