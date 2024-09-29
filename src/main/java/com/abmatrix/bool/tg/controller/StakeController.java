package com.abmatrix.bool.tg.controller;

import com.abmatrix.bool.tg.common.model.vo.ResultVo;
import com.abmatrix.bool.tg.model.req.StakeReq;
import com.abmatrix.bool.tg.service.StakeService;
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
@RequestMapping("/stake")
public class StakeController {
	/**
	 * 执行质押
	 */
	@Autowired
	private StakeService stakeService;

	/**
	 * 执行stake
	 * 
	 * @param req
	 * @return
	 */
	@PostMapping("/do")
	public Mono<ResultVo<String>> doStake(@RequestBody StakeReq req) {
		return Mono.fromCallable(() -> {
			String raw = stakeService.stake(req);
			return ResultVo.success(raw);
		}).subscribeOn(Schedulers.boundedElastic());
	}
}
