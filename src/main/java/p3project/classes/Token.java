package p3project.classes;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;

public class Token {

    @Value("${token.secret}")
    private String SECRET;
    private String payload;
    private String hash;

    private Token() {};

    public static Token sign(String id) {
        Token token = new Token();
        token.payload = id;
        token.hash = token.hash(token.SECRET, token.payload);
        return token;
    }

    public boolean verify(String inputHash) {
        String computedHash = hash(this.SECRET, this.payload);
        return computedHash.equals(inputHash);
    }

    // SHA256 implementation stjålet fra: https://medium.com/@AlexanderObregon/what-is-sha-256-hashing-in-java-0d46dfb83888
    private String hash(String secret, String payload) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String input = secret + payload;
            byte[] encodedHash = sha256.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new RuntimeException("Error getting SHA256 algorithm: " + error);
        }
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
