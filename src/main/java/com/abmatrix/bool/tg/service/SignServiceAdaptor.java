package com.abmatrix.bool.tg.service;

import com.abmatrix.bool.tg.model.req.SignReq;

/**
 * 质押接口
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
public interface SignServiceAdaptor {
    /**
     * 质押
     *
     * @param req
     * @return
     */
    String signTx(SignReq req);
}