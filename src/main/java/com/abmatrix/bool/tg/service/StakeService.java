package com.abmatrix.bool.tg.service;

import com.abmatrix.bool.tg.model.req.StakeReq;

/**
 * 质押接口
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
public interface StakeService {
    /**
     * 质押
     *
     * @param req
     * @return
     */
    String stake(StakeReq req);
}