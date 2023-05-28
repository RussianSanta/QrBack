package handlers;

import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageHandler {
    private static double calcScaleRate(BufferedImage image, BufferedImage logo) {
        double scaleRate = (double) logo.getWidth() / (double) image.getWidth();
        if (scaleRate > 0.3) {
            scaleRate = 0.3;
        } else {
            scaleRate = 1;
        }
        return scaleRate;
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

    public BufferedImage drawQr(BitMatrix bitMatrix, boolean drawLogo) {
        try {
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

            if (drawLogo) {
                // Add logo to QR code
                File logoFile = new File("src/main/resources/img.jpg");
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
            }

            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
