package com.abmatrix.bool.tg.middleware.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;


/**
 * @author abm
 */
public class ReplyMarkupFactory {

    public static InlineKeyboardMarkup createStartMarkup(String webAppUrl) {

        InlineKeyboardButton twitterButton =
                InlineKeyboardButton.builder()
                        .text("Follow Twitter")
                        .url("")
                        .build();

        InlineKeyboardButton discoButton =
                InlineKeyboardButton.builder()
                        .text("Join Discord")
                        .url("")
                        .build();

        InlineKeyboardButton channelButton =
                InlineKeyboardButton.builder()
                        .text("Subscribe Channel")
                        .url("")
                        .build();

        InlineKeyboardButton mediumButton =
                InlineKeyboardButton.builder()
                        .text("Follow Medium")
                        .url("")
                        .build();
        InlineKeyboardButton youtubeButton =
                InlineKeyboardButton.builder()
                        .text("Subscribe YouTube")
                        .url("")
                        .build();

        List<InlineKeyboardRow> rowList = List.of(
                new InlineKeyboardRow(twitterButton),
                new InlineKeyboardRow(discoButton, channelButton),
                new InlineKeyboardRow(mediumButton, youtubeButton)
        );

        return InlineKeyboardMarkup.builder().keyboard(rowList).build();
    }

}
