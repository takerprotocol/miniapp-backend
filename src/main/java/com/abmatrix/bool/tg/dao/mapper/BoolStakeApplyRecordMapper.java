package com.abmatrix.bool.tg.dao.mapper;

import com.abmatrix.bool.tg.dao.entity.BoolStakeApplyRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 质押申请记录，只要来这里拼过交易都会记录一笔，无论最终是否提交上链 Mapper 接口
 * </p>
 *
 * @author PeterWongLovQpp
 * @since 2024-08-01
 */
public interface BoolStakeApplyRecordMapper extends BaseMapper<BoolStakeApplyRecord> {

}