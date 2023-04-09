import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class QrExample {
    public static void main(String[] args) throws IOException {
        ArrayList<BufferedImage> images = new ArrayList<>();
        ArrayList<String> dataSets = new ArrayList<>();
        StringBuilder dataBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/data.txt"))) {
            int c;
            while ((c = br.read()) != -1) {
                dataBuilder.append((char) c);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        int blockSize = (int) Math.ceil(dataBuilder.length() / Math.ceil(dataBuilder.length() / 700.0));
        int counter = 0;
        while (counter < dataBuilder.length()) {
            char[] chars = new char[blockSize];
            int end = counter + blockSize;
            if (end > dataBuilder.length()) end = dataBuilder.length();
            dataBuilder.getChars(counter, end, chars, 0);
            dataSets.add(String.copyValueOf(chars));
            counter += blockSize;
        }

        for (String s : dataSets) {
            String fileName = "result/qr" + dataSets.indexOf(s) + ".png";
            images.add(QrProvider.createQrCode(s, 300, "png", fileName));
        }

        AWTSequenceEncoder encoder = null;
        try {
            encoder = AWTSequenceEncoder.createSequenceEncoder(new File("result/move.mp4"), 10);
            for (BufferedImage image : images) {
                encoder.encodeImage(image);
            }
        } catch (Exception e) {
            System.out.println("Fail to generate video!");

        }
        encoder.finish();
    }
}

