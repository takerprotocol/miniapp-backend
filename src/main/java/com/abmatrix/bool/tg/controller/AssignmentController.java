package com.abmatrix.bool.tg.controller;

import com.abmatrix.bool.tg.common.model.vo.ResultVo;
import com.abmatrix.bool.tg.model.req.AssignmentReq;
import com.abmatrix.bool.tg.model.resp.AssignmentResp;
import com.abmatrix.bool.tg.service.AssignmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 小任务接口
 *
 * @author PeterWong
 * @date 2024年8月3日
 */
@RestController
@RequestMapping("/assignment")
public class AssignmentController {

    @Resource
    private AssignmentService assignmentService;

    /**
     * 执行任务
     *
     * @param req req
     * @return
     */
    @PostMapping("/do")
    public Mono<ResultVo<Boolean>> doAssignment(@RequestBody AssignmentReq req) {
        return Mono.fromCallable(
                () -> assignmentService.completeAssignment(req)
        ).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 任务列表
     *
     * @param req req
     * @return
     */
    @PostMapping("/list")
    public Mono<ResultVo<List<AssignmentResp>>> assignments(@RequestBody AssignmentReq req) {
        return Mono.fromCallable(() -> {
            List<AssignmentResp> list = assignmentService.queryAssignments(req);
            return ResultVo.success(list);
        }).subscribeOn(Schedulers.boundedElastic());

    }

}
