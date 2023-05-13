package telegramBotLogic;

import com.google.zxing.WriterException;
import org.json.JSONObject;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import telegramBotLogic.commands.CommandHelp;
import telegramBotLogic.commands.CommandStart;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class BotProcessor extends TelegramLongPollingCommandBot {
    private final static BotSettings botSettings = new BotSettings();
    private static BotProcessor instance;
    private final TelegramBotsApi telegramBotsApi;
    private List<String> registeredCommands = new ArrayList<>();

    {
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            registerBot();
            registerCommands();
        } catch (TelegramApiException e) {
            throw new RuntimeException("Telegram Bot initialization error: " + e.getMessage());
        }
    }

    public BotProcessor() {
        super();
    }

    public static BotProcessor getInstance() {
        if (instance == null)
            instance = new BotProcessor();
        return instance;
    }

    public void sendMessage(Long chatId, String message) {
        try {
            SendMessage sendMessage = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(message)
                    .build();
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendImage(Long chatId, String path) {
        try {
            SendPhoto photo = new SendPhoto();
            photo.setPhoto(new InputFile(new File(path)));
            photo.setChatId(chatId.toString());
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botSettings.getUserName();
    }

    @Override
    protected void processInvalidCommandUpdate(Update update) {
        String command = update.getMessage().getText().substring(1);
        sendMessage(
                update.getMessage().getChatId()
                , String.format("Некорректная команда [%s], доступные команды: %s"
                        , command
                        , registeredCommands.toString()));
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            try {
                if (update.getMessage().getPhoto() != null) {
                    processImage(update);
                } else if (update.getMessage().getText() != null) {
                    if (update.getMessage().getText().matches("^/[\\w]*$")) {
                        processInvalidCommandUpdate(update);
                    } else {
                        processText(update);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotToken() {
        return botSettings.getToken();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    private void processText(Update update) throws TelegramApiException, IOException, WriterException {

    }

    private void processImage(Update update) throws TelegramApiException, IOException {

    }

    private JSONObject getFileRequest(String fileId) throws IOException {
        String fileUrl = String.format("https://api.telegram.org/bot%s/getFile?file_id=%s",
                botSettings.getToken(),
                fileId);
        return IOTools.readJsonFromUrl(fileUrl);
    }

    private String getFileUrl(String fileId) throws IOException {
        JSONObject jsonObject = getFileRequest(fileId);
        return String.format("https://api.telegram.org/file/bot%s/%s",
                botSettings.getToken(),
                jsonObject.get("file_path"));
    }

    private void setRegisteredCommands() {
        registeredCommands = getRegisteredCommands()
                .stream()
                .map(IBotCommand::getCommandIdentifier)
                .collect(Collectors.toList());
    }

    private void registerCommands() {
        register(new CommandStart());
        register(new CommandHelp());
        setRegisteredCommands();
    }

    public void registerBot() {
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Telegram API initialization error: " + e.getMessage());
        }
    }
}
