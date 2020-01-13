package ch.supsi.isin.accedy.xmppconversation;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Base64;

public class AES implements Encryption {
    private static final int IV_SIZE = 16;
    private Cipher cipher;

    public AES() {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    private SecretKeySpec getHashingKey(String myKey) {
        SecretKeySpec secretKey = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(myKey.getBytes("UTF-8"));
            byte[] keyBytes = new byte[16];
            System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
            secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return secretKey;
    }


    @Override
    public String encrypt(String message, String key) {
        // Generate Initial Values
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        // Hash key
        SecretKeySpec secretKeySpec = getHashingKey(key);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(message.getBytes("UTF-8"));
            // Combine IV and encrypted part.
            byte[] encryptedIVAndText = new byte[IV_SIZE + encrypted.length];
            System.arraycopy(iv, 0, encryptedIVAndText, 0, IV_SIZE);
            System.arraycopy(encrypted, 0, encryptedIVAndText, IV_SIZE, encrypted.length);
            return Base64.getEncoder().encodeToString(encryptedIVAndText);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        //return Base64.getEncoder().encodeToString(encryptedIVAndText);
        return null;
    }

    @Override
    public String decrypt(String encryptedMessageStr, String key) {
        byte[] encryptedMessage = Base64.getDecoder().decode(encryptedMessageStr);
        // Extract Initial Values
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(encryptedMessage, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        // Extract encrypted part
        int encryptedSize = encryptedMessage.length - IV_SIZE;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedMessage, IV_SIZE, encryptedBytes, 0, encryptedSize);
        // Hash key
        SecretKeySpec secretKeySpec = getHashingKey(key);
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[]  decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

