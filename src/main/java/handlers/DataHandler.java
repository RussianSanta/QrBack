package handlers;

import org.apache.commons.io.FileUtils;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DataHandler {
    private static final int BUFFER_SIZE = 512;
    private static final int QR_SIZE = 512;
    private static final ArrayList<String> textFileExtensions = new ArrayList<>();

    static {
        textFileExtensions.add("txt");
        textFileExtensions.add("properties");
        textFileExtensions.add("md");
        textFileExtensions.add("xml");
    }

    private char[] toChars(byte[] barr, int length) {
        char[] carr = new char[length];
        for (int i = 0; i < length; i++) {
            carr[i] = (char) barr[i];
        }
        return carr;
    }

    private byte[] toBytes(char[] carr) {
        byte[] barr = new byte[carr.length];
        for (int i = 0; i < carr.length; i++) {
            barr[i] = (byte) carr[i];
        }
        return barr;
    }

    private String getFileExtension(File file) {
        int dotPosition = file.getName().indexOf(".") + 1;
        return file.getName().substring(dotPosition);
    }

    private String makePath() {
        String path = "result/" + UUID.randomUUID();
        File folder = new File(path);
        if (!folder.exists()) folder.mkdir();
        return path;
    }

    public void clear(String path) {
        try {
            FileUtils.deleteDirectory(new File(path).getParentFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String encode(String data, String fileExtension, String characterSet) {
        String path = makePath();
        ArrayList<String> dataSets = prepareData(data, fileExtension);
        ArrayList<BufferedImage> images = convertDataToImages(dataSets, characterSet, path);
        if (images.size() > 1) {
            collectToVideo(images, path);
            System.out.println("Успешно зашифровано.");
            return path + "/result.mp4";
        } else {
            collectToPhoto(images, path);
            System.out.println("Успешно зашифровано.");
            return path + "/result.jpg";
        }
    }

    public String decodePhoto(String fileUrl) throws IOException {
        File file = new File(fileUrl);

        ArrayList<BufferedImage> images = new ArrayList<>();
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        images.add(image);
        System.out.println(images.size());
        ArrayList<String> dataSets = convertImagesToData(images);
        String result = collectToFile(dataSets);
        System.out.println("Успешно расшифровано.");
        return result;
    }

    public String decodeVideo(String fileUrl) throws JCodecException, IOException {
        File file = new File(fileUrl);

        ArrayList<BufferedImage> images = cutTheVideo(file);
        ArrayList<String> dataSets = convertImagesToData(images);
        String result = collectToFile(dataSets);
        System.out.println("Успешно расшифровано.");
        return result;
    }

    public String convertText(String text) throws IOException {
        return encode(text, "st", "windows-1251");
    }

    public String convertFile(String fileUrl) throws FileNotFoundException {
        boolean isText = false;
        for (String s : textFileExtensions) {
            if (fileUrl.contains(s)) {
                isText = true;
                break;
            }
        }
        if (isText) return convertTextFile(fileUrl);
        else return convertOtherFile(fileUrl);
    }

    private String convertTextFile(String fileUrl) {
        File file = new File(fileUrl);
        StringBuilder dataBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            int c;
            while ((c = br.read()) != -1) {
                dataBuilder.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encode(dataBuilder.toString(), getFileExtension(file), "windows-1251");
    }

    private String convertOtherFile(String fileUrl) throws FileNotFoundException {
        File file = new File(fileUrl);
        StringBuilder dataBuilder = new StringBuilder();

        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) > 0) {
                char[] chars = toChars(buffer, length);
                dataBuilder.append(String.valueOf(chars));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encode(dataBuilder.toString(), getFileExtension(file), "UTF-8");
    }

    private ArrayList<String> prepareData(String data, String fileExtension) {
        ArrayList<String> dataSets = new ArrayList<>();

        int blockSize = (int) Math.ceil(data.length() / Math.ceil(data.length() / (double) BUFFER_SIZE));
        int counter = 0;
        while (counter < data.length()) {
            char[] chars = new char[blockSize];
            int end = counter + blockSize;
            if (end > data.length()) end = data.length();
            data.getChars(counter, end, chars, 0);
            dataSets.add(String.copyValueOf(chars));
            counter += blockSize;
        }
        return appendTechnicalData(dataSets, fileExtension);

    }

    private ArrayList<BufferedImage> convertDataToImages(ArrayList<String> dataSets, String characterSet, String path) {
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (String s : dataSets) {
            images.add(QrHandler.createQrCode(s, QR_SIZE, characterSet));
        }
        return images;
    }

    private void collectToPhoto(ArrayList<BufferedImage> images, String path) {
        BufferedImage image = images.get(0);
        File resultFile = new File(path + "/result.jpg");
        try {
            ImageIO.write(image, "jpg", resultFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void collectToVideo(ArrayList<BufferedImage> images, String path) {
        try {
            AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(new File(path + "/result.mp4"), 10);
            for (BufferedImage image : images) {
                encoder.encodeImage(image);
            }
            encoder.finish();
        } catch (Exception e) {
            System.out.println("Fail to generate video!");
        }
    }

    private ArrayList<BufferedImage> cutTheVideo(File file) throws IOException, JCodecException {
        ArrayList<BufferedImage> images = new ArrayList<>();

        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
        Picture picture;
        while (null != (picture = grab.getNativeFrame())) {
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
            images.add(bufferedImage);
        }

        return images;
    }

    private ArrayList<String> convertImagesToData(ArrayList<BufferedImage> images) throws IOException {
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
                numberOfDataSet = Integer.parseInt(decodeResult.substring(12, 18));
                collectedDataSets.add(numberOfDataSet);
                dataSets.add(numberOfDataSet, decodeResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (countOfDataSets != collectedDataSets.size()) {
            System.out.println("Получено: " + collectedDataSets.size() + ". Ожидалось: " + countOfDataSets);
            throw new IOException("Ошибка при получении данных. Количество полученных блоков не совпадает с ожидаемым");
        }
        return dataSets;
    }

    private String collectToFile(ArrayList<String> dataSets) {
        StringBuilder builder = new StringBuilder();
        String fileExtension = "";

        for (String s : dataSets) {
            if (fileExtension.equals("")) {
                fileExtension = s.substring(6, 12);
                fileExtension = fileExtension.replaceAll(" ", "");
            }
            s = s.substring(18);
            builder.append(s);
        }
        String resultFileName = "result/decoded/result." + fileExtension;

        String resultText = builder.toString();
        if (textFileExtensions.contains(fileExtension)) {
            System.out.println("Расшифровка в текстовый файл");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultFileName))) {
                bw.write(resultText);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            return "_F_" + fileExtension;
        } else if (fileExtension.equals("st")) {
            System.out.println("Расшифровка в текст");
            return resultText;
        } else {
            System.out.println("Расшифровка в другой файл");
            try (OutputStream os = new FileOutputStream(resultFileName)) {
                int endPosition = BUFFER_SIZE;
                while (endPosition < resultText.length()) {
                    char[] chars = new char[BUFFER_SIZE];
                    resultText.getChars(endPosition - BUFFER_SIZE, endPosition, chars, 0);
                    byte[] buffer = toBytes(chars);
                    os.write(buffer, 0, buffer.length);
                    endPosition += BUFFER_SIZE;
                }
                char[] chars = new char[resultText.length() - (endPosition - BUFFER_SIZE)];
                resultText.getChars((endPosition - BUFFER_SIZE), resultText.length(), chars, 0);
                byte[] buffer = toBytes(chars);
                os.write(buffer, 0, buffer.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "_F_" + fileExtension;
        }
    }

    private ArrayList<String> appendTechnicalData(ArrayList<String> dataSets, String fileExtension) {
        ArrayList<String> newDataSets = new ArrayList<>();
        int countOfFrames = dataSets.size();
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
