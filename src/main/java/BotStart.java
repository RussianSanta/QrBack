import telegramBotLogic.BotProcessor;

public class BotStart {
    public static void main(String[] args) {
        try {
            BotProcessor botProcessor = BotProcessor.getInstance();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
