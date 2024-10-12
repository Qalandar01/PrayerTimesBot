package org.example;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.example.entity.TgUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.BotService.getOrCreate;
import static org.example.BotService.telegramBot;

public class BotController {
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    public static void start() {
        telegramBot.setUpdatesListener(updates->{
            for (Update update : updates) {
                executorService.execute(()->{
                    handleUpdate(update);
                });
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static void handleUpdate(Update update) {
        if (update.message() != null) {
            Message message = update.message();
            TgUser tgUser = BotService.getOrCreate(message.chat().id());
            if (message.text() != null) {
                String text = message.text();
                if (text.equals("/start")){
                    BotService.sendWelcomingMsg(tgUser);
                }else if (text.equals("Change region")){
                    BotService.sendWelcomingMsg(tgUser);
                }

            }
        } else if (update.callbackQuery() != null) {
            CallbackQuery callbackQuery = update.callbackQuery();
            String data = callbackQuery.data();
            TgUser tgUser = getOrCreate(callbackQuery.from().id());
            if (tgUser.getState().equals(State.CHOOSING_REGION)){
                BotService.acceptRegion(tgUser,data);
            }
        }
    }
}
