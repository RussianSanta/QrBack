package telegramBotLogic;

import com.google.zxing.WriterException;
import handlers.DataHandler;
import org.jcodec.api.JCodecException;
import org.json.JSONObject;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import telegramBotLogic.commands.CommandHelp;
import telegramBotLogic.commands.CommandStart;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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

    private File getFileFromServer(String fileId, String fileName) throws IOException {
        URL fileUrl = new URL(getFileUrl(fileId));
        File downloadedFile = new File("result/downloaded/" + fileName);

        FileOutputStream fos = new FileOutputStream(downloadedFile.getAbsolutePath());
        System.out.println("Start upload");
        ReadableByteChannel rbc = Channels.newChannel(fileUrl.openStream());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();

        return downloadedFile;
    }

    private void sendMessage(Long chatId, String message) {
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

    public void sendVideo(Long chatId, String path) {
        try {
            SendVideo video = new SendVideo();
            video.setVideo(new InputFile(new File(path)));
            video.setChatId(chatId.toString());
            execute(video);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(Long chatId, String path) {
        try {
            SendDocument document = new SendDocument();
            document.setDocument(new InputFile(new File(path)));
            document.setChatId(chatId.toString());
            execute(document);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processText(Update update) throws TelegramApiException, IOException, WriterException {
        String text = update.getMessage().getText();
        String resultPath = DataHandler.convertText(text);
        sendMessage(update.getMessage().getChatId(), "Текст");
        if (resultPath.contains(".mp4")) {
            sendVideo(update.getMessage().getChatId(), resultPath);
        } else if (resultPath.contains("jpg")) {
            sendImage(update.getMessage().getChatId(), resultPath);
        }
        DataHandler.clear(resultPath);
    }

    private void processResult(String result, Update update) {
        if (result.contains("_F_")) {
            String fileExtension = result.substring(3);
            File resultFile = new File("result/decoded/result." + fileExtension);
            sendFile(update.getMessage().getChatId(), resultFile.getAbsolutePath());
            resultFile.delete();
        } else {
            sendMessage(update.getMessage().getChatId(), result);
        }
    }

    private void processImage(Update update) throws TelegramApiException, IOException {
        sendMessage(update.getMessage().getChatId(), "Картинка");
        List<PhotoSize> photoSizes = update.getMessage().getPhoto();
        File file = getFileFromServer(photoSizes.get(0).getFileId(), "uploadedImage.jpg");

        String result = DataHandler.decodePhoto(file.getAbsolutePath());
        processResult(result, update);

        file.delete();
    }

    private void processVideo(Update update) throws TelegramApiException, IOException, JCodecException {
        sendMessage(update.getMessage().getChatId(), "Видео");
        Video video = update.getMessage().getVideo();
        File file = getFileFromServer(video.getFileId(), video.getFileName());

        String result = DataHandler.decodeVideo(file.getAbsolutePath());
        processResult(result, update);

        file.delete();
    }

    private void processFile(File file, Update update) throws FileNotFoundException {
        String resultPath = DataHandler.convertFile(file.getAbsolutePath());
        sendVideo(update.getMessage().getChatId(), resultPath);
        DataHandler.clear(resultPath);
    }

    private void processAudio(Update update) throws IOException, TelegramApiException {
        Audio audio = update.getMessage().getAudio();
        File file = getFileFromServer(audio.getFileId(), audio.getFileName());
        sendMessage(update.getMessage().getChatId(), "Аудио");
        processFile(file, update);
        file.delete();
    }

    private void processDocument(Update update) throws IOException, TelegramApiException {
        Document document = update.getMessage().getDocument();
        File file = getFileFromServer(document.getFileId(), document.getFileName());
        sendMessage(update.getMessage().getChatId(), "Файл");
        processFile(file, update);
        file.delete();
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
                if (update.getMessage().getAudio() != null) {
                    processAudio(update);
                } else if (update.getMessage().getDocument() != null) {
                    processDocument(update);
                } else if (update.getMessage().getPhoto() != null) {
                    processImage(update);
                } else if (update.getMessage().getVideo() != null) {
                    processVideo(update);
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
}
