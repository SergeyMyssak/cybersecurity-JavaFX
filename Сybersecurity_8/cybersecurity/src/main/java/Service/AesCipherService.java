package Service;

import ensemble.AlertHelper;
import javafx.scene.control.Alert;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AesCipherService {
    private static AesCipher aesCipher = new AesCipher();
    private static AlertHelper alertHelper = new AlertHelper();

    public static String initAesCipher(String url) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(aesCipher.getInitVector());
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesCipher.getSecretKey().getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            String encryptedText = readFile(url);

            return decrypt(cipher, encryptedText);
        } catch (IOException e) {
            alertHelper.configureAlert(
                    Alert.AlertType.WARNING,
                    "Предупреждение",
                    "Выбранный элемент не является html-файлом",
                    "Причины: \n" +
                            "1) Выбранный элемент - папка \n" +
                            "2) Название html-файла является некорректным"
            );

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            alertHelper.configureAlert(
                    Alert.AlertType.WARNING,
                    "Предупреждение",
                    "Невозможно открыть файл",
                    "Причина: неправильно закодирован файл");
        }

        return null;
    }

    private static String decrypt(Cipher cipher, String encryptedText) throws BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        return new String(cipher.doFinal(Base64.decodeBase64(encryptedText)), "UTF-8");
    }

    private static String readFile(String url) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(url), "UTF-8"));

        String s;
        StringBuilder sb = new StringBuilder("");
        while ((s = bufferedReader.readLine()) != null) {
            sb.append(s);
        }

        bufferedReader.close();

        return sb.toString();
    }

    public AesCipher getAesCipher() {
        return aesCipher;
    }

    public void setAesCipher(AesCipher aesCipher) {
        AesCipherService.aesCipher = aesCipher;
    }
}
