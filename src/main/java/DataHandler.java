import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataHandler {
    public static void toMyFormat(File file) throws IOException {
        ArrayList<String> dataSets = prepareData(file);
        ArrayList<BufferedImage> images = convertToImages(dataSets);
        collectToVideo  (images);
    }

    private static ArrayList<String> prepareData(File file) {
        ArrayList<String> dataSets = new ArrayList<>();
        StringBuilder dataBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
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
        return dataSets;
    }

    private static ArrayList<BufferedImage> convertToImages(ArrayList<String> dataSets) {
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (String s : dataSets) {
            String fileName = "result/qr" + dataSets.indexOf(s) + ".png";
            images.add(QrProvider.createQrCode(s, 300, "png", fileName));
        }
        return images;
    }

    private static void collectToVideo(ArrayList<BufferedImage> images) throws IOException {
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
