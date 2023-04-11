import org.jcodec.api.JCodecException;

import java.io.File;
import java.io.IOException;

public class QrExample {
    public static void main(String[] args) throws IOException, JCodecException {
//        DataHandler.toMyFormat(new File("src/main/resources/data.txt"));
        DataHandler.fromMyFormat(new File("result/move.mp4"));
    }
}

