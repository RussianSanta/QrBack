import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;

public class QrProvider {
    private static final File logoFile = new File("src/main/resources/img.jpg");
    private static FileOutputStream resultStream;

    public static BufferedImage createQrCode(String content, int qrCodeSize, String imageFormat, String fileName) {
        try {
            resultStream = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // Correction level - HIGH - more chances to recover message
            Hashtable<EncodeHintType, Object> hintMap =
                    new Hashtable<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hintMap.put(EncodeHintType.CHARACTER_SET, "windows-1251");

            // Generate QR-code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content,
                    BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);

            // Start work with picture
            int matrixWidth = bitMatrix.getWidth();
            BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,
                    BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
            graphics.setFont(font);
            graphics.setColor(Color.white);
            graphics.fillRect(0, 0, matrixWidth, matrixWidth);
            Color mainColor = new Color(30, 70, 153);
            graphics.setColor(mainColor);
            // Write message under the QR-code

            //Write Bit Matrix as image
            for (int i = 0; i < matrixWidth; i++) {
                for (int j = 0; j < matrixWidth; j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            // Add logo to QR code
            BufferedImage logo = ImageIO.read(logoFile);

            //scale logo image and insert it to center of QR-code
            double scale = calcScaleRate(image, logo);
            logo = getScaledImage(logo,
                    (int) (logo.getWidth() * scale),
                    (int) (logo.getHeight() * scale));
            graphics.drawImage(logo,
                    image.getWidth() / 2 - logo.getWidth() / 2,
                    image.getHeight() / 2 - logo.getHeight() / 2,
                    image.getWidth() / 2 + logo.getWidth() / 2,
                    image.getHeight() / 2 + logo.getHeight() / 2,
                    0, 0, logo.getWidth(), logo.getHeight(), null);

            ImageIO.write(image, imageFormat, resultStream);
            //todo проверить, почему не работает проверка

            // Check correctness of QR-code
//            if (isQRCodeCorrect(content, image)) {
//                ImageIO.write(image, imageFormat, resultStream);
//                System.out.println("Your QR-code was succesfully generated.");
//            } else {
//                System.out.println("Sorry, your logo has broke QR-code. ");
//            }
            return image;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    private static double calcScaleRate(BufferedImage image, BufferedImage logo) {
        double scaleRate = (double) logo.getWidth() / (double) image.getWidth();
        if (scaleRate > 0.3) {
            scaleRate = 0.3;
        } else {
            scaleRate = 1;
        }
        return scaleRate;
    }

    private static boolean isQRCodeCorrect(String content, BufferedImage image) {
        boolean result = false;
        Result qrResult = decode(image);
        if (qrResult != null && content != null && content.equals(qrResult.getText())) {
            result = true;
        }
        return result;
    }

    public static Result decode(BufferedImage image) {
        if (image == null) {
            return null;
        }
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap, Collections.EMPTY_MAP);
            return result;
        } catch (NotFoundException nfe) {
            nfe.printStackTrace();
            return null;
        }
    }

    private static BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double scaleX = (double) width / imageWidth;
        double scaleY = (double) height / imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp bilinearScaleOp = new AffineTransformOp(
                scaleTransform, AffineTransformOp.TYPE_BILINEAR);

        return bilinearScaleOp.filter(
                image,
                new BufferedImage(width, height, image.getType()));
    }
}
