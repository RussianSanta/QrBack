import com.google.zxing.Result;
import handlers.QrHandler;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Test
public class MainTests {
    public void test() throws IOException {
        File image = new File("result/test2.jpg");
//        File image = new File("result/qr0.png");
        BufferedImage in = ImageIO.read(image);

        BufferedImage newImage = new BufferedImage(
                in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(in, 0, 0, null);
        g.dispose();

        Result r = QrHandler.decode(newImage);
        System.out.println(r.getText());
    }
}
