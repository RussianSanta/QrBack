package telegramBotLogic;

import lombok.Data;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Data
@Singleton
public class BotSettings {

    public static final String FILE_NAME = "config.properties";
    private static BotSettings instance;
    private Properties properties;
    private String token;
    private String userName;
    private TelegramBotsApi telegramBotsApi;

    {
        try {
            properties = new Properties();
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME)) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new IOException(String.format("Error loading properties file '%s'", FILE_NAME));
            }
            token = properties.getProperty("token");
            if (token == null) {
                throw new RuntimeException("Token value is null");
            }
            userName = properties.getProperty("username");
            if (userName == null) {
                throw new RuntimeException("UserName value is null");
            }
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("Bot initialization error: " + e.getMessage());
        }
    }
}
