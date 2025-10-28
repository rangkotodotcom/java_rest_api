package com.rangkoto.rest_api.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rangkoto.rest_api.exception.CustomIllegalArgumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AESUtil {

    private final String cipherAlgorithm;
    private final byte[] defaultKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AESUtil(
            @Value("${aes.key}") String key,
            @Value("${aes.cipher:AES/CBC/PKCS5Padding}") String cipher
    ) throws Exception {
        if (key == null || key.isEmpty()) throw new CustomIllegalArgumentException("AES key is required");
        this.cipherAlgorithm = cipher;

        // Hash key ke 32 bytes (AES-256)
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        this.defaultKey = sha.digest(key.getBytes(StandardCharsets.UTF_8));
    }

    // ================= Enkripsi =================
    public String encrypt(Object data) throws Exception {
        return encrypt(data, defaultKey, null);
    }

    public String encrypt(Object data, byte[] key, byte[] iv) throws Exception {
        if (data == null) throw new CustomIllegalArgumentException("Input data is null");

        byte[] jsonBytes = objectMapper.writeValueAsBytes(data);

        byte[] ivBytes = iv != null ? iv : new byte[16];
        if (iv == null) new SecureRandom().nextBytes(ivBytes);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(jsonBytes);

        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
        String ivBase64 = Base64.getEncoder().encodeToString(ivBytes);

        return Base64.getEncoder().encodeToString((encryptedBase64 + "::" + ivBase64).getBytes(StandardCharsets.UTF_8));
    }

    // ================= Dekripsi dengan TypeReference =================
    public <T> T decrypt(String data, TypeReference<T> typeRef) throws Exception {
        return decrypt(data, defaultKey, typeRef);
    }

    public <T> T decrypt(String data, byte[] key, TypeReference<T> typeRef) throws Exception {
        if (data == null || data.isEmpty()) throw new CustomIllegalArgumentException("Encrypted data is null");

        // Decode outer Base64
        String decoded = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);

        if (!decoded.contains("::")) throw new CustomIllegalArgumentException("Invalid encrypted data format");
        String[] parts = decoded.split("::");

        byte[] encrypted = Base64.getDecoder().decode(parts[0]);
        byte[] iv = Base64.getDecoder().decode(parts[1]);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decrypted = cipher.doFinal(encrypted);

        return objectMapper.readValue(decrypted, typeRef);
    }

    // ================= Enkripsi String =================
    public String encryptString(String plainText) throws Exception {
        if (plainText == null) throw new CustomIllegalArgumentException("Input string is null");
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);

        SecretKeySpec keySpec = new SecretKeySpec(defaultKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
        String ivBase64 = Base64.getEncoder().encodeToString(ivBytes);

        return encryptedBase64 + "::" + ivBase64;
    }

    // ================= Dekripsi String =================
    public String decryptString(String encryptedText) throws Exception {
        if (encryptedText == null || !encryptedText.contains("::"))
            throw new CustomIllegalArgumentException("Invalid encrypted string format");

        String[] parts = encryptedText.split("::");
        byte[] encrypted = Base64.getDecoder().decode(parts[0]);
        byte[] iv = Base64.getDecoder().decode(parts[1]);

        SecretKeySpec keySpec = new SecretKeySpec(defaultKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

}
