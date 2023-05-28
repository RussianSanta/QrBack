package telegramBotLogic.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class CommandStart extends Command {

    public CommandStart() {
        super("start", "Запуск бота");
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        message.setText("Привет!\n" +
                "Этот бот - клиент для взаимодействия с системой генерации и расшифровки динамических qr кодов\n" +
                "Здесь можно зашифровать и расшифровать текст, картинки, различные файлы."
        );
        super.processMessage(absSender, message, null);
    }

}
