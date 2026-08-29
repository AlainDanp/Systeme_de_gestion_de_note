package MVC;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String hashPassword(String password) {
        if (password == null) throw new IllegalArgumentException("Mot de passe requis");
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String password, String stocke) {
        if (password == null || stocke == null) return false;

        // Compatibilité : anciens hash SHA-256 nus (migration, voir plus bas)
        if (!stocke.startsWith("pbkdf2$")) {
            return MessageDigest.isEqual(
                    legacySha256(password).getBytes(StandardCharsets.UTF_8),
                    stocke.getBytes(StandardCharsets.UTF_8));
        }

        String[] parts = stocke.split("\\$");
        if (parts.length != 4) return false;
        try {
            int    iterations = Integer.parseInt(parts[1]);
            byte[] salt       = Base64.getDecoder().decode(parts[2]);
            byte[] attendu    = Base64.getDecoder().decode(parts[3]);
            byte[] calcule    = pbkdf2(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(attendu, calcule);   // comparaison à temps constant
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public static boolean besoinDeMigration(String stocke) {
        return stocke != null && !stocke.startsWith("pbkdf2$");
    }


    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return sb.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;
        boolean maj = false, min = false, chiffre = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) maj = true;
            else if (Character.isLowerCase(c)) min = true;
            else if (Character.isDigit(c)) chiffre = true;
        }
        return maj && min && chiffre;
    }
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Erreur de hachage du mot de passe", e);
        }
    }

    private static String legacySha256(String password) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }


}
