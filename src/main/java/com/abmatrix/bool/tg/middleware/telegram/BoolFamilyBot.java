package com.abmatrix.bool.tg.middleware.telegram;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.boosts.GetUserChatBoosts;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.boost.UserChatBoosts;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.abmatrix.bool.tg.common.constants.BotConstants.BOT_START_COMMAND;
import static com.abmatrix.bool.tg.common.constants.BotConstants.BOT_START_TEXT;


/**
 * @author abm
 */
@Slf4j
@Component("familyBot")
public class BoolFamilyBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final String LOCK_CHAT_PREFIX = "LOCK_BOT_CHAT:%s:%s";
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);


    @Value("${bool.family.bot-token}")
    private String token;
    @Value("${bool.family.mini-app-url}")
    private String webAppUrl;

    public TelegramClient telegramClient;

    @Getter
    private String botToken;

    /**
     * redisson客户端
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        telegramClient = new OkHttpTelegramClient(token);
        botToken = token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {

        executorService.submit(() -> {

            if (null == update.getMessage()) {
                return;
            }

            // 此 bot 没有 call data query, 所以写死此方法获取
            Message message = update.getMessage();

            String lockKey = String.format(LOCK_CHAT_PREFIX, message.getChatId(), message.getMessageId());
            RLock lock = redissonClient.getFairLock(lockKey);

            try {

                // 防止网络波动多次调用
                if (!lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                    log.warn("未抢占到分布式锁 bot");
                    return;
                }

                if (message.isUserMessage()) {

                    if (message.isCommand()) {
                        String command = message.getText();
                        if (command.equals(BOT_START_COMMAND)) {

                            SendMessage sendMsg =
                                    SendMessage.builder()
                                            .chatId(message.getChatId())
                                            .text(BOT_START_TEXT)
                                            .replyMarkup(ReplyMarkupFactory.createStartMarkup(webAppUrl))
                                            .parseMode(ParseMode.HTML)
                                            .build();

                            telegramClient.execute(sendMsg);
                        }
                    }

                }
            } catch (Exception e) {
                log.info("Bot send msg error by [{}]", e.getMessage());
            } finally {
                if (lock.isLocked()) {
                    lock.unlock();
                }
            }

        });
    }

    /**
     * 查询 用户是否 join group channel
     *
     * @param chatId   group channel chatId
     * @param userTgId user tg id
     * @return ChatMember
     */
    public ChatMember getChatMember(String chatId, Long userTgId) {

        ChatMember chatMember = null;
        try {

            GetChatMember build = GetChatMember.builder()
                    .chatId(chatId)
                    .userId(userTgId)
                    .build();
            chatMember = telegramClient.execute(build);
            log.info("Query join success [{}]", userTgId);

        } catch (TelegramApiException e) {
            log.info("Query user is join community error,[{}]-[{}]", userTgId, e.getMessage());
        }

        return chatMember;
    }

    /**
     * 查询用户是否助力频道
     *
     * @param chatId   channel chat id
     * @param userTgId user tg id
     * @return UserChatBoosts
     */
    public UserChatBoosts getUserChatBoosts(String chatId, Long userTgId) {

        UserChatBoosts userChatBoosts = null;
        try {

            GetUserChatBoosts boosts = GetUserChatBoosts.builder()
                    .chatId(chatId)
                    .userId(userTgId)
                    .build();
            userChatBoosts = telegramClient.execute(boosts);
            log.info("Query user boosts success [{}]", userTgId);

        } catch (TelegramApiException e) {
            log.info("Query user boosts error,[{}]-[{}]", userTgId, e.getMessage());
        }

        return userChatBoosts;
    }

}