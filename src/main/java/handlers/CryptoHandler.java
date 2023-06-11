package handlers;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Properties;

public class CryptoHandler {
    private static SecretKey secretKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
        Properties properties = new Properties();
        String propertiesFileName = "config.properties";
        try (InputStream inputStream = CryptoHandler.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            properties.load(inputStream);
            String keyWord = properties.getProperty("keyword");
            if (keyWord.equals("") || keyWord.equals("null")) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                SecureRandom random = new SecureRandom();
                int keyBitSize = 256;
                keyGenerator.init(keyBitSize, random);
                secretKey = keyGenerator.generateKey();
                byte[] keyBytes = secretKey.getEncoded();
                char[] symbols = new char[keyBytes.length];
                for (int i = 0; i < keyBytes.length; i++) {
                    symbols[i] = (char) keyBytes[i];
                }

                properties.setProperty("keyword", String.copyValueOf(symbols));
                OutputStream os = new FileOutputStream("src/main/resources/" + propertiesFileName);
                properties.store(os, "Добавлен ключ");
            } else {
                char[] symbols = keyWord.toCharArray();
                byte[] keyBytes = new byte[symbols.length];
                for (int i = 0; i < symbols.length; i++) {
                    keyBytes[i] = (byte) symbols[i];
                }
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("AES");
                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "RawBytes");
                secretKey = keyFactory.generateSecret(secretKeySpec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String encryptData(String data) {
        try {
            char[] dataSymbols = data.toCharArray();
            byte[] dataBytes = new byte[dataSymbols.length];
            for (int i = 0; i < dataBytes.length; i++) {
                dataBytes[i] = (byte) dataSymbols[i];
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[16]));
            byte[] cipherResult = cipher.doFinal(dataBytes);
            char[] symbols = new char[cipherResult.length];
            for (int i = 0; i < symbols.length; i++) {
                symbols[i] = (char) cipherResult[i];
            }
            return String.copyValueOf(symbols);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public String decryptData(String data) {
        try {
            char[] dataSymbols = data.toCharArray();
            byte[] dataBytes = new byte[dataSymbols.length];
            for (int i = 0; i < dataBytes.length; i++) {
                dataBytes[i] = (byte) dataSymbols[i];
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[16]));
            byte[] cipherResult = cipher.doFinal(dataBytes);
            char[] symbols = new char[cipherResult.length];
            for (int i = 0; i < symbols.length; i++) {
                symbols[i] = (char) cipherResult[i];
            }
            return String.copyValueOf(symbols);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
