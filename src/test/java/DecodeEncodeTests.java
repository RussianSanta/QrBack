import handlers.DataHandler;
import org.jcodec.api.JCodecException;
import org.testng.annotations.Test;

import java.io.IOException;

public class DecodeEncodeTests {
    @Test
    public void testDecodeToAudio() throws JCodecException, IOException {
        new DataHandler().decodeVideo("src/main/resources/test-vid.mp4");
    }
}
