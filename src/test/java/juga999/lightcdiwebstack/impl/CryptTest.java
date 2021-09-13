package juga999.lightcdiwebstack.impl;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class CryptTest {

    private static final Logger logger = LoggerFactory.getLogger(CryptTest.class);

    @Test
    public void hashPassword() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        md.update(salt);

        String password = "pwd";
        byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

        logger.info("Hashed password: " + getFormattedBytes(hashedPassword));
        logger.info("Salt: " + getFormattedBytes(salt));
    }

    private String getFormattedBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
