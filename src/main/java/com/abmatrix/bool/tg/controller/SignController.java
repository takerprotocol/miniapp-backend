package com.abmatrix.bool.tg.controller;

import com.abmatrix.bool.tg.common.model.vo.ResultVo;
import com.abmatrix.bool.tg.model.req.SignReq;
import com.abmatrix.bool.tg.service.SignServiceAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 质押借款
 *
 * @author PeterWong
 * @date 2024年7月30日
 */
@RestController
@RequestMapping("/sign")
public class SignController {
    /**
     * sign adaptor
     */
    @Autowired
    private SignServiceAdaptor signServiceAdaptor;

    /**
     * sign tx
     *
     * @param req
     * @return
     */
    @PostMapping("/tx")
    public Mono<ResultVo<String>> signTx(@RequestBody SignReq req) {
        return Mono.fromCallable(() -> {
            String raw = signServiceAdaptor.signTx(req);
            return ResultVo.success(raw);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}