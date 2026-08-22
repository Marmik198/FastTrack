package com.karmayuva.pointsystem.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Transparently encrypts/decrypts a String entity field using AES-256-GCM before it
 * ever reaches the database, and decrypts it on the way back out. Applied explicitly
 * per-field via @Convert(converter = EncryptedStringConverter.class) - autoApply is
 * left false so it never silently encrypts a field we don't intend to (e.g. Kid.name,
 * which must remain searchable).
 *
 * Storage format: base64( 12-byte IV || ciphertext || 16-byte GCM tag )
 * A fresh random IV is generated for every single encryption, so the same plaintext
 * (e.g. two kids with the same phone number) never produces the same ciphertext.
 *
 * Registered as a Spring @Component so the key is injected the same way as everywhere
 * else in this app (via app.encryption.key / APP_ENCRYPTION_KEY). Spring Boot wires
 * Hibernate's bean container to the Spring context by default, so JPA resolves this
 * converter as a managed bean rather than instantiating it itself.
 */
@Component
@Converter(autoApply = false)
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptedStringConverter(@Value("${app.encryption.key}") String base64Key) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "app.encryption.key (APP_ENCRYPTION_KEY) must be valid Base64", e);
        }
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "app.encryption.key (APP_ENCRYPTION_KEY) must decode to exactly 32 bytes for AES-256. " +
                    "Generate one with: openssl rand -base64 32");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt field", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String storedValue) {
        if (storedValue == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(storedValue);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] cipherText = new byte[combined.length - IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(combined, IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plainBytes = cipher.doFinal(cipherText);

            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to decrypt field - the data may be corrupted, or APP_ENCRYPTION_KEY " +
                    "doesn't match the key it was encrypted with", e);
        }
    }
}
