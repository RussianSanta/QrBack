package handlers;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.util.Hashtable;

public class QrHandler {
    public static BufferedImage createQrCode(String content, int qrCodeSize, String characterSet) {
        try {
            Hashtable<EncodeHintType, Object> hintMap =
                    new Hashtable<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            if (!characterSet.equals("")) {
                hintMap.put(EncodeHintType.CHARACTER_SET, characterSet);
            }

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content,
                    BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);

            return new ImageHandler().drawQr(bitMatrix, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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
}
