package com.abmatrix.bool.tg.controller;

import com.abmatrix.bool.tg.common.model.vo.*;
import com.abmatrix.bool.tg.model.req.UserInfoReq;
import com.abmatrix.bool.tg.model.req.UserInvitersRankReq;
import com.abmatrix.bool.tg.model.req.UserRegisterReq;
import com.abmatrix.bool.tg.service.InviteService;
import com.abmatrix.bool.tg.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author abm
 */
@RestController
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private InviteService inviteService;

    @PostMapping("/user/register")
    public Mono<ResultVo<String>> registerUser(@RequestBody @Validated UserRegisterReq req) {

        return Mono.fromCallable(
                () -> ResultVo.success(userService.register(req))
        ).subscribeOn(Schedulers.boundedElastic());

    }


    /**
     * 查询用户信息
     *
     * @param req req
     * @return
     */
    @PostMapping("/user/user/strict")
    public Mono<ResultVo<UserVo>> getUserInfoForStrict(@RequestBody UserInfoReq req) {

        return Mono.fromCallable(
                () -> ResultVo.success(userService.getUserInfoForStrict(req))
        ).subscribeOn(Schedulers.boundedElastic());

    }


    /**
     * 查询用户 join group channel
     * @param req
     * @return
     */
    @PostMapping("/user/chat/member")
    public Mono<ResultVo<ChatMember>> getChatMember(@RequestBody UserInfoReq req) {
        return Mono.fromCallable(
                () -> ResultVo.success(userService.getChatMember(req))
        ).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 查询邀请记录
     *
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/user/inviter-records")
    public Mono<ResultVo<Page<UserInvitationRelationVo>>> inviterRecords(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "level", defaultValue = "1") Integer level) {
        return Mono.fromCallable(() -> {
            Page<UserInvitationRelationVo> page = inviteService.pageInviteRecords(userId, pageNo, pageSize, level);
            return ResultVo.success(page);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询排行榜
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/user/reward:rank")
    public Mono<ResultVo<Page<UserRankVo>>> rank(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "100") Integer pageSize) {

        return Mono.fromCallable(() -> {
            Integer pageNoN = 1;
            Integer pageSizeN = 500;
            Page<UserRankVo> page = inviteService.rank(pageNoN, pageSizeN);
            return ResultVo.success(page);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询排行榜
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/user/inviter:rank")
    public Mono<ResultVo<Page<InvitationRankVo>>> inviterRank(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "100") Integer pageSize, UserInvitersRankReq req) {

        return Mono.fromCallable(() -> {
            Page<InvitationRankVo> page = inviteService.inviterRank(pageNo, pageSize, req);
            return ResultVo.success(page);
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * 查询邀请人总榜
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/user/inviter/rank/full")
    public Mono<ResultVo<Page<InvitationRankVo>>> inviterFullRank(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "500") Integer pageSize) {
        return Mono.fromCallable(() -> {
            Page<InvitationRankVo> page = inviteService.inviterRankFull(pageNo, pageSize);
            return ResultVo.success(page);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询邀请人周榜
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/user/inviter/rank/week")
    public Mono<ResultVo<Page<InvitationRankVo>>> inviterWeekRank(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "500") Integer pageSize) {
        return Mono.fromCallable(() -> {
            Page<InvitationRankVo> page = inviteService.inviterRankWeek(pageNo, pageSize);
            return ResultVo.success(page);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
