package com.posdb.sync.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

@ApplicationScoped
public class RandomKeyGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomKeyGenerator.class);
    private static final String API_KEY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private final SecureRandom random = new SecureRandom();

    public String generateApiKey() {
        return generateRandomString(32, API_KEY_CHARS);
    }

    public String generatePassword() {
        return generateRandomString(8, PASSWORD_CHARS);
    }

    private String generateRandomString(int length, String charset) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(random.nextInt(charset.length())));
        }
        LOGGER.debug("Generated random string of length {}", length);
        return sb.toString();
    }
}

