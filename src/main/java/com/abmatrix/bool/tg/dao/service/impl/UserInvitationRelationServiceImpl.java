package com.abmatrix.bool.tg.dao.service.impl;

import com.abmatrix.bool.tg.common.enuma.InviteModelType;
import com.abmatrix.bool.tg.common.model.vo.InvitationRankVo;
import com.abmatrix.bool.tg.dao.entity.BoolUserInvitationRelation;
import com.abmatrix.bool.tg.dao.mapper.UserInvitationRelationMapper;
import com.abmatrix.bool.tg.dao.service.IUserInvitationRelationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author abm
 * @since 2024-07-15
 */
@Service
public class UserInvitationRelationServiceImpl extends ServiceImpl<UserInvitationRelationMapper, BoolUserInvitationRelation> implements IUserInvitationRelationService {

    @Autowired
    private UserInvitationRelationMapper mapper;

    @Override
    public List<InvitationRankVo> queryRankByInviters(LocalDate startTime, LocalDate endTime, InviteModelType type) {

        return mapper.queryRankByInviters(startTime,endTime, type.name());
    }
}
