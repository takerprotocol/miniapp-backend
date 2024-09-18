package com.abmatrix.bool.tg.dao.service;

import com.abmatrix.bool.tg.common.enuma.InviteModelType;
import com.abmatrix.bool.tg.common.model.vo.InvitationRankVo;
import com.abmatrix.bool.tg.dao.entity.BoolUserInvitationRelation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author abm
 * @since 2024-07-15
 */
public interface IUserInvitationRelationService extends IService<BoolUserInvitationRelation> {

    List<InvitationRankVo> queryRankByInviters(LocalDate startTime, LocalDate endTime, InviteModelType type);
}
