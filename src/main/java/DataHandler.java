import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class DataHandler {
    public static void toMyFormat(File file) throws IOException {
        ArrayList<String> dataSets = prepareData(file);
        ArrayList<BufferedImage> images = convertDataToImages(dataSets);
        collectToVideo(images);
    }

    public static void fromMyFormat(File file) throws JCodecException, IOException {
        ArrayList<BufferedImage> images = cutTheVideo(file);
        ArrayList<String> dataSets = convertImagesToData(images);
        collectToFile(dataSets);
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

    private static ArrayList<BufferedImage> convertDataToImages(ArrayList<String> dataSets) {
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

    private static ArrayList<BufferedImage> cutTheVideo(File file) throws IOException, JCodecException {
        ArrayList<BufferedImage> images = new ArrayList<>();

        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        Picture picture;
        while (null != (picture = grab.getNativeFrame())) {
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
            images.add(bufferedImage);
        }

        return images;
    }

    private static ArrayList<String> convertImagesToData(ArrayList<BufferedImage> images) {
        ArrayList<String> dataSets = new ArrayList<>();

        for (BufferedImage image : images) {
            String decodeResult = QrProvider.decode(image).getText();
            dataSets.add(decodeResult);
        }

        return dataSets;
    }

    private static void collectToFile(ArrayList<String> dataSets) {
        StringBuilder builder = new StringBuilder();
        for (String s : dataSets) {
            builder.append(s);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/resources/result.txt"))) {
            bw.write(builder.toString());
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
    }
}
