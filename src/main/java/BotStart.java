import telegramBotLogic.BotProcessor;

public class BotStart {
    public static void main(String[] args) {
        try {
            BotProcessor botProcessor = BotProcessor.getInstance();
            if (botProcessor != null) System.out.println("Бот успешно запущен");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
