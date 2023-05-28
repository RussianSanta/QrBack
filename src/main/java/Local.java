import handlers.DataHandler;
import org.jcodec.api.JCodecException;

import java.io.IOException;

public class Local {
    public static void main(String[] args) throws IOException, JCodecException {
//        new DataHandler().convertTextFile("src/main/resources/data.txt");
//        new DataHandler().convertOtherFile("src/main/resources/img.jpg");
        new DataHandler().decodeVideo("result/move.mp4");
    }
}

