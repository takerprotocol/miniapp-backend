package com.abmatrix.bool.tg.service;

import cn.hutool.core.lang.Assert;
import com.abmatrix.bool.tg.common.enuma.AssignmentTypeEnum;
import com.abmatrix.bool.tg.dao.entity.BoolAssignment;
import com.abmatrix.bool.tg.dao.entity.BoolAssignmentUserRelation;
import com.abmatrix.bool.tg.dao.entity.BoolUserRewardRecord;
import com.abmatrix.bool.tg.dao.mapper.BoolAssignmentMapper;
import com.abmatrix.bool.tg.dao.mapper.BoolAssignmentUserRelationMapper;
import com.abmatrix.bool.tg.dao.service.IUserRewardRecordService;
import com.abmatrix.bool.tg.middleware.redis.clients.ListRedisClient;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.DsPropagation;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 任务接口事务适配器
 *
 * @author PeterWong
 * @date 2024年8月3日
 */
@Service
public class AssignmentServiceAdaptor {

    /**
     * 任务关联关系接口
     */
    @Autowired
    private BoolAssignmentUserRelationMapper boolAssignmentUserRelationMapper;
    /**
     * 用户得奖记录数据接口
     */
    @Autowired
    private IUserRewardRecordService rewardRecordService;

    /**
     * list redis客户端
     */
    @Autowired
    private ListRedisClient listRedisClient;

    @Resource
    private BoolAssignmentMapper boolAssignmentMapper;

    /**
     * 执行完成任务
     *
     * @param userId
     * @param assignmentId
     */
    @SneakyThrows
    @DSTransactional(rollbackFor = Exception.class, propagation = DsPropagation.REQUIRED)
    public void doAssignmentComplete(Long userId, Long assignmentId, BoolAssignmentUserRelation relation,
                                     BoolUserRewardRecord reward) {
        // 记录用户完成任务关系
        LambdaQueryWrapper<BoolAssignmentUserRelation> relationQuery = Wrappers.lambdaQuery();
        relationQuery.eq(BoolAssignmentUserRelation::getUserId, userId);
        relationQuery.eq(BoolAssignmentUserRelation::getAssignmentId, assignmentId);
        relationQuery.select(BoolAssignmentUserRelation::getId);
        relationQuery.orderByDesc(BoolAssignmentUserRelation::getId);
        BoolAssignmentUserRelation existRelation = boolAssignmentUserRelationMapper.selectOne(relationQuery,
                Boolean.FALSE);

        BoolAssignment assignment = boolAssignmentMapper.selectById(assignmentId);
        Assert.notNull(assignment, "The corresponding task does not exist.");
        AssignmentTypeEnum type = assignment.getAssignmentType();
        // 当前任务类型为 非每日任务
        if (existRelation != null) {
            // 更新每日任务，完成时间
            if (type == AssignmentTypeEnum.DAILY) {
                existRelation.setUpdateTime(LocalDateTime.now());
                boolAssignmentUserRelationMapper.updateById(existRelation);
            }
            return;
        }

        boolAssignmentUserRelationMapper.insert(relation);
        rewardRecordService.save(reward);
    }

}
