import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class QrExample {
    public static void main(String[] args) {
        BufferedReader appInput = new BufferedReader(new InputStreamReader(System.in));
        String data = null;
        System.out.print("Please enter data to encode: ");
        while (data == null) {
            try {
                data = appInput.readLine().trim();
            } catch (IOException e1) {
                System.out.println(e1.getMessage());
            }
        }

        new QrProvider().createQrCode(data, 300, "png");
        System.out.println("done.");
    }
}

