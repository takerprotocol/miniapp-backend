package com.abmatrix.bool.tg.dao.service;

import com.abmatrix.bool.tg.dao.entity.BoolUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author abm
 * @since 2024-07-15
 */
public interface IUserService extends IService<BoolUser> {

    String getUserRank(Long userId);
}
