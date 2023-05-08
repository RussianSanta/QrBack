package handlers;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DataHandler {
    private static String fileExtension = "";

    public static void toMyFormat(File file) throws IOException {
        ArrayList<String> dataSets = prepareData(file);
        ArrayList<BufferedImage> images = convertDataToImages(dataSets);
        collectToVideo(images);
        System.out.println("Успешно зашифровано. Полученный файл можно найти в папке result");
    }

    public static void fromMyFormat(File file) throws JCodecException, IOException {
        ArrayList<BufferedImage> images = cutTheVideo(file);
        ArrayList<String> dataSets = convertImagesToData(images);
        collectToFile(dataSets);
        System.out.println("Успешно расшифровано. Полученный файл можно найти в папке resources");
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
        return appendTechnicalData(dataSets, file.getName());
    }

    private static ArrayList<BufferedImage> convertDataToImages(ArrayList<String> dataSets) {
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (String s : dataSets) {
            String fileName = "result/qr" + dataSets.indexOf(s) + ".png";
            images.add(QrHandler.createQrCode(s, 300, "png", fileName));
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

    private static ArrayList<String> convertImagesToData(ArrayList<BufferedImage> images) throws IOException {
        ArrayList<String> dataSets = new ArrayList<>();
        Set<Integer> collectedDataSets = new HashSet<>();
        int countOfDataSets = -1;
        int numberOfDataSet;

        for (BufferedImage image : images) {
            String decodeResult;
            try {
                decodeResult = QrHandler.decode(image).getText();
                if (decodeResult == null) continue;
                countOfDataSets = Integer.parseInt(decodeResult.substring(0, 6));
                fileExtension = decodeResult.substring(6, 12);
                fileExtension = fileExtension.replaceAll(" ", "");
                numberOfDataSet = Integer.parseInt(decodeResult.substring(12, 18));
                collectedDataSets.add(numberOfDataSet);
                dataSets.add(decodeResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (countOfDataSets != collectedDataSets.size()) {
            throw new IOException("Ошибка при получении данных. Количество полученных блоков не совпадает с ожидаемым");
        }
        return dataSets;
    }

    private static void collectToFile(ArrayList<String> dataSets) {
        StringBuilder builder = new StringBuilder();
        for (String s : dataSets) {
            s = s.substring(18);
            builder.append(s);
        }
        String resultFileName = "src/main/resources/result." + fileExtension;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultFileName))) {
            bw.write(builder.toString());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static ArrayList<String> appendTechnicalData(ArrayList<String> dataSets, String coreFileName) {
        ArrayList<String> newDataSets = new ArrayList<>();
        int countOfFrames = dataSets.size();
        int dotPosition = coreFileName.indexOf(".") + 1;
        String fileExtension = coreFileName.substring(dotPosition);
        StringBuilder technicalDataBuilder = new StringBuilder();
        technicalDataBuilder
                .append("0".repeat(Math.max(0, 6 - String.valueOf(countOfFrames).length())))
                .append(countOfFrames)
                .append(" ".repeat(Math.max(0, 6 - fileExtension.length())))
                .append(fileExtension);
        for (String s : dataSets) {
            String dataSetNumber = "0".repeat(Math.max(0, 6 - String.valueOf(dataSets.indexOf(s)).length())) + dataSets.indexOf(s);
            s = technicalDataBuilder + dataSetNumber + s;
            newDataSets.add(s);
        }
        return newDataSets;
    }
}
