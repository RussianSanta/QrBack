package handlers;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

public class QrHandler {
    private static final File logoFile = new File("src/main/resources/img.jpg");

    public static BufferedImage createQrCode(String content, int qrCodeSize, String imageFormat, String fileName, String characterSet) {
        try (FileOutputStream resultStream = new FileOutputStream(fileName)) {
            // Correction level - HIGH - more chances to recover message
            Hashtable<EncodeHintType, Object> hintMap =
                    new Hashtable<>();
//            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            if (!characterSet.equals("")) {
                hintMap.put(EncodeHintType.CHARACTER_SET, characterSet);
            }

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
            graphics.setColor(Color.white);
            graphics.fillRect(0, 0, matrixWidth, matrixWidth);
//            Color mainColor = new Color(30, 70, 153);
            Color mainColor = new Color(1, 1, 1);
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

    public static Result decode(BufferedImage image) {
        if (image == null) {
            return null;
        }
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            return new MultiFormatReader().decode(bitmap);
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
