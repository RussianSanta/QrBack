import java.io.File;
import java.io.IOException;

public class QrExample {
    public static void main(String[] args) throws IOException {
        DataHandler.toMyFormat(new File("src/main/resources/data.txt"));
    }
}

