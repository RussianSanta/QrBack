import handlers.DataHandler;
import org.jcodec.api.JCodecException;

import java.io.IOException;

public class Local {
    public static void main(String[] args) throws IOException, JCodecException {
//        handlers.DataHandler.convertTextFile("src/main/resources/data.txt");
//        handlers.DataHandler.convertOtherFile("src/main/resources/img.jpg");
        DataHandler.decode("result/move.mp4");
    }
}

