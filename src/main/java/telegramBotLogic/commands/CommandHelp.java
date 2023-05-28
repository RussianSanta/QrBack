package telegramBotLogic.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class CommandHelp extends Command {

    public CommandHelp() {
        super("help", "Справка \\help \n");
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        message.setText("* Чтобы зашифровать текст, напишите боту прямо в чат.\n" +
                "* Если вы хотите зашифровать файл, приложите его через вкладку файлы.\n" +
                "* Все полученные фото и видео бот будет пытаться расшифровать, только если они не приложены как файлы.");
        super.processMessage(absSender, message, strings);
    }

}
