package com.essenciaviva.security;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class CryptoService {

    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12; // IV recomendado para GCM (96 bits)
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secretKeyBytes;
    private final byte[] hmacKeyBytes;

    public CryptoService() {
        // Carrega a chave das variáveis de ambiente (LGPD & Requisito do SENAI)
        String envKey = System.getenv("APP_ENCRYPTION_KEY");
        if (envKey == null || envKey.length() < 32) {
            // Em ambiente de testes/desenvolvimento, lança exceção se não configurado
            throw new IllegalStateException("A variável de ambiente APP_ENCRYPTION_KEY deve ser configurada com no mínimo 32 caracteres!");
        }
        this.secretKeyBytes = envKey.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        this.hmacKeyBytes = (envKey + "_HMAC_SALT").getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Criptografa um dado sensível utilizando AES-256-GCM com IV aleatório para cada chamada.
     */
    public byte[] encrypt(String text) throws Exception {
        if (text == null || text.isBlank()) return null;

        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv); // Vetor de inicialização (IV) único

        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        SecretKey key = new SecretKeySpec(secretKeyBytes, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] cipherText = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

        // Empacota IV + Texto Cifrado em um único array de bytes
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

        return combined;
    }

    /**
     * Descriptografa os dados extraindo o IV dos primeiros 12 bytes.
     */
    public String decrypt(byte[] combined) throws Exception {
        if (combined == null || combined.length <= IV_LENGTH_BYTES) return null;

        byte[] iv = new byte[IV_LENGTH_BYTES];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);

        byte[] cipherText = new byte[combined.length - IV_LENGTH_BYTES];
        System.arraycopy(combined, IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        SecretKey key = new SecretKeySpec(secretKeyBytes, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    /**
     * Gera um Hash HMAC-SHA256 do CPF (apenas números) para permitir busca rápida e unicidade sem descriptografar a tabela inteira.
     */
    public String calculateCPFHash(String cpf) throws Exception {
        if (cpf == null) return null;
        String digitsOnly = cpf.replaceAll("\\D", ""); // Mantém apenas dígitos
        
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(hmacKeyBytes, HMAC_ALGORITHM);
        mac.init(keySpec);
        
        byte[] hmacBytes = mac.doFinal(digitsOnly.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hmacBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}