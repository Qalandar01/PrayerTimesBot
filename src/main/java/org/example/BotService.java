package org.example;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.SneakyThrows;
import org.example.entity.TgUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BotService {
    public static LocalDate currentDate = LocalDate.now();
    public static TelegramBot telegramBot = new TelegramBot("7524788008:AAGsrZgb4SFIgFhG0vWmWHM78SvoiyT9NDQ");
    static String[] regions = {"Toshkent", "Andijon", "Namangan", "Fargona", "Sirdaryo", "Jizzax", "Navoiy", "Samarqand", "Qarshi", "Termiz", "Buxoro", "Urganch"};


    public static TgUser getOrCreate(Long id) {
        Optional<TgUser> first = DB.TG_USERS.stream().filter(tgUser -> tgUser.getChatId().equals(id)).findFirst();
        if (first.isPresent()) {
            return first.get();
        } else {
            TgUser user = new TgUser(id);
            DB.TG_USERS.add(user);
            return user;
        }
    }

    public static void sendWelcomingMsg(TgUser tgUser) {
        if (tgUser.getState().equals(State.START)) {

            SendMessage sendMessage = new SendMessage(tgUser.getChatId(), """
                    Assalomu alaykum Bizning Namoz vaqlaridan boxabar qiluvchi botimizga xush kelibsiz!
                    Botimiz sizga har kungi namoz vaqlari haqida xabar berib turadi
                    """);
            telegramBot.execute(sendMessage);
        }
        SendMessage sendMessage1 = new SendMessage(tgUser.getChatId(), "Iltimos Shaharingizni tanlang");
        sendMessage1.replyMarkup(generateRegionsBtns());
        SendResponse response = telegramBot.execute(sendMessage1);
        tgUser.setMessageId(response.message().messageId());
        tgUser.setState(State.CHOOSING_REGION);

    }

    private static InlineKeyboardMarkup generateRegionsBtns() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        for (int i = 0; i < regions.length; i++) {
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(regions[i]).callbackData("region/" + i));
        }
        return inlineKeyboardMarkup;
    }

    @SneakyThrows
    private static String getPrayerTimes(TgUser tgUser) {
        String prayerTimes = """
                Bomdod : %s
                Quyosh : %s
                Peshin : %s
                Asr : %s
                Shom : %s
                Xufton : %s
                """;
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        HtmlPage page = webClient.getPage("https://namozvaqti.uz/shahar/" + tgUser.getRegion().toLowerCase());
        List<HtmlElement> forms = page.getByXPath("//p");
        String[] arr = new String[forms.size() - 4];
        int in = 0;
        for (int i = 1; i < forms.size() - 2; i++) {
            if (i == 5) {
                continue;
            }
            arr[in++] = forms.get(i).asNormalizedText();
        }
        webClient.close();
        return prayerTimes.formatted(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
    }

    public static void acceptRegion(TgUser tgUser, String data) {
        int regionIndex = Integer.parseInt(data.split("/")[1]);
        tgUser.setRegion(regions[regionIndex]);
        telegramBot.execute(new DeleteMessage(tgUser.getChatId(), tgUser.getMessageId()));
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), "Viloyat qabul qilindi! Keyinchalik pastdagi 'Change region' ni bosish orqali o'zgartirishingiz mumkin.");
        sendMessage.replyMarkup(new ReplyKeyboardMarkup("Change region").resizeKeyboard(true));
        telegramBot.execute(sendMessage);
        String getPrayerTimes = getPrayerTimes(tgUser);
        SendMessage sendMessage1 = new SendMessage(tgUser.getChatId(), """
                Namoz vaqtlari : %s
                %s
                """.formatted(tgUser.getRegion(), getPrayerTimes));
        telegramBot.execute(sendMessage1);

    }

    public static void sendAllUsersPrayerTimes() {

        for (TgUser tgUser : DB.TG_USERS) {
            SendMessage sendMessage = new SendMessage(tgUser.getChatId(), """
                    Bugungi namoz vaqtlari: %s
                    %s
                    """.formatted(tgUser.getRegion(), getPrayerTimes(tgUser)));
            telegramBot.execute(sendMessage);

        }
    }
}
