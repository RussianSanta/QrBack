package telegramBotLogic.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class CommandStart extends Command {

    public CommandStart() {
        super("start", "Запуск бота");
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        message.setText("Добро пожаловать! \n"
                + "Вас приветствует бот @QRVisorBot, у меня простые функции: чтение и генерация QR-кодов. \n"
                + "Начнём?"
        );
        super.processMessage(absSender, message, null);
    }

}
