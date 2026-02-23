package com.rangkoto.rest_api.common;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rangkoto.rest_api.exception.CustomIllegalArgumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AESHandler {
    private final String cipherAlgorithm;
    private final byte[] key;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AESHandler(
            @Value("${aes.key}") String key,
            @Value("${aes.cipher}") String cipher
    ) throws Exception {
        if (key == null || key.isEmpty()) throw new CustomIllegalArgumentException("AES key is required");

        if (cipher == null || cipher.isEmpty()) throw new CustomIllegalArgumentException("Cipher is not provided");

        this.cipherAlgorithm = convertCipherName(cipher.toLowerCase());

        int keySizeBits = extractKeySize(cipher);
        if (keySizeBits != 128 && keySizeBits != 192 && keySizeBits != 256) {
            throw new CustomIllegalArgumentException("Unsupported AES key size: " + keySizeBits);
        }

        int keySizeBytes = keySizeBits / 8;

        byte[] rawKey;
        if (key.startsWith("base64:")) {
            rawKey = Base64.getDecoder().decode(key.substring(7));
        } else {
            rawKey = key.getBytes(StandardCharsets.UTF_8);
        }

        this.key = normalizeKey(rawKey, keySizeBytes);
    }

    private String convertCipherName(String nodeCipher) {
        if (nodeCipher.contains("cbc")) {
            return "AES/CBC/PKCS5Padding";
        }
        throw new IllegalArgumentException("Unsupported cipher mode");
    }

    private int extractKeySize(String cipher) {
        String digits = cipher.replaceAll("\\D+", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private byte[] normalizeKey(byte[] rawKey, int requiredLength) {
        byte[] finalKey = new byte[requiredLength];
        System.arraycopy(rawKey, 0, finalKey, 0, Math.min(rawKey.length, requiredLength));
        return finalKey;
    }

    /**
     * Encrypt JSON-serializable object
     */
    public String encrypt(Object data) throws Exception {
        return encrypt(data, null, null);
    }

    public String encrypt(Object data, byte[] customKey, byte[] customIv) throws Exception {
        byte[] encryptionKey = (customKey != null) ? customKey : this.key;

        String jsonData = objectMapper.writeValueAsString(data);
        if (jsonData == null || jsonData.isEmpty()) {
            throw new IllegalArgumentException("Unable to encode data to JSON");
        }

        byte[] iv = (customIv != null) ? customIv : generateRandomIV();

        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"), new IvParameterSpec(iv));

        byte[] encryptedBytes = cipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        String ivBase64 = Base64.getEncoder().encodeToString(iv);

        String combined = encryptedBase64 + "::" + ivBase64;
        return Base64.getEncoder().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decrypt JSON data
     */
    public <T> T decrypt(String data, TypeReference<T> typeReference) throws Exception {
        return decrypt(data, typeReference, null);
    }

    public <T> T decrypt(String data, TypeReference<T> typeReference, byte[] customKey) throws Exception {
        byte[] decryptionKey = (customKey != null) ? customKey : this.key;

        String decoded = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
        String[] parts = decoded.split("::");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }

        String encryptedData = parts[0];
        String ivBase64 = parts[1];

        byte[] iv = Base64.getDecoder().decode(ivBase64);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptionKey, "AES"), new IvParameterSpec(iv));

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        String decryptedJson = new String(decryptedBytes, StandardCharsets.UTF_8);

        try {
            return objectMapper.readValue(decryptedJson, typeReference);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to decode decrypted data from JSON");
        }
    }

    private byte[] generateRandomIV() {
        byte[] iv = new byte[16]; // AES block size
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
