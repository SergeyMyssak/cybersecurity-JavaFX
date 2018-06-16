package Service;

public class AesCipher {
    private byte[] initVector = {1, 9, 65, 13, 43, 74, 12, 45, 2, 34, 75, 13, 10, 78, 25, 23};
    private final String defaultSecretKey = "ItIsMyBigSecret!";
    private String secretKey;

    AesCipher() {
        secretKey = defaultSecretKey;
    }

    public byte[] getInitVector() {
        return initVector;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setDefaultSecretKey() {
        secretKey = defaultSecretKey;
    }

    public void setSecretKey(String secretKey) throws Exception {
        if (secretKey.length() != 16 && secretKey.length() != 24 && secretKey.length() != 32) {
            throw new Exception("Secret key's length must be 128, 192 or 256 bits");
        } else this.secretKey = secretKey;
    }
}
